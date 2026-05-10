package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdInvitationRepository extends JpaRepository<HouseholdInvitation, Long> {

    Optional<HouseholdInvitation> findByTokenHash(String tokenHash);

    List<HouseholdInvitation> findAllByHouseholdIdOrderByCreatedAtDesc(Long householdId);
}
