# Dev Backend QA Checklist — FR-01, FR-02, FR-03

## Feature Scope

This checklist verifies the backend implementation currently checked on `dev-QA` / prepared for `dev` acceptance for the following features only:

| Feature ID | Feature name | Use case | Priority | Category | Version |
|---|---|---|---|---|---|
| FR-01 | User registration and login | UC-01 | High | Core | V1 |
| FR-02 | Secure WG creation & invitations | UC-01 | High | Core | V1 |
| FR-03 | Task creation and assignment | UC-02 | High | Core | V2 |

Out of scope for this backend QA pass: FR-04 cleaning schedule management, expenses, shopping, dashboard, notifications, NFC, ASR, LLM parsing, chat, events, payment settlement, and all extended features unless the current code already touches them and they can break FR-01 to FR-03.

---

## QA Fill Policy Used in This Version

Legend:

- `[x]` = checked and passed, or checked and accepted as sufficient for the current dev QA scope
- `[!]` = checked and failed / blocking issue
- `[ ]` = not checked
- `N/A` = not applicable to the current implementation or no implemented rule exists

This version is ready to commit after the final backend QA fixes. The remaining notes are accepted follow-up notes, not blockers for FR-01, FR-02, and FR-03 backend dev acceptance.

---

## QA Metadata

| Field | Value |
|---|---|
| Branch under test | `dev-QA` |
| Backend technology | Java Spring Boot REST API |
| Security | Spring Security, JWT, BCrypt, RBAC |
| Persistence | H2, Spring Data JPA |
| QA reviewer | Andrei Ciuta |
| Date | 17.05.2026 |
| Final local backend test result | `Tests run: 74, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` |
| Pipeline result | Green, confirmed by QA reviewer |
| Final decision | Passed with accepted follow-up notes |

---

## 0. Backend Setup and Build Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | `dev-QA` branch is clean or all intended backend QA changes are committed/staged correctly before final handover. | Final cleanup was performed by QA reviewer. Unrelated `.idea`, Cypress screenshots/results, and duplicate generated QA files were removed/restored before commit. |
| [x] | Backend starts locally with the documented setup from the README or backend README. | Backend startup was verified during QA with Spring Boot startup and `/health` availability. |
| [x] | Backend can be started with the intended dev/demo H2 configuration. | H2 configuration exists and test/runtime evidence uses H2 successfully. Live `generateData` profile remains an accepted optional follow-up, not a blocker for FR-01 to FR-03 backend acceptance. |
| [x] | Backend automated tests run successfully. | Final local run: `mvn clean test` → `Tests run: 74, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`. |
| [x] | The GitLab pipeline for `dev-QA` / backend QA changes is green. | QA reviewer confirmed pipeline is green after backend cleanup and fixes. |
| [x] | Test data or seed data needed for demo/testing is documented and reproducible. | Backend README documents the `generateData` profile; data generator exists. Live profile run is optional follow-up. |
| [x] | No production secrets, passwords, API keys, or personal data are newly introduced by this QA pass. | No new secret files were committed. JWT secret remains a dev/demo configuration value accepted by the team as a follow-up note, not production-ready. |
| [x] | Swagger/OpenAPI endpoint is reachable when the backend is running. | `/v3/api-docs` was verified as reachable with HTTP 200 and Swagger UI redirect was verified. |
| [x] | All implemented FR-01, FR-02, and FR-03 endpoints appear in backend mappings/OpenAPI. | Runtime mappings show user/authentication, household/invitation, and task endpoints. |
| [x] | The documented API behavior matches the actual endpoint behavior for success and error cases for this QA scope. | Verified through integration tests, service tests, repository tests, and manual acceptance scenario. |

---

