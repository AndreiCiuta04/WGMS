# UC-04 — FRONTEND QA FORM

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

**Angular/browser layer: structured shopping catalogue, quantity/unit behavior, personal/targeted/shared visibility, mark/unmark, aggregation/history, suggestions/votes, QR scanner, every control, accessibility and responsive behavior.**

This form owns visible behavior for FR-07, FR-08, FR-09, FR-10 and FR-18 plus all UI-facing NFR-01..NFR-06 proof.


## A. FR-07 — Structured shopping item creation, catalogue and quantity/unit handling

### FE-001 — Shopping routes are protected and household-scoped
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-01; NFR-04
- **Execute:** Open shopping/list/history routes as logged out, authenticated no-WG, Main Tenant, Roommate, former member and direct URL state.
- **Expected:** Only current household members reach correct shopping context; no stale/foreign item flash.
- **PASS evidence required:** Video, route matrix, console capture.
- **Automated proof required:** Route-guard tests plus Cypress route matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-002 — Shopping lists are household-scoped in UI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-01
- **Execute:** Use two households with distinct shared/personal/targeted fixtures, switch users, refresh and use browser history.
- **Expected:** Only items visible to current user in current household appear; no cached foreign item/history.
- **PASS evidence required:** Screenshots/API comparison.
- **Automated proof required:** Component/service test plus two-household Cypress test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-003 — Add-item form matches current API/OpenAPI contract
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; TG-04
- **Execute:** Compare all visible form fields, labels, types, required indicators and submitted JSON with current DTO/OpenAPI.
- **Expected:** All server-required fields are usable and correctly serialized; no stale client field blocks valid creation.
- **PASS evidence required:** Screenshot, redacted request, OpenAPI reference.
- **Automated proof required:** Component/client-service request-shape test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-004 — Every shopping form control has accessible labels/help
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-04/WAI
- **Execute:** Inspect labels, accessible names, required state, descriptions and error links for catalogue item, name, quantity, unit, visibility, target and all live fields.
- **Expected:** No placeholder-only/unlabeled control; every error is programmatically associated.
- **PASS evidence required:** Accessibility tree/axe evidence.
- **Automated proof required:** Component accessibility test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-005 — Structured catalogue autocomplete works correctly
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-04
- **Execute:** Search/select common catalogue items using prefix, case, whitespace, no match, fast typing and keyboard selection.
- **Expected:** Relevant results appear safely; selected item populates intended structured fields; no stale selection or duplicate insertion.
- **PASS evidence required:** Video/screenshots.
- **Automated proof required:** Component autocomplete tests plus Cypress keyboard flow.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-006 — Manual item creation behavior is truthful
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-04
- **Execute:** Inspect whether manual names are allowed, restricted to catalogue, or offered as fallback. Try known/unknown/manual entries.
- **Expected:** UI follows actual product policy; no false claim of a structured catalogue if arbitrary free-text silently bypasses it.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Component validation test or documented N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-007 — Required item-name/catalogue validation is complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-04
- **Execute:** Submit empty, whitespace-only, min/max/boundary, unknown and malformed item values according to delivered model.
- **Expected:** Invalid item cannot be submitted; clear accessible error; no phantom item.
- **PASS evidence required:** Screenshots/network capture.
- **Automated proof required:** Parameterized form tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-008 — Quantity input is numeric/structured and validates boundaries — BUG #36
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #36; FR-07; NFR-03
- **Execute:** Inspect authoritative UI model; test empty, zero, negative, decimal, huge, locale comma/dot, scientific notation, symbols and free-text such as `a lot`, `one-ish`, `2 packs maybe`.
- **Expected:** Only valid numeric quantity under the contract model is accepted; arbitrary free text cannot masquerade as quantity.
- **PASS evidence required:** Screenshots/request/response.
- **Automated proof required:** Mandatory named component/Cypress BUG #36 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-009 — Unit/type is a structured selection, not a free-text ambiguity — BUG #36
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #36; FR-07; NFR-04
- **Execute:** Inspect unit field; test all supported units, keyboard selection, invalid manual entry/tampering, unit absence and rendered list/history values.
- **Expected:** Unit comes from authoritative controlled set/current API model; unsupported text is rejected; valid unit persists and displays consistently.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Mandatory named unit-enum/select regression for BUG #36.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-010 — Quantity and unit render unambiguously in all item views — BUG #36
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #36; FR-07; FR-09
- **Execute:** Create items using every supported unit and quantity boundary; inspect personal/shared/targeted list, aggregate and history.
- **Expected:** No concatenation ambiguity, loss of unit, free-text fallback or locale confusion; UI reflects canonical stored values.
- **PASS evidence required:** Screenshots/API comparison.
- **Automated proof required:** Component formatting tests plus Cypress end-to-end regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-011 — Item creation action prevents duplicate submits
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Double-click/Enter repeatedly under slow network; reload/back during request.
- **Expected:** Only one logical shopping item is created; pending state visible; no duplicate row/toast.
- **PASS evidence required:** Video/request count.
- **Automated proof required:** Pending-state component test + Cypress duplicate-submit test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-012 — Shopping item creation error recovery is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03; NFR-04
- **Execute:** Simulate 400/401/403/404/409/500/offline/timeout and retry after correction.
- **Expected:** Safe actionable error; no raw stack/SQL; no phantom item; fields recover where safe.
- **PASS evidence required:** Screenshots/video/network.
- **Automated proof required:** Service/component error mapping tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-013 — Created item appears consistently after refresh
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-03
- **Execute:** Create structured item, inspect relevant lists/history state, refresh and navigate back.
- **Expected:** Exactly one item with correct name/quantity/unit/visibility/status appears.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress create-item happy path.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-014 — Item name/text rendering is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-02
- **Execute:** Use Unicode, long, markup-like, special-character and multiline display names if manual/fallback names exist.
- **Expected:** Allowed content displays safely; no script/HTML rendering or layout break.
- **PASS evidence required:** DOM screenshots/API evidence.
- **Automated proof required:** Sanitization/validation component tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## B. FR-07 / FR-08 — Visibility, targeting, personal/shared requests and status changes

