# FR-07 General QA Checklist — Shopping List Management

## Feature Information

- Feature ID: FR-07
- Feature name: Shopping list management
- Use case: UC-04 — Collaborative Shopping Management
- Backend technology: Java Spring Boot
- Frontend technology: Angular (NgModule-based)
- Security: Spring Security, JWT
- Database: H2 (file-based)
- Automated tests: JUnit, Karma/Jasmine, Cypress
- Branch: `FR-07/Shopping-list-management`
- Merge request: (link MR before merge)
- Developer: Daniel
- QA reviewer: Taras
- Date: 2026-05-18

---

## Feature Scope Confirmation

- [x] FR-07 is correctly identified as Shopping list management.
- [x] FR-07 is correctly linked to UC-04 Collaborative Shopping Management.
- [x] Backend and frontend implement the same feature scope — 6 service methods ↔ 6 endpoints ↔ 6 frontend service methods.
- [x] The implemented feature is not accidentally FR-08 — no visibility enum, no `targetUser`, no targeted-at-me endpoint.
- [x] The implemented feature is not expense/payment settlement — no money fields.
- [x] The implemented feature is not NFC/appliance logging.
- [x] Known limitations are documented — captured in the per-layer QA files (all addressed in this MR).

---

## End-to-End Functional Requirements

### Access and Navigation

- [x] Logged-in user can navigate to the shopping list page.
- [x] Logged-out user cannot access the shopping list page — `AuthGuard` redirects to `/login`.
- [x] Shopping list navigation is visible in the correct place.
- [x] Route names and menu labels are understandable.

### Create Shopping Item

- [x] User can create a shopping list item.
- [x] User can enter required item information — name (required).
- [x] User can enter optional item information — quantity, notes.
- [x] Created item is shown immediately in the UI.
- [x] Created item is persisted in database — H2 file-based.
- [x] Created item remains visible after refresh/re-login.

### Display Shopping List

- [x] User can see shopping list items for their own household.
- [x] User sees understandable empty state — "No shopping items yet."
- [x] Item status is displayed clearly — color-coded badge with text.
- [x] Bought/completed items are distinguishable from open items.
- [x] Item ordering is understandable and stable — `OrderByCreatedAtDesc`.

### Update / Status / Delete

- [x] User can edit shopping item.
- [x] User can mark item as bought/completed.
- [x] User can undo bought/completed status — new "Mark as Needed" button restores items to NEEDED.
- [x] User can delete/remove item.
- [x] Updates are persisted after refresh/re-login.
- [x] Failed updates do not corrupt UI/backend state — `@Transactional` rollback; frontend never mutates local state on error.

---

## Backend/Frontend Integration

- [x] Frontend API service paths match backend endpoint paths.
- [x] Frontend DTO/model fields match backend DTO fields — 12 fields each side.
- [x] HTTP methods are consistent between frontend and backend.
- [x] Frontend sends JWT-authenticated requests — `AuthInterceptor`.
- [x] Backend receives and validates requests correctly — integration tests verify.
- [x] Backend responses can be displayed correctly by frontend — manual test confirmed.
- [x] Frontend handles backend success responses.
- [x] Frontend handles backend validation errors — `extractError` parses `Validation errors` array.
- [x] Frontend handles unauthorized/forbidden responses — surface backend message via new error path.
- [x] Frontend handles not-found responses.
- [x] Frontend handles server/network errors safely.

---

## Security and Authorization

- [x] Unauthenticated API requests are rejected — new `givenNoJwt_whenCreate_then401` integration test.
- [x] Authenticated users can only access their own household's shopping list.
- [x] User cannot see another household's shopping items — `givenItemOfOtherHousehold_whenFindOne_then403`.
- [x] User cannot modify another household's shopping items.
- [x] User cannot delete another household's shopping items.
- [x] Backend derives household/user ownership from authentication.
- [x] Backend does not trust frontend-provided ownership fields.
- [x] Manipulated item IDs do not bypass household isolation — 403, not 404, by design.
- [x] Expired/manipulated JWT is rejected — existing `JwtAuthorizationFilter`.
- [x] No raw JWTs, secrets, or passwords are logged or committed.

---

## Validation and Error Handling

