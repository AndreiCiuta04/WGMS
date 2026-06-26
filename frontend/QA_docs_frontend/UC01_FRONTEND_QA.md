# UC-01 — FRONTEND QA FORM

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

    **Layer:** Angular/UI layer: browser behavior, routes, forms, client state, accessibility, responsive design, user-facing privacy, and frontend tests.

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

    This file is the primary owner for all visible UI requirements of FR-01/FR-02 and for the frontend-facing proof of NFR-01 through NFR-06. Backend enforcement is proven in the backend form; the full real-user flow is proven in the E2E form.

    | Requirement group | Covered by these sections |
|---|---|
| FR-01 secure registration/login | A, B |
| FR-02 WG creation, Main Tenant/Roommate UX, invitations | C, D |
| NFR-01 client auth/session/role visibility/no leakage | B, D, F |
| NFR-02 privacy/minimization/rights/no secret logging | A, E, F |
| NFR-03 retries, duplicate submit, stale state, recovery | A–F |
| NFR-04 all buttons, accessibility, peer/device/browser tests | A–F |
| NFR-05 frontend quality, tests, docs | F |
| NFR-06 visible lifecycle/audit linkage | D, E |

    ## Completion standard

    A checkbox is PASS only where the live behavior, persistence/API behavior where relevant, and required automated
    evidence agree. “Code looks correct”, “endpoint exists”, “button is visible”, “works manually once”, or “pipeline
    was green before” is insufficient.

## A. FR-01 — Registration

### FE-001 — Public registration route is reachable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-04.
- **Execute:** In a clean browser profile, use every public CTA and direct route that should open registration.
- **Expected:** Registration renders without a JWT, no protected household content flashes, and route errors are controlled.
- **PASS evidence required:** Screenshots/video, route list, browser console capture.
- **Automated proof required:** Frontend route/component test plus Cypress public-route smoke.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-002 — Registration form matches the live API contract
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; TG-04.
- **Execute:** Compare visible fields, labels, required markers, types, submitted request keys, and content type against current OpenAPI and backend DTO.
- **Expected:** Every server-required field is collectable; no missing/outdated frontend field blocks valid registration.
- **PASS evidence required:** OpenAPI reference, redacted network request, screenshot.
- **Automated proof required:** Component/service test asserts exact request DTO.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-003 — Every registration control has a label and usable help
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-04/WAI.
- **Execute:** Inspect visible labels, placeholders, required markers, accessible names, help text, and error descriptions for every control.
- **Expected:** Each control can be understood without relying on placeholder text alone; labels and help connect programmatically to inputs.
- **PASS evidence required:** Accessibility tree / axe output and screenshots.
- **Automated proof required:** Accessibility/component test for labels and descriptions.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-004 — Required-field validation is complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-04.
- **Execute:** Submit with each required field empty, then whitespace-only where applicable.
- **Expected:** Each missing/blank field receives a precise visible and accessible error; no request creates an account.
- **PASS evidence required:** Screenshots and network capture.
- **Automated proof required:** Parameterized component/form validation tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-005 — Email format and normalization UX are correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-03.
- **Execute:** Try malformed addresses, leading/trailing spaces, mixed case, valid plus-addresses, and maximum-length boundaries supported by backend.
- **Expected:** Invalid values are blocked or server errors are mapped cleanly; canonical values are not corrupted; no false acceptance occurs.
- **PASS evidence required:** Screenshots, request/response evidence.
- **Automated proof required:** Parameterized form tests for valid/invalid/boundary inputs.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-006 — Password policy is accurately communicated
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-01.
- **Execute:** Compare UI password rules with actual backend policy; try every policy boundary through UI.
- **Expected:** UI wording matches server enforcement and gives actionable feedback; no weak password succeeds because only backend rejects it later.
- **PASS evidence required:** Screenshots and API evidence.
- **Automated proof required:** Frontend policy/boundary tests; linked backend proof.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-007 — Password confirmation behavior is correct when present
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-04.
- **Execute:** If a confirmation field exists, submit mismatch, correct it, then resubmit. If none exists, record N/A with proof that API does not require it.
- **Expected:** Mismatch blocks registration with clear error; correcting only this field clears the relevant error; no stale error remains.
- **PASS evidence required:** Screenshot/video or justified N/A.
- **Automated proof required:** Component test for mismatch behavior or justified N/A evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-008 — Passwords are masked and never echoed in the UI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-02.
- **Execute:** Inspect input type, toggle-visibility behavior if present, validation errors, summaries, toasts, DOM, console, and analytics hooks.
- **Expected:** Password text is not exposed in normal UI, errors, logs, or rendered summaries.
- **PASS evidence required:** DOM/console inspection, screenshots.
- **Automated proof required:** Component test for password input type; static scan/review evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-009 — Registration accepts terms/privacy consent only where required
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-02.
- **Execute:** If the live contract has terms/privacy consent control, test unchecked, checked, keyboard use, and server rejection. If absent, record N/A with API/schema proof.
- **Expected:** Consent behavior is clear and cannot be forged by client-only UI; privacy link works if present.
- **PASS evidence required:** Screenshot/request/response or N/A proof.
- **Automated proof required:** Component and endpoint tests where control exists.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-010 — Duplicate-email registration is handled safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-03; NFR-01.
- **Execute:** Register a synthetic email, repeat exact/case/space variants, and inspect UI recovery.
- **Expected:** No duplicate account, blank screen, raw 500, SQL error, or account-enumeration message; user can correct/retry.
- **PASS evidence required:** Video, redacted response, DB/API evidence.
- **Automated proof required:** Cypress regression + frontend error mapping test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-011 — Double submission cannot create duplicate registration
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Double-click, press Enter repeatedly, and simulate slow network during registration.
- **Expected:** One logical registration attempt occurs; UI shows pending state; no duplicate user or contradictory success/error.
- **PASS evidence required:** Video and network request count.
- **Automated proof required:** Pending-state component test plus E2E duplicate-submit test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-012 — Registration network/server failures are recoverable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; NFR-04.
- **Execute:** Simulate offline, timeout, 4xx validation, 409 conflict, and 5xx errors; then retry.
- **Expected:** Safe actionable error appears, no technical internals leak, values remain recoverable where safe, and retry works.
- **PASS evidence required:** Screenshots/video/network evidence.
- **Automated proof required:** Service/component error-state tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-013 — Successful registration reaches a valid next step
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01.
- **Execute:** Create a brand-new synthetic user and observe route, auth state, and onboarding affordances.
- **Expected:** User lands in documented next state: authenticated onboarding/WG creation or correct login path; no dead end.
- **PASS evidence required:** Video, route and state evidence.
- **Automated proof required:** Cypress registration happy path.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

