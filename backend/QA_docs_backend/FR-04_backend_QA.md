# FR-04 Backend QA Checklist — Cleaning Schedule Management

## Feature Information

- Feature ID: FR-04
- Feature name: Cleaning schedule management
- Use case: UC-02 — Cleaning Schedule and Task Management
- Priority: High
- Category: Core
- Version: V2
- Estimated effort: 32h
- Feature lead: Joan de la Fuente C.
- Backend technology: Java Spring Boot
- Security: Spring Security, JWT
- Database: H2 via Spring Data JPA / JPA
- Automated tests: JUnit / integration tests
- Branch: FR-04/Cleaning-schedule-management
- Merge request:
- Developer:
- QA reviewer:
- Date:

---

## How to Use This Checklist

This checklist must be completed before the backend part of FR-04 can be considered done.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- `N/A` means not applicable to the implemented scope, with a reason in the notes.
- If something fails, leave it unchecked and write a short note under the relevant section.
- Do not merge into `dev` while critical items are unchecked.

---

## Project-Specific Backend Scope

FR-04 belongs to UC-02: Cleaning Schedule and Task Management.

The backend implementation supports cleaning schedule management for a WG. A cleaning schedule defines which recurring cleaning duty exists, who is responsible, when it is due, and whether the schedule is active. Concrete cleaning duties are generated as normal `Task` entities and use the existing task status model.

FR-04 is cleaning schedule management, not only simple task creation. FR-03 covers basic task creation and assignment. FR-04 adds schedule-specific behavior: recurring duties, due dates, schedule views, active/inactive schedule state, generated tasks, and household-scoped retrieval.

---

## 1. Branch, Build, and Baseline Checks

- [x] Correct branch is checked out: `FR-04/Cleaning-schedule-management` or equivalent.
- [x] Branch is up to date with `origin`.
- [x] Branch is compared against the correct base branch, normally `dev`.
- [x] `git status` shows no unrelated local changes before QA starts.
- [x] Backend compiles successfully.
- [x] Backend application starts without crashing.
- [x] Existing backend tests still pass.
- [x] FR-04 changes do not accidentally remove or break FR-01, FR-02, or FR-03 behavior.
- [x] No unrelated frontend/backend files were changed without explanation.
- [x] No secrets, JWT secrets, database passwords, API keys, or local credentials are committed.

Notes:

- Branch was checked as `FR-04/Cleaning-schedule-management`.
- Backend tests pass locally.
- GitLab pipeline is green after the final test-cleanup fix.
- Secret scan did not show hardcoded secrets. CI password/token references are environment variables, not committed credentials.
- The branch also contains frontend FR-04 files and QA documentation files; these should be explained in the merge request.
- `.idea/` should not be committed.

---

## 2. Requirement Coverage

FR-04 must satisfy the core requirement:

> Roommates can create, assign, and track recurring household tasks or cleaning duties.

Check that the backend supports the following:

- [x] Authenticated users can create or manage a cleaning schedule.
- [x] Cleaning schedule entries are attached to one household/WG.
- [x] Cleaning duties/tasks have meaningful names, e.g. kitchen, bathroom, trash, floors.
- [x] Cleaning duties/tasks can have assigned roommates.
- [x] Cleaning duties/tasks can have due dates or schedule dates.
- [x] Recurring duties are supported if planned by the implementation.
- N/A Rotation/cycle behavior is supported if planned by the implementation.
- [x] A cleaning duty can be marked as completed.
- [x] A cleaning duty can be shown as pending/open.
- N/A Overdue duties can be identified by backend data or response fields.
- [x] Cleaning schedule entries can be retrieved after creation.
- [x] Cleaning schedule list can be retrieved for the current household.
- [x] Cleaning schedule details can be retrieved by ID where applicable.
- [x] Cleaning schedule entries can be updated where allowed.
- [x] Cleaning schedule entries can be deleted/cancelled where allowed, or deletion is explicitly out of scope and documented.
- [x] Backend response includes enough information for the frontend to display name, assignee, due date, status, and recurrence/rotation information.
- [x] Backend does not expose cleaning schedules from other households.
- [x] Backend behavior is documented in API docs or README where necessary.

