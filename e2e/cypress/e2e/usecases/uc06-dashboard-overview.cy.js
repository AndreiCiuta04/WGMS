const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function todayIso() {
  const date = new Date();
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

function authToken() {
  return cy.window().then((win) => {
    const token = win.localStorage.getItem('authToken');
    expect(token, 'auth token').to.be.a('string').and.not.be.empty;
    return token.startsWith('Bearer ') ? token : `Bearer ${token}`;
  });
}

function createTask(token, task) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/tasks`,
    headers: { Authorization: token },
    body: task
  }).then((response) => {
    expect(response.status).to.eq(201);
  });
}

function createCleaningSchedule(token, schedule) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/cleaning-schedules`,
    headers: { Authorization: token },
    body: schedule
  }).then((response) => {
    expect(response.status).to.eq(201);
  });
}

describe('UC-06 Dashboard Overview After Household Activity E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('UC-06 positive: dashboard summarizes tasks, schedules, housemates, and links', () => {
    const runId = Date.now();
    const user = {
      firstName: 'Uc06',
      lastName: 'Dashboard',
      email: uniqueEmail('uc06.dashboard'),
      password
    };
    const taskTitle = `UC06 Today Task ${runId}`;
    const scheduleTitle = `UC06 Schedule ${runId}`;

    cy.registerUser(user);
    cy.createHousehold({ name: `UC06 WG ${runId}`, address: 'UC06 Street' });
    authToken().then((token) => {
      return createTask(token, {
        title: taskTitle,
        description: 'UC06 task due today',
        dueDate: todayIso(),
        assignedToEmail: user.email
      }).then(() => createCleaningSchedule(token, {
        title: scheduleTitle,
        description: 'UC06 active schedule',
        frequency: 'DAILY',
        startDate: todayIso(),
        assignedToEmail: user.email
      }));
    });

    cy.visit('/#/tasks');
    cy.contains('h2', 'Tasks').should('be.visible');
    cy.visit('/#/');

    cy.contains('h2', 'Welcome home').should('be.visible');
    cy.contains('.stat-card', 'Open Tasks').should('be.visible');
    cy.contains('.stat-card', 'My Tasks').should('be.visible');
    cy.contains('.stat-card', 'Schedules').should('be.visible');
    cy.contains('.stat-card', 'Housemates').should('be.visible');
    cy.contains('.feed-widget', 'Today').contains(taskTitle).should('be.visible');
    cy.contains('.feed-widget', 'Upcoming Schedules').contains(scheduleTitle).should('be.visible');

    cy.contains('.stat-card', 'My Tasks').click();
    cy.location('hash').should('include', '#/tasks');
    cy.location('hash').should('include', 'tab=mine');

    cy.visit('/#/');
    cy.contains('.stat-card', 'Schedules').click();
    cy.location('hash').should('eq', '#/cleaning-schedules');
  });

  it('UC-06 empty state: empty household dashboard shows empty responsibility messages', () => {
    const user = {
      firstName: 'Uc06',
      lastName: 'Empty',
      email: uniqueEmail('uc06.empty'),
      password
    };

    cy.registerUser(user);
    cy.createHousehold({ name: `UC06 Empty WG ${Date.now()}`, address: 'UC06 Empty Street' });
    cy.visit('/#/');

    cy.contains('.feed-widget', 'Today').contains('Nothing due today!').should('be.visible');
    cy.contains('.feed-widget', 'Upcoming Schedules').contains('No active schedules yet.').should('be.visible');
  });
});
