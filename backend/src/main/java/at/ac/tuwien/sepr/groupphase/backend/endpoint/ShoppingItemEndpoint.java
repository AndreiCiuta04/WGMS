package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AggregatedShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.service.ShoppingItemService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/shopping-items")
public class ShoppingItemEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ShoppingItemService shoppingItemService;
    private final UserService userService;

    @Autowired
    public ShoppingItemEndpoint(ShoppingItemService shoppingItemService, UserService userService) {
        this.shoppingItemService = shoppingItemService;
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public ShoppingItemDto create(@Valid @RequestBody ShoppingItemCreateDto dto,
                                  Authentication authentication) {
        LOGGER.info("POST /api/v1/shopping-items");
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        return shoppingItemService.create(dto, currentUser);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<ShoppingItemDto> findAll(Authentication authentication) {
        LOGGER.info("GET /api/v1/shopping-items");
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        return shoppingItemService.findVisibleItems(currentUser);
    }

    @GetMapping("/targeted-at-me")
    @PreAuthorize("hasRole('USER')")
    public List<ShoppingItemDto> findTargetedAtMe(Authentication authentication) {
        LOGGER.info("GET /api/v1/shopping-items/targeted-at-me");
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        return shoppingItemService.findTargetedAtCurrentUser(currentUser);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ShoppingItemDto findOne(@PathVariable("id") Long id, Authentication authentication) {
        LOGGER.info("GET /api/v1/shopping-items/{}", id);
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        return shoppingItemService.findOne(id, currentUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ShoppingItemDto update(@PathVariable("id") Long id,
                                  @Valid @RequestBody ShoppingItemUpdateDto dto,
                                  Authentication authentication) {
        LOGGER.info("PUT /api/v1/shopping-items/{}", id);
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        return shoppingItemService.update(id, dto, currentUser);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('USER')")
    public ShoppingItemDto updateStatus(@PathVariable("id") Long id,
                                        @Valid @RequestBody ShoppingItemStatusUpdateDto dto,
                                        Authentication authentication) {
        LOGGER.info("PATCH /api/v1/shopping-items/{}/status", id);
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        return shoppingItemService.updateStatus(id, dto, currentUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void delete(@PathVariable("id") Long id, Authentication authentication) {
        LOGGER.info("DELETE /api/v1/shopping-items/{}", id);
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        shoppingItemService.delete(id, currentUser);
    }

    @GetMapping("/aggregated")
    @PreAuthorize("hasRole('USER')")
    public List<AggregatedShoppingItemDto> findAggregatedHouseholdShoppingList(Authentication authentication) {
        LOGGER.info("GET /api/v1/shopping-items/aggregated");
        ApplicationUser currentUser = userService.findApplicationUserByEmail(authentication.getName());
        return shoppingItemService.findAggregatedHouseholdShoppingList(currentUser);
    }
}
