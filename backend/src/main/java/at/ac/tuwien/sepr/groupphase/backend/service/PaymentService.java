package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentOverviewDto;

public interface PaymentService {

    PaymentOverviewDto getOverview(String currentUserEmail);

    PaymentDto createPayment(PaymentCreateDto paymentCreateDto, String currentUserEmail);
}
