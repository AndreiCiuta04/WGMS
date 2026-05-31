# FR-02 Branch QA Checklist — Secure WG Creation & Invitations

## Feature Information

- Feature ID: FR-02
- Feature name: Secure WG creation & invitations
- Use case: UC-01 — User Registration and WG Initialization
- Priority: High
- Category: Core
- Version: V1
- Lead: Dzovani Koller
- Backend QA file: `backend/FR-02_backend_QA.md`
- Frontend QA file: `frontend/FR-02_frontend_QA.md`
- Branch:
- Merge request:
- Developer:
- QA reviewer:
- Date:

---

## How to Use This Checklist

This is the general QA checklist for the complete FR-02 branch.

It checks whether backend, frontend, integration, non-functional requirements, documentation, CI, and merge readiness are complete.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a short note under **QA Notes / Evidence**.
- Do not merge into `dev` until this checklist is passed or explicitly approved with documented exceptions.

---

# 1. Feature Scope

## 1.1 Required User Value

- [ ] A user can create a new WG after registration/login.
- [ ] The WG creator is connected to the created WG.
- [ ] The WG creator receives the correct management role.
- [ ] The WG creator or authorized WG user can invite roommates.
- [ ] Invitations are secure.
- [ ] Invitations are time-limited.
- [ ] Invited users can join the correct WG.
- [ ] Users cannot join a WG without a valid invitation unless another approved flow exists.
- [ ] Users cannot use invitations to gain unauthorized privileges.
- [ ] Users cannot manage another WG's invitations.

## 1.2 Feature Boundary

- [ ] FR-02 only covers secure WG creation and invitations.
- [ ] FR-02 does not include cleaning task management.
- [ ] FR-02 does not include expense tracking.
- [ ] FR-02 does not include shopping list management.
- [ ] FR-02 does not include dashboard aggregation except minimal navigation after WG creation/joining.
- [ ] FR-02 does not include notifications unless explicitly agreed.
- [ ] FR-02 does not include NFC functionality.
- [ ] Any extra implemented functionality is documented and justified.

---

# 2. Backend Completion

- [ ] Backend QA checklist exists.
- [ ] Backend QA checklist is completed.
- [ ] Backend supports WG creation.
- [ ] Backend supports creator-to-WG membership.
- [ ] Backend supports correct creator role assignment.
- [ ] Backend supports invitation creation.
- [ ] Backend supports secure invitation tokens.
- [ ] Backend supports invitation expiry.
- [ ] Backend supports invitation acceptance.
- [ ] Backend blocks invalid invitations.
- [ ] Backend blocks expired invitations.
- [ ] Backend blocks revoked invitations if revocation exists.
- [ ] Backend blocks already-used invitations if single-use.
- [ ] Backend blocks unauthorized WG access.
- [ ] Backend blocks role escalation.
- [ ] Backend tests pass.
- [ ] Backend code was reviewed.
- [ ] Backend has no known critical bugs.

---

# 3. Frontend Completion

- [ ] Frontend QA checklist exists.
- [ ] Frontend QA checklist is completed.
- [ ] Frontend has WG creation UI.
- [ ] Frontend has invitation creation UI.
- [ ] Frontend has invitation acceptance UI.
- [ ] Frontend handles logged-in users correctly.
- [ ] Frontend handles logged-out invitation links correctly.
- [ ] Frontend handles valid invitation links.
- [ ] Frontend handles invalid invitation links.
- [ ] Frontend handles expired invitation links.
- [ ] Frontend handles used/revoked invitation links if applicable.
- [ ] Frontend hides or blocks invitation controls for unauthorized users.
- [ ] Frontend shows clear success and error messages.
- [ ] Frontend works on desktop.
- [ ] Frontend works on mobile.
- [ ] Frontend tests pass.
- [ ] Frontend code was reviewed.
- [ ] Frontend has no known critical bugs.

---

# 4. Full Integration Flow

## 4.1 Happy Path Integration

Complete this manually or through E2E automation.

- [ ] Start backend locally or in test environment.
- [ ] Start frontend locally or in test environment.
- [ ] Use clean test database or known test users.
- [ ] Register/login as User A.
- [ ] User A creates a WG.
- [ ] Backend stores WG correctly.
- [ ] Frontend shows User A inside the WG.
- [ ] User A creates invitation.
- [ ] Backend stores invitation correctly.
- [ ] Frontend displays/copies invitation link.
- [ ] Logout User A or open private browser.
- [ ] Register/login as User B.
- [ ] Open invitation link as User B.
- [ ] User B sees invitation page.
- [ ] User B accepts invitation.
- [ ] Backend connects User B to User A's WG.
- [ ] Frontend shows User B inside correct WG.
- [ ] User B has roommate-level permissions.
- [ ] User B cannot see unauthorized management controls.
- [ ] User A can see User B as part of the WG if member display exists.

## 4.2 Negative Integration Flows

- [ ] Opening invalid invitation link shows clear error.
- [ ] Opening expired invitation link shows clear error.
- [ ] Opening used invitation link shows clear error if invitations are single-use.
- [ ] Trying to create WG while logged out redirects to login.
- [ ] Trying to create invitation while logged out redirects to login.
- [ ] Trying to create invitation as unauthorized user is blocked by backend and frontend.
- [ ] Trying to accept invitation twice is blocked.
- [ ] Trying to manipulate WG ID in frontend/backend request is blocked.
- [ ] Trying to manipulate role in request body is blocked.
- [ ] Network failure during WG creation is handled.
- [ ] Network failure during invitation acceptance is handled.

---

# 5. Security and Privacy

## 5.1 Authentication and Authorization

- [ ] All protected backend endpoints require authentication.
- [ ] Frontend route guards exist where appropriate.
- [ ] Backend does not trust frontend-only route guards.
- [ ] Users cannot access WG data from another WG.
- [ ] Users cannot create invitations for another WG.
- [ ] Users cannot revoke or view invitations from another WG.
- [ ] Normal roommates cannot perform main-tenant/admin actions unless intended.
- [ ] Invitation acceptance does not grant excessive permissions.
- [ ] JWT identity is used server-side.

## 5.2 Invitation Security

- [ ] Invitation tokens are random and sufficiently long.
- [ ] Invitation tokens are not predictable.
- [ ] Invitation tokens expire.
- [ ] Invitation tokens are single-use if this is the agreed design.
- [ ] Invalid tokens do not leak private WG details.
- [ ] Expired tokens do not allow joining.
- [ ] Revoked tokens do not allow joining.
- [ ] Used tokens do not allow joining again if single-use.
- [ ] Tokens are not printed in production logs.
- [ ] Tokens are not exposed through unrelated API responses.
- [ ] Tokens are not unnecessarily stored in frontend local storage.

## 5.3 Data Protection / GDPR Awareness

- [ ] Only necessary user data is processed for invitations.
- [ ] Email addresses are only stored if required.
- [ ] Invitation data is not exposed to unauthorized users.
- [ ] Error messages do not reveal private user information.
- [ ] Logs do not contain sensitive invitation tokens or personal data.
- [ ] Test data does not contain real private user data unless explicitly approved.
- [ ] Screenshots used as evidence do not expose real credentials or private tokens.

---

# 6. Non-Functional Requirements

## 6.1 Reliability

- [ ] WG creation is transactional.
- [ ] Invitation acceptance is transactional.
- [ ] Duplicate clicks do not create corrupted state.
- [ ] Concurrent acceptance of same invitation is handled.
- [ ] Backend handles invalid input safely.
- [ ] Frontend handles API failures gracefully.
- [ ] Database constraints protect core relationships.
- [ ] No known data-corruption issue remains.

## 6.2 Usability

- [ ] WG creation flow is understandable.
- [ ] Invitation flow is understandable.
- [ ] Success states are clear.
- [ ] Error states are clear.
- [ ] User is never left without a next step.
- [ ] User can recover from common errors.
- [ ] Invitation link flow is easy enough for a non-technical roommate.
- [ ] UI language is consistent with the rest of the app.

## 6.3 Accessibility

- [ ] WG creation form has labels.
- [ ] Invitation form has labels.
- [ ] Error messages are visible and understandable.
- [ ] Keyboard navigation works.
- [ ] Focus states are visible.
- [ ] Color is not the only error indicator.
- [ ] Text contrast is acceptable.
- [ ] Main flow is usable on mobile.

## 6.4 Performance

- [ ] WG creation completes in acceptable time locally/test environment.
- [ ] Invitation creation completes in acceptable time locally/test environment.
- [ ] Invitation acceptance completes in acceptable time locally/test environment.
- [ ] No unnecessary repeated API calls occur.
- [ ] Token lookup is indexed or otherwise efficient.
- [ ] No obvious frontend performance issue exists.

## 6.5 Maintainability

- [ ] Backend code follows project architecture.
- [ ] Frontend code follows project architecture.
- [ ] DTOs/interfaces are clear.
- [ ] Business logic is not duplicated unnecessarily.
- [ ] Security checks are centralized or consistently implemented.
- [ ] Error handling is consistent.
- [ ] Tests are readable.
- [ ] Naming is consistent.
- [ ] No debug logs remain.
- [ ] No commented-out experimental code remains.

---

# 7. Code-Oriented Test Coverage

The checklists do not automatically guarantee a percentage. They define what must be tested. Actual numeric code coverage must be measured with the project tools.

## 7.1 Coverage Types Achieved If Implemented Properly

- [ ] Unit test coverage for service/business logic.
- [ ] Repository/data-access coverage for custom queries and constraints.
- [ ] Controller/API integration coverage for HTTP endpoints.
- [ ] Security/authorization coverage for allowed and denied requests.
- [ ] Validation/error-path coverage for invalid input and invalid invitation states.
- [ ] Transactional behavior coverage for WG creation and invitation acceptance.
- [ ] Frontend component coverage for forms, validation, loading, success, and error states.
- [ ] Frontend service coverage for API calls and error mapping.
- [ ] Route guard/interceptor coverage for authentication and JWT handling.
- [ ] E2E/behavioral coverage for complete user flows across frontend and backend.
- [ ] Manual exploratory coverage for usability, responsiveness, and edge cases.

## 7.2 Coverage Not Automatically Guaranteed

- [ ] Exact line coverage percentage is not guaranteed until measured.
- [ ] Exact branch coverage percentage is not guaranteed until measured.
- [ ] Mutation coverage is not guaranteed unless a mutation testing tool is used.
- [ ] Selenium/E2E tests prove user behavior but usually do not count directly as frontend unit-test code coverage.
- [ ] Manual QA proves behavior but does not count as code coverage.
- [ ] High line coverage does not prove security unless authorization and abuse cases are explicitly tested.
- [ ] Frontend route guards do not prove backend security.

## 7.3 Suggested Numeric Targets

- [ ] Backend service/business logic: at least 85% line coverage.
- [ ] Backend service/business logic: at least 75% branch coverage.
- [ ] Backend controller/API layer: at least 70% line coverage.
- [ ] Backend security-sensitive allow/deny cases: 100% behaviorally tested.
- [ ] Backend custom repository queries: each custom query covered at least once.
- [ ] Frontend services: at least 85% line coverage.
- [ ] Frontend components for FR-02: at least 75% line coverage.
- [ ] Frontend route guards/interceptors: at least 80% line coverage.
- [ ] Critical invitation lifecycle states: 100% behaviorally covered.

---

# 8. Automated Test Summary

## 8.1 Backend Tests

- [ ] JUnit tests exist for WG creation.
- [ ] JUnit tests exist for creator membership.
- [ ] JUnit tests exist for invitation creation.
- [ ] JUnit tests exist for invitation expiry.
- [ ] JUnit tests exist for invitation acceptance.
- [ ] JUnit tests exist for invalid invitation cases.
- [ ] JUnit tests exist for authorization failures.
- [ ] JUnit tests exist for role-escalation prevention.
- [ ] Backend tests pass locally.
- [ ] Backend tests pass in CI.
- [ ] Backend coverage report is generated if configured.

## 8.2 Frontend Tests

- [ ] Frontend tests exist for WG creation UI.
- [ ] Frontend tests exist for invitation creation UI.
- [ ] Frontend tests exist for invitation acceptance UI.
- [ ] Frontend tests exist for invalid invitation states.
- [ ] Frontend tests exist for authentication/route behavior.
- [ ] Frontend tests pass locally.
- [ ] Frontend tests pass in CI.
- [ ] Frontend coverage report is generated if configured.

## 8.3 E2E Tests

- [ ] E2E happy path exists or was manually documented.
- [ ] E2E covers User A creating WG.
- [ ] E2E covers User A creating invitation.
- [ ] E2E covers User B accepting invitation.
- [ ] E2E verifies User B ends up in correct WG.
- [ ] E2E verifies invalid invitation handling.
- [ ] E2E verifies unauthorized access is blocked.
- [ ] E2E tests pass or manual equivalent is documented.

---

# 9. CI / Pipeline / Build

- [ ] Backend builds successfully.
- [ ] Frontend builds successfully.
- [ ] Backend tests pass in pipeline.
- [ ] Frontend tests pass in pipeline.
- [ ] Linting passes if configured.
- [ ] Formatting checks pass if configured.
- [ ] Docker build passes if configured.
- [ ] No failing pipeline jobs remain.
- [ ] No ignored failing tests remain.
- [ ] No secrets are committed.
- [ ] No local environment files are committed unless intended examples.

---

# 10. Documentation

## 10.1 Technical Documentation

- [ ] Backend API endpoints are documented.
- [ ] Request bodies are documented.
- [ ] Response bodies are documented.
- [ ] Error responses are documented.
- [ ] Authentication requirements are documented.
- [ ] Authorization rules are documented.
- [ ] Invitation expiration behavior is documented.
- [ ] Invitation single-use/reuse behavior is documented.
- [ ] Assumptions are documented.

## 10.2 User / QA Documentation

- [ ] Manual test flow is documented.
- [ ] Test users or setup steps are documented.
- [ ] Known limitations are documented.
- [ ] Screenshots are added if useful.
- [ ] QA checklist files are committed.
- [ ] MR description links to relevant QA files.

---

# 11. Merge Request Readiness

## 11.1 Code Review

- [ ] MR has a clear title.
- [ ] MR description explains what FR-02 implements.
- [ ] MR description lists backend changes.
- [ ] MR description lists frontend changes.
- [ ] MR description lists tests performed.
- [ ] MR description lists known limitations.
- [ ] MR links to this QA checklist.
- [ ] MR has no unrelated large changes.
- [ ] MR has no accidental formatting-only mass changes.
- [ ] MR has no merge conflict.
- [ ] At least one reviewer approved backend-relevant changes.
- [ ] At least one reviewer approved frontend-relevant changes if applicable.

## 11.2 Final Branch Check

- [ ] Branch is up to date with target branch.
- [ ] Branch runs locally.
- [ ] No temporary test credentials are committed.
- [ ] No console/debug logs remain.
- [ ] No broken routes remain.
- [ ] No broken API endpoints remain.
- [ ] No unresolved TODOs remain unless documented.
- [ ] No critical security issue remains.
- [ ] No critical functional issue remains.
- [ ] QA reviewer approves merge.

---

# 12. Acceptance Criteria

FR-02 can be accepted only if all of these are true:

- [ ] An authenticated user can create a WG.
- [ ] The WG creator is automatically connected to the WG.
- [ ] The WG creator receives the correct role.
- [ ] Authorized users can create secure invitations.
- [ ] Invitation links are time-limited.
- [ ] Valid invitations can be accepted.
- [ ] Invalid invitations cannot be accepted.
- [ ] Expired invitations cannot be accepted.
- [ ] Used/revoked invitations cannot be accepted if applicable.
- [ ] Invitation acceptance connects the user to the correct WG.
- [ ] Invited users receive the correct roommate-level role.
- [ ] Users cannot create invitations for other WGs.
- [ ] Users cannot use invitation flows for privilege escalation.
- [ ] Backend and frontend are integrated.
- [ ] Automated tests pass.
- [ ] Manual happy path was verified.
- [ ] Manual negative paths were verified.
- [ ] Documentation is updated.
- [ ] CI pipeline passes.
- [ ] QA reviewer approves the feature.

---

# 13. QA Notes / Evidence

## Test Environment

- Backend URL:
- Frontend URL:
- Database:
- Browser(s):
- Test users:
- Date tested:

## Commands Run

Backend:

```bash

```

Frontend:

```bash

```

E2E:

```bash

```

## Coverage Evidence

- Backend coverage tool:
- Backend line coverage:
- Backend branch coverage:
- Frontend coverage tool:
- Frontend line coverage:
- Frontend branch coverage:
- E2E/manual behavior covered:

## Manual Test Evidence

- Happy path result:
- Negative path result:
- Screenshots:
- Logs:

## Bugs Found

| Bug | Severity | Status | Link |
|-----|----------|--------|------|
|     |          |        |      |

## Known Limitations

- Limitation 1:
- Limitation 2:
- Limitation 3:

## Final QA Decision

- [ ] Passed
- [ ] Failed
- [ ] Passed with minor notes

QA reviewer:

Date:
