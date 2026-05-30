package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;

import java.util.List;

public interface TaskService {

    List<Task> findVisibleTasks(String currentUserEmail, boolean admin);

    List<Task> findAssignedToCurrentUser(String currentUserEmail);

    Task findOne(Long id, String currentUserEmail, boolean admin);

    Task create(TaskCreateDto taskCreateDto, String currentUserEmail);

    Task update(Long id, TaskUpdateDto taskUpdateDto, String currentUserEmail, boolean admin);

    Task updateStatus(Long id, TaskStatusUpdateDto taskStatusUpdateDto, String currentUserEmail, boolean admin);

    void delete(Long id, String currentUserEmail, boolean admin);
}