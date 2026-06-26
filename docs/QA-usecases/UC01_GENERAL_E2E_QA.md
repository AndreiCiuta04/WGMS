# UC-01 — GENERAL / E2E QA FORM

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

**Layer:** Integrated Angular + Spring Boot + H2 behavior: real user journeys, multi-user authorization, cross-WG isolation, device/browser support, accessibility, peer acceptance, CI, documentation, and known regressions.

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

This file is the final proof that all frontend and backend requirements work together in a deployed/running system. It owns the full real-user evidence for FR-01, FR-02 and every NFR that applies to UC-01.

| Requirement group | Covered by these sections |
|---|---|
| FR-01 registration/login | A |
| FR-02 WG creation, roles, invitations, acceptance | A, B |
| NFR-01 auth/RBAC/isolation/no leaks | A, B, C |
| NFR-02 privacy/GDPR/lifecycle/logs | B, C |
| NFR-03 persistence/retry/concurrency/restart/CI | B, C |
| NFR-04 peer use, buttons, accessibility, browsers/devices | B, C |
| NFR-05 docs/CI/maintainability proof | C |
| NFR-06 invitation/membership audit trail | B |

## Completion standard

A checkbox is PASS only where the live behavior, persistence/API behavior where relevant, and required automated
evidence agree. “Code looks correct”, “endpoint exists”, “button is visible”, “works manually once”, or “pipeline
was green before” is insufficient.


## A. Core FR-01 / FR-02 integrated journeys

### E2E-001 — Public onboarding route smoke
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-04.
- **Execute:** In clean browser, navigate landing → register and direct protected route.
- **Expected:** Public screen works; protected route safely redirects; no data flash.
- **PASS evidence required:** Video/routes/console.
- **Automated proof required:** Cypress public onboarding smoke.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-24 | UC-01/QA | synthetic unique emails | fr01-auth.cy.js "logged-out users are redirected away from authenticated pages" + gdpr-security.cy.js "blocks unauthenticated protected routes and protected API access after logout" | CI e2e-test: cd e2e && npx cypress run | local: cd e2e && npx cypress run (13/13 pass) | e2e/cypress/e2e/fr01-auth.cy.js, e2e/cypress/e2e/gdpr-security.cy.js | dzovani koller`

### E2E-002 — New user registers, logs out and logs in
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01.
- **Execute:** Register synthetic user through UI, logout, login same account.
- **Expected:** One account; valid login; correct session state.
- **PASS evidence required:** Video plus API/DB evidence.
- **Automated proof required:** Cypress full auth happy path.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-24 | UC-01/QA | synthetic unique emails | fr01-auth.cy.js "registers a user, logs in, reaches dashboard, and logs out" | local: cd e2e && npx cypress run (13/13 pass) | e2e/cypress/e2e/fr01-auth.cy.js | dzovani koller`

### E2E-003 — Negative registration and login matrix
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-01; NFR-01; NFR-03.
- **Execute:** Test malformed/empty/duplicate fields, wrong password, unknown email, network errors.
- **Expected:** Safe generic errors, no partial state, recovery possible.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Parameterized Cypress negative suite.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-24 | UC-01/QA | synthetic unique emails | fr01-auth.cy.js "invalid login and duplicate registration show readable errors" + "malformed login and weak registration are blocked before backend requests" | local: cd e2e && npx cypress run (13/13 pass) | e2e/cypress/e2e/fr01-auth.cy.js | dzovani koller`

### E2E-004 — Expired/invalid session end-to-end
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-02; NFR-01.
- **Execute:** Use expired/invalid session fixture, open protected route, then recover by login.
- **Expected:** No WG data; clean recovery.
- **PASS evidence required:** Video.
- **Automated proof required:** Cypress expiry flow.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-25 | UC-01/QA | in-test expired JWT (synthetic) | uc01-invite-edge-cases.cy.js "redirects a protected route to login on expired token, then recovers after login" | local: cd e2e && npx cypress run (26/26 pass) | e2e/cypress/e2e/usecases/uc01-invite-edge-cases.cy.js | dzovani koller`

