# FR-02 Backend QA Checklist — Secure WG Creation & Invitations

## Feature Information

- Feature ID: FR-02
- Feature name: Secure WG creation & invitations
- Use case: UC-01 — User Registration and WG Initialization
- Priority: High
- Category: Core
- Version: V1
- Backend technology: Java Spring Boot
- Security: Spring Security, JWT, RBAC
- Database: PostgreSQL
- Automated tests: JUnit
- Branch:
- Merge request:
- Developer:
- QA reviewer:
- Date:

---

## How to Use This Checklist

This checklist must be completed before the backend part of FR-02 can be considered done.

For each item:

- `[ ]` means not checked yet.
- `[x]` means checked and passed.
- If something fails, leave it unchecked and write a short note under **QA Notes / Evidence**.
- Every checked item should be backed by one of the following:
  - an automated test,
  - a manual API test,
  - a code review finding,
  - a screenshot,
  - a log excerpt,
  - or a short explanation.

The goal is that after this checklist is completed, the backend implementation of FR-02 fully supports secure WG creation and secure invitations.

---

# 1. Scope Verification

## 1.1 Feature Scope

- [ ] The backend allows an authenticated user to create a new WG.
- [ ] The backend stores the WG persistently in PostgreSQL.
- [ ] The creator of the WG is automatically connected to that WG.
- [ ] The creator receives the correct role inside the WG, for example `MAIN_TENANT`, `ADMIN`, or equivalent.
- [ ] The backend allows authorized users to create invitation links for their WG.
- [ ] Invitation links are time-limited.
- [ ] Invitation links contain a secure token that cannot be guessed.
- [ ] A valid invitation can be accepted by another user.
- [ ] Accepting an invitation connects the accepting user to the correct WG.
- [ ] The feature does not accidentally include unrelated functionality from later features.

## 1.2 Out-of-Scope Check

- [ ] The backend does not implement cleaning tasks as part of FR-02.
- [ ] The backend does not implement expenses as part of FR-02.
- [ ] The backend does not implement shopping lists as part of FR-02.
- [ ] The backend does not implement notifications unless they are strictly needed for invitation delivery.
- [ ] The backend does not implement full email infrastructure unless the team explicitly decided this is part of FR-02.

---

# 2. API Endpoint Verification

Update endpoint paths if the actual implementation uses different names.

## 2.1 WG Creation Endpoint

Expected endpoint example:

```http
POST /api/wgs
```

- [ ] Endpoint exists.
- [ ] Endpoint requires authentication.
- [ ] Endpoint rejects unauthenticated requests with `401 Unauthorized`.
- [ ] Endpoint accepts a valid WG creation request.
- [ ] Endpoint rejects invalid WG names.
- [ ] Endpoint rejects empty WG names.
- [ ] Endpoint rejects blank-only WG names.
- [ ] Endpoint trims leading and trailing whitespace if this is the intended behavior.
- [ ] Endpoint validates maximum WG name length.
- [ ] Endpoint returns a suitable success status, for example `201 Created`.
- [ ] Endpoint returns the created WG ID.
- [ ] Endpoint returns the created WG name.
- [ ] Endpoint does not expose internal database details.
- [ ] Endpoint creates a membership entry connecting the creator to the WG.
- [ ] Endpoint assigns the creator the correct WG role.
- [ ] Endpoint behaves correctly if the user already belongs to a WG.
- [ ] Endpoint behavior for “one user may only have one active WG” is clearly implemented if that is the domain rule.
- [ ] Endpoint behavior for “one user may create multiple WGs” is clearly implemented if that is the domain rule.

## 2.2 Invitation Creation Endpoint

Expected endpoint example:

```http
POST /api/wgs/{wgId}/invitations
```

- [ ] Endpoint exists.
- [ ] Endpoint requires authentication.
- [ ] Endpoint rejects unauthenticated requests with `401 Unauthorized`.
- [ ] Endpoint checks that the authenticated user belongs to the WG.
- [ ] Endpoint checks that the authenticated user has permission to invite roommates.
- [ ] Endpoint rejects requests from users who are not part of the target WG.
- [ ] Endpoint rejects requests from users who are only normal roommates if only main tenants/admins may invite.
- [ ] Endpoint rejects invitations for non-existing WG IDs.
- [ ] Endpoint creates an invitation for the correct WG.
- [ ] Endpoint creates a unique invitation token.
- [ ] Endpoint sets an expiration timestamp.
- [ ] Endpoint returns an invitation link or token in a controlled way.
- [ ] Endpoint does not return sensitive internal fields.
- [ ] Endpoint does not expose raw user password, JWT secrets, or internal security values.
- [ ] Endpoint handles duplicate invitation requests consistently.
- [ ] Endpoint handles invitation by email if email-based invitation is implemented.
- [ ] Endpoint validates email format if email-based invitation is implemented.
- [ ] Endpoint does not allow inviting an already existing WG member unless explicitly intended.
- [ ] Endpoint does not allow privilege escalation through invitation creation.

## 2.3 Invitation Validation Endpoint

Expected endpoint example:

```http
GET /api/invitations/{token}
```

- [ ] Endpoint exists if the frontend needs invitation preview/validation.
- [ ] Endpoint accepts a token and checks whether it exists.
- [ ] Endpoint rejects invalid tokens.
- [ ] Endpoint rejects expired tokens.
- [ ] Endpoint rejects revoked tokens.
- [ ] Endpoint rejects already-used tokens if invitations are single-use.
- [ ] Endpoint returns safe preview data only, for example WG name.
- [ ] Endpoint does not leak private WG information.
- [ ] Endpoint does not reveal whether a user email is registered unless intended.
- [ ] Endpoint does not return the raw database invitation object.
- [ ] Endpoint uses consistent error responses.

## 2.4 Invitation Acceptance Endpoint

Expected endpoint example:

```http
POST /api/invitations/{token}/accept
```

- [ ] Endpoint exists.
- [ ] Endpoint requires authentication if only registered users can join a WG.
- [ ] Endpoint rejects unauthenticated users with `401 Unauthorized`.
- [ ] Endpoint accepts a valid, non-expired, non-revoked invitation.
- [ ] Endpoint connects the accepting user to the correct WG.
- [ ] Endpoint assigns the accepting user the correct role, for example `ROOMMATE`.
- [ ] Endpoint does not allow a user to accept the same invitation twice.
- [ ] Endpoint does not allow two users to accept the same single-use invitation.
- [ ] Endpoint marks the invitation as used if invitations are single-use.
- [ ] Endpoint stores the accepted timestamp if supported.
- [ ] Endpoint rejects expired invitations.
- [ ] Endpoint rejects revoked invitations.
- [ ] Endpoint rejects malformed tokens.
- [ ] Endpoint rejects tokens with path traversal or special-character abuse.
- [ ] Endpoint handles the case where the accepting user is already in the WG.
- [ ] Endpoint handles the case where the accepting user is already in another WG.
- [ ] Endpoint does not allow the accepting user to choose their own role.
- [ ] Endpoint does not allow joining a WG by changing the WG ID in the request.
- [ ] Endpoint returns a suitable success response.

## 2.5 Invitation Revocation Endpoint

Expected endpoint example:

```http
DELETE /api/wgs/{wgId}/invitations/{invitationId}
```

- [ ] Endpoint exists if invitation revocation is implemented.
- [ ] Endpoint requires authentication.
- [ ] Endpoint checks WG membership.
- [ ] Endpoint checks invitation-management permission.
- [ ] Endpoint rejects normal roommates if only main tenants/admins may revoke invitations.
- [ ] Endpoint revokes only invitations belonging to the correct WG.
- [ ] Endpoint prevents IDOR by blocking access to invitations from other WGs.
- [ ] Revoked invitations can no longer be accepted.
- [ ] Revoked invitations can no longer be validated as active.

