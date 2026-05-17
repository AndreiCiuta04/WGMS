# Dev Project Overall QA Checklist — FR-01, FR-02, FR-03

## Purpose

This checklist verifies whether the whole `dev` branch is acceptable as an integrated project version containing exactly these three implemented core features:

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
- `[!]` = checked and failed; add a bug note
- `N/A` = not applicable; add a reason

Evidence should be concrete: test output, pipeline link, screenshots, screen recording, API logs, database inspection, README section, Swagger page, or manual acceptance notes.

---

## QA Metadata

| Field | Value |
|---|---|
| Branch under test | `dev` |
| Scope | FR-01, FR-02, FR-03 only |
| QA reviewer |  |
| Date |  |
| Commit hash |  |
| Backend test result |  |
| Frontend test result |  |
| E2E test result |  |
| Pipeline link |  |
| Deployed URL if available |  |

---

## 0. Scope and Acceptance Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | `dev` contains FR-01 User registration and login. |  |
| [ ] | `dev` contains FR-02 Secure WG creation and invitations. |  |
| [ ] | `dev` contains FR-03 Task creation and assignment. |  |
| [ ] | `dev` does not depend on incomplete extended features to make FR-01 to FR-03 work. |  |
| [ ] | Any partially merged out-of-scope features are hidden, disabled, or documented and do not break core workflow. |  |
| [ ] | The system can be started from a clean checkout using documented instructions. |  |
| [ ] | The project can be demonstrated without manual database hacking or undocumented local fixes. |  |
| [ ] | Critical known limitations are documented. |  |
| [ ] | No critical blocker remains open for the three-feature workflow. |  |

---

## 1. Integrated Happy Path Scenario

This is the most important project-level acceptance test. It should be reproducible by a teammate who did not implement the features.

| Step | Expected result | Status | Evidence / Notes |
|---|---|---|---|
| 1 | Start backend and frontend from clean checkout. | [ ] |  |
| 2 | Open frontend in browser. | [ ] |  |
| 3 | Register user Anna. | [ ] |  |
| 4 | Anna logs in successfully. | [ ] |  |
| 5 | Anna creates a WG/household. | [ ] |  |
| 6 | Anna is recognized as Main Tenant/admin according to project role model. | [ ] |  |
| 7 | Anna creates an invitation link/token. | [ ] |  |
| 8 | Register or log in as Lukas in a separate session/browser. | [ ] |  |
| 9 | Lukas opens/uses Anna's invitation link/token. | [ ] |  |
| 10 | Lukas joins Anna's WG as Roommate. | [ ] |  |
| 11 | Anna or an authorized user creates a task. | [ ] |  |
| 12 | The task is assigned to Lukas. | [ ] |  |
| 13 | Lukas can see his assigned task. | [ ] |  |
| 14 | Page refresh keeps Anna's and Lukas's data consistent. | [ ] |  |
| 15 | Backend database contains consistent user, household, membership, invitation, task, and assignment records. | [ ] |  |

---

## 2. Integrated Negative Path Scenario

These tests prove that the system is secure and not only working in the happy case.

| Step | Expected result | Status | Evidence / Notes |
|---|---|---|---|
| 1 | Access protected WG/task page while logged out. | Request/UI is rejected or redirected. | [ ] |  |
| 2 | Log in with wrong password. | Login fails clearly. | [ ] |  |
| 3 | Register duplicate email. | Registration is rejected. | [ ] |  |
| 4 | Create WG while logged out via API. | Backend rejects request. | [ ] |  |
| 5 | Create invitation as unauthorized roommate if invitations are restricted. | Backend rejects request; UI handles error. | [ ] |  |
| 6 | Use invalid invitation token. | Join fails clearly. | [ ] |  |
| 7 | Use expired invitation token if expiry is implemented/testable. | Join fails clearly. | [ ] |  |
| 8 | User from WG B tries to access WG A tasks by direct ID/API. | Backend rejects request. | [ ] |  |
| 9 | User from WG B tries to assign a WG A task or assign to WG A member. | Backend rejects request. | [ ] |  |
| 10 | Frontend receives 401/403/400 responses. | UI shows usable message, not a crash. | [ ] |  |

---

## 3. Cross-Feature Consistency Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Registration creates users that can be used immediately for login. |  |
| [ ] | Login creates authentication state accepted by WG and task endpoints. |  |
| [ ] | WG creation correctly creates membership/role data used later by invitation/task logic. |  |
| [ ] | Invitation acceptance creates membership data used by task assignment. |  |
| [ ] | Task assignment uses current WG membership, not stale or hardcoded user lists. |  |
| [ ] | Role model is consistent across backend responses and frontend UI. |  |
| [ ] | User identity is consistent across JWT, backend principal, database user, and UI display. |  |
| [ ] | Error messages are consistent across related flows. |  |
| [ ] | Frontend and backend agree on DTO field names and status values. |  |
| [ ] | Swagger/OpenAPI contract matches the frontend's actual API calls. |  |

