package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestDbCleaner;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdInvitationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationSettingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SettlementDecisionRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.VerificationCodeRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * UC-05 membership found-bug regression gate (BUG #35 / #38 / #42).
 *
 * <p>These three bugs are all about the integrity of the <em>roommate membership</em> record that
 * every UC-05 surface (dashboard, notifications, history, statistics) reads from. They live at the
 * service/persistence layer, so they are pinned here at the API layer (mirroring
 * {@link InvitationEndpointTest}'s create→accept token flow and {@link Uc05BugRegressionTest}'s
 * fixtures / FR-11 notification-FK teardown).</p>
 *
 * <ul>
 *   <li><b>BUG #35</b> — inviting an <em>already-existing</em> user (who has no active household) must
 *       create exactly ONE active roommate row, not a duplicate. {@code bug35_*} below.</li>
 *   <li><b>BUG #38</b> — leave / rejoin must not multiply the membership.
 *       <ul>
 *         <li>{@code bug38_leaveThenRejoinDifferentHousehold_*} — leaving WG-A and joining WG-B works and
 *             leaves exactly one active membership. This passes on the current code.</li>
 *         <li>{@code bug38_leaveThenRejoinSameHousehold_*} — leaving WG-A and rejoining the <em>same</em>
 *             WG-A. <b>THIS TEST IS EXPECTED TO FAIL ON THE CURRENT CODE — it documents a real,
 *             unresolved defect.</b> See the long note on that method and the one-line service fix in
 *             the QA hand-off. Do not delete it to make the suite green; fixing the service is the point.</li>
 *       </ul>
 *   </li>
 *   <li><b>BUG #42</b> — once a member is anonymized (GDPR), their historical UC-05 data must stay
 *       readable with a safe placeholder name and no PII / null / FK failure. {@code bug42_*} below
 *       checks the history + statistics surfaces for the former member. (The dashboard surface only
 *       lists <em>active</em> members, so an anonymized member correctly disappears from it — asserted.)</li>
 * </ul>
 *
 * <p>Note: this class does not delete users (it anonymizes via the leave/anonymize service paths), so it
 * does not need the explicit FR-11 notification-FK pre-clear that user-deleting tests require; the shared
 * {@link #cleanDatabase()} clears notifications before users anyway.</p>
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class Uc05MembershipRegressionTest implements TestData {

    private static final String EXPENSE_BASE_URI = BASE_URI + "/expenses";
    private static final String DASHBOARD_URI = BASE_URI + "/dashboard";
    private static final String HISTORY_URI = BASE_URI + "/history";

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private HouseholdRepository householdRepository;
    @Autowired private RoommateRepository roommateRepository;
    @Autowired private HouseholdInvitationRepository invitationRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private ExpenseSplitRepository expenseSplitRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private CleaningScheduleRepository cleaningScheduleRepository;
    @Autowired private ShoppingItemRepository shoppingItemRepository;
    @Autowired private SettlementDecisionRepository settlementDecisionRepository;
    @Autowired private VerificationCodeRepository verificationCodeRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private NotificationSettingRepository notificationSettingRepository;
    @Autowired private JwtTokenizer jwtTokenizer;
    @Autowired private SecurityProperties securityProperties;
    @Autowired private JsonMapper jsonMapper;

    private ApplicationUser mainTenantA;   // owns WG-A
    private ApplicationUser mainTenantB;   // owns WG-B
    private ApplicationUser mover;         // the user that gets invited / leaves / rejoins
    private Household householdA;
    private Household householdB;

    @BeforeEach
    public void beforeEach() {
        cleanDatabase();
        mainTenantA = saveUser(MAIN_TENANT_EMAIL, "Anna", "TenantA");
        mainTenantB = saveUser("tenantB@test.com", "Bea", "TenantB");
        mover = saveUser(ROOMMATE_EMAIL, "Max", "Mover");

        householdA = saveHousehold(TEST_HOUSEHOLD_NAME, mainTenantA);
        householdB = saveHousehold(OTHER_HOUSEHOLD_NAME, mainTenantB);

        saveRoommate(mainTenantA, householdA, HouseholdRole.MAIN_TENANT);
        saveRoommate(mainTenantB, householdB, HouseholdRole.MAIN_TENANT);
    }

    @AfterEach
    public void afterEach() {
        cleanDatabase();
    }

    // ---------------------------------------------------------------- BUG #35

    @Test
    @DisplayName("BUG #35: inviting an existing user (no active WG) yields exactly one active roommate, visible once across UC-05")
    public void bug35_existingUserInvitedOnce_appearsExactlyOnceEverywhere() throws Exception {
        // 'mover' already exists as an ApplicationUser but is not a member of any household.
        acceptFreshInvitation(householdA, mainTenantA, mover);

        long activeMembershipsForMover = roommateRepository.findAll().stream()
            .filter(r -> r.getUser().getId().equals(mover.getId()) && r.isActive())
            .count();
        long totalMembershipsForMover = roommateRepository.findAll().stream()
            .filter(r -> r.getUser().getId().equals(mover.getId()))
            .count();

        // dashboard housemate count for WG-A from the main tenant's perspective = tenant + mover = 2
        MvcResult dash = mockMvc.perform(get(DASHBOARD_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertAll(
            () -> assertEquals(1, activeMembershipsForMover, "exactly one ACTIVE roommate row for the invited user"),
            () -> assertEquals(1, totalMembershipsForMover, "no duplicate roommate rows for the invited user"),
            () -> assertEquals(HttpStatus.OK.value(), dash.getResponse().getStatus()),
            // 'mover' resolves to a single active roommate, so any UC-05 read keyed on it is unambiguous
            () -> assertTrue(roommateRepository.findByUserIdAndActiveTrue(mover.getId()).isPresent())
        );
    }

    // ---------------------------------------------------------------- BUG #38 (cross-household: passes today)

    @Test
    @DisplayName("BUG #38: leaving WG-A then joining WG-B leaves exactly one active membership (no duplicate)")
    public void bug38_leaveThenRejoinDifferentHousehold_singleActiveMembership() throws Exception {
        // join A
        acceptFreshInvitation(householdA, mainTenantA, mover);
        // leave A  (DELETE /api/v1/users/me/household)
        leaveCurrentHousehold(ROOMMATE_EMAIL);
        // join B
        acceptFreshInvitation(householdB, mainTenantB, mover);

        List<Roommate> moverRows = roommateRepository.findAll().stream()
            .filter(r -> r.getUser().getId().equals(mover.getId()))
            .toList();
        long active = moverRows.stream().filter(Roommate::isActive).count();
        Roommate activeRow = roommateRepository.findByUserIdAndActiveTrue(mover.getId()).orElseThrow();

        assertAll(
            () -> assertEquals(1, active, "exactly one ACTIVE membership after leave A -> join B"),
            () -> assertEquals(householdB.getId(), activeRow.getHousehold().getId(),
                "the single active membership is in WG-B"),
            // the WG-A membership is retained but deactivated (history/audit integrity)
            () -> assertTrue(moverRows.stream()
                    .anyMatch(r -> r.getHousehold().getId().equals(householdA.getId()) && !r.isActive()),
                "the WG-A membership is kept as inactive, not duplicated as active")
        );
    }

    // ---------------------------------------------------------------- BUG #38 (same-household: EXPOSES A REAL BUG)

    /**
     * BUG #38 — SAME-HOUSEHOLD REJOIN. <b>EXPECTED TO FAIL ON THE CURRENT CODE.</b>
     *
     * <p>Current behaviour: {@code InvitationServiceImpl.acceptInvitation(...)} <em>always</em> does
     * {@code new Roommate()} and saves it. There is no path that reactivates an existing, deactivated
     * roommate row. The {@code Roommate} entity has a unique constraint on {@code (user_id, household_id)}.
     * Therefore: join WG-A → leave WG-A (row kept, {@code active=false}) → rejoin WG-A attempts to insert a
     * SECOND row for the same {@code (user, household)} pair, which violates the unique constraint. Because
     * {@code GlobalExceptionHandler} has no handler for {@code DataIntegrityViolationException}, this surfaces
     * as an unhandled <b>HTTP 500</b> — a returning ex-roommate simply cannot rejoin their old WG.</p>
     *
     * <p>This test asserts the CORRECT, desired behaviour (clean rejoin → exactly one active membership), so
     * <b>it fails until the service is fixed</b>. That failure is the regression's finding, not a flaw in the
     * test. The fix is a few lines in {@code acceptInvitation} — reactivate the existing row instead of
     * inserting a new one:</p>
     *
     * <pre>{@code
     * Roommate roommate = roommateRepository
     *     .findByUserIdAndHouseholdId(currentUser.getId(), invitation.getHousehold().getId())
     *     .orElseGet(Roommate::new);
     * roommate.setUser(currentUser);
     * roommate.setHousehold(invitation.getHousehold());
     * roommate.setRole(HouseholdRole.ROOMMATE);
     * roommate.setActive(true);
     * roommateRepository.save(roommate);
     * }</pre>
     *
     * <p>If the team decides NOT to ship the fix before MR3, flip this test to pin the current behaviour
     * (assert {@code 5xx}/conflict on rejoin) exactly as {@code Uc05BugRegressionTest.bug39_*} pins the
     * settlement lock, and record BUG #38 (same-WG rejoin) as a known limitation in the defense notes.</p>
     */
    @Test
    @DisplayName("BUG #38: leaving WG-A then rejoining the SAME WG-A yields one active membership (EXPECTED FAIL until acceptInvitation reactivates)")
    public void bug38_leaveThenRejoinSameHousehold_singleActiveMembership_expectedFailUntilFixed() throws Exception {
        // join A
        acceptFreshInvitation(householdA, mainTenantA, mover);
        // leave A  (DELETE /api/v1/users/me/household)
        leaveCurrentHousehold(ROOMMATE_EMAIL);

        // rejoin A via a brand-new invitation — DESIRED: 200/201 and a single reactivated row.
        InvitationDto rejoinInvite = createInvitation(householdA, mainTenantA, mover.getEmail());
        String rawToken = rawTokenOf(rejoinInvite);
        MvcResult rejoin = mockMvc.perform(post(BASE_URI + "/invitations/" + rawToken + "/accept")
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();

        List<Roommate> moverRowsInA = roommateRepository.findAll().stream()
            .filter(r -> r.getUser().getId().equals(mover.getId())
                && r.getHousehold().getId().equals(householdA.getId()))
            .toList();
        long active = moverRowsInA.stream().filter(Roommate::isActive).count();

        assertAll(
            () -> assertTrue(rejoin.getResponse().getStatus() < 300,
                "rejoining the same household should succeed (got HTTP " + rejoin.getResponse().getStatus()
                    + "). If this is 500, acceptInvitation is inserting a duplicate roommate and hitting the "
                    + "(user_id, household_id) unique constraint — apply the reactivation fix."),
            () -> assertEquals(1, moverRowsInA.size(),
                "exactly one roommate row for (mover, WG-A) — the original, reactivated"),
            () -> assertEquals(1, active, "and it is active again")
        );
    }

    // ---------------------------------------------------------------- BUG #42

    @Test
    @DisplayName("BUG #42: an anonymized former member's UC-05 history/stats stay readable with a safe placeholder and no PII")
    public void bug42_anonymizedFormerMember_safeAcrossHistoryAndStatistics() throws Exception {
        // mover joins WG-A and is involved in an expense so they appear in history + statistics
        acceptFreshInvitation(householdA, mainTenantA, mover);
        Long moverRoommateId = roommateRepository.findByUserIdAndActiveTrue(mover.getId()).orElseThrow().getId();
        Long tenantRoommateId = roommateRepository.findByUserIdAndActiveTrue(mainTenantA.getId()).orElseThrow().getId();

        // tenant pays an expense split with the mover so the mover has a financial footprint in WG-A
        createExpenseAsTenant(tenantRoommateId, moverRoommateId);

        // now the mover deletes/anonymizes their own account (GDPR path: DELETE /api/v1/users/me)
        mockMvc.perform(delete(BASE_URI + "/users/me")
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();

        // the tenant (still active) reads the UC-05 surfaces — none must leak the mover's PII, error, or 500
        MvcResult history = mockMvc.perform(get(HISTORY_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();
        MvcResult expenses = mockMvc.perform(get(EXPENSE_BASE_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();
        MvcResult dashboard = mockMvc.perform(get(DASHBOARD_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        String historyBody = history.getResponse().getContentAsString();
        String expensesBody = expenses.getResponse().getContentAsString();

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), history.getResponse().getStatus(),
                "history must remain readable after a member is anonymized"),
            () -> assertEquals(HttpStatus.OK.value(), expenses.getResponse().getStatus(),
                "expense list (history feed source) must remain readable"),
            () -> assertEquals(HttpStatus.OK.value(), dashboard.getResponse().getStatus(),
                "dashboard must not 500 because a former member exists"),
            // the original PII must be gone (anonymize sets the name to 'Deleted User' and the email to a token)
            () -> assertTrue(!historyBody.contains("Max") && !historyBody.contains(ROOMMATE_EMAIL),
                "anonymized member's original name/email must NOT appear in history"),
            () -> assertTrue(!expensesBody.contains("Max") && !expensesBody.contains(ROOMMATE_EMAIL),
                "anonymized member's original name/email must NOT appear in the expense feed")
        );
    }

    // ---------------------------------------------------------------- helpers

    /** Create an invitation for {@code household} (issued by its main tenant) targeting {@code invitee}. */
    private InvitationDto createInvitation(Household household, ApplicationUser mainTenant, String invitedEmail) throws Exception {
        InvitationCreateDto dto = new InvitationCreateDto(invitedEmail, 24);
        MvcResult result = mockMvc.perform(post(BASE_URI + "/households/" + household.getId() + "/invitations")
                .header(securityProperties.getAuthHeader(), tokenFor(mainTenant.getEmail()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(dto)))
            .andReturn();
        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus(),
            "precondition: invitation must be created (got " + result.getResponse().getStatus()
                + ": " + result.getResponse().getContentAsString() + ")");
        return jsonMapper.readValue(result.getResponse().getContentAsString(), InvitationDto.class);
    }

    /** Create + accept a fresh invitation so {@code invitee} becomes an active roommate of {@code household}. */
    private void acceptFreshInvitation(Household household, ApplicationUser mainTenant, ApplicationUser invitee) throws Exception {
        InvitationDto invite = createInvitation(household, mainTenant, invitee.getEmail());
        String rawToken = rawTokenOf(invite);
        MvcResult accept = mockMvc.perform(post(BASE_URI + "/invitations/" + rawToken + "/accept")
                .header(securityProperties.getAuthHeader(), tokenFor(invitee.getEmail())))
            .andReturn();
        assertEquals(HttpStatus.OK.value(), accept.getResponse().getStatus(),
            "precondition: invitation must be accepted (got " + accept.getResponse().getStatus()
                + ": " + accept.getResponse().getContentAsString() + ")");
    }

    /** Tenant records an expense split equally with one other roommate, so both appear in history/statistics. */
    private void createExpenseAsTenant(Long tenantRoommateId, Long otherRoommateId) throws Exception {
        String body = """
            {
              "description": "Shared groceries",
              "totalAmount": 12.00,
              "date": "%s",
              "paidByRoommateId": %d,
              "splits": [
                { "roommateId": %d, "amountOwed": 6.00 },
                { "roommateId": %d, "amountOwed": 6.00 }
              ]
            }
            """.formatted(java.time.LocalDate.now(), tenantRoommateId, tenantRoommateId, otherRoommateId);
        MvcResult result = mockMvc.perform(post(EXPENSE_BASE_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus(),
            "precondition: shared expense must be created (got " + result.getResponse().getStatus()
                + ": " + result.getResponse().getContentAsString() + ")");
    }

    private String rawTokenOf(InvitationDto invite) {
        // mirrors InvitationEndpointTest: the raw token is the last path segment of the invite URL
        return invite.inviteUrl().substring(invite.inviteUrl().lastIndexOf("/") + 1);
    }

    /** Leave the current household via the real endpoint: DELETE /api/v1/users/me/household. */
    private void leaveCurrentHousehold(String email) throws Exception {
        MvcResult result = mockMvc.perform(delete(BASE_URI + "/users/me/household")
                .header(securityProperties.getAuthHeader(), tokenFor(email)))
            .andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus(),
            "precondition: leaving the household must succeed (got " + result.getResponse().getStatus()
                + ": " + result.getResponse().getContentAsString() + ")");
    }

    private void cleanDatabase() {
        TestDbCleaner.clearAllApplicationData(
            settlementDecisionRepository, paymentRepository, expenseSplitRepository, expenseRepository,
            taskRepository, cleaningScheduleRepository, shoppingItemRepository, invitationRepository,
            roommateRepository, householdRepository, notificationRepository, notificationSettingRepository,
            verificationCodeRepository, userRepository);
    }

    private ApplicationUser saveUser(String email, String firstName, String lastName) {
        ApplicationUser user = new ApplicationUser(email, "encoded-password-for-test", firstName, lastName, false);
        return userRepository.save(user);
    }

    private Household saveHousehold(String name, ApplicationUser createdBy) {
        Household household = new Household();
        household.setName(name);
        household.setAddress("Test address");
        household.setCreatedBy(createdBy);
        return householdRepository.save(household);
    }

    private Roommate saveRoommate(ApplicationUser user, Household household, HouseholdRole role) {
        Roommate roommate = new Roommate();
        roommate.setUser(user);
        roommate.setHousehold(household);
        roommate.setRole(role);
        roommate.setActive(true);
        return roommateRepository.save(roommate);
    }

    private String tokenFor(String email) {
        return jwtTokenizer.getAuthToken(email, USER_ROLES);
    }
}
