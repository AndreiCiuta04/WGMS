package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;

public record StatisticsMemberValueDto(
    Long roommateId,
    String displayName,
    BigDecimal value
) {
}
