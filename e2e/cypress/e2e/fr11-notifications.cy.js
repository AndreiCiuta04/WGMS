const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@example.com`;
}

function isoDate(daysFromToday = 0) {
  const date = new Date();
  date.setDate(date.getDate() + daysFromToday);
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

function bearer(token) {
  return {
    Authorization: token.startsWith('Bearer ') ? token : `Bearer ${token}`
  };
}

function registerViaApi(user) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/users`,
    body: {
      ...user,
      termsAccepted: true
    }
  }).then((response) => {
    expect(response.status).to.be.oneOf([200, 201]);
    return String(response.body);
  });
}

function createNotificationHousehold() {
  const runId = Date.now();
  const owner = {
    firstName: 'Fr11',
    lastName: 'Owner',
    email: uniqueEmail('fr11.owner'),
    password
  };
  const roommate = {
    firstName: 'Fr11',
    lastName: 'Roommate',
    email: uniqueEmail('fr11.roommate'),
    password
  };

  return registerViaApi(owner).then((ownerToken) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: bearer(ownerToken),
      body: { name: `FR11 Notifications WG ${runId}`, address: 'FR11 Street 1' }
    }).then((householdResponse) => {
      const householdId = householdResponse.body.id;

      return cy.request({
        method: 'POST',
        url: `${apiUrl}/households/${householdId}/invitations`,
        headers: bearer(ownerToken),
        body: { invitedEmail: roommate.email, expiresInHours: 168 }
      }).then((invitationResponse) => {
        const inviteToken = invitationResponse.body.inviteUrl.split('/').pop();

        return registerViaApi(roommate).then((roommateToken) => {
          return cy.request({
            method: 'POST',
            url: `${apiUrl}/invitations/${inviteToken}/accept`,
            headers: bearer(roommateToken)
          }).then(() => ({ owner, roommate, ownerToken, roommateToken, householdId }));
        });
      });
    });
  });
}

function createAssignedTask(token, { title, assignedToEmail }) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/tasks`,
    headers: bearer(token),
    body: {
      title,
      description: 'FR-11 notification test task',
      dueDate: isoDate(2),
      assignedToEmail
    }
  }).then((response) => {
    expect(response.status).to.eq(201);
    return response.body;
  });
}

function openNotificationPanel() {
  cy.intercept('GET', '**/api/v1/notifications').as('loadNotifications');
  cy.get('[data-cy="notification-bell"]').click();
  cy.get('[data-cy="notification-panel"]').should('be.visible');
  cy.wait('@loadNotifications').its('response.statusCode').should('eq', 200);
}

describe('FR-11 Notifications and Reminders E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('FR-11 positive: assignee sees task reminder and can dismiss it', () => {
    const runId = Date.now();
    const taskTitle = `FR11 Assigned Task ${runId}`;

    createNotificationHousehold().then(({ owner, roommate, ownerToken }) => {
      createAssignedTask(ownerToken, {
        title: taskTitle,
        assignedToEmail: roommate.email
      }).then(() => {
        cy.loginUser(roommate.email, roommate.password);
        cy.get('[data-cy="notification-bell"] .badge', { timeout: 10000 }).should('be.visible');

        openNotificationPanel();
        cy.get('[data-cy="notification-item"]').should('contain', taskTitle);
        cy.get('[data-cy="notification-bell"] .badge').should('be.visible');

        cy.intercept('PATCH', '**/api/v1/notifications/**/read').as('markRead');
        cy.get('[data-cy="notification-item"]').first().click();
        cy.wait('@markRead').its('response.statusCode').should('eq', 200);
        cy.location('hash').should('eq', '#/tasks');
        cy.get('[data-cy="notification-bell"] .badge').should('not.exist');
      });
    });
  });

  it('FR-11 empty-state: user without notifications sees neutral panel message', () => {
    const user = {
      firstName: 'Fr11',
      lastName: 'Empty',
      email: uniqueEmail('fr11.empty'),
      password
    };

    cy.registerUser(user);
    cy.createHousehold({
      name: `FR11 Empty WG ${Date.now()}`,
      address: 'FR11 Empty Street'
    });

    openNotificationPanel();
    cy.get('[data-cy="notification-empty-state"]').should('contain', 'No notifications yet');
    cy.get('[data-cy="notification-bell"] .badge').should('not.exist');
  });

  it('FR-11 security: unauthenticated API access and logout block reminders', () => {
    const user = {
      firstName: 'Fr11',
      lastName: 'Security',
      email: uniqueEmail('fr11.security'),
      password
    };

    cy.request({
      method: 'GET',
      url: `${apiUrl}/notifications`,
      failOnStatusCode: false
    }).its('status').should('be.oneOf', [401, 403]);

    cy.registerUser(user);
    cy.window().its('localStorage.authToken').should('exist');
    cy.logoutUser();

    cy.request({
      method: 'GET',
      url: `${apiUrl}/notifications`,
      failOnStatusCode: false
    }).its('status').should('be.oneOf', [401, 403]);
  });

  it('FR-11 security: user cannot mark another user notification as read', () => {
    createNotificationHousehold().then(({ owner, roommate, ownerToken, roommateToken }) => {
      createAssignedTask(ownerToken, {
        title: `FR11 Cross User ${Date.now()}`,
        assignedToEmail: roommate.email
      }).then(() => {
        cy.request({
          method: 'GET',
          url: `${apiUrl}/notifications`,
          headers: bearer(roommateToken)
        }).then((response) => {
          expect(response.status).to.eq(200);
          expect(response.body.length).to.be.greaterThan(0);
          const notificationId = response.body[0].id;

          cy.request({
            method: 'PATCH',
            url: `${apiUrl}/notifications/${notificationId}/read`,
            headers: bearer(ownerToken),
            failOnStatusCode: false
          }).its('status').should('eq', 403);
        });
      });
    });
  });
});
