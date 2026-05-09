# FR-03 Backend QA Checklist — Task Creation and Assignment

## Feature Information

- Feature ID: FR-03
- Feature name: Task creation and assignment
- Use case: UC-02 — Cleaning Schedule and Task Management
- Priority: High
- Category: Core
- Version: V2
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

This checklist must be completed before the backend part of FR-03 can be considered done.

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

Expected or relevant backend files for FR-03 may include:

- `endpoint/TaskEndpoint.java`
- `endpoint/dto/TaskCreateDto.java`
- `endpoint/dto/TaskUpdateDto.java`
- `endpoint/dto/TaskAssignmentDto.java`
- `endpoint/dto/TaskDetailDto.java`
- `endpoint/dto/TaskListDto.java`
- `entity/Task.java`
- `entity/ApplicationUser.java`
- `entity/Wg.java` or equivalent household/WG entity
- `repository/TaskRepository.java`
- `repository/UserRepository.java`
- `repository/WgRepository.java` or equivalent
- `service/TaskService.java`
- `service/impl/TaskServiceImpl.java`
- `mapper/TaskMapper.java`
- `security/JwtAuthorizationFilter.java`
- `security/JwtTokenizer.java`
- `config/SecurityConfig.java`
- `exceptionhandler/GlobalExceptionHandler.java`
- `exception/ConflictException.java`
- `exception/notFoundException.java`

If the actual file names differ, write the actual file names here:

```text
Actual task endpoint:
Actual task DTOs:
Actual task entity:
Actual task repository:
Actual task service:
Actual task mapper:
Other relevant files:
```

Whenever this checklist says “endpoint”, it means the REST class receiving HTTP requests.

---

# 1. Scope Check

- [ ] The branch only contains changes related to FR-03 or clearly justified support changes.
- [ ] Backend task creation is implemented.
- [ ] Backend task assignment is implemented.
- [ ] Task creation and task assignment belong to the correct WG/household context.
- [ ] Only authenticated users can create tasks.
- [ ] Only authorized WG members can create tasks for their own WG.
- [ ] Only authorized WG members can assign tasks within their own WG.
- [ ] The feature does not accidentally include unrelated FR-04 cleaning schedule logic unless clearly justified.
- [ ] The feature does not accidentally include unrelated FR-15 fair chore balancing logic unless clearly justified.
- [ ] The feature does not implement notifications/reminders unless clearly justified as integration support.
- [ ] The backend uses the existing project architecture instead of bypassing layers.
- [ ] The backend starts successfully after the changes.
- [ ] The backend compiles successfully.

Notes:

```text

```

---

# 2. Functional Acceptance Criteria

The backend part of FR-03 may only pass if these user-level outcomes work.

- [ ] An authenticated WG member can create a task.
- [ ] A task has at least a title/name.
- [ ] A task can include a description if this is part of the design.
- [ ] A task can include a due date/deadline if this is part of the design.
- [ ] A task can include priority/category if this is part of the design.
- [ ] A task can be assigned to one roommate if single assignment is the design.
- [ ] A task can be assigned to multiple roommates if multi-assignment is the design.
- [ ] A task cannot be assigned to a user outside the WG.
- [ ] A task cannot be created for a WG where the creator is not a member.
- [ ] A task cannot be assigned by an unauthenticated user.
- [ ] A task cannot be assigned to a nonexistent user.
- [ ] A task cannot be created with invalid required data.
- [ ] Created tasks are persisted in the database.
- [ ] Task assignment is persisted in the database.
- [ ] Created task can be retrieved after creation if retrieval endpoint is part of FR-03.
- [ ] Created task is associated with the correct WG.
- [ ] Created task is associated with the correct creator.
- [ ] Created task is associated with the correct assignee or assignees.
- [ ] Created task receives the correct default status.
- [ ] The backend prevents unauthorized cross-WG access.
- [ ] The implementation supports later FR-04 cleaning schedule management.
- [ ] The implementation supports later FR-13 task/payment history if relevant.
- [ ] The implementation supports later FR-15 fair chore balancing via history.

Notes:

```text

```

---

# 3. API Contract Check

Document and verify the task-related API contract.

## Task Creation Endpoint