## B. FR-01 — Login, client session, logout

### FE-014 — Login route and public navigation work
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-04.
- **Execute:** Open login via every public navigation path and direct route in clean and authenticated browser states.
- **Expected:** Login is reachable for logged-out users; authenticated users are routed consistently; no broken link.
- **PASS evidence required:** Screenshots/routes.
- **Automated proof required:** Route guard and Cypress smoke tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-015 — Login form matches the live API contract
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; TG-04.
- **Execute:** Compare controls and request DTO with OpenAPI/backend.
- **Expected:** Correct fields, keys, method, and response behavior are used.
- **PASS evidence required:** Redacted request and OpenAPI evidence.
- **Automated proof required:** Client service/unit test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-016 — Valid credentials establish authenticated UI state
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; TG-02.
- **Execute:** Log in with a seeded synthetic account and verify protected navigation/action availability.
- **Expected:** Correct authenticated state and role-aware UI appear only after successful login.
- **PASS evidence required:** Video/network evidence.
- **Automated proof required:** Cypress login happy path.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-017 — Unknown-user and wrong-password errors are generic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Compare UI response for valid-email/wrong-password and nonexistent-email/any-password.
- **Expected:** Messages are equivalent/safely generic; UI does not reveal account existence.
- **PASS evidence required:** Side-by-side screenshots and response capture.
- **Automated proof required:** Cypress generic-auth-error regression.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-018 — Empty/malformed login input is validated
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-04.
- **Execute:** Submit blank, whitespace-only, malformed, oversized inputs.
- **Expected:** Clear errors; no avoidable network call; no UI crash.
- **PASS evidence required:** Screenshots/network log.
- **Automated proof required:** Parameterized form tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-019 — Login cannot be double-submitted
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Double-click/press Enter repeatedly while throttling auth response.
- **Expected:** One final deterministic auth result; no duplicate toasts/navigation/race state.
- **PASS evidence required:** Video/request count.
- **Automated proof required:** Component pending-state + Cypress test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-020 — Failed login leaves no partial protected UI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Fail login from logged-out state; inspect nav, cached data, profile header, local storage.
- **Expected:** No protected household/profile state appears; route remains safe.
- **PASS evidence required:** Screenshots/storage inspection.
- **Automated proof required:** Auth-store/route-guard test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-021 — Valid session restoration works only when token/session is valid
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-02; NFR-01.
- **Execute:** Refresh after login; repeat using invalid/expired-session fixture.
- **Expected:** Valid state restores as designed; invalid/expired state clears and routes safely to login.
- **PASS evidence required:** Video/redacted storage evidence.
- **Automated proof required:** Auth service and Cypress expiry tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-022 — Logout clears usable client access
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Use every logout entry point, then Back/Refresh/direct protected route.
- **Expected:** No protected page/data remains accessible through normal client flow; repeated logout is harmless.
- **PASS evidence required:** Video, route and storage evidence.
- **Automated proof required:** Cypress logout regression.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-023 — No false email password-reset workflow is advertised
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02 scope boundary; NFR-04.
- **Execute:** Inspect login/register/help routes, buttons, links and copy.
- **Expected:** UI does not claim email reset is sent or expose a broken reset flow; any support wording is accurate.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Copy snapshot/manual inventory test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-024 — Authentication errors are accessible and non-sensitive
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-04.
- **Execute:** Trigger errors using keyboard; inspect focus, ARIA/live region and screen-reader tree.
- **Expected:** Error is perceivable and actionable without exposing security details.
- **PASS evidence required:** Accessibility audit/video.
- **Automated proof required:** Component accessibility test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

