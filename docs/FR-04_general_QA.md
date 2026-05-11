# FR-04 General QA Checklist — Cleaning Schedule Management

## Feature Information

- Feature ID: FR-04
- Feature name: Cleaning schedule management
- Use case: UC-02 — Cleaning Schedule and Task Management
- Priority:
- Category:
- Version:
- Backend technology: Java Spring Boot
- Frontend technology: Angular
- Database: PostgreSQL
- Security: JWT / Spring Security
- Automated backend tests: JUnit
- Automated frontend tests: Selenium / frontend test framework
- Branch:
- Merge request:
- Developer:
- QA reviewer:
- Date:

---

## How to Use This Checklist

This general checklist verifies that backend, frontend, API contract, security, privacy, and integration work together.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a short note.
- If something does not apply, write `N/A` with a short reason.
- Do not merge into `dev` while critical items are unchecked.

---

# 1. Feature Scope and Alignment

- [ ] FR-04 is clearly identified as cleaning schedule management.
- [ ] FR-04 is linked to UC-02 — Cleaning Schedule and Task Management.
- [ ] Backend implementation matches FR-04.
- [ ] Frontend implementation matches FR-04.
- [ ] API contract matches the agreed FR-04 design.
- [ ] Feature does not accidentally implement unrelated expense/payment functionality.
- [ ] Feature does not accidentally implement unrelated fair chore balancing unless clearly justified.
- [ ] Feature does not accidentally implement unrelated notifications/reminders unless clearly justified.
- [ ] Dependencies on FR-03 task creation/assignment are documented.
- [ ] Dependencies on WG/user membership features are documented.
- [ ] Known limitations are documented.
- [ ] Branch contains only related changes or clearly justified support changes.

Notes:

```text

```

---

# 2. End-to-End User Story Check

Verify the feature from the perspective of a real user.

- [ ] User can log in.
- [ ] User can access their WG/household context.
- [ ] User can navigate to cleaning schedule management.
- [ ] User can see existing cleaning schedules for their WG.
- [ ] User can create a new cleaning schedule.
- [ ] User can define schedule dates/period/recurrence according to the design.
- [ ] User can define cleaning entries/tasks according to the design.
- [ ] User can assign cleaning entries to WG members according to the design.
- [ ] User can save the schedule.
- [ ] User can see the saved schedule after creation.
- [ ] User can see assigned users.
- [ ] User can see due/planned dates clearly.
- [ ] User can update schedule if update is part of FR-04.
- [ ] User can delete/cancel schedule if deletion/cancellation is part of FR-04.
- [ ] User receives understandable validation feedback.
- [ ] User receives understandable backend error feedback.
- [ ] User cannot access schedules from another WG.
- [ ] User cannot assign duties to users outside the WG.
- [ ] User cannot use protected feature while unauthenticated.

Notes:

```text

```

---

# 3. Backend/Frontend API Contract Compatibility

Record the final agreed API contract here.

## API Endpoints

```text
Create schedule endpoint:
Retrieve schedules endpoint:
Retrieve one schedule endpoint:
Update schedule endpoint:
Delete/cancel schedule endpoint:
Schedule entry endpoint(s):
```

## Request/Response DTOs

```text
Schedule creation request DTO:
Schedule update request DTO:
Schedule response DTO:
Schedule list response DTO:
Schedule entry DTO:
Error response format:
```

Checklist:

- [ ] Frontend endpoint paths match backend endpoint paths.
- [ ] Frontend HTTP methods match backend HTTP methods.
- [ ] Frontend request body shape matches backend DTOs.
- [ ] Backend response body shape matches frontend models/interfaces.
- [ ] Date/time format is compatible between frontend and backend.
- [ ] Recurrence/frequency format is compatible between frontend and backend.
- [ ] Assignee/member identifiers are compatible between frontend and backend.
- [ ] WG/household identifier handling is compatible between frontend and backend.
- [ ] Backend returns expected status on success.
- [ ] Backend returns expected status on validation error.
- [ ] Backend returns expected status on unauthorized request.
- [ ] Backend returns expected status on forbidden cross-WG request.
- [ ] Backend returns expected status on not-found request.
- [ ] Frontend handles all expected backend statuses.
- [ ] Error response format is understandable for frontend.
- [ ] No sensitive fields are returned by backend or displayed by frontend.
- [ ] API contract is documented in the merge request or project docs.

Notes:

```text

```

---

# 4. Data Model and Integration Check

