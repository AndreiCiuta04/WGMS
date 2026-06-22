# UC-05 — GENERAL / E2E QA FORM

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

**Integrated Angular + Spring Boot + H2 proof: notifications/reminders, dashboard responsibilities/payment status, task/payment history, statistical visualizations, privacy, multi-user reliability, browser/device/accessibility, peer testing, CI and all found-bug gates.**

This is the final system-level proof that FR-11, FR-12, FR-13, FR-16 and every applicable NFR-01..NFR-06 work together in the running application.


## A. Core FR-11 / FR-12 / FR-13 / FR-16 integrated journeys

### E2E-001 — Core dashboard current-responsibility journey
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12
- **Execute:** Use two users in one WG; create assigned/open/overdue/completed tasks, then open dashboard as each user.
- **Expected:** Each user sees exactly their intended current responsibilities and correct task state.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress dashboard-responsibility journey.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-002 — Core payment-status dashboard journey
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; FR-06
- **Execute:** Create equal/unequal expenses and partial/full payments; inspect dashboard and balance views as relevant users.
- **Expected:** Open balances/payment status are exact and consistent across views after refresh.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Cypress dashboard-payment journey.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-003 — Core active-reminder journey if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; FR-12
- **Execute:** Trigger every delivered reminder type, inspect notification list/badge/dashboard active reminders, open deep link and mark read/dismiss.
- **Expected:** One correct reminder state across all surfaces; no duplicated count/dead link.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress reminder-dashboard journey or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-004 — Core task and payment history journey
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13
- **Execute:** Create/assign/complete/undo task and create/record payment; inspect history, filters, deep links and refresh.
- **Expected:** Accurate actor/time/action chronology; no missing/duplicate events.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress history journey.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-005 — Core statistical-history journey
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16
- **Execute:** Create known operating-cost/food/electricity expenses, time-series entries and purchases by multiple users; inspect every delivered chart/data table.
- **Expected:** Pie categories, time series and top buyer match source data; charts remain descriptive, not predictive.
- **PASS evidence required:** Video/API/expected table.
- **Automated proof required:** Cypress statistics journey.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-006 — Equal split edit refreshes all UC-05 views — BUG #31
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #31; FR-12; FR-13; FR-16
- **Execute:** Edit equal-split expense amount/participants, then inspect dashboard balance, history and every affected chart.
- **Expected:** Correct values appear once across all UC-05 views; no stale/duplicate contribution.
- **PASS evidence required:** Before/after video/API proof.
- **Automated proof required:** Mandatory named Cypress BUG #31 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-007 — Expense update refreshes all UC-05 views — BUG #32
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #32; FR-12; FR-13; FR-16
- **Execute:** Update permitted expense fields including amount/category/date/payer where allowed, then inspect dashboard/history/charts.
- **Expected:** Every affected view reflects canonical update after refresh/reload.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #32 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-008 — Same-name/amount expenses remain distinct everywhere — BUG #37
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #37; FR-12; FR-13; FR-16
- **Execute:** Create two valid same-description/same-amount expenses and inspect dashboard/history/statistics.
- **Expected:** Both records exist and contribute independently; no conflict/collapse/missing chart contribution.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #37 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-009 — Payment does not freeze UC-05 refresh — BUG #39
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #39; FR-12; FR-13; FR-16
- **Execute:** Record payment, then update/delete unrelated permitted expense and inspect dashboard/history/charts.
- **Expected:** No global block/stale data; authorized action and downstream refresh work.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory named Cypress BUG #39 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-010 — Existing invited user appears once in all UC-05 data — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-11; FR-12; FR-13; FR-16
- **Execute:** Invite existing user, assign task/create expense/trigger reminder, inspect dashboard/history/stats.
- **Expected:** One logical user contribution/responsibility/reminder/history actor; no duplicates.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #35 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-011 — Leave/rejoin appears once in all UC-05 data — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-11; FR-12; FR-13; FR-16
- **Execute:** Create representative data, leave/rejoin account, inspect dashboard/reminders/history/charts.
- **Expected:** No duplicate rows/counts/chart contributor/history/vote-like identity or 500.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Mandatory named Cypress BUG #38 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-012 — Former/anonymized member is safe across UC-05 — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-11; FR-12; FR-13; FR-16; NFR-02
- **Execute:** Create task/expense/payment/reminder/history/contribution involving member, leave/delete/anonymize, inspect all UC-05 views.
- **Expected:** Privacy-safe fallback; totals/history/settlement context remain understandable; no null/PII/FK failure.
- **PASS evidence required:** Before/after evidence.
- **Automated proof required:** Mandatory cross-feature Cypress BUG #42 regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## B. Strict negative, security, privacy and reliability journeys

### E2E-013 — All UC-05 controls work in the integrated app
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04; explicit user requirement
- **Execute:** Inventory all notification/dashboard/history/chart buttons, links, icons, filters, tooltips, menus, dialogs, data tables and empty-state CTAs across roles/states; activate each.
- **Expected:** No dead control/false success; disabled/hidden state clear and accurate.
- **PASS evidence required:** Completed inventory/video.
- **Automated proof required:** Cypress control-inventory suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-014 — Notification lifecycle race/retry is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-03
- **Execute:** Double-trigger events, mark read/dismiss from two tabs, refresh/retry after network interruption.
- **Expected:** One canonical notification state/count; no duplicate/negative badge/500.
- **PASS evidence required:** Video/request trace.
- **Automated proof required:** Cypress notification reconciliation suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-015 — Dashboard/chart stale-data reconciliation is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; FR-16; NFR-03
- **Execute:** Mutate task/expense/payment in one tab, inspect dashboard/charts in another then refresh/reload/back.
- **Expected:** Latest canonical state appears; no permanent stale cache/duplicate cards.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress cache/reload regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-016 — Two-household UC-05 isolation works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Use WG A/B, direct URLs, tampered IDs/query filters, browser history and cross-user notification/history/chart access.
- **Expected:** No foreign dashboard/history/reminder/statistic data or mutation.
- **PASS evidence required:** Video/network/DB evidence.
- **Automated proof required:** Cypress two-household security suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-017 — Negative error matrix is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03; NFR-04
- **Execute:** Trigger validation, 401, 403, 404, 409, 500, offline, timeout, deleted-resource and stale notification/chart cases.
- **Expected:** Actionable safe UX; no SQL/stack/secret/private/foreign data/false current state.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Cypress error-mapping matrix.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-018 — Restart/reload/seed restore preserves UC-05 state
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03; TG-05
- **Execute:** Create notifications/history/dashboard/chart data, restart/reset per docs and inspect all views.
- **Expected:** State survives/restores as documented; no duplicate reminder/history/statistical record.
- **PASS evidence required:** Commands/video/API evidence.
- **Automated proof required:** Clean-environment/restart smoke.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-019 — Audit and privacy lifecycle work end-to-end
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; NFR-06; TG-07
- **Execute:** Create assignment/completion/undo/payment/reminder, leave/delete/anonymize fixture, inspect history/audit/dashboard/charts.
- **Expected:** Required actor/time trace with privacy-safe fallback; no secret/PII leak.
- **PASS evidence required:** Audit/before-after evidence.
- **Automated proof required:** Integration + Cypress linkage.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-020 — No UC-05 secrets or PII leak in browser/backend logs
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02
- **Execute:** Capture console/backend logs during normal and negative notification/dashboard/history/chart flows.
- **Expected:** No passwords/JWT/raw tokens/excess task/financial history/PII appears.
- **PASS evidence required:** Redacted logs.
- **Automated proof required:** Console guard + log scan.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-021 — Whole-project found-bug gate remains green
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** Bugs #31/#32/#35/#36/#37/#38/#39/#42
- **Execute:** Run all focused regression tests after UC-05 integration.
- **Expected:** Every found bug stays fixed; tests/commands/output recorded.
- **PASS evidence required:** CI/test output.
- **Automated proof required:** Mandatory regression gate.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## C. All applicable NFRs — usability, accessibility, compatibility, documentation, CI and final release gate

### E2E-022 — Peer tester completes UC-05 workflow unaided
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Ask non-implementer to find current responsibilities, open balance, reminder, task/payment history and interpret one chart/data table.
- **Expected:** Peer succeeds; confusion/blockers fixed or documented.
- **PASS evidence required:** Peer test record.
- **Automated proof required:** Manual acceptance evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-023 — WAI/Lighthouse/axe UC-05 quality gate
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Audit notification drawer/list, dashboard cards, history timeline/filters, chart tooltips/legends/data alternatives and dialogs.
- **Expected:** No critical accessibility blocker; findings resolved/escalated.
- **PASS evidence required:** Audit reports.
- **Automated proof required:** Repeatable axe/Lighthouse run.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-024 — Keyboard-only UC-05 workflow works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Open/read/dismiss delivered notification, inspect dashboard, filter history and operate chart data alternatives via keyboard only.
- **Expected:** No mouse/hover-only blocker; focus/dialogs safe.
- **PASS evidence required:** Video.
- **Automated proof required:** Manual + automated accessibility proof.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-025 — Reference browser/device matrix passes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04; TG-08
- **Execute:** Run core dashboard/history/notification/chart flow on Chrome/Firefox desktop 1366×768+, iPhone 12 Pro Safari 390×844, Pixel 7 Chrome 412×915 and 360px.
- **Expected:** No clipped card/chart/filter/notification control, overflow, keyboard/touch/browser blocker.
- **PASS evidence required:** Screenshots/video/browser versions.
- **Automated proof required:** Cypress viewport + manual-device tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-026 — Zoom/text scaling is usable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Run primary dashboard/history/chart operations at 200% zoom/text scale.
- **Expected:** Critical totals, data tables, controls and notification actions remain visible/reachable.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Manual accessibility evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-027 — OpenAPI and browser behavior agree
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-04
- **Execute:** Compare UC-05 network calls/errors to served OpenAPI for all core flows.
- **Expected:** No undocumented/deprecated mismatch.
- **PASS evidence required:** Network/OpenAPI proof.
- **Automated proof required:** Contract suite plus manual check.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-028 — CI executes all UC-05 layers
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-05; TG-09
- **Execute:** Review/run pipeline after UC-05 test change.
- **Expected:** Frontend/backend/repository/integration/E2E dashboard layers run and failure blocks merge.
- **PASS evidence required:** Pipeline evidence.
- **Automated proof required:** CI verification.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-029 — UC-05 setup/API/user documentation matches working flow
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-10; NFR-05
- **Execute:** Follow docs from clean environment for notifications, dashboard, history, charts and stated limitations.
- **Expected:** No obsolete steps/false push/prediction claim/missing prerequisite.
- **PASS evidence required:** Execution record.
- **Automated proof required:** Documentation acceptance test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-030 — Final integrated requirement sign-off
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** All FR-11/FR-12/FR-13/FR-16 and NFR-01..06
- **Execute:** Review all three UC-05 forms, evidence, commands and every bug gate before merge.
- **Expected:** No hidden critical failure/evidence gap; limitations remain visible.
- **PASS evidence required:** Signed review summary.
- **Automated proof required:** Reviewer sign-off.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

