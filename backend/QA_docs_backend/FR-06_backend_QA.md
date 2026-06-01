# FR-06 Backend QA Checklist - Payment Status Overview

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
- Merge request:
- Developer:
- QA reviewer:
- Date: 2026-06-01

## How to Use This Checklist

Use this checklist before merging the backend implementation. A checked item must be backed by code inspection, backend unit tests, backend integration tests, command output, or documented manual evidence. If an item does not apply, mark it `N/A` and explain why.

## 1. Critical Checks - API Contract

- [x] Backend endpoint(s) for Payment Status Overview exist.
- [x] Endpoint naming follows project conventions.
- [x] Endpoint uses the correct HTTP method.
- [x] Endpoint is authenticated.
- [x] Endpoint does not require the frontend to send `householdId`.
- [x] Endpoint derives the household from the authenticated user.
- [x] Endpoint returns a stable DTO shape.
- [x] DTO fields are sufficient for the frontend.
- [x] DTOs do not expose raw JPA entities.
- [x] DTOs do not expose passwords, tokens, invitation secrets, or unrelated user data.
- [x] Empty result returns an empty array/object as appropriate, not `null` or an error.
- [x] Error cases use the project-standard exception handling.

Evidence:

```text
Endpoint:
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/PaymentEndpoint.java

Service:
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/PaymentService.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/PaymentServiceImpl.java

DTO:
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/PaymentCreateDto.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/PaymentDto.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/PaymentOverviewDto.java

Integration test:
backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/PaymentEndpointTest.java
```

## 2. Critical Checks - Authentication and Authorization

- [x] Request without token is rejected.
- [x] Request with valid authenticated household user succeeds.
- [x] Backend uses the authenticated principal, not client-provided user data.
- [x] Backend resolves the active roommate membership.
- [x] Backend scopes all data to the active roommate's household.
- [x] User from household A cannot see household B data.
- [x] User without active household membership is rejected.
- [x] Authorization is enforced in backend service/repository logic.
- [x] Frontend cannot override household scope through parameters.

Evidence:

```text
Security annotation:
PaymentEndpoint uses @PreAuthorize("hasRole('USER')") on GET /overview and POST /payments.

Membership lookup:
PaymentServiceImpl#getCurrentRoommateByEmail uses the authenticated email and RoommateRepository#findByUserIdAndActiveTrue.

Household isolation test:
PaymentEndpointTest#givenOtherHouseholdData_whenGetOverview_thenDoesNotLeak
PaymentEndpointTest#givenUserWithoutActiveHousehold_whenGetOverview_then403
PaymentEndpointTest#givenNoToken_whenGetOverview_then403
PaymentServiceTest#givenPaymentToRoommateInOtherHousehold_whenCreatePayment_thenNotFoundAndNoSave
```

## 3. Critical Checks - Feature Correctness

- [x] Backend implements the accepted FR-06 scope: payment/balance overview based on household expenses, splits, payments or settlements.
- [x] Correct source entities/repositories are used.
- [x] Data from the current household is included.
- [x] Data from other households is excluded.
- [x] Empty household data is handled.
- [x] Multiple roommates are handled.
- [x] Multiple records are handled.
- [x] Duplicate or repeated records are handled according to the feature rules.
- [x] Sorting is deterministic and documented.
- [x] Optional/null fields are handled safely.
- [x] No fake data is generated in production code.
- [x] Existing related feature behavior is not changed unexpectedly.

Evidence:

```text
Unit test:
backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/PaymentServiceTest.java

Integration test:
backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/PaymentEndpointTest.java

Related FR-05 consistency regression:
backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/ExpenseEndpointTest.java
  - givenRecordedPayment_whenCalculateBalances_thenOpenBalanceIsReduced

Manual/code inspection:
PaymentServiceImpl calculates open balances from expense splits and recorded payments via BalanceCalculator.
Payment history uses repository ordering by date DESC, createdAt DESC, id DESC.
PaymentCreateDto and PaymentServiceImpl reject null, invalid amount, and future date input.
```

## 4. Important Checks - Repository and Query Design

- [x] Repository queries are household-scoped.
- [x] Backend does not fetch all households globally and filter in memory.
- [x] Query filters by relevant status/visibility where applicable. N/A: payments have no status/visibility field.
- [x] Query sorting is deterministic.
- [x] Query avoids obvious N+1 loading issues for payment history DTO mapping.
- [x] `@EntityGraph` or explicit fetch is used when mapped relations are needed.
- [x] Repository method names are readable and maintainable.
- [x] Query handles demo-scale data.
- [x] Cleanup order in integration tests respects foreign keys.

Evidence:

```text
Repository:
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/repository/PaymentRepository.java

EntityGraph/query:
PaymentRepository#findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc
PaymentRepository#findAllInvolvingRoommate

N+1/performance evidence:
PaymentRepository uses @EntityGraph for household, fromRoommate.user, toRoommate.user, and createdBy.user.
PaymentRepositoryTest verifies that accessing mapped payment history fields after repository loading does not issue additional SQL statements.
```

## 5. Critical Checks - Backend Unit Tests Exist

- [x] Unit tests exist for the main service/calculation/aggregation logic.
- [x] Unit tests cover successful data retrieval.
- [x] Unit tests cover empty data.
- [x] Unit tests cover multiple roommates.
- [x] Unit tests cover multiple records.
- [x] Unit tests cover household-scoped repository calls.
- [x] Unit tests cover user without active household.
- [x] Unit tests cover null/optional field handling.
- [x] Unit tests cover important edge cases for this feature.
- [x] No relevant unit tests are skipped or weakened.

Command evidence:

```bash
cd backend && mvn -B clean install
```

Result:

```text
BUILD SUCCESS
Tests run: 189, Failures: 0, Errors: 0, Skipped: 0
```

Specific unit test evidence:

```text
PaymentServiceTest:
- givenEmptyHousehold_whenGetOverview_thenReturnsEmptyOverview
- givenOneExpenseWithoutPayments_whenGetOverview_thenReturnsExpectedOpenBalance
- givenPartialPayment_whenGetOverview_thenReducesOpenBalance
- givenFullPayment_whenGetOverview_thenOpenBalanceIsRemoved
- givenRoommate_whenGetOverview_thenOnlyOwnBalancesAndPaymentsAreReturned
- givenValidPayment_whenCreatePayment_thenPaymentIsSaved
- givenUserWithoutActiveHousehold_whenGetOverview_thenForbidden
- givenPaymentToRoommateInOtherHousehold_whenCreatePayment_thenNotFoundAndNoSave
- givenInvalidAmount_whenCreatePayment_thenConflictAndNoSave
- givenFutureDate_whenCreatePayment_thenConflictAndNoSave

PaymentRepositoryTest:
- findAllByHousehold_fetchesRoommateUsersWithoutAdditionalQueries
- findAllInvolvingRoommate_fetchesRoommateUsersWithoutAdditionalQueries
```

## 6. Critical Checks - Backend Integration Tests Exist

- [x] Integration test exists for the main endpoint.
- [x] Integration test rejects unauthenticated request.
- [x] Integration test accepts authenticated household user.
- [x] Integration test asserts JSON response shape.
- [x] Integration test creates at least two households.
- [x] Integration test proves no cross-household leakage.
- [x] Integration test covers empty result.
- [x] Integration test covers populated result.
- [x] Integration test runs as part of `mvn test`.
- [x] Integration tests use synthetic data only.

Evidence:

```text
Integration test class:
backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/PaymentEndpointTest.java

Covered cases:
- givenNoToken_whenGetOverview_then403
- givenUserWithoutActiveHousehold_whenGetOverview_then403
- givenEmptyHousehold_whenGetOverview_thenStableEmptyShape
- givenPopulatedHousehold_whenGetOverview_thenReturnsBalancesAndPaymentHistory
- givenOtherHouseholdData_whenGetOverview_thenDoesNotLeak
- givenValidPayment_whenCreatePayment_then201AndPaymentIsPersisted
- givenFutureDate_whenCreatePayment_then400AndNoPaymentIsCreated

Command output:
cd backend && mvn -B clean install
BUILD SUCCESS, Tests run: 189, Failures: 0, Errors: 0, Skipped: 0
```

## 7. Critical Checks - E2E Evidence Exists

- [x] E2E test exists for the backend-visible user flow.
- [x] E2E uses existing frontend/API flow to create or seed relevant data.
- [x] E2E proves authenticated user can reach the feature.
- [x] E2E proves real backend response is displayed/used.
- [x] E2E proves no crash on empty data or documents why not covered.
- [x] E2E does not use production data.
- [x] E2E spec path is documented.

Command evidence:

```bash
cd e2e && npx cypress run
```

Result:

```text
All specs passed.
Tests: 32
Passing: 32
Failing: 0
```

FR-06 spec:

```text
e2e/cypress/e2e/paymentStatus.cy.js
- records a partial payment and keeps FR-05 balances in sync
- shows empty payment states for a household without balances
```

## 8. Regression Checks

- [x] Existing auth tests still pass.
- [x] Existing household tests still pass.
- [x] Existing related feature tests still pass.
- [x] Full backend test suite passes.
- [x] No unrelated backend business logic was changed for this FR-06 backend fix.
- [x] No existing tests were deleted to make this feature pass.
- [ ] No secrets or local files were committed. Not verifiable before commit; current working tree still contains untracked local files such as `.idea/` and patch helper files which must not be staged.

## 9. Backend Merge Blockers

- [x] Verified blocker not present: backend endpoint is missing.
- [x] Verified blocker not present: endpoint is unauthenticated.
- [x] Verified blocker not present: endpoint trusts frontend household id.
- [x] Verified blocker not present: cross-household data leaks.
- [x] Verified blocker not present: empty data crashes endpoint.
- [x] Verified blocker not present: DTO exposes raw entities or sensitive fields.
- [x] Verified blocker not present: backend unit tests are missing.
- [x] Verified blocker not present: backend integration tests are missing.
- [x] Verified blocker not present: e2e evidence is missing without justification.
- [x] Verified blocker not present: backend test suite fails.

## 10. Backend Final Approval

- [x] Backend implementation complete.
- [x] Backend unit tests exist and pass.
- [x] Backend integration tests exist and pass.
- [x] E2E evidence exists.
- [x] Household isolation verified.
- [x] Regression tests pass.
- [x] Known limitations documented.
- [ ] Backend ready for merge review. Pending final commit hygiene: do not stage local IDE files or patch helper files.

## Known Limitations / Follow-Up

- Payments can be created but cannot yet be edited, deleted, or undone.
- Payment history queries now use `@EntityGraph` and have repository-level N+1 regression coverage.
