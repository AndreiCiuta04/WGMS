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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void givenTasksInDifferentHouseholds_whenFindAllByHouseholdIdForFairBalancing_thenOnlyHouseholdTasksAreReturned() {
        TestSetup setup = createSetup();

        taskRepository.save(createTask(setup.household, setup.mainTenant, setup.roommate, "Own WG fairness task"));
        taskRepository.save(createTask(
            setup.otherHousehold,
            setup.outsider,
            setup.mainTenant,
            "Other WG task assigned to current user"
        ));
        taskRepository.flush();

        List<Task> result = taskRepository.findAllByHousehold_Id(setup.household.getId());

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals("Own WG fairness task", result.get(0).getTitle()),
            () -> assertEquals(setup.household.getId(), result.get(0).getHousehold().getId())
        );
    }

    @Test
    public void givenFairBalancingTasks_whenFindFairBalancingTasksByHouseholdId_thenFiltersHouseholdStatusAndHistoryWindow() {
        TestSetup setup = createSetup();

        taskRepository.save(createTask(setup.household, setup.mainTenant, setup.roommate, "Open included"));
        taskRepository.save(createTaskWithStatus(setup.household, setup.mainTenant, setup.roommate, "In progress included", TaskStatus.IN_PROGRESS));
        taskRepository.save(createTaskWithStatus(setup.household, setup.mainTenant, setup.roommate, "Done recent included", TaskStatus.DONE));
        taskRepository.save(createTaskWithStatus(setup.household, setup.mainTenant, setup.roommate, "Cancelled excluded", TaskStatus.CANCELLED));
        taskRepository.save(createTask(setup.otherHousehold, setup.outsider, setup.outsider, "Other household excluded"));
        taskRepository.flush();

        List<Task> result = taskRepository.findFairBalancingTasksByHouseholdId(
            setup.household.getId(),
            List.of(TaskStatus.OPEN, TaskStatus.IN_PROGRESS),
            TaskStatus.DONE,
            LocalDateTime.now().minusDays(1)
        );
        List<String> titles = result.stream().map(Task::getTitle).toList();

        List<Task> futureWindowResult = taskRepository.findFairBalancingTasksByHouseholdId(
            setup.household.getId(),
            List.of(TaskStatus.OPEN, TaskStatus.IN_PROGRESS),
            TaskStatus.DONE,
            LocalDateTime.now().plusDays(1)
        );
        List<String> futureWindowTitles = futureWindowResult.stream().map(Task::getTitle).toList();

        assertAll(
            () -> assertEquals(3, result.size()),
            () -> assertTrue(titles.contains("Open included")),
            () -> assertTrue(titles.contains("In progress included")),
            () -> assertTrue(titles.contains("Done recent included")),
            () -> assertFalse(titles.contains("Cancelled excluded")),
            () -> assertFalse(titles.contains("Other household excluded")),
            () -> assertTrue(futureWindowTitles.contains("Open included")),
            () -> assertTrue(futureWindowTitles.contains("In progress included")),
            () -> assertFalse(futureWindowTitles.contains("Done recent included"))
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

    private Task createTaskWithStatus(Household household, ApplicationUser creator, ApplicationUser assignee, String title, TaskStatus status) {
        Task task = createTask(household, creator, assignee, title);
        task.setStatus(status);
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
