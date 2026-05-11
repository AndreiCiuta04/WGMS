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
- QA reviewer:
- Date:

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

- [ ] Branch only contains frontend changes related to FR-03 or justified support changes.
- [ ] Frontend provides a way to create a task.
- [ ] Frontend provides a way to assign a task.
- [ ] Frontend provides a way to view created/assigned tasks if required.
- [ ] Frontend works with backend API contract.
- [ ] Frontend does not implement unrelated FR-04 cleaning schedule logic unless justified.
- [ ] Frontend does not implement unrelated expense/payment logic.
- [ ] Frontend does not implement unrelated fair chore balancing unless justified.
- [ ] Frontend uses existing project architecture and styling conventions.
- [ ] Frontend builds successfully.
- [ ] Frontend starts successfully.

Notes:

```text

```

---

# 2. Functional Acceptance Criteria

- [ ] Authenticated WG member can open task management page.
- [ ] Unauthenticated user cannot access protected task functionality.
- [ ] Page shows tasks for selected/current WG if retrieval is part of design.
- [ ] Page does not show tasks from another WG.
- [ ] User can create new task with valid data.
- [ ] User can enter task title/name.
- [ ] User can enter description if part of design.
- [ ] User can select due date/deadline if part of design.
- [ ] User can select priority/category if part of design.
- [ ] User can assign task during creation if part of design.
- [ ] User can assign/reassign task after creation if separate assignment is part of design.
- [ ] User can only select assignees from WG members.
- [ ] User cannot select users outside WG.
- [ ] Created task appears after creation.
- [ ] Assigned task displays correct assignee.
- [ ] User sees loading feedback during backend calls.
- [ ] User sees useful validation messages.
- [ ] User sees useful backend error messages.
- [ ] User cannot accidentally submit duplicate task creation by repeated clicks.
- [ ] UI remains usable after failed API requests.
- [ ] UI refreshes or updates state after create/assign.
- [ ] Frontend behavior matches backend API contract.

Notes:

```text

```

---

# 3. UI and User Flow Check

## Task Overview

- [ ] Task overview/list page exists if retrieval is part of design.
- [ ] Overview page is reachable through intended navigation.
- [ ] Overview page requires authentication.
- [ ] Overview page displays tasks for correct WG.
- [ ] Overview page displays empty state if no tasks exist.
- [ ] Overview page displays loading state while data loads.
- [ ] Overview page displays error state if backend request fails.
- [ ] Overview page displays task title/name.
- [ ] Overview page displays assignee information if assigned.
- [ ] Overview page displays due date/deadline if relevant.
- [ ] Overview page displays task status if relevant.
- [ ] Overview page is readable on common screen sizes.
- [ ] Overview page does not display sensitive data.

## Task Creation Flow

- [ ] Create task button/action exists.
- [ ] Create task form/page/dialog opens correctly.
- [ ] Form contains all required fields.
- [ ] Form labels are clear.
- [ ] Required fields are visually indicated if project style supports this.
- [ ] User can enter title/name.
- [ ] User can enter description if relevant.
- [ ] User can choose due date/deadline if relevant.
- [ ] User can choose priority/category if relevant.
- [ ] User can choose assignee from WG members if relevant.
- [ ] Form does not allow choosing users outside WG.
- [ ] Form prevents submission if required fields are missing.
- [ ] Form prevents submission if dates are invalid.
- [ ] Form sends correct request body to backend.
- [ ] Form includes JWT automatically through auth/interceptor.
- [ ] Successful creation returns user to correct page or updates list.
- [ ] Failed creation keeps user input where appropriate.
- [ ] Failed creation displays understandable error message.

## Task Assignment Flow

- [ ] Assignment UI exists if assignment is separate from creation.
- [ ] Assignment UI opens from correct task.
- [ ] Assignment UI shows current assignee if task already assigned.
- [ ] Assignment UI supports single-assignment if designed.
- [ ] Assignment UI supports multi-assignment if designed.
- [ ] Assignment UI only lists valid WG members.
- [ ] Assignment UI sends correct task identifier.
- [ ] Assignment UI sends correct assignee identifier(s).
- [ ] Assignment success updates visible task assignment.
- [ ] Assignment failure displays understandable error.
- [ ] Assignment UI does not expose users from another WG.

Notes:

```text

```

---

# 4. Frontend API Integration Check