---

# 3. Data Model and Persistence

## 3.1 WG Entity

- [ ] WG entity exists.
- [ ] WG has a stable primary key.
- [ ] WG has a name or equivalent household identifier.
- [ ] WG stores creation timestamp if supported by project conventions.
- [ ] WG stores update timestamp if supported by project conventions.
- [ ] WG fields are validated at entity or service level.
- [ ] WG database table has appropriate constraints.
- [ ] WG database table does not allow invalid required fields to be null.
- [ ] WG name uniqueness behavior is clearly defined.
- [ ] WG deletion behavior is not accidentally implemented in a dangerous way.

## 3.2 Membership Entity

- [ ] There is a relationship between users and WGs.
- [ ] The creator is inserted as a member of the created WG.
- [ ] The creator receives the correct role.
- [ ] Invited users receive the correct role.
- [ ] Duplicate memberships are prevented.
- [ ] Membership table has a unique constraint for `(user_id, wg_id)` or equivalent.
- [ ] Membership role is stored safely.
- [ ] Membership role cannot be manipulated from untrusted request body fields.
- [ ] Membership creation is transactional together with WG creation.
- [ ] Invitation acceptance creates membership transactionally.

## 3.3 Invitation Entity

- [ ] Invitation entity exists if invitations are persisted.
- [ ] Invitation belongs to exactly one WG.
- [ ] Invitation stores creation timestamp.
- [ ] Invitation stores expiration timestamp.
- [ ] Invitation stores accepted/used state if invitations are single-use.
- [ ] Invitation stores revoked state if revocation is supported.
- [ ] Invitation stores invited email if email-based invitations are implemented.
- [ ] Invitation token is unique.
- [ ] Invitation token is sufficiently long and random.
- [ ] Raw token is not stored in plain text if the team chose hashed-token storage.
- [ ] Expired invitations are not accepted.
- [ ] Used invitations are not accepted again.
- [ ] Revoked invitations are not accepted.
- [ ] Database constraints prevent duplicate active invitation misuse where necessary.

---

# 4. Security Verification

## 4.1 Authentication

- [ ] WG creation requires a valid JWT.
- [ ] Invitation creation requires a valid JWT.
- [ ] Invitation acceptance requires a valid JWT if only registered users may join.
- [ ] Expired JWTs are rejected.
- [ ] Invalid JWTs are rejected.
- [ ] Missing JWTs are rejected.
- [ ] JWT identity is used from the security context, not from user-controlled request body fields.
- [ ] A user cannot create a WG on behalf of another user.
- [ ] A user cannot accept an invitation on behalf of another user.

## 4.2 Authorization / RBAC

- [ ] Only authorized users can create invitations for a WG.
- [ ] Users cannot invite people to WGs they do not belong to.
- [ ] Users cannot revoke invitations for WGs they do not belong to.
- [ ] Users cannot view invitations for WGs they do not belong to.
- [ ] Users cannot access WG management endpoints by changing path IDs.
- [ ] Normal roommates cannot perform main-tenant-only actions if the role model distinguishes them.
- [ ] Main tenant/admin permissions are checked server-side.
- [ ] Frontend hiding of buttons is not relied on as security.

## 4.3 Invitation Token Security

- [ ] Tokens are generated using a cryptographically secure random generator.
- [ ] Tokens have enough entropy.
- [ ] Tokens are not predictable.
- [ ] Tokens are not based on sequential IDs.
- [ ] Tokens are not based on user IDs.
- [ ] Tokens are not based only on timestamps.
- [ ] Tokens expire after the configured validity period.
- [ ] Tokens are single-use if that is the intended behavior.
- [ ] Token validation uses constant or safe comparison where relevant.
- [ ] Invalid token responses do not reveal too much information.
- [ ] Tokens are not logged in production logs.
- [ ] Tokens are not included in error stack traces.
- [ ] Tokens are not exposed through unrelated endpoints.

## 4.4 IDOR and Access-Control Abuse

- [ ] User A cannot create invitations for User B's WG.
- [ ] User A cannot list invitations of User B's WG.
- [ ] User A cannot revoke invitations of User B's WG.
- [ ] User A cannot accept an invitation and force membership into a different WG by modifying the request.
- [ ] Changing `wgId` in the URL does not bypass authorization.
- [ ] Changing `userId` in the request body does not bypass authorization.
- [ ] Changing `role` in the request body does not create admin/main-tenant privileges.
- [ ] Backend ignores or rejects client-supplied role fields where they are not allowed.

## 4.5 Input Validation and Abuse Cases

- [ ] WG name cannot be empty.
- [ ] WG name cannot be only whitespace.
- [ ] WG name cannot exceed the maximum length.
- [ ] WG name with HTML or script content is safely handled.
- [ ] Invitation email cannot be empty if required.
- [ ] Invitation email must have valid format if required.
- [ ] Invitation token cannot crash the backend when extremely long.
- [ ] Invitation token cannot crash the backend when malformed.
- [ ] API rejects invalid JSON bodies.
- [ ] API rejects unsupported content types if relevant.
- [ ] API returns validation errors in a consistent format.
- [ ] No stack traces are returned to the client.

---

# 5. Business Logic Verification

## 5.1 WG Creation Rules

- [ ] Creating a WG creates exactly one WG record.
- [ ] Creating a WG creates exactly one creator membership record.
- [ ] Creator role is correct.
- [ ] Creator does not need a separate invitation to join their own WG.
- [ ] WG creation is atomic.
- [ ] If membership creation fails, WG creation is rolled back.
- [ ] If WG creation fails, no membership is created.
- [ ] Duplicate WG creation by the same request does not create inconsistent state.
- [ ] Backend handles repeated submit/click cases gracefully.

## 5.2 Invitation Creation Rules

- [ ] Invitation belongs to the WG of the authenticated authorized user.
- [ ] Invitation expiry is calculated correctly.
- [ ] Invitation expiry uses server time, not client time.
- [ ] Invitation expiry handles timezone consistently.
- [ ] Invitation token is generated on the server.
- [ ] Invitation cannot be created for a deleted or inactive WG.
- [ ] Invitation cannot grant higher privileges than intended.
- [ ] Invitation cannot assign arbitrary roles unless explicitly allowed and checked.
- [ ] Invitation status is initially active/pending.
- [ ] Invitation status transitions are correct.

## 5.3 Invitation Acceptance Rules

- [ ] Accepting a valid invitation creates membership.
- [ ] Accepting a valid invitation marks the invitation as used if single-use.
- [ ] Accepting an expired invitation fails.
- [ ] Accepting a revoked invitation fails.
- [ ] Accepting a used invitation fails.
- [ ] Accepting an invitation for a non-existing WG fails safely.
- [ ] Accepting an invitation into a WG where the user is already a member is handled consistently.
- [ ] Accepting an invitation while already belonging to another WG is handled according to project rules.
- [ ] The accepting user cannot choose the WG manually.
- [ ] The accepting user cannot choose their role manually.
- [ ] Concurrent acceptance of the same invitation does not create duplicate memberships.

---

# 6. Error Handling

## 6.1 Expected Error Cases

- [ ] Unauthenticated WG creation returns `401`.
- [ ] Invalid WG creation body returns `400`.
- [ ] Forbidden invitation creation returns `403`.
- [ ] Non-existing WG returns `404` or equivalent agreed response.
- [ ] Invalid invitation token returns `404`, `400`, or equivalent agreed response.
- [ ] Expired invitation returns a clear error.
- [ ] Revoked invitation returns a clear error.
- [ ] Already-used invitation returns a clear error.
- [ ] Duplicate membership attempt returns a clear error.
- [ ] Database constraint errors are translated into safe API errors.
- [ ] Unexpected exceptions are handled by global exception handling.
- [ ] Error format is consistent with the rest of the backend.

