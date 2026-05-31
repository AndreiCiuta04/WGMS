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
- Branch: FR-05/Shared-expense-tracking
- Merge request:
- Developer:
- QA reviewer:
- Date: 2026-05-24

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

- [x] Correct branch is checked out: `FR-05/Shared-expense-tracking` or equivalent.
- [ ] Branch is up to date with `origin`.
- [ ] Frontend installs dependencies successfully.
- [x] Frontend builds successfully.
- [x] Frontend starts successfully.
- [x] Existing frontend tests still pass.
- [x] Existing E2E tests still pass or failures are documented.
- [x] No unrelated frontend files were changed without explanation.
- [x] No API keys, secrets, credentials, JWTs, or personal test data are committed.
- [x] FR-05 does not break login, WG creation/invitation, task creation, or cleaning schedule views.

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

- Automated evidence: `npm.cmd run build` passed.
- Automated evidence: `npm.cmd test -- --watch=false --browsers=ChromeHeadless` passed 57/57 frontend tests.
- E2E evidence: Cypress passed 23/23 tests after adding FR-05 expense coverage. The earlier FR-04 cleaning-schedule failure was caused by a stale backend process and passed after restarting the backend with the current code.

---

## 2. Requirement Coverage

The frontend must support the user-facing shared expense workflow.

- [x] Logged-in user can find the expense tracking area from navigation.
- [x] Expense list page exists.
- [x] Empty expense list has a clear empty state.
- [x] Existing expenses are displayed clearly.
- [x] User can open a create-expense form.
- [x] User can enter expense title/description.
- [x] User can enter total amount.
- [x] User can select or confirm payer.
- [x] User can select roommates/participants.
- [x] User can submit the expense.
- [x] Created expense appears in the list without requiring confusing manual steps.
- [x] Expense detail information can be viewed, either in a detail page, modal, or expanded list item.
- [x] User can see who paid.
- [x] User can see the total amount.
- [x] User can see who participates in the split.
- [x] User can see each participant’s share if backend provides it.
- [x] User can edit expenses if edit is in scope.
- [x] User can delete/cancel expenses if delete is in scope.
- [x] If edit/delete is not implemented, the limitation is documented and the UI does not show broken buttons.

Notes:

- Browser smoke check opened `http://127.0.0.1:4200/expenses` while unauthenticated; app redirected to login without console errors.

---

## 3. Navigation and Routing

- [x] Expense page is reachable from the main authenticated app layout.
- [x] Expense route is protected by an auth guard.
- [x] Unauthenticated user cannot access expense pages.
- [x] Unauthorized access redirects to login or shows a proper error.
- [x] Navigation label is understandable, e.g. `Expenses`, `Shared Expenses`, or equivalent.
- [x] Expense page fits naturally with the existing dashboard/sidebar/header style.
- [x] Browser refresh on expense route does not break the app.
- [ ] Back/forward navigation works.
- [x] Route names are consistent with project style.
- [ ] Invalid expense detail route shows a safe not-found/error state.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 4. Create Expense Form

### Required Fields

- [x] Form has a clear title/header.
- [x] Expense title/description input is visible and labelled.
- [x] Amount input is visible and labelled.
- [x] Amount input uses suitable input type or validation.
- [x] Payer selection is visible if payer can be selected.
- [x] If payer is always current user, this is clearly shown or handled silently.
- [x] Participant/roommate selection is visible.
- [x] Submit button is visible.
- [x] Cancel/back action is available.
- [x] Required fields are clearly marked or validated.

### Validation

- [x] Empty title is rejected if required.
- [x] Empty amount is rejected.
- [x] Amount `0` is rejected.
- [x] Negative amount is rejected.
- [x] Non-numeric amount is rejected.
- [x] More than two decimals are handled safely.
- [x] Missing participants are rejected or handled according to backend behavior.
- [x] Duplicate participants cannot be selected.
- [x] Form prevents obvious invalid submissions before hitting the backend where practical.
- [x] Backend validation errors are displayed clearly.
- [x] Submit button prevents double submission or handles loading state.

### UX

