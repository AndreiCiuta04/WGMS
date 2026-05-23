package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import jakarta.validation.constraints.NotNull;

public record ShoppingItemStatusUpdateDto(
    @NotNull
    ShoppingItemStatus status
) {}
