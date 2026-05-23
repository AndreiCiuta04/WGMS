package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;

import java.time.LocalDateTime;

public record ShoppingItemDto(
    Long id,
    String name,
    String quantity,
    String notes,
    ShoppingItemStatus status,
    Long householdId,
    Long createdById,
    String createdByEmail,
    String createdByFirstName,
    String createdByLastName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
