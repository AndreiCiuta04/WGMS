package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.FairChoreRecommendationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.FairChoreRoommateStatsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.FairChoreBalancingService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.FairChoreBalancingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.MAIN_TENANT_EMAIL;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.ROOMMATE_EMAIL;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.SECOND_ROOMMATE_EMAIL;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.TEST_HOUSEHOLD_NAME;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FairChoreBalancingServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private RoommateRepository roommateRepository;

    @Mock
    private TaskRepository taskRepository;

    private FairChoreBalancingService service;

    private ApplicationUser mainTenant;
    private ApplicationUser roommate;
    private ApplicationUser secondRoommate;
    private ApplicationUser inactiveUser;
    private Household household;
    private Household otherHousehold;
    private Roommate mainTenantMembership;
    private Roommate roommateMembership;
    private Roommate secondRoommateMembership;

    @BeforeEach
    public void setUp() {
        service = new FairChoreBalancingServiceImpl(userService, roommateRepository, taskRepository);

        mainTenant = user(1L, MAIN_TENANT_EMAIL, "Main", "Tenant");
        roommate = user(2L, ROOMMATE_EMAIL, "Normal", "Roommate");
        secondRoommate = user(3L, SECOND_ROOMMATE_EMAIL, "Second", "Roommate");
        inactiveUser = user(4L, "inactive@test.com", "Inactive", "User");
        household = household(1L, TEST_HOUSEHOLD_NAME, mainTenant);
        otherHousehold = household(2L, "Other WG", inactiveUser);
        mainTenantMembership = membership(1L, mainTenant, household, true);
        roommateMembership = membership(2L, roommate, household, true);
        secondRoommateMembership = membership(3L, secondRoommate, household, true);
    }

    @Test
    public void givenNoHistory_whenRecommend_thenAllActiveRoommatesAreCandidatesAndDefaultHistoryDaysIsThirty() {
        givenCurrentHouseholdTasks(List.of());

        FairChoreRecommendationDto recommendation = service.recommendAssignee(MAIN_TENANT_EMAIL, null, "Vacuum", LocalDate.now(), null);

        assertAll(
            () -> assertEquals(30, recommendation.historyDays()),
            () -> assertEquals(3, recommendation.stats().size()),
            () -> assertTrue(recommendation.stats().stream().allMatch(FairChoreRoommateStatsDto::candidate)),
            () -> assertTrue(recommendation.stats().stream().anyMatch(FairChoreRoommateStatsDto::recommended)),
            () -> assertEquals(0.0d, stat(recommendation, MAIN_TENANT_EMAIL).fairnessScore())
        );
    }

    @Test
    public void givenCompletedOpenAndOverdueTasks_whenRecommend_thenScoresUseWeightsAndOverdueIsNotOpen() {
        givenCurrentHouseholdTasks(List.of(
            task(household, mainTenant, TaskStatus.DONE, LocalDate.now().minusDays(1), LocalDateTime.now().minusDays(1)),
            task(household, mainTenant, TaskStatus.OPEN, LocalDate.now().plusDays(1), LocalDateTime.now()),
            task(household, roommate, TaskStatus.IN_PROGRESS, LocalDate.now().minusDays(1), LocalDateTime.now()),
            task(household, roommate, TaskStatus.OPEN, LocalDate.now().plusDays(2), LocalDateTime.now()),
            task(otherHousehold, inactiveUser, TaskStatus.DONE, LocalDate.now().minusDays(1), LocalDateTime.now())
        ));

        FairChoreRecommendationDto recommendation = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Kitchen", LocalDate.now().plusDays(2), null);

        FairChoreRoommateStatsDto mainStats = stat(recommendation, MAIN_TENANT_EMAIL);
        FairChoreRoommateStatsDto roommateStats = stat(recommendation, ROOMMATE_EMAIL);
        FairChoreRoommateStatsDto secondStats = stat(recommendation, SECOND_ROOMMATE_EMAIL);

        assertAll(
            () -> assertEquals(1, mainStats.completedTasks()),
            () -> assertEquals(1, mainStats.openTasks()),
            () -> assertEquals(0, mainStats.overdueTasks()),
            () -> assertEquals(1.5d, mainStats.fairnessScore()),
            () -> assertEquals(0, roommateStats.completedTasks()),
            () -> assertEquals(1, roommateStats.openTasks()),
            () -> assertEquals(1, roommateStats.overdueTasks()),
            () -> assertEquals(1.5d, roommateStats.fairnessScore()),
            () -> assertEquals(0.0d, secondStats.fairnessScore()),
            () -> assertTrue(secondStats.candidate()),
            () -> assertFalse(mainStats.candidate()),
            () -> assertFalse(roommateStats.candidate())
        );
    }

    @Test
    public void givenDoneTaskOutsideWindowAndUndoneOpenTask_whenRecommend_thenOnlyCurrentStateCounts() {
        givenCurrentHouseholdTasks(List.of(
            task(household, mainTenant, TaskStatus.DONE, LocalDate.now().minusDays(20), LocalDateTime.now().minusDays(40)),
            task(household, roommate, TaskStatus.OPEN, LocalDate.now().plusDays(1), LocalDateTime.now())
        ));

        FairChoreRecommendationDto recommendation = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Hallway", LocalDate.now().plusDays(1), null);

        assertAll(
            () -> assertEquals(0, stat(recommendation, MAIN_TENANT_EMAIL).completedTasks()),
            () -> assertEquals(1, stat(recommendation, ROOMMATE_EMAIL).openTasks()),
            () -> assertEquals(0.5d, stat(recommendation, ROOMMATE_EMAIL).fairnessScore())
        );
    }

    @Test
    public void givenInactiveAndAnonymizedRoommates_whenRecommend_thenTheyAreIgnored() {
        ApplicationUser anonymized = user(5L, "anon@test.com", "Anon", "User");
        anonymized.setAnonymized(true);
        Roommate inactiveMembership = membership(4L, inactiveUser, household, false);
        Roommate anonymizedMembership = membership(5L, anonymized, household, true);

        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(roommateRepository.findAllByHousehold_IdAndActiveTrue(household.getId()))
            .thenReturn(List.of(mainTenantMembership, roommateMembership, secondRoommateMembership, anonymizedMembership));
        when(taskRepository.findAllByHousehold_Id(household.getId()))
            .thenReturn(List.of(task(household, inactiveUser, TaskStatus.OPEN, LocalDate.now(), LocalDateTime.now())));

        FairChoreRecommendationDto recommendation = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Windows", LocalDate.now(), null);

        assertAll(
            () -> assertEquals(3, recommendation.stats().size()),
            () -> assertFalse(recommendation.stats().stream().anyMatch(stats -> "anon@test.com".equals(stats.email()))),
            () -> assertFalse(inactiveMembership.isActive())
        );
    }

    @Test
    public void givenSameContext_whenRecommendTwice_thenRecommendationIsStable() {
        givenCurrentHouseholdTasks(List.of());

        FairChoreRecommendationDto first = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Kitchen", LocalDate.now().plusDays(2), null);
        FairChoreRecommendationDto second = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Kitchen", LocalDate.now().plusDays(2), null);

        assertEquals(first.recommendedUserId(), second.recommendedUserId());
    }

    @Test
    public void givenDifferentContext_whenRecommend_thenRecommendedRoommateStillComesFromCandidatePool() {
        givenCurrentHouseholdTasks(List.of());

        FairChoreRecommendationDto recommendation = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Bathroom", LocalDate.now().plusDays(3), null);

        FairChoreRoommateStatsDto recommended = recommendation.stats().stream()
            .filter(FairChoreRoommateStatsDto::recommended)
            .findFirst()
            .orElseThrow();
        assertTrue(recommended.candidate());
    }

    @Test
    public void givenInvalidHistoryDays_whenRecommend_thenValidationException() {
        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));

        assertThrows(ValidationException.class, () -> service.recommendAssignee(MAIN_TENANT_EMAIL, 0, "Kitchen", LocalDate.now(), null));
    }

    private void givenCurrentHouseholdTasks(List<Task> tasks) {
        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(roommateRepository.findAllByHousehold_IdAndActiveTrue(household.getId()))
            .thenReturn(List.of(mainTenantMembership, roommateMembership, secondRoommateMembership));
        when(taskRepository.findAllByHousehold_Id(household.getId())).thenReturn(tasks);
    }

    private FairChoreRoommateStatsDto stat(FairChoreRecommendationDto recommendation, String email) {
        return recommendation.stats().stream()
            .filter(stats -> email.equals(stats.email()))
            .findFirst()
            .orElseThrow();
    }

    private ApplicationUser user(Long id, String email, String firstName, String lastName) {
        ApplicationUser user = new ApplicationUser();
        user.setId(id);
        user.setEmail(email);
        user.setPassword("encoded-password-for-test");
        user.setFirstName(firstName);
        user.setLastName(lastName);
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

    private Roommate membership(Long id, ApplicationUser user, Household household, boolean active) {
        Roommate roommate = new Roommate();
        roommate.setId(id);
        roommate.setUser(user);
        roommate.setHousehold(household);
        roommate.setRole(HouseholdRole.ROOMMATE);
        roommate.setActive(active);
        return roommate;
    }

    private Task task(Household household, ApplicationUser assignedTo, TaskStatus status, LocalDate dueDate, LocalDateTime updatedAt) {
        Task task = new Task();
        task.setHousehold(household);
        task.setTitle("Test task");
        task.setDescription("Test description");
        task.setDueDate(dueDate);
        task.setStatus(status);
        task.setCreatedBy(mainTenant);
        task.setAssignedTo(assignedTo);
        task.setUpdatedAt(updatedAt);
        return task;
    }
}
