package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ExpenseSplitCreateDto(
    @NotNull(message = "Roommate id must not be null")
    Long roommateId,

    @NotNull(message = "Amount owed must not be null")
    @DecimalMin(value = "0.01", message = "Amount owed must be at least 0.01")
    @Digits(integer = 10, fraction = 2, message = "Amount owed must have at most two decimal places")
    BigDecimal amountOwed
) {}
