# FR-09 Frontend QA Checklist — Aggregated Household Shopping List

## Feature Information

- Feature ID: FR-09
- Feature name: Aggregated Household Shopping List
- Use case: UC-04 — Collaborative Shopping Management
- Priority: Medium
- Category: Core
- Version: V3
- Frontend technology: Angular
- Dependency: FR-07 Shopping list management and FR-08 Targeted shopping request collection
- Branch:
- Merge request:
- Developer: Taras Trishch and Andrei Ciuta
- QA reviewer: Andrei Ciuta
- Date: 31.05.26

## How to Use This Checklist

Use this checklist before merging the frontend implementation. A checked item must be backed by code inspection, frontend unit/component tests, service tests, e2e tests, command output, screenshot/manual evidence, or QA notes.

## 1. Critical Checks — Route and Navigation

- [x] Feature page/section is reachable in the UI.
- [x] Route or parent page follows existing Angular routing conventions.
- [x] Navigation/sidebar entry exists if this feature has a standalone page.
- [x] Navigation label is clear and user-facing.
- [x] Route is protected consistently with other authenticated app routes.
- [x] Existing navigation links remain intact.
- [x] Browser refresh works on the route if standalone.
- [x] Back/forward navigation works.
- [x] Feature does not conflict with existing routes.

Evidence:

```text
Routing:
Navigation:
E2E navigation:
```

## 2. Critical Checks — Service and DTO Integration

- [x] Frontend DTO/interface exists.
- [x] DTO field names match backend JSON.
- [x] Optional/null fields are typed safely.
- [x] Service method exists for loading feature data.
- [x] Service calls the correct backend endpoint.
- [x] Service uses existing global backend URI convention.
- [x] Service sends no `householdId`.
- [x] Service sends no user id, roommate id, or email for household scoping.
- [x] Service relies on existing auth/JWT mechanism.
- [x] Service contains no fake/mock production data.
- [x] Service test verifies endpoint URL and HTTP method.
- [x] Service test verifies no household id is sent.

Evidence:

```text
DTO:
Service:
Service spec:
```

## 3. Critical Checks — Component Behavior

- [x] Component loads data on init or at the accepted interaction point.
- [x] Component avoids nested subscription leaks.
- [x] Loading state is shown.
- [x] Error state is shown for failed API calls.
- [x] Empty state is shown for no data.
- [x] Populated state renders backend data.
- [x] Component handles optional/null fields safely.
- [x] Component handles multiple records.
- [x] Component does not render `undefined`, `NaN`, or `[object Object]`.
- [x] Component does not show fake production data.
- [x] Component can recover by refresh/navigation after error.

Evidence:

```text
Component:
Template:
Spec:
```

## 4. Critical Checks — Feature Rendering

- [x] UI displays all required fields for Aggregated Household Shopping List.
- [x] UI labels are understandable.
- [x] Status/visibility/amount/quantity fields are shown where relevant.
- [x] Empty state text is clear.
- [x] Error state text is user-friendly and does not expose stack traces.
- [x] Long names/descriptions/emails do not break layout.
- [x] Multiple records remain readable.
- [x] Sorting/grouping displayed by backend is understandable.
- [x] UI does not misrepresent unsupported behavior.
- [x] No fake data is displayed before backend response.

Evidence:

```text
Template:
Component test:
E2E test:
```

## 5. Important Checks — UI Theme and Style

- [x] UI matches existing WGMS theme.
- [x] Existing card/table/list styling is reused.
- [x] Existing spacing and typography are preserved.
- [x] Buttons/links match existing style.
- [x] Badges/status labels match existing app style.
- [x] No external UI library is introduced.
- [x] No unrelated global CSS changes are introduced.
- [x] Page works on desktop.
- [x] Page works on mobile widths.
- [x] Long text wraps/truncates safely.

Evidence:

```text
SCSS/template:
Screenshot/manual:
Build/lint:
```

## 6. Critical Checks — Frontend Tests Exist

- [x] Component creation test exists.
- [x] Component calls service on load.
- [x] Loading state test exists.
- [x] Error state test exists.
- [x] Empty state test exists.
- [x] Populated rendering test exists.
- [x] Test covers multiple records.
- [x] Test covers optional/null fields.
- [x] Service test exists.
- [x] Service test verifies endpoint URL.
- [x] Service test verifies no household id is sent.
- [x] Existing related frontend tests still pass.
- [x] No frontend tests are skipped or weakened.

Command:

```bash
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
```

Result:

```text
Successes:
Failures:
```

## 7. Critical Checks — E2E Tests Exist

- [x] Cypress/e2e spec exists for FR-09.
- [x] E2E user can log in.
- [x] E2E user can navigate to the feature.
- [x] E2E creates or seeds relevant synthetic data.
- [x] E2E verifies populated rendering.
- [x] E2E verifies empty state if possible.
- [x] E2E verifies no page crash.
- [x] E2E does not depend on production data.
- [x] E2E spec is documented.
- [x] Existing e2e tests still pass or limitation is documented.

Command:

```bash
cd e2e && npx cypress run --spec cypress/e2e/<fr-09-spec-name>.cy.js
```

Evidence:

```text
Spec:
Result:
```

## 8. Regression Checks

- [x] Existing login flow still works.
- [x] Existing sidebar/navigation still works.
- [x] Existing related feature pages still work.
- [x] Existing frontend tests pass.
- [x] Frontend build passes.
- [x] Lint passes if configured.
- [x] No local/mock/fake production data remains.
- [x] No unrelated frontend refactoring is mixed into this feature.

Commands:

```bash
cd frontend && npm run build
cd frontend && npm run lint
```

## 9. Frontend Merge Blockers

- [x] Verified blocker not present: feature is unreachable.
- [x] Verified blocker not present: frontend sends household id.
- [x] Verified blocker not present: fake production data is shown.
- [x] Verified blocker not present: empty state crashes page.
- [x] Verified blocker not present: backend error crashes page.
- [x] Verified blocker not present: normal null fields render ugly values.
- [x] Verified blocker not present: UI style is inconsistent with app.
- [x] Verified blocker not present: frontend tests are missing.
- [x] Verified blocker not present: e2e test is missing without justification.
- [x] Verified blocker not present: build fails.

## 10. Frontend Final Approval

- [x] Route/navigation verified.
- [x] Service/DTO contract verified.
- [x] Loading/error/empty states verified.
- [x] Feature rendering verified.
- [x] Frontend tests exist and pass.
- [x] E2E tests exist and pass, or absence is explicitly justified.
- [x] Build/lint pass.
- [x] Known limitations documented.
- [x] Frontend ready for merge review.
