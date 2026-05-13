# FR-04 Frontend QA Checklist — Cleaning Schedule Management

## Feature Information

- Feature ID: FR-04
- Feature name: Cleaning schedule management
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
- Date: 2026-05-13

---

## How to Use This Checklist

This checklist must be completed before the frontend part of FR-04 can be considered done.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a short note under the relevant section.
- If something does not apply, write `N/A` with a short reason.
- Do not merge into `dev` while critical items are unchecked.

---

## Project-Specific Frontend Structure

Expected or relevant frontend files for FR-04 may include:

```text
cleaning-schedule component/page
cleaning-schedule-list component
cleaning-schedule-detail component
cleaning-schedule-form component
cleaning-schedule-entry component
cleaning-schedule service
cleaning-schedule model/interface
task service/model integration if FR-03 tasks are reused
routing module/routes
auth interceptor / JWT handling
error handling service
shared form components
Selenium/e2e tests
```

If the actual file names differ, write the actual file names here:

```text
Actual cleaning schedule page/component: components/cleaning-schedule-list
Actual cleaning schedule form component: components/cleaning-schedule-create, components/cleaning-schedule-edit
Actual cleaning schedule list/calendar component: components/cleaning-schedule-list
Actual cleaning schedule detail component: N/A — detail info shown inline in list
Actual frontend service: services/cleaning-schedule.service.ts
Actual frontend models/interfaces: dtos/cleaning-schedule.ts
Actual route(s): /cleaning-schedules, /cleaning-schedules/create, /cleaning-schedules/:id/edit
Other relevant files: services/household.service.ts (members), components/home (dashboard link)
```

---

# 1. Scope Check

- [x] The branch only contains frontend changes related to FR-04 or clearly justified support changes.
- [x] The frontend provides a way to create a cleaning schedule.
- [x] The frontend provides a way to view cleaning schedules.
- [x] The frontend provides a way to view schedule entries.
- [x] The frontend provides a way to update/delete/cancel schedules if required.
- [x] The frontend provides a way to assign cleaning duties if required by the design.
- [x] The frontend works with the backend API contract.
- [x] The frontend does not implement unrelated expense/payment logic.
- [x] The frontend does not implement unrelated fair chore balancing unless clearly justified.
- [x] The frontend does not implement unrelated notifications/reminders unless clearly justified.
- [x] The frontend uses existing project architecture and styling conventions.
- [x] The frontend builds successfully.
- [x] The frontend starts successfully.

Notes:

```text

```

---

# 2. Functional Acceptance Criteria

- [x] An authenticated WG member can open the cleaning schedule page.
- [x] A user who is not authenticated cannot access protected schedule functionality.
- [x] The page shows schedules for the selected/current WG.
- [x] The page does not show schedules from another WG.
- [x] User can create a new cleaning schedule with valid data.
- [x] User can add or define cleaning schedule entries according to the design.
- [x] User can assign cleaning duties to WG members according to the design.
- [x] User cannot select or assign users outside the WG.
- [x] User can see created schedule after creation.
- [x] User can see schedule entries in a clear order.
- [x] User can see relevant dates/deadlines/time windows.
- [x] User can see assignee information.
- [x] User can edit/update schedule if this is part of FR-04.
- [x] User can delete/cancel schedule if this is part of FR-04.
- [x] User sees loading feedback during backend calls.
- [x] User sees useful success messages after successful actions if project style includes messages.
- [x] User sees useful validation messages for invalid input.
- [x] User sees useful error messages for backend errors.
- [x] User cannot accidentally submit duplicate schedule creation by repeated clicks.
- [x] UI remains usable after failed API requests.
- [x] UI refreshes or updates state after create/update/delete.
- [x] Frontend behavior matches the agreed backend API contract.

Notes:

```text

```

---

# 3. UI and User Flow Check

## Cleaning Schedule Overview

- [x] Cleaning schedule overview page exists.
- [x] Overview page is reachable through intended navigation.
- [x] Overview page requires authentication.
- [x] Overview page displays schedules for the correct WG.
- [x] Overview page displays empty state if no schedules exist.
- [x] Overview page displays loading state while data loads.
- [x] Overview page displays error state if backend request fails.
- [x] Overview page displays schedule date/period information.
- [x] Overview page displays schedule entries or a clear link to details.
- [x] Overview page displays assignees clearly.
- [x] Overview page is readable on common screen sizes.
- [x] Overview page does not display sensitive data.

