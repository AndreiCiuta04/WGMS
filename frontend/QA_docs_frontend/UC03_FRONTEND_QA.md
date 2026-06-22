# UC-03 — FRONTEND QA FORM

## Embedded execution instructions — no Project Proposal upload required

This file is self-contained for **UC-03**. Do **not** ask the user to upload, paste, or re-explain the Project Proposal.

Before checking any box, inspect the current repository’s Angular routes/components/services/tests; Spring controllers/services/entities/repositories/DTOs/security configuration/tests; H2 schema; OpenAPI; seed/reset tools; CI; implementation notes; and the current state of all found bug fixes.

**Strict rules**
1. Mark PASS only with the exact evidence and automated proof named below. “The code looks correct”, “it worked once”, “the button exists”, or “CI was green earlier” is not evidence.
2. If evidence or a required test does not exist, create it. If a defect appears, fix it, add a regression test, rerun the relevant suite, then update the evidence.
3. Never weaken expected behavior, remove/skip a critical test, use `|| true`, or hide FAIL/BLOCKED items.
4. Use synthetic test users, households, expenses and payments only. Do not place passwords, JWTs, raw invitation tokens, hashes, secrets, bank data, or real personal data in evidence.
5. UC-03 is not releasable while a critical requirement lacks proof.

## Binding UC-03 functional baseline

- **FR-05 — Shared expense tracking (High, Core):** roommates can enter shared expenses, identify payer and relevant participants, split costs, edit/delete according to documented business rules, and track resulting balances.
- **FR-06 — Payment status overview & deterministic settlement (High, Core):** balances, pending-versus-settled amounts, and recommended settlement are computed deterministically. The financial engine must use `java.math.BigDecimal`, `HALF_UP` rounding to two decimals, a transactional service boundary, and reduce the debt graph to a minimum-transactions settlement. The contract explicitly requires automated coverage for equal splits, unequal splits, rounding edges, deleted-user fallbacks, and settlement status after recorded payments.
- **FR-14 — One-click expense calculator / bill scanner (Medium, Extended):** where delivered, scanned/calculated bill data can produce an expense workflow. The actual scanner/parser/confirmation design must be discovered from the repository. Do not invent OCR, external provider, or image-retention behavior; test the delivered implementation and document any prototype limitation truthfully.
- **FR-17 — Swipe-based settlement approval with test account (Low, Extended):** where delivered, a dedicated test-account demonstration supports approval/rejection of pending settlements. The actual state model and actor permissions must be discovered from the implementation. Do not assume approval represents a real bank transfer; real money transfers are explicitly out of scope.

## Contract domain invariants that must be proven

- `Expense` records description, amount, payer and household context.
- `ExpenseSplit` represents each participant’s `amountOwed`.
- `Payment` represents a settling transfer between roommates.
- Balances and pending/settled status are **derived** by netting expense splits and recorded payments; there is **no per-split settled flag** to treat as source of truth.
- Deterministic settlement must be server-side, use BigDecimal (not binary floating point), two-decimal `HALF_UP` semantics, be transactionally safe, and minimize the transfer graph.
- Users may access and mutate only data belonging to their own household.
- WGMS tracks money but does not execute real transfers, store bank accounts, credit cards, or payment-provider credentials.

## Binding non-functional baseline

- **NFR-01 Security & Access Control:** authenticated access to WG-specific financial data; server-side household scoping/RBAC; input validation; no client-forged actor/household/role.
- **NFR-02 Data Privacy & GDPR:** financial data minimization; access/correction/deletion/anonymization where possible; safe historical representation after a user leaves/deletes; no PII/secrets in logs or responses.
- **NFR-03 Reliability:** expenses, splits, payments and settlement state persist correctly; reproducible seed/reset; atomic financial mutations; safe retries, edits, deletes, races, restart and controlled errors.
- **NFR-04 Usability & Compatibility:** peer-testable financial workflows; WAI/Lighthouse; Chrome/Firefox desktop 1366×768+, iPhone 12 Pro Safari 390×844, Pixel 7 Chrome 412×915, and 360px boundary.
- **NFR-05 Maintainability:** clean Angular/Spring/repository/business-rule separation, documented financial policies, tests and setup.
- **NFR-06 Auditability:** expense creation/change/deletion and settlement/payment/approval actions must be traceable with responsible actor and timestamp where audit facility exists.

## Mandatory found-bug regression gate

**Direct UC-03 bugs**
- **BUG #31:** Equal split when editing an expense. Re-editing must recompute correct split data and totals without stale/duplicated values.
- **BUG #32:** An expense cannot be updated. Update must work for every permitted field and remain transactionally correct.
- **BUG #37:** Creating an expense with the same name and amount conflicts. Two legitimate distinct expenses with same description and amount must be supported; no erroneous uniqueness conflict.
- **BUG #39:** Any payment in the WG hard-blocks editing/deleting all expenses. A payment must not globally block unrelated expense updates/deletions. Rules for financially related records must be granular, documented and tested.
- **BUG #42:** Debt to a roommate who left/deleted their account can never be settled. The privacy-safe identity fallback must preserve settlement capability and history.

**Membership integration bugs**
- **BUG #35:** Existing user acceptance must not create duplicate expense participants/payers/counterparties.
- **BUG #38:** Leave then rejoin must not create duplicate expense participants, balances or settlement counterparts.

**Whole-project non-regression**
After UC-03 changes, run focused regression tests for **#31, #32, #35, #36, #37, #38, #39 and #42** and record every test command/name.

## Layer scope

**Angular/browser layer: expense creation/editing/deletion, split editor, balances, payments, deterministic-settlement presentation, one-click bill-scanner/calculator and swipe approval UI.**

Owns visible behavior for FR-05, FR-06, FR-14 and FR-17, every UI-facing NFR-01..NFR-06 obligation, all controls, usability and visual/regression proof.


## A. FR-05 — Expense creation, payer, participants and split input

