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

- [x] `/statistics` route exists and loads the Statistics page.
- [x] The route is protected by the existing authentication guard.
- [x] Unauthenticated direct navigation to `/statistics` redirects or is rejected according to existing routing rules.
- [x] Authenticated users can reach Statistics from the main application navigation.
- [x] The navigation item is configured through the existing `NAV_ITEMS`/equivalent navigation configuration rather than duplicated ad hoc.
- [x] Sidebar navigation contains the Statistics entry where applicable.
- [x] Mobile bottom navigation contains or correctly exposes the Statistics entry where applicable.
- [x] Mobile overflow navigation contains the Statistics entry where applicable.
- [x] Active navigation state is visible when the Statistics page is open.
- [x] Existing `/history` route still opens the chronological FR-13 History page.
- [x] Statistics page does not replace History with a tabbed or merged implementation unless this is the existing agreed design.
- [x] Browser refresh and direct URL navigation to `/statistics` work after login.
- [x] Login/logout transitions do not leave the Statistics page in a broken state.

## Test coverage evidence

- [x] Routing/component test covers Statistics route creation.
- [x] Auth guard test covers unauthenticated Statistics access.
- [x] Frontend navigation test covers desktop/mobile navigation expectations.
- [x] Cypress covers direct unauthenticated route access.
- [x] Cypress covers navigation from an authenticated session to `/statistics`.
- [x] Existing History route tests still pass.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Route files reviewed: `frontend/src/app/app-routing.module.ts` includes protected `/statistics` route; existing `/history` route remains separate.
- Navigation configuration reviewed: `frontend/src/app/global/nav-items.ts` contains a single `Statistics` entry and existing sidebar/bottom/mobile navigation consumes `NAV_ITEMS`.
- Frontend tests: `statistics.component.spec.ts`; updated `mobile-nav-menu.component.spec.ts`; existing `history.component.spec.ts` still passes.
- E2E route tests: `fr16-statistical-history-view.cy.js` covers unauthenticated redirect and authenticated navigation to Statistics.
- Remaining issues: none.
```

---

# FR16-02 — Date Range, Presets, Grouping, and Module Filters

## Requirement

Users must be able to inspect statistics over configurable time ranges. The frontend must validate custom ranges before requesting data. Grouping controls must support day, week, and month. Overview statistics must support repeated module filter query parameters.

## Checklist

### Date range and preset controls

- [x] Last 7 days preset exists.
- [x] Last 30 days preset exists.
- [x] Last 3 months preset exists.
- [x] Last 6 months preset exists.
- [x] Last 12 months preset exists.
- [x] Custom range mode exists.
- [x] Selecting a preset updates `from` and `to` consistently.
- [x] Custom range allows users to enter/select both `from` and `to`.
- [x] Invalid custom range where `from > to` is rejected in the UI.
- [x] Invalid custom range does not send a statistics request to the backend.
- [x] Missing required custom range dates are handled clearly.
- [x] Changing range refreshes all applicable statistics sections consistently.
- [x] Date formatting/timezone handling is consistent with project date conventions.

### Grouping controls

- [x] Day grouping exists.
- [x] Week grouping exists.
- [x] Month grouping exists.
- [x] Selected grouping is sent to relevant statistics requests.
- [x] Trend data changes grouping shape/labels consistently.
- [x] Grouping selection remains stable after data reload where expected.

### Overview module filters

- [x] Tasks module filter exists.
- [x] Cleaning module filter exists.
- [x] Expenses module filter exists.
- [x] Payments module filter exists.
- [x] Shopping module filter exists.
- [x] Multiple modules can be selected.
- [x] Selected modules are sent as repeated query parameters, not concatenated into an undocumented format.
- [x] Clearing filters restores the unfiltered overview behavior.
- [x] Module filters affect overview statistics only unless another endpoint explicitly supports them.
- [x] UI labels are understandable to a peer tester.

## Test coverage evidence

- [x] Frontend test covers each preset date calculation.
- [x] Frontend test covers invalid custom range with no backend request.
- [x] Frontend test covers grouping selection.
- [x] Frontend/service test verifies repeated module query parameters.
- [x] Backend test verifies date range filtering.
- [x] Backend test verifies day/week/month grouping.
- [x] Endpoint test verifies request parameter binding for `from`, `to`, `grouping`, and repeated `modules`.
- [x] Explicit `@RequestParam` names or equivalent binding protection are code-reviewed.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Filter components reviewed: `StatisticsComponent` implements presets, custom date range, grouping, and module selection.
- Request-building code reviewed: `HistoryService` sends `from`, `to`, `grouping`, and repeated `modules` parameters for overview.
- Backend request parameter binding reviewed: statistics endpoint methods use explicit `@RequestParam(name = ...)` bindings.
- Tests: frontend component/service tests cover preset behavior, invalid custom range, grouping, and repeated module query parameters; backend service/endpoint tests cover date filtering, grouping, and request binding.
- Remaining issues: none.
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

- [x] Completed task count is correct for the selected range.
- [x] Task completion rate uses the documented denominator and handles zero-task range safely.
- [x] Total recorded expense amount is correct for the selected range.
- [x] Recorded payment amount is correct for the selected range.
- [x] Purchased shopping item count is correct for the selected range.
- [x] Active cleaning schedule count is correct.
- [x] Activity-by-module values are correct.
- [x] Activity trend contains correct periods for selected grouping.
- [x] Module filters exclude unselected modules from overview values/trend according to documented behavior.
- [x] Empty household/range returns a valid zero/empty-shaped response.
- [x] Date-boundary records are included/excluded consistently.
- [x] Monetary values preserve existing financial precision/rounding behavior.
- [x] Response does not include unnecessary raw source records.

## Backend test evidence

- [x] Unit test for overview with mixed task/expense/payment/shopping/cleaning data.
- [x] Unit test for zero-data/empty household response.
- [x] Unit test for date boundaries.
- [x] Unit test for module filtering.
- [x] Unit test for day/week/month overview trend.
- [x] Integration test for overview endpoint response mapping.
- [x] Integration test for repeated `modules` query parameters.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Overview service reviewed: `HistoryStatisticsServiceImpl#getStatisticsOverview` aggregates task, cleaning, expense, payment, and shopping data scoped to the active household.
- DTO mapping reviewed: `StatisticsOverviewDto`, category values, member values, and series DTOs expose aggregate data only.
- Unit tests: overview mixed-data, empty-data, date-boundary, module-filter, and grouping cases covered in `HistoryStatisticsServiceTest`.
- Endpoint tests: overview response mapping and repeated `modules` parameter binding covered in `HistoryStatisticsEndpointTest`.
- Remaining issues: none.
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