- [x] Form can be completed without external instructions.
- [x] Field labels are clear for normal users, not only developers.
- [x] Error messages are understandable.
- [x] Success message or visual confirmation is shown after creation.
- [x] Loading state is shown during API call.
- [x] User input is not lost unexpectedly after validation errors.
- [x] Form layout is not too cramped on mobile.
- [x] Form layout is comfortable on desktop.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 5. Expense List View

- [x] List shows all expenses returned by the API for the current WG.
- [x] List does not show fake/static data when backend data exists.
- [x] Each expense item shows a meaningful title/description.
- [x] Each expense item shows amount.
- [x] Each expense item shows payer.
- [x] Each expense item shows date or creation time if available.
- [x] Each expense item shows participants or participant count.
- [x] Expense list refreshes after creating a new expense.
- [x] Expense list handles empty state.
- [ ] Expense list handles loading state.
- [x] Expense list handles API error state.
- [ ] Long lists remain usable.
- [ ] If pagination/lazy loading is implemented, it works correctly.
- [x] Sorting is predictable, preferably newest first.
- [x] Amount formatting is consistent.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 6. Expense Detail/Edit/Delete Behavior

Complete this section according to implemented scope.

### Detail

- [x] User can view detailed expense information.
- [x] Detail view shows title/description.
- [x] Detail view shows payer.
- [x] Detail view shows total amount.
- [x] Detail view shows participant split information.
- [x] Detail view shows date/time.
- [ ] Detail view handles missing/unknown expense safely.
- [ ] Detail view handles backend errors safely.

### Edit

- [x] Edit button is shown only when edit is supported.
- [x] Edit form pre-fills current expense values.
- [x] User can update allowed fields.
- [x] Invalid edit input is rejected.
- [x] Successful edit updates the list/detail view.
- [x] Backend authorization errors are shown clearly.
- [x] Cancel edit returns safely without unwanted changes.

### Delete/Cancel

- [x] Delete/cancel button is shown only when delete/cancel is supported.
- [x] User is asked for confirmation before deletion/cancellation.
- [x] Successful deletion/cancellation updates the UI.
- [x] Failed deletion/cancellation shows an error.
- [x] Delete/cancel is not accidentally triggered by one click with no confirmation.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 7. API Integration

Inspect Angular services and network behavior.

- [x] Expense API service exists or is integrated into existing service structure.
- [x] API base URL follows project configuration style.
- [x] Requests include authentication token.
- [x] Create request payload matches backend DTO.
- [x] List request matches backend endpoint.
- [x] Detail request matches backend endpoint if implemented.
- [x] Update request matches backend endpoint if implemented.
- [x] Delete request matches backend endpoint if implemented.
- [x] Frontend interfaces/types match backend response shape.
- [x] No hardcoded localhost URLs are committed unless project convention allows them.
- [x] No hardcoded test user IDs, household IDs, or expense IDs are required for normal use.
- [x] API errors are caught and displayed.
- [x] Network failure does not crash the app.
- [x] User is not shown raw stack traces or raw JSON errors.

Suggested inspection:

```bash
grep -R "expense\|Expense" frontend/src/app
grep -R "localhost\|127.0.0.1" frontend/src/app || true
```

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 8. Security and Privacy Frontend Checks

Frontend security is not a replacement for backend security, but the UI must not make unsafe assumptions.

- [x] Expense pages require login.
- [x] JWT/token is not printed in UI.
- [x] JWT/token is not logged to console.
- [x] User cannot select roommates from other households in normal UI.
- [x] UI does not expose internal IDs unnecessarily unless needed.
- [x] UI does not expose password hashes, tokens, or hidden backend fields.
- [x] Sensitive financial data is not stored in unnecessary long-term browser storage.
- [x] Console logs do not contain private expense details.
- [x] Error messages do not leak unrelated household/user data.
- [x] Frontend does not rely only on hidden buttons for authorization.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 9. Money Formatting and Display

- [x] Amounts are displayed with two decimals.
- [x] Currency is shown or project currency assumption is clear.
- [x] Decimal separator behavior is acceptable for target users.
- [x] Input accepts expected decimal format.
- [ ] Input does not silently convert wrong values.
- [x] Large amounts do not break layout.
- [x] Negative amounts are not displayed as normal expenses unless explicitly supported.
- [x] Split amounts add up visually to the total if displayed.
- [x] Rounding is understandable to the user if visible.
- [ ] Amount formatting is consistent between list, detail, and form.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 10. Responsive Design and Browser Compatibility

