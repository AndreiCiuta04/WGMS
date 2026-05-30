package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseUpdateDto(
    @NotBlank(message = "Description must not be blank")
    @Size(max = 500, message = "Description must not be longer than 500 characters")
    String description,

    @NotNull(message = "Total amount must not be null")
    @DecimalMin(value = "0.01", message = "Total amount must be at least 0.01")
    @Digits(integer = 10, fraction = 2, message = "Total amount must have at most two decimal places")
    BigDecimal totalAmount,

    @NotNull(message = "Date must not be null")
    LocalDate date,

    @NotNull(message = "Paid by roommate id must not be null")
    Long paidByRoommateId,

    @Valid
    @NotEmpty(message = "At least one split is required")
    List<ExpenseSplitCreateDto> splits
) {}
