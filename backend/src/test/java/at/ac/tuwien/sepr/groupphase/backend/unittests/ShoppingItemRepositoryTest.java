package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Repository tests for FR-08 visibility/target-user derived queries and FR-07 derived queries.
 */
@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class ShoppingItemRepositoryTest {

    @Autowired private ShoppingItemRepository shoppingItemRepository;
    @Autowired private HouseholdRepository householdRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private ApplicationUser userA;
    private ApplicationUser userB;
    private Household householdA;
    private Household householdB;

    @BeforeEach
    public void setUp() {
        userA = userRepository.save(new ApplicationUser("a@test.com", "x", "User", "A", false));
        userB = userRepository.save(new ApplicationUser("b@test.com", "x", "User", "B", false));

        householdA = new Household();
        householdA.setName("HH A");
        householdA.setCreatedBy(userA);
        householdA = householdRepository.save(householdA);

        householdB = new Household();
        householdB.setName("HH B");
        householdB.setCreatedBy(userB);
        householdB = householdRepository.save(householdB);
    }

    @Test
    public void givenItemsInTwoHouseholds_whenFindByHouseholdId_thenOnlyOwnHouseholdReturned() {
        save("Bread", householdA, userA, null, ShoppingItemVisibility.PERSONAL, LocalDateTime.now().minusMinutes(2));
        save("Eggs", householdA, userA, null, ShoppingItemVisibility.PERSONAL, LocalDateTime.now().minusMinutes(1));
        save("Soap", householdB, userB, null, ShoppingItemVisibility.PERSONAL, LocalDateTime.now());

        List<ShoppingItem> aItems =
            shoppingItemRepository.findAllByHouseholdIdOrderByCreatedAtDesc(householdA.getId());

        assertEquals(2, aItems.size());
        assertTrue(aItems.stream().allMatch(i -> i.getHousehold().getId().equals(householdA.getId())));
    }

    @Test
    public void givenMultipleItems_whenFindByHousehold_thenOrderedByCreatedAtDesc() {
        save("Old", householdA, userA, null, ShoppingItemVisibility.PERSONAL, LocalDateTime.now().minusHours(2));
        save("New", householdA, userA, null, ShoppingItemVisibility.PERSONAL, LocalDateTime.now());

        List<ShoppingItem> items =
            shoppingItemRepository.findAllByHouseholdIdOrderByCreatedAtDesc(householdA.getId());

        assertEquals(2, items.size());
        assertEquals("New", items.get(0).getName());
        assertEquals("Old", items.get(1).getName());
    }

    @Test
    public void givenItemsFromTwoCreatorsInSameHousehold_whenFindByHouseholdAndCreator_thenOnlyOwnReturned() {
        ApplicationUser userC = userRepository.save(
            new ApplicationUser("c@test.com", "x", "User", "C", false));

        save("From A", householdA, userA, null, ShoppingItemVisibility.PERSONAL, LocalDateTime.now());
        save("From C", householdA, userC, null, ShoppingItemVisibility.PERSONAL, LocalDateTime.now());

        List<ShoppingItem> aItems = shoppingItemRepository
            .findAllByHouseholdIdAndCreatedByIdOrderByCreatedAtDesc(householdA.getId(), userA.getId());

        assertEquals(1, aItems.size());
        assertEquals("From A", aItems.get(0).getName());
    }

    @Test
    public void findByHouseholdAndTargetUser_returnsOnlyItemsTargetedAtThatUser() {
        save("For B", householdA, userA, userB, ShoppingItemVisibility.TARGETED, LocalDateTime.now());
        save("For A", householdA, userB, userA, ShoppingItemVisibility.TARGETED, LocalDateTime.now());

        List<ShoppingItem> forB = shoppingItemRepository
            .findAllByHouseholdIdAndTargetUserIdOrderByCreatedAtDesc(householdA.getId(), userB.getId());

        assertEquals(1, forB.size());
        assertEquals("For B", forB.get(0).getName());
    }

    @Test
    public void findByHouseholdAndVisibility_returnsOnlyMatchingVisibility() {
        save("Personal", householdA, userA, null, ShoppingItemVisibility.PERSONAL, LocalDateTime.now());
        save("Shared 1", householdA, userA, null, ShoppingItemVisibility.SHARED, LocalDateTime.now());
        save("Shared 2", householdA, userB, null, ShoppingItemVisibility.SHARED, LocalDateTime.now());

        List<ShoppingItem> shared = shoppingItemRepository
            .findAllByHouseholdIdAndVisibilityOrderByCreatedAtDesc(
                householdA.getId(), ShoppingItemVisibility.SHARED);

        assertEquals(2, shared.size());
        assertTrue(shared.stream().allMatch(i -> i.getVisibility() == ShoppingItemVisibility.SHARED));
    }

    @Test
    public void findByHouseholdAndVisibility_isBoundedByHousehold() {
        // SHARED items in another household are NOT returned for householdA.
        save("Shared HH-B", householdB, userB, null, ShoppingItemVisibility.SHARED, LocalDateTime.now());

        List<ShoppingItem> sharedForA = shoppingItemRepository
            .findAllByHouseholdIdAndVisibilityOrderByCreatedAtDesc(
                householdA.getId(), ShoppingItemVisibility.SHARED);

        assertTrue(sharedForA.isEmpty());
    }

    private ShoppingItem save(String name, Household household, ApplicationUser creator,
                              ApplicationUser target, ShoppingItemVisibility visibility,
                              LocalDateTime createdAt) {
        ShoppingItem item = new ShoppingItem();
        item.setName(name);
        item.setHousehold(household);
        item.setCreatedBy(creator);
        item.setTargetUser(target);
        item.setVisibility(visibility);
        item.setStatus(ShoppingItemStatus.NEEDED);
        item.setCreatedAt(createdAt);
        item.setUpdatedAt(createdAt);
        return shoppingItemRepository.save(item);
    }

    @Test
    public void findByHouseholdAndVisibilityAndStatus_returnsOnlySharedNeededItems() {
        save("Milk", householdA, userA, null, ShoppingItemVisibility.SHARED, LocalDateTime.now());
        ShoppingItem bought = save("Bread", householdA, userA, null, ShoppingItemVisibility.SHARED, LocalDateTime.now());
        bought.setStatus(ShoppingItemStatus.BOUGHT);
        shoppingItemRepository.save(bought);

        save("Private", householdA, userA, null, ShoppingItemVisibility.PERSONAL, LocalDateTime.now());
        save("Other household", householdB, userB, null, ShoppingItemVisibility.SHARED, LocalDateTime.now());

        List<ShoppingItem> result = shoppingItemRepository
            .findAllByHouseholdIdAndVisibilityAndStatusAndSuggestedFalseOrderByCreatedAtDesc(
                householdA.getId(),
                ShoppingItemVisibility.SHARED,
                ShoppingItemStatus.NEEDED
            );

        assertEquals(1, result.size());
        assertEquals("Milk", result.get(0).getName());
        assertEquals(ShoppingItemVisibility.SHARED, result.get(0).getVisibility());
        assertEquals(ShoppingItemStatus.NEEDED, result.get(0).getStatus());
    }

    @Test
    public void findByHouseholdAndStatusIn_returnsOnlyHistoryItemsOrderedByUpdatedAtDesc() {
        LocalDateTime base = LocalDateTime.of(2026, 5, 31, 12, 0);

        ShoppingItem bought = save("Bought old", householdA, userA, null, ShoppingItemVisibility.SHARED, base.minusDays(4));
        markStatusAndUpdatedAt(bought, ShoppingItemStatus.BOUGHT, base.minusHours(2));

        ShoppingItem cancelled = save("Cancelled new", householdA, userA, null, ShoppingItemVisibility.PERSONAL, base.minusDays(3));
        markStatusAndUpdatedAt(cancelled, ShoppingItemStatus.CANCELLED, base);

        ShoppingItem needed = save("Still needed", householdA, userA, null, ShoppingItemVisibility.SHARED, base.minusDays(2));
        markStatusAndUpdatedAt(needed, ShoppingItemStatus.NEEDED, base.plusHours(1));

        ShoppingItem otherHousehold = save("Other household bought", householdB, userB, null, ShoppingItemVisibility.SHARED, base.minusDays(1));
        markStatusAndUpdatedAt(otherHousehold, ShoppingItemStatus.BOUGHT, base.plusHours(2));

        List<ShoppingItem> result = shoppingItemRepository
            .findAllByHouseholdIdAndSuggestedFalseAndStatusInOrderByUpdatedAtDesc(
                householdA.getId(),
                List.of(ShoppingItemStatus.BOUGHT, ShoppingItemStatus.CANCELLED)
            );

        assertEquals(2, result.size());
        assertEquals("Cancelled new", result.get(0).getName());
        assertEquals("Bought old", result.get(1).getName());
        assertTrue(result.stream().allMatch(item -> item.getHousehold().getId().equals(householdA.getId())));
        assertTrue(result.stream().noneMatch(item -> item.getStatus() == ShoppingItemStatus.NEEDED));
    }

    @Test
    public void findSuggestionsForHousehold_returnsOnlyNeededSuggestionsForHousehold() {
        ShoppingItem suggestion = save("Suggested milk", householdA, userA, null, ShoppingItemVisibility.SHARED, LocalDateTime.now());
        suggestion.setSuggested(true);
        suggestion.getVotedBy().add(userA);
        shoppingItemRepository.save(suggestion);

        ShoppingItem normalShared = save("Normal shared", householdA, userA, null, ShoppingItemVisibility.SHARED, LocalDateTime.now());
        normalShared.setSuggested(false);
        shoppingItemRepository.save(normalShared);

        ShoppingItem boughtSuggestion = save("Bought suggestion", householdA, userA, null, ShoppingItemVisibility.SHARED, LocalDateTime.now());
        boughtSuggestion.setSuggested(true);
        boughtSuggestion.setStatus(ShoppingItemStatus.BOUGHT);
        shoppingItemRepository.save(boughtSuggestion);

        ShoppingItem otherHouseholdSuggestion = save("Other household suggestion", householdB, userB, null, ShoppingItemVisibility.SHARED, LocalDateTime.now());
        otherHouseholdSuggestion.setSuggested(true);
        shoppingItemRepository.save(otherHouseholdSuggestion);

        List<ShoppingItem> result = shoppingItemRepository.findSuggestionsForHousehold(
            householdA.getId(),
            ShoppingItemStatus.NEEDED
        );

        assertEquals(1, result.size());
        assertEquals("Suggested milk", result.get(0).getName());
    }

    @Test
    public void findVisibleItems_excludesSuggestions() {
        ShoppingItem normalShared = save("Normal shared", householdA, userA, null, ShoppingItemVisibility.SHARED, LocalDateTime.now());
        normalShared.setSuggested(false);
        shoppingItemRepository.save(normalShared);

        ShoppingItem suggestion = save("Suggestion", householdA, userA, null, ShoppingItemVisibility.SHARED, LocalDateTime.now());
        suggestion.setSuggested(true);
        shoppingItemRepository.save(suggestion);

        List<ShoppingItem> result = shoppingItemRepository.findVisibleItems(
            householdA.getId(),
            userA.getId(),
            ShoppingItemVisibility.SHARED
        );

        assertEquals(1, result.size());
        assertEquals("Normal shared", result.get(0).getName());
    }

    @Test
    public void findAllBySuggestedFalse_returnsOnlyNormalShoppingItems() {
        ShoppingItem normal = save(
            "Normal item",
            householdA,
            userA,
            null,
            ShoppingItemVisibility.SHARED,
            LocalDateTime.now()
        );
        normal.setSuggested(false);
        shoppingItemRepository.save(normal);

        ShoppingItem suggestion = save(
            "Suggested item",
            householdA,
            userA,
            null,
            ShoppingItemVisibility.SHARED,
            LocalDateTime.now()
        );
        suggestion.setSuggested(true);
        shoppingItemRepository.save(suggestion);

        List<ShoppingItem> result =
            shoppingItemRepository.findAllBySuggestedFalseOrderByCreatedAtDesc();

        assertEquals(1, result.size());
        assertEquals("Normal item", result.get(0).getName());
        assertTrue(!result.get(0).isSuggested());
    }

    private void markStatusAndUpdatedAt(ShoppingItem item, ShoppingItemStatus status, LocalDateTime updatedAt) {
        item.setStatus(status);
        shoppingItemRepository.saveAndFlush(item);
        jdbcTemplate.update(
            "UPDATE shopping_item SET updated_at = ? WHERE id = ?",
            updatedAt,
            item.getId()
        );
    }
}
