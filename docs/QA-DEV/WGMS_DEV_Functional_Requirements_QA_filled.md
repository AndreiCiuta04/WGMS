# WGMS General Functional Requirements QA Checklist (DEV) — Filled QA Pass

**Scope:** General QA for the current `dev` state.  
**Covered functional requirements:** FR-01, FR-02, FR-03, FR-04, FR-05, FR-06, FR-07, FR-08, FR-09, FR-12, FR-13.  
**Purpose:** Verify whether each implemented functional requirement has backend, frontend, and e2e evidence, including both positive and negative tests.

---

## QA evidence rule

A checklist item is marked as done only where concrete evidence was found in backend tests, frontend tests, Cypress tests, command output reported during QA, or code locations reviewed through terminal output.

**Important QA notes from this pass:**

- FR-06 is **not complete in the current branch**. It is marked as pending because the feature owner is expected to provide implementation/evidence separately.
- FR-07 QR/catalogue-specific behavior was not fully evidenced in the current branch. The developer was notified. Basic shopping CRUD and shopping visibility are covered by FR-07/FR-08 tests.
- FR-04 frontend does **not** block `endDate < startDate`; this is backend-enforced and covered by backend tests.
- FR-12 has no dedicated backend endpoint; the dashboard composes existing task, cleaning schedule, and household/member endpoints.
- The E2E suite was reorganized into FR-specific specs and use-case journey specs. A QA coverage overview file was also added: `docs/QA-DEV/QA_TEST_COVERAGE_OVERVIEW.md`.

---

## Global functional QA checks for every covered FR

### Backend baseline

- [x] REST endpoint or service method exists for implemented requirements.
- [x] DTO/request validation exists for implemented requirements.
- [x] Service-layer business rule exists for implemented requirements.
- [x] Persistence/repository behavior is covered for implemented requirements.
- [x] Authenticated access is required where WG data is involved.
- [x] Household isolation is enforced in backend tests for WG-scoped features.
- [x] Role checks are enforced in backend where relevant.
- [x] Positive backend test exists for implemented FRs except FR-12 where dashboard composes existing endpoints.
- [x] Negative backend test exists for implemented FRs.
- [x] Edge-case backend test exists where relevant.
- [x] Tests are included in backend test suite / local test command evidence was reported.
- [ ] FR-06 backend evidence is still pending.

### Frontend baseline

- [x] UI entry point exists for implemented FRs.
- [x] Form/list/detail behavior matches intended workflows for implemented FRs.
- [x] Loading state exists where applicable.
- [x] Empty state exists where applicable.
- [x] Error state exists where applicable.
- [x] Invalid user input is handled clearly in frontend tests where applicable.
- [x] Unauthorized/forbidden actions are hidden, blocked, or backend-protected where relevant.
- [x] Frontend component/service tests exist for implemented FRs.
- [x] Frontend tests include negative cases where applicable.
- [ ] FR-06 frontend evidence is still pending.

### E2E baseline

- [x] Main happy-path workflow exists as Cypress/e2e test for implemented FRs.
- [x] At least one negative/edge e2e case exists for implemented FRs where feasible.
- [x] Tests prove data persistence after reload/navigation where relevant.
- [x] Tests prove dashboard/history/list integration where relevant.
- [x] Tests use isolated/synthetic data and unique emails/names.
- [x] Tests are documented in `docs/QA-DEV/QA_TEST_COVERAGE_OVERVIEW.md` and selectable through `run-e2e.sh`.
- [ ] FR-06 E2E evidence is still pending.

---

# FR-01 — User registration and login

## Requirement expectation

Users can register and log in securely. Authentication is required before accessing WG-specific data.

## Positive QA

### Backend

- [x] Registration endpoint accepts valid new user data.
- [x] Login endpoint accepts valid credentials.
- [x] Successful login returns a usable auth token.
- [x] Password is stored hashed, not in plain text.
- [x] Registered user can be used by later WG workflows.
- [x] Backend integration test exists for successful registration.
- [x] Backend integration test exists for successful login.

### Frontend

- [x] Registration page/form exists.
- [x] Login page/form exists.
- [x] Successful registration redirects correctly.
- [x] Successful login stores auth state/token correctly.
- [x] Authenticated navigation works after login.
- [x] Frontend tests exist for valid registration/login flow.

### E2E

- [x] Cypress/e2e test registers a user.
- [x] Cypress/e2e test logs in a user.
- [x] After login, dashboard/protected content is reachable.
- [x] Logout works.

## Negative QA

### Backend

- [x] Duplicate email/username is rejected.
- [x] Invalid email is rejected.
- [x] Missing password is rejected.
- [x] Weak/invalid password is rejected.
- [x] Wrong password is rejected.
- [x] Unknown user login is rejected.
- [x] Protected endpoints reject unauthenticated requests.
- [x] Login/token error handling avoids sensitive leakage.

### Frontend

- [x] Empty/invalid form is blocked or shows validation errors.
- [x] Invalid email shows validation error.
- [x] Wrong credentials show safe error message.
- [x] Protected route access while logged out is blocked by guard.

### E2E

