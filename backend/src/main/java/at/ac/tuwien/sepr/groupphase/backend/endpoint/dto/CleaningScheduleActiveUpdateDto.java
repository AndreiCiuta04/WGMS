package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;

public record CleaningScheduleActiveUpdateDto(
    @NotNull(message = "Active flag must not be null")
    Boolean active
) {
}
