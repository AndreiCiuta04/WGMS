package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AggregatedShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemSuggestionCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemSuggestionDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.QuantityUnit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShoppingItemMapper;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ShoppingItemService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ShoppingItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ShoppingItemServiceTest {

    @Mock private ShoppingItemRepository shoppingItemRepository;
    @Mock private RoommateRepository roommateRepository;
    @Mock private UserService userService;
    @Mock private ShoppingItemMapper shoppingItemMapper;

    private ShoppingItemService shoppingItemService;

    private ApplicationUser user;
    private ApplicationUser otherUser;
    private Household household;
    private Roommate roommate;

    @BeforeEach
    public void setUp() {
        shoppingItemService = new ShoppingItemServiceImpl(
            shoppingItemRepository, roommateRepository, userService, shoppingItemMapper);

        user = new ApplicationUser();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setAdmin(false);

        otherUser = new ApplicationUser();
        otherUser.setId(2L);
        otherUser.setEmail("other@test.com");
        otherUser.setFirstName("Other");
        otherUser.setLastName("Mate");
        otherUser.setAdmin(false);

        household = new Household();
        household.setId(10L);
        household.setName("Test WG");

        roommate = new Roommate();
        roommate.setId(100L);
        roommate.setUser(user);
        roommate.setHousehold(household);
        roommate.setRole(HouseholdRole.ROOMMATE);
        roommate.setActive(true);

        // Lenient stubbing for mapstruct mapper mock
        org.mockito.Mockito.lenient().when(shoppingItemMapper.entityToDto(any(ShoppingItem.class))).thenAnswer(invocation -> {
            ShoppingItem item = invocation.getArgument(0);
            if (item == null) return null;
            return new ShoppingItemDto(
                item.getId(),
                item.getName(),
                item.getQuantityCount(),
                item.getQuantityAmount(),
                item.getQuantityUnit(),
                item.getNotes(),
                item.getStatus(),
                item.getVisibility(),
                item.getHousehold() != null ? item.getHousehold().getId() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getId() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getEmail() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getFirstName() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getLastName() : null,
                item.getTargetUser() != null ? item.getTargetUser().getId() : null,
                item.getTargetUser() != null ? item.getTargetUser().getEmail() : null,
                item.getTargetUser() != null ? item.getTargetUser().getFirstName() : null,
                item.getTargetUser() != null ? item.getTargetUser().getLastName() : null,
                item.getCreatedAt(),
                item.getUpdatedAt()
            );
        });

        org.mockito.Mockito.lenient().when(shoppingItemMapper.entityToDto(any(List.class))).thenAnswer(invocation -> {
            List<ShoppingItem> items = invocation.getArgument(0);
            if (items == null) return null;
            return items.stream().map(item -> new ShoppingItemDto(
                item.getId(),
                item.getName(),
                item.getQuantityCount(),
                item.getQuantityAmount(),
                item.getQuantityUnit(),
                item.getNotes(),
                item.getStatus(),
                item.getVisibility(),
                item.getHousehold() != null ? item.getHousehold().getId() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getId() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getEmail() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getFirstName() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getLastName() : null,
                item.getTargetUser() != null ? item.getTargetUser().getId() : null,
                item.getTargetUser() != null ? item.getTargetUser().getEmail() : null,
                item.getTargetUser() != null ? item.getTargetUser().getFirstName() : null,
                item.getTargetUser() != null ? item.getTargetUser().getLastName() : null,
                item.getCreatedAt(),
                item.getUpdatedAt()
            )).toList();
        });
    }

    @Test
    public void givenUserInHousehold_whenCreatePersonalItem_thenItemAttachedToHouseholdAndUser() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(shoppingItemRepository.save(any(ShoppingItem.class))).thenAnswer(i -> {
            ShoppingItem s = i.getArgument(0);
            s.setId(42L);
            return s;
        });

        ShoppingItemCreateDto dto = new ShoppingItemCreateDto(
            "Milk", null, new BigDecimal("2"), QuantityUnit.LITER, "Whole milk", ShoppingItemVisibility.PERSONAL, null);

        ShoppingItemDto result = shoppingItemService.create(dto, user);

        assertNotNull(result);
        assertEquals(42L, result.id());
        assertEquals("Milk", result.name());
        assertEquals(ShoppingItemVisibility.PERSONAL, result.visibility());
        assertEquals(10L, result.householdId());
        assertEquals(1L, result.createdById());
        verify(shoppingItemRepository, times(1)).save(any(ShoppingItem.class));
    }

    @Test
    public void givenUserWithoutHousehold_whenCreate_thenForbidden() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.empty());
        ShoppingItemCreateDto dto = new ShoppingItemCreateDto(
            "Milk", null, null, null, null, ShoppingItemVisibility.PERSONAL, null);
        assertThrows(ForbiddenException.class, () -> shoppingItemService.create(dto, user));
    }

    @Test
    public void givenValidTarget_whenCreateTargetedItem_thenSavedWithTarget() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(userService.findApplicationUserByEmail("other@test.com")).thenReturn(otherUser);
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(2L, 10L)).thenReturn(true);
        when(shoppingItemRepository.save(any(ShoppingItem.class))).thenAnswer(i -> {
            ShoppingItem s = i.getArgument(0);
            s.setId(42L);
            return s;
        });

        ShoppingItemCreateDto dto = new ShoppingItemCreateDto(
            "Milk", null, new BigDecimal("2"), QuantityUnit.LITER, null, ShoppingItemVisibility.TARGETED, "other@test.com");

        ShoppingItemDto result = shoppingItemService.create(dto, user);

        assertEquals(ShoppingItemVisibility.TARGETED, result.visibility());
        assertEquals(2L, result.targetUserId());
        assertEquals("other@test.com", result.targetUserEmail());
    }

    @Test
    public void givenTargetedWithoutTarget_whenCreate_thenValidationException() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        ShoppingItemCreateDto dto = new ShoppingItemCreateDto(
            "Milk", null, null, null, null, ShoppingItemVisibility.TARGETED, null);
        assertThrows(ValidationException.class, () -> shoppingItemService.create(dto, user));
    }

    @Test
    public void givenSelfTarget_whenCreate_thenConflict() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        ShoppingItemCreateDto dto = new ShoppingItemCreateDto(
            "Milk", null, null, null, null, ShoppingItemVisibility.TARGETED, "user@test.com");
        assertThrows(ConflictException.class, () -> shoppingItemService.create(dto, user));
    }

    @Test
    public void givenTargetOutsideHousehold_whenCreate_thenConflict() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(userService.findApplicationUserByEmail("other@test.com")).thenReturn(otherUser);
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(2L, 10L)).thenReturn(false);
        ShoppingItemCreateDto dto = new ShoppingItemCreateDto(
            "Milk", null, null, null, null, ShoppingItemVisibility.TARGETED, "other@test.com");
        assertThrows(ConflictException.class, () -> shoppingItemService.create(dto, user));
    }

    @Test
    public void givenUserInHousehold_whenFindVisibleItems_thenSingleQueryInvoked() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(shoppingItemRepository
            .findVisibleItems(10L, 1L, ShoppingItemVisibility.SHARED))
            .thenReturn(List.of(
                sampleItem(1L, ShoppingItemVisibility.PERSONAL),
                sampleItem(2L, ShoppingItemVisibility.TARGETED),
                sampleItem(3L, ShoppingItemVisibility.SHARED)
            ));

        List<ShoppingItemDto> result = shoppingItemService.findVisibleItems(user);

        assertEquals(3, result.size());
    }

    @Test
    public void givenItemOfOtherHousehold_whenFindOne_thenForbidden() {
        ShoppingItem item = sampleItem(5L, ShoppingItemVisibility.PERSONAL);
        Household otherHousehold = new Household();
        otherHousehold.setId(99L);
        item.setHousehold(otherHousehold);

        when(shoppingItemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(1L, 99L)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> shoppingItemService.findOne(5L, user));
    }

    @Test
    public void givenSharedItemInOtherHousehold_whenFindOne_thenForbidden() {
        ShoppingItem item = sampleItem(9L, ShoppingItemVisibility.SHARED);
        Household otherHousehold = new Household();
        otherHousehold.setId(99L);
        item.setHousehold(otherHousehold);

        when(shoppingItemRepository.findById(9L)).thenReturn(Optional.of(item));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(1L, 99L)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> shoppingItemService.findOne(9L, user));
    }

    @Test
    public void givenMissingItem_whenFindOne_thenNotFound() {
        when(shoppingItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> shoppingItemService.findOne(999L, user));
    }

    @Test
    public void givenOwnItem_whenUpdateStatus_thenStatusIsSaved() {
        ShoppingItem item = sampleItem(7L, ShoppingItemVisibility.PERSONAL);
        when(shoppingItemRepository.findById(7L)).thenReturn(Optional.of(item));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(1L, 10L)).thenReturn(true);
        when(shoppingItemRepository.save(any(ShoppingItem.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingItemDto result = shoppingItemService.updateStatus(
            7L, new ShoppingItemStatusUpdateDto(ShoppingItemStatus.BOUGHT), user);

        assertEquals(ShoppingItemStatus.BOUGHT, result.status());
    }

    @Test
    public void givenItemOfOtherCreator_whenDelete_thenForbidden() {
        ShoppingItem item = sampleItem(8L, ShoppingItemVisibility.PERSONAL);
        item.setCreatedBy(otherUser);

        when(shoppingItemRepository.findById(8L)).thenReturn(Optional.of(item));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(1L, 10L)).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> shoppingItemService.delete(8L, user));
    }

    @Test
    public void givenTargetedItemForMe_whenDelete_thenForbidden() {
        ShoppingItem item = sampleItem(8L, ShoppingItemVisibility.TARGETED);
        item.setCreatedBy(otherUser);
        item.setTargetUser(user);

        when(shoppingItemRepository.findById(8L)).thenReturn(Optional.of(item));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(1L, 10L)).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> shoppingItemService.delete(8L, user));
    }

    @Test
    public void givenSharedNeededItems_whenFindAggregatedHouseholdShoppingList_thenGroupsByNormalizedNameAndStructuredQuantity() {
        ShoppingItem milkA = sampleItem(1L, ShoppingItemVisibility.SHARED);
        milkA.setName("Milk");
        milkA.setQuantityAmount(new BigDecimal("2"));
        milkA.setQuantityUnit(QuantityUnit.LITER);

        // Same quantity typed as 2.0 with a name that differs only in case/whitespace — must still merge.
        ShoppingItem milkB = sampleItem(2L, ShoppingItemVisibility.SHARED);
        milkB.setName(" milk ");
        milkB.setQuantityAmount(new BigDecimal("2.0"));
        milkB.setQuantityUnit(QuantityUnit.LITER);

        ShoppingItem bread = sampleItem(3L, ShoppingItemVisibility.SHARED);
        bread.setName("Bread");
        bread.setQuantityAmount(new BigDecimal("1"));
        bread.setQuantityUnit(QuantityUnit.PACK);

        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(shoppingItemRepository.findAllByHouseholdIdAndVisibilityAndStatusAndSuggestedFalseOrderByCreatedAtDesc(
            10L,
            ShoppingItemVisibility.SHARED,
            ShoppingItemStatus.NEEDED
        )).thenReturn(List.of(milkA, milkB, bread));

        List<AggregatedShoppingItemDto> result =
            shoppingItemService.findAggregatedHouseholdShoppingList(user);

        assertEquals(2, result.size());
        assertEquals("Bread", result.get(0).name());
        assertEquals(1, result.get(0).itemCount());
        assertEquals("Milk", result.get(1).name());
        assertEquals(2, result.get(1).itemCount());
        assertEquals(2, result.get(1).sourceItems().size());
    }

    @Test
    public void givenUserWithoutHousehold_whenFindAggregatedHouseholdShoppingList_thenForbidden() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
            () -> shoppingItemService.findAggregatedHouseholdShoppingList(user));
    }

    @Test
    public void givenUserInHousehold_whenFindHouseholdShoppingHistory_thenRepositoryUsesHouseholdAndHistoryStatuses() {
        ShoppingItem bought = sampleItem(20L, ShoppingItemVisibility.SHARED);
        bought.setStatus(ShoppingItemStatus.BOUGHT);

        ShoppingItem cancelled = sampleItem(21L, ShoppingItemVisibility.PERSONAL);
        cancelled.setStatus(ShoppingItemStatus.CANCELLED);

        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(shoppingItemRepository.findAllByHouseholdIdAndSuggestedFalseAndStatusInOrderByUpdatedAtDesc(
            10L,
            List.of(ShoppingItemStatus.BOUGHT, ShoppingItemStatus.CANCELLED)
        )).thenReturn(List.of(bought, cancelled));

        List<ShoppingItemDto> result = shoppingItemService.findHouseholdShoppingHistory(user);

        assertEquals(2, result.size());
        assertEquals(ShoppingItemStatus.BOUGHT, result.get(0).status());
        assertEquals(ShoppingItemStatus.CANCELLED, result.get(1).status());
        verify(shoppingItemRepository).findAllByHouseholdIdAndSuggestedFalseAndStatusInOrderByUpdatedAtDesc(
            eq(10L),
            eq(List.of(ShoppingItemStatus.BOUGHT, ShoppingItemStatus.CANCELLED))
        );
    }

    @Test
    public void givenUserWithoutHousehold_whenFindHouseholdShoppingHistory_thenForbidden() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class,
            () -> shoppingItemService.findHouseholdShoppingHistory(user));
    }

    @Test
    public void givenUserInHousehold_whenCreateSuggestion_thenSuggestionCreatedWithCreatorVote() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(shoppingItemRepository.save(any(ShoppingItem.class))).thenAnswer(invocation -> {
            ShoppingItem item = invocation.getArgument(0);
            item.setId(50L);
            return item;
        });

        ShoppingItemSuggestionCreateDto dto = new ShoppingItemSuggestionCreateDto(
            "Dishwasher tabs",
            "1 pack",
            "Almost empty"
        );

        ShoppingItemSuggestionDto result = shoppingItemService.createSuggestion(dto, user);

        assertEquals(50L, result.id());
        assertEquals("Dishwasher tabs", result.name());
        assertEquals(10L, result.householdId());
        assertEquals(1, result.voteCount());
        assertTrue(result.votedByCurrentUser());
    }

    @Test
    public void givenUserInHousehold_whenFindSuggestions_thenRepositoryUsesHouseholdAndNeededStatus() {
        ShoppingItem suggestion = sampleItem(51L, ShoppingItemVisibility.SHARED);
        suggestion.setSuggested(true);
        suggestion.getVotedBy().add(user);

        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(shoppingItemRepository.findSuggestionsForHousehold(10L, ShoppingItemStatus.NEEDED))
            .thenReturn(List.of(suggestion));

        List<ShoppingItemSuggestionDto> result = shoppingItemService.findSuggestions(user);

        assertEquals(1, result.size());
        assertEquals(51L, result.get(0).id());
        assertEquals(1, result.get(0).voteCount());
        assertTrue(result.get(0).votedByCurrentUser());
    }

    @Test
    public void givenSuggestion_whenVoteForSuggestion_thenCurrentUserAddedToVotes() {
        ShoppingItem suggestion = sampleItem(52L, ShoppingItemVisibility.SHARED);
        suggestion.setSuggested(true);
        suggestion.setCreatedBy(otherUser);
        suggestion.getVotedBy().add(otherUser);

        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(shoppingItemRepository.findById(52L)).thenReturn(Optional.of(suggestion));
        when(shoppingItemRepository.save(any(ShoppingItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShoppingItemSuggestionDto result = shoppingItemService.voteForSuggestion(52L, user);

        assertEquals(2, result.voteCount());
        assertTrue(result.votedByCurrentUser());
    }

    @Test
    public void givenAlreadyVotedSuggestion_whenVoteForSuggestion_thenVoteCountDoesNotIncrease() {
        ShoppingItem suggestion = sampleItem(53L, ShoppingItemVisibility.SHARED);
        suggestion.setSuggested(true);
        suggestion.getVotedBy().add(user);

        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(shoppingItemRepository.findById(53L)).thenReturn(Optional.of(suggestion));
        when(shoppingItemRepository.save(any(ShoppingItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShoppingItemSuggestionDto result = shoppingItemService.voteForSuggestion(53L, user);

        assertEquals(1, result.voteCount());
        assertTrue(result.votedByCurrentUser());
    }

    @Test
    public void givenNormalItem_whenVoteForSuggestion_thenConflict() {
        ShoppingItem item = sampleItem(54L, ShoppingItemVisibility.SHARED);
        item.setSuggested(false);

        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(shoppingItemRepository.findById(54L)).thenReturn(Optional.of(item));

        assertThrows(ConflictException.class, () -> shoppingItemService.voteForSuggestion(54L, user));
    }

    @Test
    public void givenSuggestionFromOtherHousehold_whenVoteForSuggestion_thenForbidden() {
        Household otherHousehold = new Household();
        otherHousehold.setId(99L);

        ShoppingItem item = sampleItem(55L, ShoppingItemVisibility.SHARED);
        item.setSuggested(true);
        item.setHousehold(otherHousehold);

        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(shoppingItemRepository.findById(55L)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class, () -> shoppingItemService.voteForSuggestion(55L, user));
    }

    @Test
    public void givenVotedSuggestion_whenRemoveVote_thenCurrentUserRemoved() {
        ShoppingItem suggestion = sampleItem(56L, ShoppingItemVisibility.SHARED);
        suggestion.setSuggested(true);
        suggestion.getVotedBy().add(user);
        suggestion.getVotedBy().add(otherUser);

        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(shoppingItemRepository.findById(56L)).thenReturn(Optional.of(suggestion));
        when(shoppingItemRepository.save(any(ShoppingItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShoppingItemSuggestionDto result = shoppingItemService.removeVoteFromSuggestion(56L, user);

        assertEquals(1, result.voteCount());
        assertTrue(!result.votedByCurrentUser());
    }

    @Test
    public void givenEquivalentSuggestionFromOtherUser_whenCreateSuggestion_thenExistingSuggestionReturnedAndVoteAdded() {
        ShoppingItem existing = sampleItem(60L, ShoppingItemVisibility.SHARED);
        existing.setName(" Milk ");
        existing.setQuantity(" 2L ");
        existing.setSuggested(true);
        existing.setCreatedBy(otherUser);
        existing.getVotedBy().add(otherUser);

        when(roommateRepository.findByUserIdAndActiveTrue(1L))
            .thenReturn(Optional.of(roommate));

        when(shoppingItemRepository.findSuggestionsForHousehold(
            10L,
            ShoppingItemStatus.NEEDED
        )).thenReturn(List.of(existing));

        when(shoppingItemRepository.save(existing)).thenReturn(existing);

        ShoppingItemSuggestionDto result = shoppingItemService.createSuggestion(
            new ShoppingItemSuggestionCreateDto(
                "milk",
                "2l",
                "Different notes"
            ),
            user
        );

        assertEquals(60L, result.id());
        assertEquals(2, result.voteCount());
        assertTrue(result.votedByCurrentUser());

        verify(shoppingItemRepository).save(existing);
    }

    @Test
    public void givenEquivalentSuggestionAlreadyVotedByUser_whenCreateSuggestion_thenNoDuplicateVoteOrSave() {
        ShoppingItem existing = sampleItem(61L, ShoppingItemVisibility.SHARED);
        existing.setName("Milk");
        existing.setQuantity("2L");
        existing.setSuggested(true);
        existing.getVotedBy().add(user);

        when(roommateRepository.findByUserIdAndActiveTrue(1L))
            .thenReturn(Optional.of(roommate));

        when(shoppingItemRepository.findSuggestionsForHousehold(
            10L,
            ShoppingItemStatus.NEEDED
        )).thenReturn(List.of(existing));

        ShoppingItemSuggestionDto result = shoppingItemService.createSuggestion(
            new ShoppingItemSuggestionCreateDto(
                " milk ",
                " 2l ",
                "Ignored notes"
            ),
            user
        );

        assertEquals(61L, result.id());
        assertEquals(1, result.voteCount());

        verify(shoppingItemRepository, times(0))
            .save(existing);
    }

    @Test
    public void givenNoEquivalentSuggestionInCurrentHousehold_whenCreateSuggestion_thenCreatesNewSuggestion() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L))
            .thenReturn(Optional.of(roommate));

        when(shoppingItemRepository.findSuggestionsForHousehold(
            10L,
            ShoppingItemStatus.NEEDED
        )).thenReturn(List.of());

        when(shoppingItemRepository.save(any(ShoppingItem.class)))
            .thenAnswer(invocation -> {
                ShoppingItem saved = invocation.getArgument(0);
                saved.setId(62L);
                return saved;
            });

        ShoppingItemSuggestionDto result = shoppingItemService.createSuggestion(
            new ShoppingItemSuggestionCreateDto(
                "Milk",
                "2L",
                null
            ),
            user
        );

        assertEquals(62L, result.id());
        assertEquals(1, result.voteCount());
    }


    @Test
    public void givenSuggestionWithVotes_whenFindSuggestions_thenVoterDetailsReturned() {
        ShoppingItem suggestion = sampleItem(63L, ShoppingItemVisibility.SHARED);
        suggestion.setSuggested(true);
        suggestion.getVotedBy().add(user);
        suggestion.getVotedBy().add(otherUser);

        when(roommateRepository.findByUserIdAndActiveTrue(1L))
            .thenReturn(Optional.of(roommate));

        when(shoppingItemRepository.findSuggestionsForHousehold(
            10L,
            ShoppingItemStatus.NEEDED
        )).thenReturn(List.of(suggestion));

        List<ShoppingItemSuggestionDto> result =
            shoppingItemService.findSuggestions(user);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).voteCount());
        assertEquals(2, result.get(0).voters().size());

        assertTrue(result.get(0).voters().stream()
            .anyMatch(voter -> voter.userId().equals(1L)));

        assertTrue(result.get(0).voters().stream()
            .anyMatch(voter -> voter.userId().equals(2L)));
    }

    @Test
    public void givenActiveSuggestion_whenPromote_thenBecomesNormalSharedNeededItemAndVotesCleared() {
        ShoppingItem suggestion = sampleItem(64L, ShoppingItemVisibility.SHARED);
        suggestion.setSuggested(true);
        suggestion.getVotedBy().add(user);
        suggestion.getVotedBy().add(otherUser);

        when(roommateRepository.findByUserIdAndActiveTrue(1L))
            .thenReturn(Optional.of(roommate));

        when(shoppingItemRepository.findById(64L))
            .thenReturn(Optional.of(suggestion));

        when(shoppingItemRepository.save(suggestion))
            .thenReturn(suggestion);

        ShoppingItemDto result =
            shoppingItemService.promoteSuggestion(64L, user);

        assertTrue(!suggestion.isSuggested());
        assertEquals(ShoppingItemVisibility.SHARED, suggestion.getVisibility());
        assertEquals(ShoppingItemStatus.NEEDED, suggestion.getStatus());
        assertTrue(suggestion.getVotedBy().isEmpty());

        assertEquals(64L, result.id());
        assertEquals(ShoppingItemVisibility.SHARED, result.visibility());
    }

    @Test
    public void givenNormalItem_whenPromoteSuggestion_thenConflict() {
        ShoppingItem item = sampleItem(65L, ShoppingItemVisibility.SHARED);
        item.setSuggested(false);

        when(roommateRepository.findByUserIdAndActiveTrue(1L))
            .thenReturn(Optional.of(roommate));

        when(shoppingItemRepository.findById(65L))
            .thenReturn(Optional.of(item));

        assertThrows(
            ConflictException.class,
            () -> shoppingItemService.promoteSuggestion(65L, user)
        );
    }

    @Test
    public void givenSuggestionFromOtherHousehold_whenPromote_thenForbidden() {
        Household otherHousehold = new Household();
        otherHousehold.setId(99L);

        ShoppingItem suggestion = sampleItem(66L, ShoppingItemVisibility.SHARED);
        suggestion.setSuggested(true);
        suggestion.setHousehold(otherHousehold);

        when(roommateRepository.findByUserIdAndActiveTrue(1L))
            .thenReturn(Optional.of(roommate));

        when(shoppingItemRepository.findById(66L))
            .thenReturn(Optional.of(suggestion));

        assertThrows(
            ForbiddenException.class,
            () -> shoppingItemService.promoteSuggestion(66L, user)
        );
    }

    @Test
    public void givenOwnActiveSuggestion_whenDeleteSuggestion_thenRepositoryDeletesIt() {
        ShoppingItem suggestion = sampleItem(67L, ShoppingItemVisibility.SHARED);
        suggestion.setSuggested(true);
        suggestion.setCreatedBy(user);

        when(shoppingItemRepository.findById(67L))
            .thenReturn(Optional.of(suggestion));

        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(
            1L,
            10L
        )).thenReturn(true);

        shoppingItemService.deleteSuggestion(67L, user);

        verify(shoppingItemRepository).delete(suggestion);
    }

    @Test
    public void givenSuggestionOwnedByAnotherUser_whenDeleteSuggestion_thenForbidden() {
        ShoppingItem suggestion = sampleItem(68L, ShoppingItemVisibility.SHARED);
        suggestion.setSuggested(true);
        suggestion.setCreatedBy(otherUser);

        when(shoppingItemRepository.findById(68L))
            .thenReturn(Optional.of(suggestion));

        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(
            1L,
            10L
        )).thenReturn(true);

        assertThrows(
            ForbiddenException.class,
            () -> shoppingItemService.deleteSuggestion(68L, user)
        );
    }

    @Test
    public void givenNormalItem_whenDeleteSuggestion_thenConflict() {
        ShoppingItem item = sampleItem(69L, ShoppingItemVisibility.SHARED);
        item.setSuggested(false);

        when(shoppingItemRepository.findById(69L))
            .thenReturn(Optional.of(item));

        assertThrows(
            ConflictException.class,
            () -> shoppingItemService.deleteSuggestion(69L, user)
        );
    }


    private ShoppingItem sampleItem(Long id, ShoppingItemVisibility visibility) {
        ShoppingItem item = new ShoppingItem();
        item.setId(id);
        item.setName("Milk");
        item.setQuantityAmount(new BigDecimal("2"));
        item.setQuantityUnit(QuantityUnit.LITER);
        item.setStatus(ShoppingItemStatus.NEEDED);
        item.setVisibility(visibility);
        item.setHousehold(household);
        item.setCreatedBy(user);
        return item;
    }
}
