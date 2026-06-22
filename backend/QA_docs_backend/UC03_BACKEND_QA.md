# UC-03 — BACKEND QA FORM

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

**Spring Boot finance API, BigDecimal settlement engine, H2/JPA persistence, security/RBAC, transactions, payment status, scanner/approval extension behavior, OpenAPI, CI and auditability.**

This form owns server-side enforcement for FR-05, FR-06, FR-14 and FR-17 plus NFR-01, NFR-02, NFR-03, NFR-05 and NFR-06.


## A. FR-05 — Expense creation, participants, equal/custom splits and persistence

### BE-001 — All expense endpoints require valid authentication
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-01
- **Execute:** Enumerate every expense endpoint; call each without/malformed/expired JWT.
- **Expected:** Safe 401 and no finance data/mutation.
- **PASS evidence required:** Endpoint matrix/responses.
- **Automated proof required:** Parameterized security integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-002 — Expense API matches OpenAPI contract
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; TG-04
- **Execute:** Compare all paths, methods, security, DTOs, success and error schemas to served OpenAPI.
- **Expected:** Implementation and docs agree.
- **PASS evidence required:** Spec/API comparison.
- **Automated proof required:** Contract tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-003 — Expense create DTO validates every required field
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-03
- **Execute:** Submit missing/blank/boundary/malformed description, amount, payer, participants, splits and all live fields directly to API.
- **Expected:** Controlled 4xx, no persisted Expense/Split and no 500.
- **PASS evidence required:** Responses/DB assertions.
- **Automated proof required:** Parameterized endpoint tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-004 — Amount uses precise decimal parsing and rejects invalid values
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06; TG-03
- **Execute:** Submit zero, negative, too many decimals, scientific notation, locale variants, extreme values and float-artifact strings.
- **Expected:** Only documented positive decimal format/scale accepted; no binary-float corruption or silent rounding outside policy.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Parameterized money-validation tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-005 — Expense derives household and creator from authenticated principal
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-01; TG-01
- **Execute:** Inject householdId, createdBy, role, payer owner IDs in body/query/header.
- **Expected:** Server ignores/rejects forged authority; expense is scoped to actor’s active household.
- **PASS evidence required:** Request/DB proof.
- **Automated proof required:** Authorization/mass-assignment tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-006 — Payer must be an eligible current household member
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-01; NFR-03
- **Execute:** Attempt payer from foreign WG, former member, deleted/anonymized user, duplicate/rejoined member and nonexistent ID.
- **Expected:** Only valid active current payer accepted; no cross-WG/null relation.
- **PASS evidence required:** Response/DB matrix.
- **Automated proof required:** Service/repository/integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-007 — Participants must be unique eligible current household members
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-01; NFR-03
- **Execute:** Submit duplicate, foreign, former, deleted/anonymized, rejoined and nonexistent participant lists.
- **Expected:** Only unique active same-WG participants accepted; no duplicate ExpenseSplit rows.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Validation/repository/integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-008 — Existing-user invitation creates no duplicate financial participant — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-05; NFR-03
- **Execute:** Invite existing account, create expense involving it, inspect user/membership/splits.
- **Expected:** Exactly one participant/split/counterparty for that user; no 500.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-009 — Leave/rejoin creates no duplicate financial participant — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-05; NFR-03
- **Execute:** Join, create expense, leave, rejoin, create/edit/list expense and settlement.
- **Expected:** One active member and one intended split/counterparty; no duplicate balance/500.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory repository + integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-010 — Anonymized/deleted financial references remain usable — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-05; FR-06; NFR-02; NFR-03
- **Execute:** Create expense/splits, delete/anonymize member, read/update/settle fixture.
- **Expected:** No FK break/null failure; privacy-safe fallback supports later settlement.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory cross-feature integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-011 — Equal split uses BigDecimal and exact cent-total invariant
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06; TG-03
- **Execute:** Create equal split for divisible and non-divisible cent amounts over 2–10 participants; inspect persisted splits.
- **Expected:** All shares are 2-decimal BigDecimal values, deterministic and sum exactly to expense amount.
- **PASS evidence required:** DB calculations/test table.
- **Automated proof required:** Unit/service/integration equal-split tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-012 — Equal split remainder allocation is deterministic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06; TG-03
- **Execute:** Repeat equal split of values such as 10.00/3 and 0.01/3 under stable participant ordering.
- **Expected:** Same documented remainder policy each run; no missing/extra cent; no random order effect.
- **PASS evidence required:** Repeated DB/API evidence.
- **Automated proof required:** Unit tests for rounding/remainder allocation.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-013 — Custom/unequal split validates exact total
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06; TG-03
- **Execute:** Submit exact, under, over, negative, zero, duplicate and scale-invalid share sets.
- **Expected:** Only allowed exact total persists; no residual cent or silent adjustment unless documented.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Parameterized split validation tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-014 — Expense creation is atomic across Expense, Splits and audit
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03; NFR-06
- **Execute:** Force failure after Expense insert/before Split insert/audit record.
- **Expected:** No partial expense, orphan split or missing coupled state remains.
- **PASS evidence required:** DB assertions.
- **Automated proof required:** Transactional service/integration test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-015 — Duplicate create retries are safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Send repeated/concurrent identical create requests and simulate client retry.
- **Expected:** Behavior is documented and no accidental duplicate/constraint 500 occurs.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Concurrency/idempotency tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-016 — Identical description and amount may create distinct expenses — BUG #37
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #37; FR-05; NFR-03
- **Execute:** Create two valid expenses in same WG with same description and same amount but distinct records/context.
- **Expected:** Both persist as separate expense IDs; no false uniqueness conflict/500.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-017 — Expense create response is privacy-minimized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; TG-04
- **Execute:** Inspect success/error DTOs, headers and serialized nested users/splits.
- **Expected:** No password/JWT/raw invite/internal security data/excess PII.
- **PASS evidence required:** Response inventory.
- **Automated proof required:** Serialization/contract test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-018 — Expense create errors are sanitized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Trigger validation/auth/constraint/unexpected failures.
- **Expected:** Safe schema/status; no SQL/stack/class/secret.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception-handler tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## B. FR-05 — Expense reads, updates, deletion and payment-related edit policy

