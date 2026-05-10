package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class TaskRepositoryTest implements TestData {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private RoommateRepository roommateRepository;

    @Test
    public void givenValidHouseholdTask_whenSaveTask_thenTaskRelationsArePersisted() {
        TestSetup setup = createSetup();

        Task task = createTask(setup.household, setup.mainTenant, setup.roommate, TEST_TASK_TITLE);
        Task savedTask = taskRepository.saveAndFlush(task);

        assertAll(
            () -> assertNotNull(savedTask.getId()),
            () -> assertNotNull(savedTask.getCreatedAt()),
            () -> assertNotNull(savedTask.getUpdatedAt()),
            () -> assertEquals(setup.household.getId(), savedTask.getHousehold().getId()),
            () -> assertEquals(setup.mainTenant.getId(), savedTask.getCreatedBy().getId()),
            () -> assertEquals(setup.roommate.getId(), savedTask.getAssignedTo().getId()),
            () -> assertEquals(TaskStatus.OPEN, savedTask.getStatus())
        );
    }

    @Test
    public void givenTasksInDifferentHouseholds_whenFindAllByHouseholdId_thenOnlyHouseholdTasksAreReturned() {
        TestSetup setup = createSetup();

        taskRepository.save(createTask(setup.household, setup.mainTenant, setup.roommate, "Own WG task"));
        taskRepository.save(createTask(setup.otherHousehold, setup.outsider, setup.outsider, "Other WG task"));
        taskRepository.flush();

        List<Task> result = taskRepository.findAllByHousehold_IdOrderByDueDateAscCreatedAtDesc(setup.household.getId());

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals("Own WG task", result.get(0).getTitle()),
            () -> assertEquals(setup.household.getId(), result.get(0).getHousehold().getId())
        );
    }

    @Test
    public void givenAssignedTasks_whenFindByHouseholdIdAndAssignedToId_thenOnlyMatchingAssignedTasksAreReturned() {
        TestSetup setup = createSetup();

        taskRepository.save(createTask(setup.household, setup.mainTenant, setup.roommate, "Assigned to roommate"));
        taskRepository.save(createTask(setup.household, setup.mainTenant, setup.secondRoommate, "Assigned to second roommate"));
        taskRepository.save(createTask(setup.otherHousehold, setup.outsider, setup.outsider, "Other WG task"));
        taskRepository.flush();

        List<Task> result = taskRepository.findByHousehold_IdAndAssignedTo_IdOrderByDueDateAscCreatedAtDesc(
            setup.household.getId(),
            setup.roommate.getId()
        );

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals("Assigned to roommate", result.get(0).getTitle()),
            () -> assertEquals(setup.roommate.getId(), result.get(0).getAssignedTo().getId()),
            () -> assertEquals(setup.household.getId(), result.get(0).getHousehold().getId())
        );
    }

    @Test
    public void givenTaskWithoutHousehold_whenSaveTask_thenDatabaseRejectsIt() {
        TestSetup setup = createSetup();

        Task task = new Task();
        task.setTitle(TEST_TASK_TITLE);
        task.setDescription(TEST_TASK_DESCRIPTION);
        task.setDueDate(TEST_TASK_DUE_DATE);
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedBy(setup.mainTenant);
        task.setAssignedTo(setup.roommate);

        assertThrows(DataIntegrityViolationException.class, () -> taskRepository.saveAndFlush(task));
    }

    @Test
    public void givenTaskWithoutAssignee_whenSaveTask_thenDatabaseRejectsIt() {
        TestSetup setup = createSetup();

        Task task = new Task();
        task.setHousehold(setup.household);
        task.setTitle(TEST_TASK_TITLE);
        task.setDescription(TEST_TASK_DESCRIPTION);
        task.setDueDate(TEST_TASK_DUE_DATE);
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedBy(setup.mainTenant);

        assertThrows(DataIntegrityViolationException.class, () -> taskRepository.saveAndFlush(task));
    }

    private TestSetup createSetup() {
        ApplicationUser mainTenant = saveUser(MAIN_TENANT_EMAIL, "Main", "Tenant");
        ApplicationUser roommate = saveUser(ROOMMATE_EMAIL, "Normal", "Roommate");
        ApplicationUser secondRoommate = saveUser(SECOND_ROOMMATE_EMAIL, "Second", "Roommate");
        ApplicationUser outsider = saveUser(OUTSIDER_EMAIL, "Other", "User");

        Household household = saveHousehold(TEST_HOUSEHOLD_NAME, mainTenant);
        Household otherHousehold = saveHousehold(OTHER_HOUSEHOLD_NAME, outsider);

        saveRoommate(mainTenant, household, HouseholdRole.MAIN_TENANT);
        saveRoommate(roommate, household, HouseholdRole.ROOMMATE);
        saveRoommate(secondRoommate, household, HouseholdRole.ROOMMATE);
        saveRoommate(outsider, otherHousehold, HouseholdRole.MAIN_TENANT);

        return new TestSetup(mainTenant, roommate, secondRoommate, outsider, household, otherHousehold);
    }

    private ApplicationUser saveUser(String email, String firstName, String lastName) {
        ApplicationUser user = new ApplicationUser();
        user.setEmail(email);
        user.setPassword("encoded-password-for-test");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAdmin(false);
        return userRepository.save(user);
    }

    private Household saveHousehold(String name, ApplicationUser createdBy) {
        Household household = new Household();
        household.setName(name);
        household.setAddress("Test address");
        household.setCreatedBy(createdBy);
        return householdRepository.save(household);
    }

    private Roommate saveRoommate(ApplicationUser user, Household household, HouseholdRole role) {
        Roommate roommate = new Roommate();
        roommate.setUser(user);
        roommate.setHousehold(household);
        roommate.setRole(role);
        roommate.setActive(true);
        return roommateRepository.save(roommate);
    }

    private Task createTask(Household household, ApplicationUser creator, ApplicationUser assignee, String title) {
        Task task = new Task();
        task.setHousehold(household);
        task.setTitle(title);
        task.setDescription(TEST_TASK_DESCRIPTION);
        task.setDueDate(TEST_TASK_DUE_DATE);
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedBy(creator);
        task.setAssignedTo(assignee);
        return task;
    }

    private record TestSetup(
        ApplicationUser mainTenant,
        ApplicationUser roommate,
        ApplicationUser secondRoommate,
        ApplicationUser outsider,
        Household household,
        Household otherHousehold
    ) {
    }
}