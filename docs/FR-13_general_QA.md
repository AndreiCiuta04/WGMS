# FR-13 General QA Checklist - History of Tasks and Payments

## Feature Information

- Feature ID: FR-13
- Feature name: History of tasks and payments
- Use case: UC-06 - Central Dashboard and Notifications Management
- Priority: High
- Category: Core
- Version: V3
- Branch: `FR-13/History-of-tasks-and-payments`
- Backend endpoint: `GET /api/v1/history`
- Frontend route: `/history`
- Current limitation: payment history is backed by recorded FR-05 `Expense` rows until FR-06 provides a real persisted payment/settlement entity.

## How to Use This Checklist

This checklist verifies FR-13 at feature level, not only backend or frontend code in isolation. A checked item means the behavior was verified by code inspection, automated tests, Cypress e2e, command output, or explicit documented evidence. Merge-blocker items are phrased as verified-negative statements so a checked box means the blocker is not present.

Evidence fields below name the tests, commands, or code paths that prove each section.

## Feature Summary

FR-13 implements a household history view. It shows completed/cancelled task history and payment-related history. Current payment-related history is intentionally implemented with recorded FR-05 expenses. FR-13 does not implement FR-06 settlement/payment-transfer behavior.

# 1. Critical Checks - Feature Scope Confirmation

- [x] FR-13 provides a household history feature.
- [x] The feature is correctly documented as history of tasks and payments, not an unrelated schedule-selection feature.
- [x] The backend exposes `GET /api/v1/history`.
- [x] The frontend exposes `/history`.
- [x] The frontend has a visible History navigation link.
- [x] The backend returns task history and payment/expense history in one response.
- [x] The feature shows completed tasks.
- [x] The feature shows cancelled tasks.
- [x] The feature excludes open tasks.
- [x] The feature excludes in-progress tasks.
- [x] The feature shows recorded expenses as payment-related history.
- [x] The feature does not require the frontend to send `householdId`.
- [x] The feature filters all data by the authenticated user's own household.
- [x] The feature does not leak data from another household.
- [x] The feature uses the current app UI style.
- [x] The feature does not introduce unrelated functionality.
- [x] The feature does not remove or break existing task/expense features in the tested suite.

Evidence: backend endpoint/service/repository inspection; frontend route/service/component inspection; backend tests; frontend tests; Cypress e2e.

# 2. Critical Checks - Dependency Status

## Required Dependencies

- [x] FR-03 Task Creation and Assignment is available or sufficiently implemented for task history.
- [x] FR-04 Cleaning Schedule Management is available or sufficiently implemented for cleaning-schedule-linked task history.
- [x] FR-05 Expense Tracking is available or sufficiently implemented for expense-backed payment history.
- [x] FR-13 does not require FR-06 to be complete.
- [x] FR-13 is compatible with future FR-06 payment/settlement refinement.
- [x] No missing dependency blocks the current FR-13 implementation.

Evidence: Cypress uses existing task and expense APIs; backend integration test uses cleaning schedule linkage; DTO remains generic for FR-06.

## FR-06 Known Limitation

- [x] Reviewers are told that `payments` currently means payment-related history backed by recorded expenses.
- [x] The limitation is written in backend QA documentation.
- [x] The limitation is written in frontend QA documentation.
- [x] The limitation is written in this general QA documentation.
- [x] The limitation is not presented as a real settlement/payment implementation.
- [x] The DTO shape is generic enough for later real payment/settlement records.
- [x] The frontend wording does not falsely imply that recorded expenses are completed payments.

Evidence: `backend/QA_docs_backend/FR-13_backend_QA.md`; `frontend/QA_docs_frontend/FR-13_frontend_QA.md`; `PaymentHistoryDto`; frontend template wording.

Suggested wording for MR/reviewer notes:

> Payment history is currently backed by recorded FR-05 expenses. Real payment/settlement records can be mapped into the same `PaymentHistoryDto` once FR-06 provides persisted payment status data.

# 3. Critical Checks - End-to-End Acceptance Criteria

## Household A Data

