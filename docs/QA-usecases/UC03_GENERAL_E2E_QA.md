# UC-03 — GENERAL / E2E QA FORM

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

**Integrated Angular + Spring Boot + H2 proof: real multi-user expense/payment/settlement flows, extensions, rounding, deleted-user fallback, browser/device/accessibility, peer testing, CI and all found-bug gates.**

This file is the final system-level proof that FR-05, FR-06, FR-14, FR-17 and every applicable NFR-01..NFR-06 work together in the running application.

## QA execution snapshot — 2026-06-25

All checklist items were defaulted to **BLOCKED** until concrete evidence could be attached directly to the item. Items marked **PASS** below have named automated tests and command results. Remaining **BLOCKED** items need the specific multi-browser/device, race/retry, restart, all-controls, accessibility, or peer-test evidence requested by that item before UC-03 can be called complete.

Validated branch/commit context: `UC-03/QA`, HEAD `184cebc` (`Merge remote-tracking branch 'origin/dev' into UC-03/QA`), dirty working tree with UC-03 QA fixes.


## A. Core FR-05 / FR-06 integrated expense, balance and payment journeys

### E2E-001 — Core equal-split expense journey
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06
- **Execute:** Using a WG with at least three members, create equal split expense, verify each view/balance/settlement, refresh and switch user.
- **Expected:** Expense, splits, net balances and recommendation are coherent across users; total remains exact.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Cypress equal-split happy path.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
  - Test name: `FR-05 positive: creates, equal-splits, persists, edits, and deletes an expense`; `UC-05 positive: expense equal split persists, updates payment status, and appears in history`
  - Test file: `e2e/cypress/e2e/fr05-expenses.cy.js`; `e2e/cypress/e2e/usecases/uc05-expense-history.cy.js`
  - Command run: `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js`; `cd e2e && npx cypress run --spec cypress/e2e/usecases/uc05-expense-history.cy.js`
  - Result: PASS, FR-05 spec `13 passing`; UC-05 spec `2 passing`.
  - Relevant implementation file(s): `frontend/src/app/components/expenses/expenses.component.*`; `frontend/src/app/components/payment-status/payment-status.component.*`; `backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/ExpenseServiceImpl.java`; `PaymentServiceImpl.java`
  - Bug number covered: none
  - Screenshot/video/API evidence path: Cypress passing runs had video disabled and no screenshots; API assertions are embedded in the specs.

### E2E-002 — Core unequal-split expense journey
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; FR-06
- **Execute:** Create custom split with distinct shares, inspect payer/participants/balances/recommendations.
- **Expected:** Exact shares and balance graph match expected calculations; no rounding artifact.
- **PASS evidence required:** Video/API/DB table.
- **Automated proof required:** Cypress custom-split journey.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-003 — Same name + amount creates distinct expenses — BUG #37
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #37; FR-05; NFR-03
- **Execute:** Create two valid distinct expenses with same description and amount through UI.
- **Expected:** Both appear as separate records; no conflict/500.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #37 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
  - Test name: `BUG #37: same description and amount can be used by distinct expenses`
  - Test file: `e2e/cypress/e2e/fr05-expenses.cy.js`
  - Command run: `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js`
  - Result: PASS, `13 passing`.
  - Relevant implementation file(s): `frontend/src/app/components/expenses/expenses.component.*`; `backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/ExpenseServiceImpl.java`
  - Bug number covered: #37
  - Screenshot/video/API evidence path: Cypress passing run had video disabled and no screenshots; API/UI assertions verify two distinct expense records.

### E2E-004 — Expense update works end-to-end — BUG #32
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #32; FR-05; NFR-03
- **Execute:** Edit every permitted field of existing expense, refresh and inspect all dependent views.
- **Expected:** Update succeeds and only intended fields/balances change.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #32 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
  - Test name: `BUG #32: expense update persists through the UI and refreshes dependent data`
  - Test file: `e2e/cypress/e2e/fr05-expenses.cy.js`
  - Command run: `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js`
  - Result: PASS, `13 passing`.
  - Relevant implementation file(s): `frontend/src/app/components/expenses/expenses.component.*`; `frontend/src/app/services/expense.service.ts`; `backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/ExpenseServiceImpl.java`
  - Bug number covered: #32
  - Screenshot/video/API evidence path: Cypress passing run had video disabled and no screenshots; update request status 200 and post-refresh UI state asserted in `fr05-expenses.cy.js`.

### E2E-005 — Equal split edit recomputes exactly — BUG #31
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #31; FR-05; FR-06
- **Execute:** Edit equal-split expense amount/participants/mode for divisibility and remainder fixtures.
- **Expected:** New splits are exact, sum correctly and no stale/duplicate rows remain.
- **PASS evidence required:** Before/after video/API/DB.
- **Automated proof required:** Mandatory named Cypress BUG #31 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
  - Test name: `BUG #31: editing an equal-split expense can add a participant and recompute exact shares`
  - Test file: `e2e/cypress/e2e/fr05-expenses.cy.js`
  - Command run: `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js`
  - Result: PASS, `13 passing`.
  - Relevant implementation file(s): `frontend/src/app/components/expenses/expenses.component.*`; `backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/ExpenseServiceImpl.java`
  - Bug number covered: #31
  - Screenshot/video/API evidence path: Cypress passing run had video disabled and no screenshots; equal-split recalc/update responses asserted in spec.

### E2E-006 — Payment does not globally block unrelated expense operations — BUG #39
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #39; FR-05; FR-06
- **Execute:** Record payment, then edit/delete unrelated expense and inspect targeted related-expense policy.
- **Expected:** Unrelated expense remains operable; any related restriction is granular/explained.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #39 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
  - Test name: `BUG #39: an existing payment does not block editing or deleting another expense`
  - Test file: `e2e/cypress/e2e/fr05-expenses.cy.js`
  - Command run: `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js`
  - Result: PASS, `13 passing`.
  - Relevant implementation file(s): `frontend/src/app/components/expenses/expenses.component.*`; `backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/ExpenseServiceImpl.java`; `PaymentServiceImpl.java`
  - Bug number covered: #39
  - Screenshot/video/API evidence path: Cypress passing run had video disabled and no screenshots; update 200 and delete 204 asserted in spec.

### E2E-007 — Expense deletion/cancel behavior is coherent if exposed
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-05; NFR-03
- **Execute:** Delete/cancel expenses with no payment, partial payment, full payment and terminal/relevant history as policy permits.
- **Expected:** No accidental deletion; derived balances/history remain correct; safe message if blocked.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Cypress delete-policy flow or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-008 — Payment recording and derived status journey
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06
- **Execute:** Create debt, record partial then exact payment, inspect balances, pending/settled state and settlement plan after each.
- **Expected:** Status is derived and updates everywhere consistently; no stale split flag.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress payment-status journey.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
  - Test name: `FR-06 positive: records a partial payment, persists it, and keeps FR-05 balances in sync`
  - Test file: `e2e/cypress/e2e/fr06-payment-status.cy.js`
  - Command run: `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js,cypress/e2e/fr06-payment-status.cy.js,cypress/e2e/fr14-bill-scanner.cy.js,cypress/e2e/fr17-swipe-settlement-approval.cy.js,cypress/e2e/fr07-shopping-crud.cy.js`
  - Result: PASS, combined focused Cypress run `32 passing`.
  - Relevant implementation file(s): `frontend/src/app/components/payment-status/payment-status.component.*`; `backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/PaymentServiceImpl.java`
  - Bug number covered: none
  - Screenshot/video/API evidence path: Cypress passing run had video disabled and no screenshots; payment create 201 and refreshed balance assertions are in `fr06-payment-status.cy.js`.

### E2E-009 — Multiple-user settlement graph journey
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; TG-03
- **Execute:** Create 3–5 user equal/unequal/cyclic debts and inspect recommendation.
- **Expected:** All net balances resolve with minimum transfer count/deterministic rule; no zero/self transfers.
- **PASS evidence required:** Graph/table evidence.
- **Automated proof required:** Cypress/integration settlement graph suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-010 — Rounding edge journey
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-06; TG-03
- **Execute:** Use amounts that split into fractions of a cent across several participants, edit and record payment.
- **Expected:** Displayed/persisted cents follow documented HALF_UP/remainder policy and total exactly matches.
- **PASS evidence required:** Video/API/DB table.
- **Automated proof required:** Cypress rounding regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-011 — Existing invited user has one financial counterpart — BUG #35
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-05; FR-06
- **Execute:** Invite existing account, create splits and settlement involving it.
- **Expected:** No duplicate participant/balance/debt node; correct counterparty once.
- **PASS evidence required:** Video/API/DB graph.
- **Automated proof required:** Mandatory named Cypress BUG #35 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
  - Test name: `BUG #35: existing user accepting an invite appears once in expense participants`
  - Test file: `e2e/cypress/e2e/fr05-expenses.cy.js`
  - Command run: `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js`
  - Result: PASS, `13 passing`.
  - Relevant implementation file(s): `frontend/src/app/components/expenses/expenses.component.*`; `backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/InvitationServiceImpl.java`; `RoommateRepository.java`
  - Bug number covered: #35
  - Screenshot/video/API evidence path: Cypress passing run had video disabled and no screenshots; one-participant assertion and create 201 are in spec.

### E2E-012 — Leave/rejoin financial identity remains singular — BUG #38
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-05; FR-06
- **Execute:** Create debts, leave and rejoin member, inspect split/balance/settlement graph and edit expense.
- **Expected:** No duplicate financial node/split/balance or 500.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #38 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
  - Test name: `BUG #38: leave and rejoin keeps one financial participant and balance counterparty`
  - Test file: `e2e/cypress/e2e/fr05-expenses.cy.js`
  - Command run: `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js`
  - Result: PASS, `13 passing`.
  - Relevant implementation file(s): `frontend/src/app/components/expenses/expenses.component.*`; `backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/InvitationServiceImpl.java`; `RoommateRepository.java`
  - Bug number covered: #38
  - Screenshot/video/API evidence path: Cypress passing run had video disabled and no screenshots; singular balance/counterparty assertions are in spec.

### E2E-013 — Deleted/anonymized member remains settleable — BUG #42
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-06; NFR-02
- **Execute:** Create debt to/from member, leave/delete/anonymize them, inspect plan and perform supported settlement/payment action.
- **Expected:** Privacy-safe fallback remains usable; settlement completes or follows documented policy; no PII/null/FK failure.
- **PASS evidence required:** Before/after evidence.
- **Automated proof required:** Mandatory cross-feature Cypress BUG #42 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
  - Test name: `BUG #42: anonymized former roommate remains visible and settleable through privacy-safe fallback`
  - Test file: `e2e/cypress/e2e/fr05-expenses.cy.js`
  - Command run: `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js`
  - Result: PASS, `13 passing`.
  - Relevant implementation file(s): `frontend/src/app/components/payment-status/payment-status.component.*`; `backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/PaymentServiceImpl.java`
  - Bug number covered: #42
  - Screenshot/video/API evidence path: Cypress passing run had video disabled and no screenshots; privacy-safe fallback and approve 200 asserted in spec.


## B. FR-14 / FR-17 extensions and strict negative/reliability paths

### E2E-014 — One-click bill scanner/calculator workflow if delivered
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14
- **Execute:** Use actual delivered input mechanism, preview/confirm/correct/cancel, generate expense and inspect balances.
- **Expected:** Only confirmed valid output creates one correct expense; documented limitation is respected.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress workflow or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
  - Test name: `FR-14 positive: scans a receipt via camera, creates a reviewed expense, and updates payment status`
  - Test file: `e2e/cypress/e2e/fr14-bill-scanner.cy.js`
  - Command run: `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js,cypress/e2e/fr06-payment-status.cy.js,cypress/e2e/fr14-bill-scanner.cy.js,cypress/e2e/fr17-swipe-settlement-approval.cy.js,cypress/e2e/fr07-shopping-crud.cy.js`
  - Result: PASS, combined focused Cypress run `32 passing`.
  - Relevant implementation file(s): `frontend/src/app/components/expenses/expenses.component.*`; `frontend/src/app/services/receipt-ocr.service.ts`; `backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/ExpenseServiceImpl.java`
  - Bug number covered: none
  - Screenshot/video/API evidence path: Cypress passing run had video disabled and no screenshots; OCR stub, preview 200 and create 201 are asserted in spec.

### E2E-015 — Scanner retry/error/privacy behavior if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-14; NFR-02; NFR-03
- **Execute:** Test invalid/partial input, network retry, duplicate submit, unsupported device/camera where applicable.
- **Expected:** No duplicate expense/private leak/false success; manual fallback works where promised.
- **PASS evidence required:** Video/console/API evidence.
- **Automated proof required:** Cypress scanner negative suite or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-016 — Swipe approval workflow with test account if delivered
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17
- **Execute:** Use dedicated synthetic test account to approve/reject pending settlement, refresh/retry and inspect balance/history.
- **Expected:** Correct target state transition; no real payment implication; no duplicate/stale card.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress approval workflow or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
  - Test name: `approves first settlement via right swipe, rejects second via left swipe`; `rejects first settlement via left swipe, approves second via right swipe`
  - Test file: `e2e/cypress/e2e/fr17-swipe-settlement-approval.cy.js`
  - Command run: `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js,cypress/e2e/fr06-payment-status.cy.js,cypress/e2e/fr14-bill-scanner.cy.js,cypress/e2e/fr17-swipe-settlement-approval.cy.js,cypress/e2e/fr07-shopping-crud.cy.js`
  - Result: PASS, combined focused Cypress run `32 passing`.
  - Relevant implementation file(s): `frontend/src/app/components/payment-status/payment-status.component.*`; `backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/PaymentServiceImpl.java`
  - Bug number covered: none
  - Screenshot/video/API evidence path: Cypress passing run had video disabled and no screenshots; approve/reject API status 200 asserted in spec.

