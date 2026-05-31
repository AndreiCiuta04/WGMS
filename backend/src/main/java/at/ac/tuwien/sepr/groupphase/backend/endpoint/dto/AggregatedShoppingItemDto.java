package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

/**
 * Aggregated household shopping-list entry for FR-09.
 *
 * <p>Because the existing shopping item quantity is stored as free text,
 * aggregation groups equal normalized item names and equal normalized quantities.
 * The service does not perform unsafe numeric parsing of values such as
 * "2L", "1 pack", or "some".</p>
 *
 * @param name        display name of the aggregated item
 * @param quantity    free-text quantity shared by all grouped source items
 * @param itemCount   number of source items in this group
 * @param sourceItems original shopping items contributing to this group
 */
public record AggregatedShoppingItemDto(
    String name,
    String quantity,
    int itemCount,
    List<AggregatedShoppingItemSourceDto> sourceItems
) {}