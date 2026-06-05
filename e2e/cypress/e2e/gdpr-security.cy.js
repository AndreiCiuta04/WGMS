const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function uniqueUser(prefix, firstName = 'Gdpr', lastName = 'Security') {
  return {
    firstName,
    lastName,
    email: uniqueEmail(prefix),
    password
  };
}

function authHeaderFromLocalStorage() {
  return cy.window().then(win => `Bearer ${win.localStorage.getItem('authToken')}`);
}

function currentHouseholdId() {
  return authHeaderFromLocalStorage().then(authHeader => cy.request({
    method: 'GET',
    url: `${apiUrl}/households/me`,
    headers: { Authorization: authHeader }
  }).then(response => response.body.id));
}

describe('GDPR security matrix', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('blocks unauthenticated protected routes and protected API access after logout', () => {
    cy.visit('/#/profile');
    cy.location('hash').should('include', '/login');

    cy.visit('/#/tasks');
    cy.location('hash').should('include', '/login');

    cy.visit('/#/expenses');
    cy.location('hash').should('include', '/login');

    const user = uniqueUser('gdpr.security.logout');
    cy.registerUser(user);
    cy.window().its('localStorage.authToken').should('exist');
    cy.logoutUser();
    cy.window().its('localStorage.authToken').should('not.exist');

    cy.request({
      method: 'GET',
      url: `${apiUrl}/users/me`,
      failOnStatusCode: false
    }).its('status').should('be.oneOf', [401, 403]);
  });

  it('prevents roommates from performing Main Tenant invitation actions', () => {
    const owner = uniqueUser('gdpr.security.owner', 'Main', 'Tenant');
    const roommate = uniqueUser('gdpr.security.roommate', 'Room', 'Mate');
    const household = {
      name: `GDPR Security WG ${Date.now()}`,
      address: 'Security Street 1'
    };
    let householdId;

    cy.registerUser(owner);
    cy.createHousehold(household);
    currentHouseholdId().then(id => {
      householdId = id;
      cy.createInvitation({ invitedEmail: roommate.email }).then(inviteUrl => {
        cy.logoutUser();
        cy.visitInviteUrl(inviteUrl);
        cy.get('input[formcontrolname="firstName"]').clear().type(roommate.firstName);
        cy.get('input[formcontrolname="lastName"]').clear().type(roommate.lastName);
        cy.get('input[formcontrolname="password"]').clear().type(roommate.password);
        cy.get('input[formcontrolname="termsAccepted"]').check();
        cy.contains('button', 'Register & Join WG').click();
        cy.contains('You have successfully joined the household!').should('be.visible');

        cy.visit('/#/household/invitations');
        cy.contains('Access Denied').should('be.visible');
        cy.contains('Create New Invitation').should('not.exist');

        authHeaderFromLocalStorage().then(roommateAuthHeader => {
          cy.request({
            method: 'POST',
            url: `${apiUrl}/households/${householdId}/invitations`,
            headers: { Authorization: roommateAuthHeader },
            failOnStatusCode: false,
            body: {
              invitedEmail: uniqueEmail('gdpr.security.forbidden'),
              expiresInHours: 24
            }
          }).its('status').should('eq', 403);
        });
      });
    });
  });

  it('prevents a user from WG B accessing WG A data by changing IDs', () => {
    const userA = uniqueUser('gdpr.security.wga', 'Wg', 'A');
    const userB = uniqueUser('gdpr.security.wgb', 'Wg', 'B');
    let householdAId;

    cy.registerUser(userA);
    cy.createHousehold({ name: `GDPR WG A ${Date.now()}`, address: 'A Street' });
    currentHouseholdId().then(id => {
      householdAId = id;
      cy.logoutUser();

      cy.registerUser(userB);
      cy.createHousehold({ name: `GDPR WG B ${Date.now()}`, address: 'B Street' });
      authHeaderFromLocalStorage().then(userBAuthHeader => {
        cy.request({
          method: 'GET',
          url: `${apiUrl}/households/${householdAId}/members`,
          headers: { Authorization: userBAuthHeader },
          failOnStatusCode: false
        }).its('status').should('eq', 403);
      });
    });
  });

  it('rejects reuse of a single-use invitation link', () => {
    const owner = uniqueUser('gdpr.security.invite.owner', 'Invite', 'Owner');
    const invited = uniqueUser('gdpr.security.invite.first', 'Invite', 'First');
    const secondUser = uniqueUser('gdpr.security.invite.second', 'Invite', 'Second');

    cy.registerUser(owner);
    cy.createHousehold({ name: `GDPR Single Use WG ${Date.now()}`, address: 'Invite Street' });
    cy.createInvitation({ invitedEmail: invited.email }).then(inviteUrl => {
      const token = String(inviteUrl).substring(String(inviteUrl).lastIndexOf('/') + 1);

      cy.logoutUser();
      cy.visitInviteUrl(inviteUrl);
      cy.get('input[formcontrolname="firstName"]').clear().type(invited.firstName);
      cy.get('input[formcontrolname="lastName"]').clear().type(invited.lastName);
      cy.get('input[formcontrolname="password"]').clear().type(invited.password);
      cy.get('input[formcontrolname="termsAccepted"]').check();
      cy.contains('button', 'Register & Join WG').click();
      cy.contains('You have successfully joined the household!').should('be.visible');

      cy.logoutUser();
      cy.registerUser(secondUser);
      authHeaderFromLocalStorage().then(secondAuthHeader => {
        cy.request({
          method: 'POST',
          url: `${apiUrl}/invitations/${token}/accept`,
          headers: { Authorization: secondAuthHeader },
          failOnStatusCode: false
        }).its('status').should('eq', 410);
      });
    });
  });
});
