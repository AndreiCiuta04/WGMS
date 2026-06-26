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
      lastName: user.lastName,
      termsAccepted: true
    }
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

describe('FR-04 Cleaning Schedule integration', () => {
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

  it('creates, lists, edits, pauses, resumes, and deletes a cleaning schedule', () => {
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

  it('generates a concrete task from a cleaning schedule and links it back to the schedule', () => {
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

  it('does not allow deleting a schedule after it generated tasks', () => {
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

  it('keeps cleaning schedules scoped to the user household', () => {
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

  it('rejects unauthenticated access to cleaning schedule endpoints', () => {
    cy.request({
      method: 'GET',
      url: `${backendUrl}/cleaning-schedules`,
      failOnStatusCode: false
    }).then((response) => {
      expect(response.status).to.be.oneOf([401, 403]);
    });
  });
});