- [ ] Task creation endpoint path is known and documented.
- [ ] Task creation endpoint uses the correct HTTP method.
- [ ] Task creation endpoint accepts JSON.
- [ ] Task creation endpoint requires authentication.
- [ ] Task creation endpoint uses a DTO, not the entity directly.
- [ ] Task creation request contains task title/name.
- [ ] Task creation request contains WG/household identifier if needed by the API design.
- [ ] Task creation request contains assignee identifier or identifiers if assignment happens during creation.
- [ ] Task creation request contains due date/deadline only if this is part of FR-03.
- [ ] Task creation request contains description only if this is part of FR-03.
- [ ] Task creation request does not allow the client to set internal-only fields.
- [ ] Task creation request does not allow the client to fake creator identity.
- [ ] Task creation request does not allow the client to set unauthorized WG ownership.
- [ ] Task creation success response has the correct HTTP status.
- [ ] Task creation validation error response has the correct HTTP status.
- [ ] Unauthorized task creation response has the correct HTTP status.
- [ ] Forbidden cross-WG task creation response has the correct HTTP status.
- [ ] Task creation response contains the created task identifier.
- [ ] Task creation response contains the task title/name.
- [ ] Task creation response contains the assigned user or users if relevant.
- [ ] Task creation response contains the default status if relevant.
- [ ] Task creation response does not expose sensitive user data.
- [ ] Task creation response does not expose unnecessary internal entity fields.

## Task Assignment Endpoint

- [ ] Task assignment endpoint path is known and documented if assignment is separate from creation.
- [ ] Task assignment endpoint uses the correct HTTP method if assignment is separate from creation.
- [ ] Task assignment endpoint accepts JSON if assignment is separate from creation.
- [ ] Task assignment endpoint requires authentication.
- [ ] Task assignment endpoint uses a DTO, not the entity directly.
- [ ] Task assignment request contains task identifier.
- [ ] Task assignment request contains assignee identifier or identifiers.
- [ ] Task assignment request does not allow assignment to users outside the WG.
- [ ] Task assignment request does not allow unauthorized users to assign tasks.
- [ ] Task assignment success response has the correct HTTP status.
- [ ] Task assignment validation error response has the correct HTTP status.
- [ ] Task assignment not-found response has the correct HTTP status.
- [ ] Task assignment forbidden response has the correct HTTP status.
- [ ] Task assignment response contains updated task assignment information.
- [ ] Task assignment response does not expose sensitive user data.
- [ ] Task assignment response does not expose unnecessary internal entity fields.

## Task Retrieval Endpoint

- [ ] Task retrieval endpoint exists if needed by the frontend for FR-03.
- [ ] Task retrieval endpoint requires authentication.
- [ ] Task retrieval endpoint only returns tasks from the authenticated user’s WG.
- [ ] Task retrieval endpoint does not return tasks from other WGs.
- [ ] Task retrieval response contains enough data for the frontend to display created/assigned tasks.
- [ ] Task retrieval response does not expose sensitive user data.
- [ ] Task retrieval response does not expose unnecessary internal entity fields.

Notes:

```text

```

---

# 4. DTO Validation Check

## Task Creation DTO

- [ ] Task creation DTO exists.
- [ ] Task creation DTO contains title/name.
- [ ] Task creation DTO contains description only if needed.
- [ ] Task creation DTO contains due date/deadline only if needed.
- [ ] Task creation DTO contains assignee ID or IDs if assignment happens during creation.
- [ ] Task creation DTO contains WG/household ID if needed.
- [ ] Task creation DTO does not contain creator ID unless explicitly justified.
- [ ] Task creation DTO does not contain internal database ID for fields that should be generated.
- [ ] Task creation DTO does not contain status if status should be assigned by backend.
- [ ] Task creation DTO does not contain fields for unauthorized role manipulation.
- [ ] Empty title/name is rejected.
- [ ] Blank title/name is rejected.
- [ ] Extremely long title/name is rejected or handled safely.
- [ ] Extremely long description is rejected or handled safely.
- [ ] Invalid due date format is rejected if due date exists.
- [ ] Past due date is rejected or allowed according to project decision.
- [ ] Missing assignee is rejected if assignment is required.
- [ ] Empty assignee list is rejected if assignment is required.
- [ ] Duplicate assignee IDs are rejected or normalized if multi-assignment exists.
- [ ] DTO validation annotations are used where appropriate.

## Task Assignment DTO

- [ ] Task assignment DTO exists if assignment is separate from creation.
- [ ] Task assignment DTO contains assignee ID or IDs.
- [ ] Task assignment DTO does not contain unauthorized internal fields.
- [ ] Missing assignee is rejected.
- [ ] Nonexistent assignee ID is handled safely.
- [ ] Assignee from another WG is rejected.
- [ ] Duplicate assignee IDs are rejected or normalized if multi-assignment exists.
- [ ] DTO validation annotations are used where appropriate.

