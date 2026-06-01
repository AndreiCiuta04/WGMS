# FR-06 General QA Evidence - Payment Status Overview

## Feature Information

- Feature ID: FR-06
- Feature name: Payment Status Overview
- Use case: UC-03 - Shared Expense Tracking and Settlement
- Priority: High
- Category: Core
- Version: V3
- Dependency: FR-05 Shared expense tracking
- Branch: FR-06/Payment-status
- Date: 2026-06-01

## Scope Verified

- [x] FR-06 implements the payment status overview for household settlements.
- [x] The feature uses the existing authentication and household membership model.
- [x] Backend derives household scope from the authenticated user.
- [x] Frontend sends no `householdId` for payment overview or payment creation.
- [x] Cross-household payment and overview data is not exposed.
- [x] Empty, loading, populated, and error states are covered.
- [x] FR-05 expense balances and FR-06 payment balances are now consistent after recorded payments.
- [x] Future-dated payments are rejected.
- [x] Concurrent settlement requests cannot over-settle a balance.
- [x] Expense edit/delete is blocked after recorded household payments until refund/undo semantics are implemented.
- [x] Known limitation remains documented: recorded payments cannot yet be edited, deleted, or undone.

## Implementation Evidence

Backend endpoints:

```text
GET  /api/v1/payments/overview
POST /api/v1/payments
```

Backend implementation:

```text
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/PaymentEndpoint.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/PaymentServiceImpl.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/BalanceCalculator.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/PaymentCreateDto.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/PaymentDto.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/PaymentOverviewDto.java
```

Frontend implementation:

```text
frontend/src/app/components/payment-status/payment-status.component.ts
frontend/src/app/components/payment-status/payment-status.component.html
frontend/src/app/services/payment.service.ts
frontend/src/app/dtos/payment.ts
frontend/src/app/dtos/expense.ts
```

Tests:

```text
backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/PaymentServiceTest.java
backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/PaymentEndpointTest.java
backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/ExpenseEndpointTest.java
frontend/src/app/components/payment-status/payment-status.component.spec.ts
frontend/src/app/services/payment.service.spec.ts
e2e/cypress/e2e/paymentStatus.cy.js
```

## Acceptance Criteria

- [x] User can log in.
- [x] User can navigate to Payment Status.
- [x] User can see open balances for their own household.
- [x] User can record a payment against an open balance.
- [x] Payment history shows recorded settlements.
- [x] Empty household financial data displays stable empty states.
- [x] Backend loading failures display an error state instead of a false empty state.
- [x] Household scope is enforced in backend, not by frontend parameters.
- [x] Cross-household data leakage is covered by backend integration tests.
- [x] E2E covers populated and empty states with synthetic data.

## Security And Privacy

- [x] Payment endpoints require authenticated user role.
- [x] Backend resolves active roommate membership from the authenticated principal.
- [x] Overview data is scoped to the active household.
- [x] Payment recipient must be an active roommate in the same household.
- [x] Response DTOs expose only settlement data needed by the UI.
- [x] No passwords, JWTs, invitation secrets, raw JPA entities, or unrelated household data are returned.
- [x] Tests use synthetic users, households, expenses, and payments.

Note: main tenants can see all intra-household balances. This is treated as an intended household oversight behavior for the current implementation.

## Known Limitation

- Payments can be created but not edited, deleted, or undone yet. A follow-up should add creator-scoped delete/undo behavior if required by product scope.

## Command Evidence

Backend full suite:

```bash
cd backend && mvn -B test
```

Result:

```text
BUILD SUCCESS
Tests run: 192, Failures: 0, Errors: 0, Skipped: 0
```

Frontend lint:

```bash
cd frontend && npm.cmd run lint
```

Result:

```text
All files pass linting.
```

Frontend build:

```bash
cd frontend && npm.cmd run build
```

Result:

```text
Application bundle generation complete.
```

Frontend unit/component tests:

```bash
cd frontend && npm.cmd test -- --watch=false --browsers=ChromeHeadless
```

Result:

```text
TOTAL: 71 SUCCESS
```

FR-06 Cypress spec:

```bash
cd e2e && npx.cmd cypress run --spec cypress/e2e/paymentStatus.cy.js
```

Result:

```text
2 passing, 0 failing
```

## Final QA Status

- [x] Backend QA completed.
- [x] Frontend QA completed.
- [x] General QA completed.
- [x] Backend unit and integration tests exist and pass.
- [x] Frontend service/component tests exist and pass.
- [x] FR-06 E2E spec exists and passes.
- [x] Household isolation verified.
- [x] FR-05/FR-06 balance consistency verified.
- [x] Known limitation documented.
- [x] Ready for merge review after committing the prepared changes.
