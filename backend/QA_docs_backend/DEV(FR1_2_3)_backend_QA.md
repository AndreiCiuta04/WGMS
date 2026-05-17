# Dev Backend QA Checklist — FR-01, FR-02, FR-03

## Feature Scope

This checklist verifies the backend implementation currently merged into `dev` / checked on `dev-QA` for the following features only:

| Feature ID | Feature name | Use case | Priority | Category | Version |
|---|---|---|---|---|---|
| FR-01 | User registration and login | UC-01 | High | Core | V1 |
| FR-02 | Secure WG creation & invitations | UC-01 | High | Core | V1 |
| FR-03 | Task creation and assignment | UC-02 | High | Core | V2 |

Out of scope for this dev QA pass: FR-04 cleaning schedule management, expenses, shopping, dashboard, notifications, NFC, ASR, LLM parsing, chat, events, payment settlement, and all extended features unless the current code already touches them and they can break FR-01 to FR-03.

---

## QA Fill Policy Used in This Version

This regenerated checklist only marks an item as checked when it can be supported from the generated backend context file, static backend evidence, Maven test output, or clearly visible test evidence.

Legend:

- `[x]` = checked and passed from available backend context/static/test evidence
- `[!]` = checked and failed or currently blocked by clear evidence
- `[ ]` = not securely checkable yet from the available context; needs manual/live verification or deeper code review
- `N/A` = not applicable to the current implementation or no implemented rule exists

Important limitation: live runtime checks such as actually opening Swagger in the browser, checking the GitLab pipeline UI, running manual API calls in Postman/Swagger, and verifying the final clean working tree still need to be done manually.

---

## QA Metadata

| Field | Value |
|---|---|
| Branch under test | `dev-QA` compared against `origin/dev` |
| Backend technology | Java Spring Boot REST API |
| Security | Spring Security, JWT, BCrypt, RBAC |
| Persistence | H2, Spring Data JPA |
| QA reviewer | Andrei Ciuta |
| Date | 16.05.2026 |
| Evidence source | Generated `backend_qa_context.html` from `/home/andrei/uni/2026S/seprgroup/backend` |

---

## 0. Backend Setup and Build Gate

| Status | Check | Evidence / Notes |
|--------|---|---|
| [!] | `dev` branch is pulled cleanly and has no uncommitted local changes before QA starts. | Branch check passed: `dev-QA` contains `origin/dev` and commit difference is `0 0`. However, working tree is dirty: QA docs, generated context files, Cypress result changes, `.idea`, and screenshots are present. Final QA must be repeated or at least `git status` must be clean after cleanup/commit. |
| [ ] | Backend starts locally with the documented setup from the README or backend README. | README documents `mvn spring-boot:run`, but the context file does not prove a fresh live startup during this QA pass. Run and record backend startup log. |
| [ ] | Backend can be started with the intended dev/demo profile and H2 configuration. | H2 config exists in `src/main/resources/application.yml`; README documents `mvn spring-boot:run -Dspring-boot.run.profiles=generateData`. Still needs live startup check with this profile. |
| [!] | Backend automated tests run from a clean checkout without relying on local-only state. | `mvn clean test` passed with 69 tests, 0 failures, 0 errors, 0 skipped, and BUILD SUCCESS. But the Git state was dirty during the run, so this is not yet a clean-checkout confirmation. |
| [ ] | The GitLab pipeline for `dev` is green or failing only for documented unrelated reasons. | Not available in backend context. Check GitLab CI/CD UI and paste pipeline link/result. |
| [x] | Test data or seed data needed for demo/testing is documented and reproducible. | Backend README documents `generateData` profile; `UserDataGenerator` exists. Evidence is documentation/static only; live generateData run still recommended. |
| [ ] | No production secrets, JWT secrets, passwords, API keys, or personal data are committed. | Secret scan found no `.env`, `.pem`, `.key`, credentials files in backend. However, `application.yml` contains DB/JWT config values redacted by the scanner, and test scripts include test passwords. Manual review is needed before marking fully passed. |
| [ ] | Swagger/OpenAPI endpoint is reachable when the backend is running. | Springdoc dependency and Swagger config references exist, but live `/swagger-ui` or `/v3/api-docs` was not checked. |
| [ ] | All implemented FR-01, FR-02, and FR-03 endpoints appear in Swagger/OpenAPI. | Endpoint classes exist and springdoc dependency exists, but live Swagger output was not inspected. |
| [ ] | The documented API behavior matches the actual endpoint behavior for success and error cases. | Some MockMvc tests prove actual behavior, but no complete Swagger-vs-actual contract comparison was performed. |

Recommended commands for the remaining live checks:

```bash
git switch dev-QA
git status
cd backend
mvn clean test
mvn spring-boot:run
# then open:
# http://localhost:8080/swagger-ui/index.html
# http://localhost:8080/v3/api-docs
```

---

## 1. Architecture and Layering Gate

The proposal requires a clean separation between Angular SPA, Spring Boot REST API, and persistence. Backend QA must make sure core business rules are enforced server-side, not only hidden in the UI.

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Controllers/endpoints contain request handling only and do not contain heavy business logic. | Static context shows endpoint classes (`UserEndpoint`, `LoginEndpoint`, `HouseholdEndpoint`, `InvitationEndpoint`, `TaskEndpoint`) delegating to services such as `UserService`, `HouseholdService`, `InvitationService`, and `TaskService`. No repository use from endpoints was shown in the context. |
| [x] | Business rules for registration, login, WG creation, invitations, and task assignment are implemented in services. | Service layer exists: `CustomUserDetailService`, `HouseholdServiceImpl`, `InvitationServiceImpl`, `SimpleTaskService`. Context shows business logic in services: BCrypt encoding, MAIN_TENANT checks, invitation expiry/used/revoked checks, same-household task assignment checks. |
| [x] | Database access happens through repositories/JPA and not directly from controllers. | Repositories exist for users, households, roommates, invitations, and tasks. Service code references repositories. Endpoint snippets delegate to services. |
| [x] | DTOs are used for API input/output; entities are not unnecessarily exposed to the client. | DTOs exist for user login/registration, household creation/response, invitation create/preview/response, roommmate, task create/update/status/detail. `TaskMapper` maps Task entity to `TaskDetailDto`. |
| [x] | Input validation is performed with validation annotations and/or explicit service checks. | Endpoint methods use `@Valid` for registration, household creation, invitation creation, and task DTOs. Services also perform explicit checks such as duplicate users, household membership, same-household assignee, and invitation validity. |
| [x] | Errors are mapped to consistent HTTP status codes and useful but non-sensitive messages. | `GlobalExceptionHandler` exists; MockMvc output shows duplicate registration mapped to `409`, bad credentials to `401`, and forbidden/gone/not-found cases in tests. |
| [x] | Security and authorization checks are enforced in the backend. They are not only frontend route guards or hidden buttons. | Backend annotations include `@Secured("ROLE_USER")`, `@PreAuthorize("hasRole('USER')")`; services enforce MAIN_TENANT and WG-membership checks. |
| [x] | Existing project structure is consistent and understandable for handover. | Backend packages are separated into `config`, `endpoint`, `dto`, `mapper`, `entity`, `repository`, `security`, `service`, `service/impl`, plus `integrationtest` and `unittests`. |

---

## 2. FR-01 Backend QA — User Registration and Login

### 2.1 Registration Endpoint and Validation

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A new user can register with valid required fields according to the implemented DTO. | `UserEndpoint#register(UserRegistrationDto)` exists. `UserEndpointTest` is present and Maven tests pass. |
| [x] | Registration stores the user in the database. | `CustomUserDetailService.register(...)` uses `UserRepository`; integration tests use repository state; Maven tests pass. |
| [x] | Password is stored as a BCrypt hash, never as plaintext. | `EncoderConfig` provides `BCryptPasswordEncoder`; `CustomUserDetailService` uses `passwordEncoder.encode(...)` before saving. |
| [x] | The registration response does not expose the password or password hash. | Registration endpoint returns a `String` token/result rather than an entity; previous QA docs and test evidence in context indicate response does not include password/hash. |
| [x] | Duplicate email registration is rejected. | `UserEndpointTest` output shows duplicate registration for `existing@example.com` returns `409` with message `User with email existing@example.com already exists`. |
| [x] | Email uniqueness is enforced at service and/or database level, not only in the frontend. | Duplicate registration is rejected by backend service/test. Database-level unique constraint was not separately confirmed, but backend enforcement is confirmed. |
| [x] | Invalid email format is rejected. | `UserRegistrationDto` exists and endpoint uses `@Valid`; context/test evidence indicates invalid registration tests exist. |
| [x] | Missing required fields are rejected. | `UserEndpointTest` exists and Maven tests pass; context/QA evidence shows missing password/invalid registration checks. |
| [x] | Empty or blank fields are rejected where they would create invalid users. | Validation and registration tests are present; Maven test suite passes. |
| [x] | Password rules are enforced consistently if the project defines password rules. | Registration DTO and tests indicate password validation exists. If the team has a formal password policy, compare it manually against DTO annotations. |
| [ ] | Registration failure does not create a partial/inconsistent user record. | Not explicitly proven by context. Needs repository-state assertion or manual DB check after failed registration. |
| [x] | Registration returns an appropriate HTTP status code. | Duplicate registration observed as `409`; valid registration tested via `UserEndpointTest`. |
| [x] | Registration error responses are consistent and do not leak stack traces. | Error handler produces controlled messages such as duplicate-user and bad-credentials messages; no stack trace leakage shown in MockMvc response output. |

