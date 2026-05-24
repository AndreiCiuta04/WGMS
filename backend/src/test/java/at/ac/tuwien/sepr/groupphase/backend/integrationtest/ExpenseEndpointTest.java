package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EqualSplitRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseSplitCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ExpenseEndpointTest implements TestData {

    private static final String EXPENSE_BASE_URI = BASE_URI + "/expenses";
    private static final LocalDate EXPENSE_DATE = LocalDate.now();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseSplitRepository expenseSplitRepository;

    @Autowired
    private TaskRepository taskRepository;

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
    private Roommate roommateRecord;
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
        roommateRecord = saveRoommate(roommate, household, HouseholdRole.ROOMMATE);
        outsiderRoommate = saveRoommate(outsider, otherHousehold, HouseholdRole.MAIN_TENANT);
    }

    @AfterEach
    public void afterEach() {
        cleanDatabase();
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

    @Test
    public void givenHouseholdMember_whenCreateExpense_then201AndExpenseIsPersistedWithSplits() throws Exception {
        ExpenseCreateDto createDto = validCreateDto(
            mainTenantRoommate.getId(),
            new ExpenseSplitCreateDto(mainTenantRoommate.getId(), money("6.00")),
            new ExpenseSplitCreateDto(roommateRecord.getId(), money("6.00"))
        );

        MvcResult mvcResult = this.mockMvc.perform(post(EXPENSE_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        ExpenseDto expenseDto = jsonMapper.readValue(response.getContentAsString(), ExpenseDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED.value(), response.getStatus()),
            () -> assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType()),
            () -> assertNotNull(expenseDto.id()),
            () -> assertEquals(household.getId(), expenseDto.householdId()),
            () -> assertEquals("Groceries", expenseDto.description()),
            () -> assertEquals(0, money("12.00").compareTo(expenseDto.totalAmount())),
            () -> assertEquals(mainTenantRoommate.getId(), expenseDto.paidByRoommateId()),
            () -> assertEquals(mainTenantRoommate.getId(), expenseDto.createdByRoommateId()),
            () -> assertEquals(2, expenseDto.splits().size()),
            () -> assertEquals(1, expenseRepository.findAll().size()),
            () -> assertEquals(2, expenseSplitRepository.findAll().size())
        );
    }

    @Test
    public void givenNoToken_whenCreateExpense_then403AndNoExpenseIsCreated() throws Exception {
        ExpenseCreateDto createDto = validCreateDto(
            mainTenantRoommate.getId(),
            new ExpenseSplitCreateDto(mainTenantRoommate.getId(), money("12.00"))
        );

        MvcResult mvcResult = this.mockMvc.perform(post(EXPENSE_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createDto)))
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, expenseRepository.findAll().size())
        );
    }

    @Test
    public void givenInvalidAmount_whenCreateExpense_then400AndNoExpenseIsCreated() throws Exception {
        ExpenseCreateDto createDto = new ExpenseCreateDto(
            "Invalid amount",
            money("-1.00"),
            EXPENSE_DATE,
            mainTenantRoommate.getId(),
            List.of(new ExpenseSplitCreateDto(mainTenantRoommate.getId(), money("1.00")))
        );

        MvcResult mvcResult = this.mockMvc.perform(post(EXPENSE_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, expenseRepository.findAll().size())
        );
    }

    @Test
    public void givenInvalidSplitSum_whenCreateExpense_then409AndNoPartialExpenseIsCreated() throws Exception {
        ExpenseCreateDto createDto = validCreateDto(
            mainTenantRoommate.getId(),
            new ExpenseSplitCreateDto(mainTenantRoommate.getId(), money("5.00")),
            new ExpenseSplitCreateDto(roommateRecord.getId(), money("5.00"))
        );

        MvcResult mvcResult = this.mockMvc.perform(post(EXPENSE_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.CONFLICT.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, expenseRepository.findAll().size()),
            () -> assertEquals(0, expenseSplitRepository.findAll().size())
        );
    }

    @Test
    public void givenParticipantFromOtherHousehold_whenCreateExpense_then404AndNoExpenseIsCreated() throws Exception {
        ExpenseCreateDto createDto = validCreateDto(
            mainTenantRoommate.getId(),
            new ExpenseSplitCreateDto(mainTenantRoommate.getId(), money("6.00")),
            new ExpenseSplitCreateDto(outsiderRoommate.getId(), money("6.00"))
        );

        MvcResult mvcResult = this.mockMvc.perform(post(EXPENSE_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, expenseRepository.findAll().size())
        );
    }

    @Test
    public void givenExpensesInDifferentHouseholds_whenListExpenses_thenOnlyCurrentHouseholdIsReturned() throws Exception {
        Expense ownExpense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Own WG expense", money("10.00"));
        saveExpense(otherHousehold, outsiderRoommate, outsiderRoommate, "Other WG expense", money("9.00"));

        MvcResult mvcResult = this.mockMvc.perform(get(EXPENSE_BASE_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        ExpenseDto[] expenses = jsonMapper.readValue(mvcResult.getResponse().getContentAsString(), ExpenseDto[].class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(1, expenses.length),
            () -> assertEquals(ownExpense.getId(), expenses[0].id()),
            () -> assertEquals(household.getId(), expenses[0].householdId())
        );
    }

    @Test
    public void givenExpenseInHousehold_whenFindExpense_then200AndExpenseIsReturned() throws Exception {
        Expense expense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Internet bill", money("30.00"));
        saveSplit(expense, mainTenantRoommate, money("15.00"));
        saveSplit(expense, roommateRecord, money("15.00"));

        MvcResult mvcResult = this.mockMvc.perform(get(EXPENSE_BASE_URI + "/{id}", expense.getId())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        ExpenseDto expenseDto = jsonMapper.readValue(mvcResult.getResponse().getContentAsString(), ExpenseDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(expense.getId(), expenseDto.id()),
            () -> assertEquals("Internet bill", expenseDto.description()),
            () -> assertEquals(2, expenseDto.splits().size())
        );
    }

    @Test
    public void givenCreator_whenUpdateExpense_then200AndExpenseAndSplitsAreUpdated() throws Exception {
        Expense expense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Old expense", money("12.00"));
        saveSplit(expense, mainTenantRoommate, money("6.00"));
        saveSplit(expense, roommateRecord, money("6.00"));

        ExpenseUpdateDto updateDto = new ExpenseUpdateDto(
            "Updated expense",
            money("20.00"),
            EXPENSE_DATE,
            mainTenantRoommate.getId(),
            List.of(
                new ExpenseSplitCreateDto(mainTenantRoommate.getId(), money("10.00")),
                new ExpenseSplitCreateDto(roommateRecord.getId(), money("10.00"))
            )
        );

        MvcResult mvcResult = this.mockMvc.perform(put(EXPENSE_BASE_URI + "/{id}", expense.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        ExpenseDto expenseDto = jsonMapper.readValue(mvcResult.getResponse().getContentAsString(), ExpenseDto.class);
        BigDecimal splitSum = expenseDto.splits().stream()
            .map(split -> split.amountOwed())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals("Updated expense", expenseDto.description()),
            () -> assertEquals(0, money("20.00").compareTo(expenseDto.totalAmount())),
            () -> assertEquals(0, money("20.00").compareTo(splitSum)),
            () -> assertEquals(2, expenseSplitRepository.findAll().size())
        );
    }

    @Test
    public void givenTenEuroAndThreeParticipants_whenCalculateEqualSplits_thenRemainderIsDistributedAndTotalMatches() throws Exception {
        EqualSplitRequestDto requestDto = new EqualSplitRequestDto(
            money("10.00"),
            List.of(mainTenantRoommate.getId(), roommateRecord.getId(), saveRoommate(saveUser(SECOND_ROOMMATE_EMAIL, "Second", "Roommate"), household, HouseholdRole.ROOMMATE).getId())
        );

        MvcResult mvcResult = this.mockMvc.perform(post(EXPENSE_BASE_URI + "/equal-split")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(requestDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        ExpenseSplitCreateDto[] splits = jsonMapper.readValue(mvcResult.getResponse().getContentAsString(), ExpenseSplitCreateDto[].class);
        BigDecimal splitSum = List.of(splits).stream()
            .map(ExpenseSplitCreateDto::amountOwed)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(3, splits.length),
            () -> assertEquals(0, money("10.00").compareTo(splitSum)),
            () -> assertEquals(0, money("3.34").compareTo(splits[0].amountOwed())),
            () -> assertEquals(0, money("3.33").compareTo(splits[1].amountOwed())),
            () -> assertEquals(0, money("3.33").compareTo(splits[2].amountOwed()))
        );
    }

    @Test
    public void givenCreator_whenDeleteExpense_then204AndSplitsAreDeleted() throws Exception {
        Expense expense = saveExpense(household, mainTenantRoommate, mainTenantRoommate, "Delete me", money("12.00"));
        saveSplit(expense, mainTenantRoommate, money("12.00"));

        MvcResult mvcResult = this.mockMvc.perform(delete(EXPENSE_BASE_URI + "/{id}", expense.getId())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.NO_CONTENT.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, expenseRepository.findAll().size()),
            () -> assertEquals(0, expenseSplitRepository.findAll().size())
        );
    }

    @Test
    public void givenExpenseFromOtherHousehold_whenFindExpense_then404() throws Exception {
        Expense otherExpense = saveExpense(otherHousehold, outsiderRoommate, outsiderRoommate, "Private", money("5.00"));

        MvcResult mvcResult = this.mockMvc.perform(get(EXPENSE_BASE_URI + "/{id}", otherExpense.getId())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.NOT_FOUND.value(), mvcResult.getResponse().getStatus()),
            () -> assertFalse(mvcResult.getResponse().getContentAsString().contains("Private"))
        );
    }

    private ExpenseCreateDto validCreateDto(Long paidByRoommateId, ExpenseSplitCreateDto... splits) {
        return new ExpenseCreateDto("Groceries", money("12.00"), EXPENSE_DATE, paidByRoommateId, List.of(splits));
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
        expense.setDate(EXPENSE_DATE);
        return expenseRepository.save(expense);
    }

    private ExpenseSplit saveSplit(Expense expense, Roommate roommate, BigDecimal amount) {
        ExpenseSplit split = new ExpenseSplit();
        split.setExpense(expense);
        split.setRoommate(roommate);
        split.setAmountOwed(amount);
        return expenseSplitRepository.save(split);
    }
}