## 1. Architecture and Layering Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Controllers/endpoints contain request handling only and do not contain heavy business logic. | Endpoints delegate to services such as `CustomUserDetailService`, `HouseholdServiceImpl`, `InvitationServiceImpl`, and `SimpleTaskService`. |
| [x] | Business rules for registration, login, WG creation, invitations, and task assignment are implemented in services. | Password encoding, duplicate-user checks, household membership checks, MAIN_TENANT checks, invitation validity checks, and task-assignment scoping are implemented server-side. |
| [x] | Database access happens through repositories/JPA and not directly from controllers. | Repositories are used for users, households, roommates, invitations, messages, and tasks. |
| [x] | DTOs are used for API input/output; entities are not unnecessarily exposed to the client. | DTOs exist for user, household, invitation, roommate, and task flows. Task mapping is performed via mapper/DTOs. |
| [x] | Input validation is performed with validation annotations and/or explicit service checks. | `@Valid` is used on endpoint DTOs and service checks cover domain rules. Login validation was fixed during QA. |
| [x] | Errors are mapped to consistent HTTP status codes and useful but non-sensitive messages. | `GlobalExceptionHandler` maps validation/conflict/auth/forbidden/not-found/gone cases. Tests verify 400/401/403/404/409/410 behavior. |
| [x] | Security and authorization checks are enforced in the backend. They are not only frontend route guards or hidden buttons. | Spring Security annotations plus service-level RBAC/membership checks enforce access. |
| [x] | Existing project structure is consistent and understandable for handover. | Backend packages are separated into `config`, `endpoint`, `dto`, `mapper`, `entity`, `repository`, `security`, `service`, `service/impl`, `integrationtest`, and `unittests`. |

---

## 2. FR-01 Backend QA — User Registration and Login

### 2.1 Registration Endpoint and Validation

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A new user can register with valid required fields according to the implemented DTO. | `UserEndpoint#register(UserRegistrationDto)` exists and `UserEndpointTest` passes. |
| [x] | Registration stores the user in the database. | Registration service persists via `UserRepository`; integration tests verify stored users. |
| [x] | Password is stored as a BCrypt hash, never as plaintext. | `BCryptPasswordEncoder` is configured and tests assert the saved password differs from plaintext and matches via encoder. |
| [x] | The registration response does not expose the password or password hash. | Response uses token/string/DTO behavior, not direct entity exposure. No password/hash is returned. |
| [x] | Duplicate email registration is rejected. | Duplicate registration returns `409`. |
| [x] | Email uniqueness is enforced at backend/service and database level. | Service rejects duplicate email; entity/DDL includes unique email constraint. |
| [x] | Invalid email format is rejected. | DTO validation and integration tests cover invalid registration input. |
| [x] | Missing required fields are rejected. | DTO validation and integration tests cover missing/invalid required fields. |
| [x] | Empty or blank fields are rejected where they would create invalid users. | Validation annotations and tests cover invalid user data. |
| [x] | Password rules are enforced consistently according to the implemented DTO. | Registration validation rejects invalid passwords according to current backend rules. |
| [x] | Registration failure does not create a partial/inconsistent user record. | Validation failures are rejected before save; duplicate-user service path rejects before creating a new user. No partial save behavior observed in tests. |
| [x] | Registration returns an appropriate HTTP status code. | Valid registration returns success/created behavior; duplicate returns `409`; invalid data returns `400`. |
| [x] | Registration error responses are consistent and do not leak stack traces. | Error handler returns controlled messages/statuses; no stack traces are exposed in client responses. |

### 2.2 Login Endpoint and Token Handling

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A registered user can log in with correct credentials. | `LoginEndpointTest` verifies successful login with HTTP 200. |
| [x] | Login with wrong password is rejected. | Failed login tests return HTTP 401. |
| [x] | Login with unknown email is rejected. | Unknown email path returns HTTP 401. |
| [x] | Failed login responses do not reveal whether email or password was the wrong part. | Wrong password and unknown user use the same generic message: `Username or password is incorrect`. |
| [x] | Successful login returns a JWT or authentication token in the expected response format. | Login returns a JWT generated by `JwtTokenizer`. |
| [x] | JWT contains only necessary claims and does not include passwords or hashes. | Token generation uses subject/email and roles; password/hash is not included. |
| [x] | JWT expiration is configured. | `JwtTokenizer` uses configured expiration time. The exact value is accepted as dev configuration. |
| [x] | Expired or malformed JWTs are rejected. | JWT filter/security tests cover invalid token handling; expiration handling is implemented through JWT validation. |
| [x] | Tampered JWTs are rejected. | Security tests cover invalid/manipulated JWT behavior. |
| [x] | Backend does not rely on frontend-only state for authentication. | Authentication is enforced by Spring Security and JWT filter. |
| N/A | Logout behavior is documented if the backend has token invalidation or if logout is frontend-only token removal. | No backend logout/token invalidation endpoint is implemented in scope. |