### FE-015 — Visibility selector exposes all delivered modes correctly
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; FR-08
- **Execute:** Inspect UI/API visibility enum and render PERSONAL, TARGETED and SHARED fixtures.
- **Expected:** Every supported mode appears once, has understandable meaning and maps to correct payload.
- **PASS evidence required:** Screenshots/payload.
- **Automated proof required:** Component enum-mapping tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-016 — Personal item visibility is correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-08; NFR-01; NFR-02
- **Execute:** Create personal item as user A; inspect A, same-WG B, Main Tenant and foreign WG user views.
- **Expected:** Only creator/authorized actor sees it under actual policy; no household-wide leakage.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Cypress personal-visibility matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-017 — Targeted item requires valid current household recipient
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-08; NFR-01; NFR-03
- **Execute:** Choose TARGETED then test active, foreign, former, deleted/anonymized, rejoined and nonexistent targets.
- **Expected:** Only eligible current target can be selected; no invalid target/submission.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Component target-selector test plus Cypress isolation.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-018 — Targeted item visibility is correct for creator, target and others
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-08; NFR-01; NFR-02
- **Execute:** Create targeted item from A to B; inspect A/B/C and foreign household users.
- **Expected:** Only intended people/roles see item according to delivered policy; no target leak to unrelated user.
- **PASS evidence required:** Screenshots/API comparison.
- **Automated proof required:** Cypress targeted-visibility matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-019 — Shared item visibility is correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; FR-08; NFR-01
- **Execute:** Create shared item; inspect every active member/current and foreign WG users.
- **Expected:** All intended household members see it; no foreign user does.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Cypress shared-visibility test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-020 — Existing invited user does not duplicate target recipient — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-08; NFR-03
- **Execute:** Invite an already-existing account, create targeted item to it, inspect target selector/list/history.
- **Expected:** Recipient appears once and item is visible exactly as policy requires; no duplicate row.
- **PASS evidence required:** Video/API/DB count.
- **Automated proof required:** Mandatory named Cypress BUG #35 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-021 — Rejoined user does not duplicate target recipient — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-08; NFR-03
- **Execute:** Join, leave, rejoin same user; create targeted item and inspect target lists/history.
- **Expected:** User appears once; no duplicate target row/history/visibility error or 500.
- **PASS evidence required:** Video/screenshots/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #38 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-022 — Deleted/anonymized target references render safely — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-08; NFR-02; NFR-03
- **Execute:** Create target/personal history, leave/delete/anonymize relevant member, inspect history/current lists.
- **Expected:** Privacy-safe fallback/no broken item/null error; no removed PII leak.
- **PASS evidence required:** Before/after screenshots.
- **Automated proof required:** Cypress cross-feature regression linked to BUG #42.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-023 — Mark-as-bought control works from every supported list
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; FR-08
- **Execute:** Use mouse/touch/keyboard to mark personal, targeted and shared NEEDED items as bought from all visible screens.
- **Expected:** Correct item changes status once, appears in correct list/history state, and feedback is clear.
- **PASS evidence required:** Video/screenshots.
- **Automated proof required:** Cypress mark-bought workflow.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-024 — Unmark-as-bought works after mistake
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; FR-08
- **Execute:** Mark item bought, undo/unmark it, refresh and inspect all relevant lists/history.
- **Expected:** Item returns to correct NEEDED state without duplicate/lost item; history/status semantics remain coherent.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory Cypress unmark regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-025 — Status-action availability is restricted correctly
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; FR-08; NFR-01
- **Execute:** Compare creator, target, other roommate, Main Tenant, former member and foreign user action UI.
- **Expected:** Only policy-authorized user sees/uses actions; UI never falsely promises ability.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Role-state component/Cypress tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-026 — Rapid mark/unmark state remains coherent
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Click mark/unmark repeatedly under slow network; use two tabs if practical.
- **Expected:** Final UI reconciles canonical server state; no duplicate toast/history/status flip.
- **PASS evidence required:** Video/request trace.
- **Automated proof required:** Cypress race/reconciliation test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-027 — Cancel/delete item actions work safely if exposed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-07; NFR-03
- **Execute:** Inventory destructive actions, test confirmation/cancel/Escape, status/history and actor permission.
- **Expected:** No accidental destructive change; action follows documented policy; aggregate/history remain coherent.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress destructive-action test or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-028 — Visibility/target edits work safely if exposed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-08; NFR-01; NFR-03
- **Execute:** Change PERSONAL/TARGETED/SHARED and target recipient, refresh and inspect affected users.
- **Expected:** Correct visibility changes persist; no temporary leak/stale cache; no foreign target.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress visibility-edit workflow or N/A proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## C. FR-09 — Aggregated household list, filters and shopping history

