package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;
import java.util.Objects;

public class HistoryDto {

    private List<TaskHistoryDto> tasks;
    private List<PaymentHistoryDto> payments;

    public List<TaskHistoryDto> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskHistoryDto> tasks) {
        this.tasks = tasks;
    }

    public List<PaymentHistoryDto> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentHistoryDto> payments) {
        this.payments = payments;
    }

    @Override
    public String toString() {
        return "HistoryDto{"
            + "tasks=" + tasks
            + ", payments=" + payments
            + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HistoryDto that)) {
            return false;
        }
        return Objects.equals(tasks, that.tasks)
            && Objects.equals(payments, that.payments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tasks, payments);
    }
}