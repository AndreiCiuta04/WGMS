package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationPreviewDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class InvitationEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private RoommateRepository roommateRepository;

    @Autowired
    private HouseholdInvitationRepository invitationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CleaningScheduleRepository cleaningScheduleRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JsonMapper jsonMapper;

    private ApplicationUser userA;
    private ApplicationUser userB;
    private Household household;

    @BeforeEach
    public void beforeEach() {
        taskRepository.deleteAll();
        cleaningScheduleRepository.deleteAll();
        invitationRepository.deleteAll();
        roommateRepository.deleteAll();
        householdRepository.deleteAll();
        userRepository.deleteAll();

        userA = new ApplicationUser("userA@test.com", "pass", "User", "A", false);
        userB = new ApplicationUser("userB@test.com", "pass", "User", "B", false);
        userRepository.saveAll(List.of(userA, userB));

        household = new Household();
        household.setName("Integration WG");
        household.setCreatedBy(userA);
        householdRepository.save(household);

        Roommate roommateA = new Roommate();
        roommateA.setUser(userA);
        roommateA.setHousehold(household);
        roommateA.setRole(HouseholdRole.MAIN_TENANT);
        roommateA.setActive(true);
        roommateRepository.save(roommateA);
    }

    @Test
    public void givenMainTenant_whenCreateAndAcceptInvitation_thenSuccess() throws Exception {
        // 1. Create Invitation
        InvitationCreateDto createDto = new InvitationCreateDto("userB@test.com", 24);
        String createBody = jsonMapper.writeValueAsString(createDto);

        MvcResult createResult = this.mockMvc.perform(post("/api/v1/households/" + household.getId() + "/invitations")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userA@test.com", USER_ROLES))
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody))
            .andReturn();

        assertEquals(HttpStatus.CREATED.value(), createResult.getResponse().getStatus());
        InvitationDto invitationDto = jsonMapper.readValue(createResult.getResponse().getContentAsString(), InvitationDto.class);
        String rawToken = invitationDto.inviteUrl().substring(invitationDto.inviteUrl().lastIndexOf("/") + 1);

        // 2. Preview Invitation (Unauthenticated)
        MvcResult previewResult = this.mockMvc.perform(get("/api/v1/invitations/" + rawToken))
            .andReturn();
        assertEquals(HttpStatus.OK.value(), previewResult.getResponse().getStatus());
        InvitationPreviewDto previewDto = jsonMapper.readValue(previewResult.getResponse().getContentAsString(), InvitationPreviewDto.class);
        assertEquals("Integration WG", previewDto.householdName());

        // 3. Accept Invitation (User B)
        MvcResult acceptResult = this.mockMvc.perform(post("/api/v1/invitations/" + rawToken + "/accept")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userB@test.com", USER_ROLES)))
            .andReturn();
        assertEquals(HttpStatus.OK.value(), acceptResult.getResponse().getStatus());

        // 4. Verify Membership
        assertTrue(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(userB.getId(), household.getId()));
    }

    @Test
    public void givenRevokedInvitation_whenAccept_thenGone() throws Exception {
        // 1. Create Invitation
        InvitationCreateDto createDto = new InvitationCreateDto("userB@test.com", 24);
        String createBody = jsonMapper.writeValueAsString(createDto);

        MvcResult createResult = this.mockMvc.perform(post("/api/v1/households/" + household.getId() + "/invitations")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userA@test.com", USER_ROLES))
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody))
            .andReturn();

        InvitationDto invitationDto = jsonMapper.readValue(createResult.getResponse().getContentAsString(), InvitationDto.class);
        String rawToken = invitationDto.inviteUrl().substring(invitationDto.inviteUrl().lastIndexOf("/") + 1);

        // 2. Revoke Invitation (Delete)
        this.mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/invitations/" + invitationDto.id())
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userA@test.com", USER_ROLES)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent());

        // 3. Accept Invitation (User B) -> Should be GONE
        this.mockMvc.perform(post("/api/v1/invitations/" + rawToken + "/accept")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userB@test.com", USER_ROLES)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isGone());
    }

    @Test
    public void givenUnauthorizedUser_whenCreateInvitation_thenForbidden() throws Exception {
        InvitationCreateDto createDto = new InvitationCreateDto("userB@test.com", 24);
        String createBody = jsonMapper.writeValueAsString(createDto);

        // User B is not in a WG and tries to create an invitation for household A
        this.mockMvc.perform(post("/api/v1/households/" + household.getId() + "/invitations")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userB@test.com", USER_ROLES))
            .contentType(MediaType.APPLICATION_JSON)
            .content(createBody))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void givenMainTenantWithInvitation_whenGetInvitations_thenReturnsInvitations() throws Exception {
        InvitationCreateDto createDto = new InvitationCreateDto("userB@test.com", 24);
        String createBody = jsonMapper.writeValueAsString(createDto);

        this.mockMvc.perform(post("/api/v1/households/" + household.getId() + "/invitations")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userA@test.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andReturn();

        MvcResult listResult = this.mockMvc.perform(get("/api/v1/households/" + household.getId() + "/invitations")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userA@test.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), listResult.getResponse().getStatus());

        InvitationDto[] invitations = jsonMapper.readValue(listResult.getResponse().getContentAsString(), InvitationDto[].class);

        assertEquals(1, invitations.length);
        assertEquals("userB@test.com", invitations[0].invitedEmail());
        assertEquals("ACTIVE", invitations[0].status());
    }

    @Test
    public void givenRoommate_whenGetInvitations_thenForbidden() throws Exception {
        saveRoommate(userB, household, HouseholdRole.ROOMMATE);
        createInvitation("invitee@test.com");

        this.mockMvc.perform(get("/api/v1/households/" + household.getId() + "/invitations")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userB@test.com", USER_ROLES)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void givenRoommate_whenDeleteInvitation_thenForbiddenAndInvitationRemainsActive() throws Exception {
        saveRoommate(userB, household, HouseholdRole.ROOMMATE);
        InvitationDto invitationDto = createInvitation("invitee@test.com");

        this.mockMvc.perform(delete("/api/v1/invitations/" + invitationDto.id())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userB@test.com", USER_ROLES)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());

        assertFalse(invitationRepository.findById(invitationDto.id()).orElseThrow().isRevoked());
    }

    @Test
    public void givenNonMember_whenGetInvitations_thenForbidden() throws Exception {
        createInvitation("invitee@test.com");

        this.mockMvc.perform(get("/api/v1/households/" + household.getId() + "/invitations")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userB@test.com", USER_ROLES)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void givenNonMember_whenDeleteInvitation_thenForbiddenAndInvitationRemainsActive() throws Exception {
        InvitationDto invitationDto = createInvitation("invitee@test.com");

        this.mockMvc.perform(delete("/api/v1/invitations/" + invitationDto.id())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userB@test.com", USER_ROLES)))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());

        assertFalse(invitationRepository.findById(invitationDto.id()).orElseThrow().isRevoked());
    }

    @Test
    public void givenAcceptedInvitation_whenAcceptAgain_then410AndNoDuplicateMembership() throws Exception {
        // 1. Create invitation
        InvitationCreateDto createDto = new InvitationCreateDto("userB@test.com", 24);
        String createBody = jsonMapper.writeValueAsString(createDto);

        MvcResult createResult = this.mockMvc.perform(post("/api/v1/households/" + household.getId() + "/invitations")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userA@test.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andReturn();

        assertEquals(HttpStatus.CREATED.value(), createResult.getResponse().getStatus());

        InvitationDto invitationDto = jsonMapper.readValue(createResult.getResponse().getContentAsString(), InvitationDto.class);
        String rawToken = invitationDto.inviteUrl().substring(invitationDto.inviteUrl().lastIndexOf("/") + 1);

        // 2. First accept should work
        MvcResult firstAccept = this.mockMvc.perform(post("/api/v1/invitations/" + rawToken + "/accept")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userB@test.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), firstAccept.getResponse().getStatus());

        // 3. Second accept should be rejected because the invitation is single-use
        MvcResult secondAccept = this.mockMvc.perform(post("/api/v1/invitations/" + rawToken + "/accept")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userB@test.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.GONE.value(), secondAccept.getResponse().getStatus());

        // 4. Verify userB is active in the household and no duplicate active membership was created
        int activeMembersAfterSecondAccept = roommateRepository
            .findAllByHouseholdIdAndActiveTrue(household.getId())
            .size();

        assertTrue(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(userB.getId(), household.getId()));
        assertEquals(2, activeMembersAfterSecondAccept);
    }

    private InvitationDto createInvitation(String invitedEmail) throws Exception {
        InvitationCreateDto createDto = new InvitationCreateDto(invitedEmail, 24);
        String createBody = jsonMapper.writeValueAsString(createDto);

        MvcResult createResult = this.mockMvc.perform(post("/api/v1/households/" + household.getId() + "/invitations")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userA@test.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody))
            .andReturn();

        assertEquals(HttpStatus.CREATED.value(), createResult.getResponse().getStatus());
        return jsonMapper.readValue(createResult.getResponse().getContentAsString(), InvitationDto.class);
    }

    private Roommate saveRoommate(ApplicationUser user, Household household, HouseholdRole role) {
        Roommate roommate = new Roommate();
        roommate.setUser(user);
        roommate.setHousehold(household);
        roommate.setRole(role);
        roommate.setActive(true);
        return roommateRepository.save(roommate);
    }
}
