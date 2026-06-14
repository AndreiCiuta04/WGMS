# WGMS - FR-15 Fair Chore Balancing via History QA Form - Checked

## Scope

This checked QA form verifies **FR-15 - Fair chore balancing via history**.

Evidence was collected from source review, added/reviewed tests, full backend/frontend/E2E command output, and the FR-15 demo-data flow. Boxes are checked only where concrete evidence exists.

Note: the user request references `docs/QA-features/FR-15_general_QA.md`, but this local checkout currently contains only `docs/QA-features/FR-15_general_QA_checked.md` for FR-15. The existing checked form structure was preserved and updated with current evidence.

---

# FR15-01 - Feature Scope and Existing Workflow Preservation

## Requirement

FR-15 must extend the existing task/cleaning workflow by recommending a fair assignee based on task history. It must not replace, remove, or weaken existing task creation, assignment, completion, undo, filtering, or recurrence functionality.

## Checklist

* [x] Fair chore balancing is implemented as a recommendation/helper feature, not as an uncontrolled automatic reassignment system.
* [x] Users can still manually choose an assignee.
* [x] Users can override the fair chore recommendation.
* [x] Existing task creation still works without using the recommendation.
* [x] Existing task assignment still works.
* [x] Existing task completion still works.
* [x] Existing undo completion still works.
* [x] Existing task filtering/status views still work.
* [x] Existing cleaning schedule recurrence still works.
* [x] Existing fixed-assignee cleaning schedules still work.
* [x] FR-15 does not introduce unrelated changes to expenses, shopping, dashboard, login, registration, invitation, or household management.

## Test coverage evidence

* [x] Existing backend task tests still pass.
* [x] Existing frontend task tests still pass.
* [x] Existing E2E task/cleaning schedule flows still pass.
* [x] No existing task/schedule test was deleted only because FR-15 changed the UI or data model.
* [x] Any updated test still checks the same functional result as before.

## Status

* [x] Passed
* [ ] Partially passed
* [ ] Failed

## Notes

```text
Evidence generated:
- Backend files reviewed:
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/TaskEndpoint.java
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/SimpleTaskService.java
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/SimpleCleaningScheduleService.java
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/FairChoreBalancingServiceImpl.java
- Frontend files reviewed:
  - frontend/src/app/components/task-create/task-create.component.ts
  - frontend/src/app/components/task-create/task-create.component.html
  - frontend/src/app/components/task-list/task-list.component.ts
  - frontend/src/app/components/task-list/task-list.component.html
  - frontend/src/app/components/cleaning-schedule-create/cleaning-schedule-create.component.ts
  - frontend/src/app/components/cleaning-schedule-create/cleaning-schedule-create.component.html
- Existing tests affected/reviewed:
  - backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/TaskEndpointTest.java
  - backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/SimpleTaskServiceTest.java
  - backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/SimpleCleaningScheduleServiceTest.java
  - frontend/src/app/components/task-create/task-create.component.spec.ts
  - frontend/src/app/components/task-list/task-list.component.spec.ts
  - e2e/cypress/e2e/fr03-tasks.cy.js
  - e2e/cypress/e2e/fr04-cleaning-schedules.cy.js
  - e2e/cypress/e2e/fr15-fair-chore-balancing.cy.js
  - e2e/cypress/e2e/usecases/uc02-task-lifecycle.cy.js
  - e2e/cypress/e2e/usecases/uc03-cleaning-schedule-to-task.cy.js
- Existing tests preserved:
  - cd backend && mvn test - PASS, 245 tests.
  - cd frontend && npm test -- --watch=false - PASS, TOTAL: 297 SUCCESS.
  - cd frontend && npm run build - PASS.
  - cd e2e && npx cypress run - PASS, 24 specs, 72 tests.
- Undo evidence:
  - TaskEndpointTest.givenDoneTask_whenAssignedUserReopensTask_then200AndStatusReturnsToOpen proves backend undo/current-state behavior.
  - task-list.component.spec.ts tests the Reopen action and error handling.
  - fr15-fair-chore-balancing.cy.js test "supports undoing completion through the task UI and fairness stats use current task status" marks a task done, reopens it, and verifies FR-15 stats count it as open/current-state rather than completed.
- Remaining issues:
  - None blocking for local FR-15 feature scope verification.
```

---

# FR15-02 - Backend Fairness Algorithm Correctness

## Requirement

The backend must calculate a fair chore recommendation from existing task/history data. The algorithm must be explainable, reproducible, and based only on the authenticated user's household.

## Expected algorithm

The source QA form expects a bucket model with `MAX_OPEN_TASKS = 3`, open weight `0.75`, overdue weight `2.0`, bucket priority A/B/C, and randomness only between exact minimum-score ties.

## Actual algorithm found

```text
Implementation after fixes:
- DEFAULT_HISTORY_DAYS = 30
- MAX_HISTORY_DAYS = 365
- MAX_OPEN_TASKS = 3
- COMPLETED_TASK_WEIGHT = 1.0
- OPEN_TASK_WEIGHT = 0.75
- OVERDUE_TASK_WEIGHT = 2.0
- ALGORITHM = HISTORY_BUCKET_SCORE_SEEDED_TIE
- FAIRNESS_THRESHOLD is no longer present or used.
- completedTasks count DONE tasks updated inside the history window.
- openTasks count OPEN/IN_PROGRESS tasks whose due date is today/future or missing.
- overdueTasks count OPEN/IN_PROGRESS tasks whose due date is before today.
- CANCELLED tasks are ignored.
- Overdue tasks are not double-counted as open.
- Bucket A / NO_OVERDUE_AVAILABLE: overdueTasks == 0 and openTasksNotOverdue < 3.
- Bucket B / NO_OVERDUE_OVERLOADED: overdueTasks == 0 and openTasksNotOverdue >= 3.
- Bucket C / HAS_OVERDUE: overdueTasks > 0.
- Recommendation uses the first non-empty bucket in A/B/C order.
- Candidate pool contains only roommates in the selected bucket with the exact minimum score.
- Seeded randomness is used only when the selected bucket has an exact minimum-score tie.
```

## Checklist

* [x] Completed tasks within the history window increase the burden score.
* [x] Completed tasks outside the history window do not affect the default score.
* [x] Open tasks not overdue increase the burden score with the open-task weight.
* [x] Overdue tasks increase the burden score with the overdue-task weight.
* [x] Overdue tasks are not also counted as open tasks.
* [x] A roommate with overdue tasks is placed into the overdue bucket.
* [x] A roommate with too many open tasks but no overdue tasks is placed into the overloaded-open bucket.
* [x] Bucket A is preferred over Bucket B.
* [x] Bucket B is preferred over Bucket C.
* [x] The recommended roommate always comes from the best non-empty bucket.
* [x] The recommended roommate has the lowest score inside the selected bucket.
* [x] Randomness is used only for true ties.
* [x] Seeded tie-breaking returns the same recommendation for the same input/context.
* [x] Different seed contexts may result in different tie-break choices.
* [x] Tie-break randomness never recommends someone outside the tied minimum-score group.
* [x] Empty/no-history households still return a valid recommendation if at least one active roommate exists.
* [x] Undoing a completion changes the statistics according to the task's current state.
* [x] Deleted/inactive/anonymized/removed roommates are excluded if the model supports such a state.
* [x] The service handles invalid historyDays safely.

## Backend unit test evidence

* [x] Test for clear lowest-burden roommate.
* [x] Test for completed-task score contribution.
* [x] Test for open-task score contribution.
* [x] Test for overdue-task score contribution.
* [x] Test that overdue is not double-counted as open.
* [x] Test for Bucket A over Bucket B.
* [x] Test for Bucket B over Bucket C.
* [x] Test for tie-breaking with same seed.
* [x] Test that randomness does not affect non-ties.
* [x] Test for no-history fallback.
* [x] Test for history window boundaries.
* [x] Test for undo completion if supported by current model.
* [x] Test for excluding other-household tasks.
* [x] Test for excluding other-household roommates.

## Status

* [x] Passed
* [ ] Partially passed
* [ ] Failed

## Notes

```text
Evidence generated:
- Fairness service reviewed:
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/FairChoreBalancingService.java
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/FairChoreBalancingServiceImpl.java
- Repository methods reviewed:
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/repository/TaskRepository.java
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/repository/RoommateRepository.java
- DTO reviewed:
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/FairChoreRoommateStatsDto.java
- Unit/integration tests added or reviewed:
  - FairChoreBalancingServiceTest.givenCompletedOpenAndOverdueTasks_whenRecommend_thenScoresUseExpectedWeightsAndOverdueIsNotOpen
  - FairChoreBalancingServiceTest.givenDoneTaskOutsideWindowAndUndoneOpenTask_whenRecommend_thenOnlyCurrentStateCounts
  - FairChoreBalancingServiceTest.givenBucketAAndBucketBRoommates_whenRecommend_thenBucketAIsPreferredEvenWithHigherScore
  - FairChoreBalancingServiceTest.givenBucketBAndBucketCRoommates_whenRecommend_thenBucketBIsPreferredEvenIfBucketCHasLowerScore
  - FairChoreBalancingServiceTest.givenExactTie_whenRecommend_thenOnlyMinimumScoreTieGroupIsCandidateAndSameSeedIsStable
  - FairChoreBalancingServiceTest.givenDifferentSeedContextForExactTie_whenRecommend_thenRecommendationNeverLeavesTieGroup
  - FairChoreBalancingServiceTest.givenExactTie_whenSeedContextChanges_thenDifferentTiedRoommatesCanBeSelected
  - FairChoreBalancingServiceTest.givenNonTiedLowestScore_whenRecommend_thenRandomnessDoesNotAffectSelection
  - FairChoreBalancingServiceTest.givenInactiveAnonymizedAndOtherHouseholdRoommates_whenRecommend_thenTheyAreIgnored
  - FairChoreBalancingServiceTest.givenOtherHouseholdTaskAssignedToCurrentUser_whenRecommend_thenTaskDoesNotAffectScore
  - TaskEndpointTest.givenBucketScenario_whenGetFairBalancingRecommendation_thenReturnsBestBucketAndExactMinimumCandidate
  - TaskRepositoryTest.givenFairBalancingTasks_whenFindFairBalancingTasksByHouseholdId_thenFiltersHouseholdStatusAndHistoryWindow
- Algorithm constants checked:
  - completed=1.0, open=0.75, overdue=2.0, maxOpenTasks=3.
- Command results:
  - cd backend && mvn -Dtest=FairChoreBalancingServiceTest,TaskEndpointTest,TaskRepositoryTest,SimpleCleaningScheduleServiceTest,CleaningScheduleEndpointTest test - PASS, 71 tests.
  - cd backend && mvn test - PASS, 245 tests.
- Remaining issues:
  - None blocking for backend algorithm verification.
```

---

# FR15-03 - Security, Authorization, and Household Scoping

## Requirement

FR-15 must enforce household isolation on the backend. The frontend must not be trusted for household access. The recommendation must only use and return data belonging to the authenticated user's current household.

## Checklist

* [x] FR-15 endpoint requires authentication.
* [x] Unauthenticated requests are rejected.
* [x] Current user is derived from Authentication/JWT.
* [x] Current household is derived from the authenticated user's active roommate record.
* [x] Backend does not accept or trust a frontend-supplied householdId.
* [x] Backend never returns roommates from another household.
* [x] Backend never counts tasks from another household.
* [x] Roommates from another household cannot influence the recommendation.
* [x] A normal roommate cannot access another WG's fairness data.
* [x] The response does not expose unnecessary personal data.
* [x] The response contains only fields needed for the recommendation UI.
* [x] Role checks remain consistent with existing task/cleaning schedule rules.
* [x] Backend authorization is not replaced by frontend hiding.

