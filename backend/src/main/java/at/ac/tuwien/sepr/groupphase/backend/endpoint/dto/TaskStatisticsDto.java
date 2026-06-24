package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TaskStatisticsDto(
    LocalDate from,
    LocalDate to,
    StatisticsGrouping grouping,
    long totalTaskCount,
    long completedTaskCount,
    long cancelledTaskCount,
    long overdueTaskCount,
    BigDecimal completionRate,
    long totalCleaningScheduleCount,
    long activeCleaningScheduleCount,
    long scheduleGeneratedTaskCount,
    BigDecimal scheduleGeneratedTaskCompletionRate,
    List<StatisticsCategoryValueDto> taskStatusDistribution,
    List<StatisticsCategoryValueDto> cleaningFrequencyDistribution,
    List<StatisticsSeriesDto> completionTrend,
    List<StatisticsMemberValueDto> completedTasksByMember,
    List<StatisticsCategoryValueDto> completedTasksByWeekday
) {
}