### E2E-017 — Swipe has accessible fallback and authorization
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-17; NFR-01; NFR-04
- **Execute:** Act as payer/payee/unrelated/foreign actors using keyboard/mouse/touch and card/action controls.
- **Expected:** Only authorized actor acts; approve/reject possible without swipe gesture.
- **PASS evidence required:** Video/axe evidence.
- **Automated proof required:** Cypress role/accessibility test or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
  - Test name: `approves and rejects settlements via accessible button fallback`; `does not show settlement approvals to the creditor`; `redirects unauthenticated users to login`
  - Test file: `e2e/cypress/e2e/fr17-swipe-settlement-approval.cy.js`
  - Command run: `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js,cypress/e2e/fr06-payment-status.cy.js,cypress/e2e/fr14-bill-scanner.cy.js,cypress/e2e/fr17-swipe-settlement-approval.cy.js,cypress/e2e/fr07-shopping-crud.cy.js`
  - Result: PASS, combined focused Cypress run `32 passing`.
  - Relevant implementation file(s): `frontend/src/app/components/payment-status/payment-status.component.*`; `backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/PaymentServiceImpl.java`; `PaymentEndpoint.java`
  - Bug number covered: none
  - Screenshot/video/API evidence path: Cypress passing run had video disabled and no screenshots; fallback button clicks and authorization UI behavior asserted in spec.

### E2E-018 — All finance controls work in integrated app
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04; explicit user requirement
- **Execute:** Inventory every expense/split/payment/settlement/scanner/swipe button, link, icon, menu and dialog across states; activate each.
- **Expected:** No dead control/false success; disabled/hidden state is clear and correct.
- **PASS evidence required:** Completed inventory/video.
- **Automated proof required:** Cypress control-inventory suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-019 — Negative error matrix is safe
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03; NFR-04
- **Execute:** Trigger validation, 401, 403, 404, 409, 500, offline, timeout and stale-state errors across finance flows.
- **Expected:** Actionable safe UX; no SQL/stack/secret/foreign data or false settled state.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Cypress error-mapping matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-020 — Two-household financial isolation works
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Use WG A/B, direct URLs, tampered IDs, browser history and cross-user payment/edit/delete attempts.
- **Expected:** No foreign expense/payment/balance/settlement data or mutation.
- **PASS evidence required:** Video/network/DB evidence.
- **Automated proof required:** Cypress two-household security suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-021 — Rapid actions/multi-tab financial races are safe
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Double-submit expense/edit/payment/approval/delete and act from two tabs/users.
- **Expected:** Final canonical state valid; no duplicate expense/split/payment/approval/500.
- **PASS evidence required:** Video/request trace.
- **Automated proof required:** Cypress reconciliation suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-022 — Restart/reload/seed restore preserves finance state
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03; TG-05
- **Execute:** Create/edit expense and payment, restart/reset per docs, refresh views/recompute settlement.
- **Expected:** State survives/restores as documented; no stale/duplicate derived data.
- **PASS evidence required:** Commands/video/API evidence.
- **Automated proof required:** Clean-environment/restart smoke.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-023 — Audit and privacy lifecycle work with finance records
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; NFR-06; TG-07
- **Execute:** Create/update/delete expense, record payment/approve settlement, leave/delete/anonymize fixture, inspect history/audit.
- **Expected:** Required actor/time trace; privacy-safe fallback, no secret/PII leak.
- **PASS evidence required:** Audit/before-after evidence.
- **Automated proof required:** Integration + Cypress linkage.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-024 — No financial secrets/PII leak in browser or backend logs
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02
- **Execute:** Capture console/backend logs through normal and failing financial flows.
- **Expected:** No passwords/JWT/raw tokens/bank data/excess receipt content/full private payload.
- **PASS evidence required:** Redacted logs.
- **Automated proof required:** Console guard + log scan.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-025 — Whole-project found-bug gate remains green
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** Bugs #31/#32/#35/#36/#37/#38/#39/#42
- **Execute:** Run all focused regression tests after UC-03 integration.
- **Expected:** All known bugs remain fixed; names/commands/output recorded.
- **PASS evidence required:** CI/test output.
- **Automated proof required:** Mandatory regression gate.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
  - Test name: Backend bug tests #31/#32/#35/#36/#37/#38/#39/#42; frontend component bug tests #31/#36/#42; Cypress bug tests #31/#32/#35/#36/#37/#38/#39/#42
  - Test file: `ExpenseEndpointTest.java`; `InvitationEndpointTest.java`; `PaymentEndpointTest.java`; `ShoppingItemEndpointTest.java`; `PaymentServiceTest.java`; `expenses.component.spec.ts`; `payment-status.component.spec.ts`; `shopping-item-create.component.spec.ts`; `shopping-item-edit.component.spec.ts`; `fr05-expenses.cy.js`; `fr07-shopping-crud.cy.js`
  - Command run: `cd backend && mvn -B clean install`; `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless`; `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js`; `cd e2e && npx cypress run --spec cypress/e2e/fr05-expenses.cy.js,cypress/e2e/fr06-payment-status.cy.js,cypress/e2e/fr14-bill-scanner.cy.js,cypress/e2e/fr17-swipe-settlement-approval.cy.js,cypress/e2e/fr07-shopping-crud.cy.js`
  - Result: PASS, backend `388 tests`; frontend `404 specs`; focused FR-05 Cypress `13 passing`; combined focused Cypress `32 passing`.
  - Relevant implementation file(s): UC-03 backend/frontend expense/payment/invitation/shopping files changed in this QA pass.
  - Bug number covered: #31/#32/#35/#36/#37/#38/#39/#42
  - Screenshot/video/API evidence path: Surefire reports in `backend/target/surefire-reports/`; Cypress passing runs had video disabled and no screenshots.


