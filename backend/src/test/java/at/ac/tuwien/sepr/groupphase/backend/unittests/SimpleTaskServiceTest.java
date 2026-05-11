package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.TaskService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.MAIN_TENANT_EMAIL;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.OTHER_HOUSEHOLD_NAME;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.OUTSIDER_EMAIL;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.ROOMMATE_EMAIL;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.SECOND_ROOMMATE_EMAIL;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.TEST_HOUSEHOLD_NAME;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.TEST_TASK_DESCRIPTION;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.TEST_TASK_DUE_DATE;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.TEST_TASK_TITLE;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.UPDATED_TASK_DESCRIPTION;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.UPDATED_TASK_DUE_DATE;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.UPDATED_TASK_TITLE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SimpleTaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private RoommateRepository roommateRepository;

    private TaskService taskService;

    private ApplicationUser mainTenant;
    private ApplicationUser roommate;
    private ApplicationUser secondRoommate;
    private ApplicationUser outsider;
    private Household household;
    private Household otherHousehold;
    private Roommate mainTenantMembership;
    private Roommate roommateMembership;
    private Roommate secondRoommateMembership;

    @BeforeEach
    public void setUp() {
        taskService = new SimpleTaskService(taskRepository, userService, roommateRepository);

        mainTenant = user(1L, MAIN_TENANT_EMAIL);
        roommate = user(2L, ROOMMATE_EMAIL);
        secondRoommate = user(3L, SECOND_ROOMMATE_EMAIL);
        outsider = user(4L, OUTSIDER_EMAIL);

        household = household(1L, TEST_HOUSEHOLD_NAME, mainTenant);
        otherHousehold = household(2L, OTHER_HOUSEHOLD_NAME, outsider);

        mainTenantMembership = roommate(1L, mainTenant, household, HouseholdRole.MAIN_TENANT);
        roommateMembership = roommate(2L, roommate, household, HouseholdRole.ROOMMATE);
        secondRoommateMembership = roommate(3L, secondRoommate, household, HouseholdRole.ROOMMATE);
    }

    @Test
    public void givenCurrentUserWithHouseholdAndAssigneeInSameHousehold_whenCreateTask_thenTaskIsSavedWithHouseholdAndRelations() {
        TaskCreateDto dto = createDto(ROOMMATE_EMAIL);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(userService.findApplicationUserByEmail(ROOMMATE_EMAIL)).thenReturn(roommate);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(roommate.getId(), household.getId())).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(99L);
            return task;
        });

        Task result = taskService.create(dto, MAIN_TENANT_EMAIL);

        assertAll(
            () -> assertEquals(99L, result.getId()),
            () -> assertSame(household, result.getHousehold()),
            () -> assertEquals(TEST_TASK_TITLE, result.getTitle()),
            () -> assertEquals(TEST_TASK_DESCRIPTION, result.getDescription()),
            () -> assertEquals(TEST_TASK_DUE_DATE, result.getDueDate()),
            () -> assertEquals(TaskStatus.OPEN, result.getStatus()),
            () -> assertSame(mainTenant, result.getCreatedBy()),
            () -> assertSame(roommate, result.getAssignedTo())
        );

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    public void givenCurrentUserWithoutActiveHousehold_whenCreateTask_thenForbidden() {
        TaskCreateDto dto = createDto(ROOMMATE_EMAIL);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> taskService.create(dto, MAIN_TENANT_EMAIL));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    public void givenAssigneeFromAnotherHousehold_whenCreateTask_thenForbiddenAndTaskIsNotSaved() {
        TaskCreateDto dto = createDto(OUTSIDER_EMAIL);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(userService.findApplicationUserByEmail(OUTSIDER_EMAIL)).thenReturn(outsider);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(outsider.getId(), household.getId())).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> taskService.create(dto, MAIN_TENANT_EMAIL));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    public void givenCurrentUserWithHousehold_whenFindVisibleTasks_thenOnlyHouseholdTasksAreLoaded() {
        Task task = task(1L, household, mainTenant, roommate, TEST_TASK_TITLE);

        when(userService.findApplicationUserByEmail(ROOMMATE_EMAIL)).thenReturn(roommate);
        when(roommateRepository.findByUserIdAndActiveTrue(roommate.getId())).thenReturn(Optional.of(roommateMembership));
        when(taskRepository.findAllByHousehold_IdOrderByDueDateAscCreatedAtDesc(household.getId())).thenReturn(List.of(task));

        List<Task> result = taskService.findVisibleTasks(ROOMMATE_EMAIL, false);

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals(TEST_TASK_TITLE, result.get(0).getTitle()),
            () -> assertEquals(household.getId(), result.get(0).getHousehold().getId())
        );
    }

    @Test
    public void givenCurrentUser_whenFindAssignedToCurrentUser_thenOnlyOwnAssignedHouseholdTasksAreLoaded() {
        Task task = task(1L, household, mainTenant, roommate, TEST_TASK_TITLE);

        when(userService.findApplicationUserByEmail(ROOMMATE_EMAIL)).thenReturn(roommate);
        when(roommateRepository.findByUserIdAndActiveTrue(roommate.getId())).thenReturn(Optional.of(roommateMembership));
        when(taskRepository.findByHousehold_IdAndAssignedTo_IdOrderByDueDateAscCreatedAtDesc(
            household.getId(),
            roommate.getId()
        )).thenReturn(List.of(task));

        List<Task> result = taskService.findAssignedToCurrentUser(ROOMMATE_EMAIL);

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals(ROOMMATE_EMAIL, result.get(0).getAssignedToEmail()),
            () -> assertEquals(household.getId(), result.get(0).getHousehold().getId())
        );
    }

    @Test
    public void givenTaskFromAnotherHousehold_whenFindOne_thenForbidden() {
        Task task = task(1L, otherHousehold, outsider, outsider, "Other WG task");

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(taskRepository.findWithRelationsById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(ForbiddenException.class, () -> taskService.findOne(task.getId(), MAIN_TENANT_EMAIL, false));
    }

    @Test
    public void givenCreator_whenUpdateTaskAndAssigneeInSameHousehold_thenTaskIsUpdated() {
        Task task = task(1L, household, mainTenant, roommate, TEST_TASK_TITLE);
        TaskUpdateDto dto = updateDto(SECOND_ROOMMATE_EMAIL);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(userService.findApplicationUserByEmail(SECOND_ROOMMATE_EMAIL)).thenReturn(secondRoommate);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(taskRepository.findWithRelationsById(task.getId())).thenReturn(Optional.of(task));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(secondRoommate.getId(), household.getId())).thenReturn(true);
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.update(task.getId(), dto, MAIN_TENANT_EMAIL, false);

        assertAll(
            () -> assertEquals(UPDATED_TASK_TITLE, result.getTitle()),
            () -> assertEquals(UPDATED_TASK_DESCRIPTION, result.getDescription()),
            () -> assertEquals(UPDATED_TASK_DUE_DATE, result.getDueDate()),
            () -> assertSame(secondRoommate, result.getAssignedTo())
        );
    }

    @Test
    public void givenUnrelatedRoommate_whenUpdateTask_thenForbidden() {
        Task task = task(1L, household, mainTenant, roommate, TEST_TASK_TITLE);
        TaskUpdateDto dto = updateDto(SECOND_ROOMMATE_EMAIL);

        when(userService.findApplicationUserByEmail(SECOND_ROOMMATE_EMAIL)).thenReturn(secondRoommate);
        when(roommateRepository.findByUserIdAndActiveTrue(secondRoommate.getId())).thenReturn(Optional.of(secondRoommateMembership));
        when(taskRepository.findWithRelationsById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(ForbiddenException.class, () -> taskService.update(task.getId(), dto, SECOND_ROOMMATE_EMAIL, false));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    public void givenAssignedUser_whenUpdateStatus_thenStatusIsUpdated() {
        Task task = task(1L, household, mainTenant, roommate, TEST_TASK_TITLE);
        TaskStatusUpdateDto dto = new TaskStatusUpdateDto();
        dto.setStatus(TaskStatus.DONE);

        when(userService.findApplicationUserByEmail(ROOMMATE_EMAIL)).thenReturn(roommate);
        when(roommateRepository.findByUserIdAndActiveTrue(roommate.getId())).thenReturn(Optional.of(roommateMembership));
        when(taskRepository.findWithRelationsById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.updateStatus(task.getId(), dto, ROOMMATE_EMAIL, false);

        assertEquals(TaskStatus.DONE, result.getStatus());
    }

    @Test
    public void givenUnrelatedRoommate_whenUpdateStatus_thenForbidden() {
        Task task = task(1L, household, mainTenant, roommate, TEST_TASK_TITLE);
        TaskStatusUpdateDto dto = new TaskStatusUpdateDto();
        dto.setStatus(TaskStatus.DONE);

        when(userService.findApplicationUserByEmail(SECOND_ROOMMATE_EMAIL)).thenReturn(secondRoommate);
        when(roommateRepository.findByUserIdAndActiveTrue(secondRoommate.getId())).thenReturn(Optional.of(secondRoommateMembership));
        when(taskRepository.findWithRelationsById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(ForbiddenException.class, () -> taskService.updateStatus(task.getId(), dto, SECOND_ROOMMATE_EMAIL, false));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    public void givenCreator_whenDeleteTask_thenTaskIsDeleted() {
        Task task = task(1L, household, mainTenant, roommate, TEST_TASK_TITLE);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(taskRepository.findWithRelationsById(task.getId())).thenReturn(Optional.of(task));

        taskService.delete(task.getId(), MAIN_TENANT_EMAIL, false);

        verify(taskRepository).delete(task);
    }

    @Test
    public void givenNonExistingTask_whenFindOne_thenNotFound() {
        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(taskRepository.findWithRelationsById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> taskService.findOne(999L, MAIN_TENANT_EMAIL, false));
    }

    private TaskCreateDto createDto(String assignedToEmail) {
        TaskCreateDto dto = new TaskCreateDto();
        dto.setTitle(TEST_TASK_TITLE);
        dto.setDescription(TEST_TASK_DESCRIPTION);
        dto.setDueDate(TEST_TASK_DUE_DATE);
        dto.setAssignedToEmail(assignedToEmail);
        return dto;
    }

    private TaskUpdateDto updateDto(String assignedToEmail) {
        TaskUpdateDto dto = new TaskUpdateDto();
        dto.setTitle(UPDATED_TASK_TITLE);
        dto.setDescription(UPDATED_TASK_DESCRIPTION);
        dto.setDueDate(UPDATED_TASK_DUE_DATE);
        dto.setAssignedToEmail(assignedToEmail);
        return dto;
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

    private Household household(Long id, String name, ApplicationUser createdBy) {
        Household household = new Household();
        household.setId(id);
        household.setName(name);
        household.setAddress("Test address");
        household.setCreatedBy(createdBy);
        return household;
    }

    private Roommate roommate(Long id, ApplicationUser user, Household household, HouseholdRole role) {
        Roommate roommate = new Roommate();
        roommate.setId(id);
        roommate.setUser(user);
        roommate.setHousehold(household);
        roommate.setRole(role);
        roommate.setActive(true);
        return roommate;
    }

    private Task task(Long id, Household household, ApplicationUser creator, ApplicationUser assignee, String title) {
        Task task = new Task();
        task.setId(id);
        task.setHousehold(household);
        task.setTitle(title);
        task.setDescription(TEST_TASK_DESCRIPTION);
        task.setDueDate(LocalDate.now().plusDays(3));
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedBy(creator);
        task.setAssignedTo(assignee);
        return task;
    }
}