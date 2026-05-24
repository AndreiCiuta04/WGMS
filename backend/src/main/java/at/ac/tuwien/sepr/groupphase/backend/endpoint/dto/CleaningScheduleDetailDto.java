package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CleaningScheduleDetailDto(
    Long id,
    Long householdId,
    String title,
    String description,
    CleaningFrequency frequency,
    LocalDate startDate,
    LocalDate endDate,
    LocalDate nextDueDate,
    boolean active,
    String createdByEmail,
    String assignedToEmail,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
