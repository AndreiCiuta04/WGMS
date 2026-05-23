package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShoppingItemUpdateDto(
    @NotBlank
    @Size(max = 100)
    String name,

    @Size(max = 50)
    String quantity,

    @Size(max = 2000)
    String notes
) {}
