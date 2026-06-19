package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;

public record StatisticsCategoryValueDto(
    String key,
    String label,
    BigDecimal value
) {
}
