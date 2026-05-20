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
- Branch:
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
- If something fails, leave it unchecked and write a short note under the relevant section.
- If something does not apply, write `N/A` with a short reason.
- Do not merge into `dev` while critical items are unchecked.

This checklist is written so that it can be given to an AI assistant or a human reviewer to verify the backend implementation systematically.

---

## Project-Specific Backend Scope

FR-04 belongs to UC-02: Cleaning Schedule and Task Management.

The backend implementation must support the management of cleaning schedules for a WG. A cleaning schedule should define which cleaning duty or recurring household task exists, who is responsible, when it is due, and what its current status is. The backend must protect all schedule data by authentication and household membership checks.

FR-04 is **cleaning schedule management**, not only simple task creation. FR-03 covers basic task creation and assignment. FR-04 must add or verify the schedule-specific behavior: recurring duties, due dates, cycle/rotation logic where implemented, schedule views, status updates, and household-scoped retrieval.

Expected backend areas to inspect:

- Entity/model classes for cleaning schedules, tasks, recurring assignments, and status history.
- DTOs for request and response payloads.
- Mapper classes if MapStruct or manual mapping is used.
- Repository classes.
- Service layer/business logic.
- Endpoint/REST API classes.
- Security configuration and method-level authorization.
- Validation logic.
- Database initialization/migration/seed data if relevant.
- Unit and integration tests.
- OpenAPI/Swagger documentation.

---

## 1. Branch, Build, and Baseline Checks

- [ ] Correct branch is checked out: `FR-04/Cleaning-schedule-management` or equivalent.
- [ ] Branch is up to date with `origin`.
- [ ] Branch is compared against the correct base branch, normally `dev`.
- [ ] `git status` shows no unrelated local changes before QA starts.
- [ ] Backend compiles successfully.
- [ ] Backend application starts without crashing.
- [ ] Existing backend tests still pass.
- [ ] FR-04 changes do not accidentally remove or break FR-01, FR-02, or FR-03 behavior.
- [ ] No unrelated frontend/backend files were changed without explanation.
- [ ] No secrets, JWT secrets, database passwords, API keys, or local credentials are committed.

Suggested commands:

```bash
git branch --show-current
git status
git fetch --all --prune
git diff --name-status dev...HEAD

cd backend
./mvnw clean test
./mvnw spring-boot:run
```

Notes:

-

---

## 2. Requirement Coverage

FR-04 must satisfy the core requirement:

> Roommates can create, assign, and track recurring household tasks or cleaning duties.

Check that the backend supports the following:

- [ ] Authenticated users can create or manage a cleaning schedule.
- [ ] Cleaning schedule entries are attached to one household/WG.
- [ ] Cleaning duties/tasks have meaningful names, e.g. kitchen, bathroom, trash, floors.
- [ ] Cleaning duties/tasks can have assigned roommates.
- [ ] Cleaning duties/tasks can have due dates or schedule dates.
- [ ] Recurring duties are supported if planned by the implementation.
- [ ] Rotation/cycle behavior is supported if planned by the implementation.
- [ ] A cleaning duty can be marked as completed.
- [ ] A cleaning duty can be shown as pending/open.
- [ ] Overdue duties can be identified by backend data or response fields.
- [ ] Cleaning schedule entries can be retrieved after creation.
- [ ] Cleaning schedule list can be retrieved for the current household.
- [ ] Cleaning schedule details can be retrieved by ID where applicable.
- [ ] Cleaning schedule entries can be updated where allowed.
- [ ] Cleaning schedule entries can be deleted/cancelled where allowed, or deletion is explicitly out of scope and documented.
- [ ] Backend response includes enough information for the frontend to display name, assignee, due date, status, and recurrence/rotation information.
- [ ] Backend does not expose cleaning schedules from other households.
- [ ] Backend behavior is documented in API docs or README where necessary.

Notes:

-

---

## 3. Domain Model and Persistence Checks

Inspect entity and repository implementation.

- [ ] Cleaning schedule, task schedule, recurring task, or equivalent entity exists and is persisted.
- [ ] Schedule entry has a stable primary key.
- [ ] Schedule entry is linked to exactly one household/WG.
- [ ] Schedule entry is linked to the relevant task/duty.
- [ ] Schedule entry is linked to the assigned roommate/user where applicable.
- [ ] Schedule entry stores due date/date range/period information.
- [ ] Schedule entry stores status, e.g. open, done, overdue, skipped.
- [ ] Schedule entry stores created/updated timestamps or equivalent audit data.
- [ ] Completion data stores who completed it and when, if this is supported.
- [ ] Recurrence fields are stored consistently, if recurrence is implemented.
- [ ] Rotation/order fields are stored consistently, if rotation is implemented.
- [ ] Deleting or updating schedules does not leave orphaned task/assignment records.
- [ ] Cascade behavior is intentional and safe.
- [ ] Repository queries are household-scoped.
- [ ] Repository queries do not return all cleaning schedules globally unless protected internally.
- [ ] Database schema starts cleanly in test and dev profiles.
- [ ] Test data/seeding does not create misleading production-like personal data.

