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
  cy.visit('/#/login');
  cy.get('input[name="email"]').clear().type(email);
  cy.get('input[name="password"]').clear().type(password);
  cy.contains('button', 'Login').click();
  cy.location('hash').should('eq', '#/');
}

function createHouseholdWithRoommate() {
  const mainEmail = uniqueEmail('fr06-main');
  const roomEmail = uniqueEmail('fr06-room');

  return registerUser(mainEmail, 'FR06', 'Main').then((mainToken) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: bearer(mainToken),
      body: { name: `FR06 WG ${Date.now()}`, address: 'QA Street' },
    }).then((householdResponse) => {
      const householdId = householdResponse.body.id;

      return cy.request({
        method: 'POST',
        url: `${apiUrl}/households/${householdId}/invitations`,
        headers: bearer(mainToken),
        body: { invitedEmail: roomEmail, expiresInHours: 168 },
      }).then((invitationResponse) => {
        const invitationToken = invitationResponse.body.inviteUrl.split('/').pop();

        return registerUser(roomEmail, 'FR06', 'Room').then((roomToken) => {
          return cy.request({
            method: 'POST',
            url: `${apiUrl}/invitations/${invitationToken}/accept`,
            headers: bearer(roomToken),
          }).then(() => {
            return cy.request({
              method: 'GET',
              url: `${apiUrl}/households/${householdId}/members`,
              headers: bearer(mainToken),
            }).then((membersResponse) => {
              const mainRoommate = membersResponse.body.find((member) => member.email === mainEmail);
              const roomRoommate = membersResponse.body.find((member) => member.email === roomEmail);

              expect(mainRoommate).to.exist;
              expect(roomRoommate).to.exist;

              return { mainEmail, roomEmail, mainToken, mainRoommate, roomRoommate };
            });
          });
        });
      });
    });
  });
}

function createHouseholdWithExpense() {
  return createHouseholdWithRoommate().then((context) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/expenses`,
      headers: bearer(context.mainToken),
      body: {
        description: 'FR06 Cypress Groceries',
        totalAmount: 20,
        date: today(),
        paidByRoommateId: context.mainRoommate.id,
        splits: [
          { roommateId: context.mainRoommate.id, amountOwed: 10 },
          { roommateId: context.roomRoommate.id, amountOwed: 10 },
        ],
      },
    }).then(() => context);
  });
}

function assertBalanceAmount(title, amount) {
  cy.contains('.balance-row', title)
    .should('be.visible')
    .within(() => {
      cy.contains(amount).should('be.visible');
    });
}

function assertPaymentAmount(title, amount) {
  cy.contains('.payment-item', title)
    .should('be.visible')
    .within(() => {
      cy.contains(amount).should('be.visible');
    });
}

function visitPaymentStatusWithoutSettlementOverlay() {
  cy.intercept('GET', '**/api/v1/payments/settlement-approvals').as('getSettlements');
  cy.intercept('GET', '**/api/v1/payments/overview').as('getPaymentOverview');
  cy.visit('/#/payment-status');
  cy.contains('h2', 'Payment Status').should('be.visible');
  cy.wait('@getSettlements').its('response.statusCode').should('eq', 200);
  cy.wait('@getPaymentOverview').its('response.statusCode').should('eq', 200);

  cy.get('body').then(($body) => {
    if ($body.find('[data-cy="close-settlement-overlay"]').length) {
      cy.get('[data-cy="close-settlement-overlay"]').click();
    }
  });
  cy.get('[data-cy="settlement-approval-overlay"]').should('not.exist');
}

describe('FR-06 Payment Status E2E Workflow', () => {
  it('FR-06 positive: records a partial payment, persists it, and keeps FR-05 balances in sync', () => {
    createHouseholdWithExpense().then(({ roomEmail }) => {
      login(roomEmail);

      visitPaymentStatusWithoutSettlementOverlay();

      assertBalanceAmount('FR06 Room owes FR06 Main', '10.00');
      cy.contains('.balance-row', 'FR06 Room owes FR06 Main')
        .find('button[title="Record payment"]')
        .click();

      cy.get('#amount').clear().type('4.00');
      cy.get('#date').clear().type(today());
      cy.intercept('POST', '**/api/v1/payments').as('createPayment');
      cy.contains('button', 'Save Payment').click();
      cy.wait('@createPayment').its('response.statusCode').should('eq', 201);

      cy.contains('Payment recorded.').should('be.visible');
      assertBalanceAmount('FR06 Room owes FR06 Main', '6.00');
      assertPaymentAmount('FR06 Room paid FR06 Main', '4.00');

      cy.reload();
      assertBalanceAmount('FR06 Room owes FR06 Main', '6.00');
      assertPaymentAmount('FR06 Room paid FR06 Main', '4.00');

      cy.visit('/#/expenses');
      cy.contains('h2', 'Expenses').should('be.visible');
      assertBalanceAmount('FR06 Main', '6.00');
    });
  });

  it('FR-06 negative: rejects overpayment before sending a request', () => {
    createHouseholdWithExpense().then(({ roomEmail }) => {
      login(roomEmail);

      visitPaymentStatusWithoutSettlementOverlay();

      cy.contains('.balance-row', 'FR06 Room owes FR06 Main')
        .find('button[title="Record payment"]')
        .click();

      cy.get('#amount').clear().type('99.00');
      cy.intercept('POST', '**/api/v1/payments').as('createPayment');
      cy.contains('button', 'Save Payment').click();
      cy.contains('Payment cannot exceed the open balance.').should('be.visible');
      cy.get('@createPayment.all').should('have.length', 0);
    });
  });

  it('FR-06 edge: shows empty payment states for a household without balances', () => {
    createHouseholdWithRoommate().then(({ roomEmail }) => {
      login(roomEmail);

      visitPaymentStatusWithoutSettlementOverlay();

      cy.contains('There are no open balances.').should('be.visible');
      cy.contains('No payments recorded yet.').should('be.visible');
    });
  });
});