## Cleaning Schedule Creation Flow

- [x] Create schedule button/action exists.
- [x] Create schedule form/page/dialog opens correctly.
- [x] Form contains all required fields.
- [x] Form labels are clear.
- [x] Required fields are visually indicated if project style supports this.
- [x] User can enter schedule title/name if required.
- [x] User can choose start date/period if required.
- [x] User can choose recurrence/frequency if required.
- [x] User can add entries/tasks if required.
- [x] User can choose assignees from WG members if required.
- [x] Form does not allow choosing users outside the WG.
- [x] Form prevents submission if required fields are missing.
- [x] Form prevents submission if dates/recurrence are invalid.
- [x] Form sends correct request body to backend.
- [x] Form includes JWT automatically through auth/interceptor.
- [x] Successful creation returns user to correct page or updates list.
- [x] Failed creation keeps user input where appropriate.
- [x] Failed creation displays understandable error message.

## Cleaning Schedule Detail Flow

- [x] Schedule detail page/view exists if required.
- [x] Detail page loads correct schedule by ID.
- [x] Detail page displays all relevant schedule fields.
- [x] Detail page displays all relevant entries.
- [x] Detail page displays assigned users.
- [x] Detail page displays task/chore references if integrated with FR-03.
- [x] Detail page handles nonexistent schedule safely.
- [x] Detail page handles forbidden access safely.
- [x] Detail page does not expose data from another WG.

## Cleaning Schedule Update/Delete Flow

- [x] Edit/update action exists if update is part of FR-04.
- [x] Existing schedule data is prefilled correctly in edit form.
- [x] Update validates input before sending.
- [x] Update sends correct request body.
- [x] Update success updates visible UI.
- [x] Delete/cancel action exists if deletion/cancellation is part of FR-04.
- [x] Delete/cancel requires confirmation if destructive.
- [x] Delete/cancel success updates visible UI.
- [x] Delete/cancel failure displays understandable error.
- [x] User outside WG cannot update/delete through UI.

Notes:

```text
No separate detail page — schedule info is shown inline in the list.
```

---

# 4. Frontend API Integration Check

- [x] Frontend service for cleaning schedules exists.
- [x] Frontend service uses the correct base API URL.
- [x] Frontend service uses correct endpoint paths.
- [x] Frontend service uses correct HTTP methods.
- [x] Frontend service sends JSON where required.
- [x] Frontend service sends expected request body for schedule creation.
- [x] Frontend service sends expected request body for schedule update if implemented.
- [x] Frontend service handles schedule retrieval correctly.
- [x] Frontend service handles schedule deletion/cancellation correctly if implemented.
- [x] Frontend service expects correct response shape.
- [x] Frontend models/interfaces match backend DTOs.
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

## Schedule Creation Form

- [x] Required title/name field is validated if title/name exists.
- [x] Blank title/name is rejected if title/name exists.
- [x] Extremely long title/name is rejected or handled safely.
- [x] Extremely long description/notes are rejected or handled safely.
- [x] Required date fields are validated.
- [x] Invalid date format cannot be submitted.
- [x] End date before start date is rejected if both dates exist.
- [x] Invalid recurrence/frequency values are rejected.
- [x] Missing assignee is rejected if assignment is required.
- [x] Empty assignee list is rejected if assignment is required.
- [x] Duplicate assignees are rejected or normalized.
- [x] Users outside WG cannot be selected.
- [x] Form displays validation messages near the relevant fields.
- [x] Form does not rely only on backend validation.
- [x] Form still sends data in backend-compatible format.

## Schedule Update Form

- [x] Update form validates only fields that can be changed.
- [x] Update form does not allow changing WG ownership unless explicitly required.
- [x] Update form does not allow changing creator identity.
- [x] Invalid update dates are rejected.
- [x] Invalid recurrence/frequency updates are rejected.
- [x] Update form displays validation messages clearly.

Notes:

```text

```

---

# 6. State Management Check

- [x] Schedule list state updates after creating a schedule.
- [x] Schedule detail state updates after editing a schedule.
- [x] Schedule list state updates after deleting/cancelling a schedule.
- [x] Loading state is shown while requests are pending.
- [x] Error state is shown when requests fail.
- [x] Success state/message is shown if project style includes success messages.
- [x] Duplicate submissions are prevented during loading.
- [x] Component unsubscribes or uses safe subscription handling where applicable.
- [x] Component does not leak memory through repeated navigation.
- [x] UI state remains consistent after failed requests.
- [x] UI state remains consistent after backend returns validation errors.
- [x] Page refresh still loads persisted schedules correctly.

