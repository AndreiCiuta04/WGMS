# UC-04 — BACKEND QA FORM

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

**Spring Boot shopping API, structured catalogue and quantity/unit model, visibility targeting, status/history aggregation, vote and QR extensions, H2/JPA, security, transactions, OpenAPI, CI and auditability.**

This form owns server-side enforcement for FR-07, FR-08, FR-09, FR-10 and FR-18 plus NFR-01, NFR-02, NFR-03, NFR-05 and NFR-06.


## A. FR-07 — Structured shopping item creation, catalogue and quantity/unit validation

### BE-001 — All shopping endpoints require valid authentication
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-01
- **Execute:** Enumerate every shopping/catalogue/history endpoint; call each without/malformed/expired JWT.
- **Expected:** Safe 401 and no shopping data/mutation.
- **PASS evidence required:** Endpoint matrix/responses.
- **Automated proof required:** Parameterized security integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-002 — Shopping API matches OpenAPI contract
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; TG-04
- **Execute:** Compare paths, methods, security, DTOs, status/error schemas to served OpenAPI.
- **Expected:** Implementation and spec agree.
- **PASS evidence required:** Spec/API comparison.
- **Automated proof required:** Contract tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-003 — Shopping item create DTO validates all required fields
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-03
- **Execute:** Submit missing/blank/boundary/malformed name/catalogue/quantity/unit/visibility/target values directly to API.
- **Expected:** Controlled 4xx, no persisted item and no 500.
- **PASS evidence required:** Responses/DB assertions.
- **Automated proof required:** Parameterized endpoint tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-004 — Structured catalogue reference is validated server-side
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-01; NFR-03
- **Execute:** Submit known/unknown/deleted/foreign/malformed catalogue IDs or names according to actual model.
- **Expected:** Only valid catalogue entries/fallback policy accepted; no arbitrary hidden bypass.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Repository/service/integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-005 — Quantity is numeric and precise — BUG #36
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #36; FR-07; NFR-03
- **Execute:** Submit zero, negative, decimal, huge, locale/scientific/text/string values and boundary scale directly to API.
- **Expected:** Only authoritative numeric quantity model is accepted; arbitrary free-text values cannot persist.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Mandatory named validation regression for BUG #36.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-006 — Unit/type is validated as structured model — BUG #36
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #36; FR-07; NFR-03
- **Execute:** Inspect actual unit enum/catalogue/relation, submit every valid unit and invalid/free-text/tampered units.
- **Expected:** Only supported canonical unit values persist; no free-text ambiguity or silent fallback outside documented policy.
- **PASS evidence required:** Enum/API/DB proof.
- **Automated proof required:** Mandatory named unit validation regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-007 — Quantity + unit preserve canonical value across persistence — BUG #36
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #36; FR-07; FR-09
- **Execute:** Create valid item for every supported unit/quantity boundary, read list/history/aggregate via API.
- **Expected:** Stored and returned canonical values are identical and unambiguous; no locale/format corruption.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Repository/integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-008 — Shopping item derives household/creator from authenticated principal
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-01; TG-01
- **Execute:** Inject householdId, creatorId, ownerId, role or history fields into request.
- **Expected:** Server ignores/rejects forged authority; item belongs to actor’s active household.
- **PASS evidence required:** Request/DB evidence.
- **Automated proof required:** Authorization/mass-assignment tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-009 — Create item is atomic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Force failure after item entity/before visibility target/audit/catalogue link persistence.
- **Expected:** No partial item/target/audit state remains.
- **PASS evidence required:** DB assertions.
- **Automated proof required:** Transactional test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-010 — Duplicate create retries are safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Send repeated/concurrent identical create requests and simulate retry.
- **Expected:** Behavior is intentional; no accidental duplicate/constraint 500.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Concurrency/idempotency tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-011 — Shopping item response is privacy-minimized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; TG-04
- **Execute:** Inspect success/error DTOs and nested user/catalogue/target fields.
- **Expected:** No password/JWT/raw invite/internal security/audit fields/excess PII.
- **PASS evidence required:** Response inventory.
- **Automated proof required:** Serialization/contract test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-012 — Shopping create errors are sanitized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Trigger validation/auth/constraint/unexpected failures.
- **Expected:** Safe status/body; no SQL/stack/class/secret.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception-handler tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## B. FR-08 — Visibility and targeted requests

