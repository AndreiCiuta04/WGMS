# UC-05 — BACKEND QA FORM

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

**Spring Boot notifications/reminders, dashboard aggregations, task/payment history, statistical-history services, H2/JPA persistence, security, auditability, OpenAPI, CI and documentation.**

This form owns server-side enforcement for FR-11, FR-12, FR-13 and FR-16 plus NFR-01, NFR-02, NFR-03, NFR-05 and NFR-06.


## A. FR-11 — Notifications and reminders backend, lifecycle and reliability

### BE-001 — FR-11 backend delivery state is explicit
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11 extended; NFR-05
- **Execute:** Locate notification/reminder controllers, services, entities, repositories, scheduler/jobs, OpenAPI and docs. If absent/prototype, validate truthful limitation.
- **Expected:** Actual trigger/channel/read-state model is identifiable; no false email/push/SMS claim.
- **PASS evidence required:** Code/docs/OpenAPI map.
- **Automated proof required:** Test or justified N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-002 — Notification endpoints require valid authentication
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-01
- **Execute:** Enumerate every delivered notification/reminder endpoint; call without/malformed/expired JWT.
- **Expected:** Safe 401 and no notification data/mutation.
- **PASS evidence required:** Endpoint matrix/responses.
- **Automated proof required:** Parameterized security integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-003 — Notification API matches OpenAPI contract
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; TG-04
- **Execute:** Compare live methods/paths/security/DTO/status/error behavior to served OpenAPI.
- **Expected:** Implementation and specification agree.
- **PASS evidence required:** Spec/API comparison.
- **Automated proof required:** Contract tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-004 — Notification recipient derives from trusted server-side event context
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-01; TG-01
- **Execute:** Attempt to inject/replace recipientId, householdId, actorId, notification type/status in request where endpoints exist.
- **Expected:** Server derives recipient/household from canonical task/expense/membership/event state; forged authority fails.
- **PASS evidence required:** Request/DB evidence.
- **Automated proof required:** Authorization/mass-assignment tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-005 — Every implemented notification trigger is enumerated and tested
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-03
- **Execute:** Discover all trigger sources (new task, expense, overdue, manual reminder, etc.) and generate each with controlled fixtures.
- **Expected:** Each documented trigger yields correct intended notification exactly once; unsupported triggers do not create phantom alerts.
- **PASS evidence required:** Trigger matrix/DB/API evidence.
- **Automated proof required:** Parameterized service/integration trigger suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-006 — Task-based reminders target only intended active member
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; FR-03; NFR-01
- **Execute:** Assign/reassign/complete/undo tasks with current, foreign, former, deleted/anonymized and rejoined members.
- **Expected:** Only correct eligible recipient receives reminder; no foreign/duplicate notification.
- **PASS evidence required:** DB/API matrix.
- **Automated proof required:** Integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-007 — Expense/payment-based reminders target only intended member
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; FR-05; FR-06; NFR-01
- **Execute:** Create expense/payment fixtures with multiple participants, partial/full status and former members.
- **Expected:** Only intended current/fallback recipient gets permitted reminder; no financial leakage.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-008 — Scheduled/overdue reminder timing is deterministic where delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-03
- **Execute:** Use injected/fixed clock around task due/reminder boundary, including retry/restart.
- **Expected:** Exactly one timely reminder per rule; no early/late/duplicate alert.
- **PASS evidence required:** Time-boundary fixture evidence.
- **Automated proof required:** Unit/integration tests with injectable clock.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-009 — Notification deduplication/idempotency is reliable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-03
- **Execute:** Trigger same source event repeatedly/concurrently or retry after transport failure.
- **Expected:** One logical notification per documented event/recipient; no duplicate unread count.
- **PASS evidence required:** DB/API/concurrency evidence.
- **Automated proof required:** Unique-key/service/concurrency tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-010 — Notification persistence is atomic with source action where required
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Force failures during task/expense event + notification creation/audit handling.
- **Expected:** No inconsistent source state or orphan/phantom notification; transaction behavior documented.
- **PASS evidence required:** DB assertions.
- **Automated proof required:** Transactional integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-011 — Notification read/unread/dismiss state is user-scoped
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-01; NFR-03
- **Execute:** Mark read/unread/dismiss as recipient, same-WG other user, foreign user and concurrent sessions.
- **Expected:** Only recipient can change own state; canonical count/state persists without race.
- **PASS evidence required:** Response/DB matrix.
- **Automated proof required:** Role/concurrency integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-012 — Bulk notification operations are user-scoped if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-01; NFR-03
- **Execute:** Test mark-all/clear-all as recipient and non-recipient, mixed state and concurrent requests.
- **Expected:** Only caller’s permitted records change; no cross-user/household mutation.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Integration tests or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-013 — Notification read/list queries enforce household/user privacy
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-01; NFR-02
- **Execute:** Use two WGs and multiple users; query notification list/detail/count/deep-link metadata.
- **Expected:** No other user or household notification data/content/count leaks.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** IDOR/privacy integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-014 — Notification content is minimized and sanitized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-02
- **Execute:** Inspect persisted payload, DTO, rendered strings, logs and error cases for task/expense/payment/reminder fields.
- **Expected:** Only necessary summary/IDs are stored/returned; no JWT/password/raw token/excess private detail.
- **PASS evidence required:** DB/response/log evidence.
- **Automated proof required:** Serialization/log-scan tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-015 — Existing-user invitation creates no duplicate notifications — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-11; NFR-03
- **Execute:** Invite existing account, trigger notification source and inspect recipient rows/count.
- **Expected:** Exactly one notification identity/recipient; no duplicated unread count.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-016 — Leave/rejoin creates no duplicate notifications — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-11; NFR-03
- **Execute:** Trigger event before/after leave/rejoin, inspect notification rows/read state.
- **Expected:** No duplicate recipient/message/count or 500; membership policy respected.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory repository + integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-017 — Anonymized/deleted reference in notification is safe — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-11; NFR-02
- **Execute:** Create notification referring to actor/counterparty later anonymized/deleted; retrieve list/detail.
- **Expected:** Privacy-safe fallback/no null error/no PII leak.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory cross-feature regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-018 — Notification actions and lifecycle are auditable where implemented
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-11; NFR-06
- **Execute:** Create delivered reminders and mark/dismiss them; inspect audit records.
- **Expected:** Required actor/time/action trace exists without secret/private payload.
- **PASS evidence required:** Audit evidence.
- **Automated proof required:** Integration audit test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-019 — Notification errors are sanitized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Trigger validation/auth/constraint/scheduler/unexpected errors.
- **Expected:** Safe status/body; no SQL/stack/class/secret.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception-handler tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## B. FR-12 — Dashboard aggregation backend and current responsibilities

