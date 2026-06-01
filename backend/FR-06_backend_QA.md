# FR-06 Backend QA Evidence - Payment Status Overview

## Feature Information

- Feature ID: FR-06
- Feature name: Payment Status Overview
- Use case: UC-03 - Shared Expense Tracking and Settlement
- Priority: High
- Category: Core
- Version: V3
- Backend technology: Java Spring Boot
- Security: Spring Security / JWT
- Database: H2 / Spring Data JPA
- Dependency: FR-05 Shared expense tracking
- Branch: FR-06/Payment-status
- Date: 2026-06-01

## Implementation Evidence

Endpoints:

```text
GET  /api/v1/payments/overview
POST /api/v1/payments
```

Backend files:

```text
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/PaymentEndpoint.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/PaymentService.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/PaymentServiceImpl.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/BalanceCalculator.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/entity/Payment.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/repository/HouseholdRepository.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/repository/PaymentRepository.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/PaymentCreateDto.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/PaymentDto.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/PaymentOverviewDto.java
```

Backend tests:

```text
backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/PaymentServiceTest.java
backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/PaymentRepositoryTest.java
backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/PaymentEndpointTest.java
backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/ExpenseEndpointTest.java
```

## API Contract

- [x] Backend endpoint(s) for Payment Status Overview exist.
- [x] Endpoint naming follows project conventions.
- [x] Endpoints use the planned HTTP methods.
- [x] Endpoints are authenticated with `@PreAuthorize("hasRole('USER')")`.
- [x] Endpoints do not require the frontend to send `householdId`.
- [x] Endpoints derive the household from `Authentication.getName()`.
- [x] Endpoint responses use stable DTOs.
- [x] DTO fields are sufficient for the frontend overview.
- [x] DTOs do not expose raw JPA entities.
- [x] DTOs do not expose passwords, tokens, invitation secrets, or unrelated user data.
- [x] Empty result is represented as empty lists and zero totals.
- [x] Error cases use project-standard exception handling.

## Authentication And Authorization

- [x] Request without token is rejected.
- [x] Request with valid authenticated household user succeeds.
- [x] Backend uses authenticated principal, not client-provided user data.
- [x] Backend resolves active roommate membership through `RoommateRepository`.
- [x] Backend scopes data to the active roommate's household.
- [x] Cross-household payment recipient is rejected.
- [x] Household A payment data does not leak into Household B overview.
- [x] User without active household membership is rejected.
- [x] Frontend cannot override household scope through parameters.

## Feature Correctness

- [x] Backend implements payment/balance overview based on household expenses, splits, and payments.
- [x] FR-05 and FR-06 use shared settlement logic through `BalanceCalculator`.
- [x] Current household data is included.
- [x] Other household data is excluded.
- [x] Empty household data is handled.
- [x] Multiple roommates and records are handled.
- [x] Payment history sorting is deterministic by date, createdAt, and id descending.
- [x] Payment amount must be positive and cannot exceed the open balance.
- [x] Payment creation serializes settlement writes per household with a pessimistic household lock.
- [x] Expense edit/delete is blocked once household payments exist, preventing settlement invalidation.
- [x] Future-dated payments are rejected by `PaymentCreateDto` and `PaymentServiceImpl`.
- [x] No fake production data is generated in production code.

## Repository And Query Design

- [x] Repository queries are household-scoped.
- [x] Backend does not accept frontend household scope.
- [x] Query sorting is deterministic.
- [x] Repository method names are readable.
- [x] Explicit payment query is used for payments involving a specific roommate.
- [x] Integration test cleanup order respects foreign keys.
- [x] Payment history queries use `@EntityGraph` for mapped roommate/user relations.
- [x] Repository tests verify payment history relation access does not trigger additional SQL statements.

## Unit Test Coverage

`PaymentServiceTest` covers:

- [x] Empty household data returns empty overview.
- [x] One expense without payments creates expected open balance.
- [x] Partial payment reduces open balance.
- [x] Full payment removes open balance.
- [x] Roommate sees own balances/payments.
- [x] Valid payment is saved.
- [x] User without active household is rejected.
- [x] Payment to roommate in another household is rejected.
- [x] Invalid amount is rejected.
- [x] Future-dated payment is rejected and not saved.

`PaymentRepositoryTest` covers:

- [x] Household payment history fetches roommate/user relations without additional SQL on access.
- [x] Roommate-specific payment history fetches roommate/user relations without additional SQL on access.

## Integration Test Coverage

`PaymentEndpointTest` covers:

- [x] Unauthenticated request rejection.
- [x] User without household rejection.
- [x] Empty overview response shape.
- [x] Populated overview with balances and payment history.
- [x] Cross-household isolation.
- [x] Valid payment creation and persistence.
- [x] Future-dated payment rejected with HTTP 400.
- [x] Concurrent payment creation for the same open balance persists only one payment.

`ExpenseEndpointTest` also covers:

- [x] Recorded payments reduce `/expenses/balances`, keeping FR-05 aligned with FR-06.
- [x] Recorded payments block expense update/delete with HTTP 409.

## Command Evidence

Targeted backend tests:

```bash
cd backend && mvn -B "-Dtest=PaymentEndpointTest,PaymentServiceTest,ExpenseEndpointTest" test
```

Result:

```text
BUILD SUCCESS
Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
```

Full backend suite:

```bash
cd backend && mvn -B test
```

Result:

```text
BUILD SUCCESS
Tests run: 192, Failures: 0, Errors: 0, Skipped: 0
```

## Backend Merge Blockers

- [x] Backend endpoint is present.
- [x] Endpoint is authenticated.
- [x] Endpoint does not trust frontend household id.
- [x] Cross-household leakage is tested.
- [x] Empty data does not crash endpoint.
- [x] DTOs do not expose raw entities or sensitive fields.
- [x] Backend unit tests exist and pass.
- [x] Backend integration tests exist and pass.
- [x] Backend full test suite passes.

## Known Limitation

- Payments currently cannot be edited, deleted, or undone. This should be handled in a follow-up if required before final product acceptance.

## Backend QA Conclusion

Backend FR-06 is QA-approved for merge review with the documented payment edit/delete/undo limitation and the explicit expense-change guard after settlements.
