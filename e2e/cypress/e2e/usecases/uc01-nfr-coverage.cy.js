const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function authHeaderFromLocalStorage() {
  return cy.window().then(win => `Bearer ${win.localStorage.getItem('authToken')}`);
}

// Creates an invitation directly through the API so the test can control the expiry window
// (e.g. a negative value yields an already-expired invite) and capture the raw token/id.
function createInvitationApi(invitedEmail, expiresInHours = 168) {
  return authHeaderFromLocalStorage().then((authHeader) =>
    cy.request({
      method: 'GET',
      url: `${apiUrl}/households/me`,
      headers: { Authorization: authHeader }
    }).then((householdRes) =>
      cy.request({
        method: 'POST',
        url: `${apiUrl}/households/${householdRes.body.id}/invitations`,
        headers: { Authorization: authHeader },
        body: { invitedEmail, expiresInHours }
      }).then((invRes) => {
        expect(invRes.status).to.eq(201);
        const url = String(invRes.body.inviteUrl);
        return { id: invRes.body.id, token: url.substring(url.lastIndexOf('/') + 1), url };
      })
    )
  );
}

function revokeInvitationApi(id) {
  return authHeaderFromLocalStorage().then((authHeader) =>
    cy.request({
      method: 'DELETE',
      url: `${apiUrl}/invitations/${id}`,
      headers: { Authorization: authHeader },
      failOnStatusCode: false
    }).its('status').should('be.oneOf', [200, 204])
  );
}

function expectInviteInvalidUi(token) {
  cy.clearLocalStorage();
  cy.visit(`/#/invite/${token}`);
  cy.contains(/no longer valid|invalid|expired|could not|already been used/i, { timeout: 10000 })
    .should('be.visible');
}

// UC-01 / E2E-008 — invitation state matrix (UI side)
// expired, revoked and already-used invitation tokens must all surface a safe "no longer valid"
// message on the public invite page and must never grant membership.
describe('UC-01 invitation state matrix surfaces safe UI for bad tokens', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('shows a safe error for expired, revoked, and used invitation tokens', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'State',
      lastName: 'Owner',
      email: uniqueEmail('state.owner'),
      password
    };
    const usedUser = {
      firstName: 'State',
      lastName: 'Used',
      email: uniqueEmail('state.used'),
      password
    };

    cy.registerUser(owner);
    cy.createHousehold({ name: `State Matrix WG ${runId}`, address: 'State St 1' });

    // Expired: negative expiry window yields an invitation already past expiresAt.
    createInvitationApi(`expired.${runId}@test.com`, -1).then((expired) => {
      expectInviteInvalidUi(expired.token);

      // Revoked: create a valid invite, then delete it.
      cy.loginUser(owner.email, owner.password);
      createInvitationApi(`revoked.${runId}@test.com`, 24).then((revoked) => {
        revokeInvitationApi(revoked.id);
        expectInviteInvalidUi(revoked.token);

        // Used: a fresh invite consumed by a real join, then reopened.
        cy.loginUser(owner.email, owner.password);
        createInvitationApi(usedUser.email, 168).then((used) => {
          cy.clearLocalStorage();
          cy.visit(`/#/invite/${used.token}`);
          cy.get('input[formcontrolname="firstName"]').clear().type(usedUser.firstName);
          cy.get('input[formcontrolname="lastName"]').clear().type(usedUser.lastName);
          cy.get('input[formcontrolname="password"]').clear().type(usedUser.password);
          cy.get('input[formcontrolname="termsAccepted"]').check();
          cy.contains('button', 'Register & Join WG').click();
          cy.contains('You have successfully joined the household!').should('be.visible');
          cy.location('hash', { timeout: 5000 }).should('eq', '#/');

          expectInviteInvalidUi(used.token);
        });
      });
    });
  });
});

// UC-01 / E2E-036 — browser/session cache isolation
// After switching accounts in the same browser, a user with no WG must never see the previous
// user's household. Verified via the app's own /households/me response per session.
describe('UC-01 session cache isolation across user switches', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('does not leak a previous user\'s household to a different account in the same browser', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'Cache',
      lastName: 'Owner',
      email: uniqueEmail('cache.owner'),
      password
    };
    const outsider = {
      firstName: 'Cache',
      lastName: 'Outsider',
      email: uniqueEmail('cache.outsider'),
      password
    };

    // Outsider registers and never creates/joins a WG.
    cy.registerUser(outsider);
    myHouseholdStatus().then((status) => expect(status, 'outsider has no WG initially').to.eq(404));
    cy.logoutUser();

    // Owner creates a WG.
    cy.registerUser(owner);
    cy.createHousehold({ name: `Cache WG ${runId}`, address: 'Cache St 1' });
    myHouseholdStatus().then((status) => expect(status, 'owner has a WG').to.eq(200));
    cy.logoutUser();

    // Outsider logs back in: must still have no WG, and the members page must not show owner data.
    cy.loginUser(outsider.email, outsider.password);
    myHouseholdStatus().then((status) => expect(status, 'outsider still has no WG after switch').to.eq(404));
    cy.visit('/#/household/members');
    cy.contains(owner.email).should('not.exist');
    cy.contains(`Cache WG ${runId}`).should('not.exist');
  });
});

function myHouseholdStatus() {
  return authHeaderFromLocalStorage().then((authHeader) =>
    cy.request({
      method: 'GET',
      url: `${apiUrl}/households/me`,
      headers: { Authorization: authHeader },
      failOnStatusCode: false
    }).its('status')
  );
}

// UC-01 / E2E-034 — 360px boundary usability
// Primary onboarding action stays reachable and the landing page does not force horizontal scroll
// at the 360px reference width.
describe('UC-01 360px viewport primary-action smoke', () => {
  it('keeps Get Started reachable without horizontal overflow at 360px', () => {
    cy.viewport(360, 740);
    cy.clearLocalStorage();
    cy.visit('/#/');

    cy.contains('a', 'Get Started').should('be.visible');
    cy.contains('a', 'Login').should('be.visible');

    cy.document().then((doc) => {
      const el = doc.documentElement;
      // Allow 1px rounding slack; anything larger means content forces horizontal scrolling.
      expect(el.scrollWidth, 'no horizontal overflow at 360px').to.be.at.most(el.clientWidth + 1);
    });
  });
});

// UC-01 / E2E-020 — no secret/PII leakage in API response bodies
// Capture every UC-01 API response during register + login + invite create and assert the raw
// plaintext password never appears in any response body.
describe('UC-01 no secret leakage in API responses', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('never echoes the plaintext password in any UC-01 API response body', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'Leak',
      lastName: 'Owner',
      email: uniqueEmail('leak.owner'),
      password
    };
    const leaks = [];

    cy.intercept({ url: '**/api/v1/**' }, (req) => {
      req.continue((res) => {
        const body = typeof res.body === 'string' ? res.body : JSON.stringify(res.body || '');
        if (body.includes(password)) {
          leaks.push(req.url);
        }
      });
    }).as('apiCalls');

    cy.registerUser(owner);
    cy.createHousehold({ name: `Leak WG ${runId}`, address: 'Leak St 1' });
    cy.createInvitation({ invitedEmail: uniqueEmail('leak.invitee') });

    cy.then(() => {
      expect(leaks, `plaintext password leaked in: ${leaks.join(', ')}`).to.have.length(0);
    });
  });
});
