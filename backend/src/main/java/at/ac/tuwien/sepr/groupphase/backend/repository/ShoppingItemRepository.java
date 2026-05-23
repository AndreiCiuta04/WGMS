package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {

    List<ShoppingItem> findAllByHouseholdIdOrderByCreatedAtDesc(Long householdId);

    List<ShoppingItem> findAllByHouseholdIdAndCreatedByIdOrderByCreatedAtDesc(
        Long householdId, Long createdById);
}