### FE-001 — Expense routes are correctly protected
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-01; NFR-04
- **Execute:** Open every expense/balance/payment route as logged out, authenticated no-WG, Main Tenant, Roommate, former member and direct URL state.
- **Expected:** Only valid current household members reach the correct financial context; no stale or foreign data flashes.
- **PASS evidence required:** Video, route matrix, console capture.
- **Automated proof required:** Route-guard tests plus Cypress route matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-002 — Expense list is household-scoped in the UI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-01
- **Execute:** Use two households with distinct expense/payment fixtures, switch users, refresh and use browser history.
- **Expected:** Only current-household expenses, balances and payments are visible; no cached data from another household appears.
- **PASS evidence required:** Screenshots and API comparison.
- **Automated proof required:** Component/service test plus two-household Cypress test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-003 — Expense form matches current API/OpenAPI contract
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; TG-04
- **Execute:** Compare all visible fields, labels, types, required indicators and submitted JSON against current DTO/OpenAPI.
- **Expected:** Every required server field is collectable and correctly serialized; no stale frontend field blocks valid creation.
- **PASS evidence required:** Screenshot, redacted request, OpenAPI reference.
- **Automated proof required:** Component/client-service request-shape test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-004 — Every expense form control has accessible labels and help
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-04/WAI
- **Execute:** Inspect labels, accessible names, required state, descriptions and error links for amount, description, payer, participants, split mode, dates/categories and every live field.
- **Expected:** No placeholder-only or unlabeled control; errors are programmatically associated.
- **PASS evidence required:** Accessibility tree/axe evidence.
- **Automated proof required:** Component accessibility test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-005 — Description and required-field validation is complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-04
- **Execute:** Submit empty/whitespace/min/max/boundary description and omit each required field independently.
- **Expected:** Invalid expense is not submitted; precise visible and accessible errors appear.
- **PASS evidence required:** Screenshots/network capture.
- **Automated proof required:** Parameterized form validation tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-006 — Amount input rejects invalid financial values
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06; NFR-03
- **Execute:** Test empty, zero, negative, too many decimal places, comma/dot locale input, huge, nonnumeric, leading zeros and boundary values.
- **Expected:** Only allowed positive monetary values proceed; no silent float rounding or misleading coercion.
- **PASS evidence required:** Screenshots/request/response.
- **Automated proof required:** Parameterized amount-validation tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-007 — Money display is consistent and two-decimal clear
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06; NFR-04
- **Execute:** Enter/display values such as 0.01, 1.10, 10.00, 10.005, 999999.99 according to accepted API policy.
- **Expected:** UI formats values unambiguously; no floating-point artifacts such as 0.3000000004.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Money-format component tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-008 — Payer selector contains only eligible same-WG members
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-01; NFR-03
- **Execute:** Use active, foreign, former, deleted/anonymized and rejoined members across two households.
- **Expected:** Only active current-WG members appear exactly once; no foreign/former duplicate payer.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Selector mapping tests plus Cypress isolation test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-009 — Participant selector contains only eligible current-WG members
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-01; NFR-03
- **Execute:** Use active, foreign, former, deleted/anonymized and rejoined fixtures.
- **Expected:** Only eligible current members can be selected once; duplicate participant cannot be selected.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Component selection test plus Cypress household isolation.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-010 — Existing invited user does not duplicate payer/participant — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-05; NFR-03
- **Execute:** Invite already-existing account into WG, open expense form, inspect payer/participant lists and create expense.
- **Expected:** User appears once; expense can be created correctly; no duplicate split/counterparty.
- **PASS evidence required:** Video/API/DB count.
- **Automated proof required:** Mandatory named Cypress BUG #35 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-011 — Leave/rejoin does not duplicate payer/participant — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-05; NFR-03
- **Execute:** Join, leave, rejoin same account, then inspect payer/participants and create/edit expense.
- **Expected:** Rejoined user appears once; no duplicate split/balance/counterparty or 500.
- **PASS evidence required:** Video/screenshots/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #38 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-012 — Anonymized/deleted user references render safely — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; NFR-02; NFR-03
- **Execute:** Create expense/history involving a member who leaves/deletes/anonymizes, then reopen expense and balance views.
- **Expected:** Privacy-safe fallback appears; no null crash/blank unusable row/PII leak.
- **PASS evidence required:** Before/after screenshots.
- **Automated proof required:** Cypress cross-feature regression linked to BUG #42.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-013 — Equal split editor computes and displays all shares
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06
- **Execute:** Create equal splits with divisible and indivisible-cent amounts for 2, 3 and more participants.
- **Expected:** Displayed shares follow documented deterministic rounding and total exactly equals amount.
- **PASS evidence required:** Screenshots with sum calculation/API comparison.
- **Automated proof required:** Component equal-split tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-014 — Custom/unequal split editor validates exact total
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06; NFR-03
- **Execute:** Enter unequal shares that sum exactly, under-sum, over-sum, negative/zero share and rounding-boundary cases.
- **Expected:** Only valid exact total can submit; clear difference/error shown; no silent residual loss.
- **PASS evidence required:** Screenshots/request evidence.
- **Automated proof required:** Parameterized split-editor validation tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-015 — Payer inclusion/exclusion behavior is understandable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-04
- **Execute:** Create expenses where payer is included/excluded among participants according to live policy.
- **Expected:** UI explains and submits correct intended participant set; balances displayed sensibly.
- **PASS evidence required:** Screenshots/API comparison.
- **Automated proof required:** Component/Cypress payer-participant tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-016 — Expense creation action prevents duplicate submits
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Double-click/press Enter under slow network; reload/back around request.
- **Expected:** Only one logical expense is created; pending state visible; no duplicate row/toast.
- **PASS evidence required:** Video/request count.
- **Automated proof required:** Pending-state component test + Cypress duplicate-submit test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-017 — Same description and amount can create distinct expenses — BUG #37
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #37; FR-05; NFR-03
- **Execute:** Create two legitimate expenses in same household with identical description and amount but distinct business context/time/payer where allowed.
- **Expected:** Both persist as separate records; no erroneous uniqueness conflict/500.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress regression for BUG #37.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-018 — Expense creation error recovery is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03; NFR-04
- **Execute:** Simulate 400/401/403/404/409/500/offline/timeout and retry after correction.
- **Expected:** Safe actionable error; no stack/SQL; no phantom expense; data remains recoverable where safe.
- **PASS evidence required:** Screenshots/video/network.
- **Automated proof required:** Service/component error mapping tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-019 — Created expense appears consistently after refresh
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-03
- **Execute:** Create expense, inspect list/detail/balances/participant view, refresh and navigate back.
- **Expected:** Exactly one record with correct payer, splits, amount and status.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress create-expense happy path.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## B. FR-05 — Expense view, editing, deletion and found-bug regression

