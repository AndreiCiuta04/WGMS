package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Finders are split per axis so the service can compose the visible-items set
 * explicitly. With FR-02's Household entity in place, the SHARED branch is
 * scoped to a single household — no global fan-out.
 */
@Repository
public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {

    @Query("SELECT s FROM ShoppingItem s WHERE s.household.id = :householdId AND "
           + "(s.createdBy.id = :userId OR s.targetUser.id = :userId OR s.visibility = :sharedVisibility) "
           + "ORDER BY s.createdAt DESC")
    List<ShoppingItem> findVisibleItems(@Param("householdId") Long householdId,
                                        @Param("userId") Long userId,
                                        @Param("sharedVisibility") ShoppingItemVisibility sharedVisibility);

    List<ShoppingItem> findAllByHouseholdIdOrderByCreatedAtDesc(Long householdId);

    List<ShoppingItem> findAllByHouseholdIdAndCreatedByIdOrderByCreatedAtDesc(
        Long householdId, Long createdById);

    List<ShoppingItem> findAllByHouseholdIdAndTargetUserIdOrderByCreatedAtDesc(
        Long householdId, Long targetUserId);

    List<ShoppingItem> findAllByHouseholdIdAndVisibilityOrderByCreatedAtDesc(
        Long householdId, ShoppingItemVisibility visibility);

    List<ShoppingItem> findAllByHouseholdIdAndVisibilityAndStatusOrderByCreatedAtDesc(
        Long householdId,
        ShoppingItemVisibility visibility,
        ShoppingItemStatus status);
}
