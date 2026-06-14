const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function addShoppingItem({ name, amount, unit, notes, visibility, targetUserEmail }) {
  cy.visit('/#/shopping-items/create');
  cy.contains('h2', 'Add Shopping Item').should('be.visible');
  cy.get('#name').clear().type(name);
  cy.fillShoppingQuantity({ amount, unit });
  if (notes) {
    cy.get('#notes').clear().type(notes);
  }
  cy.get('#visibility').select(visibility);
  if (targetUserEmail) {
    cy.get('#targetUserEmail').select(targetUserEmail);
  }
  cy.contains('button', 'Add Item').click();
  cy.location('hash').should('eq', '#/shopping-items');
}

describe('UC-04 Shopping Collaboration E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('UC-04 positive: shared and targeted shopping items are visible to roommates and history', () => {
    const runId = Date.now();
    const requester = {
      firstName: 'Uc04',
      lastName: 'Requester',
      email: uniqueEmail('uc04.requester'),
      password
    };
    const buyer = {
      firstName: 'Uc04',
      lastName: 'Buyer',
      email: uniqueEmail('uc04.buyer'),
      password
    };
    const sharedName = `UC04 Shared Pasta ${runId}`;
    const groupedName = `UC04 Grouped Rice ${runId}`;
    const personalName = `UC04 Personal Snack ${runId}`;
    const targetedName = `UC04 Targeted Tea ${runId}`;

    cy.registerUser(requester);
    cy.createHousehold({ name: `UC04 WG ${runId}`, address: 'UC04 Street' });
    cy.createInvitation({ invitedEmail: buyer.email }).then((inviteUrl) => {
      cy.logoutUser();
      cy.visitInviteUrl(inviteUrl);
      cy.get('input[formcontrolname="firstName"]').clear().type(buyer.firstName);
      cy.get('input[formcontrolname="lastName"]').clear().type(buyer.lastName);
      cy.get('input[formcontrolname="password"]').clear().type(buyer.password);
      cy.get('input[formcontrolname="termsAccepted"]').check();
      cy.contains('button', 'Register & Join WG').click();
      cy.contains('You have successfully joined the household!').should('be.visible');
      cy.location('hash', { timeout: 3000 }).should('eq', '#/');

      cy.logoutUser();
      cy.loginUser(requester.email, requester.password);
      addShoppingItem({ name: sharedName, amount: 1, unit: 'PACK', notes: 'Shared pantry item', visibility: 'SHARED' });
      addShoppingItem({ name: groupedName, amount: 1, unit: 'KILOGRAM', notes: 'First shared request', visibility: 'SHARED' });
      addShoppingItem({ name: ` ${groupedName.toLowerCase()} `, amount: 1, unit: 'KILOGRAM', notes: 'Second shared request', visibility: 'SHARED' });
      addShoppingItem({ name: personalName, amount: 1, notes: 'Private item', visibility: 'PERSONAL' });
      addShoppingItem({ name: targetedName, amount: 1, unit: 'PACK', notes: 'Please buy tea', visibility: 'TARGETED', targetUserEmail: buyer.email });

      cy.logoutUser();
      cy.loginUser(buyer.email, buyer.password);
      cy.visit('/#/shopping-items');
      cy.contains('.needed-item', sharedName).should('be.visible');
      cy.contains(personalName).should('not.exist');

      cy.get('[data-cy="shopping-tab-household-list"]').click();
      cy.contains('[data-cy="aggregated-shopping-item"]', groupedName)
        .should('be.visible')
        .within(() => {
          cy.get('[data-cy="aggregate-request-count"]').should('contain', '2 requests');
        });

      cy.get('[data-cy="shopping-tab-targeted"]').click();
      cy.contains('.needed-item', targetedName).should('be.visible');
      cy.contains('.needed-item', targetedName).find('button[title="Mark as Bought"]').click();

      cy.get('[data-cy="shopping-tab-history"]').click();
      cy.contains('[data-cy="shopping-history-item"]', targetedName).within(() => {
        cy.get('[data-cy="shopping-history-status"]').should('contain', 'BOUGHT');
        cy.contains('TARGETED').should('be.visible');
      });
    });
  });

  it('UC-04 negative: missing targeted-shopping recipient is blocked before request', () => {
    const user = {
      firstName: 'Uc04',
      lastName: 'Negative',
      email: uniqueEmail('uc04.negative'),
      password
    };

    cy.registerUser(user);
    cy.createHousehold({ name: `UC04 Negative WG ${Date.now()}`, address: 'UC04 Negative Street' });
    cy.visit('/#/shopping-items/create');
    cy.get('#name').clear().type(`UC04 Missing Target ${Date.now()}`);
    cy.get('#visibility').select('TARGETED');
    cy.intercept('POST', '**/api/v1/shopping-items').as('createItemRequest');
    cy.contains('button', 'Add Item').click();
    cy.contains("Please pick the roommate you're asking.").should('be.visible');
    cy.get('@createItemRequest.all').should('have.length', 0);
  });
});
