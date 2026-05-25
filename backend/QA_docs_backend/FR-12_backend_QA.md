# FR-12 Backend QA Checklist — Dashboard with Current Responsibilities

## Feature Information

Feature ID: FR-12
Feature name: Dashboard with Current Responsibilities
Use case: UC-06 — Central Dashboard and Notifications Management
Priority: High
Category: Core
Version: V3
Dependency: FR-03 Task creation/assignment, FR-04 Cleaning schedule management, FR-05 expense tracking, optionally FR-06 payment status overview

Backend technology: Java Spring Boot
Security: Spring Security / JWT
Database: H2 / Spring Data JPA
Branch:
Merge request:
Developer:
QA reviewer:
Date:

## How to Use This Checklist

Use this checklist before merging the backend implementation. A checked item must have evidence from code inspection, backend unit tests, backend integration tests, e2e evidence, command output, or documented manual proof.

## 1. Critical Checks — API Contract

- [ ] Backend endpoint(s) for dashboard/current responsibilities exist.
- [ ] Endpoint naming follows project conventions, for example `/api/v1/dashboard`.
- [ ] Endpoint uses appropriate HTTP method, usually `GET`.
- [ ] Endpoint is authenticated.
- [ ] Endpoint does not require the frontend to send `householdId`.
- [ ] Endpoint derives household from the authenticated user.
- [ ] Endpoint returns a stable DTO shape.
- [ ] DTO fields are sufficient for the frontend dashboard.
- [ ] Response contains current/open responsibility data.
- [ ] Response contains task responsibility data.
- [ ] Response contains overdue/upcoming data if in scope.
- [ ] Response contains payment/expense summary if in scope.
- [ ] Response contains reminder/notification data if in scope.
- [ ] Empty result returns empty arrays/zero values, not a crash.
- [ ] DTOs do not expose raw JPA entities.
- [ ] DTOs do not expose passwords, tokens, invitation secrets, or unrelated user data.

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
- [ ] Backend uses authenticated principal, not client-provided user data.
- [ ] Backend resolves current `ApplicationUser`.
- [ ] Backend resolves active `Roommate` membership.
- [ ] Backend derives household from active membership.
- [ ] User from household A cannot see dashboard data from household B.
- [ ] User without active household membership is rejected or handled safely.
- [ ] Authorization is enforced in backend service/repository logic.
- [ ] Frontend cannot override household scope through parameters.

Evidence:

```text
Security annotation:
Membership lookup:
Household isolation test:
```

## 3. Critical Checks — Dashboard Correctness

- [ ] Current/open tasks are included according to accepted scope.
- [ ] Completed/cancelled historical tasks are excluded unless explicitly required.
- [ ] Tasks assigned to current user are identified correctly if in scope.
- [ ] Household-wide task summaries are identified correctly if in scope.
- [ ] Overdue tasks are calculated with correct date comparison.
- [ ] Upcoming tasks are calculated with correct due-date window if in scope.
- [ ] Cleaning-schedule-generated tasks appear correctly if current responsibilities.
- [ ] Expense/payment summary is based on household-scoped financial data if included.
- [ ] Reminders/notifications summary is accurate if implemented.
- [ ] Counts, totals, and lists do not double-count records.
- [ ] Empty household data is handled correctly.
- [ ] Multiple roommates and multiple records are handled.
- [ ] Optional/null fields are handled safely.
- [ ] No fake dashboard data is generated in production code.

## 4. Important Checks — Repository and Query Design

- [ ] Repository queries are household-scoped.
- [ ] Backend does not fetch all households globally and filter in memory.
- [ ] Query filters by relevant status, due date, or assignment.
- [ ] Query sorting is deterministic.
- [ ] Query avoids obvious N+1 loading issues.
- [ ] `@EntityGraph` or explicit fetch is used when mapped relations are needed.
- [ ] Repository method names are readable and maintainable.
- [ ] Dashboard service does not perform unnecessary repeated queries.
- [ ] Integration test cleanup order respects foreign keys.

## 5. Critical Checks — Backend Unit Tests Exist

- [ ] Unit tests exist for dashboard service logic.
- [ ] Unit tests cover successful dashboard retrieval.
- [ ] Unit tests cover empty dashboard data.
- [ ] Unit tests cover current/open task mapping.
- [ ] Unit tests cover overdue/upcoming logic if implemented.
- [ ] Unit tests cover payment/expense summary if implemented.
- [ ] Unit tests cover reminder/notification mapping if implemented.
- [ ] Unit tests cover household-scoped repository calls.
- [ ] Unit tests cover user without active household.
- [ ] Unit tests cover null/optional field handling.
- [ ] No relevant unit tests are skipped or weakened.

Command evidence:

```bash
cd backend && mvn test
```

## 6. Critical Checks — Backend Integration Tests Exist

- [ ] Integration test exists for dashboard endpoint.
- [ ] Integration test rejects unauthenticated request.
- [ ] Integration test accepts authenticated household user.
- [ ] Integration test asserts JSON response shape.
- [ ] Integration test creates at least two households.
- [ ] Integration test proves no cross-household leakage.
- [ ] Integration test covers empty dashboard data.
- [ ] Integration test covers populated dashboard data.
- [ ] Integration test covers task responsibility data.
- [ ] Integration test covers financial/payment summary if in scope.
- [ ] Integration test covers reminder data if in scope.
- [ ] Integration tests use synthetic data only.

## 7. Critical Checks — E2E Evidence Exists

- [ ] E2E test exists for dashboard user flow.
- [ ] E2E user can log in.
- [ ] E2E user can navigate to dashboard.
- [ ] E2E creates or seeds relevant synthetic data.
- [ ] E2E proves current responsibility data is displayed.
- [ ] E2E proves payment/expense summary is displayed if in scope.
- [ ] E2E proves empty dashboard state works or documents why not covered.
- [ ] E2E proves no page crash.
- [ ] E2E does not use production data.

Command:

```bash
cd e2e && npx cypress run --spec cypress/e2e/dashboard.cy.js
```

## 8. Regression Checks

- [ ] Existing auth tests still pass.
- [ ] Existing household tests still pass.
- [ ] Existing task tests still pass.
- [ ] Existing cleaning schedule tests still pass.
- [ ] Existing expense/payment tests still pass.
- [ ] Existing shopping tests still pass.
- [ ] Full backend test suite passes.
- [ ] No existing tests were deleted to make this feature pass.

## 9. Backend Merge Blockers

- [ ] Verified blocker not present: dashboard endpoint is missing.
- [ ] Verified blocker not present: endpoint is unauthenticated.
- [ ] Verified blocker not present: endpoint trusts frontend household id.
- [ ] Verified blocker not present: cross-household dashboard data leaks.
- [ ] Verified blocker not present: empty dashboard data crashes endpoint.
- [ ] Verified blocker not present: current responsibilities are missing.
- [ ] Verified blocker not present: DTO exposes raw entities or sensitive fields.
- [ ] Verified blocker not present: backend unit tests are missing.
- [ ] Verified blocker not present: backend integration tests are missing.
- [ ] Verified blocker not present: e2e evidence is missing without justification.

## 10. Backend Final Approval

- [ ] Backend implementation complete.
- [ ] Backend unit tests exist and pass.
- [ ] Backend integration tests exist and pass.
- [ ] E2E evidence exists or justified.
- [ ] Household isolation verified.
- [ ] Dashboard data correctness verified.
- [ ] Regression tests pass.
- [ ] Known limitations documented.
- [ ] Backend ready for merge review.
