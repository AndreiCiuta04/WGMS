# WGMS DEV Non-Functional Requirements QA Checklist

This checklist is used for the general QA pass of the current `dev` branch of the WG Management System (WGMS). It covers the non-functional requirements that are relevant for the currently reviewed core scope.

The reviewed scope stops at **NFR-12**. The original project contract also contains NFR-13 to NFR-16 for automation, voice/ASR, and LLM command parsing. Those items are treated as **out of scope for this DEV QA pass** because the current general QA focuses on the implemented core WG-management workflow: authentication, WG membership, tasks, cleaning schedules, expenses, payments, shopping, dashboard, history, security basics, documentation, and compatibility.

## Checklist basis used

The checklist uses the following verification bases:

- **Project contract NFRs:** NFR-01 to NFR-12 from the WGMS project contract.
- **OWASP ASVS Level 1:** used for baseline web application security verification, especially authentication, session/token handling, authorization, access control, input validation, and safe error handling.
- **OWASP Web Security Testing Guide:** used as practical guidance for authentication testing, authorization testing, session/token testing, input validation testing, and business-logic abuse testing.
- **NIST SP 800-63B:** used only as supporting guidance for password and authenticator handling, especially that passwords must not be stored in plain text and must be protected with suitable salted password hashing.
- **GDPR Article 5 principles:** used for privacy-oriented checks: purpose limitation, data minimization, transparency, storage limitation, integrity/confidentiality, and accountability.
- **WCAG 2.2 smoke-check principles:** used for basic usability and accessibility checks: perceivable, operable, understandable, and robust UI behavior.
- **Cypress testing best practices:** used for reliable e2e test organization, state control, stable selectors, and CI-suitable automated browser tests.

## Evidence rules

A checkbox may only be marked as done if there is evidence. Acceptable evidence includes:

- automated backend test path and passing result;
- automated frontend test path and passing result;
- automated e2e test path and passing result;
- code review note with file paths;
- manual test note with tester name/date/environment;
- screenshot or terminal output reference;
- README or documentation path;
- explicit limitation note when the requirement is not fully implemented.

If there is no proof, write **Missing evidence** instead of marking the item as done.

## Summary table

| NFR | Category | Expected evidence type | Status | Evidence / notes |
|---|---|---|---|---|
| NFR-01 | Security / Authentication | Backend tests, e2e tests, OWASP ASVS Level 1 checklist review, code review | ☐ Not checked |  |
| NFR-02 | Authorization / RBAC | Backend tests, negative tests, e2e tests, code review | ☐ Not checked |  |
| NFR-03 | Data Privacy | Data inventory review, DTO review, log review | ☐ Not checked |  |
| NFR-04 | GDPR Support | Manual compliance tests, documentation review, feature checks | ☐ Not checked |  |
| NFR-05 | Performance | Browser DevTools measurement, seeded dataset, evidence log | ☐ Not checked |  |
| NFR-06 | Reliability | Integration tests, initialization tests, seed/demo restore evidence | ☐ Not checked |  |
| NFR-07 | Financial Accuracy | Unit tests, integration tests, rounding/settlement edge cases | ☐ Not checked |  |
| NFR-08 | Usability | Peer tester walkthrough, accessibility smoke check, frontend/e2e checks | ☐ Not checked |  |
| NFR-09 | Maintainability | Architecture/code review, module separation review, documentation review | ☐ Not checked |  |
| NFR-10 | Testability | CI result, test inventory, critical-service test coverage review | ☐ Not checked |  |
| NFR-11 | Auditability | Database inspection, backend tests, functional tests | ☐ Not checked |  |
| NFR-12 | Compatibility | Responsive tests, browser tests, QR fallback check where relevant | ☐ Not checked |  |

---

# NFR-01 — Security / Authentication

## Requirement

Users must authenticate before accessing WG-specific data. Passwords must never be stored in plain text and must be protected using secure hashing.

## Checklist basis

