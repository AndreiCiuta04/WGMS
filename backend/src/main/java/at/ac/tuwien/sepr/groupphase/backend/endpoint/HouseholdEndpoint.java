package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HouseholdCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HouseholdDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoommateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.service.HouseholdService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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
@RequestMapping(value = "/api/v1/households")
public class HouseholdEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final HouseholdService householdService;
    private final UserService userService;

    @Autowired
    public HouseholdEndpoint(HouseholdService householdService, UserService userService) {
        this.householdService = householdService;
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public HouseholdDto createHousehold(@Valid @RequestBody HouseholdCreateDto householdCreateDto, Authentication authentication) {
        LOGGER.info("POST /api/v1/households");
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        return householdService.createHousehold(householdCreateDto, currentUser);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public HouseholdDto getMyHousehold(Authentication authentication) {
        LOGGER.info("GET /api/v1/households/me");
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        return householdService.getMyHousehold(currentUser);
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasRole('USER')")
    public List<RoommateDto> getMembers(@PathVariable("id") Long id, Authentication authentication) {
        LOGGER.info("GET /api/v1/households/{}/members", id);
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        return householdService.getMembers(id, currentUser);
    }
}
