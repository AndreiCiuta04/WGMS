# WGMS — FR-16 Statistical History View QA Form

## Scope

This QA form checks **FR-16 — Statistical History View** as implemented in the current WGMS design.

FR-16 is a **read-only statistical analysis view** for the authenticated user’s active household. It aggregates existing task, cleaning schedule, expense, payment, and shopping data over configurable time ranges. It does **not** replace the chronological FR-13 History page, introduce predictive forecasting, create a separate household selector, or create new domain entities solely for statistics.

This QA form focuses on:

- the dedicated protected `/statistics` page and consistent navigation
- date range, preset period, grouping, and overview-module filters
- correct overview, task/cleaning, expense/payment, and shopping statistics
- strict household scoping and shopping-visibility privacy rules
- in-house SVG chart rendering, loading, empty, and error states
- backend, frontend, Cypress, reference viewport, and regression evidence
- relevant NFR-01 to NFR-06 obligations

### Accepted scope boundaries / non-defects

The following are intentionally **out of scope** and must not be reported as missing FR-16 functionality:

- replacing or embedding FR-13 chronological History inside the Statistics page
- selecting another household from the Statistics page
- predictive stock forecasting, recommendations, or ML-based analysis
- creating a separate persistence entity for statistics
- inferring settlement status for individual `ExpenseSplit` records from payments
- an outstanding-split settlement chart where no persisted payment-to-split link exists
- adding an external charting dependency when lightweight in-house SVG components are used

---

# FR16-01 — Statistics Page, Navigation, and Route Protection

## Requirement

FR-16 must be available as a dedicated **Statistics** page at `/statistics`. It must coexist with, not replace, the existing chronological History page. Navigation must remain consistent across desktop/sidebar, mobile bottom navigation, and mobile overflow navigation.

## Checklist

- [ ] `/statistics` route exists and loads the Statistics page.
- [ ] The route is protected by the existing authentication guard.
- [ ] Unauthenticated direct navigation to `/statistics` redirects or is rejected according to existing routing rules.
- [ ] Authenticated users can reach Statistics from the main application navigation.
- [ ] The navigation item is configured through the existing `NAV_ITEMS`/equivalent navigation configuration rather than duplicated ad hoc.
- [ ] Sidebar navigation contains the Statistics entry where applicable.
- [ ] Mobile bottom navigation contains or correctly exposes the Statistics entry where applicable.
- [ ] Mobile overflow navigation contains the Statistics entry where applicable.
- [ ] Active navigation state is visible when the Statistics page is open.
- [ ] Existing `/history` route still opens the chronological FR-13 History page.
- [ ] Statistics page does not replace History with a tabbed or merged implementation unless this is the existing agreed design.
- [ ] Browser refresh and direct URL navigation to `/statistics` work after login.
- [ ] Login/logout transitions do not leave the Statistics page in a broken state.

## Test coverage evidence

- [ ] Routing/component test covers Statistics route creation.
- [ ] Auth guard test covers unauthenticated Statistics access.
- [ ] Frontend navigation test covers desktop/mobile navigation expectations.
- [ ] Cypress covers direct unauthenticated route access.
- [ ] Cypress covers navigation from an authenticated session to `/statistics`.
- [ ] Existing History route tests still pass.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Route files reviewed:
- Navigation configuration reviewed:
- Frontend tests:
- E2E route tests:
- Remaining issues:
```

---

# FR16-02 — Date Range, Presets, Grouping, and Module Filters

## Requirement

Users must be able to inspect statistics over configurable time ranges. The frontend must validate custom ranges before requesting data. Grouping controls must support day, week, and month. Overview statistics must support repeated module filter query parameters.

## Checklist

### Date range and preset controls

- [ ] Last 7 days preset exists.
- [ ] Last 30 days preset exists.
- [ ] Last 3 months preset exists.
- [ ] Last 6 months preset exists.
- [ ] Last 12 months preset exists.
- [ ] Custom range mode exists.
- [ ] Selecting a preset updates `from` and `to` consistently.
- [ ] Custom range allows users to enter/select both `from` and `to`.
- [ ] Invalid custom range where `from > to` is rejected in the UI.
- [ ] Invalid custom range does not send a statistics request to the backend.
- [ ] Missing required custom range dates are handled clearly.
- [ ] Changing range refreshes all applicable statistics sections consistently.
- [ ] Date formatting/timezone handling is consistent with project date conventions.

### Grouping controls

- [ ] Day grouping exists.
- [ ] Week grouping exists.
- [ ] Month grouping exists.
- [ ] Selected grouping is sent to relevant statistics requests.
- [ ] Trend data changes grouping shape/labels consistently.
- [ ] Grouping selection remains stable after data reload where expected.

### Overview module filters

- [ ] Tasks module filter exists.
- [ ] Cleaning module filter exists.
- [ ] Expenses module filter exists.
- [ ] Payments module filter exists.
- [ ] Shopping module filter exists.
- [ ] Multiple modules can be selected.
- [ ] Selected modules are sent as repeated query parameters, not concatenated into an undocumented format.
- [ ] Clearing filters restores the unfiltered overview behavior.
- [ ] Module filters affect overview statistics only unless another endpoint explicitly supports them.
- [ ] UI labels are understandable to a peer tester.

## Test coverage evidence

- [ ] Frontend test covers each preset date calculation.
- [ ] Frontend test covers invalid custom range with no backend request.
- [ ] Frontend test covers grouping selection.
- [ ] Frontend/service test verifies repeated module query parameters.
- [ ] Backend test verifies date range filtering.
- [ ] Backend test verifies day/week/month grouping.
- [ ] Endpoint test verifies request parameter binding for `from`, `to`, `grouping`, and repeated `modules`.
- [ ] Explicit `@RequestParam` names or equivalent binding protection are code-reviewed.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Filter components reviewed:
- Request-building code reviewed:
- Backend request parameter binding reviewed:
- Tests:
- Remaining issues:
```