### 2.3 Protected Resource Access

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Protected endpoints reject unauthenticated requests. | Security tests and manual scenario verify protected endpoint rejection. |
| [x] | Protected endpoints accept valid JWT requests. | Integration tests use valid JWTs successfully. |
| [x] | A user cannot access another user's private/WG-scoped data through ID manipulation. | Household, invitation, and task services enforce membership/household scoping. |
| [x] | Authentication tests cover success login, failed login, and protected endpoint access. | `LoginEndpointTest`, `UserEndpointTest`, and `SecurityTest` pass. |
| [x] | Security tests are part of automated backend tests or integration tests. | Security tests are included in the final 74-test backend suite. |

---

## 3. FR-02 Backend QA — Secure WG Creation and Invitations

### 3.1 Household / WG Creation

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | An authenticated user can create a WG/household with valid input. | `HouseholdEndpoint` exposes `POST /api/v1/households`; tests and manual scenario verify creation. |
| [x] | Unauthenticated users cannot create a WG/household. | Endpoint requires authenticated user role through backend security. |
| [x] | WG creation persists the household entity correctly. | `HouseholdServiceImpl` saves `Household`; tests verify repository interaction/state. |
| [x] | The WG creator is linked to the WG as a member. | Service creates a `Roommate` membership for the creator. |
| [x] | The WG creator receives the correct role, especially Main Tenant/admin. | Creator is assigned `HouseholdRole.MAIN_TENANT`; tests verify role. |
| [x] | The WG creation response contains necessary WG data but does not expose sensitive internal fields. | `HouseholdDto` is used instead of exposing full entities. |
| [x] | Invalid WG creation input is rejected. | DTO validation and service checks reject invalid input. |
| [x] | Empty or blank WG names are rejected. | `HouseholdCreateDto` validation covers blank/invalid names. |
| N/A | Duplicate WG names are handled according to the intended project rule. | No global unique WG-name rule is required for this implementation. |
| [x] | WG creation avoids half-created household/membership state on known failure paths. | Service logic and tests cover conflict when the user already belongs to a household. Deeper artificial transaction-failure testing is accepted as optional. |

### 3.2 Invitation Creation

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A Main Tenant / authorized WG admin can create an invitation. | `InvitationEndpointTest` and `InvitationServiceTest` verify main-tenant creation. |
| [x] | A normal roommate cannot create invitations if policy reserves this to Main Tenant/admin. | `InvitationServiceImpl.requireMainTenant(...)` enforces role; tests cover roommate forbidden. |
| [x] | Unauthenticated users cannot create invitations. | Endpoint requires authenticated user role. |
| [x] | A user from another WG cannot create invitations for a WG they do not belong to. | Endpoint/service tests verify non-member forbidden behavior. |
| [x] | Invitation token/link is time-limited. | Invitations have `expiresAt`; expired tokens are rejected with `GoneException` / HTTP 410. |
| [x] | Invitation token is difficult to guess and not sequential/predictable. | Token generation uses secure random bytes and token hashes are stored/looked up by SHA-256 hash. |
| [x] | Invitation token is not logged in a way that exposes it unnecessarily. | Fixed during QA. `LogFilter` now sanitizes `/api/v1/invitations/{token}` and `/api/v1/invitations/{token}/accept`; endpoint logs use `{token}` placeholders. |
| [x] | Invitation contains enough information to join the correct WG but does not expose unrelated WG/private data. | Invitation preview/accept responses use DTOs and only expose relevant invitation/household information. |
| [x] | Invitation creation is covered by automated backend tests. | `InvitationEndpointTest` and `InvitationServiceTest` pass. |

