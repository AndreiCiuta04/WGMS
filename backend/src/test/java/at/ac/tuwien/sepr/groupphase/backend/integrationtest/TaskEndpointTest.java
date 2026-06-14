package at.ac.tuwien.sepr.groupphase.backend.integrationtest;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.FairChoreRecommendationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.FairChoreRoommateStatsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskUpdateDto;
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


import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TaskEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

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

    @Autowired
    private CleaningScheduleRepository cleaningScheduleRepository;

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
    public void givenHouseholdMember_whenCreateTaskForSameHouseholdMember_then201AndTaskIsHouseholdScoped() throws Exception {
        TaskCreateDto taskCreateDto = new TaskCreateDto(
            TEST_TASK_TITLE,
            TEST_TASK_DESCRIPTION,
            TEST_TASK_DUE_DATE,
            ROOMMATE_EMAIL
        );

        MvcResult mvcResult = this.mockMvc.perform(post(TASK_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(taskCreateDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        TaskDetailDto taskDetailDto = jsonMapper.readValue(response.getContentAsString(), TaskDetailDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.CREATED.value(), response.getStatus()),
            () -> assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType()),
            () -> assertNotNull(taskDetailDto.id()),
            () -> assertEquals(household.getId(), taskDetailDto.householdId()),
            () -> assertEquals(TEST_TASK_TITLE, taskDetailDto.title()),
            () -> assertEquals(TEST_TASK_DESCRIPTION, taskDetailDto.description()),
            () -> assertEquals(TEST_TASK_DUE_DATE, taskDetailDto.dueDate()),
            () -> assertEquals(TaskStatus.OPEN, taskDetailDto.status()),
            () -> assertEquals(MAIN_TENANT_EMAIL, taskDetailDto.createdByEmail()),
            () -> assertEquals(ROOMMATE_EMAIL, taskDetailDto.assignedToEmail()),
            () -> assertNotNull(taskDetailDto.createdAt()),
            () -> assertNotNull(taskDetailDto.updatedAt()),
            () -> assertEquals(1, taskRepository.findAll().size())
        );
    }

    @Test
    public void givenNoToken_whenCreateTask_then403() throws Exception {
        TaskCreateDto taskCreateDto = validCreateDto(ROOMMATE_EMAIL);

        MvcResult mvcResult = this.mockMvc.perform(post(TASK_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(taskCreateDto)))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void givenNoToken_whenGetFairBalancingRecommendation_then403() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TASK_BASE_URI + "/fair-balancing/recommendation"))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void givenBlankTitle_whenCreateTask_then400() throws Exception {
        TaskCreateDto taskCreateDto = validCreateDto(ROOMMATE_EMAIL);
        taskCreateDto = new TaskCreateDto("   ", taskCreateDto.description(), taskCreateDto.dueDate(), taskCreateDto.assignedToEmail());

        MvcResult mvcResult = this.mockMvc.perform(post(TASK_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(taskCreateDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
        assertEquals(0, taskRepository.findAll().size());
    }

    @Test
    public void givenNonexistentAssignee_whenCreateTask_then404AndNoTaskIsCreated() throws Exception {
        TaskCreateDto taskCreateDto = validCreateDto("missing.user@test.com");

        MvcResult mvcResult = this.mockMvc.perform(post(TASK_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(taskCreateDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertEquals(HttpStatus.NOT_FOUND.value(), mvcResult.getResponse().getStatus());
        assertEquals(0, taskRepository.findAll().size());
    }

    @Test
    public void givenAssigneeFromAnotherHousehold_whenCreateTask_then403AndNoTaskIsCreated() throws Exception {
        TaskCreateDto taskCreateDto = validCreateDto(OUTSIDER_EMAIL);

        MvcResult mvcResult = this.mockMvc.perform(post(TASK_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(taskCreateDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
        assertEquals(0, taskRepository.findAll().size());
    }

    @Test
    public void givenTasksInTwoHouseholds_whenFindAll_thenOnlyTasksFromCurrentUsersHouseholdAreReturned() throws Exception {
        saveTask(household, mainTenant, roommate, "Task in own WG");
        saveTask(otherHousehold, outsider, outsider, "Task in other WG");

        MvcResult mvcResult = this.mockMvc.perform(get(TASK_BASE_URI)
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        List<TaskDetailDto> tasks = Arrays.asList(jsonMapper.readValue(response.getContentAsString(), TaskDetailDto[].class));

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(1, tasks.size()),
            () -> assertEquals("Task in own WG", tasks.get(0).title()),
            () -> assertEquals(household.getId(), tasks.get(0).householdId())
        );
    }

    @Test
    public void givenAuthenticatedUser_whenGetFairBalancingRecommendation_thenReturnsCurrentHouseholdStatsOnly() throws Exception {
        Task doneForRoommate = saveTask(household, mainTenant, roommate, "Completed own WG task");
        doneForRoommate.setStatus(TaskStatus.DONE);
        taskRepository.save(doneForRoommate);
        saveTask(otherHousehold, outsider, outsider, "Other household task");

        MvcResult mvcResult = this.mockMvc.perform(get(TASK_BASE_URI + "/fair-balancing/recommendation")
                .param("taskTitle", "Mop floor")
                .param("dueDate", TEST_TASK_DUE_DATE.toString())
                .param("householdId", otherHousehold.getId().toString())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        FairChoreRecommendationDto recommendation = jsonMapper.readValue(response.getContentAsString(), FairChoreRecommendationDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertNotNull(recommendation.recommendedUserId()),
            () -> assertEquals(3, recommendation.stats().size()),
            () -> assertEquals(0, recommendation.stats().stream().filter(stats -> OUTSIDER_EMAIL.equals(stats.email())).count()),
            () -> assertEquals(1, recommendation.stats().stream().filter(stats -> ROOMMATE_EMAIL.equals(stats.email())).findFirst().orElseThrow().completedTasks())
        );
    }

    @Test
    public void givenSameRequest_whenGetFairBalancingRecommendationTwice_thenSameRecommendation() throws Exception {
        MvcResult first = this.mockMvc.perform(get(TASK_BASE_URI + "/fair-balancing/recommendation")
                .param("taskTitle", "Mop floor")
                .param("dueDate", TEST_TASK_DUE_DATE.toString())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();
        MvcResult second = this.mockMvc.perform(get(TASK_BASE_URI + "/fair-balancing/recommendation")
                .param("taskTitle", "Mop floor")
                .param("dueDate", TEST_TASK_DUE_DATE.toString())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        FairChoreRecommendationDto firstRecommendation = jsonMapper.readValue(first.getResponse().getContentAsString(), FairChoreRecommendationDto.class);
        FairChoreRecommendationDto secondRecommendation = jsonMapper.readValue(second.getResponse().getContentAsString(), FairChoreRecommendationDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), first.getResponse().getStatus()),
            () -> assertEquals(HttpStatus.OK.value(), second.getResponse().getStatus()),
            () -> assertEquals(firstRecommendation.recommendedUserId(), secondRecommendation.recommendedUserId())
        );
    }

    @Test
    public void givenNoHistory_whenGetFairBalancingRecommendation_thenReturnsValidActiveRoommate() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(TASK_BASE_URI + "/fair-balancing/recommendation")
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        FairChoreRecommendationDto recommendation = jsonMapper.readValue(
            mvcResult.getResponse().getContentAsString(),
            FairChoreRecommendationDto.class
        );

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(30, recommendation.historyDays()),
            () -> assertTrue(recommendation.stats().stream().allMatch(stats -> stats.fairnessScore() == 0.0d)),
            () -> assertTrue(recommendation.stats().stream().anyMatch(FairChoreRoommateStatsDto::recommended))
        );
    }

    @Test
    public void givenAssignedAndUnassignedTasks_whenFindMyTasks_thenOnlyAssignedTasksAreReturned() throws Exception {
        saveTask(household, mainTenant, roommate, "Assigned to current user");
        saveTask(household, mainTenant, secondRoommate, "Assigned to somebody else");
        saveTask(otherHousehold, outsider, outsider, "Other household task");

        MvcResult mvcResult = this.mockMvc.perform(get(TASK_BASE_URI + "/my")
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        List<TaskDetailDto> tasks = Arrays.asList(jsonMapper.readValue(response.getContentAsString(), TaskDetailDto[].class));

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(1, tasks.size()),
            () -> assertEquals("Assigned to current user", tasks.get(0).title()),
            () -> assertEquals(ROOMMATE_EMAIL, tasks.get(0).assignedToEmail())
        );
    }

    @Test
    public void givenTaskFromAnotherHousehold_whenFindOne_then403() throws Exception {
        Task otherHouseholdTask = saveTask(otherHousehold, outsider, outsider, "Private other WG task");

        MvcResult mvcResult = this.mockMvc.perform(get(TASK_BASE_URI + "/{id}", otherHouseholdTask.getId())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void givenAssignedUser_whenUpdateTaskStatus_then200AndStatusChanges() throws Exception {
        Task task = saveTask(household, mainTenant, roommate, TEST_TASK_TITLE);

        TaskStatusUpdateDto statusUpdateDto = new TaskStatusUpdateDto(TaskStatus.DONE);

        MvcResult mvcResult = this.mockMvc.perform(patch(TASK_BASE_URI + "/{id}/status", task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(statusUpdateDto))
                .header(securityProperties.getAuthHeader(), tokenFor(ROOMMATE_EMAIL)))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        TaskDetailDto taskDetailDto = jsonMapper.readValue(response.getContentAsString(), TaskDetailDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(TaskStatus.DONE, taskDetailDto.status()),
            () -> assertEquals(TaskStatus.DONE, taskRepository.findById(task.getId()).orElseThrow().getStatus())
        );
    }

    @Test
    public void givenUnrelatedRoommate_whenUpdateTaskStatus_then403() throws Exception {
        Task task = saveTask(household, mainTenant, roommate, TEST_TASK_TITLE);

        TaskStatusUpdateDto statusUpdateDto = new TaskStatusUpdateDto(TaskStatus.DONE);

        MvcResult mvcResult = this.mockMvc.perform(patch(TASK_BASE_URI + "/{id}/status", task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(statusUpdateDto))
                .header(securityProperties.getAuthHeader(), tokenFor(SECOND_ROOMMATE_EMAIL)))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void givenCreator_whenUpdateTaskAndReassignInsideSameHousehold_then200() throws Exception {
        Task task = saveTask(household, mainTenant, roommate, TEST_TASK_TITLE);

        TaskUpdateDto updateDto = new TaskUpdateDto(
            UPDATED_TASK_TITLE,
            UPDATED_TASK_DESCRIPTION,
            UPDATED_TASK_DUE_DATE,
            SECOND_ROOMMATE_EMAIL
        );

        MvcResult mvcResult = this.mockMvc.perform(put(TASK_BASE_URI + "/{id}", task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        TaskDetailDto taskDetailDto = jsonMapper.readValue(response.getContentAsString(), TaskDetailDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(UPDATED_TASK_TITLE, taskDetailDto.title()),
            () -> assertEquals(UPDATED_TASK_DESCRIPTION, taskDetailDto.description()),
            () -> assertEquals(UPDATED_TASK_DUE_DATE, taskDetailDto.dueDate()),
            () -> assertEquals(SECOND_ROOMMATE_EMAIL, taskDetailDto.assignedToEmail())
        );
    }

    @Test
    public void givenCreator_whenReassignTaskToUserFromAnotherHousehold_then403() throws Exception {
        Task task = saveTask(household, mainTenant, roommate, TEST_TASK_TITLE);

        TaskUpdateDto updateDto = new TaskUpdateDto(
            UPDATED_TASK_TITLE,
            UPDATED_TASK_DESCRIPTION,
            UPDATED_TASK_DUE_DATE,
            OUTSIDER_EMAIL
        );

        MvcResult mvcResult = this.mockMvc.perform(put(TASK_BASE_URI + "/{id}", task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateDto))
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void givenCreator_whenDeleteTask_then204AndTaskIsDeleted() throws Exception {
        Task task = saveTask(household, mainTenant, roommate, TEST_TASK_TITLE);

        MvcResult mvcResult = this.mockMvc.perform(delete(TASK_BASE_URI + "/{id}", task.getId())
                .header(securityProperties.getAuthHeader(), tokenFor(MAIN_TENANT_EMAIL)))
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.NO_CONTENT.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(0, taskRepository.findAll().size())
        );
    }

    @Test
    public void givenUnrelatedRoommate_whenDeleteTask_then403AndTaskIsNotDeleted() throws Exception {
        Task task = saveTask(household, mainTenant, roommate, TEST_TASK_TITLE);

        MvcResult mvcResult = this.mockMvc.perform(delete(TASK_BASE_URI + "/{id}", task.getId())
                .header(securityProperties.getAuthHeader(), tokenFor(SECOND_ROOMMATE_EMAIL)))
            .andReturn();

        assertAll(
            () -> assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus()),
            () -> assertEquals(1, taskRepository.findAll().size())
        );
    }

    private TaskCreateDto validCreateDto(String assignedToEmail) {
        return new TaskCreateDto(
            TEST_TASK_TITLE,
            TEST_TASK_DESCRIPTION,
            TEST_TASK_DUE_DATE,
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

    private Task saveTask(Household household, ApplicationUser creator, ApplicationUser assignee, String title) {
        Task task = new Task();
        task.setHousehold(household);
        task.setTitle(title);
        task.setDescription(TEST_TASK_DESCRIPTION);
        task.setDueDate(TEST_TASK_DUE_DATE);
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedBy(creator);
        task.setAssignedTo(assignee);
        return taskRepository.save(task);
    }
}
