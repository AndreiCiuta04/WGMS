package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.FairChoreRecommendationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;

import java.time.LocalDate;

public interface FairChoreBalancingService {

    int DEFAULT_HISTORY_DAYS = 30;
    int MAX_HISTORY_DAYS = 365;
    int MAX_OPEN_TASKS = 3;
    String ALGORITHM = "HISTORY_BUCKET_SCORE_SEEDED_TIE";

    FairChoreRecommendationDto recommendAssignee(
        String currentUserEmail,
        Integer historyDays,
        String taskTitle,
        LocalDate dueDate,
        Long cleaningScheduleId
    );

    FairChoreRecommendationDto recommendAssigneeForSchedule(
        Household household,
        CleaningSchedule cleaningSchedule,
        LocalDate nextDueDate
    );

    ApplicationUser findRecommendedAssigneeForSchedule(
        Household household,
        CleaningSchedule cleaningSchedule,
        LocalDate nextDueDate
    );
}
