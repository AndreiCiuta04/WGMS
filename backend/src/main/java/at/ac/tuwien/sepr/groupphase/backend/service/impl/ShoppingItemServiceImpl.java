package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ShoppingItemService;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShoppingItemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class ShoppingItemServiceImpl implements ShoppingItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ShoppingItemRepository shoppingItemRepository;
    private final RoommateRepository roommateRepository;
    private final ShoppingItemMapper shoppingItemMapper;

    @Autowired
    public ShoppingItemServiceImpl(ShoppingItemRepository shoppingItemRepository,
                                   RoommateRepository roommateRepository,
                                   ShoppingItemMapper shoppingItemMapper) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.roommateRepository = roommateRepository;
        this.shoppingItemMapper = shoppingItemMapper;
    }

    @Override
    @Transactional
    public ShoppingItemDto create(ShoppingItemCreateDto dto, ApplicationUser currentUser) {
        LOGGER.debug("Create shopping item {} for user {}", dto.name(), currentUser.getEmail());

        Roommate roommate = requireActiveRoommate(currentUser);

        ShoppingItem item = new ShoppingItem();
        item.setName(dto.name());
        item.setQuantity(dto.quantity());
        item.setNotes(dto.notes());
        item.setStatus(ShoppingItemStatus.NEEDED);
        item.setHousehold(roommate.getHousehold());
        item.setCreatedBy(currentUser);

        return shoppingItemMapper.entityToDto(shoppingItemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShoppingItemDto> findVisibleItems(ApplicationUser currentUser) {
        LOGGER.debug("Find visible shopping items for user {}", currentUser.getEmail());

        if (Boolean.TRUE.equals(currentUser.getAdmin())) {
            return shoppingItemMapper.entityToDto(shoppingItemRepository.findAll());
        }

        Roommate roommate = requireActiveRoommate(currentUser);

        // FR-07: only items the current user created within their household.
        // FR-08 will broaden this to include TARGETED-at-me and SHARED items.
        return shoppingItemMapper.entityToDto(
            shoppingItemRepository.findAllByHouseholdIdAndCreatedByIdOrderByCreatedAtDesc(
                roommate.getHousehold().getId(), currentUser.getId())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ShoppingItemDto findOne(Long id, ApplicationUser currentUser) {
        LOGGER.debug("Find shopping item {} for user {}", id, currentUser.getEmail());
        return shoppingItemMapper.entityToDto(loadAndAuthorizeView(id, currentUser));
    }

    @Override
    @Transactional
    public ShoppingItemDto update(Long id, ShoppingItemUpdateDto dto, ApplicationUser currentUser) {
        LOGGER.debug("Update shopping item {} by user {}", id, currentUser.getEmail());

        ShoppingItem item = loadAndAuthorizeManage(id, currentUser);

        item.setName(dto.name());
        item.setQuantity(dto.quantity());
        item.setNotes(dto.notes());

        return shoppingItemMapper.entityToDto(shoppingItemRepository.save(item));
    }

    @Override
    @Transactional
    public ShoppingItemDto updateStatus(Long id, ShoppingItemStatusUpdateDto dto, ApplicationUser currentUser) {
        LOGGER.debug("Update status of shopping item {} to {} by user {}",
            id, dto.status(), currentUser.getEmail());

        ShoppingItem item = loadAndAuthorizeView(id, currentUser);
        item.setStatus(dto.status());

        return shoppingItemMapper.entityToDto(shoppingItemRepository.save(item));
    }

    @Override
    @Transactional
    public void delete(Long id, ApplicationUser currentUser) {
        LOGGER.debug("Delete shopping item {} by user {}", id, currentUser.getEmail());
        ShoppingItem item = loadAndAuthorizeManage(id, currentUser);
        shoppingItemRepository.delete(item);
    }

    // ------------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------------

    private Roommate requireActiveRoommate(ApplicationUser currentUser) {
        return roommateRepository.findByUserIdAndActiveTrue(currentUser.getId())
            .orElseThrow(() -> new ForbiddenException(
                "User does not belong to any active household."));
    }

    private ShoppingItem loadAndAuthorizeView(Long id, ApplicationUser currentUser) {
        ShoppingItem item = shoppingItemRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(
                "Shopping item with id " + id + " not found."));

        if (!canView(item, currentUser)) {
            throw new ForbiddenException("You are not allowed to access this shopping item.");
        }
        return item;
    }

    private ShoppingItem loadAndAuthorizeManage(Long id, ApplicationUser currentUser) {
        ShoppingItem item = shoppingItemRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(
                "Shopping item with id " + id + " not found."));

        if (!canManage(item, currentUser)) {
            throw new ForbiddenException("You are not allowed to modify this shopping item.");
        }
        return item;
    }

    /**
     * Who can see / change-status an item. In FR-07 this collapses to "creator
     * (in their household) or admin". FR-08 makes it more permissive by adding
     * branches for TARGETED recipients and SHARED items inside the same household.
     */
    private boolean canView(ShoppingItem item, ApplicationUser currentUser) {
        if (Boolean.TRUE.equals(currentUser.getAdmin())) {
            return true;
        }
        // Household gate: only members of the item's household can see it at all.
        if (!isMemberOfItemHousehold(item, currentUser)) {
            return false;
        }
        return item.getCreatedBy().getId().equals(currentUser.getId());
    }

    /**
     * Who can edit or delete an item. Stricter than {@link #canView}: only the
     * creator (or an admin) can manage. FR-08 keeps this rule but loosens canView.
     */
    private boolean canManage(ShoppingItem item, ApplicationUser currentUser) {
        if (Boolean.TRUE.equals(currentUser.getAdmin())) {
            return true;
        }
        if (!isMemberOfItemHousehold(item, currentUser)) {
            return false;
        }
        return item.getCreatedBy().getId().equals(currentUser.getId());
    }

    private boolean isMemberOfItemHousehold(ShoppingItem item, ApplicationUser currentUser) {
        return roommateRepository.existsByUserIdAndHouseholdIdAndActiveTrue(
            currentUser.getId(), item.getHousehold().getId());
    }


}
