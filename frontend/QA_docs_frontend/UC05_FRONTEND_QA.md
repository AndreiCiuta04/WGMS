# UC-05 — FRONTEND QA FORM

## Embedded execution instructions — no Project Proposal upload required

This file is self-contained for **UC-05**. Do **not** ask the user to upload, paste, or re-explain the Project Proposal.

Before checking any box, inspect the current repository’s Angular routes/components/services/tests; Spring controllers/services/entities/repositories/DTOs/security configuration/tests; H2 schema; OpenAPI; seed/reset tools; CI; dashboard aggregation queries; notification/reminder delivery implementation; task/payment history entities; chart configuration; and the current status of every known bug fix.

**Strict rules**
1. Mark PASS only with the exact evidence and automated proof named below. “The code looks correct”, “it worked once”, “the button exists”, or “CI was green earlier” is not evidence.
2. If evidence or a required test does not exist, create it. If a defect appears, fix it, add a regression test, rerun the relevant suite, then update the evidence.
3. Never weaken expected behavior, remove/skip a critical test, use `|| true`, or hide FAIL/BLOCKED items.
4. Use synthetic users, households, tasks, expenses, payments, shopping items, notification records and chart data only. Do not place passwords, JWTs, raw invitation tokens, hashes, secrets, real personal data, raw private notifications or sensitive financial records in evidence.
5. UC-05 is not releasable while a critical requirement lacks proof.

## Binding UC-05 functional baseline

- **FR-11 — Notifications and reminders (Medium, Extended):** WGMS provides notifications/reminders where delivered. The Project Contract states that reminders flag new tasks or expenses; the actual delivered trigger set, channel and lifecycle model must be discovered from the repository. Do not invent email, push, SMS, third-party provider or background-worker behavior that is not implemented.
- **FR-12 — Dashboard with current responsibilities (High, Core):** the central dashboard provides an aggregated household view of open responsibilities, payment status and active reminders. The project/persona specification further requires current assigned chores, open expense share/balance and active shopping-list context to be available at a glance where delivered.
- **FR-13 — History of tasks and payments (High, Core):** a household history view gives users a defensible audit trail for tasks and payment-related actions. It must remain accurate, household-scoped, privacy-safe and usable after legitimate lifecycle events.
- **FR-16 — Statistical history view (Medium, Core):** dashboard history uses visualizations rather than predictive stock forecasting. Required statistical views include pie charts for expense categories such as operating costs, food and electricity, spending development over time, top buyer/user contribution statistics, and shopping history where delivered.

## Contract boundaries that must be proven

- UC-05 is a dashboard/history/reporting package. It must present canonical task, payment, expense and shopping data; it must not invent a second source of truth.
- Dashboard totals, current responsibilities, payment state, reminders, history and charts must be derived from household-scoped source data, react correctly after core mutations, and never expose another household’s data.
- Statistical views are descriptive historical visualizations, **not** predictive stock forecasting or LLM-driven recommendation features.
- WGMS does not execute real financial transfers or process banking credentials.
- Notification delivery channels and scheduling behavior must be tested only as actually delivered. If a feature is prototype-only or absent, the UI/API/docs must say so truthfully.

## Binding non-functional baseline

- **NFR-01 Security & Access Control:** authenticated access to WG-specific dashboard, history and notification data; server-side household scoping/RBAC/input validation; no client-forged actor/household/role.
- **NFR-02 Data Privacy & GDPR:** data minimization; privacy-safe notification content; access/correction/deletion/anonymization where possible; historical data safe after a user leaves/deletes; no PII/secrets in logs/responses/charts.
- **NFR-03 Reliability:** durable notification/history data, reproducible seed/reset, correct stale-cache behavior, atomic read-state changes, deduplicated reminders, safe retries/races/restarts/time boundaries and controlled errors.
- **NFR-04 Usability & Compatibility:** dashboard, history and alerts are completable without external instructions by a peer tester; WAI/Lighthouse; Chrome/Firefox desktop 1366×768+, iPhone 12 Pro Safari 390×844, Pixel 7 Chrome 412×915, and 360px boundary.
- **NFR-05 Maintainability:** clean Angular/Spring/repository/business-rule separation; aggregation/notification/documentation rules are testable and documented.
- **NFR-06 Auditability:** dashboard history must correctly surface/derive traceable records; important actions such as task assignment/completion and payment settlement remain attributable by actor and timestamp where audit facility exists.

