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
- Branch:
- Merge request:
- Developer:
- QA reviewer:
- Date:

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

- [ ] Correct branch is checked out: `FR-04/Cleaning-schedule-management` or equivalent.
- [ ] Branch is up to date with `origin`.
- [ ] Frontend installs dependencies successfully.
- [ ] Frontend builds successfully.
- [ ] Frontend starts successfully.
- [ ] Existing frontend tests still pass.
- [ ] Existing E2E tests still pass or failures are documented.
- [ ] No unrelated frontend files were changed without explanation.
- [ ] No API keys, secrets, credentials, JWTs, or personal test data are committed.
- [ ] FR-04 does not break login, registration, WG creation/invitation, or basic task creation.
- [ ] New UI changes do not hide or remove existing required workflows.

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

-

---

## 2. Requirement Coverage

The frontend must support the user-facing cleaning schedule workflow.

- [ ] Logged-in user can find the cleaning schedule area from navigation.
- [ ] Cleaning schedule page exists.
- [ ] Empty schedule has a clear empty state.
- [ ] Existing scheduled cleaning duties are displayed clearly.
- [ ] User can see each duty/task name.
- [ ] User can see the assigned roommate.
- [ ] User can see the due date or scheduled date.
- [ ] User can see the current status, e.g. open, done, overdue.
- [ ] User can create or configure schedule entries if this is in scope.
- [ ] User can edit schedule entries if edit is in scope.
- [ ] User can mark a cleaning duty as completed.
- [ ] Completed items update visually after action.
- [ ] User can cancel/delete schedule entries if delete is in scope.
- [ ] User can understand recurrence/rotation information if implemented.
- [ ] User can distinguish current/open items from past/completed items.
- [ ] Frontend does not show data from another WG.
- [ ] Frontend handles backend errors without crashing.

Notes:

-

---

## 3. New UI Review

Because this branch includes a new UI, check the following carefully.

- [ ] New UI is consistent across login, dashboard, tasks, and cleaning schedule pages.
- [ ] New navigation makes the cleaning schedule easy to find.
- [ ] Page headings clearly describe where the user is.
- [ ] Buttons use clear labels, not only icons.
- [ ] Icons are understandable or supported by text/tooltips.
- [ ] Forms are visually grouped and not confusing.
- [ ] Important actions such as complete/delete are visually distinct.
- [ ] New colors have sufficient contrast.
- [ ] Text is readable on desktop and mobile.
- [ ] Cards/tables/lists do not overflow the viewport.
- [ ] Loading states are visible.
- [ ] Empty states are visible.
- [ ] Error states are visible.
- [ ] Success states are visible.
- [ ] The UI does not rely on hidden hover-only actions for mobile users.
- [ ] The UI does not create duplicate navigation entries for the same feature.
- [ ] The UI does not leave broken or placeholder buttons.
- [ ] The UI does not show raw backend field names to users.

Notes:

-

---

## 4. Cleaning Schedule List / Overview

- [ ] Schedule list loads after login.
- [ ] Schedule list shows only current household entries.
- [ ] Schedule list has a sensible order, e.g. due soon first.
- [ ] Each entry shows enough information to act on it.
- [ ] Long task names do not break the layout.
- [ ] Missing optional values are displayed gracefully.
- [ ] Overdue items are recognizable if backend supports overdue state.
- [ ] Completed items are recognizable.
- [ ] Empty state explains what the user can do next.
- [ ] Refreshing the page keeps the correct data visible.
- [ ] Browser back/forward navigation does not break the page.

Notes:

-

---

## 5. Create / Edit Schedule UI

Complete this section if create/edit is implemented in the frontend.

- [ ] User can open the create schedule form from the schedule page.
- [ ] Form fields are labeled clearly.
- [ ] Required fields are visually identifiable.
- [ ] User can enter duty/task name.
- [ ] User can select assignee from household roommates.
- [ ] User can choose due date/date range.
- [ ] User can choose recurrence/rotation settings if implemented.
- [ ] User cannot select roommates from other households.
- [ ] Client-side validation catches obvious missing/invalid fields.
- [ ] Backend validation errors are displayed clearly.
- [ ] Save/submit button is disabled or protected against duplicate clicks.
- [ ] Successful creation returns the user to a useful state.
- [ ] Created/updated schedule appears without requiring confusing manual steps.
- [ ] Cancel/back action does not accidentally submit changes.

Notes:

-

---

## 6. Completion / Status UI

