const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function futureDateIso(daysAhead = 3) {
  const date = new Date();
  date.setDate(date.getDate() + daysAhead);
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

describe('FR-03 Task Creation and Assignment E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('FR-03 positive: user A assigns task to user B and user B marks it DONE', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'Task',
      lastName: 'Owner',
      email: uniqueEmail('task.owner'),
      password
    };
    const roommate = {
      firstName: 'Task',
      lastName: 'Roommate',
      email: uniqueEmail('task.roommate'),
      password
    };
    const task = {
      title: `Assigned E2E Task ${runId}`,
      description: 'Task assigned through the browser workflow',
      dueDate: futureDateIso(),
      assignedToEmail: roommate.email
    };

    cy.registerUser(owner);
    cy.createHousehold({
      name: `Tasks WG ${runId}`,
      address: 'Task Test Street 1'
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
      cy.createTask(task);
      cy.contains(task.title).should('be.visible');
      cy.contains(roommate.email).should('be.visible');

      cy.logoutUser();
      cy.loginUser(roommate.email, roommate.password);
      cy.visit('/#/tasks?tab=mine');
      cy.contains('.nav-link.active', 'My Tasks').should('be.visible');
      cy.contains(task.title).should('be.visible');
      cy.contains(task.description).should('be.visible');

      cy.intercept('PATCH', '**/api/v1/tasks/*/status').as('completeTask');
      cy.contains('.wgms-card', task.title).contains('button', 'Mark as Done').click();
      cy.wait('@completeTask').its('response.statusCode').should('eq', 200);
      cy.contains('.wgms-card', task.title).contains('DONE').should('be.visible');
    });
  });

  it('FR-03 negative: empty task creation is blocked before backend request', () => {
    const user = {
      firstName: 'Task',
      lastName: 'Validation',
      email: uniqueEmail('task.validation'),
      password
    };

    cy.registerUser(user);
    cy.createHousehold({
      name: `Task Validation WG ${Date.now()}`,
      address: 'Validation Street 1'
    });

    cy.visit('/#/tasks/create');
    cy.contains('h2', 'Create Task').should('be.visible');
    cy.intercept('POST', '**/api/v1/tasks').as('createTaskRequest');
    cy.contains('button', 'Create Task').click();
    cy.location('hash').should('eq', '#/tasks/create');
    cy.get('@createTaskRequest.all').should('have.length', 0);
  });

  it('FR-03 negative: user without WG sees readable task-creation guidance', () => {
    const user = {
      firstName: 'Task',
      lastName: 'NoWg',
      email: uniqueEmail('task.no.wg'),
      password
    };

    cy.registerUser(user);
    cy.visit('/#/tasks/create');

    cy.contains('Create Task').should('be.visible');
    cy.contains(/household|WG|create or join|could not load/i, { timeout: 5000 })
      .should('be.visible');
  });
});
