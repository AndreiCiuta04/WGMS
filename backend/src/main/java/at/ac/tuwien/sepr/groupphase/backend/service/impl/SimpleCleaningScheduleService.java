package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleActiveUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.CleaningScheduleService;
import at.ac.tuwien.sepr.groupphase.backend.service.FairChoreBalancingService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;

@Service
public class SimpleCleaningScheduleService implements CleaningScheduleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CleaningScheduleRepository cleaningScheduleRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final RoommateRepository roommateRepository;
    private final FairChoreBalancingService fairChoreBalancingService;

    public SimpleCleaningScheduleService(
        CleaningScheduleRepository cleaningScheduleRepository,
        TaskRepository taskRepository,
        UserService userService,
        RoommateRepository roommateRepository,
        FairChoreBalancingService fairChoreBalancingService
    ) {
        this.cleaningScheduleRepository = cleaningScheduleRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.roommateRepository = roommateRepository;
        this.fairChoreBalancingService = fairChoreBalancingService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CleaningSchedule> findVisibleSchedules(String currentUserEmail, boolean admin) {
        LOGGER.debug("Find visible cleaning schedules for user {}", currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);

        return cleaningScheduleRepository.findAllByHousehold_IdOrderByNextDueDateAscCreatedAtDesc(
            currentRoommate.getHousehold().getId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CleaningSchedule> findActiveSchedules(String currentUserEmail, boolean admin) {
        LOGGER.debug("Find active cleaning schedules for user {}", currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);

        return cleaningScheduleRepository.findAllByHousehold_IdAndActiveTrueOrderByNextDueDateAscCreatedAtDesc(
            currentRoommate.getHousehold().getId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CleaningSchedule findOne(Long id, String currentUserEmail, boolean admin) {
        LOGGER.debug("Find cleaning schedule with id {} for user {}", id, currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);
        CleaningSchedule cleaningSchedule = findScheduleById(id);

        requireScheduleInCurrentHousehold(cleaningSchedule, currentRoommate);

        return cleaningSchedule;
    }

    @Override
    @Transactional
    public CleaningSchedule create(CleaningScheduleCreateDto cleaningScheduleCreateDto, String currentUserEmail) {
        LOGGER.debug("Create cleaning schedule by user {}", currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);
        ApplicationUser assignee = getUserByEmail(cleaningScheduleCreateDto.assignedToEmail());

        requireAssigneeInSameHousehold(assignee, currentRoommate);

        CleaningSchedule cleaningSchedule = new CleaningSchedule();
        cleaningSchedule.setHousehold(currentRoommate.getHousehold());
        cleaningSchedule.setTitle(cleaningScheduleCreateDto.title());
        cleaningSchedule.setDescription(cleaningScheduleCreateDto.description());
        cleaningSchedule.setFrequency(cleaningScheduleCreateDto.frequency());
        cleaningSchedule.setStartDate(cleaningScheduleCreateDto.startDate());
        cleaningSchedule.setEndDate(cleaningScheduleCreateDto.endDate());
        cleaningSchedule.setNextDueDate(cleaningScheduleCreateDto.startDate());
        cleaningSchedule.setActive(true);
        cleaningSchedule.setFairBalancingEnabled(Boolean.TRUE.equals(cleaningScheduleCreateDto.fairBalancingEnabled()));
        cleaningSchedule.setCreatedBy(currentUser);
        cleaningSchedule.setAssignedTo(assignee);

        return cleaningScheduleRepository.save(cleaningSchedule);
    }

    @Override
    @Transactional
    public CleaningSchedule update(Long id, CleaningScheduleUpdateDto cleaningScheduleUpdateDto, String currentUserEmail, boolean admin) {
        LOGGER.debug("Update cleaning schedule with id {} by user {}", id, currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);
        CleaningSchedule cleaningSchedule = findScheduleById(id);

        requireScheduleInCurrentHousehold(cleaningSchedule, currentRoommate);
        requireCanManage(cleaningSchedule, currentUser, currentRoommate);

        ApplicationUser assignee = getUserByEmail(cleaningScheduleUpdateDto.assignedToEmail());
        requireAssigneeInSameHousehold(assignee, currentRoommate);

        cleaningSchedule.setTitle(cleaningScheduleUpdateDto.title());
        cleaningSchedule.setDescription(cleaningScheduleUpdateDto.description());
        cleaningSchedule.setFrequency(cleaningScheduleUpdateDto.frequency());
        cleaningSchedule.setStartDate(cleaningScheduleUpdateDto.startDate());
        cleaningSchedule.setEndDate(cleaningScheduleUpdateDto.endDate());
        cleaningSchedule.setAssignedTo(assignee);
        cleaningSchedule.setFairBalancingEnabled(Boolean.TRUE.equals(cleaningScheduleUpdateDto.fairBalancingEnabled()));
        cleaningSchedule.setNextDueDate(
            normalizeNextDueDate(cleaningSchedule.getNextDueDate(), cleaningScheduleUpdateDto.startDate(), cleaningScheduleUpdateDto.endDate())
        );

        return cleaningScheduleRepository.save(cleaningSchedule);
    }

    @Override
    @Transactional
    public CleaningSchedule updateActive(Long id, CleaningScheduleActiveUpdateDto cleaningScheduleActiveUpdateDto, String currentUserEmail, boolean admin) {
        LOGGER.debug("Update active flag of cleaning schedule with id {} by user {}", id, currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);
        CleaningSchedule cleaningSchedule = findScheduleById(id);

        requireScheduleInCurrentHousehold(cleaningSchedule, currentRoommate);
        requireCanManage(cleaningSchedule, currentUser, currentRoommate);

        cleaningSchedule.setActive(cleaningScheduleActiveUpdateDto.active());

        return cleaningScheduleRepository.save(cleaningSchedule);
    }

    @Override
    @Transactional
    public Task generateTask(Long id, String currentUserEmail, boolean admin) {
        LOGGER.debug("Generate task from cleaning schedule with id {} by user {}", id, currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);
        CleaningSchedule cleaningSchedule = findScheduleById(id);

        requireScheduleInCurrentHousehold(cleaningSchedule, currentRoommate);
        requireCanGenerate(cleaningSchedule);

        Task task = new Task();
        task.setHousehold(cleaningSchedule.getHousehold());
        task.setTitle(cleaningSchedule.getTitle());
        task.setDescription(cleaningSchedule.getDescription());
        task.setDueDate(cleaningSchedule.getNextDueDate());
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedBy(currentUser);
        task.setAssignedTo(resolveGeneratedTaskAssignee(cleaningSchedule));
        task.setCleaningSchedule(cleaningSchedule);

        LocalDate nextDueDate = calculateNextDueDate(cleaningSchedule.getNextDueDate(), cleaningSchedule.getFrequency());
        cleaningSchedule.setNextDueDate(nextDueDate);

        if (cleaningSchedule.getEndDate() != null && nextDueDate.isAfter(cleaningSchedule.getEndDate())) {
            cleaningSchedule.setActive(false);
        }

        cleaningScheduleRepository.save(cleaningSchedule);

        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public void delete(Long id, String currentUserEmail, boolean admin) {
        LOGGER.debug("Delete cleaning schedule with id {} by user {}", id, currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);
        CleaningSchedule cleaningSchedule = findScheduleById(id);

        requireScheduleInCurrentHousehold(cleaningSchedule, currentRoommate);
        requireCanManage(cleaningSchedule, currentUser, currentRoommate);

        boolean hasOpenTasks = taskRepository.existsByCleaningSchedule_IdAndStatusNotIn(
            cleaningSchedule.getId(),
            List.of(TaskStatus.DONE, TaskStatus.CANCELLED)
        );

        if (hasOpenTasks) {
            throw new ConflictException(
                "Cleaning schedule cannot be deleted because it has open tasks. Complete or cancel them first."
            );
        }

        cleaningScheduleRepository.delete(cleaningSchedule);
    }

    private CleaningSchedule findScheduleById(Long id) {
        return cleaningScheduleRepository.findWithRelationsById(id)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find cleaning schedule with id %s", id)));
    }

    private ApplicationUser getUserByEmail(String email) {
        return userService.findApplicationUserByEmail(email);
    }

    private Roommate getCurrentRoommate(ApplicationUser currentUser) {
        return roommateRepository.findByUserIdAndActiveTrue(currentUser.getId())
            .orElseThrow(() -> new ForbiddenException("User does not belong to an active household."));
    }

    private void requireAssigneeInSameHousehold(ApplicationUser assignee, Roommate currentRoommate) {
        boolean assigneeIsInSameHousehold = roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(
            assignee.getId(),
            currentRoommate.getHousehold().getId()
        );

        if (!assigneeIsInSameHousehold) {
            throw new ForbiddenException("Assigned user is not a member of your household.");
        }
    }

    private void requireScheduleInCurrentHousehold(CleaningSchedule cleaningSchedule, Roommate currentRoommate) {
        if (!cleaningSchedule.belongsToHousehold(currentRoommate.getHousehold().getId())) {
            throw new ForbiddenException("You are not allowed to access this cleaning schedule.");
        }
    }

    private void requireCanManage(CleaningSchedule cleaningSchedule, ApplicationUser currentUser, Roommate currentRoommate) {
        if (isMainTenant(currentRoommate) || cleaningSchedule.isCreatedBy(currentUser)) {
            return;
        }

        throw new ForbiddenException("You are not allowed to manage this cleaning schedule.");
    }

    private void requireCanGenerate(CleaningSchedule cleaningSchedule) {
        if (!cleaningSchedule.isActive()) {
            throw new ConflictException("Cleaning schedule is inactive.");
        }

        if (cleaningSchedule.getNextDueDate() == null) {
            throw new ConflictException("Cleaning schedule has no next due date.");
        }

        if (cleaningSchedule.getEndDate() != null && cleaningSchedule.getNextDueDate().isAfter(cleaningSchedule.getEndDate())) {
            throw new ConflictException("Cleaning schedule has already ended.");
        }
    }

    private boolean isMainTenant(Roommate roommate) {
        return roommate.getRole() == HouseholdRole.MAIN_TENANT;
    }

    private ApplicationUser resolveGeneratedTaskAssignee(CleaningSchedule cleaningSchedule) {
        if (!cleaningSchedule.isFairBalancingEnabled()) {
            return cleaningSchedule.getAssignedTo();
        }

        ApplicationUser recommendedAssignee;
        try {
            recommendedAssignee = fairChoreBalancingService.findRecommendedAssigneeForSchedule(
                cleaningSchedule.getHousehold(),
                cleaningSchedule,
                cleaningSchedule.getNextDueDate()
            );
        } catch (ConflictException | ForbiddenException | NotFoundException | ValidationException e) {
            LOGGER.warn(
                "Fair balancing failed for cleaning schedule {}. Falling back to configured assignee if available.",
                cleaningSchedule.getId(),
                e
            );
            recommendedAssignee = null;
        }

        if (recommendedAssignee != null) {
            return recommendedAssignee;
        }

        ApplicationUser configuredAssignee = cleaningSchedule.getAssignedTo();
        if (configuredAssignee != null) {
            boolean configuredAssigneeIsValid = roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(
                configuredAssignee.getId(),
                cleaningSchedule.getHousehold().getId()
            );

            if (configuredAssigneeIsValid) {
                return configuredAssignee;
            }

            throw new ConflictException("Cleaning schedule fallback assignee is no longer an active member of this household.");
        }

        throw new ConflictException("Cleaning schedule has no assignee and fair balancing found no recommendation.");
    }

    private LocalDate normalizeNextDueDate(LocalDate currentNextDueDate, LocalDate startDate, LocalDate endDate) {
        if (currentNextDueDate == null) {
            return startDate;
        }

        if (currentNextDueDate.isBefore(startDate)) {
            return startDate;
        }

        if (endDate != null && currentNextDueDate.isAfter(endDate)) {
            return startDate;
        }

        return currentNextDueDate;
    }

    private LocalDate calculateNextDueDate(LocalDate currentDueDate, CleaningFrequency frequency) {
        return switch (frequency) {
            case DAILY -> currentDueDate.plusDays(1);
            case WEEKLY -> currentDueDate.plusWeeks(1);
            case BIWEEKLY -> currentDueDate.plusWeeks(2);
            case MONTHLY -> currentDueDate.plusMonths(1);
        };
    }
}
