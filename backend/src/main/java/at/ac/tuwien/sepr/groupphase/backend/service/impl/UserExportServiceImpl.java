package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserDataExportDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Payment;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
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
import at.ac.tuwien.sepr.groupphase.backend.service.UserExportService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class UserExportServiceImpl implements UserExportService {

    private final UserService userService;
    private final RoommateRepository roommateRepository;
    private final TaskRepository taskRepository;
    private final CleaningScheduleRepository cleaningScheduleRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final PaymentRepository paymentRepository;
    private final ShoppingItemRepository shoppingItemRepository;

    public UserExportServiceImpl(
        UserService userService,
        RoommateRepository roommateRepository,
        TaskRepository taskRepository,
        CleaningScheduleRepository cleaningScheduleRepository,
        ExpenseRepository expenseRepository,
        ExpenseSplitRepository expenseSplitRepository,
        PaymentRepository paymentRepository,
        ShoppingItemRepository shoppingItemRepository
    ) {
        this.userService = userService;
        this.roommateRepository = roommateRepository;
        this.taskRepository = taskRepository;
        this.cleaningScheduleRepository = cleaningScheduleRepository;
        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.paymentRepository = paymentRepository;
        this.shoppingItemRepository = shoppingItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDataExportDto exportCurrentUser(String email) {
        ApplicationUser user = userService.findApplicationUserByEmail(email);
        Roommate roommate = roommateRepository.findByUserIdAndActiveTrue(user.getId()).orElse(null);

        if (roommate == null) {
            return new UserDataExportDto(
                Instant.now(),
                toAccount(user),
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new UserDataExportDto.HistoryExportDto(List.of(), List.of(), List.of())
            );
        }

        Long householdId = roommate.getHousehold().getId();
        List<UserDataExportDto.TaskExportDto> tasks = taskRepository
            .findAllByHousehold_IdOrderByDueDateAscCreatedAtDesc(householdId).stream()
            .map(this::toTask)
            .toList();
        List<UserDataExportDto.CleaningScheduleExportDto> cleaningSchedules = cleaningScheduleRepository
            .findAllByHousehold_IdOrderByNextDueDateAscCreatedAtDesc(householdId).stream()
            .map(this::toCleaningSchedule)
            .toList();
        List<UserDataExportDto.ExpenseExportDto> expenses = expenseRepository
            .findAllByHouseholdIdOrderByDateDescCreatedAtDesc(householdId).stream()
            .map(this::toExpense)
            .toList();
        List<UserDataExportDto.PaymentExportDto> payments = paymentRepository
            .findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(householdId).stream()
            .map(this::toPayment)
            .toList();
        List<UserDataExportDto.ShoppingItemExportDto> shoppingItems = shoppingItemRepository
            .findVisibleItems(householdId, user.getId(), ShoppingItemVisibility.SHARED).stream()
            .map(this::toShoppingItem)
            .toList();

        return new UserDataExportDto(
            Instant.now(),
            toAccount(user),
            new UserDataExportDto.HouseholdMembershipExportDto(
                householdId,
                roommate.getHousehold().getName(),
                roommate.getId(),
                roommate.getRole().name(),
                roommate.getJoinedAt()
            ),
            tasks,
            cleaningSchedules,
            expenses,
            payments,
            shoppingItems,
            new UserDataExportDto.HistoryExportDto(
                tasks.stream()
                    .filter(task -> task.status() == TaskStatus.DONE || task.status() == TaskStatus.CANCELLED)
                    .toList(),
                expenses,
                payments
            )
        );
    }

    private UserDataExportDto.AccountExportDto toAccount(ApplicationUser user) {
        return new UserDataExportDto.AccountExportDto(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            Boolean.TRUE.equals(user.getAdmin()) ? "ADMIN" : "USER",
            user.getTermsAcceptedAt(),
            user.getTermsVersion(),
            user.getPrivacyAcknowledgedAt(),
            user.getPrivacyVersion()
        );
    }

    private UserDataExportDto.TaskExportDto toTask(Task task) {
        return new UserDataExportDto.TaskExportDto(
            task.getId(),
            task.getHouseholdId(),
            task.getTitle(),
            task.getDescription(),
            task.getDueDate(),
            task.getStatus(),
            task.getCreatedByEmail(),
            task.getAssignedToEmail(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }

    private UserDataExportDto.CleaningScheduleExportDto toCleaningSchedule(CleaningSchedule schedule) {
        return new UserDataExportDto.CleaningScheduleExportDto(
            schedule.getId(),
            schedule.getHouseholdId(),
            schedule.getTitle(),
            schedule.getDescription(),
            schedule.getFrequency(),
            schedule.getStartDate(),
            schedule.getEndDate(),
            schedule.getNextDueDate(),
            schedule.isActive(),
            schedule.getCreatedByEmail(),
            schedule.getAssignedToEmail(),
            schedule.getCreatedAt(),
            schedule.getUpdatedAt()
        );
    }

    private UserDataExportDto.ExpenseExportDto toExpense(Expense expense) {
        return new UserDataExportDto.ExpenseExportDto(
            expense.getId(),
            expense.getHousehold().getId(),
            expense.getDescription(),
            expense.getTotalAmount(),
            expense.getDate(),
            email(expense.getPaidBy()),
            email(expense.getCreatedBy()),
            expenseSplitRepository.findAllByExpenseId(expense.getId()).stream()
                .map(this::toExpenseSplit)
                .toList(),
            expense.getCreatedAt()
        );
    }

    private UserDataExportDto.ExpenseSplitExportDto toExpenseSplit(ExpenseSplit split) {
        return new UserDataExportDto.ExpenseSplitExportDto(
            split.getRoommate().getId(),
            email(split.getRoommate()),
            split.getAmountOwed()
        );
    }

    private UserDataExportDto.PaymentExportDto toPayment(Payment payment) {
        return new UserDataExportDto.PaymentExportDto(
            payment.getId(),
            payment.getHousehold().getId(),
            payment.getFromRoommate().getId(),
            email(payment.getFromRoommate()),
            payment.getToRoommate().getId(),
            email(payment.getToRoommate()),
            payment.getAmount(),
            payment.getDate(),
            email(payment.getCreatedBy()),
            payment.getCreatedAt()
        );
    }

    private UserDataExportDto.ShoppingItemExportDto toShoppingItem(ShoppingItem item) {
        return new UserDataExportDto.ShoppingItemExportDto(
            item.getId(),
            item.getHousehold().getId(),
            item.getName(),
            item.getQuantityCount(),
            item.getQuantityAmount(),
            item.getQuantityUnit(),
            item.getNotes(),
            item.getStatus(),
            item.getVisibility(),
            item.getCreatedBy().getEmail(),
            item.getTargetUser() == null ? null : item.getTargetUser().getEmail(),
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }

    private String email(Roommate roommate) {
        return roommate == null || roommate.getUser() == null ? null : roommate.getUser().getEmail();
    }
}