---

# FR16-03 — Overview Statistics Correctness

## Requirement

The overview endpoint must provide a correct aggregated summary for the selected household, date range, grouping, and optional modules.

## Expected overview data

- completed task count
- task completion rate
- total recorded expense amount
- recorded payment amount
- purchased shopping item count
- active cleaning schedule count
- activity grouped by module
- activity trend grouped by day, week, or month

## Checklist

- [ ] Completed task count is correct for the selected range.
- [ ] Task completion rate uses the documented denominator and handles zero-task range safely.
- [ ] Total recorded expense amount is correct for the selected range.
- [ ] Recorded payment amount is correct for the selected range.
- [ ] Purchased shopping item count is correct for the selected range.
- [ ] Active cleaning schedule count is correct.
- [ ] Activity-by-module values are correct.
- [ ] Activity trend contains correct periods for selected grouping.
- [ ] Module filters exclude unselected modules from overview values/trend according to documented behavior.
- [ ] Empty household/range returns a valid zero/empty-shaped response.
- [ ] Date-boundary records are included/excluded consistently.
- [ ] Monetary values preserve existing financial precision/rounding behavior.
- [ ] Response does not include unnecessary raw source records.

## Backend test evidence

- [ ] Unit test for overview with mixed task/expense/payment/shopping/cleaning data.
- [ ] Unit test for zero-data/empty household response.
- [ ] Unit test for date boundaries.
- [ ] Unit test for module filtering.
- [ ] Unit test for day/week/month overview trend.
- [ ] Integration test for overview endpoint response mapping.
- [ ] Integration test for repeated `modules` query parameters.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Overview service reviewed:
- DTO mapping reviewed:
- Unit tests:
- Endpoint tests:
- Remaining issues:
```

---

# FR16-04 — Task and Cleaning Schedule Statistics Correctness

## Requirement

Task statistics must correctly aggregate the existing `Task` and `CleaningSchedule` model without changing task/recurrence behavior.

## Expected task/cleaning statistics

- total task count
- completed task count
- cancelled task count
- overdue task count
- completion rate
- task status distribution
- cleaning frequency distribution
- schedule-generated task count
- schedule-generated completion rate
- task completion trend
- completed tasks by member
- completed tasks by weekday

## Checklist

- [ ] Total task count is correct for selected range.
- [ ] Completed task count is derived from current/recorded task status consistently.
- [ ] Cancelled task count is correct.
- [ ] Overdue task count follows existing due-date/status rules.
- [ ] Completion rate handles zero tasks safely.
- [ ] Task status distribution sums consistently with total task count.
- [ ] Cleaning frequency distribution reflects existing `CleaningSchedule` metadata.
- [ ] Schedule-generated tasks are identified only through valid schedule linkage.
- [ ] Schedule-generated completion rate is correct.
- [ ] Task completion trend respects date range and grouping.
- [ ] Completed tasks by member excludes unrelated household users.
- [ ] Completed tasks by weekday are correctly grouped.
- [ ] Undo/reopen behavior is reflected according to current persisted task state/history rules.
- [ ] Existing task creation, completion, undo, filtering, and recurrence behavior remains unchanged.

## Backend test evidence

- [ ] Unit test for task status distribution.
- [ ] Unit test for overdue tasks.
- [ ] Unit test for task completion rate.
- [ ] Unit test for cleaning frequency distribution.
- [ ] Unit test for schedule-generated task count/completion rate.
- [ ] Unit test for member and weekday grouping.
- [ ] Unit test for current-state behavior after task completion undo where model supports it.
- [ ] Integration test for task statistics endpoint.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Task/cleaning statistics service reviewed:
- Task/CleaningSchedule repositories reviewed:
- Unit tests:
- Endpoint tests:
- Regression tests:
- Remaining issues:
```

