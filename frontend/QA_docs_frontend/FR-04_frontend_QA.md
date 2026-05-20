# FR-04 Frontend QA Checklist — Cleaning Schedule Management

## Feature Information

- Feature ID: FR-04
- Feature name: Cleaning schedule management
- Use case: UC-02 — Cleaning Schedule and Task Management
- Priority: High
- Category: Core
- Version: V2
- Estimated effort: 32h
- Feature lead: Joan de la Fuente C.
- Frontend technology: Angular
- Backend dependency: Java Spring Boot REST API
- Automated tests: Cypress / frontend unit tests where available
- Branch: `FR-04/Cleaning-schedule-management`
- Merge request: To be added in GitLab
- Developer: Joan de la Fuente C.
- QA reviewer: Andrei Ciuta
- Date: 2026-05-20

---

## How to Use This Checklist

This checklist must be completed before the frontend part of FR-04 can be considered done.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a short note under the relevant section.
- If something does not apply, write `N/A` with a short reason.
- Do not merge into `dev` while critical items are unchecked.

This checklist is written so that it can be given to an AI assistant or a human reviewer to verify the frontend implementation systematically.

---

## Project-Specific Frontend Scope

FR-04 belongs to UC-02: Cleaning Schedule and Task Management.

The frontend must allow authenticated WG members to view and manage the household cleaning schedule. The user should be able to understand which cleaning tasks are open, who is responsible, when they are due, and whether they are completed. If the feature supports editing or schedule generation, those workflows must be usable without external explanation.

Important branch-specific note: this branch contains a **new UI**. QA must therefore check not only the FR-04 feature behavior, but also whether the new UI still preserves the existing core workflows, navigation, readability, responsiveness, and integration with login/WG context.

FR-04 is **cleaning schedule management**, not only the basic task form from FR-03. FR-03 covers task creation and assignment. FR-04 must verify the schedule-specific UI: schedule overview, due dates, assigned roommates, status, recurrence/rotation display where implemented, and completion tracking.

Expected frontend areas to inspect:

- Angular routes for cleaning schedule / tasks.
- Cleaning schedule page/component.
- Schedule list, calendar, board, or table view.
- Create/edit schedule form if implemented.
- Completion/status controls.
- API service for schedule requests.
- Models/interfaces/types for schedule entries.
- Validation and error handling.
- Authentication guard integration.
- Navigation/sidebar/menu integration.
- New UI layout and styling.
- Responsive layout.
- E2E tests or component tests.

---

## 1. Branch, Build, and Baseline Checks

- [x] Correct branch is checked out: `FR-04/Cleaning-schedule-management` or equivalent.
- [x] Branch is up to date with `origin`.
- [x] Frontend installs dependencies successfully.
- [x] Frontend builds successfully.
- [x] Frontend starts successfully.
- [x] Existing frontend tests still pass.
- [x] Existing E2E tests still pass or failures are documented.
- [x] No unrelated frontend files were changed without explanation.
- [x] No API keys, secrets, credentials, JWTs, or personal test data are committed.
- [x] FR-04 does not break login, registration, WG creation/invitation, or basic task creation.
- [x] New UI changes do not hide or remove existing required workflows.

Suggested commands:

```bash
git branch --show-current
git status
git diff --name-status dev...HEAD

cd frontend
npm install
npm run build
npm test
npx cypress run
```

Use the actual frontend scripts from `package.json` if different.

Notes:

- Branch checked: `FR-04/Cleaning-schedule-management`.
- Frontend dependencies were installed successfully with `npm install`.
- Frontend build passed with `npm run build`.
- Frontend starts on port 4200.
- Backend starts on port 8080 and is used by the frontend.
- Manual UI smoke testing was completed in a live demo.
- Frontend unit/component tests passed.
- E2E setup was repaired by installing the missing Cypress dependencies.
- The old Cypress `addMessage.cy.js` issue was investigated and documented; FR-04-specific Cypress integration coverage was added and passed.
- No credentials, JWTs, API keys, or personal test data are intentionally committed.
- The branch includes a new UI, but the live demo confirmed that the main existing workflows remain reachable and usable.

---

## 2. Requirement Coverage

The frontend must support the user-facing cleaning schedule workflow.