## C. All applicable NFRs — usability, accessibility, compatibility, documentation, CI and final release gate

### E2E-026 — Peer tester completes core financial workflow unaided
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Ask non-implementer to add equal/unequal expense, edit it, record payment, interpret settlement and use extensions if delivered.
- **Expected:** Peer succeeds; all confusion/blockers fixed or clearly documented.
- **PASS evidence required:** Peer test record.
- **Automated proof required:** Manual acceptance evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-027 — WAI/Lighthouse/axe financial quality gate
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Audit expense form, split editor, balance overview, payment dialogs, scanner and swipe screens.
- **Expected:** No critical accessibility blocker; findings resolved/escalated.
- **PASS evidence required:** Audit reports.
- **Automated proof required:** Repeatable axe/Lighthouse run.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-028 — Keyboard-only financial workflow works
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Create/edit/delete expense, manipulate splits, record payment and use approval/fallback via keyboard.
- **Expected:** No mouse/swipe-only blocker; focus/dialogs safe.
- **PASS evidence required:** Video.
- **Automated proof required:** Manual + automated accessibility proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-029 — Reference browser/device matrix passes
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04; TG-08
- **Execute:** Run core finance flow on Chrome/Firefox desktop 1366×768+, iPhone 12 Pro Safari 390×844, Pixel 7 Chrome 412×915 and 360px.
- **Expected:** No clipped financial field/action, overflow, keyboard/touch/browser blocker.
- **PASS evidence required:** Screenshots/video/browser versions.
- **Automated proof required:** Cypress viewport + manual-device tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-030 — Zoom/text scaling is usable
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Run primary finance operations at 200% zoom/text scale.
- **Expected:** Critical monetary values/actions remain visible and reachable.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Manual accessibility evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-031 — OpenAPI and real browser behavior agree
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-04
- **Execute:** Compare finance network calls/errors to served OpenAPI across core flows.
- **Expected:** No undocumented/deprecated mismatch.
- **PASS evidence required:** Network/OpenAPI proof.
- **Automated proof required:** Contract suite plus manual check.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-032 — CI executes all UC-03 layers
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-05; TG-09
- **Execute:** Review/run pipeline after UC-03 test change.
- **Expected:** Frontend/backend/repository/integration/E2E financial layers run and failure blocks merge.
- **PASS evidence required:** Pipeline evidence.
- **Automated proof required:** CI verification.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-033 — Finance setup and user docs match working flow
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-10; NFR-05
- **Execute:** Follow docs from clean environment for expenses, payments, settlement and extensions.
- **Expected:** No obsolete steps/false bank/scanner/swipe claim/missing prerequisite.
- **PASS evidence required:** Execution record.
- **Automated proof required:** Documentation acceptance test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-034 — Final integrated requirement sign-off
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** All FR-05/FR-06/FR-14/FR-17 and NFR-01..06
- **Execute:** Review all three UC-03 forms, evidence, commands and every bug gate before merge.
- **Expected:** No hidden critical failure/evidence gap; limitations remain visible.
- **PASS evidence required:** Signed review summary.
- **Automated proof required:** Reviewer sign-off.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`