### BE-013 — Visibility enum is fully validated
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-08; NFR-03
- **Execute:** Inspect actual ShoppingVisibility enum/API and submit each valid/invalid value.
- **Expected:** All supported modes accepted; invalid values safely reject; no silent coercion.
- **PASS evidence required:** Enum/API/DB evidence.
- **Automated proof required:** Parameterized enum tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-014 — Personal visibility is enforced server-side
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-08; NFR-01; NFR-02
- **Execute:** Create personal item as user A; read/list as A, same-WG B, Main Tenant and foreign user.
- **Expected:** Only authorized actor sees it under policy; no leakage.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** IDOR/privacy integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-015 — Targeted visibility requires eligible current-household target
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-08; NFR-01; NFR-03
- **Execute:** Submit TARGETED with foreign, former, deleted/anonymized, rejoined, duplicate and nonexistent target.
- **Expected:** Only eligible active target persists; no invalid target relation.
- **PASS evidence required:** Responses/DB matrix.
- **Automated proof required:** Validation/repository/integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-016 — Targeted visibility is enforced for creator/target/others
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-08; NFR-01; NFR-02
- **Execute:** Create A→B targeted item; read/list as A/B/C and foreign user.
- **Expected:** Only authorized parties see it exactly as documented; no unnecessary target leak.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Targeted-visibility integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-017 — Shared visibility is enforced for current household only
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; FR-08; NFR-01
- **Execute:** Create shared item; list as every member/current and foreign user.
- **Expected:** All intended household members see it; no foreign access.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Shared-visibility integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-018 — Existing-user invite does not duplicate target relation — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-08; NFR-03
- **Execute:** Invite existing account, create targeted item, inspect target rows/read result.
- **Expected:** Exactly one logical target/visibility result; no duplicate rows/500.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-019 — Leave/rejoin does not duplicate target relation — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-08; NFR-03
- **Execute:** Create target history, leave/rejoin user, create/read/update targeted item.
- **Expected:** No duplicate target/history/visibility result or 500.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory repository + integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-020 — Deleted/anonymized target history is privacy-safe — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-08; NFR-02; NFR-03
- **Execute:** Create targeted/personal item, delete/anonymize member, retrieve active/history items.
- **Expected:** No FK/null error; no PII leak; fallback follows policy.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory cross-feature integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-021 — Visibility/target updates enforce same invariants
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-08; NFR-01; NFR-03
- **Execute:** Update PERSONAL↔TARGETED↔SHARED and target; attempt forged foreign/invalid transitions.
- **Expected:** Correct visibility persists; no transient/leaked relation; invalid transition rolls back.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Service/integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-022 — Visibility changes are auditable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-06
- **Execute:** Create/update item visibility/target and inspect audit.
- **Expected:** Actor/time/action trace exists without exposing private content unnecessarily.
- **PASS evidence required:** Audit evidence.
- **Automated proof required:** Integration audit tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## C. FR-07 / FR-09 — Status transitions, aggregation and history

### BE-023 — Shopping status enum is completely validated
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; FR-09
- **Execute:** Inspect `NEEDED`, `BOUGHT`, `CANCELLED`; submit every valid/invalid status action.
- **Expected:** All valid states serialize correctly; invalid status cannot persist.
- **PASS evidence required:** Enum/API proof.
- **Automated proof required:** Parameterized status tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-024 — Only valid shopping status transitions are permitted
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-03
- **Execute:** Attempt every status transition, including direct tampering, mark/unmark/cancel/reopen actions.
- **Expected:** Only documented transitions work; invalid transition rejects safely.
- **PASS evidence required:** Transition matrix/DB evidence.
- **Automated proof required:** Service state-machine tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-025 — Mark-as-bought is atomic and auditable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; FR-08; NFR-03; NFR-06
- **Execute:** Mark item bought and inspect status, actor/time, aggregate/history effects.
- **Expected:** One canonical BOUGHT state; correct trace; no duplicated history side effect.
- **PASS evidence required:** DB/audit/API evidence.
- **Automated proof required:** Integration test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-026 — Unmark-as-bought is atomic and auditable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; FR-08; NFR-03; NFR-06
- **Execute:** Mark then unmark; inspect status/history/aggregate.
- **Expected:** Correct NEEDED state restored per policy; trace exists; no duplicate item/history.
- **PASS evidence required:** DB/audit evidence.
- **Automated proof required:** Mandatory integration undo/unmark test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-027 — Status action permissions are server-enforced
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; FR-08; NFR-01
- **Execute:** Test creator, target, other roommate, Main Tenant, former and foreign actors against mark/unmark/cancel/delete.
- **Expected:** Only documented actor succeeds; no client-only permission.
- **PASS evidence required:** Response/DB matrix.
- **Automated proof required:** Role-based integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-028 — Concurrent mark/unmark actions remain consistent
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Send concurrent state changes from same/different allowed actors.
- **Expected:** Final state follows transaction/order policy; no duplicate audit/history/500.
- **PASS evidence required:** Concurrency output/DB.
- **Automated proof required:** Concurrent integration test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-029 — Cancel/delete policy preserves aggregate/history integrity
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; FR-09; NFR-03
- **Execute:** Cancel/delete items in NEEDED/BOUGHT/TARGETED/SHARED states and inspect list/history.
- **Expected:** No orphan/ghost item; policy is consistent and safe.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Integration tests or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-030 — Aggregate query includes exactly intended visible items
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; NFR-01; NFR-02
- **Execute:** Create personal/targeted/shared items by several users; call aggregate/list as each actor.
- **Expected:** Response follows visibility policy exactly; no private/foreign leak/missing shared data.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Aggregation/privacy integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-031 — Aggregate preserves structured quantity/unit — BUG #36
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #36; FR-09; NFR-03
- **Execute:** Create same/different catalogue item with valid units/quantities; query aggregate.
- **Expected:** Aggregation/grouping preserves canonical unit and quantity; no free-text ambiguity/incorrect merge.
- **PASS evidence required:** API/DB evidence.
- **Automated proof required:** Mandatory aggregation regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-032 — Aggregate status inclusion rules are correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; FR-07
- **Execute:** Create NEEDED/BOUGHT/CANCELLED fixtures; query active/aggregate/history endpoints.
- **Expected:** Only intended state belongs in each output; no stale/ghost item.
- **PASS evidence required:** Response/DB comparison.
- **Automated proof required:** Integration state aggregation tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-033 — Shopping history is durable and lifecycle-correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; NFR-03
- **Execute:** Create item, change target/visibility, mark/unmark/cancel/delete as supported, then retrieve history after restart.
- **Expected:** History reflects canonical lifecycle without duplicates/missing rows; persists correctly.
- **PASS evidence required:** DB/API/restart evidence.
- **Automated proof required:** Integration/repository tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-034 — History respects visibility and household boundaries
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; NFR-01; NFR-02
- **Execute:** Query history as creator/target/other member/foreign/former user across two WGs.
- **Expected:** Only permitted history returned; no targeted/personal/foreign leak.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Privacy/IDOR integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-035 — History handles deleted/anonymized members safely — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-09; NFR-02
- **Execute:** Delete/anonymize creator/target/buyer and retrieve history.
- **Expected:** Privacy-safe reference retained; no PII/FK/null error; policy documented.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory cross-feature regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-036 — History filter/query inputs validate safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; NFR-01; NFR-03
- **Execute:** Fuzz status/visibility/date/user/catalogue/search/sort/page parameters.
- **Expected:** No SQL/parser error/broad data leak; controlled result.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Parameterized tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-037 — History/status actions create audit trace
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-06
- **Execute:** Create/target/mark/unmark/cancel/delete item and inspect audit.
- **Expected:** Actor/time/action trace exists; no full private payload/secret.
- **PASS evidence required:** Audit evidence.
- **Automated proof required:** Integration audit tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## D. FR-10 — Suggestions and voting backend

### BE-038 — FR-10 backend delivery state is explicit
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10 extended; NFR-05
- **Execute:** Locate endpoints/services/entities/docs/OpenAPI. If absent/prototype, validate truthful limitation.
- **Expected:** No false production claim; actual contract is identifiable.
- **PASS evidence required:** Code/docs/OpenAPI evidence.
- **Automated proof required:** Test or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-039 — Suggestion create is household-scoped and validated
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10; NFR-01; NFR-03
- **Execute:** Create as current/foreign/former/unauthenticated actor; submit blank/duplicate/invalid structured values.
- **Expected:** Only valid current household suggestion persists; no cross-WG data.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Integration validation/security tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-040 — Vote identity is unique and concurrency-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10; NFR-01; NFR-03
- **Execute:** Vote repeatedly/concurrently, across tabs/actors/rejoined user and foreign user.
- **Expected:** One vote per documented voter/item rule; no duplicate count/race/500.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Repository + concurrency integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-041 — Existing invited user does not duplicate vote — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-10; NFR-03
- **Execute:** Invite existing account, vote/suggest and inspect identity/count.
- **Expected:** Exactly one voter identity/count contribution.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-042 — Rejoined user does not duplicate vote — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-10; NFR-03
- **Execute:** Vote/suggest, leave/rejoin, inspect state and re-vote under policy.
- **Expected:** No duplicate vote/count/history/500.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-043 — Suggestion/vote reads enforce household privacy
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10; NFR-01; NFR-02
- **Execute:** Read suggestion/vote info as current/foreign/former actors.
- **Expected:** No foreign data/unnecessary voter PII; current policy is enforced.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** IDOR/privacy tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-044 — Convert/accept suggestion to item is atomic if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10; FR-07; NFR-03
- **Execute:** Convert suggestion; force failure/retry/concurrent conversion; inspect item/vote/suggestion state.
- **Expected:** Exactly intended structured item once; no partial conversion/duplicate.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Transactional integration test or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-045 — Suggestion/vote actions are auditable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10; NFR-06
- **Execute:** Create/vote/unvote/convert as delivered; inspect audit.
- **Expected:** Actor/time/action trace correct and privacy-safe.
- **PASS evidence required:** Audit evidence.
- **Automated proof required:** Integration audit test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## E. FR-18 — QR scanner backend, payload validation and one-click creation

### BE-046 — FR-18 backend delivery state is explicit
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18 extended; NFR-05
- **Execute:** Locate scanner controller/service/OpenAPI/docs. If absent/prototype, validate truthful limitation.
- **Expected:** No false scanner claim; actual contract identifiable.
- **PASS evidence required:** Code/docs/OpenAPI evidence.
- **Automated proof required:** Test or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-047 — QR scanner endpoint requires authenticated household context
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; NFR-01
- **Execute:** Call scanner endpoint unauthenticated, invalid/expired, foreign/former/current actor.
- **Expected:** No shopping context/mutation outside current WG; safe 401/403.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Security integration test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-048 — QR payload is validated server-side before item creation
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; NFR-01; NFR-03
- **Execute:** Submit real delivered payload types: valid, malformed, unknown, oversized, invalid catalogue, invalid quantity/unit, hostile input.
- **Expected:** No invalid shopping item persists; safe controlled response.
- **PASS evidence required:** Responses/DB/log evidence.
- **Automated proof required:** Mandatory payload-validation tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-049 — QR-created item obeys structured quantity/unit model — BUG #36
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #36; FR-18; FR-07
- **Execute:** Use valid/invalid unit and quantity QR fixtures including free-text unit/quantity.
- **Expected:** Only canonical numeric quantity + structured unit becomes item; invalid payload is rejected.
- **PASS evidence required:** API/DB proof.
- **Automated proof required:** Mandatory named QR BUG #36 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-050 — QR one-click creation is transactional and retry-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; NFR-03
- **Execute:** Force failure after parse/before item persistence; retry/concurrent same scan.
- **Expected:** No partial/duplicate item; canonical result or safe error.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Transactional/idempotency tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-051 — QR payload/image data is privacy-minimized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; NFR-02
- **Execute:** Inspect storage, logs, temp files, DTOs and audit records for raw scanned payload/image.
- **Expected:** Only necessary data retained; no raw payload/image/secret in logs by default.
- **PASS evidence required:** Code/DB/log proof.
- **Automated proof required:** Privacy/log-scan test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-052 — Manual confirmation/correction state is respected
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; FR-07
- **Execute:** Submit corrected/cancelled/non-confirmed flow as actual API design permits.
- **Expected:** Only valid confirmed final data creates item; no hidden mutation.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Integration workflow test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-053 — QR errors are sanitized and OpenAPI-correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; TG-04; NFR-01
- **Execute:** Trigger parse/auth/validation/provider-like failures supported by implementation.
- **Expected:** Safe response; no stack/secret/raw payload; spec matches.
- **PASS evidence required:** Responses/OpenAPI/logs.
- **Automated proof required:** Contract/exception tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## F. All applicable NFRs — security, privacy, reliability, maintainability, CI and bug gate