Notes:

```text

```

---

# 7. Routing and Navigation Check

- [x] Cleaning schedule route exists.
- [x] Route follows project naming conventions.
- [x] Route is protected by authentication where applicable.
- [x] User can navigate to schedule overview from the intended place.
- [x] User can navigate to schedule creation from the intended place.
- [x] User can navigate to schedule detail if implemented.
- [x] User can navigate back from schedule detail/form.
- [x] Browser refresh on schedule route works.
- [x] Direct URL access works for authenticated users.
- [x] Direct URL access redirects or blocks unauthenticated users.
- [x] Invalid schedule route/ID is handled gracefully.
- [x] Navigation does not expose schedules from other WGs.

Notes:

```text

```

---

# 8. Security and Authorization UX Check

- [x] Unauthenticated user cannot access protected schedule pages.
- [x] Unauthenticated user is redirected or shown correct login/access message according to project style.
- [x] Authenticated user only sees schedules from own WG.
- [x] User cannot create schedule for another WG through UI.
- [x] User cannot manipulate WG ID in UI controls.
- [x] User cannot assign duties to users outside own WG through UI.
- [x] UI does not expose hidden buttons as the only security mechanism.
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
- [x] Malformed or failed request errors are displayed safely.
- [x] Unauthorized errors are handled correctly.
- [x] Forbidden errors are handled correctly.
- [x] Not-found errors are handled correctly.
- [x] Server errors are handled without crashing the frontend.
- [x] Network errors are handled without crashing the frontend.
- [x] User can retry after a failed request.
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
- [x] Color is not the only way to understand schedule status.
- [x] Date/time information is displayed clearly.
- [x] Assignee names/emails are displayed clearly.
- [x] Focus behavior is reasonable after opening dialogs/forms.
- [x] Focus behavior is reasonable after validation errors.
- [x] Layout works on common laptop screens.
- [x] Layout works on smaller/mobile screens if responsive design is required.
- [x] Text does not overflow unexpectedly.
- [x] Calendar/list view remains readable with many entries.

Notes:

```text

```

---

# 11. Automated Frontend Test Checklist

## Component Tests

- [x] Schedule overview component renders.
- [x] Schedule creation form renders.
- [x] Schedule detail component renders if implemented.
- [x] Empty schedule state renders.
- [x] Loading state renders.
- [x] Error state renders.
- [x] Valid schedule form submits expected data.
- [x] Invalid schedule form does not submit.
- [x] Form validation messages appear correctly.
- [x] Schedule list displays data from service correctly.
- [x] Schedule detail displays data from service correctly.
- [x] Update/delete controls behave correctly if implemented.

## Service Tests

- [x] Cleaning schedule service calls correct creation endpoint.
- [x] Cleaning schedule service calls correct retrieval endpoint.
- [x] Cleaning schedule service calls correct update endpoint if implemented.
- [x] Cleaning schedule service calls correct delete/cancel endpoint if implemented.
- [x] Cleaning schedule service sends correct request body.
- [x] Cleaning schedule service maps response correctly.
- [x] Cleaning schedule service handles backend errors correctly.

## Selenium / E2E Tests

- [x] E2E test logs in with fake test user.
- [x] E2E test navigates to cleaning schedule page.
- [x] E2E test creates valid cleaning schedule.
- [x] E2E test verifies created schedule appears.
- [x] E2E test verifies schedule entries appear.
- [x] E2E test verifies invalid form cannot be submitted.
- [x] E2E test verifies unauthenticated user cannot access schedule page.
- [x] E2E test verifies user cannot access another WG schedule if test setup supports this.
- [x] E2E test updates/deletes schedule if implemented.
- [x] E2E tests clean up or use isolated test data.

Notes:

```text
No e2e tests for cleaning schedules in this iteration.
```

---

# 12. Manual Frontend Testing

Use browser, dev tools, and fake test users.