- Project contract NFR-01.
- OWASP ASVS **Level 1** baseline verification for authentication and session/token handling.
- OWASP Web Security Testing Guide authentication/session testing areas.
- NIST SP 800-63B password/authenticator handling guidance.

## Checklist

### Authentication behavior

- [ ] Unauthenticated users cannot access WG-specific backend endpoints.
- [ ] Unauthenticated users are redirected away from protected frontend routes.
- [ ] Login with valid credentials succeeds.
- [ ] Login with invalid password fails safely.
- [ ] Login with unknown email fails safely.
- [ ] Failed login messages do not reveal whether email or password was wrong.
- [ ] Logout clears the client-side authentication state.
- [ ] Protected requests after logout fail.

### Password handling

- [ ] Passwords are not stored in plain text.
- [ ] Passwords are stored as BCrypt or equivalent secure password hashes.
- [ ] Password hashes are not returned through user-facing DTOs.
- [ ] Passwords are not printed in logs.
- [ ] Password reset or password-change behavior is safe if implemented.

### Token/session handling

- [ ] JWT or authentication token is required for protected API calls.
- [ ] Token expiration is configured or documented.
- [ ] Invalid tokens are rejected.
- [ ] Tampered tokens are rejected.
- [ ] Expired tokens are rejected if expiration is implemented.
- [ ] Tokens are not stored in committed configuration files.
- [ ] Tokens are not printed in logs.

### Evidence

- Backend test path(s):
- Frontend/e2e test path(s):
- Code review file path(s):
- Manual evidence:
- Result:

---

# NFR-02 — Authorization / RBAC

## Requirement

Access to sensitive functions must depend on the user’s role. Main Tenant or WG administrator functionality must be protected on the backend, not only hidden in the frontend.

## Checklist basis

- Project contract NFR-02.
- OWASP ASVS Level 1 access-control verification.
- OWASP WSTG authorization and business-logic testing.

## Checklist

### Role checks

- [ ] Main Tenant-only actions are protected by backend authorization checks.
- [ ] Roommate users cannot manage invitations if this is Main Tenant-only.
- [ ] Roommate users cannot remove members if this is Main Tenant-only.
- [ ] Roommate users cannot change important WG settings if this is Main Tenant-only.
- [ ] Frontend hiding is not treated as sufficient security.

### Household isolation

- [ ] User from WG A cannot access WG B resources by changing route parameters.
- [ ] User from WG A cannot access WG B resources by changing API path IDs.
- [ ] User from WG A cannot access WG B resources by changing request-body IDs.
- [ ] User from WG A cannot update/delete WG B tasks, expenses, payments, shopping items, dashboard data, or history entries.
- [ ] Query/list endpoints return only data belonging to the authenticated user’s WG unless a feature explicitly allows public data.

### JWT/token misuse checks

- [ ] A valid JWT from User A cannot be used to access resources that belong only to User B.
- [ ] A valid JWT from a Roommate cannot be used to perform Main Tenant-only actions.
- [ ] A valid JWT from a user in another WG cannot be used to access the current WG’s data.
- [ ] Backend authorization derives the acting user from the authenticated token, not from user IDs supplied by the frontend.

### Negative tests

- [ ] Missing token returns 401/403 as appropriate.
- [ ] Wrong role returns 403 as appropriate.
- [ ] Wrong household returns 403/404 as appropriate without leaking private data.
- [ ] ID tampering is covered by automated tests.

### Evidence

- Backend test path(s):
- E2E test path(s):
- Code review file path(s):
- Manual evidence:
- Result:

---

# NFR-03 — Data Privacy

## Requirement

The system should only collect data required for WG management purposes, such as account data, task assignments, expenses, messages/notifications where implemented, shopping data, and membership data.

## Checklist basis

- Project contract NFR-03.
- GDPR Article 5 principles: purpose limitation and data minimization.
- OWASP ASVS Level 1 data protection and secure design baseline.

## Checklist

### Data inventory

- [ ] Account data is documented.
- [ ] WG membership data is documented.
- [ ] Task and cleaning schedule data is documented.
- [ ] Expense and payment data is documented.
- [ ] Shopping data is documented.
- [ ] Dashboard/history data is documented as derived or stored behavior.
- [ ] Notification/message data is documented if implemented.
- [ ] Technical log data is documented.

### Data minimization

- [ ] Registration collects only required account fields.
- [ ] DTOs expose only fields needed by the frontend.
- [ ] Password hashes are never exposed to the frontend.
- [ ] JWTs/tokens are not exposed in unnecessary responses.
- [ ] Private WG data is not returned in public or cross-WG contexts.
- [ ] Deleted/anonymized users are handled without exposing unnecessary identity data.

### Logging/privacy

- [ ] Logs do not contain passwords.
- [ ] Logs do not contain JWTs or authentication tokens.
- [ ] Logs do not contain full private messages if messages are implemented.
- [ ] Logs do not contain full financial details unless strictly necessary for debugging and safe in the project context.
- [ ] Error responses do not reveal unnecessary internal details.

### Evidence

- Data inventory path:
- DTO/code review path(s):
- Log inspection evidence:
- Manual evidence:
- Result:

---

# NFR-04 — GDPR Support

## Requirement

Users must be able to request access to stored data, correction of incorrect data, and deletion or anonymization of account data where technically and legally possible.

## Checklist basis

- Project contract NFR-04.
- GDPR Article 5 principles and basic user-rights-oriented product checks.

## Checklist

### Access / transparency

- [ ] User can view their own profile/account information.
- [ ] User can view their WG membership.
- [ ] User can view assigned tasks and task history relevant to them.
- [ ] User can view expenses/payments relevant to them.
- [ ] User can view shopping items/requests relevant to them.
- [ ] Documentation or app text explains what categories of data are stored.

### Correction

- [ ] User can correct profile data where implemented.
- [ ] Authorized users can correct household/task/shopping/expense records where appropriate.
- [ ] Invalid correction attempts are rejected.
- [ ] Corrections preserve audit/history consistency where relevant.

### Deletion/anonymization

- [ ] User can leave a WG if this is implemented.
- [ ] Account deletion or anonymization exists, or the limitation is documented.
- [ ] Historical financial/task records remain understandable after user deletion/anonymization.
- [ ] Deleted/anonymized users cannot still authenticate.

### Export

- [ ] User data export exists, or manual export/demo limitation is documented.
- [ ] Export does not include password hashes or other users’ private data unnecessarily.

### Evidence

- Feature path(s):
- Documentation path(s):
- Manual test evidence:
- Known limitations:
- Result:

---

# NFR-05 — Performance

## Requirement

Standard dashboard pages should load within 2 seconds under normal project demo conditions with up to 8 roommates, 100 tasks, 100 expenses, and 100 shopping items. Large lists should support pagination or lazy loading where necessary.

## Checklist basis

- Project contract NFR-05.
- Practical browser performance smoke testing.

## Measurement procedure

Use a reproducible browser measurement:

1. Start the backend and frontend in the normal DEV/demo setup.
2. Load or create the seeded demo dataset:
   - up to 8 roommates;
   - 100 tasks;
   - 100 expenses;
   - 100 shopping items.
3. Open Chrome or Chromium DevTools.
4. Go to the **Network** tab.
5. Enable **Disable cache**.
6. Set throttling to **Fast 4G**.
7. Open the dashboard route from a fresh page load.
8. Record:
   - DOMContentLoaded time;
   - Load time;
   - largest/slowest API request if relevant;
   - visible time until the dashboard becomes usable if manually observed.
9. Repeat at least 3 times and record the average or the worst value.
10. Write the result in the evidence field.

## Checklist

### Dashboard performance

- [ ] Seed/demo dataset exists or was manually created.
- [ ] Dashboard load time was measured using the procedure above.
- [ ] DOMContentLoaded time was recorded.
- [ ] Load time was recorded.
- [ ] Result is within 2 seconds under normal demo conditions, or the limitation is documented.
- [ ] Slow API calls are identified if the target is missed.

