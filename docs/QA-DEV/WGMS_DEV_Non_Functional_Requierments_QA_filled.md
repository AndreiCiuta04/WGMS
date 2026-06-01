# WGMS DEV Non-Functional Requirements QA Checklist — Filled Version

This file fills the actual checklist boxes from the provided NFR QA template. It is intentionally honest: checked boxes mean we have evidence; unchecked boxes mean missing evidence, not implemented, or only a limitation note.

## Summary table

| NFR | Category | Status | Evidence / notes |
|---|---|---|---|
| NFR-01 | Security / Authentication | ☑ Fulfilled for DEV scope | Backend auth/security tests, frontend guard/service tests, auth E2E, expired JWT coverage, all tests green. Demo passwords remain intentional DEV/demo data. |
| NFR-02 | Authorization / RBAC | ☑ Fulfilled for DEV scope | Backend negative tests and household/role isolation evidence. Backend derives acting user from token, not frontend-supplied user IDs. |
| NFR-03 | Data Privacy | ☑ Mostly fulfilled | DTO/error/log privacy review and hardening done. Raw internal errors and duplicate-email disclosure generalized. Full production privacy audit not performed. |
| NFR-04 | GDPR Support | ◐ Partial / accepted | Domain records can be viewed/updated/deleted. No full account deletion, export, profile update, or anonymization workflow found. |
| NFR-05 | Performance | ◐ Partial | Local build/test/API smoke timings are good. Exact browser DevTools seeded-dataset measurement was skipped. |
| NFR-06 | Reliability | ☑ Mostly fulfilled | Transactional services, no-partial-state backend tests, reload/persistence E2E, frontend error handling. Seed/demo restore docs limited. |
| NFR-07 | Financial Accuracy | ☑ Fulfilled for implemented expense logic | BigDecimal, deterministic rounding, equal split rounding test, invalid split/amount tests, balance tests. Payment/partial settlement evidence limited. |
| NFR-08 | Usability | ◐ Mostly fulfilled | Labels, validation, empty/success/error states, responsive tests, UC E2E. Formal named peer tester evidence missing. |
| NFR-09 | Maintainability | ☑ Fulfilled for DEV scope | Clear backend/frontend layering, QA docs, build configs, no unresolved production TODO/FIXME/HACK in searched paths. |
| NFR-10 | Testability | ☑ Fulfilled locally | Backend, frontend, feature E2E, use-case E2E, runner script, coverage overview, all local tests green. CI link not provided. |
| NFR-11 | Auditability | ☑ Mostly fulfilled | Timestamps, creator/payer/assignee/status fields, history endpoint/tests. No immutable append-only audit log. |
| NFR-12 | Compatibility | ◐ Partial / accepted | Responsive SCSS and Cypress viewport/no-overflow tests. Full Firefox/Safari/manual browser matrix and QR fallback evidence missing. |

---

# NFR-01 — Security / Authentication

## Checklist

### Authentication behavior
- [x] Unauthenticated users cannot access WG-specific backend endpoints.
- [x] Unauthenticated users are redirected away from protected frontend routes.
- [x] Login with valid credentials succeeds.
- [x] Login with invalid password fails safely.
- [x] Login with unknown email fails safely.
- [x] Failed login messages do not reveal whether email or password was wrong.
- [x] Logout clears the client-side authentication state.
- [x] Protected requests after logout fail.

### Password handling
- [x] Passwords are not stored in plain text.
- [x] Passwords are stored as BCrypt or equivalent secure password hashes.
- [x] Password hashes are not returned through user-facing DTOs.
- [x] Passwords are not printed in logs in reviewed production/user-facing flows.
- [ ] Password reset or password-change behavior is safe if implemented.  
  **Missing / not applicable:** password reset/change was not part of the reviewed implemented scope.

### Token/session handling
- [x] JWT or authentication token is required for protected API calls.
- [x] Token expiration is configured or documented.
- [x] Invalid tokens are rejected.
- [x] Tampered tokens are rejected.
- [x] Expired tokens are rejected if expiration is implemented.
- [x] Tokens are not stored in committed configuration files.
- [x] Tokens are not printed in logs in searched production code paths.

### Evidence
- Backend: `LoginEndpointTest`, `SecurityTest`, `UserEndpointTest`, protected endpoint tests.
- Frontend/E2E: `auth.guard.spec.ts`, `auth.service.spec.ts`, `e2e/cypress/e2e/fr01-auth.cy.js`.
- Manual/test evidence: backend, frontend, and E2E tests were reported green after final fixes.
- Result: **fulfilled for DEV scope**, except password reset/change not reviewed.

---

# NFR-02 — Authorization / RBAC

## Checklist

### Role checks
- [x] Main Tenant-only actions are protected by backend authorization checks.
- [x] Roommate users cannot manage invitations if this is Main Tenant-only.
- [x] Roommate users cannot remove members if this is Main Tenant-only.
- [x] Roommate users cannot change important WG settings if this is Main Tenant-only.
- [x] Frontend hiding is not treated as sufficient security.

### Household isolation
- [x] User from WG A cannot access WG B resources by changing route parameters.
- [x] User from WG A cannot access WG B resources by changing API path IDs.
- [x] User from WG A cannot access WG B resources by changing request-body IDs.
- [x] User from WG A cannot update/delete WG B tasks, expenses, payments, shopping items, dashboard data, or history entries.
- [x] Query/list endpoints return only data belonging to the authenticated user’s WG unless a feature explicitly allows public data.

### JWT/token misuse checks
- [x] A valid JWT from User A cannot be used to access resources that belong only to User B.
- [x] A valid JWT from a Roommate cannot be used to perform Main Tenant-only actions.
- [x] A valid JWT from a user in another WG cannot be used to access the current WG’s data.
- [x] Backend authorization derives the acting user from the authenticated token, not from user IDs supplied by the frontend.

### Negative tests
- [x] Missing token returns 401/403 as appropriate.
- [x] Wrong role returns 403 as appropriate.
- [x] Wrong household returns 403/404 as appropriate without leaking private data.
- [x] ID tampering is covered by automated tests.

### Evidence
- Backend: `InvitationEndpointTest`, `HouseholdEndpointTest`, `TaskEndpointTest`, `CleaningScheduleEndpointTest`, `ExpenseEndpointTest`, `ShoppingItemEndpointTest`, `DashboardEndpointTest`, `HistoryEndpointTest`.
- E2E: household, task, expense, shopping, dashboard/history, and use-case specs.
- Result: **fulfilled for DEV scope**.

---

# NFR-03 — Data Privacy

## Checklist

### Data inventory
- [x] Account data is documented/reviewed through user DTO/entity evidence.
- [x] WG membership data is documented/reviewed through household/member DTO/entity evidence.
- [x] Task and cleaning schedule data is documented/reviewed.
- [x] Expense and payment data is documented/reviewed.
- [x] Shopping data is documented/reviewed.
- [x] Dashboard/history data is documented/reviewed as derived/stored behavior.
- [x] Notification/message data is documented if implemented.  
  **Not applicable:** no separate notification/message feature was found in reviewed core scope.
- [x] Technical log data is reviewed through grep/privacy checks.

### Data minimization
- [x] Registration collects only required account fields.
- [x] DTOs expose only fields needed by the frontend.
- [x] Password hashes are never exposed to the frontend.
- [x] JWTs/tokens are not exposed in unnecessary responses.
- [x] Private WG data is not returned in public or cross-WG contexts.
- [ ] Deleted/anonymized users are handled without exposing unnecessary identity data.  
  **Missing evidence:** no full deletion/anonymization workflow found.

### Logging/privacy
- [x] Logs do not contain passwords in reviewed production/user-facing flows.
- [x] Logs do not contain JWTs or authentication tokens in reviewed production/user-facing flows.
- [x] Logs do not contain full private messages if messages are implemented.  
  **Not applicable:** message feature not found.
- [x] Logs do not contain full financial details unless strictly necessary for debugging and safe in the project context.
- [x] Error responses do not reveal unnecessary internal details.

### Evidence
- Code reviewed: backend entities/DTOs, frontend DTOs, login DTO, error handling.
- Privacy hardening: duplicate email and internal backend error disclosure generalized; password removed from login DTO equality/hash exposure.
- Result: **mostly fulfilled**, with full account anonymization/deletion missing.

---

# NFR-04 — GDPR Support

## Checklist

### Access / transparency
- [ ] User can view their own profile/account information.  
  **Missing evidence:** no complete profile/account detail page or endpoint found.
- [x] User can view their WG membership.
- [x] User can view assigned tasks and task history relevant to them.
- [x] User can view expenses/payments relevant to them.
- [x] User can view shopping items/requests relevant to them.
- [ ] Documentation or app text explains what categories of data are stored.  
  **Missing evidence:** no explicit privacy/data inventory page found.

### Correction
- [ ] User can correct profile data where implemented.  
  **Missing evidence:** no user profile update endpoint/page found.
- [x] Authorized users can correct household/task/shopping/expense records where appropriate.
- [x] Invalid correction attempts are rejected.
- [x] Corrections preserve audit/history consistency where relevant for implemented domain data.

### Deletion/anonymization
- [ ] User can leave a WG if this is implemented.  
  **Missing evidence:** not confirmed.
- [ ] Account deletion or anonymization exists, or the limitation is documented.  
  **Limitation documented:** no account deletion/anonymization endpoint found.
- [ ] Historical financial/task records remain understandable after user deletion/anonymization.  
  **Missing evidence:** no deletion/anonymization workflow exists.
- [ ] Deleted/anonymized users cannot still authenticate.  
  **Missing evidence:** no deletion/anonymization workflow exists.

### Export
- [ ] User data export exists, or manual export/demo limitation is documented.  
  **Limitation documented:** no export endpoint found.
- [ ] Export does not include password hashes or other users’ private data unnecessarily.  
  **Not applicable:** no export feature exists.

### Evidence
- Domain CRUD exists for tasks, schedules, expenses, shopping items, invitations, dashboard/history.
- Known limitations: no profile update, account deletion, export, anonymization, or explicit GDPR page.
- Result: **partially fulfilled / accepted for project scope**.

---

# NFR-05 — Performance

## Checklist

### Dashboard performance
- [ ] Seed/demo dataset exists or was manually created.  
  **Missing exact evidence:** no proof of exactly 8 roommates, 100 tasks, 100 expenses, 100 shopping items.
- [ ] Dashboard load time was measured using the browser DevTools procedure.  
  **Skipped.**
- [ ] DOMContentLoaded time was recorded.  
  **Skipped.**
- [ ] Load time was recorded.  
  **Skipped.**
- [ ] Result is within 2 seconds under normal demo conditions, or the limitation is documented.  
  **Limitation documented:** no DevTools seeded-dataset timing.
- [x] Slow API calls are identified if the target is missed.  
  **Alternative evidence:** authenticated local API smoke found no slow endpoint; all tested endpoints were below 0.01s locally.

### List performance
- [ ] Task list remains usable with 100 tasks.  
  **Missing exact 100-item UI evidence.**
- [ ] Expense list remains usable with 100 expenses.  
  **Missing exact 100-item UI evidence.**
- [ ] Shopping list remains usable with 100 shopping items.  
  **Missing exact 100-item UI evidence.**
- [x] Dashboard does not load unnecessary full historical data if only summary data is needed.  
  **Evidence:** dedicated dashboard endpoint/service exists.
- [ ] Pagination, lazy loading, filtering, or reasonable list limits exist where the UI can grow large.  
  **Partial/missing evidence:** not proven for every large list.

### Evidence
- Frontend build: approximately `2.563s`, initial bundle approximately `3.33 MB`.
- Backend tests: `182` tests, `0` failures/errors/skips, approximately `16.130s`.
- Authenticated local API smoke:
  - `/api/v1/dashboard`: `0.004874s`
  - `/api/v1/tasks`: `0.004184s`
  - `/api/v1/tasks/my`: `0.002774s`
  - `/api/v1/expenses`: `0.007131s`
  - `/api/v1/expenses/balances`: `0.002560s`
  - `/api/v1/shopping-items`: `0.002282s`
  - `/api/v1/shopping-items/aggregated`: `0.002697s`
  - `/api/v1/shopping-items/history`: `0.002904s`
  - `/api/v1/history`: `0.003466s`
- Result: **partially fulfilled.** Local smoke performance is good, but exact browser/seeded-dataset procedure missing.

---

# NFR-06 — Reliability

## Checklist

