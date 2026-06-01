package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"household", "fromRoommate", "fromRoommate.user", "toRoommate", "toRoommate.user", "createdBy", "createdBy.user"})
    List<Payment> findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(Long householdId);

    boolean existsByHouseholdId(Long householdId);

    @EntityGraph(attributePaths = {"household", "fromRoommate", "fromRoommate.user", "toRoommate", "toRoommate.user", "createdBy", "createdBy.user"})
    @Query("""
        SELECT p
        FROM Payment p
        WHERE p.household.id = :householdId
          AND (p.fromRoommate.id = :roommateId OR p.toRoommate.id = :roommateId)
        ORDER BY p.date DESC, p.createdAt DESC, p.id DESC
        """)
    List<Payment> findAllInvolvingRoommate(
        @Param("householdId") Long householdId,
        @Param("roommateId") Long roommateId
    );
}
