# UC-02 — BACKEND QA FORM

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

**Spring Boot API, authorization, services, JPA/H2, transactions, recurrence, fairness, auditability, OpenAPI, CI and documentation.**

This form owns all server-side enforcement for FR-03, FR-04 and FR-15 plus NFR-01, NFR-02, NFR-03, NFR-05 and NFR-06.


## A. FR-03 — Task creation, assignment, validation and persistence

### BE-001 — All task endpoints require valid authentication
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-01
- **Execute:** Enumerate task endpoints; call each without/malformed/expired JWT.
- **Expected:** Safe 401 and no task data/mutation.
- **PASS evidence required:** Endpoint matrix/responses.
- **Automated proof required:** Parameterized security integration suite.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-002 — Task API matches OpenAPI contract
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; TG-04
- **Execute:** Compare every task endpoint method/path/security/DTO/status/error to served OpenAPI.
- **Expected:** Live API and spec agree.
- **PASS evidence required:** Spec/API comparison.
- **Automated proof required:** Contract tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-003 — Task create DTO validates every required field
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-03
- **Execute:** Submit missing/blank/boundary/malformed values for all task create fields directly to API.
- **Expected:** Controlled 4xx, no persisted task and no 500.
- **PASS evidence required:** Responses/DB assertions.
- **Automated proof required:** Parameterized endpoint tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-004 — Task text fields validate and store safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-01; NFR-02
- **Execute:** Submit long/Unicode/markup-like/control-character inputs under documented limits.
- **Expected:** Allowed data survives correctly; invalid values reject safely; no injection/serialization break.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Validation/service tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-005 — Task due-date rule is server-enforced
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-03
- **Execute:** Submit missing/malformed/past/boundary/timezone values direct to API.
- **Expected:** Server rule—not client clock—decides validity deterministically.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Clock-controlled tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-006 — Task household/creator derive from authenticated principal
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-01; TG-01
- **Execute:** Inject householdId, creatorId, role in request body/query/header.
- **Expected:** Server ignores/rejects forged authority; task belongs to actor’s active WG.
- **PASS evidence required:** Request/DB evidence.
- **Automated proof required:** Authorization/mass-assignment tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-007 — Task assignee must be active same-household member
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-01; NFR-03
- **Execute:** Assign foreign, former, deleted/anonymized, rejoined and nonexistent member identities.
- **Expected:** Only eligible active current member is accepted.
- **PASS evidence required:** Response/DB matrix.
- **Automated proof required:** Service/repository/integration tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-008 — Existing-user invitation regression remains task-safe — BUG #35
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #35; FR-03; NFR-03
- **Execute:** Invite existing account, then create/assign task and inspect membership/assignee data.
- **Expected:** One user/member/assignee; no duplicate or 500.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory named integration regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-009 — Leave/rejoin task references remain safe — BUG #38
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #38; FR-03; NFR-03
- **Execute:** Join, create/assign task, leave, rejoin, list/reassign task.
- **Expected:** Exactly one active membership; no duplicate visibility/assignee/history; no 500.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory repository + integration regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-010 — Anonymized task references remain valid — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #42; NFR-02; NFR-03
- **Execute:** Create task history, delete/anonymize member, fetch task/history.
- **Expected:** No FK break/null failure; privacy-safe fallback; no PII leak.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Mandatory cross-feature integration regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-011 — Task create is atomic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Force failure after partial entity/audit/schedule-link work begins.
- **Expected:** No partial task/assignment/audit remains.
- **PASS evidence required:** DB assertions.
- **Automated proof required:** Transactional test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-012 — Duplicate/concurrent task create is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Send repeated/concurrent create calls using same fixture.
- **Expected:** Behavior is documented; no unintended duplicate/raw failure.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Concurrency/idempotency test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-013 — Task create response is privacy-minimized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; TG-04
- **Execute:** Inspect success/error DTOs/headers.
- **Expected:** No password/JWT/raw invite/internal entity/security fields/excess PII.
- **PASS evidence required:** Response inventory.
- **Automated proof required:** Serialization/contract test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-014 — Task errors are sanitized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Trigger validation/auth/constraint/unexpected failures.
- **Expected:** Safe error schema; no SQL/stack/class detail.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception-handler tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## B. FR-03 / FR-04 — Task reads, status, completion, undo, filters and permissions

### BE-015 — Task reads enforce household scope
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-01
- **Execute:** Use WG A actor against every WG B task ID/list/filter.
- **Expected:** No foreign data/existence leak/mutation.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** IDOR/cross-WG integration suite.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-016 — Task detail handles bad IDs safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Request random/deleted/foreign/malformed IDs.
- **Expected:** Controlled safe result, no stack trace/data leak.
- **PASS evidence required:** Responses.
- **Automated proof required:** Parameterized endpoint tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-017 — Task update/assignment validates actor permissions
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-01
- **Execute:** Test creator, assignee, other roommate, Main Tenant, former and unrelated actors against all update actions.
- **Expected:** Only documented authorized actor succeeds; server enforces rule.
- **PASS evidence required:** Response/DB matrix.
- **Automated proof required:** Role-based integration tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-018 — Task update blocks mass assignment
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Inject ID, household, creator, audit, schedule/status/role fields beyond allowed DTO.
- **Expected:** Forbidden fields cannot alter ownership/history/authority.
- **PASS evidence required:** Requests/DB evidence.
- **Automated proof required:** Mass-assignment tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-019 — All editable task fields validate on update
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-03
- **Execute:** Submit valid/boundary/invalid values for each editable field.
- **Expected:** Only valid update persists; unrelated fields unchanged; failure rolls back.
- **PASS evidence required:** Before/after DB/API.
- **Automated proof required:** Service/endpoint tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-020 — Task status enum is completely documented and tested
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04
- **Execute:** Read current TaskStatus enum/OpenAPI; test API serialization for every value.
- **Expected:** Every supported status is valid, returned and mapped consistently.
- **PASS evidence required:** Enum/API proof.
- **Automated proof required:** Parameterized status tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-021 — Only allowed status transitions persist
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-03
- **Execute:** Attempt every from→to pair including direct tampered status edits.
- **Expected:** Valid transitions work; invalid transition rejected; no illegal state persists.
- **PASS evidence required:** Transition matrix/DB proof.
- **Automated proof required:** Service transition tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-022 — Completion is atomic and auditable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-03; NFR-06
- **Execute:** Complete task; inspect status, completion metadata, audit and schedule side effects.
- **Expected:** One completion with actor/time trace; consistent schedule state.
- **PASS evidence required:** DB/audit/API evidence.
- **Automated proof required:** Integration test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-023 — Undo completion is atomic and auditable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-03; NFR-06
- **Execute:** Complete then undo; inspect status/history/audit/schedule effects.
- **Expected:** Correct state restored per policy; trace exists; no duplicate task/side effect.
- **PASS evidence required:** DB/audit evidence.
- **Automated proof required:** Mandatory service/integration undo test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-024 — Undo invalid states are denied safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-03
- **Execute:** Attempt undo on never-completed/cancelled/foreign/already-undone task.
- **Expected:** No state/audit corruption; controlled response.
- **PASS evidence required:** Responses/DB proof.
- **Automated proof required:** Transition tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-025 — Concurrent complete/undo stays consistent
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Send concurrent completion/undo calls from allowed actors.
- **Expected:** Final state follows transaction/order policy; no duplicate audit/recurrence effect.
- **PASS evidence required:** Concurrency output/DB.
- **Automated proof required:** Concurrent integration test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-026 — Delete/cancel is safe if delivered
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-03
- **Execute:** Locate API; test authorized/unauthorized/repeated delete/cancel and scheduled task cases.
- **Expected:** Policy enforced; no orphan/schedule corruption/500.
- **PASS evidence required:** Responses/DB.
- **Automated proof required:** Integration tests or justified N/A.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-027 — Status filtering is server-correct
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04
- **Execute:** Call task list/filter for every status and combination supported.
- **Expected:** Only matching current-WG tasks returned.
- **PASS evidence required:** Response/DB comparison.
- **Automated proof required:** Repository + integration filter tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-028 — Filter query values validate safely
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Pass invalid enum/repeated/injection-like/oversized/malformed filter/sort/page values.
- **Expected:** Controlled response; no SQL error/broad data result.
- **PASS evidence required:** Responses.
- **Automated proof required:** Parameterized tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## C. FR-04 — Cleaning schedule CRUD, active-state uniqueness and deletion

