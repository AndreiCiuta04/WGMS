const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

// Builds a structurally valid but expired JWT (decodable header/payload, past exp claim) so the
// frontend AuthService treats the session as expired and the route guard redirects to login.
function expiredJwt(email) {
  const enc = (obj) => btoa(JSON.stringify(obj))
    .replace(/=/g, '')
    .replace(/\+/g, '-')
    .replace(/\//g, '_');
  const now = Math.floor(Date.now() / 1000);
  const header = enc({ alg: 'HS256', typ: 'JWT' });
  const payload = enc({ sub: email, iat: now - 7200, exp: now - 3600 });
  return `${header}.${payload}.expired-signature`;
}

// UC-01 / E2E-009 — invitation locked to a specific email
// When an invitation is bound to one email and a different account opens it while logged in,
// the UI must warn about the mismatch and disable the accept button (the backend would reject it).
describe('UC-01 invitation email mismatch is blocked in the UI', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('warns and disables accept when the logged-in user is not the invited email', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'Mismatch',
      lastName: 'Owner',
      email: uniqueEmail('mismatch.owner'),
      password
    };
    const otherUser = {
      firstName: 'Mismatch',
      lastName: 'Other',
      email: uniqueEmail('mismatch.other'),
      password
    };
    const invitedEmail = uniqueEmail('mismatch.invited');

    // The "other" account exists but is not the invited email.
    cy.registerUser(otherUser);
    cy.logoutUser();

    // Owner creates a WG and invites a different, specific email.
    cy.registerUser(owner);
    cy.createHousehold({ name: `Mismatch Household ${runId}`, address: 'Mismatch St 1' });
    cy.createInvitation({ invitedEmail }).then((inviteUrl) => {
      cy.logoutUser();

      // The other user logs in and opens the invitation meant for someone else.
      cy.loginUser(otherUser.email, otherUser.password);
      cy.visitInviteUrl(inviteUrl);

      cy.contains('Join a Household').should('be.visible');
      cy.contains('This invitation is specifically for').should('be.visible');
      cy.contains(invitedEmail).should('be.visible');
      cy.contains('button', 'Accept and Enter WG').should('be.disabled');
    });
  });
});

// UC-01 / E2E-004 — expired session UI recovery
// A stale/expired token in localStorage must not grant access: the guard redirects a protected
// route to login, and a fresh login recovers the session and reaches the dashboard.
describe('UC-01 expired session recovers via re-login', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('redirects a protected route to login on expired token, then recovers after login', () => {
    const user = {
      firstName: 'Expired',
      lastName: 'Session',
      email: uniqueEmail('expired.session'),
      password
    };

    // Establish a real account, then simulate the session having expired.
    cy.registerUser(user);
    cy.logoutUser();

    cy.visit('/#/login');
    cy.window().then((win) => {
      win.localStorage.setItem('authToken', expiredJwt(user.email));
    });

    // Protected route is denied and bounced to login.
    cy.visit('/#/household/members');
    cy.location('hash', { timeout: 10000 }).should('include', '#/login');

    // Re-login recovers the session and routes back to the dashboard.
    cy.loginUser(user.email, user.password);
    cy.location('hash').should('eq', '#/');
    cy.visit('/#/household/members');
    cy.contains(/WG|household|member|create/i, { timeout: 10000 }).should('be.visible');
  });
});

// UC-01 / E2E-011 — already-active member cannot duplicate membership
describe('UC-01 active member cannot accept another invitation', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('rejects a logged-in active roommate who tries to accept a second invite to the same WG', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'Active',
      lastName: 'Owner',
      email: uniqueEmail('active.owner'),
      password
    };
    const roommate = {
      firstName: 'Active',
      lastName: 'Roommate',
      email: uniqueEmail('active.roommate'),
      password
    };
    const household = {
      name: `Active Member WG ${runId}`,
      address: 'Active St 1'
    };

    cy.registerUser(owner);
    cy.createHousehold(household);
    cy.createInvitation({ invitedEmail: roommate.email }).then((firstInvite) => {
      cy.logoutUser();
      cy.visitInviteUrl(firstInvite);
      cy.get('input[formcontrolname="firstName"]').clear().type(roommate.firstName);
      cy.get('input[formcontrolname="lastName"]').clear().type(roommate.lastName);
      cy.get('input[formcontrolname="password"]').clear().type(roommate.password);
      cy.get('input[formcontrolname="termsAccepted"]').check();
      cy.contains('button', 'Register & Join WG').click();
      cy.contains('You have successfully joined the household!').should('be.visible');
      cy.location('hash', { timeout: 5000 }).should('eq', '#/');

      cy.logoutUser();
      cy.loginUser(owner.email, owner.password);
      cy.createInvitation({ invitedEmail: roommate.email }).then((secondInvite) => {
        cy.logoutUser();
        cy.loginUser(roommate.email, roommate.password);
        cy.visitInviteUrl(secondInvite);
        cy.contains('button', 'Accept and Enter WG').should('be.visible').click();
        cy.contains(/already belongs|could not accept|conflict/i, { timeout: 10000 }).should('be.visible');
      });
    });
  });
});