### BE-019 — Expense list/detail endpoints enforce household isolation
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-01
- **Execute:** Use WG A actor against every WG B expense ID/list/filter.
- **Expected:** No foreign data/existence leak/mutation.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** IDOR/cross-WG integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-020 — Expense detail handles missing/foreign IDs safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Request random, deleted, foreign and malformed IDs.
- **Expected:** Controlled safe response; no stack trace/financial leak.
- **PASS evidence required:** Responses.
- **Automated proof required:** Parameterized endpoint tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-021 — Expense update permission policy is backend-enforced
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-01
- **Execute:** Test creator, payer, participant, other roommate, Main Tenant, former and foreign actors against update endpoints.
- **Expected:** Only documented actor succeeds; server not UI decides.
- **PASS evidence required:** Response/DB matrix.
- **Automated proof required:** Role-based integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-022 — Expense update works for every permitted field — BUG #32
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #32; FR-05; NFR-03
- **Execute:** Update each allowed field: description, amount, payer, participants, split mode/shares, date/category if supported.
- **Expected:** Update succeeds, persists after reload, and unrelated values remain unchanged.
- **PASS evidence required:** Before/after DB/API.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-023 — Expense update validates all edited values atomically
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06; NFR-03
- **Execute:** Submit valid/boundary/invalid values for every editable field, including invalid new split total.
- **Expected:** No partial update on failure; full valid change persists transactionally.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Service/endpoint tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-024 — Equal split recomputes correctly on edit — BUG #31
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #31; FR-05; FR-06; TG-03
- **Execute:** Edit amount, participant count and equal/custom mode on existing expense using divisible/indivisible cent fixtures.
- **Expected:** New splits use deterministic BigDecimal rounding, sum exactly to new total and replace stale splits cleanly.
- **PASS evidence required:** DB before/after proof.
- **Automated proof required:** Mandatory named unit + integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-025 — Custom split edit preserves financial invariants
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06; NFR-03
- **Execute:** Edit custom shares, add/remove participants, change payer, switch custom/equal.
- **Expected:** No duplicate/stale split, total mismatch or unrelated balance corruption.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Service/integration split-edit matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-026 — Update recomputes derived balances and settlement deterministically
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; TG-03
- **Execute:** Create balances, edit one expense, inspect all net balances/recommendations before/after.
- **Expected:** Derived outputs update from canonical expenses/payments; no stale settled flag/state.
- **PASS evidence required:** DB/API test table.
- **Automated proof required:** Integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-027 — Payment does not globally block unrelated expense update/delete — BUG #39
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #39; FR-05; FR-06; NFR-03
- **Execute:** Record payment in household; update/delete a distinct unrelated expense through API.
- **Expected:** Unrelated expense actions follow normal policy; no global payment lock/500.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-028 — Related-record edit/delete policy is granular and explicit
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06; NFR-03
- **Execute:** Record payment affecting target expense/balance; attempt update/delete target and unrelated record.
- **Expected:** Any restriction applies only to actual affected state and returns clear business result; no blanket WG lock.
- **PASS evidence required:** Response/DB matrix.
- **Automated proof required:** Integration policy test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-029 — Expense delete/cancel policy is server-safe if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-03
- **Execute:** Locate delete/cancel endpoint; test authorized, unrelated, paid/unpaid, partial/full, repeated and stale cases.
- **Expected:** No orphan splits/balance corruption; policy is consistent and safe.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Integration tests or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-030 — Expense delete/update is transactionally atomic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Force failure during split replacement, balance recalculation, audit or related check.
- **Expected:** No partial update/delete or stale split remains.
- **PASS evidence required:** DB assertions.
- **Automated proof required:** Transactional rollback tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-031 — Expense filters/query inputs validate safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-01; NFR-03
- **Execute:** Fuzz date/category/payer/status/search/sort/page values supported by API.
- **Expected:** No SQL/parser error/broad financial leak; controlled response.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Parameterized tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-032 — Expense actions create audit traces
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-06
- **Execute:** Create, update, change participants/splits, delete/cancel according to support; inspect audit.
- **Expected:** Actor/time/action/entity trace exists without secret/private payload leak.
- **PASS evidence required:** Audit records.
- **Automated proof required:** Integration audit tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-033 — Expense errors never expose database internals
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Trigger uniqueness/FK/validation/payment-related/unexpected errors.
- **Expected:** Safe status/body; no SQL constraint/entity/class/stack.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception-handler tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## C. FR-06 — Deterministic balances, minimum-transactions settlement and recorded payments

