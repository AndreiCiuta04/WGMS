package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HistoryDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentHistoryDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskHistoryDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.HistoryService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class SimpleHistoryService implements HistoryService {

    private static final String RECORDED_PAYMENT_STATUS = "RECORDED";

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TaskRepository taskRepository;
    private final ExpenseRepository expenseRepository;
    private final UserService userService;
    private final RoommateRepository roommateRepository;

    public SimpleHistoryService(
        TaskRepository taskRepository,
        ExpenseRepository expenseRepository,
        UserService userService,
        RoommateRepository roommateRepository
    ) {
        this.taskRepository = taskRepository;
        this.expenseRepository = expenseRepository;
        this.userService = userService;
        this.roommateRepository = roommateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryDto getHistory(String currentUserEmail, boolean admin) {
        LOGGER.debug("Get history for user {}", currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);
        Long householdId = currentRoommate.getHousehold().getId();

        List<Task> taskHistory = taskRepository.findAllByHousehold_IdAndStatusInOrderByUpdatedAtDescDueDateDescCreatedAtDesc(
            householdId,
            List.of(TaskStatus.DONE, TaskStatus.CANCELLED)
        );
        List<Expense> expenseHistory = expenseRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDesc(householdId);

        return new HistoryDto(
            taskHistory.stream()
                .map(this::taskToTaskHistoryDto)
                .toList(),
            expenseHistory.stream()
                .map(this::expenseToPaymentHistoryDto)
                .toList()
        );
    }

    private TaskHistoryDto taskToTaskHistoryDto(Task task) {
        return new TaskHistoryDto(
            task.getId(),
            task.getHouseholdId(),
            task.getCleaningScheduleId(),
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

    private PaymentHistoryDto expenseToPaymentHistoryDto(Expense expense) {
        return new PaymentHistoryDto(
            expense.getId(),
            expense.getDescription(),
            expense.getTotalAmount(),
            RECORDED_PAYMENT_STATUS,
            payerEmail(expense),
            null,
            expense.getCreatedAt(),
            null
        );
    }

    private String payerEmail(Expense expense) {
        if (expense.getPaidBy() == null || expense.getPaidBy().getUser() == null) {
            return null;
        }
        return expense.getPaidBy().getUser().getEmail();
    }

    private ApplicationUser getUserByEmail(String email) {
        return userService.findApplicationUserByEmail(email);
    }

    private Roommate getCurrentRoommate(ApplicationUser currentUser) {
        return roommateRepository.findByUserIdAndActiveTrue(currentUser.getId())
            .orElseThrow(() -> new ForbiddenException("User does not belong to an active household."));
    }
}
