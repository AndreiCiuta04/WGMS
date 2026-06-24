package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDate;
import java.util.Set;

public record StatisticsFilterDto(
    LocalDate from,
    LocalDate to,
    StatisticsGrouping grouping,
    Set<StatisticsModule> modules
) {
}
