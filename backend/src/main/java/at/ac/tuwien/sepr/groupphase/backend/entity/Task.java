package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status;

    @Column(nullable = false, name = "created_by_email", length = 255)
    private String createdByEmail;

    @Column(nullable = false, name = "assigned_to_email", length = 255)
    private String assignedToEmail;

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

    public boolean isCreatedBy(String email) {
        return createdByEmail != null && createdByEmail.equalsIgnoreCase(email);
    }

    public boolean isAssignedTo(String email) {
        return assignedToEmail != null && assignedToEmail.equalsIgnoreCase(email);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Task task)) {
            return false;
        }
        return Objects.equals(id, task.id)
            && Objects.equals(title, task.title)
            && Objects.equals(description, task.description)
            && Objects.equals(dueDate, task.dueDate)
            && status == task.status
            && Objects.equals(createdByEmail, task.createdByEmail)
            && Objects.equals(assignedToEmail, task.assignedToEmail)
            && Objects.equals(createdAt, task.createdAt)
            && Objects.equals(updatedAt, task.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, dueDate, status, createdByEmail, assignedToEmail, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Task{"
            + "id=" + id
            + ", title='" + title + '\''
            + ", description='" + description + '\''
            + ", dueDate=" + dueDate
            + ", status=" + status
            + ", createdByEmail='" + createdByEmail + '\''
            + ", assignedToEmail='" + assignedToEmail + '\''
            + ", createdAt=" + createdAt
            + ", updatedAt=" + updatedAt
            + '}';
    }

    public static final class TaskBuilder {
        private Long id;
        private String title;
        private String description;
        private LocalDate dueDate;
        private TaskStatus status;
        private String createdByEmail;
        private String assignedToEmail;
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

        public TaskBuilder withCreatedByEmail(String createdByEmail) {
            this.createdByEmail = createdByEmail;
            return this;
        }

        public TaskBuilder withAssignedToEmail(String assignedToEmail) {
            this.assignedToEmail = assignedToEmail;
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
            task.setTitle(title);
            task.setDescription(description);
            task.setDueDate(dueDate);
            task.setStatus(status);
            task.setCreatedByEmail(createdByEmail);
            task.setAssignedToEmail(assignedToEmail);
            task.setCreatedAt(createdAt);
            task.setUpdatedAt(updatedAt);
            return task;
        }
    }
}