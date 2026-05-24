# FR-13 Frontend QA Checklist — History of Tasks and Payments

## Feature Information

- Feature ID: FR-13
- Feature name: History of tasks and payments
- Use case: UC-06 — Central Dashboard and Notifications Management
- Priority: High
- Category: Core
- Version: V3
- Lead: Andrei Ciuta
- Frontend technology: Angular
- Backend endpoint: `GET /api/v1/history`
- Main route: `/history`
- Branch:
- Merge request:
- Developer:
- QA reviewer:
- Date:

---

## How to Use This Checklist

This checklist must be completed before the frontend part of FR-13 can be considered done.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a short note under the relevant section.
- If something does not apply, write `N/A` with a short reason.
- Do not merge while any critical item is unchecked.
- Each item should be verified using code inspection, manual UI testing, component tests, e2e tests, or browser network inspection.

This checklist is written so that a human reviewer or AI assistant can systematically verify whether the frontend implementation satisfies FR-13.

---

## Expected Frontend Scope

FR-13 adds a household history page.

The frontend must provide:

- A `/history` route.
- A History page/component.
- A navigation/sidebar link to the History page.
- API integration with `GET /api/v1/history`.
- No frontend-sent `householdId`.
- A task history section.
- A payment/expense history section.
- Loading state.
- Error state.
- Empty state for no task history.
- Empty state for no payment/expense history.
- UI style consistent with existing pages.
- No new visual design system or external UI library.

---

# 1. Critical Checks — Route and Navigation

## Route

- [ ] `/history` route exists.
- [ ] `/history` route loads the History page/component.
- [ ] `/history` route is protected consistently with other authenticated app routes.
- [ ] Unauthenticated users cannot access the page without login.
- [ ] Refreshing the browser on `/history` does not break routing.
- [ ] Direct navigation to `/history` works.
- [ ] Browser back/forward navigation works from and to `/history`.
- [ ] The route is added in the correct Angular routing module/file according to project structure.
- [ ] Route naming is consistent with existing frontend route conventions.

## Sidebar / Navigation Link

- [ ] A History link is visible in the sidebar/navigation for authenticated users.
- [ ] The History link points to `/history`.
- [ ] The History link uses the same styling pattern as existing navigation links.
- [ ] The active state is shown correctly when the user is on `/history`.
- [ ] Adding the History link does not break existing navigation links.
- [ ] Sidebar layout remains visually consistent.
- [ ] The navigation label is clear and user-facing, e.g. `History`.

---

# 2. Critical Checks — Service and DTO Integration

## Frontend DTOs / Interfaces

The frontend model should match the backend response:

```ts
interface HistoryDto {
  tasks: TaskHistoryDto[];
  payments: PaymentHistoryDto[];
}
```

- [ ] Frontend has a `HistoryDto` type/interface.
- [ ] Frontend has a `TaskHistoryDto` type/interface.
- [ ] Frontend has a `PaymentHistoryDto` type/interface.
- [ ] DTO field names exactly match the backend JSON contract.
- [ ] Task DTO contains `id`.
- [ ] Task DTO contains `householdId`.
- [ ] Task DTO contains `cleaningScheduleId`.
- [ ] Task DTO contains `title`.
- [ ] Task DTO contains `description`.
- [ ] Task DTO contains `dueDate`.
- [ ] Task DTO contains `status`.
- [ ] Task DTO contains `createdByEmail`.
- [ ] Task DTO contains `assignedToEmail`.
- [ ] Task DTO contains `createdAt`.
- [ ] Task DTO contains `updatedAt`.
- [ ] Payment DTO contains `id`.
- [ ] Payment DTO contains `title`.
- [ ] Payment DTO contains `amount`.
- [ ] Payment DTO contains `status`.
- [ ] Payment DTO contains `payerEmail`.
- [ ] Payment DTO contains `receiverEmail`.
- [ ] Payment DTO contains `createdAt`.
- [ ] Payment DTO contains `updatedAt`.
- [ ] Optional/null fields are typed safely.

## Service

- [ ] A frontend history service exists or an existing service is extended appropriately.
- [ ] The service calls `GET /api/v1/history`.
- [ ] The service does not send `householdId`.
- [ ] The service does not send roommate ID, user ID, or email in the request.
- [ ] The service relies on existing authentication/JWT handling.
- [ ] The service returns an observable/promise consistent with existing Angular service style.
- [ ] The service does not use hard-coded production data.
- [ ] The service does not use mock data outside tests.
- [ ] The service handles the expected response shape.
- [ ] The service file is placed consistently with the existing frontend project structure.

---

# 3. Critical Checks — API Integration

- [ ] The History component calls the history service on page load.
- [ ] The request URL is exactly the configured API base URL plus `/api/v1/history`.
- [ ] The request method is `GET`.
- [ ] Browser network inspection confirms no `householdId` is sent.
- [ ] Browser network inspection confirms no user email or roommate ID is sent by the page.
- [ ] Authentication token/header is attached through the existing auth mechanism.
- [ ] Successful response renders both sections.
- [ ] `401 Unauthorized` or auth errors are handled consistently with the rest of the app.
- [ ] Backend errors show a user-friendly error state.
- [ ] The component does not crash if `tasks` is empty.
- [ ] The component does not crash if `payments` is empty.
- [ ] The component does not crash if optional fields are `null`.

---

# 4. Critical Checks — Page Layout and Theme Consistency

- [ ] Page title says `History`.
- [ ] Page layout matches existing app structure.
- [ ] Spacing, cards, tables, fonts, and buttons match existing pages.
- [ ] No external UI library is introduced.
- [ ] No unrelated global CSS/theme changes are introduced.
- [ ] History page does not visually look like a separate app.
- [ ] The page works in the same shell/layout as the dashboard and other pages.
- [ ] Sections are clearly separated.
- [ ] Task history section is clearly labeled.
- [ ] Payment/expense history section is clearly labeled.
- [ ] The UI remains readable with long task titles.
- [ ] The UI remains readable with long descriptions.
- [ ] The UI remains readable with long email addresses.
- [ ] The UI remains readable with large amounts.
- [ ] No placeholder/fake production entries are shown.

---

# 5. Critical Checks — Loading, Error, and Empty States

## Loading State

- [ ] Loading state appears while the API request is in progress.
- [ ] Loading state is visually consistent with existing pages.
- [ ] Loading state disappears after success.
- [ ] Loading state disappears after error.
- [ ] User does not see misleading empty states while data is still loading.
- [ ] Repeated navigation to the page does not leave the component stuck in loading state.

## Error State

- [ ] Error state appears if the API request fails.
- [ ] Error message is user-friendly.
- [ ] Error message does not expose raw stack traces.
- [ ] Error message does not expose internal backend details.
- [ ] Error state does not break the rest of the page shell.
- [ ] User can retry by refreshing or navigating back to the page.
- [ ] Auth-related errors follow existing app behavior.

## Empty States

- [ ] If there are no completed/cancelled tasks, the task section shows an empty state.
- [ ] If there are no payments/expenses, the payment section shows an empty state.
- [ ] Empty task history does not hide the payment section.
- [ ] Empty payment history does not hide the task section.
- [ ] If both arrays are empty, both empty states are understandable.
- [ ] Empty states do not display fake data.
- [ ] Empty states use wording consistent with the app’s tone.

---

# 6. Critical Checks — Task History Rendering

Each task history entry should show:

- title
- status
- due date
- assignedToEmail
- createdByEmail
- description if present
- cleaningScheduleId if present
- timestamp

Verify:

- [ ] Done tasks are rendered.
- [ ] Cancelled tasks are rendered.
- [ ] Open tasks are not rendered when backend excludes them.
- [ ] In-progress tasks are not rendered when backend excludes them.
- [ ] Task title is visible.
- [ ] Task status is visible.
- [ ] Due date is visible.
- [ ] Assigned user email is visible.
- [ ] Creator email is visible.
- [ ] Description is visible when present.
- [ ] Description area is hidden or shown as empty gracefully when not present.
- [ ] Cleaning schedule ID is visible when present.
- [ ] Cleaning schedule ID is hidden or shown gracefully when not present.
- [ ] Created/updated timestamp is visible.
- [ ] Date/timestamp formatting is readable.
- [ ] Null optional fields do not display as ugly values such as `undefined`, `NaN`, or `[object Object]`.
- [ ] Long task data wraps or truncates cleanly according to existing app style.
- [ ] Task section remains usable with many entries.

---

# 7. Critical Checks — Payment / Expense History Rendering

Each payment/expense history entry should show:

- title
- amount
- status
- payerEmail
- receiverEmail if present
- timestamp

Verify:

- [ ] Recorded expenses are rendered in the payment/expense history section.
- [ ] Title is visible.
- [ ] Amount is visible.
- [ ] Amount formatting is readable and consistent.
- [ ] Status is visible.
- [ ] Payer email is visible.
- [ ] Receiver email is visible when present.
- [ ] Receiver email is hidden or shown gracefully when `null`.
- [ ] Created/updated timestamp is visible.
- [ ] Date/timestamp formatting is readable.
- [ ] `RECORDED` status is displayed clearly.
- [ ] The UI wording does not falsely claim that an expense is a completed bank/payment transfer.
- [ ] The section can be titled or described as payment/expense history to avoid misleading users.
- [ ] Null optional fields do not display as `undefined`, `NaN`, or `[object Object]`.
- [ ] Large amounts are displayed without layout breakage.
- [ ] Payment/expense section remains usable with many entries.

