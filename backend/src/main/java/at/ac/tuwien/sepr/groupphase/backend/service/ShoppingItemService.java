package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AggregatedShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemSuggestionCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemSuggestionDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;

import java.util.List;

/**
 * Service interface for managing shopping items.
 */
public interface ShoppingItemService {

    /**
     * Creates a new shopping item for the current user. The item is attached to the
     * user's active household. Validates the targeting fields when visibility is
     * {@code TARGETED}.
     *
     * @param dto         the request body
     * @param currentUser the currently authenticated user
     * @return the created shopping item
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException
     *     if the user is not a member of any active household
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException
     *     if targeting fields are invalid (missing email, self-target, target outside household)
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException
     *     if a referenced target user does not exist
     */
    ShoppingItemDto create(ShoppingItemCreateDto dto, ApplicationUser currentUser);

    /**
     * Lists items visible to the current user within their household: items they
     * created, items targeted at them, and items marked {@code SHARED}.
     * Admins see every item in the system.
     *
     * @param currentUser the currently authenticated user
     * @return the visible items, ordered most recent first
     */
    List<ShoppingItemDto> findVisibleItems(ApplicationUser currentUser);

    /**
     * Lists only the items where the current user is the explicit target — the
     * "incoming requests" view.
     *
     * @param currentUser the currently authenticated user
     * @return the items targeted at the current user, ordered most recent first
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException
     *     if the user is not a member of any active household
     */
    List<ShoppingItemDto> findTargetedAtCurrentUser(ApplicationUser currentUser);

    /**
     * Lists the aggregated household shopping list.
     * The aggregation includes only active household items with
     * {@code SHARED} visibility and {@code NEEDED} status. Personal items,
     * targeted requests, bought items, cancelled items, and items from other
     * households are excluded.
     *
     * @param currentUser the currently authenticated user
     * @return aggregated household shopping-list entries
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException
     *     if the user is not a member of any active household
     */
    List<AggregatedShoppingItemDto> findAggregatedHouseholdShoppingList(ApplicationUser currentUser);

    /**
     * Lists the current household's shopping history. History consists of items
     * with {@code BOUGHT} or {@code CANCELLED} status.
     *
     * @param currentUser the currently authenticated user
     * @return shopping history entries, ordered most recently updated first
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException
     *     if the user is not a member of any active household
     */
    List<ShoppingItemDto> findHouseholdShoppingHistory(ApplicationUser currentUser);

    /**
     * Returns a shopping item by its id.
     *
     * @param id          the id of the shopping item
     * @param currentUser the currently authenticated user
     * @return the requested shopping item
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException
     *     if no item with that id exists
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException
     *     if the user is not allowed to see the item
     */
    ShoppingItemDto findOne(Long id, ApplicationUser currentUser);

    /**
     * Updates an existing shopping item, including its visibility and target user.
     * Only the creator (or an admin) may update an item.
     *
     * @param id          the id of the shopping item to update
     * @param dto         the updated shopping item data
     * @param currentUser the currently authenticated user
     * @return the updated shopping item
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException
     *     if no shopping item with the given id exists
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException
     *     if the user is not allowed to update the shopping item
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException
     *     if targeting fields are invalid
     */
    ShoppingItemDto update(Long id, ShoppingItemUpdateDto dto, ApplicationUser currentUser);

    /**
     * Updates the status of an existing shopping item. Allowed for anyone who can
     * see the item (creator, targeted recipient, household member for SHARED, admin).
     *
     * @param id          the id of the shopping item
     * @param dto         the status update data
     * @param currentUser the currently authenticated user
     * @return the updated shopping item
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException
     *     if no shopping item with the given id exists
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException
     *     if the user is not allowed to update the shopping item
     */
    ShoppingItemDto updateStatus(Long id, ShoppingItemStatusUpdateDto dto, ApplicationUser currentUser);

    /**
     * Deletes a shopping item. Only the creator (or an admin) may delete an item.
     *
     * @param id          the id of the shopping item to delete
     * @param currentUser the currently authenticated user
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException
     *     if no shopping item with the given id exists
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException
     *     if the user is not allowed to delete the shopping item
     */
    /**
     * Creates a new household shopping suggestion. The creator's vote is counted
     * automatically.
     *
     * @param dto         the suggestion request body
     * @param currentUser the currently authenticated user
     * @return the created suggestion
     */
    ShoppingItemSuggestionDto createSuggestion(ShoppingItemSuggestionCreateDto dto,
                                               ApplicationUser currentUser);

    /**
     * Lists active suggestions for the current user's household, ordered by vote
     * count and creation date.
     *
     * @param currentUser the currently authenticated user
     * @return active household suggestions
     */
    List<ShoppingItemSuggestionDto> findSuggestions(ApplicationUser currentUser);

    /**
     * Adds the current user's vote to a suggestion.
     *
     * @param id          the suggestion id
     * @param currentUser the currently authenticated user
     * @return the updated suggestion
     */
    ShoppingItemSuggestionDto voteForSuggestion(Long id, ApplicationUser currentUser);

    /**
     * Removes the current user's vote from a suggestion.
     *
     * @param id          the suggestion id
     * @param currentUser the currently authenticated user
     * @return the updated suggestion
     */
    ShoppingItemSuggestionDto removeVoteFromSuggestion(Long id, ApplicationUser currentUser);

    void delete(Long id, ApplicationUser currentUser);
}
