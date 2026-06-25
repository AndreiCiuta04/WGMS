# UC-04 — GENERAL / E2E QA FORM

## Embedded execution instructions — no Project Proposal upload required

This file is self-contained for **UC-04**. Do **not** ask the user to upload, paste, or re-explain the Project Proposal.

Before checking any box, inspect the current repository’s Angular routes/components/services/tests; Spring controllers/services/entities/repositories/DTOs/security configuration/tests; H2 schema; OpenAPI; seed/reset tools; CI; existing shopping catalog/status/visibility logic; and the current state of all found bug fixes.

**Strict rules**
1. Mark PASS only with the exact evidence and automated proof named below. “The code looks correct”, “it worked once”, “the button exists”, or “CI was green earlier” is not evidence.
2. If evidence or a required test does not exist, create it. If a defect appears, fix it, add a regression test, rerun the relevant suite, then update the evidence.
3. Never weaken expected behavior, remove/skip a critical test, use `|| true`, or hide FAIL/BLOCKED items.
4. Use synthetic users, households, shopping items, catalog entries and QR payloads only. Do not place passwords, JWTs, raw invitation tokens, hashes, secrets, real personal data or private shopping history in evidence.
5. UC-04 is not releasable while a critical requirement lacks proof.

## Binding UC-04 functional baseline

- **FR-07 — Shopping list management (High, Core):** members can add shopping items, mark and unmark items as bought, use structured catalogue entries and autocomplete/common-item suggestions where delivered. The underlying shopping item status model is `NEEDED`, `BOUGHT`, `CANCELLED`.
- **FR-08 — Targeted shopping request collection (Medium, Core):** shopping items can be personal, targeted, or shared. Targeted items are visible to their intended recipient(s) according to the actual delivered model. Mark/unmark bought must work consistently for targeted items.
- **FR-09 — Aggregated household shopping list & shopping history (Medium, Core):** individual/shared requests aggregate into a household shopping list and a transparent shopping history. Bought/unbought/cancelled transitions must be represented correctly and persist reliably.
- **FR-10 — Shopping item suggestion/voting (Medium, Extended):** where delivered, members can suggest/vote for shopping items. The real delivery model must be discovered from the repository. If prototype-only/absent, it must be documented truthfully.
- **FR-18 — QR-code scanner for one-click shopping item entry (Low, Extended):** where delivered, valid scanned payloads can add shopping items. Scanned payloads must be validated on the backend. The actual scanner/camera/payload model must be discovered from the repository; do not invent unsupported scanner behavior.

## Contract domain invariants that must be proven

- A `ShoppingItem` has status `NEEDED`, `BOUGHT`, or `CANCELLED`.
- Shopping-item visibility is `PERSONAL`, `TARGETED`, or `SHARED`; targeted items have a target recipient according to the delivered model.
- The shopping module uses a structured catalogue, autocomplete suggestions/common-item suggestions where implemented, and keeps household shopping history.
- Items can be marked and unmarked as bought.
- Aggregate shopping views must show the correct household-visible set without leaking personal/targeted items to the wrong member or another household.
- QR payloads must be validated on the backend before creating a shopping item.
- Shopping workflows do not execute payment or banking operations.

## Binding non-functional baseline

- **NFR-01 Security & Access Control:** authenticated access to WG-specific shopping data; server-side household scoping, visibility enforcement, RBAC and input validation.
- **NFR-02 Data Privacy & GDPR:** data minimization; correct privacy for personal/targeted/shared items and history; safe leave/deletion/anonymization; no PII/secrets in logs or responses.
- **NFR-03 Reliability:** durable item/status/history/catalog/vote data; reproducible seed/reset; atomic mutations; safe duplicates/races/restarts/error handling.
- **NFR-04 Usability & Compatibility:** peer-testable shopping workflows; WAI/Lighthouse; Chrome/Firefox desktop 1366×768+, iPhone 12 Pro Safari 390×844, Pixel 7 Chrome 412×915, and 360px boundary.
- **NFR-05 Maintainability:** clean Angular/Spring/repository/business-rule separation, current documentation and test coverage.
- **NFR-06 Auditability:** material shopping actions—creation, targeting/visibility change, status change, cancellation, suggestion/vote and QR-origin creation where delivered—must be traceable by actor and timestamp where audit facility exists.

## Mandatory found-bug regression gate

**Direct UC-04 bug**
- **BUG #36:** Shopping quantity must not be treated as arbitrary free text where the product requires a structured unit/type. The current implementation must be inspected to identify the authoritative supported quantity/unit model. The UI and backend must reject free-text/unit ambiguity outside that model, preserve valid units and quantities, and render them consistently in list/history/aggregate views.

**Membership/privacy integration bugs**
- **BUG #35:** Existing user acceptance must not duplicate target recipients, personal items, shopping history visibility or voting identities.
- **BUG #38:** Leave then rejoin must not duplicate target-recipient entries, history visibility, votes, or shopping suggestions.
- **BUG #42:** Leaving/deleting/anonymizing a member must not break history, target references, or any shopping-origin trace. Historical representation must be privacy-safe.

**Whole-project non-regression**
After UC-04 changes, run focused regression tests for **#31, #32, #35, #36, #37, #38, #39 and #42** and record every test command/name.

## Layer scope

**Integrated Angular + Spring Boot + H2 proof: real structured-shopping, targeting, aggregation/history, vote/scanner workflows, privacy/visibility, browser/device/accessibility, peer testing, CI and all found-bug gates.**

This file is the final system-level proof that FR-07, FR-08, FR-09, FR-10, FR-18 and every applicable NFR-01..NFR-06 work together in the running application.


## A. Core FR-07 / FR-08 / FR-09 integrated shopping journeys

### E2E-001 — Core structured shopping journey
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07
- **Execute:** Add catalogue item with valid quantity/unit, inspect personal/shared list, mark bought, unmark and refresh.
- **Expected:** One correct item; structured values persist; status is coherent in all views.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Cypress core shopping journey.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-002 — Quantity/unit structured regression — BUG #36
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #36; FR-07; FR-09
- **Execute:** Use every valid unit and boundary quantity; attempt arbitrary quantity/unit free text through UI and direct form manipulation; inspect list/aggregate/history.
- **Expected:** Only canonical numeric quantity and structured unit accepted/displayed; free-text ambiguity is impossible.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #36 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-003 — Personal, targeted and shared visibility journey
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-08; NFR-01; NFR-02
- **Execute:** Create all three visibility types across users A/B/C and two WGs; inspect each account.
- **Expected:** Exactly intended viewers see each item; no private/targeted/foreign leak.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress visibility matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-004 — Targeted item lifecycle journey
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-08
- **Execute:** Create A→B targeted item, let B mark/unmark bought, inspect A/B/C lists and history.
- **Expected:** Status/visibility/history follows delivered policy consistently.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Cypress targeted lifecycle test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-005 — Aggregate household list journey
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09
- **Execute:** Create multiple personal/targeted/shared NEEDED/BOUGHT/CANCELLED items with various units; inspect aggregate from each member.
- **Expected:** Aggregate contains exactly intended active items, preserves units/quantities and no forbidden visibility leaks.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress aggregate visibility/unit regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-006 — History lifecycle journey
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09
- **Execute:** Create item, change status/visibility/target, mark/unmark/cancel as supported, then use history and refresh/back.
- **Expected:** History is durable, chronological/canonical and privacy-scoped; no duplicate/missing entry.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Cypress history workflow.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-007 — Existing invited user is not duplicated in shopping — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-08; FR-09
- **Execute:** Invite an existing account, create targeted/shared items and inspect recipient/history/vote UI.
- **Expected:** One recipient identity, no duplicated item/history/vote state.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #35 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-008 — Leave/rejoin does not duplicate shopping identity — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-08; FR-09
- **Execute:** Create target/history, leave, rejoin, then create/read/update items.
- **Expected:** No duplicate target/history/visibility/vote identity or 500.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #38 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-009 — Deleted/anonymized user history remains privacy-safe — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-09; NFR-02
- **Execute:** Create shopping history involving member, leave/delete/anonymize them, inspect permitted views.
- **Expected:** Privacy-safe fallback remains; no null crash/PII/foreign data leak.
- **PASS evidence required:** Before/after evidence.
- **Automated proof required:** Mandatory cross-feature Cypress BUG #42 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-010 — All status actions work under real UI conditions
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; FR-08
- **Execute:** Mark/unmark/cancel/delete every applicable personal/targeted/shared item from all visible views and roles.
- **Expected:** Correct action once, correct history/aggregate effect, no false capability.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress status-action inventory.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-011 — List/history search/filter/paging work if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; NFR-04
- **Execute:** Use every visible filter/search/sort/page control, combine/reset/refresh/back.
- **Expected:** Result matches wording; no stale/foreign/private result.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress filter/pagination suite or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## B. FR-10 / FR-18 extensions and strict negative/reliability paths

### E2E-012 — Suggestion/voting workflow if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10
- **Execute:** Create suggestion, vote/unvote, use two users, convert to item if delivered, refresh/retry.
- **Expected:** Rules/visibility/counts follow documented behavior; one vote identity per user.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress suggestion-vote workflow or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-013 — Suggestion/voting membership regression if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10; BUG #35; BUG #38
- **Execute:** Use existing-invited then leave/rejoin account and vote/suggest.
- **Expected:** No duplicated vote/candidate/count/history/500.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Cypress regression or N/A with delivery proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-014 — QR scanner one-click workflow if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18
- **Execute:** Use real valid QR payload/camera route, preview/correct/confirm/cancel, inspect generated item.
- **Expected:** Only confirmed valid scan creates one correct structured item.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress scanner workflow or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-015 — QR invalid/retry/unit safety if delivered — BUG #36
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; BUG #36; NFR-03
- **Execute:** Use malformed/unknown/invalid quantity-unit QR payloads, deny camera, retry after network interruption.
- **Expected:** No invalid/duplicate item or free-text unit/quantity; safe fallback/error.
- **PASS evidence required:** Video/console/API evidence.
- **Automated proof required:** Mandatory Cypress QR regression or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-016 — All shopping controls work in integrated app
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04; explicit user requirement
- **Execute:** Inventory all catalogue/form/quantity/unit/visibility/target/status/history/vote/scanner buttons/icons/links/menus/dialogs across states.
- **Expected:** No dead control/false success; disabled/hidden state clear and correct.
- **PASS evidence required:** Completed inventory/video.
- **Automated proof required:** Cypress control-inventory suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-017 — Negative error matrix is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03; NFR-04
- **Execute:** Trigger validation, 401, 403, 404, 409, 500, offline, timeout, stale target/status and scanner errors.
- **Expected:** Actionable safe UX; no stack/SQL/secret/private/foreign data/false success.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Cypress error-mapping matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-018 — Two-household shopping isolation works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Use WG A/B, direct URLs, tampered IDs, browser history and cross-user item/status/vote/scanner actions.
- **Expected:** No foreign item/history/catalogue/suggestion/QR data or mutation.
- **PASS evidence required:** Video/network/DB evidence.
- **Automated proof required:** Cypress two-household security suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-019 — Rapid shopping actions and multi-tab races are safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Double-submit item, mark/unmark, target change, vote, QR scan, delete from two tabs/users.
- **Expected:** Final canonical state valid; no duplicate item/status/history/vote/500.
- **PASS evidence required:** Video/request trace.
- **Automated proof required:** Cypress reconciliation suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-020 — Restart/reload/seed restore preserves shopping state
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03; TG-05
- **Execute:** Create/status-change/target item, restart/reset per docs, inspect aggregate/history/extensions.
- **Expected:** State survives/restores as documented; no duplicate/lost history/state.
- **PASS evidence required:** Commands/video/API evidence.
- **Automated proof required:** Clean-environment/restart smoke.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-021 — Audit and privacy lifecycle work with shopping records
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; NFR-06; TG-07
- **Execute:** Create/target/status/cancel item, vote/scan if delivered, leave/delete/anonymize fixture, inspect audit/history.
- **Expected:** Required actor/time trace; privacy-safe fallback, no secret/PII leak.
- **PASS evidence required:** Audit/before-after evidence.
- **Automated proof required:** Integration + Cypress linkage.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-022 — No shopping/QR secrets or PII leak in logs
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02
- **Execute:** Capture browser console/backend logs in normal and failing personal/targeted/QR flows.
- **Expected:** No passwords/JWT/raw token/raw QR/image/excess targeted history/private data.
- **PASS evidence required:** Redacted logs.
- **Automated proof required:** Console guard + log scan.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-023 — Whole-project found-bug gate remains green
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** Bugs #31/#32/#35/#36/#37/#38/#39/#42
- **Execute:** Run all focused regression tests after UC-04 integration.
- **Expected:** All found bugs remain fixed; names/commands/output recorded.
- **PASS evidence required:** CI/test output.
- **Automated proof required:** Mandatory regression gate.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## C. All applicable NFRs — usability, accessibility, compatibility, documentation, CI and final release gate