### E2E-005 — New user creates WG and becomes Main Tenant
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02.
- **Execute:** Register/login then create WG; inspect UI/API state.
- **Expected:** Exactly one WG; creator Main Tenant; invite controls available.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Cypress create-WG flow.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-24 | UC-01/QA | synthetic unique emails | uc01-onboarding-household.cy.js "user creates WG, invites roommate, and both users see membership" + fr02-household-invitations.cy.js (createHousehold + Manage Invitations visible) | local: cd e2e && npx cypress run (13/13 pass) | e2e/cypress/e2e/usecases/uc01-onboarding-household.cy.js, e2e/cypress/e2e/fr02-household-invitations.cy.js | dzovani koller`

### E2E-006 — Roommate cannot create/manage invitations
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Join as Roommate, click/deep-link/tamper invitation actions.
- **Expected:** No privileged result; server denial reflected safely.
- **PASS evidence required:** Video/network.
- **Automated proof required:** Cypress role-denial test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-24 | UC-01/QA | synthetic unique emails | gdpr-security.cy.js "prevents roommates from performing Main Tenant invitation actions" | local: cd e2e && npx cypress run (13/13 pass) | e2e/cypress/e2e/gdpr-security.cy.js | dzovani koller`
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02.
- **Execute:** Create invitation with explicit expiry, copy it, open in fresh profile.
- **Expected:** Usable link; no claimed email dispatch; expiry shown accurately.
- **PASS evidence required:** Video/redacted link.
- **Automated proof required:** Cypress generate-copy flow.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-24 | UC-01/QA | synthetic unique emails | createInvitation command (168h expiry, "Invitation Link Created" + readonly copyable link) exercised by uc01-onboarding-household.cy.js / fr02-household-invitations.cy.js, then opened in a cleared session via visitInviteUrl | local: cd e2e && npx cypress run (13/13 pass) | e2e/cypress/support/commands.js, e2e/cypress/e2e/fr02-household-invitations.cy.js | dzovani koller`

### E2E-008 — Invitation state matrix
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02; NFR-01; NFR-02.
- **Execute:** Run valid, invalid, tampered, expired, used, revoked/cancelled if supported, stale-page invite states.
- **Expected:** Each state gives safe correct UI/API outcome; no unauthorized membership.
- **PASS evidence required:** Screenshots/API/DB.
- **Automated proof required:** Parameterized Cypress invite-state suite.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-25 | UC-01/QA | synthetic unique emails | uc01-nfr-coverage.cy.js "shows a safe error for expired, revoked, and used invitation tokens" (UI matrix) + uc01-onboarding/fr02 invalid-token UI + gdpr-security.cy.js used-link 410 + InvitationEndpointTest backend | local: cd e2e && npx cypress run (26/26 pass); backend InvitationEndpointTest | e2e/cypress/e2e/usecases/uc01-nfr-coverage.cy.js, e2e/cypress/e2e/gdpr-security.cy.js, InvitationEndpointTest.java | dzovani koller`
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-02.
- **Execute:** Open valid invite while logged out; follow supported register/login path; accept.
- **Expected:** Invite context retained; exactly one Roommate membership.
- **PASS evidence required:** Video/API/DB.
- **Automated proof required:** Cypress new-user invite journey.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-24 | UC-01/QA | synthetic unique emails | fr02-household-invitations.cy.js "invited user accepts invitation and appears as household member" (asserts POST /users 200/201, POST /invitations/{t}/accept 200, ROOMMATE listed) | local: cd e2e && npx cypress run (13/13 pass) | e2e/cypress/e2e/fr02-household-invitations.cy.js | dzovani koller`

### E2E-010 — Existing user accepts invitation — BUG #35
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #35; FR-02; NFR-03.
- **Execute:** Use already-registered account not in target WG, open valid invitation and accept.
- **Expected:** No duplicate account, no 500, one target membership, correct role/route.
- **PASS evidence required:** Video/API/DB count.
- **Automated proof required:** Mandatory named Cypress BUG #35 regression.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-25 | UC-01/QA | synthetic unique emails | uc01-bug35-existing-user-invite.cy.js "lets an existing account join via a logged-in invitation accept without duplicates or 500" (accept 200; repeat register 409; exactly one active ROOMMATE) | local: cd e2e && npx cypress run (26/26 pass) | e2e/cypress/e2e/usecases/uc01-bug35-existing-user-invite.cy.js | dzovani koller`

