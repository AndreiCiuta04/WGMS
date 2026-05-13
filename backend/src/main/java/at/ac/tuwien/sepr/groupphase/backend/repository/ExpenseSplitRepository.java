package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {

    List<ExpenseSplit> findAllByExpenseId(Long expenseId);

    List<ExpenseSplit> findAllByExpenseHouseholdId(Long householdId);

    List<ExpenseSplit> findAllByRoommateId(Long roommateId);
}