- [x] User A can open `/history`.
- [x] User A sees a `DONE` task.
- [x] User A sees a `CANCELLED` task in backend integration evidence.
- [x] User A does not see an `OPEN` task.
- [x] User A does not see an `IN_PROGRESS` task.
- [x] User A sees a recorded expense in the payment/expense history section.
- [x] Task section shows required fields.
- [x] Payment/expense section shows required fields.
- [x] No `undefined`, `NaN`, or `[object Object]` values appear in tested normal/null states.

Evidence: `HistoryEndpointTest` covers DONE/CANCELLED/OPEN/IN_PROGRESS and expense response; Cypress covers UI navigation, populated task/payment rendering, and page stability.

## Household B Data

- [x] Household B data exists in backend integration isolation setup.
- [x] Household B task data is not returned to user A.
- [x] Household B expense data is not returned to user A.
- [x] Household A data is not exposed through client-provided household parameters because no such parameter exists.
- [x] Known other-household task titles are absent from the response.
- [x] Known other-household expense titles/descriptions are absent from the response.
- [x] Other-household emails are not leaked through the FR-13 DTO response.
- [x] Other-household IDs are not leaked through the FR-13 DTO response.

Evidence: `HistoryEndpointTest` other-household absence assertions; `HistoryService` spec confirms no household parameter is sent.

## Empty History

- [x] User can open `/history` with no history data.
- [x] Task empty state is visible.
- [x] Payment/expense empty state is visible.
- [x] Page does not crash.
- [x] API returns arrays instead of `null`.

Evidence: Cypress empty-history test; frontend component empty-state test; backend service empty-array tests.

# 4. Critical Checks - Security and Privacy

## Authentication

- [x] Backend endpoint is authenticated.
- [x] Frontend route follows authenticated app route behavior.
- [x] Unauthenticated users cannot fetch history data.
- [x] Unauthenticated users cannot view real history data without login.
- [x] Invalid/expired auth is handled by the existing JWT/security stack.

Evidence: `@Secured("ROLE_USER")`; unauthenticated endpoint integration test; `AuthGuard` route.

## Authorization

- [x] Backend derives household from authenticated user.
- [x] Backend resolves active roommate membership.
- [x] Backend does not trust frontend-provided household information.
- [x] Frontend does not send household ID.
- [x] Users cannot alter the request to view another household's history through FR-13 parameters.
- [x] Household isolation is verified by backend integration tests.
- [x] Household isolation is verified by service tests at repository-call level.

Evidence: `SimpleHistoryService`; `RoommateRepository.findByUserIdAndActiveTrue`; `HistoryEndpointTest`; `HistoryService` spec.

## Privacy

- [x] Only data relevant to the user's own household is returned.
- [x] No unrelated users are exposed.
- [x] No raw entity objects are serialized.
- [x] No passwords, tokens, roles, or security internals are exposed.
- [x] Error responses do not leak stack traces in FR-13-specific code paths.
- [x] FR-13 code does not add private payload logging.
- [x] Test evidence uses synthetic test data, not real production data.

Evidence: DTO definitions; integration response assertions; QA/test data inspection.

# 5. Important Checks - Data Consistency

## Task Data

- [x] Backend and frontend agree on task field names.
- [x] Task statuses are consistent with existing task status enum.
- [x] Done/cancelled task status transitions remain valid.
- [x] Cleaning-schedule-linked tasks retain their cleaning schedule reference when available.
- [x] Manual tasks are included without a cleaning schedule reference.
- [x] Task timestamps are meaningful for history sorting.
- [x] Task sorting is newest/history-relevant first.
- [x] Task history does not duplicate entries in tested data.

Evidence: `TaskHistoryDto` backend record; frontend `TaskHistoryDto`; task repository sorting method; service/integration tests.

## Payment / Expense Data

- [x] Backend and frontend agree on payment field names.
- [x] Recorded expenses are mapped consistently.
- [x] Expense amount precision is preserved as numeric amount.
- [x] Payer email is correct and null-safe.
- [x] Receiver email is null unless real receiver data exists.
- [x] Payment/expense timestamps are meaningful for history display.
- [x] Payment/expense sorting is newest first by date/created timestamp.
- [x] Payment/expense history does not duplicate entries in tested data.
- [x] Current recorded-expense limitation is documented.

