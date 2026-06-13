package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AggregatedShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AggregatedShoppingItemSourceDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemSuggestionCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemSuggestionDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemSuggestionVoterDto;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

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
            return shoppingItemMapper.entityToDto(shoppingItemRepository.findAllBySuggestedFalseOrderByCreatedAtDesc());
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

    @Override
    @Transactional(readOnly = true)
    public List<AggregatedShoppingItemDto> findAggregatedHouseholdShoppingList(ApplicationUser currentUser) {
        LOGGER.debug("Find aggregated household shopping list for user {}", currentUser.getEmail());

        Roommate roommate = requireActiveRoommate(currentUser);

        List<ShoppingItem> sharedNeededItems = shoppingItemRepository
            .findAllByHouseholdIdAndVisibilityAndStatusAndSuggestedFalseOrderByCreatedAtDesc(
                roommate.getHousehold().getId(),
                ShoppingItemVisibility.SHARED,
                ShoppingItemStatus.NEEDED
            );

        LinkedHashMap<AggregationKey, List<ShoppingItem>> groupedItems = sharedNeededItems.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                item -> new AggregationKey(normalize(item.getName()), normalize(item.getQuantity())),
                LinkedHashMap::new,
                java.util.stream.Collectors.toList()
            ));

        return groupedItems.values().stream()
            .map(this::toAggregatedShoppingItemDto)
            .sorted(Comparator.comparing(AggregatedShoppingItemDto::name, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShoppingItemDto> findHouseholdShoppingHistory(ApplicationUser currentUser) {
        LOGGER.debug("Find household shopping history for user {}", currentUser.getEmail());

        Roommate roommate = requireActiveRoommate(currentUser);

        return shoppingItemMapper.entityToDto(
            shoppingItemRepository.findAllByHouseholdIdAndSuggestedFalseAndStatusInOrderByUpdatedAtDesc(
                roommate.getHousehold().getId(),
                List.of(ShoppingItemStatus.BOUGHT, ShoppingItemStatus.CANCELLED)
            )
        );
    }

    @Override
    @Transactional
    public ShoppingItemSuggestionDto createSuggestion(ShoppingItemSuggestionCreateDto dto,
                                                      ApplicationUser currentUser) {
        LOGGER.debug("Create shopping item suggestion {} for user {}",
            dto.name(), currentUser.getEmail());

        Roommate roommate = requireActiveRoommate(currentUser);
        Long householdId = roommate.getHousehold().getId();

        List<ShoppingItem> activeSuggestions =
            shoppingItemRepository.findSuggestionsForHousehold(
                householdId,
                ShoppingItemStatus.NEEDED
            );

        String normalizedName = normalize(dto.name());
        String normalizedQuantity = normalize(dto.quantity());

        ShoppingItem duplicate = activeSuggestions.stream()
            .filter(item -> normalize(item.getName()).equals(normalizedName))
            .filter(item -> normalize(item.getQuantity()).equals(normalizedQuantity))
            .findFirst()
            .orElse(null);

        if (duplicate != null) {
            boolean alreadyVoted = duplicate.getVotedBy().stream()
                .anyMatch(voter -> voter.getId().equals(currentUser.getId()));

            if (!alreadyVoted) {
                duplicate.getVotedBy().add(currentUser);
                duplicate = shoppingItemRepository.save(duplicate);
            }

            return toSuggestionDto(duplicate, currentUser);
        }

        ShoppingItem item = new ShoppingItem();
        item.setName(cleanDisplayValue(dto.name()));
        item.setQuantity(cleanDisplayValue(dto.quantity()));
        item.setNotes(cleanDisplayValue(dto.notes()));
        item.setStatus(ShoppingItemStatus.NEEDED);
        item.setVisibility(ShoppingItemVisibility.SHARED);
        item.setHousehold(roommate.getHousehold());
        item.setCreatedBy(currentUser);
        item.setTargetUser(null);
        item.setSuggested(true);
        item.getVotedBy().add(currentUser);

        return toSuggestionDto(shoppingItemRepository.save(item), currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShoppingItemSuggestionDto> findSuggestions(ApplicationUser currentUser) {
        LOGGER.debug("Find shopping item suggestions for user {}", currentUser.getEmail());

        Roommate roommate = requireActiveRoommate(currentUser);

        return shoppingItemRepository.findSuggestionsForHousehold(
                roommate.getHousehold().getId(),
                ShoppingItemStatus.NEEDED
            ).stream()
            .map(item -> toSuggestionDto(item, currentUser))
            .toList();
    }

    @Override
    @Transactional
    public ShoppingItemSuggestionDto voteForSuggestion(Long id, ApplicationUser currentUser) {
        LOGGER.debug("Vote for shopping item suggestion {} by user {}", id, currentUser.getEmail());

        ShoppingItem item = loadAndAuthorizeSuggestion(id, currentUser);

        boolean alreadyVoted = item.getVotedBy().stream()
            .anyMatch(user -> user.getId().equals(currentUser.getId()));

        if (!alreadyVoted) {
            item.getVotedBy().add(currentUser);
        }

        return toSuggestionDto(shoppingItemRepository.save(item), currentUser);
    }

    @Override
    @Transactional
    public ShoppingItemSuggestionDto removeVoteFromSuggestion(Long id, ApplicationUser currentUser) {
        LOGGER.debug("Remove vote from shopping item suggestion {} by user {}",
            id, currentUser.getEmail());

        ShoppingItem item = loadAndAuthorizeSuggestion(id, currentUser);

        item.getVotedBy().removeIf(user -> user.getId().equals(currentUser.getId()));

        return toSuggestionDto(shoppingItemRepository.save(item), currentUser);
    }

    @Override
    @Transactional
    public ShoppingItemDto promoteSuggestion(Long id, ApplicationUser currentUser) {
        LOGGER.debug("Promote shopping item suggestion {} by user {}",
            id, currentUser.getEmail());

        ShoppingItem item = loadAndAuthorizeSuggestion(id, currentUser);

        item.setSuggested(false);
        item.setVisibility(ShoppingItemVisibility.SHARED);
        item.setStatus(ShoppingItemStatus.NEEDED);
        item.setTargetUser(null);

        /*
         * Votes apply only while the item is a suggestion. Once promoted, clearing
         * the relation prevents obsolete vote records from remaining attached to a
         * normal shopping item.
         */
        item.getVotedBy().clear();

        return shoppingItemMapper.entityToDto(
            shoppingItemRepository.save(item)
        );
    }

    @Override
    @Transactional
    public void deleteSuggestion(Long id, ApplicationUser currentUser) {
        LOGGER.debug("Delete shopping item suggestion {} by user {}",
            id, currentUser.getEmail());

        ShoppingItem item = shoppingItemRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(
                "Shopping item suggestion with id " + id + " not found."));

        if (!item.isSuggested()) {
            throw new ConflictException(
                "Shopping item with id " + id + " is not a suggestion.");
        }

        if (item.getStatus() != ShoppingItemStatus.NEEDED) {
            throw new ConflictException(
                "Only active shopping item suggestions can be removed.");
        }

        if (!canManage(item, currentUser)) {
            throw new ForbiddenException(
                "You are not allowed to remove this shopping item suggestion.");
        }

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

    private AggregatedShoppingItemDto toAggregatedShoppingItemDto(List<ShoppingItem> items) {
        ShoppingItem representative = items.stream()
            .filter(item -> item.getCreatedAt() != null)
            .min(Comparator.comparing(ShoppingItem::getCreatedAt))
            .orElseGet(() -> items.stream()
                .filter(item -> item.getName() != null && item.getName().equals(cleanDisplayValue(item.getName())))
                .findFirst()
                .orElse(items.get(0)));

        List<AggregatedShoppingItemSourceDto> sourceItems = items.stream()
            .map(this::toAggregatedShoppingItemSourceDto)
            .toList();

        return new AggregatedShoppingItemDto(
            cleanDisplayValue(representative.getName()),
            cleanDisplayValue(representative.getQuantity()),
            items.size(),
            sourceItems
        );
    }

    private String cleanDisplayValue(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim().replaceAll("\\s+", " ");
        return cleaned.isEmpty() ? null : cleaned;
    }

    private AggregatedShoppingItemSourceDto toAggregatedShoppingItemSourceDto(ShoppingItem item) {
        return new AggregatedShoppingItemSourceDto(
            item.getId(),
            item.getName(),
            item.getQuantity(),
            item.getNotes(),
            item.getCreatedBy().getId(),
            item.getCreatedBy().getEmail(),
            item.getCreatedBy().getFirstName(),
            item.getCreatedBy().getLastName(),
            item.getCreatedAt()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private record AggregationKey(String normalizedName, String normalizedQuantity) {}

    private ShoppingItem loadAndAuthorizeSuggestion(Long id, ApplicationUser currentUser) {
        Roommate roommate = requireActiveRoommate(currentUser);

        ShoppingItem item = shoppingItemRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(
                "Shopping item suggestion with id " + id + " not found."));

        if (!item.isSuggested()) {
            throw new ConflictException("Shopping item with id " + id + " is not a suggestion.");
        }

        if (!item.getHousehold().getId().equals(roommate.getHousehold().getId())) {
            throw new ForbiddenException("You are not allowed to access this shopping item suggestion.");
        }

        if (item.getStatus() != ShoppingItemStatus.NEEDED) {
            throw new ConflictException("Only active shopping item suggestions can be voted on.");
        }

        return item;
    }

    private ShoppingItemSuggestionDto toSuggestionDto(ShoppingItem item,
                                                      ApplicationUser currentUser) {
        boolean votedByCurrentUser = item.getVotedBy().stream()
            .anyMatch(user -> user.getId().equals(currentUser.getId()));

        List<ShoppingItemSuggestionVoterDto> voters = item.getVotedBy().stream()
            .sorted(
                Comparator.comparing(
                    ApplicationUser::getFirstName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ).thenComparing(
                    ApplicationUser::getLastName,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ).thenComparing(
                    ApplicationUser::getEmail,
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                )
            )
            .map(user -> new ShoppingItemSuggestionVoterDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
            ))
            .toList();

        return new ShoppingItemSuggestionDto(
            item.getId(),
            item.getName(),
            item.getQuantity(),
            item.getNotes(),
            item.getHousehold().getId(),
            item.getCreatedBy().getId(),
            item.getCreatedBy().getEmail(),
            item.getCreatedBy().getFirstName(),
            item.getCreatedBy().getLastName(),
            voters.size(),
            votedByCurrentUser,
            voters,
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }
}
