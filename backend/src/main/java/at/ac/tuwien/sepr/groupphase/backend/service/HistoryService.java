package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.HistoryDto;

public interface HistoryService {

    HistoryDto getHistory(String currentUserEmail, boolean admin);
}