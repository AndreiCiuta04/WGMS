# FR-06 General QA Checklist — Payment Status Overview

## Feature Information

- Feature ID: FR-06
- Feature name: Payment Status Overview
- Use case: UC-03 — Shared Expense Tracking and Settlement
- Priority: High
- Category: Core
- Version: V3
- Dependency: FR-05 Shared expense tracking
- Branch:
- Merge request:
- Developer:
- QA reviewer:
- Date:

## How to Use This Checklist

This is the feature-level QA checklist. It verifies backend, frontend, e2e, documentation, test evidence, and merge readiness. Use `[ ]` until evidence exists. Do not check items based only on assumptions.

## 1. Critical Checks — Feature Scope

- [ ] FR-06 implements the agreed feature: Payment Status Overview.
- [ ] Feature behavior matches the use case: UC-03 — Shared Expense Tracking and Settlement.
- [ ] Feature does not implement unrelated scope.
- [ ] Feature uses existing project authentication and household model.
- [ ] Feature is household-scoped.
- [ ] Feature does not expose other household data.
- [ ] Feature does not require frontend household selection unless explicitly required and securely validated.
- [ ] Feature is reachable in the frontend.
- [ ] Feature has backend support.
- [ ] Known limitations are documented.

## 2. Critical Checks — Dependencies

- [ ] Required previous features are available or limitations are documented.
- [ ] Existing related features still work.
- [ ] New code does not duplicate existing domain logic unnecessarily.
- [ ] New code does not block future planned features.
- [ ] Data model assumptions are documented.
- [ ] Missing dependency behavior is documented if relevant.

Dependency evidence:

```text
FR-05 Shared expense tracking
```

## 3. Critical Checks — End-to-End Acceptance Criteria

- [ ] User can log in.
- [ ] User can navigate to the feature.
- [ ] User can see relevant data for their own household.
- [ ] User cannot see data from another household.
- [ ] Empty state works.
- [ ] Error state works.
- [ ] Populated state works.
- [ ] Frontend does not send household id.
- [ ] Backend derives household from authenticated user.
- [ ] Page/API does not crash.
- [ ] Data shown is not fake production data.

Evidence:

```text
Backend integration test:
Frontend component test:
E2E spec:
Manual evidence:
```

## 4. Critical Checks — Test Evidence Exists

## Backend

- [ ] Backend unit tests exist.
- [ ] Backend integration tests exist.
- [ ] Backend tests cover auth.
- [ ] Backend tests cover household isolation.
- [ ] Backend tests cover empty data.
- [ ] Backend tests cover populated data.
- [ ] Backend tests cover feature-specific edge cases.
- [ ] Backend full suite passes.

Command:

```bash
cd backend && mvn test
```

Result:

```text
Tests run:
Failures:
Errors:
Skipped:
```

## Frontend

- [ ] Frontend service tests exist.
- [ ] Frontend component tests exist.
- [ ] Frontend tests cover loading.
- [ ] Frontend tests cover error.
- [ ] Frontend tests cover empty state.
- [ ] Frontend tests cover populated rendering.
- [ ] Frontend tests confirm no household id is sent.
- [ ] Frontend tests pass.

Command:

```bash
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
```

Result:

```text
Successes:
Failures:
```

## E2E

- [ ] E2E spec exists for FR-06.
- [ ] E2E covers login.
- [ ] E2E covers navigation.
- [ ] E2E covers populated data.
- [ ] E2E covers empty/no-crash behavior if possible.
- [ ] E2E uses synthetic data.
- [ ] E2E passes.

Command:

```bash
cd e2e && npx cypress run --spec cypress/e2e/<fr-06-spec-name>.cy.js
```

Result:

```text
Tests:
Passing:
Failing:
```

## 5. Important Checks — Security and Privacy

- [ ] Backend endpoint is authenticated.
- [ ] Backend endpoint is household-scoped.
- [ ] Frontend sends no household id.
- [ ] Backend ignores client-side attempts to change household scope.
- [ ] Cross-household leakage is tested.
- [ ] Response exposes only necessary data.
- [ ] No passwords, tokens, invitation secrets, or unrelated users are exposed.
- [ ] Logs do not dump sensitive feature data.
- [ ] Test data is synthetic.

## 6. Important Checks — Documentation

- [ ] Backend QA file exists: `backend/FR-06_backend_QA.md`.
- [ ] Frontend QA file exists: `frontend/FR-06_frontend_QA.md`.
- [ ] General QA file exists: `docs/FR-06_general_QA.md`.
- [ ] MR description explains feature scope.
- [ ] MR description lists endpoints/routes.
- [ ] MR description lists tests run.
- [ ] Known limitations are included.
- [ ] Manual/e2e verification steps are documented.
- [ ] Evidence is updated after actual test runs.

## 7. Important Checks — Regression

- [ ] Existing auth still works.
- [ ] Existing household membership still works.
- [ ] Existing related feature pages/endpoints still work.
- [ ] Full backend tests pass.
- [ ] Frontend tests pass.
- [ ] Frontend build passes.
- [ ] E2E tests pass or limitations are documented.
- [ ] No existing tests were deleted or weakened.

## 8. Merge Blockers

- [ ] Verified blocker not present: backend tests missing.
- [ ] Verified blocker not present: frontend tests missing.
- [ ] Verified blocker not present: e2e test missing without justification.
- [ ] Verified blocker not present: endpoint unauthenticated.
- [ ] Verified blocker not present: cross-household data leaks.
- [ ] Verified blocker not present: frontend sends household id.
- [ ] Verified blocker not present: fake production data appears.
- [ ] Verified blocker not present: empty state crashes.
- [ ] Verified blocker not present: build/test commands fail.
- [ ] Verified blocker not present: known limitations undocumented.

## 9. Final Approval

- [ ] Backend QA completed.
- [ ] Frontend QA completed.
- [ ] General QA completed.
- [ ] Backend tests exist and pass.
- [ ] Frontend tests exist and pass.
- [ ] E2E tests exist and pass, or absence is explicitly justified.
- [ ] Household isolation verified.
- [ ] Feature correctness verified.
- [ ] Known limitations documented.
- [ ] MR ready for review.
