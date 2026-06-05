const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

describe('FR-02 Household Invitations E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('FR-02 positive: invited user accepts invitation and appears as household member', () => {
    const runId = Date.now();
    const creator = {
      firstName: 'Household',
      lastName: 'Creator',
      email: uniqueEmail('household.creator'),
      password
    };
    const invited = {
      firstName: 'Household',
      lastName: 'Invitee',
      email: uniqueEmail('household.invitee'),
      password
    };
    const household = {
      name: `Household Invitation WG ${runId}`,
      address: 'Invitation Test Street 12'
    };

    cy.registerUser(creator);
    cy.createHousehold(household);

    cy.visit('/#/household/members');
    cy.contains('h1', household.name).should('be.visible');
    cy.contains(creator.email).should('be.visible');

    cy.createInvitation({ invitedEmail: invited.email }).then((inviteUrl) => {
      cy.logoutUser();
      cy.visitInviteUrl(inviteUrl);

      cy.contains('Join a Household').should('be.visible');
      cy.contains(household.name).should('be.visible');
      cy.get('input[formcontrolname="firstName"]').clear().type(invited.firstName);
      cy.get('input[formcontrolname="lastName"]').clear().type(invited.lastName);
      cy.get('input[formcontrolname="email"]').should('have.value', invited.email);
      cy.get('input[formcontrolname="password"]').clear().type(invited.password);
      cy.get('input[formcontrolname="termsAccepted"]').check();

      cy.intercept('POST', '**/api/v1/users').as('registerInvitee');
      cy.intercept('POST', '**/api/v1/invitations/*/accept').as('acceptInvitation');
      cy.contains('button', 'Register & Join WG').click();
      cy.wait('@registerInvitee').its('response.statusCode').should('be.oneOf', [200, 201]);
      cy.wait('@acceptInvitation').its('response.statusCode').should('eq', 200);
      cy.contains('You have successfully joined the household!').should('be.visible');

      cy.visit('/#/household/members');
      cy.contains('h1', household.name).should('be.visible');
      cy.contains(invited.email).should('be.visible');
      cy.contains('ROOMMATE').should('be.visible');
    });
  });

  it('FR-02 negative: invalid invitation email is blocked and invalid token shows readable feedback', () => {
    const owner = {
      firstName: 'Invite',
      lastName: 'Validation',
      email: uniqueEmail('invite.validation'),
      password
    };

    cy.registerUser(owner);
    cy.createHousehold({
      name: `Invitation Validation WG ${Date.now()}`,
      address: 'Validation Street 1'
    });

    cy.visit('/#/household/invitations');
    cy.contains('Manage Invitations').should('be.visible');
    cy.intercept('POST', '**/api/v1/households/*/invitations').as('inviteRequest');
    cy.get('#email').clear().type('not-an-email').blur();
    cy.contains('Please provide a valid email address').should('be.visible');
    cy.contains('button', 'Create').should('be.disabled');
    cy.get('@inviteRequest.all').should('have.length', 0);

    cy.clearLocalStorage();
    cy.visit(`/#/invite/not-a-real-token-${Date.now()}`);
    cy.contains(/invalid|expired|not found|could not|invitation/i, { timeout: 5000 })
      .should('be.visible');
  });
});
