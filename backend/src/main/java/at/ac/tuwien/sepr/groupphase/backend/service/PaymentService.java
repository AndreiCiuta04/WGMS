package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentOverviewDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PendingSettlementDto;
import java.util.List;

public interface PaymentService {

    PaymentOverviewDto getOverview(String currentUserEmail);

    PaymentDto createPayment(PaymentCreateDto paymentCreateDto, String currentUserEmail);

    List<PendingSettlementDto> getPendingSettlements(String currentUserEmail);

    PendingSettlementDto approveSettlement(String settlementId, String currentUserEmail);

    PendingSettlementDto rejectSettlement(String settlementId, String currentUserEmail);
}
