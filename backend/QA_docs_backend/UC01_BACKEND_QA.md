# UC-01 — BACKEND QA FORM

    ## Embedded QA-agent instructions — no proposal upload required

    The functional, non-functional, security, privacy, reliability, usability, maintainability, auditability,
    architecture, API-contract, CI, browser-support, and known-regression requirements relevant to UC-01 are embedded
    in this file. **Do not ask the user to upload the Project Proposal.**

    Before checking anything:
    1. Inspect the current repository, existing tests, OpenAPI, frontend routes/services, backend controllers/services/
       repositories/entities/security configuration, database schema, seed/reset scripts, user documentation, and CI.
    2. Map every checklist item to exact source files, endpoint paths, tests, and commands in its evidence record.
    3. Mark PASS only when the stated evidence exists and the required automated proof has run successfully.
    4. If an item lacks evidence or fails, fix it, add the missing regression test, rerun affected suites, and only then
       check PASS. Do not weaken the checklist, delete tests, skip tests, use `|| true`, or replace a test with a claim.
    5. Use synthetic data only. Never paste real personal data, passwords, JWTs, raw invitation tokens, password hashes,
       or secrets into this file, test fixtures, logs, screenshots, or commits.
    6. Any unresolved FAIL/BLOCKED item must remain visible. The feature cannot be declared complete while a critical
       item is unresolved.

    ## Scope

    **Layer:** Spring Boot REST API, JWT/RBAC, services, entities, JPA/H2, transactions, persistence, logs, OpenAPI, CI, documentation, and backend tests.

    **UC-01 functional requirements**
    - **FR-01 — User registration and login:** users can register securely and log in.
    - **FR-02 — Secure WG creation and time-limited invitation links:** an authenticated user can create a WG; the
      creator is the Main Tenant; Main Tenants generate in-app invitations that are single-use, securely hashed,
      configurable in expiry, and shared through their own channel. Invitation emails are not automatically sent.
      Email-based password reset is not required. Roles are MAIN_TENANT and ROOMMATE, and role checks are backend-enforced.

    **Non-functional requirements that must be covered**
    - **NFR-01 Security & Access Control:** authentication before WG data; salted password hashes; backend RBAC;
      only Main Tenant manages invitations, member removal, and important WG settings; OWASP-ASVS-oriented checks.
    - **NFR-02 Data Privacy & GDPR:** data minimization; access/correction/deletion or anonymization where possible;
      time-limited invitations; no secret/PII leakage; privacy information and safe historical retention.
    - **NFR-03 Reliability:** reliable persistence of membership/invite state; reproducible initialization/seed data;
      safe transactions, duplicates, concurrency, restart and controlled errors.
    - **NFR-04 Usability & Compatibility:** peer-testable workflows; WAI/Lighthouse/accessibility checks; reference
      platforms: Chrome/Firefox desktop 1366×768+, iPhone 12 Pro Safari 390×844, Pixel 7 Chrome 412×915, and 360px boundary.
    - **NFR-05 Maintainability:** clean frontend/backend/database/business-logic separation; meaningful documentation.
    - **NFR-06 Auditability:** membership-changing actions must be traceable by timestamp and responsible actor.

    **Relevant technical goals**
    - TG-01 three-tier architecture; TG-02 JWT + BCrypt + backend RBAC; TG-04 OpenAPI contract;
      TG-05 CI/CD; TG-07 GDPR-aligned deletion/anonymization and expiring invites; TG-08 browser support;
      TG-09 critical authentication/invitation test coverage; TG-10 documentation.

    **Known mandatory regressions**
    - **BUG #35:** an already-existing user can accept an invitation without failure, duplicate account, or 500.
    - **BUG #38:** leave then rejoin the same WG does not hit a unique constraint or return 500.
    - **BUG #42:** after a roommate leaves/deletes/anonymizes, a privacy-safe historical identity remains so later
      financial settlement is still possible. UC-01 owns the lifecycle/reference side; UC-03 owns settlement arithmetic.

    ## Coverage ownership in this file

    This file is the primary owner for server-side enforcement of every UC-01 functional requirement and for NFR-01, NFR-02, NFR-03, NFR-05 and NFR-06. It also provides backend evidence for NFR-04 where API behavior affects usability.

    | Requirement group | Covered by these sections |
|---|---|
| FR-01 registration/login | A, B |
| FR-02 WG creation/invitations/roles | C, D, E |
| NFR-01 security/RBAC/JWT/IDOR/input hardening | A–H |
| NFR-02 privacy/GDPR/token hashing/anonymization/logging | A, D–H |
| NFR-03 persistence/transactions/duplicates/concurrency/restart | A, C–H |
| NFR-04 reliable API error contract supporting usable UI | A–H |
| NFR-05 architecture/tests/docs/CI maintainability | C, H |
| NFR-06 membership auditability | C, E–H |

    ## Completion standard

    A checkbox is PASS only where the live behavior, persistence/API behavior where relevant, and required automated
    evidence agree. “Code looks correct”, “endpoint exists”, “button is visible”, “works manually once”, or “pipeline
    was green before” is insufficient.

