package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class TaskDetailDto {

    private Long id;
    private Long householdId;
    private Long cleaningScheduleId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status;
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

    public Long getCleaningScheduleId() {
        return cleaningScheduleId;
    }

    public void setCleaningScheduleId(Long cleaningScheduleId) {
        this.cleaningScheduleId = cleaningScheduleId;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
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
        return "TaskDetailDto{"
            + "id=" + id
            + ", householdId=" + householdId
            + ", cleaningScheduleId=" + cleaningScheduleId
            + ", title='" + title + '\''
            + ", dueDate=" + dueDate
            + ", status=" + status
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
        if (!(o instanceof TaskDetailDto that)) {
            return false;
        }
        return Objects.equals(id, that.id)
            && Objects.equals(householdId, that.householdId)
            && Objects.equals(cleaningScheduleId, that.cleaningScheduleId)
            && Objects.equals(title, that.title)
            && Objects.equals(description, that.description)
            && Objects.equals(dueDate, that.dueDate)
            && status == that.status
            && Objects.equals(createdByEmail, that.createdByEmail)
            && Objects.equals(assignedToEmail, that.assignedToEmail)
            && Objects.equals(createdAt, that.createdAt)
            && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, householdId, cleaningScheduleId, title, description, dueDate, status, createdByEmail, assignedToEmail, createdAt, updatedAt);
    }
}