- [x] Unauthenticated user is redirected/rejected from protected route.
- [x] Wrong credentials fail safely.
- [x] Duplicate registration fails safely.

## Evidence

- Backend tests:
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/UserEndpointTest.java`
    - `givenNothing_whenRegisterValidUser_thenReturns201AndSavesUser`
    - `givenExistingUser_whenRegisterSameEmail_thenReturns409`
    - `givenNothing_whenRegisterInvalidPassword_thenReturns400`
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/LoginEndpointTest.java`
    - `givenUser_whenLoginValidCredentials_thenReturns200AndToken`
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/SecurityTest.java`
    - invalid/malformed bearer token tests.
- Frontend tests:
  - [x] `frontend/src/app/components/login/login.component.spec.ts`
  - [x] `frontend/src/app/components/register/register.component.spec.ts`
  - [x] `frontend/src/app/services/auth.service.spec.ts`
  - [x] `frontend/src/app/guards/auth.guard.spec.ts`
- E2E tests:
  - [x] `e2e/cypress/e2e/fr01-auth.cy.js`
    - `FR-01 positive: registers a user, logs in, reaches dashboard, and logs out`
    - `FR-01 negative: invalid login and duplicate registration show readable errors`
    - `FR-01 negative: malformed login and weak registration are blocked before backend requests`
    - `FR-01 negative: logged-out users are redirected away from authenticated pages`
- Manual notes:
  - [x] Frontend unit tests and build were reported as passing during QA.

---

# FR-02 — Secure WG creation and invitations

## Requirement expectation

A registered user can create a WG/household. The creator can invite roommates through time-limited invitation links. Invitation and membership actions must be secure and role-aware.

## Positive QA

### Backend

- [x] Authenticated user can create a WG.
- [x] WG creator receives Main Tenant role.
- [x] Main Tenant/admin can create invitation token.
- [x] Valid invitation token allows a user to join the correct WG.
- [x] Invitation token stores validity/expiry behavior.
- [x] Membership is persisted correctly.
- [x] Backend tests exist for WG creation.
- [x] Backend tests exist for valid invitation acceptance.

### Frontend

- [x] WG creation UI exists.
- [x] Invitation creation UI exists.
- [x] Invitation acceptance/join flow exists.
- [x] Joined roommate sees WG context/member list.
- [x] Main Tenant sees member/invitation management behavior.

### E2E

- [x] User creates WG.
- [x] Main Tenant creates invitation.
- [x] Second user joins WG using invitation.
- [x] Joined user appears as household member.

## Negative QA

### Backend

- [x] Unauthenticated user cannot create WG.
- [x] Roommate/non-member cannot manage invitations where restricted.
- [x] Expired invitation token is rejected.
- [x] Invalid/random invitation token is rejected.
- [x] Used token behavior is handled.
- [x] User already in household cannot create/join conflicting household state.
- [x] User cannot escalate role through request body.
- [x] Cross-household access is rejected.

### Frontend

- [x] Invitation UI/actions are blocked or error-handled for unauthorized states.
- [x] Invalid invitation shows clear error.
- [x] Expired invitation shows clear error.
- [x] Joining failure does not leave inconsistent success state.

### E2E

- [x] Invalid token fails through UI.
- [x] Invalid email is blocked before request.
- [x] Member visibility is verified after join.

## Evidence

- Backend tests:
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/HouseholdEndpointTest.java`
    - `givenUserWithoutHousehold_whenCreateHousehold_thenReturns201AndCreatesMainTenantMembership`
    - `givenUserAlreadyInHousehold_whenCreateHousehold_thenReturns409`
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/InvitationEndpointTest.java`
    - `givenMainTenant_whenCreateAndAcceptInvitation_thenSuccess`
    - roommate/non-member forbidden tests.
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/InvitationServiceTest.java`
    - expired, used, wrong-email, already-member cases.
- Frontend tests:
  - [x] `frontend/src/app/components/household-create/household-create.component.spec.ts`
  - [x] `frontend/src/app/components/household-invitations/household-invitations.component.spec.ts`
  - [x] `frontend/src/app/components/invitation-accept/invitation-accept.component.spec.ts`
  - [x] `frontend/src/app/components/household-members/household-members.component.spec.ts`
  - [x] `frontend/src/app/services/household.service.spec.ts`
  - [x] `frontend/src/app/services/invitation.service.spec.ts`
- E2E tests:
  - [x] `e2e/cypress/e2e/fr02-household-invitations.cy.js`
  - [x] `e2e/cypress/e2e/usecases/uc01-onboarding-household.cy.js`
- Manual notes:
  - [x] FR-02 frontend unit-test gap was closed during QA.

---

# FR-03 — Task creation and assignment

## Requirement expectation

Roommates can create, assign, and track household tasks.

## Positive QA

### Backend

- [x] Authorized WG user can create a task.
- [x] Task can be assigned to roommate in same WG.
- [x] Task contains required title/description/due date/status fields.
- [x] Task status can be updated.
- [x] Task list retrieval returns only household-scoped tasks.
- [x] Backend tests exist for task creation.
- [x] Backend tests exist for task assignment.
- [x] Backend tests exist for status update.

### Frontend