- [ ] Task service exists.
- [ ] Task service uses correct base API URL.
- [ ] Task service uses correct endpoint paths.
- [ ] Task service uses correct HTTP methods.
- [ ] Task service sends JSON where required.
- [ ] Task service sends expected request body for task creation.
- [ ] Task service sends expected request body for task assignment.
- [ ] Task service handles retrieval correctly if implemented.
- [ ] Task service expects correct response shape.
- [ ] Models/interfaces match backend DTOs.
- [ ] JWT/auth header is attached automatically.
- [ ] Unauthorized backend response is handled correctly.
- [ ] Forbidden backend response is handled correctly.
- [ ] Validation error response is handled correctly.
- [ ] Not-found response is handled correctly.
- [ ] Server error response is handled safely.
- [ ] Network failure is handled safely.
- [ ] No hardcoded real user data or real WG IDs are used.
- [ ] Development/test mock data is clearly marked or removed before merge.

Notes:

```text

```

---

# 5. Form Validation Check

## Task Creation Form

- [ ] Required title/name field is validated.
- [ ] Blank title/name is rejected.
- [ ] Extremely long title/name is rejected or handled safely.
- [ ] Extremely long description is rejected or handled safely.
- [ ] Due date/deadline is validated if present.
- [ ] Invalid date format cannot be submitted.
- [ ] Past due date is rejected or allowed according to project decision.
- [ ] Missing assignee is rejected if assignment is required.
- [ ] Empty assignee list is rejected if assignment is required.
- [ ] Duplicate assignees are rejected or normalized if multi-assignment exists.
- [ ] Users outside WG cannot be selected.
- [ ] Form displays validation messages near relevant fields.
- [ ] Form does not rely only on backend validation.
- [ ] Form sends backend-compatible data.

## Task Assignment Form

- [ ] Assignment form validates assignee selection.
- [ ] Missing assignee is rejected.
- [ ] Duplicate assignees are rejected or normalized if multi-assignment exists.
- [ ] Users outside WG cannot be selected.
- [ ] Assignment form displays validation messages clearly.
- [ ] Assignment form sends backend-compatible request body.

Notes:

```text

```

---

# 6. State Management Check

- [ ] Task list state updates after creating task.
- [ ] Task detail/list state updates after assigning/reassigning task.
- [ ] Loading state is shown while requests are pending.
- [ ] Error state is shown when requests fail.
- [ ] Success state/message is shown if project style includes it.
- [ ] Duplicate submissions are prevented during loading.
- [ ] Component unsubscribes or uses safe subscription handling where applicable.
- [ ] Component does not leak memory through repeated navigation.
- [ ] UI state remains consistent after failed requests.
- [ ] UI state remains consistent after backend validation errors.
- [ ] Page refresh still loads persisted tasks correctly if retrieval exists.

Notes:

```text

```

---

# 7. Routing and Navigation Check

- [ ] Task management route exists.
- [ ] Route follows project naming conventions.
- [ ] Route is protected by authentication where applicable.
- [ ] User can navigate to task overview.
- [ ] User can navigate to task creation.
- [ ] User can navigate to task detail if implemented.
- [ ] User can navigate back from task detail/form.
- [ ] Browser refresh on task route works.
- [ ] Direct URL access works for authenticated users.
- [ ] Direct URL access redirects or blocks unauthenticated users.
- [ ] Invalid task route/ID is handled gracefully.
- [ ] Navigation does not expose tasks from other WGs.

Notes:

```text

```

---

# 8. Security and Authorization UX Check

- [ ] Unauthenticated user cannot access protected task pages.
- [ ] Unauthenticated user is redirected or shown correct access message.
- [ ] Authenticated user only sees tasks from own WG.
- [ ] User cannot create task for another WG through UI.
- [ ] User cannot manipulate WG ID in UI controls.
- [ ] User cannot assign tasks to users outside own WG through UI.
- [ ] UI does not rely on hidden buttons as the only security layer.
- [ ] Frontend does not store JWT in unsafe custom locations beyond project standard.
- [ ] Frontend does not log JWT or secrets.
- [ ] Frontend does not show private backend error details.
- [ ] Frontend handles expired/invalid token gracefully.
- [ ] Frontend calls backend authorization rather than trusting local state only.

Notes:

```text

```

---

# 9. Error Handling Check

