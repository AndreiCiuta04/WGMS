package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentOverviewDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PendingSettlementDto;
import at.ac.tuwien.sepr.groupphase.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping(value = "/api/v1/payments")
public class PaymentEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PaymentService paymentService;

    public PaymentEndpoint(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('USER')")
    public PaymentOverviewDto getOverview(Authentication authentication) {
        LOGGER.info("GET /api/v1/payments/overview");
        return paymentService.getOverview(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public PaymentDto createPayment(@Valid @RequestBody PaymentCreateDto paymentCreateDto, Authentication authentication) {
        LOGGER.info("POST /api/v1/payments");
        return paymentService.createPayment(paymentCreateDto, authentication.getName());
    }

    @GetMapping("/settlement-approvals")
    @PreAuthorize("hasRole('USER')")
    public List<PendingSettlementDto> getPendingSettlements(Authentication authentication) {
        LOGGER.info("GET /api/v1/payments/settlement-approvals");
        return paymentService.getPendingSettlements(authentication.getName());
    }

    @PostMapping("/settlement-approvals/{settlementId}/approve")
    @PreAuthorize("hasRole('USER')")
    public PendingSettlementDto approveSettlement(@PathVariable("settlementId") String settlementId, Authentication authentication) {
        LOGGER.info("POST /api/v1/payments/settlement-approvals/{}/approve", settlementId);
        return paymentService.approveSettlement(settlementId, authentication.getName());
    }

    @PostMapping("/settlement-approvals/{settlementId}/reject")
    @PreAuthorize("hasRole('USER')")
    public PendingSettlementDto rejectSettlement(@PathVariable("settlementId") String settlementId, Authentication authentication) {
        LOGGER.info("POST /api/v1/payments/settlement-approvals/{}/reject", settlementId);
        return paymentService.rejectSettlement(settlementId, authentication.getName());
    }
}
