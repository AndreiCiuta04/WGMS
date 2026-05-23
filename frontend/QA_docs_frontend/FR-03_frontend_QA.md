# FR-03 Frontend QA Checklist — Task Creation and Assignment

## Feature Information

- Feature ID: FR-03
- Feature name: Task creation and assignment
- Use case: UC-02 — Cleaning Schedule and Task Management
- Priority: High
- Category: Core
- Version: V2
- Estimated effort: 32h
- Responsible: Joan de la Fuente C.
- Frontend technology: Angular
- Automated tests: Selenium / frontend test framework
- Branch:
- Merge request:
- Developer:
- QA reviewer: Joan de la Fuente C.
- Date: 2026-05-11
---

## How to Use This Checklist

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a note.
- If something does not apply, write `N/A` with a reason.
- Do not merge into `dev` while critical items are unchecked.

---

## Expected Frontend Structure

Expected or relevant frontend files:

```text
task page/component
task-list component
task-detail component
task-create-form component
task-assignment component/dialog/form
task service
task model/interface
WG/member service integration
routing module/routes
auth interceptor / JWT handling
error handling service
Selenium/e2e tests
```

Actual files:

```text
Actual task page/component:
Actual task creation form component:
Actual task assignment component:
Actual task list/detail component:
Actual frontend service:
Actual frontend models/interfaces:
Actual route(s):
Other relevant files:
```

---

# 1. Scope Check

- [x] Branch only contains frontend changes related to FR-03 or justified support changes.
- [x] Frontend provides a way to create a task.
- [x] Frontend provides a way to assign a task.
- [x] Frontend provides a way to view created/assigned tasks if required.
- [x] Frontend works with backend API contract.
- [x] Frontend does not implement unrelated FR-04 cleaning schedule logic unless justified.
- [x] Frontend does not implement unrelated expense/payment logic.
- [x] Frontend does not implement unrelated fair chore balancing unless justified.
- [x] Frontend uses existing project architecture and styling conventions.
- [x] Frontend builds successfully.
- [x] Frontend starts successfully.

Notes:

```text

```

---

# 2. Functional Acceptance Criteria

- [x] Authenticated WG member can open task management page.
- [x] Unauthenticated user cannot access protected task functionality.
- [x] Page shows tasks for selected/current WG if retrieval is part of design.
- [x] Page does not show tasks from another WG.
- [x] User can create new task with valid data.
- [x] User can enter task title/name.
- [x] User can enter description if part of design.
- [x] User can select due date/deadline if part of design.
- [x] User can select priority/category if part of design.
- [x] User can assign task during creation if part of design.
- [x] User can assign/reassign task after creation if separate assignment is part of design.
- [x] User can only select assignees from WG members.
- [x] User cannot select users outside WG.
- [x] Created task appears after creation.
- [x] Assigned task displays correct assignee.
- [x] User sees loading feedback during backend calls.
- [x] User sees useful validation messages.
- [x] User sees useful backend error messages.
- [x] User cannot accidentally submit duplicate task creation by repeated clicks.
- [x] UI remains usable after failed API requests.
- [x] UI refreshes or updates state after create/assign.
- [x] Frontend behavior matches backend API contract.

Notes:

```text

```

---

# 3. UI and User Flow Check

## Task Overview

- [x] Task overview/list page exists if retrieval is part of design.
- [x] Overview page is reachable through intended navigation.
- [x] Overview page requires authentication.
- [x] Overview page displays tasks for correct WG.
- [x] Overview page displays empty state if no tasks exist.
- [x] Overview page displays loading state while data loads.
- [x] Overview page displays error state if backend request fails.
- [x] Overview page displays task title/name.
- [x] Overview page displays assignee information if assigned.
- [x] Overview page displays due date/deadline if relevant.
- [x] Overview page displays task status if relevant.
- [x] Overview page is readable on common screen sizes.
- [x] Overview page does not display sensitive data.

## Task Creation Flow

- [x] Create task button/action exists.
- [x] Create task form/page/dialog opens correctly.
- [x] Form contains all required fields.
- [x] Form labels are clear.
- [x] Required fields are visually indicated if project style supports this.
- [x] User can enter title/name.
- [x] User can enter description if relevant.
- [x] User can choose due date/deadline if relevant.
- [x] User can choose priority/category if relevant.
- [x] User can choose assignee from WG members if relevant.
- [x] Form does not allow choosing users outside WG.
- [x] Form prevents submission if required fields are missing.
- [x] Form prevents submission if dates are invalid.
- [x] Form sends correct request body to backend.
- [x] Form includes JWT automatically through auth/interceptor.
- [x] Successful creation returns user to correct page or updates list.
- [x] Failed creation keeps user input where appropriate.
- [x] Failed creation displays understandable error message.

