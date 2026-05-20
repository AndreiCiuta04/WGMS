package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"household", "createdBy", "assignedTo", "cleaningSchedule"})
    Optional<Task> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"household", "createdBy", "assignedTo", "cleaningSchedule"})
    List<Task> findAllByHousehold_IdOrderByDueDateAscCreatedAtDesc(Long householdId);

    @EntityGraph(attributePaths = {"household", "createdBy", "assignedTo", "cleaningSchedule"})
    List<Task> findByHousehold_IdAndAssignedTo_IdOrderByDueDateAscCreatedAtDesc(
        Long householdId,
        Long assignedToId
    );

    boolean existsByCleaningSchedule_Id(Long cleaningScheduleId);
}
