const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Passw0rd!';

function uniqueEmail(prefix) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 100000)}@example.com`;
}

function tomorrowIso() {
  const date = new Date();
  date.setDate(date.getDate() + 1);
  return toLocalIsoDate(date);
}

function toLocalIsoDate(date) {
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

function registerUser(email, firstName, lastName) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/users`,
    body: { email, password, firstName, lastName },
  }).then((response) => String(response.body));
}

function authHeaders(token) {
  return { Authorization: token };
}

function login(email) {
  cy.visit('/#/login');
  cy.get('input[name="email"]').clear().type(email);
  cy.get('input[name="password"]').clear().type(password);
  cy.contains('button', 'Login').click();
  cy.location('hash').should('eq', '#/');
}

function createHousehold(token, name) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/households`,
    headers: authHeaders(token),
    body: { name, address: 'History QA Street' },
  }).then((response) => {
    expect(response.status).to.eq(201);
    return response.body;
  });
}

function getCurrentRoommate(token, householdId, email) {
  return cy.request({
    method: 'GET',
    url: `${apiUrl}/households/${householdId}/members`,
    headers: authHeaders(token),
  }).then((response) => {
    expect(response.status).to.eq(200);
    const roommate = response.body.find(member => member.email === email);
    expect(roommate, 'current user roommate membership').to.exist;
    return roommate;
  });
}

function createDoneTask(token, email) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/tasks`,
    headers: authHeaders(token),
    body: {
      title: 'FR13 Cypress Completed Task',
      description: 'Visible in task history',
      dueDate: tomorrowIso(),
      assignedToEmail: email,
    },
  }).then((createResponse) => {
    expect(createResponse.status).to.eq(201);

    return cy.request({
      method: 'PATCH',
      url: `${apiUrl}/tasks/${createResponse.body.id}/status`,
      headers: authHeaders(token),
      body: { status: 'DONE' },
    });
  }).then((statusResponse) => {
    expect(statusResponse.status).to.eq(200);
  });
}

function createRecordedExpense(token, roommateId) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/expenses`,
    headers: authHeaders(token),
    body: {
      description: 'FR13 Cypress Groceries',
      totalAmount: 12.50,
      date: toLocalIsoDate(new Date()),
      paidByRoommateId: roommateId,
      splits: [
        {
          roommateId,
          amountOwed: 12.50,
        },
      ],
    },
  }).then((response) => {
    expect(response.status).to.eq(201);
  });
}

describe('FR-13 History E2E Workflow', () => {
  it('shows task and payment history from the sidebar navigation', () => {
    const email = uniqueEmail('fr13-history');

    registerUser(email, 'FR13', 'History').then((token) => {
      return createHousehold(token, `FR13 WG ${Date.now()}`).then((household) => {
        return getCurrentRoommate(token, household.id, email).then((roommate) => {
          return createDoneTask(token, email).then(() => {
            return createRecordedExpense(token, roommate.id);
          });
        });
      }).then(() => {
        login(email);
        cy.contains('a', 'History').click();
        cy.location('hash').should('eq', '#/history');

        cy.contains('h2', 'History').should('be.visible');
        cy.contains('FR13 Cypress Completed Task').should('be.visible');
        cy.contains('DONE').should('be.visible');
        cy.contains(email).should('be.visible');
        cy.contains('FR13 Cypress Groceries').should('be.visible');
        cy.contains('RECORDED').should('be.visible');
        cy.contains('€12.50').should('be.visible');
      });
    });
  });

  it('shows empty states and does not crash when the household has no history', () => {
    const email = uniqueEmail('fr13-empty');

    registerUser(email, 'FR13', 'Empty').then((token) => {
      return createHousehold(token, `FR13 Empty WG ${Date.now()}`).then(() => {
        login(email);
        cy.visit('/#/history');

        cy.contains('h2', 'History').should('be.visible');
        cy.contains('No completed or cancelled tasks yet.').should('be.visible');
        cy.contains('No payment or expense history yet.').should('be.visible');
      });
    });
  });
});
