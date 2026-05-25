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
Branch:
Merge request:
Developer:
QA reviewer:
Date:

## How to Use This Checklist

Use this checklist before merging the frontend implementation. A checked item must have evidence from code inspection, frontend tests, service tests, e2e tests, command output, or screenshot/manual proof.

## 1. Critical Checks — Route and Navigation

- [ ] Dashboard page/section is reachable in the UI.
- [ ] Route or default landing page follows existing Angular routing conventions.
- [ ] Navigation/sidebar entry exists if dashboard is not already landing page.
- [ ] Navigation label is clear, for example `Dashboard`.
- [ ] Route is protected consistently with authenticated app routes.
- [ ] Existing navigation links remain intact.
- [ ] Browser refresh works on dashboard route.
- [ ] Back/forward navigation works.
- [ ] Dashboard route does not conflict with task, expense, shopping, or history routes.

## 2. Critical Checks — Service and DTO Integration

- [ ] Frontend DTO/interface exists for dashboard response.
- [ ] DTO field names match backend JSON.
- [ ] Optional/null fields are typed safely.
- [ ] Service method exists for loading dashboard data.
- [ ] Service calls the correct backend endpoint.
- [ ] Service uses existing global backend URI convention.
- [ ] Service sends no `householdId`.
- [ ] Service sends no user id, roommate id, or email for household scoping.
- [ ] Service relies on existing auth/JWT mechanism.
- [ ] Service contains no fake/mock production data.
- [ ] Service test verifies endpoint URL and HTTP method.
- [ ] Service test verifies no household id is sent.

## 3. Critical Checks — Component Behavior

- [ ] Component loads dashboard data on initialization.
- [ ] Component avoids nested subscription leaks.
- [ ] Loading state is shown.
- [ ] Error state is shown for failed API calls.
- [ ] Empty/no-responsibilities state is shown.
- [ ] Populated state renders backend data.
- [ ] Component handles optional/null fields safely.
- [ ] Component handles multiple dashboard entries.
- [ ] Component does not render `undefined`, `NaN`, or `[object Object]`.
- [ ] Component does not show fake production data.
- [ ] Component can recover by refresh/navigation after error.

## 4. Critical Checks — Dashboard Rendering

- [ ] Dashboard displays current/open responsibilities.
- [ ] Dashboard displays tasks assigned to current user if in scope.
- [ ] Dashboard displays household-wide open tasks if in scope.
- [ ] Dashboard displays overdue tasks clearly if in scope.
- [ ] Dashboard displays upcoming tasks clearly if in scope.
- [ ] Dashboard displays payment/expense summary if in scope.
- [ ] Dashboard displays reminders/notifications if in scope.
- [ ] Task titles/statuses/due dates are visible.
- [ ] Payment/expense amounts/statuses are visible if included.
- [ ] Counts/totals are labelled clearly.
- [ ] Empty state text is understandable.
- [ ] Error state text is user-friendly and does not expose stack traces.
- [ ] Long names/descriptions/emails do not break layout.
- [ ] UI does not misrepresent historical/completed items as current responsibilities.

## 5. Important Checks — UI Theme and Style

- [ ] UI matches existing WGMS theme.
- [ ] Existing card/table/list styling is reused.
- [ ] Existing spacing and typography are preserved.
- [ ] Buttons/links match existing style.
- [ ] Badges/status labels match existing app style.
- [ ] No external UI library is introduced.
- [ ] No unrelated global CSS changes are introduced.
- [ ] Page works on desktop.
- [ ] Page works on mobile widths.
- [ ] Long text wraps/truncates safely.

## 6. Critical Checks — Frontend Tests Exist

- [ ] Component creation test exists.
- [ ] Component calls service on load.
- [ ] Loading state test exists.
- [ ] Error state test exists.
- [ ] Empty/no-responsibilities state test exists.
- [ ] Populated rendering test exists.
- [ ] Test covers current/open task responsibility rendering.
- [ ] Test covers overdue/upcoming task rendering if implemented.
- [ ] Test covers payment/expense summary rendering if implemented.
- [ ] Test covers reminder/notification rendering if implemented.
- [ ] Test covers optional/null fields.
- [ ] Service test exists.
- [ ] Service test verifies endpoint URL.
- [ ] Service test verifies no household id is sent.
- [ ] No frontend tests are skipped or weakened.

Command:

```bash
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
```

## 7. Critical Checks — E2E Tests Exist

- [ ] Cypress/e2e spec exists for FR-12.
- [ ] E2E user can log in.
- [ ] E2E user can navigate to dashboard.
- [ ] E2E creates or seeds relevant synthetic data.
- [ ] E2E verifies current responsibility data is visible.
- [ ] E2E verifies payment/expense summary is visible if in scope.
- [ ] E2E verifies empty/no-responsibilities state if possible.
- [ ] E2E verifies no page crash.
- [ ] E2E does not depend on production data.

Command:

```bash
cd e2e && npx cypress run --spec cypress/e2e/dashboard.cy.js
```

## 8. Regression Checks

- [ ] Existing login flow still works.
- [ ] Existing sidebar/navigation still works.
- [ ] Existing task page still works.
- [ ] Existing cleaning schedule page still works.
- [ ] Existing expense/payment page still works.
- [ ] Existing shopping page still works.
- [ ] Existing frontend tests pass.
- [ ] Frontend build passes.
- [ ] Lint passes if configured.
- [ ] No fake production data remains.

## 9. Frontend Merge Blockers

- [ ] Verified blocker not present: dashboard is unreachable.
- [ ] Verified blocker not present: frontend sends household id.
- [ ] Verified blocker not present: fake production data is shown.
- [ ] Verified blocker not present: empty/no-responsibilities state crashes page.
- [ ] Verified blocker not present: backend error crashes page.
- [ ] Verified blocker not present: normal null fields render ugly values.
- [ ] Verified blocker not present: UI style is inconsistent with app.
- [ ] Verified blocker not present: frontend tests are missing.
- [ ] Verified blocker not present: e2e test is missing without justification.
- [ ] Verified blocker not present: build fails.

## 10. Frontend Final Approval

- [ ] Route/navigation verified.
- [ ] Service/DTO contract verified.
- [ ] Loading/error/empty states verified.
- [ ] Dashboard rendering verified.
- [ ] Frontend tests exist and pass.
- [ ] E2E tests exist and pass, or absence is explicitly justified.
- [ ] Build/lint pass.
- [ ] Known limitations documented.
- [ ] Frontend ready for merge review.
