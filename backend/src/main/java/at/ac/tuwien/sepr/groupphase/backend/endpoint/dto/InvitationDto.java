package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDateTime;

public record InvitationDto(
    Long id,
    String inviteUrl,
    LocalDateTime expiresAt,
    String invitedEmail,
    String status
) {}
