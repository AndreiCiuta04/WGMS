package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for updating an existing shopping item.
 *
 * @param name             item name, required, up to 100 characters
 * @param quantity         free-text quantity, up to 50 characters
 * @param notes            free-text notes, up to 2000 characters
 * @param visibility       required visibility level; see {@link ShoppingItemVisibility}
 * @param targetUserEmail  required only when visibility is TARGETED; validated at the service layer
 */
public record ShoppingItemUpdateDto(
    @NotBlank
    @Size(max = 100)
    String name,

    @Size(max = 50)
    String quantity,

    @Size(max = 2000)
    String notes,

    @NotNull
    ShoppingItemVisibility visibility,

    @Email
    String targetUserEmail
) {}
