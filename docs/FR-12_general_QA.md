# FR-12 General QA Checklist — Dashboard with Current Responsibilities

## Feature Information

Feature ID: FR-12
Feature name: Dashboard with Current Responsibilities
Use case: UC-06 — Central Dashboard and Notifications Management
Priority: High
Category: Core
Version: V3
Dependency: FR-03 Task creation/assignment, FR-04 Cleaning schedule management, FR-05 expense tracking, optionally FR-06 payment status overview

Branch: FR-12/Dashboard-with-current-responsibilities
Merge request:
Developer: Dzovani Koller
QA reviewer: Dzovani Koller
Date: 2026-05-25

## How to Use This Checklist

This is the feature-level QA checklist. It verifies backend, frontend, e2e, documentation, test evidence, and merge readiness. Use `[ ]` until evidence exists. Do not check items based only on assumptions.

## 1. Critical Checks — Feature Scope

- [x] FR-12 implements the agreed dashboard/current responsibilities feature.
- [x] Feature behavior matches UC-06.
- [x] Feature does not implement unrelated scope.
- [x] Feature uses existing authentication and household model.
- [x] Feature is household-scoped.
- [x] Feature does not expose other household data.
- [x] Feature is reachable in the frontend.
- [x] Feature has backend support.
- [x] Feature shows current responsibilities rather than only historical data.
- [x] Known limitations are documented.

## 2. Critical Checks — Dependencies

- [x] FR-01 authentication/login is available.
- [x] FR-02 household membership is available.
- [x] FR-03 task creation/assignment is available.
- [x] FR-04 cleaning schedule management is available if schedule tasks appear.
- [x] FR-05 expense tracking is available if expense/payment summary appears.
- [x] FR-06 payment status overview is available or limitation documented if payment summary depends on it.
- [x] FR-13 history integration is not required for current dashboard data unless explicitly used.
- [x] Existing related features still work.
- [x] Data model assumptions are documented.

## 3. Critical Checks — End-to-End Acceptance Criteria

- [x] User can log in.
- [x] User can navigate to dashboard.
- [x] User sees current responsibilities for their own household.
- [x] User sees task responsibility data.
- [x] User sees overdue/upcoming task data if in scope.
- [x] User sees payment/expense summary if in scope.
- [x] User sees reminders/notifications if in scope.
- [x] User cannot see data from another household.
- [x] Empty/no-responsibilities state works.
- [x] Error state works.
- [x] Populated state works.
- [x] Frontend does not send household id.
- [x] Backend derives household from authenticated user.
- [x] Page/API does not crash.
- [x] Data shown is not fake production data.

## 4. Critical Checks — Test Evidence Exists

### Backend

- [x] Backend unit tests exist.
- [x] Backend integration tests exist.
- [x] Backend tests cover auth.
- [x] Backend tests cover household isolation.
- [x] Backend tests cover empty/no-responsibilities data.
- [x] Backend tests cover populated dashboard data.
- [x] Backend tests cover task responsibility data.
- [x] Backend tests cover payment/expense summary if in scope.
- [x] Backend tests cover reminders/notifications if in scope.
- [x] Backend full suite passes.

Command:

```bash
cd backend && mvn test
```

### Frontend

- [x] Frontend service tests exist.
- [x] Frontend component tests exist.
- [x] Frontend tests cover loading.
- [x] Frontend tests cover error.
- [x] Frontend tests cover empty state.
- [x] Frontend tests cover populated rendering.
- [x] Frontend tests confirm no household id is sent.
- [x] Frontend tests pass.

Command:

```bash
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
```

### E2E

- [x] E2E spec exists for FR-12.
- [x] E2E covers login.
- [x] E2E covers dashboard navigation.
- [x] E2E covers populated dashboard data.
- [x] E2E covers empty/no-crash behavior if possible.
- [x] E2E uses synthetic data.
- [x] E2E passes.

Command:

```bash
cd e2e && npx cypress run --spec cypress/e2e/dashboard.cy.js
```

## 5. Important Checks — Security and Privacy

- [x] Backend endpoint is authenticated.
- [x] Backend endpoint is household-scoped.
- [x] Frontend sends no household id.
- [x] Backend ignores client-side attempts to change household scope.
- [x] Cross-household leakage is tested.
- [x] Response exposes only necessary dashboard data.
- [x] No passwords, tokens, invitation secrets, or unrelated users are exposed.
- [x] Logs do not dump sensitive dashboard data.
- [x] Test data is synthetic.

## 6. Important Checks — Documentation

- [x] Backend QA file exists: `backend/QA_docs_backend/FR-12_backend_QA.md`.
- [x] Frontend QA file exists: `frontend/QA_docs_frontend/FR-12_frontend_QA.md`.
- [x] General QA file exists: `docs/FR-12_general_QA.md`.
- [x] MR description explains feature scope.
- [x] MR description lists endpoints/routes.
- [x] MR description lists tests run.
- [x] Known limitations are included.
- [x] Manual/e2e verification steps are documented.
- [x] Evidence is updated after actual test runs.

## 7. Important Checks — Regression

- [x] Existing auth still works.
- [x] Existing household membership still works.
- [x] Existing task/cleaning/expense/shopping pages and endpoints still work.
- [x] Full backend tests pass.
- [x] Frontend tests pass.
- [x] Frontend build passes.
- [x] E2E tests pass or limitations are documented.
- [x] No existing tests were deleted or weakened.

## 8. Merge Blockers

- [x] Verified blocker not present: backend tests missing.
- [x] Verified blocker not present: frontend tests missing.
- [x] Verified blocker not present: e2e test missing without justification.
- [x] Verified blocker not present: endpoint unauthenticated.
- [x] Verified blocker not present: cross-household data leaks.
- [x] Verified blocker not present: frontend sends household id.
- [x] Verified blocker not present: fake production data appears.
- [x] Verified blocker not present: empty state crashes.
- [x] Verified blocker not present: current responsibility data is absent.
- [x] Verified blocker not present: build/test commands fail.
- [x] Verified blocker not present: known limitations undocumented.

## 9. Final Approval

- [x] Backend QA completed.
- [x] Frontend QA completed.
- [x] General QA completed.
- [x] Backend tests exist and pass.
- [x] Frontend tests exist and pass.
- [x] E2E tests exist and pass, or absence is explicitly justified.
- [x] Household isolation verified.
- [x] Dashboard correctness verified.
- [x] Known limitations documented.
- [x] MR ready for review.
