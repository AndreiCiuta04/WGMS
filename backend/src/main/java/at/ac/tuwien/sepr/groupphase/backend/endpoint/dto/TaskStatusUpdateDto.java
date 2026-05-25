package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateDto(
    @NotNull(message = "Status must not be null")
    TaskStatus status
) {
}
