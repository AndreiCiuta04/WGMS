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
| Branch under test | `dev` |
| Frontend technology | Angular SPA |
| Target browsers | Chrome, Firefox, Safari, Edge where available |
| Target viewports | 360px mobile minimum, iPhone 12 Pro, Pixel 7, desktop 1366x768+ |
| QA reviewer |  |
| Date |  |
| Commit hash |  |
| Pipeline link |  |

---

## 0. Frontend Setup and Build Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | `dev` branch is pulled cleanly and has no uncommitted local changes before QA starts. |  |
| [ ] | Frontend dependencies install from a clean checkout using documented commands. |  |
| [ ] | Frontend starts locally with the documented development command. |  |
| [ ] | Frontend build succeeds without errors. |  |
| [ ] | Frontend automated tests run successfully or failures are documented. |  |
| [ ] | E2E test runner setup is documented and reproducible. |  |
| [ ] | Frontend points to the correct backend API base URL for dev/demo. |  |
| [ ] | No hardcoded local-only URLs remain unless documented for local dev. |  |
| [ ] | No secrets, tokens, or passwords are committed in frontend config. |  |
| [ ] | Browser console has no critical errors on initial load. |  |

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

## 1. Frontend Architecture and Integration Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | UI uses backend API calls for registration, login, WG creation, invitations, and tasks. |  |
| [ ] | Frontend does not fake success for core workflows without backend persistence. |  |
| [ ] | Business-critical authorization is not implemented only through hidden buttons; backend denial is handled correctly. |  |
| [ ] | API service files are separated from components where possible. |  |
| [ ] | Components do not contain unnecessary duplicated business rules that can diverge from backend validation. |  |
| [ ] | JWT/token handling is centralized and consistently attached to protected API calls. |  |
| [ ] | Route guards protect authenticated pages. |  |
| [ ] | Unauthorized users are redirected to login or shown a clear message. |  |
| [ ] | API errors are shown to users in a clear, non-technical way. |  |
| [ ] | Loading states are shown for slow API calls where users would otherwise be confused. |  |
| [ ] | Empty states are shown for no WG, no invitations, or no tasks where relevant. |  |

---

## 2. FR-01 Frontend QA — User Registration and Login

### 2.1 Registration Page

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Registration page is reachable from the public navigation or landing page. |  |
| [ ] | Registration form contains all required fields from the backend DTO. |  |
| [ ] | Required fields are visibly marked or clearly implied. |  |
| [ ] | Empty required fields are rejected before or after submit with clear messages. |  |
| [ ] | Invalid email format shows a clear validation message. |  |
| [ ] | Password field is masked. |  |
| [ ] | Password confirmation exists and is validated if the UI includes confirmation. |  |
| [ ] | Password rules are communicated if the backend enforces rules. |  |
| [ ] | Successful registration leads to the intended next step: login, automatic login, or WG creation. |  |
| [ ] | Duplicate email error is displayed clearly. |  |
| [ ] | Registration form does not expose password in URL, logs, or visible page state. |  |
| [ ] | Browser refresh after successful registration does not create duplicate users. |  |

### 2.2 Login Page

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Login page is reachable from public navigation. |  |
| [ ] | Login form accepts valid credentials for an existing user. |  |
| [ ] | Wrong password shows a clear failure message. |  |
| [ ] | Unknown email shows a clear failure message. |  |
| [ ] | Empty email/password fields are rejected. |  |
| [ ] | Password input is masked. |  |
| [ ] | Successful login stores authentication state correctly. |  |
| [ ] | Successful login redirects to the intended authenticated area. |  |
| [ ] | Authenticated user can refresh the page and remain logged in if intended. |  |
| [ ] | Invalid/expired token causes logout or redirect to login. |  |
| [ ] | Logout clears authentication state and prevents access to protected pages. |  |
| [ ] | Login page does not show raw backend stack traces or technical errors. |  |

### 2.3 Authenticated Routing and Navigation

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Logged-out users cannot access WG pages by typing the URL. |  |
| [ ] | Logged-out users cannot access task pages by typing the URL. |  |
| [ ] | Logged-in users do not see Login/Register as primary actions unless appropriate. |  |
| [ ] | Navigation reflects the user's current state: no WG, in WG, Main Tenant, Roommate. |  |
| [ ] | Token-bearing requests are visible in the network tab for protected API calls. |  |
| [ ] | Frontend handles 401 responses consistently. |  |
| [ ] | Frontend handles 403 responses consistently. |  |

---

## 3. FR-02 Frontend QA — Secure WG Creation and Invitations

### 3.1 WG Creation UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Authenticated user without a WG can access WG creation flow. |  |
| [ ] | Unauthenticated user cannot access WG creation flow. |  |
| [ ] | WG creation form contains the fields required by the backend. |  |
| [ ] | Empty or invalid WG name is rejected with a clear message. |  |
| [ ] | Successful WG creation updates the UI without requiring manual reload. |  |
| [ ] | After WG creation, the creator sees their WG context. |  |
| [ ] | After WG creation, the creator sees Main Tenant/admin UI if that role applies. |  |
| [ ] | If the backend rejects WG creation, the UI shows a useful error and does not falsely show success. |  |
| [ ] | WG creation is covered by an automated frontend or E2E test. |  |