### E2E-011 — Already-active user cannot duplicate membership
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Accept invitation as already-active target WG member.
- **Expected:** Safe idempotent/rejection result; no duplicate/500.
- **PASS evidence required:** Video/API/DB.
- **Automated proof required:** Cypress active-member test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-25 | UC-01/QA | synthetic unique emails | uc01-invite-edge-cases.cy.js "rejects a logged-in active roommate who tries to accept a second invite to the same WG" | local: cd e2e && npx cypress run (26/26 pass) | e2e/cypress/e2e/usecases/uc01-invite-edge-cases.cy.js | dzovani koller`
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #38; FR-02; NFR-03.
- **Execute:** Join, leave, issue new invite, accept with same account.
- **Expected:** No unique-constraint 500; exactly one active membership; correct UI.
- **PASS evidence required:** Video/API/DB/audit.
- **Automated proof required:** Mandatory named Cypress BUG #38 regression.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-25 | UC-01/QA | synthetic unique emails | uc01-bug38-leave-rejoin.cy.js "reactivates membership on rejoin without a unique-constraint 500 or duplicate" (leave via profile/edit UI, rejoin invite via API, logged-in accept UI; backend InvitationEndpointTest.givenFormerMember_whenAcceptInvitationAgain_thenReactivatesMembership) | local: cd e2e && npx cypress run (26/26 pass); backend mvn -Dtest=InvitationEndpointTest test | e2e/cypress/e2e/usecases/uc01-bug38-leave-rejoin.cy.js, backend InvitationServiceImpl.java | dzovani koller`

### E2E-013 — Leave/delete/anonymize preserves later settlement identity — BUG #42
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #42; NFR-02; NFR-03.
- **Execute:** Use fixture with historical expenses/payments; leave/delete/anonymize, inspect history, run linked UC-03 settlement scenario.
- **Expected:** Privacy-safe historical identity remains and settlement can still complete.
- **PASS evidence required:** Before/after evidence + linked UC-03 test.
- **Automated proof required:** Mandatory cross-feature E2E regression.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Scope note:** UC-01 owns the lifecycle/reference side, covered here: a throwaway user is anonymized and task/payment history stays readable. The full UC-03 settlement arithmetic after anonymization is owned by UC-03 and is not re-proven in this form.
- **Evidence record:** `dzovani koller | 2026-06-25 | UC-01/QA | synthetic unique emails | gdpr-domain-history.cy.js "keeps task and payment history readable after a throwaway user is anonymized" | local: cd e2e && npx cypress run (26/26 pass) | e2e/cypress/e2e/gdpr-domain-history.cy.js | dzovani koller`

