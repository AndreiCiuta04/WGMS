package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class CleaningScheduleRepositoryTest implements TestData {

    private static final String TEST_CLEANING_TITLE = "Clean kitchen schedule";
    private static final String TEST_CLEANING_DESCRIPTION = "Clean surfaces, sink, and floor";
    private static final LocalDate TEST_START_DATE = LocalDate.now().plusDays(1);
    private static final LocalDate TEST_END_DATE = LocalDate.now().plusWeeks(4);

    @Autowired
    private CleaningScheduleRepository cleaningScheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private RoommateRepository roommateRepository;

    @Test
    public void givenValidCleaningSchedule_whenSave_thenRelationsAndDefaultsArePersisted() {
        TestSetup setup = createSetup();

        CleaningSchedule schedule = createSchedule(setup.household, setup.mainTenant, setup.roommate, TEST_CLEANING_TITLE);
        CleaningSchedule savedSchedule = cleaningScheduleRepository.saveAndFlush(schedule);

        assertAll(
            () -> assertNotNull(savedSchedule.getId()),
            () -> assertNotNull(savedSchedule.getCreatedAt()),
            () -> assertNotNull(savedSchedule.getUpdatedAt()),
            () -> assertEquals(setup.household.getId(), savedSchedule.getHousehold().getId()),
            () -> assertEquals(setup.mainTenant.getId(), savedSchedule.getCreatedBy().getId()),
            () -> assertEquals(setup.roommate.getId(), savedSchedule.getAssignedTo().getId()),
            () -> assertEquals(CleaningFrequency.WEEKLY, savedSchedule.getFrequency()),
            () -> assertEquals(TEST_START_DATE, savedSchedule.getStartDate()),
            () -> assertEquals(TEST_START_DATE, savedSchedule.getNextDueDate()),
            () -> assertEquals(TEST_END_DATE, savedSchedule.getEndDate())
        );
    }

    @Test
    public void givenSchedulesInDifferentHouseholds_whenFindAllByHouseholdId_thenOnlyOwnHouseholdSchedulesAreReturned() {
        TestSetup setup = createSetup();

        cleaningScheduleRepository.save(createSchedule(setup.household, setup.mainTenant, setup.roommate, "Own WG schedule"));
        cleaningScheduleRepository.save(createSchedule(setup.otherHousehold, setup.outsider, setup.outsider, "Other WG schedule"));
        cleaningScheduleRepository.flush();

        List<CleaningSchedule> result = cleaningScheduleRepository.findAllByHousehold_IdOrderByNextDueDateAscCreatedAtDesc(setup.household.getId());

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals("Own WG schedule", result.get(0).getTitle()),
            () -> assertEquals(setup.household.getId(), result.get(0).getHousehold().getId())
        );
    }

    @Test
    public void givenActiveAndInactiveSchedules_whenFindActiveByHouseholdId_thenOnlyActiveSchedulesAreReturned() {
        TestSetup setup = createSetup();

        CleaningSchedule activeSchedule = createSchedule(setup.household, setup.mainTenant, setup.roommate, "Active schedule");
        CleaningSchedule inactiveSchedule = createSchedule(setup.household, setup.mainTenant, setup.roommate, "Inactive schedule");
        inactiveSchedule.setActive(false);

        cleaningScheduleRepository.save(activeSchedule);
        cleaningScheduleRepository.save(inactiveSchedule);
        cleaningScheduleRepository.flush();

        List<CleaningSchedule> result = cleaningScheduleRepository.findAllByHousehold_IdAndActiveTrueOrderByNextDueDateAscCreatedAtDesc(setup.household.getId());

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals("Active schedule", result.get(0).getTitle()),
            () -> assertEquals(setup.household.getId(), result.get(0).getHousehold().getId())
        );
    }

    @Test
    public void givenScheduleWithoutHousehold_whenSave_thenDatabaseRejectsIt() {
        TestSetup setup = createSetup();

        CleaningSchedule schedule = new CleaningSchedule();
        schedule.setTitle(TEST_CLEANING_TITLE);
        schedule.setDescription(TEST_CLEANING_DESCRIPTION);
        schedule.setFrequency(CleaningFrequency.WEEKLY);
        schedule.setStartDate(TEST_START_DATE);
        schedule.setEndDate(TEST_END_DATE);
        schedule.setNextDueDate(TEST_START_DATE);
        schedule.setCreatedBy(setup.mainTenant);
        schedule.setAssignedTo(setup.roommate);

        assertThrows(DataIntegrityViolationException.class, () -> cleaningScheduleRepository.saveAndFlush(schedule));
    }

    @Test
    public void givenScheduleWithoutAssignee_whenSave_thenDatabaseRejectsIt() {
        TestSetup setup = createSetup();

        CleaningSchedule schedule = new CleaningSchedule();
        schedule.setHousehold(setup.household);
        schedule.setTitle(TEST_CLEANING_TITLE);
        schedule.setDescription(TEST_CLEANING_DESCRIPTION);
        schedule.setFrequency(CleaningFrequency.WEEKLY);
        schedule.setStartDate(TEST_START_DATE);
        schedule.setEndDate(TEST_END_DATE);
        schedule.setNextDueDate(TEST_START_DATE);
        schedule.setCreatedBy(setup.mainTenant);

        assertThrows(DataIntegrityViolationException.class, () -> cleaningScheduleRepository.saveAndFlush(schedule));
    }

    @Test
    public void givenScheduleWithoutExplicitActiveChange_whenSave_thenScheduleIsActiveByDefault() {
        TestSetup setup = createSetup();

        CleaningSchedule schedule = createSchedule(setup.household, setup.mainTenant, setup.roommate, TEST_CLEANING_TITLE);
        CleaningSchedule savedSchedule = cleaningScheduleRepository.saveAndFlush(schedule);

        assertFalse(!savedSchedule.isActive());
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

    private CleaningSchedule createSchedule(Household household, ApplicationUser creator, ApplicationUser assignee, String title) {
        CleaningSchedule schedule = new CleaningSchedule();
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