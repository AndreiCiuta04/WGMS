package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DashboardDto;

public interface DashboardService {
    /**
     * Retrieves the central dashboard details for the currently logged-in roommate.
     *
     * @param currentUserEmail the email of the authenticated user
     * @return the dashboard data transfer object containing household metrics, chores, schedules and debts
     */
    DashboardDto getDashboardData(String currentUserEmail);
}