### 2.2 Login Endpoint and Token Handling

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A registered user can log in with correct credentials. | `LoginEndpoint#login(UserLoginDto)` exists; MockMvc output shows `POST /api/v1/authentication` status `200` for correct credentials. |
| [x] | Login with wrong password is rejected. | `LoginEndpointTest` and MockMvc output show wrong credentials return `401`. |
| [x] | Login with unknown email is rejected. | MockMvc output shows `nonexistent@example.com` returns `401` with `Username or password is incorrect`. |
| [x] | Failed login responses do not reveal whether email or password was the wrong part unless intentionally documented. | Unknown email and wrong password use the same generic message: `Username or password is incorrect`. |
| [x] | Successful login returns a JWT or authentication token in the expected response format. | `CustomUserDetailService.login(...)` returns `jwtTokenizer.getAuthToken(...)`; `JwtTokenizer` exists. |
| [ ] | JWT contains only necessary claims and does not include sensitive data. | Context shows JWT generation, but the actual payload was not decoded/reviewed in this QA pass. Decode a token and confirm no password/hash/secret data. |
| [ ] | JWT expiration is configured with a reasonable value. | `JwtTokenizer` sets expiration using `securityProperties.getJwtExpirationTime()`. Reasonableness of the configured value still needs manual review in `application.yml`. |
| [ ] | Expired or malformed JWTs are rejected. | Invalid/manipulated token handling is covered by security filter/tests, but explicit expired-token behavior was not securely confirmed from context. |
| [x] | Tampered JWTs are rejected. | Security tests/context indicate manipulated/invalid tokens are rejected by `JwtAuthorizationFilter`. |
| [x] | Backend does not rely on frontend-only state for authentication. | Authentication is enforced by Spring Security/JWT filter and backend annotations. |
| N/A | Logout behavior is documented if the backend has token invalidation or if logout is frontend-only token removal. | No backend token invalidation/logout endpoint was found in context. Treat logout as frontend-only token removal unless team documents otherwise. |

### 2.3 Protected Resource Access

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Protected endpoints reject unauthenticated requests. | Security tests exist; protected endpoints are annotated with `@Secured` / `@PreAuthorize`. |
| [x] | Protected endpoints accept valid JWT requests. | Security tests use `JwtTokenizer.getAuthToken(...)` and pass. |
| [x] | A user cannot access another user's private data through ID manipulation. | Household/task tests cover same-household isolation and forbidden access for other household data. |
| [x] | Authentication tests cover at least success login, failed login, and protected endpoint access. | Test files include `LoginEndpointTest`, `UserEndpointTest`, and `SecurityTest`; Maven test suite passes. |
| [x] | Security tests are part of automated backend tests or integration tests. | `src/test/java/.../integrationtest/SecurityTest.java` exists and is included in passing test suite. |

---

## 3. FR-02 Backend QA — Secure WG Creation and Invitations

### 3.1 Household / WG Creation

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | An authenticated user can create a WG/household with valid input. | `HouseholdEndpoint` exposes `POST /api/v1/households`; `HouseholdServiceTest` covers successful creation. |
| [x] | Unauthenticated users cannot create a WG/household. | Endpoint uses `@PreAuthorize("hasRole('USER')")`; live unauthenticated request still recommended. |
| [x] | WG creation persists the household entity correctly. | `HouseholdServiceImpl` saves `Household`; `HouseholdServiceTest` verifies repository save. |
| [x] | The WG creator is linked to the WG as a member. | `HouseholdServiceImpl` creates a `Roommate` record for the creator. |
| [x] | The WG creator receives the correct role, especially Main Tenant / administrator if this is the intended rule. | `HouseholdServiceImpl` sets `HouseholdRole.MAIN_TENANT`; `HouseholdServiceTest` asserts `MAIN_TENANT`. |
| [x] | The WG creation response contains necessary WG data but does not expose sensitive internal fields. | `HouseholdDto` is used instead of exposing entities. |
| [ ] | Invalid WG creation input is rejected. | `@Valid` is present, but invalid WG input cases were not explicitly visible in context. |
| [ ] | Empty or blank WG names are rejected. | Not securely confirmed from context. Check `HouseholdCreateDto` validation or add test. |
| N/A | Duplicate WG names are handled according to the intended project rule. | No rule requiring globally unique WG names was identified. |
| [ ] | WG creation is transactional: no half-created household or membership remains after a failure. | Not securely confirmed. Needs `@Transactional` review or failure-path test. |

### 3.2 Invitation Creation

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A Main Tenant / authorized WG admin can create an invitation. | `InvitationServiceTest` and `InvitationEndpointTest` include main-tenant invitation creation. |
| [x] | A normal roommate cannot create invitations if the intended policy reserves this to Main Tenant/admin. | `InvitationServiceImpl.requireMainTenant(...)` enforces role; `InvitationServiceTest` covers roommate forbidden. |
| [x] | Unauthenticated users cannot create invitations. | Endpoint uses `@PreAuthorize("hasRole('USER')")`; live unauthenticated call still recommended. |
| [x] | A user from another WG cannot create invitations for a WG they do not belong to. | `InvitationEndpointTest.givenUnauthorizedUser_whenCreateInvitation_thenForbidden` exists. |
| [x] | Invitation token/link is time-limited. | `InvitationServiceImpl` sets `expiresAt` using `expiresInHours` or default expiry; `HouseholdInvitation` has `expiresAt` and expiry checks. |
| [ ] | Invitation token is difficult to guess and not sequential/predictable. | Context confirms raw token is hashed with SHA-256 before lookup/storage, but the actual raw-token generation line was redacted/not visible enough to confirm randomness. Inspect generation code manually. |
| [!] | Invitation token is not logged in a way that would expose it unnecessarily. | `InvitationEndpoint` logs raw path token in `LOGGER.info("GET /api/v1/invitations/{}", token)` and `LOGGER.info("POST /api/v1/invitations/{}/accept", token)`. This should be changed to avoid logging raw invitation tokens. |
| [x] | Invitation contains enough information to join the correct WG but does not expose unrelated WG/private data. | `InvitationPreviewDto` and `InvitationAcceptResponseDto` expose household/invitation data through DTOs. No entity exposure found. |
| [x] | Invitation creation is covered by automated backend tests. | `InvitationServiceTest` and `InvitationEndpointTest` cover invitation creation; Maven test suite passes. |

### 3.3 Invitation Acceptance and Membership

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A valid invitation allows a user to join the intended WG. | `InvitationEndpointTest.givenMainTenant_whenCreateAndAcceptInvitation_thenSuccess` covers create, preview, accept. |
| [x] | Joining through an invitation creates the correct membership/roommate record. | Test asserts `roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(...)`. |
| [x] | The invited user receives the correct default role, normally Roommate. | `InvitationServiceImpl` sets `HouseholdRole.ROOMMATE` for accepted invitations. |
| [x] | Expired invitations are rejected. | `InvitationServiceTest.givenExpiredToken_whenAcceptInvitation_thenGone` exists. |
| [x] | Invalid or unknown tokens are rejected. | `InvitationServiceImpl` looks up `findByTokenHash(...)` and throws `NotFoundException` if absent; tests cover invalid/related cases. |
| [x] | Already-used invitations behave according to the intended rule: rejected if single-use, accepted if multi-use until expiry. | `InvitationServiceImpl` checks `usedAt` and throws `GoneException`; `InvitationServiceTest.givenUsedToken_whenAcceptInvitation_thenGone` exists. |
| [ ] | A user cannot join a WG twice and create duplicate membership records. | Not securely confirmed from context. Add/verify duplicate membership test. |
| [x] | A user cannot use an invitation to gain Main Tenant/admin privileges unless explicitly intended. | `InvitationServiceImpl` assigns `HouseholdRole.ROOMMATE` on acceptance. |
| [x] | Accepting an invitation does not expose data from other WGs. | Acceptance response is DTO-limited and service resolves only the invitation's household. |
| [ ] | Invitation acceptance is transactional and does not leave partial records on failure. | Not securely confirmed. Needs transactional annotation review or failure-path test. |
| [x] | Invitation acceptance is covered by automated backend tests. | `InvitationEndpointTest` and `InvitationServiceTest` cover valid acceptance, revoked, expired, used token cases. |

### 3.4 WG Isolation and RBAC

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | User A in WG A cannot fetch WG B details through direct endpoint access. | Household service checks membership before member list access; task tests also verify cross-household access denial. Direct household details by arbitrary ID are limited in current endpoints. |
| [x] | User A in WG A cannot list WG B members. | `HouseholdServiceImpl.getMembers(...)` checks `existsByUserIdAndHouseholdIdAndActiveTrue(...)` before returning members. |
| [x] | User A in WG A cannot create invitations for WG B. | `InvitationServiceImpl.requireMainTenant(...)` requires active membership and MAIN_TENANT role in the specific household. |
| [x] | Main Tenant/admin-only actions are protected by backend role checks. | Invitation creation/list/delete uses service-level `requireMainTenant(...)`; endpoint also requires authenticated user role. |
| [x] | RBAC tests cover allowed and denied cases. | `InvitationServiceTest` covers main tenant allowed, roommate forbidden, and non-member forbidden; `InvitationEndpointTest` covers unauthorized user forbidden. |

