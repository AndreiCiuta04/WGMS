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
- Branch:
- Merge request:
- Developer:
- QA reviewer:
- Date:

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

- [ ] Backend is running.
- [ ] Frontend is running.
- [ ] Database is clean or contains known test data.
- [ ] Test user 1 exists and belongs to a WG.
- [ ] Test user 2 exists and belongs to the same WG.
- [ ] Test user 3 exists and belongs to a different WG for isolation testing.
- [ ] Browser dev tools show no major console errors before starting.

### Main Successful Flow

- [ ] User logs in.
- [ ] User reaches the expense tracking page.
- [ ] User sees a clear expense list or empty state.
- [ ] User opens the create-expense form.
- [ ] User enters a realistic expense title, e.g. `Cleaning supplies`.
- [ ] User enters a realistic amount, e.g. `12.00`.
- [ ] User selects the relevant participants.
- [ ] User submits the expense.
- [ ] Frontend shows success or returns to list clearly.
- [ ] New expense appears in the frontend list.
- [ ] New expense can be opened or inspected.
- [ ] Backend/database contains the new expense.
- [ ] Backend/database contains correct split records.
- [ ] User logs out and logs back in.
- [ ] Created expense is still visible.

### Negative Flow

- [ ] User cannot create an expense while logged out.
- [ ] User cannot create an expense with amount `0`.
- [ ] User cannot create an expense with negative amount.
- [ ] User cannot create an expense without required fields.
- [ ] User cannot include a roommate from a different WG.
- [ ] User cannot access expenses from another WG.
- [ ] API errors are shown without crashing the UI.

Notes:

-

---

## 3. Requirement Traceability

Map FR-05 to project requirements.

- [ ] UC-03 is covered: shared expenses can be entered.
- [ ] UC-03 is covered: expense costs can be split or participant shares are stored.
- [ ] UC-03 is covered: users can view tracked expense records.
- [ ] NFR-01 is covered: user must authenticate before accessing WG-specific expense data.
- [ ] NFR-02 is covered: role/household authorization is enforced on the backend.
- [ ] NFR-03 is considered: only necessary expense data is collected.
- [ ] NFR-05 is considered: expense pages load normally under demo data size.
- [ ] NFR-06 is considered: expense data persists during normal demo usage.
- [ ] NFR-07 is considered: money values use precise decimal handling.
- [ ] NFR-08 is considered: peer tester can create an expense without external instructions.
- [ ] NFR-09 is considered: frontend/backend/database separation is preserved.
- [ ] NFR-10 is considered: automated tests exist for critical expense logic.
- [ ] NFR-11 is considered: expense creation has timestamps and responsible user.
- [ ] NFR-12 is considered: UI works on reference desktop and mobile sizes.
- [ ] FR-05 does not accidentally claim to implement FR-06 or FR-14 unless those are actually implemented.

Notes:

-

---

## 4. Cross-Role and Cross-Household Behavior

Use at least two users in the same WG and one user in another WG.

- [ ] Main Tenant can create an expense.
- [ ] Roommate can create an expense, unless policy says otherwise.
- [ ] Main Tenant can view WG expenses.
- [ ] Roommate can view WG expenses.
- [ ] User from WG A cannot view expenses from WG B.
- [ ] User from WG A cannot modify expenses from WG B.
- [ ] User from WG A cannot add WG B member as participant.
- [ ] User cannot forge household ID through frontend/API request.
- [ ] Backend, not only frontend, enforces WG isolation.
- [ ] Error behavior for unauthorized access is safe and does not leak private data.

Notes:

-

---

## 5. Data Correctness and Persistence

- [ ] Expense title/description is stored correctly.
- [ ] Expense amount is stored correctly.
- [ ] Expense payer is stored correctly.
- [ ] Expense household is stored correctly.
- [ ] Expense participants are stored correctly.
- [ ] Split shares are stored correctly.
- [ ] Total split amount equals expense amount.
- [ ] Expense creation timestamp is stored.
- [ ] Responsible creator/user is stored or inferable.
- [ ] Expense remains after page reload.
- [ ] Expense remains after logout/login.
- [ ] Expense remains after backend restart if persistence is expected in current setup.
- [ ] No duplicate expense is created from one normal submit.
- [ ] Failed creation does not leave partial database records.
- [ ] Update/delete behavior is consistent if implemented.

Notes:

-

---

## 6. Financial Accuracy and Rounding

