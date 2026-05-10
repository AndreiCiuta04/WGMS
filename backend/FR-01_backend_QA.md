# FR-01 Backend QA Checklist — User Registration and Login

## Feature Information

- Feature ID: FR-01
- Feature name: User registration and login
- Use case: UC-01 — User Registration and WG Initialization
- Priority: High
- Category: Core
- Version: V1
- Backend technology: Java Spring Boot
- Security: Spring Security, JWT
- Database: PostgreSQL
- Automated tests: JUnit
- Branch:
- Merge request:
- Developer:
- QA reviewer: Senior QA Engineer
- Date: 2026-05-09
---

## How to Use This Checklist

This checklist must be completed before the backend part of FR-01 can be considered done.

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

Relevant backend files for FR-01:

- `endpoint/UserEndpoint.java`
- `endpoint/LoginEndpoint.java`
- `endpoint/dto/UserRegistrationDto.java`
- `endpoint/dto/UserLoginDto.java`
- `entity/ApplicationUser.java`
- `repository/UserRepository.java`
- `service/UserService.java`
- `service/impl/CustomUserDetailService.java`
- `security/JwtTokenizer.java`
- `security/JwtAuthorizationFilter.java`
- `config/SecurityConfig.java`
- `config/SecurityPropertiesConfig.java`
- `config/EncoderConfig.java`
- `exceptionhandler/GlobalExceptionHandler.java`
- `exception/ConflictException.java`
- `exception/notFoundException.java`
- `datagenerator/UserDataGenerator.java`

Whenever this checklist says “endpoint”, it means the REST class receiving HTTP requests.

---

# 1. Scope Check

- [x] The branch only contains changes related to FR-01 or clearly justified support changes.
- [x] User registration is implemented in the backend.
- [x] User login is implemented in the backend.
- [x] The registration endpoint is placed in `UserEndpoint.java` or another clearly justified endpoint.
- [x] The login endpoint is placed in `LoginEndpoint.java`.
- [x] The feature uses existing backend architecture instead of bypassing layers.
- [x] The feature does not contain unrelated functionality from later features unless clearly justified.
- [x] The implementation supports later WG creation and invitation logic from FR-02.
- [x] The backend starts successfully after the changes.
- [x] The backend compiles successfully.

Notes:

```text

```

---

# 2. API Contract Check

## Registration API

- [x] Registration endpoint path is known and documented.
- [x] Registration endpoint uses the correct HTTP method.
- [x] Registration endpoint accepts JSON.
- [x] Registration endpoint uses `UserRegistrationDto`.
- [x] Registration request contains only necessary registration fields.
- [x] Registration request does not require unnecessary personal data.
- [x] Registration success response has the correct HTTP status.
- [x] Registration validation error response has the correct HTTP status.
- [x] Duplicate user/email response has the correct HTTP status.
- [x] Registration response does not contain the raw password.
- [x] Registration response does not contain the password hash.
- [x] Registration response does not expose unnecessary internal entity fields.
- [x] Registration response format is understandable for the frontend.

## Login API

- [x] Login endpoint path is known and documented.
- [x] Login endpoint uses the correct HTTP method.
- [x] Login endpoint accepts JSON.
- [x] Login endpoint uses `UserLoginDto`.
- [x] Login request contains email/username and password.
- [x] Login success response has the correct HTTP status.
- [x] Login failure response has the correct HTTP status.
- [x] Login success response returns a JWT/token if JWT is used.
- [x] Login response does not contain the raw password.
- [x] Login response does not contain the password hash.
- [x] Login response does not expose internal security details.
- [x] Login response format is understandable for the frontend.

Notes:

```text

```

---

# 3. DTO Validation Check

## `UserRegistrationDto.java`

- [x] Required fields are clearly defined.
- [x] Email/username field exists.
- [x] Password field exists.
- [x] Name/display name exists if required by the project.
- [x] Validation annotations are used where appropriate.
- [x] Empty email/username is rejected.
- [x] Invalid email format is rejected if email is used.
- [x] Empty password is rejected.
- [x] Password length is validated if a password policy exists.
- [x] DTO does not contain unnecessary fields.
- [x] DTO does not contain role/admin fields that a user could abuse.
- [x] DTO does not allow the client to choose privileged roles.
- [x] DTO does not expose backend-only fields.

