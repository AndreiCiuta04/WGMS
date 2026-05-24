# FR-08 Backend QA Checklist — Targeted Shopping Request Collection

## Feature Information

- Feature ID: FR-08
- Feature name: Targeted shopping request collection
- Use case: UC-04 — Collaborative Shopping Management
- Backend technology: Java Spring Boot
- Security: Spring Security, JWT
- Database: H2 (file-based)
- Automated tests: JUnit
- Branch: `FR-08/Targeted-shopping-request-collection`
- Merge request: (link MR before merge)
- Developer: Daniel
- QA reviewer: Taras
- Date: 2026-05-18

---

**Implementation note:** FR-08 is implemented as an evolution of FR-07's `ShoppingItem` entity, not as a separate `ShoppingRequest`. Same `/api/v1/shopping-items` surface; new `ShoppingItemVisibility` enum (PERSONAL/TARGETED/SHARED) and `targetUser` field add targeting. New `/targeted-at-me` endpoint.

---

## Backend Components

### Endpoint Layer

- [x] Endpoint class exists for FR-08 — extended `ShoppingItemEndpoint.java`, now 7 mappings (FR-08 adds `GET /targeted-at-me`).
- [x] Endpoint path follows `/api/v1/...`.
- [x] Endpoint uses DTOs and does not expose JPA entities directly — DTOs extended with `visibility`, `targetUserEmail`, target user info.
- [x] Endpoint is protected by Spring Security — `@PreAuthorize("hasRole('USER')")` on all 7 methods.
- [x] Endpoint derives current user from JWT — `authentication.getName()`.
- [x] Client cannot fake ownership fields — request records only expose `name`, `quantity`, `notes`, `visibility`, `targetUserEmail`.

### Service Layer

- [x] Service class exists — extended `ShoppingItemServiceImpl`. New method `findTargetedAtCurrentUser`.
- [x] Business logic is in service layer, not endpoint.
- [x] Service checks active household/WG membership — `requireActiveRoommate`.
- [x] Service prevents cross-household access — critical test `givenSharedItem_whenUserFromOtherHouseholdGetsAll_thenInvisible` proves SHARED doesn't leak.
- [x] Service validates target member belongs to same household — `resolveAndValidateTarget` enforces 4 invariants (required, no self-target, real user, same household).

### Repository Layer

- [x] Repository exists — extended `ShoppingItemRepository` with 2 new finders.
- [x] Repository queries are scoped by household — `findAllByHouseholdIdAndTargetUserIdOrderByCreatedAtDesc`, `findAllByHouseholdIdAndVisibilityOrderByCreatedAtDesc`.
- [x] Repository can retrieve requests targeted to current user — `findAllByHouseholdIdAndTargetUserIdOrderByCreatedAtDesc`.

---

## API Contract

- [x] Create endpoint exists — `POST /api/v1/shopping-items` (now accepts `visibility` + `targetUserEmail`).
- [x] List household requests endpoint — `GET /api/v1/shopping-items` returns broadened set (mine + targeted-at-me + SHARED in my household).
- [x] List my targeted requests endpoint — `GET /api/v1/shopping-items/targeted-at-me`.
- [x] Detail endpoint — `GET /api/v1/shopping-items/{id}` with broadened `canView`.
- [x] Update endpoint — `PUT /api/v1/shopping-items/{id}` with `canManage` (creator-only).
- [x] Status endpoint — `PATCH /api/v1/shopping-items/{id}/status` with `canView` (target can change status).
- [x] Delete/cancel endpoint — `DELETE /api/v1/shopping-items/{id}` with `canManage`.

---

## DTO and Validation Checks

- [x] Create DTO validates required item name — `@NotBlank`.
- [x] Target member is validated — `@Email` on `targetUserEmail` + service-level `resolveAndValidateTarget`.
- [x] Quantity/notes are validated — `@Size` limits.
- [x] Visibility enum is validated — `@NotNull`; typed as `ShoppingItemVisibility`.
- [x] Request DTO does not allow direct setting of household/creator/target id — target is identified by email, validated server-side.
- [x] Invalid body returns `400 Bad Request` — `GlobalExceptionHandler`.
- [x] Malformed JSON returns `400 Bad Request`.
- [x] Validation error format is usable by frontend — `{"Validation errors": [...]}`.

---

## Functional Backend Checks

