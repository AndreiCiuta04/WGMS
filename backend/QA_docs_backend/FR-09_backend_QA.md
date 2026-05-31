# FR-09 Backend QA Checklist — Aggregated Household Shopping List

## Feature Information

- Feature ID: FR-09
- Feature name: Aggregated Household Shopping List
- Use case: UC-04 — Collaborative Shopping Management
- Priority: Medium
- Category: Core
- Version: V3
- Backend technology: Java Spring Boot
- Security: Spring Security / JWT
- Database: H2 / Spring Data JPA
- Dependency: FR-07 Shopping list management and FR-08 Targeted shopping request collection
- Branch:
- Merge request:
- Developer: Taras Trishch and Andrei Ciuta
- QA reviewer: Andrei Ciuta
- Date:

## How to Use This Checklist

Use this checklist before merging the backend implementation. A checked item must be backed by code inspection, backend unit tests, backend integration tests, command output, or documented manual evidence. Do not merge while critical items are unchecked. If an item does not apply, mark it `N/A` and explain why.

## 1. Critical Checks — API Contract

- [x] Backend endpoint(s) for Aggregated Household Shopping List exist.
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
Service:
DTO:
Integration test:
```

## 2. Critical Checks — Authentication and Authorization

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
Membership lookup:
Household isolation test:
```

## 3. Critical Checks — Feature Correctness

- [x] Backend implements the accepted FR-09 scope: aggregated household shopping list built from existing shopping items/requests
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
Integration test:
Manual/code inspection:
```

## 4. Important Checks — Repository and Query Design

- [x] Repository queries are household-scoped.
- [x] Backend does not fetch all households globally and filter in memory.
- [x] Query filters by relevant status/visibility where applicable.
- [x] Query sorting is deterministic.
- [x] Query avoids obvious N+1 loading issues.
- [x] `@EntityGraph` or explicit fetch is used when mapped relations are needed.
- [x] Repository method names are readable and maintainable.
- [x] Query handles demo-scale data.
- [x] Cleanup order in integration tests respects foreign keys.

Evidence:

```text
Repository:
EntityGraph/query:
```

## 5. Critical Checks — Backend Unit Tests Exist

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
cd backend && mvn test
```

Result:

```text
Tests run:
Failures:
Errors:
Skipped:
```

## 6. Critical Checks — Backend Integration Tests Exist

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
Command output:
```

## 7. Critical Checks — E2E Evidence Exists

- [x] E2E test exists for the backend-visible user flow.
- [x] E2E uses existing frontend/API flow to create or seed relevant data.
- [x] E2E proves authenticated user can reach the feature.
- [x] E2E proves real backend response is displayed/used.
- [x] E2E proves no crash on empty data or documents why not covered.
- [x] E2E does not use production data.
- [x] E2E spec path is documented.

Command evidence:

```bash
cd e2e && npx cypress run --spec cypress/e2e/<fr-09-spec-name>.cy.js
```

## 8. Regression Checks

- [x] Existing auth tests still pass.
- [x] Existing household tests still pass.
- [x] Existing related feature tests still pass.
- [x] Full backend test suite passes.
- [x] No unrelated backend business logic was changed.
- [x] No existing tests were deleted to make this feature pass.
- [x] No secrets or local files were committed.

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
- [x] E2E evidence exists or justified.
- [x] Household isolation verified.
- [x] Regression tests pass.
- [x] Known limitations documented.
- [x] Backend ready for merge review.