Notes:

- `CleaningSchedule` represents the recurring schedule/template.
- A concrete cleaning duty is generated as a normal `Task`.
- Generated tasks use the existing `TaskStatus` values, especially `OPEN`, `IN_PROGRESS`, `DONE`, and `CANCELLED`.
- Generated tasks are now linked back to their source `CleaningSchedule`.
- `TaskDetailDto` exposes `cleaningScheduleId`.
- Rotation is not implemented; each schedule has one explicit assignee.
- No explicit overdue field is implemented; overdue can be inferred from due date if needed.

---

## 3. Domain Model and Persistence Checks

Inspect entity and repository implementation.

- [x] Cleaning schedule, task schedule, recurring task, or equivalent entity exists and is persisted.
- [x] Schedule entry has a stable primary key.
- [x] Schedule entry is linked to exactly one household/WG.
- [x] Schedule entry is linked to the relevant task/duty.
- [x] Schedule entry is linked to the assigned roommate/user where applicable.
- [x] Schedule entry stores due date/date range/period information.
- [x] Schedule entry stores status, e.g. open, done, overdue, skipped.
- [x] Schedule entry stores created/updated timestamps or equivalent audit data.
- N/A Completion data stores who completed it and when, if this is supported.
- [x] Recurrence fields are stored consistently, if recurrence is implemented.
- N/A Rotation/order fields are stored consistently, if rotation is implemented.
- [x] Deleting or updating schedules does not leave orphaned task/assignment records.
- [x] Cascade behavior is intentional and safe.
- [x] Repository queries are household-scoped.
- [x] Repository queries do not return all cleaning schedules globally unless protected internally.
- [x] Database schema starts cleanly in test and dev profiles.
- [x] Test data/seeding does not create misleading production-like personal data.

Notes:

- `CleaningSchedule` is persisted and linked to household, creator, assignee, recurrence, start date, end date, next due date, active flag, created timestamp, and updated timestamp.
- `Task` now has an optional `cleaningSchedule` relation.
- Generated tasks reference their source schedule.
- Manual tasks can have `cleaningScheduleId = null`.
- Deleting a schedule with generated tasks is blocked with `409 Conflict`.
- This prevents raw database foreign-key errors and preserves generated task history.
- Completion metadata such as `completedBy` / `completedAt` is not stored on `CleaningSchedule`; completion belongs to the generated `Task` model and broader FR-03 task behavior.

---

## 4. DTO and API Contract Checks

Inspect request and response DTOs.

- [x] Create-schedule request DTO exists, or task DTO clearly contains schedule fields.
- [x] Update-schedule request DTO exists where updates are supported.
- N/A Complete/mark-done request DTO exists where completion is separated from update.
- [x] Request DTO contains required fields only.
- [x] Request DTO does not allow the client to set trusted server-side fields such as creator ID, household ID, or completion user unless validated.
- [x] Response DTO contains ID, title/name, assignee, due date, status, recurrence/rotation fields if relevant, and timestamps.
- [x] DTOs do not expose password hashes, tokens, internal security fields, or unrelated user data.
- [x] DTO validation annotations are used where appropriate.
- [x] Invalid DTOs return `400 Bad Request`.
- [x] API endpoint names are consistent with project style.
- [x] API responses are consistent with existing backend response style.
- [x] OpenAPI/Swagger shows the FR-04 endpoints.
- [x] OpenAPI schemas match the implemented DTOs.
- [x] Frontend can consume the response without guessing hidden fields.

Notes:

- DTOs include:
    - `CleaningScheduleCreateDto`
    - `CleaningScheduleUpdateDto`
    - `CleaningScheduleActiveUpdateDto`
    - `CleaningScheduleDetailDto`
    - `TaskDetailDto`
- `TaskDetailDto` now exposes `cleaningScheduleId`.
- Create/update DTOs validate title, description length, frequency, start date, end date, and assignee email.
- The client cannot set household, creator, timestamps, or generated task relation directly.
- Completion is not handled by a separate FR-04 cleaning schedule DTO; completion belongs to generated tasks.

---

## 5. Endpoint Behavior Checks

Verify REST endpoint behavior manually or through integration tests.

Actual implemented endpoint base path:

- `/api/v1/cleaning-schedules`

Implemented endpoint types:

- `POST /api/v1/cleaning-schedules`
- `GET /api/v1/cleaning-schedules`
- `GET /api/v1/cleaning-schedules/active`
- `GET /api/v1/cleaning-schedules/{id}`
- `PUT /api/v1/cleaning-schedules/{id}`
- `PATCH /api/v1/cleaning-schedules/{id}/active`
- `POST /api/v1/cleaning-schedules/{id}/generate-task`
- `DELETE /api/v1/cleaning-schedules/{id}`

### Create / Generate Schedule

- [x] Authenticated WG member can create a cleaning schedule entry if this is allowed.
- [x] Main Tenant can create or configure the schedule.
- [x] Roommate permissions match the project policy.
- [x] Schedule is automatically assigned to the user’s current household or validated household.
- [x] Assigned roommate must belong to the same household.
- [x] Due date must be valid.
- [x] Required fields cannot be blank.
- [x] Invalid requests return clear `400 Bad Request` responses.

### Retrieve Schedule

- [x] Authenticated WG member can list cleaning schedule entries for their household.
- [x] List endpoint returns only current household data.
- [x] Detail endpoint returns the correct schedule entry.
- [x] Unknown schedule ID returns `404 Not Found` or equivalent.
- [x] Schedule ID from another household returns `403 Forbidden` or `404 Not Found` without leaking data.
- [x] List responses are ordered usefully, e.g. by due date or status.

### Update Schedule

- [x] Authorized user can update allowed fields.
- [x] Unauthorized user cannot update restricted schedule settings if such restrictions exist.
- [x] Assignee cannot be changed to a user from another household.
- [x] Due date/date range validation is enforced.
- N/A Status changes follow valid transitions.
- [x] Update persists correctly.

### Complete / Reopen / Skip

- [x] Assigned roommate can mark their cleaning task as completed if this is intended.
- [x] Main Tenant can mark or correct completion if this is intended.
- [x] Completion stores status correctly.
- N/A Completion stores completion timestamp where supported.
- [x] Completing an already completed item does not create inconsistent duplicate state.
- N/A Reopen/undo behavior works if implemented, or is clearly out of scope.
- N/A Skipping/rescheduling behavior works if implemented, or is clearly out of scope.

### Delete / Cancel

- [x] Authorized user can delete/cancel a schedule entry if in scope.
- [x] Unauthorized user cannot delete/cancel restricted schedule entries.
- [x] Deleted/cancelled entries are no longer shown in active schedule lists unless intentionally shown as history.
- [x] Deletion does not break task history or other users’ views.

Notes:

- Creating a schedule returns `201 Created`.
- Deleting a schedule without generated tasks returns `204 No Content`.
- Generating a task returns a `TaskDetailDto`.
- Generated task status is `OPEN`.
- Generated task contains the source `cleaningScheduleId`.
- Generating a task advances `nextDueDate`.
- Inactive schedules cannot generate tasks and return `409 Conflict`.
- Deleting a schedule with generated tasks now returns `409 Conflict` and keeps the schedule/task records intact.
- Status completion belongs to the generated `Task`, not directly to `CleaningSchedule`.

---

## 6. Authentication, Authorization, and Household Isolation

- [x] All FR-04 endpoints require authentication.
- [x] Unauthenticated requests are rejected. In the current test setup this returns `403 Forbidden`.
- [x] Users can only access schedules from their own household.
- [x] Cross-household schedule ID access is rejected.
- [x] Client-supplied household IDs cannot be used to bypass isolation.
- [x] Client-supplied assignee IDs are validated against household membership.
- [x] Role restrictions are enforced on the backend, not only hidden in the frontend.
- [x] Main Tenant and Roommate behavior matches project policy.
- [x] Authorization failures return safe error messages without leaking private data.
- [x] Tests cover at least one cross-household access attempt.

Notes:

- Endpoints are protected with `@Secured("ROLE_USER")`.
- The service derives the current user from the authenticated email.
- Household is derived from the user’s active `Roommate` membership.
- The client does not submit a household ID for schedule creation.
- Assignees are validated through active household membership.
- Cross-household list/detail/access attempts are tested.
- Assignee-from-other-household is tested.

---

## 7. Business Logic Checks

- [x] Cleaning schedule data is derived from real stored tasks/duties, not hardcoded demo objects.
- [x] Due dates are calculated consistently.
- [x] Recurrence logic does not create duplicate schedule entries unexpectedly.
- N/A Rotation logic assigns roommates fairly according to the implemented rule, if rotation is implemented.
- [x] Status is not based only on frontend display logic.
- N/A Overdue detection is consistent with backend date/time handling.
- [x] Time zones/date-only fields are handled consistently enough for demo usage.
- [x] Completing a scheduled item does not delete the historical record.
- [x] Update operations do not silently overwrite unrelated fields.
- [x] Error cases are handled explicitly rather than crashing with generic exceptions.

Notes:

- `generateTask(...)` creates a concrete `Task` and advances the schedule’s `nextDueDate`.
- Recurrence supports `DAILY`, `WEEKLY`, `BIWEEKLY`, and `MONTHLY`.
- Generating a task does not duplicate the schedule itself.
- Generated tasks preserve the historical duty.
- Delete protection prevents database-level FK crashes.
- Schedule state is stored in backend through the `active` flag.
- Generated task state is stored in backend through `TaskStatus`.
- `LocalDate` is used for due dates, which is sufficient for demo-level date handling.
- No automatic rotation queue is implemented.

---

## 8. Validation and Error Handling

- [x] Missing required name/title is rejected.
- [x] Missing assignee is rejected where assignee is required.
- [x] Invalid assignee ID is rejected.
- [x] Assignee from another household is rejected.
- [x] Missing due date is rejected where due date is required.
- [x] Invalid date range is rejected.
- [x] Invalid recurrence value is rejected.
- N/A Invalid status transition is rejected.
- N/A Duplicate schedule entries are prevented where duplicates would be wrong.
- [x] Backend returns meaningful error status codes.
- [x] Backend error responses are consistent with the rest of the project.
- [x] Validation is performed in backend, not only frontend.

Notes:

- Title is validated with `@NotBlank`.
- Frequency is validated with `@NotNull`.
- Start date is validated with `@NotNull` and `@FutureOrPresent`.
- End date is validated with `@FutureOrPresent`.
- End date before start date is rejected.
- Assignee email is validated with `@NotNull` and `@Email`.
- Assignee must be an active member of the same household.
- Inactive schedules cannot generate tasks and return `409 Conflict`.
- Ended schedules cannot generate tasks and return `409 Conflict`.
- Deleting a schedule with generated tasks returns `409 Conflict`.
- Duplicate schedules are not prevented because multiple similar duties may be valid for different people, dates, or periods.
- Status transitions belong to the generated `Task` flow, not directly to `CleaningSchedule`.

---

## 9. Automated Test Coverage

