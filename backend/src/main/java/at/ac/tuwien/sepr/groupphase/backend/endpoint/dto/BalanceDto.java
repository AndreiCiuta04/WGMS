package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.math.BigDecimal;

public record BalanceDto(
    Long fromRoommateId,
    String fromRoommateName,
    String fromRoommateEmail,
    Long toRoommateId,
    String toRoommateName,
    String toRoommateEmail,
    BigDecimal amount
) {}
