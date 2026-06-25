package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseStatisticsDto(
    LocalDate from,
    LocalDate to,
    StatisticsGrouping grouping,
    long expenseCount,
    BigDecimal totalExpenseAmount,
    BigDecimal averageExpenseAmount,
    BigDecimal largestExpenseAmount,
    long paymentCount,
    BigDecimal totalPaymentAmount,
    List<StatisticsSeriesDto> expenseTrend,
    List<StatisticsSeriesDto> paymentTrend,
    List<StatisticsMemberValueDto> expensesPaidByMember,
    List<StatisticsMemberValueDto> paymentsSentByMember,
    List<StatisticsMemberValueDto> paymentsReceivedByMember
) {
}
