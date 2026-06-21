# UC-02 — FRONTEND QA FORM

## Embedded execution instructions — no Project Proposal upload required

This file is self-contained for **UC-02**. Do **not** ask the user to upload, paste, or re-explain the Project Proposal.

Before checking any box, inspect the repository’s current Angular routes/components/services/tests; Spring controllers/services/entities/repositories/DTOs/security configuration/tests; H2 schema; OpenAPI; seed/reset tools; CI; and existing bug-fix branches.

**Strict rules**
1. Mark PASS only with the exact evidence and automated proof required below. “The code looks correct”, “it worked once”, “the button exists”, or “CI was green earlier” is not evidence.
2. If evidence or a test does not exist, create it. If a defect appears, fix it, add a regression test, rerun the relevant suite, then update the evidence.
3. Never weaken an expected result, delete/skip a critical test, use `|| true`, or hide FAIL/BLOCKED items.
4. Use synthetic data only. Do not place passwords, JWTs, raw invitation tokens, hashes, secrets, or real personal data in evidence.
5. A UC-02 merge is not complete while any critical requirement is unresolved.

## Binding UC-02 functional baseline

- **FR-03 — Task creation and assignment:** household members can create, assign and track household tasks/cleaning duties. Tasks support assignment to a roommate, completion tracking, undoing a mistaken completion, and recurrence.
- **FR-04 — Cleaning schedule management:** schedules support recurring behavior and task filtering by status.
- **FR-15 — Fair chore balancing via history:** extended feature. Where delivered, recommendations must use a documented, testable policy based on task history and must only consider eligible current household members. If prototype-only, the limitation must be truthful and documented.

## Binding non-functional baseline

- **NFR-01 Security & Access Control:** authenticated access to WG-specific data; backend-enforced household scoping, RBAC and input validation.
- **NFR-02 Data Privacy & GDPR:** data minimization; safe account leave/deletion/anonymization; privacy-safe historical task references; no secret/PII leakage.
- **NFR-03 Reliability:** durable task/schedule data, reproducible seed/reset, transactions, safe duplicates/races/restarts/error handling.
- **NFR-04 Usability & Compatibility:** peer-testable workflows; WAI/Lighthouse; Chrome/Firefox desktop 1366×768+, iPhone 12 Pro Safari 390×844, Pixel 7 Chrome 412×915, and 360px boundary.
- **NFR-05 Maintainability:** clean Angular/Spring/repository/business-rule separation, current documentation and tests.
- **NFR-06 Auditability:** task creation, assignment, completion/undo and material schedule changes are traceable by actor and timestamp where audit facility exists.

## Relevant technical goals

TG-01 three-tier architecture; TG-02 authenticated backend RBAC; TG-04 OpenAPI; TG-05 CI/CD; TG-07 GDPR-aligned lifecycle; TG-08 browser support; TG-09 automated task-scheduling coverage; TG-10 documentation.

## Mandatory found-bug regression gate

**Direct UC-02 integration**
- **BUG #35:** Existing user accepts an invitation. That user must appear exactly once in task assignee/fairness lists and see only correct household tasks.
- **BUG #38:** Leave then rejoin same WG. Rejoining must not duplicate task visibility, assignee options, fairness candidates or schedule references, and must not return 500.
- **BUG #42:** A member leaves/deletes/anonymizes. Existing tasks/history must remain readable through privacy-safe fallback data; no null crash or PII leak.

**Whole-project non-regression after UC-02 changes**
Run existing focused regression tests for **#31, #32, #35, #36, #37, #38, #39 and #42**. They are not all UC-02 features, but UC-02 changes must not re-break them.

## Layer scope

**Angular/browser layer: all task, cleaning-schedule and fair-chore UI behavior; every visible control; accessibility, responsive behavior, privacy messaging and frontend tests.**

This form owns visible FR-03/FR-04/FR-15 behavior and all UI-facing proof for NFR-01..NFR-06.


## A. FR-03 — Task creation, assignment and task input

### FE-001 — Task route access is correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-01; NFR-04
- **Execute:** Open all task routes as logged out, authenticated no-WG, Main Tenant, Roommate, former member and direct URL.
- **Expected:** Only valid members enter current household task context; no stale/foreign task flash.
- **PASS evidence required:** Video, route matrix, console capture.
- **Automated proof required:** Route-guard test plus Cypress route matrix.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-002 — Task list is household-scoped
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-01
- **Execute:** Use two WGs with distinct task fixtures, switch browser users, refresh/back.
- **Expected:** Only current-WG tasks appear; no cached task from prior user/WG.
- **PASS evidence required:** Screenshots and API comparison.
- **Automated proof required:** Component/service test plus two-household Cypress test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-003 — Task form matches current API contract
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; TG-04
- **Execute:** Compare every visible field, required marker, input type and request JSON with OpenAPI/DTO.
- **Expected:** All server-required fields are usable and serialized correctly.
- **PASS evidence required:** Screenshot, redacted request, OpenAPI reference.
- **Automated proof required:** Component/client-service request-shape test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-004 — Task fields have labels and accessible descriptions
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-04/WAI
- **Execute:** Inspect labels, ARIA names, required states, help and error associations for title, description, due date, assignee and all live fields.
- **Expected:** No placeholder-only field; each control is screen-reader understandable.
- **PASS evidence required:** Accessibility tree and axe output.
- **Automated proof required:** Component accessibility test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-005 — Task title/required validation is complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-04
- **Execute:** Submit empty, whitespace-only, min/max and boundary title cases; omit every required field separately.
- **Expected:** Invalid task is not submitted; precise field errors appear and are announced.
- **PASS evidence required:** Screenshots/network capture.
- **Automated proof required:** Parameterized form validation tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-006 — Task description and text rendering are safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-02
- **Execute:** Test Unicode, multiline, long, markup-like and special-character content.
- **Expected:** Allowed content displays safely; invalid content maps to clear error; no HTML/script rendering.
- **PASS evidence required:** DOM screenshots and response evidence.
- **Automated proof required:** Sanitization/validation component tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-007 — Due-date input and display are unambiguous
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-03; NFR-04
- **Execute:** Test missing, malformed, past/current/future, timezone boundary and max/min values according to backend rule.
- **Expected:** No off-by-one date; invalid values are blocked or mapped accurately.
- **PASS evidence required:** Screenshots, request/response.
- **Automated proof required:** Date-boundary form tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-008 — Assignee selector lists exactly eligible members
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-01; NFR-03
- **Execute:** Use active, foreign, former, deleted/anonymized and rejoined users across two WGs.
- **Expected:** Only active current-WG members appear once; no foreign/former option.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Selector mapping test plus Cypress household isolation.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-009 — Existing-invited user appears once — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #35; FR-03; NFR-03
- **Execute:** Invite an already-existing account, then open task creator and assign it work.
- **Expected:** The person appears once and can be assigned normally; no duplicate membership/task rows.
- **PASS evidence required:** Video/API/DB count.
- **Automated proof required:** Mandatory named Cypress regression for BUG #35.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-010 — Rejoined user appears once — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #38; FR-03; NFR-03
- **Execute:** Join, leave, rejoin same account, then inspect assignee list/tasks/fairness UI.
- **Expected:** No duplicate assignee/task visibility/candidate and no raw 500 message.
- **PASS evidence required:** Video/screenshots/API evidence.
- **Automated proof required:** Mandatory named Cypress regression for BUG #38.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-011 — Anonymized/deleted task reference renders safely — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #42; NFR-02; NFR-03
- **Execute:** Create task/history for user who later leaves/deletes/anonymizes; reopen task screens.
- **Expected:** Privacy-safe fallback appears; no null crash, blank row or PII leak.
- **PASS evidence required:** Before/after screenshots.
- **Automated proof required:** Cypress cross-feature regression linked to BUG #42.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-012 — Create-task action prevents duplicates
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Double-click/Enter repeatedly under throttled network; refresh/back around request.
- **Expected:** One intended task; pending state prevents duplicates; UI reconciles server truth.
- **PASS evidence required:** Video/request count.
- **Automated proof required:** Pending-state test plus Cypress duplicate-submit test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-013 — Task creation error recovery works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; NFR-04
- **Execute:** Simulate 400/401/403/404/409/500/offline/timeout and retry.
- **Expected:** Actionable safe message, no stack/SQL, no phantom task; correction/retry works.
- **PASS evidence required:** Screenshots/video/network trace.
- **Automated proof required:** Service/component error-mapping tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-014 — Task creation success is consistent across views
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03
- **Execute:** Create task then inspect list/detail/assigned user/filter results and refresh.
- **Expected:** Exactly one task appears with correct fields/status after refresh.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress create-task happy path.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## B. FR-03 / FR-04 — Status tracking, completion, undo and filtering

