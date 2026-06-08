const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Passw0rd!';

function uniqueEmail(prefix) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 100000)}@example.com`;
}

function registerUser(email, firstName, lastName) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/users`,
    body: { email, password, firstName, lastName, termsAccepted: true },
  }).then((response) => String(response.body));
}

function bearer(token) {
  return { Authorization: token };
}

function createExpenseHousehold() {
  const mainEmail = uniqueEmail('fr05-main');
  const roomEmail = uniqueEmail('fr05-room');

  return registerUser(mainEmail, 'FR05', 'Main').then((mainToken) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: bearer(mainToken),
      body: { name: `FR05 WG ${Date.now()}`, address: 'QA Street' },
    }).then((householdResponse) => {
      const householdId = householdResponse.body.id;

      return cy.request({
        method: 'POST',
        url: `${apiUrl}/households/${householdId}/invitations`,
        headers: bearer(mainToken),
        body: { invitedEmail: roomEmail, expiresInHours: 168 },
      }).then((invitationResponse) => {
        const token = invitationResponse.body.inviteUrl.split('/').pop();

        return registerUser(roomEmail, 'FR05', 'Room').then((roomToken) => {
          return cy.request({
            method: 'POST',
            url: `${apiUrl}/invitations/${token}/accept`,
            headers: bearer(roomToken),
          }).then(() => ({ mainEmail, roomEmail }));
        });
      });
    });
  });
}

function login(email) {
  cy.visit('/#/login');
  cy.get('input[name="email"]').clear().type(email);
  cy.get('input[name="password"]').clear().type(password);
  cy.contains('button', 'Login').click();
  cy.location('hash').should('eq', '#/');
}

function openExpenses() {
  cy.visit('/#/expenses');
  cy.contains('h2', 'Expenses').should('be.visible');
  cy.contains('h3', 'Create Expense').should('be.visible');
}

describe('FR-05 Shared Expenses E2E', () => {
  it('FR-05 positive: creates, equal-splits, persists, edits, and deletes an expense', () => {
    createExpenseHousehold().then(({ mainEmail }) => {
      login(mainEmail);
      openExpenses();

      cy.get('#description').clear().type('QA Cypress Expense');
      cy.get('#totalAmount').clear().type('12.00');
      cy.contains('.split-row', 'FR05 Room')
        .find('input[type="checkbox"]')
        .check();
      cy.intercept('POST', '**/api/v1/expenses/equal-split').as('equalSplit');
      cy.contains('button', 'Equal Split').click();
      cy.wait('@equalSplit').its('response.statusCode').should('eq', 200);
      cy.contains('.split-row', 'FR05 Main')
        .find('.split-amount input')
        .should('have.value', '6,00');
      cy.contains('.split-row', 'FR05 Room')
        .find('.split-amount input')
        .should('have.value', '6,00');
      cy.intercept('POST', '**/api/v1/expenses', (request) => {
        request.on('response', (response) => {
          response.setDelay(500);
        });
      }).as('createExpense');
      cy.contains('button', 'Create Expense').click();
      cy.contains('button', 'Saving...').should('be.disabled');
      cy.wait('@createExpense').its('response.statusCode').should('eq', 201);

      cy.contains('QA Cypress Expense').should('be.visible');
      cy.contains('FR05 Room').should('be.visible');
      cy.contains('6').should('be.visible');

      cy.reload();
      cy.contains('QA Cypress Expense').should('be.visible');

      cy.contains('a', 'Logout').click();
      login(mainEmail);
      openExpenses();
      cy.contains('QA Cypress Expense').should('be.visible');

      cy.contains('.expense-item', 'QA Cypress Expense')
        .find('button[title="Edit expense"]')
        .click();
      cy.contains('h3', 'Edit Expense').should('be.visible');
      cy.get('#description').clear().type('QA Cypress Expense Updated');
      cy.get('#totalAmount').clear().type('14.00').blur();
      cy.contains('.split-row', 'FR05 Main')
        .find('.split-amount input')
        .clear()
        .type('7.00')
        .blur();
      cy.contains('.split-row', 'FR05 Room')
        .find('.split-amount input')
        .clear()
        .type('7.00')
        .blur();
      cy.intercept('PUT', '**/api/v1/expenses/*').as('updateExpense');
      cy.contains('button', 'Update Expense').click();
      cy.wait('@updateExpense').its('response.statusCode').should('eq', 200);
      cy.contains('Expense updated.').should('be.visible');
      cy.contains('QA Cypress Expense Updated').should('be.visible');

      cy.contains('.expense-item', 'QA Cypress Expense Updated')
        .find('button[title="Delete expense"]')
        .click();
      cy.contains('Delete expense?').should('be.visible');
      cy.intercept('DELETE', '**/api/v1/expenses/*').as('deleteExpense');
      cy.confirmDialog();
      cy.wait('@deleteExpense').its('response.statusCode').should('eq', 204);
      cy.contains('Expense deleted.').should('be.visible');
      cy.contains('QA Cypress Expense Updated').should('not.exist');

      cy.get('#description').clear().type('Invalid QA Expense');
      cy.get('#totalAmount').clear().type('-5.00');
      cy.contains('button', 'Create Expense').click();
      cy.contains('Enter a positive total amount.').should('be.visible');
    });
  });

  it('FR-05 negative: split sum mismatch shows readable backend error', () => {
    createExpenseHousehold().then(({ mainEmail }) => {
      login(mainEmail);
      openExpenses();

      cy.get('#description').clear().type('Mismatch QA Expense');
      cy.get('#totalAmount').clear().type('10.00').blur();
      cy.contains('.split-row', 'FR05 Room')
        .find('input[type="checkbox"]')
        .check();
      cy.contains('.split-row', 'FR05 Main')
        .find('.split-amount input')
        .clear()
        .type('8.00')
        .blur();
      cy.contains('.split-row', 'FR05 Room')
        .find('.split-amount input')
        .clear()
        .type('1.00')
        .blur();

      cy.intercept('POST', '**/api/v1/expenses').as('createMismatchExpense');
      cy.contains('button', 'Create Expense').click();
      cy.wait('@createMismatchExpense')
        .its('response.statusCode')
        .should('be.oneOf', [400, 409, 422]);
      cy.contains(/split|sum|total|amount/i).should('be.visible');
    });
  });

  [
    [360, 740],
    [390, 844],
    [412, 915],
  ].forEach(([width, height]) => {
    it(`FR-05 responsive: expense page remains usable at ${width}px width`, () => {
      cy.viewport(width, height);

      createExpenseHousehold().then(({ mainEmail }) => {
        login(mainEmail);
        openExpenses();

        cy.get('#description').should('be.visible');
        cy.get('#totalAmount').should('be.visible');
        cy.contains('button', 'Create Expense').should('be.visible');
        cy.contains('.split-row', 'FR05 Room').should('be.visible');

        cy.document().then((document) => {
          expect(document.documentElement.scrollWidth).to.be.at.most(width + 2);
        });
      });
    });
  });
});
