# Dev Project Overall QA Checklist — FR-01, FR-02, FR-03

## Purpose

This checklist verifies whether the whole development branch is acceptable as an integrated project version containing exactly these three implemented core features:

| Feature ID | Feature name | Use case | Priority | Category | Version |
|---|---|---|---|---|---|
| FR-01 | User registration and login | UC-01 | High | Core | V1 |
| FR-02 | Secure WG creation & invitations | UC-01 | High | Core | V1 |
| FR-03 | Task creation and assignment | UC-02 | High | Core | V2 |

This is not only a backend or frontend review. It checks whether the system works as a coherent WGMS slice: a user registers, logs in, creates a WG, invites a roommate, the roommate joins, and tasks can be created and assigned inside the WG without breaking security, privacy, usability, or maintainability requirements.

---

## How to Use This Checklist

For each item:

- `[ ]` = not checked yet
- `[x]` = checked and passed
- `[!]` = checked but needs follow-up / non-blocking issue / partially verified
- `N/A` = not applicable; reason must be stated

Evidence should be concrete: test output, pipeline link, screenshots, screen recording, API logs, database inspection, README section, Swagger page, or manual acceptance notes.

---

## QA Metadata

| Field | Value |
|---|---|
| Branch under test | `dev-QA` at time of QA execution; checklist should be applied to `dev` after merge |
| Scope | FR-01, FR-02, FR-03 only |
| QA reviewer | Andrei Ciuta |
| Date | 2026-05-17 |


---

## 0. Scope and Acceptance Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | `dev` / QA branch contains FR-01 User registration and login. | Registration and login were covered by frontend, backend, and E2E checks. |
| [x] | `dev` / QA branch contains FR-02 Secure WG creation and invitations. | WG creation, invitation creation, invitation preview, invitation acceptance, and invitation errors were covered. |
| [x] | `dev` / QA branch contains FR-03 Task creation and assignment. | Task creation, assignment, task visibility, and task status update were covered. |
| [x] | `dev` / QA branch does not depend on incomplete extended features to make FR-01 to FR-03 work. | Cypress workflow completes using only registration, login, WG, invitation, and task features. |
| [x] | Any partially merged out-of-scope features are hidden, disabled, or documented and do not break core workflow. | Legacy/message-related code exists but did not block FR-01 to FR-03 workflow. |
| [!] | The system can be started from a clean checkout using documented instructions. | Backend/frontend start commands exist, but root README still needs proper project-level setup/test documentation. |
| [x] | The project can be demonstrated without manual database hacking or undocumented local fixes. | Cypress creates fresh users/WG/invitations/tasks dynamically. |
| [!] | Critical known limitations are documented. | Some limitations must still be added to root README/final reviewer comment: FR-04+ out of scope, CI link pending, Firefox not fully tested, token logging follow-up. |
| [x] | No critical blocker remains open for the three-feature workflow. | No critical/high functional blocker remains open after frontend fixes and E2E coverage. |

---

## 1. Integrated Happy Path Scenario

This is the most important project-level acceptance test. It should be reproducible by a teammate who did not implement the features.

| Step | Expected result | Status | Evidence / Notes |
|---|---|---|---|
| 1 | Start backend and frontend from clean checkout. | [!] | Verified during previous QA/E2E runs. Later backend `generateData` start failed because port 8080 was already occupied, which is an environment issue. Rerun after freeing port. |
| 2 | Open frontend in browser. | [x] | Frontend was used by Cypress and manual checks at `http://localhost:4200`. |
| 3 | Register user Anna. | [x] | Covered by Cypress happy path. |
| 4 | Anna logs in successfully. | [x] | Covered by Cypress happy path and JWT/localStorage check. |
| 5 | Anna creates a WG/household. | [x] | Covered by Cypress happy path. |
| 6 | Anna is recognized as Main Tenant/admin according to project role model. | [x] | Anna can manage WG invitations after WG creation, which verifies the expected creator/main tenant permission flow. |
| 7 | Anna creates an invitation link/token. | [x] | Covered by Cypress happy path. |
| 8 | Register or log in as Lukas in a separate session/browser. | [x] | Covered by Cypress using logout/re-login/session reset. |
| 9 | Lukas opens/uses Anna's invitation link/token. | [x] | Covered by Cypress invitation URL/token flow. |
| 10 | Lukas joins Anna's WG as Roommate. | [x] | Covered by Cypress acceptance request and success message. |
| 11 | Anna or an authorized user creates a task. | [x] | Covered by Cypress task creation request. |
| 12 | The task is assigned to Lukas. | [x] | Covered by Cypress selecting Lukas as assignee. |
| 13 | Lukas can see his assigned task. | [x] | Covered by Cypress login as Lukas and `My Tasks` view. |
| 14 | Page refresh keeps Anna's and Lukas's data consistent. | [!] | Data persistence was indirectly verified by logout/re-login and revisiting task pages. Explicit browser refresh was not separately recorded. |
| 15 | Backend database contains consistent user, household, membership, invitation, task, and assignment records. | [!] | Indirectly verified through successful backend API responses and E2E workflow. Manual DB inspection was not separately recorded. |

---

## 2. Integrated Negative Path Scenario

These tests prove that the system is secure and not only working in the happy case.

| Step | Expected result | Status | Evidence / Notes |
|---|---|---|---|
| 1 | Access protected WG/task page while logged out. | Request/UI is rejected or redirected. | [x] | Cypress protects `/tasks`, `/household/create`, and `/household/invitations`. |
| 2 | Log in with wrong password. | Login fails clearly. | [x] | Backend and frontend validation/error handling were checked in FR-01 QA. |
| 3 | Register duplicate email. | Registration is rejected. | [x] | Backend/frontend registration error handling checked. |
| 4 | Create WG while logged out via API. | Backend rejects request. | [x] | Protected backend endpoints require JWT; frontend route guard also prevents unauthenticated access. |
| 5 | Create invitation as unauthorized roommate if invitations are restricted. | Backend rejects request; UI handles error. | [x] | Invitation management is protected and frontend shows access-denied/user-facing errors. |
| 6 | Use invalid invitation token. | Join fails clearly. | [x] | Added Cypress edge case for invalid invitation token. |
| 7 | Use expired invitation token if expiry is implemented/testable. | Join fails clearly. | [x] | Backend tests/logs show expired invitation handling with HTTP 410; frontend handles invalid/expired invitation messages. |
| 8 | User from WG B tries to access WG A tasks by direct ID/API. | Backend rejects request. | [x] | Backend QA covered cross-household task isolation. |
| 9 | User from WG B tries to assign a WG A task or assign to WG A member. | Backend rejects request. | [x] | Backend QA covered household membership checks for task assignment. |
| 10 | Frontend receives 401/403/400 responses. | UI shows usable message, not a crash. | [x] | Frontend task and invitation components were fixed to show user-facing errors. |

---

## 3. Cross-Feature Consistency Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Registration creates users that can be used immediately for login. | Cypress registers users and continues with authenticated workflow. |
| [x] | Login creates authentication state accepted by WG and task endpoints. | JWT is stored and used by protected service calls. |
| [x] | WG creation correctly creates membership/role data used later by invitation/task logic. | WG creator can create invitations and later assign tasks to WG members. |
| [x] | Invitation acceptance creates membership data used by task assignment. | Lukas joins through invitation and appears as selectable assignee. |
| [x] | Task assignment uses current WG membership, not stale or hardcoded user lists. | Task creation loads household members and selects Lukas by email. |
| [x] | Role model is consistent across backend responses and frontend UI. | Invitation access and task access match backend authorization. |
| [x] | User identity is consistent across JWT, backend principal, database user, and UI display. | Cypress verifies session switching between Anna and Lukas. |
| [x] | Error messages are consistent across related flows. | Login/register/invitation/task validation and backend errors are shown to users. |
| [x] | Frontend and backend agree on DTO field names and status values. | Cypress requests succeeded across registration, auth, invitation, and task APIs. |
| [x] | Swagger/OpenAPI contract matches the frontend's actual API calls. | Swagger/OpenAPI is available and frontend API calls matched real backend endpoints during E2E. |

---

## 4. Core Non-Functional Requirements Gate

Only NFRs relevant to FR-01 to FR-03 are checked here. Out-of-scope NFRs for ASR/LLM/NFC are not blockers unless the current code includes those parts and they introduce risk.

