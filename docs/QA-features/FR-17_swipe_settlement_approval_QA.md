# WGMS — FR-17 Swipe-Based Settlement Approval QA Form

## Scope

This QA form checks **FR-17 — Swipe-based settlement approval with test account**.

The goal is to verify that WGMS provides a fast approval/rejection interface for pending settlements without breaking the deterministic settlement engine, payment status overview, or shared expense tracking.

This QA form focuses on:

- pending settlement approval/rejection workflow
- swipe interaction and non-swipe fallback
- backend authorization and household scoping
- consistency with deterministic settlement/payment logic
- test account/demo data
- frontend/mobile usability
- regression safety for FR-05 and FR-06

This feature must not execute real money transfers.

## Implementation Evidence — 2026-06-23

- Backend exposes `GET /api/v1/payments/settlement-approvals`, `POST /api/v1/payments/settlement-approvals/{settlementId}/approve`, and `POST /api/v1/payments/settlement-approvals/{settlementId}/reject`.
- Pending settlements are derived from the existing deterministic balance calculation; frontend-submitted amounts are not trusted.
- Approving a settlement records a normal `Payment` and persists an `APPROVED` settlement decision.
- Rejecting a settlement persists a `REJECTED` settlement decision without recording a payment.
- Only the owing roommate may approve or reject a pending settlement.
- The frontend payment status page includes settlement cards with swipe handling and approve/reject button fallback.
- Demo data includes `debtor.fr17@example.com` with password `Password123!` in the datagen profile.
- The FR-17 datagen seed resets deterministic demo financial data on every datagen backend start, so manual approval/rejection tests can be repeated after restarting the backend.
- The FR-17 debtor demo account starts with three pending settlements: EUR 15.90, EUR 21.00, and EUR 32.00.
- Added backend unit/integration tests, frontend service/component tests, and a Cypress spec for the FR-17 user flow.

Verified commands:

```bash
cd backend
mvn "-Dtest=PaymentServiceTest,PaymentEndpointTest" test
```

```bash
cd frontend
npx ng test --include src/app/services/payment.service.spec.ts --include src/app/components/payment-status/payment-status.component.spec.ts --watch=false --browsers=ChromeHeadless
npx ng build
```

```bash
cd e2e
npx cypress run --headed --browser chrome --spec "cypress/e2e/fr17-swipe-settlement-approval.cy.js"
```

Historical result: 2 passing, 0 failing.

## Merged-Branch QA Evidence — 2026-06-25

Branch context: `UC-03/QA`, HEAD `00b867c` (`Merge remote-tracking branch 'origin/dev' into UC-03/QA`), dirty working tree with QA fixes.

Verified commands:

```bash
cd backend
mvn -Dtest=ExpenseEndpointTest,PaymentEndpointTest,SecurityTest test
mvn test
```

Result: PASS. Focused backend suite ran 58 tests with 0 failures/errors/skips. Full backend suite ran 397 tests with 0 failures/errors/skips.

```bash
cd frontend
npm run lint
npm run build
npx ng test --watch=false --browsers=ChromeHeadless
```

Result: PASS. Lint reported all files pass, build emitted `frontend/dist/sepr-group-phase`, and Angular ran 404 successful specs.

```bash
cd e2e
npx cypress run --spec "cypress/e2e/fr17-swipe-settlement-approval.cy.js,cypress/e2e/fr05-expenses.cy.js,cypress/e2e/fr06-payment-status.cy.js,cypress/e2e/fr14-bill-scanner.cy.js,cypress/e2e/fr07-shopping-crud.cy.js"
npx cypress run
```

Result: PASS. Focused UC-03/FR-17 Cypress bundle ran 33 tests with 0 failures. Full Cypress suite ran 112 tests across 30 specs with 0 failures.

Additional fixes from this QA pass:
- OpenAPI YAML was returning 403 because `/v3/api-docs.yaml` was not explicitly permitted. Fixed in `SecurityConfig.java` and covered by `SecurityTest.givenNoOneLoggedIn_whenFetchOpenApiDocs_thenJsonAndYamlArePublic`.
- FR-17 overlay could cover the FR-06 payment button in full-suite order. Fixed the FR-06 Cypress helper to close the overlay based on the settlement API response before interacting with payment controls.
- Whole-project Cypress failures in FR-09 and UC-04 were stale shopping quantity fixtures. Added explicit quantity units to preserve stricter validation and keep the full suite green.

