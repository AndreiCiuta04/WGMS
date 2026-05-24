package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskDetailDto(
    Long id,
    Long householdId,
    Long cleaningScheduleId,
    String title,
    String description,
    LocalDate dueDate,
    TaskStatus status,
    String createdByEmail,
    String assignedToEmail,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