## 6.2 Error Response Safety

- [ ] Error responses do not expose stack traces.
- [ ] Error responses do not expose SQL details.
- [ ] Error responses do not expose JWT internals.
- [ ] Error responses do not expose raw invitation tokens unnecessarily.
- [ ] Error responses do not expose private user data.
- [ ] Error messages are understandable enough for the frontend.

---

# 7. Automated Backend Tests

## 7.1 Unit Tests

- [ ] Unit test: WG creation with valid data succeeds.
- [ ] Unit test: WG creation rejects invalid name.
- [ ] Unit test: creator membership is created.
- [ ] Unit test: creator receives correct role.
- [ ] Unit test: invitation token generation creates unique tokens.
- [ ] Unit test: invitation token generation creates sufficiently long tokens.
- [ ] Unit test: invitation expiry is calculated correctly.
- [ ] Unit test: expired invitation cannot be accepted.
- [ ] Unit test: revoked invitation cannot be accepted.
- [ ] Unit test: used invitation cannot be accepted twice.
- [ ] Unit test: accepting invitation creates roommate membership.
- [ ] Unit test: accepting invitation does not grant admin/main-tenant role.
- [ ] Unit test: duplicate membership is rejected or handled safely.
- [ ] Unit test: service layer uses authenticated user identity, not request-provided user ID.

## 7.2 Repository Tests

- [ ] Repository test: WG is saved correctly.
- [ ] Repository test: membership is saved correctly.
- [ ] Repository test: invitation is saved correctly.
- [ ] Repository test: invitation can be found by token or token hash.
- [ ] Repository test: expired invitations can be detected.
- [ ] Repository test: duplicate memberships are prevented.
- [ ] Repository test: duplicate invitation tokens are prevented.
- [ ] Repository test: membership lookup by user and WG works.
- [ ] Repository test: authorization helper queries return correct results.

## 7.3 Controller / Integration Tests

- [ ] Integration test: authenticated user can create WG.
- [ ] Integration test: unauthenticated user cannot create WG.
- [ ] Integration test: authenticated WG creator can create invitation.
- [ ] Integration test: unauthenticated user cannot create invitation.
- [ ] Integration test: user from another WG cannot create invitation.
- [ ] Integration test: normal roommate cannot create invitation if permission is main-tenant-only.
- [ ] Integration test: invitation preview works for valid token.
- [ ] Integration test: invitation preview rejects invalid token.
- [ ] Integration test: invitation preview rejects expired token.
- [ ] Integration test: authenticated user can accept valid invitation.
- [ ] Integration test: expired invitation cannot be accepted.
- [ ] Integration test: revoked invitation cannot be accepted.
- [ ] Integration test: used invitation cannot be accepted twice.
- [ ] Integration test: user cannot force admin role through invitation acceptance.
- [ ] Integration test: IDOR attempt with another WG ID is blocked.
- [ ] Integration test: malformed request body returns validation error.

## 7.4 Security Tests

- [ ] Security test: missing JWT returns `401`.
- [ ] Security test: invalid JWT returns `401`.
- [ ] Security test: expired JWT returns `401`.
- [ ] Security test: authenticated but unauthorized action returns `403`.
- [ ] Security test: role escalation through request body is blocked.
- [ ] Security test: user cannot access another WG's invitations.
- [ ] Security test: user cannot revoke another WG's invitations.
- [ ] Security test: user cannot create invitations for another WG.
- [ ] Security test: invitation token is not leaked in logs during tests.
- [ ] Security test: endpoint security annotations or config match intended access rules.

---

# 8. Manual API Testing