### FE-020 — Expense detail shows correct payer, participants and splits
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06
- **Execute:** Open expenses with equal/unequal splits, various participants and payments.
- **Expected:** Displayed values equal API/canonical split state; no missing/duplicated participant.
- **PASS evidence required:** Screenshots/API comparison.
- **Automated proof required:** Component mapping tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-021 — Every exposed expense edit control works — BUG #32
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #32; FR-05; NFR-03
- **Execute:** Inventory edit controls; change every permitted field: description, amount, payer, participants, split type/shares, date/category if supported.
- **Expected:** Update succeeds for permitted fields, persists after refresh, and only intended values change; no generic failure.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #32 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-022 — Equal split recomputes correctly after editing — BUG #31
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #31; FR-05; FR-06; NFR-03
- **Execute:** Edit an existing expense: alter amount, participant count and equal/custom mode; use divisible/indivisible cents.
- **Expected:** All splits recompute exactly once, sum exactly to updated amount and no stale/duplicate split remains.
- **PASS evidence required:** Before/after screenshots/API/DB proof.
- **Automated proof required:** Mandatory named Cypress BUG #31 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-023 — Custom split editing preserves exact balance invariants
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06; NFR-03
- **Execute:** Edit unequal shares, add/remove participants, switch custom↔equal and test under/over/exact totals.
- **Expected:** Only valid state saves; no stale share or rounding loss; revised balance is correct.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress split-edit matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-024 — Editing one expense does not alter unrelated expenses
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-03
- **Execute:** Create several similar expenses, edit one and inspect every other record/balance.
- **Expected:** Only target expense changes; no list/balance corruption.
- **PASS evidence required:** Before/after evidence.
- **Automated proof required:** Cypress isolation regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-025 — Payment does not globally block unrelated expense edit/delete — BUG #39
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #39; FR-05; FR-06; NFR-03
- **Execute:** Record a payment in the WG, then edit/delete a distinct unrelated expense and inspect controls/API.
- **Expected:** Unrelated expense remains editable/deletable according to normal rules; no global hard block.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #39 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-026 — Financially related expense edit/delete policy is clear and granular
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06; NFR-04
- **Execute:** Record payment related to an expense, then attempt update/delete that expense and an unrelated one.
- **Expected:** Any restriction is limited to documented affected state; UI explains exact reason; never blanket WG lock.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Cypress related-vs-unrelated policy test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-027 — Expense delete controls are safe if exposed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-03
- **Execute:** Inventory delete actions; test confirmation, cancel/Escape, authorized/unauthorized, paid/unpaid/related states and refresh.
- **Expected:** No accidental deletion; behavior follows documented policy; no stale balances/history.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Cypress delete flow or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-028 — Expense delete is not falsely available where policy blocks it
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-04
- **Execute:** Inspect control state/message for blocked cases.
- **Expected:** Disabled/hidden/actionable state accurately reflects backend rule; no false success.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Component role/state tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-029 — Expense list filters/sort/search work if exposed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-04
- **Execute:** Inventory all filter/search/sort controls; combine/reset/refresh/back.
- **Expected:** Results match UI wording, no stale query and no foreign record.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Component/Cypress filter tests or N/A proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-030 — Expense error messages are safe and actionable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-04
- **Execute:** Trigger validation/conflict/payment-related/authorization/500 errors in create/edit/delete.
- **Expected:** No SQL/stack/internal ID/secret; clear correction/next step.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Error mapping tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## C. FR-06 — Balance view, payment entry and deterministic settlement UX

