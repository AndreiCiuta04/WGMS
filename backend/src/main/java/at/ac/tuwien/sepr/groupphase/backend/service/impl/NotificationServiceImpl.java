package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.NotificationSettingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Notification;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationSetting;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationType;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationSettingRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EmailService;
import at.ac.tuwien.sepr.groupphase.backend.service.EmailTemplateRenderer;
import at.ac.tuwien.sepr.groupphase.backend.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final EmailService emailService;
    private final EmailTemplateRenderer emailTemplateRenderer;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   NotificationSettingRepository notificationSettingRepository,
                                   EmailService emailService,
                                   EmailTemplateRenderer emailTemplateRenderer) {
        this.notificationRepository = notificationRepository;
        this.notificationSettingRepository = notificationSettingRepository;
        this.emailService = emailService;
        this.emailTemplateRenderer = emailTemplateRenderer;
    }

    @Override
    @Transactional
    public void triggerNotification(ApplicationUser recipient, String message, NotificationType type, Long referenceId) {
        LOGGER.debug("Trigger notification for user {}: {}", recipient.getEmail(), message);

        NotificationSetting settings = getOrCreateSettings(recipient);

        boolean isEnabled = false;
        switch (type) {
            case TASK_ASSIGNED -> isEnabled = settings.isNotifyTaskAssigned();
            case TASK_OVERDUE -> isEnabled = settings.isNotifyTaskOverdue();
            case OVERDUE_TASK_REMINDER -> isEnabled = settings.isNotifyTaskOverdue();
            case EXPENSE_CREATED -> isEnabled = settings.isNotifyExpenseCreated();
            case PAYMENT_RECEIVED -> isEnabled = settings.isNotifyPaymentReceived();
            case CLEANING_REMINDER -> isEnabled = settings.isNotifyCleaningReminder();
            default -> isEnabled = false;
        }

        if (!isEnabled) {
            LOGGER.debug("Notification type {} is disabled for user {}", type, recipient.getEmail());
            return;
        }

        if (referenceId != null
            && notificationRepository.existsByRecipientIdAndTypeAndReferenceIdAndIsReadFalse(
                recipient.getId(), type, referenceId)) {
            LOGGER.debug("Skipping duplicate unread notification for user {} type {} ref {}",
                recipient.getEmail(), type, referenceId);
            return;
        }

        if (settings.isChannelInApp()) {
            Notification notification = new Notification();
            notification.setRecipient(recipient);
            notification.setMessage(message);
            notification.setType(type);
            notification.setReferenceId(referenceId);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        if (settings.isChannelEmail()) {
            String subject = "WGMS: " + formatNotificationHeadline(type);
            String body = emailTemplateRenderer.renderNotificationEmail(
                recipient.getFirstName(),
                subject,
                message
            );
            emailService.sendEmail(recipient.getEmail(), subject, body);
        }
    }

    private String formatNotificationHeadline(NotificationType type) {
        return switch (type) {
            case TASK_ASSIGNED -> "New task assigned";
            case TASK_OVERDUE -> "Task overdue";
            case OVERDUE_TASK_REMINDER -> "Overdue task reminder";
            case EXPENSE_CREATED -> "New expense recorded";
            case PAYMENT_RECEIVED -> "Payment received";
            case CLEANING_REMINDER -> "Cleaning reminder";
        };
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        LOGGER.debug("Get notifications for user id {}", userId);
        return notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        LOGGER.debug("Mark notification {} as read for user {}", notificationId, userId);
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotFoundException("Notification not found."));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException("Access denied.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        LOGGER.debug("Mark all notifications as read for user {}", userId);
        List<Notification> notifications = notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(userId);
        for (Notification n : notifications) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
    }

    @Override
    @Transactional
    public NotificationSetting getSettingsForUser(ApplicationUser user) {
        LOGGER.debug("Get notification settings for user id {}", user.getId());
        return getOrCreateSettings(user);
    }

    @Override
    @Transactional
    public NotificationSetting updateSettingsForUser(ApplicationUser user, NotificationSettingDto dto) {
        LOGGER.debug("Update notification settings for user id {}", user.getId());
        NotificationSetting settings = getOrCreateSettings(user);

        settings.setNotifyTaskAssigned(dto.isNotifyTaskAssigned());
        settings.setNotifyTaskOverdue(dto.isNotifyTaskOverdue());
        settings.setNotifyExpenseCreated(dto.isNotifyExpenseCreated());
        settings.setNotifyPaymentReceived(dto.isNotifyPaymentReceived());
        settings.setNotifyCleaningReminder(dto.isNotifyCleaningReminder());
        settings.setChannelInApp(dto.isChannelInApp());
        settings.setChannelEmail(dto.isChannelEmail());
        settings.setChannelPush(dto.isChannelPush());

        return notificationSettingRepository.save(settings);
    }

    private NotificationSetting getOrCreateSettings(ApplicationUser user) {
        return notificationSettingRepository.findByUserId(user.getId())
            .orElseGet(() -> notificationSettingRepository.save(new NotificationSetting(user)));
    }
}
