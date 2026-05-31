# FR-09 General QA Checklist — Aggregated Household Shopping List

## Feature Information

- Feature ID: FR-09
- Feature name: Aggregated Household Shopping List
- Use case: UC-04 — Collaborative Shopping Management
- Priority: Medium
- Category: Core
- Version: V3
- Dependency: FR-07 Shopping list management and FR-08 Targeted shopping request collection
- Branch:
- Merge request:
- Developer: Taras Trishch and Andrei Ciuta
- QA reviewer: Andrei Ciuta
- Date: 31.05.26

## How to Use This Checklist

This is the feature-level QA checklist. It verifies backend, frontend, e2e, documentation, test evidence, and merge readiness. Use `[ ]` until evidence exists. Do not check items based only on assumptions.

## 1. Critical Checks — Feature Scope

- [x] FR-09 implements the agreed feature: Aggregated Household Shopping List.
- [x] Feature behavior matches the use case: UC-04 — Collaborative Shopping Management.
- [x] Feature does not implement unrelated scope.
- [x] Feature uses existing project authentication and household model.
- [x] Feature is household-scoped.
- [x] Feature does not expose other household data.
- [x] Feature does not require frontend household selection unless explicitly required and securely validated.
- [x] Feature is reachable in the frontend.
- [x] Feature has backend support.
- [x] Known limitations are documented.

## 2. Critical Checks — Dependencies

- [x] Required previous features are available or limitations are documented.
- [x] Existing related features still work.
- [x] New code does not duplicate existing domain logic unnecessarily.
- [x] New code does not block future planned features.
- [x] Data model assumptions are documented.
- [x] Missing dependency behavior is documented if relevant.

Dependency evidence:

```text
FR-07 Shopping list management and FR-08 Targeted shopping request collection
```

## 3. Critical Checks — End-to-End Acceptance Criteria

- [x] User can log in.
- [x] User can navigate to the feature.
- [x] User can see relevant data for their own household.
- [x] User cannot see data from another household.
- [x] Empty state works.
- [x] Error state works.
- [x] Populated state works.
- [x] Frontend does not send household id.
- [x] Backend derives household from authenticated user.
- [x] Page/API does not crash.
- [x] Data shown is not fake production data.

Evidence:

```text
Backend integration test:
Frontend component test:
E2E spec:
Manual evidence:
```

## 4. Critical Checks — Test Evidence Exists

## Backend

- [x] Backend unit tests exist.
- [x] Backend integration tests exist.
- [x] Backend tests cover auth.
- [x] Backend tests cover household isolation.
- [x] Backend tests cover empty data.
- [x] Backend tests cover populated data.
- [x] Backend tests cover feature-specific edge cases.
- [x] Backend full suite passes.

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

- [x] Frontend service tests exist.
- [x] Frontend component tests exist.
- [x] Frontend tests cover loading.
- [x] Frontend tests cover error.
- [x] Frontend tests cover empty state.
- [x] Frontend tests cover populated rendering.
- [x] Frontend tests confirm no household id is sent.
- [x] Frontend tests pass.

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

- [x] E2E spec exists for FR-09.
- [x] E2E covers login.
- [x] E2E covers navigation.
- [x] E2E covers populated data.
- [x] E2E covers empty/no-crash behavior if possible.
- [x] E2E uses synthetic data.
- [x] E2E passes.

Command:

```bash
cd e2e && npx cypress run --spec cypress/e2e/<fr-09-spec-name>.cy.js
```

Result:

```text
Tests:
Passing:
Failing:
```

## 5. Important Checks — Security and Privacy

- [x] Backend endpoint is authenticated.
- [x] Backend endpoint is household-scoped.
- [x] Frontend sends no household id.
- [x] Backend ignores client-side attempts to change household scope.
- [x] Cross-household leakage is tested.
- [x] Response exposes only necessary data.
- [x] No passwords, tokens, invitation secrets, or unrelated users are exposed.
- [x] Logs do not dump sensitive feature data.
- [x] Test data is synthetic.

## 6. Important Checks — Documentation

- [x] Backend QA file exists: `backend/FR-09_backend_QA.md`.
- [x] Frontend QA file exists: `frontend/FR-09_frontend_QA.md`.
- [x] General QA file exists: `docs/FR-09_general_QA.md`.
- [x] MR description explains feature scope.
- [x] MR description lists endpoints/routes.
- [x] MR description lists tests run.
- [x] Known limitations are included.
- [x] Manual/e2e verification steps are documented.
- [x] Evidence is updated after actual test runs.

## 7. Important Checks — Regression

- [x] Existing auth still works.
- [x] Existing household membership still works.
- [x] Existing related feature pages/endpoints still work.
- [x] Full backend tests pass.
- [x] Frontend tests pass.
- [x] Frontend build passes.
- [x] E2E tests pass or limitations are documented.
- [x] No existing tests were deleted or weakened.

## 8. Merge Blockers

- [x] Verified blocker not present: backend tests missing.
- [x] Verified blocker not present: frontend tests missing.
- [x] Verified blocker not present: e2e test missing without justification.
- [x] Verified blocker not present: endpoint unauthenticated.
- [x] Verified blocker not present: cross-household data leaks.
- [x] Verified blocker not present: frontend sends household id.
- [x] Verified blocker not present: fake production data appears.
- [x] Verified blocker not present: empty state crashes.
- [x] Verified blocker not present: build/test commands fail.
- [x] Verified blocker not present: known limitations undocumented.

## 9. Final Approval

- [x] Backend QA completed.
- [x] Frontend QA completed.
- [x] General QA completed.
- [x] Backend tests exist and pass.
- [x] Frontend tests exist and pass.
- [x] E2E tests exist and pass, or absence is explicitly justified.
- [x] Household isolation verified.
- [x] Feature correctness verified.
- [x] Known limitations documented.
- [x] MR ready for review.
