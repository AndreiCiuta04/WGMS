# FR-06 QA Summary - Payment Status Overview

Sources:
- `docs/FR-06_general_QA.md`
- `backend/QA_docs_backend/FR-06_backend_QA.md`
- `frontend/QA_docs_frontend/FR-06_frontend_QA.md`

## Feature Information

- Feature ID: FR-06
- Feature name: Payment Status Overview
- Use case: UC-03 - Shared Expense Tracking and Settlement
- Priority: High
- Category: Core
- Planned version: V3
- Dependency: FR-05 Shared expense tracking
- Backend technology: Java Spring Boot
- Frontend technology: Angular
- Backend security: Spring Security / JWT
- Database: H2 / Spring Data JPA

## QA Purpose

The FR-06 QA checks verify that the payment status overview is correctly implemented across backend, frontend, and end-to-end user flow.

The feature must show payment and balance information for the authenticated user's own household only. It must rely on the existing authentication and household membership model and must not expose data from other households.

## Core QA Scope

FR-06 must provide a payment/balance overview based on household financial data from FR-05, especially expenses, splits, payments, or settlements.

The QA expects:

- Backend support for the payment status overview.
- A reachable frontend page or section.
- Authenticated access only.
- Household-scoped data.
- No client-side household selection for scoping.
- Empty, error, and populated states.
- No fake production data.
- Known limitations documented.

## General Acceptance Criteria

A user should be able to:

- Log in.
- Navigate to the payment status overview.
- See relevant payment/balance data for their own household.
- See a stable empty state when no payment data exists.
- See a user-friendly error state if loading fails.

The system must:

- Derive the household from the authenticated user.
- Avoid sending `householdId` from the frontend.
- Avoid exposing other household data.
- Avoid crashing on empty or missing data.
- Avoid displaying fake production data.

## Backend QA Requirements

### API Contract

The backend must expose endpoint(s) for the Payment Status Overview.

Required API properties:

- Endpoint naming follows project conventions.
- Correct HTTP method is used.
- Endpoint is authenticated.
- Endpoint does not require `householdId` from the frontend.
- Household is derived from the authenticated principal.
- Response uses stable DTOs, not raw JPA entities.
- DTOs contain enough data for the frontend.
- DTOs do not expose passwords, tokens, invitation secrets, or unrelated user data.
- Empty result returns an empty array/object where appropriate, not `null` or an error.
- Error cases use the project-standard exception handling.

Expected evidence:

- Endpoint path.
- Service class/method.
- DTO class.
- Integration test.

### Authentication And Authorization

Backend QA must verify:

- Requests without token are rejected.
- Authenticated users with active household membership can access their overview.
- Backend uses authenticated principal, not client-provided user data.
- Backend resolves the active roommate membership.
- All returned data is scoped to the active roommate's household.
- Household A users cannot see Household B data.
- Users without active household membership are rejected.
- Frontend cannot override household scope via request parameters.

### Feature Correctness

The backend implementation should:

- Use the correct source entities/repositories from the expense/payment domain.
- Include data from the current household.
- Exclude data from all other households.
- Handle empty household financial data.
- Handle multiple roommates.
- Handle multiple records.
- Handle duplicates/repeated records according to feature rules.
- Use deterministic and documented sorting.
- Safely handle optional/null fields.
- Avoid generating fake data in production code.
- Avoid unexpected changes to related features.

### Repository And Query Design

Queries should:

- Be household-scoped.
- Avoid fetching all households globally and filtering in memory.
- Filter by relevant status/visibility where applicable.
- Sort deterministically.
- Avoid obvious N+1 loading issues.
- Use `@EntityGraph` or explicit fetches if mapped relations are needed.
- Remain readable and maintainable.
- Handle demo-scale data.

### Backend Tests

Backend unit tests should cover:

- Main service/calculation/aggregation logic.
- Successful retrieval.
- Empty data.
- Multiple roommates.
- Multiple records.
- Household-scoped repository calls.
- User without active household.
- Null/optional field handling.
- Important feature edge cases.

Backend integration tests should cover:

- Main endpoint.
- Unauthenticated request rejection.
- Authenticated household user success.
- JSON response shape.
- At least two households.
- No cross-household leakage.
- Empty result.
- Populated result.
- Synthetic test data only.

Expected command:

```bash
cd backend && mvn test
```

## Frontend QA Requirements

### Route And Navigation

The frontend must provide a reachable page or section for FR-06.

QA checks:

- Route or parent page follows existing Angular routing conventions.
- Navigation/sidebar entry exists if the feature has a standalone page.
- Navigation label is clear.
- Route is protected like other authenticated routes.
- Existing navigation links remain intact.
- Browser refresh works if standalone.
- Back/forward navigation works.
- No route conflicts are introduced.

### Service And DTO Integration

Frontend QA must verify:

- DTO/interface exists.
- DTO field names match backend JSON.
- Optional/null fields are typed safely.
- Service method exists for loading payment status data.
- Service calls the correct backend endpoint.
- Service uses existing global backend URI convention.
- Service sends no `householdId`.
- Service sends no user id, roommate id, or email for household scoping.
- Service relies on existing JWT/auth mechanism.
- Service contains no fake/mock production data.
- Service test verifies URL, HTTP method, and no household id.

### Component Behavior

The component should:

- Load data on init or at the accepted interaction point.
- Avoid nested subscription leaks.
- Show loading state.
- Show error state for failed API calls.
- Show empty state for no data.
- Render populated backend data.
- Handle optional/null fields safely.
- Handle multiple records.
- Avoid rendering `undefined`, `NaN`, or `[object Object]`.
- Avoid fake production data.
- Recover through refresh/navigation after an error.

### Feature Rendering

The UI should:

- Display all required fields for the payment status overview.
- Use understandable labels.
- Show status, visibility, amount, or quantity fields where relevant.
- Provide clear empty state text.
- Provide user-friendly error text without stack traces.
- Handle long names, descriptions, and emails without layout breakage.
- Keep multiple records readable.
- Make backend sorting/grouping understandable.
- Not claim unsupported behavior.
- Not show fake data before backend response.

### Theme And Style

The feature should match existing WGMS UI:

- Reuse existing card/table/list styling.
- Preserve spacing and typography.
- Use existing button/link/status styles.
- Introduce no external UI library.
- Avoid unrelated global CSS changes.
- Work on desktop and mobile widths.
- Wrap or truncate long text safely.

### Frontend Tests

Frontend tests should cover:

- Component creation.
- Service call on load.
- Loading state.
- Error state.
- Empty state.
- Populated rendering.
- Multiple records.
- Optional/null fields.
- Service endpoint URL.
- No household id in service call.
- Existing related tests still passing.

Expected commands:

```bash
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
cd frontend && npm run build
cd frontend && npm run lint
```

## E2E QA Requirements

An FR-06 Cypress/e2e spec is expected unless absence is explicitly justified.

The E2E flow should verify:

- User can log in.
- User can navigate to the payment status overview.
- Relevant synthetic data is created or seeded.
- Populated backend data is displayed.
- Empty/no-crash behavior is covered if possible.
- The test does not use production data.
- Existing E2E tests still pass or limitations are documented.

Expected command:

```bash
cd e2e && npx cypress run --spec cypress/e2e/<fr-06-spec-name>.cy.js
```

## Security And Privacy Checks

FR-06 is security-sensitive because it displays financial household information.

Required checks:

- Backend endpoint is authenticated.
- Backend endpoint is household-scoped.
- Frontend sends no household id.
- Backend ignores any client-side attempt to change household scope.
- Cross-household leakage is tested.
- Response exposes only necessary financial/status data.
- No passwords, tokens, invitation secrets, unrelated users, or private unrelated data are exposed.
- Logs do not dump sensitive feature data.
- Test data is synthetic.

## Regression Checks

Before merge, QA should confirm:

- Existing authentication still works.
- Existing household membership still works.
- Existing related expense/payment feature pages and endpoints still work.
- Full backend tests pass.
- Frontend tests pass.
- Frontend build passes.
- E2E tests pass or limitations are documented.
- No existing tests were deleted or weakened.
- No unrelated business logic or frontend refactoring is mixed in.
- No secrets or local files are committed.

## Merge Blockers

The FR-06 implementation should not be merged if any of these are true:

- Backend endpoint is missing.
- Endpoint is unauthenticated.
- Endpoint trusts frontend-provided household id.
- Cross-household data leaks.
- Empty data crashes backend or frontend.
- DTO exposes raw entities or sensitive fields.
- Backend unit tests are missing.
- Backend integration tests are missing.
- Frontend tests are missing.
- E2E evidence is missing without justification.
- Feature is unreachable in the UI.
- Frontend sends household id.
- Fake production data appears.
- Backend error crashes the page.
- Null/optional fields render as ugly values.
- UI style is inconsistent with the app.
- Build or test commands fail.
- Known limitations are undocumented.

## Final Approval Criteria

FR-06 is ready for merge review only when:

- Backend QA is completed.
- Frontend QA is completed.
- General QA is completed.
- Backend unit and integration tests exist and pass.
- Frontend tests exist and pass.
- E2E tests exist and pass, or absence is explicitly justified.
- Household isolation is verified.
- Feature correctness is verified.
- Loading, error, empty, and populated states are verified.
- Build/lint pass where configured.
- Regression risk is checked.
- Known limitations are documented.

## Current Evidence Status

The source QA files are checklist templates. They define the required evidence, but the fields for branch, merge request, developer, reviewer, date, command outputs, specific endpoint names, service names, DTOs, test class names, Cypress spec path, and manual evidence are not filled in yet.

Open evidence still to be added after implementation/testing:

- Actual backend endpoint path.
- Actual backend service and DTO names.
- Actual repository/query names.
- Backend unit test class and results.
- Backend integration test class and results.
- Frontend route/component/service/DTO names.
- Frontend component/service test results.
- Cypress spec path and result.
- Manual QA notes or screenshots if used.
- Known limitations.
- Final reviewer approval.
