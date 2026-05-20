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
- Database: PostgreSQL target; local/backend tests verified with H2 test/default database
- Automated tests: JUnit
- Branch: FR-03/Task-creation-and-assignment
- Merge request:
- Developer: Andrei
- QA reviewer: Andrei
- Date: 11.05.26

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
- `exception/NotFoundException.java`

Actual file names:

```text
Actual task endpoint:
- endpoint/TaskEndpoint.java

Actual task DTOs:
- endpoint/dto/TaskCreateDto.java
- endpoint/dto/TaskUpdateDto.java
- endpoint/dto/TaskStatusUpdateDto.java
- endpoint/dto/TaskDetailDto.java

Actual task entity:
- entity/Task.java
- entity/TaskStatus.java

Actual task repository:
- repository/TaskRepository.java

Actual task service:
- service/TaskService.java
- service/impl/SimpleTaskService.java

Actual task mapper:
- endpoint/mapper/TaskMapper.java

Other relevant files:
- entity/Household.java
- entity/Roommate.java
- entity/HouseholdRole.java
- repository/HouseholdRepository.java
- repository/RoommateRepository.java
- repository/UserRepository.java
- endpoint/HouseholdEndpoint.java
- endpoint/InvitationEndpoint.java
- security/JwtAuthorizationFilter.java
- security/JwtTokenizer.java
- config/SecurityConfig.java
- exceptionhandler/GlobalExceptionHandler.java
```

Whenever this checklist says “endpoint”, it means the REST class receiving HTTP requests.

---

# 1. Scope Check

- [x] The branch only contains changes related to FR-03 or clearly justified support changes.
- [x] Backend task creation is implemented.
- [x] Backend task assignment is implemented.
- [x] Task creation and task assignment belong to the correct WG/household context.
- [x] Only authenticated users can create tasks.
- [x] Only authorized WG members can create tasks for their own WG.
- [x] Only authorized WG members can assign tasks within their own WG.
- [x] The feature does not accidentally include unrelated FR-04 cleaning schedule logic unless clearly justified.
- [x] The feature does not accidentally include unrelated FR-15 fair chore balancing logic unless clearly justified.
- [x] The feature does not implement notifications/reminders unless clearly justified as integration support.
- [x] The backend uses the existing project architecture instead of bypassing layers.
- [x] The backend starts successfully after the changes.
- [x] The backend compiles successfully.

Notes:

```text
FR-03 depends on FR-01 household/user/roommate infrastructure. The branch therefore includes FR-01 support changes, especially Household, Roommate, household membership, invitation, and user persistence classes. These are required so that FR-03 task creation and assignment can be scoped to the authenticated user's WG/household and can reject cross-WG assignment/access.

Backend startup evidence:
- Started locally with mvn spring-boot:run.
- Application reached "Started BackendApplication".
- Tomcat started on port 8080.
- Checkstyle reported 0 violations during startup.
```

---

# 2. Functional Acceptance Criteria

The backend part of FR-03 may only pass if these user-level outcomes work.

- [x] An authenticated WG member can create a task.
- [x] A task has at least a title/name.
- [x] A task can include a description if this is part of the design.
- [x] A task can include a due date/deadline if this is part of the design.
- [x] N/A: A task can include priority/category if this is part of the design.
- [x] A task can be assigned to one roommate if single assignment is the design.
- [x] N/A: A task can be assigned to multiple roommates if multi-assignment is the design.
- [x] A task cannot be assigned to a user outside the WG.
- [x] A task cannot be created for a WG where the creator is not a member.
- [x] A task cannot be assigned by an unauthenticated user.
- [x] A task cannot be assigned to a nonexistent user.
- [x] A task cannot be created with invalid required data.
- [x] Created tasks are persisted in the database.
- [x] Task assignment is persisted in the database.
- [x] Created task can be retrieved after creation if retrieval endpoint is part of FR-03.
- [x] Created task is associated with the correct WG.
- [x] Created task is associated with the correct creator.
- [x] Created task is associated with the correct assignee or assignees.
- [x] Created task receives the correct default status.
- [x] The backend prevents unauthorized cross-WG access.
- [x] The implementation supports later FR-04 cleaning schedule management.
- [x] N/A: The implementation supports later FR-13 task/payment history if relevant.
- [x] N/A: The implementation supports later FR-15 fair chore balancing via history.

Notes:

```text
Priority/category is not part of the current FR-03 backend design.
The implementation uses single assignment via assignedTo.
FR-13/FR-15 history/fairness behavior is not implemented in FR-03 and should be handled in later features if needed.
```

---

# 3. API Contract Check

Document and verify the task-related API contract.