### FE-031 — Balance overview is household-scoped and internally consistent
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; NFR-01
- **Execute:** Use two WGs and multiple expense/payment fixtures; compare list, detail and overview.
- **Expected:** Only current WG balances appear; totals agree with underlying expenses/payments.
- **PASS evidence required:** Screenshots/API comparison.
- **Automated proof required:** Component mapping + Cypress two-WG test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-032 — Pending versus settled state is displayed as derived, not stale
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; NFR-03
- **Execute:** Create expense, record partial/full payments, then edit/delete expense per policy and refresh.
- **Expected:** Status updates from canonical data; no stale per-split settled label/state.
- **PASS evidence required:** Before/after evidence.
- **Automated proof required:** Cypress derived-status regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-033 — Settlement recommendations display deterministic amounts and counterparties
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; TG-03
- **Execute:** Use fixtures with equal, unequal, cycle and rounding cases; refresh/reopen multiple times.
- **Expected:** Same data produces same recommendation set/order or documented tie rule; no float artifacts.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Cypress deterministic-settlement scenarios.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-034 — Settlement UI communicates that no real money transfer occurs
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06 scope; NFR-04
- **Execute:** Review payment/settlement labels, buttons and help text.
- **Expected:** System records/approves tracked payment only; does not imply bank execution or request bank/card credentials.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Copy inventory test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-035 — Record-payment form validates amount and counterparties
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; NFR-01; NFR-03
- **Execute:** Test empty/zero/negative/too many decimals/huge amount; payer=payee; foreign/former/nonexistent counterparties.
- **Expected:** Only valid current-household payment can be submitted; errors are specific and safe.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Parameterized form/Cypress tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-036 — Record-payment action prevents duplicate payment
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Double-click/Enter under slow network; reload around submit.
- **Expected:** One logical payment record; balances update once.
- **PASS evidence required:** Video/request count.
- **Automated proof required:** Pending-state plus Cypress duplicate-submit test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-037 — Payment success updates all relevant financial views
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06
- **Execute:** Record payment, inspect balance overview, expense detail, settlement recommendation/history and refresh.
- **Expected:** Every relevant view reflects derived netting consistently.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress record-payment journey.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-038 — Partial payment and overpayment behavior is clear
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; NFR-03
- **Execute:** Record partial, exact and overpayment attempts according to policy.
- **Expected:** Partial status is correct; exact settles as calculated; prohibited overpayment is rejected or documented behavior is clear.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Cypress payment-boundary tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-039 — Payment can target former/anonymized counterparty if policy permits — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-06; NFR-02; NFR-03
- **Execute:** Use debt involving user who left/deleted/anonymized; inspect settlement/payment UI and attempt supported settlement.
- **Expected:** A privacy-safe counterpart remains usable; no impossible settlement/null error; no PII leakage.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory cross-feature Cypress BUG #42 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-040 — Payment history/status UI has correct actor/time and no secrets
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; NFR-06; NFR-02
- **Execute:** Inspect recorded payments in every view/history.
- **Expected:** Correct payer/payee/amount/time status; no bank data/JWT/internal IDs.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Component mapping tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-041 — Settlement UI handles empty/zero-balance state correctly
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; NFR-04
- **Execute:** Use household with no expenses, all settled balances and no eligible counterparties.
- **Expected:** Clear empty/settled state; no invalid action/NaN/negative zero.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Component/Cypress empty-state test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-042 — Settlement UI handles server failure/retry safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03; NFR-04
- **Execute:** Simulate settlement/payment API errors, stale data and timeout.
- **Expected:** No false settled state; safe error/retry and canonical refresh.
- **PASS evidence required:** Video/network.
- **Automated proof required:** Service/component error tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## D. FR-14 — One-click expense calculator / bill scanner UI

