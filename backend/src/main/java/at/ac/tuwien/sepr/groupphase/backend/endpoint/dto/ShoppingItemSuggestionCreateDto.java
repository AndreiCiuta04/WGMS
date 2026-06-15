package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for suggesting a shopping item to the household.
 *
 * @param name     suggested item name, required, up to 100 characters
 * @param quantity free-text quantity, up to 50 characters
 * @param notes    free-text notes, up to 2000 characters
 */
public record ShoppingItemSuggestionCreateDto(
    @NotBlank
    @Size(max = 100)
    String name,

    @Size(max = 50)
    String quantity,

    @Size(max = 2000)
    String notes
) {}