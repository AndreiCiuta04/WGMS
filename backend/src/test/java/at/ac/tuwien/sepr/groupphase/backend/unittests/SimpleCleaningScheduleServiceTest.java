package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleActiveUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.CleaningScheduleService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleCleaningScheduleService;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SimpleCleaningScheduleServiceTest {

    private static final String TEST_CLEANING_TITLE = "Clean kitchen schedule";
    private static final String TEST_CLEANING_DESCRIPTION = "Clean surfaces, sink, and floor";
    private static final String UPDATED_CLEANING_TITLE = "Clean bathroom schedule";
    private static final String UPDATED_CLEANING_DESCRIPTION = "Clean sink, mirror, and floor";
    private static final LocalDate TEST_START_DATE = LocalDate.now().plusDays(1);
    private static final LocalDate TEST_END_DATE = LocalDate.now().plusWeeks(4);
    private static final LocalDate UPDATED_START_DATE = LocalDate.now().plusDays(2);
    private static final LocalDate UPDATED_END_DATE = LocalDate.now().plusWeeks(5);

    @Mock
    private CleaningScheduleRepository cleaningScheduleRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private RoommateRepository roommateRepository;

    private CleaningScheduleService cleaningScheduleService;

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
        cleaningScheduleService = new SimpleCleaningScheduleService(cleaningScheduleRepository, taskRepository, userService, roommateRepository);

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
    public void givenCurrentUserWithHouseholdAndAssigneeInSameHousehold_whenCreateSchedule_thenScheduleIsSavedWithHouseholdAndRelations() {
        CleaningScheduleCreateDto dto = createDto(ROOMMATE_EMAIL);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(userService.findApplicationUserByEmail(ROOMMATE_EMAIL)).thenReturn(roommate);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(roommate.getId(), household.getId())).thenReturn(true);
        when(cleaningScheduleRepository.save(any(CleaningSchedule.class))).thenAnswer(invocation -> {
            CleaningSchedule schedule = invocation.getArgument(0);
            schedule.setId(99L);
            return schedule;
        });

        CleaningSchedule result = cleaningScheduleService.create(dto, MAIN_TENANT_EMAIL);

        assertAll(
            () -> assertEquals(99L, result.getId()),
            () -> assertSame(household, result.getHousehold()),
            () -> assertEquals(TEST_CLEANING_TITLE, result.getTitle()),
            () -> assertEquals(TEST_CLEANING_DESCRIPTION, result.getDescription()),
            () -> assertEquals(CleaningFrequency.WEEKLY, result.getFrequency()),
            () -> assertEquals(TEST_START_DATE, result.getStartDate()),
            () -> assertEquals(TEST_END_DATE, result.getEndDate()),
            () -> assertEquals(TEST_START_DATE, result.getNextDueDate()),
            () -> assertSame(mainTenant, result.getCreatedBy()),
            () -> assertSame(roommate, result.getAssignedTo())
        );

        verify(cleaningScheduleRepository).save(any(CleaningSchedule.class));
    }

    @Test
    public void givenAssigneeFromAnotherHousehold_whenCreateSchedule_thenForbiddenAndScheduleIsNotSaved() {
        CleaningScheduleCreateDto dto = createDto(OUTSIDER_EMAIL);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(userService.findApplicationUserByEmail(OUTSIDER_EMAIL)).thenReturn(outsider);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(outsider.getId(), household.getId())).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> cleaningScheduleService.create(dto, MAIN_TENANT_EMAIL));
        verify(cleaningScheduleRepository, never()).save(any(CleaningSchedule.class));
    }

    @Test
    public void givenCurrentUserWithHousehold_whenFindVisibleSchedules_thenOnlyHouseholdSchedulesAreLoaded() {
        CleaningSchedule schedule = schedule(1L, household, mainTenant, roommate, TEST_CLEANING_TITLE);

        when(userService.findApplicationUserByEmail(ROOMMATE_EMAIL)).thenReturn(roommate);
        when(roommateRepository.findByUserIdAndActiveTrue(roommate.getId())).thenReturn(Optional.of(roommateMembership));
        when(cleaningScheduleRepository.findAllByHousehold_IdOrderByNextDueDateAscCreatedAtDesc(household.getId())).thenReturn(List.of(schedule));

        List<CleaningSchedule> result = cleaningScheduleService.findVisibleSchedules(ROOMMATE_EMAIL, false);

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals(TEST_CLEANING_TITLE, result.get(0).getTitle()),
            () -> assertEquals(household.getId(), result.get(0).getHousehold().getId())
        );
    }

    @Test
    public void givenCurrentUserWithHousehold_whenFindActiveSchedules_thenOnlyActiveHouseholdSchedulesAreLoaded() {
        CleaningSchedule schedule = schedule(1L, household, mainTenant, roommate, TEST_CLEANING_TITLE);

        when(userService.findApplicationUserByEmail(ROOMMATE_EMAIL)).thenReturn(roommate);
        when(roommateRepository.findByUserIdAndActiveTrue(roommate.getId())).thenReturn(Optional.of(roommateMembership));
        when(cleaningScheduleRepository.findAllByHousehold_IdAndActiveTrueOrderByNextDueDateAscCreatedAtDesc(household.getId())).thenReturn(List.of(schedule));

        List<CleaningSchedule> result = cleaningScheduleService.findActiveSchedules(ROOMMATE_EMAIL, false);

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals(TEST_CLEANING_TITLE, result.get(0).getTitle())
        );
    }

    @Test
    public void givenScheduleFromAnotherHousehold_whenFindOne_thenForbidden() {
        CleaningSchedule schedule = schedule(1L, otherHousehold, outsider, outsider, "Other WG schedule");

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(cleaningScheduleRepository.findWithRelationsById(schedule.getId())).thenReturn(Optional.of(schedule));

        assertThrows(ForbiddenException.class, () -> cleaningScheduleService.findOne(schedule.getId(), MAIN_TENANT_EMAIL, false));
    }

    @Test
    public void givenCreator_whenUpdateScheduleAndAssigneeInSameHousehold_thenScheduleIsUpdated() {
        CleaningSchedule schedule = schedule(1L, household, mainTenant, roommate, TEST_CLEANING_TITLE);
        CleaningScheduleUpdateDto dto = updateDto(SECOND_ROOMMATE_EMAIL);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(userService.findApplicationUserByEmail(SECOND_ROOMMATE_EMAIL)).thenReturn(secondRoommate);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(cleaningScheduleRepository.findWithRelationsById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(secondRoommate.getId(), household.getId())).thenReturn(true);
        when(cleaningScheduleRepository.save(schedule)).thenReturn(schedule);

        CleaningSchedule result = cleaningScheduleService.update(schedule.getId(), dto, MAIN_TENANT_EMAIL, false);

        assertAll(
            () -> assertEquals(UPDATED_CLEANING_TITLE, result.getTitle()),
            () -> assertEquals(UPDATED_CLEANING_DESCRIPTION, result.getDescription()),
            () -> assertEquals(CleaningFrequency.BIWEEKLY, result.getFrequency()),
            () -> assertEquals(UPDATED_START_DATE, result.getStartDate()),
            () -> assertEquals(UPDATED_END_DATE, result.getEndDate()),
            () -> assertSame(secondRoommate, result.getAssignedTo())
        );
    }

    @Test
    public void givenUnrelatedRoommate_whenUpdateSchedule_thenForbidden() {
        CleaningSchedule schedule = schedule(1L, household, mainTenant, roommate, TEST_CLEANING_TITLE);
        CleaningScheduleUpdateDto dto = updateDto(SECOND_ROOMMATE_EMAIL);

        when(userService.findApplicationUserByEmail(SECOND_ROOMMATE_EMAIL)).thenReturn(secondRoommate);
        when(roommateRepository.findByUserIdAndActiveTrue(secondRoommate.getId())).thenReturn(Optional.of(secondRoommateMembership));
        when(cleaningScheduleRepository.findWithRelationsById(schedule.getId())).thenReturn(Optional.of(schedule));

        assertThrows(ForbiddenException.class, () -> cleaningScheduleService.update(schedule.getId(), dto, SECOND_ROOMMATE_EMAIL, false));
        verify(cleaningScheduleRepository, never()).save(any(CleaningSchedule.class));
    }

    @Test
    public void givenCreator_whenDeactivateSchedule_thenScheduleBecomesInactive() {
        CleaningSchedule schedule = schedule(1L, household, mainTenant, roommate, TEST_CLEANING_TITLE);
        CleaningScheduleActiveUpdateDto dto = new CleaningScheduleActiveUpdateDto();
        dto.setActive(false);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(cleaningScheduleRepository.findWithRelationsById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(cleaningScheduleRepository.save(schedule)).thenReturn(schedule);

        CleaningSchedule result = cleaningScheduleService.updateActive(schedule.getId(), dto, MAIN_TENANT_EMAIL, false);

        assertFalse(result.isActive());
    }

    @Test
    public void givenWeeklyActiveSchedule_whenGenerateTask_thenTaskIsCreatedAndNextDueDateAdvancesByOneWeek() {
        CleaningSchedule schedule = schedule(1L, household, mainTenant, roommate, TEST_CLEANING_TITLE);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(cleaningScheduleRepository.findWithRelationsById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(cleaningScheduleRepository.save(schedule)).thenReturn(schedule);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(88L);
            return task;
        });

        Task result = cleaningScheduleService.generateTask(schedule.getId(), MAIN_TENANT_EMAIL, false);

        assertAll(
            () -> assertEquals(88L, result.getId()),
            () -> assertSame(household, result.getHousehold()),
            () -> assertEquals(TEST_CLEANING_TITLE, result.getTitle()),
            () -> assertEquals(TEST_CLEANING_DESCRIPTION, result.getDescription()),
            () -> assertEquals(TEST_START_DATE, result.getDueDate()),
            () -> assertEquals(TaskStatus.OPEN, result.getStatus()),
            () -> assertSame(mainTenant, result.getCreatedBy()),
            () -> assertSame(roommate, result.getAssignedTo()),
            () -> assertSame(schedule, result.getCleaningSchedule()),
            () -> assertEquals(schedule.getId(), result.getCleaningScheduleId()),
            () -> assertEquals(TEST_START_DATE.plusWeeks(1), schedule.getNextDueDate())
        );

        verify(cleaningScheduleRepository).save(schedule);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    public void givenInactiveSchedule_whenGenerateTask_thenConflictAndNoTaskIsCreated() {
        CleaningSchedule schedule = schedule(1L, household, mainTenant, roommate, TEST_CLEANING_TITLE);
        schedule.setActive(false);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(cleaningScheduleRepository.findWithRelationsById(schedule.getId())).thenReturn(Optional.of(schedule));

        assertThrows(ConflictException.class, () -> cleaningScheduleService.generateTask(schedule.getId(), MAIN_TENANT_EMAIL, false));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    public void givenCreator_whenDeleteSchedule_thenScheduleIsDeleted() {
        CleaningSchedule schedule = schedule(1L, household, mainTenant, roommate, TEST_CLEANING_TITLE);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(cleaningScheduleRepository.findWithRelationsById(schedule.getId())).thenReturn(Optional.of(schedule));

        cleaningScheduleService.delete(schedule.getId(), MAIN_TENANT_EMAIL, false);

        verify(cleaningScheduleRepository).delete(schedule);
    }

    @Test
    public void givenScheduleWithOpenGeneratedTasks_whenDeleteSchedule_thenConflictAndScheduleIsNotDeleted() {
        CleaningSchedule schedule = schedule(1L, household, mainTenant, roommate, TEST_CLEANING_TITLE);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(cleaningScheduleRepository.findWithRelationsById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(taskRepository.existsByCleaningSchedule_IdAndStatusNotIn(
            schedule.getId(),
            List.of(TaskStatus.DONE, TaskStatus.CANCELLED)
        )).thenReturn(true);

        assertThrows(ConflictException.class, () -> cleaningScheduleService.delete(schedule.getId(), MAIN_TENANT_EMAIL, false));

        verify(cleaningScheduleRepository, never()).delete(any(CleaningSchedule.class));
    }

    @Test
    public void givenNonExistingSchedule_whenFindOne_thenNotFound() {
        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(cleaningScheduleRepository.findWithRelationsById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cleaningScheduleService.findOne(999L, MAIN_TENANT_EMAIL, false));
    }

    private CleaningScheduleCreateDto createDto(String assignedToEmail) {
        CleaningScheduleCreateDto dto = new CleaningScheduleCreateDto();
        dto.setTitle(TEST_CLEANING_TITLE);
        dto.setDescription(TEST_CLEANING_DESCRIPTION);
        dto.setFrequency(CleaningFrequency.WEEKLY);
        dto.setStartDate(TEST_START_DATE);
        dto.setEndDate(TEST_END_DATE);
        dto.setAssignedToEmail(assignedToEmail);
        return dto;
    }

    private CleaningScheduleUpdateDto updateDto(String assignedToEmail) {
        CleaningScheduleUpdateDto dto = new CleaningScheduleUpdateDto();
        dto.setTitle(UPDATED_CLEANING_TITLE);
        dto.setDescription(UPDATED_CLEANING_DESCRIPTION);
        dto.setFrequency(CleaningFrequency.BIWEEKLY);
        dto.setStartDate(UPDATED_START_DATE);
        dto.setEndDate(UPDATED_END_DATE);
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

    private CleaningSchedule schedule(Long id, Household household, ApplicationUser creator, ApplicationUser assignee, String title) {
        CleaningSchedule schedule = new CleaningSchedule();
        schedule.setId(id);
        schedule.setHousehold(household);
        schedule.setTitle(title);
        schedule.setDescription(TEST_CLEANING_DESCRIPTION);
        schedule.setFrequency(CleaningFrequency.WEEKLY);
        schedule.setStartDate(TEST_START_DATE);
        schedule.setEndDate(TEST_END_DATE);
        schedule.setNextDueDate(TEST_START_DATE);
        schedule.setActive(true);
        schedule.setCreatedBy(creator);
        schedule.setAssignedTo(assignee);
        return schedule;
    }
}