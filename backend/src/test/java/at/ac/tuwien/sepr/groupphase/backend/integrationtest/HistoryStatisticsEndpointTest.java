package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestDbCleaner;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsOverviewDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Payment;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdInvitationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationSettingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.VerificationCodeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SettlementDecisionRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class HistoryStatisticsEndpointTest implements TestData {

    private static final String STATISTICS_BASE_URI = BASE_URI + "/history/statistics";
    private static final LocalDate FROM = LocalDate.now().minusDays(30);
    private static final LocalDate TO = LocalDate.now();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CleaningScheduleRepository cleaningScheduleRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseSplitRepository expenseSplitRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ShoppingItemRepository shoppingItemRepository;

    @Autowired
    private HouseholdInvitationRepository invitationRepository;

    @Autowired
    private RoommateRepository roommateRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private SettlementDecisionRepository settlementDecisionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JsonMapper jsonMapper;

    private ApplicationUser mainTenant;
    private ApplicationUser roommateUser;
    private ApplicationUser outsider;
    private Household household;
    private Household otherHousehold;
    private Roommate mainTenantRoommate;
    private Roommate roommate;
    private Roommate outsiderRoommate;

    @BeforeEach
    public void beforeEach() {
        cleanDatabase();

        mainTenant = saveUser(MAIN_TENANT_EMAIL, "Main", "Tenant");
        roommateUser = saveUser(ROOMMATE_EMAIL, "Normal", "Roommate");
        outsider = saveUser(OUTSIDER_EMAIL, "Other", "User");

        household = saveHousehold(TEST_HOUSEHOLD_NAME, mainTenant);
        otherHousehold = saveHousehold(OTHER_HOUSEHOLD_NAME, outsider);

        mainTenantRoommate = saveRoommate(mainTenant, household, HouseholdRole.MAIN_TENANT);
        roommate = saveRoommate(roommateUser, household, HouseholdRole.ROOMMATE);
        outsiderRoommate = saveRoommate(outsider, otherHousehold, HouseholdRole.MAIN_TENANT);
    }

    @AfterEach
    public void afterEach() {
        cleanDatabase();
    }

    @Test
    public void givenNoToken_whenGetOverview_then403() throws Exception {
        MvcResult result = mockMvc.perform(get(STATISTICS_BASE_URI + "/overview")
                .param("from", FROM.toString())
                .param("to", TO.toString()))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void givenUserWithoutHousehold_whenGetOverview_then403() throws Exception {
        ApplicationUser withoutHousehold = saveUser("statistics-without-household@test.com", "No", "Household");

        MvcResult result = mockMvc.perform(get(STATISTICS_BASE_URI + "/overview")
                .header(securityProperties.getAuthHeader(), tokenFor(withoutHousehold.getEmail()))
                .param("from", FROM.toString())
                .param("to", TO.toString()))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void givenFromAfterTo_whenGetOverview_thenClientError() throws Exception {
        MvcResult result = mockMvc.perform(get(STATISTICS_BASE_URI + "/overview")
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL))
                .param("from", TO.toString())
                .param("to", FROM.toString()))
            .andReturn();

        assertTrue(result.getResponse().getStatus() >= 400 && result.getResponse().getStatus() < 500);
    }

    @Test
    public void givenNoActivity_whenGetAllStatistics_thenEmptyResponses() throws Exception {
        StatisticsOverviewDto overview = getDto("/overview", StatisticsOverviewDto.class);
        TaskStatisticsDto tasks = getDto("/tasks", TaskStatisticsDto.class);
        ExpenseStatisticsDto expenses = getDto("/expenses", ExpenseStatisticsDto.class);
        ShoppingStatisticsDto shopping = getDto("/shopping", ShoppingStatisticsDto.class);

        assertEquals(0, overview.totalActivityCount());
        assertEquals(0, tasks.totalTaskCount());
        assertEquals(0, expenses.expenseCount());
        assertEquals(0, expenses.paymentCount());
        assertEquals(0, shopping.addedItemCount());
        assertFalse(overview.activityTrend().isEmpty());
        assertFalse(tasks.completionTrend().isEmpty());
        assertFalse(shopping.itemActivityTrend().isEmpty());
    }

    @Test
    public void givenHouseholdActivity_whenGetOverview_thenReturnsOnlyOwnHouseholdData() throws Exception {
        saveTask(household, mainTenant, roommateUser, "Own completed", TaskStatus.DONE, TO.minusDays(2), null);
        saveTask(household, mainTenant, roommateUser, "Own open", TaskStatus.OPEN, TO.plusDays(2), null);
        saveTask(otherHousehold, outsider, outsider, "Other completed", TaskStatus.DONE, TO.minusDays(2), null);
        saveExpense(household, mainTenantRoommate, "Own expense", "15.00", TO.minusDays(1));
        saveExpense(otherHousehold, outsiderRoommate, "Other expense", "100.00", TO.minusDays(1));
        savePayment(household, mainTenantRoommate, roommate, "5.00", TO.minusDays(1));
        savePayment(otherHousehold, outsiderRoommate, outsiderRoommate, "99.00", TO.minusDays(1));
        saveShoppingItem(household, mainTenant, "Own bought", ShoppingItemStatus.BOUGHT,
            ShoppingItemVisibility.SHARED, TO.minusDays(1));
        saveShoppingItem(otherHousehold, outsider, "Other bought", ShoppingItemStatus.BOUGHT,
            ShoppingItemVisibility.SHARED, TO.minusDays(1));

        StatisticsOverviewDto overview = getDto("/overview", StatisticsOverviewDto.class);

        assertEquals(1, overview.completedTaskCount());
        assertEquals(0, new BigDecimal("15.00").compareTo(overview.totalExpenseAmount()));
        assertEquals(0, new BigDecimal("5.00").compareTo(overview.recordedPaymentAmount()));
        assertEquals(1, overview.purchasedShoppingItemCount());
    }

    @Test
    public void givenRequestedModules_whenGetOverview_thenFiltersModuleBreakdown() throws Exception {
        saveTask(household, mainTenant, roommateUser, "Completed", TaskStatus.DONE, TO.minusDays(1), null);
        saveExpense(household, mainTenantRoommate, "Expense", "20.00", TO.minusDays(1));

        MvcResult result = mockMvc.perform(get(STATISTICS_BASE_URI + "/overview")
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL))
                .param("from", FROM.toString())
                .param("to", TO.toString())
                .param("grouping", "DAY")
                .param("modules", "TASKS"))
            .andReturn();


        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        StatisticsOverviewDto overview = jsonMapper.readValue(
            result.getResponse().getContentAsString(), StatisticsOverviewDto.class);

        assertEquals(1, overview.activityByModule().size());
        assertEquals("TASKS", overview.activityByModule().get(0).key());
        assertEquals(1, overview.activityTrend().size());
        assertEquals("TASKS", overview.activityTrend().get(0).key());
    }

    @Test
    public void givenTasksAndCleaningSchedule_whenGetTaskStatistics_thenReturnsExpectedMetrics() throws Exception {
        CleaningSchedule schedule = saveCleaningSchedule(household, mainTenant, roommateUser, CleaningFrequency.WEEKLY);
        saveTask(household, mainTenant, roommateUser, "Generated done", TaskStatus.DONE, TO.minusDays(1), schedule);
        saveTask(household, mainTenant, roommateUser, "Overdue", TaskStatus.OPEN, TO.minusDays(5), null);
        saveTask(household, mainTenant, roommateUser, "Cancelled", TaskStatus.CANCELLED, TO.plusDays(1), null);

        TaskStatisticsDto statistics = getDto("/tasks", TaskStatisticsDto.class);

        assertEquals(3, statistics.totalTaskCount());
        assertEquals(1, statistics.completedTaskCount());
        assertEquals(1, statistics.cancelledTaskCount());
        assertEquals(1, statistics.overdueTaskCount());
        assertEquals(1, statistics.scheduleGeneratedTaskCount());
        assertEquals(0, new BigDecimal("100.00").compareTo(statistics.scheduleGeneratedTaskCompletionRate()));
        assertTrue(statistics.completedTasksByMember().stream()
            .anyMatch(value -> "Normal Roommate".equals(value.displayName())));
    }

    @Test
    public void givenExpensesAndPayments_whenGetExpenseStatistics_thenReturnsExpectedMetrics() throws Exception {
        saveExpense(household, mainTenantRoommate, "Groceries", "12.00", TO.minusDays(2));
        saveExpense(household, roommate, "Internet", "18.00", TO.minusDays(1));
        savePayment(household, roommate, mainTenantRoommate, "7.50", TO.minusDays(1));

        ExpenseStatisticsDto statistics = getDto("/expenses", ExpenseStatisticsDto.class);

        assertEquals(2, statistics.expenseCount());
        assertEquals(0, new BigDecimal("30.00").compareTo(statistics.totalExpenseAmount()));
        assertEquals(0, new BigDecimal("15.00").compareTo(statistics.averageExpenseAmount()));
        assertEquals(0, new BigDecimal("18.00").compareTo(statistics.largestExpenseAmount()));
        assertEquals(1, statistics.paymentCount());
        assertEquals(0, new BigDecimal("7.50").compareTo(statistics.totalPaymentAmount()));
    }

    @Test
    public void givenPrivateItemsOfAnotherUser_whenGetShoppingStatistics_thenTheyAreNotExposed() throws Exception {
        saveShoppingItem(household, mainTenant, "Shared milk", ShoppingItemStatus.BOUGHT,
            ShoppingItemVisibility.SHARED, TO.minusDays(2));
        saveShoppingItem(household, roommateUser, "Private secret", ShoppingItemStatus.BOUGHT,
            ShoppingItemVisibility.PERSONAL, TO.minusDays(1));
        saveShoppingItem(household, mainTenant, "Own personal", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.PERSONAL, TO.minusDays(1));

        ShoppingStatisticsDto statistics = getDto("/shopping", ShoppingStatisticsDto.class);

        assertEquals(2, statistics.addedItemCount());
        assertEquals(1, statistics.purchasedItemCount());
        assertTrue(statistics.mostFrequentlyPurchasedItems().stream()
            .anyMatch(value -> "Shared milk".equals(value.label())));
        assertTrue(statistics.mostFrequentlyPurchasedItems().stream()
            .noneMatch(value -> value.label().toLowerCase().contains("secret")));
    }

    @Test
    public void givenMonthlyGrouping_whenGetOverview_thenReturnsMonthBucketsIncludingEmptyMonths() throws Exception {
        LocalDate from = TO.minusMonths(2).withDayOfMonth(1);
        LocalDate to = TO.withDayOfMonth(1);
        saveExpense(household, mainTenantRoommate, "First month", "10.00", from.plusDays(2));

        MvcResult result = mockMvc.perform(get(STATISTICS_BASE_URI + "/overview")
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL))
                .param("from", from.toString())
                .param("to", to.toString())
                .param("grouping", "MONTH")
                .param("modules", "EXPENSES"))
            .andReturn();


        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        StatisticsOverviewDto overview = jsonMapper.readValue(
            result.getResponse().getContentAsString(), StatisticsOverviewDto.class);

        assertEquals(3, overview.activityTrend().get(0).points().size());
        assertEquals(BigDecimal.ONE, overview.activityTrend().get(0).points().get(0).value());
        assertEquals(BigDecimal.ZERO, overview.activityTrend().get(0).points().get(1).value());


    }

    private <T> T getDto(String path, Class<T> type) throws Exception {
        MvcResult result = mockMvc.perform(get(STATISTICS_BASE_URI + path)
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL))
                .param("from", FROM.toString())
                .param("to", TO.toString())
                .param("grouping", "DAY"))
            .andReturn();

        MockHttpServletResponse response = result.getResponse();

        assertEquals(
            HttpStatus.OK.value(),
            response.getStatus(),
            response.getContentAsString()
        );

        return jsonMapper.readValue(response.getContentAsString(), type);
    }

    private void cleanDatabase() {
        TestDbCleaner.clearAllApplicationData(
            settlementDecisionRepository,
            paymentRepository,
            expenseSplitRepository,
            expenseRepository,
            taskRepository,
            cleaningScheduleRepository,
            shoppingItemRepository,
            invitationRepository,
            roommateRepository,
            householdRepository,
            notificationRepository,
            notificationSettingRepository,
            verificationCodeRepository,
            userRepository
        );
    }

    private String tokenFor(String email) {
        return jwtTokenizer.getAuthToken(email, USER_ROLES);
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
        Household value = new Household();
        value.setName(name);
        value.setAddress("Test address");
        value.setCreatedBy(createdBy);
        return householdRepository.save(value);
    }

    private Roommate saveRoommate(ApplicationUser user, Household targetHousehold, HouseholdRole role) {
        Roommate value = new Roommate();
        value.setUser(user);
        value.setHousehold(targetHousehold);
        value.setRole(role);
        value.setActive(true);
        return roommateRepository.save(value);
    }

    private CleaningSchedule saveCleaningSchedule(
        Household targetHousehold,
        ApplicationUser creator,
        ApplicationUser assignee,
        CleaningFrequency frequency
    ) {
        CleaningSchedule schedule = new CleaningSchedule();
        schedule.setHousehold(targetHousehold);
        schedule.setTitle("Statistics schedule");
        schedule.setFrequency(frequency);
        schedule.setStartDate(FROM);
        schedule.setNextDueDate(TO);
        schedule.setActive(true);
        schedule.setCreatedBy(creator);
        schedule.setAssignedTo(assignee);
        return cleaningScheduleRepository.save(schedule);
    }

    private Task saveTask(
        Household targetHousehold,
        ApplicationUser creator,
        ApplicationUser assignee,
        String title,
        TaskStatus status,
        LocalDate dueDate,
        CleaningSchedule schedule
    ) {
        Task task = new Task();
        task.setHousehold(targetHousehold);
        task.setTitle(title);
        task.setDescription("Statistics integration test");
        task.setDueDate(dueDate);
        task.setStatus(status);
        task.setCreatedBy(creator);
        task.setAssignedTo(assignee);
        task.setCleaningSchedule(schedule);
        return taskRepository.save(task);
    }

    private Expense saveExpense(
        Household targetHousehold,
        Roommate paidBy,
        String description,
        String amount,
        LocalDate date
    ) {
        Expense expense = new Expense();
        expense.setHousehold(targetHousehold);
        expense.setPaidBy(paidBy);
        expense.setCreatedBy(paidBy);
        expense.setDescription(description);
        expense.setTotalAmount(new BigDecimal(amount));
        expense.setDate(date);
        return expenseRepository.save(expense);
    }

    private Payment savePayment(
        Household targetHousehold,
        Roommate from,
        Roommate to,
        String amount,
        LocalDate date
    ) {
        Payment payment = new Payment();
        payment.setHousehold(targetHousehold);
        payment.setFromRoommate(from);
        payment.setToRoommate(to);
        payment.setCreatedBy(from);
        payment.setAmount(new BigDecimal(amount));
        payment.setDate(date);
        return paymentRepository.save(payment);
    }

    private ShoppingItem saveShoppingItem(
        Household targetHousehold,
        ApplicationUser creator,
        String name,
        ShoppingItemStatus status,
        ShoppingItemVisibility visibility,
        LocalDate date
    ) {
        ShoppingItem item = new ShoppingItem();
        item.setHousehold(targetHousehold);
        item.setCreatedBy(creator);
        item.setName(name);
        item.setStatus(status);
        item.setVisibility(visibility);
        item.setCreatedAt(date.atTime(9, 0));
        item.setUpdatedAt(date.atTime(10, 0));
        return shoppingItemRepository.save(item);
    }
}