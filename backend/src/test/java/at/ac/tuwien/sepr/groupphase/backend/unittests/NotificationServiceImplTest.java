package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Notification;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationSetting;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationType;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationSettingRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EmailService;
import at.ac.tuwien.sepr.groupphase.backend.service.EmailTemplateRenderer;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSettingRepository notificationSettingRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateRenderer emailTemplateRenderer;

    private NotificationServiceImpl notificationService;

    private ApplicationUser recipient;

    @BeforeEach
    public void setUp() {
        notificationService = new NotificationServiceImpl(
            notificationRepository, notificationSettingRepository, emailService, emailTemplateRenderer);
        recipient = new ApplicationUser("user@example.com", "Password123!", "Test", "User", false);
        recipient.setId(1L);
    }

    @Test
    public void givenEnabledInAppSetting_whenTriggerNotification_thenPersistsNotification() {
        NotificationSetting settings = new NotificationSetting(recipient);
        settings.setChannelEmail(false);
        when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.of(settings));
        when(notificationRepository.existsByRecipientIdAndTypeAndReferenceIdAndIsReadFalse(1L,
            NotificationType.TASK_ASSIGNED, 42L)).thenReturn(false);

        notificationService.triggerNotification(
            recipient, "You have been assigned a new task: Dishes", NotificationType.TASK_ASSIGNED, 42L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertEquals(recipient, saved.getRecipient());
        assertEquals(NotificationType.TASK_ASSIGNED, saved.getType());
        assertEquals(42L, saved.getReferenceId());
        assertTrue(saved.getMessage().contains("Dishes"));
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    public void givenDisabledType_whenTriggerNotification_thenSkipsCreation() {
        NotificationSetting settings = new NotificationSetting(recipient);
        settings.setNotifyTaskAssigned(false);
        when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.of(settings));

        notificationService.triggerNotification(
            recipient, "You have been assigned a new task: Dishes", NotificationType.TASK_ASSIGNED, 42L);

        verify(notificationRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    public void givenUnreadDuplicate_whenTriggerNotification_thenSkipsCreation() {
        NotificationSetting settings = new NotificationSetting(recipient);
        settings.setChannelEmail(false);
        when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.of(settings));
        when(notificationRepository.existsByRecipientIdAndTypeAndReferenceIdAndIsReadFalse(1L,
            NotificationType.TASK_OVERDUE, 10L)).thenReturn(true);

        notificationService.triggerNotification(
            recipient, "Task is overdue: Clean room", NotificationType.TASK_OVERDUE, 10L);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    public void givenOverdueReminderTypeAndOverdueSettingEnabled_whenTriggerNotification_thenPersistsNotification() {
        NotificationSetting settings = new NotificationSetting(recipient);
        settings.setChannelEmail(false);
        settings.setNotifyTaskOverdue(true);
        when(notificationSettingRepository.findByUserId(1L)).thenReturn(Optional.of(settings));
        when(notificationRepository.existsByRecipientIdAndTypeAndReferenceIdAndIsReadFalse(1L,
            NotificationType.OVERDUE_TASK_REMINDER, 2L)).thenReturn(false);

        notificationService.triggerNotification(
            recipient,
            "Overdue task reminder: You currently have 5 overdue household tasks. Please review your assigned tasks.",
            NotificationType.OVERDUE_TASK_REMINDER,
            2L
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationType.OVERDUE_TASK_REMINDER, captor.getValue().getType());
        assertTrue(captor.getValue().getMessage().contains("5 overdue household tasks"));
    }

    @Test
    public void givenOtherUsersNotification_whenMarkAsRead_thenForbidden() {
        ApplicationUser owner = new ApplicationUser();
        owner.setId(2L);

        Notification notification = new Notification();
        notification.setId(99L);
        notification.setRecipient(owner);
        notification.setRead(false);

        when(notificationRepository.findById(99L)).thenReturn(Optional.of(notification));

        assertThrows(ForbiddenException.class, () -> notificationService.markAsRead(99L, 1L));
        verify(notificationRepository, never()).save(any());
    }
}
