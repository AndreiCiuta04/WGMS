package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.SettlementDecision;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementDecisionRepository extends JpaRepository<SettlementDecision, Long> {

    @EntityGraph(attributePaths = {"household", "fromRoommate", "fromRoommate.user", "toRoommate", "toRoommate.user", "decidedBy", "decidedBy.user"})
    Optional<SettlementDecision> findTopByHouseholdIdAndFromRoommateIdAndToRoommateIdAndAmountOrderByDecidedAtDescIdDesc(
        Long householdId,
        Long fromRoommateId,
        Long toRoommateId,
        BigDecimal amount
    );
}
