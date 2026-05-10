package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationAcceptResponseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InvitationPreviewDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import java.util.List;

public interface InvitationService {

    /**
     * Create a new invitation for a household.
     *
     * @param householdId the ID of the household
     * @param dto the invitation creation data
     * @param currentUser the user creating the invitation (must be MAIN_TENANT)
     * @return the created invitation DTO
     */
    InvitationDto createInvitation(Long householdId, InvitationCreateDto dto, ApplicationUser currentUser);

    /**
     * Get a preview of an invitation without requiring authentication.
     *
     * @param rawToken the raw invitation token
     * @return the invitation preview DTO
     */
    InvitationPreviewDto previewInvitation(String rawToken);

    /**
     * Accept an invitation and join the household.
     *
     * @param rawToken the raw invitation token
     * @param currentUser the user accepting the invitation
     * @return the response DTO containing household info
     */
    InvitationAcceptResponseDto acceptInvitation(String rawToken, ApplicationUser currentUser);

    /**
     * Get all invitations for a household.
     *
     * @param householdId the ID of the household
     * @param currentUser the current user (must be MAIN_TENANT)
     * @return a list of invitation DTOs
     */
    List<InvitationDto> getInvitations(Long householdId, ApplicationUser currentUser);

    /**
     * Delete/revoke an invitation.
     *
     * @param invitationId the ID of the invitation
     * @param currentUser the current user (must be MAIN_TENANT)
     */
    void deleteInvitation(Long invitationId, ApplicationUser currentUser);
}