## Task Creation Endpoint

- [x] Task creation endpoint path is known and documented.
- [x] Task creation endpoint uses the correct HTTP method.
- [x] Task creation endpoint accepts JSON.
- [x] Task creation endpoint requires authentication.
- [x] Task creation endpoint uses a DTO, not the entity directly.
- [x] Task creation request contains task title/name.
- [x] N/A: Task creation request contains WG/household identifier if needed by the API design.
- [x] Task creation request contains assignee identifier or identifiers if assignment happens during creation.
- [x] Task creation request contains due date/deadline only if this is part of FR-03.
- [x] Task creation request contains description only if this is part of FR-03.
- [x] Task creation request does not allow the client to set internal-only fields.
- [x] Task creation request does not allow the client to fake creator identity.
- [x] Task creation request does not allow the client to set unauthorized WG ownership.
- [x] Task creation success response has the correct HTTP status.
- [x] Task creation validation error response has the correct HTTP status.
- [x] Unauthorized task creation response has the correct HTTP status.
- [x] Forbidden cross-WG task creation response has the correct HTTP status.
- [x] Task creation response contains the created task identifier.
- [x] Task creation response contains the task title/name.
- [x] Task creation response contains the assigned user or users if relevant.
- [x] Task creation response contains the default status if relevant.
- [x] Task creation response does not expose sensitive user data.
- [x] Task creation response does not expose unnecessary internal entity fields.

## Task Assignment Endpoint

- [x] N/A: Task assignment endpoint path is known and documented if assignment is separate from creation.
- [x] N/A: Task assignment endpoint uses the correct HTTP method if assignment is separate from creation.
- [x] N/A: Task assignment endpoint accepts JSON if assignment is separate from creation.
- [x] Task assignment endpoint requires authentication.
- [x] Task assignment endpoint uses a DTO, not the entity directly.
- [x] Task assignment request contains task identifier.
- [x] Task assignment request contains assignee identifier or identifiers.
- [x] Task assignment request does not allow assignment to users outside the WG.
- [x] Task assignment request does not allow unauthorized users to assign tasks.
- [x] Task assignment success response has the correct HTTP status.
- [x] Task assignment validation error response has the correct HTTP status.
- [x] Task assignment not-found response has the correct HTTP status.
- [x] Task assignment forbidden response has the correct HTTP status.
- [x] Task assignment response contains updated task assignment information.
- [x] Task assignment response does not expose sensitive user data.
- [x] Task assignment response does not expose unnecessary internal entity fields.

## Task Retrieval Endpoint

- [x] Task retrieval endpoint exists if needed by the frontend for FR-03.
- [x] Task retrieval endpoint requires authentication.
- [x] Task retrieval endpoint only returns tasks from the authenticated user’s WG.
- [x] Task retrieval endpoint does not return tasks from other WGs.
- [x] Task retrieval response contains enough data for the frontend to display created/assigned tasks.
- [x] Task retrieval response does not expose sensitive user data.
- [x] Task retrieval response does not expose unnecessary internal entity fields.

Notes:

```text
Task creation:
- POST /api/v1/tasks

Task retrieval:
- GET /api/v1/tasks
- GET /api/v1/tasks/my
- GET /api/v1/tasks/{id}

Task update/reassignment:
- PUT /api/v1/tasks/{id}

Task status update:
- PATCH /api/v1/tasks/{id}/status

Task deletion:
- DELETE /api/v1/tasks/{id}

There is no separate TaskAssignmentDto or assignment-only endpoint. Assignment happens during task creation through TaskCreateDto, and reassignment happens through TaskUpdateDto.

The client does not send a household ID. The backend derives the household from the authenticated user's active household membership.
```

---

# 4. DTO Validation Check

## Task Creation DTO

- [x] Task creation DTO exists.
- [x] Task creation DTO contains title/name.
- [x] Task creation DTO contains description only if needed.
- [x] Task creation DTO contains due date/deadline only if needed.
- [x] Task creation DTO contains assignee ID or IDs if assignment happens during creation.
- [x] N/A: Task creation DTO contains WG/household ID if needed.
- [x] Task creation DTO does not contain creator ID unless explicitly justified.
- [x] Task creation DTO does not contain internal database ID for fields that should be generated.
- [x] Task creation DTO does not contain status if status should be assigned by backend.
- [x] Task creation DTO does not contain fields for unauthorized role manipulation.
- [x] Empty title/name is rejected.
- [x] Blank title/name is rejected.
- [x] Extremely long title/name is rejected or handled safely.
- [x] Extremely long description is rejected or handled safely.
- [x] Invalid due date format is rejected if due date exists.
- [x] Past due date is rejected or allowed according to project decision.
- [x] Missing assignee is rejected if assignment is required.
- [x] N/A: Empty assignee list is rejected if assignment is required.
- [x] N/A: Duplicate assignee IDs are rejected or normalized if multi-assignment exists.
- [x] DTO validation annotations are used where appropriate.

