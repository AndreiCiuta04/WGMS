const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@test.com`;
}

function uniqueUser(prefix, firstName = 'Gdpr', lastName = 'Domain') {
  return {
    firstName,
    lastName,
    email: uniqueEmail(prefix),
    password
  };
}

function auth(token) {
  return { Authorization: token };
}

function todayIso() {
  const date = new Date();
  return toLocalIsoDate(date);
}

function futureDateIso(daysAhead = 3) {
  const date = new Date();
  date.setDate(date.getDate() + daysAhead);
  return toLocalIsoDate(date);
}

function toLocalIsoDate(date) {
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

function registerApi(user) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/users`,
    body: {
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      password: user.password,
      termsAccepted: true
    }
  }).then(response => {
    expect(response.status).to.eq(201);
    return String(response.body);
  });
}

function loginUi(user) {
  cy.loginUser(user.email, user.password);
}

function createHousehold(token, name) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/households`,
    headers: auth(token),
    body: { name, address: 'GDPR QA Street 1' }
  }).then(response => {
    expect(response.status).to.eq(201);
    return response.body;
  });
}

function createInvitation(token, householdId, invitedEmail) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/households/${householdId}/invitations`,
    headers: auth(token),
    body: { invitedEmail, expiresInHours: 168 }
  }).then(response => {
    expect(response.status).to.eq(201);
    return String(response.body.inviteUrl).split('/').pop();
  });
}

function acceptInvitation(token, invitationToken) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/invitations/${invitationToken}/accept`,
    headers: auth(token)
  }).then(response => {
    expect(response.status).to.eq(200);
    return response.body;
  });
}

function getMembers(token, householdId) {
  return cy.request({
    method: 'GET',
    url: `${apiUrl}/households/${householdId}/members`,
    headers: auth(token)
  }).then(response => {
    expect(response.status).to.eq(200);
    return response.body;
  });
}

function createTask(token, task) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/tasks`,
    headers: auth(token),
    body: task
  }).then(response => {
    expect(response.status).to.eq(201);
    return response.body;
  });
}

function completeTask(token, taskId) {
  return cy.request({
    method: 'PATCH',
    url: `${apiUrl}/tasks/${taskId}/status`,
    headers: auth(token),
    body: { status: 'DONE' }
  }).then(response => {
    expect(response.status).to.eq(200);
    return response.body;
  });
}

function createExpense(token, description, paidByRoommateId, splits) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/expenses`,
    headers: auth(token),
    body: {
      description,
      totalAmount: 20,
      date: todayIso(),
      paidByRoommateId,
      splits
    }
  }).then(response => {
    expect(response.status).to.eq(201);
    return response.body;
  });
}

function createPayment(token, toRoommateId) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/payments`,
    headers: auth(token),
    body: {
      toRoommateId,
      amount: 4,
      date: todayIso()
    }
  }).then(response => {
    expect(response.status).to.eq(201);
    return response.body;
  });
}

describe('GDPR domain correction and anonymized history readability', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('persists task correction after reload and rejects cross-WG task edits', () => {
    const owner = uniqueUser('gdpr.domain.owner', 'Domain', 'Owner');
    const outsider = uniqueUser('gdpr.domain.outsider', 'Domain', 'Outsider');
    const originalTitle = `GDPR Task Original ${Date.now()}`;
    const updatedTitle = `GDPR Task Corrected ${Date.now()}`;
    const updatedDescription = 'Corrected task description stays visible after reload.';
    let ownerToken;
    let taskId;

    registerApi(owner).then(token => {
      ownerToken = token;
      return createHousehold(ownerToken, `GDPR Domain WG ${Date.now()}`);
    }).then(() => createTask(ownerToken, {
      title: originalTitle,
      description: 'Original task description',
      dueDate: futureDateIso(),
      assignedToEmail: owner.email
    })).then(task => {
      taskId = task.id;
      loginUi(owner);

      cy.visit('/#/tasks');
      cy.contains('.wgms-card', originalTitle).contains('a', 'Edit').click();
      cy.location('hash').should('eq', `#/tasks/${taskId}/edit`);

      cy.get('#title').clear().type(updatedTitle);
      cy.get('#description').clear().type(updatedDescription);
      cy.intercept('PUT', `**/api/v1/tasks/${taskId}`).as('updateTask');
      cy.contains('button', 'Save Changes').click();
      cy.wait('@updateTask').its('response.statusCode').should('eq', 200);

      cy.location('hash').should('eq', '#/tasks');
      cy.contains(updatedTitle).should('be.visible');
      cy.contains(updatedDescription).should('be.visible');

      cy.reload();
      cy.contains(updatedTitle).should('be.visible');
      cy.contains(updatedDescription).should('be.visible');

      return registerApi(outsider);
    }).then(outsiderToken => createHousehold(outsiderToken, `GDPR Other WG ${Date.now()}`)
      .then(() => cy.request({
        method: 'PUT',
        url: `${apiUrl}/tasks/${taskId}`,
        headers: auth(outsiderToken),
        failOnStatusCode: false,
        body: {
          title: 'Forbidden cross-WG correction',
          description: 'This must not be saved.',
          dueDate: futureDateIso(),
          assignedToEmail: outsider.email
        }
      })))
      .its('status').should('eq', 403);
  });

  it('keeps task and payment history readable after a throwaway user is anonymized', () => {
    const owner = uniqueUser('gdpr.history.owner', 'History', 'Owner');
    const throwaway = uniqueUser('gdpr.history.delete', 'History', 'Delete');
    const historyTaskTitle = `GDPR Deleted User Task ${Date.now()}`;
    const historyExpenseTitle = `GDPR Deleted User Expense ${Date.now()}`;
    let ownerToken;
    let throwawayToken;
    let householdId;
    let ownerRoommate;
    let throwawayRoommate;

    registerApi(owner).then(token => {
      ownerToken = token;
      return createHousehold(ownerToken, `GDPR History WG ${Date.now()}`);
    }).then(household => {
      householdId = household.id;
      return registerApi(throwaway);
    }).then(token => {
      throwawayToken = token;
      return createInvitation(ownerToken, householdId, throwaway.email);
    }).then(invitationToken => acceptInvitation(throwawayToken, invitationToken))
      .then(() => getMembers(ownerToken, householdId))
      .then(members => {
        ownerRoommate = members.find(member => member.email === owner.email);
        throwawayRoommate = members.find(member => member.email === throwaway.email);
        expect(ownerRoommate).to.exist;
        expect(throwawayRoommate).to.exist;

        return createTask(throwawayToken, {
          title: historyTaskTitle,
          description: 'Task remains readable after account anonymization.',
          dueDate: futureDateIso(),
          assignedToEmail: throwaway.email
        });
      }).then(task => completeTask(throwawayToken, task.id))
      .then(() => createExpense(ownerToken, historyExpenseTitle, ownerRoommate.id, [
        { roommateId: ownerRoommate.id, amountOwed: 10 },
        { roommateId: throwawayRoommate.id, amountOwed: 10 }
      ]))
      .then(() => createPayment(throwawayToken, ownerRoommate.id))
      .then(() => cy.request({
        method: 'DELETE',
        url: `${apiUrl}/users/me`,
        headers: auth(throwawayToken)
      }))
      .then(deleteResponse => {
        expect(deleteResponse.status).to.eq(204);

        loginUi(owner);
        cy.visit('/#/history');
        cy.contains('h2', 'History').should('be.visible');
        cy.contains(historyTaskTitle).should('be.visible');
        cy.contains('DONE').should('be.visible');
        cy.contains(historyExpenseTitle).should('be.visible');
        cy.contains('RECORDED').should('be.visible');
        cy.contains('Deleted User').should('be.visible');
        cy.get('body').should('not.contain', throwaway.email);

        cy.visit('/#/payment-status');
        cy.contains('h2', 'Payment Status').should('be.visible');
        cy.contains('Deleted User paid History Owner').should('be.visible');
        cy.contains('€4.00').should('be.visible');
        cy.get('body').should('not.contain', throwaway.email);
      });
  });
});