- [x] Unit tests exist for schedule service/business logic.
- [x] Unit tests cover valid schedule creation.
- [x] Unit tests cover invalid input.
- [x] Unit tests cover completion/status updates.
- [x] Unit tests cover due date or recurrence behavior if implemented.
- [x] Integration tests exist for the REST endpoints.
- [x] Integration tests cover authenticated access.
- [x] Integration tests cover unauthenticated rejection.
- [x] Integration tests cover household isolation.
- [x] Integration tests cover update/delete/complete behavior where implemented.
- [x] Existing FR-03 task tests still pass.
- [x] Tests are deterministic and do not depend on local machine state.
- [x] CI pipeline runs the relevant backend tests.

Notes:

- Unit tests cover:
    - valid schedule creation,
    - assignee from another household,
    - household-scoped listing,
    - active schedule listing,
    - cross-household access,
    - update and reassignment,
    - unauthorized update,
    - deactivation,
    - task generation,
    - generated-task traceability,
    - inactive schedule conflict,
    - schedule deletion,
    - delete conflict when generated tasks exist,
    - not-found behavior.
- Integration tests cover:
    - create schedule,
    - missing token rejection,
    - blank title rejection,
    - invalid date range rejection,
    - assignee from another household,
    - household-scoped list,
    - active-only list,
    - cross-household detail rejection,
    - update,
    - deactivate,
    - generate task,
    - generated-task `cleaningScheduleId`,
    - inactive schedule generate conflict,
    - normal delete,
    - generated-task delete conflict.
- Existing backend tests pass locally.
- GitLab pipeline is green.
- Login integration test cleanup was updated to delete tasks and cleaning schedules before households, preventing FK-order failures in CI.

---

## 10. API Documentation and Maintainability

- [x] FR-04 endpoints are visible in Swagger/OpenAPI.
- [x] Request and response examples are understandable.
- [x] Public methods/classes have enough naming clarity to be maintainable.
- [x] Service logic is not placed directly inside endpoint/controller classes unless very small and justified.
- [x] Repository access is not performed directly from frontend-facing endpoint code if the project style uses services.
- [x] No duplicated schedule logic across several unrelated classes.
- [x] No commented-out debug code remains.
- [x] No excessive console/system logging remains.
- [x] Known limitations are documented.

Notes:

- Endpoint methods have `@Operation` summaries.
- Endpoint layer is thin and delegates business logic to `CleaningScheduleService`.
- Business logic is placed in `SimpleCleaningScheduleService`.
- Repository access is performed in the service layer, not directly in the endpoint.
- Naming is clear:
    - `CleaningSchedule`
    - `CleaningFrequency`
    - `CleaningScheduleRepository`
    - `CleaningScheduleService`
    - `SimpleCleaningScheduleService`
    - `CleaningScheduleEndpoint`
    - `CleaningScheduleCreateDto`
    - `CleaningScheduleUpdateDto`
    - `CleaningScheduleDetailDto`
- Known limitations:
    - no automatic roommate rotation queue,
    - no explicit overdue response field,
    - no `completedBy` / `completedAt` metadata on `CleaningSchedule`,
    - completion/status is handled through generated `Task` entities,
    - deleting schedules with generated tasks is blocked instead of cascading.

---

## 11. Final Backend Acceptance

The backend part of FR-04 can be accepted only if:

- [x] Backend builds and starts.
- [x] Existing tests pass.
- [x] FR-04 endpoints exist and work.
- [x] Schedule data is persisted correctly.
- [x] Authentication is required.
- [x] Household isolation is enforced.
- [x] Validation is enforced on backend.
- [x] Completion/status behavior works.
- [x] No critical regressions are found in FR-01 to FR-03.
- [x] Critical issues are documented before merge.

Final notes:

- Backend FR-04 is accepted from a QA perspective.
- Generated cleaning tasks are now traceable to their source schedule.
- `TaskDetailDto` exposes `cleaningScheduleId`.
- Schedules with generated tasks cannot be deleted accidentally; backend returns `409 Conflict`.
- Generated task history is preserved.
- Backend tests pass locally.
- GitLab pipeline is green.