---

# 8. Important Checks — Status Badges and Styling

- [ ] Task status badges visually match existing app badge style.
- [ ] Payment status badges visually match existing app badge style.
- [ ] `DONE` status has a clear visual indication.
- [ ] `CANCELLED` status has a clear visual indication.
- [ ] `RECORDED` status has a clear visual indication.
- [ ] Badge colors/classes are reused from existing styles where possible.
- [ ] New badge styles, if needed, are minimal and consistent.
- [ ] Status text remains readable with sufficient contrast.
- [ ] Badges are accessible to users who cannot rely only on color.
- [ ] Badges do not introduce global CSS regressions.

---

# 9. Important Checks — Household Privacy Assumptions

The frontend must not decide which household to load.

- [ ] Frontend does not read household ID from route params for history.
- [ ] Frontend does not send household ID as query parameter.
- [ ] Frontend does not send household ID in request body.
- [ ] Frontend does not store a selected household ID for this page.
- [ ] Frontend does not allow users to switch household history manually unless backend explicitly supports it.
- [ ] Browser network inspection confirms no household selector data is sent.
- [ ] The page relies on backend authorization for household isolation.
- [ ] No hidden form/input contains household ID for this page.
- [ ] No localStorage/sessionStorage value is used to override household scope.
- [ ] The UI does not display another household’s data when switching users.

---

# 10. Important Checks — Responsive Design

- [ ] History page works on desktop width.
- [ ] History page works on tablet width.
- [ ] History page works on mobile width if the app supports mobile layouts.
- [ ] Sidebar/navigation remains usable after adding History link.
- [ ] Cards/tables do not overflow horizontally on common screen sizes.
- [ ] Long email addresses wrap or truncate safely.
- [ ] Long descriptions do not destroy the layout.
- [ ] Sections stack cleanly on narrow screens.
- [ ] Loading/error/empty states are readable on narrow screens.
- [ ] Status badges remain readable on narrow screens.
- [ ] The page does not require horizontal scrolling unless this is already accepted app behavior.

---

# 11. Important Checks — Accessibility and Usability

- [ ] Page has a clear visible heading.
- [ ] Sections have clear headings.
- [ ] Status badges include text, not only color.
- [ ] Interactive navigation link is keyboard accessible.
- [ ] Focus behavior is not broken.
- [ ] Text contrast is acceptable.
- [ ] Error messages are readable.
- [ ] Loading state is understandable.
- [ ] Tables, if used, have meaningful headers.
- [ ] Cards, if used, have readable labels.
- [ ] Dates are formatted in a user-friendly way.
- [ ] Amounts are formatted in a user-friendly way.
- [ ] User can understand that recorded expenses currently appear under payment-related history.
- [ ] UI wording does not confuse recorded expenses with settled payments.

---

# 12. Important Checks — Component Tests

If Angular component tests are used:

- [ ] Component renders page title.
- [ ] Component calls history service on initialization.
- [ ] Component displays loading state.
- [ ] Component displays error state.
- [ ] Component displays empty task state.
- [ ] Component displays empty payment state.
- [ ] Component renders task history entries.
- [ ] Component renders payment/expense history entries.
- [ ] Component handles null description.
- [ ] Component handles null cleaning schedule ID.
- [ ] Component handles null receiver email.
- [ ] Component handles null updated timestamp.
- [ ] Component does not display `undefined`.
- [ ] Component uses expected CSS classes or app components for badges.
- [ ] Component tests do not require real backend calls.

If the project does not use Angular component tests:

- [ ] This section is marked `N/A` with a reason.

---

# 13. Critical Checks — E2E Tests

E2E tests should use the project’s established Cypress/Selenium setup.

## Navigation E2E

- [ ] Login as a valid user.
- [ ] Open the sidebar/navigation.
- [ ] Click the History link.
- [ ] Verify URL contains `/history`.
- [ ] Verify page title `History` is visible.
- [ ] Verify task history section is visible.
- [ ] Verify payment/expense history section is visible.

## Data Rendering E2E

Use test data with at least:

- one done task
- one cancelled task
- one open task
- one in-progress task
- one recorded expense

Then verify:

- [ ] Done task appears.
- [ ] Cancelled task appears.
- [ ] Open task does not appear.
- [ ] In-progress task does not appear.
- [ ] Recorded expense appears in payment/expense history.
- [ ] Task fields are visible.
- [ ] Payment/expense fields are visible.
- [ ] Status badges are visible.

## Household Isolation E2E

Use at least two users from different households:

- [ ] User A logs in and opens History.
- [ ] User A sees only household A task history.
- [ ] User A sees only household A payment/expense history.
- [ ] User A does not see known household B task text.
- [ ] User A does not see known household B expense text.
- [ ] User B logs in and opens History.
- [ ] User B sees only household B task history.
- [ ] User B sees only household B payment/expense history.
- [ ] User B does not see known household A task text.
- [ ] User B does not see known household A expense text.

## Empty/Error E2E

- [ ] User with no task history sees task empty state.
- [ ] User with no payment/expense history sees payment empty state.
- [ ] Forced API error shows error state.
- [ ] No fake data appears in empty/error states.

---

# 14. Regression Checks for Existing Frontend Features

## Sidebar / Navigation

- [ ] Existing sidebar links still work.
- [ ] Existing active link styling still works.
- [ ] Layout does not break after adding History.
- [ ] Logout/login navigation still works.
- [ ] Dashboard navigation still works.

## Tasks

- [ ] Existing task list page still loads.
- [ ] Existing task creation still works.
- [ ] Existing task assignment still works.
- [ ] Existing task status updates still work.
- [ ] Existing task UI styles are not broken by shared badge changes.

## Cleaning Schedules

- [ ] Existing cleaning schedule page still loads.
- [ ] Existing cleaning schedule UI still works.
- [ ] Existing generated task display is not broken.

## Expenses

- [ ] Existing expense page still loads.
- [ ] Existing expense creation still works.
- [ ] Existing expense list still works.
- [ ] Existing expense amount formatting is not broken.
- [ ] Existing expense services are not broken by history service additions.

---

# 15. Non-Functional Checks

## Usability

- [ ] User can find History from navigation without guessing the URL.
- [ ] Page explains its content clearly.
- [ ] Empty states explain why no data appears.
- [ ] Error states are understandable.
- [ ] Payment/expense wording avoids misleading financial meaning.
- [ ] Information density is reasonable.

## Consistency

- [ ] Visual design matches existing pages.
- [ ] Badge style matches existing app style.
- [ ] Date formatting matches existing app formatting.
- [ ] Amount formatting matches existing app formatting.
- [ ] Error/loading components match existing app patterns.
- [ ] No unrelated theme changes are introduced.

## Maintainability

- [ ] Component code is readable.
- [ ] Service code is separated from presentation logic.
- [ ] DTOs are reusable and clearly named.
- [ ] No duplicate API URL strings are scattered across many files.
- [ ] No production mock data is left in the component.
- [ ] No large unrelated refactoring is mixed into the feature.
- [ ] Current limitation around expense-backed payments is documented in UI text or developer comments if appropriate.

## Accessibility

- [ ] Keyboard navigation works.
- [ ] Text contrast is acceptable.
- [ ] Status information is not conveyed only through color.
- [ ] Tables/cards use meaningful labels.
- [ ] Loading/error messages are screen-reader understandable where the project supports this.

## Performance

- [ ] Page performs one history request on initial load, not repeated unnecessary requests.
- [ ] Component does not create infinite API loops.
- [ ] Rendering many entries remains acceptable.
- [ ] No heavy third-party UI library is added.
- [ ] No expensive client-side polling is introduced.
- [ ] Large descriptions/emails do not cause browser slowdown.

---

# 16. Frontend Merge Blockers

Do not approve the frontend if any of these are true:

- [ ] `/history` route does not exist.
- [ ] History page is not reachable from navigation.
- [ ] Frontend sends `householdId` to the backend.
- [ ] Frontend uses fake/mock production data.
- [ ] Page crashes on empty task history.
- [ ] Page crashes on empty payment history.
- [ ] Page displays `undefined`, `NaN`, or `[object Object]` for normal null fields.
- [ ] Done/cancelled tasks are not displayed despite backend returning them.
- [ ] Expense-backed payment entries are not displayed despite backend returning them.
- [ ] UI introduces a completely different visual design.
- [ ] Existing sidebar, task, or expense pages break.
- [ ] No manual or automated test evidence exists.

---

# 17. Frontend Final Approval

- [ ] All critical checks passed.
- [ ] Important checks passed or documented with accepted reasons.
- [ ] Route and navigation verified.
- [ ] API integration verified.
- [ ] Loading/error/empty states verified.
- [ ] Task rendering verified.
- [ ] Payment/expense rendering verified.
- [ ] Household privacy assumptions verified.
- [ ] Component tests added or marked `N/A`.
- [ ] E2E tests added or manual E2E evidence documented.
- [ ] Regression checks passed.
- [ ] Frontend is ready to merge.
