package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.TaskService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class SimpleTaskService implements TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final RoommateRepository roommateRepository;

    public SimpleTaskService(
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
    public List<Task> findVisibleTasks(String currentUserEmail, boolean admin) {
        LOGGER.debug("Find visible tasks for user {}", currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);

        return taskRepository.findAllByHousehold_IdOrderByDueDateAscCreatedAtDesc(
            currentRoommate.getHousehold().getId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findAssignedToCurrentUser(String currentUserEmail) {
        LOGGER.debug("Find tasks assigned to user {}", currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);

        return taskRepository.findByHousehold_IdAndAssignedTo_IdOrderByDueDateAscCreatedAtDesc(
            currentRoommate.getHousehold().getId(),
            currentUser.getId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Task findOne(Long id, String currentUserEmail, boolean admin) {
        LOGGER.debug("Find task with id {} for user {}", id, currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);
        Task task = findTaskById(id);

        requireTaskInCurrentHousehold(task, currentRoommate);

        return task;
    }

    @Override
    @Transactional
    public Task create(TaskCreateDto taskCreateDto, String currentUserEmail) {
        LOGGER.debug("Create task by user {}", currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);
        ApplicationUser assignee = getUserByEmail(taskCreateDto.assignedToEmail());

        requireAssigneeInSameHousehold(assignee, currentRoommate);

        Task task = new Task();
        task.setHousehold(currentRoommate.getHousehold());
        task.setTitle(taskCreateDto.title());
        task.setDescription(taskCreateDto.description());
        task.setDueDate(taskCreateDto.dueDate());
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedBy(currentUser);
        task.setAssignedTo(assignee);

        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task update(Long id, TaskUpdateDto taskUpdateDto, String currentUserEmail, boolean admin) {
        LOGGER.debug("Update task with id {} by user {}", id, currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);
        Task task = findTaskById(id);

        requireTaskInCurrentHousehold(task, currentRoommate);
        requireCanManage(task, currentUser, currentRoommate);

        ApplicationUser assignee = getUserByEmail(taskUpdateDto.assignedToEmail());
        requireAssigneeInSameHousehold(assignee, currentRoommate);

        task.setTitle(taskUpdateDto.title());
        task.setDescription(taskUpdateDto.description());
        task.setDueDate(taskUpdateDto.dueDate());
        task.setAssignedTo(assignee);

        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public Task updateStatus(Long id, TaskStatusUpdateDto taskStatusUpdateDto, String currentUserEmail, boolean admin) {
        LOGGER.debug("Update status of task with id {} by user {}", id, currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);
        Task task = findTaskById(id);

        requireTaskInCurrentHousehold(task, currentRoommate);
        requireCanUpdateStatus(task, currentUser, currentRoommate);

        task.setStatus(taskStatusUpdateDto.status());

        return taskRepository.save(task);
    }

    @Override
    @Transactional
    public void delete(Long id, String currentUserEmail, boolean admin) {
        LOGGER.debug("Delete task with id {} by user {}", id, currentUserEmail);

        ApplicationUser currentUser = getUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);
        Task task = findTaskById(id);

        requireTaskInCurrentHousehold(task, currentRoommate);
        requireCanManage(task, currentUser, currentRoommate);

        taskRepository.delete(task);
    }

    private Task findTaskById(Long id) {
        return taskRepository.findWithRelationsById(id)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find task with id %s", id)));
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

    private void requireTaskInCurrentHousehold(Task task, Roommate currentRoommate) {
        if (!task.belongsToHousehold(currentRoommate.getHousehold().getId())) {
            throw new ForbiddenException("You are not allowed to access this task.");
        }
    }

    private void requireCanManage(Task task, ApplicationUser currentUser, Roommate currentRoommate) {
        if (isMainTenant(currentRoommate) || task.isCreatedBy(currentUser)) {
            return;
        }

        throw new ForbiddenException("You are not allowed to manage this task.");
    }

    private void requireCanUpdateStatus(Task task, ApplicationUser currentUser, Roommate currentRoommate) {
        if (isMainTenant(currentRoommate) || task.isCreatedBy(currentUser) || task.isAssignedTo(currentUser)) {
            return;
        }

        throw new ForbiddenException("You are not allowed to update this task status.");
    }

    private boolean isMainTenant(Roommate roommate) {
        return roommate.getRole() == HouseholdRole.MAIN_TENANT;
    }
}
