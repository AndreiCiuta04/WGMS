package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.FairChoreOverdueReminderDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.FairChoreRecommendationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.FairChoreRoommateStatsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationType;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.FairChoreBalancingService;
import at.ac.tuwien.sepr.groupphase.backend.service.NotificationService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.FairChoreBalancingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.MAIN_TENANT_EMAIL;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.ROOMMATE_EMAIL;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.SECOND_ROOMMATE_EMAIL;
import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.TEST_HOUSEHOLD_NAME;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FairChoreBalancingServiceTest {

    private static final String BUCKET_A = "NO_OVERDUE_AVAILABLE";
    private static final String BUCKET_B = "NO_OVERDUE_OVERLOADED";
    private static final String BUCKET_C = "HAS_OVERDUE";

    @Mock
    private UserService userService;

    @Mock
    private RoommateRepository roommateRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private NotificationService notificationService;

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
        service = new FairChoreBalancingServiceImpl(userService, roommateRepository, taskRepository, notificationService);

        mainTenant = user(1L, MAIN_TENANT_EMAIL, "Main", "Tenant");
        roommate = user(2L, ROOMMATE_EMAIL, "Normal", "Roommate");
        secondRoommate = user(3L, SECOND_ROOMMATE_EMAIL, "Second", "Roommate");
        inactiveUser = user(4L, "inactive@test.com", "Inactive", "User");
        household = household(1L, TEST_HOUSEHOLD_NAME, mainTenant);
        otherHousehold = household(2L, "Other WG", inactiveUser);
        mainTenantMembership = membership(1L, mainTenant, household, true);
        mainTenantMembership.setRole(HouseholdRole.MAIN_TENANT);
        roommateMembership = membership(2L, roommate, household, true);
        secondRoommateMembership = membership(3L, secondRoommate, household, true);
    }

    @Test
    public void givenNoHistory_whenRecommend_thenAllActiveRoommatesAreBucketATiedCandidates() {
        givenCurrentHouseholdTasks(List.of());

        FairChoreRecommendationDto recommendation = service.recommendAssignee(MAIN_TENANT_EMAIL, null, "Vacuum", LocalDate.now(), null);

        assertAll(
            () -> assertEquals(30, recommendation.historyDays()),
            () -> assertEquals("HISTORY_BUCKET_SCORE_SEEDED_TIE", recommendation.algorithm()),
            () -> assertEquals(3, recommendation.stats().size()),
            () -> assertTrue(recommendation.stats().stream().allMatch(stats -> BUCKET_A.equals(stats.fairnessBucket()))),
            () -> assertTrue(recommendation.stats().stream().allMatch(FairChoreRoommateStatsDto::candidate)),
            () -> assertTrue(recommendation.stats().stream().anyMatch(FairChoreRoommateStatsDto::recommended)),
            () -> assertEquals(0.0d, stat(recommendation, MAIN_TENANT_EMAIL).fairnessScore())
        );
    }

    @Test
    public void givenCompletedOpenAndOverdueTasks_whenRecommend_thenScoresUseExpectedWeightsAndOverdueIsNotOpen() {
        givenCurrentHouseholdTasks(List.of(
            task(household, mainTenant, TaskStatus.DONE, LocalDate.now().minusDays(1), LocalDateTime.now().minusDays(1)),
            task(household, mainTenant, TaskStatus.OPEN, LocalDate.now().plusDays(1), LocalDateTime.now()),
            task(household, roommate, TaskStatus.IN_PROGRESS, LocalDate.now().minusDays(1), LocalDateTime.now()),
            task(household, roommate, TaskStatus.OPEN, LocalDate.now().plusDays(2), LocalDateTime.now()),
            task(otherHousehold, mainTenant, TaskStatus.DONE, LocalDate.now().minusDays(1), LocalDateTime.now())
        ));

        FairChoreRecommendationDto recommendation = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Kitchen", LocalDate.now().plusDays(2), null);

        FairChoreRoommateStatsDto mainStats = stat(recommendation, MAIN_TENANT_EMAIL);
        FairChoreRoommateStatsDto roommateStats = stat(recommendation, ROOMMATE_EMAIL);
        FairChoreRoommateStatsDto secondStats = stat(recommendation, SECOND_ROOMMATE_EMAIL);

        assertAll(
            () -> assertEquals(1, mainStats.completedTasks()),
            () -> assertEquals(1, mainStats.openTasks()),
            () -> assertEquals(0, mainStats.overdueTasks()),
            () -> assertEquals(BUCKET_A, mainStats.fairnessBucket()),
            () -> assertEquals(1.75d, mainStats.fairnessScore()),
            () -> assertEquals(0, roommateStats.completedTasks()),
            () -> assertEquals(1, roommateStats.openTasks()),
            () -> assertEquals(1, roommateStats.overdueTasks()),
            () -> assertEquals(BUCKET_C, roommateStats.fairnessBucket()),
            () -> assertEquals(2.75d, roommateStats.fairnessScore()),
            () -> assertEquals(0.0d, secondStats.fairnessScore()),
            () -> assertEquals(BUCKET_A, secondStats.fairnessBucket()),
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
            () -> assertEquals(0.75d, stat(recommendation, ROOMMATE_EMAIL).fairnessScore()),
            () -> assertEquals(BUCKET_A, stat(recommendation, ROOMMATE_EMAIL).fairnessBucket())
        );
    }

    @Test
    public void givenBucketAAndBucketBRoommates_whenRecommend_thenBucketAIsPreferredEvenWithHigherScore() {
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(doneTasks(mainTenant, 5));
        tasks.addAll(openTasks(roommate, 3));
        tasks.addAll(openTasks(secondRoommate, 4));
        givenCurrentHouseholdTasks(tasks);

        FairChoreRecommendationDto recommendation = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Bucket A", LocalDate.now(), null);

        assertAll(
            () -> assertEquals(BUCKET_A, stat(recommendation, MAIN_TENANT_EMAIL).fairnessBucket()),
            () -> assertEquals(5.0d, stat(recommendation, MAIN_TENANT_EMAIL).fairnessScore()),
            () -> assertEquals(BUCKET_B, stat(recommendation, ROOMMATE_EMAIL).fairnessBucket()),
            () -> assertEquals(2.25d, stat(recommendation, ROOMMATE_EMAIL).fairnessScore()),
            () -> assertTrue(stat(recommendation, MAIN_TENANT_EMAIL).candidate()),
            () -> assertTrue(stat(recommendation, MAIN_TENANT_EMAIL).recommended()),
            () -> assertFalse(stat(recommendation, ROOMMATE_EMAIL).candidate()),
            () -> assertFalse(stat(recommendation, SECOND_ROOMMATE_EMAIL).candidate())
        );
    }

    @Test
    public void givenBucketBAndBucketCRoommates_whenRecommend_thenBucketBIsPreferredEvenIfBucketCHasLowerScore() {
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(openTasks(mainTenant, 3));
        tasks.add(task(household, roommate, TaskStatus.OPEN, LocalDate.now().minusDays(1), LocalDateTime.now()));
        tasks.addAll(openTasks(secondRoommate, 4));
        givenCurrentHouseholdTasks(tasks);

        FairChoreRecommendationDto recommendation = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Bucket B", LocalDate.now(), null);

        assertAll(
            () -> assertEquals(BUCKET_B, stat(recommendation, MAIN_TENANT_EMAIL).fairnessBucket()),
            () -> assertEquals(2.25d, stat(recommendation, MAIN_TENANT_EMAIL).fairnessScore()),
            () -> assertEquals(BUCKET_C, stat(recommendation, ROOMMATE_EMAIL).fairnessBucket()),
            () -> assertEquals(2.0d, stat(recommendation, ROOMMATE_EMAIL).fairnessScore()),
            () -> assertEquals(BUCKET_B, stat(recommendation, SECOND_ROOMMATE_EMAIL).fairnessBucket()),
            () -> assertTrue(stat(recommendation, MAIN_TENANT_EMAIL).candidate()),
            () -> assertTrue(stat(recommendation, MAIN_TENANT_EMAIL).recommended()),
            () -> assertFalse(stat(recommendation, ROOMMATE_EMAIL).candidate()),
            () -> assertFalse(stat(recommendation, SECOND_ROOMMATE_EMAIL).candidate())
        );
    }

    @Test
    public void givenExactTie_whenRecommend_thenOnlyMinimumScoreTieGroupIsCandidateAndSameSeedIsStable() {
        givenCurrentHouseholdTasks(openTasks(secondRoommate, 1));

        FairChoreRecommendationDto first = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Tie", LocalDate.now().plusDays(2), null);
        FairChoreRecommendationDto second = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Tie", LocalDate.now().plusDays(2), null);

        Set<String> tiedCandidates = Set.of(MAIN_TENANT_EMAIL, ROOMMATE_EMAIL);
        FairChoreRoommateStatsDto recommended = first.stats().stream()
            .filter(FairChoreRoommateStatsDto::recommended)
            .findFirst()
            .orElseThrow();

        assertAll(
            () -> assertEquals(first.recommendedUserId(), second.recommendedUserId()),
            () -> assertTrue(tiedCandidates.contains(recommended.email())),
            () -> assertTrue(stat(first, MAIN_TENANT_EMAIL).candidate()),
            () -> assertTrue(stat(first, ROOMMATE_EMAIL).candidate()),
            () -> assertFalse(stat(first, SECOND_ROOMMATE_EMAIL).candidate()),
            () -> assertEquals(0.75d, stat(first, SECOND_ROOMMATE_EMAIL).fairnessScore())
        );
    }

    @Test
    public void givenDifferentSeedContextForExactTie_whenRecommend_thenRecommendationNeverLeavesTieGroup() {
        givenCurrentHouseholdTasks(openTasks(secondRoommate, 1));

        FairChoreRecommendationDto first = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Tie A", LocalDate.now().plusDays(2), null);
        FairChoreRecommendationDto second = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Tie B", LocalDate.now().plusDays(2), null);

        Set<String> tiedCandidates = Set.of(MAIN_TENANT_EMAIL, ROOMMATE_EMAIL);

        assertAll(
            () -> assertTrue(tiedCandidates.contains(first.recommendedEmail())),
            () -> assertTrue(tiedCandidates.contains(second.recommendedEmail())),
            () -> assertFalse(stat(first, SECOND_ROOMMATE_EMAIL).candidate()),
            () -> assertFalse(stat(second, SECOND_ROOMMATE_EMAIL).candidate())
        );
    }

    @Test
    public void givenExactTie_whenSeedContextChanges_thenDifferentTiedRoommatesCanBeSelected() {
        givenCurrentHouseholdTasks(openTasks(secondRoommate, 1));

        Set<String> tiedCandidates = Set.of(MAIN_TENANT_EMAIL, ROOMMATE_EMAIL);
        Set<String> recommendations = new java.util.HashSet<>();
        for (int i = 0; i < 40; i++) {
            FairChoreRecommendationDto recommendation = service.recommendAssignee(
                MAIN_TENANT_EMAIL,
                30,
                "Seed context " + i,
                LocalDate.now().plusDays(2),
                null
            );
            recommendations.add(recommendation.recommendedEmail());
        }

        assertAll(
            () -> assertTrue(tiedCandidates.containsAll(recommendations)),
            () -> assertEquals(2, recommendations.size())
        );
    }

    @Test
    public void givenNonTiedLowestScore_whenRecommend_thenRandomnessDoesNotAffectSelection() {
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(openTasks(roommate, 1));
        tasks.addAll(doneTasks(secondRoommate, 1));
        givenCurrentHouseholdTasks(tasks);

        FairChoreRecommendationDto recommendation = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Non tie", LocalDate.now(), null);

        assertAll(
            () -> assertTrue(stat(recommendation, MAIN_TENANT_EMAIL).candidate()),
            () -> assertTrue(stat(recommendation, MAIN_TENANT_EMAIL).recommended()),
            () -> assertFalse(stat(recommendation, ROOMMATE_EMAIL).candidate()),
            () -> assertFalse(stat(recommendation, SECOND_ROOMMATE_EMAIL).candidate()),
            () -> assertEquals(MAIN_TENANT_EMAIL, recommendation.recommendedEmail())
        );
    }

    @Test
    public void givenInactiveAnonymizedAndOtherHouseholdRoommates_whenRecommend_thenTheyAreIgnored() {
        ApplicationUser anonymized = user(5L, "anon@test.com", "Anon", "User");
        anonymized.setAnonymized(true);
        Roommate inactiveMembership = membership(4L, inactiveUser, household, false);
        Roommate anonymizedMembership = membership(5L, anonymized, household, true);
        Roommate otherHouseholdMembership = membership(6L, inactiveUser, otherHousehold, true);

        givenCurrentHouseholdData(
            List.of(mainTenantMembership, roommateMembership, secondRoommateMembership, anonymizedMembership, otherHouseholdMembership),
            List.of(task(household, inactiveUser, TaskStatus.OPEN, LocalDate.now(), LocalDateTime.now()))
        );

        FairChoreRecommendationDto recommendation = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Windows", LocalDate.now(), null);

        assertAll(
            () -> assertEquals(3, recommendation.stats().size()),
            () -> assertFalse(recommendation.stats().stream().anyMatch(stats -> "anon@test.com".equals(stats.email()))),
            () -> assertFalse(recommendation.stats().stream().anyMatch(stats -> "inactive@test.com".equals(stats.email()))),
            () -> assertFalse(inactiveMembership.isActive())
        );
    }

    @Test
    public void givenOtherHouseholdTaskAssignedToCurrentUser_whenRecommend_thenTaskDoesNotAffectScore() {
        givenCurrentHouseholdTasks(List.of(
            task(otherHousehold, mainTenant, TaskStatus.DONE, LocalDate.now().minusDays(1), LocalDateTime.now())
        ));

        FairChoreRecommendationDto recommendation = service.recommendAssignee(MAIN_TENANT_EMAIL, 30, "Scoped", LocalDate.now(), null);

        assertEquals(0, stat(recommendation, MAIN_TENANT_EMAIL).completedTasks());
    }

    @Test
    public void givenInvalidHistoryDays_whenRecommend_thenValidationException() {
        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));

        assertThrows(ValidationException.class, () -> service.recommendAssignee(MAIN_TENANT_EMAIL, 0, "Kitchen", LocalDate.now(), null));
        assertThrows(ValidationException.class, () -> service.recommendAssignee(MAIN_TENANT_EMAIL, 366, "Kitchen", LocalDate.now(), null));
    }

    @Test
    public void givenMainTenantAndFiveOverdueTasks_whenSendOverdueReminder_thenNotificationIsTriggered() {
        givenReminderActor(mainTenant, mainTenantMembership);
        givenReminderTarget(roommateMembership, 5L);

        FairChoreOverdueReminderDto reminder = service.sendOverdueReminder(MAIN_TENANT_EMAIL, roommateMembership.getId());

        assertAll(
            () -> assertEquals(roommateMembership.getId(), reminder.targetRoommateId()),
            () -> assertEquals("Normal Roommate", reminder.targetDisplayName()),
            () -> assertEquals(5L, reminder.overdueTasks()),
            () -> assertTrue(reminder.message().contains("Reminder sent"))
        );
        verify(notificationService).triggerNotification(
            eq(roommate),
            contains("5 overdue household tasks"),
            eq(NotificationType.OVERDUE_TASK_REMINDER),
            eq(roommateMembership.getId())
        );
    }

    @Test
    public void givenMainTenantAndMoreThanFiveOverdueTasks_whenSendOverdueReminder_thenNotificationIsTriggered() {
        givenReminderActor(mainTenant, mainTenantMembership);
        givenReminderTarget(roommateMembership, 6L);

        FairChoreOverdueReminderDto reminder = service.sendOverdueReminder(MAIN_TENANT_EMAIL, roommateMembership.getId());

        assertEquals(6L, reminder.overdueTasks());
        verify(notificationService).triggerNotification(
            eq(roommate),
            contains("6 overdue household tasks"),
            eq(NotificationType.OVERDUE_TASK_REMINDER),
            eq(roommateMembership.getId())
        );
    }

    @Test
    public void givenMainTenantAndOnlyFourOverdueTasks_whenSendOverdueReminder_thenValidationException() {
        givenReminderActor(mainTenant, mainTenantMembership);
        givenReminderTarget(roommateMembership, 4L);

        assertThrows(ValidationException.class, () -> service.sendOverdueReminder(MAIN_TENANT_EMAIL, roommateMembership.getId()));
        verify(notificationService, never()).triggerNotification(any(), any(), any(), any());
    }

    @Test
    public void givenNormalRoommate_whenSendOverdueReminder_thenForbiddenException() {
        givenReminderActor(roommate, roommateMembership);

        assertThrows(ForbiddenException.class, () -> service.sendOverdueReminder(ROOMMATE_EMAIL, secondRoommateMembership.getId()));
        verify(notificationService, never()).triggerNotification(any(), any(), any(), any());
    }

    @Test
    public void givenTargetFromAnotherHousehold_whenSendOverdueReminder_thenForbiddenException() {
        Roommate otherHouseholdMembership = membership(6L, inactiveUser, otherHousehold, true);
        givenReminderActor(mainTenant, mainTenantMembership);
        when(roommateRepository.findByIdAndHouseholdIdAndActiveTrue(otherHouseholdMembership.getId(), household.getId()))
            .thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> service.sendOverdueReminder(MAIN_TENANT_EMAIL, otherHouseholdMembership.getId()));
        verify(notificationService, never()).triggerNotification(any(), any(), any(), any());
    }

    @Test
    public void givenInactiveTargetRoommate_whenSendOverdueReminder_thenForbiddenException() {
        Roommate inactiveMembership = membership(4L, inactiveUser, household, false);
        givenReminderActor(mainTenant, mainTenantMembership);
        when(roommateRepository.findByIdAndHouseholdIdAndActiveTrue(inactiveMembership.getId(), household.getId()))
            .thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> service.sendOverdueReminder(MAIN_TENANT_EMAIL, inactiveMembership.getId()));
        verify(notificationService, never()).triggerNotification(any(), any(), any(), any());
    }

    @Test
    public void givenAnonymizedTargetRoommate_whenSendOverdueReminder_thenForbiddenException() {
        ApplicationUser anonymized = user(5L, "anon@test.com", "Anon", "User");
        anonymized.setAnonymized(true);
        Roommate anonymizedMembership = membership(5L, anonymized, household, true);
        givenReminderActor(mainTenant, mainTenantMembership);
        when(roommateRepository.findByIdAndHouseholdIdAndActiveTrue(anonymizedMembership.getId(), household.getId()))
            .thenReturn(Optional.of(anonymizedMembership));

        assertThrows(ForbiddenException.class, () -> service.sendOverdueReminder(MAIN_TENANT_EMAIL, anonymizedMembership.getId()));
        verify(notificationService, never()).triggerNotification(any(), any(), any(), any());
    }

    @Test
    public void givenOnlyFourCurrentHouseholdOverdueTasks_whenSendOverdueReminder_thenOtherHouseholdTasksDoNotCount() {
        givenReminderActor(mainTenant, mainTenantMembership);
        givenReminderTarget(roommateMembership, 4L);

        assertThrows(ValidationException.class, () -> service.sendOverdueReminder(MAIN_TENANT_EMAIL, roommateMembership.getId()));
        verify(taskRepository).countByHousehold_IdAndAssignedTo_IdAndStatusInAndDueDateBefore(
            eq(household.getId()),
            eq(roommate.getId()),
            eq(List.of(TaskStatus.OPEN, TaskStatus.IN_PROGRESS)),
            eq(LocalDate.now())
        );
        verify(notificationService, never()).triggerNotification(any(), any(), any(), any());
    }

    private void givenCurrentHouseholdTasks(List<Task> tasks) {
        givenCurrentHouseholdData(List.of(mainTenantMembership, roommateMembership, secondRoommateMembership), tasks);
    }

    private void givenCurrentHouseholdData(List<Roommate> memberships, List<Task> tasks) {
        when(userService.findApplicationUserByEmail(MAIN_TENANT_EMAIL)).thenReturn(mainTenant);
        when(roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId())).thenReturn(Optional.of(mainTenantMembership));
        when(roommateRepository.findAllByHousehold_IdAndActiveTrue(household.getId())).thenReturn(memberships);
        when(taskRepository.findFairBalancingTasksByHouseholdId(
            eq(household.getId()),
            eq(List.of(TaskStatus.OPEN, TaskStatus.IN_PROGRESS)),
            eq(TaskStatus.DONE),
            any(LocalDateTime.class)
        )).thenReturn(tasks);
    }

    private void givenReminderActor(ApplicationUser user, Roommate membership) {
        when(userService.findApplicationUserByEmail(user.getEmail())).thenReturn(user);
        when(roommateRepository.findByUserIdAndActiveTrue(user.getId())).thenReturn(Optional.of(membership));
    }

    private void givenReminderTarget(Roommate targetMembership, long overdueTasks) {
        when(roommateRepository.findByIdAndHouseholdIdAndActiveTrue(targetMembership.getId(), household.getId()))
            .thenReturn(Optional.of(targetMembership));
        when(taskRepository.countByHousehold_IdAndAssignedTo_IdAndStatusInAndDueDateBefore(
            eq(household.getId()),
            eq(targetMembership.getUser().getId()),
            eq(List.of(TaskStatus.OPEN, TaskStatus.IN_PROGRESS)),
            eq(LocalDate.now())
        )).thenReturn(overdueTasks);
    }

    private FairChoreRoommateStatsDto stat(FairChoreRecommendationDto recommendation, String email) {
        return recommendation.stats().stream()
            .filter(stats -> email.equals(stats.email()))
            .findFirst()
            .orElseThrow();
    }

    private List<Task> doneTasks(ApplicationUser assignedTo, int count) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tasks.add(task(household, assignedTo, TaskStatus.DONE, LocalDate.now().minusDays(i + 1), LocalDateTime.now().minusDays(i + 1)));
        }
        return tasks;
    }

    private List<Task> openTasks(ApplicationUser assignedTo, int count) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tasks.add(task(household, assignedTo, TaskStatus.OPEN, LocalDate.now().plusDays(i + 1), LocalDateTime.now()));
        }
        return tasks;
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