- [x] Task creation UI exists.
- [x] Task assignment UI exists.
- [x] Task list displays assigned/all tasks correctly.
- [x] Task status is visible.
- [x] Task completion/update action works in UI.
- [x] Validation errors are shown for invalid task forms.

### E2E

- [x] User creates task.
- [x] User assigns task to roommate.
- [x] Assigned roommate sees task under My Tasks.
- [x] Task status update is reflected.

## Negative QA

### Backend

- [x] Unauthenticated user cannot create/read/update WG tasks.
- [x] User cannot assign task to user outside WG.
- [x] Invalid/nonexistent assignee is rejected.
- [x] Missing title/required fields are rejected.
- [x] Cross-WG task access by ID is rejected.
- [x] Unauthorized update/delete is rejected.

### Frontend

- [x] Invalid task form displays errors.
- [x] Assignee list uses current household members.
- [x] Forbidden/API errors are shown clearly.
- [x] No-household state is handled.

### E2E

- [x] Invalid task creation fails through UI.
- [x] User without WG sees readable guidance.
- [x] Cross-user task assignment lifecycle is verified.

## Evidence

- Backend tests:
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/TaskEndpointTest.java`
    - `givenHouseholdMember_whenCreateTaskForSameHouseholdMember_then201AndTaskIsHouseholdScoped`
    - `givenAssignedUser_whenUpdateTaskStatus_then200AndStatusChanges`
    - `givenBlankTitle_whenCreateTask_then400`
    - cross-household and unauthorized tests.
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/TaskRepositoryTest.java`
- Frontend tests:
  - [x] `frontend/src/app/components/task-create/task-create.component.spec.ts`
  - [x] `frontend/src/app/components/task-list/task-list.component.spec.ts`
  - [x] `frontend/src/app/services/task.service.spec.ts`
- E2E tests:
  - [x] `e2e/cypress/e2e/fr03-tasks.cy.js`
  - [x] `e2e/cypress/e2e/usecases/uc02-task-lifecycle.cy.js`
- Manual notes:
  - [x] FR-03 frontend tests were expanded during QA.

---

# FR-04 — Cleaning schedule management

## Requirement expectation

Users can create, manage, activate, and use cleaning schedules/recurring cleaning responsibilities.

## Positive QA

### Backend

- [x] Authorized WG user can create cleaning schedule.
- [x] Schedule is linked to correct WG.
- [x] Schedule contains recurrence/assignment information.
- [x] Active schedule can be retrieved.
- [x] Schedule update works.
- [x] Schedule activation/deactivation works.
- [x] Schedule can generate task.
- [x] Backend tests exist for schedule creation/update/active behavior.

### Frontend

- [x] Cleaning schedule UI exists.
- [x] User can create/edit schedule.
- [x] Active/inactive state is visually handled.
- [x] Schedule list/detail displays assignments.
- [x] Validation/errors are displayed.

### E2E

- [x] User creates cleaning schedule.
- [x] User edits/pauses/resumes schedule.
- [x] Schedule can generate task.
- [x] Generated task appears in tasks.

## Negative QA

### Backend

- [x] Unauthenticated user cannot manage schedules.
- [x] User cannot access schedule from another WG.
- [x] Invalid recurrence/assignment data is rejected.
- [x] Invalid assignee is rejected.
- [x] Empty schedule title/required fields are rejected.
- [x] Deletion after generated task is blocked where current behavior requires it.

### Frontend

- [x] Invalid form cannot be submitted or shows errors.
- [x] Unauthorized/API errors are handled.
- [x] Inactive schedules are not mislabeled as active.
- [x] Empty schedule list has usable empty state.

### E2E

- [x] Creating invalid schedule fails or is blocked.
- [x] Deleting schedule after generated task is blocked with readable feedback.
- [x] Other-household schedules do not leak.

## Evidence

- Backend tests:
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/CleaningScheduleEndpointTest.java`
    - create/update/deactivate/generate/delete tests.
    - `givenEndDateBeforeStartDate_whenCreateSchedule_then400AndNoScheduleIsCreated`
    - `givenActiveSchedule_whenGenerateTask_then200TaskCreatedAndNextDueDateAdvances`
    - `givenScheduleWithGeneratedTask_whenDeleteSchedule_then409AndScheduleAndTaskRemain`
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/CleaningScheduleRepositoryTest.java`
- Frontend tests:
  - [x] `frontend/src/app/components/cleaning-schedule-create/cleaning-schedule-create.component.spec.ts`
  - [x] `frontend/src/app/components/cleaning-schedule-list/cleaning-schedule-list.component.spec.ts`
  - [x] `frontend/src/app/services/cleaning-schedule.service.spec.ts`
- E2E tests:
  - [x] `e2e/cypress/e2e/fr04-cleaning-schedules.cy.js`
  - [x] `e2e/cypress/e2e/usecases/uc03-cleaning-schedule-to-task.cy.js`
- Manual notes:
  - [x] Frontend date-order validation is not implemented; `endDate < startDate` is backend-enforced and tested.

---

# FR-05 — Shared expense tracking

## Requirement expectation

Users can enter shared expenses, split costs, and persist participant shares correctly.

## Positive QA

