# FR-04 Frontend QA Checklist — Cleaning Schedule Management

## Feature Information

- Feature ID: FR-04
- Feature name: Cleaning schedule management
- Use case: UC-02 — Cleaning Schedule and Task Management
- Priority:
- Category:
- Version:
- Frontend technology: Angular
- Automated tests: Selenium / frontend test framework
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
Actual cleaning schedule page/component:
Actual cleaning schedule form component:
Actual cleaning schedule list/calendar component:
Actual cleaning schedule detail component:
Actual frontend service:
Actual frontend models/interfaces:
Actual route(s):
Other relevant files:
```

---

# 1. Scope Check

- [ ] The branch only contains frontend changes related to FR-04 or clearly justified support changes.
- [ ] The frontend provides a way to create a cleaning schedule.
- [ ] The frontend provides a way to view cleaning schedules.
- [ ] The frontend provides a way to view schedule entries.
- [ ] The frontend provides a way to update/delete/cancel schedules if required.
- [ ] The frontend provides a way to assign cleaning duties if required by the design.
- [ ] The frontend works with the backend API contract.
- [ ] The frontend does not implement unrelated expense/payment logic.
- [ ] The frontend does not implement unrelated fair chore balancing unless clearly justified.
- [ ] The frontend does not implement unrelated notifications/reminders unless clearly justified.
- [ ] The frontend uses existing project architecture and styling conventions.
- [ ] The frontend builds successfully.
- [ ] The frontend starts successfully.

Notes:

```text

```

---

# 2. Functional Acceptance Criteria

- [ ] An authenticated WG member can open the cleaning schedule page.
- [ ] A user who is not authenticated cannot access protected schedule functionality.
- [ ] The page shows schedules for the selected/current WG.
- [ ] The page does not show schedules from another WG.
- [ ] User can create a new cleaning schedule with valid data.
- [ ] User can add or define cleaning schedule entries according to the design.
- [ ] User can assign cleaning duties to WG members according to the design.
- [ ] User cannot select or assign users outside the WG.
- [ ] User can see created schedule after creation.
- [ ] User can see schedule entries in a clear order.
- [ ] User can see relevant dates/deadlines/time windows.
- [ ] User can see assignee information.
- [ ] User can edit/update schedule if this is part of FR-04.
- [ ] User can delete/cancel schedule if this is part of FR-04.
- [ ] User sees loading feedback during backend calls.
- [ ] User sees useful success messages after successful actions if project style includes messages.
- [ ] User sees useful validation messages for invalid input.
- [ ] User sees useful error messages for backend errors.
- [ ] User cannot accidentally submit duplicate schedule creation by repeated clicks.
- [ ] UI remains usable after failed API requests.
- [ ] UI refreshes or updates state after create/update/delete.
- [ ] Frontend behavior matches the agreed backend API contract.

Notes:

```text

```

---

# 3. UI and User Flow Check

## Cleaning Schedule Overview

- [ ] Cleaning schedule overview page exists.
- [ ] Overview page is reachable through intended navigation.
- [ ] Overview page requires authentication.
- [ ] Overview page displays schedules for the correct WG.
- [ ] Overview page displays empty state if no schedules exist.
- [ ] Overview page displays loading state while data loads.
- [ ] Overview page displays error state if backend request fails.
- [ ] Overview page displays schedule date/period information.
- [ ] Overview page displays schedule entries or a clear link to details.
- [ ] Overview page displays assignees clearly.
- [ ] Overview page is readable on common screen sizes.
- [ ] Overview page does not display sensitive data.

## Cleaning Schedule Creation Flow

- [ ] Create schedule button/action exists.
- [ ] Create schedule form/page/dialog opens correctly.
- [ ] Form contains all required fields.
- [ ] Form labels are clear.
- [ ] Required fields are visually indicated if project style supports this.
- [ ] User can enter schedule title/name if required.
- [ ] User can choose start date/period if required.
- [ ] User can choose recurrence/frequency if required.
- [ ] User can add entries/tasks if required.
- [ ] User can choose assignees from WG members if required.
- [ ] Form does not allow choosing users outside the WG.
- [ ] Form prevents submission if required fields are missing.
- [ ] Form prevents submission if dates/recurrence are invalid.
- [ ] Form sends correct request body to backend.
- [ ] Form includes JWT automatically through auth/interceptor.
- [ ] Successful creation returns user to correct page or updates list.
- [ ] Failed creation keeps user input where appropriate.
- [ ] Failed creation displays understandable error message.

## Cleaning Schedule Detail Flow

- [ ] Schedule detail page/view exists if required.
- [ ] Detail page loads correct schedule by ID.
- [ ] Detail page displays all relevant schedule fields.
- [ ] Detail page displays all relevant entries.
- [ ] Detail page displays assigned users.
- [ ] Detail page displays task/chore references if integrated with FR-03.
- [ ] Detail page handles nonexistent schedule safely.
- [ ] Detail page handles forbidden access safely.
- [ ] Detail page does not expose data from another WG.

## Cleaning Schedule Update/Delete Flow

- [ ] Edit/update action exists if update is part of FR-04.
- [ ] Existing schedule data is prefilled correctly in edit form.
- [ ] Update validates input before sending.
- [ ] Update sends correct request body.
- [ ] Update success updates visible UI.
- [ ] Delete/cancel action exists if deletion/cancellation is part of FR-04.
- [ ] Delete/cancel requires confirmation if destructive.
- [ ] Delete/cancel success updates visible UI.
- [ ] Delete/cancel failure displays understandable error.
- [ ] User outside WG cannot update/delete through UI.

Notes:

```text

```

---

# 4. Frontend API Integration Check

- [ ] Frontend service for cleaning schedules exists.
- [ ] Frontend service uses the correct base API URL.
- [ ] Frontend service uses correct endpoint paths.
- [ ] Frontend service uses correct HTTP methods.
- [ ] Frontend service sends JSON where required.
- [ ] Frontend service sends expected request body for schedule creation.
- [ ] Frontend service sends expected request body for schedule update if implemented.
- [ ] Frontend service handles schedule retrieval correctly.
- [ ] Frontend service handles schedule deletion/cancellation correctly if implemented.
- [ ] Frontend service expects correct response shape.
- [ ] Frontend models/interfaces match backend DTOs.
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

## Schedule Creation Form

- [ ] Required title/name field is validated if title/name exists.
- [ ] Blank title/name is rejected if title/name exists.
- [ ] Extremely long title/name is rejected or handled safely.
- [ ] Extremely long description/notes are rejected or handled safely.
- [ ] Required date fields are validated.
- [ ] Invalid date format cannot be submitted.
- [ ] End date before start date is rejected if both dates exist.
- [ ] Invalid recurrence/frequency values are rejected.
- [ ] Missing assignee is rejected if assignment is required.
- [ ] Empty assignee list is rejected if assignment is required.
- [ ] Duplicate assignees are rejected or normalized.
- [ ] Users outside WG cannot be selected.
- [ ] Form displays validation messages near the relevant fields.
- [ ] Form does not rely only on backend validation.
- [ ] Form still sends data in backend-compatible format.

## Schedule Update Form

- [ ] Update form validates only fields that can be changed.
- [ ] Update form does not allow changing WG ownership unless explicitly required.
- [ ] Update form does not allow changing creator identity.
- [ ] Invalid update dates are rejected.
- [ ] Invalid recurrence/frequency updates are rejected.
- [ ] Update form displays validation messages clearly.

Notes:

```text

```

---

# 6. State Management Check

- [ ] Schedule list state updates after creating a schedule.
- [ ] Schedule detail state updates after editing a schedule.
- [ ] Schedule list state updates after deleting/cancelling a schedule.
- [ ] Loading state is shown while requests are pending.
- [ ] Error state is shown when requests fail.
- [ ] Success state/message is shown if project style includes success messages.
- [ ] Duplicate submissions are prevented during loading.
- [ ] Component unsubscribes or uses safe subscription handling where applicable.
- [ ] Component does not leak memory through repeated navigation.
- [ ] UI state remains consistent after failed requests.
- [ ] UI state remains consistent after backend returns validation errors.
- [ ] Page refresh still loads persisted schedules correctly.

Notes:

```text

