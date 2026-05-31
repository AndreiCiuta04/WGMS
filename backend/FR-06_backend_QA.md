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
- Developer:
- QA reviewer:
- Date: 2026-05-25

## Implementation Evidence

Endpoint:

```text
GET  /api/v1/payments/overview
POST /api/v1/payments
```

Backend files:

```text
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/PaymentEndpoint.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/PaymentService.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/PaymentServiceImpl.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/entity/Payment.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/repository/PaymentRepository.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/PaymentCreateDto.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/PaymentDto.java
backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/PaymentOverviewDto.java
```

Unit test file:

```text
backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/PaymentServiceTest.java
```

## Backend QA Status

### API Contract

- [x] Backend endpoint(s) for Payment Status Overview exist.
- [x] Endpoint naming follows project conventions.
- [x] Endpoint uses the planned HTTP methods.
- [x] Endpoint is authenticated with `@PreAuthorize("hasRole('USER')")`.
- [x] Endpoint does not require the frontend to send `householdId`.
- [x] Endpoint derives the household from the authenticated user via `Authentication.getName()`.
- [x] Endpoint returns stable DTOs.
- [x] DTO fields are sufficient for the planned frontend overview.
- [x] DTOs do not expose raw JPA entities.
- [x] DTOs do not expose passwords, tokens, invitation secrets, or unrelated user data.
- [x] Empty result is represented as empty lists and zero totals.
- [x] Error cases use existing project exceptions such as `ForbiddenException`, `NotFoundException`, and `ConflictException`.

### Authentication And Authorization

- [ ] Request without token is rejected. Not yet covered by integration test.
- [x] Request with valid authenticated household user is supported by service logic.
- [x] Backend uses the authenticated principal, not client-provided user data.
- [x] Backend resolves the active roommate membership through `RoommateRepository`.
- [x] Backend scopes data to the active roommate's household.
- [x] Cross-household payment recipient is rejected by service logic.
- [x] User without active household membership is rejected.
- [x] Authorization is enforced in endpoint and service logic.
- [x] Frontend cannot override household scope through parameters.

### Feature Correctness

- [x] Backend implements payment/balance overview based on household expenses, splits, and payments.
- [x] Correct source entities/repositories are used.
- [x] Current household data is included.
- [x] Other household data is excluded by repository/service scoping.
- [x] Empty household data is handled.
- [x] Multiple roommates are covered by unit test scenarios.
- [x] Multiple records are covered by unit test scenarios.
- [x] Sorting for payment history is deterministic by date, createdAt, and id descending.
- [x] Optional/null input fields are handled in service validation.
- [x] No fake production data is generated in production code.
- [x] Existing FR-05 expense balance DTO is reused.

### Repository And Query Design

- [x] Repository queries are household-scoped.
- [x] Backend does not accept frontend household scope.
- [x] Query sorting is deterministic.
- [x] Repository method names are readable.
- [x] Explicit JPQL query is used for payments involving a specific roommate.
- [ ] N+1 behavior has not yet been performance-tested.

### Unit Test Coverage Added

`PaymentServiceTest` covers:

- [x] Empty household data returns empty overview.
- [x] One expense without payments creates expected open balance.
- [x] Partial payment reduces open balance.
- [x] Full payment removes open balance.
- [x] Roommate sees only own balances/payments.
- [x] Valid payment is saved.
- [x] User without active household is rejected.
- [x] Payment to roommate in another household is rejected.
- [x] Invalid amount is rejected.

## Command Evidence

Command attempted:

```bash
cd backend
mvn -Dtest=PaymentServiceTest test
```

Result:

```text
Build did not reach test execution.
Maven fails in validate phase inside maven-checkstyle-plugin 3.6.0.
The local runtime reports a Checkstyle class version mismatch:
Checkstyle classes are compiled for class file version 65.0,
while the Maven execution reports support only up to 61.0.
```

Additional command attempted earlier:

```bash
cd backend
mvn -DskipTests compile
```

Result:

```text
Same validate/checkstyle failure before Java compilation.
```

## Backend Merge Blockers Still Open

- [ ] Backend unit tests were added, but could not be executed locally because Maven fails before tests.
- [ ] Backend integration tests are not implemented yet.
- [ ] Unauthenticated endpoint behavior is not covered by integration test yet.
- [ ] Full backend test suite has not passed in this local environment.

## Backend QA Conclusion

Backend implementation for FR-06 steps 1-5 is present and code-inspection checks for endpoint shape, DTO safety, household scoping, payment validation, and unit test coverage are satisfied.

The backend is **not yet fully QA-approved** because automated test execution is blocked locally by the Java/Checkstyle runtime mismatch and because integration tests are still pending.
