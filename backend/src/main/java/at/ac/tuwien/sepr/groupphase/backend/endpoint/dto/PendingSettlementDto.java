package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.SettlementDecisionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PendingSettlementDto(
    String id,
    String expenseDescription,
    Long fromRoommateId,
    String fromRoommateName,
    String fromRoommateEmail,
    Long toRoommateId,
    String toRoommateName,
    String toRoommateEmail,
    BigDecimal amount,
    SettlementDecisionStatus status,
    boolean canDecide,
    LocalDateTime createdAt,
    LocalDateTime decidedAt,
    String decidedByName
) {}
