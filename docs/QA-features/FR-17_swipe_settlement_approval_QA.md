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

Result: 2 passing, 0 failing.

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

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Backend files reviewed:
- Frontend files reviewed:
- Existing settlement tests reviewed:
- Existing tests preserved:
- Remaining issues:
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

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Settlement approval service reviewed:
- Settlement/payment repositories reviewed:
- DTO/status model reviewed:
- Unit tests:
- Integration tests:
- Remaining issues:
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

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Endpoint reviewed:
- Auth/current-user resolution reviewed:
- Household scoping tests:
- Authorization tests:
- Remaining issues:
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

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Endpoint paths:
- DTO files:
- API docs:
- Validation behavior:
- Remaining issues:
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

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Frontend components reviewed:
- Swipe implementation reviewed:
- CSS/style changes:
- Component/service tests:
- Remaining issues:
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

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Seed/datagen file:
- Demo profile:
- Demo users:
- Expected settlement states:
- Manual verification result:
- Remaining issues:
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

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- E2E spec:
- E2E command/result:
- Test data used:
- Pipeline result:
- Remaining issues:
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

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Backend command/result:
- Frontend command/result:
- E2E command/result:
- Pipeline result:
- Existing tests changed:
- Existing tests deleted/replaced:
- Remaining issues:
```

---

# FR17-09 — Non-Functional Requirements Summary

| NFR | FR-17-specific check | Status | Evidence |
|---|---|---|---|
| NFR-01 Security & Access Control | Approval/rejection endpoints require login; household and actor permissions are enforced backend-side. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-02 Data Privacy & GDPR | Settlement approval UI/API exposes only current-household financial data needed for the workflow. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-03 Reliability | Settlement decisions are persisted consistently and do not corrupt deterministic balances or payment history. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-04 Usability & Compatibility | Swipe UI has button fallback and works on desktop/mobile reference viewports. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-05 Maintainability | Settlement decision logic is backend service logic; frontend only triggers approval/rejection and renders DTOs. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-06 Auditability | Approval/rejection actions are traceable by responsible user and timestamp where supported. | [ ] Pass [ ] Partial [ ] Fail |  |

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Security/privacy checks:
- Reliability checks:
- Usability/accessibility checks:
- Maintainability checks:
- Auditability checks:
```

---

# Final FR-17 QA Summary

| Area | Status | Notes |
|---|---|---|
| Feature scope preserved | [ ] Pass [ ] Partial [ ] Fail |  |
| Backend settlement decision logic correct | [ ] Pass [ ] Partial [ ] Fail |  |
| Household scoping/security correct | [ ] Pass [ ] Partial [ ] Fail |  |
| API/DTO contract clear | [ ] Pass [ ] Partial [ ] Fail |  |
| Swipe/button frontend works | [ ] Pass [ ] Partial [ ] Fail |  |
| Test account/demo data sufficient | [ ] Pass [ ] Partial [ ] Fail |  |
| Backend tests sufficient | [ ] Pass [ ] Partial [ ] Fail |  |
| Frontend tests sufficient | [ ] Pass [ ] Partial [ ] Fail |  |
| E2E coverage sufficient | [ ] Pass [ ] Partial [ ] Fail |  |
| Existing tests still pass | [ ] Pass [ ] Partial [ ] Fail |  |
| NFRs satisfied | [ ] Pass [ ] Partial [ ] Fail |  |

## Overall result

- [ ] FR-17 QA passed
- [ ] FR-17 QA partially passed with documented limitations
- [ ] FR-17 QA failed / blocking issues remain

## Remaining limitations

```text
- 
```

## Evidence links

```text
Backend tests:
- 

Frontend tests:
- 

E2E tests:
- 

Manual verification:
- 

Seed/demo data:
- 

GitLab issue / merge request:
- 
```

## QA sign-off

```text
Reviewer:
Date:
Branch / commit:
Result:
```
