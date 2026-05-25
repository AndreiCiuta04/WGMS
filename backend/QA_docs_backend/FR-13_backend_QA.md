# FR-13 Backend QA Checklist - History of Tasks and Payments

## Feature Information

- Feature ID: FR-13
- Feature name: History of tasks and payments
- Use case: UC-06 - Central Dashboard and Notifications Management
- Priority: High
- Category: Core
- Version: V3
- Branch: `FR-13/History-of-tasks-and-payments`
- Backend technology: Java Spring Boot
- Security: Spring Security, JWT
- Database: PostgreSQL
- Main endpoint: `GET /api/v1/history`
- Expected response DTO: `HistoryDto`
- Current limitation: payment history is backed by recorded FR-05 `Expense` rows until FR-06 provides real persisted payment/settlement records.

## How to Use This Checklist

This checklist is filled for the current FR-13 implementation. A checked item means the behavior was verified by code inspection, automated tests, or the listed command evidence. Merge-blocker items are phrased as verified-negative statements so a checked box means the blocker is not present.

Evidence fields below name the tests, commands, or code paths that prove each section.

# 1. Critical Checks - API Contract

## Endpoint Existence and Method

- [x] `GET /api/v1/history` exists.
- [x] The endpoint uses HTTP `GET`, not `POST`, `PUT`, or `PATCH`.
- [x] The endpoint is located in the established endpoint package: `at.ac.tuwien.sepr.groupphase.backend.endpoint`.
- [x] The endpoint does not require a request body.
- [x] The endpoint does not require a query parameter such as `householdId`.
- [x] The endpoint does not expose household selection through path variables.
- [x] The endpoint returns HTTP `200 OK` for a valid authenticated user with an active household membership.
- [x] The endpoint returns a JSON object with the top-level FR-13 structure.

Evidence: `HistoryEndpoint.java`; `HistoryEndpointTest.givenAuthenticatedHouseholdUser_whenGetHistory_then200AndHistoryArraysAreReturned`; `cd backend && mvn test` passed.

## Expected Response Shape

Expected JSON shape:

```json
{
  "tasks": [],
  "payments": []
}
```

- [x] The top-level response object contains `tasks`.
- [x] The top-level response object contains `payments`.
- [x] `tasks` is always an array, even when empty.
- [x] `payments` is always an array, even when empty.
- [x] The backend does not return `null` instead of an empty array.
- [x] The backend does not wrap the result in an unexpected additional object such as `data`, `content`, or `result`.

Evidence: `HistoryDto` Java record; service empty-list tests; integration JSON assertions for `tasks` and `payments`.

## TaskHistoryDto Contract

Each task entry exposes:

- [x] `id`
- [x] `householdId`
- [x] `cleaningScheduleId`
- [x] `title`
- [x] `description`
- [x] `dueDate`
- [x] `status`
- [x] `createdByEmail`
- [x] `assignedToEmail`
- [x] `createdAt`
- [x] `updatedAt`

Field mapping:

- [x] `id` maps to the task ID.
- [x] `householdId` maps to the task household ID.
- [x] `cleaningScheduleId` is present for cleaning-schedule-linked tasks when available.
- [x] `cleaningScheduleId` is `null` for manually created tasks without a cleaning schedule.
- [x] `title` maps to the task title.
- [x] `description` maps to the task description and may be `null` or empty.
- [x] `dueDate` serializes using the existing Jackson date/time configuration.
- [x] `status` serializes as readable enum text such as `DONE` or `CANCELLED`.
- [x] `createdByEmail` maps to the creator user's email.
- [x] `assignedToEmail` maps to the assignee user's email, null-safe if assignment is optional.
- [x] `createdAt` maps to the task creation timestamp.
- [x] `updatedAt` maps to the task update/completion/cancellation timestamp.

Evidence: `TaskHistoryDto` Java record; `SimpleHistoryService.taskToTaskHistoryDto`; service test assertions for all required fields; integration test includes a cleaning-schedule-linked task.

## PaymentHistoryDto Contract

Each payment/expense entry exposes:

- [x] `id`
- [x] `title`
- [x] `amount`
- [x] `status`
- [x] `payerEmail`
- [x] `receiverEmail`
- [x] `createdAt`
- [x] `updatedAt`

Field mapping:

- [x] `id` maps to the expense ID for recorded expenses.
- [x] `title` maps to `expense.description`.
- [x] `amount` maps to `expense.totalAmount`.
- [x] `status` is `"RECORDED"` for expense-backed entries.
- [x] `payerEmail` maps to `expense.paidBy.user.email`.
- [x] `receiverEmail` is `null` because no real receiver/payment target exists yet.
- [x] `createdAt` maps to `expense.createdAt`.
- [x] `updatedAt` is `null` because the current `Expense` mapping does not provide an update timestamp.
- [x] The DTO name and structure are generic enough for FR-06 to later map real payment/settlement records into the same response shape.

Evidence: `PaymentHistoryDto` Java record; `SimpleHistoryService.expenseToPaymentHistoryDto`; service tests for `"RECORDED"`, payer email, null receiver, and null updated timestamp.

# 2. Critical Checks - Authentication

- [x] `GET /api/v1/history` is protected by Spring Security.
- [x] The endpoint uses `@Secured("ROLE_USER")`.
- [x] An unauthenticated request is rejected by the project-standard Spring Security response.
- [x] The endpoint does not return history data to unauthenticated users.
- [x] The endpoint uses the current authenticated principal instead of trusting client-provided user data.
- [x] The implementation uses `Authentication.getName()`.
- [x] A forged email, user ID, household ID, or roommate ID in the request has no effect because the endpoint accepts no such input.
- [x] Authentication failure does not produce a stack trace in the response body.
- [x] Authentication failure is covered by an integration test.

Evidence: `HistoryEndpoint.getHistory(Authentication authentication)`; `HistoryEndpointTest.givenNoToken_whenGetHistory_then403`; full backend test suite passed.

# 3. Critical Checks - Authorization and Household Isolation

## Active Roommate Resolution

- [x] The backend resolves the authenticated user by email.
- [x] The backend resolves the user's active `Roommate` membership.
- [x] The backend derives the household from the active `Roommate` membership.
- [x] A user without active roommate membership receives `ForbiddenException`.
- [x] A user without household membership receives no unrelated household history.
- [x] Old or inactive memberships are not used to fetch history.
- [x] The implementation follows the existing one-active-household membership model.

Evidence: `SimpleHistoryService.getHistory`; `RoommateRepository.findByUserIdAndActiveTrue`; `SimpleHistoryServiceTest.givenUserWithoutActiveHousehold_whenGetHistory_thenForbidden`.

## Household Data Isolation

- [x] The query for tasks filters by the authenticated user's household.
- [x] The query for expenses filters by the authenticated user's household.
- [x] A user from household A cannot see tasks from household B.
- [x] A user from household A cannot see expenses from household B.
- [x] Cross-household tasks with similar titles, dates, users, or statuses are not accidentally included.
- [x] Cross-household expenses with similar descriptions, amounts, or payer emails are not accidentally included.
- [x] The endpoint does not fetch all tasks or all expenses globally.
- [x] Repository methods use household-scoped conditions.
- [x] Household isolation is verified with integration data containing at least two households.
- [x] Response fields do not leak sensitive nested objects from other households.

Evidence: `TaskRepository.findAllByHousehold_IdAndStatusInOrderByUpdatedAtDescDueDateDescCreatedAtDesc`; `ExpenseRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDesc`; `HistoryEndpointTest` other-household assertions; `SimpleHistoryServiceTest` repository-scope assertions.

# 4. Critical Checks - Task History Correctness

## Included Task Statuses

- [x] Tasks with `TaskStatus.DONE` are included.
- [x] Tasks with `TaskStatus.CANCELLED` are included.
- [x] Done manually created tasks are included.
- [x] Cancelled manually created tasks are included.
- [x] Done tasks generated from or linked to cleaning schedules are included.
- [x] Cancelled tasks generated from or linked to cleaning schedules are supported by the same mapping.
- [x] Tasks with no `cleaningScheduleId` are not dropped accidentally.
- [x] Tasks with a `cleaningScheduleId` are not dropped accidentally.

Evidence: `SimpleHistoryService` passes `List.of(TaskStatus.DONE, TaskStatus.CANCELLED)`; service test verifies manual DONE/CANCELLED tasks; integration test verifies a cleaning-schedule-linked history task.

## Excluded Task Statuses

- [x] Tasks with `TaskStatus.OPEN` are excluded.
- [x] Tasks with `TaskStatus.IN_PROGRESS` are excluded.
- [x] Any other non-history status is excluded by the explicit allowed-status list.
- [x] Open tasks from the same household do not appear in the response.
- [x] In-progress tasks from the same household do not appear in the response.
- [x] Open or in-progress tasks from another household do not appear.
- [x] Exclusion is implemented at repository-query level.
- [x] Exclusion is covered by service and integration tests.

Evidence: `SimpleHistoryServiceTest.givenActiveHousehold_whenGetHistory_thenOpenAndInProgressStatusesAreNotRequested`; `HistoryEndpointTest` JSON absence assertions.

## Task Field Mapping

- [x] Task ID is correct.
- [x] Household ID is correct.
- [x] Cleaning schedule ID is correct or `null`.
- [x] Title is correct.
- [x] Description is correct.
- [x] Due date is correct.
- [x] Status is correct.
- [x] Creator email is correct.
- [x] Assignee email is correct.
- [x] Created timestamp is correct.
- [x] Updated timestamp is correct.
- [x] Null optional fields do not crash mapping.
- [x] Missing assignee, where allowed by the domain model, does not crash mapping.
- [x] Missing description does not crash mapping.
- [x] Missing cleaning schedule does not crash mapping.

Evidence: `SimpleHistoryService.taskToTaskHistoryDto`; service field-level assertions; null-safe helper methods in service mapping.

# 5. Critical Checks - Payment / Expense History Correctness

## Expense-Backed Payment Entries

- [x] Existing FR-05 expense rows are included as payment-related history.
- [x] Only expenses from the current household are included.
- [x] Expenses from another household are excluded.
- [x] Expense ID maps to `PaymentHistoryDto.id`.
- [x] Expense description maps to `PaymentHistoryDto.title`.
- [x] Expense total amount maps to `PaymentHistoryDto.amount`.
- [x] Expense paid-by user email maps to `PaymentHistoryDto.payerEmail`.
- [x] `PaymentHistoryDto.status` is `"RECORDED"` for expense-backed entries.
- [x] `PaymentHistoryDto.receiverEmail` is `null` because the backend has no real receiver/payment target yet.
- [x] `PaymentHistoryDto.createdAt` maps to `expense.createdAt`.
- [x] `PaymentHistoryDto.updatedAt` is `null`.
- [x] The backend does not invent fake receiver information.
- [x] The backend does not invent fake payment statuses.
- [x] The backend does not require FR-06 payment entities to exist before FR-13 works.

Evidence: `SimpleHistoryService.expenseToPaymentHistoryDto`; `ExpenseRepository` household-scoped query; `SimpleHistoryServiceTest.givenExpenseHistory_whenGetHistory_thenExpensesAreMappedAsRecordedPayments`; integration test payment JSON assertions.

## Future FR-06 Compatibility

- [x] DTO and service naming do not prevent adding real payment/settlement records later.
- [x] The `payments` array can later contain settlement/payment records without changing the frontend contract.
- [x] QA documentation explains that payments are currently backed by recorded expenses.
- [x] No hard-coded logic represents expenses as completed bank transfers.
- [x] Tests describe the current limitation through expected status `"RECORDED"`.

Evidence: `PaymentHistoryDto`; QA docs; service/integration tests expecting `"RECORDED"`.

# 6. Important Checks - Sorting

## Task Sorting

- [x] Task history is sorted newest/history-relevant first.
- [x] Sorting uses `updatedAt DESC`.
- [x] Fallback ordering uses `dueDate DESC` and `createdAt DESC`.
- [x] Sorting is deterministic when tasks share the primary timestamp.
- [x] Sorting behavior is encoded in the repository method name.
- [x] Sorting is not left to database default row order.

Evidence: `TaskRepository.findAllByHousehold_IdAndStatusInOrderByUpdatedAtDescDueDateDescCreatedAtDesc`.

## Payment / Expense Sorting

- [x] Payment/expense history is sorted newest first.
- [x] Expense-backed entries use `date DESC` then `createdAt DESC`.
- [x] The response contract can later support updated payment sorting when FR-06 exists.
- [x] Sorting is deterministic when expenses share the primary date.
- [x] Sorting behavior is encoded in the repository method name.
- [x] Sorting is not left to database default row order.

Evidence: `ExpenseRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDesc`.

# 7. Important Checks - Repository and Query Design

## Task Repository

