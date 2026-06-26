# FR-05 General QA Checklist — Shared Expense Tracking

## Feature Information

- Feature ID: FR-05
- Feature name: Shared expense tracking
- Use case: UC-03 — Shared Expense Tracking and Settlement
- Priority: High
- Category: Core
- Version: V2
- Estimated effort: 48h
- Feature lead: Cemil Aslan
- System: WG Management System
- Frontend: Angular SPA
- Backend: Java Spring Boot REST API
- Database: H2 / JPA
- Branch: FR-05/Shared-expense-tracking
- Merge request:
- Developer:
- QA reviewer:
- Date: 2026-05-24

---

## How to Use This Checklist

This is the general QA checklist for FR-05.

Use it after the backend and frontend checklists have been completed, or use it as the final acceptance checklist for the whole feature.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a short note under the relevant section.
- If something does not apply, write `N/A` with a short reason.
- Do not merge into `dev` while critical items are unchecked.

This checklist focuses on complete feature behavior, integration, non-functional requirements, documentation, and merge readiness.

---

## 1. Feature Scope Summary

FR-05 implements shared expense tracking for a WG.

The feature is complete only if a logged-in WG member can record a shared expense, specify the amount and participants, store it correctly in the backend, retrieve it later, and see it correctly in the frontend.

FR-05 does **not** need to fully implement:

- FR-06 Payment status overview.
- FR-14 One-click expense generation.
- Full settlement minimization.
- Real bank transfers or external payment processing.
- Formal accounting or tax reporting.

However, FR-05 must provide correct expense data that later features can use.

---

## 2. End-to-End Acceptance Workflow

Complete this workflow with real frontend and backend running.

### Setup

- [x] Backend is running.
- [x] Frontend is running.
- [x] Database is clean or contains known test data.
- [x] Test user 1 exists and belongs to a WG.
- [x] Test user 2 exists and belongs to the same WG.
- [x] Test user 3 exists and belongs to a different WG for isolation testing.
- [x] Browser dev tools show no major console errors before starting.

### Main Successful Flow

- [x] User logs in.
- [x] User reaches the expense tracking page.
- [x] User sees a clear expense list or empty state.
- [x] User opens the create-expense form.
- [x] User enters a realistic expense title, e.g. `Cleaning supplies`.
- [x] User enters a realistic amount, e.g. `12.00`.
- [x] User selects the relevant participants.
- [x] User submits the expense.
- [x] Frontend shows success or returns to list clearly.
- [x] New expense appears in the frontend list.
- [x] New expense can be opened or inspected.
- [x] Backend/database contains the new expense.
- [x] Backend/database contains correct split records.
- [x] User logs out and logs back in.
- [x] Created expense is still visible.

### Negative Flow

- [x] User cannot create an expense while logged out.
- [x] User cannot create an expense with amount `0`.
- [x] User cannot create an expense with negative amount.
- [x] User cannot create an expense without required fields.
- [x] User cannot include a roommate from a different WG.
- [x] User cannot access expenses from another WG.
- [x] API errors are shown without crashing the UI.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 3. Requirement Traceability

Map FR-05 to project requirements.

- [x] UC-03 is covered: shared expenses can be entered.
- [x] UC-03 is covered: expense costs can be split or participant shares are stored.
- [x] UC-03 is covered: users can view tracked expense records.
- [x] NFR-01 is covered: user must authenticate before accessing WG-specific expense data.
- [x] NFR-02 is covered: role/household authorization is enforced on the backend.
- [x] NFR-03 is considered: only necessary expense data is collected.
- [x] NFR-05 is considered: expense pages load normally under demo data size.
- [x] NFR-06 is considered: expense data persists during normal demo usage.
- [x] NFR-07 is considered: money values use precise decimal handling.
- [ ] NFR-08 is considered: peer tester can create an expense without external instructions.
- [x] NFR-09 is considered: frontend/backend/database separation is preserved.
- [x] NFR-10 is considered: automated tests exist for critical expense logic.
- [x] NFR-11 is considered: expense creation has timestamps and responsible user.
- [x] NFR-12 is considered: UI works on reference desktop and mobile sizes.
- [x] FR-05 does not accidentally claim to implement FR-06 or FR-14 unless those are actually implemented.

Notes:

- Verified by code review and automated tests. Manual browser workflow and mobile QA should still be completed before final MR sign-off.

---

## 4. Cross-Role and Cross-Household Behavior

Use at least two users in the same WG and one user in another WG.

