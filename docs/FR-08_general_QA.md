# FR-08 General QA Checklist — Targeted Shopping Request Collection

## Feature Information

- Feature ID: FR-08
- Feature name: Targeted shopping request collection
- Use case: UC-04 — Collaborative Shopping Management
- Backend technology: Java Spring Boot
- Frontend technology: Angular (NgModule-based)
- Security: Spring Security, JWT
- Database: H2 (file-based)
- Automated tests: JUnit, Karma/Jasmine, Cypress
- Branch: `FR-08/Targeted-shopping-request-collection`
- Merge request: (link MR before merge)
- Developer: Daniel
- QA reviewer: Taras
- Date: 2026-05-18

---

## Feature Scope Confirmation

- [x] FR-08 is correctly identified as Targeted shopping request collection.
- [x] FR-08 is correctly linked to UC-04.
- [x] Backend and frontend implement the same feature scope — 3 visibility values + targeting on both sides.
- [x] The implemented feature is not accidentally FR-07 only — visibility enum, `targetUser` field, `/targeted-at-me` endpoint, visibility dropdown, "Targeted at Me" tab all present.
- [x] The implemented feature is not expense/payment settlement.
- [x] The implemented feature is not NFC/appliance logging.
- [x] Known limitations are documented (and addressed in this MR).

---

## End-to-End Functional Requirements

### Access and Navigation

- [x] Logged-in user can navigate to FR-08 page — same "Shopping" entry as FR-07.
- [x] Logged-out user cannot access.
- [x] Navigation is visible in the correct place.
- [x] Route names and menu labels are understandable.

### Create Shopping Request

- [x] User can create a targeted shopping request.
- [x] User can enter required information — name + visibility (required).
- [x] User can select target member/audience — visibility dropdown with PERSONAL/TARGETED/SHARED.
- [x] User can enter optional details — quantity + notes.
- [x] Created request is shown immediately.
- [x] Created request is persisted.
- [x] Created request remains visible after refresh/re-login.

### Display Requests

- [x] User can see requests for their own household.
- [x] User can see requests targeted to them — "Targeted at Me" tab.
- [x] User sees understandable empty state.
- [x] Request status is displayed clearly — visibility badge + status badge.

### Update / Status / Delete

- [x] User can edit request.
- [x] User can update request status — recipient can mark bought.
- [x] User can cancel/delete request — creator-only (`canManage`).
- [x] User can reopen a BOUGHT/CANCELLED item — new "Mark as Needed" button.
- [x] Updates are persisted after refresh/re-login.
- [x] Failed updates do not corrupt UI/backend state.

---

## Backend/Frontend Integration

- [x] Frontend API service paths match backend endpoint paths — 7 methods each side.
- [x] Frontend DTO/model fields match backend DTO fields.
- [x] HTTP methods are consistent.
- [x] Frontend sends JWT-authenticated requests.
- [x] Backend receives and validates requests correctly — 9 integration tests verify.
- [x] Backend responses can be displayed correctly by frontend.
- [x] Frontend handles backend success responses.
- [x] Frontend handles backend validation errors — `extractError` parses.
- [x] Frontend handles unauthorized/forbidden responses — new `extractError` surfaces friendly messages.
- [x] Frontend handles not-found responses.
- [x] Frontend handles 409 conflict — surfaces backend message for targeting violations.
- [x] Frontend handles server/network errors safely.

---

## Security and Authorization

- [x] Unauthenticated API requests are rejected — new `givenNoJwt_whenCreate_then401`.
- [x] Authenticated users can only access their own household's requests.
- [x] User cannot see another household's requests — `givenSharedItem_whenUserFromOtherHouseholdGetsAll_thenInvisible`.
- [x] User cannot modify another household's requests.
- [x] User cannot delete another household's requests.
- [x] User cannot target users outside their household — `givenCreateTargetedAtOutsideUser_then409` + UI dropdown limited.
- [x] User cannot target themselves — backend 409 + UI filters self from dropdown.
- [x] Backend derives household/user ownership from authentication.
- [x] Backend does not trust frontend-provided ownership fields.
- [x] Manipulated item IDs do not bypass household isolation.
- [x] Expired/manipulated JWT is rejected.
- [x] No raw JWTs, secrets, or passwords are logged or committed.

