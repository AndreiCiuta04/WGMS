package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateDto(
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Must be a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,

    @NotBlank(message = "First name must not be blank")
    @Size(max = 255, message = "First name must not exceed 255 characters")
    String firstName,

    @NotBlank(message = "Last name must not be blank")
    @Size(max = 255, message = "Last name must not exceed 255 characters")
    String lastName
) {}
