package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public record FairChoreRoommateStatsDto(
    Long roommateId,
    Long userId,
    String displayName,
    String email,
    long completedTasks,
    long openTasks,
    long overdueTasks,
    double fairnessScore,
    boolean candidate,
    boolean recommended
) {
}