### List performance

- [ ] Task list remains usable with 100 tasks.
- [ ] Expense list remains usable with 100 expenses.
- [ ] Shopping list remains usable with 100 shopping items.
- [ ] Dashboard does not load unnecessary full historical data if only summary data is needed.
- [ ] Pagination, lazy loading, filtering, or reasonable list limits exist where the UI can grow large.

### Evidence

- Dataset/seed reference:
- Browser/device:
- Throttling profile:
- Run 1 DOMContentLoaded / Load:
- Run 2 DOMContentLoaded / Load:
- Run 3 DOMContentLoaded / Load:
- Average/worst result:
- Notes:
- Result:

---

# NFR-06 — Reliability

## Requirement

Core data such as expenses, payments, tasks, WG membership, and history/audit-relevant records should not be lost during normal demo usage. Database initialization and seed/demo data should be documented and tested.

## Checklist basis

- Project contract NFR-06.
- Integration testing and demo restore testing.

## Checklist

### Persistence during normal demo usage

- [ ] Created WG membership data persists across normal page reloads.
- [ ] Created tasks persist across normal page reloads.
- [ ] Created cleaning schedules persist across normal page reloads.
- [ ] Created expenses persist across normal page reloads.
- [ ] Created payments/payment statuses persist across normal page reloads.
- [ ] Created shopping items/requests persist across normal page reloads.
- [ ] History/dashboard records remain consistent after related data changes.

### Initialization and seed data

- [ ] Database initialization is documented.
- [ ] Seed/demo data setup is documented.
- [ ] Demo data can be recreated reliably.
- [ ] Integration tests verify persistence for critical entities.
- [ ] Known H2/database persistence limitations are documented if applicable.

### Error handling / consistency

- [ ] Failed create/update/delete operations do not leave partial invalid state.
- [ ] Backend transactions are used for critical multi-step operations where needed.
- [ ] Failed expense/payment operations do not corrupt balances.
- [ ] Failed task/schedule operations do not corrupt assignment state.

### Evidence

- Integration test path(s):
- Seed/demo documentation path:
- Manual restore/recreate evidence:
- Known limitations:
- Result:

---

# NFR-07 — Financial Accuracy

## Requirement

Expense splitting and settlement calculations should use precise decimal handling. Rounding must be deterministic and visible to the user.

## Checklist basis

- Project contract NFR-07.
- Financial/business-logic unit testing.

## Checklist

### Decimal handling

- [ ] Financial amounts use decimal-safe types on the backend, such as `BigDecimal`.
- [ ] Floating-point types are not used for final money calculations.
- [ ] Rounding mode is deterministic.
- [ ] Rounding to two decimals is consistent.
- [ ] UI displays rounded values consistently with backend values.

### Positive financial tests

- [ ] Equal split test exists.
- [ ] Unequal split test exists if unequal split is implemented.
- [ ] Multiple participants test exists.
- [ ] Payment status update test exists.
- [ ] Settlement/balance overview test exists.

### Negative/edge financial tests

- [ ] Negative amount is rejected unless explicitly allowed.
- [ ] Zero amount is rejected unless explicitly allowed.
- [ ] Invalid currency/amount format is rejected if applicable.
- [ ] Deleted/anonymized user fallback is tested if relevant.
- [ ] Partially settled payment edge case is tested if relevant.
- [ ] Rounding edge case is tested.

### Evidence

- Unit test path(s):
- Integration test path(s):
- Code review path(s):
- Manual evidence:
- Result:

---

# NFR-08 — Usability

## Requirement

Main workflows such as adding an expense, checking assigned tasks, viewing open payments, and using dashboard/history should be completable without external instructions by a peer tester.

## Checklist basis

- Project contract NFR-08.
- WCAG 2.2 smoke-check principles.
- Practical peer usability testing.

## Peer tester definition

A peer tester should be someone who:

- is not the original implementer of the tested feature;
- understands the basic WGMS idea but has not been coached through the exact UI flow immediately before testing;
- uses only the app UI and normal README/setup instructions;
- records whether they completed the task without verbal help.

## Checklist

### Main workflow usability

- [ ] Peer tester can register/login without external help.
- [ ] Peer tester can join or create a WG without external help where relevant.
- [ ] Peer tester can check assigned tasks without external help.
- [ ] Peer tester can add or complete a task where allowed.
- [ ] Peer tester can add an expense without external help.
- [ ] Peer tester can view open payments/payment status without external help.
- [ ] Peer tester can use shopping list/request views without external help.
- [ ] Peer tester can understand dashboard/history information without external help.

### UI feedback

- [ ] Forms show clear validation messages.
- [ ] Successful actions show clear success state or visible result.
- [ ] Failed actions show clear error messages.
- [ ] Loading states are visible where API calls may take noticeable time.
- [ ] Empty states explain what the user can do next.

### Accessibility smoke checks

- [ ] Important form fields have labels.
- [ ] Buttons have meaningful text.
- [ ] Keyboard navigation works for main forms.
- [ ] Focus states are visible enough to navigate.
- [ ] Main text is readable on desktop and mobile.
- [ ] No obvious contrast issue blocks normal use.

### Evidence

- Peer tester name/date:
- Tested browser/device:
- Completed workflows:
- Problems found:
- Screenshots/notes:
- Result:

---

# NFR-09 — Maintainability

## Requirement

The codebase should follow a clear separation between frontend, backend, database access, and business logic. Important modules must be documented sufficiently for handover.

## Checklist basis

- Project contract NFR-09.
- Architecture/code review.

## Checklist

### Backend maintainability

- [ ] Controllers do not contain heavy business logic.
- [ ] Service layer contains business rules.
- [ ] Repository layer handles database access.
- [ ] DTO mapping is separated from persistence entities where applicable.
- [ ] Validation is centralized or consistently applied.
- [ ] Critical services are readable and reasonably small.

### Frontend maintainability

- [ ] Components do not contain unnecessary backend/business logic.
- [ ] API access is placed in services.
- [ ] DTO/model types are defined clearly.
- [ ] Repeated UI logic is not copied excessively.
- [ ] Error/loading state handling is consistent.

### Documentation / handover

- [ ] README explains how to run backend.
- [ ] README explains how to run frontend.
- [ ] README explains how to run tests.
- [ ] Architecture overview exists or is updated.
- [ ] Important modules/features are documented enough for another team member to continue.
- [ ] Known limitations are documented.

### Evidence

- Code review path(s):
- Documentation path(s):
- Known maintainability issues:
- Result:

---

# NFR-10 — Testability

## Requirement

Critical backend services, especially authentication, task assignment, expense tracking, settlement logic, shopping flows, dashboard/history behavior, and invitation handling, should be covered by automated tests.

## Checklist basis

- Project contract NFR-10.
- CI/CD test evidence.
- Cypress best practices for e2e test reliability.

## Checklist

### Backend automated tests

- [ ] Authentication tests exist.
- [ ] Authorization/RBAC tests exist.
- [ ] Invitation/WG creation tests exist.
- [ ] Task assignment tests exist.
- [ ] Cleaning schedule tests exist.
- [ ] Expense tracking tests exist.
- [ ] Payment status/settlement tests exist.
- [ ] Shopping list/request/aggregation tests exist.
- [ ] Dashboard/history tests exist.
- [ ] Negative backend tests exist for critical workflows.

### Frontend automated tests

- [ ] Critical component tests exist where appropriate.
- [ ] Frontend service tests exist where appropriate.
- [ ] Form validation tests exist where appropriate.
- [ ] Error/loading/empty state tests exist where appropriate.

### E2E automated tests

- [ ] Registration/login e2e test exists.
- [ ] WG creation/invitation e2e test exists.
- [ ] Task/cleaning workflow e2e test exists.
- [ ] Expense/payment workflow e2e test exists.
- [ ] Shopping workflow e2e test exists.
- [ ] Dashboard/history e2e test exists.
- [ ] Negative e2e tests exist for forbidden access and invalid input.
- [ ] Tests use stable selectors or reliable query strategy.
- [ ] Tests do not depend on state created by unrelated previous tests.

