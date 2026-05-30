# FR-02 Frontend QA Checklist — Secure WG Creation & Invitations

## Feature Information

- Feature ID: FR-02
- Feature name: Secure WG creation & invitations
- Use case: UC-01 — User Registration and WG Initialization
- Priority: High
- Category: Core
- Version: V1
- Frontend technology: Angular
- Automated frontend tests: Selenium or equivalent
- Backend dependency: FR-02 backend endpoints
- Branch:
- Merge request:
- Developer:
- QA reviewer:
- Date:

---

## How to Use This Checklist

This checklist must be completed before the frontend part of FR-02 can be considered done.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a short note under **QA Notes / Evidence**.
- Every checked item should be backed by one of the following:
  - an automated frontend test,
  - a manual browser test,
  - a screenshot,
  - a code review finding,
  - or a short explanation.

The goal is that after this checklist is completed, users can create a WG and invite roommates safely and clearly through the UI.

---

# 1. Scope Verification

## 1.1 Feature Scope

- [ ] The frontend provides a UI for creating a WG.
- [ ] The frontend provides a UI for generating or sending WG invitations.
- [ ] The frontend provides a UI for opening an invitation link.
- [ ] The frontend provides a UI for accepting a valid invitation.
- [ ] The frontend handles expired, invalid, revoked, or used invitations.
- [ ] The frontend correctly uses the authenticated user's JWT.
- [ ] The frontend does not expose invitation-management actions to unauthorized users.
- [ ] The frontend does not implement unrelated features from later FRs.

## 1.2 Out-of-Scope Check

- [ ] The frontend does not implement cleaning tasks as part of FR-02.
- [ ] The frontend does not implement expenses as part of FR-02.
- [ ] The frontend does not implement shopping lists as part of FR-02.
- [ ] The frontend does not implement notifications unless required for invitations.
- [ ] The frontend does not fake backend security through UI-only checks.

---

# 2. Routing and Navigation

## 2.1 WG Creation Route

Expected route example:

```text
/wg/create
```

- [ ] Route exists.
- [ ] Route is accessible after login.
- [ ] Route is not accessible before login.
- [ ] Unauthenticated users are redirected to login.
- [ ] Authenticated users without a WG are guided to create one.
- [ ] Authenticated users with an existing WG are handled according to project rules.
- [ ] Route title/page heading is clear.
- [ ] Browser refresh on the route does not break the page.
- [ ] Direct URL navigation works correctly.

## 2.2 Invitation Management Route

Expected route example:

```text
/wg/invitations
```

- [ ] Route exists if invitation management has a dedicated page.
- [ ] Route is accessible to authorized users.
- [ ] Route is not accessible to unauthenticated users.
- [ ] Route is not accessible to unauthorized WG members if only main tenant/admin may invite.
- [ ] Route shows the current WG context.
- [ ] Route does not show invitation controls for users without permission.
- [ ] Browser refresh on the route does not break the page.

## 2.3 Invitation Acceptance Route

Expected route example:

```text
/invite/:token
```

- [ ] Route exists.
- [ ] Route can be opened from a copied invitation link.
- [ ] Route extracts token correctly from the URL.
- [ ] Route handles missing token.
- [ ] Route handles malformed token.
- [ ] Route validates invitation with backend if preview endpoint exists.
- [ ] Route shows correct WG invitation information for valid tokens.
- [ ] Route shows a clear error for invalid tokens.
- [ ] Route shows a clear error for expired invitations.
- [ ] Route shows a clear error for revoked invitations.
- [ ] Route shows a clear error for already-used invitations.
- [ ] Route redirects unauthenticated users to login/register if login is required before accepting.
- [ ] After login/register, the user can continue accepting the original invitation.
- [ ] Browser refresh on the route does not lose the invitation token.

---

# 3. WG Creation UI

## 3.1 Form Fields

- [ ] WG creation form exists.
- [ ] WG name field exists.
- [ ] WG name field has a clear label.
- [ ] WG name field has a clear placeholder or helper text.
- [ ] WG name field is required.
- [ ] Submit button exists.
- [ ] Cancel/back navigation exists if appropriate.
- [ ] Form layout is understandable on desktop.
- [ ] Form layout is understandable on mobile.

## 3.2 Client-Side Validation

