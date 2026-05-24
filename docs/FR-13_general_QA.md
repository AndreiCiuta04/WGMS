# FR-13 General QA Checklist — History of Tasks and Payments

## Feature Information

- Feature ID: FR-13
- Feature name: History of tasks and payments
- Use case: UC-06 — Central Dashboard and Notifications Management
- Priority: High
- Category: Core
- Version: V3
- Lead: Andrei Ciuta
- Backend endpoint: `GET /api/v1/history`
- Frontend route: `/history`
- Branch:
- Merge request:
- Developer:
- QA reviewer:
- Date:

---

## How to Use This Checklist

This checklist verifies FR-13 at feature level, not only backend or frontend code in isolation.

Use this checklist after the backend and frontend checklists have been started or completed.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a short note under the relevant section.
- If something does not apply, write `N/A` with a short reason.
- Do not merge while any critical item is unchecked.
- Every acceptance claim should have evidence: code reference, test result, screenshot, API response, database check, or manual QA note.

This checklist is written so that a human reviewer or AI assistant can determine whether FR-13 is merge-ready.

---

## Feature Summary

FR-13 implements a household history view.

It must show:

- completed task history
- cancelled task history
- payment/expense history

The current payment/expense behavior is intentionally limited:

- FR-05 expense tracking exists.
- FR-06 payment status overview may later provide real payment or settlement records.
- Until then, recorded expenses are mapped into the `payments` array as payment-related history.
- This limitation must be documented clearly and must not be hidden from reviewers.

---

# 1. Critical Checks — Feature Scope Confirmation

- [ ] FR-13 provides a household history feature.
- [ ] The backend exposes `GET /api/v1/history`.
- [ ] The frontend exposes `/history`.
- [ ] The frontend has a visible History navigation link.
- [ ] The backend returns task history and payment/expense history in one response.
- [ ] The feature shows completed tasks.
- [ ] The feature shows cancelled tasks.
- [ ] The feature excludes open tasks.
- [ ] The feature excludes in-progress tasks.
- [ ] The feature shows recorded expenses as payment-related history.
- [ ] The feature does not require the frontend to send `householdId`.
- [ ] The feature filters all data by the authenticated user’s own household.
- [ ] The feature does not leak data from another household.
- [ ] The feature uses the current app UI style.
- [ ] The feature does not introduce unrelated functionality.
- [ ] The feature does not remove or break existing task/expense features.

---

# 2. Critical Checks — Dependency Status

## Required Dependencies

- [ ] FR-03 Task Creation and Assignment is available or sufficiently implemented for task history.
- [ ] FR-04 Cleaning Schedule Management is available or sufficiently implemented for generated cleaning schedule task history.
- [ ] FR-05 Expense Tracking is available or sufficiently implemented for expense-backed payment history.
- [ ] FR-13 does not require FR-06 to be complete.
- [ ] FR-13 is compatible with future FR-06 payment/settlement refinement.
- [ ] Any missing dependency is documented with impact and workaround.

## FR-06 Known Limitation

- [ ] Reviewers understand that `payments` currently means payment-related history backed by recorded expenses.
- [ ] The limitation is written in the MR description.
- [ ] The limitation is written in QA notes or documentation.
- [ ] The limitation is not presented as a real settlement/payment implementation.
- [ ] The DTO shape is generic enough for later real payment/settlement records.
- [ ] The frontend wording does not falsely imply that recorded expenses are completed payments.

Suggested wording:

> Payment history is currently backed by recorded FR-05 expenses. Real payment/settlement records can be mapped into the same `PaymentHistoryDto` once FR-06 provides persisted payment status data.

---

# 3. Critical Checks — End-to-End Acceptance Criteria

Use a test environment with at least two households.

## Household A Data

Prepare:

- one user in household A
- one `DONE` task in household A
- one `CANCELLED` task in household A
- one `OPEN` task in household A
- one `IN_PROGRESS` task in household A
- one recorded expense in household A

Verify:

- [ ] User A can open `/history`.
- [ ] User A sees the `DONE` task.
- [ ] User A sees the `CANCELLED` task.
- [ ] User A does not see the `OPEN` task.
- [ ] User A does not see the `IN_PROGRESS` task.
- [ ] User A sees the recorded expense in the payment/expense history section.
- [ ] Task section shows required fields.
- [ ] Payment/expense section shows required fields.
- [ ] No `undefined`, `NaN`, or `[object Object]` values appear.

## Household B Data

Prepare:

- one user in household B
- one `DONE` task in household B
- one `CANCELLED` task in household B
- one recorded expense in household B

Verify:

- [ ] User B can open `/history`.
- [ ] User B sees only household B task history.
- [ ] User B sees only household B payment/expense history.
- [ ] User B does not see household A task titles.
- [ ] User B does not see household A expense titles/descriptions.
- [ ] User B does not see household A emails.
- [ ] User B does not see household A IDs in rendered UI.

## Empty History

Prepare a household with no completed/cancelled tasks and no expenses.

Verify:

- [ ] User can open `/history`.
- [ ] Task empty state is visible.
- [ ] Payment/expense empty state is visible.
- [ ] Page does not crash.
- [ ] API returns arrays instead of `null`.

---

# 4. Critical Checks — Security and Privacy

## Authentication

- [ ] Backend endpoint is authenticated.
- [ ] Frontend route follows authenticated app route behavior.
- [ ] Unauthenticated users cannot fetch history data.
- [ ] Unauthenticated users cannot view the history page with real data.
- [ ] Invalid/expired auth fails safely.

## Authorization

- [ ] Backend derives household from authenticated user.
- [ ] Backend resolves active roommate membership.
- [ ] Backend does not trust frontend-provided household information.
- [ ] Frontend does not send household ID.
- [ ] Users cannot alter the request to view another household’s history.
- [ ] Household isolation is verified by backend integration tests.
- [ ] Household isolation is verified manually or through e2e tests.

## Privacy

- [ ] Only data relevant to the user’s own household is returned.
- [ ] No unrelated users are exposed.
- [ ] No raw entity objects are serialized.
- [ ] No passwords, tokens, roles, or security internals are exposed.
- [ ] Error responses do not leak stack traces.
- [ ] Logs do not leak excessive private household history.
- [ ] Screenshots/test evidence do not contain real private production data.

---

# 5. Important Checks — Data Consistency

## Task Data

- [ ] Backend and frontend agree on task field names.
- [ ] Task statuses are consistent with existing task status enum.
- [ ] Done/cancelled task status transitions remain valid.
- [ ] Generated cleaning schedule tasks retain their cleaning schedule reference when available.
- [ ] Manual tasks are still included without cleaning schedule reference.
- [ ] Task timestamps are meaningful for history sorting.
- [ ] Task sorting is newest/history-relevant first.
- [ ] Task history does not duplicate entries.

## Payment / Expense Data

- [ ] Backend and frontend agree on payment field names.
- [ ] Recorded expenses are mapped consistently.
- [ ] Expense amount precision is preserved.
- [ ] Payer email is correct.
- [ ] Receiver email is null unless real receiver data exists.
- [ ] Payment/expense timestamps are meaningful for history sorting.
- [ ] Payment/expense sorting is newest first.
- [ ] Payment/expense history does not duplicate entries.
- [ ] Current recorded-expense limitation is documented.

---

# 6. Important Checks — Branch and Merge Request

## Branch Hygiene

- [ ] Branch name follows team convention.
- [ ] Branch is based on the correct target branch.
- [ ] Branch contains only FR-13-related changes or unrelated changes are explicitly justified.
- [ ] No local debug files are committed.
- [ ] No generated build artifacts are committed.
- [ ] No credentials, tokens, or secrets are committed.
- [ ] No temporary console logs or debug prints remain unless accepted.
- [ ] No commented-out code blocks remain without a reason.
- [ ] No unresolved merge conflict markers remain.

## Merge Request Description

