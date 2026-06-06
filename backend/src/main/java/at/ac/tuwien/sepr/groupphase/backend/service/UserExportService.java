package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserDataExportDto;

public interface UserExportService {

    /**
     * Export safe machine-readable data for the authenticated user.
     *
     * @param email email from the authenticated principal
     * @return safe user data export
     */
    UserDataExportDto exportCurrentUser(String email);
}
