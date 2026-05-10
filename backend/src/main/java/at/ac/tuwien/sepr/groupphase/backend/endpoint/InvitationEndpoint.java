package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationAcceptResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationPreviewDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.service.InvitationService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1")
public class InvitationEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final InvitationService invitationService;
    private final UserService userService;

    @Autowired
    public InvitationEndpoint(InvitationService invitationService, UserService userService) {
        this.invitationService = invitationService;
        this.userService = userService;
    }

    @PostMapping("/households/{id}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public InvitationDto createInvitation(@PathVariable("id") Long id, @Valid @RequestBody InvitationCreateDto dto, Authentication authentication) {
        LOGGER.info("POST /api/v1/households/{}/invitations", id);
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        return invitationService.createInvitation(id, dto, currentUser);
    }

    @GetMapping("/households/{id}/invitations")
    @PreAuthorize("hasRole('USER')")
    public List<InvitationDto> getInvitations(@PathVariable("id") Long id, Authentication authentication) {
        LOGGER.info("GET /api/v1/households/{}/invitations", id);
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        return invitationService.getInvitations(id, currentUser);
    }

    @PermitAll
    @GetMapping("/invitations/{token}")
    public InvitationPreviewDto previewInvitation(@PathVariable("token") String token) {
        LOGGER.info("GET /api/v1/invitations/{}", token);
        return invitationService.previewInvitation(token);
    }

    @PostMapping("/invitations/{token}/accept")
    @PreAuthorize("hasRole('USER')")
    public InvitationAcceptResponseDto acceptInvitation(@PathVariable("token") String token, Authentication authentication) {
        LOGGER.info("POST /api/v1/invitations/{}/accept", token);
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        return invitationService.acceptInvitation(token, currentUser);
    }

    @DeleteMapping("/invitations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void deleteInvitation(@PathVariable("id") Long id, Authentication authentication) {
        LOGGER.info("DELETE /api/v1/invitations/{}", id);
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        invitationService.deleteInvitation(id, currentUser);
    }
}