- [ ] Backend validation errors are displayed clearly.
- [ ] Failed request errors are displayed safely.
- [ ] Unauthorized errors are handled correctly.
- [ ] Forbidden errors are handled correctly.
- [ ] Not-found errors are handled correctly.
- [ ] Server errors do not crash frontend.
- [ ] Network errors do not crash frontend.
- [ ] User can retry after failed request.
- [ ] User input is not unnecessarily lost after recoverable errors.
- [ ] Error messages do not expose stack traces.
- [ ] Error messages do not expose SQL/database details.
- [ ] Error messages do not expose sensitive user data.
- [ ] Console does not contain avoidable error spam after normal use.

Notes:

```text

```

---

# 10. Accessibility and Usability Check

- [ ] Main controls are reachable by keyboard.
- [ ] Form fields have labels.
- [ ] Buttons have understandable text.
- [ ] Icon-only buttons have accessible labels or tooltips.
- [ ] Validation errors are visible and understandable.
- [ ] Color is not the only way to understand task status.
- [ ] Date/time information is displayed clearly.
- [ ] Assignee names/emails are displayed clearly.
- [ ] Focus behavior is reasonable after opening forms/dialogs.
- [ ] Focus behavior is reasonable after validation errors.
- [ ] Layout works on common laptop screens.
- [ ] Layout works on smaller/mobile screens if responsive design is required.
- [ ] Text does not overflow unexpectedly.
- [ ] Task list remains readable with many tasks.

Notes:

```text

```

---

# 11. Automated Frontend Test Checklist

## Component Tests

- [ ] Task overview component renders.
- [ ] Task creation form renders.
- [ ] Task assignment component renders if implemented separately.
- [ ] Task detail component renders if implemented.
- [ ] Empty task state renders.
- [ ] Loading state renders.
- [ ] Error state renders.
- [ ] Valid task form submits expected data.
- [ ] Invalid task form does not submit.
- [ ] Form validation messages appear correctly.
- [ ] Task list displays service data correctly.
- [ ] Task detail displays service data correctly.
- [ ] Assignment controls behave correctly if implemented.

## Service Tests

- [ ] Task service calls correct creation endpoint.
- [ ] Task service calls correct assignment endpoint.
- [ ] Task service calls correct retrieval endpoint if implemented.
- [ ] Task service sends correct creation request body.
- [ ] Task service sends correct assignment request body.
- [ ] Task service maps response correctly.
- [ ] Task service handles backend errors correctly.

## Selenium / E2E Tests

- [ ] E2E test logs in with fake test user.
- [ ] E2E test navigates to task management page.
- [ ] E2E test creates valid task.
- [ ] E2E test verifies created task appears.
- [ ] E2E test assigns or reassigns task.
- [ ] E2E test verifies assignment is displayed.
- [ ] E2E test verifies invalid form cannot be submitted.
- [ ] E2E test verifies unauthenticated user cannot access task page.
- [ ] E2E test verifies user cannot access another WG task if test setup supports it.
- [ ] E2E tests clean up or use isolated fake test data.

Notes:

```text

```

---

# 12. Manual Frontend Testing

- [ ] Log in as fake WG member.
- [ ] Open task management page.
- [ ] Create valid task.
- [ ] Verify task appears in list/detail.
- [ ] Verify correct assignee.
- [ ] Verify correct due date/deadline if relevant.
- [ ] Assign/reassign task if assignment is separate.
- [ ] Try missing title/name.
- [ ] Try invalid due date if relevant.
- [ ] Try invalid/nonexistent assignee if possible.
- [ ] Verify backend validation errors display correctly.
- [ ] Verify loading states.
- [ ] Verify error states by simulating backend failure if possible.
- [ ] Verify logout/expired token behavior.
- [ ] Inspect browser console for errors.
- [ ] Inspect network requests for correct URLs and payloads.
- [ ] Attach manual evidence to merge request.

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

- [ ] Frontend displays only necessary task data.
- [ ] Frontend does not display users from other WGs.
- [ ] Frontend does not expose private user data unnecessarily.
- [ ] Frontend does not display passwords, tokens, or secrets.
- [ ] Frontend does not log JWT or sensitive data to console.
- [ ] Task titles/descriptions are treated as user-provided data.
- [ ] Manual screenshots do not expose real users.
- [ ] Test data uses fake users and fake household data only.
- [ ] Browser storage does not contain unnecessary personal data.
- [ ] Error messages do not reveal private data from other WGs.