### BE-029 — Schedule endpoints require valid household membership
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-01
- **Execute:** Call every schedule endpoint unauthenticated/invalid/foreign/former.
- **Expected:** No data/mutation outside current household; controlled error.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** Security integration suite.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-030 — Schedule API matches OpenAPI
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; TG-04
- **Execute:** Compare paths/DTOs/recurrence enum/security/error responses to OpenAPI.
- **Expected:** Spec and API agree.
- **PASS evidence required:** Spec/API proof.
- **Automated proof required:** Contract tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-031 — Schedule create validates all fields
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-03
- **Execute:** Submit missing/blank/boundary/malformed values for each live schedule field.
- **Expected:** No invalid persisted schedule; controlled 4xx.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Parameterized validation tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-032 — All recurrence values validate server-side
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04
- **Execute:** Inspect CleaningFrequency enum; submit each valid and invalid recurrence.
- **Expected:** All real values accepted; invalid never silently coerced.
- **PASS evidence required:** Enum/API/DB proof.
- **Automated proof required:** Parameterized enum tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-033 — Schedule references only eligible same-WG member
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-01
- **Execute:** Try foreign/former/deleted/rejoined/nonexistent assignee refs.
- **Expected:** Only active current member accepted.
- **PASS evidence required:** Response/DB matrix.
- **Automated proof required:** Service/repository/integration tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-034 — Schedule create is atomic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Force error after schedule entity but before active/audit state persists.
- **Expected:** No partial schedule/active marker/audit.
- **PASS evidence required:** DB assertions.
- **Automated proof required:** Transactional test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-035 — One active schedule per household is enforced
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-03
- **Execute:** Create/activate A then B, including concurrent requests.
- **Expected:** At most one active schedule; no unique-constraint 500; deterministic final state.
- **PASS evidence required:** DB/API/concurrency evidence.
- **Automated proof required:** Mandatory repository + integration uniqueness tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-036 — Activating B deactivates A transactionally
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-03
- **Execute:** Activate B while A is active; inject a failure mid-operation.
- **Expected:** Success gives B active/A inactive; failure restores prior state; never two active/no accidental state.
- **PASS evidence required:** DB before/after.
- **Automated proof required:** Transactional service/integration test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-037 — Schedule update preserves active-state invariants
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-03
- **Execute:** Edit recurrence/date/assignee/active around other schedules.
- **Expected:** No bypass of active uniqueness; invalid state rolls back.
- **PASS evidence required:** Responses/DB.
- **Automated proof required:** Integration tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-038 — Schedule delete blocks only actual open child tasks
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-03
- **Execute:** Delete with open/nonterminal child, then completed/cancelled history only, then no child.
- **Expected:** Open work blocks; terminal history alone does not wrongly hard-block.
- **PASS evidence required:** Responses/DB evidence.
- **Automated proof required:** Mandatory deletion-policy regression test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-039 — Schedule delete is idempotent/concurrency-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Delete twice/concurrently and use stale ID.
- **Expected:** No raw 500/orphan; safe response.
- **PASS evidence required:** Responses/DB.
- **Automated proof required:** Concurrency/integration test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-040 — Schedule reads enforce household isolation
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Access WG B schedule with WG A actor.
- **Expected:** No foreign data/mutation/existence leak.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** IDOR tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-041 — Schedule errors are sanitized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Trigger validation/foreign ID/unique/FK/unexpected errors.
- **Expected:** Safe error status/schema; no SQL/stack.
- **PASS evidence required:** Responses/logs.
- **Automated proof required:** Exception tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## D. FR-03 / FR-04 — Recurrence calculation, generation and undo behavior

### BE-042 — Next-due calculation is correct for every recurrence
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04; NFR-03
- **Execute:** Use fixed clock fixtures for each frequency and month-end/leap/DST/timezone boundaries where applicable.
- **Expected:** Next due follows documented recurrence exactly; no off-by-one/invalid date.
- **PASS evidence required:** Fixture table/DB evidence.
- **Automated proof required:** Unit tests with injectable clock.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-043 — Due schedule creates exactly one task instance
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04; NFR-03
- **Execute:** Trigger/process due schedule twice/concurrently and around restart.
- **Expected:** One intended task per occurrence; no missed/double generation; nextDue advances once.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Integration/concurrency test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-044 — Inactive schedule cannot generate tasks
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-03
- **Execute:** Deactivate before due, invoke scheduler/processing.
- **Expected:** No task/new next-due change while inactive.
- **PASS evidence required:** DB evidence.
- **Automated proof required:** Integration test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-045 — Completing recurring task updates correct schedule only
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04
- **Execute:** Complete a schedule-linked task and inspect all schedules/tasks.
- **Expected:** Only linked schedule changes; no unrelated mutation.
- **PASS evidence required:** DB/API proof.
- **Automated proof required:** Integration test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-046 — Undo recurring completion avoids duplicate future tasks
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04; NFR-03
- **Execute:** Complete recurring task, allow advancement/generation, undo completion.
- **Expected:** Result follows documented rollback/compensation; no duplicate future task.
- **PASS evidence required:** DB before/after.
- **Automated proof required:** Mandatory regression integration test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-047 — Schedule edit near due boundary is deterministic
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-03
- **Execute:** Edit frequency/date/active before/at/after due with fixed clock.
- **Expected:** No missed/double recurrence; documented stable behavior.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Clock-controlled tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-048 — Recurrence survives restart as documented
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Create due/near-due schedule, restart, trigger processor and inspect.
- **Expected:** No reset/double-fire/lost due event beyond documented behavior.
- **PASS evidence required:** Commands/DB evidence.
- **Automated proof required:** Restart integration test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-049 — Scheduler failure handling is safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; NFR-05
- **Execute:** Force one due schedule processing failure among several.
- **Expected:** No corrupted state; unrelated due schedules follow documented error policy; safe logs.
- **PASS evidence required:** Logs/DB proof.
- **Automated proof required:** Integration test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-050 — Manual versus generated task origin/link persists correctly
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04
- **Execute:** Create manual and generated tasks, inspect schedule linkage/origin DTO.
- **Expected:** Only generated tasks link to correct schedule; no cross-schedule reference.
- **PASS evidence required:** DB/API proof.
- **Automated proof required:** Repository/integration tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## E. FR-15 — Fair chore balancing via history