- [ ] Cleaning schedule data model is agreed and documented.
- [ ] Schedule-WG/household relationship is clear.
- [ ] Schedule-entry relationship is clear.
- [ ] Entry-assignee relationship is clear.
- [ ] Entry-task/chore relationship is clear if FR-03 task integration exists.
- [ ] Creator/owner behavior is clear.
- [ ] Schedule status behavior is clear if status exists.
- [ ] Entry status behavior is clear if status exists.
- [ ] Date/time behavior is clear.
- [ ] Recurrence/frequency behavior is clear if recurrence exists.
- [ ] Deletion/cancellation behavior is clear.
- [ ] History behavior is clear or documented as out of scope.
- [ ] Fair balancing behavior is clear or documented as out of scope.
- [ ] Data model supports expected frontend display.
- [ ] Data model supports expected backend validation.
- [ ] Data model does not store unnecessary personal data.

Notes:

```text

```

---

# 5. Security and Authorization Integration

- [ ] Backend endpoints require authentication.
- [ ] Frontend protects schedule routes/pages.
- [ ] JWT is sent with frontend API calls.
- [ ] Backend uses JWT/auth context to identify current user.
- [ ] Backend verifies WG membership server-side.
- [ ] Frontend only shows schedules for current WG.
- [ ] Backend prevents cross-WG access even if frontend is manipulated.
- [ ] Users cannot create schedules in another WG.
- [ ] Users cannot retrieve schedules from another WG.
- [ ] Users cannot update/delete schedules from another WG.
- [ ] Users cannot assign duties to users outside the WG.
- [ ] Users cannot fake creator identity through frontend or API.
- [ ] Users cannot fake WG ownership through frontend or API.
- [ ] Role restrictions are enforced if schedule management is role-limited.
- [ ] Frontend does not rely on hidden buttons as the only security layer.
- [ ] Error messages do not reveal private data from another WG.
- [ ] Expired or invalid token behavior is handled.
- [ ] Missing token behavior is handled.
- [ ] Manipulated token behavior is handled.
- [ ] Security behavior is covered by backend tests.
- [ ] Security behavior is covered by frontend/e2e tests where possible.

Notes:

```text

```

---

# 6. Validation and Error Handling Integration

- [ ] Frontend validates required fields before submit.
- [ ] Backend validates required fields independently.
- [ ] Frontend and backend validation rules are consistent.
- [ ] Missing required schedule data is handled.
- [ ] Invalid date data is handled.
- [ ] Invalid recurrence/frequency data is handled.
- [ ] Invalid assignee data is handled.
- [ ] Nonexistent WG is handled.
- [ ] Nonexistent schedule is handled.
- [ ] Nonexistent user/assignee is handled.
- [ ] Assignee from another WG is handled.
- [ ] Malformed JSON is handled.
- [ ] Duplicate/repeated submissions are handled safely.
- [ ] Backend returns safe errors.
- [ ] Frontend displays safe and understandable errors.
- [ ] Internal errors do not expose stack traces.
- [ ] Internal errors do not expose SQL/database details.
- [ ] UI remains usable after errors.
- [ ] User can retry after recoverable errors.

Notes:

```text

```

---

# 7. Persistence and Data Consistency

- [ ] Schedule creation persists data.
- [ ] Schedule entries persist data.
- [ ] Schedule assignments persist data.
- [ ] Schedule retrieval returns persisted data.
- [ ] Update persists changes if implemented.
- [ ] Delete/cancel persists changes if implemented.
- [ ] Failed creation does not leave partial data.
- [ ] Failed update does not leave partial data.
- [ ] Failed entry creation does not leave partial data.
- [ ] Data remains correct after backend restart.
- [ ] Data remains correct after frontend refresh.
- [ ] Database test data is isolated.
- [ ] Manual test data is fake.
- [ ] No real personal data is used in tests.
- [ ] No real personal data is committed.

Notes:

```text

```

---

# 8. Cross-Feature Compatibility

- [ ] Existing login still works.
- [ ] Existing registration/WG flow still works if implemented.
- [ ] Existing FR-03 task creation/assignment still works if implemented.
- [ ] Cleaning schedule integrates with tasks if required.
- [ ] Cleaning schedule does not break existing task endpoints.
- [ ] Cleaning schedule does not break existing frontend task views.
- [ ] Existing navigation still works.
- [ ] Existing auth interceptor still works.
- [ ] Existing backend security tests still pass.
- [ ] Existing frontend tests still pass.
- [ ] Existing database schema/migrations still work.
- [ ] Existing data generators still work.
- [ ] No unrelated feature behavior is changed without documentation.

Notes:

```text

```

---

# 9. Automated Test Evidence

## Backend

