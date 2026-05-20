# FR-05 Backend QA Checklist — Shared Expense Tracking

## Feature Information

- Feature ID: FR-05
- Feature name: Shared expense tracking
- Use case: UC-03 — Shared Expense Tracking and Settlement
- Priority: High
- Category: Core
- Version: V2
- Estimated effort: 48h
- Feature lead: Cemil Aslan
- Backend technology: Java Spring Boot
- Security: Spring Security, JWT
- Database: H2 via Spring Data JPA / JPA
- Automated tests: JUnit / integration tests
- Branch:
- Merge request:
- Developer:
- QA reviewer:
- Date:

---

## How to Use This Checklist

This checklist must be completed before the backend part of FR-05 can be considered done.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a short note under the relevant section.
- If something does not apply, write `N/A` with a short reason.
- Do not merge into `dev` while critical items are unchecked.

This checklist is written so that it can be given to an AI assistant or a human reviewer to verify the backend implementation systematically.

---

## Project-Specific Backend Scope

FR-05 belongs to UC-03: Shared Expense Tracking and Settlement.

The backend implementation must allow authenticated WG members to create, read, update, and manage shared expenses. A shared expense records who paid, how much was paid, what the expense was for, which household it belongs to, which roommates participate in the split, and what each participant owes.

FR-05 is **shared expense tracking**, not the full settlement engine. FR-06 covers the payment status overview, and FR-14 covers one-click expense generation from shopping. However, FR-05 must store enough clean and correct data so that FR-06 and later settlement logic can work.

Expected backend areas to inspect:

- Entity/model classes for expenses and splits.
- DTOs for request and response payloads.
- Mapper classes if MapStruct or manual mapping is used.
- Repository classes.
- Service layer/business logic.
- Endpoint/REST API classes.
- Security configuration and method-level authorization.
- Validation logic.
- Database initialization/migration/seed data if relevant.
- Unit and integration tests.
- OpenAPI/Swagger documentation.

---

## 1. Branch, Build, and Baseline Checks

- [ ] Correct branch is checked out: `FR-05/Shared-expense-tracking` or equivalent.
- [ ] Branch is up to date with `origin`.
- [ ] Branch is compared against the correct base branch, normally `dev`.
- [ ] `git status` shows no unrelated local changes before QA starts.
- [ ] Backend compiles successfully.
- [ ] Backend application starts without crashing.
- [ ] Existing backend tests still pass.
- [ ] FR-05 changes do not accidentally remove or break FR-01, FR-02, FR-03, or FR-04 behavior.
- [ ] No unrelated frontend/backend files were changed without explanation.
- [ ] No secrets, JWT secrets, database passwords, API keys, or local credentials are committed.

Suggested commands:

```bash
git branch --show-current
git status
git fetch --all --prune
git diff --name-status dev...HEAD

cd backend
./mvnw clean test
./mvnw spring-boot:run
```

Notes:

-

---

## 2. Requirement Coverage

FR-05 must satisfy the core requirement:

> Users can enter shared expenses, split costs, and track expense records inside their WG.

Check that the backend supports the following:

- [ ] Authenticated users can create a shared expense.
- [ ] Expense has a title/description/category or equivalent meaningful label.
- [ ] Expense stores the total amount.
- [ ] Expense stores the payer.
- [ ] Expense stores the household/WG context.
- [ ] Expense stores the date/time of creation or expense occurrence.
- [ ] Expense stores participants/splits.
- [ ] Expense stores each participant's owed amount or split share.
- [ ] Expense supports equal splitting between selected roommates.
- [ ] Expense supports at least the project’s intended split mode. If only equal split is implemented, this is documented.
- [ ] Expense can be retrieved after creation.
- [ ] Expense list can be retrieved for the current household.
- [ ] Expense details can be retrieved by ID.
- [ ] Expense can be updated where allowed.
- [ ] Expense can be deleted/cancelled where allowed, or deletion is explicitly out of scope and documented.
- [ ] Expense response includes enough information for the frontend to display payer, amount, participants, and shares.
- [ ] Backend does not expose expenses from other households.
- [ ] Backend behavior is documented in API docs or README where necessary.

Notes:

-

---

## 3. Domain Model and Persistence Checks

Inspect entity and repository implementation.

