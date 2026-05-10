package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Objects;

public class TaskCreateDto {

    @NotBlank(message = "Title must not be blank")
    @Size(max = 100, message = "Title must not be longer than 100 characters")
    private String title;

    @Size(max = 2000, message = "Description must not be longer than 2000 characters")
    private String description;

    @NotNull(message = "Due date must not be null")
    @FutureOrPresent(message = "Due date must be today or in the future")
    private LocalDate dueDate;

    @NotNull(message = "Assigned user email must not be null")
    @Email(message = "Assigned user email must be a valid email address")
    private String assignedToEmail;

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

    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
    }

    @Override
    public String toString() {
        return "TaskCreateDto{"
            + "title='" + title + '\''
            + ", dueDate=" + dueDate
            + ", assignedToEmail='" + assignedToEmail + '\''
            + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskCreateDto that)) {
            return false;
        }
        return Objects.equals(title, that.title)
            && Objects.equals(description, that.description)
            && Objects.equals(dueDate, that.dueDate)
            && Objects.equals(assignedToEmail, that.assignedToEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, dueDate, assignedToEmail);
    }
}