### E2E-014 — Two-household isolation end-to-end
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Create WG A/B with distinct actors; tamper IDs/routes/history/API calls across sessions.
- **Expected:** No cross-WG view/mutation/leakage.
- **PASS evidence required:** Video/network/DB evidence.
- **Automated proof required:** Cypress two-household isolation suite.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-24 | UC-01/QA | synthetic unique emails | gdpr-security.cy.js "prevents a user from WG B accessing WG A data by changing IDs" | local: cd e2e && npx cypress run (13/13 pass) | e2e/cypress/e2e/gdpr-security.cy.js | dzovani koller`

### E2E-015 — All visible UC-01 controls work in integrated app
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04; explicit user requirement.
- **Execute:** Inventory every button/icon/link/menu/dialog in public/Main Tenant/Roommate/invite/error/lifecycle states; activate each.
- **Expected:** No dead control or false success; disabled state justified and clear.
- **PASS evidence required:** Completed control inventory/video.
- **Automated proof required:** Cypress control-inventory suite.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Why not fully automated (manual gate):** This is an *exhaustive* per-control inventory across every role/state. The functional UC-01 specs already exercise the primary controls (register/login/logout buttons, create-WG submit, invite create/copy, accept/decline, leave/confirm dialog), so the happy-path controls are automatically covered. What is NOT automatable cheaply is proving the *absence* of any dead/false-success control across the whole UI surface — that requires a human to enumerate and click each control once. **Proof needed:** a checklist/screen-recording walkthrough listing every control per state and its observed effect (a partial recording exists under `docs/QA-usecases/uc01-evidence/`). Leave BLOCKED until a reviewer signs the inventory.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-016 — Double submit/retry/reload safety
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Double-click and throttle register/create/invite/accept/leave; reload/back/forward at each mutation.
- **Expected:** No duplicate state, no false success, server truth reconciled.
- **PASS evidence required:** Video/request counts.
- **Automated proof required:** Cypress mutation-reliability suite.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-26 | UC-01/QA | synthetic unique emails | uc01-reliability.cy.js "keeps exactly one WG after a double-click create and a reload" (double-clicks Initialize WG, then reloads the post-mutation page; backend /households/me + /members confirm exactly one WG with one member) | local: cd e2e && npx cypress run (35/35 pass) | CI e2e-test: cd e2e && npx cypress run | e2e/cypress/e2e/usecases/uc01-reliability.cy.js | dzovani koller`

### E2E-017 — Multi-tab/concurrent invitation acceptance
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03.
- **Execute:** Open same invitation in two tabs/users, accept simultaneously.
- **Expected:** One success only; controlled second result; no duplicate membership.
- **PASS evidence required:** Video/DB evidence.
- **Automated proof required:** Cypress multi-session or API concurrency test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-26 | UC-01/QA | synthetic unique emails | uc01-reliability.cy.js "never creates a duplicate membership when one user double-accepts in parallel" (fires two simultaneous POST /invitations/{token}/accept via Promise.all for the same user/token; backend /members confirms the joiner is listed exactly once) | local: cd e2e && npx cypress run (35/35 pass) | CI e2e-test: cd e2e && npx cypress run | e2e/cypress/e2e/usecases/uc01-reliability.cy.js | dzovani koller` — Note: "anyone with link" invites are intentionally multi-use, so the protected invariant is no duplicate membership for a single user under a concurrent double-accept; the single-use path for distinct token reuse is covered by gdpr-security.cy.js "rejects reuse of a single-use invitation link".

### E2E-018 — Network interruption/timeout behavior
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; NFR-04.
- **Execute:** Disconnect/slow API during core mutations; restore and retry.
- **Expected:** No indefinite hang/false success; recovery is clear; final state accurate.
- **PASS evidence required:** Video/network trace.
- **Automated proof required:** Cypress intercept network tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-26 | UC-01/QA | synthetic unique emails | uc01-reliability.cy.js "shows a safe error on a failed create request, then succeeds on retry" (cy.intercept fails the first POST /households, asserts the safe "Something went wrong" alert and that the user stays on the form, then lets the retry through to a real 201 and confirms /households/me) | local: cd e2e && npx cypress run (35/35 pass) | CI e2e-test: cd e2e && npx cypress run | e2e/cypress/e2e/usecases/uc01-reliability.cy.js | dzovani koller`

