const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Passw0rd!';

function uniqueEmail(prefix) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 100000)}@example.com`;
}

function today() {
  const date = new Date();
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toISOString().slice(0, 10);
}

function bearer(token) {
  return { Authorization: token };
}

function registerUser(email, firstName, lastName) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/users`,
    body: { email, password, firstName, lastName, termsAccepted: true },
  }).then((response) => String(response.body));
}

function login(email) {
  cy.clearCookies();
  cy.visit('/#/login', {
    onBeforeLoad(win) {
      win.localStorage.clear();
      win.sessionStorage.clear();
    },
  });
  cy.get('input[name="email"]').clear().type(email);
  cy.get('input[name="password"]').clear().type(password);
  cy.contains('button', 'Login').click();
  cy.location('hash').should('eq', '#/');
}

function inviteAndAccept(householdId, mainToken, invitedEmail, firstName, lastName) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/households/${householdId}/invitations`,
    headers: bearer(mainToken),
    body: { invitedEmail, expiresInHours: 168 },
  }).then((invitationResponse) => {
    const invitationToken = invitationResponse.body.inviteUrl.split('/').pop();
    return registerUser(invitedEmail, firstName, lastName).then((roommateToken) => {
      return cy.request({
        method: 'POST',
        url: `${apiUrl}/invitations/${invitationToken}/accept`,
        headers: bearer(roommateToken),
      }).then(() => roommateToken);
    });
  });
}

function createFr17Household() {
  const mainEmail = uniqueEmail('fr17-main');
  const debtorEmail = uniqueEmail('fr17-debtor');
  const helperEmail = uniqueEmail('fr17-helper');

  return registerUser(mainEmail, 'FR17', 'Main').then((mainToken) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: bearer(mainToken),
      body: { name: `FR17 WG ${Date.now()}`, address: 'QA Street' },
    }).then((householdResponse) => {
      const householdId = householdResponse.body.id;

      return inviteAndAccept(householdId, mainToken, debtorEmail, 'FR17', 'Debtor')
        .then((debtorToken) => inviteAndAccept(householdId, mainToken, helperEmail, 'FR17', 'Helper')
          .then((helperToken) => ({ debtorToken, helperToken })))
        .then((tokens) => cy.request({
          method: 'GET',
          url: `${apiUrl}/households/${householdId}/members`,
          headers: bearer(mainToken),
        }).then((membersResponse) => ({ membersResponse, tokens })))
        .then(({ membersResponse, tokens }) => {
          const mainRoommate = membersResponse.body.find((m) => m.email === mainEmail);
          const debtorRoommate = membersResponse.body.find((m) => m.email === debtorEmail);
          const helperRoommate = membersResponse.body.find((m) => m.email === helperEmail);

          expect(mainRoommate).to.exist;
          expect(debtorRoommate).to.exist;
          expect(helperRoommate).to.exist;

          return {
            mainEmail, debtorEmail, helperEmail,
            mainToken, helperToken: tokens.helperToken,
            mainRoommate, debtorRoommate, helperRoommate,
          };
        });
    });
  });
}

function createExpense(token, description, paidByRoommateId, splits) {
  const totalAmount = splits.reduce((sum, split) => sum + split.amountOwed, 0);
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/expenses`,
    headers: bearer(token),
    body: { description, totalAmount, date: today(), paidByRoommateId, splits },
  });
}

function createFr17SettlementData() {
  return createFr17Household().then((context) => {
    const { mainToken, mainRoommate, debtorRoommate, helperRoommate } = context;

    return createExpense(mainToken, 'FR17 main paid utilities', mainRoommate.id, [
      { roommateId: mainRoommate.id, amountOwed: 20 },
      { roommateId: debtorRoommate.id, amountOwed: 20 },
      { roommateId: helperRoommate.id, amountOwed: 20 },
    ])
      .then(() => createExpense(context.helperToken, 'FR17 helper paid groceries', helperRoommate.id, [
        { roommateId: mainRoommate.id, amountOwed: 25 },
        { roommateId: debtorRoommate.id, amountOwed: 25 },
        { roommateId: helperRoommate.id, amountOwed: 25 },
      ]))
      .then(() => context);
  });
}