### 3.2 Invitation Creation UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Main Tenant/admin can access invitation creation or invite roommate UI. |  |
| [ ] | Normal roommate cannot see invitation management UI if the policy restricts invitations. |  |
| [ ] | Even if a roommate navigates directly to invitation UI, forbidden backend response is handled correctly. |  |
| [ ] | Invite creation sends the correct authenticated API request. |  |
| [ ] | Generated invitation link/token is displayed clearly enough to share. |  |
| [ ] | Expiration or time-limited nature of invitation is shown if available from backend. |  |
| [ ] | Copy-to-clipboard works if implemented. |  |
| [ ] | UI does not expose unrelated WG/private data inside the invite link view. |  |
| [ ] | Error on invitation creation is shown clearly. |  |
| [ ] | Invitation creation is covered by an automated frontend or E2E test. |  |

### 3.3 Invitation Acceptance UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Opening a valid invitation link routes to the correct frontend page. |  |
| [ ] | If the invited user is not logged in, the UI guides them to login/register and then continue joining. |  |
| [ ] | If the invited user is logged in, accepting the invitation joins the WG. |  |
| [ ] | Successful acceptance updates the user's WG context. |  |
| [ ] | Successful acceptance shows the user as a Roommate unless the intended policy says otherwise. |  |
| [ ] | Expired invitation link shows a clear expired/invalid message. |  |
| [ ] | Invalid invitation link shows a clear error and does not crash. |  |
| [ ] | Reusing an invitation behaves according to backend policy and is explained by the UI if needed. |  |
| [ ] | User cannot accidentally join the same WG twice through repeated clicks. |  |
| [ ] | Invitation acceptance is covered by an automated frontend or E2E test. |  |

### 3.4 Role-Aware UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Main Tenant/admin sees management actions appropriate to their role. |  |
| [ ] | Roommate does not see Main Tenant/admin-only actions. |  |
| [ ] | Role-based UI is consistent after page refresh. |  |
| [ ] | Role-based UI is consistent after logout/login. |  |
| [ ] | If backend returns 403 for a hidden action forced through devtools/direct URL, UI handles it gracefully. |  |

---

## 4. FR-03 Frontend QA — Task Creation and Assignment

### 4.1 Task Page / Entry Point

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Authenticated WG member can access task area. |  |
| [ ] | User without WG sees a clear message or is guided to create/join a WG. |  |
| [ ] | Logged-out user cannot access task area. |  |
| [ ] | Task page loads existing tasks from backend. |  |
| [ ] | Empty task list has a clear empty state. |  |
| [ ] | Loading state is shown while tasks are fetched. |  |
| [ ] | API failure is shown as a readable error message. |  |
| [ ] | Task page does not display tasks from other WGs. |  |

### 4.2 Task Creation UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Task creation form is reachable for users allowed to create tasks. |  |
| [ ] | Users not allowed to create tasks do not see the action or receive a clear forbidden message. |  |
| [ ] | Task form contains the fields required by backend: title/name at minimum, plus any implemented fields. |  |
| [ ] | Required task title/name cannot be empty. |  |
| [ ] | Optional description behaves correctly. |  |
| [ ] | Due date field behaves correctly if implemented. |  |
| [ ] | Invalid due dates are rejected if backend rejects them. |  |
| [ ] | Successful task creation updates the task list without manual reload. |  |
| [ ] | Backend validation errors are displayed clearly. |  |
| [ ] | Form prevents accidental double-submit or handles duplicate clicks safely. |  |
| [ ] | Task creation is covered by automated frontend or E2E test. |  |

### 4.3 Task Assignment UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Task can be assigned to a roommate from the same WG. |  |
| [ ] | Assignee selector lists only roommates from the current WG. |  |
| [ ] | Assignee selector does not list users from other WGs. |  |
| [ ] | Assigning to a non-existing or removed user is handled gracefully. |  |
| [ ] | Assigned user is displayed clearly in the task list/detail view. |  |
| [ ] | Assignment due date/status is displayed if implemented. |  |
| [ ] | Reassignment works correctly if implemented. |  |
| [ ] | Duplicate assignment behavior is clear and consistent with backend. |  |
| [ ] | Assignment failure from backend is shown clearly. |  |
| [ ] | Task assignment is covered by automated frontend or E2E test. |  |

### 4.4 Task Tracking / Status UI

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Task status is visible. |  |
| [ ] | Assigned user can see their assigned task. |  |
| [ ] | Task creator/Main Tenant can see created/assigned task if intended. |  |
| [ ] | Completing or changing status works if implemented in FR-03. |  |
| [ ] | Unauthorized status changes are prevented or handled gracefully. |  |
| [ ] | Status change updates UI after backend success. |  |
| [ ] | Status change does not falsely update UI after backend failure. |  |
| [ ] | Task list remains consistent after page refresh. |  |

