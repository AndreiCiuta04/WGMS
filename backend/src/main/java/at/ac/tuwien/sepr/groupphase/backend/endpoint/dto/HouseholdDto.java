package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDateTime;

public record HouseholdDto(
    Long id,
    String name,
    String address,
    LocalDateTime createdAt,
    String currentUserRole
) {}
