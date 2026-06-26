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

function createDashboardHousehold() {
  const mainEmail = uniqueEmail('fr12-main');
  const roomEmail = uniqueEmail('fr12-room');

  return registerUser(mainEmail, 'FR12', 'Main').then((mainToken) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: bearer(mainToken),
      body: { name: `FR12 WG ${Date.now()}`, address: 'QA Street' },
    }).then((householdResponse) => {
      const householdId = householdResponse.body.id;

      return cy.request({
        method: 'POST',
        url: `${apiUrl}/households/${householdId}/invitations`,
        headers: bearer(mainToken),
        body: { invitedEmail: roomEmail, expiresInHours: 168 },
      }).then((invitationResponse) => {
        const token = invitationResponse.body.inviteUrl.split('/').pop();

        return registerUser(roomEmail, 'FR12', 'Room').then((roomToken) => {
          return cy.request({
            method: 'POST',
            url: `${apiUrl}/invitations/${token}/accept`,
            headers: bearer(roomToken),
          }).then(() => ({ mainEmail, roomEmail, mainToken, roomToken, householdId }));
        });
      });
    });
  });
}

function login(email, mockDate = null) {
  if (mockDate) {
    cy.visit('/#/login', {
      onBeforeLoad(win) {
        const OriginalDate = win.Date;
        class MockedDate extends OriginalDate {
          constructor(...args) {
            if (args.length === 0) {
              const stack = new Error().stack || '';
              if (stack.includes('isLoggedIn') || stack.includes('Expiration') || stack.includes('AuthService')) {
                super();
              } else {
                super(mockDate);
              }
            } else {
              super(...args);
            }
          }
          static now() {
            const stack = new Error().stack || '';
            if (stack.includes('isLoggedIn') || stack.includes('Expiration') || stack.includes('AuthService')) {
              return OriginalDate.now();
            }
            return new OriginalDate(mockDate).getTime();
          }
        }
        win.Date = MockedDate;
      }
    });
  } else {
    cy.visit('/#/login');
  }
  cy.get('input[name="email"]').clear().type(email);
  cy.get('input[name="password"]').clear().type(password);
  cy.contains('button', 'Login').click();
  cy.location('hash').should('eq', '#/');
}