## C. FR-02 — WG creation and Main Tenant onboarding

### FE-025 — WG creation entry point is shown only in valid onboarding states
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Compare fresh authenticated user, Main Tenant, Roommate, former/no-household user, and logged-out user.
- **Expected:** UI reflects correct state and avoids misleading actions; backend remains ultimate authority.
- **PASS evidence required:** Screenshots per state.
- **Automated proof required:** Role/state component tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-026 — WG creation form matches API schema
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; TG-04.
- **Execute:** Compare visible inputs and submitted payload to current OpenAPI/DTO.
- **Expected:** All required fields are usable and correctly sent; no stale client field breaks creation.
- **PASS evidence required:** Screenshot/request/OpenAPI evidence.
- **Automated proof required:** Form/client service test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-027 — WG creation validation handles all boundaries
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-03.
- **Execute:** Test empty/whitespace/min/max/special characters/duplicates if supported.
- **Expected:** Controlled, understandable validation; no phantom household or raw server error.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Parameterized frontend validation tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-028 — Create-WG pending state prevents duplicates
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Rapidly submit under normal and throttled network; refresh/back around request.
- **Expected:** At most one household results; UI resolves to correct state.
- **PASS evidence required:** Video/request count.
- **Automated proof required:** Component pending test + Cypress duplicate-submit test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-029 — Successful WG creation visibly establishes Main Tenant context
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Create a WG then inspect household identity, role-aware actions, invitation management visibility and route.
- **Expected:** Creator sees correct household and Main-Tenant affordances; no roommate-only view.
- **PASS evidence required:** Video/screenshots.
- **Automated proof required:** Cypress create-WG journey.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-030 — WG creation errors remain recoverable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; NFR-04.
- **Execute:** Simulate validation, conflict, unauthorized, network and 5xx responses.
- **Expected:** No raw internals, no fake created WG; user can retry/correct.
- **PASS evidence required:** Screenshots/video.
- **Automated proof required:** Service/component error tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-031 — UI does not let user choose/forge MAIN_TENANT role
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Inspect controls and request in devtools; tamper client state/payload if feasible.
- **Expected:** No editable role claim is offered; UI treats server response as authority.
- **PASS evidence required:** DOM/request evidence.
- **Automated proof required:** Client service test linked to backend authorization tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-032 — Post-create refresh/back/deep-link behavior is stable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; NFR-04.
- **Execute:** Create WG then refresh, Back/Forward, open protected routes directly.
- **Expected:** No duplicate creation, stale state, or broken navigation.
- **PASS evidence required:** Video.
- **Automated proof required:** Cypress navigation/reload tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-033 — No fake email dispatch is shown after WG creation
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02 scope boundary.
- **Execute:** Inspect onboarding/invite messages immediately after creation.
- **Expected:** UI offers generated link/share-through-own-channel only; it does not claim an invitation email was sent.
- **PASS evidence required:** Screenshot.
- **Automated proof required:** Copy/manual test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

