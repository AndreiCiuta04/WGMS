package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserDataExportDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserProfileUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegistrationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Payment;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private RoommateRepository roommateRepository;

    @Autowired
    private HouseholdInvitationRepository invitationRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private CleaningScheduleRepository cleaningScheduleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @BeforeEach
    public void beforeEach() {
        paymentRepository.deleteAll();
        expenseRepository.deleteAll();
        taskRepository.deleteAll();
        cleaningScheduleRepository.deleteAll();
        invitationRepository.deleteAll();
        roommateRepository.deleteAll();
        householdRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void givenNothing_whenRegisterValidUser_thenReturns201AndSavesUser() throws Exception {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto();
        userRegistrationDto.setEmail("newuser@example.com");
        userRegistrationDto.setPassword("Password123!");
        userRegistrationDto.setFirstName("New");
        userRegistrationDto.setLastName("User");
        userRegistrationDto.setTermsAccepted(true);

        String body = jsonMapper.writeValueAsString(userRegistrationDto);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertTrue(response.getContentAsString().startsWith("Bearer "));

        ApplicationUser savedUser = userRepository.findUserByEmail("newuser@example.com");
        assertNotNull(savedUser);
        assertEquals("New", savedUser.getFirstName());
        assertEquals("User", savedUser.getLastName());
        assertFalse(savedUser.getAdmin());
        assertEquals(0, savedUser.getFailedLoginAttempts());
        assertNull(savedUser.getAccountLockedUntil());
        assertNotEquals("Password123!", savedUser.getPassword());
        assertTrue(passwordEncoder.matches("Password123!", savedUser.getPassword()));
        assertNotNull(savedUser.getTermsAcceptedAt());
        assertEquals("v1.0", savedUser.getTermsVersion());
        assertNotNull(savedUser.getPrivacyAcknowledgedAt());
        assertEquals("v1.0", savedUser.getPrivacyVersion());
    }

    @Test
    public void givenRegisteredUser_whenGetCurrentUserProfile_thenReturnsProfileAndLoadsGdprFields() throws Exception {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto();
        userRegistrationDto.setEmail("registered.profile@example.com");
        userRegistrationDto.setPassword("Password123!");
        userRegistrationDto.setFirstName("Registered");
        userRegistrationDto.setLastName("Profile");
        userRegistrationDto.setTermsAccepted(true);

        MvcResult registrationResult = this.mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(userRegistrationDto)))
            .andReturn();

        assertEquals(HttpStatus.CREATED.value(), registrationResult.getResponse().getStatus());
        String authHeader = registrationResult.getResponse().getContentAsString();

        MvcResult profileResult = this.mockMvc.perform(get("/api/v1/users/me")
                .header(securityProperties.getAuthHeader(), authHeader))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), profileResult.getResponse().getStatus());
        UserProfileDto profile = jsonMapper.readValue(profileResult.getResponse().getContentAsString(), UserProfileDto.class);
        assertEquals("registered.profile@example.com", profile.email());
        assertEquals("Registered", profile.firstName());
        assertEquals("Profile", profile.lastName());
        assertEquals("USER", profile.role());

        ApplicationUser savedUser = userRepository.findUserByEmail("registered.profile@example.com");
        assertNotNull(savedUser);
        assertFalse(savedUser.isAnonymized());
        assertEquals(0, savedUser.getFailedLoginAttempts());
        assertNull(savedUser.getAccountLockedUntil());
        assertNotNull(savedUser.getTermsAcceptedAt());
        assertEquals("v1.0", savedUser.getTermsVersion());
        assertNotNull(savedUser.getPrivacyAcknowledgedAt());
        assertEquals("v1.0", savedUser.getPrivacyVersion());
    }

    @Test
    public void givenExistingUser_whenRegisterSameEmail_thenReturns409() throws Exception {
        ApplicationUser existingUser = new ApplicationUser("existing@example.com", "hashedpassword", "Existing", "User", false);
        userRepository.save(existingUser);

        UserRegistrationDto userRegistrationDto = new UserRegistrationDto();
        userRegistrationDto.setEmail("existing@example.com");
        userRegistrationDto.setPassword("Password123!");
        userRegistrationDto.setFirstName("New");
        userRegistrationDto.setLastName("User");
        userRegistrationDto.setTermsAccepted(true);

        String body = jsonMapper.writeValueAsString(userRegistrationDto);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
        assertEquals("Request conflicts with the current resource state.", response.getContentAsString());
    }

    @Test
    public void givenNothing_whenRegisterInvalidPassword_thenReturns400() throws Exception {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto();
        userRegistrationDto.setEmail("newuser@example.com");
        userRegistrationDto.setPassword("weak"); // Too short, no numbers, no special characters
        userRegistrationDto.setFirstName("New");
        userRegistrationDto.setLastName("User");
        userRegistrationDto.setTermsAccepted(true);

        String body = jsonMapper.writeValueAsString(userRegistrationDto);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        String content = response.getContentAsString();
        assertTrue(content.contains("Validation errors"));
        assertTrue(content.contains("Password must be at least 8 characters long") || content.contains("Password must contain at least one number and one special character"));
    }

    @Test
    public void givenTermsNotAccepted_whenRegister_thenReturns400() throws Exception {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto();
        userRegistrationDto.setEmail("terms@example.com");
        userRegistrationDto.setPassword("Password123!");
        userRegistrationDto.setFirstName("Terms");
        userRegistrationDto.setLastName("User");
        userRegistrationDto.setTermsAccepted(false);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(userRegistrationDto)))
            .andReturn();

        assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
        assertTrue(mvcResult.getResponse().getContentAsString().contains("Terms and privacy acknowledgement must be accepted"));
    }

    @Test
    public void givenTermsMissing_whenRegister_thenReturns400() throws Exception {
        String body = """
            {
              "email": "missing.terms@example.com",
              "password": "Password123!",
              "firstName": "Missing",
              "lastName": "Terms"
            }
            """;

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
        assertTrue(mvcResult.getResponse().getContentAsString().contains("Terms and privacy acknowledgement must be accepted"));
    }

    @Test
    public void givenAuthenticatedUserInHousehold_whenGetCurrentUserProfile_thenReturnsSafeOwnAccountData() throws Exception {
        ApplicationUser currentUser = new ApplicationUser("current@example.com", "hashed-password", "Current", "User", false);
        ApplicationUser otherUser = new ApplicationUser("other@example.com", "other-password", "Other", "User", true);
        userRepository.save(currentUser);
        userRepository.save(otherUser);

        Household household = new Household();
        household.setName("Current WG");
        household.setAddress("Private address");
        household.setCreatedBy(currentUser);
        householdRepository.save(household);

        Roommate roommate = new Roommate();
        roommate.setUser(currentUser);
        roommate.setHousehold(household);
        roommate.setRole(HouseholdRole.MAIN_TENANT);
        roommate.setActive(true);
        roommateRepository.save(roommate);

        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/users/me")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("current@example.com", USER_ROLES)))
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        UserProfileDto profile = jsonMapper.readValue(response.getContentAsString(), UserProfileDto.class);
        assertEquals(currentUser.getId(), profile.id());
        assertEquals("current@example.com", profile.email());
        assertEquals("Current", profile.firstName());
        assertEquals("User", profile.lastName());
        assertEquals("USER", profile.role());
        assertEquals(household.getId(), profile.householdId());
        assertEquals("Current WG", profile.householdName());
        assertEquals(roommate.getId(), profile.roommateId());

        String content = response.getContentAsString();
        assertFalse(content.contains("password"));
        assertFalse(content.contains("passwordHash"));
        assertFalse(content.contains("token"));
        assertFalse(content.contains("failedLoginAttempts"));
        assertFalse(content.contains("accountLockedUntil"));
        assertFalse(content.contains("other@example.com"));
        assertNotEquals(otherUser.getId(), profile.id());
    }

    @Test
    public void givenAuthenticatedUserWithoutHousehold_whenGetCurrentUserProfile_thenReturnsNullHouseholdFields() throws Exception {
        ApplicationUser currentUser = new ApplicationUser("solo@example.com", "hashed-password", "Solo", "User", true);
        userRepository.save(currentUser);

        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/users/me")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("solo@example.com", ADMIN_ROLES)))
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        UserProfileDto profile = jsonMapper.readValue(response.getContentAsString(), UserProfileDto.class);
        assertEquals(currentUser.getId(), profile.id());
        assertEquals("solo@example.com", profile.email());
        assertEquals("ADMIN", profile.role());
        assertNull(profile.householdId());
        assertNull(profile.householdName());
        assertNull(profile.roommateId());
    }

    @Test
    public void givenNoAuthentication_whenGetCurrentUserProfile_thenRequestIsRejected() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/users/me"))
            .andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertTrue(status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void givenAuthenticatedUserInHousehold_whenExportCurrentUserData_thenReturnsSafeHouseholdScopedJson() throws Exception {
        ApplicationUser currentUser = new ApplicationUser("export@example.com", "hashed-password", "Export", "User", false);
        ApplicationUser otherUser = new ApplicationUser("other.export@example.com", "other-password", "Other", "User", false);
        userRepository.save(currentUser);
        userRepository.save(otherUser);

        Household household = new Household();
        household.setName("Export WG");
        household.setAddress("Address");
        household.setCreatedBy(currentUser);
        householdRepository.save(household);

        Household otherHousehold = new Household();
        otherHousehold.setName("Private Other WG");
        otherHousehold.setAddress("Other address");
        otherHousehold.setCreatedBy(otherUser);
        householdRepository.save(otherHousehold);

        Roommate roommate = new Roommate();
        roommate.setUser(currentUser);
        roommate.setHousehold(household);
        roommate.setRole(HouseholdRole.MAIN_TENANT);
        roommate.setActive(true);
        roommateRepository.save(roommate);

        Task task = new Task();
        task.setHousehold(household);
        task.setTitle("Exported task");
        task.setDescription("Visible in export");
        task.setDueDate(TEST_TASK_DUE_DATE);
        task.setStatus(TaskStatus.DONE);
        task.setCreatedBy(currentUser);
        task.setAssignedTo(currentUser);
        taskRepository.save(task);

        Task otherTask = new Task();
        otherTask.setHousehold(otherHousehold);
        otherTask.setTitle("Other household private task");
        otherTask.setDescription("Must not be exported");
        otherTask.setDueDate(TEST_TASK_DUE_DATE);
        otherTask.setStatus(TaskStatus.DONE);
        otherTask.setCreatedBy(otherUser);
        otherTask.setAssignedTo(otherUser);
        taskRepository.save(otherTask);

        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/users/me/export")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("export@example.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        UserDataExportDto export = jsonMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDataExportDto.class);
        assertNotNull(export.generatedAt());
        assertEquals(currentUser.getId(), export.account().id());
        assertEquals("export@example.com", export.account().email());
        assertEquals(household.getId(), export.householdMembership().householdId());
        assertTrue(export.tasks().stream().anyMatch(t -> "Exported task".equals(t.title())));

        String content = mvcResult.getResponse().getContentAsString();
        assertFalse(content.contains("password"));
        assertFalse(content.contains("passwordHash"));
        assertFalse(content.contains("token"));
        assertFalse(content.contains("jwt"));
        assertFalse(content.contains("Other household private task"));
        assertFalse(content.contains("other.export@example.com"));
    }

    @Test
    public void givenNoAuthentication_whenExportCurrentUserData_thenRequestIsRejected() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/api/v1/users/me/export"))
            .andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertTrue(status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void givenAuthenticatedUser_whenPatchCurrentUserProfile_thenUpdatesEditableFieldsOnly() throws Exception {
        ApplicationUser currentUser = new ApplicationUser("patch@example.com", passwordEncoder.encode("Password123!"), "Old", "Name", false);
        userRepository.save(currentUser);

        UserProfileUpdateDto updateDto = new UserProfileUpdateDto("new-patch@example.com", "New", "Name");

        MvcResult mvcResult = this.mockMvc.perform(patch("/api/v1/users/me")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("patch@example.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateDto)))
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        UserProfileDto profile = jsonMapper.readValue(response.getContentAsString(), UserProfileDto.class);
        assertEquals(currentUser.getId(), profile.id());
        assertEquals("new-patch@example.com", profile.email());
        assertEquals("New", profile.firstName());
        assertEquals("Name", profile.lastName());

        assertNull(userRepository.findUserByEmail("patch@example.com"));
        ApplicationUser savedUser = userRepository.findUserByEmail("new-patch@example.com");
        assertEquals("New", savedUser.getFirstName());
        assertEquals("Name", savedUser.getLastName());
        assertTrue(passwordEncoder.matches("Password123!", savedUser.getPassword()));

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("new-patch@example.com");
        loginDto.setPassword("Password123!");
        MvcResult loginResult = this.mockMvc.perform(post("/api/v1/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(loginDto)))
            .andReturn();
        assertEquals(HttpStatus.OK.value(), loginResult.getResponse().getStatus());
    }

    @Test
    public void givenInvalidProfileUpdate_whenPatchCurrentUserProfile_thenReturns400() throws Exception {
        ApplicationUser currentUser = new ApplicationUser("patch-invalid@example.com", "hashed-password", "Old", "Name", false);
        userRepository.save(currentUser);

        UserProfileUpdateDto updateDto = new UserProfileUpdateDto("not-an-email", " ", "Name");

        MvcResult mvcResult = this.mockMvc.perform(patch("/api/v1/users/me")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("patch-invalid@example.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateDto)))
            .andReturn();

        assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void givenEmailAlreadyExists_whenPatchCurrentUserProfile_thenReturns409() throws Exception {
        ApplicationUser currentUser = new ApplicationUser("patch-conflict@example.com", "hashed-password", "Old", "Name", false);
        ApplicationUser otherUser = new ApplicationUser("taken@example.com", "hashed-password", "Other", "User", false);
        userRepository.save(currentUser);
        userRepository.save(otherUser);

        UserProfileUpdateDto updateDto = new UserProfileUpdateDto("taken@example.com", "New", "Name");

        MvcResult mvcResult = this.mockMvc.perform(patch("/api/v1/users/me")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("patch-conflict@example.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateDto)))
            .andReturn();

        assertEquals(HttpStatus.CONFLICT.value(), mvcResult.getResponse().getStatus());
        assertNotNull(userRepository.findUserByEmail("patch-conflict@example.com"));
    }

    @Test
    public void givenAuthenticatedUserInHousehold_whenLeaveCurrentHousehold_thenMembershipIsInactiveAndProfileHasNoHousehold() throws Exception {
        ApplicationUser currentUser = new ApplicationUser("leave@example.com", "hashed-password", "Leave", "User", false);
        userRepository.save(currentUser);

        Household household = new Household();
        household.setName("Leave WG");
        household.setAddress("Address");
        household.setCreatedBy(currentUser);
        householdRepository.save(household);

        Roommate roommate = new Roommate();
        roommate.setUser(currentUser);
        roommate.setHousehold(household);
        roommate.setRole(HouseholdRole.ROOMMATE);
        roommate.setActive(true);
        roommateRepository.save(roommate);

        MvcResult mvcResult = this.mockMvc.perform(delete("/api/v1/users/me/household")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("leave@example.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), mvcResult.getResponse().getStatus());
        UserProfileDto profile = jsonMapper.readValue(mvcResult.getResponse().getContentAsString(), UserProfileDto.class);
        assertNull(profile.householdId());
        assertNull(profile.householdName());
        assertNull(profile.roommateId());
        assertFalse(roommateRepository.findByUserIdAndActiveTrue(currentUser.getId()).isPresent());
        assertTrue(roommateRepository.findByUserIdAndHouseholdIdAndActiveTrue(currentUser.getId(), household.getId()).isEmpty());
    }

    @Test
    public void givenAuthenticatedUserWithHistory_whenDeleteCurrentUser_thenAnonymizesAccountRejectsLoginAndKeepsHistoryReadable() throws Exception {
        ApplicationUser currentUser = new ApplicationUser("delete@example.com", passwordEncoder.encode("Password123!"), "Delete", "Me", false);
        ApplicationUser otherUser = new ApplicationUser("remaining@example.com", passwordEncoder.encode("Password123!"), "Remaining", "User", false);
        userRepository.save(currentUser);
        userRepository.save(otherUser);

        Household household = new Household();
        household.setName("History WG");
        household.setAddress("Address");
        household.setCreatedBy(currentUser);
        householdRepository.save(household);

        Roommate currentRoommate = new Roommate();
        currentRoommate.setUser(currentUser);
        currentRoommate.setHousehold(household);
        currentRoommate.setRole(HouseholdRole.MAIN_TENANT);
        currentRoommate.setActive(true);
        roommateRepository.save(currentRoommate);

        Roommate otherRoommate = new Roommate();
        otherRoommate.setUser(otherUser);
        otherRoommate.setHousehold(household);
        otherRoommate.setRole(HouseholdRole.ROOMMATE);
        otherRoommate.setActive(true);
        roommateRepository.save(otherRoommate);

        Task doneTask = new Task();
        doneTask.setHousehold(household);
        doneTask.setTitle("Historic task");
        doneTask.setDescription("Task must remain readable");
        doneTask.setDueDate(TEST_TASK_DUE_DATE);
        doneTask.setStatus(TaskStatus.DONE);
        doneTask.setCreatedBy(currentUser);
        doneTask.setAssignedTo(otherUser);
        taskRepository.save(doneTask);

        Expense expense = new Expense();
        expense.setHousehold(household);
        expense.setPaidBy(currentRoommate);
        expense.setCreatedBy(currentRoommate);
        expense.setDescription("Historic groceries");
        expense.setTotalAmount(new BigDecimal("12.00"));
        expense.setDate(TEST_TASK_DUE_DATE);
        expenseRepository.save(expense);

        Payment payment = new Payment();
        payment.setHousehold(household);
        payment.setFromRoommate(currentRoommate);
        payment.setToRoommate(otherRoommate);
        payment.setCreatedBy(currentRoommate);
        payment.setAmount(new BigDecimal("5.00"));
        payment.setDate(TEST_TASK_DUE_DATE);
        paymentRepository.save(payment);

        MvcResult deleteResult = this.mockMvc.perform(delete("/api/v1/users/me")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("delete@example.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.NO_CONTENT.value(), deleteResult.getResponse().getStatus());
        assertNull(userRepository.findUserByEmail("delete@example.com"));

        ApplicationUser anonymizedUser = userRepository.findAll().stream()
            .filter(ApplicationUser::isAnonymized)
            .findFirst()
            .orElseThrow();
        assertEquals(currentUser.getId(), anonymizedUser.getId());
        assertEquals("Deleted", anonymizedUser.getFirstName());
        assertEquals("User", anonymizedUser.getLastName());
        assertTrue(anonymizedUser.getEmail().startsWith("anonymized-user-" + currentUser.getId()));
        assertFalse(roommateRepository.findByUserIdAndActiveTrue(currentUser.getId()).isPresent());

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("delete@example.com");
        loginDto.setPassword("Password123!");
        MvcResult loginResult = this.mockMvc.perform(post("/api/v1/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(loginDto)))
            .andReturn();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), loginResult.getResponse().getStatus());

        MvcResult historyResult = this.mockMvc.perform(get("/api/v1/history")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("remaining@example.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), historyResult.getResponse().getStatus());
        String history = historyResult.getResponse().getContentAsString();
        assertTrue(history.contains("Historic task"));
        assertTrue(history.contains("Historic groceries"));
        assertTrue(history.contains("anonymized-user-" + currentUser.getId()));
        assertFalse(history.contains("delete@example.com"));

        MvcResult paymentOverviewResult = this.mockMvc.perform(get("/api/v1/payments/overview")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("remaining@example.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), paymentOverviewResult.getResponse().getStatus());
        String paymentOverview = paymentOverviewResult.getResponse().getContentAsString();
        assertTrue(paymentOverview.contains("Deleted User"));
        assertTrue(paymentOverview.contains("anonymized-user-" + currentUser.getId()));
        assertTrue(paymentOverview.contains("5.00"));
        assertFalse(paymentOverview.contains("delete@example.com"));
    }
}
