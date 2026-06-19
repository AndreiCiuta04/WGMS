# WGMS — FR-16 Statistical History View QA Form

## Scope

This QA form checks **FR-16 — Statistical history view**.

The goal is to verify that WGMS provides meaningful statistical visualizations based on existing household data, especially:

- pie charts / category breakdowns for costs such as food, electricity, operating costs, and other expense categories
- expense development over time
- shopping history visualizations where implemented
- user contribution statistics, such as who bought most
- correct household scoping
- stable dashboard/history behavior without breaking existing FR-12 and FR-13 flows

This QA form does **not** check predictive stock forecasting, because stock prediction is out of scope. The view should remain a historical/statistical dashboard, not a prediction feature.

---

# FR16-01 — Feature Scope and Existing Workflow Preservation

## Requirement

FR-16 must extend the dashboard/history functionality with statistical history visualizations without breaking existing dashboard, history, expense, payment, shopping, login, or household workflows.

## Checklist

- [ ] Statistical history view is reachable from the dashboard/history area.
- [ ] The feature is presented as historical/statistical visualization, not prediction.
- [ ] Existing dashboard/current responsibilities view still works.
- [ ] Existing task history view still works.
- [ ] Existing payment/settlement history view still works.
- [ ] Existing shopping history view still works where implemented.
- [ ] Existing expense creation and display still work.
- [ ] Existing payment recording/settlement flows still work.
- [ ] Existing shopping item add/mark bought/history flows still work.
- [ ] No existing route was removed without replacement.
- [ ] No unrelated workflow was changed only to support FR-16.

## Test coverage evidence

- [ ] Existing dashboard tests still pass.
- [ ] Existing history tests still pass.
- [ ] Existing expense/payment tests still pass.
- [ ] Existing shopping tests still pass.
- [ ] No existing tests were deleted or weakened.
- [ ] Updated tests still assert functional outcomes, not only that charts render.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Backend files reviewed:
- Frontend files reviewed:
- Existing tests reviewed:
- Existing tests preserved:
- Remaining issues:
```

---

# FR16-02 — Backend Data Aggregation Correctness

## Requirement

The backend must calculate statistical history data correctly from persisted household data. Aggregations must be household-scoped and must not rely on frontend-only calculations for critical values.

## Checklist

- [ ] Expense category totals are calculated correctly.
- [ ] Food spending is included in the correct category.
- [ ] Electricity spending is included in the correct category.
- [ ] Operating costs are included in the correct category.
- [ ] Unknown/other categories are handled consistently.
- [ ] Expense development over time is calculated correctly.
- [ ] Time-series buckets are correct for the selected period.
- [ ] User contribution statistics are calculated correctly.
- [ ] “Who bought most” is based on actual shopping/expense records.
- [ ] Shopping history statistics are calculated correctly where implemented.
- [ ] Deleted/anonymized users are handled consistently.
- [ ] Cancelled/deleted expenses or shopping records are excluded if the model supports such status.
- [ ] Amounts use the same numeric/rounding rules as the expense module.
- [ ] Empty datasets return a valid empty response instead of failing.
- [ ] Date filters are applied correctly.
- [ ] Invalid date ranges are rejected with clear validation errors.
- [ ] Backend returns DTOs suitable for charting without exposing unnecessary raw records.

## Backend test evidence

- [ ] Unit test for expense category aggregation.
- [ ] Unit test for time-series aggregation.
- [ ] Unit test for user contribution/top buyer calculation.
- [ ] Unit test for empty household/no-data behavior.
- [ ] Unit test for deleted/anonymized user handling.
- [ ] Unit test for invalid date range.
- [ ] Repository test for household-scoped expense retrieval.
- [ ] Repository test for household-scoped shopping history retrieval where applicable.
- [ ] Integration test for full statistical history endpoint response.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Statistics service reviewed:
- Repository methods reviewed:
- DTOs reviewed:
- Unit tests:
- Repository tests:
- Integration tests:
- Remaining issues:
```

---

# FR16-03 — Security, Authorization, and Household Scoping

## Requirement

FR-16 must only expose statistical data for the authenticated user’s current household. The frontend must not be trusted for household access.

## Checklist

- [ ] Statistical history endpoint requires authentication.
- [ ] Unauthenticated requests are rejected.
- [ ] Current user is derived from Authentication/JWT.
- [ ] Current household is derived backend-side from the authenticated user’s active roommate record.
- [ ] Backend does not accept or trust frontend-supplied householdId.
- [ ] Users cannot view statistics for another household.
- [ ] Other-household expenses do not affect category totals.
- [ ] Other-household shopping records do not affect shopping statistics.
- [ ] Other-household users do not appear in user contribution statistics.
- [ ] Response exposes aggregate statistics, not unnecessary raw personal data.
- [ ] Role behavior is consistent with existing dashboard/history access rules.
- [ ] Backend authorization is not replaced by frontend hiding.

## Security/integration test evidence

- [ ] Unauthenticated endpoint test exists.
- [ ] Authenticated current-household endpoint test exists.
- [ ] Cross-household leakage test exists.
- [ ] Test verifies frontend householdId is not trusted or not accepted.
- [ ] Test verifies only current-household users appear in statistics.
- [ ] Test verifies only current-household records affect totals.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Endpoint reviewed:
- Auth/current-user resolution reviewed:
- Household scoping tests:
- Security tests:
- Remaining issues:
```

---

# FR16-04 — API Contract and DTO Quality

## Requirement

FR-16 must expose a clear backend API for statistical history data. DTOs should be stable, documented, and practical for chart rendering.

## Checklist

- [ ] Endpoint path follows existing dashboard/history naming conventions.
- [ ] Endpoint returns category breakdown data.
- [ ] Endpoint returns time-series data.
- [ ] Endpoint returns user contribution/top buyer data.
- [ ] Endpoint returns shopping history statistics where implemented.
- [ ] DTOs use clear field names.
- [ ] DTOs avoid leaking raw task/expense/shopping details unnecessarily.
- [ ] Date range query parameters are documented.
- [ ] Default date range is documented or obvious.
- [ ] Invalid parameters return clear validation errors.
- [ ] Empty response shape is stable and frontend-safe.
- [ ] OpenAPI/Swagger documentation is updated if applicable.
- [ ] Angular service consumes the DTO without duplicating backend calculations.

## Suggested DTO evidence

```text
StatisticalHistoryDto:
- categoryBreakdown:
- expenseDevelopment:
- userContributions:
- topBuyer:
- shoppingHistory:
- dateRange:
```

```text
CategoryBreakdownDto:
- category:
- totalAmount:
- percentage:
```

```text
TimeSeriesPointDto:
- period:
- totalAmount:
```

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Endpoint path:
- DTO files:
- API docs:
- Validation behavior:
- Remaining issues:
```

---

# FR16-05 — Frontend Visualization and UI/UX

## Requirement

The frontend must present the statistical history data clearly, responsively, and consistently with the existing WGMS UI.

## Checklist

- [ ] Statistical history page/section is reachable from dashboard/history navigation.
- [ ] Expense category breakdown chart is visible.
- [ ] Expense development over time chart is visible.
- [ ] User contribution/top buyer visualization is visible.
- [ ] Shopping history visualization is visible where implemented.
- [ ] Chart labels are understandable.
- [ ] Monetary values are formatted consistently.
- [ ] Empty state is shown when there is no data.
- [ ] Loading state is shown while data is loading.
- [ ] Error state is shown if data loading fails.
- [ ] Date range/filter controls are usable where implemented.
- [ ] Charts do not overflow horizontally.
- [ ] UI remains readable on desktop.
- [ ] UI remains usable on mobile-sized viewports.
- [ ] Charts are not the only source of information; key values are also visible as text/table/cards where reasonable.
- [ ] No predictive wording such as “forecast” or “prediction” is used.
- [ ] The UI style matches the existing dashboard/history design.

## Accessibility/usability evidence

