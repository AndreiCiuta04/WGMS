package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class TaskRepositoryTest implements TestData {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    public void givenNothing_whenSaveTask_thenTaskIsPersisted() {
        Task task = Task.TaskBuilder.aTask()
            .withTitle(TEST_TASK_TITLE)
            .withDescription(TEST_TASK_DESCRIPTION)
            .withDueDate(TEST_TASK_DUE_DATE)
            .withStatus(TaskStatus.OPEN)
            .withCreatedByEmail(DEFAULT_USER)
            .withAssignedToEmail(ADMIN_USER)
            .build();

        Task savedTask = taskRepository.save(task);

        assertAll(
            () -> assertNotNull(savedTask.getId()),
            () -> assertNotNull(savedTask.getCreatedAt()),
            () -> assertNotNull(savedTask.getUpdatedAt()),
            () -> assertEquals(1, taskRepository.findAll().size())
        );
    }

    @Test
    public void givenTasks_whenFindAssignedToUser_thenOnlyAssignedTasksAreReturned() {
        Task assignedTask = Task.TaskBuilder.aTask()
            .withTitle("Assigned task")
            .withDescription("This task is assigned to the default user")
            .withDueDate(TEST_TASK_DUE_DATE)
            .withStatus(TaskStatus.OPEN)
            .withCreatedByEmail(ADMIN_USER)
            .withAssignedToEmail(DEFAULT_USER)
            .build();

        Task otherTask = Task.TaskBuilder.aTask()
            .withTitle("Other task")
            .withDescription("This task belongs to another user")
            .withDueDate(TEST_TASK_DUE_DATE)
            .withStatus(TaskStatus.OPEN)
            .withCreatedByEmail(ADMIN_USER)
            .withAssignedToEmail(ADMIN_USER)
            .build();

        taskRepository.save(assignedTask);
        taskRepository.save(otherTask);

        List<Task> result = taskRepository.findByAssignedToEmailIgnoreCaseOrderByDueDateAscCreatedAtDesc(DEFAULT_USER);

        assertEquals(1, result.size());
        assertEquals("Assigned task", result.get(0).getTitle());
    }
}