package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleActiveUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.CleaningScheduleMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.TaskMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.CleaningScheduleService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/cleaning-schedules")
public class CleaningScheduleEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final CleaningScheduleService cleaningScheduleService;
    private final CleaningScheduleMapper cleaningScheduleMapper;
    private final TaskMapper taskMapper;

    public CleaningScheduleEndpoint(
        CleaningScheduleService cleaningScheduleService,
        CleaningScheduleMapper cleaningScheduleMapper,
        TaskMapper taskMapper
    ) {
        this.cleaningScheduleService = cleaningScheduleService;
        this.cleaningScheduleMapper = cleaningScheduleMapper;
        this.taskMapper = taskMapper;
    }

    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Create a new cleaning schedule", security = @SecurityRequirement(name = "apiKey"))
    public CleaningScheduleDetailDto create(
        @Valid @RequestBody CleaningScheduleCreateDto cleaningScheduleCreateDto,
        Authentication authentication
    ) {
        LOGGER.info("POST /api/v1/cleaning-schedules body: {}", cleaningScheduleCreateDto);
        return cleaningScheduleMapper.cleaningScheduleToCleaningScheduleDetailDto(
            cleaningScheduleService.create(cleaningScheduleCreateDto, getCurrentUserEmail(authentication))
        );
    }

    @Secured("ROLE_USER")
    @GetMapping
    @Operation(summary = "Get cleaning schedules visible to the current user", security = @SecurityRequirement(name = "apiKey"))
    public List<CleaningScheduleDetailDto> findAll(Authentication authentication) {
        LOGGER.info("GET /api/v1/cleaning-schedules");
        return cleaningScheduleMapper.cleaningScheduleToCleaningScheduleDetailDto(
            cleaningScheduleService.findVisibleSchedules(getCurrentUserEmail(authentication), isAdmin(authentication))
        );
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/active")
    @Operation(summary = "Get active cleaning schedules visible to the current user", security = @SecurityRequirement(name = "apiKey"))
    public List<CleaningScheduleDetailDto> findActive(Authentication authentication) {
        LOGGER.info("GET /api/v1/cleaning-schedules/active");
        return cleaningScheduleMapper.cleaningScheduleToCleaningScheduleDetailDto(
            cleaningScheduleService.findActiveSchedules(getCurrentUserEmail(authentication), isAdmin(authentication))
        );
    }

    @Secured("ROLE_USER")
    @GetMapping(value = "/{id}")
    @Operation(summary = "Get one cleaning schedule by id", security = @SecurityRequirement(name = "apiKey"))
    public CleaningScheduleDetailDto findOne(@PathVariable(name = "id") Long id, Authentication authentication) {
        LOGGER.info("GET /api/v1/cleaning-schedules/{}", id);
        return cleaningScheduleMapper.cleaningScheduleToCleaningScheduleDetailDto(
            cleaningScheduleService.findOne(id, getCurrentUserEmail(authentication), isAdmin(authentication))
        );
    }

    @Secured("ROLE_USER")
    @PutMapping(value = "/{id}")
    @Operation(summary = "Update a cleaning schedule", security = @SecurityRequirement(name = "apiKey"))
    public CleaningScheduleDetailDto update(
        @PathVariable(name = "id") Long id,
        @Valid @RequestBody CleaningScheduleUpdateDto cleaningScheduleUpdateDto,
        Authentication authentication
    ) {
        LOGGER.info("PUT /api/v1/cleaning-schedules/{} body: {}", id, cleaningScheduleUpdateDto);
        return cleaningScheduleMapper.cleaningScheduleToCleaningScheduleDetailDto(
            cleaningScheduleService.update(id, cleaningScheduleUpdateDto, getCurrentUserEmail(authentication), isAdmin(authentication))
        );
    }

    @Secured("ROLE_USER")
    @PatchMapping(value = "/{id}/active")
    @Operation(summary = "Activate or deactivate a cleaning schedule", security = @SecurityRequirement(name = "apiKey"))
    public CleaningScheduleDetailDto updateActive(
        @PathVariable(name = "id") Long id,
        @Valid @RequestBody CleaningScheduleActiveUpdateDto cleaningScheduleActiveUpdateDto,
        Authentication authentication
    ) {
        LOGGER.info("PATCH /api/v1/cleaning-schedules/{}/active body: {}", id, cleaningScheduleActiveUpdateDto);
        return cleaningScheduleMapper.cleaningScheduleToCleaningScheduleDetailDto(
            cleaningScheduleService.updateActive(id, cleaningScheduleActiveUpdateDto, getCurrentUserEmail(authentication), isAdmin(authentication))
        );
    }

    @Secured("ROLE_USER")
    @PostMapping(value = "/{id}/generate-task")
    @Operation(summary = "Generate a concrete task from a cleaning schedule", security = @SecurityRequirement(name = "apiKey"))
    public TaskDetailDto generateTask(@PathVariable(name = "id") Long id, Authentication authentication) {
        LOGGER.info("POST /api/v1/cleaning-schedules/{}/generate-task", id);
        return taskMapper.taskToTaskDetailDto(
            cleaningScheduleService.generateTask(id, getCurrentUserEmail(authentication), isAdmin(authentication))
        );
    }

    @Secured("ROLE_USER")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Delete a cleaning schedule", security = @SecurityRequirement(name = "apiKey"))
    public void delete(@PathVariable(name = "id") Long id, Authentication authentication) {
        LOGGER.info("DELETE /api/v1/cleaning-schedules/{}", id);
        cleaningScheduleService.delete(id, getCurrentUserEmail(authentication), isAdmin(authentication));
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