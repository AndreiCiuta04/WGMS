# FR-06 QA Summary - Payment Status Overview

Sources:

- `docs/FR-06_general_QA.md`
- `backend/FR-06_backend_QA.md`
- `backend/QA_docs_backend/FR-06_backend_QA.md`
- `frontend/FR-06_frontend_QA.md`
- `frontend/QA_docs_frontend/FR-06_frontend_QA.md`

## Current Status

FR-06 is implemented and QA evidence has been updated after the review fixes.

The feature provides a household-scoped payment status overview and allows a roommate to record payments toward open balances. FR-05 expense balances and FR-06 payment balances now use shared settlement logic, so both pages show the same remaining balance after a payment is recorded.

## Review Items Addressed

- [x] FR-05 and FR-06 balance divergence fixed through shared balance calculation.
- [x] Future-dated payments rejected through DTO validation and service validation.
- [x] Payment overview failures show an error instead of a false empty state.
- [x] Redundant frontend household/member round-trips removed from Payment Status.
- [x] Backend endpoint integration test added.
- [x] FR-06 Cypress spec added.
- [x] Concurrent payment creation is serialized per household to prevent over-settlement.
- [x] Expense edit/delete is blocked after recorded household payments to avoid invalidating settlements.
- [x] QA evidence updated from stale templates to actual implementation/test results.

## Remaining Known Limitation

- Recorded payments cannot yet be edited, deleted, or undone. This is documented as a known limitation and should be handled in a follow-up if required before final product acceptance.

## Implementation Evidence

Backend:

```text
GET  /api/v1/payments/overview
POST /api/v1/payments
PaymentEndpoint
PaymentServiceImpl
BalanceCalculator
PaymentCreateDto
PaymentDto
PaymentOverviewDto
```

Frontend:

```text
/payment-status
PaymentStatusComponent
PaymentService
PaymentOverviewDto
BalanceDto
```

Tests:

```text
PaymentServiceTest
PaymentEndpointTest
ExpenseEndpointTest
PaymentStatusComponent spec
PaymentService spec
e2e/cypress/e2e/fr06-payment-status.cy.js
```

## Test Evidence

Backend:

```bash
cd backend && mvn -B test
```

```text
BUILD SUCCESS
Tests run: 192, Failures: 0, Errors: 0, Skipped: 0
```

Frontend lint/build/tests:

```bash
cd frontend && npm.cmd run lint
cd frontend && npm.cmd run build
cd frontend && npm.cmd test -- --watch=false --browsers=ChromeHeadless
```

```text
Lint: All files pass linting.
Build: Application bundle generation complete.
Tests: TOTAL: 71 SUCCESS
```

FR-06 E2E:

```bash
cd e2e && npx.cmd cypress run --spec cypress/e2e/fr06-payment-status.cy.js
```

```text
3 passing, 0 failing
```

FR-06 plus UC-05 use-case E2E:

```bash
cd e2e && npx.cmd cypress run --spec cypress/e2e/fr06-payment-status.cy.js,cypress/e2e/usecases/uc05-expense-history.cy.js
```

```text
5 passing, 0 failing
```

## Merge Readiness

FR-06 has backend, frontend, and E2E evidence for the core happy path, empty state, validation, household scoping, cross-household isolation, and FR-05/FR-06 consistency. It is ready for merge review once the prepared code and QA documentation changes are committed.
