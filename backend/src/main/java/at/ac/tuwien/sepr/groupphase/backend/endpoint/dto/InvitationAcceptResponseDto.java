package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public record InvitationAcceptResponseDto(
    Long householdId,
    String householdName,
    String role
) {}
