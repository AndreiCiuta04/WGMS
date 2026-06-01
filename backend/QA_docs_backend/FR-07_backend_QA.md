# FR-07 Backend QA Checklist — Shopping List Management

## Feature Information

- Feature ID: FR-07
- Feature name: Shopping list management
- Use case: UC-04 — Collaborative Shopping Management
- Backend technology: Java Spring Boot
- Security: Spring Security, JWT
- Database: H2 (file-based, `jdbc:h2:file:./database/db`)
- Automated tests: JUnit
- Branch: `FR-07/Shopping-list-management`
- Merge request: (link MR before merge)
- Developer: Daniel
- QA reviewer: Taras
- Date: 2026-05-18

---

## How to Use This Checklist

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and add a short note.
- If something does not apply, write `N/A` with a reason.
- Do not merge into `dev` while critical items are unchecked.

---

## Backend Components

### Endpoint Layer

- [x] Endpoint class exists for FR-07.
  - Evidence: `backend/src/main/java/at/ac/tuwien/sepr/groupphase/backend/endpoint/ShoppingItemEndpoint.java` — `@RestController` with 6 mappings.

- [x] Endpoint path follows `/api/v1/...`.
  - Evidence: `@RequestMapping("/api/v1/shopping-items")` at class level.

- [x] Endpoint uses DTOs and does not expose JPA entities directly.
  - Evidence: Methods consume/return `ShoppingItemCreateDto`, `ShoppingItemUpdateDto`, `ShoppingItemStatusUpdateDto`, `ShoppingItemDto` records — never the entity.

- [x] Endpoint is protected by Spring Security.
  - Evidence: Every method annotated `@PreAuthorize("hasRole('USER')")`. `SecurityConfig.anyRequest().authenticated()` is the default.

- [x] Endpoint derives current user from JWT/security context.
  - Evidence: Each method takes `Authentication` and looks up the user via `userService.findApplicationUserByEmail(authentication.getName())`.

- [x] Client cannot fake `userId`, `creatorId`, or `householdId`.
  - Evidence: Request records only expose `name`, `quantity`, `notes`. `createdBy` and `household` are set server-side.

### Service Layer

- [x] Service class exists for shopping list logic — `service/ShoppingItemService.java` (interface) + `service/impl/ShoppingItemServiceImpl.java`.
- [x] Business logic is implemented in service layer, not endpoint — endpoint methods are 2–4 lines, service holds authorization, validation, persistence.
- [x] Service checks active household/WG membership — `requireActiveRoommate(currentUser)` at the start of every operation.
- [x] Service prevents cross-household access — `isMemberOfItemHousehold` called from `canView`/`canManage`. Tested at unit and integration level.
- [x] Service uses transactions where needed — `create`, `update`, `updateStatus`, `delete` annotated `@Transactional`; reads `@Transactional(readOnly = true)`.

### Repository Layer

- [x] Repository exists for shopping item entity — `repository/ShoppingItemRepository.java extends JpaRepository<ShoppingItem, Long>`.
- [x] Repository queries are scoped by household/WG — custom finders `findAllByHouseholdIdOrderByCreatedAtDesc(...)` and `findAllByHouseholdIdAndCreatedByIdOrderByCreatedAtDesc(...)`.
- [x] Repository methods do not return global data accidentally — all custom finders require `householdId`; unscoped `findAll()` only used in admin branch.

---

## API Contract

- [x] Create endpoint exists — `POST /api/v1/shopping-items` returns 201 + `ShoppingItemDto`.
- [x] List endpoint exists — `GET /api/v1/shopping-items` returns `List<ShoppingItemDto>`.
- [x] Detail endpoint exists — `GET /api/v1/shopping-items/{id}`.
- [x] Update endpoint exists — `PUT /api/v1/shopping-items/{id}`.
- [x] Status endpoint exists — `PATCH /api/v1/shopping-items/{id}/status`.
- [x] Delete/remove endpoint exists — `DELETE /api/v1/shopping-items/{id}` returns 204.

---

## DTO and Validation Checks

- [x] Create DTO validates required item name — `@NotBlank` on `name`.
- [x] Item name has max length validation — `@Size(max = 100)`.
- [x] Quantity is validated — `@Size(max = 50)`.
- [x] Notes are validated — `@Size(max = 2000)`.
- [x] Status enum is validated — `@NotNull`; Jackson rejects unrecognized enum values.
- [x] Request DTO does not allow direct setting of household/creator — records only have `name`, `quantity`, `notes`.
- [x] Invalid body returns `400 Bad Request` — `GlobalExceptionHandler#handleMethodArgumentNotValid` returns `{"Validation errors": [...]}`.
- [x] Malformed JSON returns `400 Bad Request` — Spring default handler.
- [x] Validation error format is usable by frontend — `{"Validation errors": [strings]}`, parsed in create/edit components.

---

## Entity and Persistence Checks

- [x] Shopping item entity exists — `entity/ShoppingItem.java` (`@Entity @Table(name = "shopping_item")`).
- [x] Entity is linked to household — `@ManyToOne(optional = false) household`.
- [x] Entity is linked to creator — `@ManyToOne(optional = false) createdBy`.
- [x] Required fields are non-null at entity/DB level — `name`, `status`, `household`, `createdBy`, `createdAt`, `updatedAt`.
- [x] User-generated text fields have length limits — `@Column(length = ...)` matches DTO `@Size`.
- [x] Status state is stored safely — `@Enumerated(EnumType.STRING)`; values NEEDED/BOUGHT/CANCELLED.
- [x] Created/updated timestamps exist — `@PrePersist onCreate`, `@PreUpdate onUpdate`.
- [x] Database migration/schema changes are intentional and documented — Hibernate auto-DDL with H2; entity Javadoc documents forward-compatibility for FR-08/FR-09.