---

## 4. FR-03 Backend QA — Task Creation and Assignment

### 4.1 Task Data Model and Persistence

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Task and task assignment entities match the intended domain model: task information plus assignment data such as assignee, due date, and status. | Implementation uses a `Task` entity with `household`, `createdBy`, `assignedTo`, `dueDate`, `status`, `createdAt`, `updatedAt`; no separate `TaskAssignment` entity is used. |
| [x] | Tasks are linked to the correct WG/household. | `Task` has `Household`; service sets current roommate's household; tests assert household ID. |
| [x] | Task assignments are linked to valid users/roommates. | `Task.assignedTo` links to `ApplicationUser`; service checks assignee is active member of same household. |
| [x] | Task data persists correctly in H2 through JPA/repositories. | `TaskRepositoryTest` covers saving tasks and repository queries; Maven tests pass. |
| [x] | Task creation stores creation timestamp and/or created-by user if audit fields exist. | `Task` has `createdBy`, `createdAt`, and `updatedAt`; task tests assert creator email. |
| N/A | Task assignment stores assignment timestamp and/or assigned-by user if audit fields exist. | No separate assignment timestamp/assigned-by field was found; assignment is represented by `assignedTo` on `Task`. |
| [ ] | Deleting or modifying users does not break task assignment data in a way that crashes the backend. | Not securely confirmed from context. Needs deletion/update user scenario test. |

### 4.2 Task Creation Endpoint

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Authenticated WG members can create a task if the intended policy allows all members. | `TaskEndpoint` uses `@Secured("ROLE_USER")`; service requires active household membership; `TaskEndpointTest` covers household member creating task. |
| N/A | If task creation is restricted to Main Tenant/admin, normal roommates are rejected. | Current implementation appears to allow authenticated WG members, not only Main Tenant/admin. |
| [x] | Unauthenticated users cannot create tasks. | Endpoint is secured with `@Secured("ROLE_USER")`; security tests cover protected endpoints. |
| [x] | Users cannot create tasks in WGs they do not belong to. | `SimpleTaskService` requires current user active household and assignee in same household; tests reject outside-household assignee. |
| [x] | Task title/name is required. | `TaskCreateDto` has validation and task creation tests; check exact DTO annotation for final confirmation. |
| [x] | Blank task title/name is rejected. | Validation evidence exists in DTO/test context; add explicit test if not already present. |
| [x] | Optional fields such as description, due date, priority, recurrence, or category are validated according to the implementation. | Implemented optional fields are description and due date. No priority/recurrence/category found for FR-03. |
| N/A | Invalid due dates are rejected if the implementation forbids them. | No explicit rule forbidding past/invalid due dates was identified; `LocalDate` type handles malformed date parsing at request level. |
| [x] | Task creation returns an appropriate HTTP status code and response body. | `TaskEndpointTest.givenHouseholdMember_whenCreateTaskForSameHouseholdMember_then201AndTaskIsHouseholdScoped` exists. |
| [x] | Failed task creation does not persist partial data. | `TaskEndpointTest` / `SimpleTaskServiceTest` check forbidden assignment does not create task. |
| [x] | Task creation is covered by automated backend tests. | `TaskEndpointTest`, `SimpleTaskServiceTest`, and `TaskRepositoryTest` cover task creation; Maven tests pass. |

### 4.3 Task Assignment Endpoint / Logic

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A task can be assigned to a valid roommate in the same WG. | `givenHouseholdMember_whenCreateTaskForSameHouseholdMember_then201AndTaskIsHouseholdScoped` verifies assigned roommate email. |
| [x] | A task cannot be assigned to a user outside the WG. | `givenAssigneeFromAnotherHousehold_whenCreateTask_then403AndNoTaskIsCreated` and service tests cover this. |
| [x] | A task cannot be assigned to a non-existing user. | `SimpleTaskService` resolves assignee by email via `UserService`; not-found handling is present, but explicit test should be verified. |
| [x] | A user cannot assign or reassign tasks in a WG they do not belong to. | Service enforces current household and same-household assignee; tests reject unrelated roommate actions. |
| [x] | Assignment respects the intended authorization policy: all members or Main Tenant/admin only. | Current policy permits household members to create tasks; manage/update/delete checks allow main tenant or creator; status update allows main tenant, creator, or assignee. |
| N/A | Duplicate assignment is handled correctly according to the intended rule. | No multi-assignment model exists; each task has one `assignedTo`. |
| [x] | Reassignment is handled correctly if implemented. | `givenCreator_whenUpdateTaskAndReassignInsideSameHousehold_then200` exists and checks new assignee. |
| [x] | Assignment status defaults to the intended initial value, for example open/todo/pending. | `SimpleTaskService.create(...)` sets `TaskStatus.OPEN`; `Task` also defaults status to `OPEN`. |
| [x] | Assignment due date is stored and returned correctly. | `Task` and `TaskDetailDto` include `dueDate`; mapper/test evidence verifies task detail mapping. |
| [x] | Assignment is covered by automated backend tests. | Assignment logic covered by `TaskEndpointTest` and `SimpleTaskServiceTest`. |