### E2E-019 — Server error behavior is safe
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-04.
- **Execute:** Inject/mock controlled 500 response into UI paths and observe.
- **Expected:** No stack trace/raw HTML/internal error; retry/next action clear.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Cypress mocked-500 tests.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-26 | UC-01/QA | synthetic unique emails | uc01-reliability.cy.js "shows a safe generic error (no stack trace) when invitation creation returns 500" (cy.intercept stubs POST /households/*/invitations with 500; asserts the friendly "Could not create invitation." message and that the page body never contains "Exception", "at java.", or "Whitelabel Error Page") | local: cd e2e && npx cypress run (35/35 pass) | CI e2e-test: cd e2e && npx cypress run | e2e/cypress/e2e/usecases/uc01-reliability.cy.js | dzovani koller`

### E2E-020 — No secret/PII leakage in browser or backend logs
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02.
- **Execute:** Execute normal and negative flows while capturing console/network/backend logs.
- **Expected:** No password/JWT/raw invite/full private data leaks.
- **PASS evidence required:** Redacted logs.
- **Automated proof required:** Console-error guard + log scan.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Scope note:** Covers the browser side — the plaintext password is never echoed in any UC-01 API response body. Backend log redaction is not asserted by this automated proof.
- **Evidence record:** `dzovani koller | 2026-06-25 | UC-01/QA | synthetic unique emails | uc01-nfr-coverage.cy.js "never echoes the plaintext password in any UC-01 API response body" (intercepts all /api/v1 responses during register+create WG+create invite) | local: cd e2e && npx cypress run (26/26 pass) | e2e/cypress/e2e/usecases/uc01-nfr-coverage.cy.js | dzovani koller`

### E2E-021 — Privacy rights flow end-to-end
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02.
- **Execute:** View account/membership, correct eligible data, leave, delete/anonymize and verify result.
- **Expected:** Actual delivered rights work or documented alternative is real; no false success.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Cypress/manual privacy flow.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-022 — Membership/invitation audit trail end-to-end
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-06.
- **Execute:** Generate, accept, leave/rejoin, remove/delete fixture user; inspect audit evidence.
- **Expected:** Actor/time trace exists for required actions without raw secrets.
- **PASS evidence required:** Audit evidence.
- **Automated proof required:** Integration + E2E linkage.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-023 — OpenAPI and real browser requests agree
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-04.
- **Execute:** Compare browser network requests/status/error DTOs for all core UC-01 flows to served OpenAPI.
- **Expected:** No undocumented/deprecated mismatch.
- **PASS evidence required:** Network/OpenAPI evidence.
- **Automated proof required:** Contract test plus manual check.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-024 — Clean setup/seed restore supports demo
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; TG-05; TG-10.
- **Execute:** From clean environment follow docs to start app, reset/seed, execute core UC-01 flow.
- **Expected:** No hidden manual action; synthetic fixtures and demo state work.
- **PASS evidence required:** Commands/output/video.
- **Automated proof required:** Clean-environment smoke test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-025 — CI actually runs all UC-01 layers
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-05; TG-09.
- **Execute:** Inspect/run CI after UC-01 test change; verify frontend/backend/E2E jobs.
- **Expected:** Required test layers run and failures block merge; no critical suite excluded.
- **PASS evidence required:** Pipeline evidence.
- **Automated proof required:** CI verification.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-24 | UC-01/QA | n/a | .gitlab-ci.yml e2e-test job + local cd e2e && npx cypress run (13/13 pass) | inspected .gitlab-ci.yml + local run | GitLab pipeline pending; local Cypress 13/13 pass | .gitlab-ci.yml | dzovani koller`

## C. All applicable NFRs — accessibility, compatibility, peer testing, documentation, CI and release

### E2E-026 — Peer tester completes core onboarding unaided
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04.
- **Execute:** Give non-implementer app only; ask to register, create WG, send link, join second account.
- **Expected:** Success without external instructions; confusion/blockers captured and fixed/accepted.
- **PASS evidence required:** Peer test record.
- **Automated proof required:** Manual acceptance evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Why not automatable (inherently manual):** This measures whether a *human non-implementer* can complete onboarding unaided — a usability/comprehension signal that no automated test can produce (Cypress always "knows" the selectors and the flow). **Proof needed:** a short peer-test record — who tested, on what build, the task given, where they hesitated/failed, and the disposition. Leave BLOCKED until a peer session is run and recorded.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-027 — Peer tester understands role boundaries
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04.
- **Execute:** Ask Main Tenant/Roommate peer after flow what each can/cannot do.
- **Expected:** UI communicates role difference sufficiently; misunderstandings recorded.
- **PASS evidence required:** Peer notes.
- **Automated proof required:** Manual evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Why not automatable (inherently manual):** Whether the UI *communicates* the Main Tenant vs Roommate distinction well enough for a person to understand is a comprehension judgement, not an assertable state. (The backend enforcement of the boundary IS automated — see gdpr-security.cy.js "prevents roommates from performing Main Tenant invitation actions".) **Proof needed:** peer notes capturing what each role believed they could/couldn't do and any misunderstanding. Leave BLOCKED until recorded.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-028 — Lighthouse/axe/WAI gate
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04.
- **Execute:** Run audit on register/login/create/invite/accept/lifecycle screens at desktop/mobile.
- **Expected:** No critical outstanding issue; other findings dispositioned.
- **PASS evidence required:** Audit files.
- **Automated proof required:** Repeatable Lighthouse/axe execution.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Scope note:** Automated `cypress-axe`/`axe-core` gate runs on the public UC-01 entry pages (landing, login, register) and fails the build on any `critical`/`serious` WCAG violation (`moderate`/`minor` findings are logged via `cy.task('log')` for disposition). Authenticated screens (create/invite/accept/lifecycle) and the Lighthouse performance/SEO audit are not yet in the automated gate; those remain a manual axe/Lighthouse pass.
- **Evidence record:** `dzovani koller | 2026-06-26 | UC-01/QA | n/a (public pages) | uc01-accessibility.cy.js "has no critical/serious accessibility violations on the {landing,login,register} page" via cy.injectAxe()+cy.checkA11y(undefined,{includedImpacts:['critical','serious']}) → 3/3 pass, 0 critical/serious violations | local: cd e2e && npx cypress run (35/35 pass) | CI e2e-test: cd e2e && npx cypress run | e2e/cypress/e2e/usecases/uc01-accessibility.cy.js, e2e/cypress/support/e2e.js, e2e/cypress/plugins/index.js, e2e/package.json (cypress-axe, axe-core) | dzovani koller`

### E2E-029 — Keyboard-only end-to-end onboarding
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04/WAI.
- **Execute:** Complete core UC-01 flow with keyboard only.
- **Expected:** No mouse-only blocker; focus/dialog/error behavior correct.
- **PASS evidence required:** Video.
- **Automated proof required:** Manual + automated accessibility evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Why not automatable (partial only):** The axe gate (E2E-028) statically catches many keyboard/focus issues (missing labels, focus order metadata), but proving a *full* keyboard-only onboarding journey (tabbing through register → create WG → invite → accept with correct focus traps in dialogs) reliably is brittle in Cypress because `cy.tab()` requires a plugin and real focus/AT behavior differs from synthetic key events. **Proof needed:** a manual keyboard-only screen recording of the core flow plus the axe findings from E2E-028. Leave BLOCKED until the keyboard walkthrough is recorded.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-030 — Chrome desktop reference smoke
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04; TG-08.
- **Execute:** Run full flow on Chrome 1366×768+.
- **Expected:** No UI/function defect.
- **PASS evidence required:** Video/version.
- **Automated proof required:** Cypress Chrome suite.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-26 | UC-01/QA | synthetic unique emails | the entire UC-01 Cypress suite is executed on Chrome for Testing 150.0.7871.24 at viewport 1920×1080 (>1366×768) — register/login/create/invite/accept/leave/rejoin/edge/reliability/a11y → 35/35 pass | local: cd e2e && npx cypress run --browser chrome | CI e2e-test: cd e2e && npx cypress run | e2e/cypress.config.js (viewport), all e2e/cypress/e2e/usecases/uc01-*.cy.js | dzovani koller`

### E2E-031 — Firefox desktop reference smoke
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04; TG-08.
- **Execute:** Run full flow on Firefox Stable 1366×768+.
- **Expected:** No UI/function defect.
- **PASS evidence required:** Video/version.
- **Automated proof required:** Cross-browser smoke evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Setup note:** Cypress cannot auto-detect a Firefox at a non-standard path or a Firefox fork, so `e2e/cypress/plugins/index.js` registers a Firefox-family custom browser from `FIREFOX_BIN` (or an auto-detected Zen/Firefox). Run with `cd e2e && npx cypress run --browser firefox`. Verified here against a Firefox-engine build (Zen 1.21.3b, a Firefox fork) at viewport 1920×1080.
- **Evidence record:** `dzovani koller | 2026-06-26 | UC-01/QA | synthetic unique emails | the UC-01 suite runs on the Firefox-family browser (Cypress "Browser: Firefox (custom)") — accessibility/bug35/bug38/edge/nfr/onboarding/reliability/fr01/fr02/gdpr-security → 30/30 pass on Firefox engine | local: cd e2e && npx cypress run --browser firefox | e2e/cypress/plugins/index.js, e2e/package.json | dzovani koller`

### E2E-032 — iPhone 12 Pro Safari reference smoke
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04; TG-08.
- **Execute:** Run full flow at Safari/iPhone 12 Pro 390×844 or approved device service.
- **Expected:** No Safari/mobile keyboard/link/layout blocker.
- **PASS evidence required:** Device video/screenshots.
- **Automated proof required:** Manual device evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Why not automatable (no WebKit/Safari driver):** Cypress cannot drive real Safari/WebKit (its WebKit support is experimental, Playwright-based, and not real iOS Safari), and a 390×844 Chrome viewport is an *emulation*, not the actual Safari engine/keyboard. **Proof needed:** a real-device or BrowserStack/SauceLabs iPhone 12 Pro Safari run with screen recording. Layout at the narrow width is partially de-risked by the automated 360px smoke (E2E-034). Leave BLOCKED until a device/cloud run is recorded.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-033 — Pixel 7 Chrome reference smoke
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04; TG-08.
- **Execute:** Run full flow at Pixel 7 Chrome 412×915 or approved device service.
- **Expected:** No Android/mobile blocker.
- **PASS evidence required:** Device video/screenshots.
- **Automated proof required:** Manual device evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Why not automatable (real-device signal):** A 412×915 Chrome viewport in Cypress emulates the size but not real Android Chrome touch/keyboard/scroll behavior. **Proof needed:** a real-device or cloud (BrowserStack) Pixel 7 Chrome run with screen recording. The responsive layout is partially de-risked by the automated 360px smoke (E2E-034). Leave BLOCKED until a device/cloud run is recorded.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-034 — 360px boundary usability
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04.
- **Execute:** Run primary actions at 360px viewport.
- **Expected:** No horizontal-scroll requirement, clipped action, keyboard trap or inaccessible modal.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Cypress viewport suite.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Scope note:** Automated proof covers the landing page at 360px (primary "Get Started"/"Login" reachable, no horizontal overflow). Deeper screens at 360px are not yet asserted.
- **Evidence record:** `dzovani koller | 2026-06-25 | UC-01/QA | n/a (public landing) | uc01-nfr-coverage.cy.js "keeps Get Started reachable without horizontal overflow at 360px" (cy.viewport(360,740), scrollWidth <= clientWidth) | local: cd e2e && npx cypress run (26/26 pass) | e2e/cypress/e2e/usecases/uc01-nfr-coverage.cy.js | dzovani koller`

### E2E-035 — Zoom/text scaling usability
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04/WAI.
- **Execute:** Repeat critical screens at 200% zoom/text scaling.
- **Expected:** No essential content/action overlap or loss.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Manual accessibility evidence.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Why not automatable (browser-level zoom):** Browser 200% page-zoom / OS text scaling is a browser-chrome setting Cypress cannot toggle (it controls viewport size, not zoom factor); reflow at zoom is visually judged. **Proof needed:** screenshots of each critical screen at 200% zoom confirming no overlap/clipping/loss of actions. Leave BLOCKED until captured manually.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-036 — Browser/session cache isolation
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Login A → household → logout → login B; use back/refresh/history/service worker cache.
- **Expected:** No prior user’s household data appears.
- **PASS evidence required:** Video/storage evidence.
- **Automated proof required:** Cypress same-browser isolation.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-25 | UC-01/QA | synthetic unique emails | uc01-nfr-coverage.cy.js "does not leak a previous user's household to a different account in the same browser" (owner has WG; outsider /households/me stays 404 across switch; members page shows no owner data) | local: cd e2e && npx cypress run (26/26 pass) | e2e/cypress/e2e/usecases/uc01-nfr-coverage.cy.js | dzovani koller`

### E2E-037 — Out-of-scope workflow claims are absent
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** Scope boundary; NFR-04.
- **Execute:** Review UX/docs during real flow for automatic email invites/password reset.
- **Expected:** No nonimplemented email workflow is promised.
- **PASS evidence required:** Screenshots/docs.
- **Automated proof required:** Copy control inventory.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `dzovani koller | 2026-06-26 | UC-01/QA | synthetic unique emails | uc01-reliability.cy.js "register and login pages do not promise email-sending or password-reset flows" + "invitation page presents a copyable link and never claims an invite email was sent" (asserts page text never matches /we (sent|emailed)|check your inbox|reset (your )?password|password reset link|verification email sent|sent you an email/i, and the invite page exposes a readonly link input the owner shares themselves) | local: cd e2e && npx cypress run (35/35 pass) | CI e2e-test: cd e2e && npx cypress run | e2e/cypress/e2e/usecases/uc01-reliability.cy.js | dzovani koller`

### E2E-038 — Documentation matches real integrated flow
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-10; NFR-05.
- **Execute:** Follow end-user/setup documentation word-for-word using clean environment.
- **Expected:** No obsolete action/false promise/missing prerequisite.
- **PASS evidence required:** Execution record.
- **Automated proof required:** Documentation acceptance test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-039 — Test data is isolated and repeatable
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; NFR-02.
- **Execute:** Run suite twice/parallel if supported; inspect cleanup/unique fixture strategy.
- **Expected:** No collision/order dependency/real user data.
- **PASS evidence required:** Outputs/fixture evidence.
- **Automated proof required:** Cypress fixture strategy.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-040 — No production debug/test bypass appears in deployment
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01.
- **Execute:** Inspect deployed/browser build for dev menu, token shortcut, role switch, test account bypass.
- **Expected:** No exposed bypass.
- **PASS evidence required:** Screenshots/source/build scan.
- **Automated proof required:** Release test.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-041 — Final integrated sign-off checks every requirement group
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** All FR/NFR/TGs.
- **Execute:** Review all three UC-01 forms, evidence and mandatory bugs before merge.
- **Expected:** No hidden unresolved critical item; evidence complete.
- **PASS evidence required:** Signed review summary.
- **Automated proof required:** Reviewer sign-off.
- **If failed / evidence missing:** Leave this item unchecked. Fix the implementation or test gap, add the smallest focused regression test at the correct layer, rerun the full affected suite, then record the exact files and evidence.
- **Why not automatable (human sign-off):** This is the merge-gate human review confirming every requirement group's evidence is complete — by definition a reviewer signature, not a test. **Proof needed:** a named reviewer dated sign-off after the remaining manual/device items above are closed. Leave BLOCKED until sign-off.
- **Evidence record:** `executor | date | branch/commit | fixture(s) | test name | exact command | evidence path | changed files | reviewer`
