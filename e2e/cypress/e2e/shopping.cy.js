describe('FR-07 Shopping List E2E Workflow', () => {
    const password = 'Password123!';
    const runId = Date.now();

    const daniel = {
        firstName: 'Daniel',
        lastName: 'Developer',
        email: `daniel.shopping.qa.${runId}@test.com`,
        password
    };

    const household = {
        name: `Shopping WG ${runId}`,
        address: 'Shopping Avenue 7, Vienna'
    };

    const shoppingItem = {
        name: `Milk ${runId}`,
        quantity: '2 Liters',
        notes: 'Whole milk preferred'
    };

    const updatedItem = {
        name: `Soy Milk ${runId}`,
        quantity: '3 Liters',
        notes: 'Unsweetened'
    };

    beforeEach(() => {
        cy.clearLocalStorage();
    });

    it('logged-out user is redirected away from the route', () => {
        cy.visit('/#/shopping-items');
        cy.url().should('include', '/login');

        cy.visit('/#/shopping-items/create');
        cy.url().should('include', '/login');
    });

    it('creates, displays, edits, status transitions, and deletes a shopping item', () => {
        // Register user and initialize household/WG
        cy.registerUser(daniel);
        cy.createHousehold(household);

        // Navigate to shopping list
        cy.visit('/#/shopping-items');
        cy.contains('h2', 'Shopping List').should('be.visible');

        // Verify empty state is displayed
        cy.contains('All stocked up! No items needed right now.').should('be.visible');

        // Try creating with empty name (validation check)
        cy.visit('/#/shopping-items/create');
        cy.contains('button', 'Add Item').click();
        cy.contains('Name is required.').should('be.visible');

        // Create a valid shopping item
        cy.get('#name').type(shoppingItem.name);
        cy.get('#quantity').type(shoppingItem.quantity);
        cy.get('#notes').type(shoppingItem.notes);
        
        cy.intercept('POST', '**/api/v1/shopping-items').as('createRequest');
        cy.contains('button', 'Add Item').click();
        cy.wait('@createRequest').its('response.statusCode').should('eq', 201);

        // Verify it appears in the Needed list
        cy.location('hash').should('eq', '#/shopping-items');
        cy.contains(shoppingItem.name).should('be.visible');
        cy.contains(shoppingItem.quantity).should('be.visible');
        cy.contains(shoppingItem.notes).should('be.visible');

        // Edit the shopping item
        cy.contains('.needed-item', shoppingItem.name).find('a[title="Edit"]').click();
        cy.location('hash').should('include', '/edit');
        
        cy.get('#name').clear().type(updatedItem.name);
        cy.get('#quantity').clear().type(updatedItem.quantity);
        cy.get('#notes').clear().type(updatedItem.notes);

        cy.intercept('PUT', '**/api/v1/shopping-items/*').as('updateRequest');
        cy.contains('button', 'Save Changes').click();
        cy.wait('@updateRequest').its('response.statusCode').should('eq', 200);

        // Verify list shows updated values
        cy.location('hash').should('eq', '#/shopping-items');
        cy.contains(updatedItem.name).should('be.visible');
        cy.contains(updatedItem.quantity).should('be.visible');
        cy.contains(updatedItem.notes).should('be.visible');

        // Mark as Bought
        cy.intercept('PATCH', '**/api/v1/shopping-items/*/status').as('statusRequest1');
        cy.contains('.needed-item', updatedItem.name).find('button[title="Mark as Bought"]').click();
        cy.wait('@statusRequest1').its('response.statusCode').should('eq', 200);

        // Verify it moved from Needed list to Completed section
        cy.contains('.needed-item', updatedItem.name).should('not.exist');
        
        // Expand the completed section
        cy.contains('.completed-header', 'Bought & Cancelled').click();
        cy.contains('.completed-item', updatedItem.name).should('be.visible');

        // Reopen (Mark as Needed)
        cy.intercept('PATCH', '**/api/v1/shopping-items/*/status').as('statusRequest2');
        cy.contains('.completed-item', updatedItem.name).find('button[title="Mark as Needed"]').click();
        cy.wait('@statusRequest2').its('response.statusCode').should('eq', 200);

        // Verify it moved back to Needed list
        cy.contains('.needed-item', updatedItem.name).should('be.visible');

        // Cancel the item
        cy.intercept('PATCH', '**/api/v1/shopping-items/*/status').as('statusRequest3');
        cy.contains('.needed-item', updatedItem.name).find('button[title="Cancel"]').click();
        cy.wait('@statusRequest3').its('response.statusCode').should('eq', 200);

        // Verify it is in Completed section
        cy.contains('.completed-item', updatedItem.name).should('be.visible');

        // Delete the item
        cy.intercept('DELETE', '**/api/v1/shopping-items/*').as('deleteRequest');
        cy.contains('.completed-item', updatedItem.name).find('button[title="Delete"]').click();
        cy.wait('@deleteRequest').its('response.statusCode').should('eq', 204);

        // Verify it is gone entirely
        cy.contains(updatedItem.name).should('not.exist');
    });
});
