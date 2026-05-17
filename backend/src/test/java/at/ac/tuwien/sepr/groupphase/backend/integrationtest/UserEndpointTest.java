package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegistrationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdInvitationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

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
    private JsonMapper jsonMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void beforeEach() {
        taskRepository.deleteAll();
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

        String body = jsonMapper.writeValueAsString(userRegistrationDto);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andDo(print())
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

        String body = jsonMapper.writeValueAsString(userRegistrationDto);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("User with email existing@example.com already exists"));
    }

    @Test
    public void givenNothing_whenRegisterInvalidPassword_thenReturns400() throws Exception {
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto();
        userRegistrationDto.setEmail("newuser@example.com");
        userRegistrationDto.setPassword("weak"); // Too short, no numbers, no special characters
        userRegistrationDto.setFirstName("New");
        userRegistrationDto.setLastName("User");

        String body = jsonMapper.writeValueAsString(userRegistrationDto);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        String content = response.getContentAsString();
        assertTrue(content.contains("Validation errors"));
        assertTrue(content.contains("Password must be at least 8 characters long") || content.contains("Password must contain at least one number and one special character"));
    }
}