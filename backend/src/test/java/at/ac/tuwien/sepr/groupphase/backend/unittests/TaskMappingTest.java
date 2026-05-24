package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.TaskMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TaskMappingTest implements TestData {

    @Autowired
    private TaskMapper taskMapper;

    @Test
    public void givenTaskEntity_whenMapToDetailDto_thenHouseholdCreatorAndAssigneeAreMapped() {
        ApplicationUser creator = user(1L, MAIN_TENANT_EMAIL);
        ApplicationUser assignee = user(2L, ROOMMATE_EMAIL);

        Household household = new Household();
        household.setId(10L);
        household.setName(TEST_HOUSEHOLD_NAME);
        household.setAddress("Test address");
        household.setCreatedBy(creator);

        Task task = new Task();
        task.setId(99L);
        task.setHousehold(household);
        task.setTitle(TEST_TASK_TITLE);
        task.setDescription(TEST_TASK_DESCRIPTION);
        task.setDueDate(TEST_TASK_DUE_DATE);
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedBy(creator);
        task.setAssignedTo(assignee);

        TaskDetailDto dto = taskMapper.taskToTaskDetailDto(task);

        assertAll(
            () -> assertEquals(99L, dto.id()),
            () -> assertEquals(10L, dto.householdId()),
            () -> assertEquals(TEST_TASK_TITLE, dto.title()),
            () -> assertEquals(TEST_TASK_DESCRIPTION, dto.description()),
            () -> assertEquals(TEST_TASK_DUE_DATE, dto.dueDate()),
            () -> assertEquals(TaskStatus.OPEN, dto.status()),
            () -> assertEquals(MAIN_TENANT_EMAIL, dto.createdByEmail()),
            () -> assertEquals(ROOMMATE_EMAIL, dto.assignedToEmail())
        );
    }

    private ApplicationUser user(Long id, String email) {
        ApplicationUser user = new ApplicationUser();
        user.setId(id);
        user.setEmail(email);
        user.setPassword("encoded-password-for-test");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setAdmin(false);
        return user;
    }
}