- [x] Logged-in user can find the cleaning schedule area from navigation.
- [x] Cleaning schedule page exists.
- [x] Empty schedule has a clear empty state.
- [x] Existing scheduled cleaning duties are displayed clearly.
- [x] User can see each duty/task name.
- [x] User can see the assigned roommate.
- [x] User can see the due date or scheduled date.
- [x] User can see the current status, e.g. open, done, overdue.
- [x] User can create or configure schedule entries if this is in scope.
- [x] User can edit schedule entries if edit is in scope.
- [x] User can mark a cleaning duty as completed.
- [x] Completed items update visually after action.
- [x] User can cancel/delete schedule entries if delete is in scope.
- [x] User can understand recurrence/rotation information if implemented.
- [x] User can distinguish current/open items from past/completed items.
- [x] Frontend does not show data from another WG.
- [x] Frontend handles backend errors without crashing.

Notes:

- The cleaning schedule page is reachable through the authenticated frontend navigation.
- The list/overview separates current or due items from upcoming schedule entries.
- The UI shows schedule title, assigned roommate, due date / next due date, active status, and recurrence/frequency where available.
- FR-04 schedules are recurring templates. Completion itself is handled through generated concrete FR-03 tasks, not directly on the schedule template.
- The generated task workflow was tested and confirmed as the completion/status integration path.
- Rotation queue logic is not implemented. Recurrence/frequency is implemented and visible.
- Household isolation was tested through Cypress integration tests, so a user from another WG cannot see the schedule data.
- Backend errors are handled without crashing the frontend.

---

## 3. New UI Review

Because this branch includes a new UI, check the following carefully.

- [x] New UI is consistent across login, dashboard, tasks, and cleaning schedule pages.
- [x] New navigation makes the cleaning schedule easy to find.
- [x] Page headings clearly describe where the user is.
- [x] Buttons use clear labels, not only icons.
- [x] Icons are understandable or supported by text/tooltips.
- [x] Forms are visually grouped and not confusing.
- [x] Important actions such as complete/delete are visually distinct.
- [x] New colors have sufficient contrast.
- [x] Text is readable on desktop and mobile.
- [x] Cards/tables/lists do not overflow the viewport.
- [x] Loading states are visible.
- [x] Empty states are visible.
- [x] Error states are visible.
- [x] Success states are visible.
- [x] The UI does not rely on hidden hover-only actions for mobile users.
- [x] The UI does not create duplicate navigation entries for the same feature.
- [x] The UI does not leave broken or placeholder buttons.
- [x] The UI does not show raw backend field names to users.

Notes:

- The new UI was reviewed during a live demo.
- Login, dashboard/navigation, cleaning schedules, task management, and household-related flows were checked manually.
- The UI was accepted as-is.
- No style/layout changes were requested during this QA pass.
- Buttons and icons were understandable in the demo context.
- No broken placeholder controls or raw backend field names were observed.

---

## 4. Cleaning Schedule List / Overview

- [x] Schedule list loads after login.
- [x] Schedule list shows only current household entries.
- [x] Schedule list has a sensible order, e.g. due soon first.
- [x] Each entry shows enough information to act on it.
- [x] Long task names do not break the layout.
- [x] Missing optional values are displayed gracefully.
- [x] Overdue items are recognizable if backend supports overdue state.
- [x] Completed items are recognizable.
- [x] Empty state explains what the user can do next.
- [x] Refreshing the page keeps the correct data visible.
- [x] Browser back/forward navigation does not break the page.

Notes:

- The overview was manually tested in the live demo.
- The list is loaded from the backend API, not from hardcoded demo data.
- Current/due and upcoming schedule entries are separated clearly.
- Each schedule entry gives enough information to act on it: title, assignee, frequency, due/next due date, and active status.
- Household scoping was verified by Cypress integration tests.
- The backend does not expose a separate explicit `overdue` field; overdue/current state is inferred from date/status where applicable.
- Completed state belongs to generated concrete tasks, not directly to the schedule template.

---

## 5. Create / Edit Schedule UI

Complete this section if create/edit is implemented in the frontend.

- [x] User can open the create schedule form from the schedule page.
- [x] Form fields are labeled clearly.
- [x] Required fields are visually identifiable.
- [x] User can enter duty/task name.
- [x] User can select assignee from household roommates.
- [x] User can choose due date/date range.
- [x] User can choose recurrence/rotation settings if implemented.
- [x] User cannot select roommates from other households.
- [x] Client-side validation catches obvious missing/invalid fields.
- [x] Backend validation errors are displayed clearly.
- [x] Save/submit button is disabled or protected against duplicate clicks.
- [x] Successful creation returns the user to a useful state.
- [x] Created/updated schedule appears without requiring confusing manual steps.
- [x] Cancel/back action does not accidentally submit changes.

Notes:

- Create and edit schedule forms are implemented.
- Required fields are validated.
- The user can enter the schedule title/duty name, description, assignee, start/end date where applicable, and frequency.
- Assignee selection uses household members and does not expose other household members.
- Recurrence/frequency is implemented. Rotation queue logic is not implemented and is outside the currently implemented scope.
- Backend validation errors are shown to the user.
- Successful create/update returns the user to a useful state and the saved schedule is visible afterward.
- Cancel/back navigation does not submit unintended changes.

---

## 6. Completion / Status UI

- [x] User can mark an open cleaning duty as completed if allowed.
- [x] Completion control is easy to find.
- [x] User gets clear feedback after completion.
- [x] Completed item status updates immediately or after a clear reload.
- [x] Completing an item twice does not create duplicate visual entries.
- [x] User cannot complete another household’s task through the UI.
- [x] Reopen/undo is visible if implemented.
- [x] Skip/reschedule is visible if implemented.
- [x] Dangerous or irreversible actions ask for confirmation if needed.
- [x] Error during completion is shown clearly and does not fake success.

Notes:

- FR-04 schedules are recurring schedule templates.
- The schedule itself is not directly completed. Instead, the user generates a concrete task from the schedule.
- Completion/status handling happens through the FR-03 task workflow.
- Cypress integration verifies that a generated task is created from a cleaning schedule and linked back through `cleaningScheduleId`.
- Completing a generated cleaning task belongs to task management and does not create duplicate schedule entries.
- Reopen/undo and skip/reschedule are not separate FR-04 schedule controls in the current implementation.
- Deleting a schedule asks for confirmation.
- The backend prevents deleting schedules that already generated tasks, and this is covered by Cypress integration tests.

---

## 7. API Integration Checks

- [x] Frontend calls the correct backend endpoint for schedule list.
- [x] Frontend calls the correct backend endpoint for create/update/delete/complete actions.
- [x] JWT/auth token is included where required.
- [x] Frontend does not hardcode demo data instead of using the API.
- [x] Frontend does not hardcode user IDs, household IDs, or assignee IDs.
- [x] Frontend models/interfaces match backend DTOs.
- [x] Loading states are shown during API calls.
- [x] API errors are handled with user-readable messages.
- [x] `401 Unauthorized` leads to login/session handling.
- [x] `403 Forbidden` is shown as permission issue.
- [x] `404 Not Found` is handled gracefully.

Suggested checks:

```bash
grep -R "schedule\|cleaning\|task" frontend/src/app -n | head -100
grep -R "HttpClient\|http\." frontend/src/app -n | grep -i "schedule\|cleaning\|task" || true
```

Notes:

- The frontend uses `CleaningScheduleService` for backend communication.
- The service calls the cleaning schedule API for list, active list, detail, create, update, active/inactive toggle, task generation, and delete.
- The generated task workflow confirms integration between FR-04 cleaning schedules and FR-03 task management.
- Cypress integration tests verified create, list, update, pause/resume, delete, generate-task, delete protection, household isolation, and unauthenticated access.
- The frontend does not rely on hardcoded household IDs, user IDs, or demo schedule data.
- DTO/interfaces match the backend fields used by the API.
- User-readable errors are shown where relevant and the frontend does not crash on backend errors.

---

## 8. Authentication and Route Guards

- [x] Logged-out user cannot access cleaning schedule page.
- [x] Logged-out user is redirected to login or shown a safe error.
- [x] Logged-in user can access their own WG schedule.
- [x] User without a WG gets a sensible message instead of a crash.
- [x] User from WG A cannot see WG B data.
- [x] UI does not expose admin-only controls to users who should not have them.
- [x] Backend still enforces restrictions even if frontend hides buttons.

Notes:

- Cleaning schedule routes are guarded by the Angular authentication guard.
- Logged-out access was checked and rejected safely.
- Cypress integration verifies unauthenticated API access is rejected.
- Cypress integration verifies household/WG isolation by creating users in separate households and confirming schedules are not shared across households.
- Backend authorization remains the source of truth even if the frontend hides or disables controls.
- No admin-only controls were exposed in the FR-04 UI.

---

## 9. Frontend Validation and Usability

- [x] Required fields are validated before submit.
- [x] Invalid dates are rejected or explained.
- [x] Invalid recurrence choices are rejected or explained.
- [x] Validation messages are close to the relevant fields.
- [x] Form reset/cancel behavior is predictable.
- [x] Keyboard navigation works for main actions.
- [x] The page can be used without reading external documentation.
- [x] A peer tester can complete the main workflow in under a few minutes.
- [x] Button labels and messages use consistent project language.
- [x] No raw JSON or stack traces are shown to the user.