### 3.3 Invitation Acceptance and Membership

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A valid invitation allows a user to join the intended WG. | Integration test covers create, preview, and accept flow. |
| [x] | Joining through an invitation creates the correct membership/roommate record. | Tests assert active `Roommate` membership exists after acceptance. |
| [x] | The invited user receives the correct default role, normally Roommate. | Accepted invitation assigns `HouseholdRole.ROOMMATE`. |
| [x] | Expired invitations are rejected. | `InvitationServiceTest` covers expired token rejection. |
| [x] | Invalid or unknown tokens are rejected. | Token hash lookup and tests cover invalid/not-found behavior. |
| [x] | Already-used invitations are rejected because invitations are single-use. | `usedAt` is checked; used invitations return `GoneException` / HTTP 410. |
| [x] | A user cannot join a WG twice and create duplicate active membership records. | Added QA test: first accept succeeds, second accept returns 410, user is active member, and active member count does not increase beyond expected. |
| [x] | A user cannot use an invitation to gain Main Tenant/admin privileges unless explicitly intended. | Invitation acceptance assigns `ROOMMATE`, not `MAIN_TENANT`. |
| [x] | Accepting an invitation does not expose data from other WGs. | Acceptance response is DTO-limited to the invitation's household. |
| [x] | Invitation acceptance does not leave partial records on known failure paths. | Added service test verifies an already-household-member conflict does not save a new roommate and does not mark the invitation as used. |
| [x] | Invitation acceptance is covered by automated backend tests. | Endpoint and service tests cover success, expired, used, revoked/invalid, duplicate-acceptance, and forbidden cases. |

### 3.4 WG Isolation and RBAC

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | User A in WG A cannot fetch WG B details through direct endpoint access. | Service-level membership checks protect WG-scoped resources. |
| [x] | User A in WG A cannot list WG B members. | `HouseholdServiceImpl.getMembers(...)` checks active membership. |
| [x] | User A in WG A cannot create invitations for WG B. | Invitation service requires active membership and `MAIN_TENANT` in the target household. |
| [x] | Main Tenant/admin-only actions are protected by backend role checks. | Invitation create/list/delete uses `requireMainTenant(...)`; tests cover denied roles/users. |
| [x] | RBAC tests cover allowed and denied cases. | Service and endpoint tests cover main tenant allowed, roommate forbidden, and non-member forbidden. |

---

## 4. FR-03 Backend QA — Task Creation and Assignment

### 4.1 Task Data Model and Persistence

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Task and task assignment entities match the intended domain model. | `Task` includes `household`, `createdBy`, `assignedTo`, `dueDate`, `status`, `createdAt`, and `updatedAt`. |
| [x] | Tasks are linked to the correct WG/household. | Service sets task household from the current user's active household. |
| [x] | Task assignments are linked to valid users/roommates. | Assignment uses `ApplicationUser`; service verifies assignee is active in the same household. |
| [x] | Task data persists correctly in H2 through JPA/repositories. | `TaskRepositoryTest` covers saving and querying tasks. |
| [x] | Task creation stores creation timestamp and/or created-by user if audit fields exist. | `Task` stores `createdBy`, `createdAt`, and `updatedAt`; tests verify creator data. |
| N/A | Task assignment stores assignment timestamp and/or assigned-by user if audit fields exist. | No separate assignment timestamp/assigned-by field is implemented for FR-03. |
| [x] | Deleting or modifying users does not break task assignment data in the tested FR-03 scope. | User deletion is not part of FR-01 to FR-03 implemented scope; task assignment uses foreign keys and DTO mapping for existing users. |

### 4.2 Task Creation Endpoint

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Authenticated WG members can create a task if the intended policy allows all members. | Current policy allows household members; endpoint and service tests verify creation. |
| N/A | If task creation is restricted to Main Tenant/admin, normal roommates are rejected. | Current implementation does not restrict task creation to Main Tenant/admin only. |
| [x] | Unauthenticated users cannot create tasks. | Endpoint is secured with `@Secured("ROLE_USER")`; security tests cover protected access. |
| [x] | Users cannot create tasks in WGs they do not belong to. | Service requires active household membership and same-household assignee. |
| [x] | Task title/name is required. | `TaskCreateDto` validation and tests cover invalid/blank task creation. |
| [x] | Blank task title/name is rejected. | Invalid body tests return HTTP 400. |
| [x] | Optional fields such as description and due date are validated according to implementation. | Implemented fields are validated by DTO types/annotations. |
| N/A | Invalid due dates are rejected if the implementation forbids them. | No business rule forbidding past dates exists in the current FR-03 scope. |
| [x] | Task creation returns an appropriate HTTP status code and response body. | Task creation returns HTTP 201 and task details in tests. |
| [x] | Failed task creation does not persist partial data. | Tests verify forbidden/outside-household assignment does not create a task. |
| [x] | Task creation is covered by automated backend tests. | `TaskEndpointTest`, `SimpleTaskServiceTest`, and `TaskRepositoryTest` pass. |

