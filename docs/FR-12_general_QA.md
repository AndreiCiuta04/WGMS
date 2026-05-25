# FR-12 General QA Checklist — Dashboard with Current Responsibilities

## Feature Information

Feature ID: FR-12
Feature name: Dashboard with Current Responsibilities
Use case: UC-06 — Central Dashboard and Notifications Management
Priority: High
Category: Core
Version: V3
Dependency: FR-03 Task creation/assignment, FR-04 Cleaning schedule management, FR-05 expense tracking, optionally FR-06 payment status overview

Branch:
Merge request:
Developer:
QA reviewer:
Date:

## How to Use This Checklist

This is the feature-level QA checklist. It verifies backend, frontend, e2e, documentation, test evidence, and merge readiness. Use `[ ]` until evidence exists. Do not check items based only on assumptions.

## 1. Critical Checks — Feature Scope

- [ ] FR-12 implements the agreed dashboard/current responsibilities feature.
- [ ] Feature behavior matches UC-06.
- [ ] Feature does not implement unrelated scope.
- [ ] Feature uses existing authentication and household model.
- [ ] Feature is household-scoped.
- [ ] Feature does not expose other household data.
- [ ] Feature is reachable in the frontend.
- [ ] Feature has backend support.
- [ ] Feature shows current responsibilities rather than only historical data.
- [ ] Known limitations are documented.

## 2. Critical Checks — Dependencies

- [ ] FR-01 authentication/login is available.
- [ ] FR-02 household membership is available.
- [ ] FR-03 task creation/assignment is available.
- [ ] FR-04 cleaning schedule management is available if schedule tasks appear.
- [ ] FR-05 expense tracking is available if expense/payment summary appears.
- [ ] FR-06 payment status overview is available or limitation documented if payment summary depends on it.
- [ ] FR-13 history integration is not required for current dashboard data unless explicitly used.
- [ ] Existing related features still work.
- [ ] Data model assumptions are documented.

## 3. Critical Checks — End-to-End Acceptance Criteria

- [ ] User can log in.
- [ ] User can navigate to dashboard.
- [ ] User sees current responsibilities for their own household.
- [ ] User sees task responsibility data.
- [ ] User sees overdue/upcoming task data if in scope.
- [ ] User sees payment/expense summary if in scope.
- [ ] User sees reminders/notifications if in scope.
- [ ] User cannot see data from another household.
- [ ] Empty/no-responsibilities state works.
- [ ] Error state works.
- [ ] Populated state works.
- [ ] Frontend does not send household id.
- [ ] Backend derives household from authenticated user.
- [ ] Page/API does not crash.
- [ ] Data shown is not fake production data.

## 4. Critical Checks — Test Evidence Exists

### Backend

- [ ] Backend unit tests exist.
- [ ] Backend integration tests exist.
- [ ] Backend tests cover auth.
- [ ] Backend tests cover household isolation.
- [ ] Backend tests cover empty/no-responsibilities data.
- [ ] Backend tests cover populated dashboard data.
- [ ] Backend tests cover task responsibility data.
- [ ] Backend tests cover payment/expense summary if in scope.
- [ ] Backend tests cover reminders/notifications if in scope.
- [ ] Backend full suite passes.

Command:

```bash
cd backend && mvn test
```

### Frontend

- [ ] Frontend service tests exist.
- [ ] Frontend component tests exist.
- [ ] Frontend tests cover loading.
- [ ] Frontend tests cover error.
- [ ] Frontend tests cover empty state.
- [ ] Frontend tests cover populated rendering.
- [ ] Frontend tests confirm no household id is sent.
- [ ] Frontend tests pass.

Command:

```bash
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
```

### E2E

- [ ] E2E spec exists for FR-12.
- [ ] E2E covers login.
- [ ] E2E covers dashboard navigation.
- [ ] E2E covers populated dashboard data.
- [ ] E2E covers empty/no-crash behavior if possible.
- [ ] E2E uses synthetic data.
- [ ] E2E passes.

Command:

```bash
cd e2e && npx cypress run --spec cypress/e2e/dashboard.cy.js
```

## 5. Important Checks — Security and Privacy

- [ ] Backend endpoint is authenticated.
- [ ] Backend endpoint is household-scoped.
- [ ] Frontend sends no household id.
- [ ] Backend ignores client-side attempts to change household scope.
- [ ] Cross-household leakage is tested.
- [ ] Response exposes only necessary dashboard data.
- [ ] No passwords, tokens, invitation secrets, or unrelated users are exposed.
- [ ] Logs do not dump sensitive dashboard data.
- [ ] Test data is synthetic.

## 6. Important Checks — Documentation

- [ ] Backend QA file exists: `backend/FR-12_backend_QA.md`.
- [ ] Frontend QA file exists: `frontend/FR-12_frontend_QA.md`.
- [ ] General QA file exists: `docs/FR-12_general_QA.md`.
- [ ] MR description explains feature scope.
- [ ] MR description lists endpoints/routes.
- [ ] MR description lists tests run.
- [ ] Known limitations are included.
- [ ] Manual/e2e verification steps are documented.
- [ ] Evidence is updated after actual test runs.

## 7. Important Checks — Regression

- [ ] Existing auth still works.
- [ ] Existing household membership still works.
- [ ] Existing task/cleaning/expense/shopping pages and endpoints still work.
- [ ] Full backend tests pass.
- [ ] Frontend tests pass.
- [ ] Frontend build passes.
- [ ] E2E tests pass or limitations are documented.
- [ ] No existing tests were deleted or weakened.

## 8. Merge Blockers

- [ ] Verified blocker not present: backend tests missing.
- [ ] Verified blocker not present: frontend tests missing.
- [ ] Verified blocker not present: e2e test missing without justification.
- [ ] Verified blocker not present: endpoint unauthenticated.
- [ ] Verified blocker not present: cross-household data leaks.
- [ ] Verified blocker not present: frontend sends household id.
- [ ] Verified blocker not present: fake production data appears.
- [ ] Verified blocker not present: empty state crashes.
- [ ] Verified blocker not present: current responsibility data is absent.
- [ ] Verified blocker not present: build/test commands fail.
- [ ] Verified blocker not present: known limitations undocumented.

## 9. Final Approval

- [ ] Backend QA completed.
- [ ] Frontend QA completed.
- [ ] General QA completed.
- [ ] Backend tests exist and pass.
- [ ] Frontend tests exist and pass.
- [ ] E2E tests exist and pass, or absence is explicitly justified.
- [ ] Household isolation verified.
- [ ] Dashboard correctness verified.
- [ ] Known limitations documented.
- [ ] MR ready for review.
