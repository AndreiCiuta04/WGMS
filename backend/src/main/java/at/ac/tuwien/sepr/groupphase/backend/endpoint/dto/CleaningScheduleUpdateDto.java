package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Objects;

public class CleaningScheduleUpdateDto {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 100, message = "Title must not be longer than 100 characters")
    private String title;

    @Size(max = 2000, message = "Description must not be longer than 2000 characters")
    private String description;

    @NotNull(message = "Frequency must not be null")
    private CleaningFrequency frequency;

    @NotNull(message = "Start date must not be null")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @FutureOrPresent(message = "End date must be today or in the future")
    private LocalDate endDate;

    @NotNull(message = "Assigned user email must not be null")
    @Email(message = "Assigned user email must be a valid email address")
    private String assignedToEmail;

    @AssertTrue(message = "End date must not be before start date")
    public boolean isEndDateNotBeforeStartDate() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CleaningFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(CleaningFrequency frequency) {
        this.frequency = frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
    }

    @Override
    public String toString() {
        return "CleaningScheduleUpdateDto{"
            + "title='" + title + '\''
            + ", frequency=" + frequency
            + ", startDate=" + startDate
            + ", endDate=" + endDate
            + ", assignedToEmail='" + assignedToEmail + '\''
            + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CleaningScheduleUpdateDto that)) {
            return false;
        }
        return Objects.equals(title, that.title)
            && Objects.equals(description, that.description)
            && frequency == that.frequency
            && Objects.equals(startDate, that.startDate)
            && Objects.equals(endDate, that.endDate)
            && Objects.equals(assignedToEmail, that.assignedToEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, frequency, startDate, endDate, assignedToEmail);
    }
}