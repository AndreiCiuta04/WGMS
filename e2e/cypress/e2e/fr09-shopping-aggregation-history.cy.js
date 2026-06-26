const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueUser(prefix, runId) {
    return {
        firstName: prefix,
        lastName: 'FR09Qa',
        email: `${prefix.toLowerCase()}.fr09.${runId}.${Math.floor(Math.random() * 100000)}@test.com`,
        password
    };
}

function authHeaders(rawToken) {
    const token = String(rawToken);
    return {
        Authorization: token.startsWith('Bearer ') ? token : `Bearer ${token}`
    };
}

function createShoppingItem(item, status = 'NEEDED') {
    return cy.window()
        .its('localStorage.authToken')
        .should('exist')
        .then((token) => {
            return cy.request({
                method: 'POST',
                url: `${apiUrl}/shopping-items`,
                headers: authHeaders(token),
                body: {
                    name: item.name,
                    quantityCount: item.quantityCount,
                    quantityAmount: item.quantityAmount,
                    quantityUnit: item.quantityUnit,
                    notes: item.notes,
                    visibility: item.visibility || 'SHARED',
                    targetUserEmail: item.targetUserEmail
                }
            }).then((createResponse) => {
                expect(createResponse.status).to.eq(201);

                if (status === 'NEEDED') {
                    return createResponse.body;
                }

                return cy.request({
                    method: 'PATCH',
                    url: `${apiUrl}/shopping-items/${createResponse.body.id}/status`,
                    headers: authHeaders(token),
                    body: { status }
                }).then((statusResponse) => {
                    expect(statusResponse.status).to.eq(200);
                    expect(statusResponse.body.status).to.eq(status);
                    return statusResponse.body;
                });
            });
        });
}

function registerUserWithHousehold(user, householdName) {
    cy.registerUser(user);
    cy.createHousehold({
        name: householdName,
        address: 'FR09 Cypress Street, Vienna'
    });
}

function visitShoppingList() {
    cy.intercept('GET', '**/api/v1/shopping-items/aggregated').as('loadAggregated');
    cy.intercept('GET', '**/api/v1/shopping-items/history').as('loadHistory');

    cy.visit('/#/shopping-items');
    cy.contains('h2', 'Shopping List').should('be.visible');

    cy.wait('@loadAggregated').its('response.statusCode').should('eq', 200);
    cy.wait('@loadHistory').its('response.statusCode').should('eq', 200);
}

describe('FR-09 Shopping Aggregation and History E2E', () => {
    beforeEach(() => {
        cy.clearLocalStorage();
    });

    it('FR-09 positive/negative: shared needed items group while bought, personal, and other-household items are excluded', () => {
        const runId = Date.now();
        const owner = uniqueUser('Owner', runId);
        const otherHouseholdUser = uniqueUser('OtherHousehold', runId);

        const aggregateName = `FR09 Milk ${runId}`;
        const repeatedAggregateName = ` fr09 milk ${runId} `;
        const boughtName = `FR09 Bought Hidden ${runId}`;
        const otherHouseholdName = `FR09 Other Household Milk ${runId}`;

        registerUserWithHousehold(owner, `FR09 Household List WG ${runId}`);

        visitShoppingList();
        cy.get('[data-cy="shopping-tab-household-list"]').click();
        cy.get('[data-cy="household-list-section"]').should('be.visible');
        cy.get('[data-cy="household-list-empty"]')
            .should('be.visible')
            .and('contain', 'No shared household shopping needs right now.');

        createShoppingItem({
            name: aggregateName,
            quantityAmount: 2,
            quantityUnit: 'LITER',
            notes: 'First aggregate source'
        });
        createShoppingItem({
            name: repeatedAggregateName,
            quantityAmount: 2,
            quantityUnit: 'LITER',
            notes: 'Second aggregate source'
        });
        createShoppingItem({
            name: boughtName,
            quantityAmount: 1,
            quantityUnit: 'PIECE',
            notes: 'Should stay out of active aggregate list'
        }, 'BOUGHT');

        cy.clearLocalStorage();
        registerUserWithHousehold(otherHouseholdUser, `FR09 Other WG ${runId}`);
        createShoppingItem({
            name: otherHouseholdName,
            quantityAmount: 2,
            quantityUnit: 'LITER',
            notes: 'Must not leak into owner household'
        });

        cy.clearLocalStorage();
        cy.loginUser(owner.email, owner.password);

        visitShoppingList();
        cy.get('[data-cy="shopping-tab-household-list"]').click();

        cy.contains('[data-cy="household-list-section"] h3', 'Aggregated Household List')
            .should('be.visible');
        cy.contains('[data-cy="aggregated-shopping-item"]', aggregateName)
            .should('be.visible')
            .within(() => {
                cy.get('[data-cy="aggregate-request-count"]')
                    .should('contain', '2 requests');
                cy.contains('2 L').should('be.visible');
            });

        cy.get('[data-cy="household-list-section"]')
            .should('not.contain', boughtName)
            .and('not.contain', otherHouseholdName);
    });

    it('FR-09 positive/negative: history shows bought/cancelled items and excludes needed/other-household items', () => {
        const runId = Date.now();
        const owner = uniqueUser('HistoryOwner', runId);
        const otherHouseholdUser = uniqueUser('HistoryOther', runId);

        const boughtName = `FR09 Bought History ${runId}`;
        const cancelledName = `FR09 Cancelled History ${runId}`;
        const neededName = `FR09 Needed Active ${runId}`;
        const otherHouseholdHistoryName = `FR09 Other History ${runId}`;

        registerUserWithHousehold(owner, `FR09 History WG ${runId}`);

        createShoppingItem({
            name: boughtName,
            quantityAmount: 1,
            quantityUnit: 'PACK',
            notes: 'Bought history item'
        }, 'BOUGHT');
        createShoppingItem({
            name: cancelledName,
            quantityAmount: 3,
            quantityUnit: 'PIECE',
            notes: 'Cancelled history item'
        }, 'CANCELLED');
        createShoppingItem({
            name: neededName,
            quantityAmount: 5,
            quantityUnit: 'PIECE',
            notes: 'Needed item should not be in history'
        });

        cy.clearLocalStorage();
        registerUserWithHousehold(otherHouseholdUser, `FR09 Other History WG ${runId}`);
        createShoppingItem({
            name: otherHouseholdHistoryName,
            quantityAmount: 1,
            quantityUnit: 'PIECE',
            notes: 'Must not leak into owner history'
        }, 'BOUGHT');

        cy.clearLocalStorage();
        cy.loginUser(owner.email, owner.password);

        visitShoppingList();
        cy.get('[data-cy="shopping-tab-history"]').click();

        cy.contains('[data-cy="shopping-history-section"] h3', 'Shopping History')
            .should('be.visible');
        cy.contains('[data-cy="shopping-history-item"]', boughtName)
            .should('be.visible')
            .within(() => {
                cy.get('[data-cy="shopping-history-status"]').should('contain', 'BOUGHT');
                cy.get('[data-cy="mark-needed-action"]').should('be.visible');
            });
        cy.contains('[data-cy="shopping-history-item"]', cancelledName)
            .should('be.visible')
            .within(() => {
                cy.get('[data-cy="shopping-history-status"]').should('contain', 'CANCELLED');
            });

        cy.get('[data-cy="shopping-history-section"]')
            .should('not.contain', neededName)
            .and('not.contain', otherHouseholdHistoryName);

        cy.intercept('PATCH', '**/api/v1/shopping-items/*/status').as('markNeeded');
        cy.intercept('GET', '**/api/v1/shopping-items/history').as('reloadHistory');

        cy.contains('[data-cy="shopping-history-item"]', boughtName)
            .find('[data-cy="mark-needed-action"]')
            .click();

        cy.wait('@markNeeded').its('response.statusCode').should('eq', 200);
        cy.wait('@reloadHistory').its('response.statusCode').should('eq', 200);

        cy.get('[data-cy="shopping-history-section"]').should('not.contain', boughtName);

        cy.get('[data-cy="shopping-tab-all"]').click();
        cy.contains('.needed-item', boughtName).should('be.visible');
    });
});