- [x] A repository method fetches history tasks by household and allowed statuses.
- [x] The task query filters by household ID.
- [x] The task query filters by status in `DONE`, `CANCELLED`.
- [x] The task query applies deterministic sorting.
- [x] The query does not fetch all tasks across all households.
- [x] The query uses `@EntityGraph` to avoid avoidable N+1 lookups for mapped relations.
- [x] Creator, assignee, household, and cleaning schedule relations are loaded safely.
- [x] The repository method name is readable and maintainable.

Evidence: `TaskRepository` method with `@EntityGraph(attributePaths = {"household", "createdBy", "assignedTo", "cleaningSchedule"})`.

## Expense Repository

- [x] A repository method fetches expenses by household.
- [x] The expense query filters by household ID.
- [x] The expense query applies deterministic sorting.
- [x] The query does not fetch all expenses across all households.
- [x] The query uses `@EntityGraph` to avoid avoidable N+1 lookups for payer email.
- [x] `paidBy.user` is loaded safely for payer email mapping.
- [x] The repository method name is readable and maintainable.

Evidence: `ExpenseRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDesc` with entity graph for `paidBy.user`.

# 8. Important Checks - DTO and Serialization

- [x] DTOs are placed in the established backend DTO package.
- [x] DTO field names match the frontend contract.
- [x] Date/time fields serialize through existing Jackson configuration.
- [x] Amount values serialize as numeric `BigDecimal` values.
- [x] No entity objects are serialized directly.
- [x] No passwords, tokens, internal security fields, or unrelated user data are serialized.
- [x] Email fields only expose emails needed by the history view.
- [x] `null` values are handled consistently with project JSON behavior.
- [x] The response is stable for frontend DTO typing.
- [x] DTO names are clear: `HistoryDto`, `TaskHistoryDto`, `PaymentHistoryDto`.
- [x] DTOs are Java records, not verbose mutable classes.

Evidence: `HistoryDto.java`; `TaskHistoryDto.java`; `PaymentHistoryDto.java`; integration JSON assertions.

# 9. Important Checks - Error Handling

- [x] Missing authenticated user is handled by Spring Security before service execution.
- [x] Unknown authenticated email is handled consistently by the user lookup path.
- [x] User without active roommate membership throws `ForbiddenException`.
- [x] User without household membership receives a safe error response.
- [x] Database errors are not exposed as raw stack traces by FR-13 code.
- [x] Mapping errors do not expose internal implementation details.
- [x] The endpoint uses the project-standard exception handling path.
- [x] Error cases are covered where practical.
- [x] Logs do not include full private history payloads from FR-13.

Evidence: `ForbiddenException` service test; unauthenticated integration test; code inspection of endpoint/service.

# 10. Important Checks - OpenAPI / Swagger / API Documentation

- [x] Formal OpenAPI/Swagger documentation is not used for this feature in the current project structure.
- [x] FR-13 backend QA documentation records the endpoint contract.
- [x] Authentication requirement is documented in this QA file.
- [x] Response DTO structure is documented in this QA file.
- [x] Task history fields are documented in this QA file.
- [x] Payment history fields are documented in this QA file.
- [x] Documentation says household is inferred from the authenticated user.
- [x] Documentation does not suggest that the frontend should send `householdId`.
- [x] Documentation notes that payment entries are backed by recorded expenses until FR-06.
- [x] Documentation examples do not contain real private user data.

Evidence: this file; `docs/FR-13_general_QA.md`; frontend QA file.

# 11. Important Checks - Unit Tests

## Service Unit Tests

- [x] Service test covers successful history retrieval.
- [x] Service test verifies active roommate membership resolution.
- [x] Service test verifies household ID is derived from the authenticated user.
- [x] Service test verifies only `DONE` and `CANCELLED` tasks are included.
- [x] Service test verifies `OPEN` and `IN_PROGRESS` tasks are excluded.
- [x] Service test verifies manual tasks are included.
- [x] Service test verifies cleaning-schedule-linked tasks are included.
- [x] Service test verifies task DTO mapping.
- [x] Service test verifies expense-to-payment DTO mapping.
- [x] Service test verifies empty tasks array.
- [x] Service test verifies empty payments array.
- [x] Service test verifies repository-level sorting method is called.
- [x] Service test verifies null-safe mapping of optional fields.
- [x] Service test verifies user-without-household behavior.
- [x] Service test verifies repository calls are household-scoped.

Evidence: `SimpleHistoryServiceTest`; `cd backend && mvn test` passed.

## Mapper Unit Tests

- [x] Separate mapper classes are not used; mapping is tested through `SimpleHistoryServiceTest`.
- [x] Task mapping covers all fields.
- [x] Payment/expense mapping covers all fields.
- [x] Mapping handles `null` description.
- [x] Mapping handles `null` cleaning schedule.
- [x] Mapping handles `null` receiver email.
- [x] Mapping handles `null` updated timestamp.
- [x] Mapping does not expose nested entity objects.

Evidence: `SimpleHistoryServiceTest` field mapping and null-safe mapping assertions.

# 12. Critical Checks - Integration Tests

## Successful Request

- [x] Authenticated user with active household receives `200 OK`.
- [x] Response contains `tasks`.
- [x] Response contains `payments`.
- [x] Response contains completed tasks.
- [x] Response contains cancelled tasks.
- [x] Response contains recorded expenses mapped into payments.
- [x] Response excludes open tasks.
- [x] Response excludes in-progress tasks.

Evidence: `HistoryEndpointTest.givenAuthenticatedHouseholdUser_whenGetHistory_then200AndHistoryArraysAreReturned`.

## Authentication

- [x] Unauthenticated request to `GET /api/v1/history` fails.
- [x] Invalid-token behavior is covered by the shared security filter behavior used by endpoint tests.
- [x] Expired or malformed token handling is delegated to the existing JWT security stack.

Evidence: `HistoryEndpointTest.givenNoToken_whenGetHistory_then403`; existing security test coverage; full backend suite passed.

## Household Isolation

- [x] Integration test data includes at least two households.
- [x] Household A has completed/cancelled tasks and expenses.
- [x] Household B has completed/cancelled tasks and expenses.
- [x] User A sees only household A tasks.
- [x] User A sees only household A payments/expenses.
- [x] Other household IDs, titles, descriptions, amounts, emails, and timestamps are not returned.
- [x] Household isolation is asserted at HTTP response level.

Evidence: `HistoryEndpointTest` creates other-household task and expense and asserts they are absent.

## Empty State Backend Behavior

- [x] User with household but no done/cancelled tasks receives `tasks: []`.
- [x] User with household but no expenses receives `payments: []`.
- [x] User with neither task history nor expenses receives both arrays as empty.
- [x] Empty response still returns `200 OK`.

Evidence: `SimpleHistoryServiceTest` empty-array tests; endpoint returns `HistoryDto` with lists.

# 13. Regression Checks for Existing Backend Features

## FR-03 Task Creation and Assignment

- [x] Creating a task still works under the existing test suite.
- [x] Assigning a task still works under the existing test suite.
- [x] Existing task list behavior is not changed by FR-13.
- [x] Existing task DTOs are not modified by FR-13 history DTOs.
- [x] Existing task status transitions still work under integration/e2e setup.
- [x] Existing task tests pass.

Evidence: full `mvn test` passed; FR-13 adds read-only history mapping.

## FR-04 Cleaning Schedule Management

- [x] Existing cleaning schedule behavior is not changed by FR-13.
- [x] Generated or linked schedule tasks remain represented by `cleaningScheduleId`.
- [x] Existing cleaning schedule tests pass as part of the backend suite.
- [x] FR-13 does not change active schedule logic.

Evidence: full `mvn test` passed; integration test saves a cleaning schedule and reads its linked task through history.

## FR-05 Expense Tracking

- [x] Creating an expense still works under e2e setup.
- [x] Listing/mapping expenses is not changed outside the history read path.
- [x] Expense paid-by relation is preserved.
- [x] Expense amount precision is unchanged.
- [x] Existing expense tests pass as part of the backend suite.

Evidence: full `mvn test` passed; Cypress history setup creates an expense through the existing API.

## FR-06 Payment Status Overview Compatibility

- [x] FR-13 does not require FR-06 to be fully implemented.
- [x] FR-13 does not block later FR-06 payment/settlement entities.
- [x] Current limitation is documented: payments are backed by recorded expenses.

Evidence: DTO/service naming; QA docs; tests assert `"RECORDED"` rather than fake settlement statuses.

# 14. Non-Functional Checks

## Security

