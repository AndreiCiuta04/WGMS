# FR-05 Frontend QA Checklist — Shared Expense Tracking

## Feature Information

- Feature ID: FR-05
- Feature name: Shared expense tracking
- Use case: UC-03 — Shared Expense Tracking and Settlement
- Priority: High
- Category: Core
- Version: V2
- Estimated effort: 48h
- Feature lead: Cemil Aslan
- Frontend technology: Angular
- Backend dependency: Java Spring Boot REST API
- Automated tests: Cypress / frontend unit tests where available
- Branch:
- Merge request:
- Developer:
- QA reviewer:
- Date:

---

## How to Use This Checklist

This checklist must be completed before the frontend part of FR-05 can be considered done.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a short note under the relevant section.
- If something does not apply, write `N/A` with a short reason.
- Do not merge into `dev` while critical items are unchecked.

This checklist is written so that it can be given to an AI assistant or a human reviewer to verify the frontend implementation systematically.

---

## Project-Specific Frontend Scope

FR-05 belongs to UC-03: Shared Expense Tracking and Settlement.

The frontend must allow authenticated WG members to record and view shared expenses. The user should be able to enter what was bought/paid, the total amount, the payer, and the roommates who participate in the expense. The UI must be understandable enough that a peer tester can complete the workflow without external instructions.

FR-05 is not the full payment status overview. FR-06 covers payment status overview. FR-14 covers one-click expense generation from shopping. However, the FR-05 frontend must collect and display the data needed by those later features.

Expected frontend areas to inspect:

- Angular routes for expenses.
- Expense list page/component.
- Expense detail page/component if implemented.
- Create/edit expense form.
- API service for expense requests.
- Models/interfaces/types for expenses and splits.
- Validation and error handling.
- Authentication guard integration.
- Navigation/sidebar/menu integration.
- Responsive layout.
- E2E tests or component tests.

---

## 1. Branch, Build, and Baseline Checks

- [ ] Correct branch is checked out: `FR-05/Shared-expense-tracking` or equivalent.
- [ ] Branch is up to date with `origin`.
- [ ] Frontend installs dependencies successfully.
- [ ] Frontend builds successfully.
- [ ] Frontend starts successfully.
- [ ] Existing frontend tests still pass.
- [ ] Existing E2E tests still pass or failures are documented.
- [ ] No unrelated frontend files were changed without explanation.
- [ ] No API keys, secrets, credentials, JWTs, or personal test data are committed.
- [ ] FR-05 does not break login, WG creation/invitation, task creation, or cleaning schedule views.

Suggested commands:

```bash
git branch --show-current
git status
git diff --name-status dev...HEAD

cd frontend
npm install
npm run build
npm test
npx cypress run
```

Use the actual frontend scripts from `package.json` if different.

Notes:

-

---

## 2. Requirement Coverage

The frontend must support the user-facing shared expense workflow.

- [ ] Logged-in user can find the expense tracking area from navigation.
- [ ] Expense list page exists.
- [ ] Empty expense list has a clear empty state.
- [ ] Existing expenses are displayed clearly.
- [ ] User can open a create-expense form.
- [ ] User can enter expense title/description.
- [ ] User can enter total amount.
- [ ] User can select or confirm payer.
- [ ] User can select roommates/participants.
- [ ] User can submit the expense.
- [ ] Created expense appears in the list without requiring confusing manual steps.
- [ ] Expense detail information can be viewed, either in a detail page, modal, or expanded list item.
- [ ] User can see who paid.
- [ ] User can see the total amount.
- [ ] User can see who participates in the split.
- [ ] User can see each participant’s share if backend provides it.
- [ ] User can edit expenses if edit is in scope.
- [ ] User can delete/cancel expenses if delete is in scope.
- [ ] If edit/delete is not implemented, the limitation is documented and the UI does not show broken buttons.

Notes:

-

---

## 3. Navigation and Routing

- [ ] Expense page is reachable from the main authenticated app layout.
- [ ] Expense route is protected by an auth guard.
- [ ] Unauthenticated user cannot access expense pages.
- [ ] Unauthorized access redirects to login or shows a proper error.
- [ ] Navigation label is understandable, e.g. `Expenses`, `Shared Expenses`, or equivalent.
- [ ] Expense page fits naturally with the existing dashboard/sidebar/header style.
- [ ] Browser refresh on expense route does not break the app.
- [ ] Back/forward navigation works.
- [ ] Route names are consistent with project style.
- [ ] Invalid expense detail route shows a safe not-found/error state.

Notes:

-

---

## 4. Create Expense Form

### Required Fields

- [ ] Form has a clear title/header.
- [ ] Expense title/description input is visible and labelled.
- [ ] Amount input is visible and labelled.
- [ ] Amount input uses suitable input type or validation.
- [ ] Payer selection is visible if payer can be selected.
- [ ] If payer is always current user, this is clearly shown or handled silently.
- [ ] Participant/roommate selection is visible.
- [ ] Submit button is visible.
- [ ] Cancel/back action is available.
- [ ] Required fields are clearly marked or validated.