Notes:

```text

```

---

# 14. Non-Functional Frontend Check

## Reliability

- [ ] UI does not crash on empty task data.
- [ ] UI does not crash on partial/missing optional fields.
- [ ] UI does not crash on backend error.
- [ ] UI does not crash on network failure.
- [ ] UI remains usable after failed create/assign.
- [ ] Page refresh works.
- [ ] Back/forward navigation works.

## Performance

- [ ] Task page loads in acceptable time in local/dev.
- [ ] Repeated task creation/retrieval does not noticeably degrade UI.
- [ ] No unnecessary repeated API calls occur.
- [ ] Task list rendering remains acceptable with many tasks.
- [ ] No obvious memory leak occurs during repeated navigation.

## Maintainability

- [ ] Components are not overloaded with API logic.
- [ ] API calls are centralized in services.
- [ ] Models/interfaces match backend DTOs.
- [ ] Naming is understandable.
- [ ] Code follows project style.
- [ ] No commented-out dead code remains.
- [ ] No temporary debug logs remain.
- [ ] No hardcoded local-only URLs remain.
- [ ] No credentials or secrets are present.

Notes:

```text

```

---

# 15. Documentation Check

- [ ] Frontend route/page documented if project docs track routes.
- [ ] User flow documented if required.
- [ ] Form fields and validation behavior documented.
- [ ] API contract assumptions documented.
- [ ] Known limitations documented.
- [ ] How to run frontend tests documented.
- [ ] How to run Selenium/e2e tests documented.
- [ ] Manual testing steps documented.
- [ ] README/project docs updated if needed.

Notes:

```text

```

---

# 16. Frontend CI and Merge Readiness

- [ ] Frontend installs dependencies successfully.
- [ ] Frontend builds successfully.
- [ ] Frontend starts successfully.
- [ ] Frontend lint passes if configured.
- [ ] Frontend unit/component tests pass.
- [ ] Selenium/e2e tests pass if configured.
- [ ] GitLab CI pipeline passes.
- [ ] No secrets committed.
- [ ] No local config files accidentally committed.
- [ ] No real user data committed.
- [ ] No unrelated files changed.
- [ ] Manual frontend testing complete.
- [ ] Frontend documentation/API notes updated.
- [ ] All critical frontend items checked.
- [ ] All unchecked items documented with reason.
- [ ] Frontend QA reviewer approves.

---

# 17. Blocking Issue Rules

If any item in this section is true, frontend FR-03 must be rejected.

- [ ] Frontend does not build.
- [ ] Frontend does not start.
- [ ] Task management page cannot be accessed by authenticated users.
- [ ] Task creation does not work.
- [ ] Task assignment does not work.
- [ ] Task retrieval/display does not work if required.
- [ ] Frontend sends API request incompatible with backend contract.
- [ ] Frontend shows tasks from another WG.
- [ ] Frontend allows assignment to users outside WG without visible backend rejection.
- [ ] Unauthenticated users can use protected task functionality.
- [ ] Invalid input can be submitted without error feedback.
- [ ] User cannot recover from common backend errors.
- [ ] Automated tests fail without documented non-blocking reason.
- [ ] CI pipeline fails.
- [ ] Manual testing was not performed.
- [ ] Sensitive personal data is leaked.
- [ ] Real user data is committed.
- [ ] Secrets are committed.
- [ ] Critical security issue remains unresolved.
- [ ] Critical GDPR/privacy issue remains unresolved.
- [ ] Feature breaks existing login/navigation/WG functionality.

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

- [ ] APPROVED — frontend can be merged
- [ ] REJECTED — frontend fixes required
- [ ] APPROVED WITH MINOR NON-BLOCKING ISSUES

Final frontend summary:

```text
Feature: FR-03 Task creation and assignment

Frontend builds: YES/NO
Frontend starts: YES/NO
Frontend tests passed: YES/NO
Selenium/e2e tests passed: YES/NO
Manual frontend testing completed: YES/NO
Security checks passed: YES/NO
GDPR/privacy checks passed: YES/NO
Documentation updated: YES/NO
CI pipeline passed: YES/NO

Critical issues:
Non-critical issues:
Automated test evidence:
Manual test evidence:
Security/GDPR notes:
Decision:
QA reviewer:
Date:
```