---

# FR17-01 — Feature Scope and Existing Workflow Preservation

## Requirement

FR-17 must add a swipe-based approval interface for pending settlements while preserving existing shared expense tracking, payment status overview, deterministic settlement, and recorded payment behavior.

## Checklist

- [ ] Swipe approval interface is reachable from the settlement/payment area.
- [ ] Pending settlements are displayed clearly.
- [ ] User can approve a pending settlement.
- [ ] User can reject a pending settlement.
- [ ] User can use button fallback if swipe is not available.
- [ ] Existing shared expense creation still works.
- [ ] Existing expense split calculation still works.
- [ ] Existing payment status overview still works.
- [ ] Existing deterministic settlement calculation still works.
- [ ] Existing recorded payment flow still works.
- [ ] Existing settlement history still works.
- [ ] No real payment execution or banking integration was introduced.
- [ ] No unrelated task/shopping/dashboard/login workflows were changed.

## Test coverage evidence

- [ ] Existing expense tests still pass.
- [ ] Existing settlement tests still pass.
- [ ] Existing payment status tests still pass.
- [ ] Existing payment history tests still pass.
- [ ] No existing test was deleted or weakened.
- [ ] Any updated test still checks the same financial outcome.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Backend files reviewed: PaymentEndpoint.java; PaymentServiceImpl.java; ExpenseServiceImpl.java; SecurityConfig.java; DemoDataGenerator.java.
- Frontend files reviewed: payment-status.component.ts/html/scss; payment.service.ts.
- Existing settlement tests reviewed: PaymentServiceTest; PaymentEndpointTest; payment-status.component.spec.ts; payment.service.spec.ts; fr17-swipe-settlement-approval.cy.js.
- Existing tests preserved: full backend 397/397, frontend 404/404, Cypress 112/112.
- Remaining issues: no automated FR-17 blocker; real iPhone Safari/Firefox manual proof remains outside this form's automated evidence.
```

---

# FR17-02 — Backend Settlement Approval Correctness

## Requirement

The backend must process approval/rejection decisions consistently and must not corrupt deterministic settlement logic or balances.

## Checklist

- [ ] Pending settlement entity/DTO/status is clearly modeled or derived.
- [ ] Approving a pending settlement produces the intended backend state.
- [ ] Rejecting a pending settlement produces the intended backend state.
- [ ] Approved settlement is not shown as pending anymore.
- [ ] Rejected settlement is not shown as pending anymore or is shown with clear rejected status.
- [ ] Double approval is rejected or handled idempotently.
- [ ] Double rejection is rejected or handled idempotently.
- [ ] Approving after rejection is rejected or handled according to documented rules.
- [ ] Rejecting after approval is rejected or handled according to documented rules.
- [ ] Settlement amounts are not recalculated using frontend values.
- [ ] Backend uses existing deterministic settlement/payment logic.
- [ ] Financial amounts keep existing BigDecimal/rounding behavior.
- [ ] Deleted/anonymized users are handled consistently.
- [ ] Existing recorded payments remain understandable after approval/rejection.
- [ ] Settlement history/audit trail records who approved/rejected and when, where implemented.

## Backend test evidence

- [ ] Unit test for approving pending settlement.
- [ ] Unit test for rejecting pending settlement.
- [ ] Unit test for double approval.
- [ ] Unit test for double rejection.
- [ ] Unit test for approval after rejection.
- [ ] Unit test for rejection after approval.
- [ ] Unit test that frontend-supplied amount is not trusted.
- [ ] Unit test that BigDecimal/rounding behavior remains unchanged.
- [ ] Integration test for settlement approval endpoint.
- [ ] Integration test for settlement rejection endpoint.
- [ ] Integration test that settlement overview updates after approval/rejection.
- [ ] Regression test for deterministic settlement engine.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Settlement approval service reviewed: PaymentServiceImpl derives pending settlements server-side and records approval as Payment plus SettlementDecision.
- Settlement/payment repositories reviewed: PaymentRepository; SettlementDecisionRepository; RoommateRepository fallback behavior.
- DTO/status model reviewed: PendingSettlementDto and settlement decision status values.
- Unit tests: PaymentServiceTest settlement/payment/rounding cases.
- Integration tests: PaymentEndpointTest approval, rejection, double decision, wrong actor, no-token, anonymized counterparty, target-only decision tests.
- Remaining issues: none found in automated backend evidence.
```

