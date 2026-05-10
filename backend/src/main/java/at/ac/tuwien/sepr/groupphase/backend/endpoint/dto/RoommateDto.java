package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDateTime;

public record RoommateDto(
    Long id,
    Long userId,
    String firstName,
    String lastName,
    String email,
    String role,
    LocalDateTime joinedAt
) {}
