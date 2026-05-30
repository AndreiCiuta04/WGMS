package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CleaningScheduleRepository extends JpaRepository<CleaningSchedule, Long> {

    @EntityGraph(attributePaths = {"household", "createdBy", "assignedTo"})
    Optional<CleaningSchedule> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"household", "createdBy", "assignedTo"})
    List<CleaningSchedule> findAllByHousehold_IdOrderByNextDueDateAscCreatedAtDesc(Long householdId);

    @EntityGraph(attributePaths = {"household", "createdBy", "assignedTo"})
    List<CleaningSchedule> findAllByHousehold_IdAndActiveTrueOrderByNextDueDateAscCreatedAtDesc(Long householdId);
}