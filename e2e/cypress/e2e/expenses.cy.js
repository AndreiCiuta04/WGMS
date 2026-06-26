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

describe('FR-05 Expenses E2E Workflow', () => {
  it('creates an expense, shows split data, validates invalid amount, and persists after reload/login', () => {
    createExpenseHousehold().then(({ mainEmail }) => {
      login(mainEmail);
      openExpenses();

      cy.get('#description').clear().type('QA Cypress Expense');
      cy.get('#totalAmount').clear().type('12.00');
      cy.contains('.split-row', 'FR05 Room')
        .find('input[type="checkbox"]')
        .check();
      cy.contains('button', 'Equal Split').click();
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

      cy.get('#description').clear().type('Invalid QA Expense');
      cy.get('#totalAmount').clear().type('-5.00');
      cy.contains('button', 'Create Expense').click();
      cy.contains('Enter a positive total amount with at most two decimal places.').should('be.visible');
    });
  });

  [
    [360, 740],
    [390, 844],
    [412, 915],
  ].forEach(([width, height]) => {
    it(`keeps the expense page usable at ${width}px width`, () => {
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