### FE-029 — Aggregate list includes exactly intended household-visible items
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; NFR-01; NFR-02
- **Execute:** Create personal, targeted and shared items by multiple users; inspect aggregate list from each user.
- **Expected:** Aggregation follows documented visibility rules exactly; no private/foreign item leak or missing intended shared item.
- **PASS evidence required:** Screenshots/API comparison.
- **Automated proof required:** Cypress aggregate-visibility matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-030 — Aggregate list handles quantity/unit combinations correctly — BUG #36
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #36; FR-09
- **Execute:** Create same/different catalogue items with varied valid quantity/unit values; inspect aggregate presentation/grouping.
- **Expected:** No ambiguous free-text quantity; grouping/counting follows delivered business rule and preserves structured unit.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Cypress BUG #36 aggregate regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-031 — Aggregate list status handling is correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; FR-07
- **Execute:** Create NEEDED, BOUGHT and CANCELLED items; mark/unmark/cancel and inspect aggregate.
- **Expected:** Only items/statuses intended for active list appear; no stale bought/cancelled item in wrong view.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Component/Cypress status aggregation test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-032 — Shopping history records correct lifecycle state
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; NFR-03
- **Execute:** Create item, change visibility/target, mark bought, unmark, cancel/delete as supported, then inspect history.
- **Expected:** History reflects canonical lifecycle with correct actor/item/status/time semantics and no duplicates.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress history workflow.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-033 — History remains readable after refresh and navigation
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; NFR-03
- **Execute:** Perform lifecycle actions, refresh, use Back/Forward/direct history route.
- **Expected:** No lost/stale duplicated history; correct filtering context persists.
- **PASS evidence required:** Video.
- **Automated proof required:** Cypress navigation/reload test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-034 — History respects visibility and household boundaries
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; NFR-01; NFR-02
- **Execute:** Use personal/targeted/shared history fixtures across two WGs and multiple roles.
- **Expected:** User sees only permitted history; no private target/foreign history leak.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Cypress history-privacy matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-035 — History handles former/anonymized user safely — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-09; NFR-02
- **Execute:** Leave/delete/anonymize creator/target/buyer of historical item; inspect history from allowed users.
- **Expected:** Privacy-safe label/reference persists; no null crash/PII leak or missing entire history unless documented policy.
- **PASS evidence required:** Before/after screenshots.
- **Automated proof required:** Mandatory Cypress BUG #42 history regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-036 — List/history empty, loading and error states are usable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; NFR-04
- **Execute:** Use no items, no matching filter, all bought, all cancelled, loading and API-error fixtures.
- **Expected:** Clear next action and state explanation; no data leak.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Component state tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-037 — List/history filters, sort and search work if exposed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; NFR-04
- **Execute:** Inventory all controls; combine/reset/refresh/back with status, visibility, item/category/date/user filters.
- **Expected:** Results match UI wording; reset works; no stale/foreign result.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Component/Cypress filter tests or N/A proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-038 — Pagination/infinite-scroll works if exposed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; NFR-03; NFR-04
- **Execute:** Use enough synthetic history; load pages, change filters, refresh and inspect duplicates/missing items.
- **Expected:** Stable ordering; no duplicates/gaps/foreign items; loading state clear.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress pagination test or N/A proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-039 — History export/download is truthful if exposed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-09; NFR-02
- **Execute:** Inventory export controls and use synthetic data; inspect generated data scope.
- **Expected:** Only authorized/allowed data exported; no secrets/foreign/private history; no false claim if absent.
- **PASS evidence required:** Output/screenshots.
- **Automated proof required:** Cypress/manual export test or N/A proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## D. FR-10 — Shopping suggestions and voting UI

