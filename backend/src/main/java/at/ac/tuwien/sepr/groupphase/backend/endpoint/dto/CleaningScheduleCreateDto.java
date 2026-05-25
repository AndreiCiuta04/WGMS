package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CleaningScheduleCreateDto(
    @NotBlank(message = "Title must not be blank")
    @Size(max = 100, message = "Title must not be longer than 100 characters")
    String title,

    @Size(max = 2000, message = "Description must not be longer than 2000 characters")
    String description,

    @NotNull(message = "Frequency must not be null")
    CleaningFrequency frequency,

    @NotNull(message = "Start date must not be null")
    @FutureOrPresent(message = "Start date must be today or in the future")
    LocalDate startDate,

    @FutureOrPresent(message = "End date must be today or in the future")
    LocalDate endDate,

    @NotNull(message = "Assigned user email must not be null")
    @Email(message = "Assigned user email must be a valid email address")
    String assignedToEmail
) {

    @AssertTrue(message = "End date must not be before start date")
    public boolean isEndDateNotBeforeStartDate() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }
}
