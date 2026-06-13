const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

describe('UC-01 New User Creates WG and Invites Roommate E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('UC-01 positive: user creates WG, invites roommate, and both users see membership', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'Uc01',
      lastName: 'Owner',
      email: uniqueEmail('uc01.owner'),
      password
    };
    const roommate = {
      firstName: 'Uc01',
      lastName: 'Roommate',
      email: uniqueEmail('uc01.roommate'),
      password
    };
    const household = {
      name: `UC01 Household ${runId}`,
      address: 'UC01 Street 1'
    };

    cy.registerUser(owner);
    cy.createHousehold(household);
    cy.createInvitation({ invitedEmail: roommate.email }).then((inviteUrl) => {
      cy.logoutUser();
      cy.visitInviteUrl(inviteUrl);

      cy.contains('Join a Household').should('be.visible');
      cy.contains(household.name).should('be.visible');
      cy.get('input[formcontrolname="firstName"]').clear().type(roommate.firstName);
      cy.get('input[formcontrolname="lastName"]').clear().type(roommate.lastName);
      cy.get('input[formcontrolname="email"]').should('have.value', roommate.email);
      cy.get('input[formcontrolname="password"]').clear().type(roommate.password);
      cy.get('input[formcontrolname="termsAccepted"]').check();
      cy.contains('button', 'Register & Join WG').click();
      cy.contains('You have successfully joined the household!').should('be.visible');
      cy.location('hash', { timeout: 3000 }).should('eq', '#/');

      cy.visit('/#/household/members');
      cy.contains('h1', household.name).should('be.visible');
      cy.contains(roommate.email).should('be.visible');

      cy.logoutUser();
      cy.loginUser(owner.email, owner.password);
      cy.visit('/#/household/members');
      cy.contains('h1', household.name).should('be.visible');
      cy.contains(roommate.email).should('be.visible');
      cy.contains(owner.email).should('be.visible');
    });
  });

  it('UC-01 negative: invalid invite email is blocked and invalid invite token is readable', () => {
    const owner = {
      firstName: 'Uc01',
      lastName: 'Invalid',
      email: uniqueEmail('uc01.invalid'),
      password
    };

    cy.registerUser(owner);
    cy.createHousehold({
      name: `UC01 Invalid WG ${Date.now()}`,
      address: 'UC01 Invalid Street'
    });

    cy.visit('/#/household/invitations');
    cy.intercept('POST', '**/api/v1/households/*/invitations').as('inviteRequest');
    cy.get('#email').clear().type('not-an-email').blur();
    cy.contains('Please provide a valid email address').should('be.visible');
    cy.contains('button', 'Create').should('be.disabled');
    cy.get('@inviteRequest.all').should('have.length', 0);

    cy.clearLocalStorage();
    cy.visit(`/#/invite/uc01-invalid-token-${Date.now()}`);
    cy.contains(/invalid|expired|not found|could not|invitation/i, { timeout: 5000 })
      .should('be.visible');
  });
});
