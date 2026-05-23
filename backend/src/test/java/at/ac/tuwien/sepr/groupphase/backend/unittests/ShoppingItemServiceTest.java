package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShoppingItemMapper;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ShoppingItemService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ShoppingItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ShoppingItemServiceTest {

    @Mock
    private ShoppingItemRepository shoppingItemRepository;

    @Mock
    private RoommateRepository roommateRepository;

    @Mock
    private ShoppingItemMapper shoppingItemMapper;

    private ShoppingItemService shoppingItemService;

    private ApplicationUser user;
    private Household household;
    private Roommate roommate;

    @BeforeEach
    public void setUp() {
        shoppingItemService = new ShoppingItemServiceImpl(shoppingItemRepository, roommateRepository, shoppingItemMapper);

        user = new ApplicationUser();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setAdmin(false);

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
                item.getQuantity(),
                item.getNotes(),
                item.getStatus(),
                item.getHousehold() != null ? item.getHousehold().getId() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getId() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getEmail() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getFirstName() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getLastName() : null,
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
                item.getQuantity(),
                item.getNotes(),
                item.getStatus(),
                item.getHousehold() != null ? item.getHousehold().getId() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getId() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getEmail() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getFirstName() : null,
                item.getCreatedBy() != null ? item.getCreatedBy().getLastName() : null,
                item.getCreatedAt(),
                item.getUpdatedAt()
            )).toList();
        });
    }

    @Test
    public void givenUserInHousehold_whenCreateItem_thenItemIsAttachedToHouseholdAndUser() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(shoppingItemRepository.save(any(ShoppingItem.class))).thenAnswer(invocation -> {
            ShoppingItem saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        ShoppingItemCreateDto dto = new ShoppingItemCreateDto("Milk", "2L", "Whole milk");

        ShoppingItemDto result = shoppingItemService.create(dto, user);

        assertNotNull(result);
        assertEquals(42L, result.id());
        assertEquals("Milk", result.name());
        assertEquals(ShoppingItemStatus.NEEDED, result.status());
        assertEquals(10L, result.householdId());
        assertEquals(1L, result.createdById());
        verify(shoppingItemRepository, times(1)).save(any(ShoppingItem.class));
    }

    @Test
    public void givenUserWithoutHousehold_whenCreateItem_thenForbidden() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        ShoppingItemCreateDto dto = new ShoppingItemCreateDto("Milk", "2L", null);

        assertThrows(ForbiddenException.class, () -> shoppingItemService.create(dto, user));
    }

    @Test
    public void givenUserInHousehold_whenFindVisibleItems_thenScopedQuery() {
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));
        when(shoppingItemRepository
            .findAllByHouseholdIdAndCreatedByIdOrderByCreatedAtDesc(10L, 1L))
            .thenReturn(List.of(sampleItem(1L)));

        List<ShoppingItemDto> result = shoppingItemService.findVisibleItems(user);

        assertEquals(1, result.size());
        verify(shoppingItemRepository, times(1))
            .findAllByHouseholdIdAndCreatedByIdOrderByCreatedAtDesc(10L, 1L);
    }

    @Test
    public void givenItemOfOtherHousehold_whenFindOne_thenForbidden() {
        ShoppingItem item = sampleItem(5L);
        Household otherHousehold = new Household();
        otherHousehold.setId(99L);
        item.setHousehold(otherHousehold);

        when(shoppingItemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(1L, 99L)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> shoppingItemService.findOne(5L, user));
    }

    @Test
    public void givenMissingItem_whenFindOne_thenNotFound() {
        when(shoppingItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> shoppingItemService.findOne(999L, user));
    }

    @Test
    public void givenOwnItem_whenUpdateStatus_thenStatusIsSaved() {
        ShoppingItem item = sampleItem(7L);
        when(shoppingItemRepository.findById(7L)).thenReturn(Optional.of(item));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(1L, 10L)).thenReturn(true);
        when(shoppingItemRepository.save(any(ShoppingItem.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingItemDto result = shoppingItemService.updateStatus(
            7L, new ShoppingItemStatusUpdateDto(ShoppingItemStatus.BOUGHT), user);

        assertEquals(ShoppingItemStatus.BOUGHT, result.status());
    }

    @Test
    public void givenItemOfOtherCreator_whenDelete_thenForbidden() {
        ApplicationUser otherUser = new ApplicationUser();
        otherUser.setId(2L);
        otherUser.setAdmin(false);

        ShoppingItem item = sampleItem(8L);
        item.setCreatedBy(otherUser);

        when(shoppingItemRepository.findById(8L)).thenReturn(Optional.of(item));
        when(roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(1L, 10L)).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> shoppingItemService.delete(8L, user));
    }

    private ShoppingItem sampleItem(Long id) {
        ShoppingItem item = new ShoppingItem();
        item.setId(id);
        item.setName("Milk");
        item.setQuantity("2L");
        item.setStatus(ShoppingItemStatus.NEEDED);
        item.setHousehold(household);
        item.setCreatedBy(user);
        return item;
    }
}
