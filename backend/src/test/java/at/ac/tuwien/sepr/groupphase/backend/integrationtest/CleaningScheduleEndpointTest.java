package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleActiveUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdInvitationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class CleaningScheduleEndpointTest implements TestData {

    private static final String CLEANING_SCHEDULE_BASE_URI = BASE_URI + "/cleaning-schedules";
    private static final String TEST_CLEANING_TITLE = "Clean kitchen schedule";
    private static final String TEST_CLEANING_DESCRIPTION = "Clean surfaces, sink, and floor";
    private static final String UPDATED_CLEANING_TITLE = "Clean bathroom schedule";
    private static final String UPDATED_CLEANING_DESCRIPTION = "Clean sink, mirror, and floor";
    private static final LocalDate TEST_START_DATE = LocalDate.now().plusDays(1);
    private static final LocalDate TEST_END_DATE = LocalDate.now().plusWeeks(4);
    private static final LocalDate UPDATED_START_DATE = LocalDate.now().plusDays(2);
    private static final LocalDate UPDATED_END_DATE = LocalDate.now().plusWeeks(5);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CleaningScheduleRepository cleaningScheduleRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private HouseholdInvitationRepository invitationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private RoommateRepository roommateRepository;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JsonMapper jsonMapper;

    private ApplicationUser mainTenant;
    private ApplicationUser roommate;
    private ApplicationUser secondRoommate;
    private ApplicationUser outsider;
    private Household household;
    private Household otherHousehold;

    @BeforeEach
    public void beforeEach() {
        taskRepository.deleteAll();
        cleaningScheduleRepository.deleteAll();
        invitationRepository.deleteAll();
        roommateRepository.deleteAll();
        householdRepository.deleteAll();
        userRepository.deleteAll();

        mainTenant = saveUser(MAIN_TENANT_EMAIL, "Main", "Tenant");
        roommate = saveUser(ROOMMATE_EMAIL, "Normal", "Roommate");
        secondRoommate = saveUser(SECOND_ROOMMATE_EMAIL, "Second", "Roommate");
        outsider = saveUser(OUTSIDER_EMAIL, "Other", "User");

        household = saveHousehold(TEST_HOUSEHOLD_NAME, mainTenant);
        otherHousehold = saveHousehold(OTHER_HOUSEHOLD_NAME, outsider);

        saveRoommate(mainTenant, household, HouseholdRole.MAIN_TENANT);
        saveRoommate(roommate, household, HouseholdRole.ROOMMATE);
        saveRoommate(secondRoommate, household, HouseholdRole.ROOMMATE);
        saveRoommate(outsider, otherHousehold, HouseholdRole.MAIN_TENANT);
    }

    @Test
    public void givenHouseholdMember_whenCreateScheduleForSameHouseholdMember_then201AndScheduleIsHouseholdScoped() throws Exception {
        CleaningScheduleCreateDto createDto = validCreateDto(ROOMMATE_EMAIL);

        MvcResult mvcResult = this.mockMvc.perform(post(CLEANING_SCHEDULE_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        CleaningScheduleDetailDto detailDto = jsonMapper.readValue(response.getContentAsString(), CleaningScheduleDetailDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED.value(), response.getStatus()),
            () -> assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType()),
            () -> assertNotNull(detailDto.id()),
            () -> assertEquals(household.getId(), detailDto.householdId()),
            () -> assertEquals(TEST_CLEANING_TITLE, detailDto.title()),
            () -> assertEquals(TEST_CLEANING_DESCRIPTION, detailDto.description()),
            () -> assertEquals(CleaningFrequency.WEEKLY, detailDto.frequency()),
            () -> assertEquals(TEST_START_DATE, detailDto.startDate()),
            () -> assertEquals(TEST_END_DATE, detailDto.endDate()),
            () -> assertEquals(TEST_START_DATE, detailDto.nextDueDate()),
            () -> assertEquals(MAIN_TENANT_EMAIL, detailDto.createdByEmail()),
            () -> assertEquals(ROOMMATE_EMAIL, detailDto.assignedToEmail()),
            () -> assertNotNull(detailDto.createdAt()),
            () -> assertNotNull(detailDto.updatedAt()),
            () -> assertEquals(1, cleaningScheduleRepository.findAll().size())
        );
    }

    @Test
    public void givenNoToken_whenCreateSchedule_then403() throws Exception {
        CleaningScheduleCreateDto createDto = validCreateDto(ROOMMATE_EMAIL);

        MvcResult mvcResult = this.mockMvc.perform(post(CLEANING_SCHEDULE_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createDto)))
            .andDo(print())
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void givenBlankTitle_whenCreateSchedule_then400AndNoScheduleIsCreated() throws Exception {
        CleaningScheduleCreateDto createDto = validCreateDto(ROOMMATE_EMAIL);
        createDto = new CleaningScheduleCreateDto(
            "   ",
            createDto.description(),
            createDto.frequency(),
            createDto.startDate(),
            createDto.endDate(),
            createDto.assignedToEmail()
        );

        MvcResult mvcResult = this.mockMvc.perform(post(CLEANING_SCHEDULE_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andDo(print())
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, cleaningScheduleRepository.findAll().size())
        );
    }

    @Test
    public void givenEndDateBeforeStartDate_whenCreateSchedule_then400AndNoScheduleIsCreated() throws Exception {
        CleaningScheduleCreateDto createDto = validCreateDto(ROOMMATE_EMAIL);
        createDto = new CleaningScheduleCreateDto(
            createDto.title(),
            createDto.description(),
            createDto.frequency(),
            createDto.startDate(),
            TEST_START_DATE.minusDays(1),
            createDto.assignedToEmail()
        );

        MvcResult mvcResult = this.mockMvc.perform(post(CLEANING_SCHEDULE_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andDo(print())
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, cleaningScheduleRepository.findAll().size())
        );
    }

    @Test
    public void givenAssigneeFromAnotherHousehold_whenCreateSchedule_then403AndNoScheduleIsCreated() throws Exception {
        CleaningScheduleCreateDto createDto = validCreateDto(OUTSIDER_EMAIL);

        MvcResult mvcResult = this.mockMvc.perform(post(CLEANING_SCHEDULE_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(createDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andDo(print())
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, cleaningScheduleRepository.findAll().size())
        );
    }

    @Test
    public void givenSchedulesInTwoHouseholds_whenFindAll_thenOnlySchedulesFromCurrentUsersHouseholdAreReturned() throws Exception {
        saveSchedule(household, mainTenant, roommate, "Own WG schedule", true);
        saveSchedule(otherHousehold, outsider, outsider, "Other WG schedule", true);

        MvcResult mvcResult = this.mockMvc.perform(get(CLEANING_SCHEDULE_BASE_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        List<CleaningScheduleDetailDto> schedules = Arrays.asList(jsonMapper.readValue(response.getContentAsString(), CleaningScheduleDetailDto[].class));

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(1, schedules.size()),
            () -> assertEquals("Own WG schedule", schedules.get(0).title()),
            () -> assertEquals(household.getId(), schedules.get(0).householdId())
        );
    }

    @Test
    public void givenActiveAndInactiveSchedules_whenFindActive_thenOnlyActiveSchedulesAreReturned() throws Exception {
        saveSchedule(household, mainTenant, roommate, "Active schedule", true);
        saveSchedule(household, mainTenant, roommate, "Inactive schedule", false);

        MvcResult mvcResult = this.mockMvc.perform(get(CLEANING_SCHEDULE_BASE_URI + "/active")
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        List<CleaningScheduleDetailDto> schedules = Arrays.asList(jsonMapper.readValue(response.getContentAsString(), CleaningScheduleDetailDto[].class));

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(1, schedules.size()),
            () -> assertEquals("Active schedule", schedules.get(0).title())
        );
    }

    @Test
    public void givenScheduleFromAnotherHousehold_whenFindOne_then403() throws Exception {
        CleaningSchedule otherSchedule = saveSchedule(otherHousehold, outsider, outsider, "Private other WG schedule", true);

        MvcResult mvcResult = this.mockMvc.perform(get(CLEANING_SCHEDULE_BASE_URI + "/{id}", otherSchedule.getId())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andDo(print())
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void givenCreator_whenUpdateScheduleAndReassignInsideSameHousehold_then200() throws Exception {
        CleaningSchedule schedule = saveSchedule(household, mainTenant, roommate, TEST_CLEANING_TITLE, true);
        CleaningScheduleUpdateDto updateDto = validUpdateDto(SECOND_ROOMMATE_EMAIL);

        MvcResult mvcResult = this.mockMvc.perform(put(CLEANING_SCHEDULE_BASE_URI + "/{id}", schedule.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        CleaningScheduleDetailDto detailDto = jsonMapper.readValue(response.getContentAsString(), CleaningScheduleDetailDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(UPDATED_CLEANING_TITLE, detailDto.title()),
            () -> assertEquals(UPDATED_CLEANING_DESCRIPTION, detailDto.description()),
            () -> assertEquals(CleaningFrequency.BIWEEKLY, detailDto.frequency()),
            () -> assertEquals(UPDATED_START_DATE, detailDto.startDate()),
            () -> assertEquals(UPDATED_END_DATE, detailDto.endDate()),
            () -> assertEquals(SECOND_ROOMMATE_EMAIL, detailDto.assignedToEmail())
        );
    }

    @Test
    public void givenCreator_whenDeactivateSchedule_then200AndScheduleIsInactive() throws Exception {
        CleaningSchedule schedule = saveSchedule(household, mainTenant, roommate, TEST_CLEANING_TITLE, true);
        CleaningScheduleActiveUpdateDto activeUpdateDto = new CleaningScheduleActiveUpdateDto(false);

        MvcResult mvcResult = this.mockMvc.perform(patch(CLEANING_SCHEDULE_BASE_URI + "/{id}/active", schedule.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(activeUpdateDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        CleaningScheduleDetailDto detailDto = jsonMapper.readValue(response.getContentAsString(), CleaningScheduleDetailDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertFalse(detailDto.active()),
            () -> assertFalse(cleaningScheduleRepository.findById(schedule.getId()).orElseThrow().isActive())
        );
    }

    @Test
    public void givenActiveSchedule_whenGenerateTask_then200TaskCreatedAndNextDueDateAdvances() throws Exception {
        CleaningSchedule schedule = saveSchedule(household, mainTenant, roommate, TEST_CLEANING_TITLE, true);

        MvcResult mvcResult = this.mockMvc.perform(post(CLEANING_SCHEDULE_BASE_URI + "/{id}/generate-task", schedule.getId())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        TaskDetailDto taskDetailDto = jsonMapper.readValue(response.getContentAsString(), TaskDetailDto.class);
        CleaningSchedule updatedSchedule = cleaningScheduleRepository.findById(schedule.getId()).orElseThrow();

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(TEST_CLEANING_TITLE, taskDetailDto.title()),
            () -> assertEquals(TEST_CLEANING_DESCRIPTION, taskDetailDto.description()),
            () -> assertEquals(TEST_START_DATE, taskDetailDto.dueDate()),
            () -> assertEquals(TaskStatus.OPEN, taskDetailDto.status()),
            () -> assertEquals(household.getId(), taskDetailDto.householdId()),
            () -> assertEquals(schedule.getId(), taskDetailDto.cleaningScheduleId()),
            () -> assertEquals(MAIN_TENANT_EMAIL, taskDetailDto.createdByEmail()),
            () -> assertEquals(ROOMMATE_EMAIL, taskDetailDto.assignedToEmail()),
            () -> assertEquals(1, taskRepository.findAll().size()),
            () -> assertEquals(TEST_START_DATE.plusWeeks(1), updatedSchedule.getNextDueDate())
        );
    }

    @Test
    public void givenInactiveSchedule_whenGenerateTask_then409AndNoTaskIsCreated() throws Exception {
        CleaningSchedule schedule = saveSchedule(household, mainTenant, roommate, TEST_CLEANING_TITLE, false);

        MvcResult mvcResult = this.mockMvc.perform(post(CLEANING_SCHEDULE_BASE_URI + "/{id}/generate-task", schedule.getId())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andDo(print())
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.CONFLICT.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, taskRepository.findAll().size())
        );
    }

    @Test
    public void givenScheduleWithGeneratedTask_whenDeleteSchedule_then409AndScheduleAndTaskRemain() throws Exception {
        CleaningSchedule schedule = saveSchedule(household, mainTenant, roommate, TEST_CLEANING_TITLE, true);

        this.mockMvc.perform(post(CLEANING_SCHEDULE_BASE_URI + "/{id}/generate-task", schedule.getId())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andDo(print())
            .andReturn();

        MvcResult mvcResult = this.mockMvc.perform(delete(CLEANING_SCHEDULE_BASE_URI + "/{id}", schedule.getId())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andDo(print())
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.CONFLICT.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(1, cleaningScheduleRepository.findAll().size()),
            () -> assertEquals(1, taskRepository.findAll().size())
        );
    }

    @Test
    public void givenCreator_whenDeleteSchedule_then204AndScheduleIsDeleted() throws Exception {
        CleaningSchedule schedule = saveSchedule(household, mainTenant, roommate, TEST_CLEANING_TITLE, true);

        MvcResult mvcResult = this.mockMvc.perform(delete(CLEANING_SCHEDULE_BASE_URI + "/{id}", schedule.getId())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andDo(print())
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.NO_CONTENT.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, cleaningScheduleRepository.findAll().size())
        );
    }

    private CleaningScheduleCreateDto validCreateDto(String assignedToEmail) {
        return new CleaningScheduleCreateDto(
            TEST_CLEANING_TITLE,
            TEST_CLEANING_DESCRIPTION,
            CleaningFrequency.WEEKLY,
            TEST_START_DATE,
            TEST_END_DATE,
            assignedToEmail
        );
    }

    private CleaningScheduleUpdateDto validUpdateDto(String assignedToEmail) {
        return new CleaningScheduleUpdateDto(
            UPDATED_CLEANING_TITLE,
            UPDATED_CLEANING_DESCRIPTION,
            CleaningFrequency.BIWEEKLY,
            UPDATED_START_DATE,
            UPDATED_END_DATE,
            assignedToEmail
        );
    }

    private String tokenFor(String email) {
        return jwtTokenizer.getAuthToken(email, USER_ROLES);
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
        Household householdToSave = new Household();
        householdToSave.setName(name);
        householdToSave.setAddress("Test address");
        householdToSave.setCreatedBy(createdBy);
        return householdRepository.save(householdToSave);
    }

    private Roommate saveRoommate(ApplicationUser user, Household household, HouseholdRole role) {
        Roommate roommateToSave = new Roommate();
        roommateToSave.setUser(user);
        roommateToSave.setHousehold(household);
        roommateToSave.setRole(role);
        roommateToSave.setActive(true);
        return roommateRepository.save(roommateToSave);
    }

    private CleaningSchedule saveSchedule(Household household, ApplicationUser creator, ApplicationUser assignee, String title, boolean active) {
        CleaningSchedule schedule = new CleaningSchedule();
        schedule.setHousehold(household);
        schedule.setTitle(title);
        schedule.setDescription(TEST_CLEANING_DESCRIPTION);
        schedule.setFrequency(CleaningFrequency.WEEKLY);
        schedule.setStartDate(TEST_START_DATE);
        schedule.setEndDate(TEST_END_DATE);
        schedule.setNextDueDate(TEST_START_DATE);
        schedule.setActive(active);
        schedule.setCreatedBy(creator);
        schedule.setAssignedTo(assignee);
        return cleaningScheduleRepository.save(schedule);
    }
}
