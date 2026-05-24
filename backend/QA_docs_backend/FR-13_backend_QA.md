# FR-13 Backend QA Checklist — History of Tasks and Payments

## Feature Information

- Feature ID: FR-13
- Feature name: History of tasks and payments
- Use case: UC-06 — Central Dashboard and Notifications Management
- Priority: High
- Category: Core
- Version: V3
- Lead: Andrei Ciuta
- Backend technology: Java Spring Boot
- Security: Spring Security, JWT
- Database: PostgreSQL
- Main endpoint: `GET /api/v1/history`
- Expected response DTO: `HistoryDto`
- Branch:
- Merge request:
- Developer:
- QA reviewer:
- Date:

---

## How to Use This Checklist

This checklist must be completed before the backend part of FR-13 can be considered done.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a short note under the relevant section.
- If something does not apply, write `N/A` with a short reason.
- Do not merge while any critical item is unchecked.
- Each item should be verified using code inspection, unit tests, integration tests, manual API calls, or database checks.

This checklist is written so that a human reviewer or AI assistant can systematically verify whether the backend implementation satisfies FR-13.

---

## Expected Backend Scope

FR-13 adds an authenticated household history endpoint.

The backend must provide:

- A `GET /api/v1/history` endpoint.
- The endpoint must use the logged-in user from `Authentication.getName()`.
- The endpoint must resolve the user’s active `Roommate` membership.
- The endpoint must derive the household from that active membership.
- The endpoint must not accept `householdId` from the frontend.
- The endpoint must return task history and payment/expense history for the current user’s own household only.
- Task history must include completed and cancelled tasks.
- Task history must exclude open and in-progress tasks.
- Payment history is currently backed by recorded expenses until FR-06 provides real persisted payment or settlement records.

---

# 1. Critical Checks — API Contract

## Endpoint Existence and Method

- [ ] `GET /api/v1/history` exists.
- [ ] The endpoint uses HTTP `GET`, not `POST`, `PUT`, or `PATCH`.
- [ ] The endpoint is located consistently with the project’s endpoint package structure.
- [ ] The endpoint does not require a request body.
- [ ] The endpoint does not require a query parameter such as `householdId`.
- [ ] The endpoint does not expose household selection through path variables.
- [ ] The endpoint returns HTTP `200 OK` for a valid authenticated user with an active household membership.
- [ ] The endpoint returns a JSON object with exactly the top-level structure expected by FR-13.

## Expected Response Shape

The response must have this shape:

```json
{
  "tasks": [],
  "payments": []
}
```

- [ ] The top-level response object contains `tasks`.
- [ ] The top-level response object contains `payments`.
- [ ] `tasks` is always an array, even when empty.
- [ ] `payments` is always an array, even when empty.
- [ ] The backend does not return `null` instead of an empty array.
- [ ] The backend does not wrap the result in an unexpected additional object such as `data`, `content`, or `result`, unless this is already the established project-wide API convention.

## TaskHistoryDto Contract

Each task entry must expose:

- [ ] `id`
- [ ] `householdId`
- [ ] `cleaningScheduleId`
- [ ] `title`
- [ ] `description`
- [ ] `dueDate`
- [ ] `status`
- [ ] `createdByEmail`
- [ ] `assignedToEmail`
- [ ] `createdAt`
- [ ] `updatedAt`

Check each field:

- [ ] `id` maps to the task ID.
- [ ] `householdId` maps to the task’s household ID.
- [ ] `cleaningScheduleId` is present for generated schedule tasks when available.
- [ ] `cleaningScheduleId` is `null` for manually created tasks without a cleaning schedule.
- [ ] `title` maps to the task title.
- [ ] `description` maps to the task description and may be `null` or empty if no description exists.
- [ ] `dueDate` serializes in the project’s expected date/time format.
- [ ] `status` serializes as a readable enum/string value such as `DONE` or `CANCELLED`.
- [ ] `createdByEmail` maps to the email of the user who created the task.
- [ ] `assignedToEmail` maps to the email of the assigned user, or `null` if assignment is optional.
- [ ] `createdAt` maps to the task creation timestamp.
- [ ] `updatedAt` maps to the task update/completion/cancellation timestamp where available.

## PaymentHistoryDto Contract

Each payment/expense entry must expose:

- [ ] `id`
- [ ] `title`
- [ ] `amount`
- [ ] `status`
- [ ] `payerEmail`
- [ ] `receiverEmail`
- [ ] `createdAt`
- [ ] `updatedAt`

Check each field:

- [ ] `id` maps to the expense ID for currently recorded expenses.
- [ ] `title` maps to `expense.description`.
- [ ] `amount` maps to `expense.totalAmount`.
- [ ] `status` is `"RECORDED"` for expense-backed entries.
- [ ] `payerEmail` maps to `expense.paidBy.user.email`.
- [ ] `receiverEmail` is `null` unless a real receiver/payment target exists.
- [ ] `createdAt` maps to `expense.createdAt`.
- [ ] `updatedAt` is `null` unless the expense/payment entity provides an update timestamp.
- [ ] The DTO name and structure are generic enough that FR-06 can later map real payment/settlement records into the same response shape.

---

# 2. Critical Checks — Authentication

- [ ] `GET /api/v1/history` is protected by Spring Security.
- [ ] An unauthenticated request receives `401 Unauthorized` or the project-standard unauthenticated error response.
- [ ] The endpoint does not return history data to unauthenticated users.
- [ ] The endpoint uses the current authenticated principal instead of trusting client-provided user data.
- [ ] The implementation uses `Authentication.getName()` or the project-standard authenticated email mechanism.
- [ ] A forged email, user ID, household ID, or roommate ID in the request body/query/path has no effect because the endpoint does not accept such input.
- [ ] Authentication failure does not produce a stack trace in the response body.
- [ ] Authentication failure is covered by an integration test.

---

# 3. Critical Checks — Authorization and Household Isolation

## Active Roommate Resolution

- [ ] The backend resolves the authenticated user by email.
- [ ] The backend resolves the user’s active `Roommate` membership.
- [ ] The backend derives the household from the active `Roommate` membership.
- [ ] If the user has no active roommate membership, the endpoint returns a project-appropriate error, such as `404 Not Found`, `403 Forbidden`, or a domain exception mapped consistently.
- [ ] If the user exists but is not part of a household, no unrelated household history is returned.
- [ ] If the user has old/inactive memberships, those memberships are not used to fetch history.
- [ ] If the project supports exactly one active household per user, the implementation enforces or assumes this consistently with existing household logic.

## Household Data Isolation

- [ ] The query for tasks filters by the authenticated user’s household.
- [ ] The query for expenses filters by the authenticated user’s household.
- [ ] A user from household A cannot see tasks from household B.
- [ ] A user from household A cannot see expenses from household B.
- [ ] Cross-household tasks with similar titles, dates, users, or statuses are not accidentally included.
- [ ] Cross-household expenses with similar descriptions, amounts, or payer emails are not accidentally included.
- [ ] The endpoint does not use “all tasks” or “all expenses” filtering in memory unless household filtering is still guaranteed and tested.
- [ ] Repository methods use household-scoped conditions rather than broad unfiltered queries where possible.
- [ ] Household isolation is verified with an integration test containing at least two households.
- [ ] The integration test proves that each user only receives their own household’s history.
- [ ] Response fields do not leak sensitive data from other households through nested objects.

---

# 4. Critical Checks — Task History Correctness

## Included Task Statuses

- [ ] Tasks with `TaskStatus.DONE` are included.
- [ ] Tasks with `TaskStatus.CANCELLED` are included.
- [ ] Done manually created tasks are included.
- [ ] Cancelled manually created tasks are included.
- [ ] Done tasks generated from cleaning schedules are included.
- [ ] Cancelled tasks generated from cleaning schedules are included.
- [ ] Tasks with no `cleaningScheduleId` are not dropped accidentally.
- [ ] Tasks with a `cleaningScheduleId` are not dropped accidentally.

## Excluded Task Statuses

- [ ] Tasks with `TaskStatus.OPEN` are excluded.
- [ ] Tasks with `TaskStatus.IN_PROGRESS` are excluded.
- [ ] Any other non-history status is excluded unless explicitly accepted by the feature definition.
- [ ] Open tasks from the same household do not appear in the response.
- [ ] In-progress tasks from the same household do not appear in the response.
- [ ] Open or in-progress tasks from another household do not appear either.
- [ ] Exclusion is implemented at repository/query level where possible.
- [ ] Exclusion is covered by an integration test.

## Task Field Mapping

- [ ] Task ID is correct.
- [ ] Household ID is correct.
- [ ] Cleaning schedule ID is correct or `null`.
- [ ] Title is correct.
- [ ] Description is correct.
- [ ] Due date is correct.
- [ ] Status is correct.
- [ ] Creator email is correct.
- [ ] Assignee email is correct.
- [ ] Created timestamp is correct.
- [ ] Updated timestamp is correct.
- [ ] Null optional fields do not crash mapping.
- [ ] Missing assignee, if allowed by the domain model, does not crash mapping.
- [ ] Missing description does not crash mapping.
- [ ] Missing cleaning schedule does not crash mapping.

---

# 5. Critical Checks — Payment / Expense History Correctness

## Expense-Backed Payment Entries

Current FR-13 behavior: recorded expenses are mapped into the `payments` array.

- [ ] Existing FR-05 expense rows are included as payment-related history.
- [ ] Only expenses from the current household are included.
- [ ] Expenses from another household are excluded.
- [ ] Expense ID maps to `PaymentHistoryDto.id`.
- [ ] Expense description maps to `PaymentHistoryDto.title`.
- [ ] Expense total amount maps to `PaymentHistoryDto.amount`.
- [ ] Expense paid-by user email maps to `PaymentHistoryDto.payerEmail`.
- [ ] `PaymentHistoryDto.status` is `"RECORDED"` for expense-backed entries.
- [ ] `PaymentHistoryDto.receiverEmail` is `null` unless the backend has real receiver data.
- [ ] `PaymentHistoryDto.createdAt` maps to `expense.createdAt`.
- [ ] `PaymentHistoryDto.updatedAt` is `null` unless update information exists.
- [ ] The backend does not invent fake receiver information.
- [ ] The backend does not invent fake payment statuses.
- [ ] The backend does not require FR-06 payment entities to exist before FR-13 works.

## Future FR-06 Compatibility

- [ ] DTO and service naming do not prevent adding real payment/settlement records later.
- [ ] The `payments` array can later contain settlement/payment records without changing the frontend contract.
- [ ] The current implementation contains clear comments or documentation explaining that payments are currently backed by recorded expenses.
- [ ] No hard-coded logic makes it impossible to distinguish recorded expenses from real payments in a later FR-06 refinement.
- [ ] Tests describe the current limitation clearly.

---

# 6. Important Checks — Sorting

## Task Sorting

- [ ] Task history is sorted newest/history-relevant first.
- [ ] Preferred sorting uses `updatedAt DESC` when available.
- [ ] If `updatedAt` is missing, fallback sorting uses `dueDate DESC` or `createdAt DESC` consistently.
- [ ] Sorting is deterministic when two tasks have the same primary timestamp.
- [ ] Sorting behavior is covered by a unit or integration test.
- [ ] Sorting is not left to database default row order.

## Payment / Expense Sorting

- [ ] Payment/expense history is sorted newest first.
- [ ] Preferred sorting uses `createdAt DESC` for expense-backed entries.
- [ ] If `updatedAt` exists later, the implementation can be adapted without breaking the response contract.
- [ ] Sorting is deterministic when two expenses have the same timestamp.
- [ ] Sorting behavior is covered by a unit or integration test.
- [ ] Sorting is not left to database default row order.

---

# 7. Important Checks — Repository and Query Design

## Task Repository

- [ ] There is a repository method or query that fetches history tasks by household and allowed statuses.
- [ ] The task query filters by household ID.
- [ ] The task query filters by status in `DONE`, `CANCELLED`.
- [ ] The task query applies deterministic sorting.
- [ ] The query does not fetch all tasks across all households.
- [ ] The query avoids unnecessary N+1 lookups where possible.
- [ ] If creator/assignee emails are needed, the query or mapping loads required relations safely.
- [ ] The repository method name or query is readable and maintainable.

## Expense Repository

- [ ] There is a repository method or query that fetches expenses by household.
- [ ] The expense query filters by household ID.
- [ ] The expense query applies deterministic sorting.
- [ ] The query does not fetch all expenses across all households.
- [ ] The query avoids unnecessary N+1 lookups where possible.
- [ ] If payer email is needed, the query or mapping loads required relations safely.
- [ ] The repository method name or query is readable and maintainable.

---

# 8. Important Checks — DTO and Serialization

- [ ] DTOs are placed in the project’s established DTO package.
- [ ] DTO field names match the frontend contract exactly.
- [ ] Date/time fields serialize consistently with existing backend DTOs.
- [ ] Amount values serialize with appropriate numeric precision.
- [ ] No entity objects are serialized directly.
- [ ] No passwords, tokens, internal security fields, or unrelated user data are serialized.
- [ ] Email fields only expose emails needed by the history view.
- [ ] `null` values are handled consistently with project-wide JSON configuration.
- [ ] The response is stable enough for frontend DTO typing.
- [ ] DTO names are clear: `HistoryDto`, `TaskHistoryDto`, `PaymentHistoryDto`.

---

# 9. Important Checks — Error Handling