```

---

# 7. Routing and Navigation Check

- [ ] Cleaning schedule route exists.
- [ ] Route follows project naming conventions.
- [ ] Route is protected by authentication where applicable.
- [ ] User can navigate to schedule overview from the intended place.
- [ ] User can navigate to schedule creation from the intended place.
- [ ] User can navigate to schedule detail if implemented.
- [ ] User can navigate back from schedule detail/form.
- [ ] Browser refresh on schedule route works.
- [ ] Direct URL access works for authenticated users.
- [ ] Direct URL access redirects or blocks unauthenticated users.
- [ ] Invalid schedule route/ID is handled gracefully.
- [ ] Navigation does not expose schedules from other WGs.

Notes:

```text

```

---

# 8. Security and Authorization UX Check

- [ ] Unauthenticated user cannot access protected schedule pages.
- [ ] Unauthenticated user is redirected or shown correct login/access message according to project style.
- [ ] Authenticated user only sees schedules from own WG.
- [ ] User cannot create schedule for another WG through UI.
- [ ] User cannot manipulate WG ID in UI controls.
- [ ] User cannot assign duties to users outside own WG through UI.
- [ ] UI does not expose hidden buttons as the only security mechanism.
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
- [ ] Malformed or failed request errors are displayed safely.
- [ ] Unauthorized errors are handled correctly.
- [ ] Forbidden errors are handled correctly.
- [ ] Not-found errors are handled correctly.
- [ ] Server errors are handled without crashing the frontend.
- [ ] Network errors are handled without crashing the frontend.
- [ ] User can retry after a failed request.
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
- [ ] Color is not the only way to understand schedule status.
- [ ] Date/time information is displayed clearly.
- [ ] Assignee names/emails are displayed clearly.
- [ ] Focus behavior is reasonable after opening dialogs/forms.
- [ ] Focus behavior is reasonable after validation errors.
- [ ] Layout works on common laptop screens.
- [ ] Layout works on smaller/mobile screens if responsive design is required.
- [ ] Text does not overflow unexpectedly.
- [ ] Calendar/list view remains readable with many entries.

Notes:

```text

```

---

# 11. Automated Frontend Test Checklist

## Component Tests

- [ ] Schedule overview component renders.
- [ ] Schedule creation form renders.
- [ ] Schedule detail component renders if implemented.
- [ ] Empty schedule state renders.
- [ ] Loading state renders.
- [ ] Error state renders.
- [ ] Valid schedule form submits expected data.
- [ ] Invalid schedule form does not submit.
- [ ] Form validation messages appear correctly.
- [ ] Schedule list displays data from service correctly.
- [ ] Schedule detail displays data from service correctly.
- [ ] Update/delete controls behave correctly if implemented.

## Service Tests

- [ ] Cleaning schedule service calls correct creation endpoint.
- [ ] Cleaning schedule service calls correct retrieval endpoint.
- [ ] Cleaning schedule service calls correct update endpoint if implemented.
- [ ] Cleaning schedule service calls correct delete/cancel endpoint if implemented.
- [ ] Cleaning schedule service sends correct request body.
- [ ] Cleaning schedule service maps response correctly.
- [ ] Cleaning schedule service handles backend errors correctly.

## Selenium / E2E Tests

- [ ] E2E test logs in with fake test user.
- [ ] E2E test navigates to cleaning schedule page.
- [ ] E2E test creates valid cleaning schedule.
- [ ] E2E test verifies created schedule appears.
- [ ] E2E test verifies schedule entries appear.
- [ ] E2E test verifies invalid form cannot be submitted.
- [ ] E2E test verifies unauthenticated user cannot access schedule page.
- [ ] E2E test verifies user cannot access another WG schedule if test setup supports this.
- [ ] E2E test updates/deletes schedule if implemented.
- [ ] E2E tests clean up or use isolated test data.

Notes:

```text

```

---

# 12. Manual Frontend Testing

Use browser, dev tools, and fake test users.

- [ ] Manually log in as fake WG member.
- [ ] Manually open cleaning schedule page.
- [ ] Manually create valid cleaning schedule.
- [ ] Manually verify schedule appears in list/calendar.
- [ ] Manually verify schedule detail if implemented.
- [ ] Manually verify correct assignees.
- [ ] Manually verify correct dates/time windows.
- [ ] Manually try missing required fields.
- [ ] Manually try invalid dates.
- [ ] Manually try invalid recurrence/frequency if applicable.
- [ ] Manually try duplicate assignees if applicable.
- [ ] Manually verify backend validation errors display correctly.
- [ ] Manually verify loading states.
- [ ] Manually verify error states by simulating backend failure if possible.
- [ ] Manually verify update flow if implemented.
- [ ] Manually verify delete/cancel flow if implemented.
- [ ] Manually verify logout/expired token behavior.
- [ ] Manually inspect browser console for errors.
- [ ] Manually inspect network requests for correct URLs and payloads.
- [ ] Manual evidence is attached to the merge request.

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

- [ ] Frontend displays only necessary cleaning schedule data.
- [ ] Frontend does not display users from other WGs.
- [ ] Frontend does not expose private user data unnecessarily.
- [ ] Frontend does not display passwords, tokens, or secrets.
- [ ] Frontend does not log JWT or sensitive data to console.
- [ ] Schedule titles/descriptions are treated as user-provided data.
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

- [ ] UI does not crash on empty schedule data.
- [ ] UI does not crash on partial/missing optional fields.
- [ ] UI does not crash on backend error.
- [ ] UI does not crash on network failure.
- [ ] UI remains usable after failed create/update/delete.
- [ ] Page refresh works.
- [ ] Back/forward navigation works.

## Performance

- [ ] Schedule page loads in acceptable time in local/dev environment.
- [ ] Repeated schedule creation/retrieval does not noticeably degrade UI.
- [ ] No obvious unnecessary repeated API calls occur.
- [ ] Calendar/list rendering remains acceptable with many entries.
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
- [ ] No real credentials or secrets are present.

Notes:

```text

```

---

# 15. Documentation Check

- [ ] Frontend route/page is documented if project documentation tracks routes.
- [ ] User flow is documented if required.
- [ ] Form fields and validation behavior are documented.
- [ ] API contract assumptions are documented.
- [ ] Known limitations are documented.
- [ ] How to run frontend tests is documented.
- [ ] How to run Selenium/e2e tests is documented.
- [ ] Manual testing steps are documented.
- [ ] README or project documentation was updated if needed.

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
- [ ] No secrets are committed.
- [ ] No local config files are accidentally committed.
- [ ] No real user data is committed.
- [ ] No unrelated files are changed.
- [ ] Manual frontend testing is complete.
- [ ] Frontend documentation/API notes are updated.
- [ ] All critical frontend items are checked.
- [ ] All unchecked items are documented with reason.
- [ ] Frontend QA reviewer approves.

---

# 17. Blocking Issue Rules

If any item in this section is true, the frontend part of FR-04 must be rejected.

- [ ] Frontend does not build.
- [ ] Frontend does not start.
- [ ] Cleaning schedule page cannot be accessed by authenticated users.
- [ ] Cleaning schedule creation does not work.
- [ ] Cleaning schedule retrieval/display does not work.
- [ ] Frontend sends an API request incompatible with backend contract.
- [ ] Frontend shows schedules from another WG.
- [ ] Frontend allows assignment to users outside the WG without backend rejection visible.
- [ ] Unauthenticated users can use protected schedule functionality.
- [ ] Invalid input can be submitted without any frontend/backend error feedback.
- [ ] User cannot recover from common backend errors.
- [ ] Automated tests fail without documented non-blocking reason.
- [ ] CI pipeline fails.
- [ ] Manual testing was not performed.
- [ ] Sensitive personal data is leaked.
- [ ] Real user data is committed.
- [ ] Secrets are committed.
- [ ] Critical security issue remains unresolved.
- [ ] Critical GDPR/privacy issue remains unresolved.
- [ ] Feature breaks existing login/navigation/task functionality.

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
Feature: FR-04 Cleaning schedule management

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
