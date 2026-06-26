const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Passw0rd!';

function uniqueEmail(prefix) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 100000)}@example.com`;
}

function registerUser(email, firstName, lastName) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/users`,
    body: { email, password, firstName, lastName, termsAccepted: true },
  }).then((response) => String(response.body));
}

function bearer(token) {
  return { Authorization: token };
}

function todayIso() {
  const date = new Date();
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

function createMainHousehold() {
  const mainEmail = uniqueEmail('fr05-main');

  return registerUser(mainEmail, 'FR05', 'Main').then((mainToken) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: bearer(mainToken),
      body: { name: `FR05 WG ${Date.now()}`, address: 'QA Street' },
    }).then((householdResponse) => ({
      mainEmail,
      mainToken,
      householdId: householdResponse.body.id,
    }));
  });
}

function createInvitationToken(householdId, mainToken, invitedEmail) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/households/${householdId}/invitations`,
    headers: bearer(mainToken),
    body: { invitedEmail, expiresInHours: 168 },
  }).then((invitationResponse) => invitationResponse.body.inviteUrl.split('/').pop());
}

function acceptInvitation(roomToken, invitationToken) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/invitations/${invitationToken}/accept`,
    headers: bearer(roomToken),
  });
}

function getMembers(token, householdId) {
  return cy.request({
    method: 'GET',
    url: `${apiUrl}/households/${householdId}/members`,
    headers: bearer(token),
  }).then((response) => response.body);
}

function createExpenseApi(token, description, totalAmount, paidByRoommateId, splits) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/expenses`,
    headers: bearer(token),
    body: {
      description,
      totalAmount,
      date: todayIso(),
      paidByRoommateId,
      splits,
    },
  }).then((response) => response.body);
}

function createPaymentApi(token, toRoommateId, amount) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/payments`,
    headers: bearer(token),
    body: {
      toRoommateId,
      amount,
      date: todayIso(),
    },
  });
}

function leaveHousehold(token) {
  return cy.request({
    method: 'DELETE',
    url: `${apiUrl}/users/me/household`,
    headers: bearer(token),
  });
}

function anonymizeUser(token) {
  return cy.request({
    method: 'DELETE',
    url: `${apiUrl}/users/me`,
    headers: bearer(token),
  });
}

function createExpenseHousehold() {
  const mainEmail = uniqueEmail('fr05-main');
  const roomEmail = uniqueEmail('fr05-room');

  return registerUser(mainEmail, 'FR05', 'Main').then((mainToken) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: bearer(mainToken),
      body: { name: `FR05 WG ${Date.now()}`, address: 'QA Street' },
    }).then((householdResponse) => {
      const householdId = householdResponse.body.id;

      return cy.request({
        method: 'POST',
        url: `${apiUrl}/households/${householdId}/invitations`,
        headers: bearer(mainToken),
        body: { invitedEmail: roomEmail, expiresInHours: 168 },
      }).then((invitationResponse) => {
        const token = invitationResponse.body.inviteUrl.split('/').pop();

        return registerUser(roomEmail, 'FR05', 'Room').then((roomToken) => {
          return cy.request({
            method: 'POST',
            url: `${apiUrl}/invitations/${token}/accept`,
            headers: bearer(roomToken),
          }).then(() => ({ mainEmail, roomEmail }));
        });
      });
    });
  });
}

