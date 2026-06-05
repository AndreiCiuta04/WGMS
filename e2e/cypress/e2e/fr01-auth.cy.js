const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function registerViaApi(user) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/users`,
    body: {
      email: user.email,
      password: user.password,
      firstName: user.firstName,
      lastName: user.lastName,
      termsAccepted: true
    }
  });
}

describe('FR-01 Authentication and Registration E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('FR-01 positive: registers a user, logs in, reaches dashboard, and logs out', () => {
    const user = {
      firstName: 'Auth',
      lastName: 'Qa',
      email: uniqueEmail('auth.qa'),
      password
    };

    cy.registerUser(user);

    cy.location('hash').should('eq', '#/');
    cy.contains(/Welcome home|Welcome to WGMS!/).should('be.visible');
    cy.window().its('localStorage.authToken').should('exist');

    cy.logoutUser();
    cy.location('hash').should('eq', '#/');
    cy.window().its('localStorage.authToken').should('not.exist');

    cy.loginUser(user.email, user.password);
    cy.location('hash').should('eq', '#/');
    cy.contains(/Welcome home|Welcome to WGMS!/).should('be.visible');
  });

  it('FR-01 negative: invalid login and duplicate registration show readable errors', () => {
    const user = {
      firstName: 'Duplicate',
      lastName: 'Qa',
      email: uniqueEmail('duplicate.auth.qa'),
      password
    };

    registerViaApi(user).its('status').should('be.oneOf', [200, 201]);

    cy.visit('/#/login');
    cy.get('input[name="email"]').clear().type(user.email);
    cy.get('input[name="password"]').clear().type('Wrong123!');
    cy.contains('button', 'Login').click();
    cy.contains('Authentication problem').should('be.visible');
    cy.contains(/invalid|incorrect|bad|credential|wrong|failed/i).should('be.visible');

    cy.visit('/#/register');
    cy.get('input[name="firstName"]').clear().type(user.firstName);
    cy.get('input[name="lastName"]').clear().type(user.lastName);
    cy.get('input[name="email"]').clear().type(user.email);
    cy.get('input[name="password"]').clear().type(user.password);
    cy.get('[data-cy="registration-terms-checkbox"]').check();
    cy.contains('button', 'Register').click();

    cy.contains('Registration failed').should('be.visible');
    cy.contains('Email is already registered.').should('be.visible');
  });

  it('FR-01 negative: malformed login and weak registration are blocked before backend requests', () => {
    cy.visit('/#/login');
    cy.intercept('POST', '**/api/v1/authentication').as('loginRequest');
    cy.get('input[name="email"]').clear().type('not-an-email');
    cy.get('input[name="password"]').clear().type(password);
    cy.contains('button', 'Login').click();
    cy.contains('Please provide a valid email').should('be.visible');
    cy.get('@loginRequest.all').should('have.length', 0);

    cy.visit('/#/register');
    cy.intercept('POST', '**/api/v1/users').as('registerRequest');
    cy.get('input[name="firstName"]').clear().type('Weak');
    cy.get('input[name="lastName"]').clear().type('Password');
    cy.get('input[name="email"]').clear().type(uniqueEmail('weak.password'));
    cy.get('input[name="password"]').clear().type('weak');
    cy.get('[data-cy="registration-terms-checkbox"]').check();
    cy.contains('button', 'Register').click();
    cy.contains('At least 8 characters required').should('be.visible');
    cy.get('@registerRequest.all').should('have.length', 0);
  });

  it('FR-01 negative: logged-out users are redirected away from authenticated pages', () => {
    cy.visit('/#/tasks');
    cy.url().should('include', '/login');

    cy.visit('/#/household/create');
    cy.url().should('include', '/login');

    cy.visit('/#/household/invitations');
    cy.url().should('include', '/login');
  });
});
