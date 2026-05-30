package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class TaskStatusUpdateDto {

    @NotNull(message = "Status must not be null")
    private TaskStatus status;

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TaskStatusUpdateDto{"
            + "status=" + status
            + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaskStatusUpdateDto that)) {
            return false;
        }
        return status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }
}