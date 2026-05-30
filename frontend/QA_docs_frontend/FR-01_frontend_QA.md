# FR-01 Frontend QA Checklist — User Registration and Login

## Feature Information

- Feature ID: FR-01
- Feature name: User registration and login
- Use case: UC-01 — User Registration and WG Initialization
- Priority: High
- Category: Core
- Version: V1
- Frontend technology: Angular
- Automated frontend tests: Selenium
- Branch:
- Merge request:
- Developer:
- QA reviewer: Senior QA Engineer
- Date: 2026-05-09
---

## How to Use This Checklist

This checklist must be completed before the frontend part of FR-01 can be considered done.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a short note under the relevant section.
- If something does not apply, write `N/A` with a short reason.
- Do not merge into `dev` while critical items are unchecked.

This checklist is written so that it can be given to an AI assistant or a human reviewer to verify the frontend implementation systematically.

---

# 1. Scope Check

- [x] The branch only contains changes related to FR-01 or clearly justified support changes.
- [x] A registration page/component exists.
- [x] A login page/component exists.
- [x] Registration and login are reachable through normal navigation.
- [x] Registration and login work with the backend API.
- [x] The frontend uses Angular services for API communication.
- [x] The frontend does not put API logic directly inside templates.
- [x] The frontend does not contain unrelated feature work from later features unless justified.
- [x] The implementation supports later WG creation and invitation logic from FR-02.
- [x] The frontend builds successfully.

Notes:

```text

```

---

# 2. Registration UI Check

- [x] Registration page loads successfully.
- [x] Registration form is visible.
- [x] Registration page has a clear title or heading.
- [x] Required fields are visible.
- [x] Required fields are clearly understandable.
- [x] Email/username field exists.
- [x] Password field exists.
- [x] Password confirmation field exists if required by design.
- [x] Name/display name field exists if required by design.
- [x] Submit button exists.
- [x] Link to login exists or navigation to login is possible.
- [x] Form layout is usable on desktop.
- [x] Form layout is usable on mobile-sized screen.
- [x] Form layout is not broken after validation errors.
- [x] User receives clear feedback after successful registration.
- [x] User receives clear feedback after failed registration.

Notes:

```text

```

---

# 3. Registration Functional Check

- [x] User can enter registration data.
- [x] User can submit valid registration form.
- [x] Frontend sends request to correct backend registration endpoint.
- [x] Frontend uses correct HTTP method.
- [x] Frontend sends correct JSON request body.
- [x] Frontend does not send unnecessary personal data.
- [x] Frontend handles successful registration.
- [x] Frontend handles duplicate email/username error.
- [x] Frontend handles validation error from backend.
- [x] Frontend handles backend unavailable/network error.
- [x] Frontend shows loading state during registration.
- [x] Loading state ends after success.
- [x] Loading state ends after error.
- [x] Submit button cannot be spam-clicked to send duplicate requests.
- [x] After success, user is redirected or shown a success message according to design.
- [x] After success, user can continue to login or is logged in automatically according to design.
- [x] Registration works after full page refresh.
- [x] Registration works in a clean browser session.

Notes:

```text

```

---

# 4. Registration Validation Check

- [x] Empty registration form is rejected.
- [x] Empty email/username is rejected.
- [x] Invalid email format is rejected if email is used.
- [x] Empty password is rejected.
- [x] Short/weak password is rejected if password policy exists.
- [x] Password confirmation mismatch is rejected if confirmation exists.
- [x] Leading/trailing spaces in email/username are handled correctly.
- [x] Extremely long email/username input does not break the UI.
- [x] Extremely long password input does not break the UI.
- [x] Special characters do not break the UI.
- [x] HTML/script input is not rendered as executable content.
- [x] Validation messages are clear.
- [x] Validation messages are close to the relevant fields where possible.
- [x] Validation messages update/disappear when input is corrected.
- [x] Validation messages do not expose backend internals.
- [x] Validation messages do not expose security details.

Notes:

```text

```

---

# 5. Login UI Check

- [x] Login page loads successfully.
- [x] Login form is visible.
- [x] Login page has a clear title or heading.
- [x] Email/username field exists.
- [x] Password field exists.
- [x] Submit button exists.
- [x] Link to registration exists or navigation to registration is possible.
- [x] Form layout is usable on desktop.
- [x] Form layout is usable on mobile-sized screen.
- [x] Form layout is not broken after validation errors.
- [x] User receives clear feedback after successful login.
- [x] User receives clear feedback after failed login.

Notes:

```text

```

---

# 6. Login Functional Check

- [x] User can enter login credentials.
- [x] User can submit valid login form.
- [x] Frontend sends request to correct backend login endpoint.
- [x] Frontend uses correct HTTP method.
- [x] Frontend sends correct JSON request body.
- [x] Frontend handles successful login.
- [x] Frontend handles invalid credentials.
- [x] Frontend handles unknown user.
- [x] Frontend handles backend unavailable/network error.
- [x] Frontend shows loading state during login.
- [x] Loading state ends after success.
- [x] Loading state ends after error.
- [x] Submit button cannot be spam-clicked to send duplicate requests.
- [x] After successful login, user is redirected to the correct authenticated page.
- [x] After failed login, user remains on login page.
- [x] After failed login, error message is shown.
- [x] Login works after full page refresh.
- [x] Login works in a clean browser session.