### BE-034 — Balance calculation uses BigDecimal, not binary floating point
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; TG-03
- **Execute:** Inspect financial service/types and run values that expose float errors such as 0.1+0.2.
- **Expected:** All monetary math uses BigDecimal/decimal contract; exact cents no float artifacts.
- **PASS evidence required:** Code/test/DB evidence.
- **Automated proof required:** Unit tests with rounding fixtures.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-035 — Rounding is HALF_UP to two decimals where contract requires
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; TG-03
- **Execute:** Test positive/negative/remainder boundary fixtures through actual service/API.
- **Expected:** Results follow documented BigDecimal HALF_UP two-decimal semantics consistently.
- **PASS evidence required:** Fixture table.
- **Automated proof required:** Unit tests for rounding edges.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-036 — Equal-split balances are correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; TG-03
- **Execute:** Create equal split expenses with payer included/excluded as policy allows, 2–N members.
- **Expected:** Each net balance is correct and total net sum is exactly zero.
- **PASS evidence required:** Expected-vs-actual table.
- **Automated proof required:** Unit + integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-037 — Unequal-split balances are correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; TG-03
- **Execute:** Create custom splits across several users with varied payer/participant arrangements.
- **Expected:** Each amount owed/net balance is correct; total net sum zero.
- **PASS evidence required:** Expected-vs-actual table.
- **Automated proof required:** Unit + integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-038 — Debt cycles are simplified to minimum transactions
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; TG-03
- **Execute:** Create multi-user cycle/non-cycle balance fixtures and request settlement plan.
- **Expected:** Plan clears all net balances with minimum transfer count; if multiple minima exist, output follows deterministic tie rule.
- **PASS evidence required:** Graph/table evidence.
- **Automated proof required:** Unit tests for minimum-transaction algorithm.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-039 — Settlement output is deterministic across ordering/reload
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; TG-03; NFR-03
- **Execute:** Insert logically identical expense/payment fixtures in different order; repeatedly query/restart.
- **Expected:** Same canonical settlement amounts/counterparties/order or documented deterministic tie behavior.
- **PASS evidence required:** Repeated output.
- **Automated proof required:** Determinism/restart tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-040 — Settlement plan contains no zero/self/invalid transfers
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; TG-03
- **Execute:** Use zero-balance, self-payer, full-settled and mixed fixtures.
- **Expected:** No transfer with zero amount, self payer/payee, cross-WG or precision error.
- **PASS evidence required:** API output evidence.
- **Automated proof required:** Unit/integration validation tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-041 — Payment create validates amount and counterparties
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; NFR-01; NFR-03
- **Execute:** Submit zero/negative/over-scale/huge, payer=payee, foreign/former/nonexistent payer/payee direct to API.
- **Expected:** Only valid current-household payment persists; safe errors.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Parameterized endpoint tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-042 — Payment creation is atomic and idempotency-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Force failure during payment/audit/balance read and send concurrent/retry requests.
- **Expected:** No partial payment/audit or accidental duplicate; derived balance remains correct.
- **PASS evidence required:** DB assertions/output.
- **Automated proof required:** Transactional + concurrency tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-043 — Recorded payment nets balances correctly
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; TG-03
- **Execute:** Create debt, record partial/exact payment and inspect balance/recommendation/status.
- **Expected:** Net balances/recommendations update by exact payment amount; totals remain zero.
- **PASS evidence required:** Before/after table.
- **Automated proof required:** Integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-044 — Overpayment policy is explicit and enforced
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; NFR-03
- **Execute:** Attempt payment above current obligation/net relationship under actual policy.
- **Expected:** Rejected or accepted only under documented model; no nonsensical negative/duplicate settlement state.
- **PASS evidence required:** Responses/DB proof.
- **Automated proof required:** Boundary tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-045 — Payment status is derived, never stale stored split flag
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; NFR-03
- **Execute:** Create expense, payments, edit/delete permitted expense, retrieve all status views and inspect entity design.
- **Expected:** Status recomputes from ExpenseSplit + Payment; no stale per-split settled source of truth.
- **PASS evidence required:** Code/DB/API evidence.
- **Automated proof required:** Service/integration derived-status tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-046 — Payment history is household-scoped and privacy-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; NFR-01; NFR-02
- **Execute:** Use two WGs and former/anonymized fixtures; list/detail/payment history queries.
- **Expected:** No foreign payment/PII leak; fallback identity is safe.
- **PASS evidence required:** Response/DB evidence.
- **Automated proof required:** IDOR/privacy integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-047 — Deleted/anonymized counterparty remains settleable — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-06; TG-03; NFR-02
- **Execute:** Create debt to/from member, delete/anonymize/leave them, compute plan and record supported settlement.
- **Expected:** Plan remains computable and payment/status can resolve as policy permits; no null/FK/PII leak.
- **PASS evidence required:** Before/after DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-048 — Existing-user invite does not duplicate settlement counterparties — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-06; NFR-03
- **Execute:** Invite existing account, create expense/splits, query balances/settlement.
- **Expected:** No duplicate debt node/counterparty or double-counted balance.
- **PASS evidence required:** API/DB graph evidence.
- **Automated proof required:** Mandatory integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-049 — Rejoin does not duplicate settlement counterparties — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-06; NFR-03
- **Execute:** Create financial history, leave/rejoin participant, query balances/plan.
- **Expected:** Exactly one logical counterpart/balance; no duplicate graph node/500.
- **PASS evidence required:** API/DB graph evidence.
- **Automated proof required:** Mandatory integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-050 — Payment-related actions are auditable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; NFR-06
- **Execute:** Create payment, inspect actor/time/action/status and any approval/reject events.
- **Expected:** Trace exists, does not include bank/secrets/full sensitive payload.
- **PASS evidence required:** Audit evidence.
- **Automated proof required:** Integration audit tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-051 — Financial mutations use transaction boundaries
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; TG-03; NFR-03
- **Execute:** Review services and force failures across payment, settlement status, expense edit/delete.
- **Expected:** No partial financial state; rollback proven.
- **PASS evidence required:** Code/DB evidence.
- **Automated proof required:** Transactional rollback tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-052 — No real transfer/banking integration is exposed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06 scope; NFR-02
- **Execute:** Inspect API/UI/config/docs for bank account/card/payment provider behavior.
- **Expected:** Only tracked payment records; no banking credentials/payment execution API.
- **PASS evidence required:** Code/API/docs evidence.
- **Automated proof required:** Static/API inventory test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## D. FR-14 — One-click expense calculator / bill-scanner backend