- [x] Total task count is correct for selected range.
- [x] Completed task count is derived from current/recorded task status consistently.
- [x] Cancelled task count is correct.
- [x] Overdue task count follows existing due-date/status rules.
- [x] Completion rate handles zero tasks safely.
- [x] Task status distribution sums consistently with total task count.
- [x] Cleaning frequency distribution reflects existing `CleaningSchedule` metadata.
- [x] Schedule-generated tasks are identified only through valid schedule linkage.
- [x] Schedule-generated completion rate is correct.
- [x] Task completion trend respects date range and grouping.
- [x] Completed tasks by member excludes unrelated household users.
- [x] Completed tasks by weekday are correctly grouped.
- [x] Undo/reopen behavior is reflected according to current persisted task state/history rules.
- [x] Existing task creation, completion, undo, filtering, and recurrence behavior remains unchanged.

## Backend test evidence

- [x] Unit test for task status distribution.
- [x] Unit test for overdue tasks.
- [x] Unit test for task completion rate.
- [x] Unit test for cleaning frequency distribution.
- [x] Unit test for schedule-generated task count/completion rate.
- [x] Unit test for member and weekday grouping.
- [x] Unit test for current-state behavior after task completion undo where model supports it.
- [x] Integration test for task statistics endpoint.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Task/cleaning statistics service reviewed: task metrics are derived from existing `Task` status, dates, assignment, and cleaning-schedule linkage.
- Task/CleaningSchedule repositories reviewed: existing repositories are used; no task/recurrence behavior changed.
- Unit tests: status distribution, overdue tasks, completion rate, cleaning frequency distribution, schedule-generated metrics, member grouping, and weekday grouping covered.
- Endpoint tests: task statistics endpoint response and request binding covered.
- Regression tests: existing task/history tests still pass.
- Remaining issues: none.
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

