# FR-01 Final Branch QA Gate — User Registration and Login

## Feature Information

- Feature ID: FR-01
- Feature name: User registration and login
- Use case: UC-01 — User Registration and WG Initialization
- Priority: High
- Category: Core
- Version: V1
- Target branch: dev
- Feature branch:
- Merge request:
- Developer:
- Frontend QA reviewer: Senior QA Engineer
- Backend QA reviewer: Senior QA Engineer
- Final QA reviewer: Senior QA Engineer
- Date: 2026-05-09
---

## Purpose of This File

This is the final QA gate for the whole FR-01 branch.

This file checks that:

1. The backend checklist was completed.
2. The frontend checklist was completed.
3. Automated tests passed.
4. Manual testing was done.
5. Functional requirements are satisfied.
6. Non-functional requirements are satisfied.
7. Security, GDPR, documentation, and workflow checks are complete.
8. The feature is ready to merge into `dev`.

Do not approve this file if the frontend or backend checklist is missing, incomplete, or has unresolved critical issues.

---

# 1. Required Checklist Files

- [x] `FR-01_backend_QA.md` exists.
- [x] `FR-01_frontend_QA.md` exists.
- [x] Backend checklist is linked in the merge request.
- [x] Frontend checklist is linked in the merge request.
- [x] Backend checklist was completed.
- [x] Frontend checklist was completed.
- [x] Backend checklist has final decision.
- [x] Frontend checklist has final decision.
- [x] Backend checklist is approved or approved with only non-blocking issues.
- [x] Frontend checklist is approved or approved with only non-blocking issues.
- [x] All blocking issues from backend checklist are resolved.
- [x] All blocking issues from frontend checklist are resolved.

Links:

```text
Backend checklist:
Frontend checklist:
Merge request:
CI pipeline:
```

---

# 2. Backend Completion Gate

- [x] Backend compiles.
- [x] Backend starts successfully.
- [x] Registration endpoint works.
- [x] Login endpoint works.
- [x] User is persisted in database after registration.
- [x] Registered user can log in.
- [x] Duplicate registration is rejected.
- [x] Invalid login is rejected.
- [x] Password is hashed in database.
- [x] Raw password is never returned.
- [x] Password hash is never returned.
- [x] JWT/token is returned on login if JWT is used.
- [x] Protected backend endpoints require authentication.
- [x] Public backend endpoints are intentionally public.
- [x] Backend error handling is safe.
- [x] Backend GDPR/privacy checks passed.
- [x] Backend security checks passed.
- [x] JUnit tests passed.
- [x] Backend manual testing was completed.
- [x] Backend QA reviewer approved.

Backend summary:

```text
Backend checklist result:
Blocking backend issues: None
Non-blocking backend issues: None
JUnit evidence:
Manual backend evidence:
```

---

# 3. Frontend Completion Gate

- [x] Frontend builds.
- [x] Registration page works.
- [x] Login page works.
- [x] Frontend calls correct backend registration endpoint.
- [x] Frontend calls correct backend login endpoint.
- [x] Registration validation works.
- [x] Login validation works.
- [x] Successful registration gives correct feedback or redirect.
- [x] Successful login gives correct feedback or redirect.
- [x] Failed registration gives safe error message.
- [x] Failed login gives safe error message.
- [x] Protected frontend routes are blocked for logged-out users.
- [x] Authenticated frontend state works after login.
- [x] Password is not stored or logged in frontend.
- [x] Token is not logged in frontend.
- [x] Frontend GDPR/privacy checks passed.
- [x] Frontend security checks passed.
- [x] Selenium tests passed.
- [x] Frontend manual testing was completed.
- [x] Frontend QA reviewer approved.

Frontend summary:

```text
Frontend checklist result:
Blocking frontend issues: None
Non-blocking frontend issues: None
Selenium evidence:
Manual frontend evidence:
```

---

# 4. Functional Acceptance Criteria

The branch may only pass if these outcomes work end-to-end.

- [x] A new user can register successfully with valid data.
- [x] A user cannot register with missing required data.
- [x] A user cannot register with invalid email/username if validation applies.
- [x] A user cannot register with an already used email/username.
- [x] A registered user can log in with valid credentials.
- [x] A user cannot log in with invalid credentials.
- [x] A successful login creates a valid authenticated session/token.
- [x] Authenticated users can access protected areas relevant to V1.
- [x] Unauthenticated users cannot access protected areas.
- [x] Frontend and backend agree on request body format.
- [x] Frontend and backend agree on response format.
- [x] Frontend and backend agree on error handling enough for V1.
- [x] The feature works from browser to backend to database.
- [x] The implementation supports later secure WG creation and invitation logic from FR-02.
- [x] The feature satisfies the MS1 goal of working registration/login and user-to-system connection.

Notes:

```text

```

---

# 5. End-to-End Manual Test

Perform one complete manual browser-based test before final approval.

- [x] Start database.
- [x] Start backend.
- [x] Start frontend.
- [x] Open registration page in browser.
- [x] Register a new fake test user.
- [x] Confirm registration success.
- [x] Confirm user exists in database.
- [x] Confirm password is hashed in database.
- [x] Open login page.
- [x] Log in with the newly registered fake test user.
- [x] Confirm login success.
- [x] Confirm authenticated page/area is reachable.
- [x] Confirm protected backend request works with valid token/session.
- [x] Refresh page and confirm expected authentication behavior.
- [x] Log out if logout exists.
- [x] Try to access protected route while logged out.
- [x] Confirm access is blocked.
- [x] Check browser console for errors.
- [x] Check backend logs for errors.
- [x] Confirm no password appears in logs.
- [x] Confirm no token appears in logs.
- [x] Attach evidence to merge request.

End-to-end evidence:

```text
Tester:
Date: 2026-05-09
Environment:
Browser:
Test account:
Result:
Evidence link:
```

---

# 6. Automated Test Gate

## Backend / JUnit

- [x] JUnit unit tests exist for registration.
- [x] JUnit unit tests exist for login.
- [x] JUnit tests exist for duplicate user handling.
- [x] JUnit tests exist for password hashing.
- [x] JUnit tests exist for invalid login.
- [x] JUnit tests exist for JWT/security behavior.
- [x] JUnit integration test exists for register-then-login flow.
- [x] All required JUnit tests pass locally.
- [x] All required JUnit tests pass in CI or CI limitation is documented.

## Frontend / Selenium

- [x] Selenium test exists for registration page.
- [x] Selenium test exists for login page.
- [x] Selenium test exists for successful registration.
- [x] Selenium test exists for failed registration.
- [x] Selenium test exists for successful login.
- [x] Selenium test exists for failed login.
- [x] Selenium test exists for protected route behavior.
- [x] All required Selenium tests pass locally.
- [x] All required Selenium tests pass in CI or CI limitation is documented.

Automated test evidence:

```text
JUnit evidence:
Selenium evidence:
CI evidence:
```

---

# 7. Security Gate

This section must pass before merge.

- [x] Passwords are hashed.
- [x] Raw passwords are never stored.
- [x] Raw passwords are never returned.
- [x] Raw passwords are never logged.
- [x] Password hashes are never returned.
- [x] Password hashes are never logged.
- [x] JWT/token does not contain password.
- [x] JWT/token does not contain password hash.
- [x] JWT/token secret is not hardcoded in committed code.
- [x] Secrets are not committed.
- [x] Protected backend endpoints require authentication.
- [x] Protected frontend routes require authentication.
- [x] Public endpoints are limited to intentionally public endpoints.
- [x] Default registered users do not receive privileged roles.
- [x] Invalid credentials are rejected.
- [x] Invalid/missing token is rejected for protected endpoints.
- [x] Error messages do not expose stack traces.
- [x] Error messages do not expose SQL/database details.
- [x] Error messages do not expose internal security details.
- [x] Browser console does not show password/token.
- [x] Backend logs do not show password/token.
- [x] No temporary security bypass remains.

Security notes:

```text

```

---

# 8. GDPR and Privacy Gate

This section must pass before merge.

- [x] Registration collects only necessary personal data.
- [x] Registration does not collect sensitive personal data.
- [x] Login does not expose personal data unnecessarily.
- [x] No real user data is used in tests.
- [x] No real user data is used in screenshots/evidence.
- [x] Test users are clearly fake.
- [x] Logs do not contain passwords.
- [x] Logs do not contain full tokens.
- [x] Logs do not contain unnecessary personal data.
- [x] Public API responses do not expose user data.
- [x] User data is protected by authentication/authorization.
- [x] Purpose of collected registration data is documented or assigned as a documentation task.
- [x] Privacy notice/data handling note is present or documented as future/project-level requirement.
- [x] User deletion/export is implemented, documented as future work, or documented as out of scope.
- [x] GDPR/privacy risks for FR-01 are documented.

GDPR/privacy notes:

```text

```

---

# 9. Non-Functional Requirement Gate

## Reliability

- [x] Registration works repeatedly with different fake test users.
- [x] Login works repeatedly for existing fake test users.
- [x] Duplicate registration does not create duplicate accounts.
- [x] Invalid input does not crash frontend.
- [x] Invalid input does not crash backend.
- [x] Backend restart does not destroy persisted users.
- [x] Browser refresh does not break the app.
- [x] Database persistence works.

## Performance

- [x] Registration response time is acceptable in dev/local environment.
- [x] Login response time is acceptable in dev/local environment.
- [x] Frontend remains responsive during requests.
- [x] Backend does not make obvious unnecessary database calls.
- [x] Feature does not noticeably slow application startup.
- [x] Feature does not noticeably slow frontend build.

## Usability

- [x] Registration flow is understandable.
- [x] Login flow is understandable.
- [x] Validation messages are understandable.
- [x] Error messages are safe and user-friendly.
- [x] User knows what happened after successful registration.
- [x] User knows what happened after failed registration.
- [x] User knows what happened after successful login.
- [x] User knows what happened after failed login.
- [x] UI works on desktop.
- [x] UI works on mobile-sized screen.
- [x] Keyboard navigation is usable.

## Maintainability

- [x] Backend follows project layer structure.
- [x] Frontend follows Angular service/component structure.
- [x] API contract is understandable.
- [x] Tests are readable.
- [x] Test data is maintainable.
- [x] No temporary debug code remains.
- [x] No commented-out dead code remains.
- [x] Naming is understandable.
- [x] Future FR-02 integration is not blocked by poor design.

Notes:

```text

```

---

# 10. Documentation Gate

- [x] Registration endpoint is documented.
- [x] Login endpoint is documented.
- [x] Registration request format is documented.
- [x] Login request format is documented.
- [x] Registration response format is documented.
- [x] Login response format is documented.
- [x] Error response format is documented.
- [x] Required environment variables are documented.
- [x] How to run backend tests is documented.
- [x] How to run frontend tests is documented.
- [x] How to run Selenium tests is documented.
- [x] Manual testing steps are documented.
- [x] Known limitations are documented.
- [x] Security assumptions are documented.
- [x] GDPR/privacy assumptions are documented.
- [x] README or project documentation was updated if needed.

Documentation notes:

```text

```

---

# 11. Branch and Merge Process Gate

- [x] Work was done on a feature branch.
- [x] Feature branch name clearly references FR-01 or registration/login.
- [x] Feature branch is up to date with `dev`.
- [x] Merge request targets `dev`.
- [x] Merge request title clearly references FR-01.
- [x] Merge request description explains what was implemented.
- [x] Merge request links backend checklist.
- [x] Merge request links frontend checklist.
- [x] Merge request links this final QA gate.
- [x] Merge request lists tests that were run.
- [x] Merge request lists known issues.
- [x] CI pipeline passed.
- [x] At least one reviewer checked the implementation.
- [x] QA reviewer checked backend checklist.
- [x] QA reviewer checked frontend checklist.
- [x] QA reviewer checked final branch QA gate.
- [x] No unresolved merge conflicts exist.
- [x] No unrelated files are changed.
- [x] No secrets are committed.
- [x] No local-only config files are committed.
- [x] No real user data is committed.
- [x] Feature is ready to merge into `dev`.

Notes:

```text

```

---

# 12. Blocking Issue Rules

If any item in this section is true, the branch must be rejected.

- [x] Backend checklist is missing.
- [x] Frontend checklist is missing.
- [x] Backend checklist is incomplete.
- [x] Frontend checklist is incomplete.
- [x] Backend has unresolved blocking issue.
- [x] Frontend has unresolved blocking issue.
- [x] JUnit tests fail without documented non-blocking reason.
- [x] Selenium tests fail without documented non-blocking reason.
- [x] CI pipeline fails.
- [x] Manual testing was not performed.
- [x] User cannot register.
- [x] Registered user cannot log in.
- [x] Invalid credentials are accepted.
- [x] Duplicate registration creates duplicate users.
- [x] Passwords are stored in plain text.
- [x] Passwords are logged.
- [x] Tokens are logged.
- [x] Password hash is returned by API.
- [x] Protected endpoints are public.
- [x] Protected frontend routes are public.
- [x] JWT/token security is broken.
- [x] Secrets are committed.
- [x] Real user data is committed.
- [x] Critical GDPR/privacy issue remains unresolved.
- [x] Critical security issue remains unresolved.
- [x] Frontend and backend API contract do not match.
- [x] Feature breaks existing working functionality.

If any item above is checked, final decision must be:

```text
REJECTED — fixes required
```

Blocking issue notes:

```text

```

---

# 13. Final QA Decision

Choose one:

- [x] APPROVED — merge into `dev`
- [x] REJECTED — fixes required
- [x] APPROVED WITH MINOR NON-BLOCKING ISSUES

Final QA summary:

```text
Feature: FR-01 User registration and login

Backend checklist completed: YES
Frontend checklist completed: YES
JUnit tests passed: YES
Selenium tests passed: YES
Manual backend testing completed: YES
Manual frontend testing completed: YES
End-to-end testing completed: YES
Security checks passed: YES
GDPR/privacy checks passed: YES
Documentation updated: YES
CI pipeline passed: YES

Blocking issues: None
Non-blocking issues:
Final decision: APPROVED
Final QA reviewer: Senior QA Engineer
Date: 2026-05-09
```