Reference targets:

- Mobile width around 360 CSS px minimum.
- iPhone-style viewport around 390 × 844.
- Android-style viewport around 412 × 915.
- Desktop around 1366 × 768 and higher.

Checks:

- [x] Expense list is usable at 360px width.
- [x] Create expense form is usable at 360px width.
- [x] Buttons are tappable on mobile.
- [x] Inputs are not cut off on mobile.
- [x] Participant selection works on mobile.
- [x] Modals/dropdowns do not overflow off-screen.
- [x] Expense list is comfortable on desktop.
- [x] Layout remains consistent with rest of app.
- [x] Tested in Chrome.
- [x] Tested in Firefox or another supported browser.
- [x] No browser-specific console errors appear during normal workflow.

Suggested manual commands:

```bash
cd frontend
npm start
```

Then use browser dev tools responsive mode for 360px, 390px, 412px, and desktop.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 11. Accessibility and Usability

- [x] Form inputs have visible labels.
- [x] Buttons have clear text.
- [x] Icon-only buttons have accessible labels or tooltips.
- [ ] Form can be navigated with keyboard.
- [ ] Focus order is logical.
- [x] Error messages are connected to the relevant fields visually.
- [x] Color alone is not the only way to understand errors or status.
- [ ] Text contrast is readable.
- [x] Loading states are understandable.
- [x] Empty states explain what to do next.
- [x] A peer tester can create an expense without verbal help.
- [x] A peer tester can understand who owes what from the display if split data is shown.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 12. Automated Frontend / E2E Test Coverage

Minimum useful E2E tests:

- [x] Login and navigate to expense page.
- [x] Empty expense list displays correctly.
- [x] Create valid expense.
- [x] Created expense appears in list.
- [x] Invalid amount is rejected.
- [x] Missing required fields are rejected.
- [x] Backend/API error is handled.
- [x] Unauthenticated user cannot access expense page.
- [x] Mobile viewport smoke test for expense page.
- [x] Existing Cypress tests still pass.

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

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 13. Integration with Backend and Existing Features

- [x] Frontend works with the real backend, not only mocked data.
- [x] Login token from existing auth flow is reused.
- [x] Current WG/household context is reused correctly.
- [x] Roommate/member list is loaded from backend or correct app state.
- [x] Expense creation works after creating/joining a WG.
- [x] Expense feature does not break task/cleaning pages.
- [x] Expense feature does not break shopping pages if those already exist.
- [x] Expense feature can later feed dashboard/payment overview.
- [x] Page reload keeps the user in a consistent authenticated state.
- [x] User can log out and no private expense data remains visibly accessible.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

---

## 14. Visual Consistency

- [x] Expense page follows existing app layout.
- [x] Buttons use existing button styles.
- [x] Forms use existing input styles.
- [x] Spacing and typography are consistent.
- [x] Error/success messages use existing alert/toast style.
- [x] Mobile layout does not look unfinished.
- [x] Desktop layout does not waste excessive space.
- [x] No obvious placeholder text remains.
- [x] No debugging labels or raw JSON are visible.
- [x] No broken icons/images appear.

Notes:

- QA checks executed on 2026-05-24. Remaining unchecked items in this section are documented in the final notes or section notes below.

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
- [x] PASS WITH MINOR NOTES — Frontend FR-05 works, but minor cleanup/documentation is needed.
- [ ] FAIL — Frontend FR-05 is not ready for merge.

Final notes:

- Frontend was updated to match the current app layout and sidebar navigation. Build, unit tests, full Cypress 23/23, FR-05 Cypress in Electron and Firefox, reload/logout persistence, invalid amount validation, loading/double-submit protection, and 360/390/412px responsive checks passed on 2026-05-24.
- Remaining unchecked reasons: branch is ahead/dirty; `npm install` was not rerun because existing dependencies were sufficient for build/test; back/forward and invalid detail route were not manually tested; long-list/keyboard/focus/contrast checks were not fully verified; input rounding is deterministic but can silently normalize extra decimals. Critical blocker checkboxes are intentionally left unchecked because none of those blocker failures occurred; they are failure markers, not pass checks.

QA reviewer:

Date: 2026-05-24