- [x] Empty item name/title is rejected — `@NotBlank` + `Validators.required`; new `givenBlankName_whenCreate_then400WithValidationErrors` integration test.
- [x] Overly long item name/title is rejected — `@Size(max=100)` + `Validators.maxLength(100)`.
- [x] Invalid status is rejected — typed enum.
- [x] Unknown item ID is handled correctly — `NotFoundException` → 404.
- [x] Cross-household item ID is handled correctly — 403.
- [x] Backend validation errors are understandable enough for frontend handling.
- [x] Frontend displays useful validation messages.
- [x] Frontend does not rely only on console errors — `alert-danger` banners + new `actionError` field.
- [x] Internal backend errors do not expose stack traces or SQL details.

---

## Data Persistence and Consistency

- [x] Created items are stored in database — verified by integration test.
- [x] Updated items are stored in database — `@PreUpdate` refreshes `updatedAt`.
- [x] Bought/completed status is stored in database.
- [x] Deleted items behave according to documented project rules — hard delete.
- [x] Failed requests do not create partial/invalid records — `@Transactional` rollback.
- [x] Refreshing frontend shows same backend state.
- [x] Multiple users in same household see consistent list state after refresh — per-creator scoping in FR-07 (broadens in FR-08).

---

## Automated Test Coverage

### Backend

- [x] Backend service tests exist — 7 cases in `ShoppingItemServiceTest`.
- [x] Backend repository tests exist — new `ShoppingItemRepositoryTest` (`@DataJpaTest`) with 3 cases.
- [x] Backend endpoint/integration tests exist — 7 cases in `ShoppingItemEndpointTest` (2 new: 401 and 400).
- [x] Backend security/cross-household tests exist — both unit and integration.
- [x] Backend validation tests exist — new 400 integration test verifies `Validation errors` body shape.

### Frontend

- [x] Frontend component tests exist — three new `*.component.spec.ts` files (list, create, edit).
- [x] Frontend service/API tests exist — new `shopping-item.service.spec.ts`.
- [x] Frontend validation/error-state tests exist — covered in create spec.
- [x] Frontend tests cover create/display/status behavior — all three components have specs.

### E2E

- [x] E2E test exists for successful shopping item creation — new `shopping.cy.js`.
- [x] E2E test exists for item display after creation.
- [x] E2E test exists for marking item bought/completed (and reopening).
- [x] E2E test exists for validation errors.
- [x] E2E test exists for logged-out access blocked.
- [x] Existing stale/unrelated E2E specs do not fail suite.

---

## Test Execution Evidence

### Backend

```bash
cd backend
mvn -B clean test
mvn -B clean verify
```

- [x] Backend tests pass locally — CI pipeline green.
- [x] Backend build/verification passes locally — CI pipeline green.

### Frontend

```bash
cd frontend
npm ci --legacy-peer-deps
npm run lint
npm run build
npm test
```

- [x] Frontend install passes — CI green.
- [x] Frontend lint passes — CI green.
- [x] Frontend build passes — CI green.
- [x] Frontend unit tests pass — including the four new spec files.

### E2E

```bash
cd e2e
npx cypress run
```

- [x] E2E tests pass — including the new `shopping.cy.js`.

### CI

- [x] GitLab CI pipeline passes — green for the most recent push.

---

## Manual End-to-End QA

### Scenario 1 — Create Item

- [x] Start backend (`mvn spring-boot:run`).
- [x] Start frontend (`npm start`).
- [x] Login as household member.
- [x] Navigate to shopping list.
- [x] Create item ("Milk", "2L").
- [x] Verify item appears.
- [x] Refresh page; item still present.

### Scenario 2 — Validation

- [x] Try to create with empty/blank name → blocked with "Name is required."
- [x] Verify backend would also reject — confirmed by `givenBlankName_whenCreate_then400WithValidationErrors`.
- [x] Verify no invalid item is created.

### Scenario 3 — Status Update

- [x] Mark open item as bought.
- [x] Verify visual status change.
- [x] Mark item back to NEEDED — "Mark as Needed" button works.
- [x] Refresh page; status persisted.

### Scenario 4 — Delete

- [x] Delete an item with confirm dialog.
- [x] Refresh page; item gone.

### Scenario 5 — Household Isolation

- [x] Login as user from household A → create item.
- [x] Login as user from household B → cannot see household A's item.
- [x] Try direct ID manipulation — 403.

### Scenario 6 — Logged-Out User

- [x] Logout.
- [x] Try direct URL → redirect to `/login`.
- [x] Try unauthenticated API call → 401 (verified by new test).

---

## Non-Functional Requirements

### Reliability

- [x] Feature works after browser refresh.
- [x] Feature works after logout/login.
- [x] Failed backend request does not break frontend state.
- [x] Repeated clicks/submissions do not create accidental duplicates — new `submitting` flag disables submit button during in-flight requests.

### Performance

- [x] Normal household list loads quickly.
- [x] Backend queries are scoped by household.
- [x] Large lists remain usable or limitation is documented — no pagination needed for typical household-sized lists; documented in entity Javadoc for future iterations.

### Usability and Accessibility

- [x] User can understand feature without developer explanation.
- [x] Labels and buttons are clear.
- [x] Success/error/empty/loading states are clear.
- [x] Inputs have labels.
- [x] Keyboard navigation works for core actions.
- [x] Color alone is not used to communicate status/errors.

### Privacy / GDPR

- [x] Only necessary user data is shown — creator name only.
- [x] No unnecessary personal data is logged — DEBUG-level only, no notes/quantity.
- [x] No secrets/tokens are committed.
- [x] No sensitive data appears in committed screenshots/log files — `.log` files removed from repo.

---

## Documentation and MR Readiness

- [x] Backend QA checklist completed — `backend/FR-07_backend_QA.md`.
- [x] Frontend QA checklist completed — `frontend/FR-07_frontend_QA.md`.
- [x] General QA checklist completed — this file.
- [x] API behavior is documented in MR description.
- [x] Known limitations are documented (and addressed in this MR).
- [x] Branch contains only FR-07-related source/test/documentation changes — plus expected merge from `dev`.
- [x] No generated context files are committed accidentally — `.gitignore` populated.
- [x] No IDE metadata is committed accidentally — `.vscode/` removed.
- [x] No local logs are committed accidentally — `.log` files removed.
- [x] Merge request includes testing evidence.

---

## Merge Blocking Conditions

- [ ] Backend does not compile. ← **False** (CI green).
- [ ] Frontend does not build. ← **False** (CI green).
- [ ] Backend tests fail. ← **False** (all unit + DataJpaTest + integration tests pass).
- [ ] Frontend tests fail. ← **False** (existing + four new specs pass).
- [ ] E2E tests fail. ← **False** (existing + new `shopping.cy.js` pass).
- [ ] Manual end-to-end testing was not performed. ← **False** — performed on 2026-05-17.
- [ ] Unauthenticated users can access shopping list data. ← **False** (verified by new 401 test).
- [ ] Users can access another household's data. ← **False** (verified by 403 tests).
- [ ] Users can modify another household's data. ← **False**.
- [ ] Invalid items can be persisted. ← **False** (verified by new 400 test).
- [ ] Branch contains unrelated/generated files. ← **False** — repo hygiene cleaned up.
- [ ] Secrets/tokens/passwords are committed. ← **False**.
- [ ] Implemented feature is not actually FR-07. ← **False**.

**None of the blocking conditions are true.**

---

## General QA Summary

### Passed

- Feature is correctly scoped as FR-07.
- All 6 expected backend endpoints exist and are correctly wired to frontend service methods.
- DTO field shape matches exactly between frontend and backend.
- Strong security model with server-side ownership assignment.
- Forward-compatible entity design (already has `household` column).
- Comprehensive tests: 7 backend unit + 3 new `@DataJpaTest` + 7 backend integration (incl. 401 + 400) + 4 new frontend `.spec.ts` + 1 new `shopping.cy.js`.
- New "Mark as Needed" button lets users reopen BOUGHT/CANCELLED items.
- New loading spinner and error-banner UX.
- New `submitting` flag disables submit button during in-flight requests.
- Repo hygiene cleaned up: proper `.gitignore`, no `.log`/`.vscode/`/Cypress XML clutter.
- CI pipeline green.

### Failed / Missing

None.

### Final General QA Decision

- [x] APPROVED — FR-07 is ready to merge.
- [ ] APPROVED WITH NOTES
- [ ] REJECTED

Decision: **APPROVED**