- [ ] MR title identifies FR-13.
- [ ] MR description explains backend changes.
- [ ] MR description explains frontend changes.
- [ ] MR description mentions `GET /api/v1/history`.
- [ ] MR description mentions `/history`.
- [ ] MR description explains household isolation.
- [ ] MR description states that frontend does not send household ID.
- [ ] MR description documents the payment limitation: payments currently backed by recorded expenses until FR-06.
- [ ] MR description lists tests run.
- [ ] MR description lists known limitations.
- [ ] MR links to relevant issue/user story if available.

## Review Assignment

- [ ] Backend reviewer assigned.
- [ ] Frontend reviewer assigned.
- [ ] QA reviewer assigned.
- [ ] Reviewers have access to test credentials/data if needed.
- [ ] Reviewer comments are resolved or explicitly deferred.

---

# 7. Important Checks — Documentation

- [ ] Backend API behavior is documented.
- [ ] Frontend route/page behavior is documented if the project has frontend docs.
- [ ] Known limitation around payment history is documented.
- [ ] Test data setup is documented.
- [ ] Manual QA steps are documented.
- [ ] Any new environment requirement is documented.
- [ ] No documentation says the frontend should send `householdId`.
- [ ] No documentation claims real FR-06 payment settlement behavior if it is not implemented.
- [ ] OpenAPI/Swagger is updated if the project uses it.
- [ ] README or feature notes are updated if required by team process.

---

# 8. Important Checks — Test Evidence

Attach or write evidence for each relevant category.

## Backend Evidence

- [ ] Backend unit test results attached or pasted.
- [ ] Backend integration test results attached or pasted.
- [ ] Manual `GET /api/v1/history` response attached or summarized.
- [ ] Household isolation backend test evidence attached or summarized.
- [ ] Regression test results for tasks attached or summarized.
- [ ] Regression test results for expenses attached or summarized.

## Frontend Evidence

- [ ] Screenshot of History page with data attached.
- [ ] Screenshot of task empty state attached if applicable.
- [ ] Screenshot of payment/expense empty state attached if applicable.
- [ ] Screenshot of error state attached if applicable.
- [ ] Browser network proof attached or summarized showing no `householdId` is sent.
- [ ] E2E test result attached or pasted.
- [ ] Component test result attached or pasted if applicable.
- [ ] Screenshot or note confirms sidebar History link.

## Cross-Household Evidence

- [ ] Evidence shows user A data.
- [ ] Evidence shows user B data.
- [ ] Evidence confirms user A cannot see user B data.
- [ ] Evidence confirms user B cannot see user A data.
- [ ] Evidence does not expose real private production data.

---

# 9. Important Checks — Regression Readiness

## Backend Regression

- [ ] Existing task endpoints still work.
- [ ] Existing task status changes still work.
- [ ] Existing cleaning schedule endpoints still work.
- [ ] Existing expense endpoints still work.
- [ ] Existing authentication still works.
- [ ] Existing household/roommate membership logic still works.
- [ ] Existing backend test suite passes.

## Frontend Regression

- [ ] Existing dashboard still loads.
- [ ] Existing task page still loads.
- [ ] Existing cleaning schedule page still loads.
- [ ] Existing expense page still loads.
- [ ] Existing sidebar/navigation still works.
- [ ] Existing login/logout flow still works.
- [ ] Existing frontend test suite passes.

## CI Regression

- [ ] Backend pipeline passes.
- [ ] Frontend pipeline passes.
- [ ] E2E pipeline passes if configured.
- [ ] No new flaky tests are introduced.
- [ ] No test is disabled to make the pipeline pass without explanation.

---

# 10. Important Checks — Non-Functional Requirements

## Security

- [ ] Authenticated endpoint.
- [ ] Household-scoped data.
- [ ] No frontend household selector.
- [ ] No cross-household leakage.
- [ ] Safe error handling.
- [ ] No secrets in code.

## Privacy

- [ ] Only household-relevant emails appear.
- [ ] No private data from other households appears.
- [ ] Test evidence avoids real private production data.
- [ ] Logs are not excessive.