- [ ] Expense entity exists and is persisted.
- [ ] ExpenseSplit or equivalent entity exists and is persisted.
- [ ] Expense has a stable primary key.
- [ ] Expense is linked to exactly one household/WG.
- [ ] Expense is linked to one payer/user/roommate.
- [ ] Expense splits are linked to the expense.
- [ ] Expense splits are linked to participating roommates/users.
- [ ] Expense total amount uses a precise decimal type, not floating-point arithmetic.
- [ ] Monetary values are stored consistently with two decimal places or documented precision.
- [ ] Expense has created/updated timestamps or equivalent audit data.
- [ ] Expense deletion does not leave orphaned split records.
- [ ] Cascade behavior is intentional and safe.
- [ ] Repository queries are household-scoped.
- [ ] Repository queries do not return all expenses globally unless protected internally.
- [ ] Database schema starts cleanly in test and dev profiles.
- [ ] Test data/seeding does not create misleading production-like personal data.

Critical check:

- [ ] There is no use of `double` or `float` for money calculations or persisted money values.

Suggested inspection commands:

```bash
find backend/src/main/java -iname "*Expense*" -o -iname "*Split*" -o -iname "*Payment*"
grep -R "double\|float" backend/src/main/java | grep -i "expense\|amount\|price\|money\|payment" || true
grep -R "BigDecimal" backend/src/main/java | grep -i "expense\|amount\|price\|money\|payment" || true
```

Notes:

-

---

## 4. DTO and API Contract Checks

Inspect request and response DTOs.

- [ ] Create-expense request DTO exists.
- [ ] Request DTO contains required fields only.
- [ ] Request DTO does not allow the client to set trusted server-side fields such as household ID, creator ID, or payer role unless validated.
- [ ] Response DTO contains ID, title/description, amount, payer, participants/splits, timestamps, and status if relevant.
- [ ] DTOs do not expose password hashes, tokens, internal security fields, or unrelated user data.
- [ ] DTO validation annotations are used where appropriate.
- [ ] Invalid DTOs return `400 Bad Request`.
- [ ] API endpoint names are consistent with project style.
- [ ] API responses are consistent with existing backend response style.
- [ ] OpenAPI/Swagger shows the FR-05 endpoints.
- [ ] OpenAPI schemas match the implemented DTOs.
- [ ] Frontend can consume the response without guessing hidden fields.

Suggested checks:

```bash
grep -R "class .*Expense.*Dto\|record .*Expense.*Dto" backend/src/main/java
grep -R "@Valid\|@NotNull\|@NotBlank\|@Positive\|@DecimalMin" backend/src/main/java | grep -i expense
```

Notes:

-

---

## 5. Endpoint Behavior Checks

Verify REST endpoint behavior manually or through integration tests.

Expected endpoint types may include:

- `POST /api/expenses`
- `GET /api/expenses`
- `GET /api/expenses/{id}`
- `PUT/PATCH /api/expenses/{id}`
- `DELETE /api/expenses/{id}`

Use the actual project paths if different.

### Create Expense

- [ ] Authenticated WG member can create an expense.
- [ ] Main Tenant can create an expense.
- [ ] Roommate can create an expense, unless project policy says otherwise.
- [ ] Expense is automatically assigned to the user’s current household.
- [ ] Payer must be a member of the same household.
- [ ] Participants must be members of the same household.
- [ ] Total amount must be positive.
- [ ] Empty title/description is rejected if title is required.
- [ ] Missing participants are rejected or handled according to documented behavior.
- [ ] Duplicate participant IDs are rejected or normalized.
- [ ] Creator cannot create an expense for another household.
- [ ] Creation returns a useful response and correct HTTP status.

### List Expenses

- [ ] Authenticated WG member can list expenses for their household.
- [ ] List contains only expenses from the user’s household.
- [ ] List does not contain expenses from other households.
- [ ] List is ordered predictably, preferably newest first.
- [ ] Empty list returns `200 OK` with `[]`, not an error.
- [ ] Pagination/lazy loading exists if implemented, or current scope is documented.

### Get Expense by ID

- [ ] Existing expense can be retrieved by authorized household member.
- [ ] Unknown expense ID returns `404 Not Found`.
- [ ] Expense from another household returns `403 Forbidden` or `404 Not Found`.
- [ ] Response includes all split information required by the UI.

### Update Expense

- [ ] Authorized user can update allowed fields.
- [ ] Unauthorized user cannot update an expense.
- [ ] Updating amount recalculates or validates splits.
- [ ] Updating participants recalculates or validates splits.
- [ ] Updating does not corrupt existing split records.
- [ ] Updating an expense from another household is forbidden.

### Delete Expense

