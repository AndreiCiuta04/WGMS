package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HouseholdCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HouseholdDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoommateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import java.util.List;

public interface HouseholdService {

    /**
     * Create a new household and assign the current user as MAIN_TENANT.
     *
     * @param dto the household creation data
     * @param currentUser the user creating the household
     * @return the created household DTO
     */
    HouseholdDto createHousehold(HouseholdCreateDto dto, ApplicationUser currentUser);

    /**
     * Get the household of the current user.
     *
     * @param currentUser the current user
     * @return the household DTO
     */
    HouseholdDto getMyHousehold(ApplicationUser currentUser);

    /**
     * Get all members of a household.
     *
     * @param householdId the ID of the household
     * @param currentUser the current user (must be a member)
     * @return a list of roommate DTOs
     */
    List<RoommateDto> getMembers(Long householdId, ApplicationUser currentUser);
}