### CI evidence

- [ ] Backend tests run in CI or local evidence is documented.
- [ ] Frontend tests run in CI or local evidence is documented.
- [ ] E2E tests run in CI or local evidence is documented.
- [ ] Failing tests block or are treated as merge blockers.

### Evidence

- Backend test command/result:
- Frontend test command/result:
- E2E test command/result:
- CI pipeline link/result:
- Missing tests:
- Result:

---

# NFR-11 — Auditability

## Requirement

Important actions such as creating expenses, settling payments, assigning tasks, changing WG membership, and updating important household records should be traceable through timestamps and responsible users where appropriate.

## Checklist basis

- Project contract NFR-11.
- Database inspection and functional tests.

## Checklist

### Traceable actions

- [ ] Expense creation stores creator/responsible user where appropriate.
- [ ] Expense creation stores timestamp where appropriate.
- [ ] Payment/settlement status changes store timestamp where appropriate.
- [ ] Task assignment stores responsible user/assignee where appropriate.
- [ ] Task completion stores completion state and timestamp where appropriate.
- [ ] WG membership changes store join/leave timestamps where appropriate.
- [ ] Shopping item/request creation stores creator and timestamp where appropriate.
- [ ] History view shows important completed/changed actions correctly.

### Data consistency

- [ ] History entries correspond to real actions.
- [ ] Dashboard numbers match underlying data.
- [ ] Payment status overview matches expense/payment records.
- [ ] Deleted/anonymized users do not break historical traceability.

### Evidence

- Database/entity review path(s):
- Backend test path(s):
- E2E/manual test path(s):
- Notes:
- Result:

---

# NFR-12 — Compatibility

## Requirement

The system is optimized for the defined reference platforms and should remain usable on mobile and desktop reference widths. NFC-based features are optional and are not required for core workflow completion.

## Checklist basis

- Project contract NFR-12.
- Reference-device/responsive layout tests.
- WCAG 2.2 smoke-check principles for robust UI behavior.

## Checklist

### Reference viewports

- [ ] UI works at 390 × 844 CSS px or comparable iPhone 12 Pro viewport.
- [ ] UI works at 412 × 915 CSS px or comparable Pixel 7 viewport.
- [ ] UI works at minimum 360 CSS px width.
- [ ] UI works at 1366 × 768 px desktop resolution or higher.
- [ ] Main navigation remains usable on mobile.
- [ ] Main forms remain usable on mobile.
- [ ] Tables/lists do not become unreadable or unusable on mobile.

### Reference browsers

- [ ] Chrome/Chromium desktop smoke test passed.
- [ ] Firefox desktop smoke test passed, if available.
- [ ] Mobile Chrome responsive/device smoke test passed.
- [ ] Safari/iOS behavior is checked manually or limitation is documented.

### QR/NFC fallback compatibility

- [ ] Core workflows do not depend on NFC.
- [ ] If appliance/NFC routes are implemented, a QR-code or normal link fallback exists or the limitation is documented.
- [ ] QR fallback opens the same intended route/view as the NFC/deep-link route where implemented.
- [ ] Users without NFC-capable devices can still complete the core WG workflow.

### Evidence

- Desktop browser tested:
- Mobile viewport/browser tested:
- Screenshots or notes:
- QR fallback evidence, if relevant:
- Known compatibility limitations:
- Result:

---

# Out-of-scope note — Original NFR-13 to NFR-16

The original project contract contains additional NFRs for automation safety, voice data privacy, LLM data minimization, and LLM output control. They are not included as active checklist rows in this DEV QA file because ASR/voice/LLM automation is outside the current general QA scope.

If ASR, voice input, or LLM command parsing is later implemented, create a separate dedicated QA checklist for those features instead of mixing them into the core NFR checklist.
