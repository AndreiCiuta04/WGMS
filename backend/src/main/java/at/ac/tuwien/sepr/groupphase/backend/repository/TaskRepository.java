package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @EntityGraph(attributePaths = {"household", "createdBy", "assignedTo", "cleaningSchedule"})
    Optional<Task> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"household", "createdBy", "assignedTo", "cleaningSchedule"})
    List<Task> findAllByHousehold_IdOrderByDueDateAscCreatedAtDesc(Long householdId);

    @EntityGraph(attributePaths = {"household", "createdBy", "assignedTo", "cleaningSchedule"})
    List<Task> findAllByHousehold_Id(Long householdId);

    @EntityGraph(attributePaths = {"household", "createdBy", "assignedTo", "cleaningSchedule"})
    @Query("""
        select t
        from Task t
        where t.household.id = :householdId
          and (
            t.status in :currentStatuses
            or (t.status = :doneStatus and t.updatedAt >= :historyStart)
          )
        """)
    List<Task> findFairBalancingTasksByHouseholdId(
        @Param("householdId") Long householdId,
        @Param("currentStatuses") Collection<TaskStatus> currentStatuses,
        @Param("doneStatus") TaskStatus doneStatus,
        @Param("historyStart") LocalDateTime historyStart
    );

    @EntityGraph(attributePaths = {"household", "createdBy", "assignedTo", "cleaningSchedule"})
    List<Task> findByHousehold_IdAndAssignedTo_IdOrderByDueDateAscCreatedAtDesc(
        Long householdId,
        Long assignedToId
    );

    long countByHousehold_IdAndAssignedTo_IdAndStatusInAndDueDateBefore(
        Long householdId,
        Long assignedToId,
        Collection<TaskStatus> statuses,
        LocalDate dueDate
    );

    @EntityGraph(attributePaths = {"household", "createdBy", "assignedTo", "cleaningSchedule"})
    List<Task> findAllByHousehold_IdAndStatusInOrderByUpdatedAtDescDueDateDescCreatedAtDesc(
        Long householdId,
        Collection<TaskStatus> statuses
    );

    boolean existsByCleaningSchedule_Id(Long cleaningScheduleId);

    boolean existsByCleaningSchedule_IdAndStatusNotIn(
        Long cleaningScheduleId,
        Collection<TaskStatus> statuses
    );
}
