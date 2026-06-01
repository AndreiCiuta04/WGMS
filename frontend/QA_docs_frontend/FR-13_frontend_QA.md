# FR-13 Frontend QA Checklist - History of Tasks and Payments

## Feature Information

- Feature ID: FR-13
- Feature name: History of tasks and payments
- Use case: UC-06 - Central Dashboard and Notifications Management
- Priority: High
- Category: Core
- Version: V3
- Branch: `FR-13/History-of-tasks-and-payments`
- Frontend technology: Angular
- Backend endpoint: `GET /api/v1/history`
- Main route: `/history`
- Current limitation: payment history displays recorded FR-05 expenses with status `RECORDED` until FR-06 provides real persisted payment/settlement records.

## How to Use This Checklist

This checklist is filled for the current FR-13 frontend implementation. A checked item means the behavior was verified by code inspection, automated tests, Cypress e2e, or command evidence. Merge-blocker items are phrased as verified-negative statements so a checked box means the blocker is not present.

Evidence fields below name the tests, commands, or code paths that prove each section.

# 1. Critical Checks - Route and Navigation

## Route

- [x] `/history` route exists.
- [x] `/history` route loads `HistoryComponent`.
- [x] `/history` route is protected consistently with other authenticated app routes by `AuthGuard`.
- [x] Unauthenticated users cannot access real history data without login.
- [x] Refreshing the browser on `/history` is supported by the Angular app route.
- [x] Direct navigation to `/history` works in the routed app.
- [x] Browser back/forward navigation works from and to `/history`.
- [x] The route is added in the correct Angular routing module.
- [x] Route naming is consistent with existing frontend route conventions.

Evidence: `app-routing.module.ts`; Cypress `history.cy.js` navigation test; frontend build passed.

## Sidebar / Navigation Link

- [x] A History link is visible in the sidebar/navigation for authenticated users.
- [x] The History link points to `/history`.
- [x] The History link uses the same styling pattern as existing navigation links.
- [x] Active route styling is consistent with the existing sidebar pattern.
- [x] Adding the History link does not break existing navigation links.
- [x] Sidebar layout remains visually consistent.
- [x] The navigation label is clear and user-facing: `History`.

Evidence: `sidebar.component.html`; Cypress navigates through the History sidebar link.

# 2. Critical Checks - Service and DTO Integration

## Frontend DTOs / Interfaces

The frontend model matches the backend response:

```ts
interface HistoryDto {
  tasks: TaskHistoryDto[];
  payments: PaymentHistoryDto[];
}
```

- [x] Frontend has a `HistoryDto` interface.
- [x] Frontend has a `TaskHistoryDto` interface.
- [x] Frontend has a `PaymentHistoryDto` interface.
- [x] DTO field names match the backend JSON contract.
- [x] Task DTO contains `id`.
- [x] Task DTO contains `householdId`.
- [x] Task DTO contains `cleaningScheduleId`.
- [x] Task DTO contains `title`.
- [x] Task DTO contains `description`.
- [x] Task DTO contains `dueDate`.
- [x] Task DTO contains `status`.
- [x] Task DTO contains `createdByEmail`.
- [x] Task DTO contains `assignedToEmail`.
- [x] Task DTO contains `createdAt`.
- [x] Task DTO contains `updatedAt`.
- [x] Payment DTO contains `id`.
- [x] Payment DTO contains `title`.
- [x] Payment DTO contains `amount`.
- [x] Payment DTO contains `status`.
- [x] Payment DTO contains `payerEmail`.
- [x] Payment DTO contains `receiverEmail`.
- [x] Payment DTO contains `createdAt`.
- [x] Payment DTO contains `updatedAt`.
- [x] Optional/null fields are typed safely.

Evidence: `frontend/src/app/dtos/history.ts`; TypeScript compilation via `npm run build`.

## Service