---

# FR17-03 — Security, Authorization, and Household Scoping

## Requirement

Only authorized users in the same household may approve/reject pending settlements. The backend must derive user and household context from authentication and must not trust frontend IDs for authorization.

## Checklist

- [ ] Settlement approval endpoint requires authentication.
- [ ] Settlement rejection endpoint requires authentication.
- [ ] Unauthenticated requests are rejected.
- [ ] Current user is derived from Authentication/JWT.
- [ ] Current household is derived backend-side from active roommate membership.
- [ ] Frontend-supplied householdId is not accepted or not trusted.
- [ ] User cannot approve/reject settlement from another household.
- [ ] User cannot approve/reject settlement they are not allowed to act on.
- [ ] Authorization matches documented business rule.
- [ ] Main Tenant behavior is correct if Main Tenant has special permission.
- [ ] Roommate behavior is correct if only involved settlement parties may approve/reject.
- [ ] Other-household users do not appear in pending settlement data.
- [ ] Response does not expose unrelated financial records.
- [ ] Backend authorization is not replaced by frontend hiding.

## Security test evidence

- [ ] Unauthenticated approval/rejection test exists.
- [ ] Authenticated allowed-user approval test exists.
- [ ] Authenticated allowed-user rejection test exists.
- [ ] Cross-household approval/rejection is rejected.
- [ ] Unauthorized same-household user is rejected where applicable.
- [ ] Frontend householdId is ignored/rejected.
- [ ] Response contains only current-household settlements.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Endpoint reviewed: PaymentEndpoint settlement approval routes.
- Auth/current-user resolution reviewed: backend Authentication/JWT path through PaymentServiceImpl.
- Household scoping tests: PaymentEndpointTest and full Cypress creditor/other-user exclusions.
- Authorization tests: no-token endpoints return 403; only debtor can approve/reject; unrelated settlement state is not mutated.
- Remaining issues: none found in automated backend evidence.
```

---

# FR17-04 — API Contract and DTO Quality

## Requirement

FR-17 must expose stable API contracts for pending settlements, approval, and rejection.

## Checklist

- [ ] Endpoint path follows existing settlement/payment naming conventions.
- [ ] Pending settlement list endpoint exists or existing endpoint supports pending state.
- [ ] Approval endpoint exists.
- [ ] Rejection endpoint exists.
- [ ] DTO includes settlement/payment identifier.
- [ ] DTO includes payer/debtor and receiver/creditor display information according to existing style.
- [ ] DTO includes amount.
- [ ] DTO includes currency/scale where applicable.
- [ ] DTO includes current status.
- [ ] DTO includes timestamps where applicable.
- [ ] DTO does not expose unnecessary unrelated expense data.
- [ ] Invalid settlement ID returns clear error.
- [ ] Invalid state transition returns clear error.
- [ ] OpenAPI/Swagger documentation is updated if applicable.

## Suggested DTO evidence

```text
PendingSettlementDto:
- id:
- fromRoommate:
- toRoommate:
- amount:
- status:
- createdAt:
- approvedAt/rejectedAt:
```

```text
SettlementDecisionDto:
- decision:
- reason/comment if implemented:
```

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Endpoint paths: GET /api/v1/payments/settlement-approvals; POST /api/v1/payments/settlement-approvals/{settlementId}/approve; POST /api/v1/payments/settlement-approvals/{settlementId}/reject.
- DTO files: PendingSettlementDto; PaymentCreateDto; PaymentDto.
- API docs: /v3/api-docs and /v3/api-docs.yaml both return 200 and list the FR-17 paths.
- Validation behavior: invalid/double/forbidden state transitions return safe errors and do not create duplicate payments.
- Remaining issues: none found in automated contract evidence.
```

---

# FR17-05 — Frontend Swipe Interface and Fallback

## Requirement

The frontend must provide a usable swipe-based approval interface while also supporting a reliable fallback for non-touch/desktop users.

## Checklist

