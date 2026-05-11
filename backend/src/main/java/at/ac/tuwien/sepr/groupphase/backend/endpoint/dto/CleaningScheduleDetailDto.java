package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class CleaningScheduleDetailDto {

    private Long id;
    private Long householdId;
    private String title;
    private String description;
    private CleaningFrequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextDueDate;
    private boolean active;
    private String createdByEmail;
    private String assignedToEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public Long getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(Long householdId) {
        this.householdId = householdId;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "CleaningScheduleDetailDto{"
            + "id=" + id
            + ", householdId=" + householdId
            + ", title='" + title + '\''
            + ", frequency=" + frequency
            + ", startDate=" + startDate
            + ", endDate=" + endDate
            + ", nextDueDate=" + nextDueDate
            + ", active=" + active
            + ", createdByEmail='" + createdByEmail + '\''
            + ", assignedToEmail='" + assignedToEmail + '\''
            + ", createdAt=" + createdAt
            + ", updatedAt=" + updatedAt
            + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CleaningScheduleDetailDto that)) {
            return false;
        }
        return active == that.active
            && Objects.equals(id, that.id)
            && Objects.equals(householdId, that.householdId)
            && Objects.equals(title, that.title)
            && Objects.equals(description, that.description)
            && frequency == that.frequency
            && Objects.equals(startDate, that.startDate)
            && Objects.equals(endDate, that.endDate)
            && Objects.equals(nextDueDate, that.nextDueDate)
            && Objects.equals(createdByEmail, that.createdByEmail)
            && Objects.equals(assignedToEmail, that.assignedToEmail)
            && Objects.equals(createdAt, that.createdAt)
            && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, householdId, title, description, frequency, startDate, endDate, nextDueDate, active, createdByEmail, assignedToEmail, createdAt, updatedAt);
    }
}