package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoommateRepository extends JpaRepository<Roommate, Long> {

    boolean existsByUserIdAndActiveTrue(Long userId);

    Optional<Roommate> findByUserIdAndActiveTrue(Long userId);

    List<Roommate> findAllByHouseholdIdAndActiveTrue(Long householdId);

    @EntityGraph(attributePaths = {"user", "household"})
    List<Roommate> findAllByHousehold_IdAndActiveTrue(Long householdId);

    boolean existsByUserIdAndHouseholdIdAndActiveTrue(Long userId, Long householdId);

    Optional<Roommate> findByUserIdAndHouseholdId(
        Long userId,
        Long householdId
    );

    Optional<Roommate> findByUserIdAndHouseholdIdAndActiveTrue(
        Long userId,
        Long householdId
    );

    Optional<Roommate> findByIdAndHouseholdIdAndActiveTrue(Long id, Long householdId);
}