### BE-053 — FR-14 backend delivery status is explicit
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14 extended; NFR-05
- **Execute:** Locate controller/service/OpenAPI/tests/docs for scanner/calculator. If absent/prototype, validate truthful documentation.
- **Expected:** No false claim of complete scanner; actual contract is identifiable.
- **PASS evidence required:** Code/docs/OpenAPI evidence.
- **Automated proof required:** Test or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-054 — Scanner/calculator endpoint requires authenticated household context
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; NFR-01
- **Execute:** Call endpoint unauthenticated, invalid/expired, foreign/former/current user.
- **Expected:** No financial data/mutation outside current household; safe 401/403.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Security integration test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-055 — Actual scan/calculation input is validated server-side
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; FR-05; NFR-03
- **Execute:** Use real implementation payload: image metadata, parsed fields, calculator values, barcode etc.; test malformed/oversized/invalid/partial inputs.
- **Expected:** Safe validation, no malformed expense creation or server crash.
- **PASS evidence required:** Responses/log evidence.
- **Automated proof required:** Parameterized validation tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-056 — Scanner/calculator monetary output uses exact financial logic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; FR-06; TG-03
- **Execute:** Run rounding-boundary parsed/calculated fixtures through backend.
- **Expected:** Output amount/splits use documented BigDecimal rounding; no float artifact.
- **PASS evidence required:** Fixture/API proof.
- **Automated proof required:** Unit/integration calculation tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-057 — One-click generation is transactional and retry-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; NFR-03
- **Execute:** Force failure after parse/before Expense persistence; retry/concurrent requests.
- **Expected:** No partial data/duplicate expense; canonical result or safe error.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Transactional/idempotency tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-058 — Scanner does not retain unnecessary sensitive data
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; NFR-02
- **Execute:** Inspect storage schema/logs/temp files/responses for scanned payload/image/receipt data.
- **Expected:** Only data necessary for delivered feature is retained; no raw sensitive artifact in logs by default.
- **PASS evidence required:** Code/DB/log evidence.
- **Automated proof required:** Privacy/log-scan test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-059 — Manual confirmation/correction is honored by backend
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; FR-05
- **Execute:** Submit corrected parsed fields and cancel/non-confirmed flow according to contract.
- **Expected:** Only confirmed valid data produces expense; no hidden unconfirmed mutation.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Integration workflow test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-060 — FR-14 errors are sanitized and OpenAPI-correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; TG-04; NFR-01
- **Execute:** Trigger input/auth/provider/parse failures that implementation supports.
- **Expected:** Safe response, no raw provider stack/secret/receipt content; spec matches.
- **PASS evidence required:** Responses/logs/OpenAPI.
- **Automated proof required:** Contract/exception tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## E. FR-17 — Swipe settlement approval backend and test-account isolation

