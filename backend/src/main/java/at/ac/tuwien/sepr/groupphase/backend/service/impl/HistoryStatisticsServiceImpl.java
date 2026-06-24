package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsCategoryValueDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsDataPointDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsFilterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsGrouping;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsMemberValueDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsModule;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsOverviewDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsSeriesDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HistoryStatisticsServiceImpl implements HistoryStatisticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final TaskRepository taskRepository;
    private final CleaningScheduleRepository cleaningScheduleRepository;
    private final ExpenseRepository expenseRepository;
    private final PaymentRepository paymentRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final RoommateRepository roommateRepository;
    private final UserService userService;

    public HistoryStatisticsServiceImpl(
        TaskRepository taskRepository,
        CleaningScheduleRepository cleaningScheduleRepository,
        ExpenseRepository expenseRepository,
        PaymentRepository paymentRepository,
        ShoppingItemRepository shoppingItemRepository,
        RoommateRepository roommateRepository,
        UserService userService
    ) {
        this.taskRepository = taskRepository;
        this.cleaningScheduleRepository = cleaningScheduleRepository;
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
        this.shoppingItemRepository = shoppingItemRepository;
        this.roommateRepository = roommateRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public StatisticsOverviewDto getOverview(String currentUserEmail, StatisticsFilterDto filter) {
        StatisticsContext context = context(currentUserEmail);
        LOGGER.debug("Get statistical history overview for household {}", context.householdId());

        List<Task> tasks = tasks(context.householdId(), filter);
        List<CleaningSchedule> schedules = schedules(context.householdId(), filter);
        List<Expense> expenses = expenses(context.householdId(), filter);
        List<Payment> payments = payments(context.householdId(), filter);
        List<ShoppingItem> shoppingItems = shoppingItems(context, filter);

        long completedTasks = tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
        final BigDecimal completionRate = percentage(completedTasks, tasks.size());
        final BigDecimal totalExpenses = sum(expenses, Expense::getTotalAmount);
        final BigDecimal totalPayments = sum(payments, Payment::getAmount);
        long purchasedItems = shoppingItems.stream()
            .filter(item -> item.getStatus() == ShoppingItemStatus.BOUGHT)
            .count();
        final long activeSchedules = schedules.stream().filter(CleaningSchedule::isActive).count();

        Map<StatisticsModule, Long> moduleCounts = new EnumMap<>(StatisticsModule.class);
        moduleCounts.put(StatisticsModule.TASKS, completedTasks);
        moduleCounts.put(StatisticsModule.CLEANING, (long) schedules.size());
        moduleCounts.put(StatisticsModule.EXPENSES, (long) expenses.size());
        moduleCounts.put(StatisticsModule.PAYMENTS, (long) payments.size());
        moduleCounts.put(StatisticsModule.SHOPPING, purchasedItems);

        final List<StatisticsCategoryValueDto> activityByModule = moduleCounts.entrySet().stream()
            .filter(entry -> filter.modules().contains(entry.getKey()))
            .map(entry -> category(entry.getKey().name(), label(entry.getKey().name()), entry.getValue()))
            .toList();

        List<StatisticsSeriesDto> activityTrend = new ArrayList<>();
        if (filter.modules().contains(StatisticsModule.TASKS)) {
            activityTrend.add(series("TASKS", "Completed tasks", filter,
                tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE)
                    .map(Task::getUpdatedAt).toList()));
        }
        if (filter.modules().contains(StatisticsModule.CLEANING)) {
            activityTrend.add(series("CLEANING", "Cleaning schedules", filter,
                schedules.stream().map(CleaningSchedule::getCreatedAt).toList()));
        }
        if (filter.modules().contains(StatisticsModule.EXPENSES)) {
            activityTrend.add(seriesFromDates("EXPENSES", "Expenses", filter,
                expenses.stream().map(Expense::getDate).toList()));
        }
        if (filter.modules().contains(StatisticsModule.PAYMENTS)) {
            activityTrend.add(seriesFromDates("PAYMENTS", "Payments", filter,
                payments.stream().map(Payment::getDate).toList()));
        }
        if (filter.modules().contains(StatisticsModule.SHOPPING)) {
            activityTrend.add(series("SHOPPING", "Purchased items", filter,
                shoppingItems.stream().filter(item -> item.getStatus() == ShoppingItemStatus.BOUGHT)
                    .map(ShoppingItem::getUpdatedAt).toList()));
        }

        long totalActivity = activityByModule.stream()
            .map(StatisticsCategoryValueDto::value)
            .mapToLong(BigDecimal::longValue)
            .sum();

        return new StatisticsOverviewDto(
            filter.from(),
            filter.to(),
            filter.grouping(),
            completedTasks,
            completionRate,
            totalExpenses,
            totalPayments,
            purchasedItems,
            activeSchedules,
            totalActivity,
            activityByModule,
            activityTrend
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TaskStatisticsDto getTaskStatistics(String currentUserEmail, StatisticsFilterDto filter) {
        StatisticsContext context = context(currentUserEmail);
        List<Task> tasks = tasks(context.householdId(), filter);
        List<CleaningSchedule> schedules = schedules(context.householdId(), filter);

        long completed = count(tasks, task -> task.getStatus() == TaskStatus.DONE);
        long cancelled = count(tasks, task -> task.getStatus() == TaskStatus.CANCELLED);
        long overdue = count(tasks, task -> isOverdue(task, filter.to()));
        long generated = count(tasks, task -> task.getCleaningSchedule() != null);
        long completedGenerated = count(tasks,
            task -> task.getCleaningSchedule() != null && task.getStatus() == TaskStatus.DONE);

        List<StatisticsCategoryValueDto> statusDistribution = enumDistribution(
            TaskStatus.values(),
            tasks,
            Task::getStatus
        );
        List<StatisticsCategoryValueDto> frequencyDistribution = enumDistribution(
            CleaningFrequency.values(),
            schedules,
            CleaningSchedule::getFrequency
        );

        List<StatisticsMemberValueDto> completedByMember = tasks.stream()
            .filter(task -> task.getStatus() == TaskStatus.DONE)
            .filter(task -> task.getAssignedTo() != null)
            .collect(Collectors.groupingBy(Task::getAssignedTo, Collectors.counting()))
            .entrySet().stream()
            .map(entry -> userMember(entry.getKey(), entry.getValue(), context.roommatesByUserId()))
            .sorted(Comparator.comparing(StatisticsMemberValueDto::value).reversed())
            .toList();

        Map<DayOfWeek, Long> byWeekday = tasks.stream()
            .filter(task -> task.getStatus() == TaskStatus.DONE)
            .filter(task -> task.getUpdatedAt() != null)
            .collect(Collectors.groupingBy(task -> task.getUpdatedAt().getDayOfWeek(), Collectors.counting()));
        List<StatisticsCategoryValueDto> completedByWeekday = List.of(DayOfWeek.values()).stream()
            .map(day -> category(day.name(), label(day.name()), byWeekday.getOrDefault(day, 0L)))
            .toList();

        return new TaskStatisticsDto(
            filter.from(),
            filter.to(),
            filter.grouping(),
            tasks.size(),
            completed,
            cancelled,
            overdue,
            percentage(completed, tasks.size()),
            schedules.size(),
            count(schedules, CleaningSchedule::isActive),
            generated,
            percentage(completedGenerated, generated),
            statusDistribution,
            frequencyDistribution,
            List.of(series("COMPLETED", "Completed tasks", filter,
                tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE)
                    .map(Task::getUpdatedAt).toList())),
            completedByMember,
            completedByWeekday
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseStatisticsDto getExpenseStatistics(String currentUserEmail, StatisticsFilterDto filter) {
        StatisticsContext context = context(currentUserEmail);
        List<Expense> expenses = expenses(context.householdId(), filter);
        List<Payment> payments = payments(context.householdId(), filter);

        BigDecimal totalExpenses = sum(expenses, Expense::getTotalAmount);
        BigDecimal averageExpense = expenses.isEmpty()
            ? BigDecimal.ZERO
            : totalExpenses.divide(BigDecimal.valueOf(expenses.size()), 2, RoundingMode.HALF_UP);
        BigDecimal largestExpense = expenses.stream()
            .map(Expense::getTotalAmount)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        return new ExpenseStatisticsDto(
            filter.from(),
            filter.to(),
            filter.grouping(),
            expenses.size(),
            totalExpenses,
            averageExpense,
            largestExpense,
            payments.size(),
            sum(payments, Payment::getAmount),
            List.of(amountSeries("EXPENSES", "Expense amount", filter,
                expenses, Expense::getDate, Expense::getTotalAmount)),
            List.of(amountSeries("PAYMENTS", "Payment amount", filter,
                payments, Payment::getDate, Payment::getAmount)),
            memberAmounts(expenses, expense -> expense.getPaidBy(), Expense::getTotalAmount),
            memberAmounts(payments, Payment::getFromRoommate, Payment::getAmount),
            memberAmounts(payments, Payment::getToRoommate, Payment::getAmount)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ShoppingStatisticsDto getShoppingStatistics(String currentUserEmail, StatisticsFilterDto filter) {
        StatisticsContext context = context(currentUserEmail);
        List<ShoppingItem> items = shoppingItems(context, filter);
        long purchased = count(items, item -> item.getStatus() == ShoppingItemStatus.BOUGHT);

        Map<String, Long> purchasedNames = items.stream()
            .filter(item -> item.getStatus() == ShoppingItemStatus.BOUGHT)
            .collect(Collectors.groupingBy(
                item -> item.getName().trim().toLowerCase(Locale.ROOT),
                Collectors.counting()
            ));

        List<StatisticsCategoryValueDto> frequentItems = purchasedNames.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
            .limit(10)
            .map(entry -> category(entry.getKey(), displayProductName(entry.getKey()), entry.getValue()))
            .toList();

        List<StatisticsMemberValueDto> addedByMember = items.stream()
            .filter(item -> item.getCreatedBy() != null)
            .collect(Collectors.groupingBy(ShoppingItem::getCreatedBy, Collectors.counting()))
            .entrySet().stream()
            .map(entry -> userMember(entry.getKey(), entry.getValue(), context.roommatesByUserId()))
            .sorted(Comparator.comparing(StatisticsMemberValueDto::value).reversed())
            .toList();

        return new ShoppingStatisticsDto(
            filter.from(),
            filter.to(),
            filter.grouping(),
            items.size(),
            purchased,
            percentage(purchased, items.size()),
            enumDistribution(ShoppingItemStatus.values(), items, ShoppingItem::getStatus),
            enumDistribution(ShoppingItemVisibility.values(), items, ShoppingItem::getVisibility),
            List.of(
                series("ADDED", "Items added", filter, items.stream().map(ShoppingItem::getCreatedAt).toList()),
                series("BOUGHT", "Items bought", filter,
                    items.stream().filter(item -> item.getStatus() == ShoppingItemStatus.BOUGHT)
                        .map(ShoppingItem::getUpdatedAt).toList())
            ),
            frequentItems,
            addedByMember
        );
    }

    private StatisticsContext context(String currentUserEmail) {
        ApplicationUser user = userService.findApplicationUserByEmail(currentUserEmail);
        Roommate roommate = roommateRepository.findByUserIdAndActiveTrue(user.getId())
            .orElseThrow(() -> new ForbiddenException("User does not belong to an active household."));
        List<Roommate> roommates = roommateRepository.findAllByHouseholdIdAndActiveTrue(
            roommate.getHousehold().getId()
        );
        Map<Long, Roommate> roommatesByUserId = roommates.stream()
            .filter(member -> member.getUser() != null)
            .collect(Collectors.toMap(member -> member.getUser().getId(), Function.identity()));
        return new StatisticsContext(user, roommate.getHousehold().getId(), roommatesByUserId);
    }

    private List<Task> tasks(Long householdId, StatisticsFilterDto filter) {
        return taskRepository.findAllByHousehold_IdOrderByDueDateAscCreatedAtDesc(householdId).stream()
            .filter(task -> inRange(task.getCreatedAt(), filter))
            .toList();
    }

    private List<CleaningSchedule> schedules(Long householdId, StatisticsFilterDto filter) {
        return cleaningScheduleRepository.findAllByHousehold_IdOrderByNextDueDateAscCreatedAtDesc(householdId).stream()
            .filter(schedule -> inRange(schedule.getCreatedAt(), filter))
            .toList();
    }

    private List<Expense> expenses(Long householdId, StatisticsFilterDto filter) {
        return expenseRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDesc(householdId).stream()
            .filter(expense -> inRange(expense.getDate(), filter))
            .toList();
    }

    private List<Payment> payments(Long householdId, StatisticsFilterDto filter) {
        return paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(householdId).stream()
            .filter(payment -> inRange(payment.getDate(), filter))
            .toList();
    }

    private List<ShoppingItem> shoppingItems(StatisticsContext context, StatisticsFilterDto filter) {
        return shoppingItemRepository.findVisibleItems(
            context.householdId(),
            context.user().getId(),
            ShoppingItemVisibility.SHARED
        ).stream().filter(item -> inRange(item.getCreatedAt(), filter)).toList();
    }

    private boolean inRange(LocalDateTime value, StatisticsFilterDto filter) {
        return value != null && !value.toLocalDate().isBefore(filter.from())
            && !value.toLocalDate().isAfter(filter.to());
    }

    private boolean inRange(LocalDate value, StatisticsFilterDto filter) {
        return value != null && !value.isBefore(filter.from()) && !value.isAfter(filter.to());
    }

    private boolean isOverdue(Task task, LocalDate periodEnd) {
        return task.getDueDate() != null
            && task.getDueDate().isBefore(periodEnd.plusDays(1))
            && task.getStatus() != TaskStatus.DONE
            && task.getStatus() != TaskStatus.CANCELLED;
    }

    private <T> long count(List<T> values, java.util.function.Predicate<T> predicate) {
        return values.stream().filter(predicate).count();
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
            .multiply(HUNDRED)
            .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private <T> BigDecimal sum(List<T> values, Function<T, BigDecimal> amount) {
        return values.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private StatisticsCategoryValueDto category(String key, String label, long value) {
        return new StatisticsCategoryValueDto(key, label, BigDecimal.valueOf(value));
    }

    private <E extends Enum<E>, T> List<StatisticsCategoryValueDto> enumDistribution(
        E[] values,
        List<T> source,
        Function<T, E> classifier
    ) {
        Map<E, Long> counts = source.stream()
            .filter(item -> classifier.apply(item) != null)
            .collect(Collectors.groupingBy(classifier, Collectors.counting()));
        return List.of(values).stream()
            .map(value -> category(value.name(), label(value.name()), counts.getOrDefault(value, 0L)))
            .toList();
    }

    private StatisticsSeriesDto series(
        String key,
        String label,
        StatisticsFilterDto filter,
        List<LocalDateTime> timestamps
    ) {
        return seriesFromDates(key, label, filter,
            timestamps.stream().filter(java.util.Objects::nonNull).map(LocalDateTime::toLocalDate).toList());
    }

    private StatisticsSeriesDto seriesFromDates(
        String key,
        String label,
        StatisticsFilterDto filter,
        List<LocalDate> dates
    ) {
        Map<LocalDate, BigDecimal> counts = dates.stream()
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.groupingBy(
                date -> periodStart(date, filter.grouping()),
                LinkedHashMap::new,
                Collectors.reducing(BigDecimal.ZERO, ignored -> BigDecimal.ONE, BigDecimal::add)
            ));
        return new StatisticsSeriesDto(key, label, completePoints(filter, counts));
    }

    private <T> StatisticsSeriesDto amountSeries(
        String key,
        String label,
        StatisticsFilterDto filter,
        List<T> values,
        Function<T, LocalDate> date,
        Function<T, BigDecimal> amount
    ) {
        Map<LocalDate, BigDecimal> totals = values.stream().collect(Collectors.groupingBy(
            item -> periodStart(date.apply(item), filter.grouping()),
            LinkedHashMap::new,
            Collectors.reducing(BigDecimal.ZERO, amount, BigDecimal::add)
        ));
        return new StatisticsSeriesDto(key, label, completePoints(filter, totals));
    }

    private List<StatisticsDataPointDto> completePoints(
        StatisticsFilterDto filter,
        Map<LocalDate, BigDecimal> values
    ) {
        List<StatisticsDataPointDto> points = new ArrayList<>();
        LocalDate current = periodStart(filter.from(), filter.grouping());
        LocalDate end = periodStart(filter.to(), filter.grouping());
        while (!current.isAfter(end)) {
            points.add(new StatisticsDataPointDto(current, values.getOrDefault(current, BigDecimal.ZERO)));
            current = nextPeriod(current, filter.grouping());
        }
        return points;
    }

    private LocalDate periodStart(LocalDate date, StatisticsGrouping grouping) {
        return switch (grouping) {
            case DAY -> date;
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> date.withDayOfMonth(1);
        };
    }

    private LocalDate nextPeriod(LocalDate current, StatisticsGrouping grouping) {
        return switch (grouping) {
            case DAY -> current.plusDays(1);
            case WEEK -> current.plusWeeks(1);
            case MONTH -> current.plusMonths(1);
        };
    }

    private StatisticsMemberValueDto member(ApplicationUser user, long value) {
        return new StatisticsMemberValueDto(user.getId(), displayName(user), BigDecimal.valueOf(value));
    }

    private StatisticsMemberValueDto member(Roommate roommate, BigDecimal value) {
        return new StatisticsMemberValueDto(roommate.getId(), displayName(roommate.getUser()), value);
    }

    private StatisticsMemberValueDto userMember(
        ApplicationUser user,
        long value,
        Map<Long, Roommate> roommatesByUserId
    ) {
        Roommate roommate = roommatesByUserId.get(user.getId());
        Long id = roommate == null ? null : roommate.getId();
        return new StatisticsMemberValueDto(id, displayName(user), BigDecimal.valueOf(value));
    }

    private <T> List<StatisticsMemberValueDto> memberAmounts(
        List<T> values,
        Function<T, Roommate> member,
        Function<T, BigDecimal> amount
    ) {
        return values.stream()
            .filter(item -> member.apply(item) != null)
            .collect(Collectors.groupingBy(member, Collectors.reducing(BigDecimal.ZERO, amount, BigDecimal::add)))
            .entrySet().stream()
            .map(entry -> member(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(StatisticsMemberValueDto::value).reversed())
            .toList();
    }

    private String displayName(ApplicationUser user) {
        if (user == null) {
            return "Unknown member";
        }
        if (user.isAnonymized()) {
            return "Former member";
        }
        String name = (user.getFirstName() + " " + user.getLastName()).trim();
        return name.isBlank() ? user.getEmail() : name;
    }

    private String label(String enumName) {
        String normalized = enumName.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private String displayProductName(String normalized) {
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private record StatisticsContext(
        ApplicationUser user,
        Long householdId,
        Map<Long, Roommate> roommatesByUserId
    ) {
    }
}
