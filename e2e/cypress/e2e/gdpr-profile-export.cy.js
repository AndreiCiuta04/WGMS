const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';
const sensitiveKeys = ['password', 'passwordHash', 'token', 'jwt', 'secret'];

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function uniqueUser(prefix) {
  return {
    firstName: 'Gdpr',
    lastName: 'Profile',
    email: uniqueEmail(prefix),
    password
  };
}

function expectNoSensitiveKeys(value) {
  const serialized = JSON.stringify(value).toLowerCase();
  sensitiveKeys.forEach(key => {
    expect(serialized).not.to.include(key.toLowerCase());
  });
}

describe('GDPR profile, export, correction, and anonymization', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('shows safe profile data, exports safe JSON, and blocks profile access after logout', () => {
    const user = uniqueUser('gdpr.profile.export');
    const household = {
      name: `GDPR Export WG ${Date.now()}`,
      address: 'GDPR Export Street 1'
    };

    cy.registerUser(user);
    cy.createHousehold(household);
    cy.logoutUser();
    cy.loginUser(user.email, user.password);

    cy.intercept('GET', '**/api/v1/users/me').as('loadProfile');
    cy.visit('/#/profile');
    cy.wait('@loadProfile').its('response.statusCode').should('eq', 200);
    cy.get('[data-cy="profile-page"]').should('be.visible');
    cy.get('[data-cy="profile-data"]').should('contain', user.email);
    cy.get('[data-cy="profile-data"]').should('contain', user.firstName);
    cy.get('[data-cy="profile-data"]').should('contain', user.lastName);
    cy.contains(household.name).should('be.visible');

    cy.get('[data-cy="profile-page"]').invoke('text').then(text => {
      const normalized = text.toLowerCase();
      sensitiveKeys.forEach(key => {
        expect(normalized).not.to.include(key.toLowerCase());
      });
    });

    cy.intercept('GET', '**/api/v1/users/me/export').as('exportData');
    cy.get('[data-cy="profile-export-button"]').click();
    cy.wait('@exportData').then(({ response }) => {
      expect(response?.statusCode).to.eq(200);
      expect(response?.body).to.have.property('generatedAt');
      expect(response?.body).to.have.property('account');
      expect(response?.body.account.email).to.eq(user.email);
      expect(response?.body.householdMembership.householdName).to.eq(household.name);
      expectNoSensitiveKeys(response?.body);
    });
    cy.get('[data-cy="profile-export-success"]').should('contain', 'Data export generated.');

    cy.logoutUser();
    cy.visit('/#/profile');
    cy.location('hash').should('include', '/login');

    cy.request({
      method: 'GET',
      url: `${apiUrl}/users/me`,
      failOnStatusCode: false
    }).its('status').should('be.oneOf', [401, 403]);
  });

  it('persists profile corrections after save and reload', () => {
    const user = uniqueUser('gdpr.profile.correction');
    const updatedFirstName = `Corrected${Date.now()}`;
    const updatedLastName = 'Reloaded';

    cy.registerUser(user);
    cy.visit('/#/profile');
    cy.get('[data-cy="profile-edit-link"]').click();
    cy.location('hash').should('eq', '#/profile/edit');

    cy.get('[data-cy="profile-edit-form"]').should('be.visible');
    cy.get('[data-cy="profile-first-name-input"]').clear().type(updatedFirstName);
    cy.get('[data-cy="profile-last-name-input"]').clear().type(updatedLastName);

    cy.intercept('PATCH', '**/api/v1/users/me').as('saveProfile');
    cy.get('[data-cy="profile-save-button"]').click();
    cy.wait('@saveProfile').its('response.statusCode').should('eq', 200);
    cy.get('[data-cy="profile-save-success"]').should('contain', 'Profile changes saved.');

    cy.reload();
    cy.get('[data-cy="profile-first-name-input"]').should('have.value', updatedFirstName);
    cy.get('[data-cy="profile-last-name-input"]').should('have.value', updatedLastName);

    cy.visit('/#/profile');
    cy.get('[data-cy="profile-data"]').should('contain', updatedFirstName);
    cy.get('[data-cy="profile-data"]').should('contain', updatedLastName);
  });

  it('anonymizes a throwaway account and prevents future login', () => {
    const user = uniqueUser('gdpr.delete.throwaway');

    cy.registerUser(user);
    cy.visit('/#/profile');
    cy.get('[data-cy="profile-page"]').should('be.visible');

    cy.intercept('DELETE', '**/api/v1/users/me').as('deleteAccount');
    cy.get('[data-cy="profile-delete-account-button"]').click();
    cy.confirmDialog();
    cy.wait('@deleteAccount').its('response.statusCode').should('eq', 204);

    cy.location('hash').should('include', '/login');
    cy.window().its('localStorage.authToken').should('not.exist');

    cy.visit('/#/login');
    cy.get('input[name="email"]').clear().type(user.email);
    cy.get('input[name="password"]').clear().type(user.password);
    cy.intercept('POST', '**/api/v1/authentication').as('loginAfterDelete');
    cy.contains('button', 'Login').click();
    cy.wait('@loginAfterDelete').its('response.statusCode').should('eq', 401);
    cy.contains('Authentication problem').should('be.visible');
  });
});
