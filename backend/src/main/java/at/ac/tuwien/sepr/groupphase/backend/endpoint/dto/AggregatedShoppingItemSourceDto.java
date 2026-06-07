package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.QuantityUnit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One original shopping item contributing to an aggregated household shopping-list entry.
 *
 * @param id                 source shopping item id
 * @param name               original item name
 * @param quantityCount      original multiplier, or {@code null}
 * @param quantityAmount     original measure amount, or {@code null}
 * @param quantityUnit       original measure unit, or {@code null}
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
    Integer quantityCount,
    BigDecimal quantityAmount,
    QuantityUnit quantityUnit,
    String notes,
    Long createdById,
    String createdByEmail,
    String createdByFirstName,
    String createdByLastName,
    LocalDateTime createdAt
) {}