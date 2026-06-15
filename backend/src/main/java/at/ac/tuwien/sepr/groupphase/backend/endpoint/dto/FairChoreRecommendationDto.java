package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

public record FairChoreRecommendationDto(
    Long recommendedRoommateId,
    Long recommendedUserId,
    String recommendedDisplayName,
    String recommendedEmail,
    int historyDays,
    String algorithm,
    String reason,
    List<FairChoreRoommateStatsDto> stats
) {
}
