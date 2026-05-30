package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record EqualSplitRequestDto(
    @NotNull(message = "Total amount must not be null")
    @DecimalMin(value = "0.01", message = "Total amount must be at least 0.01")
    @Digits(integer = 10, fraction = 2, message = "Total amount must have at most two decimal places")
    BigDecimal totalAmount,

    @NotEmpty(message = "At least one participant is required")
    List<Long> participantRoommateIds
) {}