Suggested inspection commands:

```bash
find backend/src/main/java -iname "*Schedule*" -o -iname "*Cleaning*" -o -iname "*Task*" -o -iname "*Assignment*"
grep -R "enum .*Status\|DONE\|COMPLETED\|OVERDUE\|PENDING" backend/src/main/java | grep -i "task\|clean\|schedule" || true
grep -R "LocalDate\|LocalDateTime\|Instant" backend/src/main/java | grep -i "task\|clean\|schedule\|due" || true
```

Notes:

-

---

## 4. DTO and API Contract Checks

Inspect request and response DTOs.

- [ ] Create-schedule request DTO exists, or task DTO clearly contains schedule fields.
- [ ] Update-schedule request DTO exists where updates are supported.
- [ ] Complete/mark-done request DTO exists where completion is separated from update.
- [ ] Request DTO contains required fields only.
- [ ] Request DTO does not allow the client to set trusted server-side fields such as creator ID, household ID, or completion user unless validated.
- [ ] Response DTO contains ID, title/name, assignee, due date, status, recurrence/rotation fields if relevant, and timestamps.
- [ ] DTOs do not expose password hashes, tokens, internal security fields, or unrelated user data.
- [ ] DTO validation annotations are used where appropriate.
- [ ] Invalid DTOs return `400 Bad Request`.
- [ ] API endpoint names are consistent with project style.
- [ ] API responses are consistent with existing backend response style.
- [ ] OpenAPI/Swagger shows the FR-04 endpoints.
- [ ] OpenAPI schemas match the implemented DTOs.
- [ ] Frontend can consume the response without guessing hidden fields.

Suggested checks:

```bash
grep -R "class .*Schedule.*Dto\|record .*Schedule.*Dto\|class .*Cleaning.*Dto\|record .*Cleaning.*Dto" backend/src/main/java
grep -R "@Valid\|@NotNull\|@NotBlank\|@Future\|@FutureOrPresent" backend/src/main/java | grep -i "task\|clean\|schedule"
```

Notes:

-

---

## 5. Endpoint Behavior Checks

Verify REST endpoint behavior manually or through integration tests.

Expected endpoint types may include:

- `POST /api/cleaning-schedules`
- `GET /api/cleaning-schedules`
- `GET /api/cleaning-schedules/{id}`
- `PUT/PATCH /api/cleaning-schedules/{id}`
- `POST/PATCH /api/cleaning-schedules/{id}/complete`
- `DELETE /api/cleaning-schedules/{id}`

Use the actual project paths if different.

### Create / Generate Schedule

- [ ] Authenticated WG member can create a cleaning schedule entry if this is allowed.
- [ ] Main Tenant can create or configure the schedule.
- [ ] Roommate permissions match the project policy.
- [ ] Schedule is automatically assigned to the user’s current household or validated household.
- [ ] Assigned roommate must belong to the same household.
- [ ] Due date must be valid.
- [ ] Required fields cannot be blank.
- [ ] Invalid requests return clear `400 Bad Request` responses.

### Retrieve Schedule

- [ ] Authenticated WG member can list cleaning schedule entries for their household.
- [ ] List endpoint returns only current household data.
- [ ] Detail endpoint returns the correct schedule entry.
- [ ] Unknown schedule ID returns `404 Not Found` or equivalent.
- [ ] Schedule ID from another household returns `403 Forbidden` or `404 Not Found` without leaking data.
- [ ] List responses are ordered usefully, e.g. by due date or status.

### Update Schedule

- [ ] Authorized user can update allowed fields.
- [ ] Unauthorized user cannot update restricted schedule settings if such restrictions exist.
- [ ] Assignee cannot be changed to a user from another household.
- [ ] Due date/date range validation is enforced.
- [ ] Status changes follow valid transitions.
- [ ] Update persists correctly.

### Complete / Reopen / Skip

- [ ] Assigned roommate can mark their cleaning task as completed if this is intended.
- [ ] Main Tenant can mark or correct completion if this is intended.
- [ ] Completion stores status correctly.
- [ ] Completion stores completion timestamp where supported.
- [ ] Completing an already completed item does not create inconsistent duplicate state.
- [ ] Reopen/undo behavior works if implemented, or is clearly out of scope.
- [ ] Skipping/rescheduling behavior works if implemented, or is clearly out of scope.

