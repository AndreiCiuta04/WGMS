package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsFilterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsGrouping;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsModule;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsOverviewDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.HistoryStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1/history/statistics")
public class HistoryStatisticsEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int MAX_RANGE_DAYS = 3660;

    private final HistoryStatisticsService statisticsService;

    public HistoryStatisticsEndpoint(HistoryStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Secured("ROLE_USER")
    @GetMapping("/overview")
    @Operation(summary = "Get household statistical history overview",
        security = @SecurityRequirement(name = "apiKey"))
    public StatisticsOverviewDto getOverview(
        Authentication authentication,
        @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(name = "grouping", defaultValue = "DAY") StatisticsGrouping grouping,
        @RequestParam(name = "modules", required = false) Set<StatisticsModule> modules
    ) {
        LOGGER.info("GET /api/v1/history/statistics/overview");
        return statisticsService.getOverview(authentication.getName(), filter(from, to, grouping, modules));
    }

    @Secured("ROLE_USER")
    @GetMapping("/tasks")
    @Operation(summary = "Get household task and cleaning statistics",
        security = @SecurityRequirement(name = "apiKey"))
    public TaskStatisticsDto getTaskStatistics(
        Authentication authentication,
        @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(name = "grouping", defaultValue = "DAY") StatisticsGrouping grouping
    ) {
        LOGGER.info("GET /api/v1/history/statistics/tasks");
        return statisticsService.getTaskStatistics(
            authentication.getName(),
            filter(from, to, grouping, EnumSet.of(StatisticsModule.TASKS, StatisticsModule.CLEANING))
        );
    }

    @Secured("ROLE_USER")
    @GetMapping("/expenses")
    @Operation(summary = "Get household expense and payment statistics",
        security = @SecurityRequirement(name = "apiKey"))
    public ExpenseStatisticsDto getExpenseStatistics(
        Authentication authentication,
        @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(name = "grouping", defaultValue = "DAY") StatisticsGrouping grouping
    ) {
        LOGGER.info("GET /api/v1/history/statistics/expenses");
        return statisticsService.getExpenseStatistics(
            authentication.getName(),
            filter(from, to, grouping, EnumSet.of(StatisticsModule.EXPENSES, StatisticsModule.PAYMENTS))
        );
    }

    @Secured("ROLE_USER")
    @GetMapping("/shopping")
    @Operation(summary = "Get shopping statistics visible to the authenticated household member",
        security = @SecurityRequirement(name = "apiKey"))
    public ShoppingStatisticsDto getShoppingStatistics(
        Authentication authentication,
        @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(name = "grouping", defaultValue = "DAY") StatisticsGrouping grouping
    ) {
        LOGGER.info("GET /api/v1/history/statistics/shopping");
        return statisticsService.getShoppingStatistics(
            authentication.getName(),
            filter(from, to, grouping, EnumSet.of(StatisticsModule.SHOPPING))
        );
    }

    private StatisticsFilterDto filter(
        LocalDate from,
        LocalDate to,
        StatisticsGrouping grouping,
        Set<StatisticsModule> modules
    ) {
        if (from == null || to == null) {
            throw new ValidationException("Statistics date range must include both from and to dates.");
        }
        if (from.isAfter(to)) {
            throw new ValidationException("Statistics from date must not be after the to date.");
        }
        if (from.plusDays(MAX_RANGE_DAYS).isBefore(to)) {
            throw new ValidationException("Statistics date range must not exceed 10 years.");
        }
        Set<StatisticsModule> effectiveModules = modules == null || modules.isEmpty()
            ? EnumSet.allOf(StatisticsModule.class)
            : EnumSet.copyOf(modules);
        return new StatisticsFilterDto(from, to, grouping, effectiveModules);
    }
}