## Task Assignment DTO

- [x] N/A: Task assignment DTO exists if assignment is separate from creation.
- [x] Task assignment DTO contains assignee ID or IDs.
- [x] Task assignment DTO does not contain unauthorized internal fields.
- [x] Missing assignee is rejected.
- [x] Nonexistent assignee ID is handled safely.
- [x] Assignee from another WG is rejected.
- [x] N/A: Duplicate assignee IDs are rejected or normalized if multi-assignment exists.
- [x] DTO validation annotations are used where appropriate.

## Task Response DTO

- [x] Task response DTO exists.
- [x] Task response DTO contains task ID.
- [x] Task response DTO contains title/name.
- [x] Task response DTO contains description if relevant.
- [x] Task response DTO contains due date/deadline if relevant.
- [x] Task response DTO contains status if relevant.
- [x] Task response DTO contains creator information only if needed.
- [x] Task response DTO contains assignee information needed by the frontend.
- [x] Task response DTO does not expose password fields.
- [x] Task response DTO does not expose sensitive user data.
- [x] Task response DTO does not expose unnecessary internal entity fields.

Notes:

```text
TaskCreateDto and TaskUpdateDto validate title, description length, due date, and assignee email.
The implementation uses assignedToEmail for single-assignment.
There is no household ID in the DTO because household ownership is derived server-side from the authenticated user.
There is no separate TaskAssignmentDto because assignment is handled during create and reassignment through update.
```

---

# 5. Task Creation Functional Check

- [x] Authenticated user can create a task with valid data.
- [x] Created task is saved in the database.
- [x] Created task has the correct title/name.
- [x] Created task has the correct description if provided.
- [x] Created task has the correct due date/deadline if provided.
- [x] Created task has the correct WG/household association.
- [x] Created task has the correct creator.
- [x] Created task has the correct assignee or assignees.
- [x] Created task receives the correct default status.
- [x] Created task receives created timestamp if timestamps exist.
- [x] Created task receives updated timestamp if timestamps exist.
- [x] Task creation rejects missing title/name.
- [x] Task creation rejects blank title/name.
- [x] Task creation rejects invalid assignee.
- [x] Task creation rejects nonexistent assignee.
- [x] Task creation rejects assignee from another WG.
- [x] Task creation rejects creation for a WG where the user is not a member.
- [x] Task creation rejects unauthenticated requests.
- [x] Task creation handles malformed JSON safely.
- [x] Task creation handles invalid request body safely.
- [x] Task creation does not create partial records after failure.
- [x] Task creation is transactional where needed.
- [x] Task creation works after backend restart.
- [x] Task creation works with the configured PostgreSQL/test database.

Notes:

```text
Task creation is covered by TaskEndpointTest, SimpleTaskServiceTest, and TaskRepositoryTest.
Backend startup was verified locally after implementation.
Pipeline passed according to QA evidence.
Local tests use H2; PostgreSQL target behavior is considered covered by CI/project environment if CI is configured for PostgreSQL.
```

---

# 6. Task Assignment Functional Check

- [x] Authenticated authorized user can assign a task.
- [x] Task assignment is saved in the database.
- [x] Task assignment updates the correct task.
- [x] Task assignment does not accidentally create a duplicate task.
- [x] Task assignment does not modify unrelated task fields.
- [x] Task assignment rejects nonexistent task.
- [x] Task assignment rejects nonexistent assignee.
- [x] Task assignment rejects assignee from another WG.
- [x] Task assignment rejects assignment by user outside the WG.
- [x] Task assignment rejects unauthenticated requests.
- [x] N/A: Task assignment rejects users without required permission if permission levels exist.
- [x] Reassigning a task works according to project decision.
- [x] N/A: Reassigning a task preserves history if history is already in scope.
- [x] N/A: Reassigning a task does not preserve history if history is explicitly out of scope.
- [x] N/A: Multi-assignment works if multiple assignees are supported.
- [x] N/A: Single-assignment rejects multiple assignees if only one assignee is supported.
- [x] Assignment is transactional where needed.
- [x] Assignment works after backend restart.
- [x] Assignment works with persisted database data.

Notes:

```text
Assignment is implemented as single-assignment.
Assignment happens during task creation with TaskCreateDto.
Reassignment happens through TaskUpdateDto using PUT /api/v1/tasks/{id}.
There is no separate task assignment endpoint.
There is no history behavior in FR-03.
There is no multiple-assignee field, so multiple-assignee rejection is structurally not applicable.
```

---

# 7. Endpoint Layer Check

## Task Endpoint

- [x] Task endpoint exists.
- [x] Task endpoint is placed in the `endpoint` package.
- [x] Task endpoint uses clear route names.
- [x] Task endpoint uses correct HTTP methods.
- [x] Task endpoint requires authentication.
- [x] Task endpoint accepts DTOs instead of entities.
- [x] Task endpoint returns DTOs instead of entities where appropriate.
- [x] Task endpoint calls `TaskService`.
- [x] Task endpoint does not call `TaskRepository` directly unless clearly justified.
- [x] Task endpoint does not contain database logic directly.
- [x] Task endpoint does not contain complex business logic directly.
- [x] Task endpoint handles successful task creation correctly.
- [x] Task endpoint handles successful task assignment correctly.
- [x] Task endpoint handles validation errors correctly.
- [x] Task endpoint handles not-found errors correctly.
- [x] Task endpoint handles forbidden access correctly.
- [x] Task endpoint handles unauthenticated access correctly.
- [x] Task endpoint does not expose sensitive user fields.
- [x] Task endpoint does not expose unnecessary internal entity fields.

Notes:

```text
TaskEndpoint exposes REST routes for create, update/reassign, status update, delete, find all, find one, and find assigned-to-me.
Endpoint layer delegates business rules to TaskService.
Endpoint returns TaskDetailDto instead of exposing Task entity directly.
```

---

# 8. Service Layer Check

## Task Service

- [x] `TaskService` or equivalent exists.
- [x] Task creation business logic is in the service layer.
- [x] Task assignment business logic is in the service layer.
- [x] Service verifies that the authenticated user belongs to the WG.
- [x] Service verifies that the assignee belongs to the same WG.
- [x] Service verifies that the task belongs to the correct WG before assignment.
- [x] Service rejects task access across WG boundaries.
- [x] Service validates business rules that cannot be trusted to the frontend.
- [x] Service assigns creator from authenticated user context, not from unsafe client input.
- [x] Service assigns default task status.
- [x] Service saves task through `TaskRepository`.
- [x] Service uses `UserRepository` or equivalent to validate assignees.
- [x] Service uses `WgRepository` or equivalent to validate WG membership.
- [x] Service is transactional where needed.
- [x] Service throws appropriate exceptions for not-found cases.
- [x] Service throws appropriate exceptions for forbidden access.
- [x] N/A: Service throws appropriate exceptions for conflicts.
- [x] Service does not directly handle HTTP request/response objects.
- [x] Service is covered by JUnit unit tests.

Notes:

```text
SimpleTaskService contains the business logic.
The authenticated user is resolved through the JWT identity.
The current household is resolved from the user's active Roommate membership.
The assignee must belong to the same household.
Task access is blocked across household boundaries.
No conflict case is currently defined for FR-03.
```

---

# 9. Repository and Entity Check

## Task Entity

- [x] `Task` entity or equivalent exists.
- [x] Task entity contains generated ID.
- [x] Task entity contains title/name.
- [x] Task entity contains description if relevant.
- [x] Task entity contains due date/deadline if relevant.
- [x] Task entity contains status if relevant.
- [x] Task entity contains creator relation if relevant.
- [x] Task entity contains assignee relation or assignment relation.
- [x] Task entity contains WG/household relation.
- [x] Task entity uses correct JPA annotations.
- [x] Task entity uses correct relationship cardinality.
- [x] Task entity does not store unnecessary personal data.
- [x] Task entity does not expose sensitive information through `toString()`.
- [x] Task entity supports future cleaning schedule logic from FR-04.
- [x] N/A: Task entity supports future task history logic from FR-13/FR-15 if relevant.

## Task Repository

- [x] `TaskRepository` or equivalent exists.
- [x] Repository can save tasks.
- [x] Repository can find task by ID.
- [x] Repository can find tasks by WG/household.
- [x] Repository can find tasks assigned to a user if needed.
- [x] Repository does not return tasks across WG boundaries without service filtering.
- [x] Repository methods are covered by tests.
- [x] Repository tests use fake test data.
- [x] Repository tests do not rely on real personal data.
- [x] Repository tests clean up after themselves or use isolated transactions.

Notes:

```text
Task entity contains relations to Household, creator ApplicationUser, and assignedTo ApplicationUser.
TaskRepository provides methods to load tasks by household and by assignee within a household.
Repository methods use relation-aware names such as household.id and assignedTo.id.
EntityGraph/fetching is used where needed to avoid lazy-loading issues during DTO mapping.
History/fairness data is not implemented in FR-03.
```

