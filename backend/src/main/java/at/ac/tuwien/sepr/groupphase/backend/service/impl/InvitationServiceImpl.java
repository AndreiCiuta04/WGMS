package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationAcceptResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationPreviewDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdInvitation;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.GoneException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdInvitationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.InvitationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvitationServiceImpl implements InvitationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final HouseholdRepository householdRepository;
    private final RoommateRepository roommateRepository;
    private final HouseholdInvitationRepository invitationRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.invitation-default-expiry-hours:168}")
    private int invitationDefaultExpiryHours;

    @Autowired
    public InvitationServiceImpl(
        HouseholdRepository householdRepository,
        RoommateRepository roommateRepository,
        HouseholdInvitationRepository invitationRepository
    ) {
        this.householdRepository = householdRepository;
        this.roommateRepository = roommateRepository;
        this.invitationRepository = invitationRepository;
    }

    @Override
    @Transactional
    public InvitationDto createInvitation(Long householdId, InvitationCreateDto dto, ApplicationUser currentUser) {
        LOGGER.debug("Creating invitation for household {} by user {}", householdId, currentUser.getEmail());
        requireMainTenant(householdId, currentUser);

        String rawToken = generateRawToken();
        String tokenHash = sha256(rawToken);

        int expiryHours = dto.expiresInHours() != null
            ? dto.expiresInHours()
            : invitationDefaultExpiryHours;

        Household household = householdRepository.findById(householdId)
            .orElseThrow(() -> new NotFoundException("Household not found."));

        HouseholdInvitation invitation = new HouseholdInvitation();
        invitation.setHousehold(household);
        invitation.setCreatedBy(currentUser);
        invitation.setTokenHash(tokenHash);
        invitation.setInvitedEmail(dto.invitedEmail());
        invitation.setExpiresAt(LocalDateTime.now().plusHours(expiryHours));
        invitation.setRoleToAssign(HouseholdRole.ROOMMATE);

        invitationRepository.save(invitation);

        String inviteUrl = frontendUrl + "/#/invite/" + rawToken;

        return new InvitationDto(
            invitation.getId(),
            inviteUrl,
            invitation.getExpiresAt(),
            invitation.getInvitedEmail(),
            "ACTIVE"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationPreviewDto previewInvitation(String rawToken) {
        LOGGER.debug("Previewing invitation with token");
        String tokenHash = sha256(rawToken);

        HouseholdInvitation invitation = invitationRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() -> new NotFoundException("Invitation not found."));

        if (invitation.isExpired() || invitation.isUsed() || invitation.isRevoked()) {
            throw new GoneException("Invitation is no longer valid.");
        }

        Household household = invitation.getHousehold();

        return new InvitationPreviewDto(
            household.getName(),
            invitation.getInvitedEmail(),
            invitation.getExpiresAt(),
            true
        );
    }

    @Override
    @Transactional
    public InvitationAcceptResponseDto acceptInvitation(String rawToken, ApplicationUser currentUser) {
        LOGGER.debug("Accepting invitation for user {}", currentUser.getEmail());
        String tokenHash = sha256(rawToken);

        HouseholdInvitation invitation = invitationRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() -> new NotFoundException("Invitation not found."));

        if (invitation.isExpired()) {
            throw new GoneException("Invitation has expired.");
        }

        if (invitation.isUsed()) {
            throw new GoneException("Invitation has already been used.");
        }

        if (invitation.isRevoked()) {
            throw new GoneException("Invitation has been revoked.");
        }

        if (roommateRepository.existsByUserIdAndActiveTrue(currentUser.getId())) {
            throw new ConflictException("User already belongs to a household.");
        }

        if (
                invitation.getInvitedEmail() != null
                && !invitation.getInvitedEmail().isBlank()
                && !invitation.getInvitedEmail().equalsIgnoreCase(currentUser.getEmail())
        ) {
            throw new ForbiddenException("This invitation is not assigned to this email address.");
        }

        Long householdId = invitation.getHousehold().getId();
        Roommate roommate = roommateRepository
            .findByUserIdAndHouseholdId(currentUser.getId(), householdId)
            .orElse(null);

        if (roommate == null) {
            roommate = new Roommate();
            roommate.setUser(currentUser);
            roommate.setHousehold(invitation.getHousehold());
            roommate.setRole(invitation.getRoleToAssign());
            roommate.setActive(true);
        } else if (roommate.isActive()) {
            throw new ConflictException("User already belongs to a household.");
        } else {
            roommate.setActive(true);
            roommate.setRole(invitation.getRoleToAssign());
        }
        roommateRepository.save(roommate);

        invitation.setUsedAt(LocalDateTime.now());
        invitation.setUsedBy(currentUser);
        invitationRepository.save(invitation);

        return new InvitationAcceptResponseDto(
            invitation.getHousehold().getId(),
            invitation.getHousehold().getName(),
            HouseholdRole.ROOMMATE.name()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitationDto> getInvitations(Long householdId, ApplicationUser currentUser) {
        LOGGER.debug("Getting invitations for household {} by user {}", householdId, currentUser.getEmail());
        requireMainTenant(householdId, currentUser);

        return invitationRepository.findAllByHouseholdIdOrderByCreatedAtDesc(householdId).stream()
            .map(inv -> new InvitationDto(
                inv.getId(),
                null, // URL not available after creation
                inv.getExpiresAt(),
                inv.getInvitedEmail(),
                getInvitationStatus(inv)
            ))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteInvitation(Long invitationId, ApplicationUser currentUser) {
        LOGGER.debug("Deleting invitation {} by user {}", invitationId, currentUser.getEmail());

        HouseholdInvitation invitation = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new NotFoundException("Invitation not found."));

        requireMainTenant(invitation.getHousehold().getId(), currentUser);

        invitation.setRevokedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
    }

    private Roommate requireMainTenant(Long householdId, ApplicationUser user) {
        LOGGER.debug("Checking MAIN_TENANT status for user {} in household {}", user.getId(), householdId);
        Roommate roommate = roommateRepository
            .findByUserIdAndHouseholdIdAndActiveTrue(user.getId(), householdId)
            .orElseThrow(() -> {
                LOGGER.warn("User {} is not a member of household {}", user.getId(), householdId);
                return new ForbiddenException("Not a member of this household.");
            });

        if (roommate.getRole() != HouseholdRole.MAIN_TENANT) {
            LOGGER.warn("User {} is a member but not the MAIN_TENANT of household {}", user.getId(), householdId);
            throw new ForbiddenException("Only the main tenant can manage invitations.");
        }

        return roommate;
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String getInvitationStatus(HouseholdInvitation inv) {
        if (inv.isUsed()) {
            return "USED";
        }
        if (inv.isRevoked()) {
            return "REVOKED";
        }
        if (inv.isExpired()) {
            return "EXPIRED";
        }
        return "ACTIVE";
    }
}