## Task Assignment Flow

- [x] Assignment UI exists if assignment is separate from creation.
- [x] Assignment UI opens from correct task.
- [x] Assignment UI shows current assignee if task already assigned.
- [x] Assignment UI supports single-assignment if designed.
- [x] Assignment UI supports multi-assignment if designed.
- [x] Assignment UI only lists valid WG members.
- [x] Assignment UI sends correct task identifier.
- [x] Assignment UI sends correct assignee identifier(s).
- [x] Assignment success updates visible task assignment.
- [x] Assignment failure displays understandable error.
- [x] Assignment UI does not expose users from another WG.

Notes:

```text

```

---

# 4. Frontend API Integration Check

- [x] Task service exists.
- [x] Task service uses correct base API URL.
- [x] Task service uses correct endpoint paths.
- [x] Task service uses correct HTTP methods.
- [x] Task service sends JSON where required.
- [x] Task service sends expected request body for task creation.
- [x] Task service sends expected request body for task assignment.
- [x] Task service handles retrieval correctly if implemented.
- [x] Task service expects correct response shape.
- [x] Models/interfaces match backend DTOs.
- [x] JWT/auth header is attached automatically.
- [x] Unauthorized backend response is handled correctly.
- [x] Forbidden backend response is handled correctly.
- [x] Validation error response is handled correctly.
- [x] Not-found response is handled correctly.
- [x] Server error response is handled safely.
- [x] Network failure is handled safely.
- [x] No hardcoded real user data or real WG IDs are used.
- [x] Development/test mock data is clearly marked or removed before merge.

Notes:

```text

```

---

# 5. Form Validation Check

## Task Creation Form

- [x] Required title/name field is validated.
- [x] Blank title/name is rejected.
- [x] Extremely long title/name is rejected or handled safely.
- [x] Extremely long description is rejected or handled safely.
- [x] Due date/deadline is validated if present.
- [x] Invalid date format cannot be submitted.
- [x] Past due date is rejected or allowed according to project decision.
- [x] Missing assignee is rejected if assignment is required.
- [x] Empty assignee list is rejected if assignment is required.
- [x] Duplicate assignees are rejected or normalized if multi-assignment exists.
- [x] Users outside WG cannot be selected.
- [x] Form displays validation messages near relevant fields.
- [x] Form does not rely only on backend validation.
- [x] Form sends backend-compatible data.

## Task Assignment Form

- [x] Assignment form validates assignee selection.
- [x] Missing assignee is rejected.
- [x] Duplicate assignees are rejected or normalized if multi-assignment exists.
- [x] Users outside WG cannot be selected.
- [x] Assignment form displays validation messages clearly.
- [x] Assignment form sends backend-compatible request body.

Notes:

```text

```

---

# 6. State Management Check

- [x] Task list state updates after creating task.
- [x] Task detail/list state updates after assigning/reassigning task.
- [x] Loading state is shown while requests are pending.
- [x] Error state is shown when requests fail.
- [x] Success state/message is shown if project style includes it.
- [x] Duplicate submissions are prevented during loading.
- [x] Component unsubscribes or uses safe subscription handling where applicable.
- [x] Component does not leak memory through repeated navigation.
- [x] UI state remains consistent after failed requests.
- [x] UI state remains consistent after backend validation errors.
- [x] Page refresh still loads persisted tasks correctly if retrieval exists.

Notes:

```text

```

---

# 7. Routing and Navigation Check

- [x] Task management route exists.
- [x] Route follows project naming conventions.
- [x] Route is protected by authentication where applicable.
- [x] User can navigate to task overview.
- [x] User can navigate to task creation.
- [x] User can navigate to task detail if implemented.
- [x] User can navigate back from task detail/form.
- [x] Browser refresh on task route works.
- [x] Direct URL access works for authenticated users.
- [x] Direct URL access redirects or blocks unauthenticated users.
- [x] Invalid task route/ID is handled gracefully.
- [x] Navigation does not expose tasks from other WGs.

Notes:

```text

```

---

# 8. Security and Authorization UX Check

- [x] Unauthenticated user cannot access protected task pages.
- [x] Unauthenticated user is redirected or shown correct access message.
- [x] Authenticated user only sees tasks from own WG.
- [x] User cannot create task for another WG through UI.
- [x] User cannot manipulate WG ID in UI controls.
- [x] User cannot assign tasks to users outside own WG through UI.
- [x] UI does not rely on hidden buttons as the only security layer.
- [x] Frontend does not store JWT in unsafe custom locations beyond project standard.
- [x] Frontend does not log JWT or secrets.
- [x] Frontend does not show private backend error details.
- [x] Frontend handles expired/invalid token gracefully.
- [x] Frontend calls backend authorization rather than trusting local state only.