### Backend

- [x] Authorized WG user can create expense.
- [x] Expense stores payer, total amount, date, description.
- [x] Expense splits are created correctly.
- [x] Equal split works.
- [x] Manual split works via split payload.
- [x] Decimal handling uses precise money logic.
- [x] Expense list returns only WG expenses.
- [x] Backend tests exist for expense creation and split calculation.

### Frontend

- [x] Expense creation UI exists.
- [x] Expense list/detail UI exists.
- [x] User can select payer/participants.
- [x] Amount input handles decimals.
- [x] Equal split preview/result is understandable.
- [x] Validation errors are shown.

### E2E

- [x] User creates shared expense.
- [x] Expense appears in list/history.
- [x] Relevant WG users can see expense behavior.
- [x] Reload/login again keeps expense data.

## Negative QA

### Backend

- [x] Unauthenticated user cannot create/read expenses.
- [x] User cannot create expense in another WG.
- [x] User cannot set payer/participant outside WG.
- [x] Negative amount is rejected.
- [x] Zero/invalid amounts are rejected according to validation.
- [x] Missing/invalid participants are rejected.
- [x] Split sum mismatch is rejected without partial persistence.

### Frontend

- [x] Invalid amount shows validation error.
- [x] Missing/invalid split participant rows are blocked.
- [x] Participants from other WGs are not selectable.
- [x] Failed expense creation shows error and does not fake success.

### E2E

- [x] Invalid expense cannot be created.
- [x] Split mismatch shows readable backend error.
- [x] Persistence after reload/login is checked.

## Evidence

- Backend tests:
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/ExpenseEndpointTest.java`
    - `givenHouseholdMember_whenCreateExpense_then201AndExpenseIsPersistedWithSplits`
    - `givenTenEuroAndThreeParticipants_whenCalculateEqualSplits_thenRemainderIsDistributedAndTotalMatches`
    - `givenInvalidAmount_whenCreateExpense_then400AndNoExpenseIsCreated`
    - `givenInvalidSplitSum_whenCreateExpense_then409AndNoPartialExpenseIsCreated`
    - cross-household payer/participant tests.
- Frontend tests:
  - [x] `frontend/src/app/components/expenses/expenses.component.spec.ts`
  - [x] `frontend/src/app/services/expense.service.spec.ts`
- E2E tests:
  - [x] `e2e/cypress/e2e/fr05-expenses.cy.js`
  - [x] `e2e/cypress/e2e/usecases/uc05-expense-history.cy.js`
- Manual notes:
  - [x] FR-05 frontend component/service tests were added during QA.
  - [x] Settlement/payment status remains FR-06 and is not claimed here.

---

# FR-06 — Payment status overview

## Requirement expectation

Users can view balances, pending payments, and settled payment status.

## QA status

**Status: PENDING / MISSING EVIDENCE.**

The person responsible for FR-06 is expected to provide the implementation and code/evidence separately. Current branch evidence is insufficient to mark this feature as complete. Expense balances may exist as part of FR-05, but that is not the same as full FR-06 payment status / settlement behavior.

## Positive QA

### Backend

- [ ] Payment/balance overview endpoint exists.  
  `MISSING EVIDENCE: FR-06 implementation/evidence not present in current branch.`
- [ ] Balance calculation uses expense and payment data correctly.  
  `MISSING EVIDENCE.`
- [ ] Pending payments are returned correctly.  
  `MISSING EVIDENCE.`
- [ ] Settled payments are returned correctly.  
  `MISSING EVIDENCE.`
- [ ] Overview is scoped to the correct WG.  
  `MISSING EVIDENCE.`
- [ ] Rounding behavior is deterministic.  
  `MISSING EVIDENCE.`
- [ ] Backend tests exist for payment overview.  
  `MISSING EVIDENCE.`
- [ ] Backend tests exist for partially settled payments.  
  `MISSING EVIDENCE.`
- [ ] Backend tests exist for zero-balance/fully settled state.  
  `MISSING EVIDENCE.`

### Frontend

- [ ] Payment overview UI exists.  
  `MISSING EVIDENCE.`
- [ ] Pending vs settled status is clearly visible.  
  `MISSING EVIDENCE.`
- [ ] Balances are formatted as currency.  
  `MISSING EVIDENCE.`
- [ ] Empty/zero-balance state is understandable.  
  `MISSING EVIDENCE.`
- [ ] Refresh/reload shows same payment state.  
  `MISSING EVIDENCE.`

### E2E

- [ ] Create expense and view payment overview through UI.  
  `MISSING EVIDENCE.`
- [ ] Mark/record payment if implemented and verify status changes.  
  `MISSING EVIDENCE.`
- [ ] Payment status persists after reload.  
  `MISSING EVIDENCE.`
- [ ] Dashboard/history integration updates where expected.  
  `MISSING EVIDENCE.`

## Negative QA

### Backend

- [ ] Unauthenticated user cannot access payment overview.  
  `MISSING EVIDENCE.`
- [ ] Cross-WG payment/balance access is rejected.  
  `MISSING EVIDENCE.`
- [ ] Invalid payment ID is rejected.  
  `MISSING EVIDENCE.`
- [ ] User cannot mark payment for another WG.  
  `MISSING EVIDENCE.`
- [ ] Overpayment/negative payment is rejected or handled.  
  `MISSING EVIDENCE.`
- [ ] Deleted/left users are displayed/anonymized safely.  
  `MISSING EVIDENCE.`

### Frontend

- [ ] API failure shows error.  
  `MISSING EVIDENCE.`
- [ ] Unauthorized payment actions are hidden/blocked.  
  `MISSING EVIDENCE.`
- [ ] Invalid payment update does not fake success.  
  `MISSING EVIDENCE.`
- [ ] Confusing negative/positive balance sign is avoided or documented.  
  `MISSING EVIDENCE.`

### E2E

- [ ] User from another WG cannot view balances.  
  `MISSING EVIDENCE.`
- [ ] Invalid payment action fails.  
  `MISSING EVIDENCE.`
- [ ] Payment overview still loads when there are no expenses.  
  `MISSING EVIDENCE.`

## Evidence

- Backend tests:
  - [ ] `MISSING EVIDENCE: pending FR-06 feature owner.`
- Frontend tests:
  - [ ] `MISSING EVIDENCE: pending FR-06 feature owner.`
- E2E tests:
  - [ ] `MISSING EVIDENCE: pending FR-06 feature owner.`
- Manual notes:
  - [x] FR-06 was explicitly deferred because the responsible developer will provide code/evidence separately.

---

# FR-07 — Shopping list management

## Requirement expectation

Users can add, view, edit, complete, and delete/manage shopping list items.

## Positive QA

### Backend

- [x] Authorized WG user can create shopping item.
- [x] Shopping item is linked to correct WG.
- [x] Shopping item list returns correct visible items.
- [x] Item can be updated.
- [x] Item can be marked bought/needed/cancelled.
- [x] Item can be deleted where allowed.
- [x] Backend tests exist for create/list/update/delete/status.

### Frontend

- [x] Shopping list UI exists.
- [x] Add item UI works.
- [x] Item status is visible.
- [x] Update/status/delete actions work.
- [x] Empty/list states exist.
- [x] Validation errors are shown.

### E2E

- [x] User adds shopping item.
- [x] Item appears in list.
- [x] Item update/status change persists.
- [x] Shared item behavior is checked.

## Negative QA

### Backend

- [x] Unauthenticated user cannot access shopping list.
- [x] User cannot access another WG’s shopping items.
- [x] Empty item name is rejected.
- [x] Invalid item ID is handled safely.
- [x] User cannot update/delete item from another WG.

### Frontend

- [x] Empty item form is invalid.
- [x] Failed API action shows error.
- [x] Forbidden actions are protected by backend and surfaced safely.

### E2E

- [x] Logged-out shopping route access is rejected/redirected.
- [x] Item state survives normal workflow changes.

## Evidence

- Backend tests:
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/ShoppingItemEndpointTest.java`
    - `givenUserInHousehold_whenCreateItem_then201AndAttachedToHousehold`
    - blank name, cross-household, delete, update/status tests.
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/ShoppingItemServiceTest.java`
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/ShoppingItemRepositoryTest.java`
- Frontend tests:
  - [x] `frontend/src/app/components/shopping-item-create/shopping-item-create.component.spec.ts`
  - [x] `frontend/src/app/components/shopping-item-list/shopping-item-list.component.spec.ts`
  - [x] `frontend/src/app/components/shopping-item-edit/shopping-item-edit.component.spec.ts`
  - [x] `frontend/src/app/services/shopping-item.service.spec.ts`
