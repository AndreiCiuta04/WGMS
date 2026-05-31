package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDateTime;

/**
 * One original shopping item contributing to an aggregated household shopping-list entry.
 *
 * @param id                 source shopping item id
 * @param name               original item name
 * @param quantity           original free-text quantity
 * @param notes              original notes
 * @param createdById        id of the user who created the source item
 * @param createdByEmail     email of the creator
 * @param createdByFirstName first name of the creator
 * @param createdByLastName  last name of the creator
 * @param createdAt          creation timestamp
 */
public record AggregatedShoppingItemSourceDto(
    Long id,
    String name,
    String quantity,
    String notes,
    Long createdById,
    String createdByEmail,
    String createdByFirstName,
    String createdByLastName,
    LocalDateTime createdAt
) {}