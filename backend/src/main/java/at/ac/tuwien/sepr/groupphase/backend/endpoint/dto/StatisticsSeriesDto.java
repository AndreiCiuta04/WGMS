package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

public record StatisticsSeriesDto(
    String key,
    String label,
    List<StatisticsDataPointDto> points
) {
}