## Task Response DTO

- [ ] Task response DTO exists.
- [ ] Task response DTO contains task ID.
- [ ] Task response DTO contains title/name.
- [ ] Task response DTO contains description if relevant.
- [ ] Task response DTO contains due date/deadline if relevant.
- [ ] Task response DTO contains status if relevant.
- [ ] Task response DTO contains creator information only if needed.
- [ ] Task response DTO contains assignee information needed by the frontend.
- [ ] Task response DTO does not expose password fields.
- [ ] Task response DTO does not expose sensitive user data.
- [ ] Task response DTO does not expose unnecessary internal entity fields.

Notes:

```text

```

---

# 5. Task Creation Functional Check

- [ ] Authenticated user can create a task with valid data.
- [ ] Created task is saved in the database.
- [ ] Created task has the correct title/name.
- [ ] Created task has the correct description if provided.
- [ ] Created task has the correct due date/deadline if provided.
- [ ] Created task has the correct WG/household association.
- [ ] Created task has the correct creator.
- [ ] Created task has the correct assignee or assignees.
- [ ] Created task receives the correct default status.
- [ ] Created task receives created timestamp if timestamps exist.
- [ ] Created task receives updated timestamp if timestamps exist.
- [ ] Task creation rejects missing title/name.
- [ ] Task creation rejects blank title/name.
- [ ] Task creation rejects invalid assignee.
- [ ] Task creation rejects nonexistent assignee.
- [ ] Task creation rejects assignee from another WG.
- [ ] Task creation rejects creation for a WG where the user is not a member.
- [ ] Task creation rejects unauthenticated requests.
- [ ] Task creation handles malformed JSON safely.
- [ ] Task creation handles invalid request body safely.
- [ ] Task creation does not create partial records after failure.
- [ ] Task creation is transactional where needed.
- [ ] Task creation works after backend restart.
- [ ] Task creation works with the configured PostgreSQL/test database.

Notes:

```text

```

---

# 6. Task Assignment Functional Check

- [ ] Authenticated authorized user can assign a task.
- [ ] Task assignment is saved in the database.
- [ ] Task assignment updates the correct task.
- [ ] Task assignment does not accidentally create a duplicate task.
- [ ] Task assignment does not modify unrelated task fields.
- [ ] Task assignment rejects nonexistent task.
- [ ] Task assignment rejects nonexistent assignee.
- [ ] Task assignment rejects assignee from another WG.
- [ ] Task assignment rejects assignment by user outside the WG.
- [ ] Task assignment rejects unauthenticated requests.
- [ ] Task assignment rejects users without required permission if permission levels exist.
- [ ] Reassigning a task works according to project decision.
- [ ] Reassigning a task preserves history if history is already in scope.
- [ ] Reassigning a task does not preserve history if history is explicitly out of scope.
- [ ] Multi-assignment works if multiple assignees are supported.
- [ ] Single-assignment rejects multiple assignees if only one assignee is supported.
- [ ] Assignment is transactional where needed.
- [ ] Assignment works after backend restart.
- [ ] Assignment works with persisted database data.

Notes:

```text

```

---

# 7. Endpoint Layer Check

## Task Endpoint

- [ ] Task endpoint exists.
- [ ] Task endpoint is placed in the `endpoint` package.
- [ ] Task endpoint uses clear route names.
- [ ] Task endpoint uses correct HTTP methods.
- [ ] Task endpoint requires authentication.
- [ ] Task endpoint accepts DTOs instead of entities.
- [ ] Task endpoint returns DTOs instead of entities where appropriate.
- [ ] Task endpoint calls `TaskService`.
- [ ] Task endpoint does not call `TaskRepository` directly unless clearly justified.
- [ ] Task endpoint does not contain database logic directly.
- [ ] Task endpoint does not contain complex business logic directly.
- [ ] Task endpoint handles successful task creation correctly.
- [ ] Task endpoint handles successful task assignment correctly.
- [ ] Task endpoint handles validation errors correctly.
- [ ] Task endpoint handles not-found errors correctly.
- [ ] Task endpoint handles forbidden access correctly.
- [ ] Task endpoint handles unauthenticated access correctly.
- [ ] Task endpoint does not expose sensitive user fields.
- [ ] Task endpoint does not expose unnecessary internal entity fields.

Notes:

```text

```

---

# 8. Service Layer Check

## Task Service