---

# FR16-05 — Expense and Payment Statistics Correctness

## Requirement

Expense and payment statistics must use existing `Expense` and `Payment` data and keep them as separate concepts. The feature must not infer a per-split settlement state where the domain model has no persisted Payment-to-ExpenseSplit link.

## Expected expense/payment statistics

- expense count
- total expense amount
- average expense amount
- largest expense amount
- payment count
- total recorded payment amount
- expense trend
- payment trend
- expenses paid by member
- payments sent and received by member

## Checklist

- [ ] Expense count is correct for selected range.
- [ ] Total expense amount is correct.
- [ ] Average expense amount is correct and handles zero expenses safely.
- [ ] Largest expense amount is correct and handles zero expenses safely.
- [ ] Payment count is correct.
- [ ] Total recorded payment amount is correct.
- [ ] Expense and payment trends are separate and correctly grouped.
- [ ] Expenses paid by member are attributed to the correct payer.
- [ ] Payments sent by member are attributed to the correct sender.
- [ ] Payments received by member are attributed to the correct receiver.
- [ ] Amount calculations use existing `BigDecimal`/rounding conventions where applicable.
- [ ] Deleted/anonymized user display is handled consistently with the finance module.
- [ ] Other-household expenses/payments never influence the response.
- [ ] UI/documentation does not falsely claim that payments settle specific ExpenseSplit records.
- [ ] Absence of outstanding split-settlement statistics is documented as an accepted scope limitation.

## Backend test evidence

- [ ] Unit test for expense count/total/average/largest amount.
- [ ] Unit test for payment count/total.
- [ ] Unit test for expense trend.
- [ ] Unit test for payment trend.
- [ ] Unit test for payer/sender/receiver member breakdowns.
- [ ] Unit test for zero-data behavior.
- [ ] Unit test for financial rounding edge values where applicable.
- [ ] Integration test for expenses statistics endpoint.
- [ ] Cross-household finance data isolation test.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Expense/payment statistics service reviewed:
- DTO mapping reviewed:
- Unit tests:
- Endpoint tests:
- Scope limitation evidence:
- Remaining issues:
```

---

# FR16-06 — Shopping Statistics and Visibility Privacy

## Requirement

Shopping statistics must reflect only the shopping items visible to the current authenticated user under the existing visibility rules. Private items created by other users must never be exposed through statistics.

## Expected shopping statistics

- added item count
- purchased item count
- purchase completion rate
- shopping item status distribution
- shopping item visibility distribution
- shopping activity trend
- most frequently purchased items
- items added by member

## Visibility rules to verify

The current user may see:

- their own personal shopping items
- shopping items targeted at them
- shared household shopping items

The current user must **not** see private/personal items created by other users unless another existing rule explicitly authorizes it.

## Checklist

- [ ] Added item count is correct for visible items in selected range.
- [ ] Purchased item count is correct.
- [ ] Purchase completion rate handles zero added items safely.
- [ ] Status distribution is correct.
- [ ] Visibility distribution is correct for data legitimately visible to the current user.
- [ ] Shopping activity trend respects date range and grouping.
- [ ] Most frequently purchased items are built only from bought items.
- [ ] Purchased item-name grouping uses normalized item names consistently.
- [ ] Items added by member are attributed correctly for visible records.
- [ ] Current user’s personal items are included.
- [ ] Items targeted at current user are included.
- [ ] Shared household items are included.
- [ ] Other users’ personal/private items are excluded.
- [ ] Other-household shopping items are excluded.
- [ ] Shopping stats do not reveal private item names/counts through aggregates, bar lists, charts, or tooltips.
- [ ] Mark/unmark bought and shopping history behavior remains unchanged.

## Backend test evidence

- [ ] Unit test for added/purchased counts and completion rate.
- [ ] Unit test for status/visibility distribution.
- [ ] Unit test for normalized most-frequently-purchased item grouping.
- [ ] Unit test for own personal item inclusion.
- [ ] Unit test for targeted item inclusion.
- [ ] Unit test for shared item inclusion.
- [ ] Unit test for exclusion of other users’ private items.
- [ ] Unit test for other-household exclusion.
- [ ] Integration test for shopping statistics endpoint.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Shopping statistics service reviewed:
- Visibility logic reviewed:
- Unit tests:
- Endpoint tests:
- Privacy evidence:
- Remaining issues:
```

