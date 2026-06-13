const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function todayIso() {
  const date = new Date();
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

describe('UC-03 Cleaning Schedule Generates Task E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('UC-03 positive: schedule appears, generates a recognizable task, and can be paused/resumed', () => {
    const runId = Date.now();
    const user = {
      firstName: 'Uc03',
      lastName: 'Cleaner',
      email: uniqueEmail('uc03.cleaner'),
      password
    };
    const scheduleTitle = `UC03 Cleaning ${runId}`;

    cy.registerUser(user);
    cy.createHousehold({ name: `UC03 WG ${runId}`, address: 'UC03 Street' });

    cy.visit('/#/cleaning-schedules/create');
    cy.get('#title').clear().type(scheduleTitle);
    cy.get('#description').clear().type('UC03 schedule generated task');
    cy.get('#frequency').select('DAILY');
    cy.get('#assignedToEmail').select(user.email);
    cy.get('#startDate').clear().type(todayIso());
    cy.contains('button', 'Create Schedule').click();

    cy.location('hash').should('eq', '#/cleaning-schedules');
    cy.contains('.due-item', scheduleTitle).should('be.visible');

    cy.contains('.manage-header', 'Manage Schedules').click();
    cy.contains('.manage-item', scheduleTitle).find('button[title="Pause"]').click();
    cy.contains('.manage-item', scheduleTitle).contains('Paused').should('be.visible');
    cy.contains('.manage-item', scheduleTitle).find('button[title="Resume"]').click();
    cy.contains('.manage-item', scheduleTitle).contains('Paused').should('not.exist');

    cy.contains('.due-item', scheduleTitle).contains('button', 'Generate Task').click();
    cy.contains(`Task created for "${scheduleTitle}"!`).should('be.visible');

    cy.visit('/#/tasks');
    cy.contains('.wgms-card', scheduleTitle).within(() => {
      cy.contains('Open').should('be.visible');
      cy.contains(user.email).should('be.visible');
    });
  });

  it('UC-03 negative: invalid schedule form is blocked and generated schedule deletion is rejected', () => {
    const runId = Date.now();
    const user = {
      firstName: 'Uc03',
      lastName: 'Negative',
      email: uniqueEmail('uc03.negative'),
      password
    };
    const scheduleTitle = `UC03 Protected Cleaning ${runId}`;

    cy.registerUser(user);
    cy.createHousehold({ name: `UC03 Negative WG ${runId}`, address: 'UC03 Negative Street' });

    cy.visit('/#/cleaning-schedules/create');
    cy.intercept('POST', '**/api/v1/cleaning-schedules').as('createScheduleRequest');
    cy.contains('button', 'Create Schedule').click();
    cy.contains('Title is required.').should('be.visible');
    cy.contains('Frequency is required.').should('be.visible');
    cy.contains('Start date is required.').should('be.visible');
    cy.get('@createScheduleRequest.all').should('have.length', 0);

    cy.get('#title').clear().type(scheduleTitle);
    cy.get('#frequency').select('DAILY');
    cy.get('#assignedToEmail').select(user.email);
    cy.get('#startDate').clear().type(todayIso());
    cy.contains('button', 'Create Schedule').click();
    cy.wait('@createScheduleRequest').its('response.statusCode').should('eq', 201);

    cy.contains('.due-item', scheduleTitle).contains('button', 'Generate Task').click();
    cy.contains(`Task created for "${scheduleTitle}"!`).should('be.visible');

    cy.contains('.manage-header', 'Manage Schedules').click();
    cy.contains('.manage-item', scheduleTitle).find('button[title="Delete"]').click();
    cy.confirmDialog();
    cy.contains(/open tasks|complete or cancel|generated tasks|Cannot delete|Pause it|Request conflicts|conflict/i)
      .should('be.visible');
  });
});