## D. FR-02 — Invitation generation and acceptance

### FE-034 — Invitation controls are visible only to Main Tenant
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Compare Main Tenant, Roommate, logged-out, and no-household states.
- **Expected:** Only Main Tenant sees active invitation-management controls; Roommate has no dead/ambiguous privileged button.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Component role-visibility tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-035 — Invitation expiry selector is accurate and usable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02.
- **Execute:** Inspect default, min/max, keyboard handling, help text and submitted request; test allowed boundaries.
- **Expected:** UI states expiry clearly and sends allowed value; default is finite and understandable.
- **PASS evidence required:** Screenshot/request/API evidence.
- **Automated proof required:** Form boundary tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-036 — Invitation creation action handles pending, failure, retry
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; NFR-04.
- **Execute:** Generate invitation under normal/slow/error/retry conditions.
- **Expected:** One invitation per intended action; safe error/retry; no misleading success.
- **PASS evidence required:** Video/request count.
- **Automated proof required:** Component pending/error tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-037 — Generated invitation link can be copied exactly
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02.
- **Execute:** Generate link; copy/paste into plain text and fresh browser.
- **Expected:** Copied link is complete and usable; no truncation/format damage.
- **PASS evidence required:** Video/redacted link evidence.
- **Automated proof required:** Cypress clipboard/link test where supported.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-038 — Clipboard failure has a truthful fallback
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04.
- **Execute:** Block Clipboard API permission or simulate failure.
- **Expected:** UI does not falsely say copied; it provides select/manual-copy fallback.
- **PASS evidence required:** Video.
- **Automated proof required:** Component/E2E clipboard-failure test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-039 — Invitation UI treats link as sensitive
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-02.
- **Execute:** Inspect copy/share text, toasts, console, history, dialogs and telemetry.
- **Expected:** Concise safe-handling guidance; raw token does not leak to console/logs/unrelated UI.
- **PASS evidence required:** Screenshots/redacted console scan.
- **Automated proof required:** Manual security review + component copy test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-040 — Invitation list/status is correct if implemented
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-03; NFR-06.
- **Execute:** Create fresh, used, expired, and revoked/cancelled invitation fixtures where supported; compare displayed state to API.
- **Expected:** Status labels reflect backend truth; no raw token/hash exposed.
- **PASS evidence required:** Screenshots/API comparison.
- **Automated proof required:** State-mapping component/service tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-041 — Roommate direct navigation/tampering does not produce invite success
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** As Roommate, deep-link to invitation route and tamper frontend state/request.
- **Expected:** UI blocks/redirects safely; no privileged success state is rendered.
- **PASS evidence required:** Video/network evidence.
- **Automated proof required:** Route guard/Cypress role-denial test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-042 — Valid invitation landing works for logged-out and existing users
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02.
- **Execute:** Open valid link in clean profile and logged-in existing-user profile.
- **Expected:** Clear supported next step; only minimal necessary household context is shown.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Cypress landing-state tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-043 — Invalid/tampered invitation link is safely handled
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Alter token, remove token, use random/malformed value.
- **Expected:** Controlled invalid-link state; no 500, stack trace, or household leak.
- **PASS evidence required:** Screenshots/network evidence.
- **Automated proof required:** Cypress invalid-token suite.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-044 — Expired invitation link is clearly handled
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-02.
- **Execute:** Open deterministic expired fixture.
- **Expected:** UI clearly says it cannot be used and provides accurate next step; no membership is created.
- **PASS evidence required:** Screenshot/API evidence.
- **Automated proof required:** Cypress expired-invite test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-045 — Used invitation link cannot be accepted again
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02.
- **Execute:** Accept once, reopen in clean and authenticated states.
- **Expected:** Second attempt is rejected; no duplicate membership or false success.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress replay regression.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-046 — New user acceptance keeps invitation context
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02.
- **Execute:** Open valid invite logged out, complete documented registration/login step, then accept.
- **Expected:** Context is retained safely; exactly one membership is created.
- **PASS evidence required:** Video.
- **Automated proof required:** Cypress new-user invitation journey.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-047 — Existing user accepts invitation — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #35; FR-02; NFR-03.
- **Execute:** Log in as registered user not in target WG; open valid invite; accept.
- **Expected:** No duplicate account, no 500, one target membership, correct route.
- **PASS evidence required:** Video, API/DB evidence.
- **Automated proof required:** Mandatory named Cypress regression test for BUG #35.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-048 — Already-active member acceptance is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** As current member, open valid/new invite for same WG and accept.
- **Expected:** Controlled idempotent/rejection response; no duplicate member/500.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress duplicate-active-member test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-049 — Former member can rejoin — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #38; FR-02; NFR-03.
- **Execute:** Join, leave via supported UI, issue fresh invite, accept as same account.
- **Expected:** Rejoin succeeds through documented behavior; no unique-constraint detail/500; exactly one active membership.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress regression for BUG #38.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-050 — Invite recipient sees ROOMMATE—not Main Tenant—state
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01.
- **Execute:** Accept invite then inspect role-specific controls and protected action attempts.
- **Expected:** Recipient gets Roommate UI; invitation/settings/member-removal actions absent or denied.
- **PASS evidence required:** Screenshots/network evidence.
- **Automated proof required:** Cypress role-after-acceptance test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-051 — Invitation acceptance cannot be double-submitted
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Rapidly accept/click/Enter under throttled response.
- **Expected:** One final membership result; no duplicate navigation/mutation.
- **PASS evidence required:** Video/request count.
- **Automated proof required:** Pending-state + Cypress test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-052 — Acceptance remains stable after refresh/back/stale page
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Accept then refresh/back; open invite before it is consumed/expired elsewhere then accept.
- **Expected:** Client reconciles server truth; no stale false success.
- **PASS evidence required:** Video.
- **Automated proof required:** Cypress stale-invite/navigation tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