## A. FR-01 — Registration API, validation, hashing and persistence

### BE-001 — Registration endpoint access is intentional
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-01.
- **Execute:** Call documented registration endpoint with valid and invalid method/content-type/auth headers.
- **Expected:** Only intended public registration contract works; malformed requests get controlled 4xx.
- **PASS evidence required:** OpenAPI + request/response matrix.
- **Automated proof required:** Endpoint integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-002 — All required registration fields are validated server-side
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-03.
- **Execute:** Omit each required field in direct API requests.
- **Expected:** Each is rejected without persisted user or 500.
- **PASS evidence required:** Responses and DB assertions.
- **Automated proof required:** Parameterized endpoint/integration tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-003 — Blank/whitespace values are rejected or canonicalized correctly
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-03.
- **Execute:** Submit blank/whitespace values in all textual fields.
- **Expected:** No unusable account persists; behavior consistent with DTO/domain rules.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Validation tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-004 — Email format validation is server-enforced
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-01.
- **Execute:** Bypass frontend with malformed email variants.
- **Expected:** Controlled validation response; no user persisted.
- **PASS evidence required:** Response/DB evidence.
- **Automated proof required:** Parameterized tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-005 — Email canonicalization and uniqueness are deterministic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-03.
- **Execute:** Register canonical address, then case/whitespace variants sequentially and concurrently.
- **Expected:** Only one identity persists; controlled conflict—not 500.
- **PASS evidence required:** DB query and response matrix.
- **Automated proof required:** Repository + integration concurrency test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-006 — Duplicate registration does not reveal account existence
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-01.
- **Execute:** Compare duplicate-register response with other invalid registration outcomes.
- **Expected:** No unnecessary account-specific detail; UX-safe error schema.
- **PASS evidence required:** Responses.
- **Automated proof required:** Exception/endpoint test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-007 — Password never persists in clear text
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-02; TG-02.
- **Execute:** Register synthetic user; inspect DB, response DTOs, logs, exceptions, audit records.
- **Expected:** No raw password in any persistent/returned/logged data.
- **PASS evidence required:** Redacted DB/log scan.
- **Automated proof required:** Integration test asserts raw password absent and hash differs.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-008 — BCrypt hashing and salting are actually used
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; TG-02.
- **Execute:** Inspect encoder configuration and register same password twice under different fixtures.
- **Expected:** BCrypt format; hashes differ; verification works only with original password.
- **PASS evidence required:** Code/config/DB evidence.
- **Automated proof required:** Unit/integration BCrypt tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-009 — Password policy is enforced by backend
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Submit weak/boundary/overlong passwords directly.
- **Expected:** Only policy-compliant passwords accepted; errors never echo value.
- **PASS evidence required:** Responses.
- **Automated proof required:** Parameterized validation tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-010 — Registration success/error DTOs are privacy-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; TG-04.
- **Execute:** Inspect response bodies/headers for success/validation/conflict/unexpected paths.
- **Expected:** No password/hash/internal security field/excess PII is exposed.
- **PASS evidence required:** Response inventory.
- **Automated proof required:** Serialization/contract tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-011 — Registration creates only intended initial state
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-03.
- **Execute:** Register fresh user and inspect related rows/entities.
- **Expected:** No unintended household/invitation/privileged membership or leaked default state.
- **PASS evidence required:** DB evidence.
- **Automated proof required:** Service/integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-012 — Registration transaction is atomic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Force failure after validation but before complete persistence.
- **Expected:** No partial account/related rows remain.
- **PASS evidence required:** DB assertions.
- **Automated proof required:** Transactional service/integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-013 — Registration exceptions are sanitized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03.
- **Execute:** Trigger constraint/mapper/unexpected failure cases.
- **Expected:** Controlled error status/schema; no SQL/stack trace/class name.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception-handler tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-014 — Registration endpoint matches OpenAPI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-04.
- **Execute:** Compare method, path, security, DTOs, validation, success and error statuses.
- **Expected:** Documentation and live behavior agree.
- **PASS evidence required:** OpenAPI diff evidence.
- **Automated proof required:** Contract/integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-015 — Registration has unit, repository and integration coverage where appropriate
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-09; QA strategy.
- **Execute:** Locate/run focused tests for validation, uniqueness, hashing, persistence and endpoint behavior.
- **Expected:** Meaningful coverage at correct layers; not status-only tests.
- **PASS evidence required:** Test paths and command output.
- **Automated proof required:** Mandatory test evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

## B. FR-01 — Login, JWT and backend authentication