---

# FR16-07 — Security, Authorization, API Contract, and Household Scoping

## Requirement

All statistics endpoints must require an authenticated `USER` and derive the active household entirely on the backend from the authenticated user. The frontend must not control or supply the household identity.

## Expected endpoints

```text
GET /api/v1/history/statistics/overview
GET /api/v1/history/statistics/tasks
GET /api/v1/history/statistics/expenses
GET /api/v1/history/statistics/shopping
```

## Checklist

- [ ] All four statistics endpoints exist or equivalent documented paths exist.
- [ ] Each endpoint requires authentication and USER-role access.
- [ ] Unauthenticated requests are rejected.
- [ ] Current application user is resolved from Authentication/JWT email/identity.
- [ ] Active roommate membership is resolved backend-side.
- [ ] Active household is resolved backend-side.
- [ ] Frontend does not send householdId for authorization.
- [ ] Backend does not accept/trust frontend householdId if attempted.
- [ ] Cross-household tasks do not influence statistics.
- [ ] Cross-household cleaning schedules do not influence statistics.
- [ ] Cross-household expenses/payments do not influence statistics.
- [ ] Cross-household shopping items do not influence statistics.
- [ ] Response DTOs expose aggregates only and avoid unnecessary raw records.
- [ ] Response DTO naming follows existing history API patterns.
- [ ] Endpoint request parameters use explicit names/binding where required.
- [ ] Invalid request values return clear validation errors or safe documented behavior.
- [ ] OpenAPI/Swagger documentation is updated if the project exposes it.

## Security/integration test evidence

- [ ] Unauthenticated test for every endpoint or representative shared security configuration test exists.
- [ ] Authenticated current-household overview test exists.
- [ ] Cross-household data isolation test exists for each statistical domain.
- [ ] Request parameter binding test exists.
- [ ] Response mapping test exists.
- [ ] Test verifies no frontend householdId is needed/trusted.
- [ ] Shopping private-item leakage test exists.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Endpoint files reviewed:
- Auth/current-user resolution reviewed:
- DTO files reviewed:
- Security/integration tests:
- OpenAPI evidence:
- Remaining issues:
```

---

# FR16-08 — Frontend Visualization, States, Accessibility, and Responsive UI

## Requirement

The Statistics page must render statistical data using lightweight in-house SVG components and readable lists/cards. It must remain understandable and usable on the project reference desktop and mobile viewports.

## Expected frontend components

- Statistics page/component
- line chart component
- donut chart component
- bar list component

## Checklist

### Data rendering and visual behavior

- [ ] Overview section is visible.
- [ ] Task statistics section is visible.
- [ ] Expense/payment statistics section is visible.
- [ ] Shopping statistics section is visible.
- [ ] Line chart renders trend data correctly.
- [ ] Donut chart renders distribution data correctly.
- [ ] Bar list renders member/category/item values correctly.
- [ ] Donut chart uses flat arc ends rather than rounded caps, where this is the agreed presentational design.
- [ ] No external charting dependency was introduced unless explicitly approved.
- [ ] Monetary values are formatted consistently.
- [ ] Percentages are formatted consistently.
- [ ] Period labels are understandable for day/week/month groupings.
- [ ] Chart legends/labels match displayed values.
- [ ] Important values are available as text/cards/lists, not only through color or hover behavior.

### Loading, empty, stale-data, and error behavior

- [ ] Loading state is visible during initial request.
- [ ] Loading state is handled when filters change.
- [ ] Empty household/range shows a clear empty state.
- [ ] Error state is visible when request fails.
- [ ] Existing/stale data is cleared or clearly prevented from being mistaken for current data after a failed refresh.
- [ ] One failed section does not leave the entire page unusable where independent requests are intended.
- [ ] Retry/refresh behavior is understandable where implemented.

### Usability and accessibility

- [ ] Sections have clear headings.
- [ ] Controls have visible labels/accessibility names.
- [ ] Keyboard navigation works for presets, custom dates, grouping, and module filters.
- [ ] Focus state is visible for interactive controls.
- [ ] Color is not the only way to distinguish chart data/status.
- [ ] Empty/error states explain what users can do next.
- [ ] Screen-reader-friendly text/labels exist for SVG charts where reasonably implemented.
- [ ] Page can be understood by a peer tester without external explanation.

### Responsive/reference platform checks

- [ ] Desktop layout is usable at 1366 × 768 and higher.
- [ ] Small mobile boundary at 360 CSS px remains usable.
- [ ] iPhone 12 Pro-like viewport around 390 × 844 is usable.
- [ ] Pixel 7-like viewport around 412 × 915 is usable.
- [ ] Charts/lists do not require horizontal scrolling at target widths.
- [ ] Filter controls remain reachable on mobile.
- [ ] Navigation remains usable on mobile.
- [ ] No important chart/control/action is clipped or hidden.

## Frontend test evidence

- [ ] Statistics page creation test exists.
- [ ] Initial statistics loading test exists.
- [ ] Successful overview/task/expense/shopping response rendering tests exist.
- [ ] Error handling test exists.
- [ ] Stale-data clearing-after-failure test exists.
- [ ] Preset filter behavior test exists.
- [ ] Custom date validation test exists.
- [ ] Grouping selection test exists.
- [ ] Module selection/repeated query parameter test exists.
- [ ] Formatting helper tests exist.
- [ ] Line chart rendering logic test exists.
- [ ] Donut chart rendering logic test exists.
- [ ] Bar list rendering logic test exists.
- [ ] Mobile navigation expectation updates are tested where navigation changed.
- [ ] Existing History component tests still pass.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Statistics components reviewed:
- SVG chart components reviewed:
- Dependency/package review:
- Frontend tests:
- Reference viewport checks:
- Accessibility evidence:
- Remaining issues:
```

