package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DashboardDto;
import at.ac.tuwien.sepr.groupphase.backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping(value = "/api/v1/dashboard")
public class DashboardEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final DashboardService dashboardService;

    public DashboardEndpoint(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Secured("ROLE_USER")
    @GetMapping
    @Operation(summary = "Get current responsibilities dashboard details", security = @SecurityRequirement(name = "apiKey"))
    public DashboardDto getDashboardData(Authentication authentication) {
        LOGGER.info("GET /api/v1/dashboard");
        return dashboardService.getDashboardData(authentication.getName());
    }
}