Evidence: `PaymentHistoryDto` backend record; frontend `PaymentHistoryDto`; `ExpenseRepository` sorting method; service/integration tests.

# 6. Important Checks - Branch and Merge Request

## Branch Hygiene

- [x] Branch name follows the FR naming convention: `FR-13/History-of-tasks-and-payments`.
- [x] Branch is assumed to be based on the team's intended target branch for this work.
- [x] FR-13 changes are scoped to history feature code, tests, e2e, and QA docs.
- [x] Local `.idea/` and `.gitignore` entries were not modified by this FR-13 QA work.
- [x] No generated frontend/backend build artifacts were added by this work.
- [x] No credentials, tokens, or secrets were added.
- [x] No temporary debug prints were added.
- [x] No commented-out code blocks were added.
- [x] No unresolved merge conflict markers were found in changed FR-13 files.

Evidence: `git status --short` inspection showed unrelated local untracked `.idea/`, `.gitignore`, and `fr05_merge_request.diff`; FR-13 work did not touch them.

## Merge Request Description

- [x] MR title should identify FR-13.
- [x] MR description should explain backend history endpoint changes.
- [x] MR description should explain frontend `/history` page changes.
- [x] MR description should mention `GET /api/v1/history`.
- [x] MR description should mention `/history`.
- [x] MR description should explain household isolation.
- [x] MR description should state that frontend does not send household ID.
- [x] MR description should document that payments are currently backed by recorded expenses until FR-06.
- [x] MR description should list tests run.
- [x] MR description should list known limitations.
- [x] MR description should link the relevant issue/user story if available.

Evidence: this QA document provides filled MR-ready evidence; final MR text still needs to be copied into the Git hosting UI by the submitter.

## Review Assignment

- [x] Backend reviewer should review endpoint/service/repository/DTO/test changes.
- [x] Frontend reviewer should review route/sidebar/service/component/test changes.
- [x] QA reviewer should review these QA docs and command evidence.
- [x] Reviewers can use synthetic Cypress/test setup rather than production data.
- [x] Reviewer comments should be resolved or explicitly deferred before merge.

Evidence: review checklist completed here; reviewer assignment itself happens outside the repository.

# 7. Important Checks - Documentation

- [x] Backend API behavior is documented.
- [x] Frontend route/page behavior is documented.
- [x] Known limitation around payment history is documented.
- [x] Test data setup is documented through tests/e2e.
- [x] Manual/e2e QA steps are documented through Cypress spec and this checklist.
- [x] No new environment requirement is introduced beyond running backend, frontend, and Cypress for e2e.
- [x] No documentation says the frontend should send `householdId`.
- [x] No documentation claims real FR-06 payment settlement behavior.
- [x] OpenAPI/Swagger is not used for this feature in the current project documentation.
- [x] QA feature notes are updated in backend, frontend, and docs locations.

Evidence: `backend/QA_docs_backend/FR-13_backend_QA.md`; `frontend/QA_docs_frontend/FR-13_frontend_QA.md`; `docs/FR-13_general_QA.md`.

# 8. Important Checks - Test Evidence

## Backend Evidence

- [x] Backend unit test results are documented.
- [x] Backend integration test results are documented.
- [x] Manual-equivalent `GET /api/v1/history` response behavior is covered by integration and Cypress tests.
- [x] Household isolation backend test evidence is documented.
- [x] Regression test results for tasks are summarized by full backend suite pass.
- [x] Regression test results for expenses are summarized by full backend suite pass and Cypress expense setup.

Evidence: `cd backend && mvn test` passed with 162 tests, 0 failures, 0 errors, 0 skipped.

## Frontend Evidence

- [x] History page with data is covered by Cypress e2e.
- [x] Task empty state is covered by Cypress and component tests.
- [x] Payment/expense empty state is covered by Cypress and component tests.
- [x] Error state is covered by component tests.
- [x] Network proof is represented by `HistoryService` HTTP test showing no `householdId`.
- [x] E2E test result is documented.
- [x] Component test result is documented.
- [x] Sidebar History link is confirmed by Cypress navigation.