- E2E tests:
  - [x] `e2e/cypress/e2e/fr07-shopping-crud.cy.js`
- Manual notes:
  - [x] QR/catalogue-specific behavior was not fully evidenced; developer was notified. Basic CRUD is covered.
  - [x] Empty no-op backend test in `ShoppingItemServiceTest` was identified as cleanup noise; real self-target test exists elsewhere.

---

# FR-08 — Targeted shopping request collection

## Requirement expectation

Users can create/request shopping items targeted to household needs or specific roommates, depending on implemented scope.

## Positive QA

### Backend

- [x] Targeted shopping item/request behavior exists through shopping item visibility.
- [x] Authorized user can create targeted request.
- [x] Target user must belong to same WG.
- [x] Request status is persisted.
- [x] Targeted list is scoped to WG/user correctly.
- [x] Backend tests exist for valid targeted request creation/retrieval.

### Frontend

- [x] UI supports creating targeted shopping request.
- [x] User can select intended target.
- [x] Request appears in targeted list.
- [x] Status/visibility is visible.
- [x] Validation errors are clear.

### E2E

- [x] User creates targeted request.
- [x] Targeted request is visible to intended user.
- [x] Target user can mark item bought.
- [x] Targeted status/history behavior is verified.

## Negative QA

### Backend

- [x] Unauthenticated user cannot create/read requests.
- [x] User cannot target user outside WG.
- [x] Cross-WG request access is rejected.
- [x] Empty request content is rejected through shopping item validation.
- [x] Invalid/missing target is rejected.
- [x] Self-targeting is rejected.

### Frontend

- [x] Current user is filtered out of target dropdown.
- [x] Missing target is blocked.
- [x] API error is shown safely.

### E2E

- [x] Targeted cross-user workflow is tested.
- [x] Missing/self-targeting behavior is blocked or prevented.

## Evidence

- Backend tests:
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/ShoppingItemEndpointTest.java`
    - `givenUserCreatesTargetedItem_whenTargetGetsTargetedAtMe_thenItemVisible`
    - `givenTargetedItem_whenTargetMarksAsBought_thenStatusUpdates`
    - `givenCreateTargetedAtOutsideUser_then409`
    - `givenCreateTargetedAtSelf_then409`
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/ShoppingItemRepositoryTest.java`
- Frontend tests:
  - [x] `frontend/src/app/components/shopping-item-create/shopping-item-create.component.spec.ts`
  - [x] `frontend/src/app/components/shopping-item-edit/shopping-item-edit.component.spec.ts`
  - [x] `frontend/src/app/components/shopping-item-list/shopping-item-list.component.spec.ts`
  - [x] `frontend/src/app/services/shopping-item.service.spec.ts`
- E2E tests:
  - [x] `e2e/cypress/e2e/fr08-shopping-targeted.cy.js`
  - [x] `e2e/cypress/e2e/usecases/uc04-shopping-collaboration.cy.js`
- Manual notes:
  - [x] FR-08 is mostly implemented as shopping item visibility + target user flow.

---

# FR-09 — Aggregated household shopping list

## Requirement expectation

Shopping requests/items are aggregated into a household shopping list so WG members can see a combined list.

## Positive QA

### Backend

- [x] Aggregation logic exists.
- [x] Duplicate/similar items are grouped according to normalized name/quantity.
- [x] Quantities/source items are shown in aggregate DTO.
- [x] Aggregated list is scoped to WG.
- [x] Aggregated list respects status/visibility.
- [x] Backend tests exist for aggregation and multiple users/items.
- [x] Completed/removed items are excluded where intended.

### Frontend

- [x] Aggregated shopping list UI exists.
- [x] Combined items display correctly.
- [x] Quantity/source/requester information is understandable.
- [x] Shopping history UI exists for bought/cancelled items.
- [x] Empty aggregate/history states exist.

### E2E

- [x] Multiple shared items are grouped.
- [x] Bought/personal/other-household items are excluded.
- [x] History shows bought/cancelled items.
- [x] Mark-needed from history moves item back to active list.

## Negative QA

### Backend

- [x] Unauthenticated user cannot access aggregate list.
- [x] Cross-WG data is not included.
- [x] Invalid states do not break aggregation.
- [x] Duplicate handling is deterministic.

### Frontend

- [x] UI handles empty list.
- [x] UI handles API failure.
- [x] UI does not show other-WG items because backend scope is enforced.
- [x] Ambiguous duplicates are displayed consistently.

### E2E

- [x] User from another WG cannot see aggregate/history items.
- [x] Completed/needed items are filtered correctly.
- [x] Multiple updates do not create wrong duplicate display.

## Evidence

- Backend tests:
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/ShoppingItemEndpointTest.java`
    - aggregated and history endpoint tests.
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/ShoppingItemServiceTest.java`
    - aggregation/service history logic.
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/ShoppingItemRepositoryTest.java`
- Frontend tests:
  - [x] `frontend/src/app/components/shopping-item-list/shopping-item-list.component.spec.ts`
  - [x] `frontend/src/app/services/shopping-item.service.spec.ts`
    - added direct tests for `/shopping-items/aggregated` and `/shopping-items/history` without identity params.
- E2E tests:
  - [x] `e2e/cypress/e2e/fr09-shopping-aggregation-history.cy.js`
- Manual notes:
  - [x] Service-spec gap was found and fixed during QA.

---

# FR-12 — Dashboard with current responsibilities

## Requirement expectation

Dashboard shows current responsibilities and household overview information.

## Positive QA

### Backend

- [x] Dashboard composes existing documented endpoints rather than using a dedicated backend endpoint.
- [x] Task data is scoped through `TaskService`/task endpoints.
- [x] Cleaning schedule data is scoped through cleaning schedule endpoints.
- [x] Household/member data is scoped through household endpoints.
- [x] Current tasks/responsibilities are included.
- [ ] Open payments/balances are not included in current dashboard scope.  
  `NOT IMPLEMENTED IN CURRENT DASHBOARD SCOPE.`
- [ ] Shopping summary is not included in current dashboard scope.  
  `NOT IMPLEMENTED IN CURRENT DASHBOARD SCOPE.`

### Frontend

- [x] Dashboard UI exists after login.
- [x] Dashboard shows open tasks.
- [x] Dashboard shows my tasks.
- [x] Dashboard shows active schedule count/upcoming schedules.
- [x] Dashboard shows housemate count.
- [x] Dashboard has loading state.
- [x] Dashboard has empty state.
- [x] Dashboard handles API errors.

### E2E

- [x] User logs in and sees dashboard.
- [x] Creating task changes dashboard.
- [x] Creating cleaning schedule changes dashboard.
- [x] Dashboard loads correctly after navigation.

## Negative QA

### Backend

- [x] Dashboard relies on backend endpoints that reject unauthenticated/cross-WG access.
- [x] Missing optional data is handled by frontend fallback/error handling.
- [x] No dashboard backend endpoint leaks unrelated WG data.

### Frontend

- [x] Logged-out user sees landing page instead of dashboard data.
- [x] API failure does not crash dashboard.
- [x] Empty household state is not broken.
- [x] Unauthorized data is not displayed because backend scoping is used.

### E2E

- [x] Empty dashboard state is tested.
- [x] Dashboard links navigate correctly.
- [x] Direct dashboard behavior through logged-in route is tested.

## Evidence

- Backend tests:
  - [x] Covered through existing backend endpoint tests for FR-02/FR-03/FR-04.
  - [x] No dedicated dashboard backend endpoint exists.
- Frontend tests:
  - [x] `frontend/src/app/components/home/home.component.spec.ts`
    - `should calculate dashboard state from service data when logged in`
    - `should finish loading and not crash when dashboard service calls fail`
    - `should show a visible dashboard loading message while logged-in data is loading`
- E2E tests:
  - [x] `e2e/cypress/e2e/fr12-dashboard.cy.js`
  - [x] `e2e/cypress/e2e/usecases/uc06-dashboard-overview.cy.js`
- Manual notes:
  - [x] Dashboard scope is limited to tasks, cleaning schedules, and housemates.

---

# FR-13 — History of tasks and payments

## Requirement expectation

Users can view history/audit trail of completed tasks, expense/payment changes, and relevant household events where implemented.

## Positive QA

### Backend

- [x] History endpoint/service exists.
- [x] Task completion history is returned.
- [x] Cancelled task history is returned.
- [x] Generated cleaning-schedule task history is returned.
- [x] Expense/payment-related history is returned.
- [x] History entries include timestamp/responsible fields where implemented.
- [x] History retrieval is scoped to WG.
- [x] Backend tests exist for task history and payment/expense history.

### Frontend

- [x] History UI exists.
- [x] Task history is displayed.
- [x] Payment/expense history is displayed.
- [x] Timestamp/responsible user info is understandable.
- [x] Empty history state exists.
- [x] Null optional fields are handled safely.

### E2E

- [x] User completes task and sees entry in history.
- [x] User creates expense and sees payment/expense entry in history.
- [x] History remains visible after navigation/login.
- [x] Empty history state is tested.

## Negative QA

### Backend

- [x] Unauthenticated user cannot access history.
- [x] User without household cannot access history.
- [x] User cannot access another WG’s history.
- [x] Open/in-progress tasks are excluded.
- [x] Other-household tasks/expenses are excluded.
- [x] Sensitive unrelated data is not exposed.

### Frontend

- [x] Empty history does not look like loading failure.
- [x] API failure shows clear error.
- [x] Optional null fields do not render `undefined`, `NaN`, or `[object Object]`.

### E2E

- [x] Empty household history states are tested.
- [x] Sidebar navigation to history is tested.

## Evidence

- Backend tests:
  - [x] `backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/HistoryEndpointTest.java`
    - `givenNoToken_whenGetHistory_then403`
    - `givenAuthenticatedUserWithoutHousehold_whenGetHistory_then403`
    - `givenAuthenticatedHouseholdUser_whenGetHistory_then200AndHistoryArraysAreReturned`
- Frontend tests:
  - [x] `frontend/src/app/components/history/history.component.spec.ts`
  - [x] `frontend/src/app/services/history.service.spec.ts`
- E2E tests:
  - [x] `e2e/cypress/e2e/fr13-history.cy.js`
  - [x] `e2e/cypress/e2e/usecases/uc05-expense-history.cy.js`
- Manual notes:
  - [x] No extra Codex fix needed for FR-13.

---

# Cross-feature integration QA for DEV

## Registration / WG / dashboard

- [x] Newly registered user can create WG and lands in usable dashboard state.
- [x] Invited roommate joins WG and sees correct membership context.
- [x] Dashboard handles empty WG data.
- Evidence:
  - `e2e/cypress/e2e/usecases/uc01-onboarding-household.cy.js`
  - `e2e/cypress/e2e/usecases/uc06-dashboard-overview.cy.js`

## Tasks / cleaning / history / dashboard

- [x] Creating a task affects dashboard responsibilities.
- [x] Completing a task creates/appears as history entry.
- [x] Cleaning schedule generates task.
- [x] Changing cleaning schedule does not corrupt task/history flow.
- Evidence:
  - `e2e/cypress/e2e/usecases/uc02-task-lifecycle.cy.js`
  - `e2e/cypress/e2e/usecases/uc03-cleaning-schedule-to-task.cy.js`
  - `e2e/cypress/e2e/usecases/uc06-dashboard-overview.cy.js`

## Expenses / payments / history / dashboard

- [x] Creating expense works and appears in expense/history views.
- [ ] Payment status overview matches expense data.  
  `PENDING FR-06.`
- [ ] Settling/marking payment creates history entry.  
  `PENDING FR-06.`
- [ ] Dashboard payment summary matches payment overview.  
  `NOT IN CURRENT FR-12 DASHBOARD SCOPE / PENDING FR-06.`
- Evidence:
  - `e2e/cypress/e2e/usecases/uc05-expense-history.cy.js`

## Shopping / aggregation / dashboard

- [x] Shopping item/request appears in shopping list.
- [x] Targeted request appears for intended user.
- [x] Shared duplicated items appear in aggregated household list.
- [x] Completed/removed/history item handling is consistent.
- [ ] Dashboard shopping summary is not in current dashboard scope.
- Evidence:
  - `e2e/cypress/e2e/usecases/uc04-shopping-collaboration.cy.js`
  - `e2e/cypress/e2e/fr09-shopping-aggregation-history.cy.js`

## Security integration

- [x] Covered FRs reject unauthenticated API access through backend tests.
- [x] Covered FRs reject cross-WG ID tampering through backend tests.
- [x] Main Tenant-only actions are backend-protected where relevant.
- [x] Frontend route guards are not the only protection; backend tests verify authorization.
- Evidence:
  - Backend integration tests across `HouseholdEndpointTest`, `InvitationEndpointTest`, `TaskEndpointTest`, `CleaningScheduleEndpointTest`, `ExpenseEndpointTest`, `ShoppingItemEndpointTest`, `HistoryEndpointTest`.

---

# DEV readiness summary

| FR | Backend positive | Backend negative | Frontend positive | Frontend negative | E2E positive | E2E negative | CI/local evidence | Status |
|---|---|---|---|---|---|---|---|---|
| FR-01 | [x] | [x] | [x] | [x] | [x] | [x] | [x] | READY |
| FR-02 | [x] | [x] | [x] | [x] | [x] | [x] | [x] | READY |
| FR-03 | [x] | [x] | [x] | [x] | [x] | [x] | [x] | READY |
| FR-04 | [x] | [x] | [x] | [x] | [x] | [x] | [x] | READY WITH NOTE: date-order validation backend-enforced |
| FR-05 | [x] | [x] | [x] | [x] | [x] | [x] | [x] | READY |
| FR-06 | [ ] | [ ] | [ ] | [ ] | [ ] | [ ] | [ ] | PENDING FEATURE OWNER / MISSING EVIDENCE |
| FR-07 | [x] | [x] | [x] | [x] | [x] | [x] | [x] | READY WITH NOTE: QR/catalogue evidence not complete |
| FR-08 | [x] | [x] | [x] | [x] | [x] | [x] | [x] | READY |
| FR-09 | [x] | [x] | [x] | [x] | [x] | [x] | [x] | READY |
| FR-12 | [x] | [x] | [x] | [x] | [x] | [x] | [x] | READY WITH NOTE: composed frontend dashboard, no dedicated backend endpoint |
| FR-13 | [x] | [x] | [x] | [x] | [x] | [x] | [x] | READY |

---

# Commands to record as evidence

```bash
# Backend
cd backend
mvn test

# Frontend
cd frontend
npm test -- --watch=false
npm run build

# E2E
cd e2e
npx cypress run

# Selectable E2E runner from project root
./run-e2e.sh
```

Add actual command output or CI link here:

```text
BACKEND RESULT:
Reported during QA as passing / to be replaced with exact final command output or CI link.

FRONTEND RESULT:
Reported during QA as passing after test additions and builds.

E2E RESULT:
Reported during QA as passing after Cypress feature/use-case spec additions.

CI RESULT:
No CI link was provided in this chat. Use local command output or add CI pipeline URL here before final submission.
```

---

# Files added/modified during QA process worth mentioning

- `docs/QA-DEV/QA_TEST_COVERAGE_OVERVIEW.md`
- `run-e2e.sh`
- FR-specific Cypress specs:
  - `e2e/cypress/e2e/fr01-auth.cy.js`
  - `e2e/cypress/e2e/fr02-household-invitations.cy.js`
  - `e2e/cypress/e2e/fr03-tasks.cy.js`
  - `e2e/cypress/e2e/fr04-cleaning-schedules.cy.js`
  - `e2e/cypress/e2e/fr05-expenses.cy.js`
  - `e2e/cypress/e2e/fr07-shopping-crud.cy.js`
  - `e2e/cypress/e2e/fr08-shopping-targeted.cy.js`
  - `e2e/cypress/e2e/fr09-shopping-aggregation-history.cy.js`
  - `e2e/cypress/e2e/fr12-dashboard.cy.js`
  - `e2e/cypress/e2e/fr13-history.cy.js`
- Use-case journey Cypress specs:
  - `e2e/cypress/e2e/usecases/uc01-onboarding-household.cy.js`
  - `e2e/cypress/e2e/usecases/uc02-task-lifecycle.cy.js`
  - `e2e/cypress/e2e/usecases/uc03-cleaning-schedule-to-task.cy.js`
  - `e2e/cypress/e2e/usecases/uc04-shopping-collaboration.cy.js`
  - `e2e/cypress/e2e/usecases/uc05-expense-history.cy.js`
  - `e2e/cypress/e2e/usecases/uc06-dashboard-overview.cy.js`
  - `e2e/cypress/e2e/usecases/uc07-navigation-smoke.cy.js`

