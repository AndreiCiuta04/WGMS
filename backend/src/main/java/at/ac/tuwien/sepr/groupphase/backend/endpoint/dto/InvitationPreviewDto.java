package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDateTime;

public record InvitationPreviewDto(
    String householdName,
    String invitedEmail,
    LocalDateTime expiresAt,
    boolean valid
) {}
