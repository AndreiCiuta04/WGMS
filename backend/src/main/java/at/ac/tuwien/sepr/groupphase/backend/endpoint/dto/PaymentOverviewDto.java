package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;
import java.util.List;

public record PaymentOverviewDto(
    List<BalanceDto> openBalances,
    List<PaymentDto> payments,
    BigDecimal totalIOwe,
    BigDecimal totalOwedToMe
) {}
