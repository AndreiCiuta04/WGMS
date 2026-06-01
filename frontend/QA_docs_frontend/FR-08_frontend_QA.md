# FR-08 Frontend QA Checklist — Targeted Shopping Request Collection

## Feature Information

- Feature ID: FR-08
- Feature name: Targeted shopping request collection
- Use case: UC-04 — Collaborative Shopping Management
- Frontend technology: Angular (NgModule-based)
- Authentication: JWT, attached via `AuthInterceptor`
- Automated tests: Karma/Jasmine, Cypress e2e
- Branch: `FR-08/Targeted-shopping-request-collection`
- Merge request: (link MR before merge)
- Developer: Daniel
- QA reviewer: Taras
- Date: 2026-05-18

---

**Implementation note:** FR-08 extends the FR-07 shopping-item UI. Same three components, same route, same nav link. New UI elements: visibility dropdown (create + edit), conditional target-roommate picker (TARGETED only), All Items / Targeted at Me tabs, visibility badge, "For: \<name\>" line for TARGETED items.

---

## Expected Frontend Components

### Components

- [x] Shopping request list component exists — extended `shopping-item-list.component.{ts,html,scss}` with tabs + visibility badge.
- [x] Shopping request create component exists — extended `shopping-item-create.component.{ts,html,scss}` with visibility dropdown + conditional target picker.
- [x] Shopping request edit/detail component exists — extended `shopping-item-edit.component.{ts,html,scss}`.
- [x] HTML templates are present and connected.
- [x] SCSS/CSS styles are present and do not break layout.

### Services

- [x] Frontend service exists — extended `shopping-item.service.ts` with new `findTargetedAtMe()` calling `GET /shopping-items/targeted-at-me`.
- [x] Service uses configured backend base URL.
- [x] Service does not hardcode localhost URLs.
- [x] Service methods match backend API paths and HTTP methods — 7 methods now.

### DTOs / Models

- [x] Frontend DTO/model exists — `dtos/shopping-item.ts` adds `ShoppingItemVisibility` enum + extends DTOs with visibility + target user fields.
- [x] Create/update/status DTOs exist.
- [x] Frontend DTO fields match backend DTO fields — verified field-by-field.

---

## Routing and Navigation

- [x] Route exists for FR-08 — same three routes as FR-07; FR-08 is integrated into the existing surface.
- [x] Route is protected by `AuthGuard`.
- [x] Navigation/header contains feature link — same "Shopping" nav entry reused.
- [x] Link is hidden for logged-out users.
- [x] Direct URL access while logged out redirects to login.
- [x] Page title and labels clearly describe targeted shopping requests — tabs "All Items" / "Targeted at Me"; form labels "Visibility *", "Ask which roommate? *".

---

## Main User Flow

### Create Shopping Request

- [x] User can open FR-08 page after login.
- [x] User can create a request.
- [x] User can enter request title — required, max 100.
- [x] User can select target member/audience — visibility dropdown + conditional target dropdown populated from `HouseholdService.getMembers()`.
- [x] User can enter quantity/notes — optional, length-validated.
- [x] Submit sends correct backend request — POST with `visibility` and optional `targetUserEmail`.
- [x] User receives clear success message — navigation back to list.
- [x] Created request appears in list.
- [x] Created request remains visible after page refresh.

### View / Update Requests

- [x] User can see household requests — "All Items" tab.
- [x] User can see requests targeted to them — "Targeted at Me" tab calls `findTargetedAtMe()`.
- [x] Empty/loading/error states are handled — new loading spinner, empty card, new `actionError` banner.
- [x] User can update/status/delete request if supported.
- [x] Failed updates show clear error messages — list-view actions now surface backend message via new `extractError` (was previously only `console.error`).

---

## Frontend Validation

- [x] Required request title is validated.
- [x] Blank required fields are rejected before API call.
- [x] Target member is required if backend requires it — inline guard `if (this.isTargeted() && !targetUserEmail) return;`.
- [x] Buttons are disabled while request is in progress — new `submitting` flag drives `[disabled]="submitting"` on submit buttons.
- [x] Repeated submissions are prevented — same `submitting` guard.
- [x] User-facing validation matches backend validation.

---

## API Integration

- [x] Frontend sends JWT-authenticated requests.
- [x] Frontend calls correct backend endpoint for creating request.
- [x] Frontend calls correct backend endpoint for retrieving requests.
- [x] Frontend calls correct backend endpoint for my/targeted requests — `GET /shopping-items/targeted-at-me`.
- [x] Frontend calls correct backend endpoint for update/status/delete.
- [x] Frontend handles `200 OK`.
- [x] Frontend handles `201 Created`.
- [x] Frontend handles `400 Bad Request` — parses `Validation errors`.
- [x] Frontend handles `401 Unauthorized` — `AuthGuard` blocks route; in-flight session expiry surfaces via `extractError`.
- [x] Frontend handles `403 Forbidden` — `extractError` returns "You are not allowed to perform this action."
- [x] Frontend handles `404 Not Found`.
- [x] Frontend handles `409 Conflict` — surfaces backend message ("Cannot target a shopping request at yourself…" etc.).
- [x] Frontend handles `500 Internal Server Error` with generic safe message.

---

## Security and Privacy

