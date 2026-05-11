# FR-04 Backend QA Checklist — Cleaning Schedule Management

## Feature Information

- Feature ID: FR-04
- Feature name: Cleaning schedule management
- Use case: UC-02 — Cleaning Schedule and Task Management
- Priority:
- Category:
- Version:
- Backend technology: Java Spring Boot
- Security: Spring Security, JWT
- Database: PostgreSQL
- Automated tests: JUnit
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

## Project-Specific Backend Structure

This project does not use a generic `controller` package.

The REST API layer is implemented through the `endpoint` package.

Expected or relevant backend files for FR-04 may include:

- `endpoint/CleaningScheduleEndpoint.java`
- `endpoint/dto/CleaningScheduleCreateDto.java`
- `endpoint/dto/CleaningScheduleUpdateDto.java`
- `endpoint/dto/CleaningScheduleDetailDto.java`
- `endpoint/dto/CleaningScheduleListDto.java`
- `endpoint/dto/CleaningScheduleEntryDto.java`
- `endpoint/dto/CleaningScheduleEntryCreateDto.java`
- `entity/CleaningSchedule.java`
- `entity/CleaningScheduleEntry.java`
- `entity/Task.java`
- `entity/Wg.java` or equivalent household/WG entity
- `entity/ApplicationUser.java`
- `repository/CleaningScheduleRepository.java`
- `repository/CleaningScheduleEntryRepository.java`
- `repository/TaskRepository.java`
- `repository/WgRepository.java` or equivalent
- `repository/UserRepository.java`
- `service/CleaningScheduleService.java`
- `service/impl/CleaningScheduleServiceImpl.java`
- `endpoint/mapper/CleaningScheduleMapper.java`
- `security/JwtAuthorizationFilter.java`
- `security/JwtTokenizer.java`
- `config/SecurityConfig.java`
- `endpoint/exceptionhandler/GlobalExceptionHandler.java`
- `exception/BadRequestException.java`
- `exception/ForbiddenException.java`
- `exception/ConflictException.java`
- `exception/NotFoundException.java`

If the actual file names differ, write the actual file names here:

```text
Actual cleaning schedule endpoint:
Actual cleaning schedule DTOs:
Actual cleaning schedule entity/entities:
Actual cleaning schedule repository/repositories:
Actual cleaning schedule service:
Actual cleaning schedule mapper:
Other relevant files:
```

Whenever this checklist says “endpoint”, it means the REST class receiving HTTP requests.

---

# 1. Scope Check

- [ ] The branch only contains changes related to FR-04 or clearly justified support changes.
- [ ] Backend cleaning schedule creation is implemented.
- [ ] Backend cleaning schedule retrieval is implemented.
- [ ] Backend cleaning schedule update is implemented if required by the feature design.
- [ ] Backend cleaning schedule deletion/cancellation is implemented if required by the feature design.
- [ ] Cleaning schedule entries can be generated, stored, or retrieved according to the design.
- [ ] Cleaning schedules belong to the correct WG/household context.
- [ ] Cleaning schedule entries are assigned to valid WG members.
- [ ] The implementation does not accidentally include unrelated expense/payment logic unless clearly justified.
- [ ] The implementation does not accidentally include unrelated notification/reminder logic unless clearly justified.
- [ ] The implementation does not accidentally include unrelated fair chore balancing logic unless clearly justified.
- [ ] The backend uses the existing project architecture instead of bypassing layers.
- [ ] The backend starts successfully after the changes.
- [ ] The backend compiles successfully.

Notes:

```text

```

---

# 2. Functional Acceptance Criteria

The backend part of FR-04 may only pass if these user-level outcomes work.

- [ ] An authenticated WG member can create a cleaning schedule.
- [ ] A cleaning schedule belongs to exactly one WG/household.
- [ ] A cleaning schedule has a meaningful name/title if required by the design.
- [ ] A cleaning schedule can define a period, start date, or recurrence according to the design.
- [ ] A cleaning schedule can contain one or more cleaning duties/entries.
- [ ] A cleaning duty/entry can be assigned to one roommate if single assignment is the design.
- [ ] A cleaning duty/entry can be assigned to multiple roommates if multi-assignment is the design.
- [ ] A cleaning duty/entry can reference a task/chore if FR-03 integration is part of the design.
- [ ] A schedule entry can include a due date, planned date, or time window according to the design.
- [ ] A schedule entry can include status if status tracking is part of the design.
- [ ] A schedule cannot be created for a WG where the creator is not a member.
- [ ] A schedule cannot assign a cleaning duty to a user outside the WG.
- [ ] A schedule cannot assign a cleaning duty to a nonexistent user.
- [ ] A schedule cannot be accessed by users outside the WG.
- [ ] Invalid required data is rejected.
- [ ] Created schedules are persisted in the database.
- [ ] Created schedule entries are persisted in the database.
- [ ] Created schedules can be retrieved after creation.
- [ ] Retrieved schedules are associated with the correct WG.
- [ ] Retrieved schedules contain the correct entries.
- [ ] Retrieved entries contain the correct assignees.
- [ ] Schedule updates are persisted if update is part of FR-04.
- [ ] Schedule deletion/cancellation is persisted if deletion/cancellation is part of FR-04.
- [ ] The implementation supports later FR-15 fair chore balancing if relevant.
- [ ] The implementation supports later history/reporting features if relevant.