- [x] Authenticated user can create valid targeted shopping request — `givenUserCreatesTargetedItem_whenTargetGetsTargetedAtMe_thenItemVisible`.
- [x] Backend assigns household server-side.
- [x] Backend assigns creator server-side.
- [x] Target user must belong to same household — `givenTargetOutsideHousehold_whenCreate_thenConflict` + `givenCreateTargetedAtOutsideUser_then409`.
- [x] User without active household receives safe error.
- [x] Authenticated user can list only own household requests.
- [x] Targeted user can see requests targeted to them.
- [x] User can update/status/delete if allowed — recipient can mark bought; only creator can edit/delete.
- [x] Cross-household ID guessing does not expose data — `givenSharedItemInOtherHousehold_whenFindOne_thenForbidden`.
- [x] Cross-household update/delete/status change is rejected.

---

## Security Checks

- [x] Unauthenticated requests are rejected — new `givenNoJwt_whenCreate_then401` integration test.
- [x] Authenticated users cannot access another household's requests — verified by `givenSharedItem_whenUserFromOtherHouseholdGetsAll_thenInvisible`.
- [x] Backend rejects cross-household target assignment — `givenCreateTargetedAtOutsideUser_then409`.
- [x] Backend does not trust frontend-provided household/user IDs.
- [x] Expired/manipulated JWT is rejected.
- [x] Error messages do not expose stack traces, SQL, or internal class names.
- [x] Logs do not contain raw JWTs, passwords, or excessive personal data.
- [x] User-generated content not logged at INFO — DEBUG only.

---

## Backend Tests

### Unit Tests

- [x] Service test for successful creation — PERSONAL and TARGETED cases.
- [x] Service test for validation/error cases — 4 dedicated tests for the 4 targeting invariants.
- [x] Service test for cross-household rejection — `givenSharedItemInOtherHousehold_whenFindOne_thenForbidden`.
- [x] Service test for target outside household rejection — `givenTargetOutsideHousehold_whenCreate_thenConflict`.
- [x] Service test for status/update/delete — `givenTargetedItemForMe_whenUpdateStatus_thenAllowed` + `givenTargetedItemForMe_whenDelete_thenForbidden`.

### Repository Tests

- [x] Repository test for household-scoped retrieval — new `ShoppingItemRepositoryTest` (`@DataJpaTest`) with `findByHouseholdId_returnsOnlyOwnHouseholdAndOrdersByCreatedAtDesc`.
- [x] Repository test for targeted-to-user retrieval — `findByHouseholdAndTargetUser_returnsOnlyItemsTargetedAtThatUser` + `findByHouseholdAndVisibility_returnsOnlyMatchingVisibility` + `findByHouseholdAndVisibility_isBoundedByHousehold`.

### Endpoint / Integration Tests

- [x] Endpoint test for successful creation — `givenUserCreatesTargetedItem_whenTargetGetsTargetedAtMe_thenItemVisible`.
- [x] Endpoint test for unauthenticated access — new `givenNoJwt_whenCreate_then401`.
- [x] Endpoint test for invalid body — new `givenBlankName_whenCreate_then400WithValidationErrors`.
- [x] Endpoint test for listing visible requests — `givenSharedItemInHousehold_whenOtherMemberGetsAll_thenItemVisible`.
- [x] Endpoint test for cross-household rejection — `givenSharedItem_whenUserFromOtherHouseholdGetsAll_thenInvisible`.

---

## Test Execution Evidence

```bash
mvn -B clean test
mvn -B clean verify
```

- [x] Backend tests pass locally — CI pipeline green.
- [x] Backend build/verification passes locally — CI green after Checkstyle fixes earlier in the branch.
- [x] Backend CI job passes — green for most recent push.

---

## Branch and Merge Readiness

- [x] Branch contains only FR-08 backend changes.
- [x] No generated context files are committed — `.gitignore` populated.
- [x] No IDE files are committed — `.vscode/` removed.
- [x] No local logs are committed — `.log` files removed.
- [x] No secrets are committed — verified by grep.
- [x] Merge request describes backend behavior — visibility enum, `/targeted-at-me`, broadened `canView`, four targeting invariants documented.

---

## Backend QA Summary

### Passed

- Clean evolution of FR-07 backend rather than parallel duplicate.
- `ShoppingItemVisibility` enum models the 3 sharing modes.
- `resolveAndValidateTarget` enforces 4 targeting invariants with clear 409/404 mappings.
- `canView` broadens correctly; `canManage` stays strict.
- 11 service unit tests + 4 new `@DataJpaTest` repository tests + 9 integration tests (including new 401 + 400 + cross-household SHARED isolation).
- `@AfterEach` cleanup in integration test.
- Repo hygiene cleaned up.
- CI pipeline green.

### Failed / Missing

None.

### Final Backend QA Decision

- [x] APPROVED — backend FR-08 is ready to merge.
- [ ] APPROVED WITH NOTES
- [ ] REJECTED

Decision: **APPROVED**