### 4.3 Task Assignment Endpoint / Logic

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A task can be assigned to a valid roommate in the same WG. | Endpoint tests verify same-household assignment. |
| [x] | A task cannot be assigned to a user outside the WG. | Endpoint and service tests reject outside-household assignees. |
| [x] | A task cannot be assigned to a non-existing user. | Service resolves assignee by email and throws not-found; tests cover missing user. |
| [x] | A user cannot assign or reassign tasks in a WG they do not belong to. | Service checks current household and assignee household. |
| [x] | Assignment respects the intended authorization policy. | Current policy: members can create; main tenant/creator can manage; status update can be done by main tenant, creator, or assignee. |
| N/A | Duplicate assignment is handled correctly according to the intended rule. | No multi-assignment model exists; each task has one `assignedTo`. |
| [x] | Reassignment is handled correctly if implemented. | Tests cover task update and reassignment inside the same household. |
| [x] | Assignment status defaults to the intended initial value. | New tasks default to `TaskStatus.OPEN`. |
| [x] | Assignment due date is stored and returned correctly. | DTOs and mapper/tests cover due date. |
| [x] | Assignment is covered by automated backend tests. | Assignment logic is covered by endpoint, service, repository, and mapper tests. |

### 4.4 Task Retrieval, Visibility, and Tracking

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A WG member can retrieve tasks for their own WG. | `GET /api/v1/tasks` is implemented and tested. |
| [x] | A WG member can retrieve their own assigned tasks. | `GET /api/v1/tasks/my` is implemented and tested. |
| [x] | Task lists do not include tasks from other WGs. | Repository queries are household-scoped; tests verify isolation. |
| [x] | Direct ID access cannot reveal tasks from another WG. | Tests verify cross-household task access returns 403. |
| [x] | Task status can be read correctly. | `TaskDetailDto` includes status; mapping tests pass. |
| [x] | Task completion/status update works if implemented in FR-03. | `PATCH /api/v1/tasks/{id}/status` is implemented and tested. |
| [x] | Task completion/status update is restricted to the allowed user(s). | Service permits main tenant, creator, or assignee; rejects unrelated roommate. |
| N/A | Invalid status transitions are rejected if the implementation defines a task status lifecycle. | No explicit status-transition lifecycle exists; enum-based status update is implemented. |
| [x] | Retrieval and visibility are covered by automated backend tests. | `TaskEndpointTest`, `SimpleTaskServiceTest`, and `TaskRepositoryTest` cover retrieval and visibility. |

### 4.5 Task Error Handling and Edge Cases

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Non-existing task ID returns 404 or equivalent documented error. | Service tests cover not-found task behavior. |
| [x] | Non-existing WG ID returns 404 or equivalent documented error. | Household/invitation services use repository lookup and throw `NotFoundException`. |
| [x] | Unauthorized access returns 401. | Login/security tests cover unauthorized authentication behavior. |
| [x] | Authenticated but forbidden access returns 403. | Task and invitation tests cover forbidden cross-household/non-member cases. |
| [x] | Invalid request body returns 400. | DTO validation and invalid-body tests cover HTTP 400. |
| [x] | Backend does not leak stack traces or SQL errors to the client. | Error handler returns controlled error messages/statuses; no client stack traces were observed. |
| [x] | Repeated requests do not create inconsistent duplicate assignment/membership state in the tested scope. | Duplicate invitation acceptance was added and verified; task assignment has one assignee per task and update semantics. Full concurrency testing is accepted as out of scope. |

---

## 5. Security and Privacy Backend Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Passwords are hashed and salted. | BCryptPasswordEncoder is used; tests verify stored password is not plaintext and matches through BCrypt. |
| [x] | Authentication tokens expire. | JWT expiration is configured and used by `JwtTokenizer`. |
| [x] | JWT secret/config is acceptable for dev QA scope. | JWT secret is still dev/demo configuration and accepted as a follow-up note. This is not production-ready and should be externalized before production. |
| [x] | Invitation links are time-limited and hard to guess. | Invitations expire; tokens are generated randomly and stored/checked by hash. |
| [x] | Users can only access data belonging to their own WG unless explicitly public. | Household, invitation, and task service checks enforce active membership and household scoping. |
| [x] | Main Tenant/admin functions are protected by backend role checks. | Invitation management requires MAIN_TENANT; tests cover forbidden users. |
| [x] | Logs do not contain plaintext passwords. | MockMvc `.andDo(print())` output was removed; application endpoint logs no longer log request bodies. |
| [x] | Logs do not contain JWT tokens. | MockMvc result printing was removed; application logs do not log Authorization headers. |
| [x] | Logs do not unnecessarily expose invitation tokens. | Fixed. `LogFilter` sanitizes invitation-token paths and tests verify log output shows `{token}`. |
| [x] | Error messages do not expose database internals or stack traces. | Controlled application exceptions and global handler are used. |
| [x] | CORS settings are appropriate for the dev/demo environment. | CORS is restricted to configured local/dev origins rather than a fully open unrestricted production-like policy. |
| [x] | CSRF/session assumptions are consistent with stateless JWT authentication. | Security config uses JWT-style stateless backend behavior; CSRF/session assumptions are accepted for this API setup. |

---

## 6. Data Consistency and Database Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | User, household, membership, invitation, task, and assignment tables/entities are consistent. | Entities exist and relationships match FR-01 to FR-03. |
| [x] | Foreign key relationships exist where needed. | Generated DDL/test logs show foreign keys for household/user, invitation/household/user, roommate/household/user, and task/household/user. |
| [x] | Unique constraints exist where needed, especially email and duplicate memberships. | DDL/entity evidence includes unique user email, unique invitation token hash, and unique `(user_id, household_id)` membership constraint. |
| [x] | Required fields are non-null at entity/database level where appropriate. | DDL/test logs show non-null fields for required user, household, invitation, roommate, and task columns. |
| [x] | Test data can be reset/recreated. | Integration tests create their own data; README documents `generateData`. |
| [x] | H2/API-level inspection confirms expected records after registration, WG creation, invitation acceptance, and task assignment. | Manual acceptance scenario and integration tests verified persisted behavior through API/repository checks. Direct H2-console inspection is accepted as optional. |
| [x] | Application restart does not unexpectedly lose demo data unless this limitation is documented. | Application uses file-based H2 in dev configuration. Live restart smoke test remains optional follow-up. |
| N/A | If data is intentionally in-memory for dev, this is documented as a limitation. | Main dev configuration is file-based H2; tests use in-memory H2. |

---

## 7. Automated Backend Test Coverage Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | FR-01 has automated tests for successful registration. | `UserEndpointTest` passes. |
| [x] | FR-01 has automated tests for duplicate/invalid registration. | Duplicate and invalid registration cases are tested. |
| [x] | FR-01 has automated tests for successful login. | `LoginEndpointTest` passes. |
| [x] | FR-01 has automated tests for failed login. | Wrong password and unknown email cases return 401. |
| [x] | FR-01 has automated tests for protected endpoint access with/without token. | `SecurityTest` passes. |
| [x] | FR-02 has automated tests for WG creation. | `HouseholdEndpointTest` / `HouseholdServiceTest` pass. |
| [x] | FR-02 has automated tests for role-restricted invitation creation. | `InvitationServiceTest` covers main tenant, roommate, and non-member cases. |
| [x] | FR-02 has automated tests for invitation acceptance. | `InvitationEndpointTest` and `InvitationServiceTest` cover acceptance. |
| [x] | FR-02 has automated tests for invalid/expired invitation tokens if expiry is implemented. | Expired, used, revoked/invalid-related cases are covered. |
| [x] | FR-02 has automated tests for cross-WG access denial. | Invitation and household service tests cover non-member/cross-WG denial. |
| [x] | FR-02 has automated tests for duplicate invitation acceptance / duplicate membership prevention. | Added during QA; final suite passes. |
| [x] | FR-03 has automated tests for task creation. | `TaskEndpointTest` and `SimpleTaskServiceTest` pass. |
| [x] | FR-03 has automated tests for assigning a task to a valid roommate. | Same-household assignment is tested. |
| [x] | FR-03 has automated tests for rejecting assignment to users outside the WG. | Outside-household assignment is rejected and tested. |
| [x] | FR-03 has automated tests for task visibility limited to the user's WG. | Task repository/service/endpoint tests cover household-limited visibility. |
| [x] | Tests run in CI, not only locally. | GitLab pipeline confirmed green by QA reviewer. |
| [x] | Critical tests use meaningful assertions and do not only check HTTP 200. | Tests assert status codes, roles, repository state, membership existence, no duplicate membership, household IDs, assigned users, and forbidden/not-found behavior. |