- [x] Expense count is correct for selected range.
- [x] Total expense amount is correct.
- [x] Average expense amount is correct and handles zero expenses safely.
- [x] Largest expense amount is correct and handles zero expenses safely.
- [x] Payment count is correct.
- [x] Total recorded payment amount is correct.
- [x] Expense and payment trends are separate and correctly grouped.
- [x] Expenses paid by member are attributed to the correct payer.
- [x] Payments sent by member are attributed to the correct sender.
- [x] Payments received by member are attributed to the correct receiver.
- [x] Amount calculations use existing `BigDecimal`/rounding conventions where applicable.
- [x] Deleted/anonymized user display is handled consistently with the finance module.
- [x] Other-household expenses/payments never influence the response.
- [x] UI/documentation does not falsely claim that payments settle specific ExpenseSplit records.
- [x] Absence of outstanding split-settlement statistics is documented as an accepted scope limitation.

## Backend test evidence

- [x] Unit test for expense count/total/average/largest amount.
- [x] Unit test for payment count/total.
- [x] Unit test for expense trend.
- [x] Unit test for payment trend.
- [x] Unit test for payer/sender/receiver member breakdowns.
- [x] Unit test for zero-data behavior.
- [x] Unit test for financial rounding edge values where applicable.
- [x] Integration test for expenses statistics endpoint.
- [x] Cross-household finance data isolation test.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Expense/payment statistics service reviewed: expenses and payments are aggregated separately from existing `Expense` and `Payment` data.
- DTO mapping reviewed: `ExpenseStatisticsDto` exposes count, total, average, largest, trends, payer breakdown, and payment sender/receiver breakdown.
- Unit tests: expense totals, averages, largest amount, payment totals, trends, member breakdowns, zero-data behavior, and isolation are covered.
- Endpoint tests: expenses statistics endpoint response mapping and request binding covered.
- Scope limitation evidence: no outstanding split-settlement chart was implemented because `Payment` is not persisted as linked to individual `ExpenseSplit` records.
- Remaining issues: none.
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

- [x] Added item count is correct for visible items in selected range.
- [x] Purchased item count is correct.
- [x] Purchase completion rate handles zero added items safely.
- [x] Status distribution is correct.
- [x] Visibility distribution is correct for data legitimately visible to the current user.
- [x] Shopping activity trend respects date range and grouping.
- [x] Most frequently purchased items are built only from bought items.
- [x] Purchased item-name grouping uses normalized item names consistently.
- [x] Items added by member are attributed correctly for visible records.
- [x] Current user’s personal items are included.
- [x] Items targeted at current user are included.
- [x] Shared household items are included.
- [x] Other users’ personal/private items are excluded.
- [x] Other-household shopping items are excluded.
- [x] Shopping stats do not reveal private item names/counts through aggregates, bar lists, charts, or tooltips.
- [x] Mark/unmark bought and shopping history behavior remains unchanged.

## Backend test evidence

- [x] Unit test for added/purchased counts and completion rate.
- [x] Unit test for status/visibility distribution.
- [x] Unit test for normalized most-frequently-purchased item grouping.
- [x] Unit test for own personal item inclusion.
- [x] Unit test for targeted item inclusion.
- [x] Unit test for shared item inclusion.
- [x] Unit test for exclusion of other users’ private items.
- [x] Unit test for other-household exclusion.
- [x] Integration test for shopping statistics endpoint.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Shopping statistics service reviewed: shopping metrics aggregate visible `ShoppingItem` records only.
- Visibility logic reviewed: current user personal items, targeted items, and shared items are included; other users’ personal/private items are excluded.
- Unit tests: added/purchased counts, completion rate, distributions, normalized item grouping, own personal inclusion, targeted inclusion, shared inclusion, other-private exclusion, and other-household exclusion covered.
- Endpoint tests: shopping statistics endpoint response mapping covered.
- Privacy evidence: private shopping item names/counts from other users are not exposed in aggregates, rankings, charts, or tooltips.
- Remaining issues: none.
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

- [x] All four statistics endpoints exist or equivalent documented paths exist.
- [x] Each endpoint requires authentication and USER-role access.
- [x] Unauthenticated requests are rejected.
- [x] Current application user is resolved from Authentication/JWT email/identity.
- [x] Active roommate membership is resolved backend-side.
- [x] Active household is resolved backend-side.
- [x] Frontend does not send householdId for authorization.
- [x] Backend does not accept/trust frontend householdId if attempted.
- [x] Cross-household tasks do not influence statistics.
- [x] Cross-household cleaning schedules do not influence statistics.
- [x] Cross-household expenses/payments do not influence statistics.
- [x] Cross-household shopping items do not influence statistics.
- [x] Response DTOs expose aggregates only and avoid unnecessary raw records.
- [x] Response DTO naming follows existing history API patterns.
- [x] Endpoint request parameters use explicit names/binding where required.
- [x] Invalid request values return clear validation errors or safe documented behavior.
- [x] OpenAPI/Swagger documentation is updated if the project exposes it.

## Security/integration test evidence

- [x] Unauthenticated test for every endpoint or representative shared security configuration test exists.
- [x] Authenticated current-household overview test exists.
- [x] Cross-household data isolation test exists for each statistical domain.
- [x] Request parameter binding test exists.
- [x] Response mapping test exists.
- [x] Test verifies no frontend householdId is needed/trusted.
- [x] Shopping private-item leakage test exists.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Endpoint files reviewed: `HistoryStatisticsEndpoint` exposes overview, tasks, expenses, and shopping statistics under `/api/v1/history/statistics`.
- Auth/current-user resolution reviewed: endpoints require `ROLE_USER`; service resolves current user and active household backend-side from authenticated identity.
- DTO files reviewed: statistics DTOs expose aggregates and avoid unnecessary raw records.
- Security/integration tests: unauthenticated access, authenticated household access, parameter binding, response mapping, cross-household isolation, and shopping privacy cases covered.
- OpenAPI evidence: endpoint documentation avoids the misleading `apiKey` security annotation on the new endpoints.
- Remaining issues: none.
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

- [x] Overview section is visible.
- [x] Task statistics section is visible.
- [x] Expense/payment statistics section is visible.
- [x] Shopping statistics section is visible.
- [x] Line chart renders trend data correctly.
- [x] Donut chart renders distribution data correctly.
- [x] Bar list renders member/category/item values correctly.
- [x] Donut chart uses flat arc ends rather than rounded caps, where this is the agreed presentational design.
- [x] No external charting dependency was introduced unless explicitly approved.
- [x] Monetary values are formatted consistently.
- [x] Percentages are formatted consistently.
- [x] Period labels are understandable for day/week/month groupings.
- [x] Chart legends/labels match displayed values.
- [x] Important values are available as text/cards/lists, not only through color or hover behavior.

### Loading, empty, stale-data, and error behavior

- [x] Loading state is visible during initial request.
- [x] Loading state is handled when filters change.
- [x] Empty household/range shows a clear empty state.
- [x] Error state is visible when request fails.
- [x] Existing/stale data is cleared or clearly prevented from being mistaken for current data after a failed refresh.
- [x] One failed section does not leave the entire page unusable where independent requests are intended.
- [x] Retry/refresh behavior is understandable where implemented.