- [ ] Authorized user can delete/cancel an expense if deletion is supported.
- [ ] Unauthorized user cannot delete/cancel an expense.
- [ ] Expense from another household cannot be deleted.
- [ ] Deletion/cancellation handles split records safely.
- [ ] Deleted/cancelled expenses do not appear as active expenses unless intended.

Notes:

-

---

## 6. Security, Authentication, and Authorization

FR-05 processes financial household data. Backend security must be enforced on the server side.

- [ ] All FR-05 endpoints require authentication.
- [ ] Missing JWT returns `401 Unauthorized`.
- [ ] Invalid JWT returns `401 Unauthorized`.
- [ ] Expired JWT returns `401 Unauthorized`.
- [ ] User can only access expenses belonging to their own household.
- [ ] User cannot create an expense for another household.
- [ ] User cannot include participants from another household.
- [ ] User cannot set another user as payer unless the use case explicitly allows it and validates it.
- [ ] Main Tenant and Roommate role rules are enforced in the backend, not only hidden in the frontend.
- [ ] Sensitive information is not logged.
- [ ] Error messages do not leak whether users from another household exist.
- [ ] Endpoint tests include cross-household access attempts.

Suggested negative tests:

- User A from WG 1 tries to read expense from WG 2.
- User A from WG 1 tries to add User B from WG 2 as split participant.
- User A sends a forged household ID in the request.
- User A sends a forged payer ID in the request.

Notes:

-

---

## 7. Validation and Edge Cases

- [ ] Amount `0` is rejected.
- [ ] Negative amount is rejected.
- [ ] Amount with too many decimals is rounded or rejected deterministically.
- [ ] Very large amount is handled safely or rejected with a clear validation error.
- [ ] Empty/null title is rejected if required.
- [ ] Very long title/description is rejected or safely stored.
- [ ] Special characters in title/description do not break persistence or API response.
- [ ] HTML/script input is stored/displayed safely or sanitized at the correct layer.
- [ ] Participant list cannot be empty unless payer-only expense is explicitly allowed.
- [ ] Participant list cannot contain non-existing user IDs.
- [ ] Participant list cannot contain users from another WG.
- [ ] Duplicate participants are handled safely.
- [ ] Payer may be included in participants if project logic requires it.
- [ ] Payer excluded from participant list is handled according to documented behavior.
- [ ] Date in the future is accepted/rejected according to documented behavior.
- [ ] Date far in the past is accepted/rejected according to documented behavior.
- [ ] Concurrent creation of expenses does not crash the backend.
- [ ] Concurrent updates do not create inconsistent split totals.

Notes:

-

---

## 8. Financial Correctness for Expense Tracking

Even though FR-06 covers payment overview and later settlement, FR-05 must track expense amounts correctly.

- [ ] Total split amounts equal the total expense amount.
- [ ] Equal split with non-divisible cents is handled deterministically.
- [ ] Rounding rule is consistent and documented if rounding occurs.
- [ ] Backend uses `BigDecimal` or equivalent precise decimal handling.
- [ ] Backend avoids binary floating-point arithmetic.
- [ ] API returns monetary values consistently.
- [ ] Updating an expense does not leave old stale split records.
- [ ] Deleting/cancelling an expense removes or ignores related split amounts.
- [ ] Tests cover at least one rounding edge case, e.g. 10.00 split by 3 people.
- [ ] Tests cover at least one normal equal split, e.g. 12.00 split by 3 people.
- [ ] Tests cover at least one single-participant or payer-only edge case if allowed.

Notes:

-

---

## 9. Service Layer and Transaction Checks

- [ ] Expense creation is implemented in a service layer, not directly in the endpoint.
- [ ] Business logic is not placed in the frontend.
- [ ] Business logic is not placed only in repository methods.
- [ ] Creation of expense and splits happens transactionally.
- [ ] If split creation fails, the expense is not partially persisted.
- [ ] If expense update fails, old state remains consistent.
- [ ] If expense deletion fails, no partial deletion occurs.
- [ ] Service checks household membership before modifying data.
- [ ] Service checks payer and participant validity before persistence.
- [ ] Service methods are readable and maintainable.
- [ ] Exceptions are mapped to meaningful HTTP responses.

Suggested inspection:

```bash
grep -R "@Transactional" backend/src/main/java | grep -i expense || true
grep -R "class .*Expense.*Service\|interface .*Expense.*Service" backend/src/main/java
```

Notes:

-

---

## 10. Automated Backend Test Coverage

Check whether the implementation has automated tests for FR-05.