- [ ] Pending settlement cards are visible.
- [ ] Swipe right/approve works where implemented.
- [ ] Swipe left/reject works where implemented.
- [ ] Approve button fallback exists.
- [ ] Reject button fallback exists.
- [ ] Swipe actions have clear visual feedback.
- [ ] User can undo/cancel before final submission if implemented.
- [ ] Confirmation behavior is clear if implemented.
- [ ] Success message appears after approval/rejection.
- [ ] Error message appears if backend rejects the action.
- [ ] Loading/saving state is visible during action.
- [ ] Card is removed or status is updated after action.
- [ ] UI does not expose raw technical status values.
- [ ] UI style matches existing payment/settlement pages.
- [ ] Interface remains usable on desktop.
- [ ] Interface remains usable on mobile viewports.
- [ ] Keyboard/button fallback allows accessibility without swipe.
- [ ] No new UI library was introduced unless already approved.

## Frontend test evidence

- [ ] Component test renders pending settlements.
- [ ] Component test approves via button fallback.
- [ ] Component test rejects via button fallback.
- [ ] Component test handles backend error.
- [ ] Component test handles empty state.
- [ ] Component test verifies approved/rejected card update.
- [ ] Swipe interaction test exists if feasible.
- [ ] Angular service test verifies API calls.
- [ ] Existing payment/settlement frontend tests still pass.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Frontend components reviewed: payment-status.component.ts/html/scss.
- Swipe implementation reviewed: Cypress covers left/right swipe and button fallback.
- CSS/style changes: existing overlay/card styles render at desktop and mobile Cypress viewports.
- Component/service tests: payment-status.component.spec.ts and payment.service.spec.ts included in Angular 404/404 pass.
- Remaining issues: real iPhone Safari and Firefox desktop manual/browser proof not executed in this Linux workspace.
```

---

# FR17-06 — Test Account and Demo Data

## Requirement

FR-17 should be demonstrable with a dedicated test account or deterministic demo data.

## Checklist

- [ ] Dedicated FR-17 test account exists or is documented.
- [ ] Test account belongs to a household with pending settlements.
- [ ] Demo data includes at least one approvable pending settlement.
- [ ] Demo data includes at least one rejectable pending settlement.
- [ ] Demo data includes settlement amount with decimal places.
- [ ] Demo data includes more than one roommate.
- [ ] Demo data is idempotent.
- [ ] Demo data is active only in dev/test/demo profile.
- [ ] Demo credentials are documented.
- [ ] Expected settlement states before/after approval are documented.
- [ ] Demo data does not break existing tests.
- [ ] No real payment/banking data is used.

## Manual verification flow

- [ ] Start backend with demo/test data.
- [ ] Start frontend.
- [ ] Login with FR-17 test account.
- [ ] Open settlement approval page.
- [ ] Verify pending settlement appears.
- [ ] Approve one settlement.
- [ ] Verify status/overview updates.
- [ ] Reject one settlement.
- [ ] Verify status/overview updates.
- [ ] Verify settlement history remains understandable.

Manual demo data:

```text
Backend command:
mvn spring-boot:run -Dspring-boot.run.profiles=datagen

Login:
debtor.fr17@example.com / Password123!

Expected pending settlements on fresh datagen start:
- Swipe Debtor owes Utility Creditor: EUR 15.90
- Swipe Debtor owes Grocery Creditor: EUR 21.00
- Swipe Debtor owes Rent Creditor: EUR 32.00

Reset behavior:
Restarting the backend with the datagen profile recreates the FR-17 pending settlements after manual approve/reject testing.
```

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Seed/datagen file: DemoDataGenerator resets FR-17 settlement approval demo data on generateData startup.
- Demo profile: backend started with `mvn spring-boot:run -Dspring-boot.run.profiles=generateData`.
- Demo users: debtor.fr17@example.com / Password123! plus creditor accounts from seed.
- Expected settlement states: EUR 15.90, EUR 21.00, EUR 32.00 pending for debtor after seed reset.
- Manual verification result: automated Cypress login and workflow verified same seeded account.
- Remaining issues: no real manual tester sign-off captured.
```

---

# FR17-07 — E2E Coverage

## Requirement

At least one Cypress E2E test should verify the full user-facing swipe settlement approval workflow.

## Minimum E2E scenario

- [ ] Login as FR-17 test account.
- [ ] Navigate to settlement approval page.
- [ ] Verify pending settlement card appears.
- [ ] Approve settlement via button fallback or swipe.
- [ ] Verify success message.
- [ ] Verify approved settlement is no longer pending or has approved status.
- [ ] Verify payment/settlement overview updates.
- [ ] Reject another pending settlement.
- [ ] Verify rejected settlement state.
- [ ] Verify empty state when no pending settlements remain if feasible.
- [ ] Verify unauthenticated access is redirected/rejected.
- [ ] Verify mobile viewport smoke test.
- [ ] Use stable selectors such as `data-cy`.

## Optional E2E scenario

- [ ] Swipe gesture is tested using Cypress pointer/touch simulation if reliable.
- [ ] Desktop button fallback is tested separately.
- [ ] Cross-household manual API request is rejected.

## Command

```bash
cd e2e
npx cypress run --spec "cypress/e2e/fr17-swipe-settlement-approval.cy.js"
```

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- E2E spec: e2e/cypress/e2e/fr17-swipe-settlement-approval.cy.js.
- E2E command/result: `npx cypress run --spec "cypress/e2e/fr17-swipe-settlement-approval.cy.js"` passed 8/8 inside focused and full Cypress runs.
- Test data used: generateData FR-17 debtor/creditor seed.
- Pipeline result: local CI-equivalent full Cypress run passed 112/112; GitLab pipeline was not run from this workspace.
- Remaining issues: no external pipeline URL captured.
```

---

# FR17-08 — Regression Coverage

## Requirement

FR-17 must not break existing expense/payment/settlement functionality or wider WGMS workflows.

## Checklist

### Finance regression

- [ ] Expense creation still passes.
- [ ] Expense split calculation still passes.
- [ ] Payment status overview still passes.
- [ ] Deterministic settlement still passes.
- [ ] Payment recording still passes.
- [ ] Settlement history still passes.
- [ ] Deleted-user fallback in settlement logic still passes.
- [ ] Rounding edge cases still pass.

### Wider regression

- [ ] Login/registration tests still pass.
- [ ] WG creation/invitation tests still pass.
- [ ] Task/cleaning schedule tests still pass.
- [ ] Shopping tests still pass.
- [ ] Dashboard/history tests still pass.
- [ ] GDPR/profile/legal tests still pass where implemented.
- [ ] No failing test was skipped or deleted to make CI green.

## Required commands

```bash
cd backend
mvn test
```

```bash
cd frontend
npm test -- --watch=false
npm run build
```

```bash
cd e2e
npx cypress run
```

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Backend command/result: `mvn test` passed 397/397.
- Frontend command/result: `npm run lint`, `npm run build`, and `npx ng test --watch=false --browsers=ChromeHeadless` passed; Angular 404/404.
- E2E command/result: focused UC-03/FR-17 Cypress passed 33/33; full Cypress passed 112/112.
- Pipeline result: local CI-equivalent commands passed; remote pipeline not executed.
- Existing tests changed: FR-06 helper made overlay close deterministic; FR-09/UC-04 fixtures given explicit units to satisfy current validation.
- Existing tests deleted/replaced: none.
- Remaining issues: none in automated regression suite; remote CI proof still external.
```

---

# FR17-09 — Non-Functional Requirements Summary

| NFR | FR-17-specific check | Status | Evidence |
|---|---|---|---|
| NFR-01 Security & Access Control | Approval/rejection endpoints require login; household and actor permissions are enforced backend-side. | [x] Pass [ ] Partial [ ] Fail | PaymentEndpointTest no-token/wrong-actor tests; Cypress creditor exclusion. |
| NFR-02 Data Privacy & GDPR | Settlement approval UI/API exposes only current-household financial data needed for the workflow. | [x] Pass [ ] Partial [ ] Fail | BUG #42 anonymized counterparty tests; GDPR history full-suite pass. |
| NFR-03 Reliability | Settlement decisions are persisted consistently and do not corrupt deterministic balances or payment history. | [x] Pass [ ] Partial [ ] Fail | PaymentEndpointTest double-decision/target-only tests; full Cypress 112/112. |
| NFR-04 Usability & Compatibility | Swipe UI has button fallback and works on desktop/mobile reference viewports. | [ ] Pass [x] Partial [ ] Fail | Cypress desktop/mobile viewport and button fallback pass; real iPhone Safari/Firefox manual proof not run. |
| NFR-05 Maintainability | Settlement decision logic is backend service logic; frontend only triggers approval/rejection and renders DTOs. | [x] Pass [ ] Partial [ ] Fail | PaymentServiceImpl service logic; payment.service.ts API calls; Angular/backend tests. |
| NFR-06 Auditability | Approval/rejection actions are traceable by responsible user and timestamp where supported. | [x] Pass [ ] Partial [ ] Fail | SettlementDecision persistence verified by PaymentEndpointTest. |