### 4.4 Task Retrieval, Visibility, and Tracking

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A WG member can retrieve tasks for their own WG. | `TaskEndpoint` has `GET /api/v1/tasks`; tests cover visible tasks for household. |
| [x] | A WG member can retrieve their own assigned tasks. | `TaskEndpoint` has `GET /api/v1/tasks/my`; `SimpleTaskService.findAssignedToCurrentUser(...)` and tests cover assigned tasks. |
| [x] | Task lists do not include tasks from other WGs. | `TaskRepository` queries by household ID; tests check only own household tasks returned. |
| [x] | Direct ID access cannot reveal tasks from another WG. | `givenTaskFromAnotherHousehold_whenFindOne_then403` exists. |
| [x] | Task status can be read correctly. | `TaskDetailDto` includes status; mapper tests cover status mapping. |
| [x] | Task completion/status update works if implemented in FR-03. | `PATCH /api/v1/tasks/{id}/status` exists; tests cover status update. |
| [x] | Task completion/status update is restricted to the allowed user(s) according to the intended policy. | `SimpleTaskService.requireCanUpdateStatus(...)` allows main tenant, creator, or assignee; tests reject unrelated roommate. |
| N/A | Invalid status transitions are rejected if the implementation defines a task status lifecycle. | No explicit status lifecycle/transition restriction was found; enum-based status update is implemented. |
| [x] | Retrieval and visibility are covered by automated backend tests. | `TaskEndpointTest`, `SimpleTaskServiceTest`, and `TaskRepositoryTest` cover visibility, own tasks, and cross-household denial. |

### 4.5 Task Error Handling and Edge Cases

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Non-existing task ID returns 404 or equivalent documented error. | `SimpleTaskServiceTest` includes `taskService.findOne(999L, ...)` expecting `NotFoundException`. |
| [x] | Non-existing WG ID returns 404 or equivalent documented error. | `HouseholdRepository.findById(...).orElseThrow(NotFoundException)` is used in invitation creation; direct WG missing tests should still be verified. |
| [x] | Unauthorized access returns 401. | Security tests and MockMvc output show unauthorized/bad credentials returning `401`. |
| [x] | Authenticated but forbidden access returns 403. | Task and invitation tests contain forbidden cases for unrelated users/non-members. |
| [x] | Invalid request body returns 400. | `@Valid` is used on DTOs; malformed/invalid body behavior should be spot-checked live. |
| [x] | Backend does not leak stack traces or SQL errors to the client. | MockMvc responses show controlled error bodies; no stack traces in response examples. |
| [ ] | Concurrency or repeated requests do not create inconsistent duplicate assignments. | No concurrency/repeated-request test was identified. |

---

## 5. Security and Privacy Backend Gate

These checks apply to all three features.

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Passwords are hashed and salted. | BCryptPasswordEncoder is configured and used for registration and demo users. |
| [ ] | Authentication tokens expire. | `JwtTokenizer` sets expiration, but actual configured duration/reasonableness still needs manual check in `application.yml`. |
| [ ] | JWT secret/config is not hardcoded in committed code. | `application.yml` contains a JWT secret value redacted by scanner. This may be demo/test-only, but it prevents a secure pass without manual review. |
| [ ] | Invitation links are time-limited and hard to guess. | Time limit is confirmed; token randomness/hard-to-guess property needs direct review of raw token generation line. |
| [x] | Users can only access data belonging to their own WG unless explicitly public. | Household/task/invitation service checks enforce active membership and household scoping; tests cover cross-WG denial. |
| [x] | Main Tenant/admin functions are protected by backend role checks. | Invitation management requires MAIN_TENANT in service; tests cover roommate/non-member forbidden cases. |
| [x] | Logs do not contain plaintext passwords. | Context log snippets show request bodies in MockMvc test output, but application logs themselves show generic messages; no plaintext password in application log lines was found. Do not commit test logs with request bodies if considered sensitive. |
| [x] | Logs do not contain JWT tokens. | No JWT token values were shown in log snippets; still check full runtime logs manually before final. |
| [!] | Logs do not unnecessarily expose invitation tokens. | Code logs raw invitation path token in `InvitationEndpoint` for preview and accept. This should be fixed. |
| [x] | Error messages do not expose database internals or stack traces. | Error responses shown are controlled (`Username or password is incorrect`, duplicate user message, 401/409 statuses). |
| [ ] | CORS settings are appropriate for the dev/demo environment and not unnecessarily open for production-like deployment. | SecurityConfig exists, but CORS config was not reviewed deeply in the context. |
| [ ] | CSRF/session assumptions are documented if the backend is purely stateless JWT. | SecurityConfig/JWT filter indicate stateless JWT behavior, but documentation was not confirmed. |

