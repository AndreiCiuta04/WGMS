package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestDbCleaner;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DashboardDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationSettingRepository;
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class DashboardEndpointTest implements TestData {

    private static final String DASHBOARD_URI = "/api/v1/dashboard";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

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

    private ApplicationUser tenantOne;
    private ApplicationUser tenantTwo;
    private Household householdOne;
    private Household householdTwo;

    @BeforeEach
    public void beforeEach() {
        taskRepository.deleteAll();
        cleaningScheduleRepository.deleteAll();
        roommateRepository.deleteAll();
        householdRepository.deleteAll();
        TestDbCleaner.clearNotificationData(notificationRepository, notificationSettingRepository);
        userRepository.deleteAll();

        tenantOne = saveUser("tenant1@test.com", "Tenant", "One");
        tenantTwo = saveUser("tenant2@test.com", "Tenant", "Two");

        householdOne = saveHousehold("Household One", tenantOne);
        householdTwo = saveHousehold("Household Two", tenantTwo);

        saveRoommate(tenantOne, householdOne, HouseholdRole.MAIN_TENANT);
        saveRoommate(tenantTwo, householdTwo, HouseholdRole.MAIN_TENANT);
    }

    @Test
    public void givenNoToken_whenGetDashboard_then403() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get(DASHBOARD_URI))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void givenValidUser_whenGetDashboard_then200AndValidStructure() throws Exception {
        saveTask(householdOne, tenantOne, tenantOne, "Own task");

        MvcResult mvcResult = this.mockMvc.perform(get(DASHBOARD_URI)
                .header(securityProperties.getAuthHeader(), tokenFor("tenant1@test.com")))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        DashboardDto dashboardDto = jsonMapper.readValue(response.getContentAsString(), DashboardDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(1, dashboardDto.getOpenTaskCount()),
            () -> assertEquals(1, dashboardDto.getMyTaskCount()),
            () -> assertEquals(1, dashboardDto.getMyPendingTasks().size()),
            () -> assertEquals("Own task", dashboardDto.getMyPendingTasks().get(0).title())
        );
    }

    @Test
    public void givenTwoHouseholds_whenGetDashboard_thenVerifyHouseholdIsolation() throws Exception {
        saveTask(householdOne, tenantOne, tenantOne, "Household One task");
        saveTask(householdTwo, tenantTwo, tenantTwo, "Household Two task");

        MvcResult mvcResult = this.mockMvc.perform(get(DASHBOARD_URI)
                .header(securityProperties.getAuthHeader(), tokenFor("tenant1@test.com")))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        DashboardDto dashboardDto = jsonMapper.readValue(response.getContentAsString(), DashboardDto.class);

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(1, dashboardDto.getOpenTaskCount()),
            () -> assertEquals(1, dashboardDto.getMyTaskCount()),
            () -> assertEquals(1, dashboardDto.getMyPendingTasks().size()),
            () -> assertEquals("Household One task", dashboardDto.getMyPendingTasks().get(0).title())
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
        task.setDescription("Description");
        task.setDueDate(TEST_TASK_DUE_DATE);
        task.setStatus(TaskStatus.OPEN);
        task.setCreatedBy(creator);
        task.setAssignedTo(assignee);
        return taskRepository.save(task);
    }
}