- [x] Logged-out user cannot access route.
- [x] User cannot see another household's requests — backend household-scoped + critical test `givenSharedItem_whenUserFromOtherHouseholdGetsAll_thenInvisible`.
- [x] User cannot modify another household's requests.
- [x] User cannot target users outside their household from UI — dropdown populated only with current household's roommates; backend rejects with 409 if bypassed.
- [x] Current user is filtered out of target dropdown — new filter in both create and edit components using `AuthService.getUserEmail()`; makes self-target unreachable from the UI.
- [x] Frontend does not trust household/user IDs as authorization proof.
- [x] Frontend does not expose JWT in console logs.
- [x] Frontend does not store unnecessary personal data.
- [x] Technical backend errors are not shown directly to normal users.

---

## Frontend Automated Tests

### Angular Unit Tests

- [x] Component creation tests exist — new `shopping-item-list/create/edit.component.spec.ts`.
- [x] Service test exists — new `shopping-item.service.spec.ts` covers `findTargetedAtMe`, visibility + targetUserEmail body, 409 error path.
- [x] Tests verify successful request creation — create spec: `'sends targetUserEmail when TARGETED is chosen'`.
- [x] Tests verify retrieving/displaying requests — list spec: `'should fetch both all-items and targeted-at-me on init'`, `'switchTab() swaps displayed items'`.
- [x] Tests verify form validation — create spec: `'rejects submit when TARGETED but no target chosen'`; tests verify `omits targetUserEmail when not TARGETED`.
- [x] Tests verify backend error handling — create spec: `'surfaces 409 error message from backend'`; list spec: `'surfaces 403 from backend on action failures'`.
- [x] Test verifies current user is filtered from target dropdown — create spec: `'filters the current user out of the roommates dropdown'`; edit spec: `'filters current user from roommates'`.

### E2E Tests

- [x] E2E test exists for opening FR-08 page as logged-in user — new `e2e/cypress/e2e/shopping.cy.js`: `'shows the All Items and Targeted at Me tabs'`.
- [x] E2E test exists for creating valid request — `'creates a SHARED item and shows the visibility badge'`.
- [x] E2E test exists for seeing created request in list.
- [x] E2E test exists for visibility dropdown behavior — `'shows the visibility dropdown on the create form, defaulting to Personal'`.
- [x] E2E test exists for validation errors — `'rejects blank name on create'`.
- [x] E2E test exists for logged-out access blocked — `'logged-out user is redirected away from the route'`.
- [x] Existing E2E tests do not fail suite.

---

## Test Execution Evidence

```bash
npm ci --legacy-peer-deps
npm run lint
npm run build
npm test
npx cypress run
```

- [x] Frontend dependencies install successfully — per CI.
- [x] Frontend lint passes — per CI.
- [x] Frontend build passes — per CI.
- [x] Frontend unit tests pass — including four new spec files.
- [x] E2E tests pass — including new `shopping.cy.js`.
- [x] CI frontend/e2e jobs pass — green for most recent push.

---

## Manual E2E Scenarios

### Scenario 1 — Valid Targeted Shopping Request

- [x] Login, create request with visibility=TARGETED + roommate.
- [x] Verify success — new item visible with TARGETED badge and "For: \<name\>" line.
- [x] Refresh; remains.

### Scenario 2 — Validation

- [x] Try empty name → blocked.
- [x] Try TARGETED without selecting roommate → blocked.
- [x] Self-target — **unreachable from the UI** because current user is filtered from the dropdown.

### Scenario 3 — Targeted User Flow

- [x] User A creates TARGETED request for user B.
- [x] User B sees it in "All Items" and "Targeted at Me" tabs.
- [x] User B can mark bought (backend allows via `canView`).
- [x] User B cannot delete or edit (backend rejects with 403; new error banner shows clear message).

### Scenario 4 — Logged-Out User

- [x] Logout → direct URL → redirect to `/login`.

---

## Branch and Merge Readiness

- [x] Branch contains only FR-08 frontend changes — plus expected `dev` merge for task content.
- [x] No generated context files are committed — `.gitignore` populated.
- [x] No IDE files are committed — `.vscode/` removed.
- [x] No frontend logs are committed — `.log` files removed.
- [x] No Cypress reports are committed accidentally — `e2e/cypress/results/*.xml` removed.
- [x] No secrets are committed.
- [x] Merge request describes frontend behavior — visibility dropdown, conditional target picker, tabs, badge, "For:" line.

---

## Frontend QA Summary

### Passed

- Clean extension of FR-07 UI; visibility dropdown, conditional target picker, two-tab list view, visibility badge, "For: \<name\>" line.
- Target dropdown populated from real roommates; **current user filtered out** so self-target is unreachable from the UI.
- DTOs and service methods match the backend exactly.
- All HTTP calls go through `AuthInterceptor`.
- Four new `.spec.ts` files cover service + list/create/edit, including filter-self and 409-error paths.
- New `shopping.cy.js` Cypress spec covers tabs, visibility dropdown defaulting to PERSONAL, SHARED item with badge, mark-bought + reopen, delete, blank-name validation, logged-out redirect.
- New loading spinner + error banner on list view.
- New `submitting` flag disables submit button during in-flight requests.
- New "Mark as Needed" reopens BOUGHT/CANCELLED items.
- Repo hygiene cleaned up.
- CI pipeline green.

### Failed / Missing

None.

### Final Frontend QA Decision

- [x] APPROVED — frontend FR-08 is ready to merge.
- [ ] APPROVED WITH NOTES
- [ ] REJECTED

Decision: **APPROVED**
