package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingStatisticsDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsFilterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.StatisticsOverviewDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskStatisticsDto;

public interface HistoryStatisticsService {

    StatisticsOverviewDto getOverview(String currentUserEmail, StatisticsFilterDto filter);

    TaskStatisticsDto getTaskStatistics(String currentUserEmail, StatisticsFilterDto filter);

    ExpenseStatisticsDto getExpenseStatistics(String currentUserEmail, StatisticsFilterDto filter);

    ShoppingStatisticsDto getShoppingStatistics(String currentUserEmail, StatisticsFilterDto filter);
}
