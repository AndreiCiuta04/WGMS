package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @EntityGraph(attributePaths = {"household", "paidBy", "paidBy.user", "createdBy", "createdBy.user"})
    List<Expense> findAllByHouseholdIdOrderByDateDescCreatedAtDesc(Long householdId);

    Optional<Expense> findByIdAndHouseholdId(Long id, Long householdId);

    boolean existsByIdAndHouseholdId(Long id, Long householdId);

    @Query("SELECT COALESCE(MAX(e.id), 0) FROM Expense e WHERE e.household.id = :householdId")
    Long findMaxIdByHouseholdId(@Param("householdId") Long householdId);
}
