package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HistoryDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskHistoryDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.HistoryService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class SimpleHistoryService implements HistoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final RoommateRepository roommateRepository;

    public SimpleHistoryService(
        TaskRepository taskRepository,
        UserService userService,
        RoommateRepository roommateRepository
    ) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.roommateRepository = roommateRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public HistoryDto getHistory(String currentUserEmail, boolean admin) {
        LOGGER.debug("Get history for user {}", currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);

        List<Task> taskHistory = taskRepository.findAllByHousehold_IdAndStatusInOrderByUpdatedAtDescDueDateDescCreatedAtDesc(
            currentRoommate.getHousehold().getId(),
            List.of(TaskStatus.DONE, TaskStatus.CANCELLED)
        );

        return new HistoryDto(
            taskHistory.stream()
                .map(this::taskToTaskHistoryDto)
                .toList(),
            List.of()
        );
    }

    private TaskHistoryDto taskToTaskHistoryDto(Task task) {
        return new TaskHistoryDto(
            task.getId(),
            task.getHouseholdId(),
            task.getCleaningScheduleId(),
            task.getTitle(),
            task.getDescription(),
            task.getDueDate(),
            task.getStatus(),
            task.getCreatedByEmail(),
            task.getAssignedToEmail(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }

    private ApplicationUser getUserByEmail(String email) {
        return userService.findApplicationUserByEmail(email);
    }

    private Roommate getCurrentRoommate(ApplicationUser currentUser) {
        return roommateRepository.findByUserIdAndActiveTrue(currentUser.getId())
            .orElseThrow(() -> new ForbiddenException("User does not belong to an active household."));
    }
}
