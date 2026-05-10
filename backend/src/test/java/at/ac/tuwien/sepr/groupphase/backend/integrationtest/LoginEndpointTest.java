package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class LoginEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    public void beforeEach() {
        userRepository.deleteAll();
    }

    @Test
    public void givenUser_whenLoginValidCredentials_thenReturns200AndToken() throws Exception {
        ApplicationUser user = new ApplicationUser("test@example.com", passwordEncoder.encode("Password123!"), "Test", "User", false);
        userRepository.save(user);

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("Password123!");

        String body = jsonMapper.writeValueAsString(loginDto);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().startsWith("Bearer "));

        ApplicationUser fetchedUser = userRepository.findUserByEmail("test@example.com");
        assertEquals(0, fetchedUser.getFailedLoginAttempts());
        assertNull(fetchedUser.getAccountLockedUntil());
    }

    @Test
    public void givenUser_whenLoginInvalidPassword_thenReturns401AndIncrementsAttempts() throws Exception {
        ApplicationUser user = new ApplicationUser("test@example.com", passwordEncoder.encode("Password123!"), "Test", "User", false);
        userRepository.save(user);

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("WrongPassword!");

        String body = jsonMapper.writeValueAsString(loginDto);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals("Username or password is incorrect", response.getContentAsString());

        ApplicationUser fetchedUser = userRepository.findUserByEmail("test@example.com");
        assertEquals(1, fetchedUser.getFailedLoginAttempts());
        assertNull(fetchedUser.getAccountLockedUntil());
    }

    @Test
    public void givenNoUser_whenLogin_thenReturns401() throws Exception {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("nonexistent@example.com");
        loginDto.setPassword("Password123!");

        String body = jsonMapper.writeValueAsString(loginDto);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals("Username or password is incorrect", response.getContentAsString());
    }

    @Test
    public void givenUser_whenBruteForceLogin_thenLocksAccountAndReturns401() throws Exception {
        ApplicationUser user = new ApplicationUser("test@example.com", passwordEncoder.encode("Password123!"), "Test", "User", false);
        userRepository.save(user);

        UserLoginDto invalidLoginDto = new UserLoginDto();
        invalidLoginDto.setEmail("test@example.com");
        invalidLoginDto.setPassword("WrongPassword!");
        String invalidBody = jsonMapper.writeValueAsString(invalidLoginDto);

        // 5 failed attempts
        for (int i = 0; i < 5; i++) {
            this.mockMvc.perform(post("/api/v1/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
                .andReturn();
        }

        ApplicationUser fetchedUser = userRepository.findUserByEmail("test@example.com");
        assertEquals(5, fetchedUser.getFailedLoginAttempts());
        assertNotNull(fetchedUser.getAccountLockedUntil());
        assertTrue(fetchedUser.getAccountLockedUntil().isAfter(LocalDateTime.now()));

        // 6th attempt even with correct password should be blocked
        UserLoginDto validLoginDto = new UserLoginDto();
        validLoginDto.setEmail("test@example.com");
        validLoginDto.setPassword("Password123!");
        String validBody = jsonMapper.writeValueAsString(validLoginDto);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/authentication")
            .contentType(MediaType.APPLICATION_JSON)
            .content(validBody))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertEquals("Account is locked due to too many failed attempts. Try again later.", response.getContentAsString());
    }
}