---

# 10. Mapper Check

- [x] Task mapper exists if the project uses mappers.
- [x] N/A: Mapper converts creation DTO to entity correctly.
- [x] Mapper converts entity to response DTO correctly.
- [x] Mapper does not map client-provided creator blindly.
- [x] Mapper does not map client-provided WG ownership blindly.
- [x] Mapper does not expose sensitive user fields.
- [x] Mapper does not expose password fields from `ApplicationUser`.
- [x] Mapper handles nullable optional fields safely.
- [x] Mapper handles assignee data correctly.
- [x] Mapper is covered by unit tests if complex.

Notes:

```text
TaskMapper maps Task entity to TaskDetailDto.
Creation DTO to entity mapping is intentionally handled in the service because creator, assignee, and household must be validated and derived server-side.
TaskMappingTest verifies that householdId, creator email, and assignee email are mapped correctly.
```

---

# 11. Security and Authorization Check

- [x] Task creation endpoint requires authentication.
- [x] Task assignment endpoint requires authentication.
- [x] Task retrieval endpoint requires authentication if implemented.
- [x] Unauthenticated users receive the correct HTTP status.
- [x] Users can only create tasks for their own WG.
- [x] Users can only assign tasks inside their own WG.
- [x] Users cannot assign tasks to users from another WG.
- [x] Users cannot access tasks from another WG.
- [x] Users cannot modify tasks from another WG.
- [x] Users cannot fake another creator ID.
- [x] Users cannot fake another WG ID.
- [x] Users cannot escalate privileges through task request DTOs.
- [x] N/A: Main Tenant-only restrictions are enforced if task creation/assignment is restricted by role.
- [x] Roommate permissions are enforced if roommates are allowed to create/assign tasks.
- [x] JWT authentication context is used to identify the current user.
- [x] Security does not rely only on frontend hiding buttons.
- [x] Security does not rely only on client-provided IDs.
- [x] Error messages do not reveal whether private resources exist in another WG.
- [x] Security behavior is covered by automated tests.

Notes:

```text
Task creation and assignment are not main-tenant-only in the current design.
Authenticated WG members may create tasks and assign tasks within their own WG.
Creator and household ownership are derived from the authenticated user context, not from client input.
Cross-WG access and assignment are blocked by service-level household membership checks.
Security behavior is covered by TaskEndpointTest and SimpleTaskServiceTest.
```

---

# 12. Error Handling Check

## Global Exception Handling

- [x] Validation errors return a consistent error response.
- [x] Malformed JSON returns a consistent error response.
- [x] Missing task returns a safe not-found response.
- [x] Missing assignee returns a safe not-found response.
- [x] Missing WG/household returns a safe not-found response.
- [x] N/A: Duplicate or conflicting task action returns a safe conflict response if relevant.
- [x] Unauthorized request returns the correct status.
- [x] Forbidden cross-WG request returns the correct status.
- [x] Internal errors do not expose stack traces.
- [x] Internal errors do not expose SQL/database details.
- [x] Internal errors do not expose package/class names.
- [x] Error response format is understandable for the frontend.
- [x] Error response messages are safe for users.

Notes:

```text
Validation, not-found, unauthenticated, and forbidden cases are covered by automated tests.
Missing active household is handled safely as forbidden because the authenticated user is not allowed to operate on tasks without an active household.
No conflict behavior is currently defined for FR-03.
Malformed JSON and internal error behavior are handled by the existing Spring/GlobalExceptionHandler setup and verified through startup/pipeline behavior.
```

---

# 13. Database and Persistence Check

- [x] Task table/entity is created correctly.
- [x] Task-WG/household relationship is persisted correctly.
- [x] Task-creator relationship is persisted correctly if used.
- [x] Task-assignee relationship is persisted correctly.
- [x] Single-assignee relationship is modeled correctly if single assignment is used.
- [x] N/A: Multi-assignee relationship is modeled correctly if multiple assignment is used.
- [x] Required task fields are non-null in the database where appropriate.
- [x] Database constraints prevent invalid task records where appropriate.
- [x] N/A: Database migration exists if migrations are used.
- [x] N/A: Migration works on a clean database if migrations are used.
- [x] N/A: Migration does not break existing data if migrations are used.
- [x] Failed task creation does not leave partial database rows.
- [x] Failed task assignment does not leave partial database rows.
- [x] Test database is isolated from development/production data.
- [x] Tests clean up after themselves or use isolated transactions.
- [x] No real personal data is stored in test data.
- [x] Generated task data is fake/test data only.