function createFr17CreditorOnlyData() {
  return createFr17Household().then((context) => {
    const { mainToken, mainRoommate, debtorRoommate, helperRoommate } = context;

    return createExpense(mainToken, 'FR17 main paid utilities', mainRoommate.id, [
      { roommateId: mainRoommate.id, amountOwed: 20 },
      { roommateId: debtorRoommate.id, amountOwed: 20 },
      { roommateId: helperRoommate.id, amountOwed: 20 },
    ]).then(() => context);
  });
}

function swipeSettlement(cardChainable, direction) {
  const delta = 110; // exceeds settlementSwipeThreshold of 90

  cardChainable.then(($card) => {
    const rect = $card[0].getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;
    const endX = direction === 'right' ? centerX + delta : centerX - delta;

    cy.wrap($card)
      .trigger('pointerdown', { pointerId: 1, pointerType: 'mouse', buttons: 1, clientX: centerX, clientY: centerY, force: true })
      .trigger('pointermove', { pointerId: 1, pointerType: 'mouse', buttons: 1, clientX: endX, clientY: centerY, force: true })
      .trigger('pointerup', { pointerId: 1, pointerType: 'mouse', buttons: 0, clientX: endX, clientY: centerY, force: true });
  });
}

function visitPaymentStatus() {
  cy.intercept('GET', '**/api/v1/payments/settlement-approvals').as('getSettlements');
  cy.intercept('GET', '**/api/v1/payments/overview').as('getPaymentOverview');
  cy.visit('/#/payment-status');
  cy.contains('h2', 'Payment Status').should('be.visible');
  cy.wait('@getSettlements').its('response.statusCode').should('eq', 200);
  cy.wait('@getPaymentOverview').its('response.statusCode').should('eq', 200);
}