Notes:

```text

```

---

# 3. API Contract Check

Document and verify the cleaning-schedule-related API contract.

## Cleaning Schedule Creation Endpoint

- [ ] Schedule creation endpoint path is known and documented.
- [ ] Schedule creation endpoint uses the correct HTTP method.
- [ ] Schedule creation endpoint accepts JSON.
- [ ] Schedule creation endpoint requires authentication.
- [ ] Schedule creation endpoint uses a DTO, not the entity directly.
- [ ] Schedule creation request contains WG/household identifier if needed by API design.
- [ ] Schedule creation request contains schedule name/title if required.
- [ ] Schedule creation request contains start date or period if required.
- [ ] Schedule creation request contains recurrence/frequency data if required.
- [ ] Schedule creation request contains entries/tasks if schedule entries are created together with the schedule.
- [ ] Schedule creation request contains assignee identifier or identifiers if needed.
- [ ] Schedule creation request does not allow the client to set internal-only fields.
- [ ] Schedule creation request does not allow the client to fake creator identity.
- [ ] Schedule creation request does not allow the client to set unauthorized WG ownership.
- [ ] Schedule creation success response has the correct HTTP status.
- [ ] Schedule creation validation error response has the correct HTTP status.
- [ ] Unauthorized schedule creation response has the correct HTTP status.
- [ ] Forbidden cross-WG schedule creation response has the correct HTTP status.
- [ ] Schedule creation response contains the created schedule identifier.
- [ ] Schedule creation response contains created schedule entries if relevant.
- [ ] Schedule creation response does not expose sensitive user data.
- [ ] Schedule creation response does not expose unnecessary internal entity fields.

## Cleaning Schedule Retrieval Endpoint

- [ ] Schedule retrieval endpoint path is known and documented.
- [ ] Schedule retrieval endpoint uses the correct HTTP method.
- [ ] Schedule retrieval endpoint requires authentication.
- [ ] Schedule retrieval endpoint only returns schedules from the authenticated user’s WG.
- [ ] Schedule retrieval endpoint supports retrieving one schedule by ID if required.
- [ ] Schedule retrieval endpoint supports retrieving all schedules for a WG if required.
- [ ] Schedule retrieval endpoint supports date range filtering if required.
- [ ] Schedule retrieval endpoint supports upcoming/current schedule retrieval if required.
- [ ] Schedule retrieval response contains enough data for the frontend calendar/list.
- [ ] Schedule retrieval response does not return schedules from other WGs.
- [ ] Schedule retrieval response does not expose sensitive user data.
- [ ] Schedule retrieval response does not expose unnecessary internal entity fields.

## Cleaning Schedule Update Endpoint

- [ ] Schedule update endpoint exists if update is part of FR-04.
- [ ] Schedule update endpoint path is known and documented if implemented.
- [ ] Schedule update endpoint uses the correct HTTP method if implemented.
- [ ] Schedule update endpoint accepts JSON if implemented.
- [ ] Schedule update endpoint requires authentication.
- [ ] Schedule update endpoint uses a DTO, not the entity directly.
- [ ] Schedule update request does not allow the client to fake creator identity.
- [ ] Schedule update request does not allow unauthorized WG ownership changes.
- [ ] Schedule update rejects users outside the WG.
- [ ] Schedule update rejects invalid recurrence/date data.
- [ ] Schedule update response has the correct HTTP status.
- [ ] Schedule update response contains updated schedule data if relevant.
- [ ] Schedule update response does not expose sensitive user data.

## Cleaning Schedule Entry Endpoint

- [ ] Entry creation endpoint exists if entries are managed separately.
- [ ] Entry update endpoint exists if entries are managed separately.
- [ ] Entry deletion/cancellation endpoint exists if entries are managed separately.
- [ ] Entry assignment endpoint exists if assignment is separate from schedule creation.
- [ ] Entry endpoint requires authentication.
- [ ] Entry endpoint validates WG membership.
- [ ] Entry endpoint rejects assignments to users outside the WG.
- [ ] Entry endpoint rejects nonexistent schedule IDs.
- [ ] Entry endpoint rejects nonexistent task IDs if task integration exists.
- [ ] Entry endpoint returns correct HTTP statuses.

