const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@example.com`;
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

function createHouseholdWithRoommate() {
  const runId = Date.now();

  const owner = {
    firstName: 'Fr10',
    lastName: 'Owner',
    email: uniqueEmail('fr10.owner'),
    password
  };

  const roommate = {
    firstName: 'Fr10',
    lastName: 'Roommate',
    email: uniqueEmail('fr10.roommate'),
    password
  };

  return registerViaApi(owner).then((ownerToken) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: bearer(ownerToken),
      body: {
        name: `FR10 Shopping WG ${runId}`,
        address: 'FR10 Street 1'
      }
    }).then((householdResponse) => {
      expect(householdResponse.status).to.be.oneOf([200, 201]);
      const householdId = householdResponse.body.id;

      return cy.request({
        method: 'POST',
        url: `${apiUrl}/households/${householdId}/invitations`,
        headers: bearer(ownerToken),
        body: {
          invitedEmail: roommate.email,
          expiresInHours: 168
        }
      }).then((invitationResponse) => {
        expect(invitationResponse.status).to.be.oneOf([200, 201]);
        const inviteToken = invitationResponse.body.inviteUrl.split('/').pop();

        return registerViaApi(roommate).then((roommateToken) => {
          return cy.request({
            method: 'POST',
            url: `${apiUrl}/invitations/${inviteToken}/accept`,
            headers: bearer(roommateToken)
          }).then((acceptResponse) => {
            expect(acceptResponse.status).to.be.oneOf([200, 201, 204]);

            return {
              owner,
              roommate,
              ownerToken,
              roommateToken,
              householdId
            };
          });
        });
      });
    });
  });
}

function createSingleUserHousehold(prefix) {
  const runId = Date.now();

  const user = {
    firstName: 'Fr10',
    lastName: 'Other',
    email: uniqueEmail(prefix),
    password
  };

  return registerViaApi(user).then((token) => {
    return cy.request({
      method: 'POST',
      url: `${apiUrl}/households`,
      headers: bearer(token),
      body: {
        name: `FR10 Other WG ${runId}`,
        address: 'FR10 Other Street 1'
      }
    }).then((householdResponse) => {
      expect(householdResponse.status).to.be.oneOf([200, 201]);

      return {
        user,
        token,
        householdId: householdResponse.body.id
      };
    });
  });
}

function createSuggestionViaApi(token, body) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/shopping-items/suggestions`,
    headers: bearer(token),
    body
  }).then((response) => {
    expect(response.status).to.be.oneOf([200, 201]);
    return response.body;
  });
}

function openSuggestionsTab() {
  cy.intercept('GET', '**/api/v1/shopping-items/suggestions').as('loadSuggestions');
  cy.get('[data-cy="shopping-tab-suggestions"]').click();
  cy.wait('@loadSuggestions').its('response.statusCode').should('eq', 200);
  cy.get('[data-cy="shopping-suggestions-section"]').should('be.visible');
}

