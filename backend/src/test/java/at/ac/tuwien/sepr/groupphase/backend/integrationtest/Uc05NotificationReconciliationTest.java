package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestDbCleaner;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Notification;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationType;
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
import at.ac.tuwien.sepr.groupphase.backend.service.NotificationService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * UC-05 notification reconciliation (FR-11 / NFR-03 — closes E2E-014) and the automatable rows of the
 * UC-05 negative error matrix (NFR-01/03/04 — closes the automatable part of E2E-017).
 *
 * <p>Reconciliation properties proven here:
 * <ul>
 *   <li><b>Double-trigger is idempotent.</b> Triggering the same (recipient, type, referenceId)
 *       notification twice while the first is still unread creates exactly ONE row — the service skips the
 *       duplicate via {@code existsByRecipientIdAndTypeAndReferenceIdAndIsReadFalse}.</li>
 *   <li><b>Double mark-as-read is safe.</b> Marking the same notification read twice is idempotent and
 *       never drives the unread count below zero.</li>
 *   <li><b>Mark-read is owner-scoped.</b> A user cannot mark another user's notification as read (403).</li>
 * </ul></p>
 *
 * <p>Negative error-matrix rows proven here (each asserts a controlled status and a body free of SQL,
 * stack traces, secrets, or foreign data):
 * <ul>
 *   <li>401 — unauthenticated UC-05 read.</li>
 *   <li>403 — authenticated user with no household hitting a household-scoped UC-05 read.</li>
 *   <li>404 — operating on a non-existent expense id.</li>
 *   <li>400 — malformed expense create payload (validation).</li>
 *   <li>409 — domain conflict (split sum != total).</li>
 * </ul>
 * Offline / timeout rows remain manual (E2E-017) — they cannot be produced deterministically in MockMvc.</p>
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class Uc05NotificationReconciliationTest implements TestData {

    private static final String EXPENSE_BASE_URI = BASE_URI + "/expenses";
    private static final String NOTIFICATIONS_URI = BASE_URI + "/notifications";
    private static final String DASHBOARD_URI = BASE_URI + "/dashboard";

    @Autowired private MockMvc mockMvc;
    @Autowired private NotificationService notificationService;
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

    private ApplicationUser tenant;
    private ApplicationUser roommate;
    private ApplicationUser outsider;     // exists but belongs to no household
    private Roommate tenantRoommate;
    private Roommate roommateRecord;

    @BeforeEach
    public void beforeEach() {
        cleanDatabase();
        tenant = saveUser(MAIN_TENANT_EMAIL, "Anna", "Tenant");
        roommate = saveUser(ROOMMATE_EMAIL, "Max", "Roommate");
        outsider = saveUser(OUTSIDER_EMAIL, "Otto", "Outsider");
        Household household = saveHousehold(TEST_HOUSEHOLD_NAME, tenant);
        tenantRoommate = saveRoommate(tenant, household, HouseholdRole.MAIN_TENANT);
        roommateRecord = saveRoommate(roommate, household, HouseholdRole.ROOMMATE);
    }

    @AfterEach
    public void afterEach() {
        cleanDatabase();
    }

    // ---------------------------------------------------------------- reconciliation (E2E-014)

    @Test
    @DisplayName("E2E-014: triggering the same notification twice while unread creates exactly one row")
    public void doubleTriggerSameReference_isDeduplicated() {
        notificationService.triggerNotification(roommate, "Anna added an expense: Groceries. You owe EUR 6.00.",
            NotificationType.EXPENSE_CREATED, 4242L);
        notificationService.triggerNotification(roommate, "Anna added an expense: Groceries. You owe EUR 6.00.",
            NotificationType.EXPENSE_CREATED, 4242L);

        List<Notification> forRoommate = notificationService.getNotificationsForUser(roommate.getId());
        long matching = forRoommate.stream()
            .filter(n -> n.getType() == NotificationType.EXPENSE_CREATED
                && n.getReferenceId() != null && n.getReferenceId() == 4242L)
            .count();

        assertEquals(1, matching, "second identical unread trigger must be skipped (no duplicate notification)");
    }

    @Test
    @DisplayName("E2E-014: marking the same notification read twice is idempotent and never yields a negative unread count")
    public void doubleMarkRead_isIdempotentAndNonNegative() throws Exception {
        notificationService.triggerNotification(roommate, "Anna added an expense: Groceries. You owe EUR 6.00.",
            NotificationType.EXPENSE_CREATED, 7L);
        Long notificationId = notificationService.getNotificationsForUser(roommate.getId()).get(0).getId();

        MvcResult first = mockMvc.perform(patch(NOTIFICATIONS_URI + "/" + notificationId + "/read")
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();
        MvcResult second = mockMvc.perform(patch(NOTIFICATIONS_URI + "/" + notificationId + "/read")
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();

        long unread = notificationRepository.countByRecipientIdAndIsReadFalse(roommate.getId());

        assertAll(
            () -> assertTrue(first.getResponse().getStatus() < 300, "first mark-read succeeds"),
            () -> assertTrue(second.getResponse().getStatus() < 300, "second mark-read is also accepted (idempotent)"),
            () -> assertTrue(unread >= 0, "unread count must never go negative"),
            () -> assertEquals(0, unread, "the only notification is read, so unread count is 0")
        );
    }

    @Test
    @DisplayName("E2E-014: a user cannot mark another user's notification as read (403)")
    public void markRead_foreignNotification_isForbidden() throws Exception {
        notificationService.triggerNotification(roommate, "Anna added an expense: Groceries. You owe EUR 6.00.",
            NotificationType.EXPENSE_CREATED, 9L);
        Long roommatesNotificationId = notificationService.getNotificationsForUser(roommate.getId()).get(0).getId();

        // the tenant tries to mark the roommate's notification as read
        MvcResult result = mockMvc.perform(patch(NOTIFICATIONS_URI + "/" + roommatesNotificationId + "/read")
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus(),
            "marking a notification you do not own must be 403");
    }

    // ---------------------------------------------------------------- negative error matrix (E2E-017, automatable rows)

    @Test
    @DisplayName("E2E-017: unauthenticated UC-05 read is blocked (401/403) with no data leak")
    public void unauthenticatedRead_isBlocked() throws Exception {
        MvcResult result = mockMvc.perform(get(DASHBOARD_URI)).andReturn();
        int status = result.getResponse().getStatus();
        assertAll(
            () -> assertTrue(status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value(),
                "unauthenticated access must be denied (401 or 403), was " + status),
            () -> assertSafeBody(result)
        );
    }

    @Test
    @DisplayName("E2E-017: authenticated user without a household gets 403 on a household-scoped read")
    public void noHouseholdRead_is403() throws Exception {
        MvcResult result = mockMvc.perform(get(DASHBOARD_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(OUTSIDER_EMAIL)))
            .andReturn();
        assertAll(
            () -> assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus()),
            () -> assertSafeBody(result)
        );
    }

    @Test
    @DisplayName("E2E-017: operating on a non-existent expense id is 404 with no data leak")
    public void nonExistentExpense_is404() throws Exception {
        MvcResult result = mockMvc.perform(get(EXPENSE_BASE_URI + "/999999")
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();
        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus()),
            () -> assertSafeBody(result)
        );
    }

    @Test
    @DisplayName("E2E-017: malformed expense create payload is 400 (validation) with no stack trace")
    public void malformedCreate_is400() throws Exception {
        // missing required fields / wrong types
        String badBody = "{ \"description\": \"\", \"totalAmount\": \"not-a-number\" }";
        MvcResult result = mockMvc.perform(post(EXPENSE_BASE_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL))
                .contentType(MediaType.APPLICATION_JSON)
                .content(badBody))
            .andReturn();
        assertAll(
            () -> assertTrue(result.getResponse().getStatus() == HttpStatus.BAD_REQUEST.value()
                    || result.getResponse().getStatus() == HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "malformed payload must be a 4xx validation error (got " + result.getResponse().getStatus() + ")"),
            () -> assertSafeBody(result)
        );
    }

    @Test
    @DisplayName("E2E-017: a domain conflict (split sum != total) is 409 with no data leak")
    public void splitSumMismatch_is409() throws Exception {
        String body = """
            {
              "description": "Groceries",
              "totalAmount": 12.00,
              "date": "%s",
              "paidByRoommateId": %d,
              "splits": [
                { "roommateId": %d, "amountOwed": 6.00 },
                { "roommateId": %d, "amountOwed": 1.00 }
              ]
            }
            """.formatted(java.time.LocalDate.now(), tenantRoommate.getId(),
            tenantRoommate.getId(), roommateRecord.getId());
        MvcResult result = mockMvc.perform(post(EXPENSE_BASE_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
        assertAll(
            () -> assertEquals(HttpStatus.CONFLICT.value(), result.getResponse().getStatus(),
                "sum-of-splits != total must be a 409 conflict"),
            () -> assertSafeBody(result)
        );
    }

    // ---------------------------------------------------------------- helpers

    /** Asserts an error body does not leak SQL, stack traces, secrets, or foreign PII. */
    private void assertSafeBody(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString().toLowerCase();
        assertAll(
            () -> assertTrue(!body.contains("sqlexception") && !body.contains("org.hibernate")
                    && !body.contains("select ") && !body.contains("nested exception"),
                "error body must not expose SQL / Hibernate internals"),
            () -> assertTrue(!body.contains("at at.ac.tuwien") && !body.contains(".java:"),
                "error body must not expose a stack trace"),
            () -> assertTrue(!body.contains("jwt") && !body.contains("secret") && !body.contains("password"),
                "error body must not expose secrets")
        );
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