### Delete / Cancel

- [ ] Authorized user can delete/cancel a schedule entry if in scope.
- [ ] Unauthorized user cannot delete/cancel restricted schedule entries.
- [ ] Deleted/cancelled entries are no longer shown in active schedule lists unless intentionally shown as history.
- [ ] Deletion does not break task history or other users’ views.

Notes:

-

---

## 6. Authentication, Authorization, and Household Isolation

- [ ] All FR-04 endpoints require authentication.
- [ ] Unauthenticated requests return `401 Unauthorized`.
- [ ] Users can only access schedules from their own household.
- [ ] Cross-household schedule ID access is rejected.
- [ ] Client-supplied household IDs cannot be used to bypass isolation.
- [ ] Client-supplied assignee IDs are validated against household membership.
- [ ] Role restrictions are enforced on the backend, not only hidden in the frontend.
- [ ] Main Tenant and Roommate behavior matches project policy.
- [ ] Authorization failures return safe error messages without leaking private data.
- [ ] Tests cover at least one cross-household access attempt.

Notes:

-

---

## 7. Business Logic Checks

- [ ] Cleaning schedule data is derived from real stored tasks/duties, not hardcoded demo objects.
- [ ] Due dates are calculated consistently.
- [ ] Recurrence logic does not create duplicate schedule entries unexpectedly.
- [ ] Rotation logic assigns roommates fairly according to the implemented rule, if rotation is implemented.
- [ ] Status is not based only on frontend display logic.
- [ ] Overdue detection is consistent with backend date/time handling.
- [ ] Time zones/date-only fields are handled consistently enough for demo usage.
- [ ] Completing a scheduled item does not delete the historical record.
- [ ] Update operations do not silently overwrite unrelated fields.
- [ ] Error cases are handled explicitly rather than crashing with generic exceptions.

Notes:

-

---

## 8. Validation and Error Handling

- [ ] Missing required name/title is rejected.
- [ ] Missing assignee is rejected where assignee is required.
- [ ] Invalid assignee ID is rejected.
- [ ] Assignee from another household is rejected.
- [ ] Missing due date is rejected where due date is required.
- [ ] Invalid date range is rejected.
- [ ] Invalid recurrence value is rejected.
- [ ] Invalid status transition is rejected.
- [ ] Duplicate schedule entries are prevented where duplicates would be wrong.
- [ ] Backend returns meaningful error status codes.
- [ ] Backend error responses are consistent with the rest of the project.
- [ ] Validation is performed in backend, not only frontend.

Notes:

-

---

## 9. Automated Test Coverage

- [ ] Unit tests exist for schedule service/business logic.
- [ ] Unit tests cover valid schedule creation.
- [ ] Unit tests cover invalid input.
- [ ] Unit tests cover completion/status updates.
- [ ] Unit tests cover due date or recurrence behavior if implemented.
- [ ] Integration tests exist for the REST endpoints.
- [ ] Integration tests cover authenticated access.
- [ ] Integration tests cover unauthenticated rejection.
- [ ] Integration tests cover household isolation.
- [ ] Integration tests cover update/delete/complete behavior where implemented.
- [ ] Existing FR-03 task tests still pass.
- [ ] Tests are deterministic and do not depend on local machine state.
- [ ] CI pipeline runs the relevant backend tests.

Suggested commands:

```bash
cd backend
./mvnw test
grep -R "FR-04\|Schedule\|Cleaning\|Task" src/test || true
```

Notes:

-

---

## 10. API Documentation and Maintainability

- [ ] FR-04 endpoints are visible in Swagger/OpenAPI.
- [ ] Request and response examples are understandable.
- [ ] Public methods/classes have enough naming clarity to be maintainable.
- [ ] Service logic is not placed directly inside endpoint/controller classes unless very small and justified.
- [ ] Repository access is not performed directly from frontend-facing endpoint code if the project style uses services.
- [ ] No duplicated schedule logic across several unrelated classes.
- [ ] No commented-out debug code remains.
- [ ] No excessive console/system logging remains.
- [ ] Known limitations are documented.

Notes:

-

---

## 11. Final Backend Acceptance

The backend part of FR-04 can be accepted only if:

- [ ] Backend builds and starts.
- [ ] Existing tests pass.
- [ ] FR-04 endpoints exist and work.
- [ ] Schedule data is persisted correctly.
- [ ] Authentication is required.
- [ ] Household isolation is enforced.
- [ ] Validation is enforced on backend.
- [ ] Completion/status behavior works.
- [ ] No critical regressions are found in FR-01 to FR-03.
- [ ] Critical issues are documented before merge.

Final notes:

-
