package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentDto(
    Long id,
    Long fromRoommateId,
    String fromRoommateName,
    String fromRoommateEmail,
    Long toRoommateId,
    String toRoommateName,
    String toRoommateEmail,
    BigDecimal amount,
    LocalDate date,
    LocalDateTime createdAt
) {}