### E2E-024 — Peer tester completes core shopping workflow unaided
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Ask non-implementer to add structured item with quantity/unit, choose visibility/target, mark/unmark and find history; use extensions if delivered.
- **Expected:** Peer succeeds; confusion/blockers fixed or documented.
- **PASS evidence required:** Peer test record.
- **Automated proof required:** Manual acceptance evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-025 — WAI/Lighthouse/axe shopping quality gate
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Audit catalogue autocomplete, item form, quantity/unit, visibility/target controls, list/history, suggestions and scanner.
- **Expected:** No critical accessibility blocker; findings resolved/escalated.
- **PASS evidence required:** Audit reports.
- **Automated proof required:** Repeatable axe/Lighthouse run.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-026 — Keyboard-only shopping workflow works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Complete core shopping flow including autocomplete, quantity/unit, target, status, history filters and scanner fallback via keyboard only.
- **Expected:** No mouse/camera-only blocker; focus/dialogs/autocomplete safe.
- **PASS evidence required:** Video.
- **Automated proof required:** Manual + automated accessibility proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-027 — Reference browser/device matrix passes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04; TG-08
- **Execute:** Run core shopping flow on Chrome/Firefox desktop 1366×768+, iPhone 12 Pro Safari 390×844, Pixel 7 Chrome 412×915 and 360px.
- **Expected:** No clipped quantity/unit/target controls, overflow, keyboard/touch/browser blocker.
- **PASS evidence required:** Screenshots/video/browser versions.
- **Automated proof required:** Cypress viewport + manual-device tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-028 — Zoom/text scaling is usable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Run primary shopping operations at 200% zoom/text scale.
- **Expected:** Critical quantity/unit/visibility/actions remain visible and reachable.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Manual accessibility evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-029 — OpenAPI and browser behavior agree
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-04
- **Execute:** Compare shopping network calls/errors to served OpenAPI across core flows.
- **Expected:** No undocumented/deprecated mismatch.
- **PASS evidence required:** Network/OpenAPI proof.
- **Automated proof required:** Contract suite plus manual check.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-030 — CI executes all UC-04 layers
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-05; TG-09
- **Execute:** Review/run pipeline after UC-04 test change.
- **Expected:** Frontend/backend/repository/integration/E2E shopping layers run and failure blocks merge.
- **PASS evidence required:** Pipeline evidence.
- **Automated proof required:** CI verification.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-031 — Shopping setup and user docs match working flow
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-10; NFR-05
- **Execute:** Follow docs from clean environment for catalogue/quantity-unit/target/history/vote/scanner behavior.
- **Expected:** No obsolete steps/false structured-unit/scanner/voting claim/missing prerequisite.
- **PASS evidence required:** Execution record.
- **Automated proof required:** Documentation acceptance test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-032 — Final integrated requirement sign-off
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** All FR-07/FR-08/FR-09/FR-10/FR-18 and NFR-01..06
- **Execute:** Review all three UC-04 forms, evidence, commands and bug gates before merge.
- **Expected:** No hidden critical failure/evidence gap; limitations remain visible.
- **PASS evidence required:** Signed review summary.
- **Automated proof required:** Reviewer sign-off.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

