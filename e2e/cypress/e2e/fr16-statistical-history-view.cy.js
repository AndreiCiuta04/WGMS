const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Passw0rd!';

function uniqueEmail(prefix) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 100000)}@example.com`;
}

function toLocalIsoDate(date) {
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

function isoDate(daysFromToday = 0) {
  const date = new Date();
  date.setDate(date.getDate() + daysFromToday);
  return toLocalIsoDate(date);
}

function registerUser(email, firstName, lastName) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/users`,
    body: { email, password, firstName, lastName, termsAccepted: true },
  }).then((response) => {
    expect(response.status).to.be.oneOf([200, 201]);
    return String(response.body);
  });
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
    body: { name, address: 'Statistics QA Street' },
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

function createDoneTask(token, email, title) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/tasks`,
    headers: authHeaders(token),
    body: {
      title,
      description: 'Visible in FR-16 statistics',
      dueDate: isoDate(),
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

function createCleaningSchedule(token, email, title) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/cleaning-schedules`,
    headers: authHeaders(token),
    body: {
      title,
      description: 'Visible in FR-16 cleaning frequency statistics',
      frequency: 'WEEKLY',
      startDate: isoDate(),
      assignedToEmail: email,
    },
  }).then((response) => {
    expect(response.status).to.eq(201);
    return response.body;
  });
}

function createRecordedExpense(token, roommateId, amount) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/expenses`,
    headers: authHeaders(token),
    body: {
      description: `FR16 Cypress Groceries ${Date.now()}`,
      totalAmount: amount,
      date: isoDate(),
      paidByRoommateId: roommateId,
      splits: [
        {
          roommateId,
          amountOwed: amount,
        },
      ],
    },
  }).then((response) => {
    expect(response.status).to.eq(201);
  });
}

function createShoppingItem(token, item) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/shopping-items`,
    headers: authHeaders(token),
    body: item,
  }).then((response) => {
    expect(response.status).to.eq(201);
    return response.body;
  });
}

function markShoppingItemBought(token, itemId) {
  return cy.request({
    method: 'PATCH',
    url: `${apiUrl}/shopping-items/${itemId}/status`,
    headers: authHeaders(token),
    body: { status: 'BOUGHT' },
  }).then((response) => {
    expect(response.status).to.eq(200);
  });
}

function prepareStatisticsHousehold() {
  const runId = Date.now();
  const email = uniqueEmail('fr16-statistics');
  const taskTitle = `FR16 Cypress Completed Task ${runId}`;
  const scheduleTitle = `FR16 Cypress Kitchen Routine ${runId}`;
  const shoppingName = `FR16 Cypress Apples ${runId}`;

  return registerUser(email, 'FR16', 'Statistics').then((token) => {
    return createHousehold(token, `FR16 Statistics WG ${runId}`).then((household) => {
      return getCurrentRoommate(token, household.id, email).then((roommate) => {
        return createDoneTask(token, email, taskTitle)
          .then(() => createCleaningSchedule(token, email, scheduleTitle))
          .then(() => createRecordedExpense(token, roommate.id, 12.50))
          .then(() => createShoppingItem(token, {
            name: shoppingName,
            quantityAmount: 2,
            quantityUnit: 'KILOGRAM',
            notes: 'FR-16 bought shopping item',
            visibility: 'SHARED',
          }))
          .then(item => markShoppingItemBought(token, item.id))
          .then(() => ({ email, token, taskTitle, scheduleTitle, shoppingName }));
      });
    });
  });
}

function waitForStatisticsRequests() {
  cy.wait('@overviewRequest').its('response.statusCode').should('eq', 200);
  cy.wait('@taskStatisticsRequest').its('response.statusCode').should('eq', 200);
  cy.wait('@expenseStatisticsRequest').its('response.statusCode').should('eq', 200);
  cy.wait('@shoppingStatisticsRequest').its('response.statusCode').should('eq', 200);
}

function interceptStatisticsRequests() {
  cy.intercept('GET', '**/api/v1/history/statistics/overview*').as('overviewRequest');
  cy.intercept('GET', '**/api/v1/history/statistics/tasks*').as('taskStatisticsRequest');
  cy.intercept('GET', '**/api/v1/history/statistics/expenses*').as('expenseStatisticsRequest');
  cy.intercept('GET', '**/api/v1/history/statistics/shopping*').as('shoppingStatisticsRequest');
}