- [x] Endpoint is authenticated.
- [x] Endpoint is household-scoped.
- [x] No household ID is accepted from the client.
- [x] No cross-household data can be returned by the tested query path.
- [x] No sensitive user data is exposed beyond necessary emails.
- [x] No raw entity serialization leaks internal fields.
- [x] Error messages do not leak internal database details in FR-13 code.
- [x] Tests prove household isolation.

Evidence: security annotation; service repository scope; integration tests.

## Privacy

- [x] Users only see history belonging to their own household.
- [x] Emails shown are only those relevant to task/payment history.
- [x] No unrelated roommates or external users are exposed.
- [x] FR-13 code does not log full response payloads.

Evidence: DTO shape and integration absence assertions.

## Auditability

- [x] History entries include timestamps.
- [x] Task entries include creator and assignee emails.
- [x] Payment/expense entries include payer email.
- [x] Status values are explicit and human-readable.
- [x] The backend preserves enough information to explain why an entry appears in history.

Evidence: DTO fields; component rendering tests; service mapping tests.

## Performance

- [x] Queries are household-scoped.
- [x] Queries are sorted at database level.
- [x] Endpoint does not fetch all households' tasks or expenses into memory.
- [x] Entity graphs reduce avoidable relation lookups for mapped fields.
- [x] Large household histories are handled reasonably for current scope.
- [x] Pagination is not implemented and is accepted as a future improvement if history grows large.

Evidence: repository method names and entity graphs.

## Maintainability

- [x] Code follows project package structure.
- [x] Endpoint, service, repository, and DTO responsibilities are separated.
- [x] Mapping logic is understandable and tested.
- [x] Current expense-backed payment limitation is explained in QA docs.
- [x] Naming is clear and consistent with existing project style.
- [x] No unrelated backend refactoring is mixed into the feature.

Evidence: code inspection; changed files limited to FR-13 history and tests/docs.

## Testability

- [x] Service logic is unit tested.
- [x] Repository behavior is covered through integration tests.
- [x] Endpoint behavior is tested with authenticated requests.
- [x] Test data covers multiple households.
- [x] Test data covers all relevant task statuses.
- [x] Test data covers empty history cases.
- [x] Test data covers expenses and optional null fields.

Evidence: `SimpleHistoryServiceTest`; `HistoryEndpointTest`.

# 15. Build and Quality Checks

- [x] Backend compiles successfully.
- [x] Backend tests pass.
- [x] New tests are included in the correct backend test packages.
- [x] No skipped or disabled tests were added for FR-13.
- [x] No unrelated tests were removed.
- [x] No unrelated production backend files were modified.
- [x] Static analysis/linting is not separately configured for backend in this QA run.
- [x] CI pipeline can run the same backend command.
- [x] Application starts successfully with the feature included for e2e.
- [x] Manual-equivalent API behavior is confirmed by integration and Cypress e2e setup.

Evidence: `cd backend && mvn test` passed with 162 tests, 0 failures, 0 errors, 0 skipped; backend was started successfully for Cypress.

# 16. Backend Merge Blockers

- [x] Verified blocker not present: `GET /api/v1/history` is unauthenticated.
- [x] Verified blocker not present: frontend/backend contract requires the frontend to send `householdId`.
- [x] Verified blocker not present: endpoint can return another household's tasks.
- [x] Verified blocker not present: endpoint can return another household's expenses.
- [x] Verified blocker not present: open or in-progress tasks appear in task history.
- [x] Verified blocker not present: done or cancelled tasks are missing without a valid reason.
- [x] Verified blocker not present: expense-backed payment history is missing despite FR-05 expenses existing.
- [x] Verified blocker not present: DTOs expose raw entities or sensitive fields.
- [x] Verified blocker not present: empty history crashes the endpoint.
- [x] Verified blocker not present: tests do not cover household isolation.
- [x] Verified blocker not present: existing task or expense functionality breaks in the backend test suite.

Evidence: full backend test run; endpoint/service/repository inspection.

# 17. Backend Final Approval

- [x] All critical backend checks passed.
- [x] Important backend checks passed or are documented with accepted reasons.
- [x] Unit tests added or updated.
- [x] Integration tests added or updated.
- [x] Regression tests pass.
- [x] Security and privacy checks passed.
- [x] Known limitation around payment history is documented.
- [x] Endpoint behavior was inspected and tested.
- [x] Backend is ready for FR-13 merge review.

Final evidence summary: `cd backend && mvn test` passed with 162 tests, 0 failures, 0 errors, 0 skipped.
