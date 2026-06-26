const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function authHeaderFromLocalStorage() {
  return cy.window().then(win => `Bearer ${win.localStorage.getItem('authToken')}`);
}

// New-user invitation accept (registration form on the invite page).
function registerAndJoinViaInvite(inviteUrl, user) {
  cy.visitInviteUrl(inviteUrl);
  cy.contains('Join a Household').should('be.visible');
  cy.get('input[formcontrolname="firstName"]').clear().type(user.firstName);
  cy.get('input[formcontrolname="lastName"]').clear().type(user.lastName);
  cy.get('input[formcontrolname="password"]').clear().type(user.password);
  cy.get('input[formcontrolname="termsAccepted"]').check();
  cy.contains('button', 'Register & Join WG').click();
  cy.contains('You have successfully joined the household!').should('be.visible');
  // invitation-accept schedules a redirect to / after 2s; wait for it so it cannot fire later.
  cy.location('hash', { timeout: 5000 }).should('eq', '#/');
}

// Creates a fresh invitation through the API (used for the rejoin invite after leave).
function createInvitationApi(invitedEmail) {
  return authHeaderFromLocalStorage().then((authHeader) => {
    return cy.request({
      method: 'GET',
      url: `${apiUrl}/households/me`,
      headers: { Authorization: authHeader }
    }).then((householdRes) => {
      return cy.request({
        method: 'POST',
        url: `${apiUrl}/households/${householdRes.body.id}/invitations`,
        headers: { Authorization: authHeader },
        body: { invitedEmail, expiresInHours: 168 }
      }).then((invRes) => {
        expect(invRes.status).to.eq(201);
        expect(invRes.body.inviteUrl).to.be.a('string').and.not.be.empty;
        return invRes.body.inviteUrl;
      });
    });
  });
}

// Logged-in invitation accept ("Accept and Enter WG" button).
function acceptInviteWhileLoggedIn(inviteUrl) {
  cy.visitInviteUrl(inviteUrl);
  cy.contains('Join a Household').should('be.visible');
  cy.contains('button', 'Accept and Enter WG').should('be.visible').and('not.be.disabled').click();
  cy.contains('You have successfully joined the household!').should('be.visible');
}

// Leaves the current WG through the profile-edit UI (with the shared confirm dialog).
function leaveCurrentWgViaProfile() {
  cy.visit('/#/profile/edit');
  cy.contains('button', 'Leave current WG').should('be.visible').click();
  cy.confirmDialog();
  cy.contains('You left your current WG.').should('be.visible');
  cy.contains('You are not currently assigned to a WG.').should('be.visible');
}

// UC-01 / E2E-012 — BUG #38
// A roommate may leave a WG (membership deactivated) and later rejoin the SAME WG through a
// fresh invitation. Rejoining must reactivate the existing membership row instead of hitting
// the (user_id, household_id) unique constraint — i.e. no 500 and exactly one active membership.
describe('UC-01 BUG #38 leave then rejoin the same WG', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('reactivates membership on rejoin without a unique-constraint 500 or duplicate', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'Bug38',
      lastName: 'Owner',
      email: uniqueEmail('bug38.owner'),
      password
    };
    const roommate = {
      firstName: 'Bug38',
      lastName: 'Roommate',
      email: uniqueEmail('bug38.roommate'),
      password
    };
    const household = {
      name: `BUG38 Household ${runId}`,
      address: 'Bug38 Street 1'
    };

    // 1. Owner creates the WG and invites the roommate.
    cy.registerUser(owner);
    cy.createHousehold(household);
    cy.createInvitation({ invitedEmail: roommate.email }).then((firstInvite) => {
      cy.logoutUser();

      // 2. Roommate registers via the invite and joins as a new user.
      registerAndJoinViaInvite(firstInvite, roommate);

      // 3. Roommate leaves the WG through their profile.
      leaveCurrentWgViaProfile();
      cy.logoutUser();

      // 4. Owner issues a fresh invitation for the same roommate email (API; UI create is covered elsewhere).
      cy.loginUser(owner.email, owner.password);
      createInvitationApi(roommate.email).then((rejoinInvite) => {
        cy.logoutUser();

        // 5. Roommate logs back in and rejoins via the new invite (logged-in accept).
        cy.loginUser(roommate.email, roommate.password);
        cy.intercept('POST', '**/api/v1/invitations/*/accept').as('rejoinAccept');
        acceptInviteWhileLoggedIn(rejoinInvite);
        cy.wait('@rejoinAccept').its('response.statusCode').should('eq', 200);

        // 6. Roommate is listed again, exactly once.
        cy.visit('/#/household/members');
        cy.contains('h1', household.name).should('be.visible');
        cy.contains(roommate.email).should('be.visible');
        cy.logoutUser();

        // 7. Backend confirms exactly one active membership for the rejoined roommate.
        cy.loginUser(owner.email, owner.password);
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
              const matches = membersRes.body.filter((m) => m.email === roommate.email);
              expect(matches.length, 'one active membership after rejoin').to.eq(1);
            });
          });
        });
      });
    });
  });
});
