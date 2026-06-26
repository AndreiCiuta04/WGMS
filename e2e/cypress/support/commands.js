Cypress.Commands.add('loginAdmin', () => {
    cy.visit('/#/login');
    cy.get('input[name="email"]').clear().type('admin@email.com');
    cy.get('input[name="password"]').clear().type('password');
    cy.contains('button', 'Login').click();

    // The generateData profile creates this admin account for e2e tests.
    cy.location('hash').should('eq', '#/');
});

Cypress.Commands.add('createMessage', (msg) => {
    cy.visit('/#/message');

    cy.contains('button', 'Add message').click();
    cy.get('input[name="title"]').type('title' + msg);
    cy.get('textarea[name="summary"]').type('summary' + msg);
    cy.get('textarea[name="text"]').type('text' + msg);
    cy.get('button[id="add-msg"]').click();
    cy.get('button[id="close-modal-btn"]').click();

    cy.visit('/#/message');

    cy.contains('title' + msg).should('be.visible');
    cy.contains('summary' + msg).should('be.visible');
});

Cypress.Commands.add('registerUser', ({ firstName, lastName, email, password }) => {
    cy.visit('/#/register');

    cy.get('input[name="firstName"]').clear().type(firstName);
    cy.get('input[name="lastName"]').clear().type(lastName);
    cy.get('input[name="email"]').clear().type(email);
    cy.get('input[name="password"]').clear().type(password);
    cy.get('[data-cy="registration-terms-checkbox"]').check();

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
    cy.get('.header-logout-btn', { timeout: 10000 }).should('be.visible').click();

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

    cy.location('hash', { timeout: 10000 }).should('eq', '#/');
});

Cypress.Commands.add('createInvitation', ({ invitedEmail }) => {
    const runToken = `${Date.now()}_${Math.floor(Math.random() * 100000)}`;
    const householdAlias = `getHousehold_${runToken}`;
    const invitationsAlias = `getInvitations_${runToken}`;

    cy.intercept('GET', '**/api/v1/households/me').as(householdAlias);
    cy.intercept('GET', '**/api/v1/households/*/invitations').as(invitationsAlias);
    cy.visit('/#/household/invitations');
    cy.location('hash', { timeout: 10000 }).should('eq', '#/household/invitations');

    cy.wait(`@${householdAlias}`, { timeout: 10000 }).its('response.statusCode').should('eq', 200);
    cy.wait(`@${invitationsAlias}`, { timeout: 10000 });
    cy.get('#expiry', { timeout: 10000 }).should('be.visible');

    if (invitedEmail) {
        cy.get('#email').should('be.visible').clear().type(invitedEmail);
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

// Confirms the shared in-app confirmation dialog (replaces the old native window.confirm()).
Cypress.Commands.add('confirmDialog', () => {
    cy.get('[data-cy="confirm-dialog-confirm"]').should('be.visible').click();
    cy.get('[data-cy="confirm-dialog"]').should('not.exist');
});

// Visible label for each QuantityUnit (the <select> uses [ngValue], so the DOM value
// is not the enum string — we select by the rendered label instead).
const QUANTITY_UNIT_LABEL = {
    MILLILITER: 'ml',
    LITER: 'L',
    GRAM: 'g',
    KILOGRAM: 'kg',
    PIECE: 'pcs',
    PACK: 'pack'
};

// Fills the structured shopping quantity (count stepper + amount + unit) on the create/edit form.
// Pass { count, amount, unit } — all optional. Assumes the count starts empty (create form).
Cypress.Commands.add('fillShoppingQuantity', ({ count, amount, unit } = {}) => {
    if (count) {
        for (let i = 0; i < count; i++) {
            cy.get('[data-cy="quantity-count-increment"]').click();
        }
    }
    if (amount !== undefined && amount !== null && amount !== '') {
        cy.get('[data-cy="quantity-amount"]').clear().type(String(amount));
    }
    if (unit) {
        cy.get('[data-cy="quantity-unit"]').select(QUANTITY_UNIT_LABEL[unit] || unit);
    }
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