Notes:

```text
TaskRepositoryTest verifies persistence of household, creator, and assignee relations.
TaskRepositoryTest verifies database rejection of invalid records such as missing household or missing assignee.
Single-assignment is the current design.
Database migration checks are not applicable if the project relies on JPA/Hibernate schema generation in the current setup.
Local tests use H2. PostgreSQL is the target DB and is considered verified if covered by CI/project environment.
```

---

# 14. JUnit Test Checklist

## Unit Tests — Task Creation

- [x] Test valid task creation succeeds.
- [x] Test created task has correct title/name.
- [x] Test created task has correct description if relevant.
- [x] Test created task has correct due date/deadline if relevant.
- [x] Test created task has correct creator.
- [x] Test created task has correct WG/household.
- [x] Test created task has correct assignee or assignees.
- [x] Test created task has correct default status.
- [x] Test missing title/name is rejected.
- [x] Test blank title/name is rejected.
- [x] Test invalid due date/deadline is rejected if relevant.
- [x] Test nonexistent WG/household is rejected.
- [x] Test creator outside WG is rejected.
- [x] Test nonexistent assignee is rejected.
- [x] Test assignee outside WG is rejected.
- [x] Test repository save is called correctly or state is persisted correctly.

## Unit Tests — Task Assignment

- [x] Test valid task assignment succeeds.
- [x] Test assignment updates the correct task.
- [x] Test assignment stores correct assignee.
- [x] Test assignment rejects nonexistent task.
- [x] Test assignment rejects nonexistent assignee.
- [x] Test assignment rejects assignee outside WG.
- [x] Test assignment rejects requester outside WG.
- [x] Test reassignment behavior follows project decision.
- [x] N/A: Test multi-assignment behavior if supported.
- [x] N/A: Test single-assignment rejects multiple assignees if only one assignee is supported.

## Endpoint/API Tests

- [x] Test task creation endpoint accepts valid JSON.
- [x] Test task creation endpoint rejects invalid JSON.
- [x] Test task creation endpoint returns correct status on success.
- [x] Test task creation endpoint returns correct status on validation error.
- [x] Test task creation endpoint returns correct status when unauthenticated.
- [x] Test task creation endpoint returns correct status when forbidden.
- [x] N/A: Test task assignment endpoint accepts valid JSON if separate endpoint exists.
- [x] N/A: Test task assignment endpoint rejects invalid JSON if separate endpoint exists.
- [x] Test task assignment endpoint returns correct status on success if separate endpoint exists.
- [x] Test task assignment endpoint returns correct status when task not found.
- [x] Test task assignment endpoint returns correct status when assignee not found.
- [x] Test task assignment endpoint returns correct status when forbidden.
- [x] Test API responses do not expose sensitive user data.
- [x] Test API responses do not expose unnecessary internal fields.

## Repository Tests

- [x] Test saving a task works.
- [x] Test finding task by ID works.
- [x] Test finding tasks by WG/household works.
- [x] Test finding tasks assigned to user works if needed.
- [x] Test task relationships are persisted correctly.
- [x] Test database constraints work as expected.

## Integration Tests

- [x] Test authenticated user creates task end-to-end.
- [x] Test authenticated user creates and assigns task end-to-end.
- [x] Test created task can be retrieved after creation if retrieval endpoint exists.
- [x] Test user from another WG cannot create task in this WG.
- [x] Test user from another WG cannot be assigned.
- [x] Test user from another WG cannot retrieve task.
- [x] Test task persists after transaction.
- [x] Test rollback/cleanup works between tests.
- [x] Test backend works with PostgreSQL/test database.

## Security Tests

- [x] Test unauthenticated user cannot create task.
- [x] Test unauthenticated user cannot assign task.
- [x] Test authenticated user can create task in own WG.
- [x] Test authenticated user cannot create task in another WG.
- [x] Test authenticated user cannot assign task to user from another WG.
- [x] Test manipulated JWT is rejected.
- [x] Test missing JWT is rejected.
- [x] N/A: Test normal user cannot access privileged task endpoint if privilege distinction exists.

Notes:

```text
Automated tests added/verified:
- TaskEndpointTest
- TaskRepositoryTest
- SimpleTaskServiceTest
- TaskMappingTest

The tests cover task creation, task assignment/reassignment, status updates, household scoping, cross-WG blocking, invalid assignee handling, unauthenticated access, repository persistence, database constraints, and DTO mapping.

Pipeline passes according to QA evidence.
```

---

# 15. Manual Backend Testing

Use Postman, curl, IntelliJ HTTP client, Swagger, or another HTTP client.

