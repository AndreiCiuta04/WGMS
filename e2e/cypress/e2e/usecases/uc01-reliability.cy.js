const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function authHeaderFromLocalStorage() {
  return cy.window().then(win => `Bearer ${win.localStorage.getItem('authToken')}`);
}

function myHousehold() {
  return authHeaderFromLocalStorage().then((authHeader) =>
    cy.request({
      method: 'GET',
      url: `${apiUrl}/households/me`,
      headers: { Authorization: authHeader },
      failOnStatusCode: false
    })
  );
}

function registerViaApi(user) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/users`,
    body: {
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      password: user.password,
      termsAccepted: true
    }
  }).then((res) => {
    expect(res.status).to.be.oneOf([200, 201]);
    return res.body; // raw JWT string
  });
}

// UC-01 / E2E-016 — double submit / reload safety
// Double-clicking a create action and reloading after a mutation must not create duplicate state.
describe('UC-01 double submit and reload safety', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('keeps exactly one WG after a double-click create and a reload', () => {
    const runId = Date.now();
    const user = {
      firstName: 'Double',
      lastName: 'Submit',
      email: uniqueEmail('double.submit'),
      password
    };

    cy.registerUser(user);

    cy.intercept('POST', '**/api/v1/households').as('createWg');
    cy.visit('/#/household/create');
    cy.get('#name').clear().type(`Double Submit WG ${runId}`);

    // Fire two clicks on the still-attached button before Angular navigates away on success.
    cy.contains('button', 'Initialize WG').dblclick();

    cy.wait('@createWg').its('response.statusCode').should('eq', 201);
    cy.location('hash', { timeout: 10000 }).should('eq', '#/');

    // Reloading the post-mutation page must not create a second WG.
    cy.reload();

    myHousehold().then((res) => {
      expect(res.status, 'user is in exactly one WG').to.eq(200);
    });

    // The WG has exactly one member (the creator) — no duplicate membership from the double click.
    myHousehold().then((res) => {
      authHeaderFromLocalStorage().then((authHeader) => {
        cy.request({
          method: 'GET',
          url: `${apiUrl}/households/${res.body.id}/members`,
          headers: { Authorization: authHeader }
        }).its('body').should('have.length', 1);
      });
    });
  });
});

// UC-01 / E2E-017 — multi-session / concurrent invitation acceptance
// The same user firing two simultaneous accepts of one invite must never create a duplicate
// membership (race-condition safety); the WG ends with exactly one new member.
describe('UC-01 concurrent invitation acceptance is race-safe', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('never creates a duplicate membership when one user double-accepts in parallel', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'Concurrent',
      lastName: 'Owner',
      email: uniqueEmail('concurrent.owner'),
      password
    };
    const joiner = { firstName: 'Conc', lastName: 'Joiner', email: uniqueEmail('concurrent.joiner'), password };

    cy.registerUser(owner);
    cy.createHousehold({ name: `Concurrent WG ${runId}`, address: 'Concurrent St 1' });

    authHeaderFromLocalStorage().then((ownerAuth) => {
      myHousehold().then((hh) => {
        const householdId = hh.body.id;
        cy.request({
          method: 'POST',
          url: `${apiUrl}/households/${householdId}/invitations`,
          headers: { Authorization: ownerAuth },
          body: { invitedEmail: joiner.email, expiresInHours: 168 }
        }).then((invRes) => {
          const url = String(invRes.body.inviteUrl);
          const token = url.substring(url.lastIndexOf('/') + 1);

          registerViaApi(joiner).then((joinerToken) => {
            // Same user fires two accepts of the same token at the same time.
            cy.then(() => {
              const accept = () => fetch(`${apiUrl}/invitations/${token}/accept`, {
                method: 'POST',
                headers: { Authorization: `Bearer ${joinerToken}` }
              }).then((r) => r.status);
              return Cypress.Promise.all([accept(), accept()]);
            }).then((statuses) => {
              const ok = statuses.filter((s) => s === 200).length;
              expect(ok, 'at least one accept succeeds').to.be.greaterThan(0);
            });

            // Invariant: the joiner appears exactly once — no duplicate row from the race.
            cy.request({
              method: 'GET',
              url: `${apiUrl}/households/${householdId}/members`,
              headers: { Authorization: ownerAuth }
            }).then((membersRes) => {
              expect(membersRes.body, 'owner + joiner, joiner not duplicated').to.have.length(2);
              const joinerRows = membersRes.body.filter((m) => m.email === joiner.email);
              expect(joinerRows, 'joiner listed exactly once').to.have.length(1);
            });
          });
        });
      });
    });
  });
});

// UC-01 / E2E-018 — failed request (interruption / unavailability) and recovery
// A failed request during a core mutation shows a safe error and the user can retry successfully
// without reloading or losing their place.
describe('UC-01 a failed request is handled and recoverable', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('shows a safe error on a failed create request, then succeeds on retry', () => {
    const runId = Date.now();
    const user = {
      firstName: 'Network',
      lastName: 'Drop',
      email: uniqueEmail('network.drop'),
      password
    };

    cy.registerUser(user);

    // First create attempt fails (service unavailable); every later attempt reaches the server.
    let attempts = 0;
    cy.intercept('POST', '**/api/v1/households', (req) => {
      attempts += 1;
      if (attempts === 1) {
        req.reply({ statusCode: 503, body: { message: 'Service temporarily unavailable' } });
      }
    }).as('createWg');

    cy.visit('/#/household/create');
    cy.get('#name').clear().type(`Network WG ${runId}`);

    cy.contains('button', 'Initialize WG').click();
    cy.contains('Something went wrong', { timeout: 10000 }).should('be.visible');
    cy.location('hash').should('eq', '#/household/create');

    // Retry the same form without reloading — the user recovers.
    cy.contains('button', 'Initialize WG').click();
    cy.location('hash', { timeout: 10000 }).should('eq', '#/');

    myHousehold().its('status').should('eq', 200);
  });
});

// UC-01 / E2E-019 — server error behavior is safe
// A mocked 500 on a core action shows a friendly message and never leaks a stack trace / raw HTML.
describe('UC-01 server error behavior is safe', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('shows a safe generic error (no stack trace) when invitation creation returns 500', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'Error',
      lastName: 'Owner',
      email: uniqueEmail('error.owner'),
      password
    };

    cy.registerUser(owner);
    cy.createHousehold({ name: `Error WG ${runId}`, address: 'Error St 1' });

    cy.intercept('POST', '**/api/v1/households/*/invitations', {
      statusCode: 500,
      body: { message: 'Internal Server Error' }
    }).as('invite500');

    cy.visit('/#/household/invitations');
    cy.get('#email').clear().type(uniqueEmail('error.invitee'));
    cy.contains('button', 'Create').click();

    cy.wait('@invite500');
    cy.contains('Could not create invitation.').should('be.visible');
    cy.get('body').should('not.contain', 'Exception');
    cy.get('body').should('not.contain', 'at java.');
    cy.get('body').should('not.contain', 'Whitelabel Error Page');
  });
});

// UC-01 / E2E-037 — out-of-scope workflow claims are absent
// The proposal states invitations are NOT auto-emailed and email password reset is out of scope.
// The UI must not promise any of these unimplemented email workflows.
describe('UC-01 no out-of-scope email workflow is promised', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  const forbidden = /we (sent|have sent|emailed)|check your (e-?mail|inbox)|reset (your )?password|password reset link|verification e-?mail sent|sent you an e-?mail/i;

  it('register and login pages do not promise email-sending or password-reset flows', () => {
    cy.visit('/#/register');
    cy.get('body').invoke('text').should((text) => {
      expect(forbidden.test(text), `register page must not promise email flows: ${text.match(forbidden)}`).to.be.false;
    });

    cy.visit('/#/login');
    cy.get('body').invoke('text').should((text) => {
      expect(forbidden.test(text), `login page must not promise email flows: ${text.match(forbidden)}`).to.be.false;
    });
  });

  it('invitation page presents a copyable link and never claims an invite email was sent', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'Scope',
      lastName: 'Owner',
      email: uniqueEmail('scope.owner'),
      password
    };

    cy.registerUser(owner);
    cy.createHousehold({ name: `Scope WG ${runId}`, address: 'Scope St 1' });
    cy.createInvitation({ invitedEmail: uniqueEmail('scope.invitee') });

    // The owner must share the link themselves — confirm the link UI exists and no email-sent claim.
    cy.get('input[readonly]').should('exist');
    cy.get('body').invoke('text').should((text) => {
      expect(forbidden.test(text), `invitation page must not claim an email was sent: ${text.match(forbidden)}`).to.be.false;
    });
  });
});