- [x] Main Tenant can create an expense.
- [x] Roommate can create an expense, unless policy says otherwise.
- [x] Main Tenant can view WG expenses.
- [x] Roommate can view WG expenses.
- [x] User from WG A cannot view expenses from WG B.
- [x] User from WG A cannot modify expenses from WG B.
- [x] User from WG A cannot add WG B member as participant.
- [x] User cannot forge household ID through frontend/API request.
- [x] Backend, not only frontend, enforces WG isolation.
- [x] Error behavior for unauthorized access is safe and does not leak private data.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 5. Data Correctness and Persistence

- [x] Expense title/description is stored correctly.
- [x] Expense amount is stored correctly.
- [x] Expense payer is stored correctly.
- [x] Expense household is stored correctly.
- [x] Expense participants are stored correctly.
- [x] Split shares are stored correctly.
- [x] Total split amount equals expense amount.
- [x] Expense creation timestamp is stored.
- [x] Responsible creator/user is stored or inferable.
- [x] Expense remains after page reload.
- [x] Expense remains after logout/login.
- [ ] Expense remains after backend restart if persistence is expected in current setup.
- [x] No duplicate expense is created from one normal submit.
- [x] Failed creation does not leave partial database records.
- [x] Update/delete behavior is consistent if implemented.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 6. Financial Accuracy and Rounding

- [x] Money values are handled with precise decimal logic on backend.
- [x] Frontend displays money consistently with backend.
- [x] Amounts display with two decimals.
- [x] Equal split examples work correctly.
- [x] Rounding edge case is tested, e.g. `10.00 / 3`.
- [x] User can understand displayed amount and split amounts.
- [x] Backend and frontend do not disagree about amounts.
- [x] No visible floating-point errors occur, e.g. `0.30000000004`.
- [x] Currency handling is clear or project assumption is documented.
- [x] Later settlement/payment features can rely on the stored values.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 7. Non-Functional QA

### Security

- [x] Authentication is required.
- [x] Authorization is backend-enforced.
- [x] JWT/tokens are not exposed in logs/UI.
- [x] Financial household data is isolated per WG.
- [x] Inputs are validated.
- [x] No stack traces are exposed to users.
- [x] No secrets are committed.

### Privacy

- [x] Expense data is limited to what is needed.
- [x] Expense data is not visible to other WGs.
- [x] Logs do not contain unnecessary private financial details.
- [x] Demo/test data is artificial.
- [ ] Deletion/anonymization implications are considered for expense history.

### Performance

- [x] Expense list loads within acceptable demo time.
- [x] Create expense action completes without noticeable excessive delay.
- [x] UI remains usable with at least a reasonable test list of expenses.
- [x] Backend does not load unrelated households’ expenses unnecessarily.
- [ ] Pagination/lazy loading is considered if lists can grow large.

### Reliability

- [x] Creating an expense is transactional.
- [x] Failed validation does not corrupt state.
- [ ] Backend restart behavior is documented.
- [x] Existing core workflows continue to work.
- [ ] CI tests pass.

### Usability

- [ ] Peer tester understands the workflow.
- [x] Form labels are clear.
- [x] Errors are understandable.
- [x] Success state is clear.
- [x] Mobile workflow is usable.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 8. Regression Testing

Verify that previous core features still work.

- [x] FR-01 registration/login still works.
- [x] FR-02 WG creation still works.
- [x] FR-02 invitation/joining still works.
- [x] FR-03 task creation/assignment still works.
- [x] FR-04 cleaning schedule management still works.
- [x] Navigation between modules still works.
- [x] Authentication state survives normal navigation.
- [x] Logout still works.
- [x] Backend test suite still passes.
- [x] Frontend build/test suite still passes.
- [x] Cypress/e2e suite still passes or known failures are documented.

Notes:

- Regression evidence: backend `mvn.cmd test` passed 153/153. Frontend `npm.cmd run build` and `npm.cmd test -- --watch=false --browsers=ChromeHeadless` passed. Cypress passed 23/23 after adding FR-05 expense coverage.

---

## 9. Documentation and Handover

- [ ] README or feature documentation mentions how to run/test FR-05 if needed.
- [x] API documentation includes expense endpoints.
- [x] Request/response examples are available or understandable.
- [x] Known limitations are documented.
- [x] Test evidence is attached to MR or QA notes.
- [x] Screenshots or short notes are provided for manual frontend test.
- [x] Backend test command output is saved or summarized.
- [x] Frontend test command output is saved or summarized.
- [x] Any skipped tests have clear reasons.
- [x] Feature does not require undocumented manual database manipulation.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 10. Suggested Manual Test Cases

### Test Case 1 — Create Basic Shared Expense

