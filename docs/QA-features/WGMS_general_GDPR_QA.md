# WGMS — General GDPR QA Form

## Scope

This QA form checks the general GDPR-oriented requirements for WGMS.

The goal is **not** to prove full production GDPR compliance.  
The goal is to verify that the university demo project handles personal data responsibly, documents its limitations honestly, and provides basic GDPR support.

This QA form focuses on:

- data inventory and transparency
- data minimization
- user access to own data
- correction where implemented
- deletion/anonymization where possible
- user data export
- Terms and Privacy acknowledgement
- secure data handling
- one general Legal/GDPR frontend component
- one E2E test that demonstrates the full GDPR/legal user-facing functionality

---

# GDPR-01 — Data Inventory and Transparency

## Requirement

WGMS must document what categories of personal data are stored and why.

## Checklist

- [x] Account data is documented: name, email, password hash, user role.
- [x] WG membership data is documented: household, invitation links, user role, join/leave timestamps if implemented.
- [x] Task data is documented: assigned chores, completion status, due dates, task history.
- [x] Cleaning schedule data is documented: schedule title, recurrence, assignment, due dates.
- [x] Expense/payment data is documented: expenses, splits, payments, balances.
- [x] Shopping data is documented: shopping items, visibility, status, shopping history.
- [x] Dashboard/history data is documented as derived data.
- [x] Technical/security data is documented: logs, timestamps, errors, authentication/session metadata.
- [x] Documentation explains that no special-category personal data is required.
- [x] Documentation explains that no real banking/payment data is processed.
- [x] Documentation explains that WGMS is a university demo project and not a production service.

## Backend evidence

- [x] Entity review confirms stored data categories.
- [x] DTO review confirms exposed data categories.
- [x] No unnecessary sensitive fields are stored.
- [x] No banking/payment-provider data is stored.

## Frontend evidence

- [x] One general Legal/GDPR component exists.
- [x] Legal/GDPR page explains what data is stored.
- [x] Legal/GDPR page explains why the data is stored.
- [x] Legal/GDPR page explains current demo limitations.

## E2E evidence

- [x] User can open the Legal/GDPR page.
- [x] Page displays the main data categories.
- [x] Page displays the university demo limitation.
- [x] Page displays that no real money transfers are performed.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated 2026-06-05.
- Legal/GDPR component: frontend/src/app/components/legal/legal.component.html documents account, WG membership, tasks, cleaning schedules, expenses/payments, shopping, dashboard/history, and technical/security data.
- Legal page states university demo scope, no special-category data required, no real banking/payment-provider data, and no real money transfers.
- Backend entity/DTO review: ApplicationUser, Household/Roommate/HouseholdInvitation, Task, CleaningSchedule, Expense/ExpenseSplit/Payment, ShoppingItem, history/dashboard DTOs. Safe DTOs: UserProfileDto and UserDataExportDto.
- E2E evidence: e2e/cypress/e2e/gdpr-legal.cy.js checks /legal, main categories, university demo limitation, and no real money transfers.
- Test run: npm run cypress -- --spec cypress/e2e/gdpr-legal.cy.js --config baseUrl=http://127.0.0.1:4201/ => 3 passing.
```

---

# GDPR-02 — One General Legal/GDPR Frontend Component

## Requirement

WGMS should have **one general frontend component** for GDPR/legal information instead of many fragmented pages.

The component should contain:

- Privacy / Data Usage information
- Terms and Conditions
- GDPR demo support
- Current limitations

## Expected implementation

Suggested component:

```text
LegalInfoComponent
```

Suggested route:

```text
/legal
```

Alternative route is acceptable if consistent with the existing routing structure.

## Checklist

- [x] One general legal/GDPR Angular component exists.
- [x] Component is reachable through a public route such as `/legal`.
- [x] Component contains a Privacy/Data Usage section.
- [x] Component contains a Terms and Conditions section.
- [x] Component contains a GDPR Demo Support section.
- [x] Component contains a Current Limitations section.
- [x] Component is reachable from the registration page.
- [x] Component is reachable from login page, public navigation, or footer if available.
- [x] Component does not claim full production GDPR compliance.
- [x] Component uses clear user-facing language.

## Required page content

### Privacy / Data Usage section

- [x] States that WGMS is a university demo project.
- [x] Lists stored data categories:
  - [x] account data
  - [x] WG membership data
  - [x] tasks and cleaning schedules
  - [x] expenses, payments, and balances
  - [x] shopping items and shopping history
  - [x] dashboard/history data
  - [x] technical/security data
- [x] Explains why data is stored:
  - [x] authentication
  - [x] WG membership management
  - [x] task assignment
  - [x] cleaning schedules
  - [x] expense/payment tracking
  - [x] shopping management
  - [x] dashboard/history display
- [x] Explains data minimization.
- [x] States that password hashes and tokens are not shown to users.
- [x] States that password hashes and tokens are not included in exports.

### Terms and Conditions section

- [x] States that WGMS is for university/demo use.
- [x] States that users should enter only appropriate demo/project data.
- [x] States that users are responsible for correctness of created tasks, expenses, payments, and shopping items.
- [x] States that household data may be visible to authorized members of the same WG.
- [x] States that WGMS does not perform real money transfers.
- [x] States that WGMS is not a legal, accounting, tax, rental-management, or banking system.
- [x] Displays Terms version, for example `v1.0`.

### GDPR Demo Support section

- [x] Mentions profile/account access if `/api/v1/users/me` exists.
- [x] Mentions user data export if `/api/v1/users/me/export` exists.
- [x] Mentions correction/deletion of domain data where implemented.
- [x] Mentions deletion/anonymization if implemented.
- [x] Otherwise documents deletion/anonymization as a limitation.

### Current Limitations section

- [x] States that full production GDPR compliance is not claimed.
- [x] States that formal retention rules are not implemented unless they are.
- [x] States that account deletion/anonymization may be limited unless implemented.
- [x] States that no real banking/payment data is processed.
- [x] States that no special-category personal data is required.
- [x] States that external hosting/processor handling is only relevant for real deployment.

## Required stable selectors

The component should include stable selectors for tests:

```html
data-cy="legal-page"
data-cy="legal-privacy-section"
data-cy="legal-terms-section"
data-cy="legal-gdpr-support-section"
data-cy="legal-limitations-section"
```

## Frontend tests to cover

- [x] Legal component renders successfully.
- [x] Privacy section is visible.
- [x] Terms section is visible.
- [x] GDPR demo support section is visible.
- [x] Current limitations section is visible.
- [x] Component contains the expected keywords:
  - [x] university demo
  - [x] account data
  - [x] household
  - [x] tasks
  - [x] expenses
  - [x] shopping
  - [x] export
  - [x] not a production service
  - [x] no real money transfers

## E2E tests to cover

- [x] Visiting `/legal` displays the legal page.
- [x] Privacy/Data Usage section is visible.
- [x] Terms and Conditions section is visible.
- [x] GDPR Demo Support section is visible.
- [x] Current Limitations section is visible.
- [x] Legal page is reachable from registration.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated 2026-06-05.
- LegalComponent exists at frontend/src/app/components/legal/ and route /legal is registered in frontend/src/app/app-routing.module.ts.
- Required selectors exist: legal-page, legal-privacy-section, legal-terms-section, legal-gdpr-support-section, legal-limitations-section.
- Registration and public header link to /legal.
- Unit evidence: frontend/src/app/components/legal/legal.component.spec.ts covers selectors and keywords.
- E2E evidence: e2e/cypress/e2e/gdpr-legal.cy.js visits /legal and checks all required sections/keywords plus registration legal link.
- Test runs: npm test -- --watch=false --browsers=ChromeHeadless => 264 SUCCESS; Cypress GDPR spec => 3 passing.
```

---

# GDPR-03 — Data Minimization

## Requirement

WGMS should only collect, store, and expose data needed for WG management.

## Checklist

- [x] Registration only asks for required user/account data.
- [x] Registration does not ask for unnecessary personal data.
- [x] DTOs do not expose password hashes.
- [x] DTOs do not expose JWT secrets.
- [x] DTOs do not expose internal security fields.
- [x] Users only see data relevant to their own WG.
- [x] Export/profile endpoints do not expose unnecessary private data.
- [x] Error messages do not reveal unnecessary internal details.
- [x] Logs do not contain passwords.
- [x] Logs do not contain JWTs/tokens.
- [x] Logs do not contain unnecessary full financial/private details.

## Backend tests to cover

- [x] Registration DTO validation test exists.
- [x] Profile response does not contain `password`.
- [x] Profile response does not contain `passwordHash`.
- [x] Profile response does not contain `token`.
- [x] Profile response does not contain `jwt`.
- [x] Export response does not contain `password`.
- [x] Export response does not contain `passwordHash`.
- [x] Export response does not contain `token`.
- [x] Export response does not contain `jwt`.
- [x] Cross-household access is rejected.

## Frontend tests to cover

- [x] Registration form only contains necessary fields.
- [x] Profile/account UI does not display sensitive fields.
- [x] Export UI does not display sensitive fields.

## E2E tests to cover

- [x] User A cannot access User B data.
- [x] User from WG A cannot access WG B data.
- [x] Profile/export flow does not reveal sensitive fields.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated 2026-06-05.
- Registration form fields are firstName, lastName, email, password, termsAccepted only.
- UserProfileDto/UserDataExportDto are dedicated safe DTOs and do not return password, passwordHash, token, jwt, failedLoginAttempts, accountLockedUntil.
- UserEndpointTest asserts profile/export exclude password/passwordHash/token/jwt and exclude another household/user data.
- Log issue fixed: DemoDataGenerator no longer logs demo passwords; LogFilter now sanitizes invitation token paths and password/token/jwt/authorization/secret query parameters.
- E2E evidence: e2e/cypress/e2e/gdpr-profile-export.cy.js opens the profile page, verifies safe account/WG data, verifies the profile UI does not show password/passwordHash/token/jwt/secret, triggers export, and asserts the export JSON contains generatedAt/account while excluding password/passwordHash/token/jwt/secret.
- E2E evidence: e2e/cypress/e2e/gdpr-security.cy.js verifies a user from WG B cannot read WG A members by changing the household ID. There is no profile-by-user-id route/API for direct User A -> User B profile access; backend UserEndpointTest covers own-user derivation and no frontend-supplied user IDs.
- Test runs: npm run cypress -- --spec cypress/e2e/gdpr-profile-export.cy.js --config baseUrl=http://127.0.0.1:4201/ => 3 passing; npm run cypress -- --spec cypress/e2e/gdpr-security.cy.js --config baseUrl=http://127.0.0.1:4201/ => 4 passing.
```

---

# GDPR-04 — User Access Right

## Requirement

A logged-in user should be able to view their own account/profile and relevant WG data.

## Checklist

- [x] `GET /api/v1/users/me` exists.
- [x] Endpoint requires authentication.
- [x] Endpoint derives the user from JWT/authentication context.
- [x] Endpoint does not trust frontend-supplied user IDs.
- [x] Response contains only safe profile/account data.
- [x] Response excludes password hashes.
- [x] Response excludes tokens/JWTs.
- [x] User can view relevant WG membership data.
- [x] User can view relevant task data.
- [x] User can view relevant expense/payment data.
- [x] User can view relevant shopping/history data.

## Backend tests to cover

- [x] Authenticated user can access `/api/v1/users/me`.
- [x] Unauthenticated user is rejected.
- [x] Response belongs to the authenticated user.
- [x] Response excludes password/token fields.
- [x] User cannot request another user's profile by changing an ID.

## Frontend tests to cover

- [x] User/profile service calls `/api/v1/users/me`.
- [x] Profile component/page renders safe account data if implemented.
- [x] Unauthorized profile request is handled safely.

## E2E tests to cover

- [x] Login succeeds.
- [x] User can open profile/account page if implemented.
- [x] User can see safe profile data.
- [x] Logout makes profile route/API inaccessible.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated 2026-06-05.
- GET /api/v1/users/me implemented in UserEndpoint and uses Authentication; no userId/email is accepted from the frontend.
- CustomUserDetailService maps the current authenticated ApplicationUser/Roommate/Household to UserProfileDto.
- UserEndpointTest covers authenticated success, unauthenticated rejection, own-account data, household fields, and sensitive-field exclusion.
- Frontend service/profile page added: AuthService.getCurrentUserProfile() and ProfileComponent.
- E2E evidence: e2e/cypress/e2e/gdpr-profile-export.cy.js registers a unique user, creates a WG, logs out, logs in with the generated credentials, opens /profile, verifies safe own profile/WG data, logs out, verifies /profile redirects to login, and verifies GET /api/v1/users/me returns 401/403 without auth.
- Test run: npm run cypress -- --spec cypress/e2e/gdpr-profile-export.cy.js --config baseUrl=http://127.0.0.1:4201/ => 3 passing.
```

---

# GDPR-05 — Correction Right

## Requirement

Users or authorized WG members should be able to correct data where the app supports editing.

## Checklist

- [x] User profile correction exists, or limitation is documented.
- [ ] Household data correction exists where implemented.
- [x] Task correction exists where implemented.
- [x] Cleaning schedule correction exists where implemented.
- [x] Shopping item correction exists where implemented.
- [x] Expense correction exists where implemented.
- [x] Invalid edits are rejected.
- [x] Unauthorized users cannot edit other WG data.
- [ ] Corrections preserve history/audit consistency where relevant.

## Backend tests to cover

- [x] Valid update succeeds.
- [x] Invalid update fails.
- [x] Unauthorized update fails.
- [x] Cross-WG update fails.
- [x] Backend derives acting user from authentication context.

## Frontend tests to cover

- [x] Edit form validation works.
- [x] Error state is shown for failed correction.
- [x] Updated value is displayed after save.
- [x] Save button is disabled or validation appears for invalid data.

## E2E tests to cover

- [x] User edits a task/shopping item/expense where supported.
- [x] User sees updated value after reload.
- [x] Unauthorized edit attempt is rejected.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated 2026-06-05.
- PATCH /api/v1/users/me corrects editable profile fields email, firstName, lastName from Authentication context.
- Profile edit UI only exposes editable profile fields and leave-WG action.
- UserEndpointTest covers valid profile update, invalid update, email conflict, login with changed email, and leave-current-household.
- Existing endpoint tests cover task/cleaning/shopping/expense update authorization and cross-WG rejection.
- E2E evidence: e2e/cypress/e2e/gdpr-profile-export.cy.js opens profile edit, changes first/last name, saves through PATCH /api/v1/users/me, sees success state, reloads the edit page, and verifies the corrected values persist and are visible on /profile.
- E2E evidence: e2e/cypress/e2e/gdpr-domain-history.cy.js creates a task, opens the task edit UI, changes title/description, saves through PUT /api/v1/tasks/{id}, reloads the task list, verifies the corrected values persist, and verifies a user from another WG receives 403 when attempting to edit the task by ID.
- Household data correction is not implemented as an editable feature in the current UI; this is treated as not applicable for this demo QA pass.
- Test runs: npm run cypress -- --spec cypress/e2e/gdpr-profile-export.cy.js --config baseUrl=http://127.0.0.1:4201/ => 3 passing; npm run cypress -- --spec cypress/e2e/gdpr-domain-history.cy.js --config baseUrl=http://127.0.0.1:4201/ => 2 passing.
```

---

# GDPR-06 — Deletion / Anonymization

## Requirement

Users should be able to leave a WG and/or request account deletion. If financial/task history must remain understandable, anonymization is acceptable.

## Checklist

- [x] User can leave WG, or limitation is documented.
- [x] Account deletion exists, or limitation is documented.
- [x] Account anonymization exists, or limitation is documented.
- [x] Deleted/anonymized users cannot still log in.
- [x] Historical expenses remain understandable after anonymization.
- [x] Historical tasks remain understandable after anonymization.
- [x] Historical payments remain understandable after anonymization.
- [x] Other users' financial history is not broken by deletion.
- [x] UI uses a clear confirmation dialog for destructive actions if implemented.

## Backend tests to cover

- [x] Delete/anonymize current user succeeds if implemented.
- [x] Deleted/anonymized user cannot authenticate.
- [x] History still displays safe fallback name such as `Deleted User`.
- [x] Cross-user deletion is rejected.
- [x] Deletion/anonymization is transactional.

## Frontend tests to cover

- [x] Delete/anonymize action is shown only where intended.
- [x] Confirmation dialog exists.
- [x] Error/success state is shown.
- [x] User is logged out after account deletion/anonymization if implemented.

## E2E tests to cover

- [x] User requests deletion/anonymization if implemented.
- [x] User cannot log in afterwards.
- [x] Existing household history remains readable.
- [ ] If feature is not implemented, limitation is visible in documentation/legal page.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated 2026-06-05.
- DELETE /api/v1/users/me anonymizes the authenticated user; DELETE /api/v1/users/me/household leaves the current WG. Both derive the actor from Authentication.
- CustomUserDetailService anonymization is transactional, disables login by changing email/password/anonymized flag, and deactivates active roommate membership.
- UserEndpointTest covers delete/anonymize success, login rejection, safe history readability for tasks/expenses, and payment overview readability with Deleted User/anonymized email while preserving other user's view.
- Profile UI has separate delete button, privacy note, confirmation dialog, and logout after delete.
- E2E evidence: e2e/cypress/e2e/gdpr-profile-export.cy.js registers a generated throwaway user, opens /profile, confirms the delete/anonymize action, verifies DELETE /api/v1/users/me returns 204, verifies the app redirects/logs out, and verifies login with the old credentials fails with 401.
- E2E evidence: e2e/cypress/e2e/gdpr-domain-history.cy.js creates task, expense, and payment history for a throwaway household member, anonymizes that account, logs in as another authorized WG member, verifies /history still displays the historic task and expense without the raw deleted email, and verifies /payment-status displays the safe fallback `Deleted User` with the historic payment amount.
- The documentation/legal limitation path is not applicable because account anonymization is implemented; the legal page documents the demo scope and limitations.
- Test runs: npm run cypress -- --spec cypress/e2e/gdpr-profile-export.cy.js --config baseUrl=http://127.0.0.1:4201/ => 3 passing; npm run cypress -- --spec cypress/e2e/gdpr-domain-history.cy.js --config baseUrl=http://127.0.0.1:4201/ => 2 passing.
```

---

# GDPR-07 — User Data Export

## Requirement

Users should be able to export their user-related data, or the limitation must be documented.

## Checklist

- [x] `GET /api/v1/users/me/export` exists, or limitation is documented.
- [x] Endpoint requires authentication.
- [x] Export is generated for the authenticated user.
- [x] Export is machine-readable JSON.
- [x] Export contains `generatedAt`.
- [x] Export includes safe account data.
- [x] Export includes relevant WG membership data.
- [x] Export includes relevant task/cleaning schedule data where available.
- [x] Export includes relevant expense/payment data where available.
- [x] Export includes relevant shopping/history data where available.
- [x] Export excludes password hashes.
- [x] Export excludes JWTs/tokens.
- [x] Export excludes unrelated private data from other households.
- [x] Export does not return raw JPA entities with circular references.

## Backend tests to cover

- [x] Authenticated export succeeds.
- [x] Unauthenticated export fails.
- [x] Export contains `generatedAt`.
- [x] Export contains only the authenticated user's relevant data.
- [x] Export excludes password fields.
- [x] Export excludes token/JWT fields.
- [x] Export does not include data from another WG.

## Frontend tests to cover

- [x] Export button/service exists if UI was implemented.
- [x] Export request handles success state.
- [x] Export request handles error state.
- [x] Export UI does not display password/token fields.

## E2E tests to cover

- [x] User logs in and exports data if UI was implemented.
- [x] Export action completes successfully.
- [x] Export response/file is valid JSON if testable.
- [x] Export does not expose sensitive fields.
- [x] If export UI is not implemented, API-level E2E or backend integration test exists.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated 2026-06-05.
- GET /api/v1/users/me/export implemented in UserEndpoint/UserExportServiceImpl and derives user from Authentication.
- UserDataExportDto is a dedicated safe JSON DTO with generatedAt, account, membership, tasks, cleaning schedules, expenses, payments, shopping items, and history where available.
- UserEndpointTest covers authenticated export, unauthenticated rejection, generatedAt, own account/membership data, no password/passwordHash/token/jwt, and exclusion of another WG's task/user.
- Frontend service/profile export button added and covered in AuthService/ProfileComponent tests.
- E2E evidence: e2e/cypress/e2e/gdpr-profile-export.cy.js logs in, opens /profile, clicks data-cy=profile-export-button, intercepts GET /api/v1/users/me/export, verifies HTTP 200, generatedAt, safe account data, household membership data, and no password/passwordHash/token/jwt/secret in the exported JSON.
- Test run: npm run cypress -- --spec cypress/e2e/gdpr-profile-export.cy.js --config baseUrl=http://127.0.0.1:4201/ => 3 passing.
```

---

# GDPR-08 — Terms and Privacy Acknowledgement

## Requirement

If Terms/Privacy acknowledgement is implemented, it must be explicit, active, and not pre-selected.

## Checklist

- [x] Registration contains a Terms/Privacy acknowledgement checkbox.
- [x] Checkbox is initially unchecked.
- [x] Checkbox is not pre-selected.
- [x] User must actively tick the checkbox before registration.
- [x] Registration is rejected if checkbox is missing.
- [x] Registration is rejected if checkbox is false.
- [x] Registration succeeds if checkbox is true and all other fields are valid.
- [x] Checkbox text links to the general Legal/GDPR page.
- [x] Checkbox wording does not falsely claim full GDPR compliance.
- [x] Acceptance timestamp is stored, or limitation is documented.
- [x] Terms version is stored, or limitation is documented.
- [x] Privacy acknowledgement timestamp/version is stored, or limitation is documented.

## Suggested checkbox text

```text
I accept the Terms and Conditions and acknowledge the Privacy/Data Usage information.
```

## Required stable selectors

```html
data-cy="registration-terms-checkbox"
data-cy="registration-legal-link"
data-cy="registration-submit"
data-cy="registration-terms-error"
```

## Backend tests to cover

- [x] Registration with `termsAccepted = true` succeeds.
- [x] Registration with `termsAccepted = false` fails.
- [x] Registration with missing `termsAccepted` fails.
- [x] Validation error is safe and understandable.
- [x] Acceptance timestamp/version is stored if implemented.
- [x] Response does not leak passwordHash/token fields.

## Frontend tests to cover

- [x] Checkbox is initially unchecked.
- [x] Submit is disabled or form invalid while checkbox is unchecked.
- [x] Validation message appears if submit is attempted without acknowledgement.
- [x] Terms/Privacy legal link exists.
- [x] Form can be submitted after checkbox is checked and all other fields are valid.

## E2E tests to cover

- [x] Registration page displays the Terms/Privacy checkbox.
- [x] Registration without acceptance is blocked.
- [x] Legal link from registration opens `/legal` or the configured legal route.
- [x] Returning to registration and checking the checkbox allows successful registration.
- [x] Backend/API rejects `termsAccepted = false` if API-level E2E is implemented.
- [x] Backend/API rejects missing `termsAccepted` if API-level E2E is implemented.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated 2026-06-05.
- Registration DTO requires termsAccepted=true and stores termsAcceptedAt, termsVersion=v1.0, privacyAcknowledgedAt, privacyVersion=v1.0.
- Registration checkbox is unchecked by default, requiredTrue, disabled submit while invalid, and links to /legal with required data-cy selectors.
- Backend tests cover true/false/missing termsAccepted and stored timestamps/versions.
- Frontend tests cover unchecked checkbox, invalid form while unchecked, selector/link rendering, and successful submit with termsAccepted=true.
- E2E evidence: gdpr-legal.cy.js checks unchecked checkbox, disabled submit, legal link, successful checked registration, and API false/missing rejection.
```

---

# GDPR-09 — Secure Data Handling

## Requirement

Personal data must be protected through authentication, authorization, secure password handling, restricted logs, and secure configuration.

## Checklist

- [x] Passwords are hashed and salted.
- [x] Password hashes are not returned in DTOs.
- [x] JWTs/tokens expire after a reasonable time.
- [x] Invalid/tampered tokens are rejected.
- [x] Invitation links are time-limited.
- [x] Invitation links are difficult to guess.
- [x] Invitation links are single-use if required by the PA.
- [x] Users can only access data belonging to their own WG.
- [x] Main Tenant-only actions are protected on the backend.
- [x] Frontend hiding is not treated as sufficient authorization.
- [x] Logs do not contain passwords.
- [x] Logs do not contain JWTs/tokens.
- [x] Logs do not contain unnecessary full private financial details.
- [ ] Secrets are not committed to Git.
- [x] Test/demo environments use artificial data.

## Backend tests to cover

- [x] Login succeeds with valid credentials.
- [x] Login fails safely with invalid credentials.
- [x] Passwords are stored as hashes.
- [x] Invalid/tampered/expired tokens are rejected.
- [x] Role-based access tests exist.
- [x] Household isolation tests exist.
- [x] Invitation expiry/single-use tests exist.
- [x] Sensitive field exclusion tests exist.

## Frontend tests to cover

- [x] Protected routes require login.
- [x] Logout clears client authentication state.
- [x] Unauthorized API errors are handled safely.
- [x] Main Tenant-only actions are not shown to Roommates where appropriate.

## E2E tests to cover

- [x] Unauthenticated user cannot access protected views.
- [x] Roommate cannot perform Main Tenant-only action.
- [x] User from WG A cannot access WG B data.
- [x] Logout prevents further protected API access.

## Status

- [ ] Passed
- [x] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated 2026-06-05.
- Password hashing verified in UserEndpointTest using PasswordEncoder.matches and non-equality to raw password.
- JWT expiry configured in application.yml at 12 hours; SecurityTest covers invalid authorization headers/tokens.
- Invitation tokens use random raw token plus SHA-256 tokenHash and expiresAt; InvitationEndpointTest covers role restrictions and invitation security paths.
- Added InvitationEndpointTest coverage that persisted tokenHash is not the raw token, tokenHash is SHA-256 length, expired invitation preview/accept returns 410, and reused invitation accept returns 410.
- Household isolation/cross-WG authorization covered by TaskEndpointTest, CleaningScheduleEndpointTest, ExpenseEndpointTest, ShoppingItemEndpointTest, PaymentEndpointTest, HistoryEndpointTest, UserEndpointTest, and e2e/cypress/e2e/gdpr-security.cy.js.
- E2E evidence: gdpr-security.cy.js verifies unauthenticated protected views redirect, protected API access after logout returns 401/403, a Roommate cannot access invitation management UI/API, WG B cannot read WG A members by changing IDs, and invitation link reuse returns 410.
- Logging issue fixed: demo password log removed and LogFilter redacts sensitive query params.
- Development fallback secret handling is documented in backend/README.md as demo-only; real deployments must set JWT_SECRET/DB_USERNAME/DB_PASSWORD through environment/config management. No real deployed credentials are documented as committed.
- Added backend/src/main/resources/application-prod.yml so the prod profile requires JWT_SECRET, DB_USERNAME, and DB_PASSWORD from environment variables without default fallback values.
- Remaining limitation: application.yml still contains development/test fallback credentials/secrets for university-demo convenience, so this section remains partial for production-secret handling.
- Test runs: mvn -q -Dtest=InvitationEndpointTest,UserEndpointTest test => passed; mvn -q test => passed; npm run cypress -- --spec cypress/e2e/gdpr-security.cy.js --config baseUrl=http://127.0.0.1:4201/ => 4 passing.
```

---

# GDPR-10 — Full GDPR/Legal E2E Test

## Requirement

There must be one E2E test that demonstrates the full user-facing GDPR/legal functionality.

Suggested Cypress spec:

```text
e2e/cypress/e2e/gdpr-legal.cy.js
```

Alternative filename is acceptable if consistent with existing naming.

## Scenario 1 — Legal page is reachable and complete

- [x] Test visits `/legal` or the configured legal route.
- [x] Test asserts `data-cy="legal-page"` is visible.
- [x] Test asserts Privacy/Data Usage section is visible.
- [x] Test asserts Terms and Conditions section is visible.
- [x] Test asserts GDPR Demo Support section is visible.
- [x] Test asserts Current Limitations section is visible.
- [x] Test checks important page content:
  - [x] `university demo`
  - [x] `account data`
  - [x] `household`
  - [x] `tasks`
  - [x] `expenses`
  - [x] `shopping`
  - [x] `export`
  - [x] `not a production`
  - [x] `no real money transfers`

## Scenario 2 — Registration requires legal acknowledgement

- [x] Test visits registration page.
- [x] Test fills all required registration fields with valid unique test data.
- [x] Test verifies Terms/Privacy checkbox is initially unchecked.
- [x] Test attempts to submit without checking the checkbox.
- [x] Test verifies registration does not complete.
- [x] Test verifies validation error is shown or submit button is disabled.
- [x] Test clicks the legal link from registration.
- [x] Test verifies legal page opens.
- [x] Test returns to registration.
- [x] Test fills registration again if needed.
- [x] Test checks the Terms/Privacy checkbox.
- [x] Test submits registration.
- [x] Test verifies registration succeeds or reaches the expected post-registration/login/WG flow.

## Scenario 3 — Backend rejects false/missing acknowledgement

Include this scenario if Cypress API helpers or direct `cy.request` registration is available.

- [x] API request with `termsAccepted = false` is rejected.
- [x] API request with missing `termsAccepted` is rejected.
- [x] API request with `termsAccepted = true` and valid unique email succeeds.
- [x] API response does not expose passwordHash/token fields.

## E2E quality requirements

- [x] Test uses unique generated emails.
- [x] Test uses stable `data-cy` selectors.
- [x] Test does not depend on unrelated previous test state.
- [x] Test does not require manual intervention.
- [x] Test is included in normal Cypress run or documented runner script.
- [x] Test is reliable in headless mode.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated 2026-06-05.
- Added e2e/cypress/e2e/gdpr-legal.cy.js.
- Scenario 1 checks /legal, stable selectors, all required sections, and required keywords.
- Scenario 2 checks registration fields, initially unchecked checkbox, disabled submit without acknowledgement, legal link to /legal, checked submit, auth token, and expected post-registration route.
- Scenario 3 checks API rejection for termsAccepted=false and missing, API success for true, and no passwordHash/token in API response.
- Test run: npm run cypress -- --spec cypress/e2e/gdpr-legal.cy.js --config baseUrl=http://127.0.0.1:4201/ => 3 passing.
```

---

# Final GDPR QA Summary

| Area | Status | Notes |
|---|---|---|
| Data inventory / transparency | [x] Pass [ ] Partial [ ] Fail | Legal page + E2E + entity/DTO review evidence. |
| One general Legal/GDPR component | [x] Pass [ ] Partial [ ] Fail | /legal component, selectors, unit and Cypress evidence. |
| Data minimization | [x] Pass [ ] Partial [ ] Fail | Backend/frontend evidence plus profile/export minimization E2E. |
| User access right | [x] Pass [ ] Partial [ ] Fail | /users/me implemented/tested plus profile/logout E2E. |
| Correction right | [x] Pass [ ] Partial [ ] Fail | Profile and task correction reload E2E covered, including cross-WG edit rejection. |
| Deletion / anonymization | [x] Pass [ ] Partial [ ] Fail | Throwaway-user anonymization and history/payment readability E2E covered. |
| User data export | [x] Pass [ ] Partial [ ] Fail | Export API/UI covered plus export button/API E2E. |
| Terms / Privacy acknowledgement | [x] Pass [ ] Partial [ ] Fail | Backend, frontend, and Cypress evidence. |
| Secure data handling | [ ] Pass [x] Partial [ ] Fail | Security matrix and invitation single-use covered; dev fallback secrets remain documented demo-only. |
| Full GDPR/legal E2E test | [x] Pass [ ] Partial [ ] Fail | gdpr-legal.cy.js passed 3/3. |

## Overall result

- [ ] GDPR QA passed for university demo scope
- [x] GDPR QA partially passed with documented limitations
- [ ] GDPR QA failed / blocking issues remain

## Remaining limitations

```text
- This remains a university demo and does not claim full production GDPR compliance.
- Formal production retention/processor/hosting processes are not implemented.
- application.yml contains documented development/test fallback credentials/secrets for local demo convenience; the prod profile requires JWT_SECRET, DB_USERNAME, and DB_PASSWORD through environment/config management.
```

## Evidence links

```text
Backend tests:
- backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/UserEndpointTest.java
- Existing domain/security tests: SecurityTest, InvitationEndpointTest, TaskEndpointTest, CleaningScheduleEndpointTest, ExpenseEndpointTest, ShoppingItemEndpointTest, PaymentEndpointTest, HistoryEndpointTest
- Run: mvn -q -Dtest=UserEndpointTest test => passed
- Run: mvn -q test => passed

Frontend tests:
- frontend/src/app/components/legal/legal.component.spec.ts
- frontend/src/app/components/register/register.component.spec.ts
- frontend/src/app/components/profile/profile.component.spec.ts
- frontend/src/app/components/profile-edit/profile-edit.component.spec.ts
- frontend/src/app/services/auth.service.spec.ts
- Run: npm test -- --watch=false --browsers=ChromeHeadless => 264 SUCCESS
- Run: npm run build => passed

E2E tests:
- e2e/cypress/e2e/gdpr-legal.cy.js
- e2e/cypress/e2e/gdpr-profile-export.cy.js
- e2e/cypress/e2e/gdpr-security.cy.js
- e2e/cypress/e2e/gdpr-domain-history.cy.js
- Run: npm run cypress -- --spec cypress/e2e/gdpr-legal.cy.js --config baseUrl=http://127.0.0.1:4201/ => 3 passing
- Run: npm run cypress -- --spec cypress/e2e/gdpr-profile-export.cy.js --config baseUrl=http://127.0.0.1:4201/ => 3 passing
- Run: npm run cypress -- --spec cypress/e2e/gdpr-security.cy.js --config baseUrl=http://127.0.0.1:4201/ => 4 passing
- Run: npm run cypress -- --spec cypress/e2e/gdpr-domain-history.cy.js --config baseUrl=http://127.0.0.1:4201/ => 2 passing

Manual review:
- Safe DTOs: UserProfileDto, UserProfileUpdateDto, UserDataExportDto
- Auth-derived user loading: UserEndpoint + CustomUserDetailService + UserExportServiceImpl
- Logging fix: LogFilter query sanitization and DemoDataGenerator password-log removal
- Invitation security: hashed token storage, expiry, single-use behavior, and Main Tenant-only invitation management
- Development-only secret note: backend/README.md
- Production secret override: backend/src/main/resources/application-prod.yml

Documentation / legal page:
- frontend/src/app/components/legal/legal.component.html
- docs/QA-features/WGMS_general_GDPR_QA.md

GitLab issues / merge requests:
- Branch: GDPR
```

## QA sign-off

```text
Reviewer: Codex-assisted QA review
Date: 2026-06-05
Branch / commit: GDPR / not committed during this review
Result: Partially passed with documented production/demo-scope limitations
```