### BE-051 — Fairness implementation or limitation is explicit
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15 extended; NFR-05
- **Execute:** Locate endpoint/service/UI contract. If absent/prototype, validate documentation.
- **Expected:** No false feature claim; delivered behavior/limitation is clear.
- **PASS evidence required:** Code/docs/OpenAPI proof.
- **Automated proof required:** Test or justified N/A.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-052 — Fairness inputs are active same-WG members only
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15; NFR-01; NFR-03
- **Execute:** Use active, foreign, former, anonymized and rejoined users with histories.
- **Expected:** Only active current members included once.
- **PASS evidence required:** DB/API evidence.
- **Automated proof required:** Service/repository/integration tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-053 — Fairness score follows documented history policy
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15; NFR-05
- **Execute:** Build fixtures for completed/open/overdue/undone/cancelled states and compare result to policy.
- **Expected:** Score/recommendation matches documented algorithm; no hidden inconsistent criterion.
- **PASS evidence required:** Fixture table/code proof.
- **Automated proof required:** Unit tests per input category.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-054 — Fairness ties are deterministic or testable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15; NFR-03
- **Execute:** Create exact tie; repeat/restart. If random tie-breaker exists inspect seed/control.
- **Expected:** No flaky opaque behavior; rule documented/testable.
- **PASS evidence required:** Repeated output.
- **Automated proof required:** Unit/integration tie tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-055 — Fairness recomputes after completion/undo/recurrence
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15; FR-03; FR-04
- **Execute:** Compute baseline then change task state/recurrence and recompute.
- **Expected:** Outcome updates only as documented; no stale cache.
- **PASS evidence required:** Before/after API/DB.
- **Automated proof required:** Integration regression tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-056 — Fairness handles anonymized historical member — BUG #42
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #42; FR-15; NFR-02
- **Execute:** Delete/anonymize historical task user then request fairness.
- **Expected:** No PII/null crash; documented handling applies.
- **PASS evidence required:** Response/DB proof.
- **Automated proof required:** Mandatory cross-feature regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-057 — Fairness output is privacy-minimized
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15; NFR-02; NFR-06
- **Execute:** Inspect response/log/audit and access behavior.
- **Expected:** No unnecessary private task/foreign data; any explanation follows policy.
- **PASS evidence required:** Response/log proof.
- **Automated proof required:** Serialization/integration test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## F. All applicable NFRs — security, privacy, reliability, architecture, CI and bug gate

### BE-058 — Complete UC-02 authorization matrix exists
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Enumerate all task/status/undo/filter/schedule/recurrence/fairness routes and classify public/auth/member/role-specific.
- **Expected:** No route unclassified; tests cover every class.
- **PASS evidence required:** Matrix evidence.
- **Automated proof required:** Parameterized security suite.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-059 — Every UC-02 read endpoint blocks cross-WG access
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Use WG A actor against all WG B resources/filters/IDs.
- **Expected:** No foreign data or meaningful existence disclosure.
- **PASS evidence required:** Endpoint matrix.
- **Automated proof required:** Security integration suite.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-060 — Every UC-02 mutation endpoint blocks cross-WG writes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Use WG A actor against WG B tasks/schedules/status/assignment IDs.
- **Expected:** No mutation; DB unchanged.
- **PASS evidence required:** Endpoint/DB matrix.
- **Automated proof required:** Security suite.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-061 — All DTOs block mass assignment
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Inject IDs, household, creator, audit, schedule, active, nextDueDate, status/role fields.
- **Expected:** No unauthorized ownership/authority/state change.
- **PASS evidence required:** Request/DB evidence.
- **Automated proof required:** Mass-assignment tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-062 — Hostile path/query/body values are safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03
- **Execute:** Fuzz IDs, enum values, dates, strings, pagination/sort/filter inputs.
- **Expected:** No parser/SQL error/500; controlled result.
- **PASS evidence required:** Response matrix.
- **Automated proof required:** Parameterized tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-063 — Logs and audit are privacy-safe
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; NFR-06
- **Execute:** Run core/negative flows, inspect logs/audit/error DTOs.
- **Expected:** No passwords/JWT/raw token/excess PII/full private payload.
- **PASS evidence required:** Redacted proof.
- **Automated proof required:** Log scan/static review.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-064 — Task and schedule actions are auditable
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-06
- **Execute:** Create, assign/reassign, complete, undo, create/activate/deactivate/edit/delete schedule.
- **Expected:** Actor/time/action trace exists or documented equivalent, without secrets.
- **PASS evidence required:** Audit records.
- **Automated proof required:** Integration audit tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-065 — Transactions cover all coupled state changes
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Review transaction boundaries and inject failures across task/schedule/audit actions.
- **Expected:** No partial state on any failed multi-row action.
- **PASS evidence required:** Code/DB proof.
- **Automated proof required:** Rollback tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-066 — Concurrency tests cover critical races
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Run concurrent create, assign, complete/undo, schedule activation, recurrence trigger, delete.
- **Expected:** Final state valid; no race/500.
- **PASS evidence required:** Output/DB evidence.
- **Automated proof required:** Concurrent integration tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-067 — Seed/reset is reproducible and contains UC-02 fixtures
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; TG-05
- **Execute:** From clean checkout follow setup/reset docs and verify synthetic tasks/statuses/schedules/history.
- **Expected:** No hidden repair; known state restored.
- **PASS evidence required:** Commands/DB/API proof.
- **Automated proof required:** Seed smoke test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-068 — Task/schedule persistence survives restart
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Create task/schedule, restart app/database per docs, inspect and perform next action.
- **Expected:** State survives/restores as claimed; no duplicate recurrence.
- **PASS evidence required:** Commands/API/DB proof.
- **Automated proof required:** Restart integration test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-069 — Repository tests cover critical queries
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; QA strategy
- **Execute:** Locate/run tests for scope, status filtering, active uniqueness, due lookup, former-member behavior.
- **Expected:** Real DB semantics proven.
- **PASS evidence required:** Test paths/output.
- **Automated proof required:** Mandatory repository evidence.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-070 — Service tests cover core business rules
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-05; TG-09
- **Execute:** Locate/run unit tests for transitions, undo, recurrence calculation/generation, active swap and fairness score.
- **Expected:** Rules tested independently of UI/E2E.
- **PASS evidence required:** Test paths/output.
- **Automated proof required:** Mandatory service test evidence.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-071 — API integration tests cover full task-scheduling path
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-09
- **Execute:** Run real security/database API workflows for task/schedule core behavior.
- **Expected:** Security, persistence and scheduling proven together.
- **PASS evidence required:** Output.
- **Automated proof required:** Mandatory integration suite.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-072 — OpenAPI is served and current
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-04
- **Execute:** Start application and retrieve Swagger/OpenAPI; inspect UC-02 operations.
- **Expected:** Spec available/current.
- **PASS evidence required:** URL/output.
- **Automated proof required:** OpenAPI smoke/contract test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-073 — CI runs critical UC-02 layers and blocks failures
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-05; TG-09
- **Execute:** Inspect pipeline profiles/exclusions/allow_failure and execute latest pipeline.
- **Expected:** Unit/repository/integration/frontend/E2E layers run; failures block merge.
- **PASS evidence required:** CI YAML/job proof.
- **Automated proof required:** CI verification.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-074 — Architecture boundaries are clean
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-01; NFR-05
- **Execute:** Trace controller → service → repository/mapper and client boundary for all UC-02 operations.
- **Expected:** No DB access outside repositories; no business rules improperly in controller/UI; one source of truth.
- **PASS evidence required:** Architecture map.
- **Automated proof required:** Code review plus tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-075 — DTO mappers exclude internal fields
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; TG-04
- **Execute:** Inspect task/schedule/member/fairness serialization/mappers.
- **Expected:** No internal security/audit fields leaked unless explicit contract requires.
- **PASS evidence required:** Code/response proof.
- **Automated proof required:** Serialization tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-076 — Setup/API/user docs are current
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-10; NFR-05
- **Execute:** Follow docs for local start, task/schedule workflows and FR-15 limitations.
- **Expected:** No obsolete command/rule/false fairness claim.
- **PASS evidence required:** Documentation execution record.
- **Automated proof required:** Manual doc acceptance test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-077 — All known-bug backend regressions remain green
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** Bugs #31/#32/#35/#36/#37/#38/#39/#42
- **Execute:** Run focused backend/integration tests for all found bugs after UC-02 changes.
- **Expected:** Every existing bug regression remains green; tests/commands named.
- **PASS evidence required:** Output.
- **Automated proof required:** Mandatory regression gate.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### BE-078 — Final backend sign-off is evidence-complete
- [ ] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** All applicable FR/NFR/TG
- **Execute:** Review all BE items and automated evidence.
- **Expected:** No critical PASS lacks proof; unresolved issues remain visible.
- **PASS evidence required:** Signed review.
- **Automated proof required:** Reviewer sign-off.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