### BE-020 — Dashboard endpoints require valid authentication
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; NFR-01
- **Execute:** Enumerate dashboard/summary endpoints and call without/malformed/expired JWT.
- **Expected:** Safe 401 and no household summary data.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** Security integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-021 — Dashboard API matches OpenAPI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; TG-04
- **Execute:** Compare route/security/DTO/error contracts to served OpenAPI.
- **Expected:** Spec and live behavior agree.
- **PASS evidence required:** Spec/API evidence.
- **Automated proof required:** Contract tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-022 — Dashboard derives household from authenticated membership
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; NFR-01; TG-01
- **Execute:** Inject householdId/userId/role query/body/header values or foreign IDs.
- **Expected:** Server scopes dashboard to caller’s current household/current membership; forged values fail/ignore.
- **PASS evidence required:** Request/DB evidence.
- **Automated proof required:** Authorization/mass-assignment tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-023 — Dashboard current-responsibility query is correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; FR-03
- **Execute:** Create assigned/unassigned/open/completed/overdue/future/recurring tasks for multiple users.
- **Expected:** Returned responsibilities exactly match documented current-user/role policy; no missed/duplicate/foreign task.
- **PASS evidence required:** Expected-vs-actual table.
- **Automated proof required:** Repository/service/integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-024 — Dashboard task counts derive from canonical task state
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; FR-03; NFR-03
- **Execute:** Create/assign/complete/undo/cancel/recur tasks and query dashboard after each.
- **Expected:** Counts/statuses update exactly; no cache/stale/duplicate metric.
- **PASS evidence required:** Before/after DB/API evidence.
- **Automated proof required:** Integration refresh regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-025 — Dashboard balance/payment status derives from finance engine
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; FR-06; NFR-03
- **Execute:** Create equal/unequal expenses, partial/full payments, valid edits/deletes and query summary.
- **Expected:** Open balance/payment status matches canonical BigDecimal engine exactly; no stale source state.
- **PASS evidence required:** Expected-vs-actual table.
- **Automated proof required:** Integration finance-dashboard tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-026 — Equal split edit refreshes dashboard correctly — BUG #31
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #31; FR-12; FR-16
- **Execute:** Edit equal split amount/participants and query dashboard cards/summary.
- **Expected:** Correct new values exactly once; no stale/double balance/card.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-027 — Expense update refreshes dashboard correctly — BUG #32
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #32; FR-12; FR-16
- **Execute:** Update all permitted expense fields and query dashboard.
- **Expected:** Canonical summary reflects all change dimensions; no failure/stale data.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-028 — Same-name/amount expenses both contribute — BUG #37
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #37; FR-12; FR-13; FR-16
- **Execute:** Create two legitimate same-description/amount expenses, query dashboard/history/statistics summary.
- **Expected:** Both records contribute independently; no false uniqueness/collapse.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-029 — Payment does not globally freeze dashboard refresh — BUG #39
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #39; FR-12; NFR-03
- **Execute:** Record payment then update/delete unrelated permitted expense; query dashboard.
- **Expected:** Dashboard recomputes normally; no global lock/stale state.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-030 — Dashboard active reminder aggregation is correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; FR-11; NFR-03
- **Execute:** Create/read/dismiss delivered notifications and query dashboard summary.
- **Expected:** Active reminder count/cards equal notification canonical state; no stale/foreign count.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Integration synchronization test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-031 — Dashboard shopping context is correct where delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; FR-07; FR-08; FR-09; NFR-01
- **Execute:** Create personal/targeted/shared statuses and query dashboard as each user.
- **Expected:** Only permitted active shopping context included; structured quantity/unit preserved.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Integration shopping-dashboard tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-032 — Dashboard reads block cross-WG access
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Use WG A user against any dashboard IDs/parameters/resources for WG B.
- **Expected:** No foreign summary/data/existence leak.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** IDOR security suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-033 — Dashboard query handles no-data/empty states safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-12; NFR-03
- **Execute:** Use household with no tasks/expenses/payments/shopping/reminders; query all endpoints.
- **Expected:** Stable zero/empty DTO, no null/NaN/exception/foreign default data.
- **PASS evidence required:** Responses.
- **Automated proof required:** Integration empty-state tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-034 — Dashboard query is consistent after restart/seed restore
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03; TG-05
- **Execute:** Create data, restart/reset per docs, query dashboard and compare source records.
- **Expected:** Summary state survives/restores exactly as documented; no duplicated cache/records.
- **PASS evidence required:** Commands/API/DB evidence.
- **Automated proof required:** Restart/seed integration test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-035 — Existing invited user is counted once — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; FR-12; NFR-03
- **Execute:** Invite existing account, create dashboard-relevant task/expense/reminder/shopping record, query dashboard.
- **Expected:** One member contribution/responsibility/count; no duplicate rows/nodes.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-036 — Rejoined user is counted once — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #38; FR-12; NFR-03
- **Execute:** Create relevant data, leave/rejoin member, query dashboard.
- **Expected:** No duplicate responsibilities, balances/reminders/member counts or 500.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-037 — Dashboard uses privacy-safe fallback for former/anonymized users — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-12; NFR-02
- **Execute:** Use task/expense/payment/reminder records linked to later deleted/anonymized member; query dashboard.
- **Expected:** No FK/null failure/PII leak; stable fallback and correct totals.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory cross-feature regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-038 — Dashboard response is privacy-minimized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; TG-04
- **Execute:** Inspect dashboard DTO, nested member/task/payment/reminder fields and errors.
- **Expected:** Only needed data returned; no raw private history/secrets/foreign details.
- **PASS evidence required:** Response inventory.
- **Automated proof required:** Serialization/contract test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-039 — Dashboard aggregation errors are sanitized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Trigger malformed filter/range, database/mapper/unexpected failures if applicable.
- **Expected:** Safe status/schema; no SQL/stack/class/secret.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception-handler tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## C. FR-13 — Task and payment history backend, audit and privacy

