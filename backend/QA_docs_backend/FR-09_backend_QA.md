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
- Developer:
- QA reviewer:
- Date:

## How to Use This Checklist

Use this checklist before merging the backend implementation. A checked item must be backed by code inspection, backend unit tests, backend integration tests, command output, or documented manual evidence. Do not merge while critical items are unchecked. If an item does not apply, mark it `N/A` and explain why.

## 1. Critical Checks — API Contract

- [ ] Backend endpoint(s) for Aggregated Household Shopping List exist.
- [ ] Endpoint naming follows project conventions.
- [ ] Endpoint uses the correct HTTP method.
- [ ] Endpoint is authenticated.
- [ ] Endpoint does not require the frontend to send `householdId`.
- [ ] Endpoint derives the household from the authenticated user.
- [ ] Endpoint returns a stable DTO shape.
- [ ] DTO fields are sufficient for the frontend.
- [ ] DTOs do not expose raw JPA entities.
- [ ] DTOs do not expose passwords, tokens, invitation secrets, or unrelated user data.
- [ ] Empty result returns an empty array/object as appropriate, not `null` or an error.
- [ ] Error cases use the project-standard exception handling.

Evidence:

```text
Endpoint:
Service:
DTO:
Integration test:
```

## 2. Critical Checks — Authentication and Authorization

- [ ] Request without token is rejected.
- [ ] Request with valid authenticated household user succeeds.
- [ ] Backend uses the authenticated principal, not client-provided user data.
- [ ] Backend resolves the active roommate membership.
- [ ] Backend scopes all data to the active roommate's household.
- [ ] User from household A cannot see household B data.
- [ ] User without active household membership is rejected.
- [ ] Authorization is enforced in backend service/repository logic.
- [ ] Frontend cannot override household scope through parameters.

Evidence:

```text
Security annotation:
Membership lookup:
Household isolation test:
```

## 3. Critical Checks — Feature Correctness

- [ ] Backend implements the accepted FR-09 scope: aggregated household shopping list built from existing shopping items/requests
- [ ] Correct source entities/repositories are used.
- [ ] Data from the current household is included.
- [ ] Data from other households is excluded.
- [ ] Empty household data is handled.
- [ ] Multiple roommates are handled.
- [ ] Multiple records are handled.
- [ ] Duplicate or repeated records are handled according to the feature rules.
- [ ] Sorting is deterministic and documented.
- [ ] Optional/null fields are handled safely.
- [ ] No fake data is generated in production code.
- [ ] Existing related feature behavior is not changed unexpectedly.

Evidence:

```text
Unit test:
Integration test:
Manual/code inspection:
```

## 4. Important Checks — Repository and Query Design

- [ ] Repository queries are household-scoped.
- [ ] Backend does not fetch all households globally and filter in memory.
- [ ] Query filters by relevant status/visibility where applicable.
- [ ] Query sorting is deterministic.
- [ ] Query avoids obvious N+1 loading issues.
- [ ] `@EntityGraph` or explicit fetch is used when mapped relations are needed.
- [ ] Repository method names are readable and maintainable.
- [ ] Query handles demo-scale data.
- [ ] Cleanup order in integration tests respects foreign keys.

Evidence:

```text
Repository:
EntityGraph/query:
```

## 5. Critical Checks — Backend Unit Tests Exist

- [ ] Unit tests exist for the main service/calculation/aggregation logic.
- [ ] Unit tests cover successful data retrieval.
- [ ] Unit tests cover empty data.
- [ ] Unit tests cover multiple roommates.
- [ ] Unit tests cover multiple records.
- [ ] Unit tests cover household-scoped repository calls.
- [ ] Unit tests cover user without active household.
- [ ] Unit tests cover null/optional field handling.
- [ ] Unit tests cover important edge cases for this feature.
- [ ] No relevant unit tests are skipped or weakened.

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

- [ ] Integration test exists for the main endpoint.
- [ ] Integration test rejects unauthenticated request.
- [ ] Integration test accepts authenticated household user.
- [ ] Integration test asserts JSON response shape.
- [ ] Integration test creates at least two households.
- [ ] Integration test proves no cross-household leakage.
- [ ] Integration test covers empty result.
- [ ] Integration test covers populated result.
- [ ] Integration test runs as part of `mvn test`.
- [ ] Integration tests use synthetic data only.

Evidence:

```text
Integration test class:
Command output:
```

## 7. Critical Checks — E2E Evidence Exists

- [ ] E2E test exists for the backend-visible user flow.
- [ ] E2E uses existing frontend/API flow to create or seed relevant data.
- [ ] E2E proves authenticated user can reach the feature.
- [ ] E2E proves real backend response is displayed/used.
- [ ] E2E proves no crash on empty data or documents why not covered.
- [ ] E2E does not use production data.
- [ ] E2E spec path is documented.

Command evidence:

```bash
cd e2e && npx cypress run --spec cypress/e2e/<fr-09-spec-name>.cy.js
```

## 8. Regression Checks

- [ ] Existing auth tests still pass.
- [ ] Existing household tests still pass.
- [ ] Existing related feature tests still pass.
- [ ] Full backend test suite passes.
- [ ] No unrelated backend business logic was changed.
- [ ] No existing tests were deleted to make this feature pass.
- [ ] No secrets or local files were committed.

## 9. Backend Merge Blockers

- [ ] Verified blocker not present: backend endpoint is missing.
- [ ] Verified blocker not present: endpoint is unauthenticated.
- [ ] Verified blocker not present: endpoint trusts frontend household id.
- [ ] Verified blocker not present: cross-household data leaks.
- [ ] Verified blocker not present: empty data crashes endpoint.
- [ ] Verified blocker not present: DTO exposes raw entities or sensitive fields.
- [ ] Verified blocker not present: backend unit tests are missing.
- [ ] Verified blocker not present: backend integration tests are missing.
- [ ] Verified blocker not present: e2e evidence is missing without justification.
- [ ] Verified blocker not present: backend test suite fails.

## 10. Backend Final Approval

- [ ] Backend implementation complete.
- [ ] Backend unit tests exist and pass.
- [ ] Backend integration tests exist and pass.
- [ ] E2E evidence exists or justified.
- [ ] Household isolation verified.
- [ ] Regression tests pass.
- [ ] Known limitations documented.
- [ ] Backend ready for merge review.
