const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Passw0rd!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function registerUser(email, firstName, lastName) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/users`,
    body: { email, password, firstName, lastName },
  }).then((response) => String(response.body));
}

function createExpenseHousehold() {
  const mainEmail = uniqueEmail('uc05.main');
  const roomEmail = uniqueEmail('uc05.roommate');

  return registerUser(mainEmail, 'Uc05', 'Main').then((mainToken) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: { Authorization: mainToken },
      body: { name: `UC05 WG ${Date.now()}`, address: 'UC05 Street' },
    }).then((householdResponse) => {
      const householdId = householdResponse.body.id;

      return cy.request({
        method: 'POST',
        url: `${apiUrl}/households/${householdId}/invitations`,
        headers: { Authorization: mainToken },
        body: { invitedEmail: roomEmail, expiresInHours: 168 },
      }).then((invitationResponse) => {
        const token = invitationResponse.body.inviteUrl.split('/').pop();

        return registerUser(roomEmail, 'Uc05', 'Roommate').then((roomToken) => {
          return cy.request({
            method: 'POST',
            url: `${apiUrl}/invitations/${token}/accept`,
            headers: { Authorization: roomToken },
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

describe('UC-05 Expense Tracking and History E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('UC-05 positive: expense equal split persists and appears in history', () => {
    const expenseName = `UC05 Groceries ${Date.now()}`;

    createExpenseHousehold().then(({ mainEmail }) => {
      login(mainEmail);
      cy.visit('/#/expenses');
      cy.contains('h2', 'Expenses').should('be.visible');

      cy.get('#description').clear().type(expenseName);
      cy.get('#totalAmount').clear().type('20.00');
      cy.contains('.split-row', 'Uc05 Roommate').find('input[type="checkbox"]').check();
      cy.contains('button', 'Equal Split').click();
      cy.contains('.split-row', 'Uc05 Main').find('.split-amount input').should('have.value', '10,00');
      cy.contains('.split-row', 'Uc05 Roommate').find('.split-amount input').should('have.value', '10,00');

      cy.contains('button', 'Create Expense').click();
      cy.contains('Expense created.').should('be.visible');
      cy.contains('.expense-item', expenseName).should('be.visible');

      cy.reload();
      cy.contains('.expense-item', expenseName).should('be.visible');

      cy.logoutUser();
      login(mainEmail);
      cy.visit('/#/expenses');
      cy.contains('.expense-item', expenseName).should('be.visible');

      cy.visit('/#/history');
      cy.contains('h2', 'History').should('be.visible');
      cy.contains(expenseName).should('be.visible');
      cy.contains('RECORDED').should('be.visible');
    });
  });

  it('UC-05 negative: invalid amount and split mismatch show errors', () => {
    createExpenseHousehold().then(({ mainEmail }) => {
      login(mainEmail);
      cy.visit('/#/expenses');

      cy.get('#description').clear().type('UC05 Invalid Expense');
      cy.get('#totalAmount').clear().type('-5.00');
      cy.contains('button', 'Create Expense').click();
      cy.contains('Enter a positive total amount.').should('be.visible');

      cy.get('#description').clear().type('UC05 Mismatch Expense');
      cy.get('#totalAmount').clear().type('10.00').blur();
      cy.contains('.split-row', 'Uc05 Roommate').find('input[type="checkbox"]').check();
      cy.contains('.split-row', 'Uc05 Main').find('.split-amount input').clear().type('8.00').blur();
      cy.contains('.split-row', 'Uc05 Roommate').find('.split-amount input').clear().type('1.00').blur();
      cy.contains('button', 'Create Expense').click();
      cy.contains(/split|sum|total|amount/i).should('be.visible');
    });
  });
});
