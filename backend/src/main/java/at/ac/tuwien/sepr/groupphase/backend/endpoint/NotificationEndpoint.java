package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.NotificationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.NotificationSettingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Notification;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationSetting;
import at.ac.tuwien.sepr.groupphase.backend.service.NotificationService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/notifications")
public class NotificationEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationEndpoint(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @Secured("ROLE_USER")
    @GetMapping
    @Operation(summary = "Get notifications for the current user", security = @SecurityRequirement(name = "apiKey"))
    public List<NotificationDto> getNotifications(Authentication authentication) {
        LOGGER.info("GET /api/v1/notifications");
        ApplicationUser user = userService.findApplicationUserByEmail(authentication.getName());
        return notificationService.getNotificationsForUser(user.getId())
            .stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Secured("ROLE_USER")
    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read", security = @SecurityRequirement(name = "apiKey"))
    public void markAsRead(@PathVariable("id") Long id, Authentication authentication) {
        LOGGER.info("PATCH /api/v1/notifications/{}/read", id);
        ApplicationUser user = userService.findApplicationUserByEmail(authentication.getName());
        notificationService.markAsRead(id, user.getId());
    }

    @Secured("ROLE_USER")
    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", security = @SecurityRequirement(name = "apiKey"))
    public void markAllAsRead(Authentication authentication) {
        LOGGER.info("POST /api/v1/notifications/read-all");
        ApplicationUser user = userService.findApplicationUserByEmail(authentication.getName());
        notificationService.markAllAsRead(user.getId());
    }

    @Secured("ROLE_USER")
    @GetMapping("/settings")
    @Operation(summary = "Get notification settings for the current user", security = @SecurityRequirement(name = "apiKey"))
    public NotificationSettingDto getSettings(Authentication authentication) {
        LOGGER.info("GET /api/v1/notifications/settings");
        ApplicationUser user = userService.findApplicationUserByEmail(authentication.getName());
        NotificationSetting setting = notificationService.getSettingsForUser(user);
        return mapToDto(setting);
    }

    @Secured("ROLE_USER")
    @PutMapping("/settings")
    @Operation(summary = "Update notification settings for the current user", security = @SecurityRequirement(name = "apiKey"))
    public NotificationSettingDto updateSettings(@Valid @RequestBody NotificationSettingDto dto, Authentication authentication) {
        LOGGER.info("PUT /api/v1/notifications/settings");
        ApplicationUser user = userService.findApplicationUserByEmail(authentication.getName());
        NotificationSetting setting = notificationService.updateSettingsForUser(user, dto);
        return mapToDto(setting);
    }

    private NotificationDto mapToDto(Notification entity) {
        NotificationDto dto = new NotificationDto();
        dto.setId(entity.getId());
        dto.setMessage(entity.getMessage());
        dto.setType(entity.getType());
        dto.setReferenceId(entity.getReferenceId());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private NotificationSettingDto mapToDto(NotificationSetting entity) {
        NotificationSettingDto dto = new NotificationSettingDto();
        dto.setNotifyTaskAssigned(entity.isNotifyTaskAssigned());
        dto.setNotifyTaskOverdue(entity.isNotifyTaskOverdue());
        dto.setNotifyExpenseCreated(entity.isNotifyExpenseCreated());
        dto.setNotifyPaymentReceived(entity.isNotifyPaymentReceived());
        dto.setNotifyCleaningReminder(entity.isNotifyCleaningReminder());
        dto.setChannelInApp(entity.isChannelInApp());
        dto.setChannelEmail(entity.isChannelEmail());
        dto.setChannelPush(entity.isChannelPush());
        return dto;
    }
}