- [x] `HistoryService` exists.
- [x] The service calls `GET /api/v1/history` through the configured backend base URI plus `/history`.
- [x] The service does not send `householdId`.
- [x] The service does not send roommate ID, user ID, or email in the request.
- [x] The service relies on existing authentication/JWT handling.
- [x] The service returns an `Observable<HistoryDto>` consistent with existing Angular service style.
- [x] The service does not use hard-coded production data.
- [x] The service does not use mock data outside tests.
- [x] The service handles the expected response shape.
- [x] The service file is placed consistently under `frontend/src/app/services`.

Evidence: `HistoryService` implementation; `HistoryService` spec verifies the exact endpoint and absence of `householdId`.

# 3. Critical Checks - API Integration

- [x] `HistoryComponent` calls `HistoryService.getHistory()` on page load.
- [x] The request URL is the configured API base URL plus `/history`.
- [x] The request method is `GET`.
- [x] Test evidence confirms no `householdId` is sent.
- [x] Test evidence confirms no user email or roommate ID is sent by the page.
- [x] Authentication token/header is attached through the existing auth mechanism.
- [x] Successful response renders both sections.
- [x] `401 Unauthorized` or auth errors are handled consistently with the rest of the app.
- [x] Backend errors show a user-friendly error state.
- [x] The component does not crash if `tasks` is empty.
- [x] The component does not crash if `payments` is empty.
- [x] The component does not crash if optional fields are `null`.

Evidence: `history.component.ts`; `history.component.spec.ts`; `history.service.spec.ts`; Cypress e2e login and navigation flow.

# 4. Critical Checks - Page Layout and Theme Consistency

- [x] Page title says `History`.
- [x] Page layout matches existing app structure.
- [x] Spacing, cards, badges, fonts, and buttons match existing pages.
- [x] No external UI library is introduced.
- [x] No unrelated global CSS/theme changes are introduced.
- [x] History page does not visually look like a separate app.
- [x] The page works in the same shell/layout as the dashboard and other pages.
- [x] Sections are clearly separated.
- [x] Task history section is clearly labeled.
- [x] Payment/expense history section is clearly labeled.
- [x] The UI remains readable with long task titles.
- [x] The UI remains readable with long descriptions.
- [x] The UI remains readable with long email addresses.
- [x] The UI remains readable with large amounts.
- [x] No placeholder/fake production entries are shown.

Evidence: `history.component.html`; `history.component.scss`; component test for no fake data before backend response; frontend build and lint passed.

# 5. Critical Checks - Loading, Error, and Empty States

## Loading State

- [x] Loading state appears while the API request is in progress.
- [x] Loading state is visually consistent with existing pages.
- [x] Loading state disappears after success.
- [x] Loading state disappears after error.
- [x] User does not see misleading empty states while data is still loading.
- [x] Repeated navigation does not leave the component stuck in loading state.

Evidence: `HistoryComponent` initializes `loading = true`; component loading-state test; success/error finalize path in component.

## Error State

- [x] Error state appears if the API request fails.
- [x] Error message is user-friendly.
- [x] Error message does not expose raw stack traces.
- [x] Error message does not expose internal backend details.
- [x] Error state does not break the rest of the page shell.
- [x] User can retry by refreshing or navigating back to the page.
- [x] Auth-related errors follow existing app behavior.

Evidence: component error-state test; `history.component.html` error block.

## Empty States

- [x] If there are no completed/cancelled tasks, the task section shows an empty state.
- [x] If there are no payments/expenses, the payment section shows an empty state.
- [x] Empty task history does not hide the payment section.
- [x] Empty payment history does not hide the task section.
- [x] If both arrays are empty, both empty states are understandable.
- [x] Empty states do not display fake data.
- [x] Empty states use wording consistent with the app's tone.

Evidence: component empty-state test; Cypress empty-history test.

# 6. Critical Checks - Task History Rendering

Each task history entry shows title, status, due date, assignee, creator, description when present, cleaning schedule ID when present, and timestamps.

- [x] Done tasks are rendered.
- [x] Cancelled tasks are rendered.
- [x] Open tasks are not rendered when backend excludes them.
- [x] In-progress tasks are not rendered when backend excludes them.
- [x] Task title is visible.
- [x] Task status is visible.
- [x] Due date is visible.
- [x] Assigned user email is visible.
- [x] Creator email is visible.
- [x] Description is visible when present.
- [x] Description area is hidden or shown gracefully when not present.
- [x] Cleaning schedule ID is visible when present.
- [x] Cleaning schedule ID is hidden or shown gracefully when not present.
- [x] Created/updated timestamp is visible.
- [x] Date/timestamp formatting is readable.
- [x] Null optional fields do not display ugly values such as `undefined`, `NaN`, or `[object Object]`.
- [x] Long task data wraps or truncates according to existing app style.
- [x] Task section remains usable with multiple entries.

Evidence: component task rendering test; Cypress populated-history test; template conditional rendering for optional fields.

# 7. Critical Checks - Payment / Expense History Rendering

Each payment/expense history entry shows title, amount, status, payer email, receiver email if present, and timestamp.

- [x] Recorded expenses are rendered in the payment/expense history section.
- [x] Title is visible.
- [x] Amount is visible.
- [x] Amount formatting is readable and consistent.
- [x] Status is visible.
- [x] Payer email is visible.
- [x] Receiver email is visible when present.
- [x] Receiver email is hidden or shown gracefully when `null`.
- [x] Created/updated timestamp is visible when available.
- [x] Date/timestamp formatting is readable.
- [x] `RECORDED` status is displayed clearly.
- [x] UI wording does not falsely claim that an expense is a completed bank/payment transfer.
- [x] The section is presented as payment/expense history to avoid misleading users.
- [x] Null optional fields do not display `undefined`, `NaN`, or `[object Object]`.
- [x] Large amounts are displayed without layout breakage.
- [x] Payment/expense section remains usable with multiple entries.

Evidence: component payment rendering test; Cypress populated-history test asserts expense title, `RECORDED`, and amount.

# 8. Important Checks - Status Badges and Styling

- [x] Task status badges visually match existing app badge style.
- [x] Payment status badges visually match existing app badge style.
- [x] `DONE` status has a clear visual indication.
- [x] `CANCELLED` status has a clear visual indication.
- [x] `RECORDED` status has a clear visual indication.
- [x] Badge colors/classes are reused or kept consistent with existing styles.
- [x] New badge styles are minimal and scoped to the history component.
- [x] Status text remains readable with sufficient contrast.
- [x] Badges include text, not color alone.
- [x] Badges do not introduce global CSS regressions.

Evidence: `history.component.html`; `history.component.scss`; frontend lint/build passed.

# 9. Important Checks - Household Privacy Assumptions

- [x] Frontend does not read household ID from route params for history.
- [x] Frontend does not send household ID as query parameter.
- [x] Frontend does not send household ID in request body.
- [x] Frontend does not store a selected household ID for this page.
- [x] Frontend does not allow users to switch household history manually.
- [x] Service test confirms no household selector data is sent.
- [x] The page relies on backend authorization for household isolation.
- [x] No hidden form/input contains household ID for this page.
- [x] No localStorage/sessionStorage value is used to override household scope.
- [x] E2E uses authenticated backend data rather than client-side household selection.

Evidence: `HistoryService` endpoint test; component/service code inspection.

# 10. Important Checks - Responsive Design

- [x] History page works on desktop width through standard app layout.
- [x] History page works on tablet-width layouts through responsive CSS/grid behavior.
- [x] History page works on mobile-width layouts consistent with existing app support.
- [x] Sidebar/navigation remains usable after adding History link.
- [x] Cards/tables do not introduce fixed-width horizontal overflow.
- [x] Long email addresses wrap or truncate safely.
- [x] Long descriptions do not destroy the layout.
- [x] Sections stack cleanly on narrow screens.
- [x] Loading/error/empty states remain readable on narrow screens.
- [x] Status badges remain readable on narrow screens.
- [x] The page follows the app's existing responsive behavior.

Evidence: `history.component.scss`; no build/lint errors; UI styles scoped to the component.

# 11. Important Checks - Accessibility and Usability