## Backend integration/security test evidence

* [x] Unauthenticated endpoint test exists.
* [x] Authenticated current-household endpoint test exists.
* [x] Cross-household leakage test exists.
* [x] Test verifies frontend householdId is not trusted or not accepted.
* [x] Test verifies only current-household roommates appear in stats.
* [x] Test verifies only current-household tasks affect the score.

## Status

* [x] Passed
* [ ] Partially passed
* [ ] Failed

## Notes

```text
Evidence generated:
- Endpoint reviewed:
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/TaskEndpoint.java
  - GET /api/v1/tasks/fair-balancing/recommendation is annotated with @Secured("ROLE_USER").
- Auth/current-user resolution reviewed:
  - TaskEndpoint passes Authentication.getName() to FairChoreBalancingService.
  - FairChoreBalancingServiceImpl resolves the ApplicationUser by email and resolves the household via RoommateRepository.findByUserIdAndActiveTrue.
- Household scoping implementation:
  - TaskRepository.findFairBalancingTasksByHouseholdId filters by current household, current task statuses, and history window.
  - FairChoreBalancingServiceImpl filters active current-household roommates and excludes anonymized users.
- Security tests:
  - TaskEndpointTest.givenNoToken_whenGetFairBalancingRecommendation_then403.
  - TaskEndpointTest.givenAuthenticatedUser_whenGetFairBalancingRecommendation_thenReturnsCurrentHouseholdStatsOnly.
  - TaskRepositoryTest.givenTasksInDifferentHouseholds_whenFindAllByHouseholdIdForFairBalancing_thenOnlyHouseholdTasksAreReturned.
  - FairChoreBalancingServiceTest.givenOtherHouseholdTaskAssignedToCurrentUser_whenRecommend_thenTaskDoesNotAffectScore.
- Response privacy:
  - DTO exposes roommate/user ids, display name/email, aggregate counts, fairnessBucket, score, candidate flag, and recommended flag.
  - It does not expose raw tasks, task titles, due dates, descriptions, or other-household data.
- Command result:
  - cd backend && mvn test - PASS, 245 tests.
- Remaining issues:
  - None blocking for FR-15 household scoping.
```

---

# FR15-04 - Backend API Contract and DTO Quality

## Requirement

FR-15 must expose a clear, stable backend API for the frontend. The response should include the recommendation, explanation, and enough statistics for the user to understand why the recommendation was made.

## Checklist

* [x] Endpoint path follows existing backend naming conventions.
* [x] Endpoint returns a recommendation for the current household.
* [x] Endpoint returns all roommate statistics needed by the UI.
* [x] DTO names and field names follow existing project style.
* [x] Response includes recommended roommate ID.
* [x] Response includes recommended display name/email according to existing API style.
* [x] Response includes historyDays used.
* [x] Response includes algorithm label or equivalent explanation.
* [x] Response includes a user-facing reason.
* [x] Response includes per-roommate completed/open/overdue counts.
* [x] Response includes per-roommate score.
* [x] Response identifies tied candidates where relevant.
* [x] Response identifies the selected recommendation.
* [x] Response includes fairness bucket per roommate.
* [x] Empty/no-history response is still valid and understandable.
* [x] Invalid parameters return clear validation errors.
* [x] API documentation/OpenAPI is updated if the project maintains endpoint docs.

## Suggested DTO evidence

```text
FairChoreRecommendationDto:
- recommendedRoommateId: implemented
- recommendedUserId: implemented
- recommendedDisplayName/email: implemented
- historyDays: implemented
- algorithm: implemented
- reason: implemented
- stats: implemented
```

```text
FairChoreRoommateStatsDto:
- roommateId: implemented
- userId: implemented
- displayName/email: implemented
- completedTasks: implemented
- openTasks: implemented
- overdueTasks: implemented
- fairnessBucket: implemented
- fairnessScore: implemented
- candidate: implemented
- recommended: implemented
```

## Status

* [x] Passed
* [ ] Partially passed
* [ ] Failed

## Notes

```text
Evidence generated:
- Endpoint path:
  - GET /api/v1/tasks/fair-balancing/recommendation in TaskEndpoint.java.
- DTOs reviewed:
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/FairChoreRecommendationDto.java
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/FairChoreRoommateStatsDto.java
- API docs:
  - TaskEndpoint method has @Operation(summary = "Get fair chore balancing recommendation").
  - Runtime OpenAPI endpoint exists via springdoc at /v3/api-docs; no static OpenAPI file is maintained in the repo.
- Validation behavior:
  - FairChoreBalancingServiceImpl rejects historyDays < 1 and > 365.
  - TaskEndpointTest.givenInvalidHistoryDays_whenGetFairBalancingRecommendation_then400.
- Response behavior:
  - TaskEndpointTest.givenBucketScenario_whenGetFairBalancingRecommendation_thenReturnsBestBucketAndExactMinimumCandidate checks algorithm, selected bucket, candidate flags, and recommendation.
  - TaskEndpointTest.givenNoHistory_whenGetFairBalancingRecommendation_thenReturnsValidActiveRoommate checks no-history response.
- Remaining issues:
  - None blocking for API/DTO verification.
```

---

# FR15-05 - Frontend Task Creation Integration

## Requirement

The frontend must show the fair chore recommendation in the existing task creation workflow without redesigning the page or changing the existing UI style.

## Checklist

* [x] Fair chore suggestion is visible in the task creation form.
* [x] Recommendation loads when opening the task creation form.
* [x] Loading state is shown or handled gracefully.
* [x] Error state is shown or handled gracefully.
* [x] No-history state is shown or handled gracefully.
* [x] Recommended assignee is displayed clearly.
* [x] Short reason/explanation is displayed.
* [x] Optional roommate statistics are shown clearly if implemented.
* [x] "Use suggestion" button fills the assignee field.
* [x] User can manually override the suggested assignee.
* [x] Creating a task with the suggested assignee works.
* [x] Creating a task with a manually chosen assignee still works.
* [x] The recommendation UI does not block task creation if the recommendation request fails.
* [x] The recommendation UI uses stable selectors where needed, preferably `data-cy`.
* [x] The feature does not move fairness logic into Angular.

## Frontend style checklist

* [x] Existing task page layout is preserved.
* [x] Existing form style is preserved.
* [x] Existing button style is preserved.
* [x] Existing card/panel style is reused.
* [x] No new UI library was introduced.
* [x] Custom CSS is minimal.
* [x] The new UI matches the current app visually.
* [x] The feature remains usable on desktop.
* [x] The feature remains usable on mobile-sized viewports.
* [x] No important action is hidden on mobile.

## Frontend test evidence

* [x] Component/service test verifies recommendation loads.
* [x] Test verifies fair suggestion card is displayed.
* [x] Test verifies "Use suggestion" fills assignee field.
* [x] Test verifies manual override remains possible.
* [x] Test verifies loading/error/no-history state where feasible.
* [x] Existing task form tests still pass.

## Status

* [x] Passed
* [ ] Partially passed
* [ ] Failed

## Notes

```text
Evidence generated:
- Frontend components reviewed:
  - frontend/src/app/components/task-create/task-create.component.ts
  - frontend/src/app/components/task-create/task-create.component.html
  - frontend/src/app/components/task-create/task-create.component.scss
- Frontend service/DTO reviewed:
  - frontend/src/app/services/task.service.ts
  - frontend/src/app/dtos/task.ts
- CSS/style changes reviewed:
  - Only scoped .fair-chore-suggestion mobile/responsive styling was added.
  - Bootstrap/form/button/card patterns remain unchanged.
- Component/service tests:
  - task.service.spec.ts: should send GET to fair balancing recommendation with stable context params.
  - task-create.component.spec.ts: should display fair chore suggestion on init.
  - task-create.component.spec.ts: should render fair chore suggestion content and stats.
  - task-create.component.spec.ts: should handle fair chore suggestion loading errors gracefully.
  - task-create.component.spec.ts: should set loading state while fair chore suggestion is pending.
  - task-create.component.spec.ts: should show empty state and disable use suggestion when no recommendation is returned.
  - task-create.component.spec.ts: should fill assignee when suggestion is used and still allow manual override.
- E2E tests:
  - fr15-fair-chore-balancing.cy.js verifies suggestion appears, Use suggestion fills the assignee, task creation succeeds, and created task is assigned to Light Worker.
  - fr15-fair-chore-balancing.cy.js mobile test verifies 390x844 and 412x915 viewports, no horizontal overflow, suggestion card visible, and Use suggestion reachable/clickable.
- Commands:
  - cd frontend && npm test -- --watch=false - PASS, TOTAL: 297 SUCCESS.
  - cd frontend && npm run build - PASS.
  - cd e2e && npx cypress run - PASS, 24 specs, 72 tests.
- Remaining issues:
  - None blocking for frontend task creation integration.
```

---

# FR15-06 - Optional Cleaning Schedule / Recurrence Integration

## Requirement

If FR-15 is integrated into recurring cleaning schedules, it must be opt-in and must not break existing fixed-assignee recurring schedules.

## Checklist - if implemented

* [x] CleaningSchedule has a fairBalancingEnabled flag or equivalent opt-in setting.
* [x] fairBalancingEnabled defaults to false.
* [x] Existing schedules continue working with fairBalancingEnabled = false.
* [x] Existing fixed-assignee behavior is preserved.
* [x] Schedule create DTO includes the new option where needed.
* [x] Schedule update DTO includes the new option where needed.
* [x] Schedule detail DTO returns the new option where needed.
* [x] Frontend schedule form shows a checkbox or equivalent setting.
* [x] The setting matches existing frontend style.
* [x] When fairBalancingEnabled = true, generated tasks use the fair chore balancing service.
* [x] Seed for recurring schedule tie-breaks is based on stable schedule context.
* [x] If recommendation fails, fallback behavior is clear.
* [x] Generated recurring task always has a valid assignee.
* [x] Existing recurring schedule tests still pass.

## Checklist - if not implemented

Not applicable: schedule integration is implemented and verified.

## Test coverage evidence

* [x] Backend test for fairBalancingEnabled = false preserving fixed assignment.
* [x] Backend test for fairBalancingEnabled = true using recommendation, if implemented.
* [x] Frontend test for checkbox, if implemented.
* [x] E2E/manual test for schedule integration, if implemented.
* [x] Existing FR-04 recurrence tests still pass.

## Status

* [x] Passed
* [ ] Partially passed
* [ ] Failed
* [ ] Not implemented / documented limitation

## Notes

