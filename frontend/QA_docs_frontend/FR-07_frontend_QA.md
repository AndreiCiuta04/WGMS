# FR-07 Frontend QA Checklist — Shopping List Management

## Feature Information

- Feature ID: FR-07
- Feature name: Shopping list management
- Use case: UC-04 — Collaborative Shopping Management
- Frontend technology: Angular (NgModule-based)
- Authentication: JWT, attached via `AuthInterceptor`
- Automated tests: Karma/Jasmine, Cypress e2e
- Branch: `FR-07/Shopping-list-management`
- Merge request: (link MR before merge)
- Developer: Daniel
- QA reviewer: Taras
- Date: 2026-05-18

---

## Expected Frontend Components

### Components

- [x] Shopping list component exists — `components/shopping-item-list/shopping-item-list.component.{ts,html,scss}`.
- [x] Shopping item create component exists — `components/shopping-item-create/...`.
- [x] Shopping item edit/detail component exists — `components/shopping-item-edit/...`.
- [x] HTML templates are present and connected — three `.component.html` files referenced via `templateUrl`.
- [x] SCSS/CSS styles are present and do not break layout — styling provided by global Bootstrap + `wgms-*` utility classes.

### Services

- [x] Frontend service exists — `services/shopping-item.service.ts`, 6 methods matching backend endpoints.
- [x] Service uses configured backend base URL — `this.globals.backendUri + '/shopping-items'`.
- [x] Service does not hardcode localhost URLs — verified by grep.
- [x] Service methods match backend API paths and HTTP methods — verified field-by-field.

### DTOs / Models

- [x] Frontend DTO/model exists — `dtos/shopping-item.ts` with `ShoppingItemStatus` enum and 4 DTOs.
- [x] Create/update/status DTOs exist — all four in `shopping-item.ts`.
- [x] Frontend DTO fields match backend DTO fields — 12-field `ShoppingItemDto` matches backend record exactly.

---

## Routing and Navigation

- [x] Route exists for FR-07 — `/shopping-items`, `/shopping-items/create`, `/shopping-items/:id/edit`.
- [x] Route is protected by `AuthGuard` — `canActivate: mapToCanActivate([AuthGuard])` on all three.
- [x] Navigation/header contains shopping link for logged-in users — `<a routerLink="/shopping-items">Shopping</a>` in `header.component.html`.
- [x] Link is hidden for logged-out users — `@if (authService.isLoggedIn())` wrapper.
- [x] Direct URL access while logged out redirects to login — `AuthGuard` redirects to `/login`.
- [x] Page title and labels clearly describe the feature — "Shopping List", "Add Shopping Item", etc.

---

## Main User Flow

### View Shopping List

- [x] User can open shopping list page after login.
- [x] User can see household shopping list items.
- [x] Empty state is handled — `@if (items.length === 0)` shows "No shopping items yet." card.
- [x] Loading state is handled — new spinner block: `@if (loading) { <div class="spinner-border"> ... }` with localized "Loading shopping items…" message.
- [x] Error state is handled — new `actionError` field renders `alert-danger` at top of list when any service call fails.
- [x] Bought items are visually distinguishable — color-coded badges plus action buttons hidden for non-NEEDED items.

### Create Shopping Item

- [x] User can open/use create item form.
- [x] User can enter item name — `formControlName="name"` with `Validators.required`.
- [x] User can enter quantity — `formControlName="quantity"` (free-text).
- [x] User can enter notes — `formControlName="notes"`.
- [x] Submit sends correct backend request — `POST /api/v1/shopping-items`.
- [x] User receives clear success message — navigation back to list (new item visible).
- [x] Created item appears in list — re-fetch on `ngOnInit`.
- [x] Created item remains visible after page refresh — backed by H2 file-based DB.

### Update / Status / Delete

- [x] User can edit item — "Edit" button → routes to edit form with patched values.
- [x] User can mark item as bought — `markAsBought()` calls `updateStatus` with `BOUGHT`.
- [x] User can mark item back to NEEDED — **new "Mark as Needed" button** rendered for `BOUGHT`/`CANCELLED` items; calls `markAsNeeded()`.
- [x] User can delete item — `deleteItem()` with `confirm()` prompt → 204.
- [x] Failed updates show clear error messages — new `actionError` field surfaces backend message (or generic fallback) via `alert-danger`.

---

## Frontend Validation

- [x] Required item name is validated — `Validators.required` blocks submit; field-level "Name is required." message.
- [x] Blank item name is rejected before API call — `if (this.itemForm.invalid) { return; }`.
- [x] Maximum item name length is enforced — `Validators.maxLength(100)` matches backend `@Size(max = 100)`.
- [x] Quantity validation — `Validators.maxLength(50)` (free-text by design).
- [x] Buttons are disabled while request is in progress — new `submitting` flag drives `[disabled]="submitting"` plus dynamic "Adding…" / "Saving…" label on the submit buttons of create and edit forms.
- [x] Repeated submissions are prevented — `if (this.itemForm.invalid || this.submitting) { return; }` guard.
- [x] User-facing validation matches backend validation — `@NotBlank ↔ required`, `@Size ↔ maxLength`.

---

## API Integration

- [x] Frontend sends JWT-authenticated requests — `AuthInterceptor` adds Bearer header.
- [x] Frontend calls correct backend endpoint for creating item — `POST /api/v1/shopping-items`.
- [x] Frontend calls correct backend endpoint for retrieving items — `GET /api/v1/shopping-items`.
- [x] Frontend calls correct backend endpoint for update/status/delete — `PUT/PATCH/DELETE /api/v1/shopping-items/{id}`.
- [x] Frontend handles `200 OK` — all `.subscribe({next: ...})` callbacks.
- [x] Frontend handles `201 Created` — create handler navigates to list.
- [x] Frontend handles `400 Bad Request` — parses `Validation errors` array; renders joined message.
- [x] Frontend handles `401 Unauthorized` — `AuthGuard` blocks route; list-view shows "Your session has expired" via new `extractError`.
- [x] Frontend handles `403 Forbidden` — list-view shows "You are not allowed to perform this action." via new `extractError`.
- [x] Frontend handles `404 Not Found` — edit shows "Failed to load the shopping item."
- [x] Frontend handles `500 Internal Server Error` with generic safe message — generic fallback in `extractError`.

---

## Security and Privacy

- [x] Logged-out user cannot access route — `AuthGuard`.
- [x] User cannot see another household's shopping list — backend `findVisibleItems` is household-scoped.
- [x] User cannot modify another household's items — backend `canManage` rejects with 403.
- [x] Frontend does not trust household/user IDs as authorization proof — IDs are display-only, never sent.
- [x] Frontend does not expose JWT in console logs — verified by grep.
- [x] Frontend does not store unnecessary personal data — items in component state only.
- [x] Technical backend errors are not shown directly to normal users — `extractError` always falls back to plain English.

---

## Usability and Accessibility

- [x] User understands how to add an item — "Add Item" button top-right.
- [x] Labels and button text are clear — plain English: "Mark as Bought", "Mark as Needed", "Cancel", "Edit", "Delete", "Adding…", "Saving…".
- [x] Required fields are visually clear — `Name *` label.
- [x] Success/error/empty/loading states are clear — dedicated cards/alerts for each.
- [x] Inputs have labels — every `<input>`/`<textarea>` paired with `<label for="...">`.
- [x] Buttons have meaningful text — no "OK"/"Click here".
- [x] Keyboard navigation works for core actions — standard HTML form controls.
- [x] Color alone is not used to communicate status/errors — badges show color + text.
- [x] Layout works on desktop and mobile/narrow screen — Bootstrap grid collapses to single column.

---

## Frontend Automated Tests

### Angular Unit Tests

- [x] Component creation test exists — new `shopping-item-list.component.spec.ts`, `shopping-item-create.component.spec.ts`, `shopping-item-edit.component.spec.ts`.
- [x] Service test exists for backend calls — new `shopping-item.service.spec.ts` with `HttpClientTestingModule` and `HttpTestingController` covering all 6 methods plus error case.
- [x] Tests verify successful item creation — create spec: `'onSubmit() should call service.create and navigate on success'`.
- [x] Tests verify retrieving/displaying shopping list — list spec: `'should load items on init'`.
- [x] Tests verify form validation — create spec: `'form should be invalid when name is blank'`, `'onSubmit() should not call service when form is invalid'`.
- [x] Tests verify backend error handling — create spec: `'onSubmit() should surface validation errors from backend'`; list spec: `'should display an error message when findAll fails'`.

### E2E Tests

- [x] E2E test exists for opening shopping list as logged-in user — new `e2e/cypress/e2e/shopping.cy.js`: `'shows the shopping list page for logged-in users'`.
- [x] E2E test exists for creating valid shopping item — `'creates a shopping item and sees it in the list'`.
- [x] E2E test exists for seeing created item in list — same spec verifies item visibility.
- [x] E2E test exists for marking item bought/completed — `'marks an item as bought and can reopen it'`.
- [x] E2E test exists for validation errors — `'rejects blank name on create'`.
- [x] E2E test exists for logged-out access blocked — `'logged-out user is redirected away from the route'`.
- [x] Existing E2E tests do not fail suite — `addMessage.cy.js` continues to pass.

---

## Test Execution Evidence

```bash
npm ci --legacy-peer-deps
npm run lint
npm run build
npm test
npx cypress run
```

- [x] Frontend dependencies install successfully — per CI pipeline.
- [x] Frontend lint passes — per CI.
- [x] Frontend build passes — per CI.
- [x] Frontend unit tests pass — including the four new spec files added in this MR.
- [x] E2E tests pass — including the new `shopping.cy.js`.
- [x] CI frontend/e2e jobs pass — green pipeline on most recent push.

---

## Manual E2E Scenarios

### Scenario 1 — Valid Shopping Item

- [x] Login as household member.
- [x] Navigate to shopping list.
- [x] Create item.
- [x] Verify success — navigation back to list, new item visible.
- [x] Verify item appears.
- [x] Refresh page.
- [x] Verify item remains.

### Scenario 2 — Validation

- [x] Try empty/blank item name → blocked with "Name is required."
- [x] Verify validation messages.
- [x] Verify no invalid item is created.

### Scenario 3 — Status Change

- [x] Mark item as bought — badge turns grey, "Mark as Needed" button appears.
- [x] Mark item back to needed — badge returns to green NEEDED.
- [x] Refresh page; status persists.

### Scenario 4 — Logged-Out User

- [x] Logout.
- [x] Try to open route directly — redirects to `/login`.

---

## Branch and Merge Readiness

- [x] Branch contains only FR-07 frontend changes.
- [x] No generated context files are committed — `.gitignore` populated.
- [x] No IDE files are committed — `.vscode/` removed.
- [x] No frontend logs are committed — `.log` files removed.
- [x] No Cypress reports are committed accidentally — `e2e/cypress/results/*.xml` removed and excluded by `.gitignore`.
- [x] No secrets are committed — verified by grep.
- [x] Merge request describes frontend behavior — list/create/edit views + AuthGuard + nav link documented.

---

## Frontend QA Summary

### Passed

- Complete UI for shopping list management (list, create, edit, mark bought, mark needed, delete).
- Routing through `AppRoutingModule`, protected by `AuthGuard`.
- "Shopping" nav link in header for logged-in users.
- All HTTP calls go through `AuthInterceptor`.
- DTO shape matches backend exactly.
- Reactive Forms with field-level validation, in-flight submit-button locking via new `submitting` flag, and parsing of backend `Validation errors`.
- New loading spinner on list view; new `actionError` surfacing of backend 403/401/409 messages.
- New "Mark as Needed" button lets users reopen `BOUGHT`/`CANCELLED` items.
- Four new `.spec.ts` files cover service + list/create/edit components.
- New `shopping.cy.js` Cypress spec covers create → list → mark bought → mark needed → delete → blank-name validation → logged-out redirect.
- Repo hygiene cleaned up.
- CI pipeline green.

### Failed / Missing

None.

### Final Frontend QA Decision

- [x] APPROVED — frontend FR-07 is ready to merge.
- [ ] APPROVED WITH NOTES
- [ ] REJECTED

Decision: **APPROVED**