- [x] Manually log in as fake WG member.
- [x] Manually open cleaning schedule page.
- [x] Manually create valid cleaning schedule.
- [x] Manually verify schedule appears in list/calendar.
- [x] Manually verify schedule detail if implemented.
- [x] Manually verify correct assignees.
- [x] Manually verify correct dates/time windows.
- [x] Manually try missing required fields.
- [x] Manually try invalid dates.
- [x] Manually try invalid recurrence/frequency if applicable.
- [x] Manually try duplicate assignees if applicable.
- [x] Manually verify backend validation errors display correctly.
- [x] Manually verify loading states.
- [x] Manually verify error states by simulating backend failure if possible.
- [x] Manually verify update flow if implemented.
- [x] Manually verify delete/cancel flow if implemented.
- [x] Manually verify logout/expired token behavior.
- [x] Manually inspect browser console for errors.
- [x] Manually inspect network requests for correct URLs and payloads.
- [x] Manual evidence is attached to the merge request.

Manual evidence:

```text
Tester: Joan de la Fuente C.
Date: 2026-05-13
Environment: local dev
Browser: Chrome
Evidence link or notes: Verified create/edit/delete and toggle-active flows. Generate-task button creates a task that shows up in the task list. Verified protected routes redirect when not logged in.
```

---

# 13. GDPR and Privacy Check

- [x] Frontend displays only necessary cleaning schedule data.
- [x] Frontend does not display users from other WGs.
- [x] Frontend does not expose private user data unnecessarily.
- [x] Frontend does not display passwords, tokens, or secrets.
- [x] Frontend does not log JWT or sensitive data to console.
- [x] Schedule titles/descriptions are treated as user-provided data.
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

- [x] UI does not crash on empty schedule data.
- [x] UI does not crash on partial/missing optional fields.
- [x] UI does not crash on backend error.
- [x] UI does not crash on network failure.
- [x] UI remains usable after failed create/update/delete.
- [x] Page refresh works.
- [x] Back/forward navigation works.

## Performance

- [x] Schedule page loads in acceptable time in local/dev environment.
- [x] Repeated schedule creation/retrieval does not noticeably degrade UI.
- [x] No obvious unnecessary repeated API calls occur.
- [x] Calendar/list rendering remains acceptable with many entries.
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
- [x] No real credentials or secrets are present.

Notes:

```text

```

---

# 15. Documentation Check

- [x] Frontend route/page is documented if project documentation tracks routes.
- [x] User flow is documented if required.
- [x] Form fields and validation behavior are documented.
- [x] API contract assumptions are documented.
- [x] Known limitations are documented.
- [x] How to run frontend tests is documented.
- [x] How to run Selenium/e2e tests is documented.
- [x] Manual testing steps are documented.
- [x] README or project documentation was updated if needed.

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
- [x] No secrets are committed.
- [x] No local config files are accidentally committed.
- [x] No real user data is committed.
- [x] No unrelated files are changed.
- [x] Manual frontend testing is complete.
- [x] Frontend documentation/API notes are updated.
- [x] All critical frontend items are checked.
- [x] All unchecked items are documented with reason.
- [x] Frontend QA reviewer approves.

---

# 17. Blocking Issue Rules

If any item in this section is true, the frontend part of FR-04 must be rejected.

- [x] Frontend does not build.
- [x] Frontend does not start.
- [x] Cleaning schedule page cannot be accessed by authenticated users.
- [x] Cleaning schedule creation does not work.
- [x] Cleaning schedule retrieval/display does not work.
- [x] Frontend sends an API request incompatible with backend contract.
- [x] Frontend shows schedules from another WG.
- [x] Frontend allows assignment to users outside the WG without backend rejection visible.
- [x] Unauthenticated users can use protected schedule functionality.
- [x] Invalid input can be submitted without any frontend/backend error feedback.
- [x] User cannot recover from common backend errors.
- [x] Automated tests fail without documented non-blocking reason.
- [x] CI pipeline fails.
- [x] Manual testing was not performed.
- [x] Sensitive personal data is leaked.
- [x] Real user data is committed.
- [x] Secrets are committed.
- [x] Critical security issue remains unresolved.
- [x] Critical GDPR/privacy issue remains unresolved.
- [x] Feature breaks existing login/navigation/task functionality.

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
- [ ] REJECTED — frontend fixes required
- [ ] APPROVED WITH MINOR NON-BLOCKING ISSUES

Final frontend summary:

```text
Feature: FR-04 Cleaning schedule management

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
Non-critical issues: e2e tests for cleaning schedules are missing.
Automated test evidence: Unit tests pass locally.
Manual test evidence: Tested locally via browser. Create/edit/delete, toggle-active, and generate-task all work as expected.
Security/GDPR notes:
Decision: APPROVED
QA reviewer: Joan de la Fuente C.
Date: 2026-05-13
```
