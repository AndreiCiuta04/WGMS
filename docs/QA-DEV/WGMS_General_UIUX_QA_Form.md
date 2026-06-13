# WGMS — General UI/UX Redesign QA Form

## Scope

This QA form checks the general UI/UX redesign of WGMS after the visual and interaction redesign.

The goal is **not** to retest every backend business rule from scratch.  
The goal is to verify that the redesigned interface still supports the same functional workflows as before, still satisfies the relevant non-functional requirements, and that the existing automated tests were **adapted to the new UI without weakening what they check**.

This QA form focuses on:

- preservation of existing functional behavior after the UI/UX redesign
- navigation, layout, routing, and responsive behavior
- form usability and validation feedback
- stable selectors and adapted frontend/E2E tests
- accessibility/usability checks according to the project NFRs
- confirmation that tests still cover the same user-facing requirements as before
- regression checks for core WGMS workflows

---

# UIUX-01 — Redesign Preserves Existing Functional Workflows

## Requirement

The UI/UX redesign must not remove or weaken existing WGMS functionality. Users must still be able to complete the same core workflows through the redesigned interface.

## Checklist

- [ ] Registration and login are still reachable and usable.
- [ ] WG creation and invitation flows are still reachable and usable.
- [ ] Task creation, assignment, completion, undo, filtering/sorting where implemented, and recurrence remain usable.
- [ ] Cleaning schedule creation, activation, update, and status filtering remain usable.
- [ ] Shared expense creation, display, payment status, and settlement flows remain usable.
- [ ] Shopping list management still supports adding items, visibility handling, mark/unmark bought, aggregation, and history where implemented.
- [ ] Dashboard/current responsibilities still display the expected user information.
- [ ] History views still display task/payment/shopping/expense history where implemented.
- [ ] Legal/GDPR/profile pages remain reachable after the redesign.
- [ ] No core route was removed without replacement.
- [ ] No user-facing action became hidden in a way that blocks normal use.

## Test coverage evidence

- [ ] Existing frontend tests were updated to the redesigned DOM/components.
- [ ] Existing Cypress flows were updated to the redesigned navigation/selectors.
- [ ] Test assertions still check the same functional outcomes, not only that the new page renders.
- [ ] No test was deleted only because the selector/layout changed.
- [ ] Any removed test has an equivalent replacement test.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Frontend files reviewed:
- E2E files reviewed:
- Tests adapted, not weakened:
- Remaining issues:
```

---

# UIUX-02 — Navigation, Routing, and User Orientation

## Requirement

The redesigned UI must make the main WGMS areas discoverable and must preserve route behavior across refresh, direct navigation, and login/logout transitions.

## Checklist

- [ ] Main navigation/header/sidebar/bottom-tab structure is consistent across pages.
- [ ] Navigation labels are understandable to a peer tester.
- [ ] Active/current page state is visible.
- [ ] Protected pages redirect unauthenticated users to login.
- [ ] Public pages such as login/register/legal remain reachable without authentication.
- [ ] Direct URL access works for core routes.
- [ ] Refreshing a protected page after login does not break the UI.
- [ ] Logout clears the visible user state and redirects safely.
- [ ] Back/forward browser navigation does not create broken states.
- [ ] Mobile navigation does not hide required actions.

## Frontend evidence

- [ ] Routing tests or component tests cover navigation links where reasonable.
- [ ] Header/sidebar/bottom-tab tests were updated if the DOM changed.
- [ ] Auth guard behavior still passes.

## E2E evidence

- [ ] Cypress covers login -> navigate through core pages -> logout.
- [ ] Cypress uses stable selectors such as `data-cy`, not fragile visual text/classes where possible.
- [ ] E2E route checks still assert actual functionality after navigation.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Navigation components checked:
- Route tests checked:
- E2E navigation specs:
- Remaining issues:
```

---

# UIUX-03 — Forms, Validation, and Error Feedback

## Requirement

The redesigned forms must remain usable, validate input clearly, and display backend/frontend errors in a way the user can understand.

## Checklist

- [ ] Registration form still validates required fields and Terms/Privacy acknowledgement.
- [ ] Login form still handles invalid credentials safely.
- [ ] Task forms still validate title, assignment, status, due date, recurrence where applicable.
- [ ] Cleaning schedule forms still validate recurrence and required schedule fields.
- [ ] Expense forms still validate amount, participants, description, duplicate/conflicting data where applicable.
- [ ] Shopping item forms still validate name, quantity/unit/visibility/target where applicable.
- [ ] Buttons are disabled or validation messages are shown for invalid forms.
- [ ] Backend validation errors are surfaced clearly where possible.
- [ ] Generic errors are not misleading.
- [ ] Loading/saving states are visible for important actions.
- [ ] Successful actions give clear feedback or update the view immediately.

## Test coverage evidence

- [ ] Frontend component tests still cover invalid and valid form states.
- [ ] Cypress still covers at least the most important happy paths and one negative path per major workflow.
- [ ] Tests were updated for changed markup/selectors but still check submitted payloads or resulting state where possible.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Form components checked:
- Validation tests checked:
- E2E specs checked:
- Remaining issues:
```

---

# UIUX-04 — Responsiveness and Reference Platform Compatibility

## Requirement

The redesigned UI must remain usable on the project reference platforms and viewport targets.

## Checklist

- [ ] Desktop layout is usable at 1366 × 768 and higher.
- [ ] Small mobile boundary remains usable at minimum 360 CSS px width.
- [ ] iPhone 12 Pro target viewport around 390 × 844 CSS px is usable.
- [ ] Google Pixel 7 target viewport around 412 × 915 CSS px is usable.
- [ ] Important buttons are not cut off on mobile.
- [ ] Forms do not overflow horizontally.
- [ ] Tables/lists/cards remain readable on mobile.
- [ ] Header/sidebar/bottom-tab behavior works on mobile and desktop.
- [ ] Modals/dropdowns/toasts are readable and dismissible.
- [ ] Page content remains usable without pixel-perfect support for out-of-scope devices.

## Manual/reference-device evidence

- [ ] Desktop browser smoke test completed.
- [ ] Chrome mobile viewport smoke test completed.
- [ ] Safari/iOS or simulated iPhone viewport smoke test completed if available.
- [ ] Android/Pixel or simulated Pixel viewport smoke test completed if available.

## E2E evidence

- [ ] Cypress viewport smoke tests exist for at least one desktop and one mobile-sized viewport, or manual reference-device evidence is documented.
- [ ] E2E tests do not rely on desktop-only interactions if the workflow should work on mobile.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Desktop viewport tested:
- Mobile viewports tested:
- Cypress viewport specs:
- Remaining issues:
```

---

# UIUX-05 — Accessibility and Basic Usability

## Requirement

The redesigned UI should be understandable without external instructions and should satisfy basic accessibility/usability expectations for the university demo scope.

## Checklist

- [ ] Main workflows can be completed by a peer tester without explanation.
- [ ] Buttons and links have clear labels.
- [ ] Inputs have visible labels or accessible names.
- [ ] Error messages are associated with the relevant form/action.
- [ ] Color is not the only way to understand important status.
- [ ] Text contrast is acceptable in normal use.
- [ ] Keyboard navigation works for main forms/actions.
- [ ] Focus states are visible enough.
- [ ] Icons-only actions have labels/tooltips/accessible names.
- [ ] Empty states explain what the user can do next.
- [ ] Success/error/loading states are consistent across pages.

## Evidence

- [ ] Peer tester completed registration/login + one core WG workflow without external instructions.
- [ ] Lighthouse or WAI-style checklist was run for representative pages.
- [ ] Accessibility issues found by the redesign were documented or fixed.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Peer tester:
- Pages checked:
- Lighthouse/WAI result:
- Remaining issues:
```

---

# UIUX-06 — Test Adaptation Quality After Redesign

## Requirement

The UI/UX redesign may require tests to change selectors, navigation steps, or component expectations. However, the tests must still verify the **same functional requirements** as before.

## Checklist

- [ ] Test changes are mostly selector/navigation adaptations, not semantic weakening.
- [ ] Old assertions that checked real behavior were preserved or replaced by equivalent assertions.
- [ ] Tests still check backend-visible effects where relevant, such as created task/expense/shopping item/profile update.
- [ ] Tests still check frontend-visible effects after reload where relevant.
- [ ] Tests still include negative cases where they existed before.
- [ ] Tests still use unique generated data for e2e flows.
- [ ] Tests do not depend on execution order.
- [ ] Tests do not depend on manually seeded browser state unless explicitly documented.
- [ ] Stable selectors were added where needed, preferably `data-cy`.
- [ ] No brittle CSS-class-only selectors were introduced for important e2e flows.
- [ ] No failing test was simply removed to make CI green.
- [ ] If a test was deleted, the reason and replacement coverage are documented.

## Required comparison table

| Previous test / scenario | New adapted test / scenario | Same requirement still checked? | Notes |
|---|---|---:|---|
| Registration/login |  | [ ] Yes [ ] No |  |
| WG creation/invitation |  | [ ] Yes [ ] No |  |
| Task flow |  | [ ] Yes [ ] No |  |
| Cleaning schedule flow |  | [ ] Yes [ ] No |  |
| Expense/payment flow |  | [ ] Yes [ ] No |  |
| Shopping flow |  | [ ] Yes [ ] No |  |
| Dashboard/history flow |  | [ ] Yes [ ] No |  |
| GDPR/legal/profile flow |  | [ ] Yes [ ] No |  |

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Changed tests reviewed:
- Deleted tests/replacements:
- Selector adaptations:
- Remaining issues:
```

---

# UIUX-07 — Regression Coverage for Core Workflows

## Requirement

Because the redesign touches shared layout/components, it must be proven that existing feature flows still work together.

## Minimum automated coverage expected

### Backend

- [ ] Backend tests still pass unchanged or with only necessary test-data/schema updates.
- [ ] No backend behavior was changed only to satisfy the redesigned frontend.
- [ ] Security/authorization tests still pass.

Command:

```bash
cd backend
./mvnw test
```

### Frontend

- [ ] Component/service tests pass.
- [ ] Tests cover redesigned shared layout components where relevant.
- [ ] Tests cover important forms after the redesign.
- [ ] Build passes without template/type errors.

Command:

```bash
cd frontend
npm test -- --watch=false
npm run build
```

### E2E

- [ ] Cypress suite covers representative full user flows.
- [ ] E2E tests run in headless mode.
- [ ] E2E tests are pipeline-stable.
- [ ] E2E tests cover at least registration/login, one WG workflow, one task/cleaning workflow, one expense/payment workflow, one shopping workflow, and one dashboard/history/legal/profile route where implemented.

Command:

```bash
cd e2e
npx cypress run
```

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Backend command/result:
- Frontend command/result:
- E2E command/result:
- Pipeline result:
- Remaining issues:
```

---

# UIUX-08 — Non-Functional Requirements Summary

## Requirement

The UI/UX redesign must still satisfy the relevant project NFRs without duplicating all feature-specific QA forms.

| NFR | UI/UX-specific check | Status | Evidence |
|---|---|---|---|
| NFR-01 Security & Access Control | Protected pages/actions still require login and backend authorization; UI hiding is not treated as security. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-02 Data Privacy & GDPR | Profile/legal/privacy/data export pages remain reachable and do not expose sensitive data. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-03 Reliability | Refresh/restart/repeated navigation does not break core pages; CI/e2e stays stable. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-04 Usability & Compatibility | Peer tester can complete main workflows; reference viewports remain usable; WAI/Lighthouse checked. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-05 Maintainability | Redesign uses shared components/styles cleanly; no excessive duplication; tests remain readable. | [ ] Pass [ ] Partial [ ] Fail |  |
| NFR-06 Auditability | UI changes do not remove visible timestamps/status/responsible-user information for critical actions where implemented. | [ ] Pass [ ] Partial [ ] Fail |  |

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence generated:
- Security/privacy checks:
- Reliability checks:
- Usability/accessibility checks:
- Maintainability checks:
- Auditability checks:
```

---

# Final UI/UX QA Summary

| Area | Status | Notes |
|---|---|---|
| Existing functionality preserved | [ ] Pass [ ] Partial [ ] Fail |  |
| Navigation/routing works | [ ] Pass [ ] Partial [ ] Fail |  |
| Forms and validation usable | [ ] Pass [ ] Partial [ ] Fail |  |
| Responsive/reference platforms checked | [ ] Pass [ ] Partial [ ] Fail |  |
| Accessibility/basic usability checked | [ ] Pass [ ] Partial [ ] Fail |  |
| Tests adapted without weakening coverage | [ ] Pass [ ] Partial [ ] Fail |  |
| Backend/frontend/e2e regression coverage sufficient | [ ] Pass [ ] Partial [ ] Fail |  |
| NFRs still satisfied | [ ] Pass [ ] Partial [ ] Fail |  |

## Overall result

- [ ] UI/UX redesign QA passed
- [ ] UI/UX redesign QA partially passed with documented limitations
- [ ] UI/UX redesign QA failed / blocking issues remain

## Remaining limitations

```text
- 
```

## Evidence links

```text
Backend tests:
- 

Frontend tests:
- 

E2E tests:
- 

Manual/reference-device checks:
- 

GitLab issues / merge requests:
- 
```

## QA sign-off

```text
Reviewer:
Date:
Branch / commit:
Result:
```
