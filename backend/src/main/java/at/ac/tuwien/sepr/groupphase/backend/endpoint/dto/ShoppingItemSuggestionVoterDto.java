package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * A household member who voted for a shopping-item suggestion.
 *
 * @param userId    user id
 * @param email     user email
 * @param firstName first name
 * @param lastName  last name
 */
public record ShoppingItemSuggestionVoterDto(
    Long userId,
    String email,
    String firstName,
    String lastName
) {}