### FE-043 — FR-14 delivery status is truthful
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14 extended; NFR-04; NFR-05
- **Execute:** Locate all bill-scanner/one-click-calculator UI, docs and feature flags.
- **Expected:** Working feature is testable; prototype/absence is explicitly documented and not falsely advertised.
- **PASS evidence required:** Screenshots/docs.
- **Automated proof required:** UI/doc test or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-044 — Scanner/calculator entry point is household and role safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; NFR-01
- **Execute:** Open entry point as logged out, no-WG, current member, former/foreign user.
- **Expected:** Only intended actor accesses it; no financial context leak.
- **PASS evidence required:** Screenshots/routes.
- **Automated proof required:** Route-guard/Cypress test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-045 — Input/scan preview is validated before expense creation
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; FR-05; NFR-03
- **Execute:** Use actual delivered input type (image, parsed payload, manual calculator, barcode etc.) with valid/invalid/partial data.
- **Expected:** Parsed/calculated fields are visible and validated before expense mutation; user understands what will be created.
- **PASS evidence required:** Video/screenshots/API evidence.
- **Automated proof required:** Component workflow test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-046 — Manual correction and cancel path work
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; NFR-04
- **Execute:** Correct parsed amount/description/participants, cancel, then rescan/retry.
- **Expected:** User can correct mistakes; cancel creates no expense; retry is stable.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress correction/cancel test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-047 — One-click result uses correct monetary rounding
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; FR-06
- **Execute:** Use scanner/calculator fixtures with rounding boundaries and compare output to expected cents.
- **Expected:** Displayed/generated expense uses documented two-decimal result; no float artifact.
- **PASS evidence required:** Fixture screenshots/API evidence.
- **Automated proof required:** Component money-calculation test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-048 — Scanner/calculator does not create duplicate expenses on retry
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; NFR-03
- **Execute:** Repeat scan/submit, double-click one-click action, simulate network interruption after processing.
- **Expected:** No duplicate expense; user gets canonical result.
- **PASS evidence required:** Video/request count.
- **Automated proof required:** Cypress idempotency/retry regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-049 — Bill/scan UI does not leak sensitive data
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; NFR-02
- **Execute:** Inspect preview, console, errors, storage, generated text and share UI.
- **Expected:** No raw image content/extra personal data/secret is leaked; retention behavior is clear.
- **PASS evidence required:** Screenshots/console evidence.
- **Automated proof required:** Static/manual privacy review.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-050 — FR-14 accessibility and mobile fallback work
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; NFR-04
- **Execute:** Operate via keyboard and at mobile reference viewports; test unavailable camera/unsupported browser if scanner uses hardware.
- **Expected:** No gesture/camera-only dead end; manual fallback and safe error are available.
- **PASS evidence required:** Video/screenshots.
- **Automated proof required:** Cypress/manual device test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## E. FR-17 — Swipe settlement approval UI with test account

### FE-051 — FR-17 delivery status and test account are truthful
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17 extended; NFR-04; NFR-05
- **Execute:** Locate swipe-approval screen, dedicated test-account configuration, docs and feature flags.
- **Expected:** Feature/prototype/demo scope is explicit; no real money transfer claim; credentials are not exposed.
- **PASS evidence required:** Screenshots/docs/config review.
- **Automated proof required:** UI/doc test or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-052 — Only correct settlement actor sees approve/reject UI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; NFR-01
- **Execute:** Compare payer, payee, unrelated roommate, Main Tenant, former and foreign user states.
- **Expected:** Only authorized party/parties can act under documented policy; no false capability.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Role-state Cypress test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-053 — Swipe has accessible non-gesture fallback
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; NFR-04/WAI
- **Execute:** Use keyboard, screen reader, mouse and touch; inspect approve/reject buttons or equivalent.
- **Expected:** All actions are possible without swipe; controls have accessible names and confirmation feedback.
- **PASS evidence required:** Video/axe evidence.
- **Automated proof required:** Accessibility/Cypress test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-054 — Approve and reject actions are clear, reversible only if policy allows
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; NFR-03
- **Execute:** Perform approve/reject, refresh, reopen, retry action and inspect status/history.
- **Expected:** State transition follows actual model; no duplicate action/stale card; policy is explained.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress approval-state workflow.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-055 — Swipe approval affects only intended pending settlement
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; FR-06; NFR-03
- **Execute:** Create multiple pending settlements; act on one and inspect all others/balances.
- **Expected:** Only target changes; unrelated settlement/balance remains correct.
- **PASS evidence required:** Before/after evidence.
- **Automated proof required:** Cypress isolation regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-056 — Approval/rejection errors and stale cards are safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; NFR-01; NFR-03
- **Execute:** Act after another tab processed settlement, with invalid role, offline and server failure.
- **Expected:** No false success; card refreshes to canonical state and message is safe.
- **PASS evidence required:** Video/network.
- **Automated proof required:** Cypress stale-state/error tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-057 — FR-17 actions appear in audit/history without exposing secrets
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; NFR-06; NFR-02
- **Execute:** Approve/reject test-account settlement and inspect visible history.
- **Expected:** Actor/time/status trace correct; no secret/test credential/bank data.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Component mapping test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-058 — FR-17 test account cannot become a production bypass
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; NFR-01
- **Execute:** Inspect deployed UI/build/config and attempts to activate test mode as arbitrary user.
- **Expected:** No exposed credentials, hard-coded token or role bypass; demo account is isolated/documented.
- **PASS evidence required:** Source/build/screenshots.
- **Automated proof required:** Release/Cypress security review.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## F. All applicable NFRs — controls, accessibility, compatibility, docs and bug gate

