package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HistoryDto;
import at.ac.tuwien.sepr.groupphase.backend.service.HistoryService;
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
@RequestMapping(value = "/api/v1/history")
public class HistoryEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final HistoryService historyService;

    public HistoryEndpoint(HistoryService historyService) {
        this.historyService = historyService;
    }

    @Secured("ROLE_USER")
    @GetMapping
    @Operation(summary = "Get task and payment history for the current household", security = @SecurityRequirement(name = "apiKey"))
    public HistoryDto getHistory(Authentication authentication) {
        LOGGER.info("GET /api/v1/history");
        return historyService.getHistory(getCurrentUserEmail(authentication), isAdmin(authentication));
    }

    private String getCurrentUserEmail(Authentication authentication) {
        return authentication.getName();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities()
            .stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}