Notes:

```text

```

---

# 4. DTO Validation Check

## Schedule Creation DTO

- [ ] Schedule creation DTO exists.
- [ ] Schedule creation DTO contains WG/household ID if needed.
- [ ] Schedule creation DTO contains schedule title/name if required.
- [ ] Schedule creation DTO contains start date if required.
- [ ] Schedule creation DTO contains end date if required.
- [ ] Schedule creation DTO contains recurrence/frequency information if required.
- [ ] Schedule creation DTO contains schedule entries if entries are created together.
- [ ] Schedule creation DTO contains assignee ID/email/list if assignment is included.
- [ ] Schedule creation DTO does not contain creator ID unless explicitly justified.
- [ ] Schedule creation DTO does not contain internal database ID for generated fields.
- [ ] Schedule creation DTO does not contain fields for unauthorized role or WG manipulation.
- [ ] Empty title/name is rejected if title/name exists.
- [ ] Blank title/name is rejected if title/name exists.
- [ ] Extremely long title/name is rejected or handled safely.
- [ ] Extremely long description/notes are rejected or handled safely.
- [ ] Invalid date format is rejected.
- [ ] Invalid recurrence format is rejected.
- [ ] End date before start date is rejected if both dates exist.
- [ ] Missing required assignee data is rejected if assignees are required.
- [ ] Empty assignee list is rejected if assignees are required.
- [ ] Duplicate assignee IDs/emails are rejected or normalized.
- [ ] DTO validation annotations are used where appropriate.

## Schedule Update DTO

- [ ] Schedule update DTO exists if updates are implemented.
- [ ] Schedule update DTO contains only fields that are allowed to change.
- [ ] Schedule update DTO does not contain unauthorized internal fields.
- [ ] Schedule update DTO does not allow changing WG ownership unless explicitly justified.
- [ ] Invalid update dates are rejected.
- [ ] Invalid recurrence updates are rejected.
- [ ] DTO validation annotations are used where appropriate.

## Schedule Response DTO

- [ ] Schedule response DTO exists.
- [ ] Schedule response DTO contains schedule ID.
- [ ] Schedule response DTO contains WG/household ID or safe WG reference.
- [ ] Schedule response DTO contains schedule title/name if relevant.
- [ ] Schedule response DTO contains date/period/recurrence information.
- [ ] Schedule response DTO contains entries needed by the frontend.
- [ ] Schedule response DTO contains assignee information needed by the frontend.
- [ ] Schedule response DTO contains task references if relevant.
- [ ] Schedule response DTO does not expose password fields.
- [ ] Schedule response DTO does not expose sensitive user data.
- [ ] Schedule response DTO does not expose unnecessary internal entity fields.

Notes:

```text

```

---

# 5. Cleaning Schedule Creation Functional Check

- [ ] Authenticated user can create a cleaning schedule with valid data.
- [ ] Created schedule is saved in the database.
- [ ] Created schedule has the correct title/name if provided.
- [ ] Created schedule has the correct date/period/recurrence data.
- [ ] Created schedule has the correct WG/household association.
- [ ] Created schedule has the correct creator if creator tracking exists.
- [ ] Created schedule creates correct entries if entries are created together.
- [ ] Created entries have correct planned/due dates.
- [ ] Created entries have correct assignee or assignees.
- [ ] Created entries reference correct tasks/chores if task integration exists.
- [ ] Created entries receive correct default status if status exists.
- [ ] Created schedule receives created timestamp if timestamps exist.
- [ ] Created schedule receives updated timestamp if timestamps exist.
- [ ] Schedule creation rejects missing required fields.
- [ ] Schedule creation rejects invalid dates.
- [ ] Schedule creation rejects invalid recurrence/frequency data.
- [ ] Schedule creation rejects nonexistent assignee.
- [ ] Schedule creation rejects assignee from another WG.
- [ ] Schedule creation rejects creation for a WG where the user is not a member.
- [ ] Schedule creation rejects unauthenticated requests.
- [ ] Schedule creation handles malformed JSON safely.
- [ ] Schedule creation handles invalid request body safely.
- [ ] Schedule creation does not create partial records after failure.
- [ ] Schedule creation is transactional where needed.
- [ ] Schedule creation works after backend restart.
- [ ] Schedule creation works with the configured PostgreSQL/test database.

Notes:

```text

```

---

# 6. Cleaning Schedule Retrieval Functional Check

