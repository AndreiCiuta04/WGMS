package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.TaskService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class SimpleTaskService implements TaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TaskRepository taskRepository;
    private final UserService userService;

    public SimpleTaskService(TaskRepository taskRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    @Override
    public List<Task> findVisibleTasks(String currentUserEmail, boolean admin) {
        LOGGER.debug("Find visible tasks for user {} with admin={}", currentUserEmail, admin);

        if (admin) {
            return taskRepository.findAllByOrderByDueDateAscCreatedAtDesc();
        }

        return taskRepository.findByCreatedByEmailIgnoreCaseOrAssignedToEmailIgnoreCaseOrderByDueDateAscCreatedAtDesc(
            currentUserEmail,
            currentUserEmail
        );
    }

    @Override
    public List<Task> findAssignedToCurrentUser(String currentUserEmail) {
        LOGGER.debug("Find tasks assigned to user {}", currentUserEmail);
        return taskRepository.findByAssignedToEmailIgnoreCaseOrderByDueDateAscCreatedAtDesc(currentUserEmail);
    }

    @Override
    public Task findOne(Long id, String currentUserEmail, boolean admin) {
        LOGGER.debug("Find task with id {} for user {} with admin={}", id, currentUserEmail, admin);

        Task task = findTaskById(id);

        if (!canView(task, currentUserEmail, admin)) {
            throw new AccessDeniedException("You are not allowed to access this task");
        }

        return task;
    }

    @Override
    public Task create(TaskCreateDto taskCreateDto, String currentUserEmail) {
        LOGGER.debug("Create task {} by user {}", taskCreateDto, currentUserEmail);

        validateUserExists(taskCreateDto.getAssignedToEmail());

        Task task = new Task();
        task.setTitle(taskCreateDto.getTitle());
        task.setDescription(taskCreateDto.getDescription());
        task.setDueDate(taskCreateDto.getDueDate());
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedByEmail(currentUserEmail);
        task.setAssignedToEmail(taskCreateDto.getAssignedToEmail());

        return taskRepository.save(task);
    }

    @Override
    public Task update(Long id, TaskUpdateDto taskUpdateDto, String currentUserEmail, boolean admin) {
        LOGGER.debug("Update task with id {} using data {} by user {} with admin={}", id, taskUpdateDto, currentUserEmail, admin);

        Task task = findTaskById(id);

        if (!canManage(task, currentUserEmail, admin)) {
            throw new AccessDeniedException("You are not allowed to update this task");
        }

        validateUserExists(taskUpdateDto.getAssignedToEmail());

        task.setTitle(taskUpdateDto.getTitle());
        task.setDescription(taskUpdateDto.getDescription());
        task.setDueDate(taskUpdateDto.getDueDate());
        task.setAssignedToEmail(taskUpdateDto.getAssignedToEmail());

        return taskRepository.save(task);
    }

    @Override
    public Task updateStatus(Long id, TaskStatusUpdateDto taskStatusUpdateDto, String currentUserEmail, boolean admin) {
        LOGGER.debug("Update status of task with id {} to {} by user {} with admin={}",
            id,
            taskStatusUpdateDto.getStatus(),
            currentUserEmail,
            admin
        );

        Task task = findTaskById(id);

        if (!canView(task, currentUserEmail, admin)) {
            throw new AccessDeniedException("You are not allowed to update the status of this task");
        }

        task.setStatus(taskStatusUpdateDto.getStatus());

        return taskRepository.save(task);
    }

    @Override
    public void delete(Long id, String currentUserEmail, boolean admin) {
        LOGGER.debug("Delete task with id {} by user {} with admin={}", id, currentUserEmail, admin);

        Task task = findTaskById(id);

        if (!canManage(task, currentUserEmail, admin)) {
            throw new AccessDeniedException("You are not allowed to delete this task");
        }

        taskRepository.delete(task);
    }

    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find task with id %s", id)));
    }

    private void validateUserExists(String email) {
        userService.findApplicationUserByEmail(email);
    }

    private boolean canView(Task task, String currentUserEmail, boolean admin) {
        return admin || task.isCreatedBy(currentUserEmail) || task.isAssignedTo(currentUserEmail);
    }

    private boolean canManage(Task task, String currentUserEmail, boolean admin) {
        return admin || task.isCreatedBy(currentUserEmail);
    }
}