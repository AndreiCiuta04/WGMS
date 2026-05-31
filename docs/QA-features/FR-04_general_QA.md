# FR-04 General QA Checklist — Cleaning Schedule Management

## Feature Information

- Feature ID: FR-04
- Feature name: Cleaning schedule management
- Use case: UC-02 — Cleaning Schedule and Task Management
- Priority: High
- Category: Core
- Version: V2
- Estimated effort: 32h
- Feature lead: Joan de la Fuente C.
- System: WG Management System
- Frontend: Angular SPA
- Backend: Java Spring Boot REST API
- Database: H2 / JPA
- Branch: `FR-04/Cleaning-schedule-management`
- Merge request:
- Developer:
- QA reviewer:
- Date:

---

## 1. Feature Scope Summary

FR-04 implements cleaning schedule management for a WG.

The feature was checked as complete for the implemented scope. A logged-in WG member can access the cleaning schedule, see household cleaning duties, understand who is responsible, see due dates/status information, create/edit/manage schedule entries, and generate concrete tasks from schedule entries.

FR-04 does not implement full fair rotation/balancing, dashboard history, notification automation, NFC, or voice/LLM automation. These are outside the implemented scope and are not blockers for FR-04.

The branch also contains a new UI. The new UI was checked in the live demo and did not block the FR-04 workflow or existing core flows.

---

## 2. End-to-End Acceptance Workflow

### Setup

- [x] Backend is running.
- [x] Frontend is running.
- [x] Database is clean or contains known test data.
- [x] Test user 1 exists and belongs to a WG.
- [x] Test user 2 exists and belongs to the same WG.
- [x] Test user 3 exists and belongs to a different WG for isolation testing.
- [x] Browser dev tools show no major console errors before starting.
- [x] The new UI loads correctly after login.

### Main Successful Flow

- [x] User logs in.
- [x] User reaches the cleaning schedule page through the new UI/navigation.
- [x] User sees a clear schedule list, calendar, board, table, or empty state.
- [x] User creates or opens a cleaning duty/schedule entry if creation is implemented.
- [x] User enters or verifies a realistic cleaning duty, e.g. `Clean kitchen`.
- [x] User assigns or verifies an assigned roommate.
- [x] User enters or verifies a realistic due date.
- [x] User saves the schedule entry if creation/editing is implemented.
- [x] Created/updated schedule entry appears in the frontend.
- [x] User can mark an open cleaning duty as completed.
- [x] Frontend shows the completed status clearly.
- [x] Backend/database contains the schedule entry.
- [x] Backend/database contains the correct assignee, household, due date, and status.
- [x] User logs out and logs back in.
- [x] Schedule data is still visible.

### Negative Flow

- [x] User cannot access the cleaning schedule while logged out.
- [x] User cannot create/update a schedule with missing required fields.
- [x] User cannot assign a schedule entry to a roommate from a different WG.
- [x] User cannot access a schedule entry from another WG.
- [x] User cannot perform restricted actions if their role should not allow it.
- [x] API errors are shown without crashing the UI.
- [x] New UI does not hide critical error/success feedback.

Notes:

- Backend was started successfully with `mvn spring-boot:run`.
- Frontend was tested through the live demo.
- Cypress integration tests verify schedule creation, listing, updating, pause/resume, delete, generated task linkage, household isolation, and unauthenticated access rejection.
- Completion is handled through the generated FR-03 task workflow, not directly on the schedule template.

---

## 3. Requirement Traceability

- [x] UC-02 is covered: cleaning duties/tasks can be scheduled or managed.
- [x] UC-02 is covered: responsibilities can be assigned or displayed.
- [x] UC-02 is covered: task/schedule status can be tracked.
- [x] UC-02 is covered: recurring household duties are supported or the limitation is documented.
- [x] NFR-01 is covered: user must authenticate before accessing WG-specific schedule data.
- [x] NFR-02 is covered: role/household authorization is enforced on the backend.
- [x] NFR-03 is considered: only necessary task/schedule data is collected.
- [x] NFR-05 is considered: schedule pages load normally under demo data size.
- [x] NFR-06 is considered: schedule data persists during normal demo usage.
- [x] NFR-08 is covered: main workflow is usable by a peer tester.
- [x] NFR-09 is considered: frontend/backend responsibilities are clearly separated.
- [x] NFR-10 is considered: critical task/schedule logic has automated tests or documented test gaps.
- [x] NFR-11 is considered: important actions are traceable through timestamps/user data where implemented.
- [x] NFR-12 is considered: new UI is usable on reference desktop/mobile viewports.

Notes:

- Recurrence/frequency is implemented.
- Full fair rotation/balancing is outside the implemented FR-04 scope.
- Schedule status/completion is connected to generated task handling.

---

## 4. Backend and Frontend Integration

- [x] Frontend uses real backend data, not hardcoded demo schedule data.
- [x] Backend accepts the request format sent by the frontend.
- [x] Frontend correctly interprets backend response fields.
- [x] Schedule ID, task ID, assignee ID, and household context are handled consistently.
- [x] Create/update/complete/delete actions update both backend and frontend state.
- [x] Refreshing the browser does not lose schedule data.
- [x] Backend validation errors are displayed in the frontend.
- [x] Authorization errors are displayed safely.
- [x] Loading states avoid duplicate accidental submissions.
- [x] Date fields are displayed consistently between backend and frontend.

Notes:

- Frontend calls the Spring Boot backend through the cleaning schedule service.
- Backend exposes the expected `/api/v1/cleaning-schedules` endpoints.
- Cypress integration verifies backend/frontend behavior and generated task integration.

---

## 5. New UI Acceptance

- [x] New UI is not just visually changed; the FR-04 workflow still works end to end.
- [x] Navigation remains understandable after login.
- [x] Cleaning schedule can be found without guessing hidden routes.
- [x] Existing login/logout flow still works.
- [x] Existing WG context/membership flow still works.
- [x] Existing task creation/assignment behavior from FR-03 still works or regressions are documented.
- [x] Layout does not break on desktop reference viewport.
- [x] Layout does not break on mobile reference viewport.
- [x] Buttons and forms are clear enough for peer testing.
- [x] No placeholder content remains visible.
- [x] No broken icons/images/routes remain visible.
- [x] Console has no repeated frontend errors during the normal workflow.
- [x] Visual changes do not create accessibility/readability problems.

Notes:

- New UI was checked in the live demo.
- No style changes were made during this QA pass.
- The redesigned UI is accepted as-is.

---

## 6. Data Privacy and Security Review

- [x] Schedule data is household-private.
- [x] User names/emails are shown only where needed for assignment clarity.
- [x] No password hashes, JWT tokens, or internal IDs are unnecessarily exposed in the UI.
- [x] No sensitive data is logged to browser console.
- [x] No sensitive data is logged unnecessarily in backend logs.
- [x] Cross-household access attempts are rejected by backend.
- [x] Frontend hiding is not treated as the only security mechanism.
- [x] Test data does not contain real personal information.

Notes:

- Cypress tests use generated dummy users.
- Cross-household isolation was tested.
- Backend authorization remains the source of truth.

---

## 7. Regression Checks

- [x] Registration still works.
- [x] Login still works.
- [x] Logout still works.
- [x] WG creation/invitation flow still works or known issues are documented.
- [x] Basic task creation and assignment still works.
- [x] Cleaning schedule management works after page refresh.
- [x] Navigation between main pages works.
- [x] Protected routes remain protected.
- [x] Backend tests for earlier features still pass.
- [x] Frontend build still passes.
- [x] Cypress smoke tests still pass or failures are documented.

Notes:

- Backend pipeline is green.
- Frontend build passes.
- FR-04 Cypress integration tests pass.
- Existing old Cypress/login-helper issue was investigated and documented separately as not blocking FR-04.

---

## 8. Performance and Usability Smoke Test

- [x] Schedule page loads within acceptable demo time.
- [x] Page remains usable with around 20 schedule entries.
- [x] Page remains usable with around 100 task/schedule records if easy to seed.
- [x] Filtering/sorting/pagination exists if the list becomes long, or limitation is documented.
- [x] Main action can be completed without external instructions.
- [x] Peer tester can identify their next cleaning duty quickly.
- [x] Peer tester can mark a duty complete without confusion.
- [x] Error messages are understandable.
- [x] UI does not require precise mouse-only interactions.

Notes:

- Demo-size performance and usability were accepted.
- No blocking performance or usability issue was observed.
- If future data volume grows, additional filtering/pagination can be improved later.

---

## 9. Documentation and Merge Readiness

- [x] FR-04 behavior is described in README, feature notes, or MR description where necessary.
- [x] Known limitations are documented.
- [x] Test evidence is attached or described in the MR.
- [x] Screenshots of the new UI are attached if useful.
- [x] Backend endpoint changes are documented.
- [x] Frontend route/navigation changes are documented.
- [x] Database/model changes are documented if relevant.
- [x] CI pipeline passes.
- [x] Reviewer can reproduce the workflow locally.
- [x] No TODOs or debug logs remain in critical paths unless documented.
- [x] No unrelated formatting-only changes obscure the review.

Notes:

- MR evidence should mention:
    - Branch: `FR-04/Cleaning-schedule-management`
    - Backend startup and pipeline status
    - Frontend build/test status
    - Cypress FR-04 integration test result
    - Manual live demo
    - Known limitations:
        - no full fair rotation queue
        - completion happens through generated tasks
        - old unrelated Cypress `addMessage` issue documented separately

---

## 10. Final General Acceptance

- [x] Backend checklist has no unresolved critical failures.
- [x] Frontend checklist has no unresolved critical failures.
- [x] End-to-end workflow works with real backend and frontend.
- [x] Cleaning schedule data persists.
- [x] Authentication and household isolation are enforced.
- [x] New UI is usable and does not break core workflows.
- [x] Existing implemented features still work.
- [x] CI/build status is acceptable.
- [x] Remaining limitations are documented before merge.

Final notes:

- None :(