Notes:

- Required form fields are validated on the frontend.
- Backend validation provides additional protection for business rules and invalid date ranges.
- Validation and error messages are understandable and close enough to the relevant workflow.
- The live demo confirmed that the workflow can be completed without external explanation.
- Button labels and messages are consistent with the rest of the project UI.
- No raw JSON, Java stack traces, or backend exception dumps were shown in the UI.

---

## 10. Responsive and Browser Checks

Check especially because the branch contains a new UI.

- [x] Desktop layout works at 1366x768.
- [x] Wide desktop layout does not stretch content awkwardly.
- [x] Mobile layout works at approximately 390x844.
- [x] Mobile layout works at minimum 360px width.
- [x] Navigation is usable on mobile.
- [x] Forms are usable on mobile.
- [x] Buttons are large enough to tap on mobile.
- [x] Tables/lists/cards do not require impossible horizontal scrolling.
- [x] Text remains readable on mobile.
- [x] Chrome smoke test passes.
- [x] Firefox smoke test passes where possible.
- [x] Safari/iOS behavior is checked where possible or documented.

Suggested manual check:

```text
Chrome DevTools -> Toggle device toolbar -> test 390x844 and 360px width.
```

Notes:

- Responsive behavior was checked during the live demo.
- Desktop and mobile layouts were accepted.
- Navigation and forms remain usable on mobile.
- Chrome smoke testing passed.
- Firefox behavior was checked where possible.
- Safari/iOS was not available in the Linux local test environment; no Safari-specific issue was observed or reported, so this is documented as not separately tested on native Safari.

---

## 11. Automated Frontend / E2E Tests

- [x] Frontend unit/component tests exist where project style supports them.
- [x] E2E test exists for opening the cleaning schedule page, or this gap is documented.
- [x] E2E test exists for completing a cleaning duty, or this gap is documented.
- [x] E2E test exists for creating/editing schedule if implemented, or this gap is documented.
- [x] Existing Cypress login helpers still work with the new UI.
- [x] Tests do not rely on fragile text or layout assumptions unless unavoidable.
- [x] Tests use stable selectors where possible.
- [x] Test failures are investigated and not ignored silently.

Suggested commands:

```bash
cd frontend
npm test
npx cypress run
```

Notes:

- Frontend unit/component tests exist and were reported as passing.
- Cypress dependencies were installed with `npm install` inside the `e2e` project.
- Added FR-04 Cypress integration spec: `e2e/cypress/e2e/cleaningScheduleIntegration.cy.js`.
- The FR-04 Cypress integration spec passed.
- The new spec covers:
  - user registration/login integration,
  - household creation integration,
  - cleaning schedule creation,
  - schedule listing,
  - schedule update,
  - pause/resume through active flag,
  - deletion of empty schedules,
  - generated task creation from schedules,
  - generated task linkage through `cleaningScheduleId`,
  - delete protection for schedules with generated tasks,
  - household isolation,
  - unauthenticated access rejection.
- Completion is documented through generated FR-03 tasks, because the schedule template itself is not directly completed.
- Earlier Cypress failures were investigated and were not ignored silently.

---

## 12. Final Frontend Acceptance

The frontend part of FR-04 can be accepted only if:

- [x] Frontend builds and starts.
- [x] Cleaning schedule page is reachable.
- [x] Schedule entries are displayed correctly.
- [x] Create/edit/complete behavior works according to scope.
- [x] New UI is usable and does not break existing core workflows.
- [x] Authentication and WG context are respected.
- [x] Errors are handled without crashing.
- [x] Desktop and mobile layouts are usable.
- [x] Critical issues are documented before merge.

Final notes:

- FR-04 frontend is accepted from a QA perspective.
- Frontend build/start checks passed.
- Manual live demo confirmed usability of the new UI and the main workflows.
- Automated frontend/unit tests passed.
- Cypress integration tests for FR-04 passed.
- Cleaning schedule management works with backend API integration.
- FR-04 integrates with FR-03 by generating concrete tasks from cleaning schedules.
- Authentication and household/WG isolation are respected.
- Known scope limitation: schedule entries are recurring templates; completion is handled on generated tasks, not directly on the schedule template.
- Known scope limitation: rotation queue is not implemented; recurrence/frequency is implemented.
- Known environment note: native Safari/iOS was not separately tested in the local Linux environment.
- Known project note: moved QA/documentation markdown files may make the Git tree dirty; this is documentation-related and should be reviewed separately before merge.
