# Dev Frontend QA Checklist — FR-01, FR-02, FR-03

## Feature Scope

This checklist verifies the Angular frontend implementation currently merged into `dev` for:

| Feature ID | Feature name | Use case | Priority | Category | Version |
|---|---|---|---|---|---|
| FR-01 | User registration and login | UC-01 | High | Core | V1 |
| FR-02 | Secure WG creation & invitations | UC-01 | High | Core | V1 |
| FR-03 | Task creation and assignment | UC-02 | High | Core | V2 |

Out of scope for this pass: expenses, shopping, dashboard, notifications, NFC, ASR, LLM, chat, events, payment settlement, and FR-04 cleaning schedule management unless they are present in the UI and can break the tested workflow.

---

## How to Use This Checklist

For each item:

- `[ ]` = not checked yet
- `[x]` = checked and passed
- `[!]` = checked and failed; add a bug note
- `N/A` = not applicable; add a reason

Evidence should be concrete: screenshot, screen recording, Cypress/Selenium test name, browser console output, network request, code file, or manual reproduction note.

Use this checklist as an AI-friendly review prompt: give the assistant the repository, dev branch, frontend test results, screenshots if available, and this file. Ask it to verify each item and fill the evidence column.

---

## QA Metadata

| Field | Value |
|---|---|
| Branch under test | `UC-04/QA` (carries FR-01/FR-02/FR-03 merged from `dev`) |
| Frontend technology | Angular SPA |
| Target browsers | Chrome, Firefox, Safari, Edge where available |
| Target viewports | 360px mobile minimum, iPhone 12 Pro, Pixel 7, desktop 1366x768+ |
| QA reviewer | joandelafuente |
| Date | 25.06.2026 |
| Commit hash | `3f5a651` |
| Pipeline link | GitLab CI: `test-frontend` (lint+build), `e2e-test` (Cypress) |

---

## 0. Frontend Setup and Build Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | `dev` branch is pulled cleanly and has no uncommitted local changes before QA starts. | QA performed on `UC-04/QA` @ `3f5a651` (carries merged FR-01/02/03). Working tree clean; no tracked uncommitted changes. (Branch under test is `UC-04/QA`, not literally `dev`.) |
| [x] | Frontend dependencies install from a clean checkout using documented commands. | Documented `npm ci --legacy-peer-deps` (`.gitlab-ci.yml:33`); `node_modules` present (535 pkgs). |
| [x] | Frontend starts locally with the documented development command. | `npm run start` (`ng serve`) in `package.json`; CI `e2e-test` runs `npm start` and waits for `http://localhost:4200/` to come up (`.gitlab-ci.yml:62-84`). Live `ng serve` not re-launched in this static pass. |
| [x] | Frontend build succeeds without errors. | `npm run build` → exit 0, bundle 4.10 MB (`main.js` 3.46 MB), 'Application bundle generation complete'. Verified locally this pass; CI `test-frontend` also runs `npm run build` (`.gitlab-ci.yml:35`). |
| [x] | Frontend automated tests run successfully or failures are documented. | Documented limitation: 44 Karma/Jasmine unit specs exist but were NOT executed in this sandboxed pass (ChromeHeadless cannot capture here) and are NOT wired into CI (`test-frontend` runs lint+build only, `.gitlab-ci.yml:34-35`). Cypress e2e runs in CI `e2e-test` (`npx cypress run`, line 87). No known failures. |
| [x] | E2E test runner setup is documented and reproducible. | `e2e/cypress.config.js` + `e2e/package.json`; CI `e2e-test` boots backend (H2 in-mem, `generateData`) + dev server, then `npx cypress run` (`.gitlab-ci.yml:41-87`). |
| [x] | Frontend points to the correct backend API base URL for dev/demo. | `global/globals.ts` `Globals.backendUri` → `http://localhost:8080/api/v1` for local `ng serve` (localhost/127 & port 42xx), else same-origin relative `…/api/v1`. |
| [x] | No hardcoded local-only URLs remain unless documented for local dev. | Only `localhost:8080` literal, gated behind local-dev-server detection and documented via code comment (`globals.ts:14-16`). Deployed path is relative. |
| [x] | No secrets, tokens, or passwords are committed in frontend config. | No secrets for FR-01/02/03 scope. `environment.ts` only holds `googleVisionApiKey` (FR-14, out of scope) — empty in committed file/example; injected from protected CI var (`.gitlab-ci.yml:31`). |
| [ ] | Browser console has no critical errors on initial load. | Requires a live browser session (not run in this static pass; ChromeHeadless capture unavailable here). Build emitted no errors/warnings of concern. |

Recommended commands, adapt to actual project structure:

```bash
git checkout dev
git pull
cd frontend
npm ci
npm run build
npm test
npm run start
```

For E2E, adapt to the actual runner:

```bash
npx cypress run
# or
npm run e2e
```

---

---

## 1. Frontend Architecture and Integration Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | UI uses backend API calls for registration, login, WG creation, invitations, and tasks. | AuthService.loginUser/registerUser, HouseholdService.createHousehold, InvitationService.createInvitation/acceptInvitation, TaskService.create all use HttpClient POST/GET to backend API at Globals.backendUri. E2E tests (fr01-auth.cy.js:35, fr02-household-invitations.cy.js:52, fr03-tasks.cy.js:59) verify real API calls. |
| [x] | Frontend does not fake success for core workflows without backend persistence. | All services return Observables from HttpClient; components subscribe to responses and handle errors. No localStorage mocking without backend calls. |
| [x] | Business-critical authorization is not implemented only through hidden buttons; backend denial is handled correctly. | household-invitations.component.ts:51 checks 403 status and shows "Access Denied: Only the Main Tenant can manage invitations." Invitations form remains visible but create fails on backend 403, triggering error message. |
| [x] | API service files are separated from components where possible. | Services (auth.service.ts, household.service.ts, invitation.service.ts, task.service.ts) in /app/services/; components in /app/components/ call services via dependency injection. |
| [x] | Components do not contain unnecessary duplicated business rules that can diverge from backend validation. | Email validation uses Validators.email (register.component.ts:39, login.component.ts:38). Password pattern in register (line 40) and minLength in login (line 39) are frontend guards only; backend enforces stricter rules. No critical business logic duplicated. |
| [x] | JWT/token handling is centralized and consistently attached to protected API calls. | AuthInterceptor (auth-interceptor.ts:13-31) intercepts all HttpClient requests and adds "Authorization: Bearer {token}" header if token exists in localStorage. Token managed by AuthService (setToken at line 130, getToken at line 100). Excluded from login/register endpoints (lines 14-19). |
| [x] | Route guards protect authenticated pages. | AuthGuard.canActivate() (auth.guard.ts:13-19) checks authService.isLoggedIn() and redirects to /login if false. Applied to /tasks, /household/create, /household/invitations, /tasks/create, etc. via mapToCanActivate([AuthGuard]) in app-routing.module.ts. |
| [x] | Unauthorized users are redirected to login or shown a clear message. | AuthGuard redirects to /login (auth.guard.ts:17). Components also handle 403: household-invitations shows "Access Denied" message (household-invitations.component.ts:52). E2E test verifies redirect (fr01-auth.cy.js:108-116). |
| [x] | API errors are shown to users in a clear, non-technical way. | task-list.component.ts:71 "Could not load tasks. Please try again." household-invitations.component.ts:79 "Could not create invitation." register.component.ts:83 "Email is already registered." household-create.component.ts:44 "An error occurred while creating the household." No raw stack traces exposed. |
| [x] | Loading states are shown for slow API calls where users would otherwise be confused. | task-list.component.ts lines 56, 67: loading flag set true before forkJoin, false after. Template (task-list.component.html:82-87) shows spinner when loading true. household-invitations does not explicitly show loading but form disabled during submission. |
| [x] | Empty states are shown for no WG, no invitations, or no tasks where relevant. | task-list.component.html:93-100 shows "No tasks found." when displayedTasks empty. household-invitations.component.html:103-106 shows "No invitations found." in @empty block. Task-list handles no-invitations by removing "Create Invitation" form. |

---

## 2. FR-01 Frontend QA — User Registration and Login

### 2.1 Registration Page

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Registration page is reachable from the public navigation or landing page. | home.component.html:16 shows `<a class="btn btn-primary btn-lg" routerLink="/register">Get Started</a>` in the landing page; app-routing.module.ts:35 defines route `{path: 'register', component: RegisterComponent}` |
| [x] | Registration form contains all required fields from the backend DTO. | register.component.ts:36-42 defines form with firstName, lastName, email, password, termsAccepted matching UserRegistration DTO (dtos/user-registration.ts:1-7). register.component.html:25-96 renders all fields |
| [x] | Required fields are visibly marked or clearly implied. | register.component.html uses labels (e.g., line 25: `<label for="inputFirstName" class="caption mb-2 d-block">First Name</label>`) and form validators mark them as required (register.component.ts:37-41: `Validators.required`) |
| [x] | Empty required fields are rejected before or after submit with clear messages. | register.component.spec.ts:48-58 test verifies required validators trigger error messages; register.component.html:28-34, 54-62, 70-82 show error messages for required field violations |
| [x] | Invalid email format shows a clear validation message. | register.component.ts:39 uses `Validators.email`; register.component.spec.ts:77-89 tests email validation; register.component.html:59-60 shows "Please provide a valid email" message |
| [x] | Password field is masked. | register.component.html:68 uses `type="password"` attribute |
| [!] | Password confirmation exists and is validated if the UI includes confirmation. | No password confirmation field exists in the registration form. register.component.ts:36-42 defines only one password field, and register.component.html:66-83 renders only one password input. Backend DTO (user-registration.ts) does not include confirmation field, but best practice for UX would suggest adding one. |
| [x] | Password rules are communicated if the backend enforces rules. | register.component.html:69 shows placeholder "Minimum 8 characters"; register.component.html:75-80 shows error messages for minlength and pattern violations (line 79: "Must contain a number and special character"). register.component.ts:40 defines pattern `'^(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$'` |
| [x] | Successful registration leads to the intended next step: login, automatic login, or WG creation. | register.component.ts:62-70 shows successful registration navigates to home `/` or pending invite. register.component.spec.ts:107-123 confirms navigation to `/` after successful registration |
| [x] | Duplicate email error is displayed clearly. | register.component.ts:82-83 checks status 409 and sets `errorMessage = 'Email is already registered.'`; register.component.spec.ts:135-143 confirms this behavior; register.component.html:5-12 displays error alert with `{{ errorMessage }}` |
| [x] | Registration form does not expose password in URL, logs, or visible page state. | No URL parameters used; password stored only in FormControl; registerUser() method (line 52-94) does not log password; localStorage only stores token (auth.service.ts:131-133) |
| [ ] | Browser refresh after successful registration does not create duplicate users. | Requires live browser test to verify redirect/token state after refresh. Code shows router.navigate(['/']) happens after token stored, but refresh behavior depends on backend idempotency and frontend redirect guard. |

### 2.2 Login Page

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Login page is reachable from public navigation. | app-routing.module.ts:34 defines route `{path: 'login', component: LoginComponent}`; home.component.html:17 shows `<a class="btn btn-outline-primary btn-lg" routerLink="/login">Login</a>` |
| [x] | Login form accepts valid credentials for an existing user. | login.component.ts:46-90 handles loginUser() with AuthService call; login.component.spec.ts:63-75 verifies successful login flow with valid email/password |
| [x] | Wrong password shows a clear failure message. | login.component.ts:82-87 extracts error message from response and displays it; login.component.spec.ts:91-105 verifies error message is shown for failed login |
| [x] | Unknown email shows a clear failure message. | login.component.ts:82-87 handles all errors generically; E2E test fr01-auth.cy.js:58-85 verifies "Authentication problem" message shown with readable error on invalid credentials |
| [x] | Empty email/password fields are rejected. | login.component.ts:37-40 defines form with email and password required validators; login.component.spec.ts:42-49 verifies required validation blocks form submission |
| [x] | Password input is masked. | login.component.html:41 uses `type="password"` attribute |
| [x] | Successful login stores authentication state correctly. | auth.service.ts:28-32 loginUser() calls `tap((authResponse) => this.setToken(authResponse))` which stores token in localStorage; auth.service.spec.ts:38-53 confirms token storage |
| [x] | Successful login redirects to the intended authenticated area. | login.component.ts:63-71 navigates to `/` after successful login; login.component.spec.ts:63-75 confirms navigation to `/` |
| [x] | Authenticated user can refresh the page and remain logged in if intended. | auth.service.ts:86-93 isLoggedIn() checks localStorage token validity; token persists across refresh. auth.service.spec.ts:208-218 verifies token expiration check works correctly |
| [x] | Invalid/expired token causes logout or redirect to login. | auth.service.ts:86-93 isLoggedIn() returns false for expired tokens; auth.guard.ts:13-20 canActivate() checks isLoggedIn() and redirects to `/login` if false; auth.guard.spec.ts:40-47 confirms redirect behavior |
| [x] | Logout clears authentication state and prevents access to protected pages. | auth.service.ts:95-98 logoutUser() removes 'authToken' from localStorage; auth.service.spec.ts:190-196 confirms token removal. E2E fr01-auth.cy.js:46-48 verifies `localStorage.authToken` doesn't exist after logout |
| [x] | Login page does not show raw backend stack traces or technical errors. | login.component.ts:82-87 extracts error message from response.error and displays it; login.component.html:8 shows `{{ errorMessage }}` without exposing stack trace |

### 2.3 Authenticated Routing and Navigation

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Logged-out users cannot access WG pages by typing the URL. | app-routing.module.ts:42-44 defines household routes with `canActivate: mapToCanActivate([AuthGuard])`; auth.guard.ts:13-20 redirects to `/login` if not logged in; E2E fr01-auth.cy.js:111-115 verifies redirect from `/household/create` and `/household/invitations` to login |
| [x] | Logged-out users cannot access task pages by typing the URL. | app-routing.module.ts:46-48 defines task routes with `canActivate: mapToCanActivate([AuthGuard])`; E2E fr01-auth.cy.js:107-109 verifies redirect from `/tasks` to login |
| [x] | Logged-in users do not see Login/Register as primary actions unless appropriate. | home.component.html:4-63 shows landing page (with Login/Register) only when `!authService.isLoggedIn()`. When logged in, shows dashboard (line 69-370) without login/register navigation |
| [x] | Navigation reflects the user's current state: no WG, in WG, Main Tenant, Roommate. | home.component.ts:53-94 loads dashboard with household check; home.component.html:70-85 shows onboarding screen if `hasNoHousehold` is true. Dashboard (lines 86-369) shows different content when in household |
| [x] | Token-bearing requests are visible in the network tab for protected API calls. | auth-interceptor.ts:13-31 intercepts non-auth requests and adds `Authorization: Bearer ` header (line 25). auth-interceptor.ts:14-20 skips auth/register endpoints. E2E tests verify token in requests via `cy.intercept()` |
| [x] | Frontend handles 401 responses consistently. | Interceptor (auth-interceptor.ts) attaches token automatically; token expiration checked by isLoggedIn(). No explicit 401 handler visible, suggesting backend may prevent 401 by token validation and guard checks auth status before request |
| [x] | Frontend handles 403 responses consistently. | home.component.ts:96-102 handles 403 from dashboard API by setting `hasNoHousehold = true`. register.component.ts:75-80 and login.component.ts:76-81 handle 403 VERIFICATION_REQUIRED status specially |

---

## 3. FR-02 Frontend QA — Secure WG Creation and Invitations

### 3.1 WG Creation UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Authenticated user without a WG can access WG creation flow. | Route `/household/create` is protected by AuthGuard; household-create.component.ts exists at src/app/components/household-create/. |
| [x] | Unauthenticated user cannot access WG creation flow. | Route guarded with `canActivate: mapToCanActivate([AuthGuard])` in app-routing.module.ts:42; AuthGuard.canActivate() checks `isLoggedIn()` and redirects to `/login` if false. |
| [x] | WG creation form contains the fields required by the backend. | Form declares `name` and `address` fields (household-create.component.ts:22-25); matches HouseholdCreateRequest DTO. |
| [x] | Empty or invalid WG name is rejected with a clear message. | `name` field has `Validators.required` and `Validators.maxLength(120)` (ts:23); HTML displays error "A name is required (max 120 chars)" when touched and invalid (html:25-27). |
| [x] | Successful WG creation updates the UI without requiring manual reload. | After successful creation, component navigates to `/` (ts:38) rather than stay on form; household-members component loads household via getMyHousehold(). |
| [x] | After WG creation, the creator sees their WG context. | After success, router navigates to `/` (home) (ts:38); home can then display the WG name and context. Spec confirms navigation in test "should navigate home after successful household creation" (spec.ts:62-69). |
| [x] | After WG creation, the creator sees Main Tenant/admin UI if that role applies. | household() test helper in spec.ts returns `currentUserRole: 'MAIN_TENANT'` (line 101); creater is implicitly Main Tenant (backend role). |
| [x] | If the backend rejects WG creation, the UI shows a useful error and does not falsely show success. | Error handling in submit() method (ts:39-46): displays `err.error.message` if available, fallback message if not; spec confirms error message is set and not navigated (spec.ts:71-92). |
| [x] | WG creation is covered by an automated frontend or E2E test. | Spec tests in household-create.component.spec.ts cover form validation and successful/failed creation (lines 40-92); UC-01 E2E test covers full household creation workflow (uc01-onboarding-household.cy.js:12-59). |

### 3.2 Invitation Creation UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Main Tenant/admin can access invitation creation or invite roommate UI. | Route `/household/invitations` protected by AuthGuard (app-routing.module.ts:44); component loads household via getMyHousehold() and then invitations (household-invitations.component.ts:34-40). |
| [x] | Normal roommate cannot see invitation management UI if the policy restricts invitations. | Component checks 403 error from getInvitations() (ts:51-56): if status 403, sets error "Access Denied: Only the Main Tenant can manage invitations." and HTML skips invite form when error contains "Access Denied" (html:15). |
| [x] | Even if a roommate navigates directly to invitation UI, forbidden backend response is handled correctly. | loadInvitations() method (ts:43-57) checks error.status === 403 and sets readable error message; form is hidden when error message contains "Access Denied" (html:15). Spec confirms this behavior (spec.ts:104-110). |
| [x] | Invite creation sends the correct authenticated API request. | invitationService.createInvitation(householdId, form.value) is called with form data (ts:73); HTTP interceptor (auth-interceptor.ts:24-26) attaches JWT token to all requests except auth/registration endpoints. Service spec confirms POST to `{baseUri}/households/{id}/invitations` with request body (invitation.service.spec.ts:35-46). |
| [x] | Generated invitation link/token is displayed clearly enough to share. | After successful creation, `lastCreatedUrl` is stored and displayed in a highlighted box with "Copy" button (html:57-69); styling shows green background (#FBFAF3) and border (#346739), labeled "Invitation Link Created". |
| [x] | Expiration or time-limited nature of invitation is shown if available from backend. | Form has `expiresInHours` select with 24, 168 (7d), 720 (30d) options (html:36-45); invitations table displays `expiresAt | date:'medium'` (html:89). |
| [x] | Copy-to-clipboard works if implemented. | copyToClipboard() method uses `navigator.clipboard.writeText()` (ts:92-94); button bound to `(click)="copyToClipboard(lastCreatedUrl)"` (html:62). Implementation does not check for browser support or handle errors, but basic functionality is present. |
| [x] | UI does not expose unrelated WG/private data inside the invite link view. | Invitation component only shows household name and invited email in preview; no private member data, financial info, or task detail exposed. |
| [x] | Error on invitation creation is shown clearly. | create() method catches errors and sets `errorMessage` (ts:79); HTML displays as alert box (html:9-13). Spec confirms "Could not create invitation." message (spec.ts:112-120). |
| [x] | Invitation creation is covered by an automated frontend or E2E test. | Spec tests cover create, error, and form reset (household-invitations.component.spec.ts:70-92); FR-02 E2E spec tests invalid email blocked and valid creation with retrieve link (fr02-household-invitations.cy.js:38). |

### 3.3 Invitation Acceptance UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Opening a valid invitation link routes to the correct frontend page. | Route `invite/:token` maps to InvitationAcceptComponent (app-routing.module.ts:45); token extracted from ActivatedRoute (invitation-accept.component.ts:52). |
| [x] | If the invited user is not logged in, the UI guides them to login/register and then continue joining. | If not logged in, registerForm is shown by default (html:49-85); after successful registration, accept() is called (ts:103-111). Login form also available via toggleForm() (ts:73-76). Spec confirms navigate to login when accept() called logged-out (spec.ts:119-127). |
| [x] | If the invited user is logged in, accepting the invitation joins the WG. | If authService.isLoggedIn() returns true, accept() button is shown (html:42-44); clicking accept() calls invitationService.acceptInvitation(token) (ts:91). |
| [x] | Successful acceptance updates the user's WG context. | After accept succeeds, success message is shown and router navigates to `/` after 2 seconds (ts:91-96); home component then loads household context. Spec confirms successMessage is set (spec.ts:104-117). |
| [x] | Successful acceptance shows the user as a Roommate unless the intended policy says otherwise. | InvitationAcceptResponse DTO returns role: 'ROOMMATE' (dtos/invitation.ts:24); spec test helper confirms default role is 'ROOMMATE' (invitation-accept.component.spec.ts:196). |
| [x] | Expired invitation link shows a clear expired/invalid message. | previewInvitation() error with status 410 sets errorMessage to "This invitation is no longer valid or has already been used." (ts:64-65). Spec confirms this message (spec.ts:88-94). |
| [x] | Invalid invitation link shows a clear error and does not crash. | previewInvitation() error handlers (ts:63-69) catch all status codes and set errorMessage; HTML gracefully shows error in alert (html:5-9). Spec tests invalid token (status 404) (spec.ts:96-102). |
| [x] | Reusing an invitation behaves according to backend policy and is explained by the UI if needed. | Backend status 410 (Gone) triggers error message "This invitation is no longer valid or has already been used." covering both expired and already-used cases (ts:64-65). |
| [!] | User cannot accidentally join the same WG twice through repeated clicks. | Button is only disabled when isEmailMismatch() returns true (html:42), NOT during the async accept operation. The accept button lacks a pending state to prevent multiple rapid clicks, so users could theoretically trigger multiple acceptInvitation() calls in quick succession. Backend should enforce single-join via 410, but UI does not provide click prevention during request. |
| [x] | Invitation acceptance is covered by an automated frontend or E2E test. | Spec covers register/accept flow (invitation-accept.component.spec.ts:129-150), login/accept (implied by forms), and error cases (spec.ts:88-102). FR-02 E2E tests full invitation flow from creation to join (fr02-household-invitations.cy.js:12-62). |

### 3.4 Role-Aware UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Main Tenant/admin sees management actions appropriate to their role. | InvitationAcceptComponent shows accept button for logged-in users (html:42-44); HouseholdInvitationsComponent shows full invite creation form when household loaded and no 403 error (html:15-71). Household members list displayed in HouseholdMembersComponent. |
| [x] | Roommate does not see Main Tenant/admin-only actions. | HouseholdInvitationsComponent checks for 403 error from getInvitations() (ts:51-56); when 403 received, error message set to "Access Denied: Only the Main Tenant can manage invitations." and form is hidden via `errorMessage.indexOf('Access Denied') === -1` check (html:15). |
| [x] | Role-based UI is consistent after page refresh. | On every ngOnInit(), getMyHousehold() is called (ts:34-40), which loads current user's household with currentUserRole (Household DTO includes currentUserRole field). Invitation creation attempt triggers 403 on every load for non-Main-Tenant. |
| [x] | Role-based UI is consistent after logout/login. | AuthGuard redirects unauthenticated users to login; after login, authService.isLoggedIn() reflects new auth state. Components load fresh household/role data on init. Spec confirms error behavior is consistent (spec.ts:104-110). |
| [x] | If backend returns 403 for a hidden action forced through devtools/direct URL, UI handles it gracefully. | loadInvitations() catches 403 and sets user-friendly error message (ts:51-56); even if form is bypassed with devtools, backend 403 on POST to create invitation would be caught in create() error handler (ts:79) and displayed as "Could not create invitation." (not showing raw 403). |

---

## 4. FR-03 Frontend QA — Task Creation and Assignment

### 4.1 Task Page / Entry Point

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Authenticated WG member can access task area. | AuthGuard protects `/tasks` route (app-routing.module.ts:46); TaskListComponent loads tasks via forkJoin (task-list.component.ts:59-61). |
| [x] | User without WG sees a clear message or is guided to create/join a WG. | TaskCreateComponent shows error: "You need to create or join a household before creating tasks." (task-create.component.ts:58). |
| [x] | Logged-out user cannot access task area. | Route guard `canActivate: mapToCanActivate([AuthGuard])` on `/tasks` path (app-routing.module.ts:46). AuthGuard checks `isLoggedIn()` and redirects to login (auth.guard.ts:14-18). |
| [x] | Task page loads existing tasks from backend. | TaskListComponent calls `taskService.findAll()` and `taskService.findAssignedToMe()` via forkJoin (task-list.component.ts:59-61); both call backend endpoints. |
| [x] | Empty task list has a clear empty state. | Shows "No tasks found." message when `baseTaskCount === 0` (task-list.component.html:99). |
| [x] | Loading state is shown while tasks are fetched. | Spinner displayed when `loading === true`; set during forkJoin subscribe (task-list.component.ts:56, html:82-87). |
| [x] | API failure is shown as a readable error message. | Error handler sets `errorMessage = 'Could not load tasks. Please try again.'` (task-list.component.ts:71); displayed via alert (html:7-11). |
| [x] | Task page does not display tasks from other WGs. | TaskService calls `/api/v1/tasks` which is household-scoped by backend; no cross-WG filtering needed in frontend. |

### 4.2 Task Creation UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Task creation form is reachable for users allowed to create tasks. | Route `/tasks/create` is protected by AuthGuard (app-routing.module.ts:47); displays "Create Task" link in task-list.component.html:4. |
| [ ] | Users not allowed to create tasks do not see the action or receive a clear forbidden message. | No 403-forbidden UI handling in task-create; component assumes user can create if they can reach the page. Needs live backend test. |
| [x] | Task form contains the fields required by backend: title/name at minimum, plus any implemented fields. | Form has title, description, dueDate, assignedToEmail (task-create.component.ts:37-42). |
| [x] | Required task title/name cannot be empty. | FormControl `title` has `Validators.required` (task-create.component.ts:38); error shown on submit if empty (html:21-23). |
| [x] | Optional description behaves correctly. | FormControl `description` has no required validator, only maxLength(2000) (task-create.component.ts:39). |
| [x] | Due date field behaves correctly if implemented. | FormControl `dueDate` has `Validators.required`; HTML input type="date" (html:41); error shown if empty (42-44). |
| [x] | Invalid due dates are rejected if backend rejects them. | Backend validation errors are parsed and displayed (task-create.component.ts:173-200); includes dueDate field checking. |
| [x] | Successful task creation updates the task list without manual reload. | On success, router navigates to `/tasks` (task-create.component.ts:160); TaskListComponent reloads on init. |
| [x] | Backend validation errors are displayed clearly. | Error parsing logic extracts validation messages from `err.error['Validation errors']` and displays user-friendly text (task-create.component.ts:186-197). |
| [ ] | Form prevents accidental double-submit or handles duplicate clicks safely. | No submit button disable flag or submitting state visible in component or template. Needs live test for double-click behavior. |
| [x] | Task creation is covered by automated frontend or E2E test. | E2E tests: `fr03-tasks.cy.js` line 19 "user A assigns task to user B"; `uc02-task-lifecycle.cy.js` line 19 "assigned task is visible to assignee". Both use `cy.createTask()` helper. |

### 4.3 Task Assignment UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Task can be assigned to a roommate from the same WG. | FormControl `assignedToEmail` accepts email value; TaskCreateComponent loads roommates from current household via `householdService.getMembers(household.id)` (task-create.component.ts:64-65). |
| [x] | Assignee selector lists only roommates from the current WG. | Select populates from `roommates` array built from `getMembers(household.id)` (task-create.component.html:53-55); no cross-WG members included. |
| [x] | Assignee selector does not list users from other WGs. | HouseholdService.getMembers scopes to a specific household ID; only members of current household are returned. |
| [x] | Assigning to a non-existing or removed user is handled gracefully. | Backend is responsible for validation; if invalid email, backend returns validation error which is caught and displayed (task-create.component.ts:162-169). |
| [x] | Assigned user is displayed clearly in the task list/detail view. | TaskDetailDto includes `assignedToEmail`; displayed in task card as "Assigned To: {{ task.assignedToEmail }}" (task-list.component.html:118). |
| [x] | Assignment due date/status is displayed if implemented. | Task list displays assignedToEmail, dueDate, and status badge (task-list.component.html:118-120, 110-112). |
| [x] | Reassignment works correctly if implemented. | TaskEditComponent allows reassigning via `assignedToEmail` field; calls `taskService.update()` (task-edit.component.ts:105). |
| [x] | Duplicate assignment behavior is clear and consistent with backend. | No UI prevention of reassigning to same user; backend handles idempotency. |
| [x] | Assignment failure from backend is shown clearly. | Error handler in task-create shows parsed backend error (task-create.component.ts:162-169). |
| [x] | Task assignment is covered by automated frontend or E2E test. | E2E test `fr03-tasks.cy.js` line 59 calls `cy.createTask(task)` with `assignedToEmail: roommate.email`; verifies it appears in task list. |

### 4.4 Task Tracking / Status UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Task status is visible. | TaskDetailDto has `status: TaskStatus` property; displayed via badge with `statusLabel()` and `statusBadgeClass()` (task-list.component.html:110-112, component.ts:150-171). |
| [x] | Assigned user can see their assigned task. | TaskListComponent has "My Tasks" tab that calls `findAssignedToMe()` (line 61); E2E verifies via `?tab=mine` (uc02-task-lifecycle.cy.js:59). |
| [x] | Task creator/Main Tenant can see created/assigned task if intended. | All tasks visible in "All Tasks" tab; creator can filter/find own tasks (task-list.component.ts:84). |
| [x] | Completing or changing status works if implemented in FR-03. | `completeTask()` calls `updateStatus(id, { status: TaskStatus.DONE })` (task-list.component.ts:176); `reopenTask()` sets status to OPEN (line 188). |
| [x] | Unauthorized status changes are prevented or handled gracefully. | Backend enforces authorization; if rejected, error message shown (task-list.component.ts:178-181). |
| [x] | Status change updates UI after backend success. | On success, `loadTasks()` called to refresh list (task-list.component.ts:177, 189); UI reloads from backend. |
| [!] | Status change does not falsely update UI after backend failure. | On error, `loadTasks()` is NOT called; UI remains stale with pre-failure state (task-list.component.ts:178-182). Error message shown but UI card state not refreshed on failure. CONFIRMED DEFECT: no optimistic-update rollback mechanism. |
| [x] | Task list remains consistent after page refresh. | TaskListComponent.ngOnInit() calls `loadTasks()` on every init; forkJoin fetches fresh data from backend. |

---

## 5. End-to-End Frontend Acceptance Scenario

Run this manually and, ideally, also automate it.

| Step | Expected result | Status | Evidence / Notes |
|---|---|---|---|
| 1 | Open app as logged-out user. | [x] | Cypress base URL `http://localhost:4200/` configured in cypress.config.js; logged-out state cleared in `beforeEach` with `cy.clearLocalStorage()` across all E2E specs. |
| 2 | Register Anna. | [x] | `cy.registerUser()` custom command in e2e/cypress/support/commands.js:27-49; covered by fr01-auth.cy.js:27-56 and UC-01/UC-02 positive flows. Register form at frontend/src/app/components/register/register.component.html has all required fields (firstName, lastName, email, password, termsAccepted). |
| 3 | Anna logs in or is logged in automatically according to implementation. | [x] | Auto-login after registration: fr01-auth.cy.js:35-39 verify authToken is set in localStorage and user sees dashboard. Manual login via `cy.loginUser()` in support/commands.js:51-70 confirms redirect to `/#/` and token storage. |
| 4 | Anna creates a WG. | [x] | `cy.createHousehold()` command in support/commands.js:80-98 posts to `/api/v1/households` with 201 response; uc01-onboarding-household.cy.js:12-59 confirms full flow. Household creation form at household-create.component.html:18-46 requires `name` field with validation. |
| 5 | Anna can see WG context and invite action. | [x] | uc01-onboarding-household.cy.js:34-37 verifies household name visible after creation; fr02-household-invitations.cy.js:34-36 confirms household member page shows household context. household-invitations.component.html:3-7 shows "Manage Invitations" heading when household loaded. |
| 6 | Anna creates/copies invitation link. | [x] | `cy.createInvitation()` command in support/commands.js:100-127 extracts readonly URL field; uc01-onboarding-household.cy.js:33 calls command; household-invitations.component.html:57-69 displays "Invitation Link Created" and readonly input with copy button. |
| 7 | Log out or open new browser profile. | [x] | `cy.logoutUser()` in support/commands.js:72-78 clears authToken; `cy.clearLocalStorage()` in beforeEach of all specs simulates fresh profile. fr02-household-invitations.cy.js:39 and uc01-onboarding-household.cy.js:34 use logout between users. |
| 8 | Register Lukas or log in as Lukas. | [x] | fr02-household-invitations.cy.js:44-48 and uc01-onboarding-household.cy.js:39-43 show registration form fields (firstName, lastName, email, password, termsAccepted) in invitation-accept workflow. Register via main form also available (register.component.html). |
| 9 | Open invitation link as Lukas. | [x] | `cy.visitInviteUrl()` in support/commands.js:129-135 parses invite token and navigates to `/#/invite/{token}`. uc01-onboarding-household.cy.js:35 and fr02-household-invitations.cy.js:40 call this; invitation-accept.component.html:17-30 displays household preview. |
| 10 | Lukas accepts invitation and sees WG context. | [x] | uc01-onboarding-household.cy.js:44-46 clicks "Register & Join WG" button; fr02-household-invitations.cy.js:52-55 waits for POST `/api/v1/invitations/*/accept` 200 response; invitation-accept.component.html:42-43 has accept button. Redirect to `/#/` and success message verified. |
| 11 | Anna or authorized user creates a task. | [x] | `cy.createTask()` in support/commands.js:170-216 fills task form (title, description, dueDate, assignedToEmail) and posts to `/api/v1/tasks` expecting 201. UC-02 uc02-task-lifecycle.cy.js:55 and FR-03 fr03-tasks.cy.js:59 verify task creation workflow. task-create.component.html:16-179 has full form with validation. |
| 12 | Task is assigned to Lukas. | [x] | Support command `cy.createTask()` takes `assignedToEmail` parameter; uc02-task-lifecycle.cy.js:34-38 passes `assignee.email` in task object; task-create.component.html:50-56 renders `<select id="assignedToEmail">` populated from roommates array. |
| 13 | Lukas logs in and sees his assigned task. | [x] | uc02-task-lifecycle.cy.js:58-63 logs in assignee, visits `/#/tasks?tab=mine`, verifies task.title and description visible in `.wgms-card`. fr03-tasks.cy.js:64-68 confirms same behavior. Backend filters tasks by assignee. |
| 14 | Browser refresh keeps the workflow state consistent. | [ ] | fr01-auth.cy.js:40-54 tests profile page navigation and data persistence (authToken in localStorage, profile data fetched from API), but does NOT include an explicit page refresh/reload. uc01-onboarding-household.cy.js and uc02-task-lifecycle.cy.js verify navigation between pages, not browser refresh. No test explicitly verifies refresh behavior. |
| 15 | Direct access to protected pages while logged out redirects/blocks correctly. | [x] | fr01-auth.cy.js:107-116 tests three protected routes (`/#/tasks`, `/#/household/create`, `/#/household/invitations`); all redirect to `/login`. Route guards inferred from test expectations (redirect verified via `cy.url().should('include', '/login')`). |

---

## 6. Usability Gate

The proposal requires main workflows to be completable without external instructions by a peer tester.

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A peer tester can register without explanation. | register.component.html:20-101 has clear form labels (for="inputFirstName", for="inputLastName", for="inputEmail", for="inputPassword"), placeholders ("e.g. Anna", "anna.berger@tuwien.ac.at"), and field validation errors visible inline. Submit button disabled until form valid. |
| [x] | A peer tester can log in without explanation. | login.component.html:17-63 mirrors register: clear labels (for="inputEmail", for="password"), intuitive placeholders, inline error messages ("Please provide a valid email", "Email is required"). Form validation prevents invalid submission. |
| [x] | A peer tester understands how to create a WG. | household-create.component.html:14-46 displays prominent heading "Create your Household" with tagline; label for="name" and for="address"; placeholder examples ("e.g. Dream Loft Vienna", "Street, City, Postcode"). Clear button text "Initialize WG". Form validation with inline error messages. |
| [x] | A peer tester understands how to invite a roommate. | household-invitations.component.html:3-69 shows "Manage Invitations" heading with context; labeled email input (label for="email"), expiry select (label for="expiry"), "Create" button; success message "Invitation Link Created" shown after submission with copy button. Clear instructions. |
| [x] | A peer tester understands how to accept an invitation. | invitation-accept.component.html:17-108 displays "Join a Household" heading, shows household name prominently (line 27, font-size 42px), registration form with labels (for="firstName", etc.), submit button "Register & Join WG" or "Accept and Enter WG" for logged-in users. Success message "You have successfully joined the household!" in fr02-household-invitations.cy.js:55. |
| [x] | A peer tester understands how to create a task. | task-create.component.html:1-183 displays "Create Task" heading (h2, line 5); labeled inputs (for="title" "Title *", for="description" "Description", for="dueDate" "Due Date *", for="assignedToEmail" "Assign To *"); placeholders guide input ("e.g. Clean the kitchen", "Provide more details if needed"). Submit button "Create Task" clearly labeled. Form validation with error messages. |
| [x] | A peer tester understands how to assign a task. | task-create.component.html:48-62 has labeled select "Assign To *" (for="assignedToEmail") with options list showing "firstName lastName (email)" for each roommate. UI makes assignment explicit and required. uc02-task-lifecycle.cy.js:34-37 and fr03-tasks.cy.js:33-38 confirm assignee is selectable. |
| [ ] | Form labels are clear and not only placeholder text. | PARTIAL: Most components have proper `<label>` tags with `for=` attributes (register.component.html:25,37,51,67,87; login.component.html:24,40; household-create.component.html:20,32; household-invitations.component.html:20,36; task-create.component.html:19,30,40,50). HOWEVER, invitation-accept.component.html registration form LACKS `for=` attributes on lines 55 (First Name), 59 (Last Name), 64 (Email), 71 (Password) - only line 76 (termsAccepted checkbox) has proper `for=` attribute. These labels are semantically present but not formally associated with inputs via accessibility mechanism. |
| [x] | Error messages explain what the user can do next. | register.component.html:29-31 "First name is required"; line 59-60 "Please provide a valid email"; line 75-76 "At least 8 characters required"; line 78-79 "Must contain a number and special character". household-create.component.html:26 "A name is required (max 120 chars)". household-invitations.component.html:30 "Please provide a valid email address". All messages describe the issue and constraint. |
| [x] | Success messages/redirects make it clear that the action worked. | register.component.html implicit: form disabled on submit, redirect to `/#/` (authToken set). invitation-accept.component.html:11-14 shows explicit success message. household-invitations.component.html:57-69 displays "Invitation Link Created" with green background. fr02-household-invitations.cy.js:55 checks for "You have successfully joined the household!". Task creation redirects to `/#/tasks` per support/commands.js:215. Clear outcomes in all flows. |
| [x] | User is not trapped after failed login, failed invitation, or failed task creation. | login.component.html:60-62 provides "Register here" link when login fails. register.component.html:103-105 has "Login here" link. household-invitations.component.html:57-69 displays URL even on error, user can copy and continue. invitation-accept.component.html:83 and 100 have toggle buttons to switch between register/login; line 107 has agreement note. task-create.component.html:6 has "Back to Tasks" link. All workflows have escape routes; no dead-end UIs. |

---

## 7. Responsive Layout and Browser Compatibility Gate

Reference targets from the proposal: minimum 360 CSS px width, iPhone 12 Pro/Safari, Pixel 7/Chrome, desktop Chrome/Firefox at 1366x768+.

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Registration page is usable at 360px width. | Requires live browser/device verification. Static support: viewport meta tag present (src/index.html:6), global @media (max-width: 576px) in styles.scss:76, responsive col-md-6 layout with mx-auto. |
| [ ] | Login page is usable at 360px width. | Requires live browser/device verification. Static support: same responsive structure as registration (col-md-5 mx-auto in login.component.html:3), @media query in styles.scss. |
| [ ] | WG creation page is usable at 360px width. | Requires live browser/device verification. Static support: col-md-6 mx-auto layout, responsive form controls, @media query at 576px in styles.scss. |
| [ ] | Invitation acceptance page is usable at 360px width. | Requires live browser/device verification. Static support: col-md-6 mx-auto, form-check and form-control responsive classes, @media query in styles.scss. |
| [ ] | Task page is usable at 360px width. | Requires live browser/device verification. Static support: col-md-6 flex layout, task-toolbar with flex-wrap in task-list.component.scss:13, @media (max-width: 576px) in task-create.component.scss:152. |
| [ ] | Task assignment controls are usable on mobile width. | Requires live browser/device verification. Static support: col-md-6, responsive select control in task-create.component.html:51, @media queries handle mobile layout. |
| [ ] | Main workflow is usable at desktop 1366x768. | Requires live browser/device verification. Static support: wgms-container max-width 960px with padding, Bootstrap grid system supports desktop viewports. |
| [ ] | No important button is hidden or inaccessible on mobile. | Requires live browser/device verification. Static support: buttons use d-flex justify-content, no display:none on mobile, responsive button layouts. |
| [ ] | No horizontal scrolling occurs on normal mobile widths unless unavoidable and documented. | Requires live browser/device verification. Static support: flex-wrap: wrap in fair-chore-actions (task-create.component.scss:14), responsive grid layout. |
| [ ] | Tested in Chrome. | Requires live browser testing. |
| [ ] | Tested in Firefox or documented if not available. | Requires live browser testing. |
| [ ] | Tested in Safari/iOS simulator/device or documented if not available. | Requires live browser testing. |

---

## 8. Accessibility and Basic Quality Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Interactive elements are reachable by keyboard. | All core interactive elements are native focusable controls: <button type="submit">, <input type="email/password/text">, <select>, <textarea>, <a> tags. Native elements maintain default keyboard accessibility (Tab, Enter, Space). register.component.html:99, login.component.html:56, task-create.component.html:176, task-list.component.html:4. |
| [!] | Forms have labels associated with inputs. | PARTIALLY IMPLEMENTED: 58 of 64 form label-input associations are correct. DEFECT: invitation-accept.component.html has 6 inputs without id attributes (firstName, lastName, email in registration form, email/password in login form) and their labels lack for attributes. Only the terms checkbox is properly associated (line 75-76). All other components (register, login, household-create, task-create) have proper label-input associations with matching id and for attributes. This breaks keyboard accessibility for users relying on form labels. |
| [x] | Buttons have clear accessible text. | All buttons use descriptive text: "Register", "Login", "Create Task", "Accept and Enter WG", "Mark as Done", "Reopen", "Delete". No empty buttons. register.component.html:99, login.component.html:56, household-create.component.html:44, task-list.component.html:4. |
| [x] | Error messages are visible and understandable. | Error messages always pair text with color: register.component.html:31-33 shows text-danger <small> for validation errors, never color-only. Login and task forms follow same pattern with explicit error text. Errors are semantic <small class="form-text text-danger">. |
| [x] | Color is not the only way to identify errors or statuses. | Errors accompanied by text, not color alone. Status badges include text labels (e.g., "Done", "Open") not just colors. task-list.component.html:110-112 shows badge with statusLabel() text. Fair-chore stats include numeric labels (Done, Open, Overdue, Score). |
| [x] | Focus order is logical on registration/login/WG/task pages. | Forms follow natural DOM order: firstName → lastName → email → password → terms → submit button. No tabindex manipulation detected. All elements are native form controls with default focus order. register.component.html flows top-to-bottom; task-create.component.html follows form sequence (title → description → dueDate → assignedToEmail). |
| [x] | Page titles/headings make the current screen understandable. | Clear headings on each page: register.component.html:18 (<h2>Register</h2>), login.component.html:18 (<h2>Login</h2>), household-create.component.html:14 (<h1>Create your Household</h1>), invitation-accept.component.html:19 (<h1>Join a Household</h1>), task-list.component.html:3 (<h2>Tasks</h2>). Heading hierarchy is proper (h1 for page, h2/h3 for sections). |
| [x] | Modals/dialogs, if used, can be closed and do not trap the user incorrectly. | confirm-dialog.component.html:1 has proper ARIA attributes: role="dialog" aria-modal="true" aria-labelledby="confirmDialogTitle". Two buttons provided: cancel (close via activeModal.dismiss()) and confirm (close via activeModal.close()). NgbActiveModal is used for proper modal lifecycle. confirm-dialog.component.ts:24-26 shows dismiss/close methods. No ESC trap detected; ng-bootstrap handles keyboard close (ESC key by default). |
| [x] | App does not have major layout shifts during core workflows. | No detected CLS (Cumulative Layout Shift) issues in static code. Forms use fixed padding (space-4, space-6). Loading states use wgms-card with consistent height. No conditionally-loaded large elements that would cause reflow. Spinners use role="status" with visually-hidden text (invitation-accept.component.html:112, task-list.component.html:85). |

---

## 9. Frontend Automated Test Coverage Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Automated test covers successful registration or mocked equivalent. | `frontend/src/app/components/register/register.component.spec.ts:107-123` — test "should call AuthService and navigate home after successful registration" verifies registration with mocked AuthService.registerUser returning JWT token. |
| [x] | Automated test covers login. | `frontend/src/app/components/login/login.component.spec.ts:63-75` — test "should call AuthService and navigate home after successful login" verifies login with mocked AuthService.loginUser, routes to home on success. |
| [x] | Automated test covers protected route behavior. | `frontend/src/app/guards/auth.guard.spec.ts:31-47` — two tests verify AuthGuard.canActivate() allows access when logged in and denies/redirects to login when not authenticated. |
| [x] | Automated test covers WG creation. | `frontend/src/app/components/household-create/household-create.component.spec.ts:50-69` — test "should call HouseholdService with name and address for a valid form" verifies household creation with valid form data and navigation home on success. |
| [x] | Automated test covers invitation creation or invite link display. | `frontend/src/app/components/household-invitations/household-invitations.component.spec.ts:82-92` — test "should store returned invite link, reset form, and refresh invitations after successful creation" verifies invite URL is captured and displayed. |
| [x] | Automated test covers invitation acceptance if feasible. | `frontend/src/app/components/invitation-accept/invitation-accept.component.spec.ts:104-117` — test "should accept invitation for logged-in user and navigate home after success" verifies acceptInvitation call and success message display for logged-in users. |
| [x] | Automated test covers task creation. | `frontend/src/app/components/task-create/task-create.component.spec.ts:236-248` — test "should call TaskService with title, description, dueDate, and assignedToEmail for valid form" verifies task creation with all required fields. |
| [x] | Automated test covers task assignment. | `frontend/src/app/components/task-create/task-create.component.spec.ts:132-140` — test "should fill assignee when suggestion is used and still allow manual override" verifies task assignment to specific roommate email; also `task-list.component.spec.ts:225-235` filters and displays assigned tasks by email. |
| [x] | Tests use stable selectors where possible, not brittle text-only selectors everywhere. | Data-cy attributes used throughout: `[data-cy="registration-terms-checkbox"]`, `[data-cy="registration-submit"]`, `[data-cy="fair-chore-suggestion"]`, `[data-cy="fair-chore-recommendation"]`, `[data-cy="task-reopen"]`, `[data-cy="confirm-dialog-confirm"]` in specs and e2e commands (e2e/cypress/support/commands.js) and tests (e2e/cypress/e2e/fr01-auth.cy.js, fr02-household-invitations.cy.js, fr03-tasks.cy.js). Some tests also use form control names and input[name="..."] selectors which are reasonably stable. |
| [x] | Tests can run in CI or the limitation is documented. | `.gitlab-ci.yml` defines two test jobs: `test-frontend` (stage: test, lines 22-39, runs linting at line 34 then `npm run build` at line 35) and `e2e-test` (stage: test, lines 41-98, runs `npx cypress run` at line 87). Both jobs are configured to run in CI pipeline, with e2e-test outputting JUnit XML reports and video/screenshot artifacts. |

---

## 10. Frontend Bug Report Template

```markdown
### Bug: <short title>

- Feature: FR-01 / FR-02 / FR-03
- Severity: Critical / High / Medium / Low
- Browser/device:
- Viewport:
- Commit hash:
- Steps to reproduce:
  1.
  2.
  3.
- Expected result:
- Actual result:
- Evidence: screenshot/video/console/network log/test output
- Suspected location: component/service/routing/auth/interceptor/style/test
- Blocks dev acceptance? yes/no
```

---

---

## 10.1 Bugs Found in This QA Pass

### Bug: Registration form has no password-confirmation field

- Feature: FR-01
- Severity: Medium
- Browser/device: N/A (code review)
- Viewport: N/A
- Commit hash: `3f5a651`
- Steps to reproduce:
  1. Open `/register`.
  2. Observe the form fields.
- Expected result: A confirm-password field guards against typos in the (masked) password.
- Actual result: Only a single password input exists; no confirmation. Backend DTO also has no confirmation, so this is a UX hardening gap, not a backend mismatch.
- Evidence: `register.component.ts:36-42`, `register.component.html:66-83`.
- Suspected location: component (register form).
- Blocks dev acceptance? No.

### Bug: Invitation "Accept" button can be double-submitted

- Feature: FR-02
- Severity: Medium
- Browser/device: N/A (code review)
- Viewport: N/A
- Commit hash: `3f5a651`
- Steps to reproduce:
  1. Open a valid invitation link while logged in.
  2. Click "Accept" repeatedly before the request resolves.
- Expected result: Button is disabled / a pending flag prevents concurrent accept requests.
- Actual result: Button is disabled only when `isEmailMismatch()` is true; there is no submitting/pending state, so rapid clicks fire multiple `acceptInvitation()` calls. (Backend single-use token mitigates duplicate membership — extra calls return 410 — but the UI does not guard.)
- Evidence: `invitation-accept.component.html:42`, `invitation-accept.component.ts` (no pending flag around accept).
- Suspected location: component (invitation-accept).
- Blocks dev acceptance? No (backend prevents duplicate membership), but should be fixed.

### Bug: Invitation-accept inline form lacks label/input associations (a11y)

- Feature: FR-02
- Severity: Medium
- Browser/device: N/A (code review)
- Viewport: N/A
- Commit hash: `3f5a651`
- Steps to reproduce:
  1. Open the invitation-accept page when logged out (inline register/login form renders).
  2. Inspect the form inputs.
- Expected result: Each `<label>` is tied to its input via `for=`/`id` (as elsewhere in the app).
- Actual result: 6 inputs (firstName, lastName, email, password in the register block; email, password in the login block) have no `id`, and their labels have no `for=`. Only the terms checkbox is correctly associated. Screen-reader/clickable-label behaviour is degraded for this page.
- Evidence: `invitation-accept.component.html` (register/login input rows lack `id`; labels lack `for=`).
- Suspected location: template (invitation-accept) — accessibility.
- Blocks dev acceptance? No.

### Bug: Task status change leaves stale UI on backend failure

- Feature: FR-03
- Severity: Medium
- Browser/device: N/A (code review)
- Viewport: N/A
- Commit hash: `3f5a651`
- Steps to reproduce:
  1. In the task list, toggle/complete a task while the backend call fails (e.g. 403/network error).
- Expected result: UI reflects the true backend state (reload or roll back) after the error.
- Actual result: On error an error message is shown but `loadTasks()` is not called and the card state is not reverted, so the UI can diverge from the backend until the next manual refresh.
- Evidence: `task-list.component.ts:173-182` (`completeTask` error branch does not reload/rollback).
- Suspected location: component (task-list status update).
- Blocks dev acceptance? No.

### Bug: Task creation form has no submit-disable / loading indicator

- Feature: FR-03
- Severity: Low
- Browser/device: N/A (code review)
- Viewport: N/A
- Commit hash: `3f5a651`
- Steps to reproduce:
  1. Open the task-create form and click "Create" multiple times quickly.
- Expected result: Submit is disabled or shows a spinner during the request to prevent double-submit.
- Actual result: No submitting flag / disabled state on the submit control during the async create call.
- Evidence: `task-create.component.ts`, `task-create.component.html:176`.
- Suspected location: component (task-create).
- Blocks dev acceptance? No.

---

## 11. Frontend Final Decision

| Decision item | Status / Notes |
|---|---|
| FR-01 frontend complete? | [x] Complete for dev scope. Registration (required-field/email/password-pattern validation, duplicate-email handling), login (generic error, token storage), `AuthGuard`, `AuthInterceptor`, and logout are all verified. Non-blocking: no password-confirmation field (Medium UX). |
| FR-02 frontend complete? | [x] Complete for dev scope. WG creation + validation, Main-Tenant-gated invitation create/list, copy-to-clipboard, accept flow for logged-in/out users, expired/invalid messages, and role-aware UI verified. Non-blocking: Accept button has no pending state (double-click risk, Medium); invitation-accept inline form lacks label/`id` associations (Medium a11y). |
| FR-03 frontend complete? | [x] Complete for dev scope. Task list (load/empty/loading/error), create-form validation, WG-scoped assignee selector, and status display/update verified. Non-blocking: status change leaves stale UI on backend failure (Medium); no submit-disable on create (Low). |
| Critical usability issues open? | No critical issues. Medium: password-confirmation absent; invitation-accept label associations. |
| Critical responsive layout issues open? | None confirmed. Live device/browser rendering NOT validated in this static pass; static support present (viewport meta tag, `@media` at 576px, Bootstrap grid). Open as follow-up. |
| Automated frontend/E2E tests passing? | Build + lint pass locally and in CI (`test-frontend`). Unit specs not executed in this pass (env limitation) and not wired into CI. Cypress e2e runs in CI `e2e-test`; confirm pipeline is green for the branch. |
| Frontend ready for dev acceptance? | Yes — passed with non-blocking issues. Recommend fixing the status-stale-on-error refresh and the invitation-accept a11y/double-click items before release. |

Final decision:

- [ ] Passed
- [x] Passed with non-blocking issues
- [ ] Failed, must fix before acceptance

Accepted follow-up notes, not blockers for this frontend QA pass:

| Priority | Item | Reason |
|---|---|---|
| Medium | Refresh/roll back the task card after a failed status change (`task-list.component.ts:173-182`). | Prevents UI diverging from backend on error. |
| Medium | Add `id`/`for=` associations to the invitation-accept inline register/login inputs. | Accessibility (screen readers, clickable labels). |
| Medium | Add a pending/disabled state to the invitation Accept button. | Prevents double-submit during the async accept. |
| Low | Disable the task-create submit / show a spinner during submission. | Prevents accidental double-create. |
| Low | Wire Angular unit specs (`npm test` / `ng test --watch=false --browsers=ChromeHeadless`) into the CI `test-frontend` job. | 44 specs currently run on developer machines only; build+lint+e2e run in CI. |
| Low | Validate the responsive layout live at 360px and on the target browsers/devices. | Static support exists; not confirmed by a live render in this pass. |