describe('FR-10 Shopping Item Suggestions and Voting E2E', () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('creates a structured suggestion and shows the creator vote', () => {
    const runId = Date.now();
    const suggestionName = `FR10 Dishwasher Tabs ${runId}`;

    createHouseholdWithRoommate().then(({ owner }) => {
      cy.loginUser(owner.email, owner.password);
      cy.visit('/#/shopping-items');

      openSuggestionsTab();

      cy.get('[data-cy="suggestion-name-input"]')
        .clear()
        .type(suggestionName);

      cy.get('[data-cy="suggestion-quantity-amount-input"]')
        .clear()
        .type('1');

      cy.get('[data-cy="suggestion-quantity-unit-select"]')
        .select('PACK');

      cy.get('[data-cy="suggestion-notes-input"]')
        .clear()
        .type('Almost empty');

      cy.intercept('POST', '**/api/v1/shopping-items/suggestions').as('createSuggestion');
      cy.get('[data-cy="create-suggestion-button"]').click();

      cy.wait('@createSuggestion').then(({ response }) => {
        expect(response.statusCode).to.be.oneOf([200, 201]);
        expect(response.body.name).to.eq(suggestionName);
        expect(response.body.quantity).to.eq('1 pack');
        expect(response.body.voteCount).to.eq(1);
        expect(response.body.votedByCurrentUser).to.eq(true);
      });

      cy.contains('[data-cy="shopping-suggestion-item"]', suggestionName)
        .should('be.visible')
        .within(() => {
          cy.get('[data-cy="suggestion-vote-count"]').should('contain', '1 vote');
          cy.get('[data-cy="suggestion-voted-badge"]').should('contain', 'You voted');
          cy.get('[data-cy="suggestion-voter-list"]').should('contain', 'Fr10 Owner');
        });
    });
  });

  it('allows a second household member to vote and remove the vote', () => {
    const runId = Date.now();
    const suggestionName = `FR10 Coffee ${runId}`;

    createHouseholdWithRoommate().then(({ ownerToken, roommate }) => {
      createSuggestionViaApi(ownerToken, {
        name: suggestionName,
        quantity: '2 pack',
        notes: 'Shared suggestion'
      });

      cy.loginUser(roommate.email, roommate.password);
      cy.visit('/#/shopping-items');

      openSuggestionsTab();

      cy.contains('[data-cy="shopping-suggestion-item"]', suggestionName)
        .as('suggestionItem')
        .should('be.visible');

      cy.intercept('POST', '**/api/v1/shopping-items/suggestions/*/votes').as('voteSuggestion');

      cy.get('@suggestionItem')
        .find('[data-cy="vote-suggestion-action"]')
        .click();

      cy.wait('@voteSuggestion').then(({ response }) => {
        expect(response.statusCode).to.eq(200);
        expect(response.body.voteCount).to.eq(2);
        expect(response.body.votedByCurrentUser).to.eq(true);
      });

      cy.contains('[data-cy="shopping-suggestion-item"]', suggestionName)
        .within(() => {
          cy.get('[data-cy="suggestion-vote-count"]').should('contain', '2 votes');
          cy.get('[data-cy="suggestion-popular-badge"]').should('contain', 'Popular');
          cy.get('[data-cy="suggestion-voted-badge"]').should('contain', 'You voted');
        });

      cy.intercept('DELETE', '**/api/v1/shopping-items/suggestions/*/votes')
        .as('removeSuggestionVote');

      cy.contains('[data-cy="shopping-suggestion-item"]', suggestionName)
        .find('[data-cy="remove-suggestion-vote-action"]')
        .click();

      cy.wait('@removeSuggestionVote').then(({ response }) => {
        expect(response.statusCode).to.eq(200);
        expect(response.body.voteCount).to.eq(1);
        expect(response.body.votedByCurrentUser).to.eq(false);
      });

      cy.contains('[data-cy="shopping-suggestion-item"]', suggestionName)
        .within(() => {
          cy.get('[data-cy="suggestion-vote-count"]').should('contain', '1 vote');
          cy.get('[data-cy="suggestion-popular-badge"]').should('not.exist');
          cy.get('[data-cy="vote-suggestion-action"]').should('exist');
        });
    });
  });

  it('promotes a suggestion into the normal and aggregated shopping lists', () => {
    const runId = Date.now();
    const suggestionName = `FR10 Rice ${runId}`;

    createHouseholdWithRoommate().then(({ owner, ownerToken }) => {
      createSuggestionViaApi(ownerToken, {
        name: suggestionName,
        quantity: '2 kg',
        notes: 'For shared meals'
      });

      cy.loginUser(owner.email, owner.password);
      cy.visit('/#/shopping-items');

      openSuggestionsTab();

      cy.on('window:confirm', () => true);
      cy.intercept('POST', '**/api/v1/shopping-items/suggestions/*/promote')
        .as('promoteSuggestion');

      cy.contains('[data-cy="shopping-suggestion-item"]', suggestionName)
        .find('[data-cy="promote-suggestion-action"]')
        .click();

      cy.wait('@promoteSuggestion').then(({ response }) => {
        expect(response.statusCode).to.eq(200);
        expect(response.body.name).to.eq(suggestionName);
        expect(response.body.visibility).to.eq('SHARED');
        expect(response.body.status).to.eq('NEEDED');
      });

      cy.contains('[data-cy="shopping-suggestion-item"]', suggestionName)
        .should('not.exist');

      cy.get('[data-cy="shopping-tab-all"]').click();
      cy.contains('.needed-item', suggestionName).should('be.visible');

      cy.get('[data-cy="shopping-tab-household-list"]').click();
      cy.contains('[data-cy="aggregated-shopping-item"]', suggestionName)
        .should('be.visible');
    });
  });

  it('prevents a user from another household from voting', () => {
    const runId = Date.now();
    const suggestionName = `FR10 Isolated Item ${runId}`;

    createHouseholdWithRoommate().then(({ ownerToken }) => {
      createSuggestionViaApi(ownerToken, {
        name: suggestionName,
        quantity: '1 pack',
        notes: null
      }).then((suggestion) => {
        createSingleUserHousehold('fr10.other').then(({ token: otherToken }) => {
          cy.request({
            method: 'POST',
            url: `${apiUrl}/shopping-items/suggestions/${suggestion.id}/votes`,
            headers: bearer(otherToken),
            failOnStatusCode: false
          }).then((response) => {
            expect(response.status).to.eq(403);
          });

          cy.request({
            method: 'GET',
            url: `${apiUrl}/shopping-items/suggestions`,
            headers: bearer(otherToken)
          }).then((response) => {
            expect(response.status).to.eq(200);
            expect(response.body).to.have.length(0);
          });
        });
      });
    });
  });

  it('rejects unauthenticated voting', () => {
    createHouseholdWithRoommate().then(({ ownerToken }) => {
      createSuggestionViaApi(ownerToken, {
        name: `FR10 Security ${Date.now()}`,
        quantity: '1 piece',
        notes: null
      }).then((suggestion) => {
        cy.request({
          method: 'POST',
          url: `${apiUrl}/shopping-items/suggestions/${suggestion.id}/votes`,
          failOnStatusCode: false
        }).its('status').should('be.oneOf', [401, 403]);
      });
    });
  });
});