- [ ] Authenticated WG member can retrieve cleaning schedules for own WG.
- [ ] Retrieved schedules are filtered by WG.
- [ ] Retrieved schedules do not include data from other WGs.
- [ ] Retrieved schedules include required schedule details.
- [ ] Retrieved schedules include required entry details.
- [ ] Retrieved entries are ordered chronologically if required.
- [ ] Date range filtering works if implemented.
- [ ] Upcoming/current schedule filtering works if implemented.
- [ ] Retrieval rejects nonexistent schedule ID.
- [ ] Retrieval rejects access by users outside the WG.
- [ ] Retrieval rejects unauthenticated requests.
- [ ] Retrieval handles empty schedule lists safely.
- [ ] Retrieval response is stable and frontend-compatible.
- [ ] Retrieval works after backend restart.
- [ ] Retrieval works with persisted database data.

Notes:

```text

```

---

# 7. Cleaning Schedule Update/Delete Functional Check

- [ ] Authenticated authorized user can update a schedule if update is implemented.
- [ ] Schedule update is saved in the database.
- [ ] Schedule update modifies the correct schedule.
- [ ] Schedule update does not modify unrelated schedules.
- [ ] Schedule update rejects nonexistent schedule.
- [ ] Schedule update rejects invalid date/recurrence data.
- [ ] Schedule update rejects users outside the WG.
- [ ] Schedule update rejects unauthenticated requests.
- [ ] Schedule update preserves or regenerates entries according to project decision.
- [ ] Schedule update is transactional where needed.
- [ ] Schedule deletion/cancellation works if implemented.
- [ ] Schedule deletion/cancellation only affects correct schedule.
- [ ] Schedule deletion/cancellation rejects users outside the WG.
- [ ] Schedule deletion/cancellation rejects unauthenticated requests.
- [ ] Deletion/cancellation behavior is documented.
- [ ] Update/delete works after backend restart.
- [ ] Update/delete works with persisted database data.

Notes:

```text

```

---

# 8. Endpoint Layer Check

- [ ] Cleaning schedule endpoint exists.
- [ ] Cleaning schedule endpoint is placed in the `endpoint` package.
- [ ] Cleaning schedule endpoint uses clear route names.
- [ ] Cleaning schedule endpoint uses correct HTTP methods.
- [ ] Cleaning schedule endpoint requires authentication.
- [ ] Cleaning schedule endpoint accepts DTOs instead of entities.
- [ ] Cleaning schedule endpoint returns DTOs instead of entities where appropriate.
- [ ] Cleaning schedule endpoint calls `CleaningScheduleService`.
- [ ] Cleaning schedule endpoint does not call repositories directly unless clearly justified.
- [ ] Cleaning schedule endpoint does not contain database logic directly.
- [ ] Cleaning schedule endpoint does not contain complex business logic directly.
- [ ] Cleaning schedule endpoint handles successful schedule creation correctly.
- [ ] Cleaning schedule endpoint handles successful retrieval correctly.
- [ ] Cleaning schedule endpoint handles successful update/delete if implemented.
- [ ] Cleaning schedule endpoint handles validation errors correctly.
- [ ] Cleaning schedule endpoint handles not-found errors correctly.
- [ ] Cleaning schedule endpoint handles forbidden access correctly.
- [ ] Cleaning schedule endpoint handles unauthenticated access correctly.
- [ ] Cleaning schedule endpoint does not expose sensitive user fields.
- [ ] Cleaning schedule endpoint does not expose unnecessary internal entity fields.

Notes:

```text

```

---

# 9. Service Layer Check

- [ ] `CleaningScheduleService` or equivalent exists.
- [ ] Schedule creation business logic is in the service layer.
- [ ] Schedule retrieval business logic is in the service layer.
- [ ] Schedule update/delete business logic is in the service layer if implemented.
- [ ] Service verifies that the authenticated user belongs to the WG.
- [ ] Service verifies that assignees belong to the same WG.
- [ ] Service verifies that schedule belongs to the correct WG before retrieval/update/delete.
- [ ] Service rejects schedule access across WG boundaries.
- [ ] Service validates business rules that cannot be trusted to the frontend.
- [ ] Service assigns creator from authenticated user context if creator is stored.
- [ ] Service assigns default schedule/entry status where relevant.
- [ ] Service saves schedule through repository/repositories.
- [ ] Service uses user repository or equivalent to validate assignees.
- [ ] Service uses WG repository or equivalent to validate WG membership.
- [ ] Service is transactional where needed.
- [ ] Service throws appropriate exceptions for not-found cases.
- [ ] Service throws appropriate exceptions for forbidden access.
- [ ] Service throws appropriate exceptions for conflicts.
- [ ] Service does not directly handle HTTP request/response objects.
- [ ] Service is covered by JUnit unit tests.

