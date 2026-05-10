package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Email;

public record InvitationCreateDto(
    @Email
    String invitedEmail,

    Integer expiresInHours
) {}
