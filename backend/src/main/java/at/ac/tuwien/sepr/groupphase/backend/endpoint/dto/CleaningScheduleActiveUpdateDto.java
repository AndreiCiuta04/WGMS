package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class CleaningScheduleActiveUpdateDto {

    @NotNull(message = "Active flag must not be null")
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "CleaningScheduleActiveUpdateDto{"
            + "active=" + active
            + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CleaningScheduleActiveUpdateDto that)) {
            return false;
        }
        return Objects.equals(active, that.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(active);
    }
}