## Mandatory found-bug regression gate

**Direct UC-05 consequences of found bugs**
- **BUG #31:** Equal split when editing an expense. Dashboard balances, payment status, expense-history entries and chart totals must immediately reflect the corrected split/amount without stale or duplicate values.
- **BUG #32:** Expense update failure. Dashboard/history/statistics must refresh correctly after every permitted expense update.
- **BUG #37:** Same-name/amount expense conflict. Two legitimate same-description/same-amount expenses must both appear in dashboard history and statistical totals.
- **BUG #39:** Payment globally blocks editing/deleting expenses. Dashboard/history/statistics must update for permitted unrelated expense changes even after a payment exists.
- **BUG #42:** Debt to a member who left/deleted their account. Dashboard/history/reminders/chart labels must preserve a privacy-safe fallback and must not break settlement/status display.
- **BUG #35:** Existing-user invitation acceptance. Newly joined existing user must appear exactly once in dashboard responsibilities, notifications, histories and chart participant/buyer data.
- **BUG #38:** Leave then rejoin. Rejoined member must not duplicate dashboard rows, notifications, history entries, chart contributions or member counters.
- **BUG #36:** Structured shopping quantity/unit. Dashboard shopping context/history/statistical views must preserve canonical quantity/unit values and not reintroduce free-text ambiguity.

**Whole-project non-regression**
After UC-05 changes, run focused regression tests for **#31, #32, #35, #36, #37, #38, #39 and #42** and record every test command/name.

## Layer scope

**Angular/browser layer: notifications and reminders, current-responsibility dashboard, task/payment history, historical statistical visualizations, all controls, accessibility, responsive behavior and frontend regressions.**

This form owns visible behavior for FR-11, FR-12, FR-13 and FR-16 plus every UI-facing NFR-01..NFR-06 obligation.


## A. FR-11 — Notifications and reminders UI