Notes:

```text

```

---

# 10. Repository and Entity Check

## Cleaning Schedule Entity

- [ ] Cleaning schedule entity or equivalent exists.
- [ ] Cleaning schedule entity contains generated ID.
- [ ] Cleaning schedule entity contains WG/household relation.
- [ ] Cleaning schedule entity contains title/name if relevant.
- [ ] Cleaning schedule entity contains start/end/period/recurrence data if relevant.
- [ ] Cleaning schedule entity contains created/updated timestamps if relevant.
- [ ] Cleaning schedule entity contains creator relation or creator email if relevant.
- [ ] Cleaning schedule entity uses correct JPA annotations.
- [ ] Cleaning schedule entity uses correct relationship cardinality.
- [ ] Cleaning schedule entity does not store unnecessary personal data.
- [ ] Cleaning schedule entity does not expose sensitive information through `toString()`.
- [ ] Cleaning schedule entity supports later fair chore balancing/history if relevant.

## Cleaning Schedule Entry Entity

- [ ] Cleaning schedule entry entity or equivalent exists if entries are modeled separately.
- [ ] Entry entity contains generated ID.
- [ ] Entry entity belongs to a schedule or WG.
- [ ] Entry entity references assignee or assignees according to the design.
- [ ] Entry entity references task/chore if task integration exists.
- [ ] Entry entity contains planned date/due date/time window.
- [ ] Entry entity contains status if relevant.
- [ ] Entry entity uses correct JPA annotations.
- [ ] Entry entity uses correct relationship cardinality.
- [ ] Entry entity does not store unnecessary personal data.
- [ ] Entry entity does not expose sensitive information through `toString()`.

## Cleaning Schedule Repository

- [ ] Schedule repository or equivalent exists.
- [ ] Repository can save schedules.
- [ ] Repository can find schedule by ID.
- [ ] Repository can find schedules by WG/household.
- [ ] Repository can find schedules/entries by date range if needed.
- [ ] Repository does not return schedules across WG boundaries without service filtering.
- [ ] Repository methods are covered by tests.
- [ ] Repository tests use fake test data.
- [ ] Repository tests do not rely on real personal data.
- [ ] Repository tests clean up after themselves or use isolated transactions.

Notes:

```text

```

---

# 11. Mapper Check

- [ ] Cleaning schedule mapper exists if the project uses mappers.
- [ ] Mapper converts creation DTO to entity correctly where appropriate.
- [ ] Mapper converts update DTO to entity/update model correctly where appropriate.
- [ ] Mapper converts entity to response DTO correctly.
- [ ] Mapper does not map client-provided creator blindly.
- [ ] Mapper does not map client-provided WG ownership blindly.
- [ ] Mapper does not expose sensitive user fields.
- [ ] Mapper does not expose password fields from `ApplicationUser`.
- [ ] Mapper handles nullable optional fields safely.
- [ ] Mapper handles schedule entries correctly.
- [ ] Mapper handles assignee data correctly.
- [ ] Mapper is covered by unit tests if complex.

Notes:

```text

```

---

# 12. Security and Authorization Check

- [ ] Schedule creation endpoint requires authentication.
- [ ] Schedule retrieval endpoint requires authentication.
- [ ] Schedule update/delete endpoint requires authentication if implemented.
- [ ] Unauthenticated users receive the correct HTTP status.
- [ ] Users can only create schedules for their own WG.
- [ ] Users can only view schedules inside their own WG.
- [ ] Users can only update/delete schedules inside their own WG.
- [ ] Users cannot assign cleaning duties to users from another WG.
- [ ] Users cannot access schedules from another WG.
- [ ] Users cannot modify schedules from another WG.
- [ ] Users cannot fake another creator ID.
- [ ] Users cannot fake another WG ID.
- [ ] Users cannot escalate privileges through request DTOs.
- [ ] Main Tenant-only restrictions are enforced if schedule management is restricted by role.
- [ ] Roommate permissions are enforced if roommates are allowed to manage schedules.
- [ ] JWT authentication context is used to identify the current user.
- [ ] Security does not rely only on frontend hiding buttons.
- [ ] Security does not rely only on client-provided IDs.
- [ ] Error messages do not reveal whether private resources exist in another WG.
- [ ] Security behavior is covered by automated tests.

Notes:

```text

```

---

# 13. Error Handling Check

