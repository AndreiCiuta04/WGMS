# WGMS — FR10 QA Form: Shopping Item Suggestion / Voting

## Scope

This QA form checks **FR10 — Shopping Item Suggestion/Voting**.

FR10 is an extended UC-04 feature. It must improve collaborative shopping without destabilizing the existing shopping list, targeted shopping requests, aggregated household shopping list, and shopping history.

This QA form intentionally avoids repeating the full general QA/GDPR checklist. Shared requirements such as authentication, household isolation, GDPR-safe data handling, reliability, usability, maintainability, and auditability are checked once in the NFR section below.

---

# FR10-01 — Suggestion Display and Source

## Requirement

Users should receive useful shopping item suggestions based on the structured catalogue, common items, household shopping history, or existing item data.

## Checklist

- [ ] Suggestions are shown in the intended shopping input/search location.
- [ ] Suggestions are based on valid shopping item data, not hardcoded demo-only behavior unless documented.
- [ ] Suggestions do not show cancelled/deleted/private items that should not be visible.
- [ ] Suggestions respect item visibility: personal, targeted, shared.
- [ ] Empty suggestion state is handled cleanly.
- [ ] Selecting a suggestion fills or creates the intended shopping item correctly.

## Test coverage required

### Backend

- [ ] Service/repository test for returning suggestions.
- [ ] Test excludes items outside the current household.
- [ ] Test excludes invisible/private/targeted items for the wrong user.
- [ ] Test handles empty suggestion results.

### Frontend

- [ ] Component test renders suggestions.
- [ ] Component test selects a suggestion.
- [ ] Component test shows empty state.

### E2E

- [ ] User creates or searches shopping item and sees suggestions.
- [ ] User selects suggestion and item appears in shopping list.
- [ ] Test uses unique household/user data and can run repeatedly.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence:
Backend tests:
Frontend tests:
E2E tests:
Manual QA:
```

---

# FR10-02 — Voting Behavior

## Requirement

Users should be able to vote for suggested shopping items if the implemented design includes voting.

## Checklist

- [ ] Authenticated users can vote for a suggestion.
- [ ] Vote count updates correctly in UI.
- [ ] User cannot vote multiple times unless intentionally allowed.
- [ ] User can undo/remove vote if this is part of the implementation.
- [ ] Voting is household-scoped.
- [ ] Voting does not mark an item as bought automatically.
- [ ] Voting does not break existing mark/unmark bought behavior.

## Test coverage required

### Backend

- [ ] Vote creation/update test.
- [ ] Duplicate vote handling test.
- [ ] Cross-household vote rejection test.
- [ ] Unauthenticated vote rejection test.

### Frontend

- [ ] Vote button/action test.
- [ ] Vote count update test.
- [ ] Error state test for failed vote.

### E2E

- [ ] Two users in same WG vote and see updated result.
- [ ] User from another WG cannot influence the suggestion/vote.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence:
Backend tests:
Frontend tests:
E2E tests:
Manual QA:
```

---

# FR10-03 — Integration with Existing Shopping Flow

## Requirement

FR10 must not break FR07, FR08, or FR09 shopping workflows.

## Checklist

- [ ] Existing shopping item creation still works.
- [ ] Personal, targeted, and shared visibility still work.
- [ ] Mark/unmark bought still works.
- [ ] Aggregated household shopping list still works.
- [ ] Shopping history still works.
- [ ] Existing shopping E2E tests still pass.

## Test coverage required

- [ ] Regression test for normal shopping item creation.
- [ ] Regression test for targeted/shared visibility.
- [ ] Regression test for mark/unmark bought.
- [ ] Regression test for aggregated/history view.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Regression evidence:
```

---

# FR10-04 — Shared NFR Coverage

## Requirement

FR10 must satisfy the shared NFRs without duplicating the full general checklist.

## Checklist

### NFR-01 Security & Access Control

- [ ] Requires authentication.
- [ ] Backend derives acting user from JWT/authentication context.
- [ ] Household isolation is enforced in backend.
- [ ] Role/visibility rules are not only hidden in the frontend.

### NFR-02 Data Privacy / GDPR

- [ ] Suggestions/votes expose only necessary shopping data.
- [ ] No personal data from other WGs is exposed.
- [ ] Logs do not contain private shopping content unnecessarily.

### NFR-03 Reliability

- [ ] Suggestions/votes persist correctly after refresh.
- [ ] Feature works after backend restart with documented seed/database setup.
- [ ] Failed vote/suggestion requests do not corrupt item state.

### NFR-04 Usability & Compatibility

- [ ] Suggestion/voting flow is understandable without external explanation.
- [ ] Works on desktop reference viewport.
- [ ] Smoke-tested on mobile reference viewport or documented limitation.

### NFR-05 Maintainability

- [ ] Logic is in service/repository layer, not only frontend.
- [ ] DTOs are clear and do not expose unnecessary entity fields.
- [ ] No unrelated refactoring/log files committed.

### NFR-06 Auditability

- [ ] Important vote/suggestion actions have timestamp/responsible user where implemented.
- [ ] If no audit log exists for this extended feature, limitation is documented.

## Status

- [ ] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
NFR evidence:
```

---

# Final FR10 QA Summary

| Area | Status | Evidence |
|---|---|---|
| Suggestions | [ ] Pass [ ] Partial [ ] Fail | |
| Voting | [ ] Pass [ ] Partial [ ] Fail [ ] Not implemented | |
| Shopping regression | [ ] Pass [ ] Partial [ ] Fail | |
| Backend tests | [ ] Pass [ ] Partial [ ] Fail | |
| Frontend tests | [ ] Pass [ ] Partial [ ] Fail | |
| E2E tests | [ ] Pass [ ] Partial [ ] Fail | |
| Shared NFRs | [ ] Pass [ ] Partial [ ] Fail | |

## Required verification commands

```bash
cd backend
./mvnw test

cd ../frontend
npm test -- --watch=false
npm run build

cd ../e2e
npx cypress run
```

## QA sign-off

```text
Reviewer:
Date:
Branch / commit:
Result:
```
