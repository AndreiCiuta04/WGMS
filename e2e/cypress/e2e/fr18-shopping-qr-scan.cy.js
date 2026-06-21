describe('FR-18 QR shopping scan (manual/paste path)', () => {
    const password = 'Password123!';
    const runId = Date.now();

    const daniel = {
        firstName: 'Daniel',
        lastName: 'Scanner',
        email: `daniel.scan.qa.${runId}@test.com`,
        password
    };

    const household = {
        name: `Scan WG ${runId}`,
        address: 'Scan Street 18, Vienna'
    };

    const itemName = `Oat Milk ${runId}`;
    const validPayload = JSON.stringify({
        type: 'wgms.shopping-item',
        version: 1,
        name: itemName,
        quantityCount: 2,
        quantityAmount: 1,
        quantityUnit: 'LITER',
        visibility: 'SHARED'
    });

    // Force getUserMedia to reject *reliably* before the app loads, so the camera
    // path is deterministic in headless CI (no real camera, no permission prompt,
    // no hang). A plain `mediaDevices.getUserMedia = ...` assignment silently
    // no-ops when the method is inherited/non-writable (which it is in Electron),
    // so we install it with defineProperty(configurable:true) to guarantee the
    // override actually takes effect.
    const visitScanWithoutCamera = () => {
        cy.visit('/#/shopping-items/scan', {
            onBeforeLoad(win) {
                const reject = () =>
                    Promise.reject(new win.DOMException('No camera in test environment', 'NotFoundError'));
                if (!win.navigator.mediaDevices) {
                    Object.defineProperty(win.navigator, 'mediaDevices', {
                        configurable: true,
                        value: {}
                    });
                }
                Object.defineProperty(win.navigator.mediaDevices, 'getUserMedia', {
                    configurable: true,
                    writable: true,
                    value: reject
                });
            }
        });
    };

    // The manual entry box auto-opens when the camera is unavailable, but we must
    // not depend on that timing in CI. The toggle button is always rendered, so
    // open the panel through it whenever the textarea isn't already visible.
    // Clicking the toggle is a real DOM event, which reliably triggers rendering.
    const openManualEntry = () => {
        cy.get('[data-cy="scan-toggle-manual"]', { timeout: 10000 }).should('exist');
        cy.get('body').then(($body) => {
            if ($body.find('[data-cy="scan-manual-input"]:visible').length === 0) {
                cy.get('[data-cy="scan-toggle-manual"]').click();
            }
        });
        return cy.get('[data-cy="scan-manual-input"]', { timeout: 10000 }).should('be.visible');
    };

    beforeEach(() => {
        cy.clearLocalStorage();
    });

    it('redirects logged-out users away from the scan route', () => {
        cy.visit('/#/shopping-items/scan');
        cy.url().should('include', '/login');
    });

    it('adds an item from a pasted WGMS code and returns to the list', () => {
        cy.registerUser(daniel);
        cy.createHousehold(household);

        visitScanWithoutCamera();
        cy.contains('h2', 'Scan a shopping code').should('be.visible');

        // Set the value directly (the submit handler reads textarea.value live),
        // avoiding Cypress's special handling of "{" in .type().
        openManualEntry().invoke('val', validPayload);

        cy.intercept('POST', '**/api/v1/shopping-items/scan').as('scanRequest');
        cy.get('[data-cy="scan-manual-submit"]').click();
        cy.wait('@scanRequest').its('response.statusCode').should('eq', 201);

        // Lands back on the shopping list with the new item visible.
        cy.location('hash', { timeout: 10000 }).should('eq', '#/shopping-items');
        cy.contains(itemName).should('be.visible');
    });

    it('shows a validation error for a non-WGMS code and stays on the page', () => {
        cy.registerUser({ ...daniel, email: `daniel.scan.neg.${runId}@test.com` });
        cy.createHousehold({ ...household, name: `Scan WG Neg ${runId}` });

        visitScanWithoutCamera();
        openManualEntry().invoke('val', 'https://example.com/not-a-wgms-code');

        cy.intercept('POST', '**/api/v1/shopping-items/scan').as('scanRequest');
        cy.get('[data-cy="scan-manual-submit"]').click();
        cy.wait('@scanRequest').its('response.statusCode').should('eq', 400);

        cy.get('[data-cy="scan-error"]').should('be.visible');
        cy.location('hash').should('include', '#/shopping-items/scan');
    });
});