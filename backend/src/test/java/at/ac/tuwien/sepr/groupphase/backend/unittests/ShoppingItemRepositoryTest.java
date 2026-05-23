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
}
