package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public record FairChoreOverdueReminderDto(
    Long targetRoommateId,
    String targetDisplayName,
    long overdueTasks,
    String message
) {
}
