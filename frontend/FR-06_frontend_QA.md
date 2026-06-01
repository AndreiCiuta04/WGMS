# FR-06 Frontend QA Evidence - Payment Status Overview

This root QA file exists for the project-level checklist. The detailed frontend QA evidence is kept in `frontend/QA_docs_frontend/FR-06_frontend_QA.md`.

## Status

- [x] `/payment-status` route exists and is protected.
- [x] Sidebar and bottom navigation expose Payment Status.
- [x] Payment service calls `/payments/overview` and `/payments`.
- [x] Frontend sends no `householdId` for payment scoping.
- [x] Component shows loading, empty, populated, and error states.
- [x] Overview load failures no longer render false empty states.
- [x] Payment recording visibility uses authenticated user email instead of extra household/member requests.
- [x] FR-06 Cypress spec exists and passes.
- [x] Frontend lint, build, and unit/component tests pass.

## Evidence

Implementation:

```text
frontend/src/app/components/payment-status/payment-status.component.ts
frontend/src/app/components/payment-status/payment-status.component.html
frontend/src/app/services/payment.service.ts
frontend/src/app/dtos/payment.ts
```

Tests:

```text
frontend/src/app/components/payment-status/payment-status.component.spec.ts
frontend/src/app/services/payment.service.spec.ts
e2e/cypress/e2e/paymentStatus.cy.js
```

Commands:

```bash
cd frontend && npm.cmd run lint
cd frontend && npm.cmd run build
cd frontend && npm.cmd test -- --watch=false --browsers=ChromeHeadless
cd e2e && npx.cmd cypress run --spec cypress/e2e/paymentStatus.cy.js
```

Results:

```text
Lint: All files pass linting.
Build: Application bundle generation complete.
Frontend tests: TOTAL: 71 SUCCESS
Cypress FR-06: 2 passing, 0 failing
```

## Known Limitation

- Payments cannot yet be edited, deleted, or undone.
