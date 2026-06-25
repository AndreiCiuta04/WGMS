package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestDbCleaner;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * UC-05 two-household isolation (NFR-01 — closes E2E-016).
 *
 * <p>Builds two fully independent households (WG-A and WG-B) and verifies that a member of WG-B can
 * neither read nor mutate WG-A's UC-05 data, including via <em>direct id tampering</em> (guessing the
 * other household's expense id in the URL). This complements the existing {@code gdpr-security} and
 * {@code gdpr-domain-history} Cypress specs with a dedicated, server-side WG-A/WG-B matrix at the API
 * layer — the reliable place to prove the household scope derived from {@code authentication.getName()}.</p>
 *
 * <p>Expected contract:
 * <ul>
 *   <li>Aggregated reads ({@code /dashboard}, {@code /history}, {@code /expenses}) only ever return the
 *       caller's own household — WG-B's dashboard/history/expense list never contains WG-A content.</li>
 *   <li>A direct {@code GET/PUT/DELETE /expenses/{wgA_id}} by a WG-B user is rejected with 404 (the
 *       expense is "not found in your household" — the scope check refuses to even acknowledge it), and
 *       WG-A's data is left untouched.</li>
 * </ul></p>
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class Uc05TwoHouseholdIsolationTest implements TestData {

    private static final String EXPENSE_BASE_URI = BASE_URI + "/expenses";
    private static final String DASHBOARD_URI = BASE_URI + "/dashboard";
    private static final String HISTORY_URI = BASE_URI + "/history";
    private static final LocalDate TODAY = LocalDate.now();

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

    // WG-A
    private ApplicationUser tenantA;
    private Roommate tenantARoommate;
    private Long expenseInA;
    // WG-B
    private ApplicationUser tenantB;
    private Roommate tenantBRoommate;

    @BeforeEach
    public void beforeEach() {
        cleanDatabase();

        tenantA = saveUser(MAIN_TENANT_EMAIL, "Anna", "TenantA");
        Household householdA = saveHousehold(TEST_HOUSEHOLD_NAME, tenantA);
        tenantARoommate = saveRoommate(tenantA, householdA, HouseholdRole.MAIN_TENANT);

        tenantB = saveUser("tenantB@test.com", "Bea", "TenantB");
        Household householdB = saveHousehold(OTHER_HOUSEHOLD_NAME, tenantB);
        tenantBRoommate = saveRoommate(tenantB, householdB, HouseholdRole.MAIN_TENANT);

        // a unique, identifiable expense exists in WG-A only
        Expense expense = new Expense();
        expense.setHousehold(householdA);
        expense.setDescription("WG-A-SECRET-EXPENSE");
        expense.setTotalAmount(new BigDecimal("33.33"));
        expense.setDate(TODAY);
        expense.setPaidBy(tenantARoommate);
        expense.setCreatedBy(tenantARoommate);
        Expense savedExpense = expenseRepository.save(expense);
        expenseInA = savedExpense.getId();

        ExpenseSplit split = new ExpenseSplit();
        split.setExpense(savedExpense);
        split.setRoommate(tenantARoommate);
        split.setAmountOwed(new BigDecimal("33.33"));
        expenseSplitRepository.save(split);
    }

    @AfterEach
    public void afterEach() {
        cleanDatabase();
    }

    @Test
    @DisplayName("WG-B aggregated reads (dashboard/history/expenses) never contain WG-A data")
    public void aggregatedReads_areHouseholdScoped() throws Exception {
        MvcResult dashB = mockMvc.perform(get(DASHBOARD_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(tenantB.getEmail())))
            .andReturn();
        MvcResult histB = mockMvc.perform(get(HISTORY_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(tenantB.getEmail())))
            .andReturn();
        MvcResult expB = mockMvc.perform(get(EXPENSE_BASE_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(tenantB.getEmail())))
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), dashB.getResponse().getStatus()),
            () -> assertEquals(HttpStatus.OK.value(), histB.getResponse().getStatus()),
            () -> assertEquals(HttpStatus.OK.value(), expB.getResponse().getStatus()),
            () -> assertTrue(!dashB.getResponse().getContentAsString().contains("WG-A-SECRET-EXPENSE"),
                "WG-B dashboard must not contain WG-A's expense"),
            () -> assertTrue(!histB.getResponse().getContentAsString().contains("WG-A-SECRET-EXPENSE"),
                "WG-B history must not contain WG-A's expense"),
            () -> assertTrue(!expB.getResponse().getContentAsString().contains("WG-A-SECRET-EXPENSE"),
                "WG-B expense list must not contain WG-A's expense"),
            // and WG-A's own owner DOES see it (positive control)
            () -> assertTrue(mockMvc.perform(get(EXPENSE_BASE_URI)
                        .header(securityProperties.getAuthHeader(), tokenFor(tenantA.getEmail())))
                    .andReturn().getResponse().getContentAsString().contains("WG-A-SECRET-EXPENSE"),
                "positive control: WG-A owner sees their own expense")
        );
    }

    @Test
    @DisplayName("WG-B user cannot READ a WG-A expense by direct id (404, not found in your household)")
    public void directIdRead_acrossHouseholds_isRejected() throws Exception {
        MvcResult result = mockMvc.perform(get(EXPENSE_BASE_URI + "/" + expenseInA)
                .header(securityProperties.getAuthHeader(), tokenFor(tenantB.getEmail())))
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus(),
                "cross-household GET by id must be 404 (scope refuses to acknowledge the foreign id)"),
            () -> assertTrue(!result.getResponse().getContentAsString().contains("WG-A-SECRET-EXPENSE"),
                "the 404 body must not leak the foreign expense's description")
        );
    }

    @Test
    @DisplayName("WG-B user cannot MUTATE (update/delete) a WG-A expense by direct id; WG-A data is untouched")
    public void directIdMutate_acrossHouseholds_isRejectedAndLeavesDataIntact() throws Exception {
        String updateBody = """
            {
              "description": "HACKED",
              "totalAmount": 1.00,
              "date": "%s",
              "paidByRoommateId": %d,
              "splits": [ { "roommateId": %d, "amountOwed": 1.00 } ]
            }
            """.formatted(TODAY, tenantBRoommate.getId(), tenantBRoommate.getId());

        MvcResult put = mockMvc.perform(put(EXPENSE_BASE_URI + "/" + expenseInA)
                .header(securityProperties.getAuthHeader(), tokenFor(tenantB.getEmail()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
            .andReturn();

        MvcResult del = mockMvc.perform(delete(EXPENSE_BASE_URI + "/" + expenseInA)
                .header(securityProperties.getAuthHeader(), tokenFor(tenantB.getEmail())))
            .andReturn();

        Expense untouched = expenseRepository.findById(expenseInA).orElseThrow();

        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND.value(), put.getResponse().getStatus(),
                "cross-household PUT must be 404"),
            () -> assertEquals(HttpStatus.NOT_FOUND.value(), del.getResponse().getStatus(),
                "cross-household DELETE must be 404"),
            () -> assertEquals("WG-A-SECRET-EXPENSE", untouched.getDescription(),
                "WG-A expense description must be unchanged by the foreign mutation attempt"),
            () -> assertEquals(0, new BigDecimal("33.33").compareTo(untouched.getTotalAmount()),
                "WG-A expense amount must be unchanged"),
            () -> assertEquals(1, expenseRepository.findAll().size(),
                "the WG-A expense must still exist")
        );
    }

    // ---------------------------------------------------------------- helpers

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
