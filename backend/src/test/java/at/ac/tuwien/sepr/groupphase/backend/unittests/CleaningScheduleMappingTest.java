package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.CleaningScheduleMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CleaningScheduleMappingTest implements TestData {

    private static final String TEST_CLEANING_TITLE = "Clean kitchen schedule";
    private static final String TEST_CLEANING_DESCRIPTION = "Clean surfaces, sink, and floor";
    private static final LocalDate TEST_START_DATE = LocalDate.now().plusDays(1);
    private static final LocalDate TEST_END_DATE = LocalDate.now().plusWeeks(4);
    private static final LocalDate TEST_NEXT_DUE_DATE = LocalDate.now().plusDays(1);
    private static final LocalDateTime TEST_CREATED_AT = LocalDateTime.now().minusHours(1);
    private static final LocalDateTime TEST_UPDATED_AT = LocalDateTime.now();

    @Autowired
    private CleaningScheduleMapper cleaningScheduleMapper;

    @Test
    public void givenCleaningScheduleEntity_whenMapToDetailDto_thenHouseholdCreatorAndAssigneeAreMapped() {
        ApplicationUser creator = user(1L, MAIN_TENANT_EMAIL);
        ApplicationUser assignee = user(2L, ROOMMATE_EMAIL);

        Household household = new Household();
        household.setId(10L);
        household.setName(TEST_HOUSEHOLD_NAME);
        household.setAddress("Test address");
        household.setCreatedBy(creator);

        CleaningSchedule cleaningSchedule = new CleaningSchedule();
        cleaningSchedule.setId(99L);
        cleaningSchedule.setHousehold(household);
        cleaningSchedule.setTitle(TEST_CLEANING_TITLE);
        cleaningSchedule.setDescription(TEST_CLEANING_DESCRIPTION);
        cleaningSchedule.setFrequency(CleaningFrequency.WEEKLY);
        cleaningSchedule.setStartDate(TEST_START_DATE);
        cleaningSchedule.setEndDate(TEST_END_DATE);
        cleaningSchedule.setNextDueDate(TEST_NEXT_DUE_DATE);
        cleaningSchedule.setActive(true);
        cleaningSchedule.setCreatedBy(creator);
        cleaningSchedule.setAssignedTo(assignee);
        cleaningSchedule.setCreatedAt(TEST_CREATED_AT);
        cleaningSchedule.setUpdatedAt(TEST_UPDATED_AT);

        CleaningScheduleDetailDto dto = cleaningScheduleMapper.cleaningScheduleToCleaningScheduleDetailDto(cleaningSchedule);

        assertAll(
            () -> assertEquals(99L, dto.getId()),
            () -> assertEquals(10L, dto.getHouseholdId()),
            () -> assertEquals(TEST_CLEANING_TITLE, dto.getTitle()),
            () -> assertEquals(TEST_CLEANING_DESCRIPTION, dto.getDescription()),
            () -> assertEquals(CleaningFrequency.WEEKLY, dto.getFrequency()),
            () -> assertEquals(TEST_START_DATE, dto.getStartDate()),
            () -> assertEquals(TEST_END_DATE, dto.getEndDate()),
            () -> assertEquals(TEST_NEXT_DUE_DATE, dto.getNextDueDate()),
            () -> assertTrue(dto.isActive()),
            () -> assertEquals(MAIN_TENANT_EMAIL, dto.getCreatedByEmail()),
            () -> assertEquals(ROOMMATE_EMAIL, dto.getAssignedToEmail()),
            () -> assertEquals(TEST_CREATED_AT, dto.getCreatedAt()),
            () -> assertEquals(TEST_UPDATED_AT, dto.getUpdatedAt())
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