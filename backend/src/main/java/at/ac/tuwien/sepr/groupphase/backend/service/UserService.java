package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserProfileUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserService extends UserDetailsService {

    /**
     * Find a user in the context of Spring Security based on the email address.
     * <br>
     * For more information have a look at this tutorial:
     * https://www.baeldung.com/spring-security-authentication-with-a-database
     *
     * @param email the email address
     * @return a Spring Security user
     * @throws UsernameNotFoundException is thrown if the specified user does not exists
     */
    @Override
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;

    /**
     * Find an application user based on the email address.
     *
     * @param email the email address
     * @return a application user
     */
    ApplicationUser findApplicationUserByEmail(String email);

    /**
     * Get safe profile/account data for the authenticated user.
     *
     * @param email email from the authenticated principal
     * @return profile data safe to return to the user
     */
    UserProfileDto getCurrentUserProfile(String email);

    /**
     * Correct editable profile fields for the authenticated user.
     *
     * @param email email from the authenticated principal
     * @param userProfileUpdateDto new editable profile data
     * @return updated safe profile data
     */
    UserProfileDto updateCurrentUserProfile(String email, UserProfileUpdateDto userProfileUpdateDto);

    /**
     * Anonymize the authenticated user's account while preserving historical domain records.
     *
     * @param email email from the authenticated principal
     */
    void anonymizeCurrentUser(String email);

    /**
     * Leave the authenticated user's current active household membership.
     *
     * @param email email from the authenticated principal
     * @return updated safe profile data
     */
    UserProfileDto leaveCurrentHousehold(String email);

    /**
     * Log in a user.
     *
     * @param userLoginDto login credentials
     * @return the JWT, if successful
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are bad
     */
    String login(UserLoginDto userLoginDto);

    /**
     * Register a new user.
     *
     * @param userRegistrationDto registration credentials
     * @return the JWT, if successful login directly after registration, or simply the registered user email
     */
    String register(at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegistrationDto userRegistrationDto);
}