Use Postman, Insomnia, curl, HTTPie, or Swagger if available.

## 8.1 Happy Path

- [ ] Register or use User A.
- [ ] Login as User A and obtain JWT.
- [ ] Create a WG as User A.
- [ ] Confirm WG was created in the database.
- [ ] Confirm User A is member of the WG.
- [ ] Confirm User A has main tenant/admin role.
- [ ] Create invitation as User A.
- [ ] Confirm invitation response contains expected link/token.
- [ ] Register or use User B.
- [ ] Login as User B and obtain JWT.
- [ ] Accept invitation as User B.
- [ ] Confirm User B is member of the same WG.
- [ ] Confirm User B has roommate role.
- [ ] Confirm invitation can no longer be reused if single-use.

## 8.2 Negative Path

- [ ] Try creating WG without JWT.
- [ ] Try creating WG with invalid WG name.
- [ ] Try creating invitation without JWT.
- [ ] Try creating invitation for another user's WG.
- [ ] Try accepting an invalid invitation token.
- [ ] Try accepting an expired invitation token.
- [ ] Try accepting a revoked invitation token.
- [ ] Try accepting an already-used invitation token.
- [ ] Try modifying request body to assign admin role.
- [ ] Try modifying URL path to target a different WG.
- [ ] Try malformed JSON.
- [ ] Try very long token.
- [ ] Try token with unusual characters.

---

# 9. Database Verification

## 9.1 After WG Creation

- [ ] WG row exists.
- [ ] Creator user row still exists and is unchanged.
- [ ] Membership row exists.
- [ ] Membership role is correct.
- [ ] No duplicate membership row exists.
- [ ] Created timestamps are present if implemented.
- [ ] No unrelated tables are changed unexpectedly.

## 9.2 After Invitation Creation

- [ ] Invitation row exists.
- [ ] Invitation references correct WG.
- [ ] Invitation has active/pending state.
- [ ] Invitation has expiration timestamp.
- [ ] Token or token hash is stored.
- [ ] Invitation does not store unnecessary sensitive data.
- [ ] Created timestamp is present if implemented.

## 9.3 After Invitation Acceptance

- [ ] User B membership row exists.
- [ ] User B is connected to correct WG.
- [ ] User B role is correct.
- [ ] Invitation state is changed to used/accepted if implemented.
- [ ] Accepted timestamp is stored if implemented.
- [ ] No duplicate membership exists.
- [ ] No incorrect WG membership was created.

---

# 10. Non-Functional Backend Requirements

## 10.1 Security

- [ ] Backend enforces authentication and authorization server-side.
- [ ] Invitation token design is secure enough for a public link.
- [ ] Sensitive values are not logged.
- [ ] Error messages are safe.
- [ ] Access checks are tested.
- [ ] There is no obvious IDOR vulnerability.
- [ ] There is no obvious privilege-escalation vulnerability.
- [ ] Backend does not rely on frontend-only checks.

## 10.2 Reliability

- [ ] WG creation is transactional.
- [ ] Invitation acceptance is transactional.
- [ ] Concurrent invitation acceptance cannot corrupt data.
- [ ] Duplicate requests do not create inconsistent state.
- [ ] Database constraints protect critical invariants.
- [ ] Backend returns predictable errors for expected failure cases.

## 10.3 Maintainability

- [ ] Controller logic is not overloaded with business logic.
- [ ] Business logic is in a service layer.
- [ ] DTOs are used for request and response bodies.
- [ ] Entity objects are not exposed directly if project convention avoids this.
- [ ] Validation annotations or validation logic are clear.
- [ ] Method names clearly describe WG/invitation behavior.
- [ ] Code follows project package structure.
- [ ] Code follows existing naming conventions.
- [ ] No large commented-out code blocks remain.
- [ ] No debug `System.out.println` statements remain.

## 10.4 Performance

