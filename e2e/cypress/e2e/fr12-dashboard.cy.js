const backendUrl = 'http://localhost:8080/api/v1';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@example.com`;
}

function todayIso() {
  const today = new Date();
  const localDate = new Date(today.getTime() - today.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

function authToken() {
  return cy.window().then((win) => {
    const token = win.localStorage.getItem('authToken');
    expect(token, 'auth token').to.be.a('string').and.not.be.empty;
    return token;
  });
}

function authHeaders(token) {
  return {
    Authorization: token.startsWith('Bearer ') ? token : `Bearer ${token}`
  };
}

function createTask(token, task) {
  return cy.request({
    method: 'POST',
    url: `${backendUrl}/tasks`,
    headers: authHeaders(token),
    body: task
  }).then((response) => {
    expect(response.status).to.eq(201);
    expect(response.body.title).to.eq(task.title);
    return response.body;
  });
}

function createCleaningSchedule(token, schedule) {
  return cy.request({
    method: 'POST',
    url: `${backendUrl}/cleaning-schedules`,
    headers: authHeaders(token),
    body: schedule
  }).then((response) => {
    expect(response.status).to.eq(201);
    expect(response.body.title).to.eq(schedule.title);
    expect(response.body.active).to.eq(true);
    return response.body;
  });
}

function expectDashboardDataExists(token, taskTitle, scheduleTitle) {
  return cy.request({
    method: 'GET',
    url: `${backendUrl}/tasks`,
    headers: authHeaders(token)
  }).then((response) => {
    expect(response.status).to.eq(200);
    expect(response.body.map(task => task.title)).to.include(taskTitle);
  }).then(() => {
    return cy.request({
      method: 'GET',
      url: `${backendUrl}/cleaning-schedules/active`,
      headers: authHeaders(token)
    }).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body.map(schedule => schedule.title)).to.include(scheduleTitle);
    });
  });
}

describe('FR-12 Dashboard E2E', () => {
  const password = 'Password123!';

  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('FR-12 positive: dashboard shows current tasks, schedules, housemates, and working links', () => {
    const runId = Date.now();
    const user = {
      firstName: 'Dora',
      lastName: 'DashboardQa',
      email: uniqueEmail('fr12.dashboard'),
      password
    };
    const household = {
      name: `FR12 Dashboard WG ${runId}`,
      address: 'Dashboard Test Street 1'
    };
    const task = {
      title: `FR12 Today Task ${runId}`,
      description: 'Dashboard task created by Cypress',
      dueDate: todayIso(),
      assignedToEmail: user.email
    };
    const schedule = {
      title: `FR12 Cleaning Schedule ${runId}`,
      description: 'Dashboard schedule created by Cypress',
      frequency: 'WEEKLY',
      startDate: todayIso(),
      assignedToEmail: user.email
    };

    cy.registerUser(user);
    cy.logoutUser();
    cy.loginUser(user.email, user.password);
    cy.createHousehold(household);

    authToken()
      .then((token) => createTask(token, task)
        .then(() => createCleaningSchedule(token, schedule))
        .then(() => expectDashboardDataExists(token, task.title, schedule.title)));

    cy.visit('/#/tasks');
    cy.contains('h2', 'Tasks').should('be.visible');

    cy.intercept('GET', '**/api/v1/tasks').as('dashboardAllTasks');
    cy.intercept('GET', '**/api/v1/tasks/my').as('dashboardMyTasks');
    cy.intercept('GET', '**/api/v1/cleaning-schedules/active').as('dashboardActiveSchedules');

    cy.visit('/#/');

    cy.wait('@dashboardAllTasks').then(({ response }) => {
      expect(response.statusCode).to.eq(200);
      expect(response.body.map(item => item.title)).to.include(task.title);
    });
    cy.wait('@dashboardMyTasks').then(({ response }) => {
      expect(response.statusCode).to.eq(200);
      expect(response.body.map(item => item.title)).to.include(task.title);
    });
    cy.wait('@dashboardActiveSchedules').then(({ response }) => {
      expect(response.statusCode).to.eq(200);
      expect(response.body.map(item => item.title)).to.include(schedule.title);
    });

    cy.contains('h2', 'Welcome home').should('be.visible');
    cy.contains('.stat-card', 'Open Tasks').should('be.visible');
    cy.contains('.stat-card', 'My Tasks').should('be.visible');
    cy.contains('.stat-card', 'Schedules').should('be.visible');
    cy.contains('.stat-card', 'Housemates').should('be.visible');
    cy.contains('.feed-widget', 'Today').should('be.visible');
    cy.contains('.feed-widget', 'Upcoming Schedules').should('be.visible');

    cy.contains('.feed-widget', 'Today').within(() => {
      cy.contains(task.title).should('be.visible');
    });

    cy.contains('.feed-widget', 'Upcoming Schedules').within(() => {
      cy.contains(schedule.title).should('be.visible');
    });

    cy.contains('.stat-card', 'My Tasks').click();
    cy.location('hash').should('include', '#/tasks');
    cy.location('hash').should('include', 'tab=mine');
    cy.contains('.nav-link.active', 'My Tasks').should('be.visible');

    cy.visit('/#/');
    cy.contains('.stat-card', 'Schedules').click();
    cy.location('hash').should('eq', '#/cleaning-schedules');
    cy.contains('h2', 'Cleaning').should('be.visible');
  });

  it('FR-12 empty state: dashboard shows empty messages without tasks or schedules', () => {
    const runId = Date.now();
    const user = {
      firstName: 'Erin',
      lastName: 'DashboardQa',
      email: uniqueEmail('fr12.empty.dashboard'),
      password
    };

    cy.registerUser(user);
    cy.createHousehold({
      name: `FR12 Empty Dashboard WG ${runId}`,
      address: 'Empty Dashboard Street 1'
    });

    cy.visit('/#/');

    cy.contains('h2', 'Welcome home').should('be.visible');
    cy.contains('.feed-widget', 'Today').within(() => {
      cy.contains('Nothing due today!').should('be.visible');
    });
    cy.contains('.feed-widget', 'Upcoming Schedules').within(() => {
      cy.contains('No active schedules yet.').should('be.visible');
    });
  });
});