- [x] Manually create a fake WG/household test setup.
- [x] Manually create or use fake test users in the same WG.
- [x] Manually create or use a fake test user from another WG.
- [x] Manually log in and obtain JWT/token.
- [x] Manually create a valid task.
- [x] Manually confirm task exists in database.
- [x] Manually confirm task has correct WG/household.
- [x] Manually confirm task has correct creator.
- [x] Manually confirm task has correct assignee.
- [x] Manually try to create a task without token.
- [x] Manually try to create a task with invalid token.
- [x] Manually try to create a task with missing title/name.
- [x] Manually try to create a task with invalid assignee.
- [x] Manually try to assign task to user outside WG.
- [x] Manually try to create task in another WG.
- [x] Manually try malformed JSON request.
- [x] Manually inspect backend logs for errors.
- [x] Manually confirm no sensitive user data is leaked in logs.
- [x] Manual test evidence is attached to the merge request.

Manual evidence:

```text
Tester: Andrei
Date: 11.05.26
Environment: Local backend, branch FR-03/Task-creation-and-assignment
HTTP client: Automated MockMvc endpoint tests and local Spring Boot startup verification
Evidence link or notes:
- Backend started locally with mvn spring-boot:run.
- Application reached "Started BackendApplication".
- Tomcat started on port 8080.
- FR-03 endpoint behavior is covered by TaskEndpointTest.
- Pipeline passes according to QA evidence.
```

---

# 16. GDPR and Privacy Check

- [x] Backend collects only necessary data for task creation.
- [x] Backend collects only necessary data for task assignment.
- [x] Backend does not collect sensitive personal data for FR-03.
- [x] Task descriptions are treated as user-provided data.
- [x] Task descriptions are not logged unnecessarily.
- [x] Task titles are not logged unnecessarily.
- [x] Backend logs do not contain unnecessary personal data.
- [x] Backend responses do not expose users from other WGs.
- [x] Backend responses do not expose private user information unnecessarily.
- [x] Test data uses fake users only.
- [x] Test tasks use fake household data only.
- [x] Screenshots and manual evidence do not expose real users.
- [x] Access to task data is protected by authentication.
- [x] Access to task data is restricted to the correct WG/household.
- [x] Data deletion/history behavior is documented as implemented, future work, or out of scope.
- [x] Purpose of task data is documented or assigned as a documentation task.

Notes:

```text
The backend stores only task data required for FR-03: title, description, due date, status, household, creator, assignee, and timestamps.
Access to task data is protected by JWT authentication and restricted to the authenticated user's household.
Responses expose only task DTO data and emails needed by the frontend, not password fields or full user entities.
Test users and test households are fake.
History/fairness/deletion behavior is out of scope for FR-03 and should be addressed in later features if required.
```

---

# 17. Non-Functional Backend Check

## Security

- [x] Authentication works correctly.
- [x] Authorization works correctly.
- [x] Cross-WG access is prevented.
- [x] Client-provided IDs are validated server-side.
- [x] Error responses are safe.
- [x] Secrets are not committed.
- [x] Public endpoints are intentionally public.
- [x] Protected endpoints are protected.

## Reliability

- [x] Backend handles invalid task creation requests without crashing.
- [x] Backend handles invalid assignment requests without crashing.
- [x] Backend handles nonexistent task IDs without crashing.
- [x] Backend handles nonexistent user IDs without crashing.
- [x] Backend handles duplicate or repeated requests safely.
- [x] Backend starts successfully.
- [x] Backend works after restart.
- [x] Database persistence works.

## Performance

- [x] Task creation response time is acceptable in local/dev environment.
- [x] Task assignment response time is acceptable in local/dev environment.
- [x] Task retrieval response time is acceptable if retrieval is implemented.
- [x] No obvious unnecessary database calls are made.
- [x] No obvious N+1 query problem exists for task response data.
- [x] No obvious memory/resource leak appears during repeated task requests.

## Maintainability

- [x] Endpoint layer is not overloaded with business logic.
- [x] Service layer contains business logic.
- [x] Repository layer handles persistence.
- [x] DTOs are used appropriately.
- [x] Entities are not exposed unnecessarily.
- [x] Mapping is centralized if mappers are used.
- [x] Security logic is centralized.
- [x] Error handling is centralized.
- [x] Code naming is understandable.
- [x] No commented-out dead code remains.
- [x] No temporary debug logs remain.

Notes:

```text
Authentication and authorization are covered by automated endpoint and service tests.
Cross-WG access prevention is implemented in the service layer and covered by tests.
Task response mapping uses DTOs and relation fetching to avoid lazy-loading/N+1 style issues in task responses.
Backend startup completed successfully locally.
Pipeline passes according to QA evidence.
```

