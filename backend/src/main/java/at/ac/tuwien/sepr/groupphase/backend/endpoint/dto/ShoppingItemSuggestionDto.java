package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response body for a shopping item suggestion with voting information.
 *
 * @param id                 suggestion id
 * @param name               suggested item name
 * @param quantity           free-text quantity
 * @param notes              free-text notes
 * @param householdId        id of the household
 * @param createdById        id of the creator
 * @param createdByEmail     email of the creator
 * @param createdByFirstName first name of the creator
 * @param createdByLastName  last name of the creator
 * @param voteCount          number of votes
 * @param votedByCurrentUser whether the current user has voted
 * @param voters             household members who voted for the suggestion
 * @param createdAt          creation timestamp
 * @param updatedAt          last update timestamp
 */
public record ShoppingItemSuggestionDto(
    Long id,
    String name,
    String quantity,
    String notes,
    Long householdId,
    Long createdById,
    String createdByEmail,
    String createdByFirstName,
    String createdByLastName,
    int voteCount,
    boolean votedByCurrentUser,
    List<ShoppingItemSuggestionVoterDto> voters,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}