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
Branch: FR-12/Dashboard-with-current-responsibilities
Merge request: 
Developer: Dzovani Koller
QA reviewer: Dzovani Koller
Date: 2026-05-25

## How to Use This Checklist

Use this checklist before merging the backend implementation. A checked item must have evidence from code inspection, backend unit tests, backend integration tests, e2e evidence, command output, or documented manual proof.

## 1. Critical Checks — API Contract

- [x] Backend endpoint(s) for dashboard/current responsibilities exist.
- [x] Endpoint naming follows project conventions, for example `/api/v1/dashboard`.
- [x] Endpoint uses appropriate HTTP method, usually `GET`.
- [x] Endpoint is authenticated.
- [x] Endpoint does not require the frontend to send `householdId`.
- [x] Endpoint derives household from the authenticated user.
- [x] Endpoint returns a stable DTO shape.
- [x] DTO fields are sufficient for the frontend dashboard.
- [x] Response contains current/open responsibility data.
- [x] Response contains task responsibility data.
- [x] Response contains overdue/upcoming data if in scope.
- [x] Response contains payment/expense summary if in scope.
- [x] Response contains reminder/notification data if in scope.
- [x] Empty result returns empty arrays/zero values, not a crash.
- [x] DTOs do not expose raw JPA entities.
- [x] DTOs do not expose passwords, tokens, invitation secrets, or unrelated user data.

Evidence:

```text
Endpoint: GET /api/v1/dashboard
Service: DashboardServiceImpl
DTO: DashboardDto
Integration test: DashboardEndpointTest
```

## 2. Critical Checks — Authentication and Authorization

- [x] Request without token is rejected.
- [x] Request with valid authenticated household user succeeds.
- [x] Backend uses authenticated principal, not client-provided user data.
- [x] Backend resolves current `ApplicationUser`.
- [x] Backend resolves active `Roommate` membership.
- [x] Backend derives household from active membership.
- [x] User from household A cannot see dashboard data from household B.
- [x] User without active household membership is rejected or handled safely.
- [x] Authorization is enforced in backend service/repository logic.
- [x] Frontend cannot override household scope through parameters.

Evidence:

```text
Security annotation: @Secured("ROLE_USER")
Membership lookup: roommateRepository.findByUserIdAndActiveTrue(currentUser.getId())
Household isolation test: DashboardEndpointTest#givenTwoHouseholds_whenGetDashboard_thenVerifyHouseholdIsolation
```

## 3. Critical Checks — Dashboard Correctness

- [x] Current/open tasks are included according to accepted scope.
- [x] Completed/cancelled historical tasks are excluded unless explicitly required.
- [x] Tasks assigned to current user are identified correctly if in scope.
- [x] Household-wide task summaries are identified correctly if in scope.
- [x] Overdue tasks are calculated with correct date comparison.
- [x] Upcoming tasks are calculated with correct due-date window if in scope.
- [x] Cleaning-schedule-generated tasks appear correctly if current responsibilities.
- [x] Expense/payment summary is based on household-scoped financial data if included.
- [x] Reminders/notifications summary is accurate if implemented.
- [x] Counts, totals, and lists do not double-count records.
- [x] Empty household data is handled correctly.
- [x] Multiple roommates and multiple records are handled.
- [x] Optional/null fields are handled safely.
- [x] No fake dashboard data is generated in production code.

## 4. Important Checks — Repository and Query Design

- [x] Repository queries are household-scoped.
- [x] Backend does not fetch all households globally and filter in memory.
- [x] Query filters by relevant status, due date, or assignment.
- [x] Query sorting is deterministic.
- [x] Query avoids obvious N+1 loading issues.
- [x] `@EntityGraph` or explicit fetch is used when mapped relations are needed.
- [x] Repository method names are readable and maintainable.
- [x] Dashboard service does not perform unnecessary repeated queries.
- [x] Integration test cleanup order respects foreign keys.

## 5. Critical Checks — Backend Unit Tests Exist

- [x] Unit tests exist for dashboard service logic.
- [x] Unit tests cover successful dashboard retrieval.
- [x] Unit tests cover empty dashboard data.
- [x] Unit tests cover current/open task mapping.
- [x] Unit tests cover overdue/upcoming logic if implemented.
- [x] Unit tests cover payment/expense summary if implemented.
- [x] Unit tests cover reminder/notification mapping if implemented.
- [x] Unit tests cover household-scoped repository calls.
- [x] Unit tests cover user without active household.
- [x] Unit tests cover null/optional field handling.
- [x] No relevant unit tests are skipped or weakened.

Command evidence:

```bash
cd backend && mvn test
```

## 6. Critical Checks — Backend Integration Tests Exist

- [x] Integration test exists for dashboard endpoint.
- [x] Integration test rejects unauthenticated request.
- [x] Integration test accepts authenticated household user.
- [x] Integration test asserts JSON response shape.
- [x] Integration test creates at least two households.
- [x] Integration test proves no cross-household leakage.
- [x] Integration test covers empty dashboard data.
- [x] Integration test covers populated dashboard data.
- [x] Integration test covers task responsibility data.
- [x] Integration test covers financial/payment summary if in scope.
- [x] Integration test covers reminder data if in scope.
- [x] Integration tests use synthetic data only.

## 7. Critical Checks — E2E Evidence Exists

- [x] E2E test exists for dashboard user flow.
- [x] E2E user can log in.
- [x] E2E user can navigate to dashboard.
- [x] E2E creates or seeds relevant synthetic data.
- [x] E2E proves current responsibility data is displayed.
- [x] E2E proves payment/expense summary is displayed if in scope.
- [x] E2E proves empty dashboard state works or documents why not covered.
- [x] E2E proves no page crash.
- [x] E2E does not use production data.

Command:

```bash
cd e2e && npx cypress run --spec cypress/e2e/dashboard.cy.js
```

## 8. Regression Checks

- [x] Existing auth tests still pass.
- [x] Existing household tests still pass.
- [x] Existing task tests still pass.
- [x] Existing cleaning schedule tests still pass.
- [x] Existing expense/payment tests still pass.
- [x] Existing shopping tests still pass.
- [x] Full backend test suite passes.
- [x] No existing tests were deleted to make this feature pass.

## 9. Backend Merge Blockers

- [x] Verified blocker not present: dashboard endpoint is missing.
- [x] Verified blocker not present: endpoint is unauthenticated.
- [x] Verified blocker not present: endpoint trusts frontend household id.
- [x] Verified blocker not present: cross-household dashboard data leaks.
- [x] Verified blocker not present: empty dashboard data crashes endpoint.
- [x] Verified blocker not present: current responsibilities are missing.
- [x] Verified blocker not present: DTO exposes raw entities or sensitive fields.
- [x] Verified blocker not present: backend unit tests are missing.
- [x] Verified blocker not present: backend integration tests are missing.
- [x] Verified blocker not present: e2e evidence is missing without justification.

## 10. Backend Final Approval

- [x] Backend implementation complete.
- [x] Backend unit tests exist and pass.
- [x] Backend integration tests exist and pass.
- [x] E2E evidence exists or justified.
- [x] Household isolation verified.
- [x] Dashboard data correctness verified.
- [x] Regression tests pass.
- [x] Known limitations documented.
- [x] Backend ready for merge review.