- [ ] Missing authenticated user is handled consistently.
- [ ] Unknown authenticated email is handled consistently.
- [ ] User without active roommate membership is handled consistently.
- [ ] User without household membership receives a safe error response.
- [ ] Database errors are not exposed as raw stack traces.
- [ ] Mapping errors do not expose internal implementation details.
- [ ] The endpoint returns a project-standard error format.
- [ ] Error cases are covered by tests where practical.
- [ ] Logs contain enough information for debugging without leaking private household data.

---

# 10. Important Checks — OpenAPI / Swagger / API Documentation

If the project uses OpenAPI or Swagger:

- [ ] `GET /api/v1/history` is documented.
- [ ] Authentication requirement is documented.
- [ ] Response DTO structure is documented.
- [ ] Task history fields are documented.
- [ ] Payment history fields are documented.
- [ ] Error responses are documented.
- [ ] Documentation says household is inferred from the authenticated user.
- [ ] Documentation does not suggest that the frontend should send `householdId`.
- [ ] Documentation notes that payment entries are currently backed by recorded expenses until FR-06.
- [ ] Swagger examples do not contain real user emails or private data.

If the project does not use OpenAPI/Swagger:

- [ ] This section is marked `N/A` with a reason.

---

# 11. Important Checks — Unit Tests

## Service Unit Tests

- [ ] Service test covers successful history retrieval.
- [ ] Service test verifies active roommate membership resolution.
- [ ] Service test verifies household ID is derived from the authenticated user.
- [ ] Service test verifies only `DONE` and `CANCELLED` tasks are included.
- [ ] Service test verifies `OPEN` and `IN_PROGRESS` tasks are excluded.
- [ ] Service test verifies manual tasks are included.
- [ ] Service test verifies cleaning-schedule-generated tasks are included.
- [ ] Service test verifies task DTO mapping.
- [ ] Service test verifies expense-to-payment DTO mapping.
- [ ] Service test verifies empty tasks array.
- [ ] Service test verifies empty payments array.
- [ ] Service test verifies sorting.
- [ ] Service test verifies null-safe mapping of optional fields.
- [ ] Service test verifies user-without-household behavior.
- [ ] Service test verifies repository calls are household-scoped.

## Mapper Unit Tests

If separate mapper classes are used:

- [ ] Task mapper maps all fields correctly.
- [ ] Payment/expense mapper maps all fields correctly.
- [ ] Mapper handles `null` description.
- [ ] Mapper handles `null` cleaning schedule.
- [ ] Mapper handles `null` receiver email.
- [ ] Mapper handles `null` updated timestamp.
- [ ] Mapper does not accidentally expose nested entity objects.

---

# 12. Critical Checks — Integration Tests

Integration tests should use `MockMvc` or the project’s standard backend integration test setup.

## Successful Request

- [ ] Authenticated user with active household receives `200 OK`.
- [ ] Response contains `tasks`.
- [ ] Response contains `payments`.
- [ ] Response contains completed tasks.
- [ ] Response contains cancelled tasks.
- [ ] Response contains recorded expenses mapped into payments.
- [ ] Response excludes open tasks.
- [ ] Response excludes in-progress tasks.

## Authentication

- [ ] Unauthenticated request to `GET /api/v1/history` fails.
- [ ] Invalid token request fails.
- [ ] Expired or malformed token request fails if this is supported by the test setup.

## Household Isolation

Create at least two households:

- Household A with user A.
- Household B with user B.
- Household A has completed/cancelled tasks and expenses.
- Household B has completed/cancelled tasks and expenses.

Then verify:

- [ ] User A sees only household A tasks.
- [ ] User A sees only household A payments/expenses.
- [ ] User B sees only household B tasks.
- [ ] User B sees only household B payments/expenses.
- [ ] User A does not see household B IDs, titles, descriptions, amounts, emails, or timestamps.
- [ ] User B does not see household A IDs, titles, descriptions, amounts, emails, or timestamps.

## Empty State Backend Behavior

- [ ] User with household but no done/cancelled tasks receives `tasks: []`.
- [ ] User with household but no expenses receives `payments: []`.
- [ ] User with neither task history nor expenses receives both arrays as empty.
- [ ] Empty response still returns `200 OK`.

---

# 13. Regression Checks for Existing Backend Features

## FR-03 Task Creation and Assignment

- [ ] Creating a task still works.
- [ ] Assigning a task still works.
- [ ] Existing task list endpoint still works.
- [ ] Existing task DTOs are not broken by history DTOs.
- [ ] Existing task status transitions still work.
- [ ] Existing task tests pass.

