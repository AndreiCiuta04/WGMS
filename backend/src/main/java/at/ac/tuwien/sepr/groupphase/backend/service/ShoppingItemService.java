package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;

import java.util.List;

/**
 * Service interface for managing shopping items.
 */
public interface ShoppingItemService {

    /**
     * Create a new shopping item for the current user. The item is attached to the
     * user's active household.
     *
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException
     *     if the user is not a member of any active household
     */
    ShoppingItemDto create(ShoppingItemCreateDto dto, ApplicationUser currentUser);

    /**
     * List shopping items visible to the current user. In FR-07 this is "items I
     * created in my active household". Admins see every item in the system.
     */
    List<ShoppingItemDto> findVisibleItems(ApplicationUser currentUser);

    /**
     * Returns a shopping item by its id.
     *
     * @param id the id of the shopping item
     * @param currentUser the currently authenticated user
     * @return the requested shopping item
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException
     *     if no item with that id exists
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException
     *     if the user is not allowed to see the item
     */
    ShoppingItemDto findOne(Long id, ApplicationUser currentUser);

    /**
     * Updates an existing shopping item.
     *
     * @param id the id of the shopping item to update
     * @param dto the updated shopping item data
     * @param currentUser the currently authenticated user
     * @return the updated shopping item
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException
     *     if no shopping item with the given id exists
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException
     *     if the user is not allowed to update the shopping item
     */

    ShoppingItemDto update(Long id, ShoppingItemUpdateDto dto, ApplicationUser currentUser);

    /**
     * Updates the status of an existing shopping item.
     *
     * @param id the id of the shopping item
     * @param dto the status update data
     * @param currentUser the currently authenticated user
     * @return the updated shopping item
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException
     *     if no shopping item with the given id exists
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException
     *     if the user is not allowed to update the shopping item
     */
    ShoppingItemDto updateStatus(Long id, ShoppingItemStatusUpdateDto dto, ApplicationUser currentUser);

    /**
     * Deletes a shopping item.
     *
     * @param id the id of the shopping item to delete
     * @param currentUser the currently authenticated user
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException
     *     if no shopping item with the given id exists
     * @throws at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException
     *     if the user is not allowed to delete the shopping item
     */
    void delete(Long id, ApplicationUser currentUser);
}