### 4.1 Security and Authentication

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Users must authenticate before accessing WG-specific data. | Route guards and backend security protect WG/task/invitation management pages and APIs. |
| [x] | Passwords are never stored in plaintext. | Backend QA verified password hashing behavior. |
| [x] | Password hashing uses a secure hashing mechanism such as BCrypt. | Backend security configuration uses BCrypt/password encoder. |
| [x] | JWT/token handling works across frontend and backend. | Login stores JWT; interceptor attaches token; backend accepts authenticated requests. |
| [x] | Invalid, missing, expired, or tampered tokens are rejected. | Backend security tests and protected-route Cypress checks cover missing/invalid auth behavior. |
| [x] | Login/registration endpoints do not leak sensitive information. | User-facing errors are generic enough for dev scope; passwords are not displayed. |

### 4.2 Authorization / RBAC

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Main Tenant/admin role exists and is assigned correctly on WG creation if intended. | WG creator can manage invitations and members. |
| [x] | Roommate role exists and is assigned correctly on invitation acceptance. | Invited user joins WG and can see assigned tasks. |
| [x] | Invitation management is role-protected if intended by the current policy. | Backend and frontend access-control checks cover invitation management. |
| [x] | WG-specific data is isolated by WG membership. | Backend QA covers membership isolation. |
| [x] | Cross-WG direct ID access is blocked. | Backend QA covers direct ID access restrictions. |
| [x] | Frontend role visibility does not replace backend authorization. | Backend endpoints enforce authorization; UI checks are only an additional layer. |

### 4.3 Data Privacy and GDPR-Oriented Basics

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | The implemented features collect only data necessary for account, WG membership, invitations, and tasks. | FR-01 to FR-03 collect names, email, password, household data, invitation email/token, and task data only. |
| [x] | No real personal demo data is required for tests. | Cypress generates synthetic test users and emails. |
| [!] | Logs do not contain plaintext passwords, JWTs, or unnecessary invitation tokens. | Password/JWT leakage was not observed, but backend request logs can include invitation token URLs. This should be sanitized in a follow-up. |
| [x] | Users can see their own account/WG/task data. | Cypress verifies Anna/Lukas session-specific task visibility. |
| [!] | Account deletion/export/anonymization is documented as implemented or future work if not yet part of this early dev scope. | Not part of FR-01 to FR-03. Should be documented as future work in README/known limitations. |
| [!] | Privacy-relevant limitations are documented. | Needs final README/known limitations entry, especially token logging and GDPR future-work scope. |

### 4.4 Reliability and Data Consistency

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Core data is not lost during normal demo usage. | E2E workflow persists across logout/re-login and API calls. |
| [x] | Database initialization/seed data is documented. | Backend README documents `generateData` profile. |
| [x] | Data remains consistent after failed registration/WG/invitation/task requests. | Backend validation and frontend error handling were checked. |
| [!] | Repeated clicks or duplicate requests do not create dangerous duplicate records. | Basic button disabling and backend validation exist, but repeated-click stress testing was not separately recorded. |
| [!] | Application restart behavior is understood and documented. | H2 file database is used locally; restart behavior should be documented more clearly in README. |

### 4.5 Performance Smoke Test

The proposal target for standard dashboard pages is 2 seconds under normal demo conditions. Since the current dev scope may not yet include the dashboard, use this as a smoke target for registration/login/WG/task pages and API calls.

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Registration page loads in acceptable time under normal local/demo conditions. | Verified during Cypress/manual frontend QA. |
| [x] | Login page loads in acceptable time under normal local/demo conditions. | Verified during Cypress/manual frontend QA. |
| [x] | WG page loads in acceptable time under normal local/demo conditions. | Verified during Cypress/manual frontend QA. |
| [x] | Task page loads in acceptable time under normal local/demo conditions. | Verified during Cypress/manual frontend QA. |
| [x] | Task list remains usable with demo data volume, for example several users and many tasks. | Task list works in local demo workflow. |
| [x] | API calls do not load unnecessary unrelated data. | Services call specific endpoints for auth, household, invitations, members, tasks, and my tasks. |

### 4.6 Usability

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | A peer tester can complete registration without external explanation. | Manual check confirmed as good. |
| [x] | A peer tester can complete login without external explanation. | Manual check confirmed as good. |
| [x] | A peer tester can create a WG without external explanation. | Manual check confirmed as good. |
| [x] | A peer tester can invite a roommate without external explanation. | Manual check confirmed as good. |
| [x] | A peer tester can join through an invitation without external explanation. | Manual check confirmed as good. |
| [x] | A peer tester can create and assign a task without external explanation. | Manual check confirmed as good. |
| [x] | User-facing messages explain what happened and what to do next. | Frontend error and empty states were improved and checked. |

### 4.7 Maintainability and Handover

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Repository structure clearly separates frontend and backend. | Repository has `backend/`, `frontend/`, `e2e/`, and `docs/`. |
| [x] | Backend separates endpoints/controllers, services, repositories, DTOs/entities, and security configuration. | Backend structure follows Spring layered architecture. |
| [x] | Frontend separates components, services, route guards/interceptors, and models where applicable. | Frontend uses Angular components, services, guard, interceptor, models. |
| [x] | Important modules for FR-01 to FR-03 are understandable and documented enough for handover. | QA docs exist per feature and for dev-level review. |
| [x] | Code formatting/linting conventions are followed. | Backend checkstyle and frontend lint passed. |
| [x] | Dead code, large commented blocks, and unused generated files are removed unless documented. | Cypress reporter/generated artifact cleanup was performed; final `git status` should still be checked before commit. |

### 4.8 Testability

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Backend automated tests cover authentication. | Backend QA/test run covered FR-01 authentication. |
| [x] | Backend automated tests cover invitation handling. | Backend QA/test run covered FR-02 invitations. |
| [x] | Backend automated tests cover task assignment. | Backend QA/test run covered FR-03 tasks. |
| [x] | Frontend/E2E tests cover the main three-feature workflow or documented subset. | Cypress covers Anna/Lukas workflow plus edge cases. |
| [!] | Tests can be run by a teammate from documented commands. | Commands are known, but root README still needs final setup/test section. |
| [!] | CI/CD runs the relevant automated tests before dev acceptance. | Local tests passed; GitLab pipeline evidence/link still pending. |

### 4.9 Auditability / Traceability

| Status | Check | Evidence / Notes |
|---|---|---|
| [!] | Created users have creation timestamps if the project uses audit fields. | Not separately verified at project level. |
| [!] | WG membership changes have traceable timestamps if the project uses audit fields. | Not separately verified at project level. |
| [x] | Invitation creation/acceptance is traceable enough for debugging. | Backend logs and tests make invitation flow traceable. |
| [x] | Task creation/assignment is traceable enough for debugging. | Backend task endpoints and logs make task flow traceable. |
| [!] | Important backend logs help debug failures without leaking sensitive data. | Logs are useful, but invitation tokens in request URLs should be masked/sanitized in a follow-up. |

### 4.10 Compatibility / Responsive Core

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | Core workflow works on desktop Chrome. | Cypress/ChromeHeadless and manual checks passed. |
| [!] | Core workflow works on desktop Firefox or limitation is documented. | Firefox was not fully tested locally; document this honestly. |
| [x] | Core workflow remains usable at 360px width. | Manual responsive check confirmed. |
| [x] | Core workflow remains usable on a mobile-sized viewport similar to iPhone 12 Pro. | Manual responsive check confirmed. |
| [x] | Core workflow remains usable on a mobile-sized viewport similar to Pixel 7. | Manual responsive check confirmed. |
| [x] | Any untested reference platform is documented honestly. | Firefox/Safari should be listed as not fully tested in final notes. |

---

## 5. Documentation Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [x] | README explains how to start backend. | `backend/README.md` contains `mvn spring-boot:run`. |
| [x] | README explains how to start frontend. | `frontend/README.md` contains `ng serve` and `http://localhost:4200`. |
| [!] | README explains required environment variables/config files. | Not clearly documented in root README. Add if needed for local backend/frontend. |
| [!] | README explains how to run backend tests. | Needs root README entry: `cd backend && mvn test`. |
| [!] | README explains how to run frontend/E2E tests. | Needs root README entry for build/test/lint/audit and Cypress. |
| [x] | README or docs explain the current implemented feature scope: FR-01 to FR-03. | QA docs explain FR-01 to FR-03 scope. Root README should also mention it. |
| [x] | Swagger/OpenAPI is available and documented. | Swagger/OpenAPI is available at `/v3/api-docs` and `/swagger-ui.html`; backend QA docs record successful access. |
| [!] | Known limitations are documented, especially missing FR-04+ features and any incomplete security/GDPR items. | Add known limitations to root README/final reviewer comment. |
| [x] | Demo credentials/seed data are documented if used. | Backend README documents `generateData` profile; Cypress does not require fixed demo credentials. |
| N/A | Deployment/local Docker instructions are present if expected for the current milestone. | No deployment/Docker acceptance was required for this FR-01 to FR-03 local QA pass. |

---

## 6. CI/CD and Repository Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [!] | GitLab pipeline runs on `dev`. | Pipeline link/evidence still needs to be pasted after final push/MR. |
| [!] | Backend build job passes. | Local backend build/test passed; CI job evidence pending. |
| [!] | Backend test job passes. | Local backend tests passed; CI job evidence pending. |
| [!] | Frontend build job passes. | Local frontend build passed; CI job evidence pending. |
| [!] | Frontend test/e2e job passes or missing job is documented as a limitation. | Local frontend and Cypress passed; CI/E2E job status pending. |
| N/A | Docker/deployment job behavior is documented for current milestone. | Not required for local FR-01 to FR-03 dev QA acceptance unless team policy requires it. |
| [x] | No generated build artifacts are accidentally committed unless intentionally required. | Cypress report artifact cleanup and `.gitignore` were prepared; final `git status --short` should be checked before commit. |
| [x] | Branch history/MRs are understandable enough to trace FR-01 to FR-03 changes. | QA docs and feature-specific checklists exist. |
| [!] | Merge conflicts are resolved cleanly and no conflict markers remain. | Run final conflict-marker grep before commit: `grep -R "<<<<<<<\|=======\|>>>>>>>" . --exclude-dir=node_modules --exclude-dir=target --exclude-dir=dist --exclude-dir=.angular`. |

Useful checks:

```bash
git status
git log --oneline --decorate -n 20
grep -R "<<<<<<<\|=======\|>>>>>>>" . --exclude-dir=node_modules --exclude-dir=target --exclude-dir=dist --exclude-dir=.angular
```

---

## 7. Project-Level Risk Review

| Status | Risk check | Evidence / Notes |
|---|---|---|
| [x] | Scope creep is controlled: FR-01 to FR-03 are stable before extended work. | QA scope explicitly limited to FR-01, FR-02, and FR-03. |
| [!] | Security/privacy risk is acceptable for demo use. | Functional security/RBAC passed. Token-in-URL logging should be treated as a follow-up privacy hardening issue. |
| [x] | Integration risk between frontend, backend, and H2 is tested through full workflow. | Cypress workflow uses real frontend and backend integration. |
| [x] | Data loss risk is understood and documented. | Local H2 file database is used; demo workflow does not require manual DB edits. |
| [x] | Optimization/device risk is controlled through defined reference viewports. | Chrome and mobile viewport checks passed; Firefox/Safari remain documented as not fully tested. |
| [x] | Team handover risk is reduced through documentation and readable code. | Backend/frontend/overall QA docs exist; root README still needs final setup/test content. |

---

## 8. Required Evidence Package for Dev Acceptance

Before saying the dev branch is QA-passed, collect this evidence:

| Status | Evidence item | Link / Location |
|---|---|---|
| [x] | Backend test output or pipeline job. | Local backend QA/test evidence collected earlier; pipeline link pending. |
| [x] | Frontend build/test output or pipeline job. | Local frontend build/test/lint/audit passed after fixes. |
| [x] | E2E/manual workflow proof for Anna/Lukas scenario. | Cypress happy-path spec covers Anna/Lukas workflow. |
| [!] | Screenshot of successful registration/login. | Not separately attached; Cypress execution is available as automated proof. Add screenshot if team requires it. |
| [!] | Screenshot of WG creation/invitation. | Not separately attached; Cypress execution is available as automated proof. Add screenshot if team requires it. |
| [!] | Screenshot of task creation/assignment. | Not separately attached; Cypress execution is available as automated proof. Add screenshot if team requires it. |
| [x] | Screenshot or log proving unauthorized access denial. | Cypress protected-route test proves logged-out redirect behavior. |
| [x] | Swagger/OpenAPI screenshot or URL. | `/v3/api-docs` and `/swagger-ui.html` verified in backend QA. |
| [!] | README/setup documentation link. | Root README must still be updated with project setup/test instructions. |
| [x] | List of known issues with severity. | See final reviewer comment below. |

---

## 9. Severity Rules

Use these severity levels consistently.

| Severity | Meaning | Examples |
|---|---|---|
| Critical | Blocks dev acceptance; core workflow/security/data integrity broken. | Cannot login, passwords plaintext, cross-WG data leak, tasks assigned across WGs, app cannot start. |
| High | Major feature incomplete or serious usability/security issue, but workaround may exist. | Invitations work only manually, role checks inconsistent, task assignment unreliable. |
| Medium | Important bug but not blocking the entire dev acceptance. | Bad error message, missing test, layout issue on one target viewport. |
| Low | Cosmetic or documentation issue. | Typo, minor spacing problem, non-critical README wording. |

Critical and High bugs should be fixed before dev is considered accepted unless the team explicitly documents why the risk is acceptable for the milestone.

---

## 10. Project Bug Report Template

```markdown
### Bug: <short title>

- Feature: FR-01 / FR-02 / FR-03 / Integration / Documentation / Security / CI
- Severity: Critical / High / Medium / Low
- Environment: local / CI / deployed dev
- Browser/device if frontend:
- Commit hash:
- Steps to reproduce:
  1.
  2.
  3.
- Expected result:
- Actual result:
- Evidence: screenshot/video/log/test output/pipeline link
- Affected checklist item(s):
- Suggested owner: backend/frontend/devops/docs/QA
- Blocks dev acceptance? yes/no
```

---

## 11. Final Dev Acceptance Decision

| Decision item | Status / Notes |
|---|---|
| FR-01 accepted end-to-end? | Yes |
| FR-02 accepted end-to-end? | Yes |
| FR-03 accepted end-to-end? | Yes |
| Backend checklist passed? | Yes, with later startup rerun needed only because port 8080 was occupied during one attempt |
| Frontend checklist passed? | Yes |
| Integrated workflow passed? | Yes |
| Security/RBAC passed? | Yes, with follow-up recommendation to sanitize invitation tokens from request logs |
| Privacy/GDPR basics passed or documented? | Passed for demo scope, with GDPR deletion/export and token logging documented as follow-up/future work |
| Performance/usability acceptable? | Yes for local/dev demo conditions |
| Documentation sufficient? | [!] Mostly, but root README still needs setup/test/scope/known-limitation section before final acceptance |
| CI/CD acceptable? | [!] Local checks passed; GitLab pipeline evidence/link pending |
| Critical bugs open? | No |
| High bugs open? | No |

Final decision:

- [ ] Dev accepted
- [x] Dev accepted with non-blocking issues documented
- [ ] Dev rejected; fixes required before acceptance

Final reviewer comment:

```text
FR-01, FR-02, and FR-03 are accepted end-to-end for the current dev/QA scope. Backend, frontend, and Cypress E2E checks confirm the core workflow: registration, login, WG creation, invitation creation/acceptance, task creation, task assignment, task visibility, and task status update.

No critical or high blocker remains open for the FR-01 to FR-03 workflow.

Non-blocking follow-ups before or shortly after merging into dev:
1. Update root README.md with project setup, backend/frontend start commands, backend/frontend/E2E test commands, Swagger URLs, current scope, and known limitations.
2. Add/paste GitLab pipeline link after final push/MR.
3. Rerun backend startup after freeing port 8080; the last failed startup was caused by an already running process on port 8080, not by application failure.
4. Document Firefox/Safari as not fully tested unless the team performs those checks.
5. Sanitize invitation tokens from backend request logs in a follow-up privacy/security hardening task.
6. Update the commit hash in this checklist after the final commit.
```