---

# FR16-09 — Demo/Test Data, Cypress E2E, and Reliability Evidence

## Requirement

FR-16 must be testable with deterministic data that demonstrates non-empty, empty, filtered, and privacy-sensitive statistical outcomes. Cypress must verify the user-facing workflow without relying only on visual inspection.

## Demo/test data checklist

- [ ] Test data includes tasks in multiple statuses.
- [ ] Test data includes completed, cancelled, open, and overdue tasks where required by visible metrics.
- [ ] Test data includes schedule-generated tasks and cleaning schedules where schedule metrics are displayed.
- [ ] Test data includes expenses with different values/dates/payers.
- [ ] Test data includes payments with different senders/receivers/dates.
- [ ] Test data includes shopping items with NEEDED/BOUGHT/CANCELLED or applicable statuses.
- [ ] Test data includes SHARED, PERSONAL, and TARGETED shopping visibility scenarios.
- [ ] Test data includes another household for cross-household isolation evidence.
- [ ] Test data includes another user’s private shopping item for privacy-exclusion evidence.
- [ ] Test/demo profile is isolated from production data.
- [ ] Seed/data generator is idempotent or test setup is isolated.

## Minimum Cypress E2E coverage

- [ ] Unauthenticated navigation to `/statistics` redirects/rejects.
- [ ] Authenticated user reaches Statistics page through navigation.
- [ ] Overview statistics render from backend-backed data.
- [ ] Completed task statistics render.
- [ ] Expense statistics render.
- [ ] Shopping statistics render.
- [ ] Invalid custom date range is blocked in UI and does not send invalid request.
- [ ] Module filter request behavior is verified.
- [ ] Empty household/range state is verified.
- [ ] Private shopping item data is not displayed to unauthorized household member.
- [ ] Responsive viewport test verifies no horizontal overflow.
- [ ] Stable selectors (`data-cy`) are used for critical interactions.
- [ ] Test does not depend on fragile CSS class selectors alone.

## Commands

```bash
cd e2e
npx cypress run --spec "cypress/e2e/fr16-statistical-history.cy.js"
```