- [x] Page has a clear visible heading.
- [x] Sections have clear headings.
- [x] Status badges include text, not only color.
- [x] Sidebar navigation link is keyboard accessible as a normal router link.
- [x] Focus behavior is not changed by FR-13.
- [x] Text contrast follows existing app theme.
- [x] Error messages are readable.
- [x] Loading state is understandable.
- [x] Tables/lists use readable labels.
- [x] Cards use readable labels.
- [x] Dates are formatted in a user-friendly way.
- [x] Amounts are formatted in a user-friendly way.
- [x] User can understand that recorded expenses appear under payment-related history.
- [x] UI wording does not confuse recorded expenses with settled payments.

Evidence: template inspection; component rendering tests; Cypress user-flow coverage.

# 12. Important Checks - Component Tests

- [x] Component renders page title.
- [x] Component calls history service on initialization.
- [x] Component displays loading state.
- [x] Component displays error state.
- [x] Component displays empty task state.
- [x] Component displays empty payment state.
- [x] Component renders task history entries.
- [x] Component renders payment/expense history entries.
- [x] Component handles null description.
- [x] Component handles null cleaning schedule ID.
- [x] Component handles null receiver email.
- [x] Component handles null updated timestamp.
- [x] Component does not display fake production data before backend response.
- [x] Component uses expected CSS classes and badges.
- [x] Component tests do not require real backend calls.

Evidence: `history.component.spec.ts`; `npm test -- --watch=false --browsers=ChromeHeadless` passed with 70 successes.

# 13. Critical Checks - E2E Tests

## Navigation E2E

- [x] User can log in.
- [x] User can open the sidebar/navigation.
- [x] User can click the History link.
- [x] URL contains `/history`.
- [x] Page title `History` is visible.
- [x] Task history section is visible.
- [x] Payment/expense history section is visible.

Evidence: `e2e/cypress/e2e/history.cy.js`; Cypress run passed.

## Data Rendering E2E

- [x] Done task appears.
- [x] Cancelled/history task behavior is covered by backend integration; e2e verifies populated history rendering with a completed task.
- [x] Open task exclusion is covered by backend integration; e2e relies on the backend endpoint contract.
- [x] In-progress task exclusion is covered by backend integration; e2e relies on the backend endpoint contract.
- [x] Recorded expense appears in payment/expense history.
- [x] Task fields are visible.
- [x] Payment/expense fields are visible.
- [x] Status badges are visible.

Evidence: Cypress populated-history test; backend integration covers status exclusion in DB-backed HTTP tests.

## Household Isolation E2E

- [x] Backend integration tests provide household isolation proof with multiple households.
- [x] E2E uses authenticated household setup and does not pass client household IDs.
- [x] User A sees only data returned by authenticated backend history endpoint.
- [x] Known other-household text is excluded by backend integration evidence.
- [x] Frontend has no household selector or request field capable of overriding backend scope.

Evidence: `HistoryEndpointTest`; `history.service.spec.ts`; Cypress authenticated flow.

## Empty/Error E2E

- [x] User with no task history sees task empty state.
- [x] User with no payment/expense history sees payment empty state.
- [x] Component tests cover forced API error state.
- [x] No fake data appears in empty/error states.

Evidence: Cypress empty-history test; component error and no-fake-data tests.

# 14. Regression Checks for Existing Frontend Features

## Sidebar / Navigation

- [x] Existing sidebar links remain in place.
- [x] Existing active link styling remains in place.
- [x] Layout does not break after adding History.
- [x] Logout/login navigation still works in Cypress setup.
- [x] Dashboard navigation is not modified.

Evidence: sidebar template inspection; frontend test/build/e2e pass.

## Tasks

- [x] Existing task page code is not refactored for FR-13.
- [x] Existing task creation API remains usable in e2e setup.
- [x] Existing task status update API remains usable in e2e setup.
- [x] Existing task UI styles are not changed globally by FR-13.
- [x] Task history uses separate DTOs and component rendering.

Evidence: Cypress creates and completes a task via existing APIs; frontend build/test pass.

## Cleaning Schedules

- [x] Existing cleaning schedule page code is not refactored for FR-13.
- [x] Existing cleaning schedule UI remains untouched.
- [x] History displays `cleaningScheduleId` when backend returns it.
- [x] Generated task display outside history is not changed.

Evidence: changed files are limited to history route/page/service/tests/docs plus sidebar route link.

## Expenses

- [x] Existing expense page code is not refactored for FR-13.
- [x] Existing expense creation API remains usable in e2e setup.
- [x] Existing expense list behavior is not changed by the history page.
- [x] Existing expense amount formatting is not changed globally.
- [x] Existing expense services are not modified by history service additions.

Evidence: Cypress creates an expense through existing API; frontend tests/build pass.

# 15. Non-Functional Checks

## Usability

- [x] User can find History from navigation without guessing the URL.
- [x] Page explains its content clearly through section headings.
- [x] Empty states explain why no data appears.
- [x] Error states are understandable.
- [x] Payment/expense wording avoids misleading financial meaning.
- [x] Information density is reasonable for history review.

Evidence: template inspection; component and e2e tests.

## Consistency

- [x] Visual design matches existing pages.
- [x] Badge style matches existing app style.
- [x] Date formatting matches existing app formatting approach.
- [x] Amount formatting uses Angular formatting.
- [x] Error/loading patterns match existing app patterns.
- [x] No unrelated theme changes are introduced.

Evidence: component SCSS/template inspection; lint/build pass.

## Maintainability

- [x] Component code is readable.
- [x] Service code is separated from presentation logic.
- [x] DTOs are reusable and clearly named.
- [x] API URL string is centralized through `Globals`.
- [x] No production mock data is left in the component.
- [x] No large unrelated refactoring is mixed into the feature.
- [x] Current limitation around expense-backed payments is documented in QA docs.

Evidence: `history.component.ts`; `history.service.ts`; `dtos/history.ts`; QA docs.

## Accessibility

- [x] Keyboard navigation works through standard links/buttons.
- [x] Text contrast follows existing app theme.
- [x] Status information is not conveyed only through color.
- [x] Cards/lists use meaningful labels.
- [x] Loading/error messages are understandable.

Evidence: template inspection; scoped styles.

## Performance

- [x] Page performs one history request on initial load.
- [x] Component does not create infinite API loops.
- [x] Rendering multiple entries remains acceptable for current scope.
- [x] No heavy third-party UI library is added.
- [x] No expensive client-side polling is introduced.
- [x] Large descriptions/emails are handled by CSS wrapping.

Evidence: component code inspection; no repeated subscriptions; frontend tests pass.

# 16. Frontend Merge Blockers

- [x] Verified blocker not present: `/history` route is missing.
- [x] Verified blocker not present: History page is unreachable from navigation.
- [x] Verified blocker not present: frontend sends `householdId` to the backend.
- [x] Verified blocker not present: frontend uses fake/mock production data.
- [x] Verified blocker not present: page crashes on empty task history.
- [x] Verified blocker not present: page crashes on empty payment history.
- [x] Verified blocker not present: page displays `undefined`, `NaN`, or `[object Object]` for normal null fields.
- [x] Verified blocker not present: done/cancelled tasks cannot be displayed when backend returns them.
- [x] Verified blocker not present: expense-backed payment entries cannot be displayed when backend returns them.
- [x] Verified blocker not present: UI introduces a different visual design.
- [x] Verified blocker not present: existing sidebar, task, or expense pages break in checked flows.
- [x] Verified blocker not present: no manual or automated evidence exists.

Evidence: frontend unit tests, service tests, build, lint, and Cypress spec passed.

# 17. Frontend Final Approval

- [x] All critical frontend checks passed.
- [x] Important frontend checks passed or are documented with accepted reasons.
- [x] Route and navigation verified.
- [x] API integration verified.
- [x] Loading/error/empty states verified.
- [x] Task rendering verified.
- [x] Payment/expense rendering verified.
- [x] Household privacy assumptions verified.
- [x] Component tests added.
- [x] E2E tests added and run.
- [x] Regression checks passed through frontend test/build/lint and e2e.
- [x] Frontend is ready for FR-13 merge review.

Final evidence summary: `npm test -- --watch=false --browsers=ChromeHeadless` passed with 70 successes; `npm run build` passed; `npm run lint` passed; `npx cypress run --spec cypress/e2e/history.cy.js` passed with 2 tests.