### FE-040 — FR-10 delivery status is truthful
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10 extended; NFR-04; NFR-05
- **Execute:** Locate suggestion/voting UI, docs and feature flags.
- **Expected:** Working feature is testable; prototype/absence is documented and not falsely advertised.
- **PASS evidence required:** Screenshots/docs.
- **Automated proof required:** UI/doc test or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-041 — Suggestion entry is household-scoped and validated
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10; NFR-01; NFR-03
- **Execute:** Create suggestions as current, foreign, former and unauthenticated user; test blank/duplicate/structured fields.
- **Expected:** Only current household can create valid suggestion; no cross-WG/invalid suggestion.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Cypress suggestion-create test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-042 — Voting controls are unique per eligible user
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10; NFR-01; NFR-03
- **Execute:** Vote repeatedly, from two tabs, as foreign/former/rejoined/current users.
- **Expected:** One valid vote per documented voter/item rule; no duplicate count/race/foreign vote.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress vote-uniqueness/race test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-043 — Existing invited user does not duplicate vote identity — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-10; NFR-03
- **Execute:** Invite existing account, vote/suggest and inspect count/voter state.
- **Expected:** Exactly one vote identity and correct count; no duplicate participant.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #35 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-044 — Rejoined user does not duplicate vote identity — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-10; NFR-03
- **Execute:** Vote/suggest, leave, rejoin, revisit item and vote state.
- **Expected:** No duplicate vote/history/count or 500; policy is consistent.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory Cypress BUG #38 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-045 — Suggestion/vote visibility is correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10; NFR-01; NFR-02
- **Execute:** Inspect current WG roles, foreign users and private/target-related recommendation context if any.
- **Expected:** Only authorized household users see data; no unnecessary voter PII exposed.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Cypress visibility/security test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-046 — Suggestion conversion/acceptance into shopping item works if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10; FR-07; NFR-03
- **Execute:** Accept/convert a suggestion; inspect generated structured item, status, visibility and duplicate handling.
- **Expected:** Exactly intended item created once; vote history/suggestion state follows policy.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress conversion workflow or N/A proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-047 — FR-10 limitations are documented if incomplete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-10 extended; NFR-04
- **Execute:** Compare UI/docs to actual implementation.
- **Expected:** No false claim of production-complete voting/suggestions if prototype-only.
- **PASS evidence required:** Screenshots/docs.
- **Automated proof required:** Manual documentation review.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## E. FR-18 — QR scanner for one-click shopping entry UI

