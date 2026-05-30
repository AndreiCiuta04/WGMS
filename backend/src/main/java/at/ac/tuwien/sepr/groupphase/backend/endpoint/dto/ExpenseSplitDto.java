package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;

public record ExpenseSplitDto(
    Long id,
    Long roommateId,
    String roommateName,
    String roommateEmail,
    BigDecimal amountOwed
) {}
