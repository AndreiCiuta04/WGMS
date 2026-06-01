const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@example.com`;
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

function createDashboardHousehold() {
  const runId = Date.now();
  const owner = {
    firstName: 'Fr12',
    lastName: 'Owner',
    email: uniqueEmail('fr12.owner'),
    password
  };
  const roommate = {
    firstName: 'Fr12',
    lastName: 'Roommate',
    email: uniqueEmail('fr12.roommate'),
    password
  };

  return registerViaApi(owner).then((ownerToken) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: bearer(ownerToken),
      body: { name: `FR12 Dashboard WG ${runId}`, address: 'FR12 Street 1' }
    }).then((householdResponse) => {
      const householdId = householdResponse.body.id;

      return cy.request({
        method: 'POST',
        url: `${apiUrl}/households/${householdId}/invitations`,
        headers: bearer(ownerToken),
        body: { invitedEmail: roommate.email, expiresInHours: 168 }
      }).then((invitationResponse) => {
        const inviteToken = invitationResponse.body.inviteUrl.split('/').pop();

        return registerViaApi(roommate).then((roommateToken) => {
          return cy.request({
            method: 'POST',
            url: `${apiUrl}/invitations/${inviteToken}/accept`,
            headers: bearer(roommateToken)
          }).then(() => ({ owner, roommate, ownerToken, roommateToken, householdId }));
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

function createExpenseThatOwnerIsOwed({ ownerToken, ownerEmail, roommateEmail, amount = 20 }) {
  return cy.request({
    method: 'GET',
    url: `${apiUrl}/households/me`,
    headers: bearer(ownerToken)
  }).then((householdResponse) => {
    const householdId = householdResponse.body.id;

    return cy.request({
      method: 'GET',
      url: `${apiUrl}/households/${householdId}/members`,
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
          description: `FR12 Shared Expense ${Date.now()}`,
          totalAmount: amount,
          date: isoDate(),
          paidByRoommateId: ownerRoommate.id,
          splits: [
            { roommateId: ownerRoommate.id, amountOwed: amount / 2 },
            { roommateId: otherRoommate.id, amountOwed: amount / 2 }
          ]
        }
      }).then((response) => {
        expect(response.status).to.eq(201);
        return response.body;
      });
    });
  });
}

function loginThroughUi(email) {
  cy.loginUser(email, password);
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

describe('FR-12 Backend-Backed Dashboard E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('FR-12 positive: backend dashboard shows responsibilities, debts, shopping, schedules, and supports dashboard actions', () => {
    const runId = Date.now();
    const taskTitle = `FR12 Overdue Chore ${runId}`;
    const shoppingName = `FR12 Needed Apples ${runId}`;
    const scheduleTitle = `FR12 Kitchen Routine ${runId}`;

    createDashboardHousehold().then(({ owner, roommate, ownerToken }) => {
      return createTask(ownerToken, {
        title: taskTitle,
        description: 'FR-12 overdue dashboard task',
        dueDate: isoDate(),
        assignedToEmail: owner.email
      })
        .then(() => createShoppingItem(ownerToken, {
          name: shoppingName,
          quantity: '3 kg',
          notes: 'FR-12 dashboard shopping item',
          visibility: 'SHARED'
        }))
        .then(() => createCleaningSchedule(ownerToken, {
          title: scheduleTitle,
          description: 'FR-12 dashboard schedule',
          frequency: 'WEEKLY',
          startDate: isoDate(),
          assignedToEmail: owner.email
        }))
        .then(() => createExpenseThatOwnerIsOwed({
          ownerToken,
          ownerEmail: owner.email,
          roommateEmail: roommate.email,
          amount: 20
        }))
        .then(() => {
          loginThroughUi(owner.email);
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

          cy.get('.responsibilities-card').within(() => {
            cy.contains('h3', 'My Pending Tasks').should('be.visible');
            cy.contains(taskTitle).should('be.visible');
          });
          cy.get('.warning-banner').contains(taskTitle).should('be.visible');

          cy.get('.debts-card').within(() => {
            cy.contains('h3', 'Settlement & Debts Breakdown').should('be.visible');
            cy.contains('Owed to You').should('be.visible');
            cy.contains('Fr12 Roommate').should('be.visible');
            cy.contains('+€10.00').should('be.visible');
          });
          cy.get('.balance-card.positive').within(() => {
            cy.contains('You are owed').should('be.visible');
            cy.contains('€10.00').should('be.visible');
          });

          cy.get('.shopping-card').within(() => {
            cy.contains('h3', 'Needed Shopping Items').should('be.visible');
            cy.contains(shoppingName).should('be.visible');
            cy.contains('(3 kg)').should('be.visible');
          });

          cy.get('.routines-card').within(() => {
            cy.contains('h3', 'Upcoming Cleaning Schedules').should('be.visible');
            cy.contains(scheduleTitle).should('be.visible');
          });

          cy.intercept('PATCH', '**/api/v1/tasks/**/status').as('completeTask');
          cy.get('.responsibilities-card input[type="checkbox"]').first().check({ force: true });
          cy.wait('@completeTask').its('response.statusCode').should('eq', 200);
          cy.get('.responsibilities-card').should('not.contain', taskTitle);
          cy.get('.warning-banner').should('not.exist');

          cy.intercept('PATCH', '**/api/v1/shopping-items/**/status').as('buyShoppingItem');
          cy.get('.shopping-card').contains('button', 'Bought').click();
          cy.wait('@buyShoppingItem').its('response.statusCode').should('eq', 200);
          cy.get('.shopping-card').should('not.contain', shoppingName);
        });
    });
  });

  it('FR-12 empty-state: backend dashboard shows neutral household messages and settled balance', () => {
    const user = {
      firstName: 'Fr12',
      lastName: 'Empty',
      email: uniqueEmail('fr12.empty'),
      password
    };

    cy.registerUser(user);
    cy.intercept('GET', '**/api/v1/dashboard').as('dashboardRequest');
    cy.createHousehold({
      name: `FR12 Empty WG ${Date.now()}`,
      address: 'FR12 Empty Street'
    });
    cy.wait('@dashboardRequest').its('response.statusCode').should('eq', 200);

    cy.contains('h2', 'Welcome home').should('be.visible');
    cy.get('.balance-card.neutral').within(() => {
      cy.contains('All settled up').should('be.visible');
      cy.contains('€0.00').should('be.visible');
    });
    cy.get('.responsibilities-card').contains('All caught up! No pending chores assigned to you.').should('be.visible');
    cy.get('.debts-card').contains('Everything is perfectly balanced! No active debts.').should('be.visible');
    cy.get('.shopping-card').contains('No active shopping requests. Flat is fully stocked!').should('be.visible');
    cy.get('.routines-card').contains('No active cleaning schedules configured.').should('be.visible');
  });

  it('FR-12 negative: user without WG sees no-household onboarding state after dashboard 403', () => {
    const user = {
      firstName: 'Fr12',
      lastName: 'NoHousehold',
      email: uniqueEmail('fr12.no.household'),
      password
    };

    cy.intercept('GET', '**/api/v1/dashboard').as('dashboardRequest');
    cy.registerUser(user);
    cy.wait('@dashboardRequest').its('response.statusCode').should('eq', 403);

    cy.contains('h2', 'Welcome to WGMS!').should('be.visible');
    cy.contains('a', 'Create a WG').should('be.visible');
    cy.contains('a', 'View WG / Join').should('be.visible');
  });

  [
    [360, 740],
    [1024, 768]
  ].forEach(([width, height]) => {
    it(`FR-12 responsive: backend dashboard renders key sections without horizontal overflow at ${width}x${height}`, () => {
      cy.viewport(width, height);
      const user = {
        firstName: 'Fr12',
        lastName: 'Responsive',
        email: uniqueEmail(`fr12.responsive.${width}`),
        password
      };

      cy.registerUser(user);
      cy.intercept('GET', '**/api/v1/dashboard').as('dashboardRequest');
      cy.createHousehold({
        name: `FR12 Responsive WG ${width} ${Date.now()}`,
        address: 'FR12 Responsive Street'
      });
      cy.wait('@dashboardRequest').its('response.statusCode').should('eq', 200);
      cy.contains('h2', 'Welcome home').should('be.visible');
      cy.get('.dashboard-stats-grid').should('be.visible');
      cy.get('.responsibilities-card').contains('My Pending Tasks').should('be.visible');
      cy.get('.debts-card').contains('Settlement & Debts Breakdown').should('be.visible');
      cy.get('.shopping-card').contains('Needed Shopping Items').should('be.visible');
      cy.get('.routines-card').contains('Upcoming Cleaning Schedules').should('be.visible');

      cy.document().then((document) => {
        expect(document.documentElement.scrollWidth).to.be.at.most(width + 2);
      });
    });
  });
});