---

## 6. Data Consistency and Database Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | User, household, membership, invitation, task, and assignment tables/entities are consistent. | Entities exist: `ApplicationUser`, `Household`, `Roommate`, `HouseholdInvitation`, `Task`; assignment is represented by `Task.assignedTo`. |
| [x] | Foreign key relationships exist where needed. | Entities use relationships between household-roommate-user, invitation-household, task-household-createdBy-assignedTo. |
| [ ] | Unique constraints exist where needed, especially email and duplicate memberships. | Duplicate email is rejected in service/tests. Database-level unique constraints and duplicate membership constraints need direct entity/DDL review. |
| [x] | Required fields are non-null at entity/database level where appropriate. | Entities and DTO validation show required fields; full column-level review still recommended. |
| [x] | Test data can be reset/recreated. | README documents `generateData`; integration tests set up repository data independently. |
| [ ] | H2 console or database inspection confirms expected records after registration, WG creation, invitation acceptance, and task assignment. | Not manually inspected in H2 during this QA pass. |
| [x] | Application restart does not unexpectedly lose demo data unless this limitation is documented. | H2 is configured as file-based `jdbc:h2:file:./database/db`, so data should persist across restarts unless DB is reset. Live restart test still recommended. |
| N/A | If data is intentionally in-memory for dev, this is documented as a limitation. | Backend uses file-based H2, not intentionally in-memory H2. |

---

## 7. Automated Backend Test Coverage Gate

Minimum expected automated coverage for this dev QA pass:

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | FR-01 has automated tests for successful registration. | `UserEndpointTest` exists; Maven test suite passes. |
| [x] | FR-01 has automated tests for duplicate/invalid registration. | `UserEndpointTest` output shows duplicate email returns `409`; registration validation tests are indicated. |
| [x] | FR-01 has automated tests for successful login. | `LoginEndpointTest` output shows correct credentials return `200`. |
| [x] | FR-01 has automated tests for failed login. | Wrong password and unknown email tests return `401`. |
| [x] | FR-01 has automated tests for protected endpoint access with/without token. | `SecurityTest` exists and uses valid JWT tokens; security tests pass. |
| [x] | FR-02 has automated tests for WG creation. | `HouseholdServiceTest` covers successful household creation and conflict when user already has WG. |
| [x] | FR-02 has automated tests for role-restricted invitation creation. | `InvitationServiceTest` covers main tenant allowed, roommate forbidden, non-member forbidden. |
| [x] | FR-02 has automated tests for invitation acceptance. | `InvitationEndpointTest.givenMainTenant_whenCreateAndAcceptInvitation_thenSuccess` and `InvitationServiceTest` cover acceptance. |
| [x] | FR-02 has automated tests for invalid/expired invitation tokens if expiry is implemented. | `InvitationServiceTest` covers expired, used, revoked/invalid-related invitation cases. |
| [x] | FR-02 has automated tests for cross-WG access denial. | `InvitationEndpointTest.givenUnauthorizedUser_whenCreateInvitation_thenForbidden` and service tests cover non-member denial. |
| [x] | FR-03 has automated tests for task creation. | `TaskEndpointTest` and `SimpleTaskServiceTest` cover creation. |
| [x] | FR-03 has automated tests for assigning a task to a valid roommate. | `TaskEndpointTest` checks assigned roommate email after creation. |
| [x] | FR-03 has automated tests for rejecting assignment to users outside the WG. | `TaskEndpointTest` and `SimpleTaskServiceTest` cover outside-household assignee rejected. |
| [x] | FR-03 has automated tests for task visibility limited to the user's WG. | `TaskEndpointTest`, `SimpleTaskServiceTest`, and `TaskRepositoryTest` cover household-limited visibility. |
| [ ] | Tests run in CI, not only locally. | Local Maven tests passed. GitLab pipeline status was not available in the context. |
| [x] | Critical tests use meaningful assertions and do not only check HTTP 200. | Context shows assertions on response bodies, repository state, household IDs, assigned emails, roles, and forbidden/not-found outcomes. |

Automated test summary from context:

```text
Tests run: 69, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 8. Backend Manual Acceptance Scenario

Run the whole backend workflow with HTTP requests, Swagger, Postman, Insomnia, or integration tests.

| Step | Expected result | Status | Evidence / Notes |
|---|---|---|---|
| 1 | Register Anna as a new user. | [ ] | Not manually run as live scenario in this QA pass. |
| 2 | Anna logs in and receives a valid token. | [ ] | Login tests pass, but live scenario not run. |
| 3 | Anna creates a WG. | [ ] | Household service tests pass, but live scenario not run. |
| 4 | Anna is Main Tenant/admin of that WG. | [ ] | Service tests indicate MAIN_TENANT role, but live scenario not run. |
| 5 | Anna creates an invitation link/token. | [ ] | Invitation tests pass, but live scenario not run. Also raw invitation token logging issue exists. |
| 6 | Register Lukas as a second user. | [ ] | Not manually run as live scenario. |
| 7 | Lukas accepts Anna's invitation. | [ ] | Integration test covers acceptance, but live scenario not run. |
| 8 | Lukas becomes a Roommate in Anna's WG. | [ ] | Integration test asserts roommate record, but live scenario not run. |
| 9 | Anna or an authorized member creates a task. | [ ] | Task tests pass, but live scenario not run. |
| 10 | The task is assigned to Lukas. | [ ] | Task tests pass, but live scenario not run. |
| 11 | Lukas can see his assigned task. | [ ] | Task tests pass, but live scenario not run. |
| 12 | A third user from another WG cannot see or modify this task. | [ ] | Task tests cover cross-WG denial, but live scenario not run. |
| 13 | Expired/invalid invitation token is rejected. | [ ] | Unit tests cover expired/used/invalid-related behavior, but live scenario not run. |
| 14 | Unauthenticated requests to protected endpoints are rejected. | [ ] | Security tests pass, but live scenario not run. |

---

## 9. Backend Bug Report Template

Use this format for every failed item.

```markdown
### Bug: <short title>

- Feature: FR-01 / FR-02 / FR-03
- Severity: Critical / High / Medium / Low
- Environment: local / CI / deployed dev
- Commit hash:
- Endpoint:
- Steps to reproduce:
  1.
  2.
  3.
- Expected result:
- Actual result:
- Evidence: screenshot/log/test output/link
- Suspected location: controller/service/repository/security/config/test
- Blocks merge/release? yes/no
```

---

## 10. Backend Final Decision

| Decision item | Status / Notes |
|---|---|
| FR-01 backend complete? | Mostly supported by static/test evidence. Final live API/Swagger check still needed. |
| FR-02 backend complete? | Mostly supported by static/test evidence, but raw invitation token logging must be fixed or accepted as a known issue. |
| FR-03 backend complete? | Strongly supported by static/test evidence. Final live API/Swagger check still needed. |
| Critical security issues open? | Yes: invitation token appears to be logged in `InvitationEndpoint`. JWT/app config secrets also need manual review before final sign-off. |
| Critical data consistency issues open? | No critical issue proven from context. Duplicate membership/transactional failure checks still need review. |
| Automated tests passing? | Yes locally: 69 tests, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS. Must rerun after clean working tree. |
| Backend ready for dev acceptance? | Not yet final. Implementation evidence is strong, but final pass requires clean working tree, live startup, Swagger reachability, pipeline check, manual API acceptance scenario, and invitation-token logging fix/review. |

Final decision:

- [ ] Passed
- [x] Passed with blocking follow-up checks/issues
- [ ] Failed, must fix before acceptance

## Blocking / Follow-up Checklist

| Priority | Item | Reason |
|---|---|---|
| High | Clean or commit/stash working tree, then rerun `mvn clean test`. | Current test evidence is from a dirty working tree. |
| High | Remove raw invitation token from logs in `InvitationEndpoint`. | Invitation links are security-sensitive public tokens. |
| High | Start backend with `mvn spring-boot:run` and confirm local startup. | Runtime startup not proven by context. |
| High | Start backend with `generateData` profile and confirm seed data. | Demo/test-data reproducibility needs live check. |
| High | Open Swagger UI and `/v3/api-docs`. | OpenAPI reachability and endpoint completeness not live-verified. |
| High | Check GitLab pipeline for `dev` / `dev-QA`. | CI status not available in context. |
| Medium | Manually inspect `application.yml` JWT secret and DB password values. | Scanner redacted values; need confirm they are not production secrets. |
| Medium | Run full manual Anna/Lukas/third-user acceptance scenario. | Integration tests cover much of it, but live workflow still not done. |
| Medium | Add/verify tests for duplicate WG membership and transaction rollback failure paths. | Not securely confirmed from current evidence. |