### FE-015 — All supported task statuses render correctly
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04
- **Execute:** Inspect live TaskStatus enum/API and render fixture for each status.
- **Expected:** Every status is understandable; no missing/mislabelled state.
- **PASS evidence required:** Status screenshots/API mapping.
- **Automated proof required:** Component enum-mapping test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-016 — Complete-task control works through all task views
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03
- **Execute:** Use visible complete action by mouse/touch/keyboard in list/detail/assigned views.
- **Expected:** Completion succeeds once, UI updates consistently and feedback is clear.
- **PASS evidence required:** Video/screenshots.
- **Automated proof required:** Cypress completion workflow.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-017 — Undo completion works after a mistake
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03
- **Execute:** Complete task, undo it, refresh, revisit filtered and assigned views.
- **Expected:** Task returns to valid pre-completed state; no duplicate task or stale completed label.
- **PASS evidence required:** Video/API proof.
- **Automated proof required:** Mandatory Cypress undo regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-018 — Undo is unavailable/denied for invalid states
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-03
- **Execute:** Try undo on new/open/cancelled/foreign/already-undone tasks.
- **Expected:** Only valid completed task can be undone under documented policy.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Component state tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-019 — Rapid complete/undo state stays coherent
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Click complete/undo repeatedly under slow network and use two browser tabs if feasible.
- **Expected:** Final UI matches canonical server state; no duplicate toast/history/recurrence side effect.
- **PASS evidence required:** Video/request trace.
- **Automated proof required:** Cypress race/reconciliation test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-020 — Status filter covers every supported status
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04
- **Execute:** Create fixture for each TaskStatus and choose each filter option.
- **Expected:** Results contain exactly matching current-WG tasks.
- **PASS evidence required:** Screenshots/API comparison.
- **Automated proof required:** Component filter tests plus Cypress matrix.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-021 — Combined filters are accurate if supported
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-04
- **Execute:** Combine status with assignee/date/schedule/search filters; test all UI combinations.
- **Expected:** AND/OR behavior matches UI wording; no hidden stale filter.
- **PASS evidence required:** Screenshots/URL/API evidence.
- **Automated proof required:** Combined-filter component/Cypress tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-022 — Filter reset and navigation behavior is correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-04
- **Execute:** Apply filters, clear/reset, reload, use Back/Forward and direct route if applicable.
- **Expected:** Correct full result returns; no stale/phantom filter state.
- **PASS evidence required:** Video.
- **Automated proof required:** Cypress filter-reset/navigation test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-023 — Empty/loading/error states are usable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04; NFR-04
- **Execute:** Use no-task, no-filter-match, loading, permission and server-error fixtures.
- **Expected:** Message explains condition and next action without other-WG leak.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Component state tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-024 — Every exposed task edit action works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-03
- **Execute:** Inventory edit controls; modify each allowed field, then refresh and inspect unrelated fields.
- **Expected:** Allowed changes persist and validate; unrelated data is not overwritten.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress edit workflow or contract-based N/A.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-025 — Every exposed task delete/cancel action is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-03
- **Execute:** Inventory destructive controls; test confirmation, cancel/Escape, authorization, terminal history effect.
- **Expected:** No accidental deletion; behavior follows policy; no broken schedule/history.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress destructive-action test or justified N/A.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-026 — UI action permissions are truthful
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-04
- **Execute:** Compare creator, assignee, other roommate, Main Tenant, former and unrelated user UI states.
- **Expected:** No false capability claim; server denial maps safely.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Role-state component/Cypress tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## C. FR-04 — Cleaning schedules and recurrence UI