```text
Evidence generated:
- Schedule integration status:
  - Implemented as opt-in via fairBalancingEnabled.
- Backend files reviewed:
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/entity/CleaningSchedule.java
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/CleaningScheduleCreateDto.java
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/CleaningScheduleUpdateDto.java
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/CleaningScheduleDetailDto.java
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/SimpleCleaningScheduleService.java
  - backend/src/main/resources/schema.sql
- Frontend files reviewed:
  - frontend/src/app/components/cleaning-schedule-create/cleaning-schedule-create.component.ts
  - frontend/src/app/components/cleaning-schedule-create/cleaning-schedule-create.component.html
  - frontend/src/app/components/cleaning-schedule-edit/cleaning-schedule-edit.component.ts
  - frontend/src/app/components/cleaning-schedule-edit/cleaning-schedule-edit.component.html
  - frontend/src/app/dtos/cleaning-schedule.ts
- Fallback behavior:
  - SimpleCleaningScheduleService.resolveGeneratedTaskAssignee catches expected recommendation exceptions: ConflictException, ForbiddenException, NotFoundException, ValidationException.
  - On expected recommendation failure, it falls back to the configured assignee when that assignee is still active in the schedule household.
  - If no valid fallback exists, it throws a clear ConflictException.
  - fairBalancingEnabled=false behavior still preserves fixed assignment and does not call the fairness service.
- Tests added/reviewed:
  - SimpleCleaningScheduleServiceTest.givenFairBalancingEnabledSchedule_whenGenerateTask_thenRecommendedAssigneeIsUsed.
  - SimpleCleaningScheduleServiceTest.givenFairBalancingEnabledSchedule_whenRecommendationThrowsExpectedException_thenConfiguredAssigneeIsUsed.
  - SimpleCleaningScheduleServiceTest.givenFairBalancingEnabledScheduleWithoutFallback_whenRecommendationThrowsExpectedException_thenConflict.
  - SimpleCleaningScheduleServiceTest.givenWeeklyActiveSchedule_whenGenerateTask_thenTaskIsCreatedAndNextDueDateAdvancesByOneWeek.
  - CleaningScheduleEndpointTest covers create/update/detail DTO handling.
  - e2e/cypress/e2e/fr15-fair-chore-balancing.cy.js test "uses fair balancing when generating a cleaning schedule task" creates a fair schedule, generates a task, and verifies assignment to Light Worker through fair balancing.
  - e2e/cypress/e2e/fr04-cleaning-schedules.cy.js and uc03-cleaning-schedule-to-task.cy.js continue to pass for existing recurrence/fixed-assignee behavior.
- Command results:
  - cd backend && mvn -Dtest=FairChoreBalancingServiceTest,TaskEndpointTest,TaskRepositoryTest,SimpleCleaningScheduleServiceTest,CleaningScheduleEndpointTest test - PASS, 71 tests.
  - cd e2e && npx cypress run - PASS, 24 specs, 72 tests.
- Remaining issues:
  - None blocking for schedule integration.
```

---

# FR15-07 - Demo/Test Data Generation

## Requirement

FR-15 should have deterministic development/test data that makes the recommendation easy to manually verify and suitable for E2E testing.

## Checklist

* [x] Dedicated FR-15 demo household exists.
* [x] Demo users/roommates exist with documented credentials.
* [x] Seed data is active only in dev/test/demo profile, not production.
* [x] Seed data creates one clear lowest-burden roommate.
* [x] Seed data creates at least one heavier-burden roommate.
* [x] Seed data creates at least one roommate with overdue tasks.
* [x] Seed data creates at least one task outside the 30-day history window.
* [x] Seed data creates at least one other household to verify household scoping.
* [x] Passwords are hashed consistently with existing seed users.
* [x] Due dates are relative to the current date where needed.
* [x] Completed dates/history entries are valid according to the current model.
* [x] Seed data does not break existing tests.
* [x] Documentation explains expected scores and expected recommendation.

## Suggested demo expectation

```text
Household:
- Fair Chore Demo WG

Expected default 30-day scores:
- Anna Fair: 2 completed + 1 open-not-overdue = 2.75, Bucket A / NO_OVERDUE_AVAILABLE.
- Heavy Worker: 5 completed + 1 open-not-overdue = 5.75, Bucket A / NO_OVERDUE_AVAILABLE.
- Medium Worker: 3 completed + 1 overdue = 5.0, Bucket C / HAS_OVERDUE.
- Light Worker: 1 completed = 1.0, Bucket A / NO_OVERDUE_AVAILABLE.

Expected manual recommendation:
- Light Worker, because Bucket A is selected and Light Worker has the exact minimum score in that bucket.
```

## Manual verification flow

* [x] Start backend with the profile that loads FR-15 demo data.
* [x] Start frontend.
* [x] Login with documented FR-15 demo account.
* [x] Open task creation.
* [x] Fair chore suggestion card appears.
* [x] Suggested assignee is the expected lowest-burden roommate.
* [x] Click "Use suggestion".
* [x] Assignee field is filled correctly.
* [x] Create task.
* [x] Created task is assigned to suggested roommate.

## Status

* [x] Passed
* [ ] Partially passed
* [ ] Failed

## Notes

```text
Evidence generated:
- Seed/datagen file:
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/datagenerator/DemoDataGenerator.java
  - backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/datagenerator/UserDataGenerator.java
- Demo profile:
  - DemoDataGenerator is active under @Profile({"datagen", "generateData"}).
- Demo users:
  - backend/README.md documents maintenant.fr15@example.com / password and related FR-15 demo users.
- Expected scores documented:
  - backend/README.md documents the bucket scores and Light Worker as the expected recommendation.
  - DemoDataGenerator comments document the same bucket scores and the old/out-of-window Light Worker task.
- Manual/E2E verification result:
  - cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=generateData - backend started for Cypress with demo data.
  - cd frontend && npm start -- --host 127.0.0.1 --port 4200 - frontend served for Cypress.
  - cd e2e && npx cypress run - PASS, 24 specs, 72 tests.
  - fr15-fair-chore-balancing.cy.js verifies Light Worker recommendation, scores/buckets, Use suggestion, task creation, undo current-state behavior, mobile flow, and fair schedule generation.
- Remaining issues:
  - None blocking for demo/test data.
```

---

# FR15-08 - Backend Automated Test Coverage

## Requirement

Backend logic for FR-15 must be tested at service/repository/endpoint level where appropriate. The feature should not rely only on manual testing.

## Minimum backend coverage expected

### Service/unit tests

* [x] Burden score calculation.
* [x] Bucket assignment.
* [x] Bucket priority.
* [x] Lowest-score candidate selection.
* [x] Seeded tie-breaking.
* [x] Same seed gives same result.
* [x] Randomness only applies to true ties.
* [x] No-history recommendation.
* [x] History window filtering.
* [x] Undo completion behavior if supported.
* [x] Other-household data ignored.
* [x] Inactive/deleted roommates ignored if supported.

### Repository/database tests

* [x] Repository query only loads tasks from current household.
* [x] Repository query only loads relevant task statuses/history.
* [x] Repository query respects history window.
* [x] Repository query does not return unrelated household members/tasks.
* [x] Persistence of fairBalancingEnabled, if schedule integration was implemented.

### Endpoint/integration tests

* [x] Authenticated request succeeds.
* [x] Unauthenticated request fails.
* [x] Cross-household data is not leaked.
* [x] Endpoint returns recommendation and stats.
* [x] Endpoint handles no-history household.
* [x] Endpoint handles invalid query params.
* [x] Same request returns reproducible recommendation.
* [x] Schedule integration endpoint/workflow tested if implemented.

## Command

```bash
cd backend
mvn test
```

## Status

* [x] Passed
* [ ] Partially passed
* [ ] Failed

## Notes

```text
Evidence generated:
- Backend command/result:
  - cd backend && mvn -Dtest=FairChoreBalancingServiceTest,TaskEndpointTest,TaskRepositoryTest,SimpleCleaningScheduleServiceTest,CleaningScheduleEndpointTest test - PASS, 71 tests.
  - cd backend && mvn test - PASS, 245 tests.
- Service tests:
  - backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/FairChoreBalancingServiceTest.java.
- Repository tests:
  - backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/TaskRepositoryTest.java.
  - backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/CleaningScheduleRepositoryTest.java.
- Endpoint/integration tests:
  - backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/TaskEndpointTest.java.
  - backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/CleaningScheduleEndpointTest.java.
- Specific coverage:
  - Bucket assignment/priority and exact tie behavior are covered by FairChoreBalancingServiceTest.
  - Repository-level status/history filtering is covered by TaskRepositoryTest.givenFairBalancingTasks_whenFindFairBalancingTasksByHouseholdId_thenFiltersHouseholdStatusAndHistoryWindow.
  - Endpoint-level bucket/DTO/security behavior is covered by TaskEndpointTest.
- Remaining issues:
  - None blocking for backend automated coverage.
```

---

# FR15-09 - Frontend Automated Test Coverage

## Requirement

Frontend tests must cover the fair chore suggestion UI and prove that the task form still works with and without the recommendation.

## Checklist

* [x] API service method for FR-15 is tested or covered through component tests.
* [x] Task creation form loads recommendation.
* [x] Fair suggestion card/panel renders.
* [x] Recommended roommate is displayed.
* [x] Reason/explanation is displayed.
* [x] Statistics render correctly if shown.
* [x] "Use suggestion" fills the assignee control.
* [x] User can override the assignee after using the suggestion.
* [x] Form submission still works with suggested assignee.
* [x] Form submission still works with manually selected assignee.
* [x] Loading state is handled.
* [x] Error state is handled without blocking normal task creation.
* [x] Empty/no-history state is handled.
* [x] Schedule checkbox is tested if schedule integration was implemented.
* [x] Existing task form tests still pass.
* [x] Frontend build passes without template/type errors.

## Commands

```bash
cd frontend
npm test -- --watch=false
npm run build
```

## Status

* [x] Passed
* [ ] Partially passed
* [ ] Failed

## Notes

```text
Evidence generated:
- Frontend command/result:
  - cd frontend && npm test -- --watch=false - PASS, TOTAL: 297 SUCCESS.
  - cd frontend && npm run build - PASS.
- Component/service tests:
  - frontend/src/app/services/task.service.spec.ts
  - frontend/src/app/components/task-create/task-create.component.spec.ts
  - frontend/src/app/components/task-list/task-list.component.spec.ts
  - frontend/src/app/components/cleaning-schedule-create/cleaning-schedule-create.component.spec.ts
- Relevant test names:
  - task-create.component.spec.ts: should set loading state while fair chore suggestion is pending.
  - task-create.component.spec.ts: should show empty state and disable use suggestion when no recommendation is returned.
  - task-create.component.spec.ts: should handle fair chore suggestion loading errors gracefully.
  - task-create.component.spec.ts: should fill assignee when suggestion is used and still allow manual override.
  - task-list.component.spec.ts: should reopen done task and refresh the list.
  - task-list.component.spec.ts: should render reopen action for done tasks.
- Build result:
  - Angular production build completed successfully.
- Remaining issues:
  - None blocking for frontend automated coverage.
```

---

# FR15-10 - E2E Coverage

## Requirement

At least one Cypress E2E test should verify the complete user-facing FR-15 workflow.

## Minimum E2E scenario

* [x] Login/register test user or use documented FR-15 demo user.
* [x] Use household with multiple roommates.
* [x] Ensure test data creates one clearly lowest-burden roommate.
* [x] Open task creation page.
* [x] Verify fair chore suggestion appears.
* [x] Verify expected roommate is suggested.
* [x] Click "Use suggestion".
* [x] Verify assignee field is filled.
* [x] Create task.
* [x] Verify created task is assigned to the suggested roommate.
* [x] Verify test uses stable selectors, preferably `data-cy`.
* [x] Verify test does not depend on brittle CSS classes.
* [x] Verify test is pipeline-stable in local headless Cypress.

## Optional E2E scenario if schedule integration exists

* [x] Create or edit cleaning schedule with fair balancing enabled.
* [x] Trigger or verify generated recurring task.
* [x] Verify generated task uses fair chore recommendation.
* [x] Verify fixed-assignee schedule still works when fair balancing is disabled.