---

## 4. Core Non-Functional Requirements Gate

Only NFRs relevant to FR-01 to FR-03 are checked here. Out-of-scope NFRs for ASR/LLM/NFC are not blockers unless the current code includes those parts and they introduce risk.

### 4.1 Security and Authentication

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Users must authenticate before accessing WG-specific data. |  |
| [ ] | Passwords are never stored in plaintext. |  |
| [ ] | Password hashing uses a secure hashing mechanism such as BCrypt. |  |
| [ ] | JWT/token handling works across frontend and backend. |  |
| [ ] | Invalid, missing, expired, or tampered tokens are rejected. |  |
| [ ] | Login/registration endpoints do not leak sensitive information. |  |

### 4.2 Authorization / RBAC

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Main Tenant/admin role exists and is assigned correctly on WG creation if intended. |  |
| [ ] | Roommate role exists and is assigned correctly on invitation acceptance. |  |
| [ ] | Invitation management is role-protected if intended by the current policy. |  |
| [ ] | WG-specific data is isolated by WG membership. |  |
| [ ] | Cross-WG direct ID access is blocked. |  |
| [ ] | Frontend role visibility does not replace backend authorization. |  |

### 4.3 Data Privacy and GDPR-Oriented Basics

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | The implemented features collect only data necessary for account, WG membership, invitations, and tasks. |  |
| [ ] | No real personal demo data is required for tests. |  |
| [ ] | Logs do not contain plaintext passwords, JWTs, or unnecessary invitation tokens. |  |
| [ ] | Users can see their own account/WG/task data. |  |
| [ ] | Account deletion/export/anonymization is documented as implemented or future work if not yet part of this early dev scope. |  |
| [ ] | Privacy-relevant limitations are documented. |  |

### 4.4 Reliability and Data Consistency

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Core data is not lost during normal demo usage. |  |
| [ ] | Database initialization/seed data is documented. |  |
| [ ] | Data remains consistent after failed registration/WG/invitation/task requests. |  |
| [ ] | Repeated clicks or duplicate requests do not create dangerous duplicate records. |  |
| [ ] | Application restart behavior is understood and documented. |  |

### 4.5 Performance Smoke Test

The proposal target for standard dashboard pages is 2 seconds under normal demo conditions. Since the current dev scope may not yet include the dashboard, use this as a smoke target for registration/login/WG/task pages and API calls.

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Registration page loads in acceptable time under normal local/demo conditions. |  |
| [ ] | Login page loads in acceptable time under normal local/demo conditions. |  |
| [ ] | WG page loads in acceptable time under normal local/demo conditions. |  |
| [ ] | Task page loads in acceptable time under normal local/demo conditions. |  |
| [ ] | Task list remains usable with demo data volume, for example several users and many tasks. |  |
| [ ] | API calls do not load unnecessary unrelated data. |  |

### 4.6 Usability

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | A peer tester can complete registration without external explanation. |  |
| [ ] | A peer tester can complete login without external explanation. |  |
| [ ] | A peer tester can create a WG without external explanation. |  |
| [ ] | A peer tester can invite a roommate without external explanation. |  |
| [ ] | A peer tester can join through an invitation without external explanation. |  |
| [ ] | A peer tester can create and assign a task without external explanation. |  |
| [ ] | User-facing messages explain what happened and what to do next. |  |

### 4.7 Maintainability and Handover

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Repository structure clearly separates frontend and backend. |  |
| [ ] | Backend separates endpoints/controllers, services, repositories, DTOs/entities, and security configuration. |  |
| [ ] | Frontend separates components, services, route guards/interceptors, and models where applicable. |  |
| [ ] | Important modules for FR-01 to FR-03 are understandable and documented enough for handover. |  |
| [ ] | Code formatting/linting conventions are followed. |  |
| [ ] | Dead code, large commented blocks, and unused generated files are removed unless documented. |  |

### 4.8 Testability

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Backend automated tests cover authentication. |  |
| [ ] | Backend automated tests cover invitation handling. |  |
| [ ] | Backend automated tests cover task assignment. |  |
| [ ] | Frontend/E2E tests cover the main three-feature workflow or documented subset. |  |
| [ ] | Tests can be run by a teammate from documented commands. |  |
| [ ] | CI/CD runs the relevant automated tests before dev acceptance. |  |