---

## Functional Backend Checks

- [x] Authenticated user can create valid shopping item — `givenUserInHousehold_whenCreateItem_then201AndAttachedToHousehold`.
- [x] Backend assigns household server-side — `setHousehold(roommate.getHousehold())`.
- [x] Backend assigns creator server-side — `setCreatedBy(currentUser)`.
- [x] User without active household receives safe error — `givenUserWithoutHousehold_whenCreateItem_then403`.
- [x] Authenticated user can list only own household items — `givenOwnItems_whenFindAll_thenItemsReturned`.
- [x] Empty list is handled safely — returns `[]` with 200.
- [x] User can update allowed item — `loadAndAuthorizeManage` + happy path via integration test.
- [x] User can mark item as bought — `givenOwnItem_whenMarkAsBought_thenStatusIsUpdated`.
- [x] User can delete item — `givenOwnItem_whenDelete_then204AndItemGone`.
- [x] Cross-household ID guessing does not expose data — `givenItemOfOtherHousehold_whenFindOne_then403`.
- [x] Cross-household update/delete/status change is rejected — `givenItemOfOtherCreator_whenDelete_thenForbidden`.

---

## Security Checks

- [x] Unauthenticated requests are rejected — new `givenNoJwt_whenCreate_then401` integration test.
- [x] Authenticated users cannot access another household — cross-household tests pass.
- [x] Backend does not trust frontend-provided IDs — DTOs lack ID-typed fields.
- [x] Expired/manipulated JWT is rejected — existing `JwtAuthorizationFilter`.
- [x] Error messages do not expose stack traces, SQL, or internal class names — `GlobalExceptionHandler` returns `ex.getMessage()` only.
- [x] Logs do not contain raw JWTs, passwords, or excessive personal data — endpoint logs path only.
- [x] User-generated content not logged at INFO — DEBUG only.

---

## Backend Tests

### Unit Tests

- [x] Service test for successful creation — `ShoppingItemServiceTest#givenUserInHousehold_whenCreateItem_thenItemIsAttachedToHouseholdAndUser`.
- [x] Service test for validation/error case — `givenUserWithoutHousehold_whenCreateItem_thenForbidden`.
- [x] Service test for cross-household rejection — `givenItemOfOtherHousehold_whenFindOne_thenForbidden`.
- [x] Service test for mark bought — `givenOwnItem_whenUpdateStatus_thenStatusIsSaved`.
- [x] Service test for update/delete — `givenItemOfOtherCreator_whenDelete_thenForbidden`, `givenMissingItem_whenFindOne_thenNotFound`.

### Repository Tests

- [x] Repository test for household-scoped retrieval — new `ShoppingItemRepositoryTest#givenItemsInTwoHouseholds_whenFindByHouseholdId_thenOnlyOwnHouseholdReturned` (`@DataJpaTest`).
- [x] Repository test for ordering/filtering — new `givenMultipleItems_whenFindByHousehold_thenOrderedByCreatedAtDesc` + `givenItemsFromTwoCreatorsInSameHousehold_whenFindByHouseholdAndCreator_thenOnlyOwnReturned`.

### Endpoint / Integration Tests

- [x] Endpoint test for successful creation — `givenUserInHousehold_whenCreateItem_then201AndAttachedToHousehold`.
- [x] Endpoint test for unauthenticated access — new `givenNoJwt_whenCreate_then401`.
- [x] Endpoint test for invalid body — new `givenBlankName_whenCreate_then400WithValidationErrors`.
- [x] Endpoint test for listing visible items — `givenOwnItems_whenFindAll_thenItemsReturned`.
- [x] Endpoint test for cross-household rejection — `givenItemOfOtherHousehold_whenFindOne_then403`.

---

## Test Execution Evidence

```bash
mvn -B clean test
mvn -B clean verify
```

- [x] Backend tests pass locally — verified via GitLab CI pipeline (green).
- [x] Backend build/verification passes locally — verified via CI.
- [x] Backend CI job passes — pipeline green for the most recent push.

---

## Branch and Merge Readiness

- [x] Branch contains only FR-07 backend changes — verified file diff.
- [x] No generated context files are committed — `.gitignore` updated with proper exclusion patterns for build output, logs, IDE, Cypress artefacts, H2 DB.
- [x] No IDE files are committed — `.vscode/` removed.
- [x] No local logs are committed — `backend.log`, `backend_output.log`, `frontend.log`, `cypress.log`, `test_output.log` all removed.
- [x] No secrets are committed — verified by source-tree grep.
- [x] Merge request describes backend behavior — 6 REST endpoints + auth model + validation behavior documented.

---

## Backend QA Summary

### Passed

- Full backend implementation (entity, status enum, repository, 4 DTOs, service interface + impl, endpoint).
- All 6 REST endpoints implemented with correct verbs/statuses.
- Strong authorization model: `canView`/`canManage` helpers, household derived server-side, server-side ownership assignment.
- Comprehensive tests: 7 service unit + 3 new `@DataJpaTest` repository + 7 integration tests including 401 (no JWT), 400 (blank-name validation), and cross-household 403.
- Integration test has `@AfterEach` cleanup.
- Repo hygiene cleaned up: real `.gitignore`, no `.vscode/`, no `.log` files, no committed Cypress XMLs.
- CI pipeline green.

### Failed / Missing

None.

### Final Backend QA Decision

- [x] APPROVED — backend FR-07 is ready to merge.
- [ ] APPROVED WITH NOTES
- [ ] REJECTED

Decision: **APPROVED**
