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

- [x] Suggestions are shown in the intended shopping input/search location.
- [x] Only active suggestions are returned; suggestions use NEEDED status.
- [x] Suggestions are isolated by household.
- [x] Suggestions do not show cancelled/deleted/private items that should not be visible.
- [x] A household member can create a suggestion through the suggestion form.
- [x] Suggestions respect item visibility: personal, targeted, shared.
- [x] Empty suggestion state is handled cleanly.
- [x] A suggestion can be promoted to a regular shopping item.

## Test coverage required

### Backend

- [x] Service/repository test for returning suggestions.
- [x] Test excludes items outside the current household.
- [x] Test excludes invisible/private/targeted items for the wrong user.
- [x] Test handles empty suggestion results.
- [x] Equivalent suggestion merging test.
- [x] Promotion test.
- [x] Normal shopping items are not treated as suggestions.


### Frontend

- [x] Component test renders suggestions.
- [x] Component test selects a suggestion.
- [x] Component test shows empty state.

### E2E

- [x] User creates a shopping suggestion and sees it in the Suggestions tab.
- [x] User promotes a suggestion and sees it in the normal shopping list.
- [x] Test uses unique household/user data and can run repeatedly.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence:
Backend tests:
- givenUserInHousehold_whenCreateSuggestion_thenSuggestionCreatedWithCreatorVote 
- givenUserInHousehold_whenFindSuggestions_thenRepositoryUsesHouseholdAndNeededStatus 
- givenSuggestionInHousehold_whenMembersListSuggestions_thenOnlySameHouseholdSuggestionsReturned 
- givenEquivalentSuggestionFromOtherUser_whenCreateSuggestion_thenExistingSuggestionReturnedAndVoteAdded 
- givenNoEquivalentSuggestionInCurrentHousehold_whenCreateSuggestion_thenCreatesNewSuggestion 
- givenPromotedSuggestion_whenGetAggregatedList_thenItemAppears
Frontend tests:

E2E tests:
Manual QA:
- Suggestions are created manually in the Suggestions tab.
- Creator automatically receives the first vote.
```

---

# FR10-02 — Voting Behavior

## Requirement

Users should be able to vote for suggested shopping items if the implemented design includes voting.

## Checklist

- [x] Authenticated household users can vote for a suggestion.
- [x] Vote count is included in the backend response.
- [x] One user cannot add more than one vote.
- [x] A user can remove their vote.
- [x] Voting is household-scoped.
- [x] Voting does not mark an item as bought.
- [x] Suggestions remain NEEDED until promoted or otherwise changed.
- [x] Voting is separate from normal mark-bought behavior.
- [x] Voter details are returned.
- [x] The creator automatically votes for their own suggestion.

## Test coverage required

### Backend

- [x] Vote creation/update test.
- [x] Duplicate vote handling test.
- [x] Cross-household vote rejection test.
- [x] Explicit unauthenticated vote integration test.
- [x] Vote removal test.
- [x] Voter-details test.

### Frontend

- [x] Vote button/action test.
- [x] Vote count update test.
- [x] Error state test for failed vote.

### E2E

- [x] Two users in same WG vote and see updated result.
- [x] User from another WG cannot influence the suggestion/vote.

## Status

- [x] Passed
- [ ] Partially passed
- [ ] Failed

## Notes

```text
Evidence:
Backend tests:
- givenSuggestion_whenVoteForSuggestion_thenCurrentUserAddedToVotes 
- givenAlreadyVotedSuggestion_whenVoteForSuggestion_thenVoteCountDoesNotIncrease 
- givenVotedSuggestion_whenRemoveVote_thenCurrentUserRemoved 
- givenSuggestionFromOtherHousehold_whenVoteForSuggestion_thenForbidden 
- givenSuggestion_whenOtherHouseholdMemberVotes_thenVoteCountIncreases 
- givenSuggestion_whenSameUserVotesTwice_thenVoteCountDoesNotIncreaseTwice 
- givenSuggestionWithTwoVotes_whenGetSuggestions_thenVoterDetailsReturned
Frontend tests:
E2E tests:
Manual QA:
```

---

# FR10-03 — Integration with Existing Shopping Flow

## Requirement

FR10 must not break FR07, FR08, or FR09 shopping workflows.

## Checklist

- [x] Existing shopping item creation still works.
- [x] Personal, targeted, and shared visibility still work.
- [x] Mark/unmark bought still works.
- [x] Aggregated household shopping list still works.
- [x] Shopping history still works.
- [x] Existing shopping E2E tests still pass.

## Test coverage required

- [x] Regression test for normal shopping item creation.
- [x] Regression test for targeted/shared visibility.
- [x] Regression test for mark/unmark bought.
- [x] Regression test for aggregated/history view.

## Status

- [x] Passed
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

- [x] Requires authentication.
- [x] Backend derives acting user from JWT/authentication context.
- [x] Household isolation is enforced in backend.
- [x] Role/visibility rules are not only hidden in the frontend.

### NFR-02 Data Privacy / GDPR

- [x] Suggestions/votes expose only necessary shopping data.
- [x] No personal data from other WGs is exposed.
- [x] Logs do not contain private shopping content unnecessarily.

### NFR-03 Reliability

- [x] Suggestions/votes persist correctly after refresh.
- [x] Feature works after backend restart with documented seed/database setup.
- [x] Failed vote/suggestion requests do not corrupt item state.

### NFR-04 Usability & Compatibility

- [x] Suggestion/voting flow is understandable without external explanation.
- [x] Works on desktop reference viewport.
- [x] Smoke-tested on mobile reference viewport or documented limitation.

### NFR-05 Maintainability

- [x] Logic is in service/repository layer, not only frontend.
- [x] DTOs are clear and do not expose unnecessary entity fields.
- [x] No unrelated refactoring/log files committed.

### NFR-06 Auditability

- [x] Important vote/suggestion actions have timestamp/responsible user where implemented.
- [x] If no audit log exists for this extended feature, limitation is documented.

## Status

- [x] Passed
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
| Suggestions | [x] Pass [ ] Partial [ ] Fail | |
| Voting | [x] Pass [ ] Partial [ ] Fail [ ] Not implemented | |
| Shopping regression | [x] Pass [ ] Partial [ ] Fail | |
| Backend tests | [x] Pass [ ] Partial [ ] Fail | |
| Frontend tests | [x] Pass [ ] Partial [ ] Fail | |
| E2E tests | [x] Pass [ ] Partial [ ] Fail | |
| Shared NFRs | [x] Pass [ ] Partial [ ] Fail | |

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
Reviewer: Trishch Taras
Date: 15.06.2026
Branch / commit: Fr-10/b4f093fdd28cb9c8ddea3a584e889f81991b3f39
Result:Approved
```