- [ ] Chart sections have headings.
- [ ] Important values are available as text.
- [ ] Color is not the only way to understand chart meaning.
- [ ] Tooltips or labels are readable.
- [ ] Keyboard navigation works for filters.
- [ ] Peer tester can understand the main statistics without explanation.
- [ ] Lighthouse or WAI-style check was run for representative page if available.

## Frontend test evidence

- [ ] Component test verifies charts/sections render with data.
- [ ] Component test verifies empty state.
- [ ] Component test verifies loading/error state.
- [ ] Component test verifies filter/date range behavior where implemented.
- [ ] Angular service test verifies API call.
- [ ] Existing dashboard/history tests still pass.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Frontend components reviewed:
- Chart library/components reviewed:
- CSS/style changes:
- Component/service tests:
- Accessibility/usability notes:
- Remaining issues:
```

---

# FR16-06 — Performance and Reliability

## Requirement

FR-16 must remain usable when histories contain many records. It must avoid slow or cluttered views and must not load excessive unrelated data.

## Checklist

- [ ] Backend aggregation avoids loading unrelated household data.
- [ ] Date range filtering exists or a reasonable default range is used.
- [ ] Large histories are handled through aggregation, pagination, or bounded queries.
- [ ] Expensive database queries are scoped by household and date.
- [ ] Frontend does not render thousands of raw records inside charts.
- [ ] Chart rendering remains responsive with generated test data.
- [ ] Refreshing the page does not break the view.
- [ ] Repeated navigation to/from the page does not duplicate subscriptions or requests.
- [ ] Seed/demo data exists for meaningful statistical history verification.
- [ ] Demo data is reproducible and documented.

## Test evidence

- [ ] Backend test with multiple categories.
- [ ] Backend test with multiple months/weeks/days.
- [ ] Backend test with larger generated dataset if feasible.
- [ ] Frontend test with non-trivial chart dataset.
- [ ] Manual or E2E smoke test with demo data.
- [ ] No severe console errors during chart rendering.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Large/demo dataset:
- Query/repository checks:
- Frontend rendering checks:
- Remaining issues:
```

---

# FR16-07 — Demo/Test Data

## Requirement

FR-16 should have deterministic demo/test data that makes statistical values easy to verify manually and through tests.

## Checklist

- [ ] Dedicated FR-16 demo household exists or shared dashboard demo household is documented.
- [ ] Demo users/credentials are documented.
- [ ] Demo data includes multiple expense categories.
- [ ] Demo data includes food expenses.
- [ ] Demo data includes electricity expenses.
- [ ] Demo data includes operating costs.
- [ ] Demo data includes at least two months/weeks/days for time-series verification.
- [ ] Demo data includes multiple buyers/users for contribution statistics.
- [ ] Demo data includes shopping history where implemented.
- [ ] Demo data includes an empty/no-data household if feasible.
- [ ] Expected totals are documented.
- [ ] Expected top buyer/contributor is documented.
- [ ] Seed data is idempotent.
- [ ] Seed data is active only in dev/test/demo profile.

## Manual verification flow

- [ ] Start backend with demo/test data profile.
- [ ] Start frontend.
- [ ] Login with documented demo account.
- [ ] Open statistical history view.
- [ ] Verify expected category totals.
- [ ] Verify expected time-series points.
- [ ] Verify expected top buyer/contributor.
- [ ] Verify empty state where applicable.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Seed/datagen file:
- Demo users:
- Expected values:
- Manual verification result:
- Remaining issues:
```

---

# FR16-08 — E2E Coverage

## Requirement

At least one Cypress E2E test should verify the full user-facing statistical history workflow.

## Minimum E2E scenario

- [ ] Login as demo/test user.
- [ ] Use or create household with expense/shopping history.
- [ ] Navigate to dashboard/statistical history view.
- [ ] Verify category breakdown section appears.
- [ ] Verify expense development section appears.
- [ ] Verify user contribution/top buyer section appears.
- [ ] Verify at least one known demo value is displayed.
- [ ] Verify no-data/empty state in a separate household if feasible.
- [ ] Verify date range/filter changes update the displayed data if implemented.
- [ ] Verify route is protected from unauthenticated access.
- [ ] Verify responsive viewport smoke test for at least one mobile size.
- [ ] Use stable selectors such as `data-cy`.

## Command

```bash
cd e2e
npx cypress run --spec "cypress/e2e/fr16-statistical-history.cy.js"
```

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- E2E spec:
- E2E command/result:
- Test data used:
- Pipeline result:
- Remaining issues:
```

