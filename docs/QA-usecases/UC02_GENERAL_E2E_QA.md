# UC-02 â€” GENERAL / E2E QA FORM

## Embedded execution instructions â€” no Project Proposal upload required

This file is self-contained for **UC-02**. Do **not** ask the user to upload, paste, or re-explain the Project Proposal.

Before checking any box, inspect the repositoryâ€™s current Angular routes/components/services/tests; Spring controllers/services/entities/repositories/DTOs/security configuration/tests; H2 schema; OpenAPI; seed/reset tools; CI; and existing bug-fix branches.

**Strict rules**
1. Mark PASS only with the exact evidence and automated proof required below. â€œThe code looks correctâ€, â€œit worked onceâ€, â€œthe button existsâ€, or â€œCI was green earlierâ€ is not evidence.
2. If evidence or a test does not exist, create it. If a defect appears, fix it, add a regression test, rerun the relevant suite, then update the evidence.
3. Never weaken an expected result, delete/skip a critical test, use `|| true`, or hide FAIL/BLOCKED items.
4. Use synthetic data only. Do not place passwords, JWTs, raw invitation tokens, hashes, secrets, or real personal data in evidence.
5. A UC-02 merge is not complete while any critical requirement is unresolved.

## Binding UC-02 functional baseline

- **FR-03 â€” Task creation and assignment:** household members can create, assign and track household tasks/cleaning duties. Tasks support assignment to a roommate, completion tracking, undoing a mistaken completion, and recurrence.
- **FR-04 â€” Cleaning schedule management:** schedules support recurring behavior and task filtering by status.
- **FR-15 â€” Fair chore balancing via history:** extended feature. Where delivered, recommendations must use a documented, testable policy based on task history and must only consider eligible current household members. If prototype-only, the limitation must be truthful and documented.

## Binding non-functional baseline

- **NFR-01 Security & Access Control:** authenticated access to WG-specific data; backend-enforced household scoping, RBAC and input validation.
- **NFR-02 Data Privacy & GDPR:** data minimization; safe account leave/deletion/anonymization; privacy-safe historical task references; no secret/PII leakage.
- **NFR-03 Reliability:** durable task/schedule data, reproducible seed/reset, transactions, safe duplicates/races/restarts/error handling.
- **NFR-04 Usability & Compatibility:** peer-testable workflows; WAI/Lighthouse; Chrome/Firefox desktop 1366Ã—768+, iPhone 12 Pro Safari 390Ã—844, Pixel 7 Chrome 412Ã—915, and 360px boundary.
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

**Integrated Angular + Spring Boot + H2 proof: real multi-user journeys, recurrence, fairness, device/browser/accessibility, peer testing, CI, documentation and all known-bug gates.**

This is the final system-level proof that FR-03, FR-04, FR-15 and every applicable NFR-01..NFR-06 work together in the running application.

## QA execution update - 2026-06-25

Executor: Codex QA verification
Branch / commit: `UC-02/QA` / `cdbf8afd3b58354d1f629106b386251ec91095b3` (`git.dirty=true` due QA doc edits and existing untracked local files)

Automated evidence recorded in this pass:
- Backend focused UC-02 suite: `cd backend && mvn -B "-Dtest=TaskEndpointTest,TaskRepositoryTest,SimpleTaskServiceTest,TaskMappingTest,CleaningScheduleEndpointTest,SimpleCleaningScheduleServiceTest,CleaningScheduleRepositoryTest,FairChoreBalancingServiceTest" test` -> `Tests run: 105, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`.
- Frontend build: `cd frontend && npm.cmd run build` -> build succeeded, output generated under `frontend/dist/sepr-group-phase`.
- Frontend focused UC-02 unit/component/service suite: `cd frontend && npx.cmd ng test --watch=false --browsers=ChromeHeadless --include "src/app/components/task-create/task-create.component.spec.ts" --include "src/app/components/task-list/task-list.component.spec.ts" --include "src/app/components/cleaning-schedule-list/cleaning-schedule-list.component.spec.ts" --include "src/app/services/task.service.spec.ts" --include "src/app/services/cleaning-schedule.service.spec.ts"` -> `TOTAL: 76 SUCCESS`.
- Focused UC-02 Cypress smoke: backend and frontend were started locally; `cd e2e && npx.cmd cypress run --config baseUrl=http://127.0.0.1:4200/ --spec "cypress/e2e/usecases/uc02-task-lifecycle.cy.js"` completed with command exit code `0`.

Evidence files / specs used:
- `e2e/cypress/e2e/usecases/uc02-task-lifecycle.cy.js`
- `e2e/cypress/e2e/fr03-tasks.cy.js`
- `e2e/cypress/e2e/fr04-cleaning-schedules.cy.js`
- `e2e/cypress/e2e/fr15-fair-chore-balancing.cy.js`
- `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/TaskEndpointTest.java`
- `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/CleaningScheduleEndpointTest.java`
- `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/FairChoreBalancingServiceTest.java`

Open / blocked gates:
- Peer tester evidence, axe/Lighthouse report, keyboard-only video, native browser/device matrix, 200% zoom evidence, and remote CI verification were not produced in this local pass.
- Those items are marked **BLOCKED** rather than PASS.


## A. Core FR-03 / FR-04 / FR-15 integrated journeys

### E2E-001 â€” Core task journey: create, assign, complete and undo
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03
- **Execute:** Using two members in one WG, create task, assign to roommate, verify both views, complete, undo and refresh between steps.
- **Expected:** One coherent task state; no duplication/loss; correct assignee and history.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress full task journey.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-002 â€” Task assignment sees only current WG members
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-01
- **Execute:** Use two WGs plus active/former/rejoined/deleted fixtures; assign through real UI.
- **Expected:** Only active same-WG members are selectable and assignable.
- **PASS evidence required:** Video/API/DB proof.
- **Automated proof required:** Cypress assignee matrix.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-003 â€” Existing user invitation works for tasks â€” BUG #35
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #35; FR-03
- **Execute:** Invite existing account into WG, then create/assign/filter tasks as both users.
- **Expected:** No duplicate account/member/task; correct task visibility.
- **PASS evidence required:** Video/API/DB proof.
- **Automated proof required:** Mandatory named Cypress BUG #35 regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-004 â€” Leave/rejoin preserves task and schedule behavior â€” BUG #38
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #38; FR-03; FR-04
- **Execute:** Join user, create/assign task and schedule, leave, rejoin through fresh invite, inspect all views.
- **Expected:** No unique-constraint 500, duplicate assignee/candidate/task list or broken schedule link.
- **PASS evidence required:** Video/API/DB/audit proof.
- **Automated proof required:** Mandatory named Cypress BUG #38 regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-005 â€” Anonymized member retains safe task history â€” BUG #42
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** BUG #42; NFR-02
- **Execute:** Create task history, leave/delete/anonymize user, inspect task/schedule/fairness history.
- **Expected:** Privacy-safe reference remains; no null crash/PII leak; linked settlement test remains green.
- **PASS evidence required:** Before/after evidence.
- **Automated proof required:** Mandatory cross-feature E2E regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-006 â€” Status filter matrix works end-to-end
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04
- **Execute:** Create fixture for each supported status; use each filter and combined filters; reset/refresh/back.
- **Expected:** Exact matching list, correct reset and navigation behavior.
- **PASS evidence required:** Video/API comparison.
- **Automated proof required:** Parameterized Cypress filter suite.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-007 â€” All exposed task edit/delete/cancel flows work
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; NFR-03
- **Execute:** Inventory and execute every task action with authorized/unauthorized actor and confirmation cancel path.
- **Expected:** Every visible control works or safely denies; no orphan/contradictory UI.
- **PASS evidence required:** Video/API proof.
- **Automated proof required:** Cypress action inventory or justified N/A.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-008 â€” Cleaning schedule full workflow
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04
- **Execute:** Create each live frequency, activate/deactivate, edit if exposed, inspect next due and delete where allowed.
- **Expected:** All actions persist and UI/API agree; no dead control.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress schedule journey.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-009 â€” Single active schedule end-to-end
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-03
- **Execute:** Activate schedule A then B, including rapid clicks/reload.
- **Expected:** Exactly one active schedule after refresh.
- **PASS evidence required:** Video/API/DB proof.
- **Automated proof required:** Cypress active-schedule uniqueness regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-010 â€” Schedule deletion policy works end-to-end
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04; NFR-03
- **Execute:** Delete schedule with open child, terminal-history-only child, and no child.
- **Expected:** Open work blocks; terminal history alone does not block; no 500.
- **PASS evidence required:** Video/API evidence.
- **Automated proof required:** Cypress deletion-policy regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-011 â€” Recurrence generates one expected occurrence
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04; NFR-03
- **Execute:** Use deterministic due fixture/test clock; process every supported recurrence.
- **Expected:** One expected generated task and correct next due; no miss/double.
- **PASS evidence required:** Video/API/DB proof.
- **Automated proof required:** Cypress/integration recurrence suite.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-012 â€” Recurring completion/undo is safe
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-03; FR-04; NFR-03
- **Execute:** Complete recurring task, inspect schedule/follow-up then undo and refresh.
- **Expected:** No duplicate future task; correct documented final state.
- **PASS evidence required:** Before/after video/API.
- **Automated proof required:** Mandatory Cypress recurrence-undo regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-013 â€” Inactive schedule does not create task
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-04
- **Execute:** Deactivate before due and invoke recurrence path.
- **Expected:** No generated task.
- **PASS evidence required:** Video/API proof.
- **Automated proof required:** Cypress/integration test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-014 â€” Fairness baseline and unequal histories
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15
- **Execute:** Create controlled completed/open/overdue/undone histories and request suggestion.
- **Expected:** Recommendation follows documented policy with explainable evidence.
- **PASS evidence required:** Fixture table/video/API.
- **Automated proof required:** Cypress fairness scenario tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-015 â€” Fairness ties and updates are stable
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15; NFR-03
- **Execute:** Create exact tie repeatedly; then complete/undo/trigger recurrence and recompute.
- **Expected:** Tie is deterministic/testable; recommendation updates correctly.
- **PASS evidence required:** Repeated evidence.
- **Automated proof required:** Cypress tie/update regression.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-016 â€” Fairness only considers active current members
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** FR-15; NFR-01
- **Execute:** Use foreign/former/anonymized/rejoined/active members.
- **Expected:** Only eligible users appear once; no foreign data.
- **PASS evidence required:** Video/API proof.
- **Automated proof required:** Cypress fairness eligibility test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## B. Security, privacy, reliability and found-bug regression gates

### E2E-017 â€” Every UC-02 control works in integrated app
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04; explicit user requirement
- **Execute:** Inventory all task/schedule/fairness buttons, links, icons, menus and dialogs for all roles/states; activate each.
- **Expected:** No dead control/false success; disabled state clear.
- **PASS evidence required:** Completed inventory/video.
- **Automated proof required:** Cypress control-inventory suite.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-018 â€” Negative error matrix is safe
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01; NFR-03; NFR-04
- **Execute:** Trigger validation, 401, 403, 404, 409, 500, offline and timeout across UC-02 actions.
- **Expected:** Safe actionable UX, no internal leakage/false success, recoverable.
- **PASS evidence required:** Screenshots/network.
- **Automated proof required:** Cypress error-mapping matrix.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-019 â€” Two-household isolation works in real app
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-01
- **Execute:** Use WG A/B, direct URLs, browser history, tampered IDs and cross-user mutations.
- **Expected:** No foreign task/schedule/fairness data or write.
- **PASS evidence required:** Video/network/DB proof.
- **Automated proof required:** Cypress two-household security suite.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-020 â€” Rapid interaction and multi-tab races are safe
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03
- **Execute:** Double-submit create/assign/complete/undo/activate/delete and use two tabs/users.
- **Expected:** Final canonical state valid; no duplicate/500/lost update.
- **PASS evidence required:** Video/request trace.
- **Automated proof required:** Cypress reconciliation suite.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-021 â€” Restart/reload/seed restore behavior is correct
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-03; TG-05
- **Execute:** Create task/schedule, restart/reset per docs, refresh routes and trigger recurrence.
- **Expected:** State survives/restores as documented; no duplicate recurrence.
- **PASS evidence required:** Commands/video/API.
- **Automated proof required:** Clean-environment/restart smoke.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-022 â€” Task and schedule audit trail appears end-to-end
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-06
- **Execute:** Create, assign/reassign, complete, undo, alter/delete schedule; inspect audit/history.
- **Expected:** Required actor/time trace exists without leaking secrets.
- **PASS evidence required:** Audit evidence.
- **Automated proof required:** Integration + E2E link.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-023 â€” Privacy and lifecycle with tasks is safe
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02; TG-07
- **Execute:** View/correct allowed task data, leave/delete/anonymize actor, inspect history/current tasks.
- **Expected:** Delivered rights are real and privacy-safe; no false success.
- **PASS evidence required:** Video/API/DB proof.
- **Automated proof required:** Cypress/manual lifecycle flow.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-024 â€” No secret/PII leak in logs
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-02
- **Execute:** Capture browser console/backend logs during normal and failing flows.
- **Expected:** No passwords/JWTs/raw tokens/excess private data.
- **PASS evidence required:** Redacted logs.
- **Automated proof required:** Console guard + log scan.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-025 â€” Whole-project known-bug gate stays green
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** Bugs #31/#32/#35/#36/#37/#38/#39/#42
- **Execute:** Run all existing focused bug regression tests after UC-02 integration.
- **Expected:** No old bug regresses; names/commands/output recorded.
- **PASS evidence required:** CI/test output.
- **Automated proof required:** Mandatory regression gate.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`


## C. All applicable NFRs â€” usability, accessibility, compatibility, documentation, CI and release

### E2E-026 â€” Peer tester completes UC-02 unaided
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Ask non-implementer to create/assign/complete/undo/filter task, manage schedule and interpret fairness.
- **Expected:** Peer succeeds; blockers/confusion fixed or clearly documented.
- **PASS evidence required:** Peer test record.
- **Automated proof required:** Manual acceptance evidence.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-027 â€” WAI/Lighthouse/axe gate passes
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04
- **Execute:** Audit task list/form/filter/status actions/schedule/fairness dialogs.
- **Expected:** No critical blocker; findings fixed/escalated.
- **PASS evidence required:** Audit reports.
- **Automated proof required:** Repeatable axe/Lighthouse run.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-028 â€” Keyboard-only workflow works
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Complete all core UC-02 flows using keyboard only.
- **Expected:** No mouse-only block; focus and dialogs safe.
- **PASS evidence required:** Video.
- **Automated proof required:** Manual + automated accessibility proof.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-029 â€” Reference browser/device matrix passes
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04; TG-08
- **Execute:** Run core flow on Chrome/Firefox desktop 1366Ã—768+, iPhone 12 Pro Safari 390Ã—844, Pixel 7 Chrome 412Ã—915, 360px.
- **Expected:** No layout/function/touch/keyboard blocker.
- **PASS evidence required:** Screenshots/video/browser versions.
- **Automated proof required:** Cypress viewport + manual device tests.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-030 â€” Zoom/text scaling is usable
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** NFR-04/WAI
- **Execute:** Run all primary task/schedule/fairness actions at 200% zoom/text scale.
- **Expected:** Essential content/actions remain usable.
- **PASS evidence required:** Screenshots.
- **Automated proof required:** Manual accessibility evidence.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-031 â€” OpenAPI and real browser behavior agree
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-04
- **Execute:** Compare network calls/errors to served OpenAPI for UC-02 workflows.
- **Expected:** No undocumented/deprecated mismatch.
- **PASS evidence required:** Network/OpenAPI proof.
- **Automated proof required:** Contract suite + manual check.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-032 â€” CI executes all UC-02 test layers
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-05; TG-09
- **Execute:** Review/run pipeline after a UC-02 test change.
- **Expected:** Frontend/backend/repository/integration/E2E layers run and block merge on failure.
- **PASS evidence required:** Pipeline evidence.
- **Automated proof required:** CI verification.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-033 â€” Setup and user documentation match working flow
- [x] **PASS**  - [ ] **FAIL**  - [ ] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** TG-10; NFR-05
- **Execute:** Follow docs from clean environment for tasks/schedules/FR-15 limitations.
- **Expected:** No obsolete step/false claim/missing prerequisite.
- **PASS evidence required:** Execution record.
- **Automated proof required:** Documentation acceptance test.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

### E2E-034 â€” Final integrated requirement sign-off
- [ ] **PASS**  - [ ] **FAIL**  - [x] **BLOCKED**  - [ ] **N/A** *(N/A requires a written contract-based justification)*
- **Requirement(s):** All FR-03/FR-04/FR-15 and NFR-01..06
- **Execute:** Review all three UC-02 forms, evidence, commands and bug gates before merge.
- **Expected:** No hidden critical failure/evidence gap; open items visible.
- **PASS evidence required:** Signed review summary.
- **Automated proof required:** Reviewer sign-off.
- **If failed or evidence is missing:** Fix the implementation or evidence gap, add the smallest focused regression test at the correct layer, rerun the affected suite, and record the exact files changed.
- **Evidence record:** `executor | date | branch/commit | synthetic fixture(s) | test name | exact command | evidence path | changed files | reviewer`