### Usability and accessibility

- [x] Sections have clear headings.
- [x] Controls have visible labels/accessibility names.
- [x] Keyboard navigation works for presets, custom dates, grouping, and module filters.
- [x] Focus state is visible for interactive controls.
- [x] Color is not the only way to distinguish chart data/status.
- [x] Empty/error states explain what users can do next.
- [x] Screen-reader-friendly text/labels exist for SVG charts where reasonably implemented.
- [x] Page can be understood by a peer tester without external explanation.

### Responsive/reference platform checks

- [x] Desktop layout is usable at 1366 × 768 and higher.
- [x] Small mobile boundary at 360 CSS px remains usable.
- [x] iPhone 12 Pro-like viewport around 390 × 844 is usable.
- [x] Pixel 7-like viewport around 412 × 915 is usable.
- [x] Charts/lists do not require horizontal scrolling at target widths.
- [x] Filter controls remain reachable on mobile.
- [x] Navigation remains usable on mobile.
- [x] No important chart/control/action is clipped or hidden.

## Frontend test evidence

- [x] Statistics page creation test exists.
- [x] Initial statistics loading test exists.
- [x] Successful overview/task/expense/shopping response rendering tests exist.
- [x] Error handling test exists.
- [x] Stale-data clearing-after-failure test exists.
- [x] Preset filter behavior test exists.
- [x] Custom date validation test exists.
- [x] Grouping selection test exists.
- [x] Module selection/repeated query parameter test exists.
- [x] Formatting helper tests exist.
- [x] Line chart rendering logic test exists.
- [x] Donut chart rendering logic test exists.
- [x] Bar list rendering logic test exists.
- [x] Mobile navigation expectation updates are tested where navigation changed.
- [x] Existing History component tests still pass.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Statistics components reviewed: `StatisticsComponent` renders overview, tasks, expenses/payments, shopping, filters, loading, empty, and error states.
- SVG chart components reviewed: line chart, donut chart, and bar list components render in-house SVG/list views; donut arcs use flat caps.
- Dependency/package review: no external charting dependency was added.
- Frontend tests: statistics page, service request behavior, formatting helpers, stale-data clearing, chart components, and mobile navigation tests covered.
- Reference viewport checks: Cypress responsive checks cover narrow mobile and desktop behavior without horizontal overflow.
- Accessibility evidence: important values are also rendered as cards/lists/text, not only as color or hover-only SVG information.
- Remaining issues: none.
```

---

# FR16-09 — Demo/Test Data, Cypress E2E, and Reliability Evidence

## Requirement

FR-16 must be testable with deterministic data that demonstrates non-empty, empty, filtered, and privacy-sensitive statistical outcomes. Cypress must verify the user-facing workflow without relying only on visual inspection.

## Demo/test data checklist

- [x] Test data includes tasks in multiple statuses.
- [x] Test data includes completed, cancelled, open, and overdue tasks where required by visible metrics.
- [x] Test data includes schedule-generated tasks and cleaning schedules where schedule metrics are displayed.
- [x] Test data includes expenses with different values/dates/payers.
- [x] Test data includes payments with different senders/receivers/dates.
- [x] Test data includes shopping items with NEEDED/BOUGHT/CANCELLED or applicable statuses.
- [x] Test data includes SHARED, PERSONAL, and TARGETED shopping visibility scenarios.
- [x] Test data includes another household for cross-household isolation evidence.
- [x] Test data includes another user’s private shopping item for privacy-exclusion evidence.
- [x] Test/demo profile is isolated from production data.
- [x] Seed/data generator is idempotent or test setup is isolated.

## Minimum Cypress E2E coverage

- [x] Unauthenticated navigation to `/statistics` redirects/rejects.
- [x] Authenticated user reaches Statistics page through navigation.
- [x] Overview statistics render from backend-backed data.
- [x] Completed task statistics render.
- [x] Expense statistics render.
- [x] Shopping statistics render.
- [x] Invalid custom date range is blocked in UI and does not send invalid request.
- [x] Module filter request behavior is verified.
- [x] Empty household/range state is verified.
- [x] Private shopping item data is not displayed to unauthorized household member.
- [x] Responsive viewport test verifies no horizontal overflow.
- [x] Stable selectors (`data-cy`) are used for critical interactions.
- [x] Test does not depend on fragile CSS class selectors alone.

## Commands

```bash
cd e2e
npx cypress run --spec "cypress/e2e/fr16-statistical-history-view.cy.js"
```

```bash
cd e2e
npx cypress run
```

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Seed/demo data: updated `DemoDataGenerator` creates 12 months of statistics-friendly tasks, cleaning schedules, expenses, payments, and shopping data.
- Cypress spec(s): `e2e/cypress/e2e/fr16-statistical-history-view.cy.js`.
- Targeted E2E command/result: `npx cypress run --spec "cypress/e2e/fr16-statistical-history-view.cy.js"` passed.
- Full E2E command/result: `npx cypress run` passed after feature integration.
- Responsive evidence: Cypress verifies no horizontal overflow at reference mobile viewport.
- Privacy E2E evidence: statistics do not display another user’s private shopping item data.
- Remaining issues: none.
```

---

# FR16-10 — Regression Coverage and Non-Functional Requirements Summary

## Requirement

FR-16 must not destabilize core WGMS workflows. It must satisfy the project’s relevant NFRs through concrete code/test/manual evidence.

## Regression checklist

### Existing feature regression

- [x] FR-12 dashboard/current responsibilities remains functional.
- [x] FR-13 chronological History remains functional.
- [x] FR-03 task creation, completion, undo, filtering, and assignment remain functional.
- [x] FR-04 cleaning schedule creation, update, activation, and recurrence remain functional.
- [x] FR-05 shared expense creation and split calculation remain functional.
- [x] FR-06 payment status overview and deterministic settlement remain functional.
- [x] FR-07/FR-08 shopping item visibility and mark/unmark bought remain functional.
- [x] FR-09 aggregated shopping list and shopping history remain functional.
- [x] Login, registration, invitation, profile/GDPR workflows remain functional.
- [x] No existing test was deleted, skipped, or weakened solely to make FR-16 pass.

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
| NFR-01 Security & Access Control | `/statistics` and all history statistics endpoints require authentication/USER access; active household is derived backend-side; cross-household data is isolated. | [x] Pass [ ] Partial [ ] Fail |  |
| NFR-02 Data Privacy & GDPR | Statistics expose only current-household aggregates; shopping visibility rules exclude other users’ private items; no unnecessary raw private records are returned. | [x] Pass [ ] Partial [ ] Fail |  |
| NFR-03 Reliability | Aggregates derive consistently from persisted data; zero/empty/error states are safe; seed/demo data is reproducible; invalid custom ranges are blocked. | [x] Pass [ ] Partial [ ] Fail |  |
| NFR-04 Usability & Compatibility | Filters/charts are understandable; loading/empty/error states are clear; desktop and defined mobile viewports are usable; chart information is not color-only. | [x] Pass [ ] Partial [ ] Fail |  |
| NFR-05 Maintainability | Endpoint → service → repository/domain access → DTO layering is preserved; frontend uses existing HistoryService/DTO area; no unnecessary chart dependency or duplicated business logic. | [x] Pass [ ] Partial [ ] Fail |  |
| NFR-06 Auditability | Statistics do not remove timestamps, user attribution, categories, task status, or other source information needed to understand historical activity. | [x] Pass [ ] Partial [ ] Fail |  |

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Backend command/result: `cd backend && mvn test` passed.
- Frontend command/result: `cd frontend && npm test -- --watch=false` and `npm run build` passed.
- E2E command/result: `cd e2e && npx cypress run` passed; targeted FR-16 spec also passed.
- Pipeline result: feature pipeline passed after integration; unrelated transient Maven Central 429 failure on dev was identified as dependency-resolution infrastructure, not FR-16 code.
- Security/privacy checks: endpoints require authenticated USER access; backend derives active household; shopping private-item visibility is enforced.
- Reliability checks: invalid custom ranges are blocked; zero/empty/error states are safe; demo data is reproducible/idempotent.
- Usability/accessibility checks: filters, charts, text summaries, loading, empty, and error states verified on desktop/mobile reference viewports.
- Maintainability checks: existing endpoint-service-repository/DTO layering preserved; frontend uses existing `HistoryService`; no external chart dependency added.
- Auditability checks: timestamps, task status, member attribution, categories, and financial aggregates remain traceable to existing source records.
- Remaining issues: none.
```

---

# Final FR-16 QA Summary

| Area | Status | Notes |
|---|---|---|
| Statistics route/navigation and History separation | [x] Pass [ ] Partial [ ] Fail |  |
| Date ranges, presets, grouping, and module filters | [x] Pass [ ] Partial [ ] Fail |  |
| Overview statistics correctness | [x] Pass [ ] Partial [ ] Fail |  |
| Task/cleaning statistics correctness | [x] Pass [ ] Partial [ ] Fail |  |
| Expense/payment statistics correctness | [x] Pass [ ] Partial [ ] Fail |  |
| Shopping statistics and visibility privacy | [x] Pass [ ] Partial [ ] Fail |  |
| Security, authorization, and household scoping | [x] Pass [ ] Partial [ ] Fail |  |
| API/DTO contract and request binding | [x] Pass [ ] Partial [ ] Fail |  |
| Frontend SVG visualization/UI states | [x] Pass [ ] Partial [ ] Fail |  |
| Responsive/accessibility/reference-device evidence | [x] Pass [ ] Partial [ ] Fail |  |
| Demo data and E2E coverage | [x] Pass [ ] Partial [ ] Fail |  |
| Existing tests/regression coverage | [x] Pass [ ] Partial [ ] Fail |  |
| NFRs satisfied | [x] Pass [ ] Partial [ ] Fail |  |

## Overall result

- [x] FR-16 QA passed
- [ ] FR-16 QA partially passed with documented limitations
- [ ] FR-16 QA failed / blocking issues remain

## Remaining limitations

```text
- Outstanding split-settlement statistics are intentionally not implemented because payments are not persisted as linked to individual ExpenseSplit records.
```

## Evidence links

```text
Backend implementation/tests:
- HistoryStatisticsEndpoint.java
- HistoryStatisticsService.java
- HistoryStatisticsServiceImpl.java
- Statistics DTOs under endpoint/dto
- HistoryStatisticsServiceTest.java
- HistoryStatisticsEndpointTest.java

Frontend implementation/tests:
- statistics.component.ts/html/scss
- statistics-line-chart component
- statistics-donut-chart component
- statistics-bar-list component
- history.service.ts statistics API methods
- history.service.spec.ts
- statistics.component.spec.ts
- chart component specs
- mobile-nav-menu.component.spec.ts

E2E tests:
- e2e/cypress/e2e/fr16-statistical-history-view.cy.js

Manual/reference-device checks:
- Desktop 1366x768+
- Mobile 360 CSS px
- iPhone 12 Pro-like 390x844
- Pixel 7-like 412x915

Seed/demo data:
- DemoDataGenerator.java updated for 12-month statistics-friendly data

GitLab issue / merge request:
- FR-16 Statistical History View issue / merge request
```

## QA sign-off

```text
Reviewer: Taras Trishch
Date: 2026-06-24
Branch / commit: FR-16/Statistical-history-view / 4a04745c050cfc2cfc674e5bd00764b17a43a6fe
Result: Passed
```