- [ ] Empty WG name is rejected before submit.
- [ ] Blank-only WG name is rejected before submit.
- [ ] Too-long WG name is rejected before submit if max length is known.
- [ ] Validation message is shown near the field.
- [ ] Validation message is understandable.
- [ ] Submit button is disabled or guarded while form is invalid.
- [ ] Leading/trailing spaces are handled consistently.
- [ ] Special characters do not break the UI.
- [ ] HTML/script input is displayed safely if echoed back.

## 3.3 Successful WG Creation

- [ ] Submitting valid WG data calls the correct backend endpoint.
- [ ] Request includes authentication token.
- [ ] Loading state appears while request is pending.
- [ ] Submit button is protected against double-click duplicate submission.
- [ ] Success message appears after successful WG creation.
- [ ] User is redirected to the expected page after success.
- [ ] App state updates so the user is treated as belonging to the new WG.
- [ ] Creator role-dependent UI updates correctly.
- [ ] Page does not require full browser refresh to show the created WG.

## 3.4 Failed WG Creation

- [ ] Backend validation errors are displayed clearly.
- [ ] Authentication errors redirect to login or show a proper message.
- [ ] Authorization errors are displayed clearly.
- [ ] Network errors are displayed clearly.
- [ ] Server errors are displayed safely.
- [ ] Form data is not lost unnecessarily after recoverable errors.
- [ ] User can retry after an error.
- [ ] No raw technical stack traces are displayed.

---

# 4. Invitation Creation UI

## 4.1 Invitation Form / Controls

- [ ] Invitation creation UI exists.
- [ ] Authorized users can access invitation creation UI.
- [ ] Unauthorized users cannot access invitation creation UI.
- [ ] UI clearly explains that an invitation link lets someone join the WG.
- [ ] Email input exists if email-based invitation is implemented.
- [ ] Email input has clear validation if implemented.
- [ ] Role selection is not shown unless the backend supports safe role selection.
- [ ] Expiry information is shown if relevant.
- [ ] Create invitation button exists.
- [ ] Cancel/back navigation exists if appropriate.

## 4.2 Client-Side Invitation Validation

- [ ] Empty email is rejected if email is required.
- [ ] Invalid email format is rejected if email is required.
- [ ] Duplicate invite warnings are shown if backend provides them.
- [ ] The form does not allow users to create invitations for arbitrary WG IDs.
- [ ] The form does not expose hidden fields that allow role escalation.
- [ ] The form does not rely on frontend-only role checks for security.

## 4.3 Successful Invitation Creation

- [ ] Creating an invitation calls the correct backend endpoint.
- [ ] Request includes authentication token.
- [ ] Request targets the authenticated user's WG.
- [ ] Loading state appears while request is pending.
- [ ] Double-clicking the button does not create accidental duplicate UI confusion.
- [ ] Success message appears.
- [ ] Invitation link is shown if the backend returns a link.
- [ ] Invitation link can be copied.
- [ ] Copy-to-clipboard works.
- [ ] A visible confirmation appears after copying.
- [ ] Invitation expiry is displayed if returned by backend.
- [ ] Created invitation appears in invitation list if invitation list exists.
- [ ] UI does not expose sensitive internal fields.

## 4.4 Failed Invitation Creation

- [ ] Unauthorized error is handled.
- [ ] Forbidden error is handled.
- [ ] Invalid WG error is handled.
- [ ] Invalid email error is handled if relevant.
- [ ] Duplicate invitation error is handled if backend returns it.
- [ ] Network error is handled.
- [ ] Server error is handled safely.
- [ ] User can retry after recoverable errors.
- [ ] No raw stack traces or internal backend messages are displayed.

---

# 5. Invitation Acceptance UI

## 5.1 Opening Invitation Link

- [ ] Invitation link opens the frontend app.
- [ ] Token is read correctly from the URL.
- [ ] Page shows loading state while validating token.
- [ ] Page shows WG name or safe invitation preview for valid invitation.
- [ ] Page does not show private WG data.
- [ ] Page handles refresh correctly.
- [ ] Page handles copy-pasted links correctly.
- [ ] Page handles unknown/malformed token correctly.

## 5.2 Accepting as Logged-In User

- [ ] Logged-in user can accept a valid invitation.
- [ ] Accept button calls the correct backend endpoint.
- [ ] Request includes authentication token.
- [ ] Loading state appears while accepting.
- [ ] Double-clicking accept does not cause confusing duplicate actions.
- [ ] Success message appears.
- [ ] User is redirected to the WG/dashboard after accepting.
- [ ] App state updates so the user belongs to the WG.
- [ ] User receives roommate-level UI, not main-tenant/admin UI.
- [ ] Invitation cannot be accepted again if single-use.

## 5.3 Accepting as Logged-Out User

- [ ] Logged-out user opening invitation sees login/register guidance.
- [ ] Original invitation token is preserved during login/register.
- [ ] After successful login, user can continue accepting the invitation.
- [ ] After successful registration, user can continue accepting the invitation.
- [ ] User is not redirected to an unrelated page that loses the invite.
- [ ] If the invitation expires during login/register, user sees a clear message.

## 5.4 Invalid Invitation States

- [ ] Invalid token page shows clear error.
- [ ] Expired invitation page shows clear error.
- [ ] Revoked invitation page shows clear error.
- [ ] Already-used invitation page shows clear error.
- [ ] Invitation for deleted/inactive WG shows clear error if applicable.
- [ ] User already in WG receives clear message.
- [ ] User already in another WG receives clear message according to project rules.
- [ ] Error screen has a clear next action, for example return to dashboard/login.
- [ ] UI does not expose raw token details unnecessarily.

---

# 6. Authentication and Authorization UX

## 6.1 Authentication State

- [ ] Frontend correctly detects logged-in state.
- [ ] Frontend correctly detects logged-out state.
- [ ] JWT is attached to FR-02 API requests.
- [ ] Expired JWT is handled gracefully.
- [ ] User is redirected to login when needed.
- [ ] After login, user returns to intended invitation flow if relevant.
- [ ] Logout clears WG/invitation-related local state where necessary.

## 6.2 Role-Based UI

- [ ] Main tenant/admin sees invitation controls.
- [ ] Normal roommate does not see invitation controls if not allowed to invite.
- [ ] Unauthorized users cannot access invite page by typing URL directly.
- [ ] UI updates after role/membership changes.
- [ ] UI does not display admin/main-tenant actions to newly invited roommates.
- [ ] UI does not rely on hidden buttons as the only security measure.

---

# 7. API Integration

## 7.1 Service Layer

- [ ] Angular service exists for WG creation.
- [ ] Angular service exists for invitation creation.
- [ ] Angular service exists for invitation validation if backend supports it.
- [ ] Angular service exists for invitation acceptance.
- [ ] Services use environment-based API URL.
- [ ] Services do not hard-code production/local URLs.
- [ ] Services use typed request DTOs/interfaces.
- [ ] Services use typed response DTOs/interfaces.
- [ ] Services handle errors consistently.
- [ ] Services do not duplicate unnecessary logic across components.

## 7.2 HTTP Interceptor

- [ ] Auth token is added to protected requests.
- [ ] Token is not added to unrelated external requests.
- [ ] `401` is handled consistently.
- [ ] `403` is handled consistently.
- [ ] Network errors are handled consistently.
- [ ] Loading/error state is not left hanging after failed requests.

## 7.3 Data Consistency

- [ ] Frontend state updates after WG creation.
- [ ] Frontend state updates after invitation acceptance.
- [ ] User membership/WG context is refreshed after accepting invitation.
- [ ] UI does not show stale “no WG” state after successful WG creation.
- [ ] UI does not show stale “not member” state after successful invitation acceptance.
- [ ] Browser refresh after success still shows correct WG state.

---

# 8. Automated Frontend Tests

## 8.1 Component Tests

- [ ] Component test: WG creation form renders.
- [ ] Component test: invalid WG name shows validation error.
- [ ] Component test: valid WG name enables submit.
- [ ] Component test: successful WG creation shows success/redirect behavior.
- [ ] Component test: failed WG creation shows error.
- [ ] Component test: invitation creation UI renders for authorized user.
- [ ] Component test: invitation creation UI is hidden/blocked for unauthorized user.
- [ ] Component test: copy invitation link button works if implemented.
- [ ] Component test: invitation acceptance page handles valid invitation.
- [ ] Component test: invitation acceptance page handles invalid invitation.
- [ ] Component test: invitation acceptance page handles expired invitation.
- [ ] Component test: invitation acceptance page preserves token through login flow if implemented.

## 8.2 Service Tests

- [ ] Service test: WG creation sends correct HTTP method and URL.
- [ ] Service test: WG creation sends correct request body.
- [ ] Service test: invitation creation sends correct HTTP method and URL.
- [ ] Service test: invitation validation sends correct HTTP method and URL.
- [ ] Service test: invitation acceptance sends correct HTTP method and URL.
- [ ] Service test: errors are propagated or mapped correctly.
- [ ] Service test: DTO interfaces match backend contract.

## 8.3 Selenium / E2E Tests

Recommended E2E flow:

1. Register/login as User A.
2. Create WG.
3. Generate invitation link.
4. Logout.
5. Register/login as User B.
6. Open invitation link.
7. Accept invitation.
8. Verify User B is inside the WG as roommate.

- [ ] E2E test: authenticated user can create WG.
- [ ] E2E test: unauthenticated user cannot access WG creation page.
- [ ] E2E test: WG creator can generate invitation.
- [ ] E2E test: invitation link can be copied or retrieved.
- [ ] E2E test: invited user can open invitation link.
- [ ] E2E test: invited user can accept invitation.
- [ ] E2E test: invited user lands in correct WG after accepting.
- [ ] E2E test: expired/invalid invitation shows error.
- [ ] E2E test: unauthorized user cannot access invitation-management page.
- [ ] E2E test: browser refresh does not break WG/invitation state.
- [ ] E2E test: mobile viewport still allows WG creation and invitation acceptance.

---

# 9. Manual Browser Testing

## 9.1 Happy Path

- [ ] Login as User A.
- [ ] Create a WG with valid name.
- [ ] Confirm success message/redirect.
- [ ] Confirm User A sees WG context.
- [ ] Confirm User A sees invitation controls.
- [ ] Create invitation.
- [ ] Copy invitation link.
- [ ] Logout User A.
- [ ] Open invitation link as User B or in private browser.
- [ ] Login/register as User B.
- [ ] Accept invitation.
- [ ] Confirm User B enters correct WG.
- [ ] Confirm User B has roommate-level UI.
- [ ] Confirm User B does not see unauthorized admin controls.

## 9.2 Negative Path

- [ ] Try creating WG while logged out.
- [ ] Try submitting empty WG name.
- [ ] Try submitting blank-only WG name.
- [ ] Try submitting very long WG name.
- [ ] Try creating invitation while logged out.
- [ ] Try accessing invitation page as unauthorized user.
- [ ] Try accepting invalid invitation link.
- [ ] Try accepting expired invitation link.
- [ ] Try accepting already-used invitation link.
- [ ] Try refreshing during invitation acceptance.
- [ ] Try browser back button after accepting invitation.
- [ ] Try double-clicking WG create button.
- [ ] Try double-clicking invitation accept button.

---

# 10. UI / UX Quality

## 10.1 Clarity

- [ ] User understands what a WG is in the creation flow.
- [ ] User understands what happens after creating a WG.
- [ ] User understands that invitation links allow others to join.
- [ ] User understands whether the invitation expires.
- [ ] User understands what to do when an invitation is invalid.
- [ ] User understands what to do when not logged in.
- [ ] Success messages are clear.
- [ ] Error messages are clear.
- [ ] Button labels are clear.

## 10.2 Responsiveness

- [ ] WG creation page works on desktop.
- [ ] WG creation page works on tablet width.
- [ ] WG creation page works on mobile width.
- [ ] Invitation page works on desktop.
- [ ] Invitation page works on tablet width.
- [ ] Invitation page works on mobile width.
- [ ] Buttons are usable on mobile.
- [ ] Long WG names do not break layout.
- [ ] Long invitation links do not break layout.

## 10.3 Accessibility

- [ ] Form fields have labels.
- [ ] Buttons have accessible names.
- [ ] Error messages are associated with the relevant fields where possible.
- [ ] Keyboard navigation works.
- [ ] Focus state is visible.
- [ ] Page can be used without mouse.
- [ ] Color is not the only way errors are communicated.
- [ ] Text contrast is readable.
- [ ] Loading states are understandable.
- [ ] Success/error messages are screen-reader friendly where possible.

---

# 11. Frontend Security Checks

- [ ] JWT is not exposed in visible UI.
- [ ] Invitation token is not unnecessarily stored in long-term local storage.
- [ ] Invitation token is not printed to console.
- [ ] Invitation token is not sent to unrelated endpoints.
- [ ] Frontend does not expose hidden admin role controls.
- [ ] Frontend does not trust client-side role changes.
- [ ] User-controlled WG names are escaped in UI.
- [ ] User-controlled invitation data is escaped in UI.
- [ ] No obvious XSS through WG name display.
- [ ] No sensitive data appears in browser console logs.
- [ ] No sensitive data appears in frontend error messages.

---

# 12. Non-Functional Frontend Requirements

## 12.1 Reliability

- [ ] Page reload does not destroy valid app state.
- [ ] API errors do not leave UI stuck in loading state.
- [ ] Duplicate submits are prevented or handled.
- [ ] Slow network state is handled.
- [ ] Offline/network failure state is handled.
- [ ] User can retry failed actions.

## 12.2 Maintainability

- [ ] Components are not overly large.
- [ ] API logic is in services, not duplicated in components.
- [ ] Route guards are used where appropriate.
- [ ] Form validation logic is readable.
- [ ] Error handling is consistent.
- [ ] Naming follows project conventions.
- [ ] No debug console logs remain.
- [ ] No unused imports remain.
- [ ] No commented-out experimental code remains.

## 12.3 Performance

- [ ] WG creation page loads quickly.
- [ ] Invitation page loads quickly.
- [ ] No unnecessary repeated API calls occur on page load.
- [ ] No infinite request loops occur.
- [ ] Loading indicators appear for slow requests.
- [ ] Large or long strings do not freeze the UI.

---

# 13. Code Coverage Expectations

This checklist is intended to produce the following code-oriented coverage once the tests are implemented and measured.

## 13.1 Frontend Code Areas That Should Be Covered

- [ ] WG creation component template and TypeScript behavior.
- [ ] WG creation form validation.
- [ ] Invitation creation component template and TypeScript behavior.
- [ ] Invitation acceptance component template and TypeScript behavior.
- [ ] Angular service method for WG creation.
- [ ] Angular service method for invitation creation.
- [ ] Angular service method for invitation validation if implemented.
- [ ] Angular service method for invitation acceptance.
- [ ] Route guard for authenticated routes.
- [ ] Role/permission guard for invitation-management routes if implemented.
- [ ] HTTP interceptor behavior for attaching JWT.
- [ ] Error mapping for `400`, `401`, `403`, `404`, and network errors.
- [ ] State update after WG creation.
- [ ] State update after invitation acceptance.
- [ ] Copy-to-clipboard helper if implemented.

## 13.2 Recommended Coverage Targets

- [ ] Component TypeScript line coverage target: at least 75%.
- [ ] Service line coverage target: at least 85%.
- [ ] Route guard / interceptor line coverage target: at least 80%.
- [ ] Branch coverage target for validation and error states: at least 70%.
- [ ] E2E behavioral coverage for happy path and critical failure paths.
- [ ] Critical invitation flow paths: valid, invalid, expired, logged-out, already-used, unauthorized.

---

# 14. Definition of Done — Frontend

The frontend part of FR-02 is done only if all of the following are true:

- [ ] Authenticated users can create WGs through the UI.
- [ ] WG creation form validates input.
- [ ] WG creation success and error states are handled.
- [ ] Authorized users can create invitations through the UI.
- [ ] Invitation link can be copied or otherwise used.
- [ ] Invitation acceptance page works for valid links.
- [ ] Invalid, expired, revoked, and used invitation states are handled clearly.
- [ ] Logged-out invitation flow works or clearly redirects to login/register.
- [ ] User state updates after WG creation.
- [ ] User state updates after invitation acceptance.
- [ ] Unauthorized users do not see or access invitation controls.
- [ ] Main flows are covered by automated frontend or E2E tests.
- [ ] Manual browser testing was completed.
- [ ] UI works on desktop and mobile.
- [ ] Accessibility basics are checked.
- [ ] CI pipeline passes.
- [ ] QA reviewer has checked this file.

---

# 15. QA Notes / Evidence

Use this section to record proof.

## Automated Test Evidence

- Test command:
- Result:
- Relevant test files:
- Coverage tool:
- Line coverage:
- Branch coverage:

## Manual Browser Evidence

- Browsers tested:
- Viewports tested:
- Result:

## Screenshots

- Screenshot 1:
- Screenshot 2:
- Screenshot 3:

## Open Issues

- Issue 1:
- Issue 2:
- Issue 3:

## Final QA Decision

- [ ] Passed
- [ ] Failed
- [ ] Passed with minor notes

QA reviewer:

Date:
