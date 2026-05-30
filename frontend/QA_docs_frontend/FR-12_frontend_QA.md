# FR-12 Frontend QA Checklist — Dashboard with Current Responsibilities

## Feature Information

Feature ID: FR-12
Feature name: Dashboard with Current Responsibilities
Use case: UC-06 — Central Dashboard and Notifications Management
Priority: High
Category: Core
Version: V3
Dependency: FR-03 Task creation/assignment, FR-04 Cleaning schedule management, FR-05 expense tracking, optionally FR-06 payment status overview

Frontend technology: Angular
Branch: FR-12/Dashboard-with-current-responsibilities
Merge request:
Developer: Dzovani Koller
QA reviewer: Dzovani Koller
Date: 2026-05-25

## How to Use This Checklist

Use this checklist before merging the frontend implementation. A checked item must have evidence from code inspection, frontend tests, service tests, e2e tests, command output, or screenshot/manual proof.

## 1. Critical Checks — Route and Navigation

- [x] Dashboard page/section is reachable in the UI.
- [x] Route or default landing page follows existing Angular routing conventions.
- [x] Navigation/sidebar entry exists if dashboard is not already landing page.
- [x] Navigation label is clear, for example `Dashboard`.
- [x] Route is protected consistently with authenticated app routes.
- [x] Existing navigation links remain intact.
- [x] Browser refresh works on dashboard route.
- [x] Back/forward navigation works.
- [x] Dashboard route does not conflict with task, expense, shopping, or history routes.

## 2. Critical Checks — Service and DTO Integration

- [x] Frontend DTO/interface exists for dashboard response.
- [x] DTO field names match backend JSON.
- [x] Optional/null fields are typed safely.
- [x] Service method exists for loading dashboard data.
- [x] Service calls the correct backend endpoint.
- [x] Service uses existing global backend URI convention.
- [x] Service sends no `householdId`.
- [x] Service sends no user id, roommate id, or email for household scoping.
- [x] Service relies on existing auth/JWT mechanism.
- [x] Service contains no fake/mock production data.
- [x] Service test verifies endpoint URL and HTTP method.
- [x] Service test verifies no household id is sent.

## 3. Critical Checks — Component Behavior

- [x] Component loads dashboard data on initialization.
- [x] Component avoids nested subscription leaks.
- [x] Loading state is shown.
- [x] Error state is shown for failed API calls.
- [x] Empty/no-responsibilities state is shown.
- [x] Populated state renders backend data.
- [x] Component handles optional/null fields safely.
- [x] Component handles multiple dashboard entries.
- [x] Component does not render `undefined`, `NaN`, or `[object Object]`.
- [x] Component does not show fake production data.
- [x] Component can recover by refresh/navigation after error.

## 4. Critical Checks — Dashboard Rendering

- [x] Dashboard displays current/open responsibilities.
- [x] Dashboard displays tasks assigned to current user if in scope.
- [x] Dashboard displays household-wide open tasks if in scope.
- [x] Dashboard displays overdue tasks clearly if in scope.
- [x] Dashboard displays upcoming tasks clearly if in scope.
- [x] Dashboard displays payment/expense summary if in scope.
- [x] Dashboard displays reminders/notifications if in scope.
- [x] Task titles/statuses/due dates are visible.
- [x] Payment/expense amounts/statuses are visible if included.
- [x] Counts/totals are labelled clearly.
- [x] Empty state text is understandable.
- [x] Error state text is user-friendly and does not expose stack traces.
- [x] Long names/descriptions/emails do not break layout.
- [x] UI does not misrepresent historical/completed items as current responsibilities.

## 5. Important Checks — UI Theme and Style

- [x] UI matches existing WGMS theme.
- [x] Existing card/table/list styling is reused.
- [x] Existing spacing and typography are preserved.
- [x] Buttons/links match existing style.
- [x] Badges/status labels match existing app style.
- [x] No external UI library is introduced.
- [x] No unrelated global CSS changes are introduced.
- [x] Page works on desktop.
- [x] Page works on mobile widths.
- [x] Long text wraps/truncates safely.

## 6. Critical Checks — Frontend Tests Exist

- [x] Component creation test exists.
- [x] Component calls service on load.
- [x] Loading state test exists.
- [x] Error state test exists.
- [x] Empty/no-responsibilities state test exists.
- [x] Populated rendering test exists.
- [x] Test covers current/open task responsibility rendering.
- [x] Test covers overdue/upcoming task rendering if implemented.
- [x] Test covers payment/expense summary rendering if implemented.
- [x] Test covers reminder/notification rendering if implemented.
- [x] Test covers optional/null fields.
- [x] Service test exists.
- [x] Service test verifies endpoint URL.
- [x] Service test verifies no household id is sent.
- [x] No frontend tests are skipped or weakened.

Command:

```bash
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
```

## 7. Critical Checks — E2E Tests Exist

- [x] Cypress/e2e spec exists for FR-12.
- [x] E2E user can log in.
- [x] E2E user can navigate to dashboard.
- [x] E2E creates or seeds relevant synthetic data.
- [x] E2E verifies current responsibility data is visible.
- [x] E2E verifies payment/expense summary is visible if in scope.
- [x] E2E verifies empty/no-responsibilities state if possible.
- [x] E2E verifies no page crash.
- [x] E2E does not depend on production data.

Command:

```bash
cd e2e && npx cypress run --spec cypress/e2e/dashboard.cy.js
```

## 8. Regression Checks

- [x] Existing login flow still works.
- [x] Existing sidebar/navigation still works.
- [x] Existing task page still works.
- [x] Existing cleaning schedule page still works.
- [x] Existing expense/payment page still works.
- [x] Existing shopping page still works.
- [x] Existing frontend tests pass.
- [x] Frontend build passes.
- [x] Lint passes if configured.
- [x] No fake production data remains.

## 9. Frontend Merge Blockers

- [x] Verified blocker not present: dashboard is unreachable.
- [x] Verified blocker not present: frontend sends household id.
- [x] Verified blocker not present: fake production data is shown.
- [x] Verified blocker not present: empty/no-responsibilities state crashes page.
- [x] Verified blocker not present: backend error crashes page.
- [x] Verified blocker not present: normal null fields render ugly values.
- [x] Verified blocker not present: UI style is inconsistent with app.
- [x] Verified blocker not present: frontend tests are missing.
- [x] Verified blocker not present: e2e test is missing without justification.
- [x] Verified blocker not present: build fails.

## 10. Frontend Final Approval

- [x] Route/navigation verified.
- [x] Service/DTO contract verified.
- [x] Loading/error/empty states verified.
- [x] Dashboard rendering verified.
- [x] Frontend tests exist and pass.
- [x] E2E tests exist and pass, or absence is explicitly justified.
- [x] Build/lint pass.
- [x] Known limitations documented.
- [x] Frontend ready for merge review.
