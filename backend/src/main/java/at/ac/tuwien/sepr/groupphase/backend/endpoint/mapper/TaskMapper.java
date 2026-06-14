package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    TaskDetailDto taskToTaskDetailDto(Task task);

    List<TaskDetailDto> taskToTaskDetailDto(List<Task> tasks);
}