package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleActiveUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;

import java.util.List;

public interface CleaningScheduleService {

    List<CleaningSchedule> findVisibleSchedules(String currentUserEmail, boolean admin);

    List<CleaningSchedule> findActiveSchedules(String currentUserEmail, boolean admin);

    CleaningSchedule findOne(Long id, String currentUserEmail, boolean admin);

    CleaningSchedule create(CleaningScheduleCreateDto cleaningScheduleCreateDto, String currentUserEmail);

    CleaningSchedule update(Long id, CleaningScheduleUpdateDto cleaningScheduleUpdateDto, String currentUserEmail, boolean admin);

    CleaningSchedule updateActive(Long id, CleaningScheduleActiveUpdateDto cleaningScheduleActiveUpdateDto, String currentUserEmail, boolean admin);

    Task generateTask(Long id, String currentUserEmail, boolean admin);

    void delete(Long id, String currentUserEmail, boolean admin);
}