### FE-048 — FR-18 delivery status is truthful
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18 extended; NFR-04; NFR-05
- **Execute:** Locate scanner UI, docs, feature flags and manual fallback.
- **Expected:** Feature/prototype/absence is stated accurately; no false promise of universal camera support.
- **PASS evidence required:** Screenshots/docs.
- **Automated proof required:** UI/doc test or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-049 — Scanner entry is route/household safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; NFR-01
- **Execute:** Open scanner as logged out, no-WG, current user, former/foreign user.
- **Expected:** Only intended current member can access scanner context; no list data leak.
- **PASS evidence required:** Screenshots/routes.
- **Automated proof required:** Route-guard/Cypress test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-050 — Valid QR scan preview is reviewed before add
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; FR-07; NFR-04
- **Execute:** Use known valid payloads/real delivered camera flow; inspect preview, structured catalogue match, quantity/unit and visibility before confirmation.
- **Expected:** User can verify/correct what will be added; no blind mutation.
- **PASS evidence required:** Video/screenshots/API evidence.
- **Automated proof required:** Cypress scanner-preview flow.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-051 — Invalid/malformed QR scan is safely rejected
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; NFR-01; NFR-03
- **Execute:** Scan malformed, unknown, oversized, invalid-unit and foreign/hostile payloads.
- **Expected:** Clear safe error; no item created; no raw payload/stack leak.
- **PASS evidence required:** Video/network evidence.
- **Automated proof required:** Cypress invalid-QR regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-052 — Scanner respects structured quantity/unit — BUG #36
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #36; FR-18; FR-07
- **Execute:** Scan payloads with valid/invalid quantity and unit values, including free-text unit attempts.
- **Expected:** Only authoritative numeric quantity/unit values become item data; free text is rejected/mapped safely.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #36 scanner regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-053 — Manual correction, cancel and fallback work
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; NFR-04
- **Execute:** Correct parsed fields, cancel before confirmation, retry scan and test unavailable camera/unsupported browser.
- **Expected:** No expense/item created on cancel; correction persists into one valid item; fallback is clear.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress correction/cancel/manual-fallback test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-054 — Scanner cannot duplicate items on retry
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; NFR-03
- **Execute:** Double scan/submit, simulate network interruption after parse/creation and retry.
- **Expected:** No duplicate item; canonical result shown.
- **PASS evidence required:** Video/request count.
- **Automated proof required:** Cypress idempotency/retry regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-055 — Scanner does not leak image/payload/private data
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; NFR-02
- **Execute:** Inspect preview, errors, console, browser storage and sharing UI.
- **Expected:** No raw image/QR payload or unnecessary private data leaks; retention policy is clear.
- **PASS evidence required:** Screenshots/console evidence.
- **Automated proof required:** Static/manual privacy review.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-056 — Scanner has accessible non-camera fallback
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-18; NFR-04/WAI
- **Execute:** Use keyboard/screen reader, permission denial and no-camera device.
- **Expected:** No camera-only dead end; manual entry/fallback and errors are accessible.
- **PASS evidence required:** Video/axe evidence.
- **Automated proof required:** Accessibility/manual device test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## F. All applicable NFRs — every control, accessibility, compatibility, docs and bug gate

