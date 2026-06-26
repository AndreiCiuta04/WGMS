describe('FR-01 FR-02 FR-03 frontend workflow', () => {
    const password = 'Password123!';
    const runId = Date.now();

    const anna = {
        firstName: 'Anna',
        lastName: 'FrontendQa',
        email: `anna.frontend.qa.${runId}@test.com`,
        password
    };

    const lukas = {
        firstName: 'Lukas',
        lastName: 'FrontendQa',
        email: `lukas.frontend.qa.${runId}@test.com`,
        password
    };

    const household = {
        name: `Frontend QA WG ${runId}`,
        address: 'Frontend Test Street 1, Vienna'
    };

    const task = {
        title: `Clean kitchen ${runId}`,
        description: 'Frontend Cypress QA task',
        dueDate: '2026-12-31'
    };

    beforeEach(() => {
        cy.clearLocalStorage();
    });

    it('FR-01 validates registration and login forms', () => {
        cy.visit('/#/register');

        cy.contains('button', 'Register').should('be.disabled');

        cy.get('input[name="email"]').type('not-an-email');
        cy.get('input[name="password"]').type('weak');

        cy.contains('button', 'Register').should('be.disabled');

        cy.visit('/#/login');

        cy.contains('button', 'Login').click();

        cy.contains('Email is required').should('be.visible');
        cy.contains('Password is required').should('be.visible');
    });

    it('FR-01 FR-02 FR-03 supports register, login, WG creation, invitation acceptance, task assignment, and task visibility', () => {
        // FR-01: Anna registers and is automatically authenticated.
        cy.registerUser(anna);

        cy.contains('a', 'Logout').should('be.visible');
        cy.contains('a', 'Tasks').should('be.visible');

        // FR-02: Anna creates a WG and becomes able to manage invitations.
        cy.createHousehold(household);

        cy.visit('/#/household/members');
        cy.contains('My WG').should('be.visible');
        cy.contains(household.name).should('be.visible');

        // FR-02: Anna creates an invitation for Lukas.
        cy.createInvitation({ invitedEmail: lukas.email }).then((inviteUrl) => {
            cy.logoutUser();

            // FR-02: Lukas opens the invitation link and registers directly from it.
            cy.visitInviteUrl(inviteUrl);

            cy.contains('Join a Household').should('be.visible');
            cy.contains(household.name).should('be.visible');

            cy.get('input[formcontrolname="firstName"]').clear().type(lukas.firstName);
            cy.get('input[formcontrolname="lastName"]').clear().type(lukas.lastName);
            cy.get('input[formcontrolname="email"]').should('have.value', lukas.email);
            cy.get('input[formcontrolname="password"]').clear().type(lukas.password);
            cy.get('input[formcontrolname="termsAccepted"]').check();

            cy.intercept('POST', '**/api/v1/users').as('registerLukasRequest');
            cy.intercept('POST', '**/api/v1/invitations/*/accept').as('acceptInvitationRequest');

            cy.contains('button', 'Register & Join WG').click();

            cy.wait('@registerLukasRequest')
                .its('response.statusCode')
                .should('be.oneOf', [200, 201]);

            cy.wait('@acceptInvitationRequest')
                .its('response.statusCode')
                .should('eq', 200);

            cy.contains('You have successfully joined the household!').should('be.visible');
            cy.location('hash', { timeout: 3000 }).should('eq', '#/');

            // FR-01: Logout and login again as Anna.
            cy.logoutUser();
            cy.loginUser(anna.email, anna.password);

            // FR-03: Anna creates a task assigned to Lukas.
            cy.createTask({
                title: task.title,
                description: task.description,
                dueDate: task.dueDate,
                assignedToEmail: lukas.email
            });

            cy.contains(task.title).should('be.visible');
            cy.contains(task.description).should('be.visible');
            cy.contains(lukas.email).should('be.visible');

            // FR-01 + FR-03: Lukas logs in and sees his assigned task under My Tasks.
            cy.logoutUser();
            cy.loginUser(lukas.email, lukas.password);

            cy.visit('/#/tasks');
            cy.contains('a', 'My Tasks').click();

            cy.contains(task.title).should('be.visible');
            cy.contains(task.description).should('be.visible');
            cy.contains(lukas.email).should('be.visible');

            // FR-03: Lukas can mark the assigned task as done.
            cy.intercept('PATCH', '**/api/v1/tasks/*/status').as('updateTaskStatusRequest');

            cy.contains('button', 'Mark as Done').click();

            cy.wait('@updateTaskStatusRequest')
                .its('response.statusCode')
                .should('eq', 200);

            cy.contains('Done').should('be.visible');
        });
    });

    it('FR-01 protects authenticated pages from logged-out users', () => {
        cy.visit('/#/tasks');

        cy.url().should('include', '/login');

        cy.visit('/#/household/create');

        cy.url().should('include', '/login');

        cy.visit('/#/household/invitations');

        cy.url().should('include', '/login');
    });
});
