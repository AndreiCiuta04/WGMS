const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function authHeaderFromLocalStorage() {
  return cy.window().then(win => `Bearer ${win.localStorage.getItem('authToken')}`);
}

// Accepts an invitation through the logged-in UI path (the "Accept and Enter WG" button).
function acceptInviteWhileLoggedIn(inviteUrl) {
  cy.visitInviteUrl(inviteUrl);
  cy.contains('Join a Household').should('be.visible');
  cy.contains('button', 'Accept and Enter WG').should('be.visible').and('not.be.disabled').click();
  cy.contains('You have successfully joined the household!').should('be.visible');
}

// UC-01 / E2E-010 — BUG #35
// An already-registered account (created earlier, not in the target WG) must be able to
// accept an invitation while logged in without a duplicate account, duplicate membership,
// or 500. The user ends up exactly once as a ROOMMATE in the inviting household.
describe('UC-01 BUG #35 existing user accepts invitation while logged in', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('lets an existing account join via a logged-in invitation accept without duplicates or 500', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'Bug35',
      lastName: 'Owner',
      email: uniqueEmail('bug35.owner'),
      password
    };
    const existing = {
      firstName: 'Bug35',
      lastName: 'Existing',
      email: uniqueEmail('bug35.existing'),
      password
    };
    const household = {
      name: `BUG35 Household ${runId}`,
      address: 'Bug35 Street 1'
    };

    // 1. The "existing" user already has an account (registered earlier, no WG).
    cy.registerUser(existing);
    cy.window().its('localStorage.authToken').should('exist');
    cy.logoutUser();

    // 2. Owner registers, creates the WG and invites the existing account's email.
    cy.registerUser(owner);
    cy.createHousehold(household);
    cy.createInvitation({ invitedEmail: existing.email }).then((inviteUrl) => {
      cy.logoutUser();

      // 3. Existing user logs in and accepts the invitation through the logged-in UI.
      cy.loginUser(existing.email, existing.password);
      cy.intercept('POST', '**/api/v1/invitations/*/accept').as('acceptInvitation');
      acceptInviteWhileLoggedIn(inviteUrl);
      cy.wait('@acceptInvitation').its('response.statusCode').should('eq', 200);

      // 4. The existing user appears exactly once as ROOMMATE in the household.
      cy.visit('/#/household/members');
      cy.contains('h1', household.name).should('be.visible');
      cy.contains(existing.email).should('be.visible');
      cy.get('body').then(($body) => {
        const occurrences = $body.text().split(existing.email).length - 1;
        expect(occurrences, 'existing member listed once').to.eq(1);
      });
      cy.contains('ROOMMATE').should('be.visible');

      // 5. No duplicate account was created for that email (registration now conflicts).
      cy.request({
        method: 'POST',
        url: `${apiUrl}/users`,
        failOnStatusCode: false,
        body: {
          firstName: existing.firstName,
          lastName: existing.lastName,
          email: existing.email,
          password: existing.password,
          termsAccepted: true
        }
      }).its('status').should('eq', 409);

      // 6. Owner still sees both members from their own session.
      cy.logoutUser();
      cy.loginUser(owner.email, owner.password);
      cy.visit('/#/household/members');
      cy.contains('h1', household.name).should('be.visible');
      cy.contains(owner.email).should('be.visible');
      cy.contains(existing.email).should('be.visible');

      authHeaderFromLocalStorage().then((ownerAuth) => {
        cy.request({
          method: 'GET',
          url: `${apiUrl}/households/me`,
          headers: { Authorization: ownerAuth }
        }).then((res) => {
          cy.request({
            method: 'GET',
            url: `${apiUrl}/households/${res.body.id}/members`,
            headers: { Authorization: ownerAuth }
          }).then((membersRes) => {
            const matches = membersRes.body.filter((m) => m.email === existing.email);
            expect(matches.length, 'one active membership for existing user').to.eq(1);
          });
        });
      });
    });
  });
});
