const backendUrl = 'http://localhost:8080/api/v1';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@example.com`;
}

function todayIso() {
  return toLocalIsoDate(new Date());
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

function registerUser(user) {
  return cy.request({
    method: 'POST',
    url: `${backendUrl}/users`,
    failOnStatusCode: false,
    body: {
      email: user.email,
      password: user.password,
      firstName: user.firstName,
      lastName: user.lastName, termsAccepted: true }
  });
}

function login(user) {
  return cy.request({
    method: 'POST',
    url: `${backendUrl}/authentication`,
    body: {
      email: user.email,
      password: user.password
    }
  }).then((response) => {
    expect(response.status).to.eq(200);

    const body = response.body;
    const token =
      body.token ||
      body.accessToken ||
      body.jwt ||
      body.Authorization ||
      body.authorization ||
      body;

    expect(token, 'JWT token from login response').to.be.a('string').and.not.be.empty;

    return token.startsWith('Bearer ') ? token : `Bearer ${token}`;
  });
}

function authHeaders(token) {
  return {
    Authorization: token
  };
}

function createHousehold(token, name) {
  return cy.request({
    method: 'POST',
    url: `${backendUrl}/households`,
    headers: authHeaders(token),
    body: {
      name,
      address: 'Test Street 1'
    }
  }).then((response) => {
    expect(response.status).to.eq(201);
    expect(response.body.name).to.eq(name);
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
    expect(response.body.assignedToEmail).to.eq(schedule.assignedToEmail);
    expect(response.body.frequency).to.eq(schedule.frequency);
    expect(response.body.active).to.eq(true);
    return response.body;
  });
}

describe('FR-04 Cleaning Schedules E2E', () => {
  let user;
  let token;

  beforeEach(() => {
    user = {
      email: uniqueEmail('fr04.owner'),
      password: 'Password1!',
      firstName: 'FR04',
      lastName: 'Owner'
    };

    registerUser(user).then((response) => {
      expect(response.status).to.be.oneOf([201, 200]);
    });

    login(user).then((jwt) => {
      token = jwt;
      createHousehold(token, `FR04 Household ${Date.now()}`);
    });
  });

  it('FR-04 API setup evidence: creates, lists, edits, pauses, resumes, and deletes a schedule', () => {
    const originalTitle = `Clean Kitchen ${Date.now()}`;
    const updatedTitle = `${originalTitle} Updated`;

    const schedulePayload = {
      title: originalTitle,
      description: 'Weekly kitchen cleaning created by Cypress',
      frequency: 'WEEKLY',
      startDate: tomorrowIso(),
      assignedToEmail: user.email
    };

    createCleaningSchedule(token, schedulePayload).then((schedule) => {
      cy.request({
        method: 'GET',
        url: `${backendUrl}/cleaning-schedules`,
        headers: authHeaders(token)
      }).then((listResponse) => {
        expect(listResponse.status).to.eq(200);
        expect(listResponse.body.map(s => s.id)).to.include(schedule.id);
      });

      cy.request({
        method: 'PUT',
        url: `${backendUrl}/cleaning-schedules/${schedule.id}`,
        headers: authHeaders(token),
        body: {
          title: updatedTitle,
          description: 'Updated by Cypress',
          frequency: 'BIWEEKLY',
          startDate: tomorrowIso(),
          assignedToEmail: user.email
        }
      }).then((updateResponse) => {
        expect(updateResponse.status).to.eq(200);
        expect(updateResponse.body.title).to.eq(updatedTitle);
        expect(updateResponse.body.frequency).to.eq('BIWEEKLY');
      });

      cy.request({
        method: 'PATCH',
        url: `${backendUrl}/cleaning-schedules/${schedule.id}/active`,
        headers: authHeaders(token),
        body: {
          active: false
        }
      }).then((pauseResponse) => {
        expect(pauseResponse.status).to.eq(200);
        expect(pauseResponse.body.active).to.eq(false);
      });

      cy.request({
        method: 'PATCH',
        url: `${backendUrl}/cleaning-schedules/${schedule.id}/active`,
        headers: authHeaders(token),
        body: {
          active: true
        }
      }).then((resumeResponse) => {
        expect(resumeResponse.status).to.eq(200);
        expect(resumeResponse.body.active).to.eq(true);
      });

      cy.request({
        method: 'DELETE',
        url: `${backendUrl}/cleaning-schedules/${schedule.id}`,
        headers: authHeaders(token)
      }).then((deleteResponse) => {
        expect(deleteResponse.status).to.eq(204);
      });
    });
  });

  it('FR-04 positive: creates, edits, pauses, resumes, generates task, and deletes schedules through UI', () => {
    const runId = Date.now();
    const dueTitle = `UI Due Cleaning ${runId}`;
    const upcomingTitle = `UI Upcoming Cleaning ${runId}`;
    const updatedTitle = `${upcomingTitle} Updated`;

    cy.loginUser(user.email, user.password);

    cy.visit('/#/cleaning-schedules/create');
    cy.contains('h2', 'Create Cleaning Schedule').should('be.visible');

    cy.intercept('POST', '**/api/v1/cleaning-schedules').as('createScheduleRequest');
    cy.contains('button', 'Create Schedule').click();
    cy.contains('Title is required.').should('be.visible');
    cy.contains('Frequency is required.').should('be.visible');
    cy.contains('Start date is required.').should('be.visible');
    cy.contains('Please assign the schedule to someone.').should('be.visible');
    cy.get('@createScheduleRequest.all').should('have.length', 0);

    cy.get('#title').clear().type(dueTitle);
    cy.get('#description').clear().type('Due today cleaning schedule from Cypress');
    cy.get('#frequency').select('DAILY');
    cy.get('#assignedToEmail').select(user.email);
    cy.get('#startDate').clear().type(todayIso());
    cy.contains('button', 'Create Schedule').click();
    cy.wait('@createScheduleRequest').its('response.statusCode').should('eq', 201);

    cy.location('hash').should('eq', '#/cleaning-schedules');
    cy.contains('.due-item', dueTitle).should('be.visible');

    cy.visit('/#/cleaning-schedules/create');
    cy.get('#title').clear().type(upcomingTitle);
    cy.get('#description').clear().type('Upcoming cleaning schedule from Cypress');
    cy.get('#frequency').select('WEEKLY');
    cy.get('#assignedToEmail').select(user.email);
    cy.get('#startDate').clear().type(tomorrowIso());
    cy.contains('button', 'Create Schedule').click();
    cy.wait('@createScheduleRequest').its('response.statusCode').should('eq', 201);

    cy.contains('.upcoming-item', upcomingTitle).should('be.visible');

    cy.contains('.manage-header', 'Manage Schedules').click();
    cy.contains('.manage-item', upcomingTitle).find('a[title="Edit"]').click();
    cy.contains('h2', 'Edit Cleaning Schedule').should('be.visible');
    cy.get('#title').clear().type(updatedTitle);
    cy.get('#frequency').select('BIWEEKLY');
    cy.intercept('PUT', '**/api/v1/cleaning-schedules/*').as('updateScheduleRequest');
    cy.contains('button', 'Save Changes').click();
    cy.wait('@updateScheduleRequest').its('response.statusCode').should('eq', 200);
    cy.contains('.upcoming-item', updatedTitle).should('be.visible');

    cy.contains('.manage-header', 'Manage Schedules').click();
    cy.intercept('PATCH', '**/api/v1/cleaning-schedules/*/active').as('toggleActiveRequest');
    cy.contains('.manage-item', updatedTitle).find('button[title="Pause"]').click();
    cy.wait('@toggleActiveRequest').its('response.statusCode').should('eq', 200);
    cy.contains('.manage-item', updatedTitle).within(() => {
      cy.contains('Paused').should('be.visible');
    });

    cy.contains('.manage-item', updatedTitle).find('button[title="Resume"]').click();
    cy.wait('@toggleActiveRequest').its('response.statusCode').should('eq', 200);
    cy.contains('.manage-item', updatedTitle).within(() => {
      cy.contains('Paused').should('not.exist');
    });

    cy.on('window:confirm', () => true);
    cy.intercept('DELETE', '**/api/v1/cleaning-schedules/*').as('deleteScheduleRequest');
    cy.contains('.manage-item', updatedTitle).find('button[title="Delete"]').click();
    cy.wait('@deleteScheduleRequest').its('response.statusCode').should('eq', 204);
    cy.contains(updatedTitle).should('not.exist');

    cy.intercept('POST', '**/api/v1/cleaning-schedules/*/generate-task').as('generateTaskRequest');
    cy.contains('.due-item', dueTitle).contains('button', 'Generate Task').click();
    cy.wait('@generateTaskRequest').its('response.statusCode').should('be.oneOf', [200, 201]);
    cy.contains(`Task created for "${dueTitle}"!`).should('be.visible');

    cy.visit('/#/tasks');
    cy.contains(dueTitle).should('be.visible');

    cy.visit('/#/cleaning-schedules');
    cy.contains('.manage-header', 'Manage Schedules').click();
    cy.contains('.manage-item', dueTitle).find('button[title="Delete"]').click();
    cy.wait('@deleteScheduleRequest').its('response.statusCode').should('eq', 409);
    cy.contains(/open tasks|complete or cancel|generated tasks|Cannot delete|Pause it|Request conflicts|conflict/i).should('be.visible');
  });

  it('FR-04 positive: generated task from schedule appears in tasks', () => {
    const schedulePayload = {
      title: `Bathroom Cleaning ${Date.now()}`,
      description: 'Generated-task integration test',
      frequency: 'DAILY',
      startDate: todayIso(),
      assignedToEmail: user.email
    };

    createCleaningSchedule(token, schedulePayload).then((schedule) => {
      cy.request({
        method: 'POST',
        url: `${backendUrl}/cleaning-schedules/${schedule.id}/generate-task`,
        headers: authHeaders(token),
        body: {}
      }).then((generateResponse) => {
        expect(generateResponse.status).to.be.oneOf([200, 201]);
        expect(generateResponse.body.title).to.eq(schedule.title);
        expect(generateResponse.body.assignedToEmail).to.eq(user.email);
        expect(generateResponse.body.cleaningScheduleId).to.eq(schedule.id);

        const generatedTaskId = generateResponse.body.id;

        cy.request({
          method: 'GET',
          url: `${backendUrl}/tasks`,
          headers: authHeaders(token)
        }).then((tasksResponse) => {
          expect(tasksResponse.status).to.eq(200);

          const generatedTask = tasksResponse.body.find(task => task.id === generatedTaskId);

          expect(generatedTask, 'generated task appears in task list').to.exist;
          expect(generatedTask.cleaningScheduleId).to.eq(schedule.id);
          expect(generatedTask.assignedToEmail).to.eq(user.email);
        });
      });
    });
  });

  it('FR-04 negative: deleting a schedule after generated tasks is blocked', () => {
    const schedulePayload = {
      title: `Protected Schedule ${Date.now()}`,
      description: 'Delete protection integration test',
      frequency: 'WEEKLY',
      startDate: todayIso(),
      assignedToEmail: user.email
    };

    createCleaningSchedule(token, schedulePayload).then((schedule) => {
      cy.request({
        method: 'POST',
        url: `${backendUrl}/cleaning-schedules/${schedule.id}/generate-task`,
        headers: authHeaders(token),
        body: {}
      }).then((generateResponse) => {
        expect(generateResponse.status).to.be.oneOf([200, 201]);
      });

      cy.request({
        method: 'DELETE',
        url: `${backendUrl}/cleaning-schedules/${schedule.id}`,
        headers: authHeaders(token),
        failOnStatusCode: false
      }).then((deleteResponse) => {
        expect(deleteResponse.status).to.eq(409);
      });

      cy.request({
        method: 'GET',
        url: `${backendUrl}/cleaning-schedules/${schedule.id}`,
        headers: authHeaders(token)
      }).then((getResponse) => {
        expect(getResponse.status).to.eq(200);
        expect(getResponse.body.id).to.eq(schedule.id);
      });
    });
  });

  it('FR-04 negative: other-household schedules do not leak', () => {
    const otherUser = {
      email: uniqueEmail('fr04.other'),
      password: 'Password1!',
      firstName: 'FR04',
      lastName: 'Other'
    };

    const schedulePayload = {
      title: `Private Household Schedule ${Date.now()}`,
      description: 'Household isolation integration test',
      frequency: 'MONTHLY',
      startDate: tomorrowIso(),
      assignedToEmail: user.email
    };

    createCleaningSchedule(token, schedulePayload).then((schedule) => {
      registerUser(otherUser).then((response) => {
        expect(response.status).to.be.oneOf([201, 200]);
      });

      login(otherUser).then((otherToken) => {
        createHousehold(otherToken, `Other Household ${Date.now()}`);

        cy.request({
          method: 'GET',
          url: `${backendUrl}/cleaning-schedules`,
          headers: authHeaders(otherToken)
        }).then((otherListResponse) => {
          expect(otherListResponse.status).to.eq(200);
          expect(otherListResponse.body.map(s => s.id)).not.to.include(schedule.id);
        });

        cy.request({
          method: 'GET',
          url: `${backendUrl}/cleaning-schedules/${schedule.id}`,
          headers: authHeaders(otherToken),
          failOnStatusCode: false
        }).then((otherGetResponse) => {
          expect(otherGetResponse.status).to.be.oneOf([403, 404]);
        });
      });
    });
  });

  it('FR-04 negative: unauthenticated cleaning schedule access is rejected', () => {
    cy.request({
      method: 'GET',
      url: `${backendUrl}/cleaning-schedules`,
      failOnStatusCode: false
    }).then((response) => {
      expect(response.status).to.be.oneOf([401, 403]);
    });
  });
});