- [ ] `TaskService` or equivalent exists.
- [ ] Task creation business logic is in the service layer.
- [ ] Task assignment business logic is in the service layer.
- [ ] Service verifies that the authenticated user belongs to the WG.
- [ ] Service verifies that the assignee belongs to the same WG.
- [ ] Service verifies that the task belongs to the correct WG before assignment.
- [ ] Service rejects task access across WG boundaries.
- [ ] Service validates business rules that cannot be trusted to the frontend.
- [ ] Service assigns creator from authenticated user context, not from unsafe client input.
- [ ] Service assigns default task status.
- [ ] Service saves task through `TaskRepository`.
- [ ] Service uses `UserRepository` or equivalent to validate assignees.
- [ ] Service uses `WgRepository` or equivalent to validate WG membership.
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

# 9. Repository and Entity Check

## Task Entity

- [ ] `Task` entity or equivalent exists.
- [ ] Task entity contains generated ID.
- [ ] Task entity contains title/name.
- [ ] Task entity contains description if relevant.
- [ ] Task entity contains due date/deadline if relevant.
- [ ] Task entity contains status if relevant.
- [ ] Task entity contains creator relation if relevant.
- [ ] Task entity contains assignee relation or assignment relation.
- [ ] Task entity contains WG/household relation.
- [ ] Task entity uses correct JPA annotations.
- [ ] Task entity uses correct relationship cardinality.
- [ ] Task entity does not store unnecessary personal data.
- [ ] Task entity does not expose sensitive information through `toString()`.
- [ ] Task entity supports future cleaning schedule logic from FR-04.
- [ ] Task entity supports future task history logic from FR-13/FR-15 if relevant.

## Task Repository

- [ ] `TaskRepository` or equivalent exists.
- [ ] Repository can save tasks.
- [ ] Repository can find task by ID.
- [ ] Repository can find tasks by WG/household.
- [ ] Repository can find tasks assigned to a user if needed.
- [ ] Repository does not return tasks across WG boundaries without service filtering.
- [ ] Repository methods are covered by tests.
- [ ] Repository tests use fake test data.
- [ ] Repository tests do not rely on real personal data.
- [ ] Repository tests clean up after themselves or use isolated transactions.

Notes:

```text

```

---

# 10. Mapper Check

- [ ] Task mapper exists if the project uses mappers.
- [ ] Mapper converts creation DTO to entity correctly.
- [ ] Mapper converts entity to response DTO correctly.
- [ ] Mapper does not map client-provided creator blindly.
- [ ] Mapper does not map client-provided WG ownership blindly.
- [ ] Mapper does not expose sensitive user fields.
- [ ] Mapper does not expose password fields from `ApplicationUser`.
- [ ] Mapper handles nullable optional fields safely.
- [ ] Mapper handles assignee data correctly.
- [ ] Mapper is covered by unit tests if complex.

Notes:

```text

```

---

# 11. Security and Authorization Check

- [ ] Task creation endpoint requires authentication.
- [ ] Task assignment endpoint requires authentication.
- [ ] Task retrieval endpoint requires authentication if implemented.
- [ ] Unauthenticated users receive the correct HTTP status.
- [ ] Users can only create tasks for their own WG.
- [ ] Users can only assign tasks inside their own WG.
- [ ] Users cannot assign tasks to users from another WG.
- [ ] Users cannot access tasks from another WG.
- [ ] Users cannot modify tasks from another WG.
- [ ] Users cannot fake another creator ID.
- [ ] Users cannot fake another WG ID.
- [ ] Users cannot escalate privileges through task request DTOs.
- [ ] Main Tenant-only restrictions are enforced if task creation/assignment is restricted by role.
- [ ] Roommate permissions are enforced if roommates are allowed to create/assign tasks.
- [ ] JWT authentication context is used to identify the current user.
- [ ] Security does not rely only on frontend hiding buttons.
- [ ] Security does not rely only on client-provided IDs.
- [ ] Error messages do not reveal whether private resources exist in another WG.
- [ ] Security behavior is covered by automated tests.

Notes:

```text

```

---

# 12. Error Handling Check

## Global Exception Handling

- [ ] Validation errors return a consistent error response.
- [ ] Malformed JSON returns a consistent error response.
- [ ] Missing task returns a safe not-found response.
- [ ] Missing assignee returns a safe not-found response.
- [ ] Missing WG/household returns a safe not-found response.
- [ ] Duplicate or conflicting task action returns a safe conflict response if relevant.
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

# 13. Database and Persistence Check