Notes:

```text

```

---

# 7. Login Validation Check

- [x] Empty login form is rejected.
- [x] Empty email/username is rejected.
- [x] Empty password is rejected.
- [x] Invalid email format is rejected if email is used.
- [x] Extremely long email/username does not break the UI.
- [x] Extremely long password does not break the UI.
- [x] HTML/script input is not rendered as executable content.
- [x] Failed login message is safe.
- [x] Failed login message does not reveal unnecessary account details.
- [x] Failed login message does not reveal stack traces or backend internals.
- [x] Validation messages update/disappear when input is corrected.

Notes:

```text

```

---

# 8. Authentication State Check

- [x] Frontend stores authentication state according to project design.
- [x] Frontend stores JWT/token only if required.
- [x] Frontend does not store raw password.
- [x] Frontend does not display raw JWT/token.
- [x] Frontend does not log raw JWT/token to browser console.
- [x] Frontend does not log password to browser console.
- [x] Frontend attaches token to protected API requests after login.
- [x] Frontend does not attach token before login.
- [x] Frontend detects logged-in state correctly.
- [x] Frontend detects logged-out state correctly.
- [x] Frontend handles page refresh while logged in.
- [x] Frontend handles expired/invalid token.
- [x] Frontend blocks unauthenticated users from protected routes.
- [x] Frontend redirects unauthenticated users to login where appropriate.
- [x] Frontend shows authenticated navigation after login.
- [x] Frontend hides unauthenticated navigation after login if relevant.
- [x] Frontend logout exists if logout is part of FR-01.
- [x] Logout clears authentication state if logout is part of FR-01.
- [x] Browser back button does not expose protected content after logout.

Notes:

```text

```

---

# 9. API Integration Check

- [x] Frontend registration request matches backend `UserRegistrationDto`.
- [x] Frontend login request matches backend `UserLoginDto`.
- [x] Frontend correctly reads registration success response.
- [x] Frontend correctly reads login success response.
- [x] Frontend correctly reads JWT/token field from login response.
- [x] Frontend correctly handles backend validation errors.
- [x] Frontend correctly handles backend duplicate-user errors.
- [x] Frontend correctly handles backend authentication errors.
- [x] Frontend correctly handles backend 500 errors.
- [x] Frontend does not depend on backend internal field names that should not be public.
- [x] Frontend and backend agree on endpoint URLs.
- [x] Frontend and backend agree on HTTP methods.
- [x] Frontend and backend agree on error response format enough for V1.

Notes:

```text

```

---

# 10. Selenium Test Checklist

## Registration Selenium Tests

- [x] Selenium test: registration page loads.
- [x] Selenium test: registration form fields are visible.
- [x] Selenium test: empty registration form cannot be submitted successfully.
- [x] Selenium test: invalid email is rejected if email is used.
- [x] Selenium test: missing password is rejected.
- [x] Selenium test: short/weak password is rejected if policy exists.
- [x] Selenium test: password mismatch is rejected if confirmation exists.
- [x] Selenium test: valid registration succeeds.
- [x] Selenium test: duplicate email/username shows error.
- [x] Selenium test: successful registration redirects or shows correct success state.
- [x] Selenium test: registration handles backend error gracefully.

## Login Selenium Tests

- [x] Selenium test: login page loads.
- [x] Selenium test: login form fields are visible.
- [x] Selenium test: empty login form cannot be submitted successfully.
- [x] Selenium test: invalid login credentials show safe error.
- [x] Selenium test: valid login succeeds.
- [x] Selenium test: valid login redirects to authenticated page.
- [x] Selenium test: protected route cannot be opened before login.
- [x] Selenium test: protected route can be opened after login.
- [x] Selenium test: logout clears authentication state if logout is in scope.
- [x] Selenium test: browser refresh keeps or clears login state according to design.

## Selenium Quality

- [x] Selenium tests use stable selectors.
- [x] Important elements have stable `data-testid` attributes or equivalent.
- [x] Selenium tests do not rely on fragile CSS class names.
- [x] Selenium tests do not rely on arbitrary sleeps.
- [x] Selenium tests use explicit waits where needed.
- [x] Selenium tests use fake test accounts only.
- [x] Selenium tests can be run locally.
- [x] Selenium tests can be run in CI or have a documented temporary manual process.
- [x] Selenium test instructions are documented.

Notes:

```text

```

---

# 11. Manual Frontend Testing

- [x] Manually open registration page.
- [x] Manually register a valid fake user.
- [x] Manually try duplicate registration.
- [x] Manually try invalid registration input.
- [x] Manually open login page.
- [x] Manually log in with valid credentials.
- [x] Manually log in with invalid credentials.
- [x] Manually refresh page after login.
- [x] Manually open protected route while logged out.
- [x] Manually open protected route while logged in.
- [x] Manually test browser back button after logout if logout is in scope.
- [x] Manually inspect browser console for errors.
- [x] Manually inspect browser storage and confirm no password is stored.
- [x] Manual test evidence is attached to merge request.

Manual evidence:

```text
Tester:
Date: 2026-05-09
Browser:
Environment:
Evidence link or notes:

```

---

# 12. Frontend Security Check

- [x] Password field uses password input type.
- [x] Password is not displayed outside the password field.
- [x] Password is not stored in local storage.
- [x] Password is not stored in session storage.
- [x] Password is not logged to console.
- [x] JWT/token is not logged to console.
- [x] Sensitive values are not placed in URL query parameters.
- [x] Registration request does not send credentials through URL.
- [x] Login request does not send credentials through URL.
- [x] Frontend does not expose hidden admin-only UI to normal users.
- [x] Protected UI is not shown before authentication.
- [x] Invalid/expired token is handled safely.
- [x] No hardcoded credentials exist in frontend code.
- [x] No secret API keys are committed in frontend code.
- [x] No temporary debug panel remains.
- [x] No raw backend errors are shown to the user.

Notes:

```text

```

---

# 13. GDPR and Privacy Check

- [x] Registration form collects only necessary data.
- [x] Registration form does not collect unnecessary personal data.
- [x] Registration form does not collect sensitive personal data.
- [x] User is informed why registration data is needed, or this is documented as a project-level requirement.
- [x] Password is never shown in logs, URLs, screenshots, or visible frontend state.
- [x] Test screenshots use fake users only.
- [x] Manual QA uses fake accounts only.
- [x] Frontend does not expose another user’s personal data.
- [x] Error messages do not reveal unnecessary account details.
- [x] Privacy notice/link is present or documented as future/project-level requirement.
- [x] Data deletion/export is documented as implemented, future work, or out of scope.

Notes:

```text

```

---

# 14. Accessibility and Usability Check

- [x] Registration form can be used with keyboard only.
- [x] Login form can be used with keyboard only.
- [x] Tab order is logical.
- [x] Input fields have labels.
- [x] Buttons have clear text.
- [x] Error messages are understandable.
- [x] Error messages are visually noticeable.
- [x] Color is not the only way to communicate errors.
- [x] Text contrast is readable.
- [x] Email/username input has appropriate autocomplete if used.
- [x] Password input has appropriate autocomplete if used.
- [x] User understands what to do after successful registration.
- [x] User understands what to do after failed registration.
- [x] User understands what to do after successful login.
- [x] User understands what to do after failed login.
- [x] UI works on desktop width.
- [x] UI works on tablet/mobile width.
- [x] UI does not overflow or break on small screens.

Notes:

```text

```

---

# 15. Performance and Reliability Check

- [x] Registration page loads in acceptable time.
- [x] Login page loads in acceptable time.
- [x] Form submission gives immediate visual feedback.
- [x] UI remains responsive during API calls.
- [x] Slow backend response does not freeze the UI.
- [x] Network error does not crash the UI.
- [x] Backend 500 error does not crash the UI.
- [x] Repeated failed login attempts do not crash the UI.
- [x] Browser refresh does not break the app.
- [x] No visible frontend errors appear in browser console during normal use.
- [x] No unhandled promise errors appear in browser console.
- [x] No unnecessary repeated API calls are made.

Notes:

```text

```

---

# 16. Frontend Code Quality Check

- [x] API calls are placed in Angular service(s).
- [x] Components are not overloaded with API logic.
- [x] Form validation is readable.
- [x] Error handling is consistent.
- [x] Repeated code is avoided.
- [x] Naming is clear and consistent.
- [x] No commented-out dead code remains.
- [x] No temporary `console.log` debugging remains.
- [x] No hardcoded test users remain in production code.
- [x] No secrets are committed.
- [x] Frontend formatting/linting passes if configured.
- [x] Frontend tests are committed.
- [x] Selenium tests are committed or linked.
- [x] The implementation is maintainable for future FR-02 integration.

Notes:

```text

```

---

# 17. Frontend CI and Merge Readiness

- [x] Frontend builds successfully.
- [x] Frontend unit tests pass if available.
- [x] Selenium tests pass locally.
- [x] Selenium tests pass in CI or manual fallback is documented.
- [x] No critical browser console errors remain.
- [x] No secrets are committed.
- [x] No local config files are accidentally committed.
- [x] No real user data is committed.
- [x] Manual frontend testing is complete.
- [x] Frontend documentation/test notes are updated.
- [x] All critical frontend items are checked.
- [x] All unchecked items are documented with reason.
- [x] Frontend QA reviewer approves.

---

# Final Frontend QA Decision

Choose one:

- [x] APPROVED — frontend can be merged
- [x] REJECTED — frontend fixes required
- [x] APPROVED WITH MINOR NON-BLOCKING ISSUES

Final frontend summary:

```text
Frontend QA result: PASSED
Critical issues: None
Non-critical issues: None
Selenium evidence:
Manual test evidence:
Security/GDPR notes:
Decision: APPROVED
QA reviewer: Senior QA Engineer
Date: 2026-05-09
```
