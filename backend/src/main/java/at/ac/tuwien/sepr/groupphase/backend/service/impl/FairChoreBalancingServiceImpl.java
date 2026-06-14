package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.FairChoreRecommendationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.FairChoreRoommateStatsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.FairChoreBalancingService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
public class FairChoreBalancingServiceImpl implements FairChoreBalancingService {

    private static final double COMPLETED_TASK_WEIGHT = 1.0d;
    private static final double OPEN_TASK_WEIGHT = 0.5d;
    private static final double OVERDUE_TASK_WEIGHT = 1.0d;

    private final UserService userService;
    private final RoommateRepository roommateRepository;
    private final TaskRepository taskRepository;

    public FairChoreBalancingServiceImpl(
        UserService userService,
        RoommateRepository roommateRepository,
        TaskRepository taskRepository
    ) {
        this.userService = userService;
        this.roommateRepository = roommateRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public FairChoreRecommendationDto recommendAssignee(
        String currentUserEmail,
        Integer historyDays,
        String taskTitle,
        LocalDate dueDate,
        Long cleaningScheduleId
    ) {
        ApplicationUser currentUser = userService.findApplicationUserByEmail(currentUserEmail);
        Roommate currentRoommate = roommateRepository.findByUserIdAndActiveTrue(currentUser.getId())
            .orElseThrow(() -> new ForbiddenException("User does not belong to an active household."));

        LocalDate seedDate = dueDate == null ? LocalDate.now() : dueDate;
        String seed = "manual|"
            + currentRoommate.getHousehold().getId() + "|"
            + normalizeSeedPart(taskTitle) + "|"
            + seedDate + "|"
            + (cleaningScheduleId == null ? "" : cleaningScheduleId);

        return calculateRecommendation(currentRoommate.getHousehold(), normalizeHistoryDays(historyDays), seed);
    }

    @Override
    @Transactional(readOnly = true)
    public FairChoreRecommendationDto recommendAssigneeForSchedule(
        Household household,
        CleaningSchedule cleaningSchedule,
        LocalDate nextDueDate
    ) {
        String seed = "schedule|"
            + household.getId() + "|"
            + cleaningSchedule.getId() + "|"
            + nextDueDate;

        return calculateRecommendation(household, DEFAULT_HISTORY_DAYS, seed);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationUser findRecommendedAssigneeForSchedule(
        Household household,
        CleaningSchedule cleaningSchedule,
        LocalDate nextDueDate
    ) {
        FairChoreRecommendationDto recommendation = recommendAssigneeForSchedule(household, cleaningSchedule, nextDueDate);
        if (recommendation.recommendedUserId() == null) {
            return null;
        }

        return roommateRepository.findAllByHousehold_IdAndActiveTrue(household.getId())
            .stream()
            .map(Roommate::getUser)
            .filter(user -> Objects.equals(user.getId(), recommendation.recommendedUserId()))
            .findFirst()
            .orElse(null);
    }

    private FairChoreRecommendationDto calculateRecommendation(Household household, int historyDays, String seed) {
        List<Roommate> activeRoommates = roommateRepository.findAllByHousehold_IdAndActiveTrue(household.getId())
            .stream()
            .filter(roommate -> roommate.getUser() != null && !roommate.getUser().isAnonymized())
            .toList();

        if (activeRoommates.isEmpty()) {
            throw new ConflictException("Household has no active roommates for fair balancing.");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime historyStart = today.minusDays(historyDays).atStartOfDay();
        List<Task> tasks = taskRepository.findAllByHousehold_Id(household.getId());

        List<StatsAccumulator> accumulators = activeRoommates.stream()
            .map(StatsAccumulator::new)
            .toList();

        for (Task task : tasks) {
            if (task.getAssignedTo() == null || task.getAssignedTo().isAnonymized()) {
                continue;
            }

            StatsAccumulator stats = findStatsForUser(accumulators, task.getAssignedTo().getId());
            if (stats == null) {
                continue;
            }

            if (task.getStatus() == TaskStatus.DONE) {
                // Task has no completedAt field. updatedAt is the persisted timestamp changed by completion and undo,
                // so current DONE tasks updated inside the window are the most reliable available completion signal.
                if (task.getUpdatedAt() != null && !task.getUpdatedAt().isBefore(historyStart)) {
                    stats.completedTasks++;
                }
                continue;
            }

            if (task.getStatus() == TaskStatus.CANCELLED) {
                continue;
            }

            if (task.getDueDate() != null && task.getDueDate().isBefore(today)) {
                stats.overdueTasks++;
            } else {
                stats.openTasks++;
            }
        }

        double minimumScore = accumulators.stream()
            .mapToDouble(StatsAccumulator::fairnessScore)
            .min()
            .orElse(0.0d);

        List<StatsAccumulator> candidates = accumulators.stream()
            .filter(stats -> stats.fairnessScore() <= minimumScore + FAIRNESS_THRESHOLD)
            .sorted(Comparator.comparing(stats -> stats.roommate.getId()))
            .toList();

        StatsAccumulator recommended = candidates.get(new Random(stableSeed(seed)).nextInt(candidates.size()));

        List<FairChoreRoommateStatsDto> statsDtos = new ArrayList<>();
        for (StatsAccumulator stats : accumulators.stream().sorted(Comparator.comparing(s -> s.roommate.getId())).toList()) {
            boolean candidate = candidates.stream().anyMatch(poolEntry -> Objects.equals(poolEntry.roommate.getId(), stats.roommate.getId()));
            boolean recommendedFlag = Objects.equals(recommended.roommate.getId(), stats.roommate.getId());
            statsDtos.add(toDto(stats, candidate, recommendedFlag));
        }

        ApplicationUser recommendedUser = recommended.roommate.getUser();
        return new FairChoreRecommendationDto(
            recommended.roommate.getId(),
            recommendedUser.getId(),
            displayName(recommendedUser),
            recommendedUser.getEmail(),
            historyDays,
            ALGORITHM,
            "Lower scores mean less recent chore burden. The suggestion is selected from roommates within "
                + FAIRNESS_THRESHOLD + " point of the lowest score using a stable seed.",
            statsDtos
        );
    }

    private int normalizeHistoryDays(Integer requestedHistoryDays) {
        if (requestedHistoryDays == null) {
            return DEFAULT_HISTORY_DAYS;
        }

        if (requestedHistoryDays < 1 || requestedHistoryDays > MAX_HISTORY_DAYS) {
            throw new ValidationException("historyDays must be between 1 and " + MAX_HISTORY_DAYS + ".");
        }

        return requestedHistoryDays;
    }

    private StatsAccumulator findStatsForUser(List<StatsAccumulator> stats, Long userId) {
        return stats.stream()
            .filter(entry -> Objects.equals(entry.roommate.getUser().getId(), userId))
            .findFirst()
            .orElse(null);
    }

    private FairChoreRoommateStatsDto toDto(StatsAccumulator stats, boolean candidate, boolean recommended) {
        ApplicationUser user = stats.roommate.getUser();
        return new FairChoreRoommateStatsDto(
            stats.roommate.getId(),
            user.getId(),
            displayName(user),
            user.getEmail(),
            stats.completedTasks,
            stats.openTasks,
            stats.overdueTasks,
            stats.fairnessScore(),
            candidate,
            recommended
        );
    }

    private String displayName(ApplicationUser user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? user.getEmail() : fullName;
    }

    private String normalizeSeedPart(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private long stableSeed(String value) {
        long hash = 1125899906842597L;
        for (byte seedByte : value.getBytes(StandardCharsets.UTF_8)) {
            hash = 31 * hash + seedByte;
        }
        return hash;
    }

    private static final class StatsAccumulator {
        private final Roommate roommate;
        private long completedTasks;
        private long openTasks;
        private long overdueTasks;

        private StatsAccumulator(Roommate roommate) {
            this.roommate = roommate;
        }

        private double fairnessScore() {
            return completedTasks * COMPLETED_TASK_WEIGHT
                + openTasks * OPEN_TASK_WEIGHT
                + overdueTasks * OVERDUE_TASK_WEIGHT;
        }
    }
}