### BE-061 — FR-17 backend delivery state is explicit
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17 extended; NFR-05
- **Execute:** Locate state model/endpoints/services/docs. If prototype-only/absent, validate truthful limitation.
- **Expected:** No false production claim; actual approval/reject contract identified.
- **PASS evidence required:** Code/docs/OpenAPI evidence.
- **Automated proof required:** Test or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-062 — Approval/rejection endpoints enforce correct actor permissions
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; NFR-01
- **Execute:** Test payer, payee, unrelated roommate, Main Tenant, former and foreign actors against pending settlement.
- **Expected:** Only documented authorized actor can act; no cross-WG/forged action.
- **PASS evidence required:** Response/DB matrix.
- **Automated proof required:** Role-based integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-063 — Approval/rejection state transitions are valid and idempotent
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; NFR-03
- **Execute:** Apply approve/reject twice, approve-after-reject, reject-after-approve and stale action cases.
- **Expected:** Only valid transition occurs; repeats return safe idempotent/conflict result; no duplicate audit/state.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** State-machine tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-064 — Approval/rejection affects only target settlement
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; FR-06; NFR-03
- **Execute:** Create several pending settlements; act on one.
- **Expected:** Only intended record/status/balance changes; no unrelated mutation.
- **PASS evidence required:** Before/after DB/API evidence.
- **Automated proof required:** Integration isolation regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-065 — Approval is not confused with real money transfer
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; FR-06 scope
- **Execute:** Inspect state names, API docs and mutation effects.
- **Expected:** Approval/rejection affects tracked settlement workflow only; no bank execution/credential path.
- **PASS evidence required:** Code/API/docs evidence.
- **Automated proof required:** Contract/static review.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-066 — Test account is isolated and does not create a bypass
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; NFR-01
- **Execute:** Inspect config/build/seeding/endpoint behavior; attempt arbitrary test mode/credential use.
- **Expected:** No hard-coded secret/token/role bypass; dedicated demo account is synthetic and constrained.
- **PASS evidence required:** Config/code/test evidence.
- **Automated proof required:** Release security review.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-067 — Approval/rejection is auditable and privacy-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; NFR-06; NFR-02
- **Execute:** Approve/reject fixture and inspect audit/log/response.
- **Expected:** Actor/time/status trace correct; no secrets/bank/extra private data.
- **PASS evidence required:** Audit/log evidence.
- **Automated proof required:** Integration audit test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-068 — FR-17 errors are sanitized and contract-accurate
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; TG-04; NFR-01
- **Execute:** Trigger missing, stale, unauthorized, foreign and unexpected conditions.
- **Expected:** Safe error schema; no stack/financial leak; OpenAPI matches.
- **PASS evidence required:** Responses/OpenAPI.
- **Automated proof required:** Exception/contract tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## F. All applicable NFRs — security, privacy, reliability, maintainability, CI and known-bug gate

