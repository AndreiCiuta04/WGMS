package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdInvitation;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdInvitationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.InvitationService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.InvitationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvitationServiceTest {

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private RoommateRepository roommateRepository;

    @Mock
    private HouseholdInvitationRepository invitationRepository;

    private InvitationService invitationService;

    private ApplicationUser mainTenantUser;
    private ApplicationUser roommateUser;
    private Household household;

    @BeforeEach
    public void setUp() {
        invitationService = new InvitationServiceImpl(householdRepository, roommateRepository, invitationRepository);
        ReflectionTestUtils.setField(invitationService, "frontendUrl", "http://localhost:4200");
        ReflectionTestUtils.setField(invitationService, "invitationDefaultExpiryHours", 168);

        mainTenantUser = new ApplicationUser();
        mainTenantUser.setId(1L);
        mainTenantUser.setEmail("main@tenant.com");

        roommateUser = new ApplicationUser();
        roommateUser.setId(2L);
        roommateUser.setEmail("roommate@test.com");

        household = new Household();
        household.setId(1L);
        household.setName("Test WG");
    }

    @Test
    public void givenMainTenant_whenCreateInvitation_thenSuccess() {
        // Arrange
        Roommate mainTenantRoommate = new Roommate();
        mainTenantRoommate.setUser(mainTenantUser);
        mainTenantRoommate.setHousehold(household);
        mainTenantRoommate.setRole(HouseholdRole.MAIN_TENANT);

        when(roommateRepository.findByUserIdAndHouseholdIdAndActiveTrue(1L, 1L))
            .thenReturn(Optional.of(mainTenantRoommate));
        when(householdRepository.findById(1L)).thenReturn(Optional.of(household));

        InvitationCreateDto dto = new InvitationCreateDto("invited@test.com", 24);

        // Act
        InvitationDto result = invitationService.createInvitation(1L, dto, mainTenantUser);

        // Assert
        assertNotNull(result);
        assertEquals("invited@test.com", result.invitedEmail());
        assertTrue(result.inviteUrl().contains("/#/invite/"));
        verify(invitationRepository, times(1)).save(any(HouseholdInvitation.class));
    }

    @Test
    public void givenRoommate_whenCreateInvitation_thenForbidden() {
        // Arrange
        Roommate normalRoommate = new Roommate();
        normalRoommate.setUser(roommateUser);
        normalRoommate.setHousehold(household);
        normalRoommate.setRole(HouseholdRole.ROOMMATE);

        when(roommateRepository.findByUserIdAndHouseholdIdAndActiveTrue(2L, 1L))
            .thenReturn(Optional.of(normalRoommate));

        InvitationCreateDto dto = new InvitationCreateDto("invited@test.com", 24);

        // Act & Assert
        assertThrows(ForbiddenException.class, () ->
            invitationService.createInvitation(1L, dto, roommateUser)
        );
    }

    @Test
    public void givenNonMember_whenCreateInvitation_thenForbidden() {
        // Arrange
        when(roommateRepository.findByUserIdAndHouseholdIdAndActiveTrue(2L, 1L))
            .thenReturn(Optional.empty());

        InvitationCreateDto dto = new InvitationCreateDto("invited@test.com", 24);

        // Act & Assert
        assertThrows(ForbiddenException.class, () ->
            invitationService.createInvitation(1L, dto, roommateUser)
        );
    }

    @Test
    public void givenValidToken_whenPreviewInvitation_thenSuccess() {
        // Arrange
        HouseholdInvitation invitation = new HouseholdInvitation();
        invitation.setHousehold(household);
        invitation.setInvitedEmail("invited@test.com");
        invitation.setExpiresAt(LocalDateTime.now().plusDays(1));

        // Use a dummy token and its hash (sha256 of "token123")
        String rawToken = "token123";
        String tokenHash = "b13ae34b9231f95675ff502761b64aedd0c278f5b6e8730ce99b72f35b2ddfb0";
        invitation.setTokenHash(tokenHash);

        when(invitationRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(invitation));

        // Act
        var result = invitationService.previewInvitation(rawToken);

        // Assert
        assertNotNull(result);
        assertEquals("Test WG", result.householdName());
        assertTrue(result.valid());
    }

    @Test
    public void givenValidToken_whenAcceptInvitation_thenSuccess() {
        // Arrange
        HouseholdInvitation invitation = new HouseholdInvitation();
        invitation.setHousehold(household);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(1));
        invitation.setRoleToAssign(HouseholdRole.ROOMMATE);

        String rawToken = "token123";
        String tokenHash = "b13ae34b9231f95675ff502761b64aedd0c278f5b6e8730ce99b72f35b2ddfb0";
        invitation.setTokenHash(tokenHash);

        when(invitationRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(invitation));
        when(roommateRepository.existsByUserIdAndActiveTrue(roommateUser.getId())).thenReturn(false);

        // Act
        var result = invitationService.acceptInvitation(rawToken, roommateUser);

        // Assert
        assertNotNull(result);
        assertEquals(household.getId(), result.householdId());
        verify(invitationRepository, times(1)).save(any(HouseholdInvitation.class));
    }

    @Test
    public void givenExpiredToken_whenAcceptInvitation_thenGone() {
        // Arrange
        HouseholdInvitation invitation = new HouseholdInvitation();
        invitation.setHousehold(household);
        invitation.setExpiresAt(LocalDateTime.now().minusHours(1));

        String rawToken = "token123";
        String tokenHash = "b13ae34b9231f95675ff502761b64aedd0c278f5b6e8730ce99b72f35b2ddfb0";
        invitation.setTokenHash(tokenHash);

        when(invitationRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(invitation));

        // Act & Assert
        assertThrows(at.ac.tuwien.sepr.groupphase.backend.exception.GoneException.class, () ->
            invitationService.acceptInvitation(rawToken, roommateUser)
        );
    }

    @Test
    public void givenUsedToken_whenAcceptInvitation_thenGone() {
        // Arrange
        HouseholdInvitation invitation = new HouseholdInvitation();
        invitation.setHousehold(household);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(1));
        invitation.setUsedAt(LocalDateTime.now().minusHours(1));

        String rawToken = "token123";
        String tokenHash = "b13ae34b9231f95675ff502761b64aedd0c278f5b6e8730ce99b72f35b2ddfb0";
        invitation.setTokenHash(tokenHash);

        when(invitationRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(invitation));

        // Act & Assert
        assertThrows(at.ac.tuwien.sepr.groupphase.backend.exception.GoneException.class, () ->
            invitationService.acceptInvitation(rawToken, roommateUser)
        );
    }

    @Test
    public void givenRestrictedToken_whenAcceptWithWrongEmail_thenForbidden() {
        // Arrange
        HouseholdInvitation invitation = new HouseholdInvitation();
        invitation.setHousehold(household);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(1));
        invitation.setInvitedEmail("other@test.com");

        String rawToken = "token123";
        String tokenHash = "b13ae34b9231f95675ff502761b64aedd0c278f5b6e8730ce99b72f35b2ddfb0";
        invitation.setTokenHash(tokenHash);

        when(invitationRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(invitation));
        when(roommateRepository.existsByUserIdAndActiveTrue(roommateUser.getId())).thenReturn(false);

        // Act & Assert
        assertThrows(at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException.class, () ->
            invitationService.acceptInvitation(rawToken, roommateUser)
        );
    }

    @Test
    public void givenUserAlreadyBelongsToHousehold_whenAcceptInvitation_thenConflictAndNoPartialSave() {
        // Arrange
        HouseholdInvitation invitation = new HouseholdInvitation();
        invitation.setHousehold(household);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(1));
        invitation.setRoleToAssign(HouseholdRole.ROOMMATE);

        String rawToken = "token123";
        String tokenHash = "b13ae34b9231f95675ff502761b64aedd0c278f5b6e8730ce99b72f35b2ddfb0";
        invitation.setTokenHash(tokenHash);

        when(invitationRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(invitation));
        when(roommateRepository.existsByUserIdAndActiveTrue(roommateUser.getId())).thenReturn(true);

        // Act & Assert
        assertThrows(at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException.class, () ->
            invitationService.acceptInvitation(rawToken, roommateUser)
        );

        verify(roommateRepository, never()).save(any(Roommate.class));
        verify(invitationRepository, never()).save(any(HouseholdInvitation.class));
    }
}