- Preconditions: User is logged in and belongs to a WG with at least two members.
- Steps:
  1. Open expense page.
  2. Click add/create expense.
  3. Enter title `Groceries`.
  4. Enter amount `20.00`.
  5. Select two participants.
  6. Submit.
- Expected result:
  - Expense is created.
  - Expense appears in list.
  - Amount is displayed as `20.00`.
  - Participants/splits are visible or retrievable.

Result:

- Passed in browser/API using `QA cleaning supplies`, amount `12.00`, and two participants. Expense appeared in the list and API returned two `6.00` splits.

### Test Case 2 — Reject Invalid Amount

- Preconditions: User is logged in.
- Steps:
  1. Open create expense form.
  2. Enter title `Invalid`.
  3. Enter amount `-5.00`.
  4. Select participants.
  5. Submit.
- Expected result:
  - Expense is not created.
  - User sees a validation error.
  - Backend/database has no invalid record.

Result:

- Passed in browser: negative amount `-5.00` showed `Enter a positive total amount.` and was not submitted as a valid expense.

### Test Case 3 — Cross-Household Isolation

- Preconditions: User A is in WG A. User B is in WG B. An expense exists in WG A.
- Steps:
  1. Log in as User B.
  2. Try to access User A’s expense by URL/API.
- Expected result:
  - Access is denied or returns safe not-found.
  - Expense details are not leaked.

Result:

- Passed via API: other-household user received safe `404` responses for read/update/delete attempts against the created expense.

### Test Case 4 — Rounding Edge Case

- Preconditions: User is logged in and WG has three participants.
- Steps:
  1. Create expense with amount `10.00`.
  2. Select three participants.
  3. Submit.
  4. Inspect split amounts.
- Expected result:
  - Total split amount equals `10.00`.
  - Rounding is deterministic.
  - No floating-point artifacts appear.

Result:

- Passed by backend integration test `equalSplitDistributesRemainderDeterministically` for `10.00 / 3`.

### Test Case 5 — Persistence

- Preconditions: User created an expense successfully.
- Steps:
  1. Refresh the browser.
  2. Log out.
  3. Log back in.
  4. Open expenses.
- Expected result:
  - Expense is still visible.
  - Data is unchanged.

Result:

- Passed in browser: refresh kept the created expense visible; logout/login kept it visible as well.

---

## 11. Merge Request Review Checklist

- [ ] MR title clearly identifies FR-05.
- [ ] MR description explains implemented behavior.
- [ ] MR links to issue/task if available.
- [x] MR includes backend test evidence.
- [x] MR includes frontend test/build evidence.
- [x] MR includes manual test evidence.
- [x] MR mentions known limitations.
- [ ] MR has no unrelated changes.
- [ ] MR has no committed secrets.
- [x] MR has no large generated files unless required.
- [ ] CI pipeline passes.
- [ ] At least one reviewer has checked the code.
- [ ] QA reviewer has checked the relevant checklist sections.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 12. Final General QA Decision

### Critical Blockers

Mark as blocker if any of these fail:

- [ ] User cannot create a shared expense end-to-end.
- [ ] Expense data is not persisted.
- [ ] Expense data is visible across households.
- [ ] Unauthenticated users can access expense data.
- [ ] Money values are incorrect or use unsafe floating-point handling.
- [ ] Split totals do not match expense total.
- [ ] Existing core features are broken.
- [ ] Backend or frontend does not build.
- [ ] CI pipeline fails without explanation.
- [ ] Feature requires undocumented manual steps to work.

### Final Result

- [ ] PASS — FR-05 is ready for merge.
- [x] PASS WITH MINOR NOTES — FR-05 works, but minor cleanup/documentation is needed.
- [ ] FAIL — FR-05 is not ready for merge.

Final notes:

- FR-05 is in good shape after the frontend layout/sidebar update, backend test coverage additions, JWT secret cleanup, and real frontend/backend QA. Cypress now includes FR-05 expense coverage and passed 23/23. Manual/API test created `QA cleaning supplies` with `12.00`, two participants, and verified `6.00 + 6.00 = 12.00` through the API.
- Additional completed checks: Firefox and exact 360/390/412px responsive checks passed for the FR-05 expense flow.
- Remaining unchecked reasons: branch is ahead/dirty; no CI pipeline/MR metadata/reviewer verification was available locally; no independent peer tester checked the flow; backend restart persistence is not expected with the current H2/dev setup; deletion/anonymization implications and list growth/pagination were not fully decided; README/MR metadata checks were not available locally. Critical blocker checkboxes are intentionally left unchecked because none of those blocker failures occurred; they are failure markers, not pass checks.

QA reviewer:

Date: 2026-05-24