### FE-059 — Every UC-03 button, link, icon, menu and dialog works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04; explicit user requirement
- **Execute:** Inventory every visible expense/payment/settlement/scanner/swipe control for public, Main Tenant, Roommate, error, modal and lifecycle states; activate each.
- **Expected:** No dead control or false success; disabled/hidden state is clear and accurate.
- **PASS evidence required:** Completed control inventory/video.
- **Automated proof required:** Dedicated Cypress control-inventory spec.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-060 — All finance UI error messages are safe and actionable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03; NFR-04
- **Execute:** Trigger validation, 401, 403, 404, 409, 500, scanner and payment-related errors.
- **Expected:** No SQL/stack/internal ID/secret/foreign data; user gets next step.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Service/component error-mapping tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-061 — No frontend logging leaks financial or secret data
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02
- **Execute:** Search source and capture console/telemetry during core and negative financial flows.
- **Expected:** No password/JWT/raw token/bank/extra financial data appears in logs.
- **PASS evidence required:** Static search/redacted console evidence.
- **Automated proof required:** Static scan/manual review.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-062 — WAI/Lighthouse/axe checks pass for financial workflows
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Audit expense form, split editor, balance/settlement, payment dialogs, scanner and swipe screens.
- **Expected:** No critical blocker; all findings fixed/escalated.
- **PASS evidence required:** Audit report.
- **Automated proof required:** Repeatable axe/Lighthouse run.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-063 — Keyboard-only expense and payment workflow works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Create/edit/delete expense, change splits, record payment, inspect/approve settlement and use scanner fallback via keyboard only.
- **Expected:** No mouse/swipe-only blocker; focus/dialogs safe.
- **PASS evidence required:** Video.
- **Automated proof required:** Accessibility/E2E proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-064 — Reference browser/device matrix passes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04; TG-08
- **Execute:** Run core financial flow on Chrome/Firefox 1366×768+, iPhone Safari 390×844, Pixel Chrome 412×915 and 360px.
- **Expected:** No clipped amount/split fields, hidden action, overflow, keyboard/touch blocker or browser defect.
- **PASS evidence required:** Screenshots/video/device versions.
- **Automated proof required:** Cypress viewport plus manual-device evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-065 — Zoom/text scaling remains usable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Use 200% zoom/text scaling on amount/split/payment/scanner/swipe screens.
- **Expected:** Essential values/actions remain visible and reachable.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Manual accessibility evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-066 — Peer tester completes core expense workflow unaided
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Ask non-implementer to add equal/unequal expense, edit it, record payment and explain settlement status.
- **Expected:** Peer completes workflow; confusion/blockers fixed or documented.
- **PASS evidence required:** Peer test record.
- **Automated proof required:** Manual acceptance evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-067 — Frontend build, test, lint and type checks pass
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-05; TG-05
- **Execute:** Run actual frontend commands.
- **Expected:** All pass; no ignored critical test.
- **PASS evidence required:** Exact command output.
- **Automated proof required:** CI job evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-068 — UC-03 user documentation matches current UI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-05; TG-10
- **Execute:** Follow docs verbatim for expenses, editing, payments, settlement, FR-14/FR-17 if delivered.
- **Expected:** No obsolete screen/rule/false real-money/scanner/swipe claim.
- **PASS evidence required:** Documentation execution record.
- **Automated proof required:** Manual doc acceptance test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-069 — All found-bug regressions are green after UC-03 changes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** Bugs #31/#32/#35/#36/#37/#38/#39/#42
- **Execute:** Run focused frontend/E2E tests for every known bug after changes.
- **Expected:** No existing fix regresses; every command/test name recorded.
- **PASS evidence required:** Test output.
- **Automated proof required:** Mandatory regression gate.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-070 — Final frontend sign-off is evidence-complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** All applicable FR/NFR
- **Execute:** Review every FE check, test, bug gate and evidence record.
- **Expected:** No critical PASS lacks proof; unresolved issue remains visible.
- **PASS evidence required:** Signed review.
- **Automated proof required:** Reviewer sign-off.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

