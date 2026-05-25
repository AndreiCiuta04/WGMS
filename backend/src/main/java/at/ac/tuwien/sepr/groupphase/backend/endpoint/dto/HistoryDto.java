package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

public record HistoryDto(
    List<TaskHistoryDto> tasks,
    List<PaymentHistoryDto> payments
) {
}