describe('FR-16 Statistical History View E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('FR-16 negative: logged-out user is redirected away from statistics route', () => {
    cy.visit('/#/statistics');
    cy.url().should('include', '/login');
  });

  it('FR-16 positive: statistics page is reachable from navigation and shows backend-backed analytics', () => {
    prepareStatisticsHousehold().then(({ email }) => {
      login(email);
      interceptStatisticsRequests();

      cy.contains('a', 'Statistics').click();
      cy.location('hash').should('eq', '#/statistics');
      waitForStatisticsRequests();

      cy.contains('h2', 'Statistics').should('be.visible');
      cy.contains('Statistics controls').should('be.visible');
      cy.contains('label', 'Period').should('be.visible');
      cy.contains('label', 'Grouping').should('be.visible');
      cy.contains('.module-filter-chip', 'Tasks').should('be.visible');
      cy.contains('.module-filter-chip', 'Shopping').should('be.visible');

      cy.get('.statistics-kpi-grid').within(() => {
        cy.contains('.stat-kpi-card', 'Completed tasks').contains('1').should('be.visible');
        cy.contains('.stat-kpi-card', 'Expenses').contains('€12.50').should('be.visible');
        cy.contains('.stat-kpi-card', 'Shopping bought').contains('1').should('be.visible');
        cy.contains('.stat-kpi-card', 'Active schedules').contains('1').should('be.visible');
      });

      cy.contains('button', 'Tasks & Cleaning').click();
      cy.contains('Total tasks').should('be.visible');
      cy.contains('Completed').should('be.visible');
      cy.contains('Cleaning frequencies').should('be.visible');
      cy.contains('Completed tasks by member').should('be.visible');

      cy.contains('button', 'Expenses & Payments').click();
      cy.contains('Total spent').should('be.visible');
      cy.contains('€12.50').should('be.visible');
      cy.contains('Expense trend').should('be.visible');
      cy.contains('Expenses paid by member').should('be.visible');

      cy.contains('button', 'Shopping').click();
      cy.contains('Bought items').should('be.visible');
      cy.contains('Frequently purchased products').should('be.visible');
      cy.contains('Items added by member').should('be.visible');
    });
  });

  it('FR-16 positive: custom filters validate date ranges and send selected modules', () => {
    prepareStatisticsHousehold().then(({ email }) => {
      login(email);
      interceptStatisticsRequests();
      cy.visit('/#/statistics');
      waitForStatisticsRequests();

      cy.get('#statisticsPreset').select('Custom');
      cy.get('#statisticsFrom').clear().type(isoDate(1));
      cy.get('#statisticsTo').clear().type(isoDate(-1));
      cy.contains('button', 'Apply filters').click();
      cy.contains('The start date must be before or equal to the end date.').should('be.visible');

      cy.get('#statisticsFrom').clear().type(isoDate(-30));
      cy.get('#statisticsTo').clear().type(isoDate());
      cy.contains('.module-filter-chip', 'Expenses').find('input').uncheck({ force: true });
      cy.contains('.module-filter-chip', 'Payments').find('input').uncheck({ force: true });
      cy.contains('.module-filter-chip', 'Shopping').find('input').uncheck({ force: true });

      cy.intercept('GET', '**/api/v1/history/statistics/overview*').as('filteredOverviewRequest');
      cy.contains('button', 'Apply filters').click();
      cy.wait('@filteredOverviewRequest').then((interception) => {
        expect(interception.response.statusCode).to.eq(200);
        expect(interception.request.url).to.include('modules=TASKS');
        expect(interception.request.url).to.include('modules=CLEANING');
        expect(interception.request.url).not.to.include('modules=EXPENSES');
        expect(interception.request.url).not.to.include('modules=PAYMENTS');
        expect(interception.request.url).not.to.include('modules=SHOPPING');
      });
    });
  });

  it('FR-16 empty state: household without activity shows statistics page without fake data', () => {
    const email = uniqueEmail('fr16-empty');

    registerUser(email, 'FR16', 'Empty').then((token) => {
      return createHousehold(token, `FR16 Empty WG ${Date.now()}`).then(() => {
        login(email);
        interceptStatisticsRequests();
        cy.visit('/#/statistics');
        waitForStatisticsRequests();

        cy.contains('h2', 'Statistics').should('be.visible');
        cy.get('.statistics-kpi-grid').within(() => {
          cy.contains('.stat-kpi-card', 'Completed tasks').contains('0').should('be.visible');
          cy.contains('.stat-kpi-card', 'Expenses').contains('€0.00').should('be.visible');
          cy.contains('.stat-kpi-card', 'Shopping bought').contains('0').should('be.visible');
        });
        cy.contains('Household activity trend').should('be.visible');
        cy.contains('No chart data available for the selected period.').should('be.visible');
      });
    });
  });

  [360, 740].forEach((width) => {
    it(`FR-16 responsive: statistics page renders without horizontal overflow at ${width}px width`, () => {
      cy.viewport(width, 740);
      const email = uniqueEmail(`fr16-responsive-${width}`);

      registerUser(email, 'FR16', 'Responsive').then((token) => {
        return createHousehold(token, `FR16 Responsive WG ${width} ${Date.now()}`).then(() => {
          login(email);
          interceptStatisticsRequests();
          cy.visit('/#/statistics');
          waitForStatisticsRequests();

          cy.contains('h2', 'Statistics').should('be.visible');
          cy.contains('Statistics controls').should('be.visible');
          cy.document().then((document) => {
            expect(document.documentElement.scrollWidth).to.be.at.most(width + 2);
          });
        });
      });
    });
  });
});