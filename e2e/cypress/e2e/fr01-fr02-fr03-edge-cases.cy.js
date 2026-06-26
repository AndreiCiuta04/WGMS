describe('FR-01 FR-02 FR-03 frontend edge cases', () => {
    const password = 'Password123!';
    const runId = Date.now();

    const userWithoutWg = {
        firstName: 'NoWg',
        lastName: 'FrontendQa',
        email: `no.wg.frontend.qa.${runId}@test.com`,
        password
    };

    const mainTenant = {
        firstName: 'Main',
        lastName: 'TenantQa',
        email: `main.tenant.frontend.qa.${runId}@test.com`,
        password
    };

    const household = {
        name: `Frontend Edge WG ${runId}`,
        address: 'Frontend Edge Street 1, Vienna'
    };

    beforeEach(() => {
        cy.clearLocalStorage();
    });

    it('FR-01 blocks malformed login email before sending authentication request', () => {
        cy.visit('/#/login');

        cy.intercept('POST', '**/api/v1/authentication').as('loginRequest');

        cy.get('input[name="email"]').clear().type('not-an-email');
        cy.get('input[name="password"]').clear().type(password);

        cy.contains('button', 'Login').click();

        cy.contains('Please provide a valid email').should('be.visible');
        cy.location('hash').should('eq', '#/login');

        cy.get('@loginRequest.all').should('have.length', 0);
    });

    it('FR-01 blocks weak registration password before sending registration request', () => {
        cy.visit('/#/register');

        cy.intercept('POST', '**/api/v1/users').as('registerRequest');

        cy.get('input[name="firstName"]').clear().type('Weak');
        cy.get('input[name="lastName"]').clear().type('Password');
        cy.get('input[name="email"]').clear().type(`weak.password.${runId}@test.com`);
        cy.get('input[name="password"]').clear().type('weak');

        cy.contains('button', 'Register').should('be.disabled');

        cy.location('hash').should('eq', '#/register');

        cy.get('@registerRequest.all').should('have.length', 0);
    });

    it('FR-02 blocks invalid invitation email before sending invite request', () => {
        cy.registerUser(mainTenant);
        cy.createHousehold(household);

        cy.visit('/#/household/invitations');

        cy.contains('Manage Invitations').should('be.visible');

        cy.intercept('POST', '**/api/v1/households/*/invitations').as('createInvitationRequest');

        cy.get('#email').clear().type('not-an-email').blur();

        cy.contains('Please provide a valid email address').should('be.visible');
        cy.contains('button', 'Create').should('be.disabled');

        cy.get('@createInvitationRequest.all').should('have.length', 0);
    });

    it('FR-02 shows a readable error for an invalid invitation token', () => {
        cy.visit(`/#/invite/invalid-token-${runId}`);

        cy.location('hash').should('include', '/invite/invalid-token-');

        cy.get('body').should('be.visible');
        cy.contains(/invalid|expired|not found|could not|invitation/i, { timeout: 5000 })
            .should('be.visible');
    });

    it('FR-03 shows a readable message when a logged-in user without WG opens task creation', () => {
        cy.registerUser(userWithoutWg);

        cy.visit('/#/tasks/create');

        cy.contains('Create Task').should('be.visible');
        cy.contains(/household|WG|create or join|could not load/i, { timeout: 5000 })
            .should('be.visible');
    });

    it('FR-03 blocks empty task creation before sending task request', () => {
        cy.registerUser({
            firstName: 'Task',
            lastName: 'ValidationQa',
            email: `task.validation.frontend.qa.${runId}@test.com`,
            password
        });

        cy.createHousehold({
            name: `Task Validation WG ${runId}`,
            address: 'Task Validation Street 1, Vienna'
        });

        cy.visit('/#/tasks/create');

        cy.contains('Create Task').should('be.visible');

        cy.intercept('POST', '**/api/v1/tasks').as('createTaskRequest');

        cy.contains('button', 'Create Task').click();

        cy.location('hash').should('eq', '#/tasks/create');
        cy.get('@createTaskRequest.all').should('have.length', 0);
    });
});