### FE-057 — Every UC-04 button, link, icon, menu and dialog works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04; explicit user requirement
- **Execute:** Inventory every shopping/catalogue/visibility/status/history/vote/scanner control for all role/state/modal/error screens; activate each.
- **Expected:** No dead control or false success; disabled/hidden state is clear and accurate.
- **PASS evidence required:** Completed control inventory/video.
- **Automated proof required:** Dedicated Cypress control-inventory spec.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-058 — All shopping UI error messages are safe and actionable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03; NFR-04
- **Execute:** Trigger validation, 401, 403, 404, 409, 500, scanner and visibility errors.
- **Expected:** No SQL/stack/internal ID/secret/private item/foreign data; user gets next step.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Service/component error-mapping tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-059 — No frontend logging leaks shopping secrets or private history
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02
- **Execute:** Search source and capture console/telemetry during normal and negative flows.
- **Expected:** No password/JWT/raw token/excess personal/targeted history/QR payload appears.
- **PASS evidence required:** Static search/redacted console evidence.
- **Automated proof required:** Static scan/manual review.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-060 — WAI/Lighthouse/axe checks pass
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Audit catalogue autocomplete, item form, quantity/unit, visibility target selector, lists/history, voting and scanner dialogs.
- **Expected:** No critical accessibility blocker; findings fixed/escalated.
- **PASS evidence required:** Audit report.
- **Automated proof required:** Repeatable axe/Lighthouse run.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-061 — Keyboard-only shopping workflow works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Add structured item, select unit/visibility/target, mark/unmark, filter history, vote and scanner fallback via keyboard only.
- **Expected:** No mouse/camera-only blocker; focus/dialogs/autocomplete safe.
- **PASS evidence required:** Video.
- **Automated proof required:** Accessibility/E2E proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-062 — Reference browser/device matrix passes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04; TG-08
- **Execute:** Run core shopping flow on Chrome/Firefox 1366×768+, iPhone Safari 390×844, Pixel Chrome 412×915 and 360px.
- **Expected:** No clipped quantity/unit/target control, overflow, keyboard/touch/browser blocker.
- **PASS evidence required:** Screenshots/video/device versions.
- **Automated proof required:** Cypress viewport plus manual-device evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-063 — Zoom/text scaling remains usable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Use 200% zoom/text scaling on catalogue/autocomplete, forms, list/history, vote and scanner fallback.
- **Expected:** Essential actions/quantities/units remain visible and reachable.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Manual accessibility evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-064 — Peer tester completes core shopping workflow unaided
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Ask non-implementer to add structured item, choose quantity/unit, target/shared/personal visibility, mark/unmark, find history and interpret suggestions.
- **Expected:** Peer succeeds; confusion/blockers fixed or documented.
- **PASS evidence required:** Peer test record.
- **Automated proof required:** Manual acceptance evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-065 — Frontend build, test, lint and type checks pass
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-05; TG-05
- **Execute:** Run actual frontend commands.
- **Expected:** All pass; no ignored critical test.
- **PASS evidence required:** Exact command output.
- **Automated proof required:** CI job evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-066 — UC-04 user documentation matches current UI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-05; TG-10
- **Execute:** Follow docs verbatim for catalogue, quantity/unit, targeting, history, voting and scanner behavior.
- **Expected:** No obsolete control/rule/false scanner/voting claim.
- **PASS evidence required:** Documentation execution record.
- **Automated proof required:** Manual doc acceptance test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-067 — All found-bug regressions are green after UC-04 changes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** Bugs #31/#32/#35/#36/#37/#38/#39/#42
- **Execute:** Run focused frontend/E2E tests for every found bug after changes.
- **Expected:** No existing fix regresses; every command/test name recorded.
- **PASS evidence required:** Test output.
- **Automated proof required:** Mandatory regression gate.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-068 — Final frontend sign-off is evidence-complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** All applicable FR/NFR
- **Execute:** Review every FE check, automated proof and bug gate.
- **Expected:** No critical PASS lacks proof; unresolved issue remains visible.
- **PASS evidence required:** Signed review.
- **Automated proof required:** Reviewer sign-off.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