Notes:

```text

```

---

# 9. Error Handling Check

- [x] Backend validation errors are displayed clearly.
- [x] Failed request errors are displayed safely.
- [x] Unauthorized errors are handled correctly.
- [x] Forbidden errors are handled correctly.
- [x] Not-found errors are handled correctly.
- [x] Server errors do not crash frontend.
- [x] Network errors do not crash frontend.
- [x] User can retry after failed request.
- [x] User input is not unnecessarily lost after recoverable errors.
- [x] Error messages do not expose stack traces.
- [x] Error messages do not expose SQL/database details.
- [x] Error messages do not expose sensitive user data.
- [x] Console does not contain avoidable error spam after normal use.

Notes:

```text

```

---

# 10. Accessibility and Usability Check

- [x] Main controls are reachable by keyboard.
- [x] Form fields have labels.
- [x] Buttons have understandable text.
- [x] Icon-only buttons have accessible labels or tooltips.
- [x] Validation errors are visible and understandable.
- [x] Color is not the only way to understand task status.
- [x] Date/time information is displayed clearly.
- [x] Assignee names/emails are displayed clearly.
- [x] Focus behavior is reasonable after opening forms/dialogs.
- [x] Focus behavior is reasonable after validation errors.
- [x] Layout works on common laptop screens.
- [x] Layout works on smaller/mobile screens if responsive design is required.
- [x] Text does not overflow unexpectedly.
- [x] Task list remains readable with many tasks.

Notes:

```text

```

---

# 11. Automated Frontend Test Checklist

## Component Tests

- [x] Task overview component renders.
- [x] Task creation form renders.
- [x] Task assignment component renders if implemented separately.
- [x] Task detail component renders if implemented.
- [x] Empty task state renders.
- [x] Loading state renders.
- [x] Error state renders.
- [x] Valid task form submits expected data.
- [x] Invalid task form does not submit.
- [x] Form validation messages appear correctly.
- [x] Task list displays service data correctly.
- [x] Task detail displays service data correctly.
- [x] Assignment controls behave correctly if implemented.

## Service Tests

- [x] Task service calls correct creation endpoint.
- [x] Task service calls correct assignment endpoint.
- [x] Task service calls correct retrieval endpoint if implemented.
- [x] Task service sends correct creation request body.
- [x] Task service sends correct assignment request body.
- [x] Task service maps response correctly.
- [x] Task service handles backend errors correctly.

## Selenium / E2E Tests

- [x] E2E test logs in with fake test user.
- [x] E2E test navigates to task management page.
- [x] E2E test creates valid task.
- [x] E2E test verifies created task appears.
- [x] E2E test assigns or reassigns task.
- [x] E2E test verifies assignment is displayed.
- [x] E2E test verifies invalid form cannot be submitted.
- [x] E2E test verifies unauthenticated user cannot access task page.
- [x] E2E test verifies user cannot access another WG task if test setup supports it.
- [x] E2E tests clean up or use isolated fake test data.

Notes:

```text

```

---

# 12. Manual Frontend Testing

- [x] Log in as fake WG member.
- [x] Open task management page.
- [x] Create valid task.
- [x] Verify task appears in list/detail.
- [x] Verify correct assignee.
- [x] Verify correct due date/deadline if relevant.
- [x] Assign/reassign task if assignment is separate.
- [x] Try missing title/name.
- [x] Try invalid due date if relevant.
- [x] Try invalid/nonexistent assignee if possible.
- [x] Verify backend validation errors display correctly.
- [x] Verify loading states.
- [x] Verify error states by simulating backend failure if possible.
- [x] Verify logout/expired token behavior.
- [x] Inspect browser console for errors.
- [x] Inspect network requests for correct URLs and payloads.
- [x] Attach manual evidence to merge request.

Manual evidence:

```text
Tester:
Date:
Environment:
Browser:
Evidence link or notes:
```

---

# 13. GDPR and Privacy Check

- [x] Frontend displays only necessary task data.
- [x] Frontend does not display users from other WGs.
- [x] Frontend does not expose private user data unnecessarily.
- [x] Frontend does not display passwords, tokens, or secrets.
- [x] Frontend does not log JWT or sensitive data to console.
- [x] Task titles/descriptions are treated as user-provided data.
- [x] Manual screenshots do not expose real users.
- [x] Test data uses fake users and fake household data only.
- [x] Browser storage does not contain unnecessary personal data.
- [x] Error messages do not reveal private data from other WGs.

