# FR-06 Frontend QA Evidence - Payment Status Overview

## Feature Information

- Feature ID: FR-06
- Feature name: Payment Status Overview
- Use case: UC-03 - Shared Expense Tracking and Settlement
- Priority: High
- Category: Core
- Version: V3
- Frontend technology: Angular
- Dependency: FR-05 Shared expense tracking
- Branch: FR-06/Payment-status
- Date: 2026-06-01

## Implementation Evidence

Route and navigation:

```text
frontend/src/app/app-routing.module.ts
frontend/src/app/components/sidebar/sidebar.component.html
frontend/src/app/components/bottom-tab-bar/bottom-tab-bar.component.html
/payment-status
```

Frontend files:

```text
frontend/src/app/components/payment-status/payment-status.component.ts
frontend/src/app/components/payment-status/payment-status.component.html
frontend/src/app/components/payment-status/payment-status.component.scss
frontend/src/app/services/payment.service.ts
frontend/src/app/dtos/payment.ts
```

Frontend tests:

```text
frontend/src/app/components/payment-status/payment-status.component.spec.ts
frontend/src/app/services/payment.service.spec.ts
e2e/cypress/e2e/fr06-payment-status.cy.js
```

## Route And Navigation

- [x] Feature page is reachable in the UI.
- [x] Route follows existing Angular routing conventions.
- [x] Sidebar and bottom-tab navigation entries exist.
- [x] Navigation label is clear: Payment Status.
- [x] Route is protected by the existing auth guard.
- [x] Existing navigation links remain intact.
- [x] Feature does not conflict with existing routes.
- [x] E2E navigates to `/payment-status`.

## Service And DTO Integration

- [x] Frontend DTO/interface exists.
- [x] DTO field names match backend JSON, including `totalIOwe`.
- [x] Service method exists for loading payment overview.
- [x] Service method exists for creating payments.
- [x] Service calls `/payments/overview` and `/payments`.
- [x] Service uses existing global backend URI convention.
- [x] Service sends no `householdId`.
- [x] Service sends no user id, roommate id, or email for household scoping.
- [x] Service relies on existing JWT/auth interceptor.
- [x] Service contains no fake/mock production data.
- [x] Service tests verify endpoint URL and HTTP method.

## Component Behavior

- [x] Component loads data on init.
- [x] Component no longer needs household/member round-trips just to decide payment recording visibility.
- [x] Loading state is shown.
- [x] Error state is shown for failed API calls.
- [x] Failed overview load no longer displays a false empty state.
- [x] Empty state is shown for no open balances and no payments.
- [x] Populated state renders backend balances and payment history.
- [x] Multiple records are handled.
- [x] `canRecord` compares authenticated user email with `fromRoommateEmail`.
- [x] Component does not show fake production data.

## Feature Rendering

- [x] UI displays totals, open balances, record-payment form, and payment history.
- [x] UI labels are understandable.
- [x] Amount fields use existing currency formatting.
- [x] Empty state text is clear.
- [x] Error state text is user-friendly and does not expose stack traces.
- [x] Multiple balances/payments remain readable.
- [x] UI does not claim edit/delete/undo support for payments.

## Frontend Tests

Component/service tests cover:

- [x] Component creation.
- [x] Service call on init.
- [x] Loading state.
- [x] Error state.
- [x] Empty state.
- [x] Populated rendering.
- [x] Multiple records.
- [x] Recording a valid payment.
- [x] Payment cannot exceed open balance.
- [x] Overview load failure suppresses empty states.
- [x] `canRecord` works by email without household member lookup.
- [x] Service endpoint URL and HTTP method.
- [x] No household id is sent by payment service methods.

E2E covers:

- [x] Login.
- [x] Navigation to Payment Status.
- [x] Synthetic household, roommate, and expense setup.
- [x] Populated open balance rendering.
- [x] Recording a partial payment.
- [x] Payment history rendering.
- [x] FR-05 Expenses page showing the same reduced balance.
- [x] Empty payment state for a household without balances.

## Command Evidence

Frontend unit/component tests:

```bash
cd frontend && npm.cmd test -- --watch=false --browsers=ChromeHeadless
```

Result:

```text
TOTAL: 71 SUCCESS
```

Frontend lint:

```bash
cd frontend && npm.cmd run lint
```

Result:

```text
All files pass linting.
```

Frontend build:

```bash
cd frontend && npm.cmd run build
```

Result:

```text
Application bundle generation complete.
```

FR-06 Cypress spec:

```bash
cd e2e && npx.cmd cypress run --spec cypress/e2e/fr06-payment-status.cy.js
```

Result:

```text
3 passing, 0 failing
```

## Known Limitation

- Payments cannot yet be edited, deleted, or undone. The UI only exposes creation of payments against open balances.

## Frontend QA Conclusion

Frontend FR-06 is QA-approved for merge review with the documented payment edit/delete/undo limitation.