### Validation

- [ ] Empty title is rejected if required.
- [ ] Empty amount is rejected.
- [ ] Amount `0` is rejected.
- [ ] Negative amount is rejected.
- [ ] Non-numeric amount is rejected.
- [ ] More than two decimals are handled safely.
- [ ] Missing participants are rejected or handled according to backend behavior.
- [ ] Duplicate participants cannot be selected.
- [ ] Form prevents obvious invalid submissions before hitting the backend where practical.
- [ ] Backend validation errors are displayed clearly.
- [ ] Submit button prevents double submission or handles loading state.

### UX

- [ ] Form can be completed without external instructions.
- [ ] Field labels are clear for normal users, not only developers.
- [ ] Error messages are understandable.
- [ ] Success message or visual confirmation is shown after creation.
- [ ] Loading state is shown during API call.
- [ ] User input is not lost unexpectedly after validation errors.
- [ ] Form layout is not too cramped on mobile.
- [ ] Form layout is comfortable on desktop.

Notes:

-

---

## 5. Expense List View

- [ ] List shows all expenses returned by the API for the current WG.
- [ ] List does not show fake/static data when backend data exists.
- [ ] Each expense item shows a meaningful title/description.
- [ ] Each expense item shows amount.
- [ ] Each expense item shows payer.
- [ ] Each expense item shows date or creation time if available.
- [ ] Each expense item shows participants or participant count.
- [ ] Expense list refreshes after creating a new expense.
- [ ] Expense list handles empty state.
- [ ] Expense list handles loading state.
- [ ] Expense list handles API error state.
- [ ] Long lists remain usable.
- [ ] If pagination/lazy loading is implemented, it works correctly.
- [ ] Sorting is predictable, preferably newest first.
- [ ] Amount formatting is consistent.

Notes:

-

---

## 6. Expense Detail/Edit/Delete Behavior

Complete this section according to implemented scope.

### Detail

- [ ] User can view detailed expense information.
- [ ] Detail view shows title/description.
- [ ] Detail view shows payer.
- [ ] Detail view shows total amount.
- [ ] Detail view shows participant split information.
- [ ] Detail view shows date/time.
- [ ] Detail view handles missing/unknown expense safely.
- [ ] Detail view handles backend errors safely.

### Edit

- [ ] Edit button is shown only when edit is supported.
- [ ] Edit form pre-fills current expense values.
- [ ] User can update allowed fields.
- [ ] Invalid edit input is rejected.
- [ ] Successful edit updates the list/detail view.
- [ ] Backend authorization errors are shown clearly.
- [ ] Cancel edit returns safely without unwanted changes.

### Delete/Cancel

- [ ] Delete/cancel button is shown only when delete/cancel is supported.
- [ ] User is asked for confirmation before deletion/cancellation.
- [ ] Successful deletion/cancellation updates the UI.
- [ ] Failed deletion/cancellation shows an error.
- [ ] Delete/cancel is not accidentally triggered by one click with no confirmation.

Notes:

-

---

## 7. API Integration

Inspect Angular services and network behavior.

- [ ] Expense API service exists or is integrated into existing service structure.
- [ ] API base URL follows project configuration style.
- [ ] Requests include authentication token.
- [ ] Create request payload matches backend DTO.
- [ ] List request matches backend endpoint.
- [ ] Detail request matches backend endpoint if implemented.
- [ ] Update request matches backend endpoint if implemented.
- [ ] Delete request matches backend endpoint if implemented.
- [ ] Frontend interfaces/types match backend response shape.
- [ ] No hardcoded localhost URLs are committed unless project convention allows them.
- [ ] No hardcoded test user IDs, household IDs, or expense IDs are required for normal use.
- [ ] API errors are caught and displayed.
- [ ] Network failure does not crash the app.
- [ ] User is not shown raw stack traces or raw JSON errors.

Suggested inspection:

```bash
grep -R "expense\|Expense" frontend/src/app
grep -R "localhost\|127.0.0.1" frontend/src/app || true
```

Notes:

-

---

## 8. Security and Privacy Frontend Checks

Frontend security is not a replacement for backend security, but the UI must not make unsafe assumptions.

- [ ] Expense pages require login.
- [ ] JWT/token is not printed in UI.
- [ ] JWT/token is not logged to console.
- [ ] User cannot select roommates from other households in normal UI.
- [ ] UI does not expose internal IDs unnecessarily unless needed.
- [ ] UI does not expose password hashes, tokens, or hidden backend fields.
- [ ] Sensitive financial data is not stored in unnecessary long-term browser storage.
- [ ] Console logs do not contain private expense details.
- [ ] Error messages do not leak unrelated household/user data.
- [ ] Frontend does not rely only on hidden buttons for authorization.

Notes:

-

---

## 9. Money Formatting and Display