## `UserLoginDto.java`

- [x] Email/username field exists.
- [x] Password field exists.
- [x] Empty email/username is rejected.
- [x] Empty password is rejected.
- [x] DTO contains only fields needed for login.
- [x] DTO does not contain role/admin fields.
- [x] DTO does not expose backend-only fields.

Notes:

```text

```

---

# 4. Registration Functional Check

- [x] A new user can register with valid data.
- [x] A registered user is saved in the database.
- [x] Registration rejects missing email/username.
- [x] Registration rejects invalid email format if email is used.
- [x] Registration rejects missing password.
- [x] Registration rejects weak/short password if password policy exists.
- [x] Registration rejects duplicate email/username.
- [x] Duplicate email/username check is case-insensitive if this is the agreed behavior.
- [x] Leading and trailing spaces in email/username are handled consistently.
- [x] Registration does not create multiple users from repeated identical requests.
- [x] Registration does not create partial users after validation failure.
- [x] Registration assigns the correct default role.
- [x] Registration does not assign admin/main-tenant privileges unless explicitly intended.
- [x] Registration does not require an existing WG yet unless this is intentionally part of FR-01.
- [x] Registration works after backend restart.
- [x] Registration works on a clean database.
- [x] Registration works with the configured PostgreSQL/test database.

Notes:

```text

```

---

# 5. Login Functional Check

- [x] An existing user can log in with valid credentials.
- [x] A newly registered user can log in.
- [x] Login rejects an unknown email/username.
- [x] Login rejects correct email/username with wrong password.
- [x] Login rejects missing email/username.
- [x] Login rejects missing password.
- [x] Login rejects malformed JSON.
- [x] Login does not create a new user.
- [x] Login does not modify unrelated user data.
- [x] Login returns a valid JWT/token if JWT is used.
- [x] Login failure uses a safe error message.
- [x] Login failure does not reveal unnecessary account details.
- [x] Login works after backend restart.
- [x] Login works with persisted database data.
- [x] Login works with the user created during registration testing.

Notes:

```text

```

---

# 6. Endpoint Layer Check

## `UserEndpoint.java`

- [x] Contains or exposes the registration endpoint.
- [x] Uses the correct route/path.
- [x] Uses the correct HTTP method.
- [x] Accepts `UserRegistrationDto`.
- [x] Validates incoming registration data.
- [x] Calls `UserService`.
- [x] Does not call `UserRepository` directly unless clearly justified.
- [x] Does not contain password hashing logic directly.
- [x] Does not contain database logic directly.
- [x] Returns correct success status.
- [x] Returns correct validation error status.
- [x] Returns correct duplicate-user error status.
- [x] Does not return raw password.
- [x] Does not return password hash.
- [x] Does not expose unnecessary internal fields.

## `LoginEndpoint.java`

- [x] Contains or exposes the login endpoint.
- [x] Uses the correct route/path.
- [x] Uses the correct HTTP method.
- [x] Accepts `UserLoginDto`.
- [x] Validates incoming login data.
- [x] Authenticates valid users.
- [x] Rejects invalid users.
- [x] Returns JWT/token on successful login if JWT is used.
- [x] Does not manually compare raw passwords.
- [x] Does not return raw password.
- [x] Does not return password hash.
- [x] Does not expose unnecessary internal security details.

Notes:

```text

```

---

# 7. Service Layer Check

## `UserService.java`

- [x] Contains the main registration business logic.
- [x] Checks whether the user already exists before creating a new user.
- [x] Throws `ConflictException` or another appropriate exception for duplicate users.
- [x] Encodes/hashes passwords before saving.
- [x] Never saves raw passwords.
- [x] Assigns the correct default role.
- [x] Does not assign privileged roles by default.
- [x] Uses `UserRepository` for persistence.
- [x] Does not directly handle HTTP request/response objects.
- [x] Is covered by JUnit tests.

## `CustomUserDetailService.java`

- [x] Loads users by email/username correctly.
- [x] Rejects unknown users correctly.
- [x] Provides the correct username/email to Spring Security.
- [x] Provides the correct password hash to Spring Security.
- [x] Provides the correct authorities/roles.
- [x] Does not expose password hashes through API responses.
- [x] Is compatible with Spring Security authentication flow.
- [x] Is covered by JUnit tests or integration tests.

Notes:

```text

```

---

# 8. Repository and Entity Check

## `ApplicationUser.java`

- [x] Contains all necessary user fields.
- [x] Contains email/username field.
- [x] Contains password hash field.
- [x] Does not store raw password separately.
- [x] Contains role/authority information if required.
- [x] Has correct database annotations.
- [x] Does not expose passwords through `toString()`.
- [x] Does not expose sensitive fields unnecessarily.
- [x] Supports future relation to WG/household if needed.

## `UserRepository.java`

- [x] Can find users by email/username.
- [x] Supports duplicate-user checks.
- [x] Email/username uniqueness is enforced by repository logic, database constraint, or both.
- [x] Repository methods work with PostgreSQL/test database.
- [x] Repository tests use fake test data.
- [x] Repository tests do not rely on real user data.
- [x] Tests do not depend on `UserDataGenerator.java` unless explicitly intended.

Notes:

```text

```

---

# 9. Password Security Check

- [x] Passwords are never stored as plain text.
- [x] Passwords are hashed using the configured password encoder.
- [x] `EncoderConfig.java` provides a secure password encoder.
- [x] Password hash is different from the raw password.
- [x] Raw passwords are never logged.
- [x] Password hashes are never logged.
- [x] Raw passwords are never returned in API responses.
- [x] Password hashes are never returned in API responses.
- [x] Password comparison uses Spring Security/password encoder logic.
- [x] Password policy is documented if one exists.
- [x] Extremely short passwords are rejected if policy exists.
- [x] Extremely long passwords are handled safely.
- [x] Test users use fake passwords only.
- [x] No real passwords appear in tests, seed data, screenshots, or logs.

Notes:

```text

```

---

# 10. JWT and Authentication Check

## `JwtTokenizer.java`

- [x] Creates a valid JWT/token after successful login.
- [x] Token contains only necessary claims.
- [x] Token does not contain raw password.
- [x] Token does not contain password hash.
- [x] Token contains user identity if needed.
- [x] Token contains roles/authorities if needed.
- [x] Token has an expiration time.
- [x] Token is signed.
- [x] Token secret/key is not hardcoded in committed code.
- [x] Token secret/key is loaded from configuration.
- [x] Invalid or manipulated tokens are rejected.

## `JwtAuthorizationFilter.java`

- [x] Reads JWT/token from the expected request header.
- [x] Handles missing Authorization header safely.
- [x] Rejects missing token for protected endpoints.
- [x] Rejects invalid token for protected endpoints.
- [x] Accepts valid token for protected endpoints.
- [x] Sets the Spring Security authentication context correctly.
- [x] Does not crash on malformed Authorization headers.
- [x] Does not expose token parsing internals to the client.

Notes:

```text

```

---

# 11. Security Configuration Check

## `SecurityConfig.java`

- [x] Registration endpoint is public.
- [x] Login endpoint is public.
- [x] Protected endpoints require authentication.
- [x] Security configuration does not accidentally permit all requests.
- [x] Security configuration does not accidentally disable all protection.
- [x] JWT filter is integrated correctly.
- [x] Password encoder is used correctly.
- [x] CORS is configured intentionally.
- [x] CSRF setting is configured intentionally and documented if disabled.
- [x] Development-only security shortcuts are not active in normal runtime.
- [x] Normal users cannot access privileged endpoints.
- [x] Default registered users do not receive privileged authorities.

## `SecurityPropertiesConfig.java`

- [x] Security properties are loaded from configuration.
- [x] Secrets are not hardcoded.
- [x] Production-like secrets are not committed.
- [x] Test secrets are clearly test-only.
- [x] Missing security configuration fails safely or is handled clearly.

Notes:

```text

```

---

# 12. Exception Handling Check

## `GlobalExceptionHandler.java`

- [x] Handles validation errors.
- [x] Handles duplicate user conflicts.
- [x] Handles not-found cases.
- [x] Handles authentication errors where appropriate.
- [x] Handles malformed request errors.
- [x] Returns consistent error response format.
- [x] Does not expose Java stack traces.
- [x] Does not expose SQL/database errors.
- [x] Does not expose internal package/class names.
- [x] Provides messages that are useful for the frontend.

## Custom Exceptions

- [x] `ConflictException.java` is used for duplicate user/email if appropriate.
- [x] `notFoundException.java` or equivalent is used correctly for missing users if appropriate.
- [x] Exception names and status codes are consistent.
- [x] Exceptions are tested.

Notes:

```text

```

---

# 13. Database and Persistence Check

- [x] User table/entity is created correctly.
- [x] Email/username uniqueness is enforced.
- [x] Password hash is persisted.
- [x] Raw password is not persisted.
- [x] Default role is persisted.
- [x] Registration persists user data correctly.
- [x] Login reads user data correctly.
- [x] Database migration exists if migrations are used.
- [x] Migration works on a clean database if migrations are used.
- [x] Migration does not break existing data if migrations are used.
- [x] Test database is isolated from development/production data.
- [x] Tests clean up after themselves or use isolated transactions.
- [x] No real personal data is stored in test data.
- [x] `UserDataGenerator.java` does not create unsafe default users.
- [x] `UserDataGenerator.java` does not create committed real credentials.
- [x] Generated users have fake/test data only.

Notes:

```text

```

---

# 14. JUnit Test Checklist

## Unit Tests

- [x] Test valid registration creates a user.
- [x] Test missing email/username is rejected.
- [x] Test invalid email is rejected if email validation exists.
- [x] Test missing password is rejected.
- [x] Test weak/short password is rejected if policy exists.
- [x] Test duplicate email/username is rejected.
- [x] Test password is encoded before saving.
- [x] Test password hash is not equal to raw password.
- [x] Test default role is assigned.
- [x] Test privileged role is not assigned by default.
- [x] Test valid login succeeds.
- [x] Test wrong password fails.
- [x] Test unknown user fails.
- [x] Test login response does not expose password/hash.

## Endpoint/API Tests

- [x] Test registration endpoint accepts valid JSON.
- [x] Test registration endpoint rejects invalid JSON.
- [x] Test registration endpoint returns correct status on success.
- [x] Test registration endpoint returns correct status on validation error.
- [x] Test registration endpoint returns correct status on duplicate user.
- [x] Test login endpoint accepts valid JSON.
- [x] Test login endpoint rejects invalid credentials.
- [x] Test login endpoint returns correct status on success.
- [x] Test login endpoint returns correct status on failure.
- [x] Test API responses do not contain raw password.
- [x] Test API responses do not contain password hash.

## Integration Tests

- [x] Test register user and then login with same credentials.
- [x] Test duplicate registration fails after first registration.
- [x] Test user persists in database.
- [x] Test generated JWT can access a protected endpoint.
- [x] Test missing JWT cannot access protected endpoint.
- [x] Test invalid JWT cannot access protected endpoint.
- [x] Test expired JWT is rejected if feasible.
- [x] Test database cleanup/rollback works between tests.

## Security Tests

- [x] Test registration is accessible without token.
- [x] Test login is accessible without token.
- [x] Test protected endpoint is not accessible without token.
- [x] Test protected endpoint is accessible with valid token.
- [x] Test normal user cannot access privileged endpoint.
- [x] Test manipulated token signature is rejected.
- [x] Test password/hash never appears in response body.

Notes:

```text

```

---

# 15. Manual Backend Testing

Use Postman, curl, IntelliJ HTTP client, Swagger, or another HTTP client.

- [x] Manually register a valid test user.
- [x] Manually try to register with missing email/username.
- [x] Manually try to register with invalid email.
- [x] Manually try to register with missing password.
- [x] Manually try to register with duplicate email/username.
- [x] Manually log in with valid credentials.
- [x] Manually log in with wrong password.
- [x] Manually log in with unknown email/username.
- [x] Manually call a protected endpoint without token.
- [x] Manually call a protected endpoint with invalid token.
- [x] Manually call a protected endpoint with valid token.
- [x] Manually inspect the database and confirm password is hashed.
- [x] Manually inspect backend logs and confirm no password/token leak.
- [x] Manual test evidence is attached to the merge request.

Manual evidence:

```text
Tester:
Date: 2026-05-09
Environment:
Evidence link or notes:

```

---

# 16. GDPR and Privacy Check

- [x] Backend collects only necessary data for registration/login.
- [x] Backend does not collect unnecessary personal data.
- [x] Backend does not collect sensitive personal data for FR-01.
- [x] Backend does not store raw passwords.
- [x] Backend does not expose personal data through public endpoints.
- [x] Backend does not expose user lists publicly.
- [x] Backend logs do not contain passwords.
- [x] Backend logs do not contain full tokens.
- [x] Backend logs do not contain unnecessary personal data.
- [x] Test data uses fake users only.
- [x] Seed/generated data uses fake users only.
- [x] Screenshots and test evidence do not expose real users.
- [x] Purpose of collected registration data is documented.
- [x] Data deletion/export is documented as implemented, future work, or out of scope.
- [x] Access to user data is protected by authentication/authorization.

Notes:

```text

```

---

# 17. Non-Functional Backend Check

## Security

- [x] Authentication works correctly.
- [x] Authorization works correctly.
- [x] Passwords are hashed.
- [x] JWT/token handling works correctly.
- [x] Secrets are not committed.
- [x] Error responses are safe.
- [x] Protected endpoints are protected.
- [x] Public endpoints are intentionally public.

## Reliability

- [x] Backend handles invalid requests without crashing.
- [x] Backend handles duplicate registrations safely.
- [x] Backend handles repeated login failures without crashing.
- [x] Backend starts successfully.
- [x] Backend works after restart.
- [x] Database persistence works.

## Performance

- [x] Registration response time is acceptable in local/dev environment.
- [x] Login response time is acceptable in local/dev environment.
- [x] No obvious unnecessary database calls are made.
- [x] No obvious memory/resource leak appears during repeated auth requests.

## Maintainability

- [x] Endpoint layer is not overloaded with business logic.
- [x] Service layer contains business logic.
- [x] Repository layer handles persistence.
- [x] DTOs are used appropriately.
- [x] Entities are not exposed unnecessarily.
- [x] Security logic is centralized.
- [x] Error handling is centralized.
- [x] Code naming is understandable.
- [x] No commented-out dead code remains.
- [x] No temporary debug logs remain.

Notes:

```text

```

---

# 18. Backend CI and Merge Readiness

- [x] Backend compiles.
- [x] Backend starts.
- [x] All JUnit unit tests pass.
- [x] All JUnit integration tests pass.
- [x] All security tests pass.
- [x] Database tests pass.
- [x] Docker build passes if configured.
- [x] GitLab CI pipeline passes.
- [x] No secrets are committed.
- [x] No local config files are accidentally committed.
- [x] No real user data is committed.
- [x] Manual backend testing is complete.
- [x] Backend documentation/API notes are updated.
- [x] All critical backend items are checked.
- [x] All unchecked items are documented with reason.
- [x] Backend QA reviewer approves.

---

# Final Backend QA Decision

Choose one:

- [x] APPROVED — backend can be merged
- [x] REJECTED — backend fixes required
- [x] APPROVED WITH MINOR NON-BLOCKING ISSUES

Final backend summary:

```text
Backend QA result: PASSED
Critical issues: None
Non-critical issues: None
JUnit evidence:
Manual test evidence:
Security/GDPR notes:
Decision: APPROVED
QA reviewer: Senior QA Engineer
Date: 2026-05-09
```