- [ ] Validation errors return a consistent error response.
- [ ] Malformed JSON returns a consistent error response.
- [ ] Missing schedule returns a safe not-found response.
- [ ] Missing schedule entry returns a safe not-found response if entries are separate.
- [ ] Missing assignee returns a safe not-found response.
- [ ] Missing WG/household returns a safe not-found response.
- [ ] Duplicate or conflicting schedule action returns a safe conflict response if relevant.
- [ ] Invalid recurrence/date data returns a safe validation error.
- [ ] Unauthorized request returns the correct status.
- [ ] Forbidden cross-WG request returns the correct status.
- [ ] Internal errors do not expose stack traces.
- [ ] Internal errors do not expose SQL/database details.
- [ ] Internal errors do not expose package/class names.
- [ ] Error response format is understandable for the frontend.
- [ ] Error response messages are safe for users.

Notes:

```text

```

---

# 14. Database and Persistence Check

- [ ] Cleaning schedule table/entity is created correctly.
- [ ] Cleaning schedule entry table/entity is created correctly if entries are separate.
- [ ] Schedule-WG/household relationship is persisted correctly.
- [ ] Schedule-creator relationship is persisted correctly if used.
- [ ] Schedule-entry relationship is persisted correctly.
- [ ] Entry-assignee relationship is persisted correctly.
- [ ] Entry-task relationship is persisted correctly if task integration exists.
- [ ] Required fields are non-null in the database where appropriate.
- [ ] Database constraints prevent invalid records where appropriate.
- [ ] Database migration exists if migrations are used.
- [ ] Migration works on a clean database if migrations are used.
- [ ] Migration does not break existing data if migrations are used.
- [ ] Failed schedule creation does not leave partial database rows.
- [ ] Failed schedule update does not leave partial database rows.
- [ ] Failed schedule entry creation does not leave partial database rows.
- [ ] Test database is isolated from development/production data.
- [ ] Tests clean up after themselves or use isolated transactions.
- [ ] No real personal data is stored in test data.
- [ ] Generated schedule data is fake/test data only.

Notes:

```text

```

---

# 15. JUnit Test Checklist

## Unit Tests — Schedule Creation

- [ ] Test valid schedule creation succeeds.
- [ ] Test created schedule has correct WG/household.
- [ ] Test created schedule has correct title/name if relevant.
- [ ] Test created schedule has correct date/period/recurrence data.
- [ ] Test created schedule has correct creator if relevant.
- [ ] Test created entries have correct assignees.
- [ ] Test created entries have correct dates.
- [ ] Test created entries reference correct tasks if relevant.
- [ ] Test missing required fields are rejected.
- [ ] Test invalid dates are rejected.
- [ ] Test invalid recurrence is rejected.
- [ ] Test nonexistent WG/household is rejected.
- [ ] Test creator outside WG is rejected.
- [ ] Test nonexistent assignee is rejected.
- [ ] Test assignee outside WG is rejected.
- [ ] Test repository save is called correctly or state is persisted correctly.

## Unit Tests — Schedule Retrieval

- [ ] Test valid schedule retrieval succeeds.
- [ ] Test retrieval returns only schedules from requested WG.
- [ ] Test retrieval rejects nonexistent schedule.
- [ ] Test retrieval rejects requester outside WG.
- [ ] Test empty schedule list is handled safely.
- [ ] Test date range filtering if implemented.
- [ ] Test upcoming/current filtering if implemented.

## Unit Tests — Schedule Update/Delete

- [ ] Test valid schedule update succeeds if implemented.
- [ ] Test update modifies correct fields.
- [ ] Test update rejects invalid dates/recurrence.
- [ ] Test update rejects requester outside WG.
- [ ] Test valid schedule deletion/cancellation succeeds if implemented.
- [ ] Test deletion/cancellation rejects requester outside WG.
- [ ] Test deletion/cancellation behavior follows project decision.

## Endpoint/API Tests

- [ ] Test schedule creation endpoint accepts valid JSON.
- [ ] Test schedule creation endpoint rejects invalid JSON.
- [ ] Test schedule creation endpoint returns correct status on success.
- [ ] Test schedule creation endpoint returns correct status on validation error.
- [ ] Test schedule creation endpoint returns correct status when unauthenticated.
- [ ] Test schedule creation endpoint returns correct status when forbidden.
- [ ] Test schedule retrieval endpoint returns correct status on success.
- [ ] Test schedule retrieval endpoint returns correct status when unauthenticated.
- [ ] Test schedule retrieval endpoint returns correct status when forbidden.
- [ ] Test schedule update/delete endpoints if implemented.
- [ ] Test API responses do not expose sensitive user data.
- [ ] Test API responses do not expose unnecessary internal fields.

## Repository Tests