### FE-027 — Schedule routes are protected and household-scoped
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-01
- **Execute:** Open schedule routes from all membership/auth states and direct URLs.
- **Expected:** Only valid current-WG members see correct schedule context.
- **PASS evidence required:** Video/screenshots.
- **Automated proof required:** Route-guard/Cypress matrix.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-028 — Schedule form matches API schema
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; TG-04
- **Execute:** Compare all schedule fields, recurrence values, active controls and payload to OpenAPI/DTO.
- **Expected:** No missing/stale field; canonical payload is sent.
- **PASS evidence required:** Screenshot/request/OpenAPI evidence.
- **Automated proof required:** Component/client-service test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-029 — Schedule validation covers all live fields
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-04
- **Execute:** Submit empty/whitespace/boundary/malformed data for each schedule field.
- **Expected:** Clear error, no invalid schedule, no raw backend details.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Parameterized form tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-030 — All supported recurrence frequencies are selectable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04
- **Execute:** Inspect current CleaningFrequency values and test every visible option.
- **Expected:** Every supported option appears once, has clear wording and sends correct enum value.
- **PASS evidence required:** Screenshots/payload.
- **Automated proof required:** Component enum-mapping tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-031 — Schedule create action prevents duplicates
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Double-submit under slow network, reload/back during request.
- **Expected:** No accidental duplicate schedule; pending/retry state clear.
- **PASS evidence required:** Video/request count.
- **Automated proof required:** Pending-state plus Cypress duplicate-submit test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-032 — Activation/deactivation controls work
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04
- **Execute:** Create schedules, activate/deactivate through every visible control, refresh.
- **Expected:** Canonical active state remains correct after refresh.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress activation workflow.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-033 — Only one active schedule is reflected in UI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-03
- **Execute:** Activate A then B in same WG, including rapid interaction.
- **Expected:** At most one active badge/control state; activating B visibly deactivates A.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress active-schedule uniqueness regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-034 — Schedule edit controls work if exposed
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-03
- **Execute:** Edit every allowed field, including recurrence/date/assignee/active state, then refresh.
- **Expected:** Correct persistent state; no duplicate generated task or stale UI.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress schedule-edit workflow or N/A proof.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-035 — Schedule deletion respects child-task rule
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-03
- **Execute:** Delete schedule with open/nonterminal child task, then with only completed/cancelled historical tasks.
- **Expected:** Open work blocks deletion clearly; terminal history alone does not wrongly hard-block.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Mandatory Cypress deletion-policy regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-036 — Schedule list/history/empty states are clear
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-04
- **Execute:** Inspect no schedule, active, inactive, deleted/archived and failed-load states.
- **Expected:** No stale/foreign schedule; clear next action.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Component list-state tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-037 — Next-due date is rendered unambiguously
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-04
- **Execute:** Create recurrence near timezone/date boundary and inspect before/after completion.
- **Expected:** Display matches server state with no off-by-one date/time.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Date-format tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-038 — Schedule control permissions are accurate
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Compare actors and tamper/deep-link restricted controls where applicable.
- **Expected:** No false privileged success; denial is safe and understandable.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Role-state/Cypress tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-039 — Recurring generation is visible exactly once
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04; NFR-03
- **Execute:** Use deterministic due fixtures, trigger recurrence and inspect task list before/after refresh.
- **Expected:** Expected instance appears once; no missed/double task; next due updated.
- **PASS evidence required:** Video/API/DB evidence.
- **Automated proof required:** Cypress recurrence workflow linked to backend test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-040 — Complete recurring task advances correct schedule
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04
- **Execute:** Complete task linked to active schedule and inspect next due/follow-up task.
- **Expected:** Only intended schedule changes; no unrelated task/schedule mutation.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress complete-recurring-task test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-041 — Undo recurring completion avoids duplicates
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04; NFR-03
- **Execute:** Complete recurring task, observe generated/advanced state, undo, refresh.
- **Expected:** No duplicate future instance; state follows documented rollback rule.
- **PASS evidence required:** Before/after evidence.
- **Automated proof required:** Mandatory Cypress recurrence-undo regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-042 — Inactive schedule cannot generate tasks
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-03
- **Execute:** Deactivate before due trigger, then inspect task list.
- **Expected:** No background task created while inactive.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress/integration regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## D. FR-15 — Fair chore balancing via history

### FE-043 — Fairness feature entry point is truthful and scoped
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15; NFR-01; NFR-04
- **Execute:** Locate every fair-balance/suggestion UI for Main Tenant, Roommate, no-WG and foreign states.
- **Expected:** Only intended users see/use it; prototype/unavailable state is honest, not a dead button.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Component/route tests or justified N/A.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-044 — Fairness policy is explainable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15; NFR-04; NFR-05
- **Execute:** Inspect UI/help/docs for recommendation basis.
- **Expected:** User can understand meaningful basis without exposing unnecessary private data.
- **PASS evidence required:** Screenshots/docs.
- **Automated proof required:** Copy/docs test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-045 — Fairness candidates are active same-WG members only
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15; NFR-01; NFR-03
- **Execute:** Use active, foreign, former, deleted/anonymized and rejoined fixtures.
- **Expected:** Only active current members appear exactly once.
- **PASS evidence required:** Screenshots/API evidence.
- **Automated proof required:** Cypress fairness-eligibility regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-046 — Fairness output follows controlled history scenarios
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15
- **Execute:** Build distinct completed/open/overdue/undone histories and request recommendation.
- **Expected:** Recommendation/ordering follows documented policy.
- **PASS evidence required:** Fixture table/screenshots.
- **Automated proof required:** Cypress deterministic fairness scenarios.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-047 — Tie behavior is deterministic or testable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15; NFR-03
- **Execute:** Create exact tie and repeat request/reload; inspect random-tie strategy if it exists.
- **Expected:** Documented tie rule holds; no flaky assertion.
- **PASS evidence required:** Repeated-run evidence.
- **Automated proof required:** Cypress/unit tie test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-048 — Fairness updates after completion, undo and recurrence
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15; FR-03; FR-04
- **Execute:** Record suggestion, complete/undo relevant task, trigger recurrence, recompute.
- **Expected:** No stale cache; changes only as algorithm dictates.
- **PASS evidence required:** Before/after evidence.
- **Automated proof required:** Cypress fairness-update regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-049 — Fairness handles anonymized history safely — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #42; FR-15; NFR-02
- **Execute:** Delete/anonymize member with historical tasks, then inspect fairness UI.
- **Expected:** No PII leak/null crash; behavior follows documented policy.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Cypress cross-feature regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-050 — FR-15 limitation is documented if incomplete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15 extended scope; NFR-04
- **Execute:** Compare feature UI/docs to actual implementation.
- **Expected:** No false claim of complete automatic fairness if only prototype/partial behavior exists.
- **PASS evidence required:** Screenshots/docs.
- **Automated proof required:** Manual documentation review.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## E. All applicable NFRs — UI, accessibility, compatibility, docs and bug gate

### FE-051 — Every UC-02 button/link/icon/menu/dialog works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04; explicit user requirement
- **Execute:** Inventory every visible control in task, schedule, fairness, error, modal and role states; activate each.
- **Expected:** No dead button or false success; disabled states are clear and justified.
- **PASS evidence required:** Control inventory/video.
- **Automated proof required:** Dedicated Cypress UI-control-inventory spec.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-052 — All error messages are safe and actionable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03; NFR-04
- **Execute:** Trigger 400/401/403/404/409/500/offline/timeout for each UC-02 action.
- **Expected:** Clear next step; no SQL, stack, internal ID, secret or foreign data.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Service/component error mapping tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-053 — No frontend logs leak secrets or private task content
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02
- **Execute:** Search source and record browser console/telemetry during core and negative flows.
- **Expected:** No passwords/JWTs/raw tokens/excess PII appear.
- **PASS evidence required:** Static search/redacted console evidence.
- **Automated proof required:** Static scan/manual security check.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-054 — WAI/Lighthouse/axe checks pass
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Run audits on task list/form/filter/status dialogs/schedule/fairness screens.
- **Expected:** No critical accessibility blocker; findings are fixed/escalated.
- **PASS evidence required:** Audit report.
- **Automated proof required:** Repeatable axe/Lighthouse run.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-055 — Keyboard-only UC-02 workflow works
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Create/assign/filter/complete/undo/manage schedule/use fairness via keyboard only.
- **Expected:** No mouse-only blocker; focus/dialogs are safe.
- **PASS evidence required:** Video.
- **Automated proof required:** Accessibility/E2E evidence.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-056 — Reference browser/device matrix passes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04; TG-08
- **Execute:** Run core flow on Chrome/Firefox 1366×768+, iPhone 12 Pro Safari 390×844, Pixel 7 Chrome 412×915 and 360px.
- **Expected:** No clipped action, horizontal overflow, keyboard/touch blocker or browser defect.
- **PASS evidence required:** Screenshots/video/device versions.
- **Automated proof required:** Cypress viewport plus manual-device evidence.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-057 — Zoom/text scaling remains usable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Use 200% zoom/text scaling on all task/schedule/fairness primary actions.
- **Expected:** Essential labels/actions remain reachable.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Manual accessibility evidence.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-058 — Peer tester completes UC-02 without instructions
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Ask non-implementer to create/assign/complete/undo/filter task, manage schedule and interpret fairness.
- **Expected:** Peer succeeds; confusion/blockers fixed or documented.
- **PASS evidence required:** Peer test record.
- **Automated proof required:** Manual acceptance evidence.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-059 — Frontend test/build/lint/type checks pass
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-05; TG-05
- **Execute:** Run actual project commands.
- **Expected:** All pass; no ignored/allow-failure critical test.
- **PASS evidence required:** Exact output.
- **Automated proof required:** CI job evidence.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-060 — UC-02 user docs match current UI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-05; TG-10
- **Execute:** Follow documentation word-for-word for task/schedule/FR-15 behavior.
- **Expected:** No obsolete route/button/rule; fair feature limits are truthful.
- **PASS evidence required:** Doc execution record.
- **Automated proof required:** Manual doc acceptance test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-061 — All found-bug regression tests are green
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** Bugs #31/#32/#35/#36/#37/#38/#39/#42
- **Execute:** Run focused frontend/E2E regressions after UC-02 changes.
- **Expected:** No existing bug reappears; evidence names every test/command.
- **PASS evidence required:** Test output.
- **Automated proof required:** Mandatory regression gate.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### FE-062 — Final frontend sign-off is evidence-complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** All applicable FR/NFR
- **Execute:** Review every FE item, bug gate and automated proof.
- **Expected:** No critical PASS lacks evidence; unresolved issues visible.
- **PASS evidence required:** Signed review.
- **Automated proof required:** Reviewer sign-off.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

