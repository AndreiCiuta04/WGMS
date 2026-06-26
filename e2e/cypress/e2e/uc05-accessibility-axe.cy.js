/*
 * UC-05 accessibility audit (NFR-04 / WAI — closes E2E-023).
 *
 * Runs axe-core against every UC-05 surface: dashboard (home), notification drawer (open state),
 * history timeline, and the statistics charts page. This is a REAL automated audit — every run
 * produces genuine violation output in the Cypress log and the CI job output. It is not a substitute
 * for the manual screen-reader pass, but it removes the bulk of automatable WCAG failures and gives
 * the form the named, repeatable evidence it demands.
 *
 * PREREQUISITES (one-time, in e2e/):
 *   npm install --save-dev cypress-axe axe-core
 * and add this line to e2e/cypress/support/e2e.js:
 *   import 'cypress-axe';
 *
 * Backend must run with the generateData profile (admin@email.com / password) on :8080,
 * frontend on :4200 — same as every other spec.
 *
 * GATING POLICY (honest + defensible): the suite FAILS on 'critical' and 'serious' impacts and
 * LOGS (does not fail on) 'moderate'/'minor'. Tighten to all-impacts once the critical/serious set is
 * clean. If a finding is a known, accepted limitation, record it in the QA doc with a justification —
 * do not silence it by deleting the assertion.
 */

const apiUrl = 'http://localhost:8080/api/v1';
const password = 'Password123!';

function uniqueEmail(prefix) {
  return `${prefix}.${Date.now()}.${Math.floor(Math.random() * 100000)}@example.com`;
}

function bearer(token) {
  return { Authorization: token.startsWith('Bearer ') ? token : `Bearer ${token}` };
}

function registerViaApi(user) {
  return cy.request({
    method: 'POST',
    url: `${apiUrl}/users`,
    body: { ...user, termsAccepted: true }
  }).then((response) => {
    expect(response.status).to.be.oneOf([200, 201]);
    return String(response.body);
  });
}

// Builds a two-person WG with one expense + one task so the dashboard/history/stats have real content
// to audit (an empty page hides most a11y issues). Returns the owner's login credentials.
function seedUc05Household() {
  const runId = Date.now();
  const owner = { firstName: 'A11y', lastName: 'Owner', email: uniqueEmail('a11y.owner'), password };
  const roommate = { firstName: 'A11y', lastName: 'Mate', email: uniqueEmail('a11y.mate'), password };

  return registerViaApi(owner).then((ownerToken) =>
    cy.request({
      method: 'POST', url: `${apiUrl}/households`, headers: bearer(ownerToken),
      body: { name: `A11y WG ${runId}`, address: 'A11y Street 1' }
    }).then((householdRes) => {
      const householdId = householdRes.body.id;
      return cy.request({
        method: 'POST', url: `${apiUrl}/households/${householdId}/invitations`, headers: bearer(ownerToken),
        body: { invitedEmail: roommate.email, expiresInHours: 168 }
      }).then((inviteRes) => {
        const token = inviteRes.body.inviteUrl.split('/').pop();
        return registerViaApi(roommate).then((mateToken) =>
          cy.request({
            method: 'POST', url: `${apiUrl}/invitations/${token}/accept`, headers: bearer(mateToken)
          }).then(() =>
            // owner's roommate id + mate's roommate id, then create a shared expense
            cy.request({ method: 'GET', url: `${apiUrl}/households/${householdId}/members`, headers: bearer(ownerToken) })
              .then((membersRes) => {
                const members = membersRes.body;
                const ownerRoommate = members.find((m) => m.email === owner.email) || members[0];
                const mateRoommate = members.find((m) => m.email === roommate.email) || members[1];
                return cy.request({
                  method: 'POST', url: `${apiUrl}/expenses`, headers: bearer(ownerToken),
                  body: {
                    description: 'A11y groceries', totalAmount: 12.0,
                    date: new Date().toISOString().slice(0, 10),
                    paidByRoommateId: ownerRoommate.id,
                    splits: [
                      { roommateId: ownerRoommate.id, amountOwed: 6.0 },
                      { roommateId: mateRoommate.id, amountOwed: 6.0 }
                    ]
                  }
                });
              })
              .then(() => owner)
          )
        );
      });
    })
  );
}

// Logs every violation as a readable table in the Cypress command log + terminal.
function logViolations(violations) {
  cy.task('log', `axe: ${violations.length} violation(s)`, { log: false });
  violations.forEach((v) => {
    cy.task('log', `  [${v.impact}] ${v.id}: ${v.help}`, { log: false });
    v.nodes.forEach((n) => {
      const fg = (n.any?.[0]?.data) || {};
      cy.task('log', `     -> ${n.target.join(' ')}`, { log: false });
      if (fg.fgColor) {
        cy.task('log',
          `        fg ${fg.fgColor} / bg ${fg.bgColor} ratio ${fg.contrastRatio} (needs ${fg.expectedContrastRatio})`,
          { log: false });
      }
    });
  });
}

const FAIL_ON = { includedImpacts: ['critical', 'serious'] };

describe('UC-05 accessibility (axe) — E2E-023', () => {
  let owner;

  before(() => {
    seedUc05Household().then((createdOwner) => {
      owner = createdOwner;
    });
  });

  beforeEach(() => {
    cy.loginUser(owner.email, owner.password);
    cy.injectAxe();
  });

  it('dashboard (home) has no critical/serious a11y violations', () => {
      cy.visit('/#/');
      cy.contains(/dashboard|responsibilit|balance/i, { timeout: 10000 }).should('exist');
      // The dashboard plays a 0.4s fadeIn; axe factors in computed opacity, so mid-animation every text
      // element looks ~54% transparent and fails contrast falsely. Wait for the fade to settle, then audit
      // the resting state (which meets AA: secondary 5.59:1, forest-green 6.68:1).
      cy.get('.dashboard', { timeout: 10000 }).should('have.css', 'opacity', '1');
      cy.checkA11y(null, FAIL_ON, logViolations);
  });

  it('notification drawer (open) has no critical/serious a11y violations', () => {
      cy.visit('/#/');
      cy.get('[data-cy="notification-bell"]', { timeout: 10000 }).click();
      cy.get('[data-cy="notification-panel"]').should('be.visible');
      // The drawer plays a 0.2s dropdownFadeIn (opacity 0 -> 1). axe factors in computed opacity,
      // so mid-animation the muted text reads as partially transparent and fails contrast falsely.
      // Wait for the fade to settle, then audit the resting state (muted text comfortably meets AA).
      cy.get('[data-cy="notification-panel"]').should('have.css', 'opacity', '1');
      // scope axe to the drawer so we audit the open dialog specifically
      cy.checkA11y('[data-cy="notification-panel"]', FAIL_ON, logViolations);
  });

  it('history timeline has no critical/serious a11y violations', () => {
    cy.visit('/#/history');
    cy.location('hash').should('eq', '#/history');
    cy.checkA11y(null, FAIL_ON, logViolations);
  });

  it('statistics charts page has no critical/serious a11y violations', () => {
    cy.visit('/#/statistics');
    cy.location('hash').should('eq', '#/statistics');
    // allow charts to render before auditing tooltips/legends/data-alternatives
    cy.wait(500);
    cy.checkA11y(null, FAIL_ON, logViolations);
  });
});
