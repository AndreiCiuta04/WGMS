const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

describe('UC-07 Navigation Smoke E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
    cy.viewport(1280, 900);
  });

  it('UC-07 positive: logged-in user can navigate all major app pages without crashes', () => {
    const runId = Date.now();
    const user = {
      firstName: 'Nav',
      lastName: 'Smoke',
      email: uniqueEmail('nav.smoke'),
      password
    };
    const householdName = `Navigation Smoke WG ${runId}`;

    cy.registerUser(user);
    cy.createHousehold({
      name: householdName,
      address: 'Navigation Street 1'
    });

    cy.contains('a', 'Dashboard').click();
    cy.location('hash').should('eq', '#/');
    cy.contains('h2', 'Welcome home').should('be.visible');

    cy.contains('a', 'Tasks').click();
    cy.location('hash').should('eq', '#/tasks');
    cy.contains('h2', 'Tasks').should('be.visible');

    cy.contains('a', 'Cleaning').click();
    cy.location('hash').should('eq', '#/cleaning-schedules');
    cy.contains('h2', 'Cleaning').should('be.visible');

    cy.contains('a', 'Expenses').click();
    cy.location('hash').should('eq', '#/expenses');
    cy.contains('h2', 'Expenses').should('be.visible');

    cy.contains('a', 'Shopping').click();
    cy.location('hash').should('eq', '#/shopping-items');
    cy.contains('h2', 'Shopping List').should('be.visible');

    cy.contains('a', 'My WG').click();
    cy.location('hash').should('eq', '#/household/members');
    cy.contains('h1', householdName).should('be.visible');
    cy.contains('h3', 'Household Members').should('be.visible');

    cy.contains('a', 'History').click();
    cy.location('hash').should('eq', '#/history');
    cy.contains('h2', 'History').should('be.visible');
  });
});
