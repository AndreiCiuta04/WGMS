package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationType;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;

@Component
public class ReminderScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final TaskRepository taskRepository;
    private final CleaningScheduleRepository cleaningScheduleRepository;
    private final NotificationService notificationService;

    @Autowired
    public ReminderScheduler(TaskRepository taskRepository,
                             CleaningScheduleRepository cleaningScheduleRepository,
                             NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.cleaningScheduleRepository = cleaningScheduleRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "${wgms.reminders.cron:0 0 8 * * *}")
    public void sendReminders() {
        LOGGER.info("Starting daily overdue task and cleaning reminders check");
        LocalDate today = LocalDate.now();

        // Overdue Tasks
        List<Task> allTasks = taskRepository.findAll();
        for (Task task : allTasks) {
            if (task.getStatus() != TaskStatus.DONE && task.getDueDate() != null && task.getDueDate().isBefore(today)) {
                if (task.getAssignedTo() != null) {
                    notificationService.triggerNotification(
                        task.getAssignedTo(),
                        String.format("Task is overdue: %s (Due was: %s)", task.getTitle(), task.getDueDate()),
                        NotificationType.TASK_OVERDUE,
                        task.getId()
                    );
                }
            }
        }

        // Due Cleaning Schedules
        List<CleaningSchedule> schedules = cleaningScheduleRepository.findAll();
        for (CleaningSchedule schedule : schedules) {
            if (schedule.isActive() && schedule.getNextDueDate() != null && !schedule.getNextDueDate().isAfter(today)) {
                if (schedule.getAssignedTo() != null) {
                    notificationService.triggerNotification(
                        schedule.getAssignedTo(),
                        String.format("Cleaning is due: %s (Due: %s)", schedule.getTitle(), schedule.getNextDueDate()),
                        NotificationType.CLEANING_REMINDER,
                        schedule.getId()
                    );
                }
            }
        }
        LOGGER.info("Finished daily overdue task and cleaning reminders check");
    }
}