### FE-001 — Notification/reminder routes are protected
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-01; NFR-04
- **Execute:** Open every delivered notification/reminder route, drawer, badge, list and deep link as logged out, authenticated no-WG, Main Tenant, Roommate, former member and direct URL state.
- **Expected:** Only intended authenticated current household user reaches their own notification context; no stale/private content flashes.
- **PASS evidence required:** Video, route matrix, browser console capture.
- **Automated proof required:** Route-guard tests plus Cypress route matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-002 — FR-11 delivery state is truthful
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11 extended; NFR-04; NFR-05
- **Execute:** Inspect all reminder/notification UI, docs, feature flags and API usage. Identify actual trigger types, delivery channels and read-state model.
- **Expected:** Delivered notifications are testable. Prototype/absence/limited trigger set is accurately documented; no fake email/push/SMS promise.
- **PASS evidence required:** Screenshots/docs/API inventory.
- **Automated proof required:** UI/doc test or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-003 — Notification list and badge reflect only current user’s items
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-01; NFR-02
- **Execute:** Use several users in one and two households, generate delivered notification events, then switch sessions/refresh/history.
- **Expected:** User sees only their own authorized notifications; badge/count has no foreign/stale items.
- **PASS evidence required:** Screenshots/API comparison.
- **Automated proof required:** Component/service test plus Cypress multi-user isolation.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-004 — Every delivered reminder trigger appears exactly once
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-03
- **Execute:** Discover all implementation trigger types (for example new task, new expense, overdue task, manual reminder); generate each once and inspect list/badge.
- **Expected:** Each canonical event produces exactly intended notification/reminder state; no missed or duplicate alert.
- **PASS evidence required:** Fixture table, screenshots/API evidence.
- **Automated proof required:** Parameterized Cypress trigger suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-005 — New task reminder is understandable where delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; FR-03; NFR-04
- **Execute:** Assign a task to a user and inspect notification content, deep link, target state and read behavior.
- **Expected:** Notification tells user only necessary information, reaches correct task/context and contains no forbidden household/private detail.
- **PASS evidence required:** Video/screenshots.
- **Automated proof required:** Cypress task-notification flow or N/A with delivery proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-006 — New expense/payment reminder is understandable where delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; FR-05; FR-06; NFR-04
- **Execute:** Create expense/payment affecting a user, inspect reminder and linked financial context.
- **Expected:** Reminder targets intended user, amount/status wording is correct and content is privacy-minimized.
- **PASS evidence required:** Video/screenshots/API evidence.
- **Automated proof required:** Cypress financial-notification flow or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-007 — Overdue-task or scheduled reminder behavior is correct where delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; FR-03; NFR-03
- **Execute:** Use deterministic due-time fixture and trigger delivered scheduler/reminder behavior.
- **Expected:** Reminder appears at correct time/state exactly once; no false overdue notification.
- **PASS evidence required:** Video/API/time evidence.
- **Automated proof required:** Cypress/integration time-bound reminder test or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-008 — Reminder content is privacy-safe in list, badge and preview
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-02
- **Execute:** Inspect list, card, toast, browser title, mobile preview, screen-reader text, console and any desktop/browser notification UI.
- **Expected:** No password/JWT/raw token/full private financial/task description or other user’s data is exposed beyond necessity.
- **PASS evidence required:** Screenshots/redacted console evidence.
- **Automated proof required:** Component copy/privacy test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-009 — Notification action/deep link works for every type
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-04
- **Execute:** Click/tap every notification and action button from list, toast and badge in logged-in, expired-session and deleted-resource scenarios.
- **Expected:** Correct target opens; missing/foreign resource gets safe controlled state; no dead link.
- **PASS evidence required:** Video/network evidence.
- **Automated proof required:** Cypress notification-deep-link matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-010 — Mark read/unread/dismiss actions work if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-03
- **Execute:** Use all exposed notification lifecycle controls, refresh, reopen and inspect badge/history.
- **Expected:** Canonical state persists and count updates exactly once; no duplicate/negative badge.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Component/Cypress read-state tests or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-011 — Bulk notification actions work safely if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-03; NFR-04
- **Execute:** Test select-all/mark-all/clear-all/retry controls with mixed read/unread and multiple user fixtures.
- **Expected:** Only current user’s intended notifications change; cancel/undo behavior is clear; no cross-user effect.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress bulk-action tests or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-012 — Notification list empty/loading/error states are usable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-04
- **Execute:** Use no notifications, unread only, loading, unauthorized, network/server-error fixtures.
- **Expected:** State is clear; retry/next action is safe; no stale old notifications remain.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Component state tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-013 — Duplicate notifications are not rendered after retry/reload
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-03
- **Execute:** Trigger same event with delayed response/retry, refresh/back and use two tabs.
- **Expected:** One logical notification shown; UI reconciles server truth.
- **PASS evidence required:** Video/request trace.
- **Automated proof required:** Cypress dedup/reconciliation test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-014 — Existing invited user is not duplicated in reminders — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-11; NFR-03
- **Execute:** Invite an existing account, trigger delivered reminder event, inspect badge/list/deep link.
- **Expected:** Exactly one user notification/action; no duplicated unread count.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #35 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-015 — Rejoined user is not duplicated in reminders — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-11; NFR-03
- **Execute:** Trigger reminder before/after leave and rejoin same account; inspect list/count/history.
- **Expected:** No duplicate notification identity/count/target state and no 500.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #38 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-016 — Anonymized/deleted actor in reminder displays safely — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-11; NFR-02
- **Execute:** Trigger/view reminder referring to task/expense/payment created by user who later leaves/deletes/anonymizes.
- **Expected:** Privacy-safe label/summary, no null crash/PII leak or broken deep link.
- **PASS evidence required:** Before/after screenshots.
- **Automated proof required:** Cypress cross-feature regression linked to BUG #42.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## B. FR-12 — Dashboard with current responsibilities, payment status and active context

### FE-017 — Dashboard route is protected and correct for each membership state
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; NFR-01; NFR-04
- **Execute:** Open dashboard as logged out, new no-WG user, Main Tenant, Roommate, former member and direct URL.
- **Expected:** Only correct current household dashboard appears; no foreign/stale data flash.
- **PASS evidence required:** Video, route matrix.
- **Automated proof required:** Route-guard/Cypress matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-018 — Dashboard has clear loading, error and empty states
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; NFR-04
- **Execute:** Use empty household, no tasks/no expenses/no shopping/no reminders, loading, 401/403/500/offline fixtures.
- **Expected:** User can understand state and next action; no blank screen/raw error/stale card.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Component state tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-019 — Current responsibilities are complete and role-relevant
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; FR-03; NFR-04
- **Execute:** Create assigned open, completed, overdue, future and unassigned tasks for several users; inspect dashboard as each.
- **Expected:** Dashboard shows exactly responsibilities intended for current user/role; no unrelated/foreign task.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Component/Cypress responsibility matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-020 — Dashboard task counts and labels equal source task state
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; FR-03; NFR-03
- **Execute:** Compare dashboard cards/counts to underlying task list after create, assign, complete, undo and recurrence event.
- **Expected:** Counts/status labels update correctly; no stale/duplicated task metric.
- **PASS evidence required:** Before/after evidence.
- **Automated proof required:** Cypress dashboard-task-refresh regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-021 — Dashboard payment/open-balance status equals canonical finance data
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; FR-06; NFR-03
- **Execute:** Create equal/unequal expenses, partial/full payments, edit/delete permitted expense, refresh dashboard and balance page.
- **Expected:** Open balance/payment status matches canonical engine exactly; no stale/float artifact/negative zero.
- **PASS evidence required:** Screenshots/API comparison.
- **Automated proof required:** Cypress dashboard-balance regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-022 — Dashboard handles equal-split edit correctly — BUG #31
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #31; FR-12; FR-16
- **Execute:** Edit equal-split expense amount/participants and inspect dashboard balance/payment cards and charts.
- **Expected:** Corrected values appear once; no stale amount or duplicated contribution.
- **PASS evidence required:** Before/after video/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #31 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-023 — Dashboard reflects expense update correctly — BUG #32
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #32; FR-12; FR-16
- **Execute:** Update each permitted expense field then inspect responsibility/payment/history/statistical cards.
- **Expected:** Dashboard reflects canonical new data after refresh/reload.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #32 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-024 — Same-name/amount expenses appear as two real records — BUG #37
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #37; FR-12; FR-13; FR-16
- **Execute:** Create two legitimate same-description/same-amount expenses and inspect cards/history/charts.
- **Expected:** Both contribute independently; no collapsed/hidden/missing record.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #37 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-025 — Payment does not freeze unrelated dashboard refresh — BUG #39
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #39; FR-12; NFR-03
- **Execute:** Record payment, then edit/delete unrelated permitted expense and refresh dashboard.
- **Expected:** Dashboard recomputes relevant cards/history/statistics; no global stale or blocked state.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #39 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-026 — Active reminder section reflects notification state
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; FR-11; NFR-03
- **Execute:** Create/read/dismiss delivered reminders, inspect dashboard reminder card/count/deep link.
- **Expected:** Dashboard reminder card equals notification source state; no stale badge/count.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Cypress dashboard-reminder sync test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-027 — Active shopping context is correct where delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; FR-07; FR-09; NFR-01
- **Execute:** Create personal/targeted/shared shopping items with statuses; inspect dashboard as every relevant actor.
- **Expected:** Only intended active shopping context appears; structured quantity/unit and visibility are respected.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Cypress dashboard-shopping-context test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-028 — Dashboard actions/quick links work
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; NFR-04
- **Execute:** Inventory every dashboard CTA/card/action/shortcut; click/tap with normal, missing-resource, expired-session and unauthorized fixtures.
- **Expected:** Every control opens correct route/state or shows safe error; no dead button.
- **PASS evidence required:** Completed inventory/video.
- **Automated proof required:** Dedicated Cypress dashboard-control suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-029 — Dashboard refresh, reload and browser navigation are stable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Perform mutations in other view, then refresh dashboard, use Back/Forward/direct URL and two tabs.
- **Expected:** Canonical latest state appears; no duplicate cards/stale counts/unauthorized cache.
- **PASS evidence required:** Video.
- **Automated proof required:** Cypress reload/cache reconciliation test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-030 — Existing invited user appears once in dashboard — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-12; NFR-03
- **Execute:** Invite existing user; assign task, create balance/reminder/shopping item; inspect dashboard.
- **Expected:** Exactly one responsibility/balance/reminder/member contribution for that user.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #35 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-031 — Rejoined user appears once in dashboard — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-12; NFR-03
- **Execute:** Create dashboard-relevant data, leave/rejoin user, inspect responsibilities/counts/balances/reminders.
- **Expected:** No duplicate row/card/count/contribution/500.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #38 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-032 — Anonymized/deleted member dashboard references are safe — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-12; NFR-02
- **Execute:** Use dashboard data referring to former/anonymized member; inspect balances, payment/reminder/history cards.
- **Expected:** Privacy-safe fallback appears; no null/PII/FK-related failure.
- **PASS evidence required:** Before/after screenshots.
- **Automated proof required:** Cypress cross-feature regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## C. FR-13 — History of tasks and payments UI

### FE-033 — History route and navigation are protected
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; NFR-01; NFR-04
- **Execute:** Open task/payment history routes as logged out, no-WG, current member, former member and direct URLs.
- **Expected:** Only authorized current household history appears; no stale/foreign history.
- **PASS evidence required:** Video/routes.
- **Automated proof required:** Route-guard/Cypress matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-034 — Task history shows correct lifecycle records
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; FR-03; NFR-06
- **Execute:** Create/assign/reassign/complete/undo/cancel recurring and manual tasks; inspect history.
- **Expected:** Entries accurately represent canonical actions, actor/time/status/task identity; no duplicates/missing events.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress task-history workflow.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-035 — Payment history shows correct financial lifecycle records
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; FR-06; NFR-06
- **Execute:** Create expense/payment/partial/full settlement and approval/reject if delivered; inspect history.
- **Expected:** Amounts/counterparties/status/time are correct; no real bank-transfer claim.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress payment-history workflow.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-036 — History ordering is stable and time-correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; NFR-03
- **Execute:** Create events close together and around timezone/day boundary; refresh/restart/filter.
- **Expected:** Ordering follows documented timestamp/secondary ordering; no swapped/duplicate entries.
- **PASS evidence required:** Screenshots/API/time evidence.
- **Automated proof required:** Component/Cypress order tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-037 — History detail links work and handle removed resources safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; NFR-04; NFR-03
- **Execute:** Open every history row/detail link, including deleted/cancelled/anonymized resource.
- **Expected:** Correct detail opens or safe unavailable state; no broken/dead link.
- **PASS evidence required:** Video.
- **Automated proof required:** Cypress history-deep-link matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-038 — History filters/search/date range work if exposed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; NFR-04
- **Execute:** Inventory type/status/actor/date/search filters, combine/reset/refresh/back.
- **Expected:** Results match wording; reset stable; no unrelated/foreign event.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Component/Cypress filter suite or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-039 — History pagination/infinite scroll works if exposed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; NFR-03; NFR-04
- **Execute:** Create enough synthetic history, load pages, change filter, refresh and inspect duplicates/gaps.
- **Expected:** Stable ordering with no missing/duplicate/foreign records.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress pagination test or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-040 — History respects household and role privacy
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; NFR-01; NFR-02
- **Execute:** Use two WGs/current/foreign/former users and private data fixture; compare history responses/UI.
- **Expected:** Only permitted household history; no secret/private/foreign content.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Cypress history-privacy matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-041 — Equal-split edit history is correct — BUG #31
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #31; FR-13; NFR-06
- **Execute:** Edit equal-split expense and inspect affected payment/expense history entries.
- **Expected:** History records correct canonical change once; old/new values or audit semantics follow policy; no stale duplicate.
- **PASS evidence required:** Before/after evidence.
- **Automated proof required:** Mandatory Cypress BUG #31 history regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-042 — Expense update history is correct — BUG #32
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #32; FR-13; NFR-06
- **Execute:** Update expense fields and inspect related history/dashboard chronology.
- **Expected:** Correct actor/time/action data; no generic failure/history gap.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #32 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-043 — Same-name/amount records remain distinguishable — BUG #37
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #37; FR-13
- **Execute:** Create two same-name/amount expenses and inspect history identifiers/context.
- **Expected:** Both records are represented distinctly; no accidental merge.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Mandatory Cypress BUG #37 history regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-044 — Payment-related unrelated edits retain history — BUG #39
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #39; FR-13; NFR-03
- **Execute:** Record payment then edit/delete unrelated permitted expense and inspect history.
- **Expected:** Action/history is recorded normally; no global lock/stale timeline.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory Cypress BUG #39 history regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-045 — Former/anonymized actors display privacy-safe history — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-13; NFR-02
- **Execute:** Delete/anonymize task actor/payment counterpart and inspect history cards/detail.
- **Expected:** Stable fallback identity without PII/null crash; timeline stays understandable.
- **PASS evidence required:** Before/after screenshots.
- **Automated proof required:** Mandatory Cypress BUG #42 history regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-046 — History empty/loading/error states are usable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; NFR-04
- **Execute:** Use no history, no match, loading, unauthorized, network/server-error states.
- **Expected:** Clear state/next action and no stale historical data.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Component state tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## D. FR-16 — Statistical history visualizations and data interpretation

### FE-047 — Statistical dashboard entry and delivery scope are truthful
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; NFR-04; NFR-05
- **Execute:** Locate charts, statistics filters, help text and docs; compare to delivered implementation.
- **Expected:** All delivered charts are accessible/testable; no predictive stock forecasting/LLM claim is made.
- **PASS evidence required:** Screenshots/docs.
- **Automated proof required:** UI/doc test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-048 — Expense category pie chart includes correct categories
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; TG-06
- **Execute:** Create expenses in operating-cost, food, electricity and other delivered categories; inspect pie chart/legend/table.
- **Expected:** Category totals and labels match canonical expense data; zero/missing categories handled clearly.
- **PASS evidence required:** Chart screenshots/API evidence.
- **Automated proof required:** Component chart-data mapping tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-049 — Category chart percentages/totals are mathematically correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; TG-06; NFR-03
- **Execute:** Use fixtures with known category totals, same-name expenses and rounding boundaries.
- **Expected:** Slice values/percentages sum correctly under documented rounding; no double count/float artifact.
- **PASS evidence required:** Expected-vs-actual table.
- **Automated proof required:** Component/Cypress chart-math regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-050 — Spending development time series uses correct time buckets
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; TG-06
- **Execute:** Create expenses across days/weeks/months/timezone boundaries; apply every delivered range/granularity filter.
- **Expected:** Points/buckets equal canonical data and timezone policy; no off-by-one/missing period.
- **PASS evidence required:** Chart/API comparison.
- **Automated proof required:** Component/Cypress time-series tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-051 — Top buyer/user contribution statistics are correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; TG-06
- **Execute:** Create known purchases by several users including tie, deleted/anonymized and rejoined fixtures.
- **Expected:** Ranking/contribution matches source records and documented tie/fallback policy; no duplicate user contribution.
- **PASS evidence required:** Chart/table/API evidence.
- **Automated proof required:** Cypress top-buyer scenarios.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-052 — Shopping history statistics are correct where delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; FR-09; TG-06
- **Execute:** Create structured shopping lifecycle with units/statuses/actors; inspect any shopping charts/cards.
- **Expected:** Statistics match canonical history and visibility policy; no free-text quantity/unit ambiguity.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Component/Cypress shopping-statistics test or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-053 — Statistics update after expense create/edit/delete/payment changes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; FR-05; FR-06; NFR-03
- **Execute:** Create, edit, delete permitted expense, record payment and refresh charts/cards.
- **Expected:** Charts/totals update according to intended source data; payment affects only metrics it should affect.
- **PASS evidence required:** Before/after evidence.
- **Automated proof required:** Cypress chart-refresh regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-054 — Equal split edit updates charts correctly — BUG #31
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #31; FR-16
- **Execute:** Edit equal-split expense and inspect category/time/user charts.
- **Expected:** Corrected amount/contribution appears once; no stale/double total.
- **PASS evidence required:** Before/after chart/API proof.
- **Automated proof required:** Mandatory Cypress BUG #31 statistics regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-055 — Expense update updates charts correctly — BUG #32
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #32; FR-16
- **Execute:** Update category/amount/date/payer where allowed and inspect every relevant chart.
- **Expected:** All affected charts recompute accurately; unaffected chart data unchanged.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #32 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-056 — Same-name/amount expenses both contribute — BUG #37
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #37; FR-16
- **Execute:** Create two legitimate identical-description/amount expenses and inspect totals/time series/top buyer.
- **Expected:** Both contribute as separate events; no collapsed chart total.
- **PASS evidence required:** Chart/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #37 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-057 — Payment does not globally freeze statistical refresh — BUG #39
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #39; FR-16; NFR-03
- **Execute:** Record payment, edit/delete unrelated permitted expense and inspect statistics.
- **Expected:** Metrics refresh normally from source records; no global stale/blocked dashboard.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #39 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-058 — Former/anonymized contributor displays safely — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-16; NFR-02
- **Execute:** Delete/anonymize user with expense/shopping contribution; inspect charts/tooltips/legends/tables.
- **Expected:** Privacy-safe fallback, no PII/null crash; totals stay correct.
- **PASS evidence required:** Before/after screenshots/API evidence.
- **Automated proof required:** Mandatory Cypress BUG #42 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-059 — Rejoined/existing user does not duplicate statistics — BUG #35/#38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; BUG #38; FR-16
- **Execute:** Invite existing/rejoin fixture, create contribution events and inspect top-buyer/member charts.
- **Expected:** One logical contributor/count; no duplicate bars/slices/points or 500.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory Cypress membership-statistics regressions.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-060 — Chart filters and date ranges work if exposed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; NFR-04
- **Execute:** Inventory filters/range controls; combine/reset/refresh/back/direct URL.
- **Expected:** Data matches selected scope; reset stable; no stale query.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Component/Cypress filter tests or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-061 — Charts provide accessible alternative representation
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; NFR-04/WAI
- **Execute:** Use keyboard/screen reader/zoom; inspect data table, accessible text, legend/tooltips and non-colour distinction.
- **Expected:** Every chart has usable textual/tabular equivalent or accessible summary; no colour-only meaning.
- **PASS evidence required:** Axe/accessibility tree/screenshots.
- **Automated proof required:** Component accessibility test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-062 — Chart empty/loading/error states are accurate
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; NFR-04
- **Execute:** Use no-data/zero-only/loading/failed API/range with no results fixtures.
- **Expected:** No misleading chart/NaN/divide-by-zero; next step clear.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Component state tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## E. All applicable NFRs — controls, accessibility, compatibility, docs and bug gate

### FE-063 — Every UC-05 button, link, icon, menu and dialog works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04; explicit user requirement
- **Execute:** Inventory every notification, dashboard, history and chart control across role/state/modal/error screens; activate each.
- **Expected:** No dead control or false success; disabled/hidden state is clear and accurate.
- **PASS evidence required:** Completed control inventory/video.
- **Automated proof required:** Dedicated Cypress control-inventory spec.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-064 — All UC-05 error messages are safe and actionable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03; NFR-04
- **Execute:** Trigger validation, 401, 403, 404, 409, 500, offline, timeout, deleted-resource and stale-chart errors.
- **Expected:** No SQL/stack/internal ID/secret/foreign/private data; user gets next step.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Service/component error-mapping tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-065 — No frontend logging leaks private notification/history/chart data
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02
- **Execute:** Search source and capture console/telemetry during normal and failing UC-05 flows.
- **Expected:** No password/JWT/raw token/unnecessary task or financial history/PII appears.
- **PASS evidence required:** Static search/redacted console evidence.
- **Automated proof required:** Static scan/manual review.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-066 — WAI/Lighthouse/axe checks pass
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Audit notification drawer/list, dashboard cards, history filters/details, charts/tooltips/data tables and dialogs.
- **Expected:** No critical accessibility blocker; findings fixed/escalated.
- **PASS evidence required:** Audit report.
- **Automated proof required:** Repeatable axe/Lighthouse run.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-067 — Keyboard-only UC-05 workflow works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Open/read/dismiss notifications, inspect dashboard, filter history and operate chart range/filter/data-table controls using keyboard only.
- **Expected:** No mouse/chart-hover-only blocker; focus/dialogs safe.
- **PASS evidence required:** Video.
- **Automated proof required:** Accessibility/E2E proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-068 — Reference browser/device matrix passes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04; TG-08
- **Execute:** Run core dashboard/history/notification/chart flows on Chrome/Firefox 1366×768+, iPhone Safari 390×844, Pixel Chrome 412×915 and 360px.
- **Expected:** No clipped card/chart/filter/notification control, overflow, keyboard/touch/browser blocker.
- **PASS evidence required:** Screenshots/video/device versions.
- **Automated proof required:** Cypress viewport plus manual-device evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-069 — Zoom/text scaling remains usable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Use 200% zoom/text scaling on dashboards, charts, notification drawer and history lists.
- **Expected:** Critical totals, controls and data alternatives remain reachable.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Manual accessibility evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-070 — Peer tester completes UC-05 workflow unaided
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Ask non-implementer to find responsibilities/open balance/reminders, use history and explain a chart; use notifications if delivered.
- **Expected:** Peer succeeds; confusion/blockers fixed or documented.
- **PASS evidence required:** Peer test record.
- **Automated proof required:** Manual acceptance evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-071 — Frontend build, test, lint and type checks pass
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-05; TG-05
- **Execute:** Run actual frontend commands.
- **Expected:** All pass; no ignored critical test.
- **PASS evidence required:** Exact command output.
- **Automated proof required:** CI job evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-072 — UC-05 user documentation matches current UI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-05; TG-10
- **Execute:** Follow docs verbatim for dashboard/history/notifications/statistics and their limits.
- **Expected:** No obsolete control/rule/false push/prediction claim.
- **PASS evidence required:** Documentation execution record.
- **Automated proof required:** Manual doc acceptance test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-073 — All found-bug regressions are green after UC-05 changes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** Bugs #31/#32/#35/#36/#37/#38/#39/#42
- **Execute:** Run focused frontend/E2E tests for every found bug after changes.
- **Expected:** No existing fix regresses; every command/test name recorded.
- **PASS evidence required:** Test output.
- **Automated proof required:** Mandatory regression gate.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-074 — Final frontend sign-off is evidence-complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** All applicable FR/NFR
- **Execute:** Review every FE check, test, bug gate and evidence record.
- **Expected:** No critical PASS lacks proof; unresolved issue remains visible.
- **PASS evidence required:** Signed review.
- **Automated proof required:** Reviewer sign-off.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