```bash
cd e2e
npx cypress run
```

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Seed/demo data:
- Cypress spec(s):
- Targeted E2E command/result:
- Full E2E command/result:
- Responsive evidence:
- Privacy E2E evidence:
- Remaining issues:
```

---

# FR16-10 — Regression Coverage and Non-Functional Requirements Summary

## Requirement

FR-16 must not destabilize core WGMS workflows. It must satisfy the project’s relevant NFRs through concrete code/test/manual evidence.

## Regression checklist

### Existing feature regression

- [ ] FR-12 dashboard/current responsibilities remains functional.
- [ ] FR-13 chronological History remains functional.
- [ ] FR-03 task creation, completion, undo, filtering, and assignment remain functional.
- [ ] FR-04 cleaning schedule creation, update, activation, and recurrence remain functional.
- [ ] FR-05 shared expense creation and split calculation remain functional.
- [ ] FR-06 payment status overview and deterministic settlement remain functional.
- [ ] FR-07/FR-08 shopping item visibility and mark/unmark bought remain functional.
- [ ] FR-09 aggregated shopping list and shopping history remain functional.
- [ ] Login, registration, invitation, profile/GDPR workflows remain functional.
- [ ] No existing test was deleted, skipped, or weakened solely to make FR-16 pass.

### Required commands

```bash
cd backend
mvn test
```

```bash
cd frontend
npm test -- --watch=false
npm run build
```

```bash
cd e2e
npx cypress run
```

## NFR summary

| NFR | FR-16-specific check | Status | Evidence |
|---|---|---|---|
| NFR-01 Security & Access Control | `/statistics` and all history statistics endpoints require authentication/USER access; active household is derived backend-side; cross-household data is isolated. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-02 Data Privacy & GDPR | Statistics expose only current-household aggregates; shopping visibility rules exclude other users’ private items; no unnecessary raw private records are returned. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-03 Reliability | Aggregates derive consistently from persisted data; zero/empty/error states are safe; seed/demo data is reproducible; invalid custom ranges are blocked. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-04 Usability & Compatibility | Filters/charts are understandable; loading/empty/error states are clear; desktop and defined mobile viewports are usable; chart information is not color-only. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-05 Maintainability | Endpoint → service → repository/domain access → DTO layering is preserved; frontend uses existing HistoryService/DTO area; no unnecessary chart dependency or duplicated business logic. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-06 Auditability | Statistics do not remove timestamps, user attribution, categories, task status, or other source information needed to understand historical activity. | [ ] Pass [ ] Partial [ ] Fail |  |

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Backend command/result:
- Frontend command/result:
- E2E command/result:
- Pipeline result:
- Security/privacy checks:
- Reliability checks:
- Usability/accessibility checks:
- Maintainability checks:
- Auditability checks:
- Remaining issues:
```

---

# Final FR-16 QA Summary

| Area | Status | Notes |
|---|---|---|
| Statistics route/navigation and History separation | [ ] Pass [ ] Partial [ ] Fail |  |
| Date ranges, presets, grouping, and module filters | [ ] Pass [ ] Partial [ ] Fail |  |
| Overview statistics correctness | [ ] Pass [ ] Partial [ ] Fail |  |
| Task/cleaning statistics correctness | [ ] Pass [ ] Partial [ ] Fail |  |
| Expense/payment statistics correctness | [ ] Pass [ ] Partial [ ] Fail |  |
| Shopping statistics and visibility privacy | [ ] Pass [ ] Partial [ ] Fail |  |
| Security, authorization, and household scoping | [ ] Pass [ ] Partial [ ] Fail |  |
| API/DTO contract and request binding | [ ] Pass [ ] Partial [ ] Fail |  |
| Frontend SVG visualization/UI states | [ ] Pass [ ] Partial [ ] Fail |  |
| Responsive/accessibility/reference-device evidence | [ ] Pass [ ] Partial [ ] Fail |  |
| Demo data and E2E coverage | [ ] Pass [ ] Partial [ ] Fail |  |
| Existing tests/regression coverage | [ ] Pass [ ] Partial [ ] Fail |  |
| NFRs satisfied | [ ] Pass [ ] Partial [ ] Fail |  |

## Overall result

- [ ] FR-16 QA passed
- [ ] FR-16 QA partially passed with documented limitations
- [ ] FR-16 QA failed / blocking issues remain

## Remaining limitations

```text
- Outstanding split-settlement statistics are intentionally not implemented because payments are not persisted as linked to individual ExpenseSplit records.
- 
```

## Evidence links

```text
Backend implementation/tests:
- 

Frontend implementation/tests:
- 

E2E tests:
- 

Manual/reference-device checks:
- 

Seed/demo data:
- 

GitLab issue / merge request:
- 
```

## QA sign-off

```text
Reviewer:
Date:
Branch / commit:
Result:
```