### BE-016 — Login endpoint accepts only documented request shape
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; TG-04.
- **Execute:** Submit valid, missing, malformed, extra and wrong-content-type requests.
- **Expected:** Valid login works; invalid requests controlled; no 500.
- **PASS evidence required:** Responses/OpenAPI.
- **Automated proof required:** Endpoint integration tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-017 — Valid login verifies BCrypt hash correctly
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; TG-02.
- **Execute:** Register fixture then login with exact credential.
- **Expected:** Only matching password authenticates.
- **PASS evidence required:** Response/service trace.
- **Automated proof required:** Integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-018 — Unknown user and wrong password responses are indistinguishable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Compare body/status/header behavior for both failures.
- **Expected:** No account enumeration oracle.
- **PASS evidence required:** Response comparison.
- **Automated proof required:** Security regression test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-019 — JWT is issued only after valid authentication
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-02; NFR-01.
- **Execute:** Inspect every login failure response and success response.
- **Expected:** No token/session on failure; valid success follows documented contract.
- **PASS evidence required:** Response capture.
- **Automated proof required:** Endpoint tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-020 — JWT has deterministic expiry and rejects expired tokens
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-02; NFR-01.
- **Execute:** Inspect claims/config; use injected clock/short-lived fixture on protected endpoints.
- **Expected:** Expiry exists and expired JWT yields safe 401/no data.
- **PASS evidence required:** Redacted claim/config + response.
- **Automated proof required:** Security integration test with controlled clock.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-021 — Malformed/tampered/wrong-signature JWT is rejected
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Call protected UC-01 routes with malformed, modified, wrong-signature and forbidden-algorithm tokens.
- **Expected:** Safe 401; no fallback auth/500.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Parameterized security tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-022 — JWT claims are minimized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; TG-02.
- **Execute:** Decode synthetic token and inspect claims.
- **Expected:** No password/hash/raw invitation token/excess profile data; only necessary claims.
- **PASS evidence required:** Redacted claim record.
- **Automated proof required:** JWT unit/integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-023 — Server derives actor/role from authenticated principal
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; TG-01.
- **Execute:** Tamper userId, role, householdId in body/query/header on protected actions.
- **Expected:** Server ignores/rejects client authority claims; state comes from security principal/database.
- **PASS evidence required:** Request/DB evidence.
- **Automated proof required:** Authorization integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-024 — All UC-01 protected endpoints return 401 when unauthenticated
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Build endpoint matrix and call each with no/invalid auth.
- **Expected:** No data/mutation; controlled unauthorized schema.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** Parameterized security test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-025 — Wrong-role requests return controlled denial
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Call Main-Tenant-only endpoints using Roommate token.
- **Expected:** No mutation/leak; 403 or documented business response.
- **PASS evidence required:** Responses/DB unchanged.
- **Automated proof required:** Role-based integration tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-026 — Auth filter/security layer never leaks exceptions
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Trigger token parsing/auth failures before controllers.
- **Expected:** Safe JSON error; no stack trace/500.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Filter/security tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-027 — Auth secrets are not committed or logged
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-02.
- **Execute:** Scan repo/config/CI/logs for JWT secret, raw tokens, passwords, production DB secrets.
- **Expected:** No real secret tracked/logged; examples use placeholders.
- **PASS evidence required:** Secret scan evidence.
- **Automated proof required:** CI/static scan.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-028 — Authentication tests run in CI and are not skipped
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-05; TG-09.
- **Execute:** Inspect CI config, profiles, exclusions and latest job output.
- **Expected:** Critical auth tests run and failures fail pipeline.
- **PASS evidence required:** Pipeline/config evidence.
- **Automated proof required:** CI verification.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

## C. FR-02 — WG creation and initial Main Tenant membership

