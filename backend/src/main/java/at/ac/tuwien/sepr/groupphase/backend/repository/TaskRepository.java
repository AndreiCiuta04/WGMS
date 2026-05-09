package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOrderByDueDateAscCreatedAtDesc();

    List<Task> findByCreatedByEmailIgnoreCaseOrAssignedToEmailIgnoreCaseOrderByDueDateAscCreatedAtDesc(
        String createdByEmail,
        String assignedToEmail
    );

    List<Task> findByAssignedToEmailIgnoreCaseOrderByDueDateAscCreatedAtDesc(String assignedToEmail);
}