package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestDbCleaner;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentOverviewDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PendingSettlementDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Payment;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationSettingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SettlementDecisionRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class PaymentEndpointTest implements TestData {

    private static final String PAYMENT_BASE_URI = BASE_URI + "/payments";
    private static final LocalDate PAYMENT_DATE = LocalDate.now();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseSplitRepository expenseSplitRepository;

    @Autowired
    private SettlementDecisionRepository settlementDecisionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CleaningScheduleRepository cleaningScheduleRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private RoommateRepository roommateRepository;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JsonMapper jsonMapper;

    private ApplicationUser mainTenant;
    private ApplicationUser roommate;
    private ApplicationUser outsider;
    private ApplicationUser userWithoutHousehold;
    private Household household;
    private Household otherHousehold;
    private Roommate mainTenantRoommate;
    private Roommate roommateRecord;
    private Roommate outsiderRoommate;

    @BeforeEach
    public void beforeEach() {
        cleanDatabase();

        mainTenant = saveUser(MAIN_TENANT_EMAIL, "Main", "Tenant");
        roommate = saveUser(ROOMMATE_EMAIL, "Normal", "Roommate");
        outsider = saveUser(OUTSIDER_EMAIL, "Other", "User");
        userWithoutHousehold = saveUser("no.household@test.com", "No", "Household");

        household = saveHousehold(TEST_HOUSEHOLD_NAME, mainTenant);
        otherHousehold = saveHousehold(OTHER_HOUSEHOLD_NAME, outsider);

        mainTenantRoommate = saveRoommate(mainTenant, household, HouseholdRole.MAIN_TENANT);
        roommateRecord = saveRoommate(roommate, household, HouseholdRole.ROOMMATE);
        outsiderRoommate = saveRoommate(outsider, otherHousehold, HouseholdRole.MAIN_TENANT);
    }

    @AfterEach
    public void afterEach() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        settlementDecisionRepository.deleteAll();
        paymentRepository.deleteAll();
        expenseSplitRepository.deleteAll();
        expenseRepository.deleteAll();
        taskRepository.deleteAll();
        cleaningScheduleRepository.deleteAll();
        roommateRepository.deleteAll();
        householdRepository.deleteAll();
        TestDbCleaner.clearNotificationData(notificationRepository, notificationSettingRepository);
        userRepository.deleteAll();
    }

    @Test
    public void givenNoToken_whenGetOverview_then403() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(PAYMENT_BASE_URI + "/overview"))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void givenUserWithoutActiveHousehold_whenGetOverview_then403() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(PAYMENT_BASE_URI + "/overview")
                .header(securityProperties.getAuthHeader(), tokenFor(userWithoutHousehold.getEmail())))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void givenEmptyHousehold_whenGetOverview_thenStableEmptyShape() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(PAYMENT_BASE_URI + "/overview")
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        PaymentOverviewDto overview = jsonMapper.readValue(mvcResult.getResponse().getContentAsString(), PaymentOverviewDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, overview.openBalances().size()),
            () -> assertEquals(0, overview.payments().size()),
            () -> assertEquals(0, money("0.00").compareTo(overview.totalIowe())),
            () -> assertEquals(0, money("0.00").compareTo(overview.totalOwedToMe()))
        );
    }

    @Test
    public void givenPopulatedHousehold_whenGetOverview_thenReturnsBalancesAndPaymentHistory() throws Exception {
        Expense expense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Internet bill", money("20.00"));
        saveSplit(expense, mainTenantRoommate, money("10.00"));
        saveSplit(expense, roommateRecord, money("10.00"));
        Payment payment = savePayment(roommateRecord, mainTenantRoommate, money("4.00"));

        MvcResult mvcResult = this.mockMvc.perform(get(PAYMENT_BASE_URI + "/overview")
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        PaymentOverviewDto overview = jsonMapper.readValue(mvcResult.getResponse().getContentAsString(), PaymentOverviewDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(1, overview.openBalances().size()),
            () -> assertEquals(roommateRecord.getId(), overview.openBalances().get(0).fromRoommateId()),
            () -> assertEquals(mainTenantRoommate.getId(), overview.openBalances().get(0).toRoommateId()),
            () -> assertEquals(0, money("6.00").compareTo(overview.openBalances().get(0).amount())),
            () -> assertEquals(1, overview.payments().size()),
            () -> assertEquals(payment.getId(), overview.payments().get(0).id()),
            () -> assertEquals(0, money("6.00").compareTo(overview.totalOwedToMe()))
        );
    }

    @Test
    public void givenOtherHouseholdData_whenGetOverview_thenDoesNotLeak() throws Exception {
        Expense ownExpense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Own bill", money("20.00"));
        saveSplit(ownExpense, mainTenantRoommate, money("10.00"));
        saveSplit(ownExpense, roommateRecord, money("10.00"));

        ApplicationUser otherRoommateUser = saveUser("other.roommate@test.com", "Other", "Roommate");
        Roommate otherRoommate = saveRoommate(otherRoommateUser, otherHousehold, HouseholdRole.ROOMMATE);
        Expense otherExpense = saveExpense(otherHousehold, outsiderRoommate, outsiderRoommate, "Private other bill", money("30.00"));
        saveSplit(otherExpense, outsiderRoommate, money("15.00"));
        saveSplit(otherExpense, otherRoommate, money("15.00"));
        savePayment(otherRoommate, outsiderRoommate, money("5.00"));

        MvcResult mvcResult = this.mockMvc.perform(get(PAYMENT_BASE_URI + "/overview")
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        PaymentOverviewDto overview = jsonMapper.readValue(responseBody, PaymentOverviewDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(1, overview.openBalances().size()),
            () -> assertFalse(responseBody.contains(OUTSIDER_EMAIL)),
            () -> assertFalse(responseBody.contains("other.roommate@test.com")),
            () -> assertFalse(responseBody.contains("Private other bill"))
        );
    }

    @Test
    public void givenValidPayment_whenCreatePayment_then201AndPaymentIsPersisted() throws Exception {
        Expense expense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Internet bill", money("20.00"));
        saveSplit(expense, mainTenantRoommate, money("10.00"));
        saveSplit(expense, roommateRecord, money("10.00"));

        PaymentCreateDto createDto = new PaymentCreateDto(mainTenantRoommate.getId(), money("4.00"), PAYMENT_DATE);

        MvcResult mvcResult = this.mockMvc.perform(post(PAYMENT_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createDto))
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        PaymentDto paymentDto = jsonMapper.readValue(response.getContentAsString(), PaymentDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED.value(), response.getStatus()),
            () -> assertNotNull(paymentDto.id()),
            () -> assertEquals(roommateRecord.getId(), paymentDto.fromRoommateId()),
            () -> assertEquals(mainTenantRoommate.getId(), paymentDto.toRoommateId()),
            () -> assertEquals(0, money("4.00").compareTo(paymentDto.amount())),
            () -> assertEquals(1, paymentRepository.findAll().size())
        );
    }

    @Test
    public void givenOpenBalance_whenGetPendingSettlementApprovals_thenReturnsCurrentHouseholdPendingSettlement() throws Exception {
        Expense expense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Internet bill", money("20.00"));
        saveSplit(expense, mainTenantRoommate, money("10.00"));
        saveSplit(expense, roommateRecord, money("10.00"));

        MvcResult mvcResult = this.mockMvc.perform(get(PAYMENT_BASE_URI + "/settlement-approvals")
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();

        List<PendingSettlementDto> settlements = jsonMapper.readValue(
            mvcResult.getResponse().getContentAsString(),
            jsonMapper.getTypeFactory().constructCollectionType(List.class, PendingSettlementDto.class)
        );

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(1, settlements.size()),
            () -> assertEquals(roommateRecord.getId() + "-" + mainTenantRoommate.getId() + "-1000", settlements.get(0).id()),
            () -> assertEquals("Internet bill", settlements.get(0).expenseDescription()),
            () -> assertEquals(roommateRecord.getId(), settlements.get(0).fromRoommateId()),
            () -> assertEquals(mainTenantRoommate.getId(), settlements.get(0).toRoommateId()),
            () -> assertEquals(0, money("10.00").compareTo(settlements.get(0).amount())),
            () -> assertEquals(true, settlements.get(0).canDecide())
        );
    }

    @Test
    public void givenOpenBalanceOwedToMainTenant_whenGetPendingSettlementApprovalsAsMainTenant_thenReturnsNoApprovals() throws Exception {
        Expense expense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Internet bill", money("20.00"));
        saveSplit(expense, mainTenantRoommate, money("10.00"));
        saveSplit(expense, roommateRecord, money("10.00"));

        MvcResult mvcResult = this.mockMvc.perform(get(PAYMENT_BASE_URI + "/settlement-approvals")
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        List<PendingSettlementDto> settlements = jsonMapper.readValue(
            mvcResult.getResponse().getContentAsString(),
            jsonMapper.getTypeFactory().constructCollectionType(List.class, PendingSettlementDto.class)
        );

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, settlements.size())
        );
    }

    @Test
    public void givenPendingSettlement_whenApprove_thenPaymentIsRecordedAndSettlementNoLongerPending() throws Exception {
        Expense expense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Internet bill", money("20.00"));
        saveSplit(expense, mainTenantRoommate, money("10.00"));
        saveSplit(expense, roommateRecord, money("10.00"));
        String settlementId = roommateRecord.getId() + "-" + mainTenantRoommate.getId() + "-1000";

        MvcResult approveResult = this.mockMvc.perform(post(PAYMENT_BASE_URI + "/settlement-approvals/" + settlementId + "/approve")
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();

        MvcResult pendingResult = this.mockMvc.perform(get(PAYMENT_BASE_URI + "/settlement-approvals")
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();
        List<PendingSettlementDto> pending = jsonMapper.readValue(
            pendingResult.getResponse().getContentAsString(),
            jsonMapper.getTypeFactory().constructCollectionType(List.class, PendingSettlementDto.class)
        );

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), approveResult.getResponse().getStatus()),
            () -> assertEquals(1, paymentRepository.findAll().size()),
            () -> assertEquals(1, settlementDecisionRepository.findAll().size()),
            () -> assertEquals(0, pending.size())
        );
    }

    @Test
    public void bug42_givenDebtToAnonymizedFormerRoommate_whenApproveSettlement_thenPaymentIsRecordedToPrivacySafeCounterparty() throws Exception {
        ApplicationUser deletedUser = saveUser("deleted.counterparty@test.com", "Former", "Member");
        Roommate deletedRoommate = saveRoommate(deletedUser, household, HouseholdRole.ROOMMATE);
        deletedUser.setEmail("anonymized-user-" + deletedUser.getId() + "@deleted.local");
        deletedUser.setFirstName("Deleted");
        deletedUser.setLastName("User");
        deletedUser.setAnonymized(true);
        userRepository.save(deletedUser);
        deletedRoommate.setActive(false);
        roommateRepository.save(deletedRoommate);

        Expense expense = saveExpense(household, deletedRoommate, deletedRoommate, "Former member groceries", money("20.00"));
        saveSplit(expense, deletedRoommate, money("10.00"));
        saveSplit(expense, roommateRecord, money("10.00"));
        String settlementId = roommateRecord.getId() + "-" + deletedRoommate.getId() + "-1000";

        MvcResult pendingResult = this.mockMvc.perform(get(PAYMENT_BASE_URI + "/settlement-approvals")
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();
        MvcResult approveResult = this.mockMvc.perform(post(PAYMENT_BASE_URI + "/settlement-approvals/" + settlementId + "/approve")
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();

        List<PendingSettlementDto> pending = jsonMapper.readValue(
            pendingResult.getResponse().getContentAsString(),
            jsonMapper.getTypeFactory().constructCollectionType(List.class, PendingSettlementDto.class)
        );
        Payment savedPayment = paymentRepository.findAll().get(0);
        String approveBody = approveResult.getResponse().getContentAsString();

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), pendingResult.getResponse().getStatus()),
            () -> assertEquals(1, pending.size()),
            () -> assertEquals("Deleted User", pending.get(0).toRoommateName()),
            () -> assertEquals(deletedRoommate.getId(), pending.get(0).toRoommateId()),
            () -> assertEquals(HttpStatus.OK.value(), approveResult.getResponse().getStatus()),
            () -> assertEquals(1, paymentRepository.findAll().size()),
            () -> assertEquals(roommateRecord.getId(), savedPayment.getFromRoommate().getId()),
            () -> assertEquals(deletedRoommate.getId(), savedPayment.getToRoommate().getId()),
            () -> assertEquals(1, settlementDecisionRepository.findAll().size()),
            () -> assertFalse(approveBody.contains("deleted.counterparty@test.com")),
            () -> assertEquals(0, money("10.00").compareTo(savedPayment.getAmount()))
        );
    }

    @Test
    public void givenPendingSettlement_whenReject_thenDecisionIsRecordedWithoutPaymentAndSettlementNoLongerPending() throws Exception {
        Expense expense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Internet bill", money("20.00"));
        saveSplit(expense, mainTenantRoommate, money("10.00"));
        saveSplit(expense, roommateRecord, money("10.00"));
        String settlementId = roommateRecord.getId() + "-" + mainTenantRoommate.getId() + "-1000";

        MvcResult rejectResult = this.mockMvc.perform(post(PAYMENT_BASE_URI + "/settlement-approvals/" + settlementId + "/reject")
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();
        MvcResult pendingResult = this.mockMvc.perform(get(PAYMENT_BASE_URI + "/settlement-approvals")
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();
        List<PendingSettlementDto> pending = jsonMapper.readValue(
            pendingResult.getResponse().getContentAsString(),
            jsonMapper.getTypeFactory().constructCollectionType(List.class, PendingSettlementDto.class)
        );

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), rejectResult.getResponse().getStatus()),
            () -> assertEquals(0, paymentRepository.findAll().size()),
            () -> assertEquals(1, settlementDecisionRepository.findAll().size()),
            () -> assertEquals(0, pending.size())
        );
    }

    @Test
    public void givenSettlementFromAnotherUser_whenApprove_then403AndNoPaymentIsCreated() throws Exception {
        Expense expense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Internet bill", money("20.00"));
        saveSplit(expense, mainTenantRoommate, money("10.00"));
        saveSplit(expense, roommateRecord, money("10.00"));
        String settlementId = roommateRecord.getId() + "-" + mainTenantRoommate.getId() + "-1000";

        MvcResult mvcResult = this.mockMvc.perform(post(PAYMENT_BASE_URI + "/settlement-approvals/" + settlementId + "/approve")
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, paymentRepository.findAll().size()),
            () -> assertEquals(0, settlementDecisionRepository.findAll().size())
        );
    }

    @Test
    public void givenFutureDate_whenCreatePayment_then400AndNoPaymentIsCreated() throws Exception {
        Expense expense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Internet bill", money("20.00"));
        saveSplit(expense, mainTenantRoommate, money("10.00"));
        saveSplit(expense, roommateRecord, money("10.00"));
        PaymentCreateDto createDto = new PaymentCreateDto(mainTenantRoommate.getId(), money("4.00"), PAYMENT_DATE.plusDays(1));

        MvcResult mvcResult = this.mockMvc.perform(post(PAYMENT_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createDto))
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, paymentRepository.findAll().size())
        );
    }

    @Test
    public void givenConcurrentPaymentsForSameOpenBalance_whenCreatePayment_thenOnlyOneIsPersisted() throws Exception {
        Expense expense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Internet bill", money("20.00"));
        saveSplit(expense, mainTenantRoommate, money("10.00"));
        saveSplit(expense, roommateRecord, money("10.00"));
        PaymentCreateDto createDto = new PaymentCreateDto(mainTenantRoommate.getId(), money("10.00"), PAYMENT_DATE);
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Integer> request = () -> {
            startLatch.await();
            return this.mockMvc.perform(post(PAYMENT_BASE_URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonMapper.writeValueAsString(createDto))
                    .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
                .andReturn()
                .getResponse()
                .getStatus();
        };

        try {
            Future<Integer> firstRequest = executor.submit(request);
            Future<Integer> secondRequest = executor.submit(request);
            startLatch.countDown();
            List<Integer> statuses = List.of(firstRequest.get(), secondRequest.get());

            assertAll(
                () -> assertEquals(1, statuses.stream().filter(status -> status == HttpStatus.CREATED.value()).count()),
                () -> assertEquals(1, statuses.stream().filter(status -> status == HttpStatus.CONFLICT.value()).count()),
                () -> assertEquals(1, paymentRepository.findAll().size())
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private String tokenFor(String email) {
        return jwtTokenizer.getAuthToken(email, USER_ROLES);
    }

    private BigDecimal money(String amount) {
        return new BigDecimal(amount);
    }

    private ApplicationUser saveUser(String email, String firstName, String lastName) {
        ApplicationUser user = new ApplicationUser();
        user.setEmail(email);
        user.setPassword("encoded-password-for-test");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAdmin(false);
        return userRepository.save(user);
    }

    private Household saveHousehold(String name, ApplicationUser createdBy) {
        Household householdToSave = new Household();
        householdToSave.setName(name);
        householdToSave.setAddress("Test address");
        householdToSave.setCreatedBy(createdBy);
        return householdRepository.save(householdToSave);
    }

    private Roommate saveRoommate(ApplicationUser user, Household household, HouseholdRole role) {
        Roommate roommateToSave = new Roommate();
        roommateToSave.setUser(user);
        roommateToSave.setHousehold(household);
        roommateToSave.setRole(role);
        roommateToSave.setActive(true);
        return roommateRepository.save(roommateToSave);
    }

    private Expense saveExpense(Household household, Roommate paidBy, Roommate createdBy, String description, BigDecimal amount) {
        Expense expense = new Expense();
        expense.setHousehold(household);
        expense.setPaidBy(paidBy);
        expense.setCreatedBy(createdBy);
        expense.setDescription(description);
        expense.setTotalAmount(amount);
        expense.setDate(PAYMENT_DATE);
        return expenseRepository.save(expense);
    }

    private ExpenseSplit saveSplit(Expense expense, Roommate roommate, BigDecimal amount) {
        ExpenseSplit split = new ExpenseSplit();
        split.setExpense(expense);
        split.setRoommate(roommate);
        split.setAmountOwed(amount);
        return expenseSplitRepository.save(split);
    }

    private Payment savePayment(Roommate fromRoommate, Roommate toRoommate, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setHousehold(fromRoommate.getHousehold());
        payment.setFromRoommate(fromRoommate);
        payment.setToRoommate(toRoommate);
        payment.setCreatedBy(fromRoommate);
        payment.setAmount(amount);
        payment.setDate(PAYMENT_DATE);
        return paymentRepository.save(payment);
    }
}