### BE-029 — Create-WG endpoint requires authenticated user
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Call without JWT, malformed JWT, and valid user JWT.
- **Expected:** Only authenticated valid user can create WG.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Security integration tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-030 — Create-WG request validation is server-enforced
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-03.
- **Execute:** Submit missing/blank/boundary/malformed/unknown fields direct to API.
- **Expected:** Invalid request produces controlled 4xx and no persisted WG.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** DTO/endpoint tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-031 — Creator is persisted as exactly one active MAIN_TENANT
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02.
- **Execute:** Create WG and inspect Household/Roommate/membership rows.
- **Expected:** One active membership, correct household and MAIN_TENANT role.
- **PASS evidence required:** DB assertions.
- **Automated proof required:** Service/repository/integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-032 — Client cannot forge role or creator identity
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Inject role/owner/creator/household fields into request.
- **Expected:** Server ignores/rejects forged data; persisted role remains correct.
- **PASS evidence required:** Request/DB evidence.
- **Automated proof required:** Mass-assignment/authorization test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-033 — Create-WG operation is atomic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Force membership/audit persistence failure after household creation begins.
- **Expected:** No orphan household/no partial membership survives.
- **PASS evidence required:** DB assertions.
- **Automated proof required:** Transactional test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-034 — Duplicate create requests are safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Send repeated/concurrent identical create calls as same user.
- **Expected:** Behavior is intentional; no accidental duplicate WG or raw error.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Concurrency/idempotency test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-035 — WG reads/writes are scoped to current membership
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Create WG A/B; use actor A against B resource/IDs.
- **Expected:** No cross-WG data/mutation/existence leak beyond documented response.
- **PASS evidence required:** Endpoint/DB matrix.
- **Automated proof required:** IDOR/cross-WG integration suite.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-036 — All sensitive member-management paths are enumerated
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Inventory controllers/services/routes for invite, remove member, important WG setting changes.
- **Expected:** Every sensitive path has backend Main-Tenant/household enforcement.
- **PASS evidence required:** Endpoint inventory.
- **Automated proof required:** Authorization test matrix.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-037 — Main Tenant leaving/deletion is explicitly safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; NFR-03.
- **Execute:** Attempt lifecycle actions as Main Tenant with/without other members.
- **Expected:** System blocks with rule or performs documented handover/closure; no orphaned WG.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Integration policy test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-038 — WG creation creates auditable trace
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-06.
- **Execute:** Create WG, inspect timestamp/actor/audit record or documented equivalent.
- **Expected:** Action trace contains actor/time without secrets.
- **PASS evidence required:** Audit/DB evidence.
- **Automated proof required:** Integration audit test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-039 — Create-WG endpoint matches OpenAPI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-04.
- **Execute:** Compare live endpoint to spec.
- **Expected:** Path/security/DTO/statuses current.
- **PASS evidence required:** Spec/API proof.
- **Automated proof required:** Contract test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-040 — Layer separation is preserved in create-WG flow
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-01; NFR-05.
- **Execute:** Trace controller → service → repository/entity mapping.
- **Expected:** No DB access in controller; business role rule in service/domain; repository owns persistence.
- **PASS evidence required:** File/class map.
- **Automated proof required:** Architecture review + unit tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-041 — Database constraints are deliberate and user-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Review household/membership constraints and exception translation.
- **Expected:** Constraints prevent corruption but do not surface as raw 500/SQL.
- **PASS evidence required:** Schema/code/response evidence.
- **Automated proof required:** Repository/integration tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

## D. FR-02 — Invitation creation, secure tokens, hashes and expiry

### BE-042 — Invite generation requires current Main Tenant
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Call as logged-out, invalid token, Roommate, Main Tenant, stale-role fixture.
- **Expected:** Only current Main Tenant of own WG can create invite.
- **PASS evidence required:** Response/DB matrix.
- **Automated proof required:** Role/authorization integration tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-043 — Invite generation blocks cross-WG ID tampering
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Use Main Tenant A with WG B identifier/path/body/query.
- **Expected:** No B invite created; safe denial.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Cross-WG IDOR test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-044 — Configurable expiry is fully validated
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-03.
- **Execute:** Submit missing, past, zero, min/max, too-long, malformed, timezone-boundary values.
- **Expected:** Only allowed finite expiry values accepted; canonical server timestamp stored.
- **PASS evidence required:** Responses/DB timestamps.
- **Automated proof required:** Parameterized DTO/service tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-045 — Default expiry is finite and documented
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-02.
- **Execute:** Create without explicit expiry if supported; inspect config and stored value.
- **Expected:** Finite documented default applies; never indefinite by accident.
- **PASS evidence required:** Config/DB evidence.
- **Automated proof required:** Service test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-046 — Raw invitation tokens use secure randomness
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Inspect generator and generate many synthetic tokens in test environment.
- **Expected:** CSPRNG/nonsequential/nonreused tokens; no timestamp-only predictability.
- **PASS evidence required:** Code/test sample (redacted).
- **Automated proof required:** Unit test plus source review.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-047 — Raw invitation token is never persisted
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-02.
- **Execute:** Generate invite then inspect DB, entity, ORM/log/audit/error outputs.
- **Expected:** Only one-way hash + metadata stored; raw token not persisted.
- **PASS evidence required:** Redacted DB/log evidence.
- **Automated proof required:** Repository/integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-048 — Stored hash verifies valid raw token correctly
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02.
- **Execute:** Generate then accept valid token through service/API.
- **Expected:** Hash verification succeeds without reversible storage.
- **PASS evidence required:** Test evidence.
- **Automated proof required:** Service unit test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-049 — Token hashes differ across invitations
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Generate several invites and inspect stored hashes.
- **Expected:** No token/hash reuse/collision in fixture set.
- **PASS evidence required:** DB evidence.
- **Automated proof required:** Service/repository test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-050 — Raw token is absent from list/read DTOs
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02.
- **Execute:** Call all invitation list/detail endpoints before/after creation.
- **Expected:** Raw token appears only in necessary generation response path, never later list/history responses.
- **PASS evidence required:** Response inventory.
- **Automated proof required:** Serialization/contract test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-051 — Raw token never enters logs/audit/telemetry
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02.
- **Execute:** Generate/accept/tamper invites while capturing app logs.
- **Expected:** No raw token in any log/audit/exception/debug output.
- **PASS evidence required:** Redacted grep evidence.
- **Automated proof required:** Log-capture test/static scan.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-052 — Invitation metadata persists correctly
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-03.
- **Execute:** Inspect inviter, household, createdAt, expiresAt, used state, hash and status.
- **Expected:** All map to correct Main Tenant/WG and survive transaction.
- **PASS evidence required:** DB assertions.
- **Automated proof required:** Repository/integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-053 — Server time—not client time—decides expiry
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Manipulate client date/header/payload; test server clock boundary.
- **Expected:** Client cannot extend validity; server treats boundaries deterministically.
- **PASS evidence required:** Responses/config evidence.
- **Automated proof required:** Clock-controlled test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-054 — Invitation creation is atomic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Force persistence/audit failure during creation.
- **Expected:** No usable link returned if stored state fails; no partial invitation row.
- **PASS evidence required:** Response/DB evidence.
- **Automated proof required:** Transactional test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-055 — Repeated invitation creation is intentional and safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Create several deliberate invites and rapid duplicate requests.
- **Expected:** Each intended invite distinct; accidental duplicate result documented/no corruption.
- **PASS evidence required:** DB/response evidence.
- **Automated proof required:** Service/integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-056 — Invite creation errors are sanitized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03.
- **Execute:** Trigger validation/authorization/constraint/unexpected errors.
- **Expected:** Controlled schema/status; no SQL/raw token/stack trace.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-057 — Invitation creation matches OpenAPI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-04.
- **Execute:** Compare spec to behavior, including errors.
- **Expected:** Spec current and executable.
- **PASS evidence required:** OpenAPI/API evidence.
- **Automated proof required:** Contract test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

## E. FR-02 — Invitation acceptance, existing users, single-use, rejoin

### BE-058 — Invite acceptance requires supported authenticated identity
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02.
- **Execute:** Call acceptance unauthenticated and authenticated according to documented sequence.
- **Expected:** No anonymous membership; supported flow is controlled and clear.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-059 — Valid unused unexpired invite accepts exactly once
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02.
- **Execute:** Generate then accept valid invite; inspect membership and invite used flag.
- **Expected:** One membership; invite consumed atomically.
- **PASS evidence required:** DB/response evidence.
- **Automated proof required:** Integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-060 — Used invite replay is rejected
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-03.
- **Execute:** Accept same raw token twice with same/different user.
- **Expected:** Second attempt safe; no duplicate member/role change/500.
- **PASS evidence required:** Responses/DB.
- **Automated proof required:** Mandatory replay regression test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-061 — Invalid and malformed tokens are rejected safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Test empty/random/malformed/too-long tokens.
- **Expected:** Controlled 4xx; no data/mutation leak.
- **PASS evidence required:** Responses/DB.
- **Automated proof required:** Parameterized security test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-062 — Tampered valid token is rejected safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Change one character in valid token.
- **Expected:** Safe denial; no household disclosure.
- **PASS evidence required:** Response evidence.
- **Automated proof required:** Integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-063 — Expired invite is rejected at exact boundary
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-02.
- **Execute:** Accept before, at, after expiry with controlled server clock.
- **Expected:** Only pre-expiry unused token succeeds; boundary behavior deterministic.
- **PASS evidence required:** Responses/DB.
- **Automated proof required:** Clock-controlled test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-064 — Existing registered user acceptance works — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #35; FR-02; NFR-03.
- **Execute:** Create account first, login, create invite from target WG, accept.
- **Expected:** No duplicate ApplicationUser; one target membership; no 500.
- **PASS evidence required:** DB counts/responses.
- **Automated proof required:** Mandatory named integration regression for BUG #35.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-065 — New-user register/login then accept sequence works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02.
- **Execute:** Run supported registration/login/accept process at API level.
- **Expected:** Invite context resolves safely; exactly one membership.
- **PASS evidence required:** Requests/DB.
- **Automated proof required:** Integration workflow test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-066 — Recipient role is always server-assigned ROOMMATE
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Inject role=MAIN_TENANT or altered fields during accept.
- **Expected:** Stored role is ROOMMATE regardless of client input.
- **PASS evidence required:** DB assertion.
- **Automated proof required:** Authorization/service test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-067 — Invite determines target WG; client cannot substitute another
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Use token for WG A while claiming B IDs in body/path.
- **Expected:** Only invitation’s WG can be joined; no cross-WG mutation.
- **PASS evidence required:** Responses/DB.
- **Automated proof required:** IDOR integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-068 — Already-active member acceptance is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Accept a valid/new invite as an already active member.
- **Expected:** Controlled idempotent/rejection result; no duplicate active membership/500.
- **PASS evidence required:** DB/response evidence.
- **Automated proof required:** Integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-069 — Leave then rejoin same WG works — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #38; FR-02; NFR-03.
- **Execute:** Join, leave, issue fresh invite, accept same account; use actual DB constraints.
- **Expected:** Safe reactivation/recreation; exactly one active membership; never unique-constraint 500.
- **PASS evidence required:** DB/log/response evidence.
- **Automated proof required:** Mandatory repository + integration regression for BUG #38.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-070 — Schema/service strategy supports former-member rejoin
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Review unique constraints, active/inactive flags, lookup queries and transaction logic.
- **Expected:** No schema/service mismatch can recreate #38.
- **PASS evidence required:** Schema/code evidence.
- **Automated proof required:** Repository test reproducing original failure sequence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-071 — Concurrent use of one invite is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-03.
- **Execute:** Send two concurrent accepts with same token/different users.
- **Expected:** Exactly one succeeds; one membership; loser gets safe controlled result.
- **PASS evidence required:** Concurrency output/DB assertions.
- **Automated proof required:** Concurrent integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-072 — Concurrent duplicate accept by one user is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Send concurrent identical accept requests.
- **Expected:** At most one active membership; safe responses.
- **PASS evidence required:** DB assertions.
- **Automated proof required:** Concurrency test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-073 — Accept failure rolls back all coupled state
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Force failure after candidate membership/invite state change.
- **Expected:** No partial membership/invite mismatch/audit inconsistency.
- **PASS evidence required:** DB assertions.
- **Automated proof required:** Transactional test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-074 — Revoked/cancelled invite rejects if model supports it
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-03.
- **Execute:** Use revoked/cancelled fixture or record N/A with model proof.
- **Expected:** No unauthorized membership from invalidated invite.
- **PASS evidence required:** Response/DB proof.
- **Automated proof required:** Test or justified N/A.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-075 — Acceptance responses are privacy-minimized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02.
- **Execute:** Inspect success/error DTOs for household/member/inviter data.
- **Expected:** Only needed onboarding data; invalid token reveals no household existence.
- **PASS evidence required:** Response inventory.
- **Automated proof required:** Contract/serialization test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-076 — Acceptance endpoint cannot enumerate valid tokens
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Compare invalid/expired/used errors and reasonable timing.
- **Expected:** No meaningful oracle distinguishes token validity/household identity.
- **PASS evidence required:** Response comparison.
- **Automated proof required:** Security regression test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-077 — Current account eligibility is revalidated on acceptance
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03.
- **Execute:** Use disabled/deleted/anonymized/stale role fixture where model supports it.
- **Expected:** Server applies documented rule; no invalid membership.
- **PASS evidence required:** Responses/DB.
- **Automated proof required:** Integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-078 — Accept API contract is current
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-04.
- **Execute:** Compare OpenAPI parameters/security/status/error schemas to live endpoint.
- **Expected:** No mismatch.
- **PASS evidence required:** Spec/API evidence.
- **Automated proof required:** Contract test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-079 — Acceptance creates audit trace without raw token
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-06; NFR-02.
- **Execute:** Accept invitation; inspect audit/timestamps/actor.
- **Expected:** Action trace exists; raw token absent.
- **PASS evidence required:** Audit evidence.
- **Automated proof required:** Integration audit test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-080 — Used/expired state survives restart
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Consume/expire invite, restart via documented method, retry accept.
- **Expected:** State remains enforced after restart.
- **PASS evidence required:** Commands/DB/response.
- **Automated proof required:** Restart/integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-081 — Known conflicts never surface as raw 500
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Force already-member, former-member, invite race, unique/FK conflict paths.
- **Expected:** Business-safe 4xx/controlled response; no SQL/stack trace.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception translation regression tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

