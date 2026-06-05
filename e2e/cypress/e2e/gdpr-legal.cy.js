const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function fillRegistration(email) {
  cy.get('input[name="firstName"]').clear().type('Gdpr');
  cy.get('input[name="lastName"]').clear().type('Legal');
  cy.get('input[name="email"]').clear().type(email);
  cy.get('input[name="password"]').clear().type(password);
}

function expectRegistrationValues(email) {
  cy.get('input[name="firstName"]').should('have.value', 'Gdpr');
  cy.get('input[name="lastName"]').should('have.value', 'Legal');
  cy.get('input[name="email"]').should('have.value', email);
  cy.get('input[name="password"]').should('have.value', password);
  cy.get('[data-cy="registration-terms-checkbox"]').should('be.checked');
}

describe('GDPR/legal user-facing flow', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('displays the complete legal page', () => {
    cy.visit('/#/legal');

    cy.get('[data-cy="legal-page"]').should('be.visible');
    cy.get('[data-cy="legal-privacy-section"]').should('be.visible');
    cy.get('[data-cy="legal-terms-section"]').should('be.visible');
    cy.get('[data-cy="legal-gdpr-support-section"]').should('be.visible');
    cy.get('[data-cy="legal-limitations-section"]').should('be.visible');

    cy.contains(/university demo/i).should('be.visible');
    cy.contains(/account data/i).should('be.visible');
    cy.contains(/household/i).should('be.visible');
    cy.contains(/tasks/i).should('be.visible');
    cy.contains(/expenses/i).should('be.visible');
    cy.contains(/shopping/i).should('be.visible');
    cy.contains(/export/i).should('be.visible');
    cy.contains(/not a production service/i).should('be.visible');
    cy.contains(/no real money transfers/i).should('be.visible');
  });

  it('displays separate terms and privacy pages', () => {
    cy.visit('/#/terms');
    cy.get('[data-cy="terms-page"]').should('be.visible');
    cy.contains('Terms and Conditions').should('be.visible');
    cy.contains('User responsibilities').should('be.visible');
    cy.contains('Data we store').should('not.exist');

    cy.visit('/#/privacy');
    cy.get('[data-cy="privacy-page"]').should('be.visible');
    cy.contains('Privacy and Data Usage').should('be.visible');
    cy.contains('Data we store').should('be.visible');
    cy.contains('User responsibilities').should('not.exist');
  });

  it('requires legal acknowledgement, links to separate pages, and preserves registration draft', () => {
    const email = uniqueEmail('gdpr.legal.ui');

    cy.visit('/#/register');
    fillRegistration(email);

    cy.get('[data-cy="registration-terms-checkbox"]').should('not.be.checked');
    cy.get('[data-cy="registration-submit"]').should('be.disabled');
    cy.get('[data-cy="registration-terms-checkbox"]').check();

    cy.get('[data-cy="registration-terms-link"]').click();
    cy.location('hash').should('eq', '#/terms');
    cy.get('[data-cy="terms-page"]').should('be.visible');
    cy.contains('Terms and Conditions').should('be.visible');

    cy.go('back');
    cy.location('hash').should('eq', '#/register');
    expectRegistrationValues(email);

    cy.get('[data-cy="registration-privacy-link"]').click();
    cy.location('hash').should('eq', '#/privacy');
    cy.get('[data-cy="privacy-page"]').should('be.visible');
    cy.contains('Privacy and Data Usage').should('be.visible');

    cy.go('back');
    cy.location('hash').should('eq', '#/register');
    expectRegistrationValues(email);

    cy.get('[data-cy="registration-submit"]').should('not.be.disabled').click();

    cy.window().its('localStorage.authToken').should('exist');
    cy.location('hash').should('eq', '#/');
  });

  it('rejects false and missing legal acknowledgement through the API', () => {
    const baseUser = {
      password,
      firstName: 'Gdpr',
      lastName: 'Api'
    };

    cy.request({
      method: 'POST',
      url: `${apiUrl}/users`,
      failOnStatusCode: false,
      body: {
        ...baseUser,
        email: uniqueEmail('gdpr.legal.false'),
        termsAccepted: false
      }
    }).then(response => {
      expect(response.status).to.be.oneOf([400, 422]);
    });

    cy.request({
      method: 'POST',
      url: `${apiUrl}/users`,
      failOnStatusCode: false,
      body: {
        ...baseUser,
        email: uniqueEmail('gdpr.legal.missing')
      }
    }).then(response => {
      expect(response.status).to.be.oneOf([400, 422]);
    });

    cy.request({
      method: 'POST',
      url: `${apiUrl}/users`,
      body: {
        ...baseUser,
        email: uniqueEmail('gdpr.legal.true'),
        termsAccepted: true
      }
    }).then(response => {
      expect(response.status).to.be.oneOf([200, 201]);
      expect(JSON.stringify(response.body)).not.to.match(/passwordHash|token/i);
    });
  });
});
