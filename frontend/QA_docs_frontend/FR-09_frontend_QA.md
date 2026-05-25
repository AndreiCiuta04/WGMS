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
- Developer:
- QA reviewer:
- Date:

## How to Use This Checklist

Use this checklist before merging the frontend implementation. A checked item must be backed by code inspection, frontend unit/component tests, service tests, e2e tests, command output, screenshot/manual evidence, or QA notes.

## 1. Critical Checks — Route and Navigation

- [ ] Feature page/section is reachable in the UI.
- [ ] Route or parent page follows existing Angular routing conventions.
- [ ] Navigation/sidebar entry exists if this feature has a standalone page.
- [ ] Navigation label is clear and user-facing.
- [ ] Route is protected consistently with other authenticated app routes.
- [ ] Existing navigation links remain intact.
- [ ] Browser refresh works on the route if standalone.
- [ ] Back/forward navigation works.
- [ ] Feature does not conflict with existing routes.

Evidence:

```text
Routing:
Navigation:
E2E navigation:
```

## 2. Critical Checks — Service and DTO Integration

- [ ] Frontend DTO/interface exists.
- [ ] DTO field names match backend JSON.
- [ ] Optional/null fields are typed safely.
- [ ] Service method exists for loading feature data.
- [ ] Service calls the correct backend endpoint.
- [ ] Service uses existing global backend URI convention.
- [ ] Service sends no `householdId`.
- [ ] Service sends no user id, roommate id, or email for household scoping.
- [ ] Service relies on existing auth/JWT mechanism.
- [ ] Service contains no fake/mock production data.
- [ ] Service test verifies endpoint URL and HTTP method.
- [ ] Service test verifies no household id is sent.

Evidence:

```text
DTO:
Service:
Service spec:
```

## 3. Critical Checks — Component Behavior

- [ ] Component loads data on init or at the accepted interaction point.
- [ ] Component avoids nested subscription leaks.
- [ ] Loading state is shown.
- [ ] Error state is shown for failed API calls.
- [ ] Empty state is shown for no data.
- [ ] Populated state renders backend data.
- [ ] Component handles optional/null fields safely.
- [ ] Component handles multiple records.
- [ ] Component does not render `undefined`, `NaN`, or `[object Object]`.
- [ ] Component does not show fake production data.
- [ ] Component can recover by refresh/navigation after error.

Evidence:

```text
Component:
Template:
Spec:
```

## 4. Critical Checks — Feature Rendering

- [ ] UI displays all required fields for Aggregated Household Shopping List.
- [ ] UI labels are understandable.
- [ ] Status/visibility/amount/quantity fields are shown where relevant.
- [ ] Empty state text is clear.
- [ ] Error state text is user-friendly and does not expose stack traces.
- [ ] Long names/descriptions/emails do not break layout.
- [ ] Multiple records remain readable.
- [ ] Sorting/grouping displayed by backend is understandable.
- [ ] UI does not misrepresent unsupported behavior.
- [ ] No fake data is displayed before backend response.

Evidence:

```text
Template:
Component test:
E2E test:
```

## 5. Important Checks — UI Theme and Style

- [ ] UI matches existing WGMS theme.
- [ ] Existing card/table/list styling is reused.
- [ ] Existing spacing and typography are preserved.
- [ ] Buttons/links match existing style.
- [ ] Badges/status labels match existing app style.
- [ ] No external UI library is introduced.
- [ ] No unrelated global CSS changes are introduced.
- [ ] Page works on desktop.
- [ ] Page works on mobile widths.
- [ ] Long text wraps/truncates safely.

Evidence:

```text
SCSS/template:
Screenshot/manual:
Build/lint:
```

## 6. Critical Checks — Frontend Tests Exist

- [ ] Component creation test exists.
- [ ] Component calls service on load.
- [ ] Loading state test exists.
- [ ] Error state test exists.
- [ ] Empty state test exists.
- [ ] Populated rendering test exists.
- [ ] Test covers multiple records.
- [ ] Test covers optional/null fields.
- [ ] Service test exists.
- [ ] Service test verifies endpoint URL.
- [ ] Service test verifies no household id is sent.
- [ ] Existing related frontend tests still pass.
- [ ] No frontend tests are skipped or weakened.

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

- [ ] Cypress/e2e spec exists for FR-09.
- [ ] E2E user can log in.
- [ ] E2E user can navigate to the feature.
- [ ] E2E creates or seeds relevant synthetic data.
- [ ] E2E verifies populated rendering.
- [ ] E2E verifies empty state if possible.
- [ ] E2E verifies no page crash.
- [ ] E2E does not depend on production data.
- [ ] E2E spec is documented.
- [ ] Existing e2e tests still pass or limitation is documented.

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

- [ ] Existing login flow still works.
- [ ] Existing sidebar/navigation still works.
- [ ] Existing related feature pages still work.
- [ ] Existing frontend tests pass.
- [ ] Frontend build passes.
- [ ] Lint passes if configured.
- [ ] No local/mock/fake production data remains.
- [ ] No unrelated frontend refactoring is mixed into this feature.

Commands:

```bash
cd frontend && npm run build
cd frontend && npm run lint
```

## 9. Frontend Merge Blockers

- [ ] Verified blocker not present: feature is unreachable.
- [ ] Verified blocker not present: frontend sends household id.
- [ ] Verified blocker not present: fake production data is shown.
- [ ] Verified blocker not present: empty state crashes page.
- [ ] Verified blocker not present: backend error crashes page.
- [ ] Verified blocker not present: normal null fields render ugly values.
- [ ] Verified blocker not present: UI style is inconsistent with app.
- [ ] Verified blocker not present: frontend tests are missing.
- [ ] Verified blocker not present: e2e test is missing without justification.
- [ ] Verified blocker not present: build fails.

## 10. Frontend Final Approval

- [ ] Route/navigation verified.
- [ ] Service/DTO contract verified.
- [ ] Loading/error/empty states verified.
- [ ] Feature rendering verified.
- [ ] Frontend tests exist and pass.
- [ ] E2E tests exist and pass, or absence is explicitly justified.
- [ ] Build/lint pass.
- [ ] Known limitations documented.
- [ ] Frontend ready for merge review.