## F. NFR-02 / TG-07 — Membership lifecycle, leave, deletion/anonymization

### BE-082 — Leave endpoint requires current membership
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-02.
- **Execute:** Call leave unauthenticated, unrelated actor, active member, former member.
- **Expected:** Only valid actor changes own membership; no cross-WG change.
- **PASS evidence required:** Responses/DB.
- **Automated proof required:** Authorization integration tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-083 — Leave updates membership state safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; NFR-03.
- **Execute:** Leave and inspect membership row, active flag/end timestamp/access/history links.
- **Expected:** Documented state transition occurs without corruption.
- **PASS evidence required:** DB evidence.
- **Automated proof required:** Service/repository/integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-084 — Leave is idempotent/concurrency-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Call leave twice/concurrently.
- **Expected:** No 500, duplicate audit or inconsistent row.
- **PASS evidence required:** Responses/DB.
- **Automated proof required:** Concurrency/idempotency test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-085 — Former member loses access immediately
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Leave then call all former-WG UC-01 endpoints with same session/token.
- **Expected:** No stale authorization/data access.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** Security integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-086 — Rejoin behavior is safe and auditable — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #38; NFR-03; NFR-06.
- **Execute:** Execute full leave/reinvite/rejoin sequence and inspect audit.
- **Expected:** No unique error; exact one active membership; leave/rejoin trace present.
- **PASS evidence required:** DB/audit evidence.
- **Automated proof required:** Mandatory regression test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-087 — Deletion/anonymization is implemented or truthfully documented
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; TG-07.
- **Execute:** Find real endpoint/service/path; if indirect process, validate it as delivered.
- **Expected:** No fake support claim; actual delivered path meets contract scope.
- **PASS evidence required:** Code/API/docs evidence.
- **Automated proof required:** Integration/manual acceptance test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-088 — Deletion/anonymization preserves financial references — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #42; NFR-02; NFR-03.
- **Execute:** Use user with expenses/payments; delete/anonymize; inspect FK/history and linked settlement service.
- **Expected:** No FK breakage; history has privacy-safe stable reference; later settlement remains computable.
- **PASS evidence required:** DB before/after + shared UC-03 evidence.
- **Automated proof required:** Mandatory integration regression linked to BUG #42.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-089 — Deleted/anonymized user cannot retain active access
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-02.
- **Execute:** Delete/anonymize then try login, old JWT and protected routes.
- **Expected:** Behavior matches policy; no unauthorized active access.
- **PASS evidence required:** Responses/DB.
- **Automated proof required:** Security integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-090 — Deletion/anonymization avoids PII leakage in history/logs
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02.
- **Execute:** Inspect DTOs/history/audit/logs after lifecycle action.
- **Expected:** Only documented anonymized representation remains; no raw PII leaked.
- **PASS evidence required:** Redacted evidence.
- **Automated proof required:** Integration/log-scan test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-091 — Main Tenant lifecycle policy avoids orphan state
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; NFR-03.
- **Execute:** Delete/leave as Main Tenant with various household member counts.
- **Expected:** Safe block/handover/closure; no orphaned privileged state.
- **PASS evidence required:** Responses/DB.
- **Automated proof required:** Integration policy test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-092 — Member removal is Main-Tenant-only and household-scoped
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Attempt as Roommate/unrelated/stale Main Tenant/current Main Tenant; target cross-WG user.
- **Expected:** Only authorized removal of own WG member succeeds.
- **PASS evidence required:** Response/DB matrix.
- **Automated proof required:** Role/cross-WG integration tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-093 — Removing member with financial history is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; NFR-03; BUG #42.
- **Execute:** Remove historic debtor/creditor fixture, inspect history/settlement references.
- **Expected:** No broken record or later-unsettleable state.
- **PASS evidence required:** DB evidence + UC-03 linkage.
- **Automated proof required:** Regression test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-094 — Lifecycle actions create timestamp/actor audit evidence
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-06.
- **Execute:** Leave, rejoin, remove, delete/anonymize fixtures.
- **Expected:** Every action traceable by time/actor under privacy policy.
- **PASS evidence required:** Audit samples.
- **Automated proof required:** Integration audit tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-095 — User-data export/manual export path is real if claimed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02.
- **Execute:** Follow documented export/manual database export using synthetic user.
- **Expected:** Working path or explicitly feasibility-limited documentation; no false claim.
- **PASS evidence required:** Docs/output.
- **Automated proof required:** Manual QA record.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-096 — Lifecycle exception mapping is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; NFR-01.
- **Execute:** Force invalid transition, constraint, missing entity, unauthorized errors.
- **Expected:** Rollback + controlled safe error; no 500 internals.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-097 — Privacy/retention behavior is documented consistently
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; NFR-05.
- **Execute:** Compare docs, API, implementation and UI copy.
- **Expected:** One consistent lifecycle story; no contradiction.
- **PASS evidence required:** Doc/code evidence.
- **Automated proof required:** Documentation review.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

