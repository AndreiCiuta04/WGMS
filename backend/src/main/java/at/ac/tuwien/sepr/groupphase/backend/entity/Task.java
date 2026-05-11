package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private ApplicationUser createdBy;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id", nullable = false)
    private ApplicationUser assignedTo;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (status == null) {
            status = TaskStatus.OPEN;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getHouseholdId() {
        return household == null ? null : household.getId();
    }

    public String getCreatedByEmail() {
        return createdBy == null ? null : createdBy.getEmail();
    }

    public String getAssignedToEmail() {
        return assignedTo == null ? null : assignedTo.getEmail();
    }

    public boolean isCreatedBy(ApplicationUser user) {
        return user != null
            && createdBy != null
            && Objects.equals(createdBy.getId(), user.getId());
    }

    public boolean isAssignedTo(ApplicationUser user) {
        return user != null
            && assignedTo != null
            && Objects.equals(assignedTo.getId(), user.getId());
    }

    public boolean belongsToHousehold(Long householdId) {
        return household != null && Objects.equals(household.getId(), householdId);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Household getHousehold() {
        return household;
    }

    public void setHousehold(Household household) {
        this.household = household;
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

    public ApplicationUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(ApplicationUser createdBy) {
        this.createdBy = createdBy;
    }

    public ApplicationUser getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(ApplicationUser assignedTo) {
        this.assignedTo = assignedTo;
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
        return "Task{"
            + "id=" + id
            + ", householdId=" + getHouseholdId()
            + ", title='" + title + '\''
            + ", dueDate=" + dueDate
            + ", status=" + status
            + ", createdByUserId=" + (createdBy == null ? null : createdBy.getId())
            + ", assignedToUserId=" + (assignedTo == null ? null : assignedTo.getId())
            + ", createdAt=" + createdAt
            + ", updatedAt=" + updatedAt
            + '}';
    }

    public static final class TaskBuilder {
        private Long id;
        private Household household;
        private String title;
        private String description;
        private LocalDate dueDate;
        private TaskStatus status;
        private ApplicationUser createdBy;
        private ApplicationUser assignedTo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private TaskBuilder() {
        }

        public static TaskBuilder aTask() {
            return new TaskBuilder();
        }

        public TaskBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public TaskBuilder withHousehold(Household household) {
            this.household = household;
            return this;
        }

        public TaskBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public TaskBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public TaskBuilder withDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public TaskBuilder withStatus(TaskStatus status) {
            this.status = status;
            return this;
        }

        public TaskBuilder withCreatedBy(ApplicationUser createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public TaskBuilder withAssignedTo(ApplicationUser assignedTo) {
            this.assignedTo = assignedTo;
            return this;
        }

        public TaskBuilder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TaskBuilder withUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Task build() {
            Task task = new Task();
            task.setId(id);
            task.setHousehold(household);
            task.setTitle(title);
            task.setDescription(description);
            task.setDueDate(dueDate);
            task.setStatus(status);
            task.setCreatedBy(createdBy);
            task.setAssignedTo(assignedTo);
            task.setCreatedAt(createdAt);
            task.setUpdatedAt(updatedAt);
            return task;
        }
    }
}