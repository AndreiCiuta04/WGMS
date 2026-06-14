package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationType;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.NotificationService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ReminderScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReminderSchedulerTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CleaningScheduleRepository cleaningScheduleRepository;

    @Mock
    private NotificationService notificationService;

    private ReminderScheduler reminderScheduler;

    @BeforeEach
    public void setUp() {
        reminderScheduler = new ReminderScheduler(taskRepository, cleaningScheduleRepository, notificationService);
    }

    @Test
    public void givenOverdueTask_whenSendReminders_thenTriggerNotification() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setEmail("test@test.com");

        Task task = new Task();
        task.setId(10L);
        task.setTitle("Clean room");
        task.setStatus(TaskStatus.OPEN);
        task.setDueDate(LocalDate.now().minusDays(1));
        task.setAssignedTo(user);

        when(taskRepository.findAll()).thenReturn(List.of(task));
        when(cleaningScheduleRepository.findAll()).thenReturn(List.of());

        reminderScheduler.sendReminders();

        verify(notificationService).triggerNotification(
            eq(user),
            contains("Task is overdue"),
            eq(NotificationType.TASK_OVERDUE),
            eq(10L)
        );
    }

    @Test
    public void givenDueCleaningSchedule_whenSendReminders_thenTriggerNotification() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setEmail("test@test.com");

        CleaningSchedule schedule = new CleaningSchedule();
        schedule.setId(20L);
        schedule.setTitle("Weekly Kitchen Sweep");
        schedule.setActive(true);
        schedule.setNextDueDate(LocalDate.now());
        schedule.setAssignedTo(user);

        when(taskRepository.findAll()).thenReturn(List.of());
        when(cleaningScheduleRepository.findAll()).thenReturn(List.of(schedule));

        reminderScheduler.sendReminders();

        verify(notificationService).triggerNotification(
            eq(user),
            contains("Cleaning is due"),
            eq(NotificationType.CLEANING_REMINDER),
            eq(20L)
        );
    }

    @Test
    public void givenCompletedTask_whenSendReminders_thenNoNotification() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setEmail("test@test.com");

        Task task = new Task();
        task.setId(10L);
        task.setTitle("Already done");
        task.setStatus(TaskStatus.DONE);
        task.setDueDate(LocalDate.now().minusDays(1));
        task.setAssignedTo(user);

        when(taskRepository.findAll()).thenReturn(List.of(task));
        when(cleaningScheduleRepository.findAll()).thenReturn(List.of());

        reminderScheduler.sendReminders();

        verify(notificationService, never()).triggerNotification(any(), any(), any(), any());
    }
}