## G. All applicable NFRs — security, privacy, reliability, maintainability, auditability

### BE-098 — Complete UC-01 endpoint authorization matrix exists
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Enumerate every UC-01 controller route and classify public/auth/self/Main-Tenant-only.
- **Expected:** No route unclassified; tests exist for each class.
- **PASS evidence required:** Matrix in evidence record.
- **Automated proof required:** Parameterized integration suite.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-099 — Every UC-01 read endpoint blocks cross-WG access
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Use WG A actor against WG B IDs/resources for all reads.
- **Expected:** No B data returned.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** Cross-WG security suite.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-100 — Every UC-01 write endpoint blocks cross-WG access
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Use WG A actor against WG B resources for all mutations.
- **Expected:** No B mutation; DB unchanged.
- **PASS evidence required:** Endpoint/DB matrix.
- **Automated proof required:** Cross-WG security suite.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-101 — Mass assignment is blocked across all UC-01 DTOs
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Inject id, role, household, used, expiry, creator, audit fields into requests.
- **Expected:** Forbidden fields cannot alter authority/state.
- **PASS evidence required:** Requests/DB evidence.
- **Automated proof required:** Mass-assignment tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-102 — Input validation covers body/path/query values
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03.
- **Execute:** Fuzz/boundary test identifiers, tokens, dates, lengths and strings.
- **Expected:** No parser exception/500; controlled 4xx.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Parameterized tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-103 — Database errors never reach client directly
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03.
- **Execute:** Trigger uniqueness/FK/constraint errors.
- **Expected:** Safe error schema; no SQL/entity/class details.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception-handler tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-104 — Logs and audit trail are privacy-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; NFR-06.
- **Execute:** Run all UC-01 flows with log capture; inspect audit entries.
- **Expected:** Needed actor/action/time only; no password/JWT/raw invite/full private payload.
- **PASS evidence required:** Redacted logs/audit.
- **Automated proof required:** Log test/static scan.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-105 — Server-side time is testable and deterministic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Inspect use of injected clock/equivalent; rerun expiry boundaries repeatedly.
- **Expected:** No sleep/flaky timing; deterministic test outcomes.
- **PASS evidence required:** Code/tests/output.
- **Automated proof required:** Unit/integration tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-106 — Critical multi-row operations are transactionally bounded
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Review create WG, create invite, accept, leave/rejoin, delete/anonymize paths.
- **Expected:** All coupled state changes are atomic or proven equivalent.
- **PASS evidence required:** Code annotations/DB proof.
- **Automated proof required:** Rollback tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-107 — Concurrency cases are tested and deterministic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Run duplicate registration, invite issue/accept, leave/rejoin concurrently.
- **Expected:** Valid final state and controlled outcomes; no race defect.
- **PASS evidence required:** Test output/DB proof.
- **Automated proof required:** Concurrent integration tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-108 — Database initialization and seed restore are reproducible
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; TG-05.
- **Execute:** Follow clean-checkout setup/reset instructions; verify synthetic UC-01 state.
- **Expected:** No hidden manual repair; state matches docs.
- **PASS evidence required:** Commands/output.
- **Automated proof required:** Setup/seed smoke test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-109 — Persistence/restart behavior is documented and tested
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Create user/WG/invite, restart per docs, inspect state.
- **Expected:** State survives/restores exactly as claimed.
- **PASS evidence required:** Commands/API/DB evidence.
- **Automated proof required:** Restart integration test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-110 — Repository tests cover critical invitation/membership queries
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; QA strategy.
- **Execute:** Locate/run repository tests for active membership, former member, hash lookup, scope and uniqueness.
- **Expected:** Real persistence semantics covered.
- **PASS evidence required:** Test paths/output.
- **Automated proof required:** Mandatory repository evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-111 — Service tests cover core rules independently
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-05; TG-09.
- **Execute:** Locate/run focused tests for role, expiry, hash, accept, rejoin, lifecycle.
- **Expected:** Rules tested without relying only on UI/E2E.
- **PASS evidence required:** Test paths/output.
- **Automated proof required:** Mandatory service tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-112 — API integration tests cover security + real persistence
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-09.
- **Execute:** Run endpoint tests using real security/database configuration.
- **Expected:** Full auth/invite/member flow proven at API level.
- **PASS evidence required:** Output.
- **Automated proof required:** Mandatory integration suite.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-113 — OpenAPI is served and all relevant contracts are current
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-04.
- **Execute:** Run app and retrieve Swagger/OpenAPI; compare security and errors.
- **Expected:** Spec available/current; no broken docs.
- **PASS evidence required:** URL/output.
- **Automated proof required:** OpenAPI smoke/contract tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-114 — CI runs all critical backend layers and blocks failures
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-05; TG-09.
- **Execute:** Review pipeline jobs, test filters, allow_failure/exclusions; inspect run.
- **Expected:** Unit/repository/integration/security tests execute and fail pipeline on failure.
- **PASS evidence required:** CI YAML/job logs.
- **Automated proof required:** CI verification.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-115 — Architecture boundaries remain clean
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-01; NFR-05.
- **Execute:** Trace all UC-01 flows from controller to service/repository/mapper.
- **Expected:** No business rules in UI/controller, no DB access outside repos, no duplicate source of truth.
- **PASS evidence required:** Architecture map.
- **Automated proof required:** Review + tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-116 — DTO mapping excludes internal security fields
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; TG-04.
- **Execute:** Inspect mapper/serialization for user/invite/membership/error DTOs.
- **Expected:** No passwordHash/tokenHash/internal flags exposed unless explicitly justified.
- **PASS evidence required:** Code/response evidence.
- **Automated proof required:** Serialization tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-117 — User/setup/API docs are current and runnable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-10; NFR-05.
- **Execute:** Follow docs from clean checkout for local setup and UC-01 flow.
- **Expected:** No undocumented prerequisite/obsolete command/false feature claim.
- **PASS evidence required:** Documentation execution record.
- **Automated proof required:** Clean-environment smoke.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-118 — Dependency/security configuration is reviewed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-05.
- **Execute:** Run supported dependency/vulnerability scan or inspect lock/build files; record critical findings.
- **Expected:** Critical known issue fixed or explicitly tracked; no unsupported security claim.
- **PASS evidence required:** Report/issues.
- **Automated proof required:** CI scan/manual review.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-119 — Test names and regression names are maintainable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-05.
- **Execute:** Review UC-01 test names and BUG #35/#38/#42 coverage.
- **Expected:** Tests state scenario/outcome; future maintainer can identify purpose.
- **PASS evidence required:** Test inventory.
- **Automated proof required:** Code review.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-120 — Final backend sign-off is evidence-complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** All applicable FR/NFR/TGs.
- **Execute:** Review every BE item, automated proof, and bug regression.
- **Expected:** No critical PASS lacks evidence; unresolved issues visible.
- **PASS evidence required:** Signed evidence review.
- **Automated proof required:** Reviewer sign-off.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`