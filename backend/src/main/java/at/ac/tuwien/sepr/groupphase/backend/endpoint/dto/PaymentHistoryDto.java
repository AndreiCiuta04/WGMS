package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentHistoryDto(
    Long id,
    String title,
    BigDecimal amount,
    String status,
    String payerEmail,
    String receiverEmail,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
