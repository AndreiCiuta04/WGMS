const backendUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';
const fr15SeedPassword = 'password';
const fr15MainTenantEmail = 'maintenant.fr15@example.com';
const fr15LightWorkerEmail = 'roommate.light@example.com';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function futureDateIso(daysAhead = 3) {
  const date = new Date();
  date.setDate(date.getDate() + daysAhead);
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

function todayIso() {
  const date = new Date();
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

describe('FR-15 Fair Chore Balancing E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('uses deterministic FR-15 seed data and recommends Light Worker', () => {
    const suggestedTaskTitle = `FR-15 Seed Suggested Task ${Date.now()}`;
    const dueDate = futureDateIso(5);
    let createdTaskId;

    cy.loginUser(fr15MainTenantEmail, fr15SeedPassword);

    cy.intercept('GET', '**/api/v1/tasks/fair-balancing/recommendation*').as('fairRecommendation');
    cy.intercept('POST', '**/api/v1/tasks').as('createTaskRequest');

    cy.visit('/#/tasks/create');
    cy.location('hash').should('eq', '#/tasks/create');
    cy.get('[data-cy="fair-chore-suggestion"]', { timeout: 10000 }).should('be.visible');
    cy.get('[data-cy="fair-chore-suggestion"]').contains('Fair chore suggestion').should('be.visible');

    cy.wait('@fairRecommendation').then(({ response }) => {
      expect(response.statusCode).to.eq(200);
      expect(response.body.recommendedEmail).to.eq(fr15LightWorkerEmail);

      const stats = response.body.stats;
      const annaFair = stats.find((stat) => stat.email === fr15MainTenantEmail);
      const lightWorker = stats.find((stat) => stat.email === fr15LightWorkerEmail);
      const heavyWorker = stats.find((stat) => stat.email === 'roommate.heavy@example.com');
      const mediumWorker = stats.find((stat) => stat.email === 'roommate.medium@example.com');

      expect(annaFair).to.include({
        completedTasks: 2,
        openTasks: 1,
        overdueTasks: 0,
        fairnessBucket: 'NO_OVERDUE_AVAILABLE',
        candidate: false
      });
      expect(annaFair.fairnessScore).to.eq(2.75);

      expect(lightWorker).to.include({
        completedTasks: 1,
        openTasks: 0,
        overdueTasks: 0,
        fairnessBucket: 'NO_OVERDUE_AVAILABLE',
        candidate: true,
        recommended: true
      });
      expect(lightWorker.fairnessScore).to.eq(1);

      expect(heavyWorker).to.include({
        completedTasks: 5,
        openTasks: 1,
        overdueTasks: 0,
        fairnessBucket: 'NO_OVERDUE_AVAILABLE',
        candidate: false
      });
      expect(heavyWorker.fairnessScore).to.eq(5.75);

      expect(mediumWorker).to.include({
        completedTasks: 3,
        openTasks: 0,
        overdueTasks: 1,
        fairnessBucket: 'HAS_OVERDUE',
        candidate: false
      });
      expect(mediumWorker.fairnessScore).to.eq(5);
    });

    cy.contains('Light Worker').should('be.visible');
    cy.contains(fr15LightWorkerEmail).should('be.visible');

    cy.get('[data-cy="fair-chore-use-suggestion"]').click();
    cy.get('#assignedToEmail').should('have.value', fr15LightWorkerEmail);
    cy.get('#title').clear().type(suggestedTaskTitle);
    cy.get('#description').clear().type('Created using the deterministic FR-15 seed suggestion');
    cy.get('#dueDate').clear().type(dueDate);
    cy.contains('button', 'Create Task').click();

    cy.wait('@createTaskRequest').then(({ response }) => {
      expect(response.statusCode).to.eq(201);
      expect(response.body.assignedToEmail).to.eq(fr15LightWorkerEmail);
      createdTaskId = response.body.id;
    });

    cy.location('hash').should('include', '#/tasks');
    cy.contains(suggestedTaskTitle).should('be.visible');
    cy.contains(fr15LightWorkerEmail).should('be.visible');

    cy.window().its('localStorage.authToken').then((token) => {
      cy.request({
        method: 'DELETE',
        url: `${backendUrl}/tasks/${createdTaskId}`,
        headers: {
          Authorization: `Bearer ${token}`
        }
      }).its('status').should('eq', 204);
    });
  });

  it('keeps the fair chore suggestion usable on mobile viewports', () => {
    cy.viewport(390, 844);
    cy.loginUser(fr15MainTenantEmail, fr15SeedPassword);

    cy.intercept('GET', '**/api/v1/tasks/fair-balancing/recommendation*').as('fairRecommendation');

    cy.visit('/#/tasks/create');
    cy.wait('@fairRecommendation')
      .its('response.body.recommendedEmail')
      .should('eq', fr15LightWorkerEmail);

    cy.get('[data-cy="fair-chore-suggestion"]').should('be.visible');
    cy.get('[data-cy="fair-chore-use-suggestion"]').scrollIntoView().should('be.visible').click();
    cy.get('#assignedToEmail').should('have.value', fr15LightWorkerEmail);

    cy.window().then((win) => {
      expect(win.document.documentElement.scrollWidth).to.be.at.most(win.innerWidth + 1);
    });

    cy.viewport(412, 915);
    cy.get('[data-cy="fair-chore-suggestion"]').should('be.visible');
    cy.get('[data-cy="fair-chore-use-suggestion"]').scrollIntoView().should('be.visible');
  });

  it('supports undoing completion through the task UI and fairness stats use current task status', () => {
    const title = `FR-15 Reopen Evidence ${Date.now()}`;
    const dueDate = futureDateIso(6);
    let createdTaskId;

    cy.loginUser(fr15MainTenantEmail, fr15SeedPassword);

    cy.window().its('localStorage.authToken').then((token) => {
      cy.request({
        method: 'POST',
        url: `${backendUrl}/tasks`,
        headers: {
          Authorization: `Bearer ${token}`
        },
        body: {
          title,
          description: 'Created to verify task completion undo evidence',
          dueDate,
          assignedToEmail: fr15MainTenantEmail
        }
      }).then((response) => {
        expect(response.status).to.eq(201);
        createdTaskId = response.body.id;
      });
    });

    cy.intercept('PATCH', '**/api/v1/tasks/*/status').as('taskStatusUpdate');
    cy.visit('/#/tasks');
    cy.contains('.wgms-card', title).within(() => {
      cy.get('[data-cy="task-mark-done"]').click();
    });
    cy.wait('@taskStatusUpdate')
      .its('response.body.status')
      .should('eq', 'DONE');
    cy.contains('.wgms-card', title).within(() => {
      cy.contains('Done').should('be.visible');
      cy.get('[data-cy="task-reopen"]').click();
    });
    cy.wait('@taskStatusUpdate')
      .its('response.body.status')
      .should('eq', 'OPEN');
    cy.contains('.wgms-card', title).within(() => {
      cy.contains('Open').should('be.visible');
    });

    cy.window().its('localStorage.authToken').then((token) => {
      cy.request({
        method: 'GET',
        url: `${backendUrl}/tasks/fair-balancing/recommendation?taskTitle=Undo%20Evidence&dueDate=${dueDate}`,
        headers: {
          Authorization: `Bearer ${token}`
        }
      }).then((response) => {
        expect(response.status).to.eq(200);
        const annaFair = response.body.stats.find((stat) => stat.email === fr15MainTenantEmail);
        expect(annaFair.completedTasks).to.eq(2);
        expect(annaFair.openTasks).to.eq(2);
      });

      cy.request({
        method: 'DELETE',
        url: `${backendUrl}/tasks/${createdTaskId}`,
        headers: {
          Authorization: `Bearer ${token}`
        }
      }).its('status').should('eq', 204);
    });
  });

  it('uses fair balancing when generating a cleaning schedule task', () => {
    const title = `FR-15 Fair Schedule ${Date.now()}`;
    let scheduleId;
    let generatedTaskId;

    cy.loginUser(fr15MainTenantEmail, fr15SeedPassword);

    cy.intercept('POST', '**/api/v1/cleaning-schedules').as('createScheduleRequest');
    cy.intercept('POST', '**/api/v1/cleaning-schedules/*/generate-task').as('generateTaskRequest');

    cy.visit('/#/cleaning-schedules/create');
    cy.get('#title').clear().type(title);
    cy.get('#description').clear().type('Generated through fair balancing');
    cy.get('#frequency').select('Weekly');
    cy.get('#assignedToEmail').select('roommate.heavy@example.com');
    cy.get('#startDate').clear().type(todayIso());
    cy.get('#fairBalancingEnabled').check();
    cy.contains('button', 'Create Schedule').click();

    cy.wait('@createScheduleRequest').then(({ response }) => {
      expect(response.statusCode).to.eq(201);
      expect(response.body.fairBalancingEnabled).to.eq(true);
      expect(response.body.assignedToEmail).to.eq('roommate.heavy@example.com');
      scheduleId = response.body.id;
    });

    cy.location('hash').should('include', '#/cleaning-schedules');
    cy.contains('.due-item', title, { timeout: 10000 }).within(() => {
      cy.contains('Generate Task').click();
    });

    cy.wait('@generateTaskRequest').then(({ response }) => {
      expect(response.statusCode).to.be.oneOf([200, 201]);
      expect(response.body.title).to.eq(title);
      expect(response.body.assignedToEmail).to.eq(fr15LightWorkerEmail);
      generatedTaskId = response.body.id;
    });

    cy.visit('/#/tasks');
    cy.contains('.wgms-card', title).within(() => {
      cy.contains(fr15LightWorkerEmail).should('be.visible');
    });

    cy.window().its('localStorage.authToken').then((token) => {
      cy.request({
        method: 'DELETE',
        url: `${backendUrl}/tasks/${generatedTaskId}`,
        headers: {
          Authorization: `Bearer ${token}`
        }
      }).its('status').should('eq', 204);

      cy.request({
        method: 'DELETE',
        url: `${backendUrl}/cleaning-schedules/${scheduleId}`,
        headers: {
          Authorization: `Bearer ${token}`
        }
      }).its('status').should('eq', 204);
    });
  });

  it('recommends the lower-burden roommate and applies the suggestion during task creation', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'Fair',
      lastName: 'Owner',
      email: uniqueEmail('fair.owner'),
      password
    };
    const roommate = {
      firstName: 'Fair',
      lastName: 'Roommate',
      email: uniqueEmail('fair.roommate'),
      password
    };
    const dueDate = futureDateIso(4);
    const suggestedTaskTitle = `Fair Suggested Task ${runId}`;

    cy.registerUser(owner);
    cy.createHousehold({
      name: `Fair WG ${runId}`,
      address: 'Fair Test Street 1'
    });

    cy.createInvitation({ invitedEmail: roommate.email }).then((inviteUrl) => {
      cy.logoutUser();
      cy.visitInviteUrl(inviteUrl);
      cy.get('input[formcontrolname="firstName"]').clear().type(roommate.firstName);
      cy.get('input[formcontrolname="lastName"]').clear().type(roommate.lastName);
      cy.get('input[formcontrolname="password"]').clear().type(roommate.password);
      cy.get('input[formcontrolname="termsAccepted"]').check();
      cy.contains('button', 'Register & Join WG').click();
      cy.contains('You have successfully joined the household!').should('be.visible');
      cy.location('hash', { timeout: 3000 }).should('eq', '#/');

      cy.logoutUser();
      cy.loginUser(owner.email, owner.password);

      cy.window().its('localStorage.authToken').then((token) => {
        for (let index = 0; index < 3; index++) {
          cy.request({
            method: 'POST',
            url: `${backendUrl}/tasks`,
            headers: {
              Authorization: `Bearer ${token}`
            },
            body: {
              title: `Owner burden ${runId} ${index}`,
              description: 'Task history for fair balancing',
              dueDate,
              assignedToEmail: owner.email
            }
          }).its('status').should('eq', 201);
        }
      });

      cy.intercept('GET', '**/api/v1/tasks/fair-balancing/recommendation*').as('fairRecommendation');
      cy.intercept('POST', '**/api/v1/tasks').as('createTaskRequest');

      cy.visit('/#/tasks/create');
      cy.location('hash').should('eq', '#/tasks/create');
      cy.get('[data-cy="fair-chore-suggestion"]', { timeout: 10000 }).should('be.visible');
      cy.get('[data-cy="fair-chore-suggestion"]').contains('Fair chore suggestion').should('be.visible');
      cy.wait('@fairRecommendation')
        .its('response.body.recommendedEmail')
        .should('eq', roommate.email);
      cy.contains(roommate.email).should('be.visible');

      cy.get('[data-cy="fair-chore-use-suggestion"]').click();
      cy.location('hash').should('eq', '#/tasks/create');
      cy.get('#assignedToEmail').should('have.value', roommate.email);
      cy.get('#title').clear().type(suggestedTaskTitle);
      cy.get('#description').clear().type('Created using the fair balancing suggestion');
      cy.get('#dueDate').clear().type(dueDate);
      cy.contains('button', 'Create Task').click();
      cy.wait('@createTaskRequest')
        .its('response.body.assignedToEmail')
        .should('eq', roommate.email);

      cy.location('hash').should('include', '#/tasks');
      cy.contains(suggestedTaskTitle).should('be.visible');
      cy.contains(roommate.email).should('be.visible');
    });
  });
});