### BE-054 — Complete UC-04 authorization matrix exists
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Enumerate item/catalogue/visibility/status/history/suggestion/vote/scanner routes and classify public/auth/member/role/state restrictions.
- **Expected:** No route unclassified; test coverage for each class.
- **PASS evidence required:** Matrix evidence.
- **Automated proof required:** Parameterized security suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-055 — Every shopping read endpoint blocks cross-WG access
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Use WG A actor against all WG B item/history/catalogue/suggestion/QR resources.
- **Expected:** No foreign data/existence leak.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** Security integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-056 — Every shopping mutation blocks cross-WG writes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Use WG A actor against WG B item/status/visibility/vote/scan resources.
- **Expected:** No mutation and DB unchanged.
- **PASS evidence required:** Endpoint/DB matrix.
- **Automated proof required:** Security suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-057 — All shopping DTOs block mass assignment
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Inject IDs, household, creator, target, visibility, status, history, votes, audit, quantity/unit fields outside allowed contract.
- **Expected:** No unauthorized ownership/state/visibility change.
- **PASS evidence required:** Request/DB evidence.
- **Automated proof required:** Mass-assignment tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-058 — Hostile inputs fail safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Fuzz IDs, unit/quantity, enum/state, QR payload, dates, search/sort/page and long strings.
- **Expected:** No parser/SQL error/500/broad shopping leak.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Parameterized tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-059 — Shopping logs/audit/responses are privacy-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; NFR-06
- **Execute:** Run normal/failing personal/targeted/shared/vote/QR flows; inspect logs/audit/DTOs.
- **Expected:** No password/JWT/raw token/excess targeted/personal history/raw QR/image PII.
- **PASS evidence required:** Redacted proof.
- **Automated proof required:** Log scan/static review.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-060 — Account lifecycle preserves required shopping history safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; TG-07; BUG #42
- **Execute:** Delete/anonymize creator/target/voter/QR actor and inspect current/history references.
- **Expected:** Privacy-safe fallback preserves usable history; no FK/null/PII leak.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Lifecycle integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-061 — Critical shopping transactions are atomic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Review/fault-inject create/update/status/visibility/history/vote/scan conversion actions.
- **Expected:** No partial item/target/status/vote/audit state.
- **PASS evidence required:** Code/DB evidence.
- **Automated proof required:** Rollback tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-062 — Concurrency coverage exists for shopping races
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Run concurrent create, mark/unmark, visibility changes, vote/unvote, QR scan, delete.
- **Expected:** Final state valid; no duplicate item/status/history/vote/500.
- **PASS evidence required:** Output/DB proof.
- **Automated proof required:** Concurrent integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-063 — Seed/reset is reproducible with UC-04 fixtures
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03; TG-05
- **Execute:** From clean checkout run documented reset/seed and verify catalogue, units, personal/targeted/shared items, histories and extended fixtures.
- **Expected:** Known synthetic state restored with no hidden repair.
- **PASS evidence required:** Commands/DB/API evidence.
- **Automated proof required:** Seed smoke test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-064 — Shopping persistence survives restart
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Create/status-change item, restart as documented, inspect lists/history/aggregate/votes.
- **Expected:** State survives/restores accurately; no duplicate history/status.
- **PASS evidence required:** Commands/API/DB evidence.
- **Automated proof required:** Restart integration test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-065 — Repository tests cover critical shopping queries
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03; QA strategy
- **Execute:** Locate/run repository tests for household scope, visibility/target lookup, status/history, catalogue/unit, vote uniqueness, former-user fallback.
- **Expected:** Real DB semantics proven.
- **PASS evidence required:** Test paths/output.
- **Automated proof required:** Mandatory repository evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-066 — Service tests cover core shopping rules
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-05; TG-09
- **Execute:** Locate/run unit tests for structured quantity/unit, visibility, transitions, aggregate/history, vote and QR validation.
- **Expected:** Rules tested independently of UI/E2E.
- **PASS evidence required:** Test paths/output.
- **Automated proof required:** Mandatory service test evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-067 — API integration tests cover complete shopping workflows
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-09
- **Execute:** Run real security/database workflows for create/target/status/history/aggregate and delivered extensions.
- **Expected:** Security and persistence proven together.
- **PASS evidence required:** Output.
- **Automated proof required:** Mandatory integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-068 — OpenAPI is served and current
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-04
- **Execute:** Start application and retrieve Swagger/OpenAPI; inspect UC-04 operations.
- **Expected:** Specification available/current.
- **PASS evidence required:** URL/output.
- **Automated proof required:** OpenAPI smoke/contract test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-069 — CI runs all critical UC-04 test layers
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-05; TG-09
- **Execute:** Inspect CI profiles/exclusions/allow_failure and pipeline execution.
- **Expected:** Unit/repository/integration/frontend/E2E shopping tests run and failures block merge.
- **PASS evidence required:** CI YAML/job proof.
- **Automated proof required:** CI verification.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-070 — Architecture boundaries are clean
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-01; NFR-05
- **Execute:** Trace controllers → services → repositories/mappers and Angular boundary for shopping flows.
- **Expected:** No DB access outside repositories; no business rules improperly in controller/UI; one source of truth.
- **PASS evidence required:** Architecture map.
- **Automated proof required:** Code review plus tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-071 — DTO mappers exclude internal fields
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; TG-04
- **Execute:** Inspect ShoppingItem/catalogue/history/vote/QR serialization.
- **Expected:** No internal security/audit/target data beyond contract.
- **PASS evidence required:** Code/response proof.
- **Automated proof required:** Serialization tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-072 — Shopping setup/API/user docs are current
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-10; NFR-05
- **Execute:** Follow docs for catalogue/quantity/unit/visibility/history/FR10/FR18 behavior.
- **Expected:** No obsolete rule/false structured-unit/scanner/voting claim.
- **PASS evidence required:** Documentation execution record.
- **Automated proof required:** Manual doc acceptance test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-073 — All known-bug backend regressions are green
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** Bugs #31/#32/#35/#36/#37/#38/#39/#42
- **Execute:** Run focused backend/integration tests for every found bug after UC-04 changes.
- **Expected:** All remain green; test names/commands recorded.
- **PASS evidence required:** Output.
- **Automated proof required:** Mandatory regression gate.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-074 — Final backend sign-off is evidence-complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** All applicable FR/NFR/TG
- **Execute:** Review every BE item and automated proof.
- **Expected:** No critical PASS lacks proof; unresolved item remains visible.
- **PASS evidence required:** Signed review.
- **Automated proof required:** Reviewer sign-off.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