describe('FR-12 Dashboard E2E Workflow', () => {
  it('displays user greeting, task counters, active shopping list, overdue warnings, debts breakdown, and empty states', () => {
    createDashboardHousehold().then(({ mainEmail, roomEmail, mainToken, roomToken }) => {
      const todayStr = new Date().toISOString().split('T')[0];

      // 1. Create an OVERDUE task assigned to main user
      cy.request({
        method: 'POST',
        url: `${apiUrl}/tasks`,
        headers: bearer(mainToken),
        body: {
          title: 'Overdue chore',
          description: 'Mop floor',
          dueDate: todayStr,
          assignedToEmail: mainEmail
        }
      });

      // 2. Create a test shopping item
      cy.request({
        method: 'POST',
        url: `${apiUrl}/shopping-items`,
        headers: bearer(mainToken),
        body: {
          name: 'Apples',
          quantity: '3 kg',
          notes: 'Organic red',
          visibility: 'SHARED'
        }
      });

      // 3. Create roommate debt: Main Tenant pays 20.00 split equally with Roommate
      // Get roommate IDs to perform a clean split
      cy.request({
        method: 'GET',
        url: `${apiUrl}/expenses`,
        headers: bearer(mainToken)
      }).then(() => {
        cy.request({
          method: 'GET',
          url: `${apiUrl}/households/me`,
          headers: bearer(mainToken)
        }).then((householdRes) => {
          const hhId = householdRes.body.id;
          cy.request({
            method: 'GET',
            url: `${apiUrl}/households/${hhId}/members`,
            headers: bearer(mainToken)
          }).then((membersRes) => {
            const mainRm = membersRes.body.find(m => m.email === mainEmail);
            const roomRm = membersRes.body.find(m => m.email === roomEmail);

            cy.request({
              method: 'POST',
              url: `${apiUrl}/expenses`,
              headers: bearer(mainToken),
              body: {
                description: 'Cypress E2E Pizza',
                totalAmount: 20.00,
                date: todayStr,
                paidByRoommateId: mainRm.id,
                splits: [
                  { roommateId: mainRm.id, amountOwed: 10.00 },
                  { roommateId: roomRm.id, amountOwed: 10.00 }
                ]
              }
            });
          });
        });
      });

      // --- RUN POPULATED STATE ASSERTS ---
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      const tomorrowStr = tomorrow.toISOString();

      login(mainEmail, tomorrowStr);

      // Verify greeting & general layout
      cy.contains('h2', 'Welcome home,').should('be.visible');

      // Verify the overdue warning banner is rendered pulsatingly
      cy.get('.warning-banner').should('be.visible');
      cy.contains('Task "Overdue chore" is overdue').should('be.visible');

      // Verify counters in stats cards
      cy.contains('My Open Tasks').parent().contains('1').should('be.visible');

      // Verify balance card indicates Main is owed money
      cy.get('.balance-card.positive').should('be.visible');
      cy.contains('You are owed').should('be.visible');
      cy.contains('€10.00').should('be.visible');

      // Verify debt section specifies who owes Main
      cy.contains('Owed to You').should('be.visible');
      cy.contains('FR12 Room').closest('.debt-row').contains('+€10.00').should('be.visible');

      // Verify active shopping request
      cy.contains('Apples').should('be.visible');

      // --- TEST INTERACTION & TRANSITIONS ---
      // Check off the task
      cy.intercept('PATCH', '**/api/v1/tasks/**/status').as('completeTask');
      cy.get('.custom-checkbox input[type="checkbox"]').first().check({ force: true });
      cy.wait('@completeTask').its('response.statusCode').should('eq', 200);

      // The warning banner and chore should disappear instantly
      cy.contains('Overdue chore').should('not.exist');
      cy.get('.warning-banner').should('not.exist');

      // Purchase the shopping request
      cy.intercept('PATCH', '**/api/v1/shopping-items/**/status').as('buyItem');
      cy.contains('button', 'Bought').click();
      cy.wait('@buyItem').its('response.statusCode').should('eq', 200);

      // Apples should disappear
      cy.contains('Apples').should('not.exist');

      // Since tasks and shopping items are settled, verify card empty states
      cy.contains('All caught up! No pending chores assigned to you.').should('be.visible');
      cy.contains('No active shopping requests. Flat is fully stocked!').should('be.visible');

      // --- ADD NEW STUFF VIA UI ---
      // 1. Create a shopping item via UI form
      cy.visit('/#/shopping-items/create');
      cy.get('#name').type('Organic Bananas');
      cy.get('[data-cy="quantity-amount"]').type('1');
      cy.get('[data-cy="quantity-unit"]').select('pack');
      cy.get('#notes').type('Fairtrade certified');
      cy.get('#visibility').select('SHARED');

      cy.intercept('POST', '**/api/v1/shopping-items').as('uiCreateShopping');
      cy.contains('button', 'Add Item').click();
      cy.wait('@uiCreateShopping').its('response.statusCode').should('eq', 201);
      cy.location('hash').should('eq', '#/shopping-items');

      // 2. Create a chore/task via UI form
      cy.createTask({
        title: 'Sweep Kitchen',
        description: 'Sweep under the cabinets',
        dueDate: todayStr,
        assignedToEmail: mainEmail
      });

      // 3. Return to Dashboard and verify the new responsibilities are correctly reflected
      cy.visit('/#/');
      cy.location('hash').should('eq', '#/');

      // Verify h2 greeting is back & page loaded
      cy.contains('h2', 'Welcome home,').should('be.visible');

      // Verify the warning banner is back with the new overdue task (since date is tomorrow in browser)
      cy.get('.warning-banner').should('be.visible');
      cy.contains('Task "Sweep Kitchen" is overdue').should('be.visible');

      // Verify counter in stats cards has updated to 1
      cy.contains('My Open Tasks').parent().contains('1').should('be.visible');

      // Verify the chore checklist has the new task listed as overdue
      cy.contains('.checklist-item', 'Sweep Kitchen').should('be.visible');
      cy.contains('.checklist-item', 'Sweep Kitchen').contains('Overdue:').should('be.visible');

      // Verify the needed shopping items card has the new shopping item
      cy.contains('.shopping-item', 'Organic Bananas').should('be.visible');
      cy.contains('.shopping-item', 'Organic Bananas').contains('1 pack').should('be.visible');
      cy.contains('.shopping-item', 'Organic Bananas').contains('Fairtrade certified').should('be.visible');

      // Verify empty states are no longer shown
      cy.contains('All caught up! No pending chores assigned to you.').should('not.exist');
      cy.contains('No active shopping requests. Flat is fully stocked!').should('not.exist');
    });
  });

  [
    [360, 740],
    [1024, 768]
  ].forEach(([width, height]) => {
    it(`renders the dashboard responsively at ${width}px width`, () => {
      cy.viewport(width, height);
      createDashboardHousehold().then(({ mainEmail }) => {
        login(mainEmail);
        cy.contains('h2', 'Welcome home,').should('be.visible');
        cy.contains('h3', 'My Pending Tasks').should('be.visible');
        cy.contains('h3', 'Needed Shopping Items').should('be.visible');

        cy.document().then((document) => {
          expect(document.documentElement.scrollWidth).to.be.at.most(width + 2);
        });
      });
    });
  });
});