### BE-040 — History endpoints require valid authentication
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; NFR-01
- **Execute:** Enumerate task/payment history/audit endpoints; call unauthenticated/invalid token.
- **Expected:** Safe 401 and no historical data.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** Security integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-041 — History API matches OpenAPI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; TG-04
- **Execute:** Compare methods/paths/security/DTO/filter/error definitions to live API.
- **Expected:** Specification and implementation agree.
- **PASS evidence required:** Spec/API proof.
- **Automated proof required:** Contract tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-042 — Task lifecycle history captures canonical actions
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; FR-03; NFR-06
- **Execute:** Create/assign/reassign/complete/undo/cancel/recur tasks and inspect stored/query history.
- **Expected:** Correct action/actor/time/resource/status information; no duplicate/missing history event.
- **PASS evidence required:** DB/API table.
- **Automated proof required:** Service/integration audit-history tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-043 — Payment lifecycle history captures canonical actions
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; FR-06; NFR-06
- **Execute:** Create expense/payment/partial/full settlement and approval/reject if delivered; inspect history.
- **Expected:** Correct amount/counterparty/action/time/status; no real-transfer claim.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Integration history tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-044 — History ordering is deterministic and time-correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; NFR-03
- **Execute:** Create events with same/close timestamps and timezone boundaries; query/restart/requery.
- **Expected:** Documented stable ordering with deterministic tie-break; no swapped/duplicate result.
- **PASS evidence required:** Expected-vs-actual evidence.
- **Automated proof required:** Repository/service/integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-045 — History read is scoped to caller household/permissions
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; NFR-01; NFR-02
- **Execute:** Use two WGs/current/foreign/former identities and historical IDs.
- **Expected:** No foreign/private history/existence leak.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** IDOR/privacy integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-046 — History filter/query values validate safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; NFR-01; NFR-03
- **Execute:** Fuzz type/status/actor/date/search/sort/page parameters and IDs.
- **Expected:** No SQL/parser error/broad data leak; controlled response.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Parameterized tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-047 — History pagination is stable if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-13; NFR-03
- **Execute:** Create enough history; fetch pages/filter/restart/insert concurrent event.
- **Expected:** No duplicate/gap/order instability beyond documented cursor policy.
- **PASS evidence required:** API/DB evidence.
- **Automated proof required:** Repository/integration pagination tests or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-048 — Equal-split edit has correct history trace — BUG #31
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #31; FR-13; NFR-06
- **Execute:** Edit equal-split expense and inspect expense/payment/audit history.
- **Expected:** One correct canonical event/change; no stale/duplicate split history.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-049 — Expense update has correct history trace — BUG #32
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #32; FR-13; NFR-06
- **Execute:** Update permitted expense fields and inspect history.
- **Expected:** Actor/time/action and changed canonical data appear correctly.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-050 — Same-name/amount history records remain distinct — BUG #37
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #37; FR-13
- **Execute:** Create two legitimate identical description/amount expenses and query history.
- **Expected:** Two distinct history/resource records; no merge/collision.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-051 — Payment does not block unrelated history generation — BUG #39
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #39; FR-13; NFR-03
- **Execute:** Record payment, then perform unrelated permitted expense update/delete and query history.
- **Expected:** Action is allowed and history generated; no global block/stale timeline.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-052 — Deleted/anonymized actors remain safe in history — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-13; NFR-02
- **Execute:** Delete/anonymize task actor/payment counterparty; query history/detail.
- **Expected:** Privacy-safe stable fallback; no PII/FK/null error and timeline still understandable.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory cross-feature regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-053 — History/audit payload is privacy-minimized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; NFR-06
- **Execute:** Inspect audit entity/DTO/logs after representative actions.
- **Expected:** Contains actor/action/time enough for audit but no password/JWT/raw invite/full sensitive payload.
- **PASS evidence required:** DB/response/log evidence.
- **Automated proof required:** Serialization/log-scan tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-054 — History is durable across restart and seed restore
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03; TG-05
- **Execute:** Create task/payment history, restart/reset per docs, query history.
- **Expected:** No loss/duplicate/ordering corruption beyond documented persistence mode.
- **PASS evidence required:** Commands/DB/API evidence.
- **Automated proof required:** Restart/seed integration test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-055 — History errors are sanitized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Trigger foreign/missing/filter/serialization/unexpected errors.
- **Expected:** Safe response; no SQL/stack/private records.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception-handler tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## D. FR-16 — Statistical aggregation backend, chart data and descriptive accuracy