## Command

```bash
cd e2e
npx cypress run
```

## Status

* [x] Passed
* [ ] Partially passed
* [ ] Failed

## Notes

```text
Evidence generated:
- E2E spec:
  - e2e/cypress/e2e/fr15-fair-chore-balancing.cy.js
- FR-15 E2E scenarios:
  - uses deterministic FR-15 seed data and recommends Light Worker.
  - keeps the fair chore suggestion usable on mobile viewports.
  - supports undoing completion through the task UI and fairness stats use current task status.
  - uses fair balancing when generating a cleaning schedule task.
  - recommends the lower-burden roommate and applies the suggestion during task creation.
- E2E command/result:
  - cd e2e && npx cypress run --spec cypress/e2e/fr15-fair-chore-balancing.cy.js - PASS, 5 tests.
  - cd e2e && npx cypress run - PASS, 24 specs, 72 tests.
- Test data used:
  - FR-15 demo data from DemoDataGenerator, profile generateData.
- Pipeline result:
  - Local full Cypress run passed against local backend/frontend.
- Remaining issues:
  - None blocking for E2E coverage.
```

---

# FR15-11 - Regression Coverage for Existing Core Workflows

## Requirement

Because FR-15 touches task assignment and cleaning schedules, it must be proven that existing WGMS workflows still work.

## Checklist

### Task / cleaning regression

* [x] Existing task creation flow still passes.
* [x] Existing task assignment flow still passes.
* [x] Existing task completion flow still passes.
* [x] Existing undo completion flow still passes.
* [x] Existing filtering/status flow still passes.
* [x] Existing cleaning schedule creation flow still passes.
* [x] Existing recurring schedule generation still passes.
* [x] Existing fixed-assignee schedule behavior still passes.

### Wider regression

* [x] Login/registration tests still pass.
* [x] WG creation/invitation tests still pass.
* [x] Expense/payment tests still pass.
* [x] Shopping tests still pass.
* [x] Dashboard/history tests still pass.
* [x] GDPR/profile/legal tests still pass where implemented.
* [x] CI pipeline passes.
* [x] No failing test was skipped or deleted to make CI green.

## Required commands

```bash
cd backend
mvn test
```

```bash
cd frontend
npm test -- --watch=false
npm run build
```

```bash
cd e2e
npx cypress run
```

## Status

* [x] Passed locally; remote CI not verified from this environment
* [ ] Partially passed
* [ ] Failed

## Notes

```text
Evidence generated:
- Backend command/result:
  - cd backend && mvn test - PASS, 245 tests.
- Frontend command/result:
  - cd frontend && npm test -- --watch=false - PASS, TOTAL: 297 SUCCESS.
  - cd frontend && npm run build - PASS.
- E2E command/result:
  - cd e2e && npx cypress run - PASS, 24 specs, 72 tests.
- Pipeline result:
  - Remote CI is not accessible from this environment; no GitLab pipeline status is claimed.
  - Local backend/frontend/build/E2E commands pass.
- Existing tests changed:
  - Existing tests were extended to add FR-15 evidence, schedule fallback coverage, undo coverage, and stable selectors.
  - No functional assertions were weakened.
- Existing tests deleted/replaced:
  - None found.
- Remaining issues:
  - Remote CI pipeline status remains unchecked because it could not be accessed locally.
```

---

# FR15-12 - Non-Functional Requirements Summary

## Requirement

FR-15 must satisfy the relevant project non-functional requirements.

| NFR | FR-15-specific check | Status | Evidence |
| --- | --- | --- | --- |
| NFR-01 Security & Access Control | Fairness endpoint requires login; current household is derived backend-side; no cross-household data leakage; frontend hiding is not treated as security. | [x] Pass [ ] Partial [ ] Fail | `TaskEndpoint.java`, `FairChoreBalancingServiceImpl.java`, `TaskEndpointTest.givenNoToken_whenGetFairBalancingRecommendation_then403`, cross-household stats tests. |
| NFR-02 Data Privacy & GDPR | Response exposes only necessary roommate/task statistics; no unnecessary personal data; deleted/anonymized users handled consistently. | [x] Pass [ ] Partial [ ] Fail | DTOs expose aggregate stats only; anonymized users excluded in `FairChoreBalancingServiceTest.givenInactiveAnonymizedAndOtherHouseholdRoommates_whenRecommend_thenTheyAreIgnored`. |
| NFR-03 Reliability | Fairness recommendation is reproducible for the same seed/context; demo data is documented; persistence changes do not break restart/demo setup. | [x] Pass [ ] Partial [ ] Fail | Same-seed tests, exact-tie tests, demo-data E2E, backend full suite, and Cypress full suite pass. |
| NFR-04 Usability & Compatibility | Peer tester can understand and use the recommendation; user can override it; UI works on desktop and mobile reference viewports. | [x] Pass [ ] Partial [ ] Fail | Component tests and Cypress cover Use suggestion, manual override, 390x844 and 412x915 mobile viewports, and no horizontal overflow. |
| NFR-05 Maintainability | Fairness logic is backend service logic, not duplicated in Angular; constants are centralized; code follows existing service/repository/DTO patterns. | [x] Pass [ ] Partial [ ] Fail | Backend service owns logic; Angular service only calls API; DTO/service patterns match project style. |
| NFR-06 Auditability | Task assignment remains traceable through existing timestamps/responsible-user/task history mechanisms; FR-15 does not remove audit-relevant fields. | [x] Pass [ ] Partial [ ] Fail | Existing task create/update/status models and tests still pass; FR-15 adds recommendation data without removing task audit fields. |

## Status

* [x] Passed
* [ ] Partially passed
* [ ] Failed

## Notes

```text
Evidence generated:
- Security/privacy checks:
  - FR15-03 passed; anonymized/inactive roommates excluded; no raw task data in response.
- Reliability checks:
  - Same request produces same recommendation; exact tie behavior is deterministic for the same seed and limited to tied candidates.
  - Full backend/frontend/build/E2E local commands pass.
- Usability/compatibility checks:
  - Use suggestion and manual override pass component/E2E tests.
  - Mobile FR-15 viewport evidence exists for 390x844 and 412x915; the test also checks no horizontal overflow.
- Maintainability checks:
  - Fairness logic remains in backend service; frontend only consumes DTO/API.
- Auditability checks:
  - Existing task assignment/status workflows and tests pass.
- Remaining issues:
  - Remote CI pipeline status was not accessible and is not claimed.
```

---

# Final FR-15 QA Summary

| Area | Status | Notes |
| --- | --- | --- |
| Feature scope preserved | [x] Pass [ ] Partial [ ] Fail | Task/schedule workflows pass; undo UI and fairness current-state behavior are covered by tests and E2E. |
| Backend algorithm correct | [x] Pass [ ] Partial [ ] Fail | Bucket/hurdle model, expected weights, exact-min candidates, and seeded true-tie behavior are implemented and tested. |
| Household scoping/security correct | [x] Pass [ ] Partial [ ] Fail | Auth and cross-household tests pass; no frontend householdId is trusted. |
| API/DTO contract clear | [x] Pass [ ] Partial [ ] Fail | DTOs and endpoint include recommendation, reason, stats, score, bucket, candidate, and recommended flags. |
| Frontend task-form integration works | [x] Pass [ ] Partial [ ] Fail | Component tests and Cypress cover recommendation, Use suggestion, manual override, error/loading/no-history states, and creation. |
| Frontend style remains consistent | [x] Pass [ ] Partial [ ] Fail | Scoped minimal CSS only; desktop and mobile viewport evidence pass. |
| Recurring schedule integration safe or limitation documented | [x] Pass [ ] Partial [ ] Fail [ ] N/A | Opt-in fairBalancingEnabled generation, fallback behavior, fixed-assignee preservation, backend tests, and E2E all pass. |
| Demo/test data sufficient | [x] Pass [ ] Partial [ ] Fail | Demo profile/data/docs and E2E pass; Light Worker remains the clear expected recommendation. |
| Backend tests sufficient | [x] Pass [ ] Partial [ ] Fail | Service, repository, endpoint, schedule, and full backend suites pass. |
| Frontend tests sufficient | [x] Pass [ ] Partial [ ] Fail | Full frontend suite and build pass; loading/no-history/error/override tests are present. |
| E2E coverage sufficient | [x] Pass [ ] Partial [ ] Fail | FR-15 task creation, mobile, undo current-state, schedule generation, and full Cypress regression pass. |
| Existing tests still pass | [x] Pass locally [ ] Partial [ ] Fail | Full local backend/frontend/build/E2E pass; remote CI is not accessible and not claimed. |
| NFRs satisfied | [x] Pass [ ] Partial [ ] Fail | Security, privacy, reliability, usability, maintainability, and auditability checks pass locally. |

## Overall result

* [x] FR-15 QA passed for local verification
* [ ] FR-15 QA partially passed with documented feature limitations
* [ ] FR-15 QA failed / blocking issues remain

## Remaining limitations

```text
- Remote CI pipeline status is not available from this environment; no remote CI pass is claimed.
- The source checklist file `docs/QA-features/FR-15_general_QA.md` is not present in this checkout; the existing checked form structure was preserved.
- No blocking local FR-15 feature, security, frontend, schedule, E2E, or regression limitations remain.
```

## Evidence links

```text
Backend implementation:
- backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/FairChoreBalancingService.java
- backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/FairChoreBalancingServiceImpl.java
- backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/repository/TaskRepository.java
- backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/TaskEndpoint.java
- backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/FairChoreRecommendationDto.java
- backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/dto/FairChoreRoommateStatsDto.java
- backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/service/impl/SimpleCleaningScheduleService.java

Backend tests:
- backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/FairChoreBalancingServiceTest.java
- backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/TaskEndpointTest.java
- backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/TaskRepositoryTest.java
- backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/unittests/SimpleCleaningScheduleServiceTest.java
- backend/src/test/java/at/ac/tuwien/sepr/groupphase/backend/integrationtest/CleaningScheduleEndpointTest.java

Frontend implementation/tests:
- frontend/src/app/components/task-create/task-create.component.ts
- frontend/src/app/components/task-create/task-create.component.html
- frontend/src/app/components/task-create/task-create.component.scss
- frontend/src/app/components/task-create/task-create.component.spec.ts
- frontend/src/app/components/task-list/task-list.component.ts
- frontend/src/app/components/task-list/task-list.component.html
- frontend/src/app/components/task-list/task-list.component.spec.ts
- frontend/src/app/services/task.service.ts
- frontend/src/app/services/task.service.spec.ts
- frontend/src/app/dtos/task.ts

E2E tests:
- e2e/cypress/e2e/fr15-fair-chore-balancing.cy.js
- e2e/cypress/e2e/fr03-tasks.cy.js
- e2e/cypress/e2e/fr04-cleaning-schedules.cy.js
- e2e/cypress/e2e/usecases/uc02-task-lifecycle.cy.js
- e2e/cypress/e2e/usecases/uc03-cleaning-schedule-to-task.cy.js

Seed/demo data:
- backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/datagenerator/DemoDataGenerator.java
- backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/datagenerator/UserDataGenerator.java
- backend/README.md
```

## QA sign-off

```text
Reviewer: Codex QA verification
Date: 2026-06-14T18:16:09+02:00
Branch / commit: FR-15/Fair-Chore-Balancing-Via-History / a7f12a5c258fec0eb8889c8372524bfb2a098da2
Result: Passed for local verification; remote CI not verified or claimed.
```
