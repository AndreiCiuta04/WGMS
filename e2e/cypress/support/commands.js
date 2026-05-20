Cypress.Commands.add('loginAdmin', () => {
    cy.fixture('settings').then(settings => {
        cy.visit(settings.baseUrl);
        cy.contains('a', 'Login').click();
        cy.get('input[name="email"]').type(settings.adminUser);
        cy.get('input[name="password"]').type(settings.adminPw);
        cy.contains('button', 'Login').click();

        // Make sure login finished before the next Cypress command continues.
        cy.url().should('not.include', '/login');
    });
});

Cypress.Commands.add('createMessage', (msg) => {
    cy.fixture('settings').then(settings => {
        const baseUrl = settings.baseUrl.replace(/\/$/, '');

        // App uses hash routing, so /message is reached as /#/message.
        cy.visit(`${baseUrl}/#/message`);

        cy.contains('button', 'Add message').click();
        cy.get('input[name="title"]').type('title' + msg);
        cy.get('textarea[name="summary"]').type('summary' + msg);
        cy.get('textarea[name="text"]').type('text' + msg);
        cy.get('button[id="add-msg"]').click();
        cy.get('button[id="close-modal-btn"]').click();

        cy.visit(`${baseUrl}/#/message`);

        cy.contains('title' + msg).should('be.visible');
        cy.contains('summary' + msg).should('be.visible');
    });
});

Cypress.Commands.add('registerUser', ({ firstName, lastName, email, password }) => {
    cy.visit('/#/register');

    cy.get('input[name="firstName"]').clear().type(firstName);
    cy.get('input[name="lastName"]').clear().type(lastName);
    cy.get('input[name="email"]').clear().type(email);
    cy.get('input[name="password"]').clear().type(password);

    cy.intercept('POST', '**/api/v1/users').as('registerRequest');

    cy.contains('button', 'Register').click();

    cy.wait('@registerRequest')
        .its('response.statusCode')
        .should('be.oneOf', [200, 201]);

    cy.window()
        .its('localStorage.authToken')
        .should('exist');

    cy.location('hash').should('eq', '#/');
});

Cypress.Commands.add('loginUser', (email, password) => {
    cy.visit('/#/login');

    cy.get('input[name="email"]').clear().type(email);
    cy.get('input[name="password"]').clear().type(password);

    cy.intercept('POST', '**/api/v1/authentication').as('loginRequest');

    cy.contains('button', 'Login').click();

    cy.wait('@loginRequest')
        .its('response.statusCode')
        .should('eq', 200);

    cy.window()
        .its('localStorage.authToken')
        .should('exist');

    cy.location('hash').should('eq', '#/');
});

Cypress.Commands.add('logoutUser', () => {
    cy.contains('a', 'Logout').click();

    cy.window()
        .its('localStorage.authToken')
        .should('not.exist');
});

Cypress.Commands.add('createHousehold', ({ name, address }) => {
    cy.visit('/#/household/create');

    cy.get('#name').clear().type(name);

    if (address) {
        cy.get('#address').clear().type(address);
    }

    cy.intercept('POST', '**/api/v1/households').as('createHouseholdRequest');

    cy.contains('button', 'Initialize WG').click();

    cy.wait('@createHouseholdRequest')
        .its('response.statusCode')
        .should('eq', 201);
});

Cypress.Commands.add('createInvitation', ({ invitedEmail }) => {
    cy.visit('/#/household/invitations');

    cy.contains('Manage Invitations').should('be.visible');

    if (invitedEmail) {
        cy.get('#email').clear().type(invitedEmail);
    }

    cy.get('#expiry').select('168');

    cy.intercept('POST', '**/api/v1/households/*/invitations').as('createInvitationRequest');

    cy.contains('button', 'Create').click();

    cy.wait('@createInvitationRequest')
        .its('response.statusCode')
        .should('eq', 201);

    cy.contains('Invitation Link Created').should('be.visible');

    return cy.get('input[readonly]')
        .invoke('val')
        .then((inviteUrl) => {
            expect(inviteUrl).to.be.a('string').and.not.be.empty;
            return inviteUrl;
        });
});

Cypress.Commands.add('visitInviteUrl', (inviteUrl) => {
    const rawUrl = String(inviteUrl);
    const token = rawUrl.substring(rawUrl.lastIndexOf('/') + 1);

    expect(token).to.not.equal('');
    cy.visit(`/#/invite/${token}`);
});

Cypress.Commands.add('createTask', ({ title, description, dueDate, assignedToEmail }) => {
    cy.window()
        .its('localStorage.authToken')
        .should('exist');

    cy.visit('/#/tasks/create');

    cy.location('hash').should('eq', '#/tasks/create');
    cy.contains('h2', 'Create Task').should('be.visible');

    cy.get('#title')
        .should('be.visible')
        .clear()
        .type(title);

    if (description) {
        cy.get('#description')
            .should('be.visible')
            .clear()
            .type(description);
    }

    cy.location('hash').should('eq', '#/tasks/create');

    cy.get('#dueDate')
        .should('be.visible')
        .clear()
        .type(dueDate);

    cy.get('#assignedToEmail')
        .should('be.visible')
        .find(`option[value="${assignedToEmail}"]`)
        .should('exist');

    cy.get('#assignedToEmail')
        .select(assignedToEmail);

    cy.intercept('POST', '**/api/v1/tasks').as('createTaskRequest');

    cy.contains('button', 'Create Task').click();

    cy.wait('@createTaskRequest')
        .its('response.statusCode')
        .should('eq', 201);

    cy.location('hash').should('include', '#/tasks');
});