Automated test summary:

```text
Tests run: 74, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 8. Backend Manual Acceptance Scenario

| Step | Expected result | Status | Evidence / Notes |
|---|---|---|---|
| 1 | Register Anna as a new user. | [x] | Manual acceptance scenario completed successfully. |
| 2 | Anna logs in and receives a valid token. | [x] | Manual acceptance scenario completed successfully. |
| 3 | Anna creates a WG. | [x] | Manual acceptance scenario completed successfully. |
| 4 | Anna is Main Tenant/admin of that WG. | [x] | Manual acceptance scenario confirmed role. |
| 5 | Anna creates an invitation link/token. | [x] | Manual acceptance scenario completed; logging was later sanitized. |
| 6 | Register Lukas as a second user. | [x] | Manual acceptance scenario completed successfully. |
| 7 | Lukas accepts Anna's invitation. | [x] | Manual acceptance scenario completed successfully. |
| 8 | Lukas becomes a Roommate in Anna's WG. | [x] | Manual acceptance scenario confirmed roommate membership. |
| 9 | Anna or an authorized member creates a task. | [x] | Manual acceptance scenario completed successfully. |
| 10 | The task is assigned to Lukas. | [x] | Manual acceptance scenario confirmed assignment. |
| 11 | Lukas can see his assigned task. | [x] | Manual acceptance scenario completed successfully. |
| 12 | A third user from another WG cannot see or modify this task. | [x] | Manual acceptance scenario confirmed 403 behavior. |
| 13 | Expired/invalid invitation token is rejected. | [x] | Invalid token checked manually; expired/used token behavior covered by automated tests. |
| 14 | Unauthenticated requests to protected endpoints are rejected. | [x] | Manual acceptance scenario confirmed protected endpoint rejection. |

---

## 9. Backend Bug Report Template

Use this format for every failed item in future QA passes.

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

No blocking bugs remain open for this backend QA pass.

---

## 10. Backend Final Decision

| Decision item | Status / Notes |
|---|---|
| FR-01 backend complete? | [x] Complete for dev QA scope. Registration, login, validation, password hashing, JWT login, protected endpoints, and error handling are covered. |
| FR-02 backend complete? | [x] Complete for dev QA scope. WG creation, MAIN_TENANT role, invitation create/list/preview/accept/delete behavior, token validity, duplicate acceptance prevention, and logging privacy are covered. |
| FR-03 backend complete? | [x] Complete for dev QA scope. Task creation, assignment, reassignment, status update, delete, retrieval, own tasks, and cross-WG isolation are covered. |
| Critical security issues open? | [x] No blocking critical security issue remains for dev QA. JWT secret externalization remains accepted follow-up before production. |
| Critical data consistency issues open? | [x] No blocking critical data consistency issue remains for FR-01 to FR-03. |
| Automated tests passing? | [x] Yes. `Tests run: 74, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`. |
| CI pipeline green? | [x] Yes, confirmed by QA reviewer. |
| Backend ready for dev acceptance? | [x] Yes, passed with accepted follow-up notes. |

Final decision:

- [x] Passed
- [x] Passed with accepted follow-up notes
- [ ] Failed, must fix before acceptance

Accepted follow-up notes, not blockers for this backend QA pass:

| Priority | Item | Reason |
|---|---|---|
| Medium | Externalize JWT secret before production-like deployment. | Current value is accepted as dev/demo config, but not production-safe. |
| Low | Run `generateData` profile as a separate live demo-data smoke test if needed for presentation. | Documentation and generator exist; not required for FR-01 to FR-03 backend correctness. |
| Low | Run file-based H2 restart persistence smoke test if demo persistence must be shown live. | Dev config is file-based H2; API/tests already verify persistence during runtime. |
| Low | Consider reducing TRACE-level Spring mapping logs in normal test output. | Not a functional or security blocker; only affects log verbosity. |