- [ ] Test saving a schedule works.
- [ ] Test finding schedule by ID works.
- [ ] Test finding schedules by WG/household works.
- [ ] Test finding schedule entries by schedule works if applicable.
- [ ] Test finding entries by date range works if applicable.
- [ ] Test relationships are persisted correctly.
- [ ] Test database constraints work as expected.

## Integration Tests

- [ ] Test authenticated user creates schedule end-to-end.
- [ ] Test authenticated user retrieves schedule end-to-end.
- [ ] Test authenticated user creates schedule with entries end-to-end.
- [ ] Test user from another WG cannot create schedule in this WG.
- [ ] Test user from another WG cannot access schedule.
- [ ] Test user from another WG cannot be assigned to schedule entry.
- [ ] Test schedule persists after transaction.
- [ ] Test rollback/cleanup works between tests.
- [ ] Test backend works with PostgreSQL/test database.

## Security Tests

- [ ] Test unauthenticated user cannot create schedule.
- [ ] Test unauthenticated user cannot retrieve schedule.
- [ ] Test unauthenticated user cannot update/delete schedule if implemented.
- [ ] Test authenticated user can create schedule in own WG.
- [ ] Test authenticated user cannot create schedule in another WG.
- [ ] Test authenticated user cannot assign schedule duty to user from another WG.
- [ ] Test manipulated JWT is rejected.
- [ ] Test missing JWT is rejected.
- [ ] Test normal user cannot access privileged schedule endpoint if privilege distinction exists.

Notes:

```text

```

---

# 16. Manual Backend Testing

Use Postman, curl, IntelliJ HTTP client, Swagger, or another HTTP client.

- [ ] Manually create a fake WG/household test setup.
- [ ] Manually create or use fake test users in the same WG.
- [ ] Manually create or use a fake test user from another WG.
- [ ] Manually log in and obtain JWT/token.
- [ ] Manually create a valid cleaning schedule.
- [ ] Manually confirm schedule exists in database.
- [ ] Manually confirm schedule has correct WG/household.
- [ ] Manually confirm schedule has correct creator if stored.
- [ ] Manually confirm schedule entries have correct assignees.
- [ ] Manually retrieve schedule list.
- [ ] Manually retrieve one schedule if endpoint exists.
- [ ] Manually try to create a schedule without token.
- [ ] Manually try to create a schedule with invalid token.
- [ ] Manually try to create a schedule with invalid required data.
- [ ] Manually try to create a schedule with invalid date/recurrence data.
- [ ] Manually try to assign a schedule duty to user outside WG.
- [ ] Manually try to create schedule in another WG.
- [ ] Manually try malformed JSON request.
- [ ] Manually inspect backend logs for errors.
- [ ] Manually confirm no sensitive user data is leaked in logs.
- [ ] Manual test evidence is attached to the merge request.

Manual evidence:

```text
Tester:
Date:
Environment:
HTTP client:
Evidence link or notes:

```

---

# 17. GDPR and Privacy Check

- [ ] Backend collects only necessary data for cleaning schedule management.
- [ ] Backend collects only necessary data for schedule assignment.
- [ ] Backend does not collect sensitive personal data for FR-04.
- [ ] Schedule titles/descriptions are treated as user-provided data.
- [ ] Schedule titles/descriptions are not logged unnecessarily.
- [ ] Backend logs do not contain unnecessary personal data.
- [ ] Backend responses do not expose users from other WGs.
- [ ] Backend responses do not expose private user information unnecessarily.
- [ ] Test data uses fake users only.
- [ ] Test schedules use fake household data only.
- [ ] Screenshots and manual evidence do not expose real users.
- [ ] Access to schedule data is protected by authentication.
- [ ] Access to schedule data is restricted to the correct WG/household.
- [ ] Data deletion/history behavior is documented as implemented, future work, or out of scope.
- [ ] Purpose of schedule data is documented or assigned as a documentation task.

Notes:

```text

```

---

# 18. Non-Functional Backend Check

## Security

- [ ] Authentication works correctly.
- [ ] Authorization works correctly.
- [ ] Cross-WG access is prevented.
- [ ] Client-provided IDs are validated server-side.
- [ ] Error responses are safe.
- [ ] Secrets are not committed.
- [ ] Public endpoints are intentionally public.
- [ ] Protected endpoints are protected.

## Reliability

- [ ] Backend handles invalid schedule creation requests without crashing.
- [ ] Backend handles invalid update/delete requests without crashing.
- [ ] Backend handles nonexistent schedule IDs without crashing.
- [ ] Backend handles nonexistent user IDs without crashing.
- [ ] Backend handles duplicate or repeated requests safely.
- [ ] Backend starts successfully.
- [ ] Backend works after restart.
- [ ] Database persistence works.

## Performance

