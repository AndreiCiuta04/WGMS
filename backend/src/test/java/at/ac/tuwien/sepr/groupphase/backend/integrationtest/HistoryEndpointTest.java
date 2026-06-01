package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HistoryDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class HistoryEndpointTest implements TestData {

    private static final String HISTORY_BASE_URI = BASE_URI + "/history";
    private static final LocalDate EXPENSE_DATE = LocalDate.now();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseSplitRepository expenseSplitRepository;

    @Autowired
    private CleaningScheduleRepository cleaningScheduleRepository;

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
    private Household household;
    private Household otherHousehold;
    private Roommate mainTenantRoommate;
    private Roommate outsiderRoommate;

    @BeforeEach
    public void beforeEach() {
        cleanDatabase();

        mainTenant = saveUser(MAIN_TENANT_EMAIL, "Main", "Tenant");
        roommate = saveUser(ROOMMATE_EMAIL, "Normal", "Roommate");
        outsider = saveUser(OUTSIDER_EMAIL, "Other", "User");

        household = saveHousehold(TEST_HOUSEHOLD_NAME, mainTenant);
        otherHousehold = saveHousehold(OTHER_HOUSEHOLD_NAME, outsider);

        mainTenantRoommate = saveRoommate(mainTenant, household, HouseholdRole.MAIN_TENANT);
        saveRoommate(roommate, household, HouseholdRole.ROOMMATE);
        outsiderRoommate = saveRoommate(outsider, otherHousehold, HouseholdRole.MAIN_TENANT);
    }

    @AfterEach
    public void afterEach() {
        cleanDatabase();
    }

    @Test
    public void givenNoToken_whenGetHistory_then403() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(HISTORY_BASE_URI))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void givenAuthenticatedUserWithoutHousehold_whenGetHistory_then403() throws Exception {
        ApplicationUser userWithoutHousehold = saveUser("without-household@test.com", "No", "Household");

        MvcResult mvcResult = this.mockMvc.perform(get(HISTORY_BASE_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(userWithoutHousehold.getEmail())))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void givenAuthenticatedHouseholdUser_whenGetHistory_then200AndHistoryArraysAreReturned() throws Exception {
        Task doneTask = saveTask(household, mainTenant, roommate, "Done task", TaskStatus.DONE);
        Task cancelledTask = saveTask(household, mainTenant, roommate, "Cancelled task", TaskStatus.CANCELLED);
        CleaningSchedule cleaningSchedule = saveCleaningSchedule(household, mainTenant, roommate);
        Task generatedTask = saveTask(household, mainTenant, roommate, "Generated task", TaskStatus.DONE, cleaningSchedule);
        saveTask(household, mainTenant, roommate, "Open task", TaskStatus.OPEN);
        saveTask(household, mainTenant, roommate, "In progress task", TaskStatus.IN_PROGRESS);
        saveTask(otherHousehold, outsider, outsider, "Other household task", TaskStatus.DONE);
        Expense expense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Groceries", money("12.00"));
        saveExpense(otherHousehold, outsiderRoommate, outsiderRoommate, "Private other expense", money("9.00"));

        MvcResult mvcResult = this.mockMvc.perform(get(HISTORY_BASE_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        HistoryDto historyDto = jsonMapper.readValue(response.getContentAsString(), HistoryDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertTrue(response.getContentAsString().contains("\"tasks\"")),
            () -> assertTrue(response.getContentAsString().contains("\"payments\"")),
            () -> assertNotNull(historyDto.tasks()),
            () -> assertNotNull(historyDto.payments()),
            () -> assertEquals(3, historyDto.tasks().size()),
            () -> assertTrue(historyDto.tasks().stream().anyMatch(task -> doneTask.getId().equals(task.id()))),
            () -> assertTrue(historyDto.tasks().stream().anyMatch(task -> cancelledTask.getId().equals(task.id()))),
            () -> assertTrue(historyDto.tasks().stream()
                .anyMatch(task -> generatedTask.getId().equals(task.id())
                    && cleaningSchedule.getId().equals(task.cleaningScheduleId()))),
            () -> assertTrue(historyDto.tasks().stream().noneMatch(task -> "Open task".equals(task.title()))),
            () -> assertTrue(historyDto.tasks().stream().noneMatch(task -> "In progress task".equals(task.title()))),
            () -> assertTrue(historyDto.tasks().stream().noneMatch(task -> "Other household task".equals(task.title()))),
            () -> assertEquals(1, historyDto.payments().size()),
            () -> assertEquals(expense.getId(), historyDto.payments().get(0).id()),
            () -> assertEquals("Groceries", historyDto.payments().get(0).title()),
            () -> assertEquals(0, money("12.00").compareTo(historyDto.payments().get(0).amount())),
            () -> assertEquals("RECORDED", historyDto.payments().get(0).status()),
            () -> assertEquals(MAIN_TENANT_EMAIL, historyDto.payments().get(0).payerEmail())
        );
    }

    private void cleanDatabase() {
        expenseSplitRepository.deleteAll();
        expenseRepository.deleteAll();
        taskRepository.deleteAll();
        cleaningScheduleRepository.deleteAll();
        roommateRepository.deleteAll();
        householdRepository.deleteAll();
        userRepository.deleteAll();
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

    private Task saveTask(
        Household household,
        ApplicationUser creator,
        ApplicationUser assignee,
        String title,
        TaskStatus status
    ) {
        return saveTask(household, creator, assignee, title, status, null);
    }

    private Task saveTask(
        Household household,
        ApplicationUser creator,
        ApplicationUser assignee,
        String title,
        TaskStatus status,
        CleaningSchedule cleaningSchedule
    ) {
        Task task = new Task();
        task.setHousehold(household);
        task.setTitle(title);
        task.setDescription(TEST_TASK_DESCRIPTION);
        task.setDueDate(TEST_TASK_DUE_DATE);
        task.setStatus(status);
        task.setCreatedBy(creator);
        task.setAssignedTo(assignee);
        task.setCleaningSchedule(cleaningSchedule);
        return taskRepository.save(task);
    }

    private CleaningSchedule saveCleaningSchedule(Household household, ApplicationUser creator, ApplicationUser assignee) {
        CleaningSchedule cleaningSchedule = new CleaningSchedule();
        cleaningSchedule.setHousehold(household);
        cleaningSchedule.setTitle("Generated schedule");
        cleaningSchedule.setDescription("Schedule used by history integration test");
        cleaningSchedule.setFrequency(CleaningFrequency.WEEKLY);
        cleaningSchedule.setStartDate(TEST_TASK_DUE_DATE);
        cleaningSchedule.setNextDueDate(TEST_TASK_DUE_DATE);
        cleaningSchedule.setActive(true);
        cleaningSchedule.setCreatedBy(creator);
        cleaningSchedule.setAssignedTo(assignee);
        return cleaningScheduleRepository.save(cleaningSchedule);
    }

    private Expense saveExpense(Household household, Roommate paidBy, Roommate createdBy, String description, BigDecimal amount) {
        Expense expense = new Expense();
        expense.setHousehold(household);
        expense.setPaidBy(paidBy);
        expense.setCreatedBy(createdBy);
        expense.setDescription(description);
        expense.setTotalAmount(amount);
        expense.setDate(EXPENSE_DATE);
        return expenseRepository.save(expense);
    }
}