---

## Validation and Error Handling

- [x] Empty item name is rejected — new `givenBlankName_whenCreate_then400WithValidationErrors`.
- [x] Overly long name is rejected.
- [x] Visibility is required — `@NotNull`.
- [x] Target email is required when TARGETED — `resolveAndValidateTarget` 409.
- [x] Self-targeting is rejected — backend 409 + UI filters self out.
- [x] Target outside household is rejected — backend 409 + UI dropdown limited.
- [x] Invalid status is rejected — typed enum.
- [x] Unknown item ID is handled correctly — 404.
- [x] Cross-household item ID is handled correctly — 403.
- [x] Backend validation errors are understandable.
- [x] Frontend displays useful validation messages.
- [x] Frontend does not rely only on console errors — new `actionError` banner.
- [x] Internal backend errors do not expose stack traces.

---

## Data Persistence and Consistency

- [x] Created items are stored — verified by integration test.
- [x] Updated items are stored.
- [x] Bought/completed status is stored.
- [x] Visibility and target user are stored — `@Enumerated(STRING)` + `@ManyToOne targetUser`.
- [x] Deleted items behave according to documented project rules — hard delete.
- [x] Failed requests do not create partial/invalid records — `@Transactional` rollback.
- [x] Refreshing frontend shows same backend state.
- [x] Multiple users in same household see consistent list state — visibility matrix covered by integration tests.

---

## Automated Test Coverage

### Backend

- [x] Backend service tests exist — 11 cases.
- [x] Backend repository tests exist — new `ShoppingItemRepositoryTest` (`@DataJpaTest`) with 4 cases covering visibility + targetUser finders + household isolation.
- [x] Backend endpoint/integration tests exist — 9 cases (2 new: 401 + 400).
- [x] Backend security/cross-household tests exist — critical `givenSharedItem_whenUserFromOtherHouseholdGetsAll_thenInvisible`.
- [x] Backend validation tests exist — Bean Validation + targeting invariants covered.

### Frontend

- [x] Frontend component tests exist — three new spec files.
- [x] Frontend service/API tests exist — new `shopping-item.service.spec.ts`.
- [x] Frontend validation/error-state tests exist.
- [x] Frontend tests cover create/display/status behavior.

### E2E

- [x] E2E test exists for successful request creation — new `shopping.cy.js`.
- [x] E2E test for request display after creation.
- [x] E2E test for visibility/tabs behavior.
- [x] E2E test for validation errors.
- [x] E2E test for logged-out access blocked.
- [x] Existing stale/unrelated E2E specs do not fail suite.

---

## Test Execution Evidence

### Backend

- [x] Backend tests pass locally — CI green.
- [x] Backend build/verification passes locally — CI green.

### Frontend

- [x] Frontend install passes.
- [x] Frontend lint passes.
- [x] Frontend build passes.
- [x] Frontend unit tests pass — including four new spec files.

### E2E

- [x] E2E tests pass — including new `shopping.cy.js`.

### CI

- [x] GitLab CI pipeline passes — green for most recent push.

---

## Manual End-to-End QA

### Scenario 1 — Create Request

- [x] Start backend + frontend.
- [x] Login as household member.
- [x] Navigate to shopping list.
- [x] Create item with visibility=TARGETED + roommate.
- [x] Verify item appears with TARGETED badge + "For: \<name\>" line.
- [x] Refresh; item still present.

### Scenario 2 — Validation

- [x] Empty/blank name → blocked.
- [x] TARGETED without roommate → blocked.
- [x] Self-target — unreachable from UI (dropdown filters current user out).
- [x] No invalid item created.

### Scenario 3 — Targeted User Flow

- [x] User A creates TARGETED for user B.
- [x] User B sees in "All Items" + "Targeted at Me".
- [x] User B can mark bought.
- [x] User B cannot delete/edit — friendly error message surfaced.

### Scenario 4 — Household Isolation

- [x] User from household A creates SHARED item.
- [x] User from household B cannot see it — verified by critical test.
- [x] Direct ID manipulation returns 403.

### Scenario 5 — Logged-Out User

- [x] Logout → direct URL → redirect to `/login`.
- [x] Unauthenticated API call → 401 — verified by new test.

---

## Non-Functional Requirements

### Reliability

- [x] Feature works after browser refresh.
- [x] Feature works after logout/login.
- [x] Failed backend request does not break frontend state.
- [x] Repeated clicks/submissions do not create accidental duplicates — new `submitting` flag.

### Performance

- [x] Normal household list loads quickly — `forkJoin` parallel calls for `findAll` + `findTargetedAtMe`.
- [x] Backend queries are scoped by household.
- [x] Large lists remain usable or limitation is documented.

### Usability and Accessibility

- [x] User can understand feature without developer explanation.
- [x] Labels and buttons are clear.
- [x] Success/error/empty/loading states are clear.
- [x] Inputs have labels.
- [x] Keyboard navigation works for core actions.
- [x] Color alone is not used to communicate status/errors.

### Privacy / GDPR

- [x] Only necessary user data is shown.
- [x] No unnecessary personal data is logged.
- [x] No secrets/tokens are committed.
- [x] No sensitive data appears in committed screenshots/log files — `.log` files removed.

---

## Documentation and MR Readiness

- [x] Backend QA checklist completed.
- [x] Frontend QA checklist completed.
- [x] General QA checklist completed.
- [x] API behavior is documented in MR description.
- [x] Known limitations are documented (and addressed in this MR).
- [x] Branch contains only FR-08-related changes.
- [x] No generated context files committed.
- [x] No IDE metadata committed.
- [x] No local logs committed.
- [x] Merge request includes testing evidence.

---

## Merge Blocking Conditions

- [ ] Backend does not compile. ← **False** (CI green).
- [ ] Frontend does not build. ← **False** (CI green).
- [ ] Backend tests fail. ← **False** (unit + DataJpaTest + integration all pass).
- [ ] Frontend tests fail. ← **False** (existing + four new specs pass).
- [ ] E2E tests fail. ← **False** (existing + new spec pass).
- [ ] Manual end-to-end testing was not performed. ← **False**.
- [ ] Unauthenticated users can access data. ← **False** (verified by new 401 test).
- [ ] Users can access another household's data. ← **False** (critical SHARED isolation test).
- [ ] Users can modify another household's data. ← **False**.
- [ ] Users can target users outside their household. ← **False**.
- [ ] Invalid items can be persisted. ← **False** (verified by new 400 test).
- [ ] Branch contains unrelated/generated files. ← **False** — repo hygiene cleaned up.
- [ ] Secrets/tokens/passwords are committed. ← **False**.
- [ ] Implemented feature is not actually FR-08. ← **False**.

**None of the blocking conditions are true.**

---

## General QA Summary

### Passed

- Feature correctly scoped as FR-08.
- Clean evolution of FR-07 surface — same entity table, REST surface, components — extended where appropriate.
- Three-mode visibility model represented at every layer.
- Four targeting invariants enforced server-side (required when TARGETED, no self-target, real user, same household) with clear 409/404 mappings.
- Bifurcated authorization: `canView` broadens; `canManage` stays strict.
- **Critical security guarantee verified**: SHARED visibility does NOT leak across households.
- 11 service unit + 4 new `@DataJpaTest` + 9 integration tests (2 new: 401 + 400).
- Frontend defense-in-depth: target dropdown only contains current household's roommates, AND filters out the current user; self-target is unreachable from the UI.
- Four new frontend `.spec.ts` files; new `shopping.cy.js` Cypress spec.
- New loading spinner, error banner, "Mark as Needed", `submitting` flag.
- Repo hygiene cleaned up.
- CI pipeline green.

### Failed / Missing

None.

### Final General QA Decision

- [x] APPROVED — FR-08 is ready to merge.
- [ ] APPROVED WITH NOTES
- [ ] REJECTED

Decision: **APPROVED**