- [ ] Money values are handled with precise decimal logic on backend.
- [ ] Frontend displays money consistently with backend.
- [ ] Amounts display with two decimals.
- [ ] Equal split examples work correctly.
- [ ] Rounding edge case is tested, e.g. `10.00 / 3`.
- [ ] User can understand displayed amount and split amounts.
- [ ] Backend and frontend do not disagree about amounts.
- [ ] No visible floating-point errors occur, e.g. `0.30000000004`.
- [ ] Currency handling is clear or project assumption is documented.
- [ ] Later settlement/payment features can rely on the stored values.

Notes:

-

---

## 7. Non-Functional QA

### Security

- [ ] Authentication is required.
- [ ] Authorization is backend-enforced.
- [ ] JWT/tokens are not exposed in logs/UI.
- [ ] Financial household data is isolated per WG.
- [ ] Inputs are validated.
- [ ] No stack traces are exposed to users.
- [ ] No secrets are committed.

### Privacy

- [ ] Expense data is limited to what is needed.
- [ ] Expense data is not visible to other WGs.
- [ ] Logs do not contain unnecessary private financial details.
- [ ] Demo/test data is artificial.
- [ ] Deletion/anonymization implications are considered for expense history.

### Performance

- [ ] Expense list loads within acceptable demo time.
- [ ] Create expense action completes without noticeable excessive delay.
- [ ] UI remains usable with at least a reasonable test list of expenses.
- [ ] Backend does not load unrelated households’ expenses unnecessarily.
- [ ] Pagination/lazy loading is considered if lists can grow large.

### Reliability

- [ ] Creating an expense is transactional.
- [ ] Failed validation does not corrupt state.
- [ ] Backend restart behavior is documented.
- [ ] Existing core workflows continue to work.
- [ ] CI tests pass.

### Usability

- [ ] Peer tester understands the workflow.
- [ ] Form labels are clear.
- [ ] Errors are understandable.
- [ ] Success state is clear.
- [ ] Mobile workflow is usable.

Notes:

-

---

## 8. Regression Testing

Verify that previous core features still work.

- [ ] FR-01 registration/login still works.
- [ ] FR-02 WG creation still works.
- [ ] FR-02 invitation/joining still works.
- [ ] FR-03 task creation/assignment still works.
- [ ] FR-04 cleaning schedule management still works.
- [ ] Navigation between modules still works.
- [ ] Authentication state survives normal navigation.
- [ ] Logout still works.
- [ ] Backend test suite still passes.
- [ ] Frontend build/test suite still passes.
- [ ] Cypress/e2e suite still passes or known failures are documented.

Notes:

-

---

## 9. Documentation and Handover

- [ ] README or feature documentation mentions how to run/test FR-05 if needed.
- [ ] API documentation includes expense endpoints.
- [ ] Request/response examples are available or understandable.
- [ ] Known limitations are documented.
- [ ] Test evidence is attached to MR or QA notes.
- [ ] Screenshots or short notes are provided for manual frontend test.
- [ ] Backend test command output is saved or summarized.
- [ ] Frontend test command output is saved or summarized.
- [ ] Any skipped tests have clear reasons.
- [ ] Feature does not require undocumented manual database manipulation.

Notes:

-

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

-

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

-

### Test Case 3 — Cross-Household Isolation

- Preconditions: User A is in WG A. User B is in WG B. An expense exists in WG A.
- Steps:
  1. Log in as User B.
  2. Try to access User A’s expense by URL/API.
- Expected result:
  - Access is denied or returns safe not-found.
  - Expense details are not leaked.

Result:

-

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

-

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

-

---

## 11. Merge Request Review Checklist

- [ ] MR title clearly identifies FR-05.
- [ ] MR description explains implemented behavior.
- [ ] MR links to issue/task if available.
- [ ] MR includes backend test evidence.
- [ ] MR includes frontend test/build evidence.
- [ ] MR includes manual test evidence.
- [ ] MR mentions known limitations.
- [ ] MR has no unrelated changes.
- [ ] MR has no committed secrets.
- [ ] MR has no large generated files unless required.
- [ ] CI pipeline passes.
- [ ] At least one reviewer has checked the code.
- [ ] QA reviewer has checked the relevant checklist sections.

Notes:

-

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
- [ ] PASS WITH MINOR NOTES — FR-05 works, but minor cleanup/documentation is needed.
- [ ] FAIL — FR-05 is not ready for merge.

Final notes:

-

QA reviewer:

Date:
