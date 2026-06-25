package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ShoppingStatisticsDto(
    LocalDate from,
    LocalDate to,
    StatisticsGrouping grouping,
    long addedItemCount,
    long purchasedItemCount,
    BigDecimal purchaseCompletionRate,
    List<StatisticsCategoryValueDto> statusDistribution,
    List<StatisticsCategoryValueDto> visibilityDistribution,
    List<StatisticsSeriesDto> itemActivityTrend,
    List<StatisticsCategoryValueDto> mostFrequentlyPurchasedItems,
    List<StatisticsMemberValueDto> itemsAddedByMember
) {
}
