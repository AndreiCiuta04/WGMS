package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsFilterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsGrouping;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsModule;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsOverviewDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.Payment;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.HistoryStatisticsService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.HistoryStatisticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HistoryStatisticsServiceTest {

    private static final String USER_EMAIL = "statistics@test.com";
    private static final LocalDate FROM = LocalDate.of(2026, 6, 1);
    private static final LocalDate TO = LocalDate.of(2026, 6, 30);

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CleaningScheduleRepository cleaningScheduleRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ShoppingItemRepository shoppingItemRepository;

    @Mock
    private RoommateRepository roommateRepository;

    @Mock
    private UserService userService;

    private HistoryStatisticsService statisticsService;
    private ApplicationUser user;
    private ApplicationUser secondUser;
    private Household household;
    private Roommate roommate;
    private Roommate secondRoommate;

    @BeforeEach
    public void setUp() {
        statisticsService = new HistoryStatisticsServiceImpl(
            taskRepository,
            cleaningScheduleRepository,
            expenseRepository,
            paymentRepository,
            shoppingItemRepository,
            roommateRepository,
            userService
        );

        user = user(1L, USER_EMAIL, "Statistics", "User", false);
        secondUser = user(2L, "second@test.com", "Second", "Member", false);

        household = new Household();
        household.setId(10L);
        household.setName("Statistics WG");

        roommate = roommate(20L, user);
        secondRoommate = roommate(21L, secondUser);
    }

    @Test
    public void givenUserWithoutActiveHousehold_whenGetOverview_thenForbidden() {
        when(userService.findApplicationUserByEmail(USER_EMAIL)).thenReturn(user);
        when(roommateRepository.findByUserIdAndActiveTrue(user.getId())).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> statisticsService.getOverview(USER_EMAIL, allModulesFilter()));
    }

    @Test
    public void givenHouseholdActivity_whenGetOverview_thenAggregatesOnlyRequestedModules() {
        prepareContext();
        when(taskRepository.findAllByHousehold_IdOrderByDueDateAscCreatedAtDesc(household.getId()))
            .thenReturn(List.of(
                task(TaskStatus.DONE, user, FROM.plusDays(1), FROM.plusDays(2).atTime(10, 0)),
                task(TaskStatus.OPEN, secondUser, FROM.plusDays(4), FROM.plusDays(3).atTime(10, 0))
            ));
        when(cleaningScheduleRepository.findAllByHousehold_IdOrderByNextDueDateAscCreatedAtDesc(household.getId()))
            .thenReturn(List.of());
        when(expenseRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDesc(household.getId()))
            .thenReturn(List.of(expense("24.50", FROM.plusDays(5), roommate)));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of());
        when(shoppingItemRepository.findVisibleItems(
            household.getId(), user.getId(), ShoppingItemVisibility.SHARED))
            .thenReturn(List.of());

        StatisticsFilterDto filter = new StatisticsFilterDto(
            FROM,
            TO,
            StatisticsGrouping.DAY,
            EnumSet.of(StatisticsModule.TASKS, StatisticsModule.EXPENSES)
        );

        StatisticsOverviewDto result = statisticsService.getOverview(USER_EMAIL, filter);

        assertEquals(1, result.completedTaskCount());
        assertEquals(0, new BigDecimal("50.00").compareTo(result.taskCompletionRate()));
        assertEquals(0, new BigDecimal("24.50").compareTo(result.totalExpenseAmount()));
        assertEquals(2, result.totalActivityCount());
        assertEquals(2, result.activityByModule().size());
        assertTrue(result.activityByModule().stream().anyMatch(value -> "TASKS".equals(value.key())));
        assertTrue(result.activityByModule().stream().anyMatch(value -> "EXPENSES".equals(value.key())));
        assertTrue(result.activityByModule().stream().noneMatch(value -> "SHOPPING".equals(value.key())));
    }

    @Test
    public void givenTasksAndSchedules_whenGetTaskStatistics_thenCalculatesDistributionsAndMembers() {
        prepareContext();
        CleaningSchedule schedule = schedule(CleaningFrequency.WEEKLY, true, FROM.atStartOfDay());
        Task completedGenerated = task(TaskStatus.DONE, user, FROM.plusDays(1), FROM.plusDays(2).atTime(12, 0));
        completedGenerated.setCleaningSchedule(schedule);
        Task overdue = task(TaskStatus.OPEN, secondUser, FROM.minusDays(1), FROM.plusDays(3).atTime(12, 0));
        Task cancelled = task(TaskStatus.CANCELLED, secondUser, FROM.plusDays(5), FROM.plusDays(4).atTime(12, 0));

        when(taskRepository.findAllByHousehold_IdOrderByDueDateAscCreatedAtDesc(household.getId()))
            .thenReturn(List.of(completedGenerated, overdue, cancelled));
        when(cleaningScheduleRepository.findAllByHousehold_IdOrderByNextDueDateAscCreatedAtDesc(household.getId()))
            .thenReturn(List.of(schedule));

        TaskStatisticsDto result = statisticsService.getTaskStatistics(USER_EMAIL, taskFilter());

        assertEquals(3, result.totalTaskCount());
        assertEquals(1, result.completedTaskCount());
        assertEquals(1, result.cancelledTaskCount());
        assertEquals(1, result.overdueTaskCount());
        assertEquals(1, result.scheduleGeneratedTaskCount());
        assertEquals(0, new BigDecimal("100.00").compareTo(result.scheduleGeneratedTaskCompletionRate()));
        assertTrue(result.taskStatusDistribution().stream()
            .anyMatch(value -> "DONE".equals(value.key()) && value.value().longValue() == 1));
        assertTrue(result.cleaningFrequencyDistribution().stream()
            .anyMatch(value -> "WEEKLY".equals(value.key()) && value.value().longValue() == 1));
        assertEquals("Statistics User", result.completedTasksByMember().get(0).displayName());
    }

    @Test
    public void givenExpensesAndPayments_whenGetExpenseStatistics_thenCalculatesAmountsAndMemberTotals() {
        prepareContext();
        Expense first = expense("12.00", FROM.plusDays(1), roommate);
        Expense second = expense("18.00", FROM.plusDays(2), secondRoommate);
        Payment payment = payment("5.50", FROM.plusDays(3), roommate, secondRoommate);

        when(expenseRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDesc(household.getId()))
            .thenReturn(List.of(first, second));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of(payment));

        ExpenseStatisticsDto result = statisticsService.getExpenseStatistics(USER_EMAIL, expenseFilter());

        assertEquals(2, result.expenseCount());
        assertEquals(0, new BigDecimal("30.00").compareTo(result.totalExpenseAmount()));
        assertEquals(0, new BigDecimal("15.00").compareTo(result.averageExpenseAmount()));
        assertEquals(0, new BigDecimal("18.00").compareTo(result.largestExpenseAmount()));
        assertEquals(1, result.paymentCount());
        assertEquals(0, new BigDecimal("5.50").compareTo(result.totalPaymentAmount()));
        assertEquals(2, result.expensesPaidByMember().size());
        assertEquals("Statistics User", result.paymentsSentByMember().get(0).displayName());
        assertEquals("Second Member", result.paymentsReceivedByMember().get(0).displayName());
    }

    @Test
    public void givenVisibleShoppingItems_whenGetShoppingStatistics_thenNormalizesNamesAndProtectsAnonymizedName() {
        secondUser.setAnonymized(true);
        prepareContext();
        ShoppingItem boughtMilk = shoppingItem(" Milk ", ShoppingItemStatus.BOUGHT,
            ShoppingItemVisibility.SHARED, user, FROM.plusDays(1));
        ShoppingItem secondMilk = shoppingItem("milk", ShoppingItemStatus.BOUGHT,
            ShoppingItemVisibility.TARGETED, secondUser, FROM.plusDays(2));
        ShoppingItem neededBread = shoppingItem("Bread", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.PERSONAL, user, FROM.plusDays(3));

        when(shoppingItemRepository.findVisibleItems(
            household.getId(), user.getId(), ShoppingItemVisibility.SHARED))
            .thenReturn(List.of(boughtMilk, secondMilk, neededBread));

        ShoppingStatisticsDto result = statisticsService.getShoppingStatistics(USER_EMAIL, shoppingFilter());

        assertEquals(3, result.addedItemCount());
        assertEquals(2, result.purchasedItemCount());
        assertEquals(0, new BigDecimal("66.67").compareTo(result.purchaseCompletionRate()));
        assertEquals(1, result.mostFrequentlyPurchasedItems().size());
        assertEquals("Milk", result.mostFrequentlyPurchasedItems().get(0).label());
        assertEquals(2, result.mostFrequentlyPurchasedItems().get(0).value().longValue());
        assertTrue(result.itemsAddedByMember().stream()
            .anyMatch(value -> "Former member".equals(value.displayName())));
    }

    @Test
    public void givenMonthlyGrouping_whenGetShoppingStatistics_thenIncludesEmptyPeriods() {
        prepareContext();
        ShoppingItem item = shoppingItem("Soap", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.SHARED, user, LocalDate.of(2026, 4, 10));
        when(shoppingItemRepository.findVisibleItems(
            household.getId(), user.getId(), ShoppingItemVisibility.SHARED))
            .thenReturn(List.of(item));

        StatisticsFilterDto filter = new StatisticsFilterDto(
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 6, 30),
            StatisticsGrouping.MONTH,
            EnumSet.of(StatisticsModule.SHOPPING)
        );

        ShoppingStatisticsDto result = statisticsService.getShoppingStatistics(USER_EMAIL, filter);

        assertEquals(3, result.itemActivityTrend().get(0).points().size());
        assertEquals(LocalDate.of(2026, 4, 1), result.itemActivityTrend().get(0).points().get(0).periodStart());
        assertEquals(BigDecimal.ONE, result.itemActivityTrend().get(0).points().get(0).value());
        assertEquals(BigDecimal.ZERO, result.itemActivityTrend().get(0).points().get(1).value());
        assertEquals(BigDecimal.ZERO, result.itemActivityTrend().get(0).points().get(2).value());
    }

    private void prepareContext() {
        when(userService.findApplicationUserByEmail(USER_EMAIL)).thenReturn(user);
        when(roommateRepository.findByUserIdAndActiveTrue(user.getId())).thenReturn(Optional.of(roommate));
        when(roommateRepository.findAllByHouseholdIdAndActiveTrue(household.getId()))
            .thenReturn(List.of(roommate, secondRoommate));
    }

    private StatisticsFilterDto allModulesFilter() {
        return new StatisticsFilterDto(FROM, TO, StatisticsGrouping.DAY, EnumSet.allOf(StatisticsModule.class));
    }

    private StatisticsFilterDto taskFilter() {
        return new StatisticsFilterDto(
            FROM, TO, StatisticsGrouping.DAY,
            EnumSet.of(StatisticsModule.TASKS, StatisticsModule.CLEANING)
        );
    }

    private StatisticsFilterDto expenseFilter() {
        return new StatisticsFilterDto(
            FROM, TO, StatisticsGrouping.DAY,
            EnumSet.of(StatisticsModule.EXPENSES, StatisticsModule.PAYMENTS)
        );
    }

    private StatisticsFilterDto shoppingFilter() {
        return new StatisticsFilterDto(
            FROM, TO, StatisticsGrouping.DAY,
            EnumSet.of(StatisticsModule.SHOPPING)
        );
    }

    private ApplicationUser user(Long id, String email, String firstName, String lastName, boolean anonymized) {
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setId(id);
        applicationUser.setEmail(email);
        applicationUser.setFirstName(firstName);
        applicationUser.setLastName(lastName);
        applicationUser.setAnonymized(anonymized);
        return applicationUser;
    }

    private Roommate roommate(Long id, ApplicationUser applicationUser) {
        Roommate value = new Roommate();
        value.setId(id);
        value.setUser(applicationUser);
        value.setHousehold(household);
        value.setActive(true);
        return value;
    }

    private Task task(TaskStatus status, ApplicationUser assignedTo, LocalDate dueDate, LocalDateTime createdAt) {
        Task task = new Task();
        task.setHousehold(household);
        task.setTitle(status.name() + " task");
        task.setStatus(status);
        task.setDueDate(dueDate);
        task.setCreatedBy(user);
        task.setAssignedTo(assignedTo);
        task.setCreatedAt(createdAt);
        task.setUpdatedAt(createdAt.plusHours(1));
        return task;
    }

    private CleaningSchedule schedule(CleaningFrequency frequency, boolean active, LocalDateTime createdAt) {
        CleaningSchedule schedule = new CleaningSchedule();
        schedule.setHousehold(household);
        schedule.setTitle("Schedule");
        schedule.setFrequency(frequency);
        schedule.setStartDate(createdAt.toLocalDate());
        schedule.setNextDueDate(createdAt.toLocalDate());
        schedule.setActive(active);
        schedule.setCreatedBy(user);
        schedule.setAssignedTo(secondUser);
        schedule.setCreatedAt(createdAt);
        schedule.setUpdatedAt(createdAt);
        return schedule;
    }

    private Expense expense(String amount, LocalDate date, Roommate paidBy) {
        Expense expense = new Expense();
        expense.setHousehold(household);
        expense.setDescription("Expense " + amount);
        expense.setTotalAmount(new BigDecimal(amount));
        expense.setDate(date);
        expense.setPaidBy(paidBy);
        expense.setCreatedBy(paidBy);
        expense.setCreatedAt(date.atStartOfDay());
        return expense;
    }

    private Payment payment(String amount, LocalDate date, Roommate from, Roommate to) {
        Payment payment = new Payment();
        payment.setHousehold(household);
        payment.setAmount(new BigDecimal(amount));
        payment.setDate(date);
        payment.setFromRoommate(from);
        payment.setToRoommate(to);
        payment.setCreatedBy(from);
        payment.setCreatedAt(date.atStartOfDay());
        return payment;
    }

    private ShoppingItem shoppingItem(
        String name,
        ShoppingItemStatus status,
        ShoppingItemVisibility visibility,
        ApplicationUser createdBy,
        LocalDate date
    ) {
        ShoppingItem item = new ShoppingItem();
        item.setHousehold(household);
        item.setName(name);
        item.setStatus(status);
        item.setVisibility(visibility);
        item.setCreatedBy(createdBy);
        item.setCreatedAt(date.atTime(9, 0));
        item.setUpdatedAt(date.atTime(10, 0));
        return item;
    }
}