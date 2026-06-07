package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.QuantityUnit;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request body for creating a shopping item.
 *
 * @param name             item name, required, up to 100 characters
 * @param quantityCount    optional multiplier ("how many"), at least 1
 * @param quantityAmount   optional measure amount (e.g. 3), must be positive
 * @param quantityUnit     optional measure unit; see {@link QuantityUnit}
 * @param notes            free-text notes, up to 2000 characters
 * @param visibility       required visibility level; see {@link ShoppingItemVisibility}
 * @param targetUserEmail  required only when visibility is TARGETED; validated at the service layer
 */
public record ShoppingItemCreateDto(
    @NotBlank
    @Size(max = 100)
    String name,

    @Min(1)
    Integer quantityCount,

    @Positive
    @Digits(integer = 7, fraction = 3)
    BigDecimal quantityAmount,

    QuantityUnit quantityUnit,

    @Size(max = 2000)
    String notes,

    @NotNull
    ShoppingItemVisibility visibility,

    @Email
    String targetUserEmail
) {}
