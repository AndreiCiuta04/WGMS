package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.NotificationSettingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Notification;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationSetting;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationType;

import java.util.List;

public interface NotificationService {

    void triggerNotification(ApplicationUser recipient, String message, NotificationType type, Long referenceId);

    List<Notification> getNotificationsForUser(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    NotificationSetting getSettingsForUser(ApplicationUser user);

    NotificationSetting updateSettingsForUser(ApplicationUser user, NotificationSettingDto dto);
}
