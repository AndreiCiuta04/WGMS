package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HouseholdCreateDto(
    @NotBlank
    @Size(max = 120)
    String name,

    @Size(max = 255)
    String address
) {}
