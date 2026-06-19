package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StatisticsDataPointDto(
    LocalDate periodStart,
    BigDecimal value
) {
}