### Persistence during normal demo usage
- [x] Created WG membership data persists across normal page reloads.
- [x] Created tasks persist across normal page reloads.
- [x] Created cleaning schedules persist across normal page reloads.
- [x] Created expenses persist across normal page reloads.
- [ ] Created payments/payment statuses persist across normal page reloads.  
  **Partial/missing evidence:** payment/settlement functionality is weaker than expense/balance evidence.
- [x] Created shopping items/requests persist across normal page reloads.
- [x] History/dashboard records remain consistent after related data changes.

### Initialization and seed data
- [ ] Database initialization is documented.  
  **Partial/missing evidence:** config exists, but polished initialization guide not confirmed.
- [ ] Seed/demo data setup is documented.  
  **Partial/missing evidence:** datagen/demo data exists, formal restore docs not confirmed.
- [ ] Demo data can be recreated reliably.  
  **Missing formal evidence:** tests run, but explicit demo restore proof not recorded.
- [x] Integration tests verify persistence for critical entities.
- [x] Known H2/database persistence limitations are documented if applicable.  
  **Documented here as limitation.**

### Error handling / consistency
- [x] Failed create/update/delete operations do not leave partial invalid state.
- [x] Backend transactions are used for critical multi-step operations where needed.
- [x] Failed expense/payment operations do not corrupt balances.
- [x] Failed task/schedule operations do not corrupt assignment state.

### Evidence
- Backend: `CleaningScheduleEndpointTest`, `ExpenseEndpointTest`, `TaskEndpointTest`, `ShoppingItemEndpointTest`, `DashboardEndpointTest`, `HistoryEndpointTest`.
- E2E: expense reload/persistence and history use-case specs.
- Result: **mostly fulfilled**, with seed/demo restore docs and payment persistence weaker.

---

# NFR-07 — Financial Accuracy

## Checklist

### Decimal handling
- [x] Financial amounts use decimal-safe types on the backend, such as `BigDecimal`.
- [x] Floating-point types are not used for final money calculations.
- [x] Rounding mode is deterministic.
- [x] Rounding to two decimals is consistent.
- [x] UI displays rounded values consistently with backend values.

### Positive financial tests
- [x] Equal split test exists.
- [x] Unequal split test exists if unequal split is implemented.
- [x] Multiple participants test exists.
- [ ] Payment status update test exists.  
  **Missing/limited evidence:** payment status coverage not as clear as expense/balance logic.
- [x] Settlement/balance overview test exists.

### Negative/edge financial tests
- [x] Negative amount is rejected unless explicitly allowed.
- [x] Zero amount is rejected unless explicitly allowed.
- [x] Invalid currency/amount format is rejected if applicable.
- [ ] Deleted/anonymized user fallback is tested if relevant.  
  **Missing evidence:** no deletion/anonymization workflow.
- [ ] Partially settled payment edge case is tested if relevant.  
  **Missing/limited evidence:** partial settlement not clearly implemented/tested.
- [x] Rounding edge case is tested.

### Evidence
- Backend/code: `ExpenseServiceImpl`, expense/balance/history DTOs/services.
- Backend tests: `ExpenseEndpointTest`, dashboard/history financial tests.
- Specific evidence: `10.00 / 3` equal split rounding to `3.34`, `3.33`, `3.33`, total `10.00`.
- Result: **fulfilled for implemented expense/balance logic**, payment/partial settlement and anonymized-user cases limited.

---

# NFR-08 — Usability

## Checklist

### Main workflow usability
- [x] Peer tester can register/login without external help.  
  **Evidence:** E2E flow; no named peer tester recorded.
- [x] Peer tester can join or create a WG without external help.  
  **Evidence:** E2E flow; no named peer tester recorded.
- [x] Peer tester can check assigned tasks without external help.
- [x] Peer tester can add or complete a task where allowed.
- [x] Peer tester can add an expense without external help.
- [ ] Peer tester can view open payments/payment status without external help.  
  **Missing/limited evidence:** payment-specific UX evidence weak.
- [x] Peer tester can use shopping list/request views without external help.
- [x] Peer tester can understand dashboard/history information without external help.

### UI feedback
- [x] Forms show clear validation messages.
- [x] Successful actions show clear success state or visible result.
- [x] Failed actions show clear error messages.
- [x] Loading states are visible where API calls may take noticeable time.
- [x] Empty states explain what the user can do next.

### Accessibility smoke checks
- [x] Important form fields have labels.
- [x] Buttons have meaningful text.
- [x] Keyboard navigation works for main forms.  
  **Evidence limited:** smoke/code review, not full WCAG audit.
- [x] Focus states are visible enough to navigate.  
  **Evidence limited:** smoke/default browser behavior, not full WCAG audit.
- [x] Main text is readable on desktop and mobile.
- [x] No obvious contrast issue blocks normal use.

### Evidence
- E2E: UC-01 to UC-07 flows.
- Frontend: labels, placeholders, validation messages, empty states, success/error messages.
- Responsive: mobile viewport/no-overflow tests.
- Result: **mostly fulfilled**, but formal peer tester name/date evidence missing.

---

# NFR-09 — Maintainability

## Checklist

### Backend maintainability
- [x] Controllers do not contain heavy business logic.
- [x] Service layer contains business rules.
- [x] Repository layer handles database access.
- [x] DTO mapping is separated from persistence entities where applicable.
- [x] Validation is centralized or consistently applied.
- [x] Critical services are readable and reasonably small.

### Frontend maintainability
- [x] Components do not contain unnecessary backend/business logic.
- [x] API access is placed in services.
- [x] DTO/model types are defined clearly.
- [x] Repeated UI logic is not copied excessively.
- [x] Error/loading state handling is consistent.

### Documentation / handover
- [x] README explains how to run backend.  
  **Partial:** setup/config exists, but docs could be more polished.
- [x] README explains how to run frontend.  
  **Partial:** setup/config exists, but docs could be more polished.
- [x] README explains how to run tests.  
  **Evidence:** test scripts and runner exist.
- [x] Architecture overview exists or is updated.  
  **Evidence:** layered structure and QA docs.
- [x] Important modules/features are documented enough for another team member to continue.
- [x] Known limitations are documented in this completed QA report.

### Evidence
- Backend folders: `endpoint`, `endpoint/dto`, `endpoint/mapper`, `entity`, `repository`, `service`, `service/impl`, `security`, `exception`, `config`.
- Frontend folders: `components`, `services`, `dtos`, `guards`, `interceptors`.
- QA docs: backend/frontend/general feature QA docs and coverage overview.
- TODO/FIXME/HACK search: no unresolved production-code blockers found in searched paths.
- Result: **fulfilled for DEV scope**.

---

# NFR-10 — Testability

## Checklist

### Backend automated tests
- [x] Authentication tests exist.
- [x] Authorization/RBAC tests exist.
- [x] Invitation/WG creation tests exist.
- [x] Task assignment tests exist.
- [x] Cleaning schedule tests exist.
- [x] Expense tracking tests exist.
- [ ] Payment status/settlement tests exist.  
  **Missing/limited evidence:** not as clear as expense/balance tests.
- [x] Shopping list/request/aggregation tests exist.
- [x] Dashboard/history tests exist.
- [x] Negative backend tests exist for critical workflows.

### Frontend automated tests
- [x] Critical component tests exist where appropriate.
- [x] Frontend service tests exist where appropriate.
- [x] Form validation tests exist where appropriate.
- [x] Error/loading/empty state tests exist where appropriate.

### E2E automated tests
- [x] Registration/login e2e test exists.
- [x] WG creation/invitation e2e test exists.
- [x] Task/cleaning workflow e2e test exists.
- [x] Expense/payment workflow e2e test exists.  
  **Note:** expense/history covered; payment-specific settlement evidence limited.
- [x] Shopping workflow e2e test exists.
- [x] Dashboard/history e2e test exists.
- [x] Negative e2e tests exist for forbidden access and invalid input.
- [x] Tests use stable selectors or reliable query strategy.
- [x] Tests do not depend on state created by unrelated previous tests.

### CI evidence
- [x] Backend tests run in CI or local evidence is documented.  
  **Local evidence documented.**
- [x] Frontend tests run in CI or local evidence is documented.  
  **Local evidence documented.**
- [x] E2E tests run in CI or local evidence is documented.  
  **Local evidence documented.**
- [ ] Failing tests block or are treated as merge blockers.  
  **Missing evidence:** no CI/pipeline enforcement link provided.

### Evidence
- Backend: `mvn test`, green, 182 tests.
- Frontend: `npm test -- --watch=false`, green.
- E2E: `npx cypress run`, green after fixes.
- Runner: `run-e2e.sh`.
- Result: **fulfilled locally**, CI enforcement evidence missing.

---

# NFR-11 — Auditability

## Checklist

### Traceable actions
- [x] Expense creation stores creator/responsible user where appropriate.
- [x] Expense creation stores timestamp where appropriate.
- [ ] Payment/settlement status changes store timestamp where appropriate.  
  **Missing/limited evidence:** payment/settlement traceability not clearly established.
- [x] Task assignment stores responsible user/assignee where appropriate.
- [x] Task completion stores completion state and timestamp where appropriate.
- [x] WG membership changes store join/leave timestamps where appropriate.  
  **Partial:** membership/household timestamps exist; leave flow not fully confirmed.
- [x] Shopping item/request creation stores creator and timestamp where appropriate.
- [x] History view shows important completed/changed actions correctly.

### Data consistency
- [x] History entries correspond to real actions.
- [x] Dashboard numbers match underlying data.
- [x] Payment status overview matches expense/payment records.  
  **Partial:** balances/expense history evidenced; dedicated payment status weaker.
- [ ] Deleted/anonymized users do not break historical traceability.  
  **Missing evidence:** no deletion/anonymization workflow.

### Evidence
- Entity/DTO review: tasks, cleaning schedules, expenses, shopping items, household/membership, dashboard/history.
- Backend tests: `HistoryEndpointTest`, `DashboardEndpointTest`, `ExpenseEndpointTest`, `TaskEndpointTest`, `CleaningScheduleEndpointTest`, `ShoppingItemEndpointTest`.
- E2E: `fr13-history.cy.js`, dashboard/history use cases.
- Result: **mostly fulfilled** at application level, no immutable append-only audit log.

---

# NFR-12 — Compatibility

## Checklist

### Reference viewports
- [x] UI works at 390 × 844 CSS px or comparable iPhone 12 Pro viewport.
- [x] UI works at 412 × 915 CSS px or comparable Pixel 7 viewport.
- [x] UI works at minimum 360 CSS px width.
- [x] UI works at 1366 × 768 px desktop resolution or higher.
- [x] Main navigation remains usable on mobile.
- [x] Main forms remain usable on mobile.
- [x] Tables/lists do not become unreadable or unusable on mobile.

### Reference browsers
- [x] Chrome/Chromium desktop smoke test passed.
- [ ] Firefox desktop smoke test passed, if available.  
  **Missing evidence.**
- [x] Mobile Chrome responsive/device smoke test passed.  
  **Evidence:** Cypress responsive viewport tests, not a physical mobile device.
- [ ] Safari/iOS behavior is checked manually or limitation is documented.  
  **Limitation documented:** not manually tested.

### QR/NFC fallback compatibility
- [x] Core workflows do not depend on NFC.
- [ ] If appliance/NFC routes are implemented, a QR-code or normal link fallback exists or the limitation is documented.  
  **Limitation documented:** QR/NFC flow was not completed/reviewed; QR issue was reported to developer.
- [ ] QR fallback opens the same intended route/view as the NFC/deep-link route where implemented.  
  **Missing evidence.**
- [x] Users without NFC-capable devices can still complete the core WG workflow.

### Evidence
- Responsive SCSS/media-query evidence exists.
- Cypress viewport/no-horizontal-overflow tests exist in:
  - `e2e/cypress/e2e/fr05-expenses.cy.js`
  - `e2e/cypress/e2e/fr12-dashboard.cy.js`
  - `e2e/cypress/e2e/usecases/uc07-navigation-smoke.cy.js`
- Angular browser build config exists: browser entry, polyfills, production build config, ES2022 target.
- Result: **partially fulfilled / accepted for core workflow**, with missing full browser matrix and QR fallback evidence.

---

# Out-of-scope note — Original NFR-13 to NFR-16

The original project contract contains additional NFRs for automation safety, voice data privacy, LLM data minimization, and LLM output control. They are not included as active checklist rows in this DEV QA file because ASR/voice/LLM automation is outside the current general QA scope.

If ASR, voice input, or LLM command parsing is later implemented, create a separate dedicated QA checklist for those features instead of mixing them into the core NFR checklist.