- [ ] Backend unit tests exist for schedule service logic.
- [ ] Backend repository tests exist for schedule persistence.
- [ ] Backend endpoint/API tests exist for schedule endpoints.
- [ ] Backend security tests exist for authentication/authorization.
- [ ] Backend integration tests cover create/retrieve flow.
- [ ] Backend tests cover invalid data.
- [ ] Backend tests cover cross-WG rejection.
- [ ] Backend tests cover nonexistent assignee/user.
- [ ] Backend tests cover nonexistent schedule.
- [ ] Backend tests pass locally.
- [ ] Backend tests pass in CI.

## Frontend

- [ ] Frontend component tests exist for schedule overview/form/detail where applicable.
- [ ] Frontend service tests exist for API calls.
- [ ] Frontend form validation tests exist.
- [ ] Frontend error handling tests exist.
- [ ] Selenium/e2e tests cover login and schedule creation.
- [ ] Selenium/e2e tests cover schedule retrieval/display.
- [ ] Selenium/e2e tests cover invalid form input.
- [ ] Selenium/e2e tests cover unauthenticated access rejection.
- [ ] Frontend tests pass locally.
- [ ] Frontend tests pass in CI.

Test evidence:

```text
Backend test command:
Backend result:
Frontend test command:
Frontend result:
E2E test command:
E2E result:
CI pipeline link:
```

---

# 10. Manual End-to-End Testing

Use fake users and fake WG/household data only.

- [ ] Manual test environment is documented.
- [ ] Manual test users are documented.
- [ ] Manual test WG/household is documented.
- [ ] Tester logs in as WG member.
- [ ] Tester opens cleaning schedule page.
- [ ] Tester creates valid cleaning schedule.
- [ ] Tester verifies schedule is displayed.
- [ ] Tester verifies schedule is persisted after refresh.
- [ ] Tester verifies schedule is persisted after backend restart if tested.
- [ ] Tester verifies schedule entries are correct.
- [ ] Tester verifies assignees are correct.
- [ ] Tester verifies invalid input is rejected.
- [ ] Tester verifies unauthenticated access is blocked.
- [ ] Tester verifies cross-WG access is blocked if test setup supports this.
- [ ] Tester verifies user outside WG cannot be assigned if test setup supports this.
- [ ] Tester verifies update/delete flow if implemented.
- [ ] Tester checks browser console.
- [ ] Tester checks backend logs.
- [ ] Tester attaches screenshots or notes to MR.
- [ ] Tester confirms screenshots do not expose real personal data.

Manual evidence:

```text
Tester:
Date:
Environment:
Browser:
Backend profile/environment:
Test users:
Test WG:
Evidence link or notes:
```

---

# 11. GDPR and Privacy

- [ ] Feature collects only necessary cleaning schedule data.
- [ ] Feature collects only necessary assignment/member data.
- [ ] Feature does not collect sensitive personal data.
- [ ] Schedule titles/descriptions are treated as user-generated data.
- [ ] User-generated schedule text is not logged unnecessarily.
- [ ] Backend logs do not contain unnecessary personal data.
- [ ] Frontend console logs do not contain unnecessary personal data.
- [ ] Responses do not expose users from other WGs.
- [ ] Screenshots/manual evidence use fake data.
- [ ] Test data uses fake users only.
- [ ] No real personal data is committed.
- [ ] JWT/secrets are not committed.
- [ ] Browser storage does not contain unnecessary personal data.
- [ ] Data deletion/history behavior is documented as implemented, future work, or out of scope.
- [ ] Purpose of cleaning schedule data is documented or assigned as documentation task.

Notes:

```text

```

---

# 12. Non-Functional Requirements

## Security

- [ ] Authentication is required.
- [ ] Authorization is enforced server-side.
- [ ] Cross-WG access is prevented.
- [ ] Client-provided IDs are validated.
- [ ] Error responses are safe.
- [ ] Secrets are not committed.
- [ ] Protected endpoints are protected.
- [ ] Public endpoints are intentionally public.

## Reliability

- [ ] Feature handles invalid input without crashing.
- [ ] Feature handles backend errors without frontend crash.
- [ ] Feature handles network failure safely.
- [ ] Feature handles empty schedule state safely.
- [ ] Feature handles repeated requests safely.
- [ ] Feature works after refresh.
- [ ] Feature works after backend restart.
- [ ] Database persistence works.

## Performance

- [ ] Schedule creation response time is acceptable in local/dev environment.
- [ ] Schedule retrieval response time is acceptable in local/dev environment.
- [ ] Schedule overview page loads in acceptable time.
- [ ] No obvious unnecessary API calls are made.
- [ ] No obvious N+1 backend query problem exists.
- [ ] No obvious frontend memory leak exists.
- [ ] Calendar/list rendering remains acceptable with many entries.

## Maintainability

- [ ] Backend endpoint layer is not overloaded.
- [ ] Backend service layer contains business logic.
- [ ] Backend repository layer handles persistence.
- [ ] Frontend components are not overloaded with API logic.
- [ ] Frontend API calls are centralized in services.
- [ ] DTOs/models are used appropriately.
- [ ] Names are understandable.
- [ ] No commented-out dead code remains.
- [ ] No temporary debug logs remain.
- [ ] Known limitations are documented.

Notes:

```text

```

---

# 13. Documentation and MR Readiness

- [ ] Feature purpose is documented.
- [ ] API endpoints are documented.
- [ ] Request/response formats are documented.
- [ ] Error behavior is documented.
- [ ] Authentication/authorization behavior is documented.
- [ ] WG membership restrictions are documented.
- [ ] Date/recurrence behavior is documented.
- [ ] Known limitations are documented.
- [ ] Backend test instructions are documented.
- [ ] Frontend test instructions are documented.
- [ ] Manual test steps are documented.
- [ ] MR description explains what was implemented.
- [ ] MR description lists test evidence.
- [ ] MR description lists known limitations.
- [ ] MR description links screenshots if useful.
- [ ] MR description explains any unchecked checklist items.

Notes:

```text

```

---

# 14. CI and Merge Readiness

- [ ] Backend compiles.
- [ ] Backend starts.
- [ ] Frontend builds.
- [ ] Frontend starts.
- [ ] Backend unit tests pass.
- [ ] Backend endpoint/API tests pass.
- [ ] Backend integration tests pass.
- [ ] Backend security tests pass.
- [ ] Frontend unit/component tests pass.
- [ ] Selenium/e2e tests pass if configured.
- [ ] Docker build passes if configured.
- [ ] GitLab CI pipeline passes.
- [ ] No secrets are committed.
- [ ] No local config files are accidentally committed.
- [ ] No real user data is committed.
- [ ] No unrelated files are changed.
- [ ] Backend checklist is completed.
- [ ] Frontend checklist is completed.
- [ ] General checklist is completed.
- [ ] All critical items are checked.
- [ ] All unchecked items are documented with reason.
- [ ] Reviewer approves.

Notes:

```text

```

---

# 15. Blocking Issue Rules

If any item in this section is true, FR-04 must be rejected.

- [ ] Backend does not compile.
- [ ] Backend does not start.
- [ ] Frontend does not build.
- [ ] Frontend does not start.
- [ ] Cleaning schedule creation does not work.
- [ ] Cleaning schedule retrieval/display does not work.
- [ ] Created schedule is not persisted.
- [ ] Schedule entries/assignments are not persisted.
- [ ] Frontend/backend API contract is incompatible.
- [ ] Unauthenticated users can use protected schedule functionality.
- [ ] Users can access schedules from another WG.
- [ ] Users can create schedules in another WG.
- [ ] Users can assign cleaning duties to users from another WG.
- [ ] Invalid schedule data creates a schedule anyway.
- [ ] Feature leaks sensitive personal data.
- [ ] Feature commits real user data.
- [ ] Feature commits secrets.
- [ ] Backend tests fail without documented non-blocking reason.
- [ ] Frontend tests fail without documented non-blocking reason.
- [ ] CI pipeline fails.
- [ ] Manual end-to-end testing was not performed.
- [ ] Critical security issue remains unresolved.
- [ ] Critical GDPR/privacy issue remains unresolved.
- [ ] Feature breaks existing login/WG/task functionality.

If any item above is checked, final decision must be:

```text
REJECTED — fixes required
```

Blocking issue notes:

```text

```

---

# Final General QA Decision

Choose one:

- [ ] APPROVED — FR-04 can be merged
- [ ] REJECTED — fixes required
- [ ] APPROVED WITH MINOR NON-BLOCKING ISSUES

Final general summary:

```text
Feature: FR-04 Cleaning schedule management

Backend approved: YES/NO
Frontend approved: YES/NO
Backend compiles/starts: YES/NO
Frontend builds/starts: YES/NO
Backend tests passed: YES/NO
Frontend tests passed: YES/NO
E2E tests passed: YES/NO
Manual E2E testing completed: YES/NO
Security checks passed: YES/NO
GDPR/privacy checks passed: YES/NO
Documentation updated: YES/NO
CI pipeline passed: YES/NO

Critical issues:
Non-critical issues:
Backend evidence:
Frontend evidence:
Manual evidence:
Security/GDPR notes:
Decision:
QA reviewer:
Date:
```