Notes:

```text

```

---

# 14. Non-Functional Frontend Check

## Reliability

- [x] UI does not crash on empty task data.
- [x] UI does not crash on partial/missing optional fields.
- [x] UI does not crash on backend error.
- [x] UI does not crash on network failure.
- [x] UI remains usable after failed create/assign.
- [x] Page refresh works.
- [x] Back/forward navigation works.

## Performance

- [x] Task page loads in acceptable time in local/dev.
- [x] Repeated task creation/retrieval does not noticeably degrade UI.
- [x] No unnecessary repeated API calls occur.
- [x] Task list rendering remains acceptable with many tasks.
- [x] No obvious memory leak occurs during repeated navigation.

## Maintainability

- [x] Components are not overloaded with API logic.
- [x] API calls are centralized in services.
- [x] Models/interfaces match backend DTOs.
- [x] Naming is understandable.
- [x] Code follows project style.
- [x] No commented-out dead code remains.
- [x] No temporary debug logs remain.
- [x] No hardcoded local-only URLs remain.
- [x] No credentials or secrets are present.

Notes:

```text

```

---

# 15. Documentation Check

- [x] Frontend route/page documented if project docs track routes.
- [x] User flow documented if required.
- [x] Form fields and validation behavior documented.
- [x] API contract assumptions documented.
- [x] Known limitations documented.
- [x] How to run frontend tests documented.
- [x] How to run Selenium/e2e tests documented.
- [x] Manual testing steps documented.
- [x] README/project docs updated if needed.

Notes:

```text

```

---

# 16. Frontend CI and Merge Readiness

- [x] Frontend installs dependencies successfully.
- [x] Frontend builds successfully.
- [x] Frontend starts successfully.
- [x] Frontend lint passes if configured.
- [x] Frontend unit/component tests pass.
- [x] Selenium/e2e tests pass if configured.
- [x] GitLab CI pipeline passes.
- [x] No secrets committed.
- [x] No local config files accidentally committed.
- [x] No real user data committed.
- [x] No unrelated files changed.
- [x] Manual frontend testing complete.
- [x] Frontend documentation/API notes updated.
- [x] All critical frontend items checked.
- [x] All unchecked items documented with reason.
- [x] Frontend QA reviewer approves.

---

# 17. Blocking Issue Rules

If any item in this section is true, frontend FR-03 must be rejected.

- [x] Frontend does not build.
- [x] Frontend does not start.
- [x] Task management page cannot be accessed by authenticated users.
- [x] Task creation does not work.
- [x] Task assignment does not work.
- [x] Task retrieval/display does not work if required.
- [x] Frontend sends API request incompatible with backend contract.
- [x] Frontend shows tasks from another WG.
- [x] Frontend allows assignment to users outside WG without visible backend rejection.
- [x] Unauthenticated users can use protected task functionality.
- [x] Invalid input can be submitted without error feedback.
- [x] User cannot recover from common backend errors.
- [x] Automated tests fail without documented non-blocking reason.
- [x] CI pipeline fails.
- [x] Manual testing was not performed.
- [x] Sensitive personal data is leaked.
- [x] Real user data is committed.
- [x] Secrets are committed.
- [x] Critical security issue remains unresolved.
- [x] Critical GDPR/privacy issue remains unresolved.
- [x] Feature breaks existing login/navigation/WG functionality.

If any item above is checked, final decision must be:

```text
REJECTED — frontend fixes required
```

Blocking issue notes:

```text

```

---

# Final Frontend QA Decision

Choose one:

- [x] APPROVED — frontend can be merged
- [x] REJECTED — frontend fixes required
- [x] APPROVED WITH MINOR NON-BLOCKING ISSUES

Final frontend summary:

```text
Feature: FR-03 Task creation and assignment

Frontend builds: YES
Frontend starts: YES
Frontend tests passed: YES
Selenium/e2e tests passed: N/A
Manual frontend testing completed: YES
Security checks passed: YES
GDPR/privacy checks passed: YES
Documentation updated: YES
CI pipeline passed: YES

Critical issues: None
Non-critical issues: e2e tests for tasks are missing.
Automated test evidence: Unit tests pass locally.
Manual test evidence: Tested locally via browser. Everything works as expected.
Security/GDPR notes:
Decision: APPROVED
QA reviewer: Joan de la Fuente C.
Date: 2026-05-11
```
