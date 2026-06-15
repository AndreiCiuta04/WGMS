package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserProfileUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegistrationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserDataExportDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserVerifyDto;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserExportService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(value = "/api/v1/users")
public class UserEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserService userService;
    private final UserExportService userExportService;

    public UserEndpoint(UserService userService, UserExportService userExportService) {
        this.userService = userService;
        this.userExportService = userExportService;
    }

    @PermitAll
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String register(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        LOGGER.info("POST /api/v1/users");
        return userService.register(userRegistrationDto);
    }

    @PermitAll
    @PostMapping("/verify")
    public String verify(@Valid @RequestBody UserVerifyDto userVerifyDto) {
        LOGGER.info("POST /api/v1/users/verify");
        return userService.verify(userVerifyDto);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public UserProfileDto getCurrentUserProfile(Authentication authentication) {
        LOGGER.info("GET /api/v1/users/me");
        return userService.getCurrentUserProfile(authentication.getName());
    }

    @GetMapping("/me/export")
    @PreAuthorize("hasRole('USER')")
    public UserDataExportDto exportCurrentUserData(Authentication authentication) {
        LOGGER.info("GET /api/v1/users/me/export");
        return userExportService.exportCurrentUser(authentication.getName());
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public UserProfileDto updateCurrentUserProfile(
        @Valid @RequestBody UserProfileUpdateDto userProfileUpdateDto,
        Authentication authentication
    ) {
        LOGGER.info("PATCH /api/v1/users/me");
        return userService.updateCurrentUserProfile(authentication.getName(), userProfileUpdateDto);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void anonymizeCurrentUser(Authentication authentication) {
        LOGGER.info("DELETE /api/v1/users/me");
        userService.anonymizeCurrentUser(authentication.getName());
    }

    @DeleteMapping("/me/household")
    @PreAuthorize("hasRole('USER')")
    public UserProfileDto leaveCurrentHousehold(Authentication authentication) {
        LOGGER.info("DELETE /api/v1/users/me/household");
        return userService.leaveCurrentHousehold(authentication.getName());
    }
}
