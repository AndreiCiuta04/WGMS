package at.ac.tuwien.sepr.groupphase.backend.entity;

/**
 * Visibility of a shopping item.
 *
 * <ul>
 *   <li>{@code PERSONAL} — only the creator sees it (the implicit default in FR-07).</li>
 *   <li>{@code TARGETED} — visible to the creator and one specific other user
 *       in the same household; the recipient is stored in
 *       {@link ShoppingItem#getTargetUser()}.</li>
 *   <li>{@code SHARED} — visible to every active member of the item's household.
 *       Feeds the aggregated household list (FR-09).</li>
 * </ul>
 */
public enum ShoppingItemVisibility {
    PERSONAL,
    TARGETED,
    SHARED
}
