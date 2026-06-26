package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskEntityBehaviorTest {

    @Test
    public void builderPopulatesFieldsAndHelperMethodsResolveRelatedIds() {
        Household household = household(10L);
        ApplicationUser creator = user(20L, "creator@test.local");
        ApplicationUser assignee = user(30L, "assignee@test.local");
        CleaningSchedule schedule = new CleaningSchedule();
        schedule.setId(40L);
        LocalDate dueDate = LocalDate.of(2026, 6, 27);
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 26, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 6, 26, 11, 0);

        Task task = Task.TaskBuilder.aTask()
            .withId(1L)
            .withHousehold(household)
            .withTitle("Clean kitchen")
            .withDescription("Counters and floor")
            .withDueDate(dueDate)
            .withStatus(TaskStatus.DONE)
            .withCreatedBy(creator)
            .withAssignedTo(assignee)
            .withCleaningSchedule(schedule)
            .withCreatedAt(createdAt)
            .withUpdatedAt(updatedAt)
            .build();

        assertAll(
            () -> assertEquals(1L, task.getId()),
            () -> assertEquals(10L, task.getHouseholdId()),
            () -> assertEquals("creator@test.local", task.getCreatedByEmail()),
            () -> assertEquals("assignee@test.local", task.getAssignedToEmail()),
            () -> assertEquals(40L, task.getCleaningScheduleId()),
            () -> assertEquals("Clean kitchen", task.getTitle()),
            () -> assertEquals("Counters and floor", task.getDescription()),
            () -> assertEquals(dueDate, task.getDueDate()),
            () -> assertEquals(TaskStatus.DONE, task.getStatus()),
            () -> assertEquals(createdAt, task.getCreatedAt()),
            () -> assertEquals(updatedAt, task.getUpdatedAt()),
            () -> assertTrue(task.isCreatedBy(creator)),
            () -> assertTrue(task.isAssignedTo(assignee)),
            () -> assertTrue(task.belongsToHousehold(10L)),
            () -> assertTrue(task.toString().contains("householdId=10"))
        );
    }

    @Test
    public void helperMethodsAreNullSafeAndCompareById() {
        Task empty = new Task();
        ApplicationUser userWithoutId = user(null, "missing@test.local");

        assertAll(
            () -> assertNull(empty.getHouseholdId()),
            () -> assertNull(empty.getCreatedByEmail()),
            () -> assertNull(empty.getAssignedToEmail()),
            () -> assertNull(empty.getCleaningScheduleId()),
            () -> assertFalse(empty.isCreatedBy(null)),
            () -> assertFalse(empty.isCreatedBy(userWithoutId)),
            () -> assertFalse(empty.isAssignedTo(null)),
            () -> assertFalse(empty.isAssignedTo(userWithoutId)),
            () -> assertFalse(empty.belongsToHousehold(1L))
        );

        Task task = new Task();
        task.setCreatedBy(user(1L, "first@test.local"));
        task.setAssignedTo(user(2L, "second@test.local"));

        assertAll(
            () -> assertTrue(task.isCreatedBy(user(1L, "changed@test.local"))),
            () -> assertFalse(task.isCreatedBy(user(99L, "first@test.local"))),
            () -> assertTrue(task.isAssignedTo(user(2L, "other@test.local"))),
            () -> assertFalse(task.isAssignedTo(user(99L, "second@test.local")))
        );
    }

    @Test
    public void lifecycleCallbacksDefaultStatusAndUpdateTimestamps() throws InterruptedException {
        Task task = new Task();

        task.onCreate();
        LocalDateTime createdAt = task.getCreatedAt();
        LocalDateTime firstUpdatedAt = task.getUpdatedAt();

        Thread.sleep(2);
        task.onUpdate();

        assertAll(
            () -> assertEquals(TaskStatus.OPEN, task.getStatus()),
            () -> assertNotNull(createdAt),
            () -> assertNotNull(firstUpdatedAt),
            () -> assertEquals(createdAt, task.getCreatedAt()),
            () -> assertTrue(task.getUpdatedAt().isAfter(firstUpdatedAt) || task.getUpdatedAt().isEqual(firstUpdatedAt))
        );
    }

    private static Household household(Long id) {
        Household household = new Household();
        household.setId(id);
        household.setName("WG");
        return household;
    }

    private static ApplicationUser user(Long id, String email) {
        ApplicationUser user = new ApplicationUser(email, "password", "First", "Last", false);
        user.setId(id);
        return user;
    }
}
