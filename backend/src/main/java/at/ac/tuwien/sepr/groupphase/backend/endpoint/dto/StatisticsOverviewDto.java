package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StatisticsOverviewDto(
    LocalDate from,
    LocalDate to,
    StatisticsGrouping grouping,
    long completedTaskCount,
    BigDecimal taskCompletionRate,
    BigDecimal totalExpenseAmount,
    BigDecimal recordedPaymentAmount,
    long purchasedShoppingItemCount,
    long activeCleaningScheduleCount,
    long totalActivityCount,
    List<StatisticsCategoryValueDto> activityByModule,
    List<StatisticsSeriesDto> activityTrend
) {
}
