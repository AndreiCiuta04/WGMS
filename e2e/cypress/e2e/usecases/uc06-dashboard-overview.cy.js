const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function isoDate(daysFromToday = 0) {
  const date = new Date();
  date.setDate(date.getDate() + daysFromToday);
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

function bearer(token) {
  return {
    Authorization: token.startsWith('Bearer ') ? token : `Bearer ${token}`
  };
}

function registerViaApi(user) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/users`,
    body: user
  }).then((response) => {
    expect(response.status).to.be.oneOf([200, 201]);
    return String(response.body);
  });
}

function createHouseholdWithRoommate() {
  const runId = Date.now();
  const owner = {
    firstName: 'Uc06',
    lastName: 'Owner',
    email: uniqueEmail('uc06.owner'),
    password
  };
  const roommate = {
    firstName: 'Uc06',
    lastName: 'Roommate',
    email: uniqueEmail('uc06.roommate'),
    password
  };

  return registerViaApi(owner).then((ownerToken) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: bearer(ownerToken),
      body: { name: `UC06 Dashboard WG ${runId}`, address: 'UC06 Street' }
    }).then((householdResponse) => {
      return cy.request({
        method: 'POST',
        url: `${apiUrl}/households/${householdResponse.body.id}/invitations`,
        headers: bearer(ownerToken),
        body: { invitedEmail: roommate.email, expiresInHours: 168 }
      }).then((invitationResponse) => {
        const inviteToken = invitationResponse.body.inviteUrl.split('/').pop();

        return registerViaApi(roommate).then((roommateToken) => {
          return cy.request({
            method: 'POST',
            url: `${apiUrl}/invitations/${inviteToken}/accept`,
            headers: bearer(roommateToken)
          }).then(() => ({ owner, roommate, ownerToken, roommateToken }));
        });
      });
    });
  });
}

function createTask(token, task) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/tasks`,
    headers: bearer(token),
    body: task
  }).then((response) => {
    expect(response.status).to.eq(201);
    return response.body;
  });
}

function createShoppingItem(token, item) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/shopping-items`,
    headers: bearer(token),
    body: item
  }).then((response) => {
    expect(response.status).to.eq(201);
    return response.body;
  });
}

function createCleaningSchedule(token, schedule) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/cleaning-schedules`,
    headers: bearer(token),
    body: schedule
  }).then((response) => {
    expect(response.status).to.eq(201);
    return response.body;
  });
}

function createExpenseThatOwnerIsOwed({ ownerToken, ownerEmail, roommateEmail }) {
  return cy.request({
    method: 'GET',
    url: `${apiUrl}/households/me`,
    headers: bearer(ownerToken)
  }).then((householdResponse) => {
    return cy.request({
      method: 'GET',
      url: `${apiUrl}/households/${householdResponse.body.id}/members`,
      headers: bearer(ownerToken)
    }).then((membersResponse) => {
      const ownerRoommate = membersResponse.body.find(member => member.email === ownerEmail);
      const otherRoommate = membersResponse.body.find(member => member.email === roommateEmail);

      expect(ownerRoommate, 'owner roommate').to.exist;
      expect(otherRoommate, 'other roommate').to.exist;

      return cy.request({
        method: 'POST',
        url: `${apiUrl}/expenses`,
        headers: bearer(ownerToken),
        body: {
          description: `UC06 Dashboard Expense ${Date.now()}`,
          totalAmount: 20,
          date: isoDate(),
          paidByRoommateId: ownerRoommate.id,
          splits: [
            { roommateId: ownerRoommate.id, amountOwed: 10 },
            { roommateId: otherRoommate.id, amountOwed: 10 }
          ]
        }
      }).then((response) => {
        expect(response.status).to.eq(201);
        return response.body;
      });
    });
  });
}

function visitDashboardWithMockedDate(mockDate) {
  cy.visit(`/?dashboardReload=${Date.now()}#/`, {
    onBeforeLoad(win) {
      const OriginalDate = win.Date;
      class MockedDate extends OriginalDate {
        constructor(...args) {
          if (args.length === 0) {
            const stack = new Error().stack || '';
            if (stack.includes('isLoggedIn') || stack.includes('Expiration') || stack.includes('AuthService')) {
              super();
            } else {
              super(mockDate);
            }
          } else {
            super(...args);
          }
        }

        static now() {
          const stack = new Error().stack || '';
          if (stack.includes('isLoggedIn') || stack.includes('Expiration') || stack.includes('AuthService')) {
            return OriginalDate.now();
          }
          return new OriginalDate(mockDate).getTime();
        }
      }
      win.Date = MockedDate;
    }
  });
}

describe('UC-06 Dashboard Overview After Household Activity E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('UC-06 positive: backend dashboard summarizes household activity and dashboard links/actions work', () => {
    const runId = Date.now();
    const taskTitle = `UC06 Dashboard Task ${runId}`;
    const shoppingName = `UC06 Dashboard Milk ${runId}`;
    const scheduleTitle = `UC06 Dashboard Routine ${runId}`;

    createHouseholdWithRoommate().then(({ owner, roommate, ownerToken }) => {
      return createTask(ownerToken, {
        title: taskTitle,
        description: 'UC-06 dashboard task',
        dueDate: isoDate(),
        assignedToEmail: owner.email
      })
        .then(() => createShoppingItem(ownerToken, {
          name: shoppingName,
          quantity: '2 l',
          notes: 'UC-06 dashboard shopping item',
          visibility: 'SHARED'
        }))
        .then(() => createCleaningSchedule(ownerToken, {
          title: scheduleTitle,
          description: 'UC-06 dashboard schedule',
          frequency: 'WEEKLY',
          startDate: isoDate(),
          assignedToEmail: owner.email
        }))
        .then(() => createExpenseThatOwnerIsOwed({
          ownerToken,
          ownerEmail: owner.email,
          roommateEmail: roommate.email
        }))
        .then(() => {
          cy.loginUser(owner.email, owner.password);
          cy.intercept('GET', '**/api/v1/dashboard').as('dashboardRequest');
          visitDashboardWithMockedDate(`${isoDate(1)}T12:00:00`);
          cy.wait('@dashboardRequest').its('response.statusCode').should('eq', 200);

          cy.contains('h2', 'Welcome home').should('be.visible');
          cy.get('.dashboard-stats-grid').within(() => {
            cy.contains('My Open Tasks').should('be.visible');
            cy.contains('Active Chores').should('be.visible');
            cy.contains('Schedules').should('be.visible');
            cy.contains('Housemates').should('be.visible');
          });
          cy.get('.responsibilities-card').contains(taskTitle).should('be.visible');
          cy.get('.warning-banner').contains(taskTitle).should('be.visible');
          cy.get('.debts-card').contains('Settlement & Debts Breakdown').should('be.visible');
          cy.get('.balance-card.positive').contains('You are owed').should('be.visible');
          cy.get('.shopping-card').contains(shoppingName).should('be.visible');
          cy.get('.routines-card').contains(scheduleTitle).should('be.visible');

          cy.get('.responsibilities-card').contains('a', 'View all').click();
          cy.location('hash').should('include', '#/tasks');
          cy.location('hash').should('include', 'tab=mine');

          cy.visit('/#/');
          cy.get('.debts-card').contains('a', 'Manage Expenses').click();
          cy.location('hash').should('eq', '#/expenses');

          cy.visit('/#/');
          cy.get('.shopping-card').contains('a', 'View all').click();
          cy.location('hash').should('eq', '#/shopping-items');

          cy.visit('/#/');
          cy.get('.routines-card').contains('a', 'View all').click();
          cy.location('hash').should('eq', '#/cleaning-schedules');

          cy.visit('/#/');
          cy.intercept('PATCH', '**/api/v1/tasks/**/status').as('completeTask');
          cy.get('.responsibilities-card input[type="checkbox"]').first().check({ force: true });
          cy.wait('@completeTask').its('response.statusCode').should('eq', 200);
          cy.get('.responsibilities-card').should('not.contain', taskTitle);

          cy.intercept('PATCH', '**/api/v1/shopping-items/**/status').as('buyShoppingItem');
          cy.get('.shopping-card').contains('button', 'Bought').click();
          cy.wait('@buyShoppingItem').its('response.statusCode').should('eq', 200);
          cy.get('.shopping-card').should('not.contain', shoppingName);
        });
    });
  });

  it('UC-06 empty state: backend dashboard shows current neutral responsibility messages', () => {
    const user = {
      firstName: 'Uc06',
      lastName: 'Empty',
      email: uniqueEmail('uc06.empty'),
      password
    };

    cy.registerUser(user);
    cy.createHousehold({ name: `UC06 Empty WG ${Date.now()}`, address: 'UC06 Empty Street' });
    cy.visit('/#/');

    cy.get('.responsibilities-card').contains('All caught up! No pending chores assigned to you.').should('be.visible');
    cy.get('.debts-card').contains('Everything is perfectly balanced! No active debts.').should('be.visible');
    cy.get('.shopping-card').contains('No active shopping requests. Flat is fully stocked!').should('be.visible');
    cy.get('.routines-card').contains('No active cleaning schedules configured.').should('be.visible');
  });
});
