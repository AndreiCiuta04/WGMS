package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ShoppingItemEndpointTest implements TestData {

    private static final String SHOPPING_ITEM_BASE_URI = BASE_URI + "/shopping-items";

    @Autowired private MockMvc mockMvc;
    @Autowired private ShoppingItemRepository shoppingItemRepository;
    @Autowired private HouseholdRepository householdRepository;
    @Autowired private RoommateRepository roommateRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtTokenizer jwtTokenizer;
    @Autowired private SecurityProperties securityProperties;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private TaskRepository taskRepository;

    private ApplicationUser userA;
    private ApplicationUser userB;
    private Household household;

    @BeforeEach
    public void beforeEach() {
        taskRepository.deleteAll();
        shoppingItemRepository.deleteAll();
        roommateRepository.deleteAll();
        householdRepository.deleteAll();
        userRepository.deleteAll();

        userA = new ApplicationUser("usera@test.com", "pass", "User", "A", false);
        userB = new ApplicationUser("userb@test.com", "pass", "User", "B", false);
        userRepository.saveAll(List.of(userA, userB));

        household = new Household();
        household.setName("Integration WG");
        household.setCreatedBy(userA);
        householdRepository.save(household);

        Roommate roommateA = new Roommate();
        roommateA.setUser(userA);
        roommateA.setHousehold(household);
        roommateA.setRole(HouseholdRole.MAIN_TENANT);
        roommateA.setActive(true);
        roommateRepository.save(roommateA);
        // userB intentionally has no household — for the "not a member" cases
    }

    @AfterEach
    public void afterEach() {
        taskRepository.deleteAll();
        shoppingItemRepository.deleteAll();
        roommateRepository.deleteAll();
        householdRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void givenUserInHousehold_whenCreateItem_then201AndAttachedToHousehold() throws Exception {
        ShoppingItemCreateDto dto = new ShoppingItemCreateDto("Milk", "2L", "Whole milk if available");
        String body = jsonMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(SHOPPING_ITEM_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)).andReturn();

        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());

        ShoppingItemDto created = jsonMapper.readValue(result.getResponse().getContentAsString(), ShoppingItemDto.class);
        assertNotNull(created.id());
        assertEquals("Milk", created.name());
        assertEquals("2L", created.quantity());
        assertEquals(ShoppingItemStatus.NEEDED, created.status());
        assertEquals(household.getId(), created.householdId());
        assertEquals("usera@test.com", created.createdByEmail());
    }

    @Test
    public void givenUserWithoutHousehold_whenCreateItem_then403() throws Exception {
        ShoppingItemCreateDto dto = new ShoppingItemCreateDto("Milk", null, null);
        String body = jsonMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(SHOPPING_ITEM_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userb@test.com", USER_ROLES))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)).andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void givenOwnItems_whenFindAll_thenItemsReturned() throws Exception {
        seedItem("Bread", userA);
        seedItem("Eggs", userA);

        MvcResult result = mockMvc.perform(get(SHOPPING_ITEM_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        ShoppingItemDto[] items = jsonMapper.readValue(result.getResponse().getContentAsString(), ShoppingItemDto[].class);
        assertEquals(2, items.length);
    }

    @Test
    public void givenOwnItem_whenMarkAsBought_thenStatusIsUpdated() throws Exception {
        ShoppingItem item = seedItem("Bananas", userA);

        String body = jsonMapper.writeValueAsString(new ShoppingItemStatusUpdateDto(ShoppingItemStatus.BOUGHT));
        MvcResult result = mockMvc.perform(patch(SHOPPING_ITEM_BASE_URI + "/" + item.getId() + "/status")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        ShoppingItemDto dto = jsonMapper.readValue(result.getResponse().getContentAsString(), ShoppingItemDto.class);
        assertEquals(ShoppingItemStatus.BOUGHT, dto.status());
    }

    @Test
    public void givenItemOfOtherHousehold_whenFindOne_then403() throws Exception {
        // Create a second household + roommate so userB is also in a household, just not userA's.
        Household otherHousehold = new Household();
        otherHousehold.setName("Other WG");
        otherHousehold.setCreatedBy(userB);
        householdRepository.save(otherHousehold);

        Roommate roommateB = new Roommate();
        roommateB.setUser(userB);
        roommateB.setHousehold(otherHousehold);
        roommateB.setRole(HouseholdRole.MAIN_TENANT);
        roommateB.setActive(true);
        roommateRepository.save(roommateB);

        ShoppingItem itemInA = seedItem("Soap", userA);

        MvcResult result = mockMvc.perform(get(SHOPPING_ITEM_BASE_URI + "/" + itemInA.getId())
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userb@test.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void givenOwnItem_whenDelete_then204AndItemGone() throws Exception {
        ShoppingItem item = seedItem("Old item", userA);

        MvcResult result = mockMvc.perform(delete(SHOPPING_ITEM_BASE_URI + "/" + item.getId())
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.NO_CONTENT.value(), result.getResponse().getStatus());
        assertEquals(0, shoppingItemRepository.findAll().size());
    }

    @Test
    public void givenNoJwt_whenCreate_then403() throws Exception {
        ShoppingItemCreateDto body = new ShoppingItemCreateDto("Bread", "1 loaf", null);

        MvcResult result = mockMvc.perform(post(SHOPPING_ITEM_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(body)))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void givenBlankName_whenCreate_then400WithValidationErrors() throws Exception {
        ShoppingItemCreateDto body = new ShoppingItemCreateDto("", null, null);

        MvcResult result = mockMvc.perform(post(SHOPPING_ITEM_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(body))
                .header(securityProperties.getAuthHeader(),
                    jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("Validation errors"),
            "Response should contain 'Validation errors' key; was: " + responseBody);
    }

    private ShoppingItem seedItem(String name, ApplicationUser creator) {
        ShoppingItem item = new ShoppingItem();
        item.setName(name);
        item.setHousehold(household);
        item.setCreatedBy(creator);
        item.setStatus(ShoppingItemStatus.NEEDED);
        return shoppingItemRepository.save(item);
    }
}
