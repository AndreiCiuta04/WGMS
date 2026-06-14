const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function futureDateIso(daysAhead = 2) {
  const date = new Date();
  date.setDate(date.getDate() + daysAhead);
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

describe('UC-02 Household Task Lifecycle E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('UC-02 positive: assigned task is visible to assignee, completed, and visible to creator/history', () => {
    const runId = Date.now();
    const owner = {
      firstName: 'Uc02',
      lastName: 'Owner',
      email: uniqueEmail('uc02.owner'),
      password
    };
    const assignee = {
      firstName: 'Uc02',
      lastName: 'Assignee',
      email: uniqueEmail('uc02.assignee'),
      password
    };
    const task = {
      title: `UC02 Assigned Task ${runId}`,
      description: 'UC02 household task lifecycle',
      dueDate: futureDateIso(),
      assignedToEmail: assignee.email
    };

    cy.registerUser(owner);
    cy.createHousehold({ name: `UC02 WG ${runId}`, address: 'UC02 Street' });
    cy.createInvitation({ invitedEmail: assignee.email }).then((inviteUrl) => {
      cy.logoutUser();
      cy.visitInviteUrl(inviteUrl);
      cy.get('input[formcontrolname="firstName"]').clear().type(assignee.firstName);
      cy.get('input[formcontrolname="lastName"]').clear().type(assignee.lastName);
      cy.get('input[formcontrolname="password"]').clear().type(assignee.password);
      cy.get('input[formcontrolname="termsAccepted"]').check();
      cy.contains('button', 'Register & Join WG').click();
      cy.contains('You have successfully joined the household!').should('be.visible');
      cy.location('hash', { timeout: 3000 }).should('eq', '#/');

      cy.logoutUser();
      cy.loginUser(owner.email, owner.password);
      cy.createTask(task);

      cy.logoutUser();
      cy.loginUser(assignee.email, assignee.password);
      cy.visit('/#/tasks?tab=mine');
      cy.contains('.nav-link.active', 'My Tasks').should('be.visible');
      cy.contains('.wgms-card', task.title).should('be.visible');
      cy.contains('.wgms-card', task.title).contains('button', 'Mark as Done').click();
      cy.contains('.wgms-card', task.title).contains('Done').should('be.visible');

      cy.logoutUser();
      cy.loginUser(owner.email, owner.password);
      cy.visit('/#/tasks');
      cy.contains('.wgms-card', task.title).contains('Done').should('be.visible');

      cy.visit('/#/history');
      cy.contains('h2', 'History').should('be.visible');
      cy.contains(task.title).should('be.visible');
      cy.contains('DONE').should('be.visible');
    });
  });

  it('UC-02 negative: empty task form and no-WG task creation are handled', () => {
    const user = {
      firstName: 'Uc02',
      lastName: 'Validation',
      email: uniqueEmail('uc02.validation'),
      password
    };

    cy.registerUser(user);
    cy.createHousehold({ name: `UC02 Validation WG ${Date.now()}`, address: 'UC02 Validation Street' });
    cy.visit('/#/tasks/create');
    cy.intercept('POST', '**/api/v1/tasks').as('createTaskRequest');
    cy.contains('button', 'Create Task').click();
    cy.get('@createTaskRequest.all').should('have.length', 0);

    const noWg = {
      firstName: 'Uc02',
      lastName: 'NoWg',
      email: uniqueEmail('uc02.no.wg'),
      password
    };
    cy.logoutUser();
    cy.registerUser(noWg);
    cy.visit('/#/tasks/create');
    cy.contains(/household|WG|create or join|could not load/i, { timeout: 5000 })
      .should('be.visible');
  });
});
