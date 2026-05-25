package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentCreateDto(
    @NotNull(message = "Recipient roommate id must not be null")
    Long toRoommateId,

    @NotNull(message = "Amount must not be null")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most two decimal places")
    BigDecimal amount,

    @NotNull(message = "Date must not be null")
    LocalDate date
) {}
