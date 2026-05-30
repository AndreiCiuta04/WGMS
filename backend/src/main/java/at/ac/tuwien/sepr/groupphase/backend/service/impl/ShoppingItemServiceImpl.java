package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ShoppingItemMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ShoppingItemService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class ShoppingItemServiceImpl implements ShoppingItemService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ShoppingItemRepository shoppingItemRepository;
    private final RoommateRepository roommateRepository;
    private final UserService userService;
    private final ShoppingItemMapper shoppingItemMapper;

    @Autowired
    public ShoppingItemServiceImpl(ShoppingItemRepository shoppingItemRepository,
                                   RoommateRepository roommateRepository,
                                   UserService userService,
                                   ShoppingItemMapper shoppingItemMapper) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.roommateRepository = roommateRepository;
        this.userService = userService;
        this.shoppingItemMapper = shoppingItemMapper;
    }

    @Override
    @Transactional
    public ShoppingItemDto create(ShoppingItemCreateDto dto, ApplicationUser currentUser) {
        LOGGER.debug("Create shopping item {} ({}) for user {}",
            dto.name(), dto.visibility(), currentUser.getEmail());

        Roommate roommate = requireActiveRoommate(currentUser);

        ApplicationUser target = resolveAndValidateTarget(
            dto.visibility(), dto.targetUserEmail(), currentUser, roommate.getHousehold(), null);

        ShoppingItem item = new ShoppingItem();
        item.setName(dto.name());
        item.setQuantity(dto.quantity());
        item.setNotes(dto.notes());
        item.setStatus(ShoppingItemStatus.NEEDED);
        item.setVisibility(dto.visibility());
        item.setHousehold(roommate.getHousehold());
        item.setCreatedBy(currentUser);
        item.setTargetUser(target);

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
        Long householdId = roommate.getHousehold().getId();
        Long userId = currentUser.getId();

        List<ShoppingItem> items = shoppingItemRepository.findVisibleItems(householdId, userId, ShoppingItemVisibility.SHARED);
        return shoppingItemMapper.entityToDto(items);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShoppingItemDto> findTargetedAtCurrentUser(ApplicationUser currentUser) {
        LOGGER.debug("Find shopping items targeted at user {}", currentUser.getEmail());
        Roommate roommate = requireActiveRoommate(currentUser);
        return shoppingItemMapper.entityToDto(
            shoppingItemRepository.findAllByHouseholdIdAndTargetUserIdOrderByCreatedAtDesc(
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

        ApplicationUser target = resolveAndValidateTarget(
            dto.visibility(), dto.targetUserEmail(), currentUser, item.getHousehold(), item.getTargetUser());

        item.setName(dto.name());
        item.setQuantity(dto.quantity());
        item.setNotes(dto.notes());
        item.setVisibility(dto.visibility());
        item.setTargetUser(target);

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

    private boolean canView(ShoppingItem item, ApplicationUser currentUser) {
        if (Boolean.TRUE.equals(currentUser.getAdmin())) {
            return true;
        }
        if (!isMemberOfItemHousehold(item, currentUser)) {
            return false;
        }
        if (item.getCreatedBy().getId().equals(currentUser.getId())) {
            return true;
        }
        if (item.getTargetUser() != null
            && item.getTargetUser().getId().equals(currentUser.getId())) {
            return true;
        }
        return item.getVisibility() == ShoppingItemVisibility.SHARED;
    }

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

    private ApplicationUser resolveAndValidateTarget(ShoppingItemVisibility visibility,
                                                     String targetEmail,
                                                     ApplicationUser currentUser,
                                                     Household household,
                                                     ApplicationUser existingTarget) {
        if (visibility != ShoppingItemVisibility.TARGETED) {
            return null;
        }
        if (targetEmail == null || targetEmail.isBlank()) {
            throw new ValidationException("targetUserEmail is required for TARGETED visibility.");
        }
        String sanitizedEmail = targetEmail.trim().toLowerCase();
        if (sanitizedEmail.equalsIgnoreCase(currentUser.getEmail().trim().toLowerCase())) {
            throw new ConflictException(
                "Cannot target a shopping request at yourself; use PERSONAL visibility instead.");
        }
        ApplicationUser target = userService.findApplicationUserByEmail(sanitizedEmail);
        if (target == null) {
            throw new NotFoundException("Target user " + targetEmail + " not found.");
        }
        
        // Relax check: if target is the same as existing target, bypass active validation
        if (existingTarget != null && existingTarget.getId().equals(target.getId())) {
            return target;
        }

        boolean targetInSameHousehold = roommateRepository
            .existsByUserIdAndHouseholdIdAndActiveTrue(target.getId(), household.getId());
        if (!targetInSameHousehold) {
            throw new ConflictException(
                "Target user " + targetEmail + " is not an active member of this household.");
        }
        return target;
    }
}