- [ ] Task table/entity is created correctly.
- [ ] Task-WG/household relationship is persisted correctly.
- [ ] Task-creator relationship is persisted correctly if used.
- [ ] Task-assignee relationship is persisted correctly.
- [ ] Single-assignee relationship is modeled correctly if single assignment is used.
- [ ] Multi-assignee relationship is modeled correctly if multiple assignment is used.
- [ ] Required task fields are non-null in the database where appropriate.
- [ ] Database constraints prevent invalid task records where appropriate.
- [ ] Database migration exists if migrations are used.
- [ ] Migration works on a clean database if migrations are used.
- [ ] Migration does not break existing data if migrations are used.
- [ ] Failed task creation does not leave partial database rows.
- [ ] Failed task assignment does not leave partial database rows.
- [ ] Test database is isolated from development/production data.
- [ ] Tests clean up after themselves or use isolated transactions.
- [ ] No real personal data is stored in test data.
- [ ] Generated task data is fake/test data only.

Notes:

```text

```

---

# 14. JUnit Test Checklist

## Unit Tests — Task Creation

- [ ] Test valid task creation succeeds.
- [ ] Test created task has correct title/name.
- [ ] Test created task has correct description if relevant.
- [ ] Test created task has correct due date/deadline if relevant.
- [ ] Test created task has correct creator.
- [ ] Test created task has correct WG/household.
- [ ] Test created task has correct assignee or assignees.
- [ ] Test created task has correct default status.
- [ ] Test missing title/name is rejected.
- [ ] Test blank title/name is rejected.
- [ ] Test invalid due date/deadline is rejected if relevant.
- [ ] Test nonexistent WG/household is rejected.
- [ ] Test creator outside WG is rejected.
- [ ] Test nonexistent assignee is rejected.
- [ ] Test assignee outside WG is rejected.
- [ ] Test repository save is called correctly or state is persisted correctly.

## Unit Tests — Task Assignment

- [ ] Test valid task assignment succeeds.
- [ ] Test assignment updates the correct task.
- [ ] Test assignment stores correct assignee.
- [ ] Test assignment rejects nonexistent task.
- [ ] Test assignment rejects nonexistent assignee.
- [ ] Test assignment rejects assignee outside WG.
- [ ] Test assignment rejects requester outside WG.
- [ ] Test reassignment behavior follows project decision.
- [ ] Test multi-assignment behavior if supported.
- [ ] Test single-assignment rejects multiple assignees if only one assignee is supported.

## Endpoint/API Tests

- [ ] Test task creation endpoint accepts valid JSON.
- [ ] Test task creation endpoint rejects invalid JSON.
- [ ] Test task creation endpoint returns correct status on success.
- [ ] Test task creation endpoint returns correct status on validation error.
- [ ] Test task creation endpoint returns correct status when unauthenticated.
- [ ] Test task creation endpoint returns correct status when forbidden.
- [ ] Test task assignment endpoint accepts valid JSON if separate endpoint exists.
- [ ] Test task assignment endpoint rejects invalid JSON if separate endpoint exists.
- [ ] Test task assignment endpoint returns correct status on success if separate endpoint exists.
- [ ] Test task assignment endpoint returns correct status when task not found.
- [ ] Test task assignment endpoint returns correct status when assignee not found.
- [ ] Test task assignment endpoint returns correct status when forbidden.
- [ ] Test API responses do not expose sensitive user data.
- [ ] Test API responses do not expose unnecessary internal fields.

## Repository Tests

- [ ] Test saving a task works.
- [ ] Test finding task by ID works.
- [ ] Test finding tasks by WG/household works.
- [ ] Test finding tasks assigned to user works if needed.
- [ ] Test task relationships are persisted correctly.
- [ ] Test database constraints work as expected.

## Integration Tests

- [ ] Test authenticated user creates task end-to-end.
- [ ] Test authenticated user creates and assigns task end-to-end.
- [ ] Test created task can be retrieved after creation if retrieval endpoint exists.
- [ ] Test user from another WG cannot create task in this WG.
- [ ] Test user from another WG cannot be assigned.
- [ ] Test user from another WG cannot retrieve task.
- [ ] Test task persists after transaction.
- [ ] Test rollback/cleanup works between tests.
- [ ] Test backend works with PostgreSQL/test database.

## Security Tests