- [ ] WG creation performs a reasonable number of database queries.
- [ ] Invitation creation performs a reasonable number of database queries.
- [ ] Invitation acceptance performs a reasonable number of database queries.
- [ ] Necessary indexes exist for token/token-hash lookup.
- [ ] Necessary indexes exist for membership lookup.
- [ ] No obviously expensive full-table scans are required for normal invitation acceptance.

---

# 11. API Documentation

- [ ] API endpoints are documented.
- [ ] Required authentication is documented.
- [ ] Required roles are documented.
- [ ] Request body for WG creation is documented.
- [ ] Response body for WG creation is documented.
- [ ] Request body for invitation creation is documented.
- [ ] Response body for invitation creation is documented.
- [ ] Invitation acceptance behavior is documented.
- [ ] Error cases are documented.
- [ ] Expiration behavior is documented.
- [ ] Any assumptions are documented, especially:
  - whether one user can belong to multiple WGs,
  - whether invitations are single-use,
  - who can invite roommates,
  - whether email delivery is implemented or only invitation-link generation.

---

# 12. Code Coverage Expectations

This checklist is intended to produce the following code-oriented coverage once the tests are implemented and measured.

## 12.1 Backend Code Areas That Should Be Covered

- [ ] Controller methods for WG creation.
- [ ] Controller methods for invitation creation.
- [ ] Controller methods for invitation validation/preview if implemented.
- [ ] Controller methods for invitation acceptance.
- [ ] Controller methods for invitation revocation if implemented.
- [ ] Service method for WG creation.
- [ ] Service method for creator membership creation.
- [ ] Service method for invitation token generation.
- [ ] Service method for invitation expiry validation.
- [ ] Service method for invitation acceptance.
- [ ] Service method for role assignment.
- [ ] Service method for duplicate membership prevention.
- [ ] Repository methods for WG lookup.
- [ ] Repository methods for membership lookup.
- [ ] Repository methods for invitation lookup.
- [ ] Exception handling paths for invalid input, unauthorized access, forbidden access, expired invitation, and duplicate membership.
- [ ] Security configuration for protected endpoints.
- [ ] DTO validation rules.

## 12.2 Recommended Coverage Targets

- [ ] Service/business logic line coverage target: at least 85%.
- [ ] Service/business logic branch coverage target: at least 75%.
- [ ] Controller/API line coverage target: at least 70%.
- [ ] Repository/query coverage: all custom queries covered at least once.
- [ ] Security-sensitive paths: 100% of intended allow/deny cases tested behaviorally.
- [ ] Critical invitation lifecycle paths: valid, invalid, expired, used, revoked, duplicate, unauthorized.

---

# 13. Definition of Done — Backend

The backend part of FR-02 is done only if all of the following are true:

- [ ] Authenticated users can create WGs.
- [ ] WG creators are correctly connected to their WG.
- [ ] WG creators receive the correct role.
- [ ] Authorized users can create secure invitations.
- [ ] Invitations are time-limited.
- [ ] Invitations cannot be guessed.
- [ ] Invalid, expired, revoked, and used invitations are rejected.
- [ ] Valid invitations can be accepted.
- [ ] Accepting an invitation connects the user to the correct WG.
- [ ] Authorization checks prevent access to other WGs.
- [ ] Role escalation is blocked.
- [ ] Core backend tests pass.
- [ ] Security tests pass.
- [ ] Database state is correct after all main flows.
- [ ] API documentation is updated.
- [ ] CI pipeline passes.
- [ ] QA reviewer has checked this file.

---

# 14. QA Notes / Evidence

Use this section to record proof.

## Automated Test Evidence

- Test command:
- Result:
- Relevant test classes:
- Coverage tool:
- Line coverage:
- Branch coverage:

## Manual API Evidence

- Tool used:
- Tested endpoints:
- Result:

## Database Evidence

- Tables checked:
- Result:

## Security Evidence

- Tested abuse cases:
- Result:

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
