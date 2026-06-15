package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.FairChoreRecommendationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.FairChoreOverdueReminderDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.TaskMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.FairChoreBalancingService;
import at.ac.tuwien.sepr.groupphase.backend.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/tasks")
public class TaskEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private final FairChoreBalancingService fairChoreBalancingService;

    public TaskEndpoint(
        TaskService taskService,
        TaskMapper taskMapper,
        FairChoreBalancingService fairChoreBalancingService
    ) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
        this.fairChoreBalancingService = fairChoreBalancingService;
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/fair-balancing/recommendation")
    @Operation(summary = "Get a fair chore assignee recommendation", security = @SecurityRequirement(name = "apiKey"))
    public FairChoreRecommendationDto fairBalancingRecommendation(
        @RequestParam(name = "historyDays", required = false) Integer historyDays,
        @RequestParam(name = "taskTitle", required = false) String taskTitle,
        @RequestParam(name = "dueDate", required = false) LocalDate dueDate,
        @RequestParam(name = "cleaningScheduleId", required = false) Long cleaningScheduleId,
        Authentication authentication
    ) {
        LOGGER.info("GET /api/v1/tasks/fair-balancing/recommendation");
        return fairChoreBalancingService.recommendAssignee(
            getCurrentUserEmail(authentication),
            historyDays,
            taskTitle,
            dueDate,
            cleaningScheduleId
        );
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/fair-balancing/overdue-reminders/{roommateId}")
    @Operation(summary = "Send an overdue task reminder to a roommate", security = @SecurityRequirement(name = "apiKey"))
    public FairChoreOverdueReminderDto sendFairBalancingOverdueReminder(
        @PathVariable(name = "roommateId") Long roommateId,
        Authentication authentication
    ) {
        LOGGER.info("POST /api/v1/tasks/fair-balancing/overdue-reminders/{}", roommateId);
        return fairChoreBalancingService.sendOverdueReminder(
            getCurrentUserEmail(authentication),
            roommateId
        );
    }

    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Create a new task and assign it to a user", security = @SecurityRequirement(name = "apiKey"))
    public TaskDetailDto create(@Valid @RequestBody TaskCreateDto taskCreateDto, Authentication authentication) {
        LOGGER.info("POST /api/v1/tasks");
        return taskMapper.taskToTaskDetailDto(taskService.create(taskCreateDto, getCurrentUserEmail(authentication)));
    }

    @Secured("ROLE_USER")
    @GetMapping
    @Operation(summary = "Get tasks visible to the current user", security = @SecurityRequirement(name = "apiKey"))
    public List<TaskDetailDto> findAll(Authentication authentication) {
        LOGGER.info("GET /api/v1/tasks");
        return taskMapper.taskToTaskDetailDto(
            taskService.findVisibleTasks(getCurrentUserEmail(authentication), isAdmin(authentication))
        );
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/my")
    @Operation(summary = "Get tasks assigned to the current user", security = @SecurityRequirement(name = "apiKey"))
    public List<TaskDetailDto> findAssignedToMe(Authentication authentication) {
        LOGGER.info("GET /api/v1/tasks/my");
        return taskMapper.taskToTaskDetailDto(taskService.findAssignedToCurrentUser(getCurrentUserEmail(authentication)));
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/{id}")
    @Operation(summary = "Get one task by id", security = @SecurityRequirement(name = "apiKey"))
    public TaskDetailDto findOne(@PathVariable(name = "id") Long id, Authentication authentication) {
        LOGGER.info("GET /api/v1/tasks/{}", id);
        return taskMapper.taskToTaskDetailDto(
            taskService.findOne(id, getCurrentUserEmail(authentication), isAdmin(authentication))
        );
    }

    @Secured("ROLE_USER")
    @PutMapping(value = "/{id}")
    @Operation(summary = "Update a task", security = @SecurityRequirement(name = "apiKey"))
    public TaskDetailDto update(
        @PathVariable(name = "id") Long id,
        @Valid @RequestBody TaskUpdateDto taskUpdateDto,
        Authentication authentication
    ) {
        LOGGER.info("PUT /api/v1/tasks/{}", id);
        return taskMapper.taskToTaskDetailDto(
            taskService.update(id, taskUpdateDto, getCurrentUserEmail(authentication), isAdmin(authentication))
        );
    }

    @Secured("ROLE_USER")
    @PatchMapping(value = "/{id}/status")
    @Operation(summary = "Update the status of a task", security = @SecurityRequirement(name = "apiKey"))
    public TaskDetailDto updateStatus(
        @PathVariable(name = "id") Long id,
        @Valid @RequestBody TaskStatusUpdateDto taskStatusUpdateDto,
        Authentication authentication
    ) {
        LOGGER.info("PATCH /api/v1/tasks/{}/status", id);
        return taskMapper.taskToTaskDetailDto(
            taskService.updateStatus(id, taskStatusUpdateDto, getCurrentUserEmail(authentication), isAdmin(authentication))
        );
    }

    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Delete a task", security = @SecurityRequirement(name = "apiKey"))
    public void delete(@PathVariable(name = "id") Long id, Authentication authentication) {
        LOGGER.info("DELETE /api/v1/tasks/{}", id);
        taskService.delete(id, getCurrentUserEmail(authentication), isAdmin(authentication));
    }

    private String getCurrentUserEmail(Authentication authentication) {
        return authentication.getName();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities()
            .stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