## E. NFR-02 / TG-07 — Account, membership, leave and anonymization lifecycle

### FE-053 — Account/profile and membership information is viewable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02 access right.
- **Execute:** Open account/profile/membership UI using valid user.
- **Expected:** User can view own relevant account and current WG membership; no other WG data leaks.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Cypress profile/membership UI test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-054 — Correction workflow works where profile fields are editable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02 correction right.
- **Execute:** Edit each permitted field with valid/invalid/cancel/retry cases.
- **Expected:** Only allowed values update; persistence and validation clear after refresh.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Component/Cypress edit test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-055 — Leave-WG flow explains consequences and supports cancel
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; NFR-04.
- **Execute:** Open leave flow as Roommate/Main Tenant as applicable; test cancel, Escape, Back, confirm.
- **Expected:** Consequences clear; cancel performs no mutation; destructive confirm is deliberate.
- **PASS evidence required:** Video/screenshots.
- **Automated proof required:** Dialog/Cypress cancel and confirm tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-056 — Leaving removes former-WG UI access
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03.
- **Execute:** Leave then refresh, Back, direct-link former pages, inspect nav/cache.
- **Expected:** No former household data renders; UI reaches coherent next state.
- **PASS evidence required:** Video/storage evidence.
- **Automated proof required:** Cypress post-leave access test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-057 — Rejoin UX does not surface database internals — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #38; NFR-03; NFR-04.
- **Execute:** Run leave/reinvite/rejoin and inspect all messages.
- **Expected:** Success or business-rule message is clean; never unique-constraint/SQL/500 detail.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Cypress BUG #38 UI regression.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-058 — Deletion/anonymization action is discoverable and truthful
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; TG-07.
- **Execute:** Locate account delete/anonymize flow or documented in-app request path.
- **Expected:** UI accurately says what is deleted/anonymized and what historic data must remain; no false promise of total erasure.
- **PASS evidence required:** Screenshots/documentation path.
- **Automated proof required:** Manual acceptance + copy test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-059 — Deletion/anonymization keeps historical identity usable — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #42; NFR-02.
- **Execute:** Use synthetic user with historical expense/payment references; delete/anonymize; inspect visible history and linked settlement flow.
- **Expected:** Privacy-safe fallback identity remains; no broken UI/history; linked UC-03 settlement can still identify counterparty.
- **PASS evidence required:** Before/after screenshots and UC-03 test reference.
- **Automated proof required:** Mandatory cross-feature E2E regression for BUG #42.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-060 — Lifecycle UI does not expose secret/internal identifiers
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-02.
- **Execute:** Inspect leave/delete/anonymize confirmations, errors, history labels and console.
- **Expected:** No password, JWT, invitation token, hash, database key, SQL, class name or stack trace is shown.
- **PASS evidence required:** Screenshots/console evidence.
- **Automated proof required:** Component error-mapping/static-review test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-061 — Privacy information is reachable in UC-01 flows
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02 transparency.
- **Execute:** Find privacy notice/link from registration, profile, invitation, footer/help.
- **Expected:** A user can reach accurate information about stored data, purpose, and rights.
- **PASS evidence required:** Screenshots/route.
- **Automated proof required:** Link smoke/snapshot test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-062 — No unnecessary personal data is requested
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02 data minimization.
- **Execute:** Review registration, WG, invitation and profile forms against UC-01 purpose.
- **Expected:** No banking, special-category or unrelated field is requested.
- **PASS evidence required:** Screenshots/schema review.
- **Automated proof required:** Manual review record.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