## Usability

- [ ] History page is easy to find.
- [ ] Page clearly separates tasks and payment/expense history.
- [ ] Empty states are understandable.
- [ ] Error state is understandable.
- [ ] Current expense-backed payment behavior is not misleading.

## Performance

- [ ] Endpoint uses household-scoped queries.
- [ ] Endpoint does not fetch all history globally.
- [ ] Frontend does not repeatedly poll unnecessarily.
- [ ] Page remains usable with a reasonable number of history entries.
- [ ] Missing pagination is documented if relevant.

## Maintainability

- [ ] Code follows existing structure.
- [ ] DTOs are clearly named.
- [ ] Service/component responsibilities are separated.
- [ ] Current limitation is documented.
- [ ] No unrelated refactoring is mixed into FR-13.

## Testability

- [ ] Backend can be tested independently.
- [ ] Frontend can be tested independently.
- [ ] E2E flow can be tested with seeded data.
- [ ] Test data covers multiple households.
- [ ] Test data covers empty histories.
- [ ] Test data covers all relevant task statuses.

---

# 11. Reviewer Checklist

## Backend Reviewer

- [ ] Read endpoint implementation.
- [ ] Read service implementation.
- [ ] Read repository queries.
- [ ] Read DTOs/mappers.
- [ ] Verified authentication.
- [ ] Verified household isolation.
- [ ] Verified task filtering.
- [ ] Verified expense mapping.
- [ ] Verified sorting.
- [ ] Verified tests.

## Frontend Reviewer

- [ ] Read route configuration.
- [ ] Read navigation/sidebar change.
- [ ] Read history service.
- [ ] Read history component/template/styles.
- [ ] Verified API call.
- [ ] Verified no household ID is sent.
- [ ] Verified loading/error/empty states.
- [ ] Verified task rendering.
- [ ] Verified payment/expense rendering.
- [ ] Verified styling consistency.
- [ ] Verified tests.

## QA Reviewer

- [ ] Ran or reviewed backend tests.
- [ ] Ran or reviewed frontend tests.
- [ ] Ran or reviewed e2e tests.
- [ ] Checked household isolation.
- [ ] Checked acceptance criteria.
- [ ] Checked known limitations.
- [ ] Checked regression risk.
- [ ] Approved or requested changes.

---

# 12. Merge Blockers

Do not merge FR-13 if any of these are true:

- [ ] Backend endpoint is missing.
- [ ] Frontend page is missing.
- [ ] Endpoint is unauthenticated.
- [ ] Frontend sends `householdId`.
- [ ] Backend trusts client-provided household information.
- [ ] Users can see another household’s tasks.
- [ ] Users can see another household’s expenses.
- [ ] Open or in-progress tasks appear in history.
- [ ] Done or cancelled tasks are missing without explanation.
- [ ] Recorded expenses do not appear despite FR-05 data existing.
- [ ] Empty history crashes backend or frontend.
- [ ] Page displays fake production data.
- [ ] Existing task or expense features are broken.
- [ ] No household isolation test or manual proof exists.
- [ ] Payment limitation is not documented.
- [ ] CI fails.
- [ ] Secrets or credentials are committed.
- [ ] Merge conflict markers remain.

---

# 13. Final Approval Checklist

## Backend

- [ ] Backend QA checklist completed.
- [ ] Backend critical items passed.
- [ ] Backend tests passed.
- [ ] Backend reviewer approved.

## Frontend

- [ ] Frontend QA checklist completed.
- [ ] Frontend critical items passed.
- [ ] Frontend tests passed or justified.
- [ ] Frontend reviewer approved.

## General

- [ ] Feature scope confirmed.
- [ ] End-to-end acceptance criteria passed.
- [ ] Household isolation verified.
- [ ] Payment limitation documented.
- [ ] Regression checks passed.
- [ ] Test evidence attached or summarized.
- [ ] MR description complete.
- [ ] QA reviewer approved.
- [ ] No merge blockers remain.
- [ ] FR-13 is ready to merge.