describe('FR-17 Swipe Settlement Approval E2E', () => {

  it('shows single-card overlay on load and dismisses via X button', () => {
    createFr17SettlementData().then(({ debtorEmail }) => {
      login(debtorEmail);
      visitPaymentStatus();

      cy.get('[data-cy="settlement-approval-overlay"]').should('be.visible');
      cy.get('[data-cy="settlement-card"]').should('have.length', 1);
      cy.get('[data-cy="settlement-card"]').should('contain', 'FR17 main paid utilities');

      cy.get('[data-cy="close-settlement-overlay"]').click();
      cy.get('[data-cy="settlement-approval-overlay"]').should('not.exist');
    });
  });

  it('approves first settlement via right swipe, rejects second via left swipe', () => {
    createFr17SettlementData().then(({ debtorEmail }) => {
      login(debtorEmail);
      visitPaymentStatus();

      cy.get('[data-cy="settlement-approval-overlay"]').should('be.visible');
      cy.get('[data-cy="settlement-card"]').should('have.length', 1);

      cy.intercept('POST', '**/api/v1/payments/settlement-approvals/*/approve').as('approveSettlement');
      swipeSettlement(cy.get('[data-cy="settlement-card"]').first(), 'right');
      cy.wait('@approveSettlement').its('response.statusCode').should('eq', 200);
      cy.contains('Settlement approved and payment recorded.').should('be.visible');
      cy.get('[data-cy="settlement-card"]').should('have.length', 1);

      cy.intercept('POST', '**/api/v1/payments/settlement-approvals/*/reject').as('rejectSettlement');
      swipeSettlement(cy.get('[data-cy="settlement-card"]').first(), 'left');
      cy.wait('@rejectSettlement').its('response.statusCode').should('eq', 200);
      cy.contains('Settlement rejected.').should('be.visible');
      cy.get('[data-cy="settlement-approval-overlay"]').should('not.exist');

      cy.contains('.payment-item', 'FR17 Debtor paid').should('be.visible');
    });
  });

  it('approves and rejects settlements via accessible button fallback', () => {
    createFr17SettlementData().then(({ debtorEmail }) => {
      login(debtorEmail);
      visitPaymentStatus();

      cy.get('[data-cy="settlement-approval-overlay"]').should('be.visible');
      cy.get('[data-cy="approve-settlement"]')
        .should('be.visible')
        .and('have.attr', 'aria-label', 'Approve settlement');
      cy.get('[data-cy="reject-settlement"]')
        .should('be.visible')
        .and('have.attr', 'aria-label', 'Reject settlement');

      cy.intercept('POST', '**/api/v1/payments/settlement-approvals/*/approve').as('approveByButton');
      cy.get('[data-cy="approve-settlement"]').click();
      cy.wait('@approveByButton').its('response.statusCode').should('eq', 200);
      cy.contains('Settlement approved and payment recorded.').should('be.visible');
      cy.get('[data-cy="settlement-card"]').should('have.length', 1);

      cy.intercept('POST', '**/api/v1/payments/settlement-approvals/*/reject').as('rejectByButton');
      cy.get('[data-cy="reject-settlement"]').click();
      cy.wait('@rejectByButton').its('response.statusCode').should('eq', 200);
      cy.contains('Settlement rejected.').should('be.visible');
      cy.get('[data-cy="settlement-approval-overlay"]').should('not.exist');
    });
  });

  it('rejects first settlement via left swipe, approves second via right swipe', () => {
    createFr17SettlementData().then(({ debtorEmail }) => {
      login(debtorEmail);
      visitPaymentStatus();

      cy.get('[data-cy="settlement-approval-overlay"]').should('be.visible');
      cy.get('[data-cy="settlement-card"]').should('have.length', 1);

      cy.intercept('POST', '**/api/v1/payments/settlement-approvals/*/reject').as('rejectFirst');
      swipeSettlement(cy.get('[data-cy="settlement-card"]').first(), 'left');
      cy.wait('@rejectFirst').its('response.statusCode').should('eq', 200);
      cy.contains('Settlement rejected.').should('be.visible');
      cy.get('[data-cy="settlement-card"]').should('have.length', 1);

      cy.intercept('POST', '**/api/v1/payments/settlement-approvals/*/approve').as('approveSecond');
      swipeSettlement(cy.get('[data-cy="settlement-card"]').first(), 'right');
      cy.wait('@approveSecond').its('response.statusCode').should('eq', 200);
      cy.contains('Settlement approved and payment recorded.').should('be.visible');
      cy.get('[data-cy="settlement-approval-overlay"]').should('not.exist');
    });
  });

  it('overlay auto-closes after all settlements are decided', () => {
    createFr17SettlementData().then(({ debtorEmail }) => {
      login(debtorEmail);
      visitPaymentStatus();

      cy.get('[data-cy="settlement-approval-overlay"]').should('be.visible');

      cy.intercept('POST', '**/api/v1/payments/settlement-approvals/*/approve').as('approve1');
      swipeSettlement(cy.get('[data-cy="settlement-card"]').first(), 'right');
      cy.wait('@approve1');
      cy.contains('Settlement approved and payment recorded.').should('be.visible');
      cy.get('[data-cy="settlement-card"]').should('be.visible');

      cy.intercept('POST', '**/api/v1/payments/settlement-approvals/*/approve').as('approve2');
      swipeSettlement(cy.get('[data-cy="settlement-card"]').first(), 'right');
      cy.wait('@approve2');

      cy.get('[data-cy="settlement-approval-overlay"]').should('not.exist');
    });
  });

  it('does not show settlement approvals to the creditor', () => {
    createFr17CreditorOnlyData().then(({ mainEmail }) => {
      login(mainEmail);
      visitPaymentStatus();

      cy.get('[data-cy="settlement-approval-overlay"]').should('not.exist');
      cy.get('[data-cy="settlement-card"]').should('not.exist');
    });
  });

  it('renders the settlement approval overlay on a mobile viewport', () => {
    cy.viewport('iphone-x');

    createFr17SettlementData().then(({ debtorEmail }) => {
      login(debtorEmail);
      visitPaymentStatus();

      cy.get('[data-cy="settlement-approval-overlay"]').should('be.visible');
      cy.get('[data-cy="close-settlement-overlay"]').should('be.visible');
      cy.get('[data-cy="settlement-card"]')
        .should('be.visible')
        .and('contain', 'FR17 main paid utilities')
        .and('contain', 'FR17 Debtor owes FR17 Main');
      cy.get('[data-cy="settlement-card"]').then(($card) => {
        const rect = $card[0].getBoundingClientRect();
        expect(rect.left).to.be.at.least(0);
        expect(rect.right).to.be.at.most(Cypress.config('viewportWidth'));
      });
    });
  });

  it('redirects unauthenticated users to login', () => {
    cy.visit('/#/payment-status');
    cy.location('hash').should('eq', '#/login');
  });
});
