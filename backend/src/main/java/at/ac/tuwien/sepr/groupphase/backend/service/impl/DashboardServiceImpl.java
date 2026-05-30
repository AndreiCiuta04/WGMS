package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BalanceDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DashboardDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.CleaningScheduleMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.TaskMapper;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.CleaningScheduleService;
import at.ac.tuwien.sepr.groupphase.backend.service.DashboardService;
import at.ac.tuwien.sepr.groupphase.backend.service.ExpenseService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShoppingItemService;
import at.ac.tuwien.sepr.groupphase.backend.service.TaskService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TaskService taskService;
    private final CleaningScheduleService cleaningScheduleService;
    private final ShoppingItemService shoppingItemService;
    private final ExpenseService expenseService;
    private final UserService userService;
    private final RoommateRepository roommateRepository;
    private final TaskMapper taskMapper;
    private final CleaningScheduleMapper cleaningScheduleMapper;

    public DashboardServiceImpl(
        TaskService taskService,
        CleaningScheduleService cleaningScheduleService,
        ShoppingItemService shoppingItemService,
        ExpenseService expenseService,
        UserService userService,
        RoommateRepository roommateRepository,
        TaskMapper taskMapper,
        CleaningScheduleMapper cleaningScheduleMapper
    ) {
        this.taskService = taskService;
        this.cleaningScheduleService = cleaningScheduleService;
        this.shoppingItemService = shoppingItemService;
        this.expenseService = expenseService;
        this.userService = userService;
        this.roommateRepository = roommateRepository;
        this.taskMapper = taskMapper;
        this.cleaningScheduleMapper = cleaningScheduleMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDto getDashboardData(String currentUserEmail) {
        LOGGER.debug("Get dashboard data for user {}", currentUserEmail);

        ApplicationUser currentUser = userService.findApplicationUserByEmail(currentUserEmail);
        Roommate currentRoommate = roommateRepository.findByUserIdAndActiveTrue(currentUser.getId())
            .orElseThrow(() -> new ForbiddenException("User does not belong to an active household."));

        Long householdId = currentRoommate.getHousehold().getId();

        // 1. All household tasks count (excluding DONE/CANCELLED)
        List<Task> allVisibleTasks = taskService.findVisibleTasks(currentUserEmail, false);
        long openTaskCount = allVisibleTasks.stream()
            .filter(t -> t.getStatus() != TaskStatus.DONE && t.getStatus() != TaskStatus.CANCELLED)
            .count();

        // 2. Tasks assigned to current user (excluding DONE/CANCELLED)
        List<Task> myTasks = taskService.findAssignedToCurrentUser(currentUserEmail);
        List<Task> myPending = myTasks.stream()
            .filter(t -> t.getStatus() != TaskStatus.DONE && t.getStatus() != TaskStatus.CANCELLED)
            .sorted(Comparator.comparing(Task::getDueDate))
            .toList();
        List<TaskDetailDto> myPendingDtos = taskMapper.taskToTaskDetailDto(myPending);

        // 3. Active schedules
        List<CleaningSchedule> activeSchedules = cleaningScheduleService.findActiveSchedules(currentUserEmail, false);
        List<CleaningSchedule> sortedSchedules = activeSchedules.stream()
            .sorted(Comparator.comparing(CleaningSchedule::getNextDueDate))
            .limit(5)
            .toList();
        List<CleaningScheduleDetailDto> scheduleDtos = cleaningScheduleMapper.cleaningScheduleToCleaningScheduleDetailDto(sortedSchedules);

        // 4. Housemates
        List<Roommate> roommates = roommateRepository.findAllByHouseholdIdAndActiveTrue(householdId);
        int housemateCount = roommates.size();

        // 5. Shopping items (needed, limited to 5)
        List<ShoppingItemDto> allShoppingItems = shoppingItemService.findVisibleItems(currentUser);
        List<ShoppingItemDto> wgShopping = allShoppingItems.stream()
            .filter(item -> "NEEDED".equals(item.status().name()) || "NEEDED".equals(item.status().toString()))
            .sorted((a, b) -> b.createdAt().compareTo(a.createdAt()))
            .limit(5)
            .toList();

        // 6. Expense balances
        List<BalanceDto> balances = expenseService.calculateBalances(currentUserEmail);
        BigDecimal net = BigDecimal.ZERO;
        List<BalanceDto> debtsToReceive = new ArrayList<>();
        List<BalanceDto> debtsToPay = new ArrayList<>();

        for (BalanceDto b : balances) {
            if (currentUserEmail.equals(b.fromRoommateEmail())) {
                net = net.subtract(b.amount());
                debtsToPay.add(b);
            } else if (currentUserEmail.equals(b.toRoommateEmail())) {
                net = net.add(b.amount());
                debtsToReceive.add(b);
            }
        }

        return new DashboardDto(
            (int) openTaskCount,
            myPendingDtos.size(),
            activeSchedules.size(),
            housemateCount,
            myPendingDtos,
            scheduleDtos,
            wgShopping,
            net,
            debtsToReceive,
            debtsToPay
        );
    }
}
