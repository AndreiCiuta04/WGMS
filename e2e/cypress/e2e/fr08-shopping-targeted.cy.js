const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

describe('FR-08 Targeted Shopping Visibility E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('FR-08 positive: user A targets item to user B and user B marks it BOUGHT', () => {
    const runId = Date.now();
    const requester = {
      firstName: 'Shopping',
      lastName: 'Requester',
      email: uniqueEmail('shopping.requester'),
      password
    };
    const buyer = {
      firstName: 'Shopping',
      lastName: 'Buyer',
      email: uniqueEmail('shopping.buyer'),
      password
    };
    const itemName = `Targeted Coffee ${runId}`;

    cy.registerUser(requester);
    cy.createHousehold({
      name: `Targeted Shopping WG ${runId}`,
      address: 'Targeted Shopping Street 1'
    });

    cy.createInvitation({ invitedEmail: buyer.email }).then((inviteUrl) => {
      cy.logoutUser();
      cy.visitInviteUrl(inviteUrl);
      cy.get('input[formcontrolname="firstName"]').clear().type(buyer.firstName);
      cy.get('input[formcontrolname="lastName"]').clear().type(buyer.lastName);
      cy.get('input[formcontrolname="password"]').clear().type(buyer.password);
      cy.contains('button', 'Register & Join WG').click();
      cy.contains('You have successfully joined the household!').should('be.visible');
      cy.location('hash', { timeout: 3000 }).should('eq', '#/');

      cy.logoutUser();
      cy.loginUser(requester.email, requester.password);
      cy.visit('/#/shopping-items/create');
      cy.contains('h2', 'Add Shopping Item').should('be.visible');
      cy.get('#name').type(itemName);
      cy.get('#quantity').type('1 bag');
      cy.get('#visibility').select('TARGETED');
      cy.get('#targetUserEmail')
        .find(`option[value="${buyer.email}"]`)
        .should('exist');
      cy.get('#targetUserEmail').select(buyer.email);

      cy.intercept('POST', '**/api/v1/shopping-items').as('createTargetedItem');
      cy.contains('button', 'Add Item').click();
      cy.wait('@createTargetedItem').its('response.statusCode').should('eq', 201);
      cy.contains('.needed-item', itemName).within(() => {
        cy.contains('TARGETED').should('be.visible');
        cy.contains('For: Shopping Buyer').should('be.visible');
      });

      cy.logoutUser();
      cy.loginUser(buyer.email, buyer.password);
      cy.visit('/#/shopping-items');
      cy.get('[data-cy="shopping-tab-targeted"]').click();
      cy.contains('.needed-item', itemName).should('be.visible');

      cy.intercept('PATCH', '**/api/v1/shopping-items/*/status').as('markBought');
      cy.contains('.needed-item', itemName).find('button[title="Mark as Bought"]').click();
      cy.wait('@markBought').its('response.statusCode').should('eq', 200);
      cy.contains('.completed-header', 'Bought & Cancelled').click();
      cy.contains('.completed-item', itemName).should('be.visible');

      cy.logoutUser();
      cy.loginUser(requester.email, requester.password);
      cy.visit('/#/shopping-items');
      cy.contains('.completed-header', 'Bought & Cancelled').click();
      cy.contains('.completed-item', itemName).within(() => {
        cy.contains('Bought').should('be.visible');
        cy.contains('TARGETED').should('be.visible');
      });
    });
  });

  it('FR-08 negative: self-targeting is blocked by excluding current user from recipients', () => {
    const user = {
      firstName: 'Self',
      lastName: 'Target',
      email: uniqueEmail('shopping.self.target'),
      password
    };

    cy.registerUser(user);
    cy.createHousehold({
      name: `Self Target WG ${Date.now()}`,
      address: 'Self Target Street 1'
    });

    cy.visit('/#/shopping-items/create');
    cy.contains('h2', 'Add Shopping Item').should('be.visible');
    cy.get('#name').type(`Self Targeted Item ${Date.now()}`);
    cy.get('#visibility').select('TARGETED');
    cy.get('#targetUserEmail')
      .find(`option[value="${user.email}"]`)
      .should('not.exist');
    cy.intercept('POST', '**/api/v1/shopping-items').as('selfTargetRequest');
    cy.contains('button', 'Add Item').click();
    cy.contains("Please pick the roommate you're asking.").should('be.visible');
    cy.get('@selfTargetRequest.all').should('have.length', 0);
  });
});
