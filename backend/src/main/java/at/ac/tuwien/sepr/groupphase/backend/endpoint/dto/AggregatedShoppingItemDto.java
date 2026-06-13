package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.QuantityUnit;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated household shopping-list entry for FR-09.
 *
 * <p>Items are grouped by equal normalized name and equal structured quantity
 * (count + amount + unit), so requests like "Milk 2 L" merge regardless of how
 * each roommate typed them.</p>
 *
 * @param name           display name of the aggregated item
 * @param quantityCount  shared multiplier of the grouped source items, or {@code null}
 * @param quantityAmount shared measure amount of the grouped source items, or {@code null}
 * @param quantityUnit   shared measure unit of the grouped source items, or {@code null}
 * @param itemCount      number of source items in this group
 * @param sourceItems    original shopping items contributing to this group
 */
public record AggregatedShoppingItemDto(
    String name,
    Integer quantityCount,
    BigDecimal quantityAmount,
    QuantityUnit quantityUnit,
    int itemCount,
    List<AggregatedShoppingItemSourceDto> sourceItems
) {}