- [ ] Amounts are displayed with two decimals.
- [ ] Currency is shown or project currency assumption is clear.
- [ ] Decimal separator behavior is acceptable for target users.
- [ ] Input accepts expected decimal format.
- [ ] Input does not silently convert wrong values.
- [ ] Large amounts do not break layout.
- [ ] Negative amounts are not displayed as normal expenses unless explicitly supported.
- [ ] Split amounts add up visually to the total if displayed.
- [ ] Rounding is understandable to the user if visible.
- [ ] Amount formatting is consistent between list, detail, and form.

Notes:

-

---

## 10. Responsive Design and Browser Compatibility

Reference targets:

- Mobile width around 360 CSS px minimum.
- iPhone-style viewport around 390 × 844.
- Android-style viewport around 412 × 915.
- Desktop around 1366 × 768 and higher.

Checks:

- [ ] Expense list is usable at 360px width.
- [ ] Create expense form is usable at 360px width.
- [ ] Buttons are tappable on mobile.
- [ ] Inputs are not cut off on mobile.
- [ ] Participant selection works on mobile.
- [ ] Modals/dropdowns do not overflow off-screen.
- [ ] Expense list is comfortable on desktop.
- [ ] Layout remains consistent with rest of app.
- [ ] Tested in Chrome.
- [ ] Tested in Firefox or another supported browser.
- [ ] No browser-specific console errors appear during normal workflow.

Suggested manual commands:

```bash
cd frontend
npm start
```

Then use browser dev tools responsive mode for 360px, 390px, 412px, and desktop.

Notes:

-

---

## 11. Accessibility and Usability

- [ ] Form inputs have visible labels.
- [ ] Buttons have clear text.
- [ ] Icon-only buttons have accessible labels or tooltips.
- [ ] Form can be navigated with keyboard.
- [ ] Focus order is logical.
- [ ] Error messages are connected to the relevant fields visually.
- [ ] Color alone is not the only way to understand errors or status.
- [ ] Text contrast is readable.
- [ ] Loading states are understandable.
- [ ] Empty states explain what to do next.
- [ ] A peer tester can create an expense without verbal help.
- [ ] A peer tester can understand who owes what from the display if split data is shown.

Notes:

-

---

## 12. Automated Frontend / E2E Test Coverage

Minimum useful E2E tests:

- [ ] Login and navigate to expense page.
- [ ] Empty expense list displays correctly.
- [ ] Create valid expense.
- [ ] Created expense appears in list.
- [ ] Invalid amount is rejected.
- [ ] Missing required fields are rejected.
- [ ] Backend/API error is handled.
- [ ] Unauthenticated user cannot access expense page.
- [ ] Mobile viewport smoke test for expense page.
- [ ] Existing Cypress tests still pass.

Suggested Cypress scenarios:

```text
1. Login as a WG member.
2. Navigate to Expenses.
3. Click Add Expense.
4. Enter title = "Toilet paper".
5. Enter amount = "6.00".
6. Select participants.
7. Submit.
8. Assert that "Toilet paper" and "6.00" appear in the expense list.
```

Suggested commands:

```bash
cd frontend
npx cypress open
npx cypress run
```

Notes:

-

---

## 13. Integration with Backend and Existing Features

- [ ] Frontend works with the real backend, not only mocked data.
- [ ] Login token from existing auth flow is reused.
- [ ] Current WG/household context is reused correctly.
- [ ] Roommate/member list is loaded from backend or correct app state.
- [ ] Expense creation works after creating/joining a WG.
- [ ] Expense feature does not break task/cleaning pages.
- [ ] Expense feature does not break shopping pages if those already exist.
- [ ] Expense feature can later feed dashboard/payment overview.
- [ ] Page reload keeps the user in a consistent authenticated state.
- [ ] User can log out and no private expense data remains visibly accessible.

Notes:

-

---

## 14. Visual Consistency

- [ ] Expense page follows existing app layout.
- [ ] Buttons use existing button styles.
- [ ] Forms use existing input styles.
- [ ] Spacing and typography are consistent.
- [ ] Error/success messages use existing alert/toast style.
- [ ] Mobile layout does not look unfinished.
- [ ] Desktop layout does not waste excessive space.
- [ ] No obvious placeholder text remains.
- [ ] No debugging labels or raw JSON are visible.
- [ ] No broken icons/images appear.

Notes:

-

---

## 15. Frontend QA Decision

### Critical Blockers

Mark as blocker if any of these fail:

- [ ] Frontend does not build.
- [ ] App cannot start.
- [ ] User cannot create an expense through the UI.
- [ ] Created expense does not appear or cannot be retrieved.
- [ ] UI uses fake/static data instead of backend data without explanation.
- [ ] Unauthenticated users can access expense pages.
- [ ] Expense API calls do not include authentication.
- [ ] Form allows obviously invalid money values with no backend/frontend rejection.
- [ ] UI breaks on mobile reference width.
- [ ] Existing core workflows are broken.

### Final Frontend Result

- [ ] PASS — Frontend FR-05 is ready for merge.
- [ ] PASS WITH MINOR NOTES — Frontend FR-05 works, but minor cleanup/documentation is needed.
- [ ] FAIL — Frontend FR-05 is not ready for merge.

Final notes:

-

QA reviewer:

Date:
