package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public record UserProfileDto(
    Long id,
    String email,
    String firstName,
    String lastName,
    String role,
    Long householdId,
    String householdName,
    Long roommateId
) {}