## FR-04 Cleaning Schedule Management

- [ ] Existing cleaning schedule endpoints still work.
- [ ] Generated schedule tasks still work.
- [ ] Cleaning schedule IDs are correctly available for generated task history.
- [ ] Existing cleaning schedule tests pass.

## FR-05 Expense Tracking

- [ ] Creating an expense still works.
- [ ] Listing expenses still works.
- [ ] Expense paid-by relation still works.
- [ ] Expense amount precision is unchanged.
- [ ] Existing expense tests pass.

## FR-06 Payment Status Overview Compatibility

- [ ] FR-13 does not require FR-06 to be fully implemented.
- [ ] FR-13 does not block later FR-06 payment/settlement entities.
- [ ] Current limitation is documented: payments are currently backed by recorded expenses.

---

# 14. Non-Functional Checks

## Security

- [ ] Endpoint is authenticated.
- [ ] Endpoint is household-scoped.
- [ ] No household ID is accepted from the client.
- [ ] No cross-household data can be returned.
- [ ] No sensitive user data is exposed beyond necessary emails.
- [ ] No raw entity serialization leaks internal fields.
- [ ] Error messages do not leak internal database details.
- [ ] Tests prove household isolation.

## Privacy

- [ ] Users only see history belonging to their own household.
- [ ] Emails shown are only those relevant to task/payment history.
- [ ] No unrelated roommates or external users are exposed.
- [ ] Logs do not contain full response payloads with private household history unless this is explicitly acceptable in the project.

## Auditability

- [ ] History entries include timestamps.
- [ ] Task entries include creator and assignee emails.
- [ ] Payment/expense entries include payer email.
- [ ] Status values are explicit and human-readable.
- [ ] The backend preserves enough information to explain why an entry appears in history.

## Performance

- [ ] Queries are household-scoped.
- [ ] Queries are sorted at database level where practical.
- [ ] Endpoint does not fetch all households’ tasks or expenses into memory.
- [ ] Endpoint does not perform excessive per-row database calls.
- [ ] Large household histories are handled reasonably.
- [ ] If pagination is not implemented, this is noted as an accepted limitation or future improvement.

## Maintainability

- [ ] Code follows project package structure.
- [ ] Endpoint, service, repository, and DTO responsibilities are separated.
- [ ] Mapping logic is understandable and testable.
- [ ] Current expense-backed payment limitation is explained in code or docs.
- [ ] Naming is clear and consistent with existing project style.
- [ ] No unrelated refactoring is mixed into the feature.

## Testability

- [ ] Service logic can be unit tested.
- [ ] Repository queries can be integration tested.
- [ ] Endpoint behavior can be tested with authenticated requests.
- [ ] Test data covers multiple households.
- [ ] Test data covers all task statuses.
- [ ] Test data covers empty history cases.
- [ ] Test data covers expenses with and without optional fields.

---

# 15. Build and Quality Checks

- [ ] Backend compiles successfully.
- [ ] Backend tests pass.
- [ ] New tests are included in the correct test packages.
- [ ] No skipped or disabled tests are added without explanation.
- [ ] No unrelated tests are removed.
- [ ] No unrelated production files are modified.
- [ ] Static analysis/linting passes if configured.
- [ ] CI pipeline passes.
- [ ] Application starts successfully with the feature included.
- [ ] Manual API call confirms the endpoint works with a real authenticated user.

---

# 16. Backend Merge Blockers

Do not approve the backend if any of these are true:

- [ ] `GET /api/v1/history` is unauthenticated.
- [ ] The frontend/backend contract requires the frontend to send `householdId`.
- [ ] The endpoint can return another household’s tasks.
- [ ] The endpoint can return another household’s expenses.
- [ ] Open or in-progress tasks appear in task history.
- [ ] Done or cancelled tasks are missing without a valid reason.
- [ ] Expense-backed payment history is missing despite FR-05 expenses existing.
- [ ] DTOs expose raw entities or sensitive fields.
- [ ] Empty history crashes the endpoint.
- [ ] Tests do not cover household isolation.
- [ ] Existing task or expense functionality breaks.

---

# 17. Backend Final Approval

- [ ] All critical checks passed.
- [ ] Important checks passed or documented with accepted reasons.
- [ ] Unit tests added or updated.
- [ ] Integration tests added or updated.
- [ ] Regression tests pass.
- [ ] Security and privacy checks passed.
- [ ] Known limitation around payment history is documented.
- [ ] Reviewer has manually inspected the endpoint behavior.
- [ ] Backend is ready to merge.
