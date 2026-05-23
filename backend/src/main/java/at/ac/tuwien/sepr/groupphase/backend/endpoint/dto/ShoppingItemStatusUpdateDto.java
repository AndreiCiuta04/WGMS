package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for changing the status of a shopping item.
 *
 * @param status the new status, required
 */
public record ShoppingItemStatusUpdateDto(
    @NotNull
    ShoppingItemStatus status
) {}