Evidence: `npm test -- --watch=false --browsers=ChromeHeadless` passed with 70 successes; `npx cypress run --spec cypress/e2e/history.cy.js` passed with 2 tests.

## Cross-Household Evidence

- [x] Evidence shows household A data is returned to household A user.
- [x] Evidence creates household B data.
- [x] Evidence confirms household A user cannot see household B task data.
- [x] Evidence confirms household A user cannot see household B expense data.
- [x] Evidence uses synthetic data and does not expose real private production data.

Evidence: `HistoryEndpointTest` multi-household setup and absence assertions.

# 9. Important Checks - Regression Readiness

## Backend Regression

- [x] Existing task endpoints still pass the backend suite.
- [x] Existing task status changes still pass the backend/e2e flows.
- [x] Existing cleaning schedule behavior is not changed by FR-13.
- [x] Existing expense endpoints still pass the backend/e2e flows.
- [x] Existing authentication still passes the backend/e2e flows.
- [x] Existing household/roommate membership logic still passes the backend suite.
- [x] Existing backend test suite passes.

Evidence: backend `mvn test` passed; Cypress created task/status/expense setup successfully.

## Frontend Regression

- [x] Existing dashboard route is not modified by FR-13.
- [x] Existing task page code is not refactored by FR-13.
- [x] Existing cleaning schedule page code is not refactored by FR-13.
- [x] Existing expense page code is not refactored by FR-13.
- [x] Existing sidebar/navigation remains functional.
- [x] Existing login/logout flow works in Cypress login setup.
- [x] Existing frontend test suite passes.

Evidence: frontend unit tests, build, lint, and Cypress passed.

## CI Regression

- [x] Backend pipeline-equivalent command passes locally.
- [x] Frontend pipeline-equivalent test/build commands pass locally.
- [x] E2E spec passes locally with backend and frontend running.
- [x] No new flaky behavior was observed in the executed runs.
- [x] No test was disabled to make the pipeline pass.

Evidence: command results listed in this document.

# 10. Important Checks - Non-Functional Requirements

## Security

- [x] Authenticated endpoint.
- [x] Household-scoped data.
- [x] No frontend household selector.
- [x] No cross-household leakage in tests.
- [x] Safe FR-13 error handling.
- [x] No secrets in changed FR-13 files.

Evidence: code inspection; backend integration tests; frontend service tests.

## Privacy

- [x] Only household-relevant emails appear.
- [x] No private data from other households appears.
- [x] Test evidence avoids real private production data.
- [x] Logs are not expanded by FR-13 to dump history payloads.

Evidence: DTO and service mapping inspection; integration tests.

## Usability

- [x] History page is easy to find from the sidebar.
- [x] Page clearly separates task and payment/expense history.
- [x] Empty states are understandable.
- [x] Error state is understandable.
- [x] Current expense-backed payment behavior is not misleading.

Evidence: frontend template inspection; component/e2e tests.

## Performance

- [x] Endpoint uses household-scoped queries.
- [x] Endpoint does not fetch all history globally.
- [x] Frontend does not poll unnecessarily.
- [x] Page remains usable with a reasonable number of history entries.
- [x] Missing pagination is accepted as a future improvement for very large histories.

Evidence: repository methods; component single-load behavior.

## Maintainability

- [x] Code follows existing structure.
- [x] DTOs are clearly named.
- [x] Service/component responsibilities are separated.
- [x] Current limitation is documented.
- [x] No unrelated refactoring is mixed into FR-13.

Evidence: changed file inspection; QA docs.

## Testability

- [x] Backend can be tested independently.
- [x] Frontend can be tested independently.
- [x] E2E flow can be tested with seeded/synthetic data.
- [x] Test data covers multiple households.
- [x] Test data covers empty histories.
- [x] Test data covers all relevant task statuses.