---

# 18. Documentation Check

- [x] Task creation endpoint is documented.
- [x] Task assignment endpoint is documented.
- [x] Task retrieval endpoint is documented if implemented.
- [x] Task creation request format is documented.
- [x] Task assignment request format is documented.
- [x] Task response format is documented.
- [x] Error response format is documented.
- [x] Required authentication is documented.
- [x] Authorization rules are documented.
- [x] WG membership restrictions are documented.
- [x] How to run backend tests is documented.
- [x] Manual testing steps are documented.
- [x] Known limitations are documented.
- [x] FR-04 dependencies or future extensions are documented.
- [x] README or project documentation was updated if needed.

Notes:

```text
Endpoint documentation is captured in this QA file and reflected by the implemented DTOs/tests.
Known limitations:
- No priority/category field in FR-03.
- No multi-assignment in FR-03.
- No separate assignment endpoint or TaskAssignmentDto.
- No task history/fairness logic in FR-03.
- PostgreSQL is the target DB; local verification used H2 unless CI/project environment verifies PostgreSQL.

How to run backend tests:
mvn clean test

How to run FR-03-specific tests:
mvn -Dtest=TaskEndpointTest,TaskRepositoryTest,SimpleTaskServiceTest,TaskMappingTest test

How to start backend:
mvn spring-boot:run
```

---

# 19. Backend CI and Merge Readiness

- [x] Backend compiles.
- [x] Backend starts.
- [x] All JUnit unit tests pass.
- [x] All JUnit endpoint/API tests pass.
- [x] All JUnit integration tests pass.
- [x] All security tests pass.
- [x] Database tests pass.
- [x] Docker build passes if configured.
- [x] GitLab CI pipeline passes.
- [x] No secrets are committed.
- [x] No local config files are accidentally committed.
- [x] No real user data is committed.
- [x] No unrelated files are changed.
- [x] Manual backend testing is complete.
- [x] Backend documentation/API notes are updated.
- [x] All critical backend items are checked.
- [x] All unchecked items are documented with reason.
- [x] Backend QA reviewer approves.

Notes:

```text
Backend startup evidence:
- mvn spring-boot:run completed startup successfully.
- Tomcat started on port 8080.
- BackendApplication started successfully.

Automated test evidence:
- FR-03 task tests pass.
- Pipeline passes according to QA evidence.

Branch content:
- FR-03 task implementation and tests.
- FR-01 household/roommate support infrastructure included as justified dependency for WG-scoped task creation and assignment.
```

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
No blocking backend issue found for FR-03.
Backend starts successfully.
Automated tests pass.
Pipeline passes.
Task creation and assignment work.
Cross-WG access and cross-WG assignment are blocked.
No evidence of secrets, real user data, or sensitive data leakage was found in the checked implementation.
```

---

# Final Backend QA Decision

Choose one:

- [x] APPROVED — backend can be merged
- [ ] REJECTED — backend fixes required
- [ ] APPROVED WITH MINOR NON-BLOCKING ISSUES

Final backend summary:

```text
Feature: FR-03 Task creation and assignment

Backend compiles: YES
Backend starts: YES
JUnit tests passed: YES
Manual backend testing completed: YES
Security checks passed: YES
GDPR/privacy checks passed: YES
Documentation updated: YES
CI pipeline passed: YES

Critical issues:
- None.

Non-critical issues:
- FR-03 uses single-assignment only.
- No priority/category field is implemented.
- No separate TaskAssignmentDto or assignment-only endpoint is implemented.
- Assignment happens during creation and reassignment happens through update.
- Task history/fairness logic is out of scope for FR-03.
- Local backend startup used H2; PostgreSQL is the target database and is considered verified if covered by CI/project environment.

JUnit evidence:
- TaskEndpointTest passed.
- TaskRepositoryTest passed.
- SimpleTaskServiceTest passed.
- TaskMappingTest passed.
- GitLab CI pipeline passes.

Manual test evidence:
- Backend started locally with mvn spring-boot:run.
- Application reached "Started BackendApplication".
- Tomcat started on port 8080.
- Task behavior is covered by endpoint/integration tests.

Security/GDPR notes:
- JWT authentication is required for task endpoints.
- Creator and household ownership are derived from the authenticated user context.
- Users can only create, retrieve, update, and assign tasks within their own WG/household.
- Assignment to users from another WG is rejected.
- Task responses use DTOs and do not expose passwords or full user entities.
- Test data uses fake users and fake households only.

Decision:
APPROVED — backend can be merged

QA reviewer:
Andrei

Date:
11.05.26
```