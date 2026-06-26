package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserDataExportDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Payment;
import at.ac.tuwien.sepr.groupphase.backend.entity.QuantityUnit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.UserExportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserExportServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private RoommateRepository roommateRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CleaningScheduleRepository cleaningScheduleRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private ExpenseSplitRepository expenseSplitRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ShoppingItemRepository shoppingItemRepository;

    private UserExportServiceImpl service;
    private ApplicationUser user;

    @BeforeEach
    public void setUp() {
        service = new UserExportServiceImpl(
            userService,
            roommateRepository,
            taskRepository,
            cleaningScheduleRepository,
            expenseRepository,
            expenseSplitRepository,
            paymentRepository,
            shoppingItemRepository
        );
        user = user(1L, "export@example.com", "Export", "User", false);
    }

    @Test
    public void givenUserWithoutActiveRoommate_whenExportCurrentUser_thenOnlyAccountDataIsExported() {
        when(userService.findApplicationUserByEmail(user.getEmail())).thenReturn(user);
        when(roommateRepository.findByUserIdAndActiveTrue(user.getId())).thenReturn(Optional.empty());

        UserDataExportDto export = service.exportCurrentUser(user.getEmail());

        assertNotNull(export.generatedAt());
        assertEquals(user.getEmail(), export.account().email());
        assertEquals("USER", export.account().role());
        assertNull(export.householdMembership());
        assertTrue(export.tasks().isEmpty());
        assertTrue(export.cleaningSchedules().isEmpty());
        assertTrue(export.expenses().isEmpty());
        assertTrue(export.payments().isEmpty());
        assertTrue(export.shoppingItems().isEmpty());
        assertTrue(export.history().completedOrCancelledTasks().isEmpty());
        verify(taskRepository, never()).findAllByHousehold_IdOrderByDueDateAscCreatedAtDesc(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void givenActiveHousehold_whenExportCurrentUser_thenMapsHouseholdFinancialShoppingAndHistoryData() {
        ApplicationUser otherUser = user(2L, "other@example.com", "Other", "Member", true);
        Household household = household(10L, "Export WG", user);
        Roommate currentRoommate = roommate(100L, user, household, HouseholdRole.MAIN_TENANT);
        Roommate otherRoommate = roommate(200L, otherUser, household, HouseholdRole.ROOMMATE);
        Roommate formerAnonymousRoommate = roommate(300L, null, household, HouseholdRole.ROOMMATE);

        Task doneTask = task(1000L, household, "Done task", TaskStatus.DONE, user, otherUser);
        Task openTask = task(1001L, household, "Open task", TaskStatus.OPEN, user, null);
        CleaningSchedule schedule = schedule(2000L, household, user, otherUser);
        Expense expense = expense(3000L, household, otherRoommate, formerAnonymousRoommate);
        ExpenseSplit splitWithEmail = split(expense, currentRoommate, "12.34");
        ExpenseSplit anonymousSplit = split(expense, formerAnonymousRoommate, "5.00");
        Payment payment = payment(4000L, household, currentRoommate, formerAnonymousRoommate, otherRoommate);
        ShoppingItem item = shoppingItem(5000L, household, user, otherUser);

        when(userService.findApplicationUserByEmail(user.getEmail())).thenReturn(user);
        when(roommateRepository.findByUserIdAndActiveTrue(user.getId())).thenReturn(Optional.of(currentRoommate));
        when(taskRepository.findAllByHousehold_IdOrderByDueDateAscCreatedAtDesc(household.getId()))
            .thenReturn(List.of(doneTask, openTask));
        when(cleaningScheduleRepository.findAllByHousehold_IdOrderByNextDueDateAscCreatedAtDesc(household.getId()))
            .thenReturn(List.of(schedule));
        when(expenseRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDesc(household.getId()))
            .thenReturn(List.of(expense));
        when(expenseSplitRepository.findAllByExpenseId(expense.getId())).thenReturn(List.of(splitWithEmail, anonymousSplit));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of(payment));
        when(shoppingItemRepository.findVisibleItems(household.getId(), user.getId(), ShoppingItemVisibility.SHARED))
            .thenReturn(List.of(item));

        UserDataExportDto export = service.exportCurrentUser(user.getEmail());

        assertEquals(household.getId(), export.householdMembership().householdId());
        assertEquals(currentRoommate.getId(), export.householdMembership().roommateId());
        assertEquals("MAIN_TENANT", export.householdMembership().membershipRole());
        assertEquals(2, export.tasks().size());
        assertEquals("other@example.com", export.tasks().getFirst().assignedToEmail());
        assertNull(export.tasks().get(1).assignedToEmail());
        assertEquals(1, export.cleaningSchedules().size());
        assertEquals(CleaningFrequency.WEEKLY, export.cleaningSchedules().getFirst().frequency());
        assertEquals(1, export.expenses().size());
        assertEquals("Shared export expense", export.expenses().getFirst().description());
        assertNull(export.expenses().getFirst().createdByEmail());
        assertEquals(2, export.expenses().getFirst().splits().size());
        assertNull(export.expenses().getFirst().splits().get(1).roommateEmail());
        assertEquals(1, export.payments().size());
        assertNull(export.payments().getFirst().toEmail());
        assertEquals("other@example.com", export.payments().getFirst().createdByEmail());
        assertEquals(1, export.shoppingItems().size());
        assertEquals("other@example.com", export.shoppingItems().getFirst().targetUserEmail());
        assertEquals(List.of(export.tasks().getFirst()), export.history().completedOrCancelledTasks());
        assertEquals(export.expenses(), export.history().recordedExpenses());
        assertEquals(export.payments(), export.history().recordedPayments());
    }

    private ApplicationUser user(Long id, String email, String firstName, String lastName, boolean admin) {
        ApplicationUser result = new ApplicationUser(email, "hash", firstName, lastName, admin);
        result.setId(id);
        result.setTermsAcceptedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        result.setTermsVersion("1.0");
        result.setPrivacyAcknowledgedAt(LocalDateTime.of(2026, 1, 2, 10, 0));
        result.setPrivacyVersion("1.0");
        return result;
    }

    private Household household(Long id, String name, ApplicationUser createdBy) {
        Household result = new Household();
        result.setId(id);
        result.setName(name);
        result.setCreatedBy(createdBy);
        return result;
    }

    private Roommate roommate(Long id, ApplicationUser user, Household household, HouseholdRole role) {
        Roommate result = new Roommate();
        result.setId(id);
        result.setUser(user);
        result.setHousehold(household);
        result.setRole(role);
        result.setJoinedAt(LocalDateTime.of(2026, 1, 3, 10, 0));
        result.setActive(true);
        return result;
    }

    private Task task(Long id, Household household, String title, TaskStatus status, ApplicationUser createdBy, ApplicationUser assignedTo) {
        Task result = new Task();
        result.setId(id);
        result.setHousehold(household);
        result.setTitle(title);
        result.setDescription(title + " description");
        result.setDueDate(LocalDate.of(2026, 2, 1));
        result.setStatus(status);
        result.setCreatedBy(createdBy);
        result.setAssignedTo(assignedTo);
        result.setCreatedAt(LocalDateTime.of(2026, 1, 10, 10, 0));
        result.setUpdatedAt(LocalDateTime.of(2026, 1, 10, 11, 0));
        return result;
    }

    private CleaningSchedule schedule(Long id, Household household, ApplicationUser createdBy, ApplicationUser assignedTo) {
        CleaningSchedule result = new CleaningSchedule();
        result.setId(id);
        result.setHousehold(household);
        result.setTitle("Weekly export cleaning");
        result.setDescription("Exported cleaning schedule");
        result.setFrequency(CleaningFrequency.WEEKLY);
        result.setStartDate(LocalDate.of(2026, 2, 1));
        result.setEndDate(LocalDate.of(2026, 3, 1));
        result.setNextDueDate(LocalDate.of(2026, 2, 8));
        result.setActive(true);
        result.setCreatedBy(createdBy);
        result.setAssignedTo(assignedTo);
        result.setCreatedAt(LocalDateTime.of(2026, 1, 11, 10, 0));
        result.setUpdatedAt(LocalDateTime.of(2026, 1, 11, 11, 0));
        return result;
    }

    private Expense expense(Long id, Household household, Roommate paidBy, Roommate createdBy) {
        Expense result = new Expense();
        result.setId(id);
        result.setHousehold(household);
        result.setDescription("Shared export expense");
        result.setTotalAmount(new BigDecimal("17.34"));
        result.setDate(LocalDate.of(2026, 2, 2));
        result.setPaidBy(paidBy);
        result.setCreatedBy(createdBy);
        result.setCreatedAt(LocalDateTime.of(2026, 1, 12, 10, 0));
        return result;
    }

    private ExpenseSplit split(Expense expense, Roommate roommate, String amount) {
        ExpenseSplit result = new ExpenseSplit();
        result.setExpense(expense);
        result.setRoommate(roommate);
        result.setAmountOwed(new BigDecimal(amount));
        return result;
    }

    private Payment payment(Long id, Household household, Roommate from, Roommate to, Roommate createdBy) {
        Payment result = new Payment();
        result.setId(id);
        result.setHousehold(household);
        result.setFromRoommate(from);
        result.setToRoommate(to);
        result.setCreatedBy(createdBy);
        result.setAmount(new BigDecimal("5.00"));
        result.setDate(LocalDate.of(2026, 2, 3));
        result.setCreatedAt(LocalDateTime.of(2026, 1, 13, 10, 0));
        return result;
    }

    private ShoppingItem shoppingItem(Long id, Household household, ApplicationUser createdBy, ApplicationUser targetUser) {
        ShoppingItem result = new ShoppingItem();
        result.setId(id);
        result.setHousehold(household);
        result.setName("Export coffee");
        result.setQuantityCount(2);
        result.setQuantityAmount(new BigDecimal("0.500"));
        result.setQuantityUnit(QuantityUnit.KILOGRAM);
        result.setNotes("Beans");
        result.setStatus(ShoppingItemStatus.NEEDED);
        result.setVisibility(ShoppingItemVisibility.TARGETED);
        result.setCreatedBy(createdBy);
        result.setTargetUser(targetUser);
        result.setCreatedAt(LocalDateTime.of(2026, 1, 14, 10, 0));
        result.setUpdatedAt(LocalDateTime.of(2026, 1, 14, 11, 0));
        return result;
    }
}
