package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface CleaningScheduleMapper {

    CleaningScheduleDetailDto cleaningScheduleToCleaningScheduleDetailDto(CleaningSchedule cleaningSchedule);

    List<CleaningScheduleDetailDto> cleaningScheduleToCleaningScheduleDetailDto(List<CleaningSchedule> cleaningSchedules);
}