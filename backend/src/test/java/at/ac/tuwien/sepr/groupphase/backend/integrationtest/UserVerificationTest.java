package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestDbCleaner;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegistrationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserVerifyDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.VerificationCode;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationSettingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.VerificationCodeRepository;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "wgms.mail.verification-enabled=true",
    "wgms.mail.provider=mock"
})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserVerificationTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    public void beforeEach() {
        verificationCodeRepository.deleteAll();
        TestDbCleaner.clearNotificationData(notificationRepository, notificationSettingRepository);
        userRepository.deleteAll();
    }

    @Test
    public void givenVerificationEnabled_whenRegisterUser_thenSavesUnverifiedAndReturns403VerificationRequired() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setEmail("verifytest@example.com");
        registrationDto.setPassword("Password123!");
        registrationDto.setFirstName("Verify");
        registrationDto.setLastName("Test");
        registrationDto.setTermsAccepted(true);

        String body = jsonMapper.writeValueAsString(registrationDto);

        MvcResult result = this.mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertEquals("VERIFICATION_REQUIRED", response.getContentAsString());

        ApplicationUser savedUser = userRepository.findUserByEmail("verifytest@example.com");
        assertNotNull(savedUser);
        assertFalse(savedUser.getVerified());

        Optional<VerificationCode> codeOpt = verificationCodeRepository.findByEmail("verifytest@example.com");
        assertTrue(codeOpt.isPresent());
        assertEquals(6, codeOpt.get().getCode().length());
    }

    @Test
    public void givenUnverifiedUser_whenVerifyCorrectCode_thenUserVerifiedAndLoginTokenReturned() throws Exception {
        ApplicationUser unverifiedUser = new ApplicationUser("verifytest@example.com", passwordEncoder.encode("Password123!"), "Verify", "Test", false);
        unverifiedUser.setVerified(false);
        userRepository.save(unverifiedUser);

        VerificationCode code = new VerificationCode("verifytest@example.com", "123456", java.time.LocalDateTime.now().plusMinutes(15));
        verificationCodeRepository.save(code);

        UserVerifyDto verifyDto = new UserVerifyDto();
        verifyDto.setEmail("verifytest@example.com");
        verifyDto.setCode("123456");

        String body = jsonMapper.writeValueAsString(verifyDto);

        MvcResult result = this.mockMvc.perform(post("/api/v1/users/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().startsWith("Bearer "));

        ApplicationUser activatedUser = userRepository.findUserByEmail("verifytest@example.com");
        assertTrue(activatedUser.getVerified());

        assertFalse(verificationCodeRepository.findByEmail("verifytest@example.com").isPresent());
    }

    @Test
    public void givenUnverifiedUser_whenVerifyExpiredCode_thenReturns400() throws Exception {
        ApplicationUser unverifiedUser = new ApplicationUser("verifytest@example.com", passwordEncoder.encode("Password123!"), "Verify", "Test", false);
        unverifiedUser.setVerified(false);
        userRepository.save(unverifiedUser);

        VerificationCode code = new VerificationCode("verifytest@example.com", "123456", java.time.LocalDateTime.now().minusMinutes(5));
        verificationCodeRepository.save(code);

        UserVerifyDto verifyDto = new UserVerifyDto();
        verifyDto.setEmail("verifytest@example.com");
        verifyDto.setCode("123456");

        String body = jsonMapper.writeValueAsString(verifyDto);

        MvcResult result = this.mockMvc.perform(post("/api/v1/users/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("Invalid request.", response.getContentAsString()); // GlobalExceptionHandler maps ValidationException to "Invalid request."

        ApplicationUser activatedUser = userRepository.findUserByEmail("verifytest@example.com");
        assertFalse(activatedUser.getVerified());
    }

    @Test
    public void givenUnverifiedUser_whenLogin_thenSendsNewCodeAndReturns403VerificationRequired() throws Exception {
        ApplicationUser unverifiedUser = new ApplicationUser("verifytest@example.com", passwordEncoder.encode("Password123!"), "Verify", "Test", false);
        unverifiedUser.setVerified(false);
        userRepository.save(unverifiedUser);

        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("verifytest@example.com");
        loginDto.setPassword("Password123!");

        String body = jsonMapper.writeValueAsString(loginDto);

        MvcResult result = this.mockMvc.perform(post("/api/v1/authentication")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertEquals("VERIFICATION_REQUIRED", response.getContentAsString());

        assertTrue(verificationCodeRepository.findByEmail("verifytest@example.com").isPresent());
    }
}
