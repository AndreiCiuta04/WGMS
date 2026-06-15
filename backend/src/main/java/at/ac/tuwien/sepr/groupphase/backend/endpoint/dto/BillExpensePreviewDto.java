package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BillExpensePreviewDto(
    String description,
    BigDecimal totalAmount,
    LocalDate date,
    Long paidByRoommateId,
    List<ExpenseSplitCreateDto> splits
) {}