### BE-069 — Complete UC-03 authorization matrix exists
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Enumerate every expense, split, payment, settlement, scanner and swipe endpoint; classify auth/member/role/state restrictions.
- **Expected:** No endpoint unclassified; tests cover every class.
- **PASS evidence required:** Matrix evidence.
- **Automated proof required:** Parameterized security suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-070 — Every financial read endpoint blocks cross-WG access
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Use WG A actor against all WG B expense/payment/settlement/scanner resources and IDs.
- **Expected:** No foreign financial data/existence leak.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** Security integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-071 — Every financial mutation blocks cross-WG writes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Use WG A actor against WG B create/edit/delete/payment/approval resources.
- **Expected:** No mutation and DB unchanged.
- **PASS evidence required:** Endpoint/DB matrix.
- **Automated proof required:** Security suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-072 — All financial DTOs block mass assignment
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Inject IDs, household, creator, audit, balance, settled, payer/payee, status/approval fields outside allowed contract.
- **Expected:** No unauthorized authority/state/ownership change.
- **PASS evidence required:** Request/DB evidence.
- **Automated proof required:** Mass-assignment tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-073 — Hostile request values fail safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Fuzz IDs, dates, decimal strings, enum/state values, pagination/sort/search, long payloads.
- **Expected:** No parser/SQL error/500 or broad financial leak.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Parameterized tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-074 — Financial logs/audit/responses are privacy-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; NFR-06
- **Execute:** Run normal and failing financial flows; inspect logs/audit/DTOs/error handlers.
- **Expected:** No password/JWT/raw invite/bank data/secret/excess PII/full receipt content in logs.
- **PASS evidence required:** Redacted evidence.
- **Automated proof required:** Log scan/static review.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-075 — Account deletion/anonymization retains required financial history
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; TG-07; BUG #42
- **Execute:** Inspect delete/anonymize behavior for payer, participant, payment counterpart and approval actor.
- **Expected:** Privacy-safe identity preserves legal/functional understanding and settlement; no broken FK.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Lifecycle integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-076 — Critical financial transactions are atomic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03; TG-03
- **Execute:** Review and fault-inject create/update/delete expense, payment, settlement/approval, scanner-generated expense.
- **Expected:** No partial financial or audit state on failure.
- **PASS evidence required:** Code/DB evidence.
- **Automated proof required:** Rollback tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-077 — Concurrency is covered for financial races
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Run concurrent create/update/delete, equal split edit, payment create, approval/reject, settlement retrieval.
- **Expected:** Final state valid/deterministic; no duplicate payment/split/500.
- **PASS evidence required:** Output/DB evidence.
- **Automated proof required:** Concurrent integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-078 — Seed/reset is reproducible with financial fixtures
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03; TG-05
- **Execute:** From clean checkout follow reset/seed docs; verify expenses, equal/unequal splits, payments, former-user fixture and settlement data.
- **Expected:** Known synthetic state restored without hidden repair.
- **PASS evidence required:** Commands/DB/API evidence.
- **Automated proof required:** Seed smoke test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-079 — Financial persistence survives restart
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Create/update expense/payment, restart as documented, inspect balances/recommendations.
- **Expected:** State survives/restores as claimed; no stale/duplicate settlement.
- **PASS evidence required:** Commands/API/DB evidence.
- **Automated proof required:** Restart integration test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-080 — Repository tests cover financial queries and invariants
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03; QA strategy
- **Execute:** Locate/run tests for household scope, split totals, payment lookup, active members, deleted fallback, transaction graphs.
- **Expected:** Real persistence semantics proven.
- **PASS evidence required:** Test paths/output.
- **Automated proof required:** Mandatory repository evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-081 — Service tests cover deterministic financial rules
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-03; NFR-05; TG-09
- **Execute:** Locate/run unit tests for equal/unequal splits, HALF_UP rounding, minimum graph, payments, edits/deletes, deleted fallback.
- **Expected:** Rules tested independently of UI/E2E.
- **PASS evidence required:** Test paths/output.
- **Automated proof required:** Mandatory service test evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-082 — API integration tests cover finance + auth + persistence
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-09
- **Execute:** Run end-to-end API workflows for create/edit/delete/payment/settlement and known bugs.
- **Expected:** Security and real DB state proven together.
- **PASS evidence required:** Output.
- **Automated proof required:** Mandatory integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-083 — OpenAPI is served and current
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-04
- **Execute:** Start application and retrieve Swagger/OpenAPI; inspect UC-03 operations.
- **Expected:** Specification available/current.
- **PASS evidence required:** URL/output.
- **Automated proof required:** OpenAPI smoke/contract test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-084 — CI runs all critical UC-03 test layers
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-05; TG-09
- **Execute:** Inspect CI profiles/exclusions/allow_failure and pipeline execution.
- **Expected:** Unit/repository/integration/frontend/E2E financial tests run; failures block merge.
- **PASS evidence required:** CI YAML/job proof.
- **Automated proof required:** CI verification.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-085 — Architecture boundaries are clean
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-01; NFR-05
- **Execute:** Trace controllers → services → repositories/mappers and Angular boundary for all financial flows.
- **Expected:** No business logic wrongly in controller/UI; no DB access outside repo; one financial source of truth.
- **PASS evidence required:** Architecture map.
- **Automated proof required:** Code review plus tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-086 — DTO mappers exclude internal financial/security fields
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; TG-04
- **Execute:** Inspect Expense/Split/Payment/Settlement/Approval/Scanner serialization.
- **Expected:** No internal balance cache/security/audit secret exposed beyond contract.
- **PASS evidence required:** Code/response proof.
- **Automated proof required:** Serialization tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-087 — Finance setup/API/user documentation is current
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-10; NFR-05
- **Execute:** Follow docs for expense, split, edit/delete, payment, settlement, FR-14 and FR-17 behavior.
- **Expected:** No obsolete rule/false bank/scanner/swipe claim.
- **PASS evidence required:** Documentation execution record.
- **Automated proof required:** Manual doc acceptance test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-088 — All known-bug backend regressions are green
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** Bugs #31/#32/#35/#36/#37/#38/#39/#42
- **Execute:** Run focused backend/integration tests for every found bug after UC-03 changes.
- **Expected:** All remain green; tests/commands recorded.
- **PASS evidence required:** Output.
- **Automated proof required:** Mandatory regression gate.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-089 — Final backend sign-off is evidence-complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** All applicable FR/NFR/TG
- **Execute:** Review every BE item and automated evidence.
- **Expected:** No critical PASS lacks proof; unresolved items remain visible.
- **PASS evidence required:** Signed review.
- **Automated proof required:** Reviewer sign-off.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

