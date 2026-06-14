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
    cy.contains('Fair chore suggestion', { timeout: 10000 }).should('be.visible');

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
        candidate: false
      });
      expect(annaFair.fairnessScore).to.eq(2.5);

      expect(lightWorker).to.include({
        completedTasks: 1,
        openTasks: 0,
        overdueTasks: 0,
        candidate: true,
        recommended: true
      });
      expect(lightWorker.fairnessScore).to.eq(1);

      expect(heavyWorker).to.include({
        completedTasks: 5,
        openTasks: 1,
        overdueTasks: 0,
        candidate: false
      });
      expect(heavyWorker.fairnessScore).to.eq(5.5);

      expect(mediumWorker).to.include({
        completedTasks: 3,
        openTasks: 0,
        overdueTasks: 1,
        candidate: false
      });
      expect(mediumWorker.fairnessScore).to.eq(4);
    });

    cy.contains('Light Worker').should('be.visible');
    cy.contains(fr15LightWorkerEmail).should('be.visible');

    cy.get('.fair-chore-suggestion').contains('button', 'Use suggestion').click();
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
      cy.contains('Fair chore suggestion', { timeout: 10000 }).should('be.visible');
      cy.wait('@fairRecommendation')
        .its('response.body.recommendedEmail')
        .should('eq', roommate.email);
      cy.contains(roommate.email).should('be.visible');

      cy.get('.fair-chore-suggestion').contains('button', 'Use suggestion').click();
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