## Status

- [ ] Passed
- [x] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Security/privacy checks: backend no-token/wrong-actor tests and Cypress creditor exclusion pass.
- Reliability checks: full backend and Cypress suites pass; double-decision regressions covered.
- Usability/accessibility checks: Cypress mobile viewport and accessible button fallback pass; real device/browser and external accessibility audit not run.
- Maintainability checks: service/component split covered by tests and reviewed implementation files.
- Auditability checks: decision persistence stores actor/status/timestamp where supported.
```

---

# Final FR-17 QA Summary

| Area | Status | Notes |
|---|---|---|
| Feature scope preserved | [x] Pass [ ] Partial [ ] Fail | Full Cypress 112/112 and focused UC-03/FR-17 33/33. |
| Backend settlement decision logic correct | [x] Pass [ ] Partial [ ] Fail | PaymentServiceTest/PaymentEndpointTest pass. |
| Household scoping/security correct | [x] Pass [ ] Partial [ ] Fail | No-token/wrong-actor/creditor exclusion tests pass. |
| API/DTO contract clear | [x] Pass [ ] Partial [ ] Fail | JSON/YAML OpenAPI probes pass and list FR-17 routes. |
| Swipe/button frontend works | [x] Pass [ ] Partial [ ] Fail | Cypress swipe and button fallback tests pass. |
| Test account/demo data sufficient | [x] Pass [ ] Partial [ ] Fail | generateData seed reset and Cypress account flow pass. |
| Backend tests sufficient | [x] Pass [ ] Partial [ ] Fail | Full backend 397/397. |
| Frontend tests sufficient | [x] Pass [ ] Partial [ ] Fail | Angular 404/404, lint/build pass. |
| E2E coverage sufficient | [x] Pass [ ] Partial [ ] Fail | FR-17 8/8; full Cypress 112/112. |
| Existing tests still pass | [x] Pass [ ] Partial [ ] Fail | Full backend/frontend/Cypress gates pass. |
| NFRs satisfied | [ ] Pass [x] Partial [ ] Fail | Automated NFRs pass; real device/browser/manual accessibility proof still external. |

## Overall result

- [ ] FR-17 QA passed
- [x] FR-17 QA partially passed with documented limitations
- [ ] FR-17 QA failed / blocking issues remain

## Remaining limitations

```text
- Automated FR-17/backend/frontend/E2E evidence is green on the merged branch.
- Real iPhone 12 Pro Safari, Firefox desktop, and external manual accessibility/peer-test evidence were not executed in this Linux workspace.
- Remote GitLab pipeline proof was not produced by this local QA run.
```

## Evidence links

```text
Backend tests:
- backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/PaymentServiceTest.java
- backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/PaymentEndpointTest.java
- backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/SecurityTest.java

Frontend tests:
- frontend/src/app/services/payment.service.spec.ts
- frontend/src/app/components/payment-status/payment-status.component.spec.ts

E2E tests:
- e2e/cypress/e2e/fr17-swipe-settlement-approval.cy.js
- Full suite: npx cypress run, 112/112 passing

Manual verification:
- Not separately performed beyond local automated browser runs.

Seed/demo data:
- DemoDataGenerator FR-17 seed: debtor.fr17@example.com / Password123!, pending EUR 15.90, EUR 21.00, EUR 32.00

GitLab issue / merge request:
- Not available from local workspace.
```

## QA sign-off

```text
Reviewer: Codex
Date: 2026-06-25
Branch / commit: UC-03/QA / 00b867c dirty working tree
Result: Automated FR-17 QA passed; final status partial only because real-device/manual/pipeline evidence is external.
```