---

# FR16-09 — Regression Coverage

## Requirement

FR-16 must not break existing WGMS workflows.

## Checklist

### Feature regression

- [ ] Dashboard/current responsibilities still pass.
- [ ] Task history still passes.
- [ ] Payment history still passes.
- [ ] Shopping history still passes.
- [ ] Expense creation still passes.
- [ ] Payment/settlement overview still passes.
- [ ] Shopping add/mark bought still passes.

### Wider regression

- [ ] Login/registration tests still pass.
- [ ] WG creation/invitation tests still pass.
- [ ] Task/cleaning schedule tests still pass.
- [ ] Expense/payment tests still pass.
- [ ] Shopping tests still pass.
- [ ] GDPR/profile/legal tests still pass where implemented.
- [ ] No failing test was skipped or deleted to make CI green.

## Required commands

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
- Existing tests changed:
- Existing tests deleted/replaced:
- Remaining issues:
```

---

# FR16-10 — Non-Functional Requirements Summary

| NFR | FR-16-specific check | Status | Evidence |
|---|---|---|---|
| NFR-01 Security & Access Control | Statistical history endpoint requires login; household is derived backend-side; no cross-household statistics leak. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-02 Data Privacy & GDPR | Response exposes aggregate history/statistics only; unnecessary raw personal data is not exposed; deleted/anonymized users are handled consistently. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-03 Reliability | Statistical data is persisted/derived from database records; seed/demo data is reproducible; empty and large datasets behave safely. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-04 Usability & Compatibility | Charts are understandable, accessible enough for demo scope, and usable on reference desktop/mobile viewports. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-05 Maintainability | Aggregation logic is backend-side/service-side; frontend only renders DTOs; code follows existing DTO/service/repository patterns. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-06 Auditability | FR-16 does not remove timestamps/responsible-user/category information needed to interpret historical actions. | [ ] Pass [ ] Partial [ ] Fail |  |

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Security/privacy checks:
- Reliability checks:
- Usability/accessibility checks:
- Maintainability checks:
- Auditability checks:
```

---

# Final FR-16 QA Summary

| Area | Status | Notes |
|---|---|---|
| Feature scope preserved | [ ] Pass [ ] Partial [ ] Fail |  |
| Backend aggregation correct | [ ] Pass [ ] Partial [ ] Fail |  |
| Household scoping/security correct | [ ] Pass [ ] Partial [ ] Fail |  |
| API/DTO contract clear | [ ] Pass [ ] Partial [ ] Fail |  |
| Frontend charts/UI work | [ ] Pass [ ] Partial [ ] Fail |  |
| Responsiveness/accessibility checked | [ ] Pass [ ] Partial [ ] Fail |  |
| Performance/reliability acceptable | [ ] Pass [ ] Partial [ ] Fail |  |
| Demo/test data sufficient | [ ] Pass [ ] Partial [ ] Fail |  |
| Backend tests sufficient | [ ] Pass [ ] Partial [ ] Fail |  |
| Frontend tests sufficient | [ ] Pass [ ] Partial [ ] Fail |  |
| E2E coverage sufficient | [ ] Pass [ ] Partial [ ] Fail |  |
| Existing tests still pass | [ ] Pass [ ] Partial [ ] Fail |  |
| NFRs satisfied | [ ] Pass [ ] Partial [ ] Fail |  |

## Overall result

- [ ] FR-16 QA passed
- [ ] FR-16 QA partially passed with documented limitations
- [ ] FR-16 QA failed / blocking issues remain

## Remaining limitations

```text
- 
```

## Evidence links

```text
Backend tests:
- 

Frontend tests:
- 

E2E tests:
- 

Manual verification:
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