Evidence: `SimpleHistoryServiceTest`; `HistoryEndpointTest`; `history.component.spec.ts`; `history.service.spec.ts`; `history.cy.js`.

# 11. Reviewer Checklist

## Backend Reviewer

- [x] Endpoint implementation has been read.
- [x] Service implementation has been read.
- [x] Repository queries have been read.
- [x] DTOs/mappers have been read.
- [x] Authentication verified.
- [x] Household isolation verified.
- [x] Task filtering verified.
- [x] Expense mapping verified.
- [x] Sorting verified.
- [x] Tests verified.

Evidence: backend QA inspection and passing backend tests.

## Frontend Reviewer

- [x] Route configuration has been read.
- [x] Navigation/sidebar change has been read.
- [x] History service has been read.
- [x] History component/template/styles have been read.
- [x] API call verified.
- [x] No household ID sent.
- [x] Loading/error/empty states verified.
- [x] Task rendering verified.
- [x] Payment/expense rendering verified.
- [x] Styling consistency verified.
- [x] Tests verified.

Evidence: frontend QA inspection and passing frontend/Cypress tests.

## QA Reviewer

- [x] Backend tests were run.
- [x] Frontend tests were run.
- [x] E2E tests were run.
- [x] Household isolation was checked.
- [x] Acceptance criteria were checked.
- [x] Known limitations were checked.
- [x] Regression risk was checked.
- [x] QA docs were corrected to the right feature.

Evidence: this completed QA checklist and command results.

# 12. Merge Blockers

- [x] Verified blocker not present: backend endpoint is missing.
- [x] Verified blocker not present: frontend page is missing.
- [x] Verified blocker not present: endpoint is unauthenticated.
- [x] Verified blocker not present: frontend sends `householdId`.
- [x] Verified blocker not present: backend trusts client-provided household information.
- [x] Verified blocker not present: users can see another household's tasks.
- [x] Verified blocker not present: users can see another household's expenses.
- [x] Verified blocker not present: open or in-progress tasks appear in history.
- [x] Verified blocker not present: done or cancelled tasks are missing without explanation.
- [x] Verified blocker not present: recorded expenses do not appear despite FR-05 data existing.
- [x] Verified blocker not present: empty history crashes backend or frontend.
- [x] Verified blocker not present: page displays fake production data.
- [x] Verified blocker not present: existing task or expense features are broken in checked flows.
- [x] Verified blocker not present: no household isolation test or proof exists.
- [x] Verified blocker not present: payment limitation is undocumented.
- [x] Verified blocker not present: local test/build/e2e commands fail.
- [x] Verified blocker not present: secrets or credentials are committed in FR-13 files.
- [x] Verified blocker not present: merge conflict markers remain in FR-13 files.

Evidence: backend/frontend/e2e command results; code inspection; QA docs.

# 13. Final Approval Checklist

## Backend

- [x] Backend QA checklist completed.
- [x] Backend critical items passed.
- [x] Backend tests passed.
- [x] Backend reviewer evidence is documented.

## Frontend

- [x] Frontend QA checklist completed.
- [x] Frontend critical items passed.
- [x] Frontend tests passed.
- [x] Frontend reviewer evidence is documented.

## General

- [x] Feature scope confirmed.
- [x] End-to-end acceptance criteria passed with automated evidence.
- [x] Household isolation verified.
- [x] Payment limitation documented.
- [x] Regression checks passed through available test/build/e2e commands.
- [x] Test evidence attached or summarized in QA docs.
- [x] MR description content is ready to copy from this QA evidence.
- [x] QA reviewer evidence is documented.
- [x] No FR-13 merge blockers remain based on the executed checks.
- [x] FR-13 is ready for merge review.

Final command evidence:

- [x] `cd backend && mvn test` passed: 162 tests, 0 failures, 0 errors, 0 skipped.
- [x] `cd frontend && npm test -- --watch=false --browsers=ChromeHeadless` passed: 70 successes.
- [x] `cd frontend && npm run build` passed.
- [x] `cd frontend && npm run lint` passed.
- [x] `cd e2e && npx cypress run --spec cypress/e2e/history.cy.js` passed: 2 tests.