### 4.9 Auditability / Traceability

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Created users have creation timestamps if the project uses audit fields. |  |
| [ ] | WG membership changes have traceable timestamps if the project uses audit fields. |  |
| [ ] | Invitation creation/acceptance is traceable enough for debugging. |  |
| [ ] | Task creation/assignment is traceable enough for debugging. |  |
| [ ] | Important backend logs help debug failures without leaking sensitive data. |  |

### 4.10 Compatibility / Responsive Core

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | Core workflow works on desktop Chrome. |  |
| [ ] | Core workflow works on desktop Firefox or limitation is documented. |  |
| [ ] | Core workflow remains usable at 360px width. |  |
| [ ] | Core workflow remains usable on a mobile-sized viewport similar to iPhone 12 Pro. |  |
| [ ] | Core workflow remains usable on a mobile-sized viewport similar to Pixel 7. |  |
| [ ] | Any untested reference platform is documented honestly. |  |

---

## 5. Documentation Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | README explains how to start backend. |  |
| [ ] | README explains how to start frontend. |  |
| [ ] | README explains required environment variables/config files. |  |
| [ ] | README explains how to run backend tests. |  |
| [ ] | README explains how to run frontend/E2E tests. |  |
| [ ] | README or docs explain the current implemented feature scope: FR-01 to FR-03. |  |
| [ ] | Swagger/OpenAPI is available and documented. |  |
| [ ] | Known limitations are documented, especially missing FR-04+ features and any incomplete security/GDPR items. |  |
| [ ] | Demo credentials/seed data are documented if used. |  |
| [ ] | Deployment/local Docker instructions are present if expected for the current milestone. |  |

---

## 6. CI/CD and Repository Gate

| Status | Check | Evidence / Notes |
|---|---|---|
| [ ] | GitLab pipeline runs on `dev`. |  |
| [ ] | Backend build job passes. |  |
| [ ] | Backend test job passes. |  |
| [ ] | Frontend build job passes. |  |
| [ ] | Frontend test/e2e job passes or missing job is documented as a limitation. |  |
| [ ] | Docker/deployment job behavior is documented for current milestone. |  |
| [ ] | No generated build artifacts are accidentally committed unless intentionally required. |  |
| [ ] | Branch history/MRs are understandable enough to trace FR-01 to FR-03 changes. |  |
| [ ] | Merge conflicts are resolved cleanly and no conflict markers remain. |  |

Useful checks:

```bash
git status
git log --oneline --decorate -n 20
grep -R "<<<<<<<\|=======\|>>>>>>>" .
```

---

## 7. Project-Level Risk Review

| Status | Risk check | Evidence / Notes |
|---|---|---|
| [ ] | Scope creep is controlled: FR-01 to FR-03 are stable before extended work. |  |
| [ ] | Security/privacy risk is acceptable for demo use. |  |
| [ ] | Integration risk between frontend, backend, and H2 is tested through full workflow. |  |
| [ ] | Data loss risk is understood and documented. |  |
| [ ] | Optimization/device risk is controlled through defined reference viewports. |  |
| [ ] | Team handover risk is reduced through documentation and readable code. |  |

---

## 8. Required Evidence Package for Dev Acceptance

Before saying the dev branch is QA-passed, collect this evidence:

| Status | Evidence item | Link / Location |
|---|---|---|
| [ ] | Backend test output or pipeline job. |  |
| [ ] | Frontend build/test output or pipeline job. |  |
| [ ] | E2E/manual workflow proof for Anna/Lukas scenario. |  |
| [ ] | Screenshot of successful registration/login. |  |
| [ ] | Screenshot of WG creation/invitation. |  |
| [ ] | Screenshot of task creation/assignment. |  |
| [ ] | Screenshot or log proving unauthorized access denial. |  |
| [ ] | Swagger/OpenAPI screenshot or URL. |  |
| [ ] | README/setup documentation link. |  |
| [ ] | List of known issues with severity. |  |

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
| FR-01 accepted end-to-end? |  |
| FR-02 accepted end-to-end? |  |
| FR-03 accepted end-to-end? |  |
| Backend checklist passed? |  |
| Frontend checklist passed? |  |
| Integrated workflow passed? |  |
| Security/RBAC passed? |  |
| Privacy/GDPR basics passed or documented? |  |
| Performance/usability acceptable? |  |
| Documentation sufficient? |  |
| CI/CD acceptable? |  |
| Critical bugs open? |  |
| High bugs open? |  |

Final decision:

- [ ] Dev accepted
- [ ] Dev accepted with non-blocking issues documented
- [ ] Dev rejected; fixes required before acceptance

Final reviewer comment:

```text

```