- [ ] Test unauthenticated user cannot create task.
- [ ] Test unauthenticated user cannot assign task.
- [ ] Test authenticated user can create task in own WG.
- [ ] Test authenticated user cannot create task in another WG.
- [ ] Test authenticated user cannot assign task to user from another WG.
- [ ] Test manipulated JWT is rejected.
- [ ] Test missing JWT is rejected.
- [ ] Test normal user cannot access privileged task endpoint if privilege distinction exists.

Notes:

```text

```

---

# 15. Manual Backend Testing

Use Postman, curl, IntelliJ HTTP client, Swagger, or another HTTP client.

- [ ] Manually create a fake WG/household test setup.
- [ ] Manually create or use fake test users in the same WG.
- [ ] Manually create or use a fake test user from another WG.
- [ ] Manually log in and obtain JWT/token.
- [ ] Manually create a valid task.
- [ ] Manually confirm task exists in database.
- [ ] Manually confirm task has correct WG/household.
- [ ] Manually confirm task has correct creator.
- [ ] Manually confirm task has correct assignee.
- [ ] Manually try to create a task without token.
- [ ] Manually try to create a task with invalid token.
- [ ] Manually try to create a task with missing title/name.
- [ ] Manually try to create a task with invalid assignee.
- [ ] Manually try to assign task to user outside WG.
- [ ] Manually try to create task in another WG.
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

# 16. GDPR and Privacy Check

- [ ] Backend collects only necessary data for task creation.
- [ ] Backend collects only necessary data for task assignment.
- [ ] Backend does not collect sensitive personal data for FR-03.
- [ ] Task descriptions are treated as user-provided data.
- [ ] Task descriptions are not logged unnecessarily.
- [ ] Task titles are not logged unnecessarily.
- [ ] Backend logs do not contain unnecessary personal data.
- [ ] Backend responses do not expose users from other WGs.
- [ ] Backend responses do not expose private user information unnecessarily.
- [ ] Test data uses fake users only.
- [ ] Test tasks use fake household data only.
- [ ] Screenshots and manual evidence do not expose real users.
- [ ] Access to task data is protected by authentication.
- [ ] Access to task data is restricted to the correct WG/household.
- [ ] Data deletion/history behavior is documented as implemented, future work, or out of scope.
- [ ] Purpose of task data is documented or assigned as a documentation task.

Notes:

```text

```

---

# 17. Non-Functional Backend Check

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

- [ ] Backend handles invalid task creation requests without crashing.
- [ ] Backend handles invalid assignment requests without crashing.
- [ ] Backend handles nonexistent task IDs without crashing.
- [ ] Backend handles nonexistent user IDs without crashing.
- [ ] Backend handles duplicate or repeated requests safely.
- [ ] Backend starts successfully.
- [ ] Backend works after restart.
- [ ] Database persistence works.

## Performance

- [ ] Task creation response time is acceptable in local/dev environment.
- [ ] Task assignment response time is acceptable in local/dev environment.
- [ ] Task retrieval response time is acceptable if retrieval is implemented.
- [ ] No obvious unnecessary database calls are made.
- [ ] No obvious N+1 query problem exists for task response data.
- [ ] No obvious memory/resource leak appears during repeated task requests.

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

# 18. Documentation Check

- [ ] Task creation endpoint is documented.
- [ ] Task assignment endpoint is documented.
- [ ] Task retrieval endpoint is documented if implemented.
- [ ] Task creation request format is documented.
- [ ] Task assignment request format is documented.
- [ ] Task response format is documented.
- [ ] Error response format is documented.
- [ ] Required authentication is documented.
- [ ] Authorization rules are documented.
- [ ] WG membership restrictions are documented.
- [ ] How to run backend tests is documented.
- [ ] Manual testing steps are documented.
- [ ] Known limitations are documented.
- [ ] FR-04 dependencies or future extensions are documented.
- [ ] README or project documentation was updated if needed.

Notes:

```text

```

---

# 19. Backend CI and Merge Readiness

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

# 20. Blocking Issue Rules

If any item in this section is true, the backend part of FR-03 must be rejected.

- [ ] Backend does not compile.
- [ ] Backend does not start.
- [ ] Task creation does not work.
- [ ] Task assignment does not work.
- [ ] Unauthenticated users can create tasks.
- [ ] Unauthenticated users can assign tasks.
- [ ] Users can create tasks in another WG.
- [ ] Users can assign tasks to users from another WG.
- [ ] Users can access tasks from another WG.
- [ ] Task is not persisted after creation.
- [ ] Assignment is not persisted.
- [ ] Invalid task data creates a task anyway.
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
- [ ] Feature breaks existing registration/login functionality.

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
Feature: FR-03 Task creation and assignment

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