function inviteAndAcceptRoommate(householdId, mainToken, email, firstName, lastName) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/households/${householdId}/invitations`,
    headers: bearer(mainToken),
    body: { invitedEmail: email, expiresInHours: 168 },
  }).then((invitationResponse) => {
    const token = invitationResponse.body.inviteUrl.split('/').pop();

    return registerUser(email, firstName, lastName).then((roomToken) => {
      return cy.request({
        method: 'POST',
        url: `${apiUrl}/invitations/${token}/accept`,
        headers: bearer(roomToken),
      });
    });
  });
}

function createExpenseHouseholdWithTwoRoommates() {
  const mainEmail = uniqueEmail('fr05-main');
  const roomEmail = uniqueEmail('fr05-room');
  const secondEmail = uniqueEmail('fr05-second');

  return registerUser(mainEmail, 'FR05', 'Main').then((mainToken) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: bearer(mainToken),
      body: { name: `FR05 WG ${Date.now()}`, address: 'QA Street' },
    }).then((householdResponse) => {
      const householdId = householdResponse.body.id;

      return inviteAndAcceptRoommate(householdId, mainToken, roomEmail, 'FR05', 'Room')
        .then(() => inviteAndAcceptRoommate(householdId, mainToken, secondEmail, 'FR05', 'Second'))
        .then(() => ({ mainEmail, roomEmail, secondEmail }));
    });
  });
}

function createHouseholdWithRoommateContext(firstName = 'FR05', lastName = 'Room') {
  const roomEmail = uniqueEmail('fr05-room');

  return createMainHousehold().then((context) => {
    return registerUser(roomEmail, firstName, lastName).then((roomToken) => {
      return createInvitationToken(context.householdId, context.mainToken, roomEmail)
        .then((invitationToken) => acceptInvitation(roomToken, invitationToken))
        .then(() => getMembers(context.mainToken, context.householdId))
        .then((members) => ({
          ...context,
          roomEmail,
          roomToken,
          mainRoommate: members.find((member) => member.email === context.mainEmail),
          roomRoommate: members.find((member) => member.email === roomEmail),
        }));
    });
  });
}

function login(email) {
  cy.visit('/#/login');
  cy.get('input[name="email"]').clear().type(email);
  cy.get('input[name="password"]').clear().type(password);
  cy.contains('button', 'Login').click();
  cy.location('hash').should('eq', '#/');
}

function openExpenses() {
  cy.visit('/#/expenses');
  cy.contains('h2', 'Expenses').should('be.visible');
  cy.contains('h3', 'Create Expense').should('be.visible');
}

describe('FR-05 Shared Expenses E2E', () => {
  it('FR-05 positive: creates, equal-splits, persists, edits, and deletes an expense', () => {
    createExpenseHousehold().then(({ mainEmail }) => {
      login(mainEmail);
      openExpenses();

      cy.get('#description').clear().type('QA Cypress Expense');
      cy.get('#totalAmount').clear().type('12.00');
      cy.contains('.split-row', 'FR05 Room')
        .find('input[type="checkbox"]')
        .check();
      cy.intercept('POST', '**/api/v1/expenses/equal-split').as('equalSplit');
      cy.contains('button', 'Equal Split').click();
      cy.wait('@equalSplit').its('response.statusCode').should('eq', 200);
      cy.contains('.split-row', 'FR05 Main')
        .find('.split-amount input')
        .should('have.value', '6,00');
      cy.contains('.split-row', 'FR05 Room')
        .find('.split-amount input')
        .should('have.value', '6,00');
      cy.intercept('POST', '**/api/v1/expenses', (request) => {
        request.on('response', (response) => {
          response.setDelay(500);
        });
      }).as('createExpense');
      cy.contains('button', 'Create Expense').click();
      cy.contains('button', 'Saving...').should('be.disabled');
      cy.wait('@createExpense').its('response.statusCode').should('eq', 201);

      cy.contains('QA Cypress Expense').should('be.visible');
      cy.contains('FR05 Room').should('be.visible');
      cy.contains('6').should('be.visible');

      cy.reload();
      cy.contains('QA Cypress Expense').should('be.visible');

      cy.contains('a', 'Logout').click();
      login(mainEmail);
      openExpenses();
      cy.contains('QA Cypress Expense').should('be.visible');

      cy.contains('.expense-item', 'QA Cypress Expense')
        .find('button[title="Edit expense"]')
        .click();
      cy.contains('h3', 'Edit Expense').should('be.visible');
      cy.get('#description').clear().type('QA Cypress Expense Updated');
      cy.get('#totalAmount').clear().type('14.00').blur();
      cy.contains('.split-row', 'FR05 Main')
        .find('.split-amount input')
        .clear()
        .type('7.00')
        .blur();
      cy.contains('.split-row', 'FR05 Room')
        .find('.split-amount input')
        .clear()
        .type('7.00')
        .blur();
      cy.intercept('PUT', '**/api/v1/expenses/*').as('updateExpense');
      cy.contains('button', 'Update Expense').click();
      cy.wait('@updateExpense').its('response.statusCode').should('eq', 200);
      cy.contains('Expense updated.').should('be.visible');
      cy.contains('QA Cypress Expense Updated').should('be.visible');

      cy.contains('.expense-item', 'QA Cypress Expense Updated')
        .find('button[title="Delete expense"]')
        .click();
      cy.contains('Delete expense?').should('be.visible');
      cy.intercept('DELETE', '**/api/v1/expenses/*').as('deleteExpense');
      cy.confirmDialog();
      cy.wait('@deleteExpense').its('response.statusCode').should('eq', 204);
      cy.contains('Expense deleted.').should('be.visible');
      cy.contains('QA Cypress Expense Updated').should('not.exist');

      cy.get('#description').clear().type('Invalid QA Expense');
      cy.get('#totalAmount').clear().type('-5.00');
      cy.contains('button', 'Create Expense').click();
      cy.contains('Enter a positive total amount with at most two decimal places.').should('be.visible');
    });
  });

  it('FR-05 negative: total and split values with more than two decimals are rejected without rounding', () => {
    createExpenseHousehold().then(({ mainEmail }) => {
      login(mainEmail);
      openExpenses();

      cy.get('#description').clear().type('Overscale QA Expense');
      cy.get('#totalAmount').clear().type('10.005').blur();
      cy.contains('.split-row', 'FR05 Main')
        .find('.split-amount input')
        .clear()
        .type('10.00')
        .blur();
      cy.intercept('POST', '**/api/v1/expenses').as('createOverscaleExpense');
      cy.contains('button', 'Create Expense').click();
      cy.contains('Enter a positive total amount with at most two decimal places.').should('be.visible');
      cy.get('@createOverscaleExpense.all').should('have.length', 0);

      cy.get('#totalAmount').clear().type('10.00').blur();
      cy.contains('.split-row', 'FR05 Main')
        .find('.split-amount input')
        .clear()
        .type('10.005')
        .blur();
      cy.contains('button', 'Create Expense').click();
      cy.contains('Select participants and enter a positive amount with at most two decimal places for every selected split.').should('be.visible');
      cy.get('@createOverscaleExpense.all').should('have.length', 0);
    });
  });

  it('BUG #32: expense update persists through the UI and refreshes dependent data', () => {
    createExpenseHousehold().then(({ mainEmail }) => {
      login(mainEmail);
      openExpenses();

      cy.get('#description').clear().type('BUG32 Original');
      cy.get('#totalAmount').clear().type('12.00');
      cy.contains('.split-row', 'FR05 Room')
        .find('input[type="checkbox"]')
        .check();
      cy.contains('.split-row', 'FR05 Main')
        .find('.split-amount input')
        .clear()
        .type('6.00')
        .blur();
      cy.contains('.split-row', 'FR05 Room')
        .find('.split-amount input')
        .clear()
        .type('6.00')
        .blur();
      cy.intercept('POST', '**/api/v1/expenses').as('createBug32Expense');
      cy.contains('button', 'Create Expense').click();
      cy.wait('@createBug32Expense').its('response.statusCode').should('eq', 201);

      cy.contains('.expense-item', 'BUG32 Original')
        .find('button[title="Edit expense"]')
        .click();
      cy.contains('h3', 'Edit Expense').should('be.visible');
      cy.get('#description').clear().type('BUG32 Updated');
      cy.get('#totalAmount').clear().type('18.00').blur();
      cy.contains('.split-row', 'FR05 Main')
        .find('.split-amount input')
        .clear()
        .type('9.00')
        .blur();
      cy.contains('.split-row', 'FR05 Room')
        .find('.split-amount input')
        .clear()
        .type('9.00')
        .blur();
      cy.intercept('PUT', '**/api/v1/expenses/*').as('updateBug32Expense');
      cy.contains('button', 'Update Expense').click();
      cy.wait('@updateBug32Expense').its('response.statusCode').should('eq', 200);
      cy.contains('Expense updated.').should('be.visible');

      cy.reload();
      cy.contains('.expense-item', 'BUG32 Updated').should('be.visible');
      cy.contains('.expense-item', 'BUG32 Updated').within(() => {
        cy.contains('18').should('be.visible');
        cy.contains('FR05 Room').should('be.visible');
        cy.contains('9').should('be.visible');
      });
      cy.contains('BUG32 Original').should('not.exist');
    });
  });

  it('BUG #31: editing an equal-split expense can add a participant and recompute exact shares', () => {
    createExpenseHouseholdWithTwoRoommates().then(({ mainEmail }) => {
      login(mainEmail);
      openExpenses();

      cy.get('#description').clear().type('BUG31 Equal Split Edit');
      cy.get('#totalAmount').clear().type('10.00');
      cy.contains('.split-row', 'FR05 Room')
        .find('input[type="checkbox"]')
        .check();
      cy.contains('button', 'Equal Split').click();
      cy.contains('.split-row', 'FR05 Main')
        .find('.split-amount input')
        .should('have.value', '5,00');
      cy.contains('.split-row', 'FR05 Room')
        .find('.split-amount input')
        .should('have.value', '5,00');
      cy.intercept('POST', '**/api/v1/expenses').as('createBug31Expense');
      cy.contains('button', 'Create Expense').click();
      cy.wait('@createBug31Expense').its('response.statusCode').should('eq', 201);

      cy.contains('.expense-item', 'BUG31 Equal Split Edit')
        .find('button[title="Edit expense"]')
        .click();
      cy.contains('h3', 'Edit Expense').should('be.visible');
      cy.contains('.split-row', 'FR05 Second')
        .find('input[type="checkbox"]')
        .check();
      cy.intercept('POST', '**/api/v1/expenses/equal-split').as('recalculateBug31Split');
      cy.contains('button', 'Equal Split').click();
      cy.wait('@recalculateBug31Split').its('response.statusCode').should('eq', 200);

      cy.contains('.split-row', 'FR05 Main')
        .find('.split-amount input')
        .should('have.value', '3,34');
      cy.contains('.split-row', 'FR05 Room')
        .find('.split-amount input')
        .should('have.value', '3,33');
      cy.contains('.split-row', 'FR05 Second')
        .find('.split-amount input')
        .should('have.value', '3,33');

      cy.intercept('PUT', '**/api/v1/expenses/*').as('updateBug31Expense');
      cy.contains('button', 'Update Expense').click();
      cy.wait('@updateBug31Expense').its('response.statusCode').should('eq', 200);
      cy.contains('Expense updated.').should('be.visible');
      cy.contains('.expense-item', 'BUG31 Equal Split Edit')
        .should('contain', 'FR05 Main')
        .and('contain', 'FR05 Room')
        .and('contain', 'FR05 Second')
        .and('contain', '3.34')
        .and('contain', '3.33');
    });
  });

  it('BUG #35: existing user accepting an invite appears once in expense participants', () => {
    const existingEmail = uniqueEmail('fr05-existing');

    createMainHousehold().then((context) => {
      registerUser(existingEmail, 'Existing', 'Member').then((existingToken) => {
        createInvitationToken(context.householdId, context.mainToken, existingEmail)
          .then((invitationToken) => acceptInvitation(existingToken, invitationToken))
          .then(() => getMembers(context.mainToken, context.householdId))
          .then((members) => {
            expect(members.filter((member) => member.email === existingEmail)).to.have.length(1);

            login(context.mainEmail);
            openExpenses();

            cy.get('.split-row')
              .filter((_, element) => element.innerText.includes('Existing Member'))
              .should('have.length', 1);
            cy.get('#description').clear().type('BUG35 Existing User Expense');
            cy.get('#totalAmount').clear().type('10.00');
            cy.contains('.split-row', 'Existing Member')
              .find('input[type="checkbox"]')
              .check();
            cy.contains('button', 'Equal Split').click();
            cy.contains('.split-row', 'Existing Member')
              .find('.split-amount input')
              .should('have.value', '5,00');
            cy.intercept('POST', '**/api/v1/expenses').as('createBug35Expense');
            cy.contains('button', 'Create Expense').click();
            cy.wait('@createBug35Expense').its('response.statusCode').should('eq', 201);
            cy.contains('.expense-item', 'BUG35 Existing User Expense')
              .should('be.visible')
              .and('contain', 'Existing Member');
          });
      });
    });
  });

  it('BUG #37: same description and amount can be used by distinct expenses', () => {
    createHouseholdWithRoommateContext('Duplicate', 'Member').then((context) => {
      const splits = [
        { roommateId: context.mainRoommate.id, amountOwed: 6 },
        { roommateId: context.roomRoommate.id, amountOwed: 6 },
      ];

      createExpenseApi(context.mainToken, 'BUG37 Duplicate Expense', 12, context.mainRoommate.id, splits)
        .then((firstExpense) => {
          return createExpenseApi(context.mainToken, 'BUG37 Duplicate Expense', 12, context.mainRoommate.id, splits)
            .then((secondExpense) => {
              expect(secondExpense.id).to.not.equal(firstExpense.id);

              login(context.mainEmail);
              openExpenses();
              cy.get('.expense-item')
                .filter((_, element) => element.innerText.includes('BUG37 Duplicate Expense'))
                .should('have.length', 2);
            });
        });
    });
  });

  it('BUG #38: leave and rejoin keeps one financial participant and balance counterparty', () => {
    createHouseholdWithRoommateContext('Rejoin', 'Member').then((context) => {
      const splits = [
        { roommateId: context.mainRoommate.id, amountOwed: 10 },
        { roommateId: context.roomRoommate.id, amountOwed: 10 },
      ];

      createExpenseApi(context.mainToken, 'BUG38 Rejoin Expense', 20, context.mainRoommate.id, splits)
        .then(() => leaveHousehold(context.roomToken))
        .then(() => createInvitationToken(context.householdId, context.mainToken, context.roomEmail))
        .then((invitationToken) => acceptInvitation(context.roomToken, invitationToken))
        .then(() => getMembers(context.mainToken, context.householdId))
        .then((members) => {
          const matchingMembers = members.filter((member) => member.email === context.roomEmail);
          expect(matchingMembers).to.have.length(1);
          expect(matchingMembers[0].id).to.equal(context.roomRoommate.id);

          login(context.mainEmail);
          openExpenses();

          cy.get('.split-row')
            .filter((_, element) => element.innerText.includes('Rejoin Member'))
            .should('have.length', 1);
          cy.get('.balance-row')
            .filter((_, element) => element.innerText.includes('Rejoin Member'))
            .should('have.length', 1);

          const rejoinedSplits = [
            { roommateId: context.mainRoommate.id, amountOwed: 4 },
            { roommateId: matchingMembers[0].id, amountOwed: 4 },
          ];
          return createExpenseApi(context.mainToken, 'BUG38 After Rejoin Expense', 8, context.mainRoommate.id, rejoinedSplits)
            .then((expense) => {
              expect(expense.splits.filter((split) => split.roommateId === matchingMembers[0].id)).to.have.length(1);
            });
        });
    });
  });

  it('BUG #42: anonymized former roommate remains visible and settleable through privacy-safe fallback', () => {
    createHouseholdWithRoommateContext('Former', 'Member').then((context) => {
      const splits = [
        { roommateId: context.mainRoommate.id, amountOwed: 10 },
        { roommateId: context.roomRoommate.id, amountOwed: 10 },
      ];

      createExpenseApi(context.roomToken, 'BUG42 Former Paid Expense', 20, context.roomRoommate.id, splits)
        .then(() => anonymizeUser(context.roomToken))
        .then(() => {
          login(context.mainEmail);
          cy.visit('/#/payment-status');
          cy.contains('h2', 'Payment Status').should('be.visible');
          cy.get('[data-cy="settlement-approval-overlay"]').should('be.visible');
          cy.get('[data-cy="settlement-card"]')
            .should('contain', 'FR05 Main owes Deleted User')
            .and('contain', 'anonymized-user-');

          cy.intercept('POST', '**/api/v1/payments/settlement-approvals/*/approve').as('approveBug42Settlement');
          cy.get('[data-cy="approve-settlement"]').click();
          cy.wait('@approveBug42Settlement').its('response.statusCode').should('eq', 200);
          cy.contains('Settlement approved and payment recorded.').should('be.visible');
          cy.contains('.payment-item', 'FR05 Main paid Deleted User').should('be.visible');
          cy.contains('former-').should('not.exist');
        });
    });
  });

  it('FR-05 negative: split sum mismatch shows readable backend error', () => {
    createExpenseHousehold().then(({ mainEmail }) => {
      login(mainEmail);
      openExpenses();

      cy.get('#description').clear().type('Mismatch QA Expense');
      cy.get('#totalAmount').clear().type('10.00').blur();
      cy.contains('.split-row', 'FR05 Room')
        .find('input[type="checkbox"]')
        .check();
      cy.contains('.split-row', 'FR05 Main')
        .find('.split-amount input')
        .clear()
        .type('8.00')
        .blur();
      cy.contains('.split-row', 'FR05 Room')
        .find('.split-amount input')
        .clear()
        .type('1.00')
        .blur();

      cy.intercept('POST', '**/api/v1/expenses').as('createMismatchExpense');
      cy.contains('button', 'Create Expense').click();
      cy.wait('@createMismatchExpense')
        .its('response.statusCode')
        .should('be.oneOf', [400, 409, 422]);
      cy.contains(/split|sum|total|amount/i).should('be.visible');
    });
  });

  [
    [360, 740],
    [390, 844],
    [412, 915],
  ].forEach(([width, height]) => {
    it(`FR-05 responsive: expense page remains usable at ${width}px width`, () => {
      cy.viewport(width, height);

      createExpenseHousehold().then(({ mainEmail }) => {
        login(mainEmail);
        openExpenses();

        cy.get('#description').should('be.visible');
        cy.get('#totalAmount').should('be.visible');
        cy.contains('button', 'Create Expense').should('be.visible');
        cy.contains('.split-row', 'FR05 Room').should('be.visible');

        cy.document().then((document) => {
          expect(document.documentElement.scrollWidth).to.be.at.most(width + 2);
        });
      });
    });
  });
});
