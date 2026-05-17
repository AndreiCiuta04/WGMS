package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HouseholdCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HouseholdDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class HouseholdEndpointTest implements TestData {

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
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    public void beforeEach() {
        taskRepository.deleteAll();
        invitationRepository.deleteAll();
        roommateRepository.deleteAll();
        householdRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void givenUserWithoutHousehold_whenCreateHousehold_thenReturns201AndCreatesMainTenantMembership() throws Exception {
        ApplicationUser user = new ApplicationUser("creator@test.com", "pass", "Creator", "User", false);
        userRepository.save(user);

        HouseholdCreateDto dto = new HouseholdCreateDto("My WG", "Address 1");
        String body = jsonMapper.writeValueAsString(dto);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/households")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("creator@test.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());

        HouseholdDto result = jsonMapper.readValue(response.getContentAsString(), HouseholdDto.class);
        assertEquals("My WG", result.name());
        assertEquals("Address 1", result.address());
        assertEquals(HouseholdRole.MAIN_TENANT.name(), result.currentUserRole());

        assertTrue(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(user.getId(), result.id()));
    }

    @Test
    public void givenUserAlreadyInHousehold_whenCreateHousehold_thenReturns409() throws Exception {
        ApplicationUser user = new ApplicationUser("creator@test.com", "pass", "Creator", "User", false);
        userRepository.save(user);

        Household existingHousehold = new Household();
        existingHousehold.setName("Existing WG");
        existingHousehold.setCreatedBy(user);
        householdRepository.save(existingHousehold);

        Roommate roommate = new Roommate();
        roommate.setUser(user);
        roommate.setHousehold(existingHousehold);
        roommate.setRole(HouseholdRole.MAIN_TENANT);
        roommate.setActive(true);
        roommateRepository.save(roommate);

        HouseholdCreateDto dto = new HouseholdCreateDto("Second WG", "Address 2");
        String body = jsonMapper.writeValueAsString(dto);

        MvcResult mvcResult = this.mockMvc.perform(post("/api/v1/households")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("creator@test.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }
}