- [ ] User can mark an open cleaning duty as completed if allowed.
- [ ] Completion control is easy to find.
- [ ] User gets clear feedback after completion.
- [ ] Completed item status updates immediately or after a clear reload.
- [ ] Completing an item twice does not create duplicate visual entries.
- [ ] User cannot complete another household’s task through the UI.
- [ ] Reopen/undo is visible if implemented.
- [ ] Skip/reschedule is visible if implemented.
- [ ] Dangerous or irreversible actions ask for confirmation if needed.
- [ ] Error during completion is shown clearly and does not fake success.

Notes:

-

---

## 7. API Integration Checks

- [ ] Frontend calls the correct backend endpoint for schedule list.
- [ ] Frontend calls the correct backend endpoint for create/update/delete/complete actions.
- [ ] JWT/auth token is included where required.
- [ ] Frontend does not hardcode demo data instead of using the API.
- [ ] Frontend does not hardcode user IDs, household IDs, or assignee IDs.
- [ ] Frontend models/interfaces match backend DTOs.
- [ ] Loading states are shown during API calls.
- [ ] API errors are handled with user-readable messages.
- [ ] `401 Unauthorized` leads to login/session handling.
- [ ] `403 Forbidden` is shown as permission issue.
- [ ] `404 Not Found` is handled gracefully.

Suggested checks:

```bash
grep -R "schedule\|cleaning\|task" frontend/src/app -n | head -100
grep -R "HttpClient\|http\." frontend/src/app -n | grep -i "schedule\|cleaning\|task" || true
```

Notes:

-

---

## 8. Authentication and Route Guards

- [ ] Logged-out user cannot access cleaning schedule page.
- [ ] Logged-out user is redirected to login or shown a safe error.
- [ ] Logged-in user can access their own WG schedule.
- [ ] User without a WG gets a sensible message instead of a crash.
- [ ] User from WG A cannot see WG B data.
- [ ] UI does not expose admin-only controls to users who should not have them.
- [ ] Backend still enforces restrictions even if frontend hides buttons.

Notes:

-

---

## 9. Frontend Validation and Usability

- [ ] Required fields are validated before submit.
- [ ] Invalid dates are rejected or explained.
- [ ] Invalid recurrence choices are rejected or explained.
- [ ] Validation messages are close to the relevant fields.
- [ ] Form reset/cancel behavior is predictable.
- [ ] Keyboard navigation works for main actions.
- [ ] The page can be used without reading external documentation.
- [ ] A peer tester can complete the main workflow in under a few minutes.
- [ ] Button labels and messages use consistent project language.
- [ ] No raw JSON or stack traces are shown to the user.

Notes:

-

---

## 10. Responsive and Browser Checks

Check especially because the branch contains a new UI.

- [ ] Desktop layout works at 1366×768.
- [ ] Wide desktop layout does not stretch content awkwardly.
- [ ] Mobile layout works at approximately 390×844.
- [ ] Mobile layout works at minimum 360px width.
- [ ] Navigation is usable on mobile.
- [ ] Forms are usable on mobile.
- [ ] Buttons are large enough to tap on mobile.
- [ ] Tables/lists/cards do not require impossible horizontal scrolling.
- [ ] Text remains readable on mobile.
- [ ] Chrome smoke test passes.
- [ ] Firefox smoke test passes where possible.
- [ ] Safari/iOS behavior is checked where possible or documented.

Suggested manual check:

```text
Chrome DevTools → Toggle device toolbar → test 390×844 and 360px width.
```

Notes:

-

---

## 11. Automated Frontend / E2E Tests

- [ ] Frontend unit/component tests exist where project style supports them.
- [ ] E2E test exists for opening the cleaning schedule page, or this gap is documented.
- [ ] E2E test exists for completing a cleaning duty, or this gap is documented.
- [ ] E2E test exists for creating/editing schedule if implemented, or this gap is documented.
- [ ] Existing Cypress login helpers still work with the new UI.
- [ ] Tests do not rely on fragile text or layout assumptions unless unavoidable.
- [ ] Tests use stable selectors where possible.
- [ ] Test failures are investigated and not ignored silently.

Suggested commands:

```bash
cd frontend
npm test
npx cypress run
```

Notes:

-

---

## 12. Final Frontend Acceptance

The frontend part of FR-04 can be accepted only if:

- [ ] Frontend builds and starts.
- [ ] Cleaning schedule page is reachable.
- [ ] Schedule entries are displayed correctly.
- [ ] Create/edit/complete behavior works according to scope.
- [ ] New UI is usable and does not break existing core workflows.
- [ ] Authentication and WG context are respected.
- [ ] Errors are handled without crashing.
- [ ] Desktop and mobile layouts are usable.
- [ ] Critical issues are documented before merge.

Final notes:

-
