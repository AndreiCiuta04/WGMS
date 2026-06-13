package at.ac.tuwien.sepr.groupphase.backend.entity;

/**
 * Measurement unit for a shopping item's structured quantity
 * ({@link ShoppingItem#getQuantityAmount()}). Stored as a string via
 * {@code @Enumerated(EnumType.STRING)}.
 */
public enum QuantityUnit {
    MILLILITER,
    LITER,
    GRAM,
    KILOGRAM,
    PIECE,
    PACK
}