### BE-056 — Statistical endpoint delivery and contract are explicit
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; TG-04; NFR-05
- **Execute:** Locate chart/statistics endpoints/services/DTOs/OpenAPI/docs and map all delivered visualizations.
- **Expected:** Delivered statistics are identifiable, documented and descriptive—not predictive forecasting.
- **PASS evidence required:** Code/docs/OpenAPI evidence.
- **Automated proof required:** Contract test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-057 — Statistics endpoints require valid authentication and household scope
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; NFR-01
- **Execute:** Call every statistics endpoint unauthenticated, invalid/expired, foreign/former/current user.
- **Expected:** No aggregate/chart data outside current WG; safe denial.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** Security integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-058 — Category totals are correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; TG-06
- **Execute:** Create known operating-cost/food/electricity/other expense fixtures; query category aggregate.
- **Expected:** Each category amount equals canonical eligible expense sum; total matches source data.
- **PASS evidence required:** Expected-vs-actual table.
- **Automated proof required:** Repository/service/integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-059 — Category percentages/rounding are correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; TG-06; NFR-03
- **Execute:** Use rounding-boundary totals and zero/single category fixture.
- **Expected:** Percentages/slices follow documented rounding and do not mis-sum due to float error.
- **PASS evidence required:** Fixture table/API evidence.
- **Automated proof required:** Unit tests for chart maths.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-060 — Time-series spending development uses correct buckets
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; TG-06
- **Execute:** Create expenses across days/weeks/months/timezone/month-end boundaries; query every supported range/granularity.
- **Expected:** Each bucket equals canonical source data under documented timezone boundary; no missing/double period.
- **PASS evidence required:** Expected-vs-actual table.
- **Automated proof required:** Repository/service/integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-061 — Top-buyer/contribution aggregate is correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; TG-06
- **Execute:** Create purchases by several users including ties, former/anonymized/rejoined users and same-name expenses.
- **Expected:** Ranking/contributions match canonical payer/purchase data; deterministic tie policy; no duplicate actor.
- **PASS evidence required:** Expected-vs-actual chart table.
- **Automated proof required:** Service/integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-062 — Shopping-history statistics are correct where delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; FR-09; TG-06
- **Execute:** Create structured shopping history with statuses/units/actors; query statistic endpoints.
- **Expected:** Data matches permitted canonical shopping history; visibility/privacy policy is preserved.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Repository/integration tests or N/A.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-063 — Statistics update after expense create/edit/delete/payment
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; FR-05; FR-06; NFR-03
- **Execute:** Create, update, delete permitted expense and record payment; query every affected aggregate.
- **Expected:** Metrics refresh from source records; payment affects only intended metric; no stale cache.
- **PASS evidence required:** Before/after DB/API evidence.
- **Automated proof required:** Integration refresh tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-064 — Equal-split edit updates statistics — BUG #31
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #31; FR-16
- **Execute:** Edit equal-split expense amount/participants and query category/time/top-buyer aggregates.
- **Expected:** Correct canonical totals once; no stale/double count.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-065 — Expense update updates statistics — BUG #32
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #32; FR-16
- **Execute:** Update category/amount/date/payer where allowed and query all relevant stats.
- **Expected:** Affected aggregates change accurately; unaffected aggregates stable.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-066 — Same-name/amount expenses both contribute to statistics — BUG #37
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #37; FR-16
- **Execute:** Create two legitimate identical description/amount records, query aggregates.
- **Expected:** Both independent records contribute; no collapsed total.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-067 — Payment does not freeze unrelated statistics refresh — BUG #39
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #39; FR-16; NFR-03
- **Execute:** Record payment then update/delete unrelated permitted expense and query statistics.
- **Expected:** Statistics recompute normally; no global stale/block state.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-068 — Former/anonymized contributor fallback is safe — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #42; FR-16; NFR-02
- **Execute:** Delete/anonymize user with expense/shopping contribution and query top-buyer/category/time data.
- **Expected:** Totals correct; privacy-safe label; no PII/null/FK failure.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory cross-feature regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-069 — Existing/rejoined users do not duplicate contribution — BUG #35/#38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** BUG #35; BUG #38; FR-16
- **Execute:** Invite existing/rejoin account, create contributions, query statistics.
- **Expected:** One logical contributor and amount; no duplicate graph/chart node/500.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory membership-statistics regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-070 — Statistics filters/range inputs validate safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; NFR-01; NFR-03
- **Execute:** Fuzz date range/granularity/category/user/status/query params and hostile bounds.
- **Expected:** No SQL/parser error/broad cross-WG data; controlled response.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Parameterized tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-071 — No-data/zero-data aggregates are stable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; NFR-03
- **Execute:** Query household/range with no expenses/shopping, zero totals, deleted-only data.
- **Expected:** Valid empty/zero DTO; no NaN/divide-by-zero/null exception.
- **PASS evidence required:** Responses.
- **Automated proof required:** Integration edge-case tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-072 — Statistics are deterministic across ordering/restart/cache
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** FR-16; NFR-03
- **Execute:** Insert logically equivalent fixtures in different order, repeat queries/restart/invalidate cache if used.
- **Expected:** Same canonical result/order/tie behavior; no stale cache.
- **PASS evidence required:** Repeated API/DB evidence.
- **Automated proof required:** Determinism/restart/cache tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-073 — Chart-data DTO is privacy-minimized and serializable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; TG-04
- **Execute:** Inspect category/time/top-buyer/shopping-stat response fields, including errors.
- **Expected:** Only needed aggregate/label data; no raw private expense/task detail/secrets.
- **PASS evidence required:** Response inventory.
- **Automated proof required:** Serialization/contract tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-074 — Statistical aggregation errors are sanitized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Trigger malformed range/filter, DB/mapper/cache/unexpected errors.
- **Expected:** Safe response; no stack/SQL/private aggregate leak.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception-handler tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## E. All applicable NFRs — security, privacy, reliability, maintainability, CI and bug gate

### BE-075 — Complete UC-05 authorization matrix exists
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Enumerate notifications, dashboard, history, statistics and any export endpoints; classify auth/member/role/state restrictions.
- **Expected:** No endpoint unclassified; test coverage exists for every class.
- **PASS evidence required:** Matrix evidence.
- **Automated proof required:** Parameterized security suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-076 — Every UC-05 read endpoint blocks cross-WG access
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Use WG A actor against all WG B dashboard/history/notification/statistics IDs/filters/resources.
- **Expected:** No foreign data/existence leak.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** Security integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-077 — Every UC-05 mutation endpoint blocks cross-WG writes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Use WG A actor against WG B notification read/dismiss/reminder/config/action resources.
- **Expected:** No mutation; DB unchanged.
- **PASS evidence required:** Endpoint/DB matrix.
- **Automated proof required:** Security suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-078 — All UC-05 DTOs block mass assignment
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Inject IDs, household, actor, recipient, read, dismissed, chart totals, date buckets, audit/status fields outside contract.
- **Expected:** No unauthorized ownership/state/visibility/data manipulation.
- **PASS evidence required:** Request/DB evidence.
- **Automated proof required:** Mass-assignment tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-079 — Hostile inputs and deep-link identifiers fail safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Fuzz notification IDs, history IDs, filters, date ranges, pagination, chart params and deep-link metadata.
- **Expected:** No parser/SQL/500/broad data leakage.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Parameterized tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-080 — Logs, notification content, history and chart responses are privacy-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; NFR-06
- **Execute:** Run normal/failing flows and scan logs/audit/DTOs.
- **Expected:** No password/JWT/raw token/excess task/financial details/PII/secret in logs or response.
- **PASS evidence required:** Redacted proof.
- **Automated proof required:** Log scan/static review.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-081 — Account lifecycle preserves required UC-05 history safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; TG-07; BUG #42
- **Execute:** Delete/anonymize creator, assignee, payer/payee, buyer and notification actor; query dashboard/history/stats.
- **Expected:** Privacy-safe fallback preserves correct explanation/totals; no FK/null/PII leak.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Lifecycle integration regression.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-082 — Critical UC-05 transactions are atomic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Review/fault-inject notification creation/read state, audit insertion and any dashboard cache/materialized projection updates.
- **Expected:** No partial source/notification/history/cache state on failure.
- **PASS evidence required:** Code/DB evidence.
- **Automated proof required:** Rollback tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-083 — Concurrency coverage exists for UC-05 races
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Run concurrent notification read/dismiss, duplicate trigger, dashboard/stat query while mutations occur, history/page reads.
- **Expected:** Final state valid/deterministic; no duplicate/unread count corruption/500.
- **PASS evidence required:** Output/DB proof.
- **Automated proof required:** Concurrent integration tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-084 — Seed/reset is reproducible with UC-05 fixtures
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03; TG-05
- **Execute:** From clean checkout execute reset/seed docs; verify responsibilities, payments, reminders, task/payment history and chart datasets.
- **Expected:** Known synthetic state restored with no hidden repair.
- **PASS evidence required:** Commands/DB/API evidence.
- **Automated proof required:** Seed smoke test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-085 — UC-05 persistence survives restart
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Create notification/history/dashboard-relevant records, restart as documented, inspect lists/counts/charts.
- **Expected:** State survives/restores as claimed; no duplicate/stale projection.
- **PASS evidence required:** Commands/API/DB evidence.
- **Automated proof required:** Restart integration test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-086 — Repository tests cover dashboard/history/statistical queries
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-03; QA strategy
- **Execute:** Locate/run repository tests for household scoping, ordering, filters, chart bucket/category aggregates, active notification counts and anonymized fallback.
- **Expected:** Real DB semantics proven.
- **PASS evidence required:** Test paths/output.
- **Automated proof required:** Mandatory repository evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-087 — Service tests cover UC-05 business/aggregation rules
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-05; TG-06; TG-09
- **Execute:** Locate/run unit tests for trigger/dedup timing, dashboard derivation, history ordering, category/time/top-buyer calculation and empty states.
- **Expected:** Rules tested independently of UI/E2E.
- **PASS evidence required:** Test paths/output.
- **Automated proof required:** Mandatory service test evidence.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-088 — API integration tests cover UC-05 data flow
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-09
- **Execute:** Run real auth/database workflows for notifications/dashboard/history/statistics and known bugs.
- **Expected:** Security, persistence and derived views proven together.
- **PASS evidence required:** Output.
- **Automated proof required:** Mandatory integration suite.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-089 — OpenAPI is served and current
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-04
- **Execute:** Start application and retrieve Swagger/OpenAPI; inspect all UC-05 operations.
- **Expected:** Specification available/current.
- **PASS evidence required:** URL/output.
- **Automated proof required:** OpenAPI smoke/contract test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-090 — CI runs all critical UC-05 layers
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-05; TG-09
- **Execute:** Inspect CI profiles/exclusions/allow_failure and pipeline execution.
- **Expected:** Unit/repository/integration/frontend/E2E dashboard tests run and failure blocks merge.
- **PASS evidence required:** CI YAML/job proof.
- **Automated proof required:** CI verification.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-091 — Architecture boundaries are clean
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-01; NFR-05
- **Execute:** Trace controllers → services → repositories/mappers and Angular boundary for notification/dashboard/history/statistics.
- **Expected:** No DB access outside repositories; no business rules wrongly in controller/UI; no duplicate aggregate source of truth.
- **PASS evidence required:** Architecture map.
- **Automated proof required:** Code review plus tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-092 — DTO mappers exclude internal fields
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** NFR-02; TG-04
- **Execute:** Inspect Notification/Dashboard/History/Statistics serialization and error DTOs.
- **Expected:** No internal security/audit/cache/private source fields leak beyond contract.
- **PASS evidence required:** Code/response proof.
- **Automated proof required:** Serialization tests.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-093 — UC-05 setup/API/user docs are current
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** TG-10; NFR-05
- **Execute:** Follow docs for notifications, dashboard, history, charts and delivery limitations.
- **Expected:** No obsolete route/false push/predictive claim/missing prerequisite.
- **PASS evidence required:** Documentation execution record.
- **Automated proof required:** Manual doc acceptance test.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-094 — All known-bug backend regressions are green
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** Bugs #31/#32/#35/#36/#37/#38/#39/#42
- **Execute:** Run focused backend/integration tests for every found bug after UC-05 changes.
- **Expected:** All remain green; test names/commands recorded.
- **PASS evidence required:** Output.
- **Automated proof required:** Mandatory regression gate.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-095 — Final backend sign-off is evidence-complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written, contract-based justification)*
- **Requirement(s):** All applicable FR/NFR/TG
- **Execute:** Review every BE item and automated proof.
- **Expected:** No critical PASS lacks proof; unresolved item remains visible.
- **PASS evidence required:** Signed review.
- **Automated proof required:** Reviewer sign-off.
- **If failed or evidence is missing:** Keep this item unchecked. Fix the defect or coverage gap, add the smallest focused regression test at the correct layer, rerun all affected suites, and record the exact changed files.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