Minimum expected tests:

- [ ] Create expense success case.
- [ ] List expenses success case.
- [ ] Get expense by ID success case.
- [ ] Update expense success case if update is supported.
- [ ] Delete/cancel expense success case if deletion is supported.
- [ ] Missing authentication is rejected.
- [ ] Cross-household read is rejected.
- [ ] Cross-household create/update/delete is rejected.
- [ ] Invalid amount is rejected.
- [ ] Invalid participant is rejected.
- [ ] Split totals equal expense total.
- [ ] Rounding edge case is tested.
- [ ] Repository/service integration test verifies persistence.
- [ ] Endpoint integration test verifies actual REST behavior.
- [ ] Existing tests from older features still pass.

Suggested commands:

```bash
cd backend
./mvnw test
find src/test -iname "*Expense*" -o -iname "*Payment*" -o -iname "*Settlement*"
grep -R "expense" src/test || true
```

Notes:

-

---

## 11. Integration with Existing Features

- [ ] Expense tracking only works after login/registration.
- [ ] Expense tracking only works for users who are part of a WG.
- [ ] WG creation/invitation logic still works.
- [ ] Roommate membership data is reused correctly.
- [ ] Existing task/cleaning schedule features are not broken.
- [ ] Existing household security rules are reused.
- [ ] Dashboard or future dashboard data can consume expense data if already integrated.
- [ ] Payment status overview can be built on the stored FR-05 data.
- [ ] Shopping module/one-click expense generation is not accidentally mixed into FR-05 unless documented.
- [ ] No circular dependencies or messy package coupling are introduced.

Notes:

-

---

## 12. Error Handling and API Consistency

- [ ] Validation errors return `400 Bad Request`.
- [ ] Missing authentication returns `401 Unauthorized`.
- [ ] Authenticated but unauthorized household access returns `403 Forbidden` or safe `404 Not Found`.
- [ ] Unknown expense ID returns `404 Not Found`.
- [ ] Error response format matches existing project conventions.
- [ ] Backend does not return raw stack traces.
- [ ] Backend logs useful technical errors without exposing sensitive financial/user data.
- [ ] Frontend receives enough error information to show a useful message.
- [ ] All exceptions are handled by existing global exception handling where possible.

Notes:

-

---

## 13. OpenAPI / Swagger Documentation

- [ ] FR-05 endpoints are visible in Swagger/OpenAPI.
- [ ] Request schemas are correct.
- [ ] Response schemas are correct.
- [ ] HTTP status codes are documented.
- [ ] Validation constraints are visible where possible.
- [ ] Security requirement/JWT is documented for endpoints.
- [ ] Example payloads are present or endpoint behavior is understandable.
- [ ] Swagger docs work when the backend is running.

Suggested manual check:

```text
Start backend and open the Swagger/OpenAPI page.
Check all expense-related endpoints.
Try creating and retrieving an expense through Swagger if authentication support allows it.
```

Notes:

-

---

## 14. Maintainability and Code Quality

- [ ] Package structure follows existing backend conventions.
- [ ] Naming is consistent with the rest of the project.
- [ ] Code avoids duplicated financial logic.
- [ ] Business rules are centralized in service methods.
- [ ] Methods are reasonably small and readable.
- [ ] DTO/entity mapping is clear.
- [ ] No dead code or commented-out debug code remains.
- [ ] No `System.out.println` debugging remains.
- [ ] No TODOs remain for required FR-05 functionality.
- [ ] No unrelated refactors are mixed into the feature without explanation.
- [ ] Code formatting matches project style.
- [ ] Static analysis/linting passes if configured.

Notes:

-

---

## 15. Backend QA Decision

### Critical Blockers

Mark as blocker if any of these fail:

- [ ] Backend does not compile.
- [ ] Backend application does not start.
- [ ] Existing tests fail because of FR-05.
- [ ] Expenses can be accessed across households.
- [ ] Money values use `double`/`float`.
- [ ] Invalid or forged payer/participant data is accepted.
- [ ] Unauthenticated users can access expense endpoints.
- [ ] Split totals do not match the expense total.
- [ ] Expense creation can partially persist broken data.

### Final Backend Result

- [ ] PASS — Backend FR-05 is ready for merge.
- [ ] PASS WITH MINOR NOTES — Backend FR-05 works, but minor cleanup/documentation is needed.
- [ ] FAIL — Backend FR-05 is not ready for merge.

Final notes:

-

QA reviewer:

Date:
