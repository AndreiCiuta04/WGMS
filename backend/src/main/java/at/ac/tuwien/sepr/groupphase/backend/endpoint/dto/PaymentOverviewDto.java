package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record PaymentOverviewDto(
    List<BalanceDto> openBalances,
    List<PaymentDto> payments,
    @JsonProperty("totalIOwe")
    BigDecimal totalIowe,
    BigDecimal totalOwedToMe
) {}
