package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;

import java.time.LocalDateTime;

/**
 * Response body describing a shopping item, including its visibility, creator,
 * and optional target user. The target user fields are populated only when the
 * item's visibility is {@link ShoppingItemVisibility#TARGETED}.
 *
 * @param id                   item id
 * @param name                 item name
 * @param quantity             free-text quantity
 * @param notes                free-text notes
 * @param status               current status
 * @param visibility           current visibility
 * @param householdId          id of the household the item belongs to
 * @param createdById          id of the creator
 * @param createdByEmail       email of the creator
 * @param createdByFirstName   first name of the creator
 * @param createdByLastName    last name of the creator
 * @param targetUserId         id of the target user, or {@code null}
 * @param targetUserEmail      email of the target user, or {@code null}
 * @param targetUserFirstName  first name of the target user, or {@code null}
 * @param targetUserLastName   last name of the target user, or {@code null}
 * @param createdAt            creation timestamp
 * @param updatedAt            last update timestamp
 */
public record ShoppingItemDto(
    Long id,
    String name,
    String quantity,
    String notes,
    ShoppingItemStatus status,
    ShoppingItemVisibility visibility,
    Long householdId,
    Long createdById,
    String createdByEmail,
    String createdByFirstName,
    String createdByLastName,
    Long targetUserId,
    String targetUserEmail,
    String targetUserFirstName,
    String targetUserLastName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