## F. All applicable NFRs — security, usability, accessibility, compatibility, quality

### FE-063 — Unauthenticated deep links never flash WG data
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Open every UC-01 protected route with network throttling while logged out.
- **Expected:** Redirect/unauthorized state occurs without stale profile/household flash.
- **PASS evidence required:** Video.
- **Automated proof required:** Route-guard/Cypress matrix.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-064 — Expired/invalid sessions clear client state safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; TG-02.
- **Execute:** Inject invalid/expired session and navigate all UC-01 routes.
- **Expected:** No stale data or role action; login recovery works.
- **PASS evidence required:** Video/redacted storage evidence.
- **Automated proof required:** Auth-store/Cypress tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-065 — Cross-WG browser cache/history cannot leak data
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Use user A in WG A, logout, login user B in WG B on same device; use Back/Refresh/history.
- **Expected:** No A data appears under B session.
- **PASS evidence required:** Video/screenshots.
- **Automated proof required:** Two-user Cypress isolation test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-066 — All UC-01 buttons, icons, links and menu actions work
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04; explicit user request.
- **Execute:** Create a role/state inventory and click/tap every visible interactive control on public, Main Tenant, Roommate, invite, error, modal and lifecycle screens.
- **Expected:** Each control works, has clear disabled semantics, or is absent; none dead-ends.
- **PASS evidence required:** Completed control inventory with screenshots/video.
- **Automated proof required:** Dedicated Cypress UI-control inventory spec.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-067 — Loading states are visible and safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04; NFR-03.
- **Execute:** Throttle all registration/login/WG/invite/lifecycle calls.
- **Expected:** User sees progress; repeated interaction cannot create inconsistent state; stale toast does not mislead.
- **PASS evidence required:** Videos.
- **Automated proof required:** Component pending-state tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-068 — All dialogs are keyboard and mobile safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04/WAI.
- **Execute:** For every modal/dialog: Tab/Shift+Tab/Enter/Escape, outside click, long text, mobile viewport.
- **Expected:** Focus is managed; cancel/close safe; destructive action never triggers accidentally.
- **PASS evidence required:** Keyboard videos/audit.
- **Automated proof required:** Dialog component/accessibility tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-069 — Errors map safely for 400/401/403/404/409/500
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-04; NFR-03.
- **Execute:** Force/mimic every relevant status through UI.
- **Expected:** Short actionable messages; no raw backend payload, internal data or false success.
- **PASS evidence required:** Screenshots/network capture.
- **Automated proof required:** Service/component error-mapping tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-070 — Frontend has no secret/PII logging
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02.
- **Execute:** Search source and capture console during full UC-01 flow.
- **Expected:** No passwords, JWTs, raw invitation tokens or excess PII in console/telemetry/toasts.
- **PASS evidence required:** Search/console evidence.
- **Automated proof required:** Static scan/lint/manual security check.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-071 — Forms use correct autocomplete semantics
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04; NFR-01.
- **Execute:** Inspect autocomplete attributes and password manager behavior for email/current/new passwords.
- **Expected:** Expected browser/password-manager behavior; no confusing/autofill leakage.
- **PASS evidence required:** DOM evidence.
- **Automated proof required:** Component DOM review/test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-072 — Accessibility audit has no unresolved critical UC-01 defects
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04.
- **Execute:** Run Lighthouse/axe/WAI checks on registration/login/WG/invite/accept/lifecycle screens.
- **Expected:** No critical blocker; all findings fixed or explicitly escalated with evidence.
- **PASS evidence required:** Audit reports.
- **Automated proof required:** Repeatable axe/Lighthouse command or CI evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-073 — Peer tester completes onboarding without instructions
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04.
- **Execute:** Ask non-implementing peer to register, log in, create WG, create/copy invite, and join with a second account.
- **Expected:** Peer completes core flow; confusion is documented and resolved/accepted.
- **PASS evidence required:** Peer test record.
- **Automated proof required:** Manual acceptance evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-074 — Reference-device UI works at all required viewports
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04; TG-08.
- **Execute:** Run core UC-01 flow at Chrome/Firefox desktop 1366×768+, Safari iPhone 12 Pro 390×844, Chrome Pixel 7 412×915, and 360px width.
- **Expected:** No clipped/unreachable controls, horizontal overflow, keyboard blockers, or browser-specific failure.
- **PASS evidence required:** Screenshots/video/browser/device versions.
- **Automated proof required:** Cypress viewport + manual device/cross-browser smoke.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-075 — 200% zoom/text scaling does not block onboarding
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04/WAI.
- **Execute:** Test forms/dialogs at browser zoom 200% and suitable OS text scaling.
- **Expected:** Content reflows; primary action stays reachable; no essential overlap.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Manual accessibility evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-076 — Keyboard-only UC-01 workflow works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04/WAI.
- **Execute:** Complete register/login/create/invite/accept/leave flow using keyboard only.
- **Expected:** Logical visible focus and complete operability; no mouse-only blocker.
- **PASS evidence required:** Video.
- **Automated proof required:** Accessibility/E2E test plus manual proof.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-077 — Screen-reader names and status updates are meaningful
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04/WAI.
- **Execute:** Inspect accessibility tree and state changes for form errors, copy success, async loading and dialogs.
- **Expected:** Controls have accessible names; errors/status are perceivable.
- **PASS evidence required:** Axe/accessibility-tree evidence.
- **Automated proof required:** Component accessibility tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-078 — No out-of-scope capability is falsely advertised
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** Scope boundary; NFR-04.
- **Execute:** Review all UC-01 copy/icons/help.
- **Expected:** No automatic invitation email, email password reset, native app, or real-money workflow is promised.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Copy inventory/snapshot test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-079 — Frontend build, test, lint and type checks pass
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-05; TG-05.
- **Execute:** Run actual project frontend commands.
- **Expected:** No ignored/allow-failure critical check; all pass.
- **PASS evidence required:** Exact command output.
- **Automated proof required:** CI job evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-080 — Frontend documentation matches current behavior
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-05; TG-10.
- **Execute:** Follow user documentation verbatim for UC-01 flows.
- **Expected:** No obsolete route/button/flow/false email claim.
- **PASS evidence required:** Documentation execution record.
- **Automated proof required:** Doc acceptance test/manual record.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-081 — No production test/debug bypass exists in UI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Inspect feature flags, dev menus, test-account controls, route guard shortcuts and built output.
- **Expected:** No exposed role switch/token/debug bypass.
- **PASS evidence required:** Source/build scan.
- **Automated proof required:** Static/release review.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-082 — Final frontend sign-off is evidence-complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** All applicable FR/NFR.
- **Execute:** Review every FE item and mandatory regressions #35/#38/#42.
- **Expected:** No critical PASS lacks evidence; unresolved items remain FAIL/BLOCKED.
- **PASS evidence required:** Signed evidence review.
- **Automated proof required:** Reviewer sign-off.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`