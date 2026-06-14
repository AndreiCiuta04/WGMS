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

function createExpenseHousehold() {
  const mainEmail = uniqueEmail('fr14-main');
  const roomEmail = uniqueEmail('fr14-room');

  return registerUser(mainEmail, 'FR14', 'Main').then((mainToken) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: bearer(mainToken),
      body: { name: `FR14 WG ${Date.now()}`, address: 'QA Street' },
    }).then((householdResponse) => {
      const householdId = householdResponse.body.id;

      return cy.request({
        method: 'POST',
        url: `${apiUrl}/households/${householdId}/invitations`,
        headers: bearer(mainToken),
        body: { invitedEmail: roomEmail, expiresInHours: 168 },
      }).then((invitationResponse) => {
        const token = invitationResponse.body.inviteUrl.split('/').pop();

        return registerUser(roomEmail, 'FR14', 'Room').then((roomToken) => {
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

function stubCameraAndVisionApi(visionText) {
  cy.intercept('POST', 'https://vision.googleapis.com/v1/images:annotate**', {
    statusCode: 200,
    body: {
      responses: [{
        textAnnotations: [{ description: visionText }]
      }]
    }
  }).as('visionApi');

  cy.window().then((win) => {
    const canvas = win.document.createElement('canvas');
    canvas.width = 100;
    canvas.height = 100;
    const context = canvas.getContext('2d');
    context.fillStyle = '#ffffff';
    context.fillRect(0, 0, canvas.width, canvas.height);
    const stream = canvas.captureStream();
    cy.stub(win.navigator.mediaDevices, 'getUserMedia').resolves(stream);
  });
}

function openCameraAndScan() {
  cy.contains('button', 'Open Camera').click();
  cy.get('video.camera-video')
    .should('be.visible')
    .and(($video) => {
      // The component refuses to scan a frame with no dimensions, so wait for the stream metadata.
      expect($video[0].videoWidth, 'camera frame has dimensions').to.be.greaterThan(0);
    });
  cy.contains('button', 'Capture & Scan').click();
}

describe('FR-14 Bill Scanner E2E', () => {
  it('FR-14 positive: scans a receipt via camera, creates a reviewed expense, and updates payment status', () => {
    createExpenseHousehold().then(({ mainEmail }) => {
      login(mainEmail);
      openExpenses();

      cy.contains('.split-row', 'FR14 Room')
        .find('input[type="checkbox"]')
        .check();

      cy.contains('button', 'Bill Scanner').click();

      stubCameraAndVisionApi('FR14 MARKET\n07.06.2026\nTotal EUR 18,00\n');

      openCameraAndScan();

      cy.wait('@visionApi');
      cy.contains('Receipt scanned. Review the extracted bill data.', { timeout: 15000 }).should('be.visible');
      cy.get('#billDescription').should('have.value', 'FR14 MARKET');
      cy.get('#billTotalAmount').should('have.value', '18,00');
      cy.get('#billDate').should('have.value', '2026-06-07');

      cy.intercept('POST', '**/api/v1/expenses/bill-preview').as('billPreview');
      cy.contains('button', 'Generate Review').click();
      cy.wait('@billPreview').its('response.statusCode').should('eq', 200);

      cy.contains('Bill data ready for review.').should('be.visible');
      cy.get('#description').should('have.value', 'FR14 MARKET');
      cy.get('#totalAmount').should('have.value', '18,00');
      cy.contains('.split-row', 'FR14 Main')
        .find('.split-amount input')
        .should('have.value', '9,00');
      cy.contains('.split-row', 'FR14 Room')
        .find('.split-amount input')
        .should('have.value', '9,00');

      cy.get('#description').clear().type('FR14 OCR Receipt Reviewed');
      cy.intercept('POST', '**/api/v1/expenses', (request) => {
        request.on('response', (response) => response.setDelay(400));
      }).as('createExpense');
      cy.contains('button', 'Create Expense').dblclick();
      cy.contains('button', 'Saving...').should('be.disabled');
      cy.wait('@createExpense').its('response.statusCode').should('eq', 201);
      cy.get('@createExpense.all').should('have.length', 1);

      cy.contains('.expense-item', 'FR14 OCR Receipt Reviewed').should('be.visible');
      cy.contains('.expense-item', 'FR14 OCR Receipt Reviewed')
        .find('.expense-total')
        .contains('18.00')
        .should('be.visible');

      cy.visit('/#/payment-status');
      cy.contains('h2', 'Payment Status').should('be.visible');
      cy.contains('.balance-row', 'FR14 Room owes FR14 Main')
        .contains('9.00')
        .should('be.visible');
    });
  });

  it('FR-14 negative: camera permission denied shows error message', () => {
    createExpenseHousehold().then(({ mainEmail }) => {
      login(mainEmail);
      openExpenses();

      cy.contains('button', 'Bill Scanner').click();

      cy.window().then((win) => {
        cy.stub(win.navigator.mediaDevices, 'getUserMedia').rejects(new win.DOMException('Permission denied', 'NotAllowedError'));
      });

      cy.contains('button', 'Open Camera').click();
      cy.contains('Camera not accessible. Allow camera permission in browser.').should('be.visible');
      cy.get('video.camera-video').should('not.exist');
    });
  });

  it('FR-14 negative: invalid reviewed bill data stays in review flow and creates no expense', () => {
    createExpenseHousehold().then(({ mainEmail }) => {
      login(mainEmail);
      openExpenses();

      cy.contains('button', 'Bill Scanner').click();
      cy.get('#billDescription').clear();
      cy.get('#billTotalAmount').clear().type('-5.00');

      cy.intercept('POST', '**/api/v1/expenses/bill-preview').as('billPreview');
      cy.contains('button', 'Generate Review').click();
      cy.contains('Enter bill data and select at least one participant.').should('be.visible');
      cy.get('@billPreview.all').should('have.length', 0);

      cy.contains('.expense-item', 'FR14 OCR Receipt Reviewed').should('not.exist');
    });
  });
});
