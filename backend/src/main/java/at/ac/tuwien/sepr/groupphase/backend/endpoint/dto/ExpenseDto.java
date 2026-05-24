package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ExpenseDto(
    Long id,
    Long householdId,
    String description,
    BigDecimal totalAmount,
    LocalDate date,
    LocalDateTime createdAt,
    Long paidByRoommateId,
    String paidByName,
    String paidByEmail,
    Long createdByRoommateId,
    String createdByName,
    String createdByEmail,
    List<ExpenseSplitDto> splits
) {}