---

## 5. End-to-End Frontend Acceptance Scenario

Run this manually and, ideally, also automate it.

| Step | Expected result | Status | Evidence / Notes |
|---|---|---|---|
| 1 | Open app as logged-out user. | [ ] |  |
| 2 | Register Anna. | [ ] |  |
| 3 | Anna logs in or is logged in automatically according to implementation. | [ ] |  |
| 4 | Anna creates a WG. | [ ] |  |
| 5 | Anna can see WG context and invite action. | [ ] |  |
| 6 | Anna creates/copies invitation link. | [ ] |  |
| 7 | Log out or open new browser profile. | [ ] |  |
| 8 | Register Lukas or log in as Lukas. | [ ] |  |
| 9 | Open invitation link as Lukas. | [ ] |  |
| 10 | Lukas accepts invitation and sees WG context. | [ ] |  |
| 11 | Anna or authorized user creates a task. | [ ] |  |
| 12 | Task is assigned to Lukas. | [ ] |  |
| 13 | Lukas logs in and sees his assigned task. | [ ] |  |
| 14 | Browser refresh keeps the workflow state consistent. | [ ] |  |
| 15 | Direct access to protected pages while logged out redirects/blocks correctly. | [ ] |  |

---

## 6. Usability Gate

The proposal requires main workflows to be completable without external instructions by a peer tester.

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | A peer tester can register without explanation. |  |
| [ ] | A peer tester can log in without explanation. |  |
| [ ] | A peer tester understands how to create a WG. |  |
| [ ] | A peer tester understands how to invite a roommate. |  |
| [ ] | A peer tester understands how to accept an invitation. |  |
| [ ] | A peer tester understands how to create a task. |  |
| [ ] | A peer tester understands how to assign a task. |  |
| [ ] | Form labels are clear and not only placeholder text. |  |
| [ ] | Error messages explain what the user can do next. |  |
| [ ] | Success messages/redirects make it clear that the action worked. |  |
| [ ] | User is not trapped after failed login, failed invitation, or failed task creation. |  |

---

## 7. Responsive Layout and Browser Compatibility Gate

Reference targets from the proposal: minimum 360 CSS px width, iPhone 12 Pro/Safari, Pixel 7/Chrome, desktop Chrome/Firefox at 1366x768+.

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Registration page is usable at 360px width. |  |
| [ ] | Login page is usable at 360px width. |  |
| [ ] | WG creation page is usable at 360px width. |  |
| [ ] | Invitation acceptance page is usable at 360px width. |  |
| [ ] | Task page is usable at 360px width. |  |
| [ ] | Task assignment controls are usable on mobile width. |  |
| [ ] | Main workflow is usable at desktop 1366x768. |  |
| [ ] | No important button is hidden or inaccessible on mobile. |  |
| [ ] | No horizontal scrolling occurs on normal mobile widths unless unavoidable and documented. |  |
| [ ] | Tested in Chrome. |  |
| [ ] | Tested in Firefox or documented if not available. |  |
| [ ] | Tested in Safari/iOS simulator/device or documented if not available. |  |

---

## 8. Accessibility and Basic Quality Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Interactive elements are reachable by keyboard. |  |
| [ ] | Forms have labels associated with inputs. |  |
| [ ] | Buttons have clear accessible text. |  |
| [ ] | Error messages are visible and understandable. |  |
| [ ] | Color is not the only way to identify errors or statuses. |  |
| [ ] | Focus order is logical on registration/login/WG/task pages. |  |
| [ ] | Page titles/headings make the current screen understandable. |  |
| [ ] | Modals/dialogs, if used, can be closed and do not trap the user incorrectly. |  |
| [ ] | App does not have major layout shifts during core workflows. |  |

---

## 9. Frontend Automated Test Coverage Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Automated test covers successful registration or mocked equivalent. |  |
| [ ] | Automated test covers login. |  |
| [ ] | Automated test covers protected route behavior. |  |
| [ ] | Automated test covers WG creation. |  |
| [ ] | Automated test covers invitation creation or invite link display. |  |
| [ ] | Automated test covers invitation acceptance if feasible. |  |
| [ ] | Automated test covers task creation. |  |
| [ ] | Automated test covers task assignment. |  |
| [ ] | Tests use stable selectors where possible, not brittle text-only selectors everywhere. |  |
| [ ] | Tests can run in CI or the limitation is documented. |  |

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

## 11. Frontend Final Decision

| Decision item | Status / Notes |
|---|---|
| FR-01 frontend complete? |  |
| FR-02 frontend complete? |  |
| FR-03 frontend complete? |  |
| Critical usability issues open? |  |
| Critical responsive layout issues open? |  |
| Automated frontend/E2E tests passing? |  |
| Frontend ready for dev acceptance? |  |

Final decision:

- [ ] Passed
- [ ] Passed with non-blocking issues
- [ ] Failed, must fix before acceptance