- [ ] Schedule creation response time is acceptable in local/dev environment.
- [ ] Schedule retrieval response time is acceptable in local/dev environment.
- [ ] Schedule update/delete response time is acceptable if implemented.
- [ ] No obvious unnecessary database calls are made.
- [ ] No obvious N+1 query problem exists for schedule response data.
- [ ] No obvious memory/resource leak appears during repeated requests.

## Maintainability

- [ ] Endpoint layer is not overloaded with business logic.
- [ ] Service layer contains business logic.
- [ ] Repository layer handles persistence.
- [ ] DTOs are used appropriately.
- [ ] Entities are not exposed unnecessarily.
- [ ] Mapping is centralized if mappers are used.
- [ ] Security logic is centralized.
- [ ] Error handling is centralized.
- [ ] Code naming is understandable.
- [ ] No commented-out dead code remains.
- [ ] No temporary debug logs remain.

Notes:

```text

```

---

# 19. Documentation Check

- [ ] Schedule creation endpoint is documented.
- [ ] Schedule retrieval endpoint is documented.
- [ ] Schedule update/delete endpoint is documented if implemented.
- [ ] Schedule entry endpoint is documented if implemented.
- [ ] Schedule creation request format is documented.
- [ ] Schedule update request format is documented if implemented.
- [ ] Schedule response format is documented.
- [ ] Error response format is documented.
- [ ] Required authentication is documented.
- [ ] Authorization rules are documented.
- [ ] WG membership restrictions are documented.
- [ ] Recurrence/date rules are documented.
- [ ] How to run backend tests is documented.
- [ ] Manual testing steps are documented.
- [ ] Known limitations are documented.
- [ ] FR-03 dependencies or integrations are documented.
- [ ] FR-15 dependencies or future extensions are documented.
- [ ] README or project documentation was updated if needed.

Notes:

```text

```

---

# 20. Backend CI and Merge Readiness

- [ ] Backend compiles.
- [ ] Backend starts.
- [ ] All JUnit unit tests pass.
- [ ] All JUnit endpoint/API tests pass.
- [ ] All JUnit integration tests pass.
- [ ] All security tests pass.
- [ ] Database tests pass.
- [ ] Docker build passes if configured.
- [ ] GitLab CI pipeline passes.
- [ ] No secrets are committed.
- [ ] No local config files are accidentally committed.
- [ ] No real user data is committed.
- [ ] No unrelated files are changed.
- [ ] Manual backend testing is complete.
- [ ] Backend documentation/API notes are updated.
- [ ] All critical backend items are checked.
- [ ] All unchecked items are documented with reason.
- [ ] Backend QA reviewer approves.

---

# 21. Blocking Issue Rules

If any item in this section is true, the backend part of FR-04 must be rejected.

- [ ] Backend does not compile.
- [ ] Backend does not start.
- [ ] Cleaning schedule creation does not work.
- [ ] Cleaning schedule retrieval does not work if required by frontend.
- [ ] Schedule entries are not persisted.
- [ ] Unauthenticated users can create schedules.
- [ ] Unauthenticated users can access schedules.
- [ ] Users can create schedules in another WG.
- [ ] Users can access schedules from another WG.
- [ ] Users can assign cleaning duties to users from another WG.
- [ ] Invalid schedule data creates a schedule anyway.
- [ ] Nonexistent assignee is accepted.
- [ ] Frontend/backend API contract is incompatible.
- [ ] JUnit tests fail without documented non-blocking reason.
- [ ] CI pipeline fails.
- [ ] Manual testing was not performed.
- [ ] Sensitive personal data is leaked.
- [ ] Real user data is committed.
- [ ] Secrets are committed.
- [ ] Critical security issue remains unresolved.
- [ ] Critical GDPR/privacy issue remains unresolved.
- [ ] Feature breaks existing registration/login/task functionality.

If any item above is checked, final decision must be:

```text
REJECTED — backend fixes required
```

Blocking issue notes:

```text

```

---

# Final Backend QA Decision

Choose one:

- [ ] APPROVED — backend can be merged
- [ ] REJECTED — backend fixes required
- [ ] APPROVED WITH MINOR NON-BLOCKING ISSUES

Final backend summary:

```text
Feature: FR-04 Cleaning schedule management

Backend compiles: YES/NO
Backend starts: YES/NO
JUnit tests passed: YES/NO
Manual backend testing completed: YES/NO
Security checks passed: YES/NO
GDPR/privacy checks passed: YES/NO
Documentation updated: YES/NO
CI pipeline passed: YES/NO

Critical issues:
Non-critical issues:
JUnit evidence:
Manual test evidence:
Security/GDPR notes:
Decision:
QA reviewer:
Date:
```
