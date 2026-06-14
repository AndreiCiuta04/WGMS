package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestDbCleaner;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AggregatedShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.QuantityUnit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationSettingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShoppingItemRepository shoppingItemRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private RoommateRepository roommateRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CleaningScheduleRepository cleaningScheduleRepository;

    private ApplicationUser userA;
    private ApplicationUser userB;
    private ApplicationUser userC;
    private Household household;
    private Household otherHousehold;

    @BeforeEach
    public void beforeEach() {
        taskRepository.deleteAll();
        cleaningScheduleRepository.deleteAll();
        shoppingItemRepository.deleteAll();
        roommateRepository.deleteAll();
        householdRepository.deleteAll();
        TestDbCleaner.clearNotificationData(notificationRepository, notificationSettingRepository);
        userRepository.deleteAll();

        userA = new ApplicationUser("usera@test.com", "pass", "User", "A", false);
        userB = new ApplicationUser("userb@test.com", "pass", "User", "B", false);
        userC = new ApplicationUser("userc@test.com", "pass", "User", "C", false);
        userRepository.saveAll(List.of(userA, userB, userC));

        household = new Household();
        household.setName("Integration WG");
        household.setCreatedBy(userA);
        householdRepository.save(household);

        Roommate ra = new Roommate();
        ra.setUser(userA);
        ra.setHousehold(household);
        ra.setRole(HouseholdRole.MAIN_TENANT);
        ra.setActive(true);

        Roommate rb = new Roommate();
        rb.setUser(userB);
        rb.setHousehold(household);
        rb.setRole(HouseholdRole.ROOMMATE);
        rb.setActive(true);
        roommateRepository.saveAll(List.of(ra, rb));

        // userC lives in a different household — used to verify cross-household isolation
        otherHousehold = new Household();
        otherHousehold.setName("Other WG");
        otherHousehold.setCreatedBy(userC);
        householdRepository.save(otherHousehold);

        Roommate rc = new Roommate();
        rc.setUser(userC);
        rc.setHousehold(otherHousehold);
        rc.setRole(HouseholdRole.MAIN_TENANT);
        rc.setActive(true);
        roommateRepository.save(rc);
    }

    @AfterEach
    public void afterEach() {
        taskRepository.deleteAll();
        cleaningScheduleRepository.deleteAll();
        shoppingItemRepository.deleteAll();
        roommateRepository.deleteAll();
        householdRepository.deleteAll();
        TestDbCleaner.clearNotificationData(notificationRepository, notificationSettingRepository);
        userRepository.deleteAll();
    }

    @Test
    public void givenUserInHousehold_whenCreateItem_then201AndAttachedToHousehold() throws Exception {
        ShoppingItemCreateDto dto = new ShoppingItemCreateDto(
            "Milk", null, new BigDecimal("2"), QuantityUnit.LITER, "Whole milk", ShoppingItemVisibility.PERSONAL, null);
        String body = jsonMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(SHOPPING_ITEM_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)).andReturn();

        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());

        ShoppingItemDto created = jsonMapper.readValue(result.getResponse().getContentAsString(), ShoppingItemDto.class);
        assertNotNull(created.id());
        assertEquals("Milk", created.name());
        assertEquals(0, new BigDecimal("2").compareTo(created.quantityAmount()));
        assertEquals(QuantityUnit.LITER, created.quantityUnit());
        assertEquals(ShoppingItemStatus.NEEDED, created.status());
        assertEquals(household.getId(), created.householdId());
        assertEquals("usera@test.com", created.createdByEmail());
    }

    @Test
    public void givenUserWithoutHousehold_whenCreateItem_then403() throws Exception {
        // We temporarily deactivate userB's roommate record to test "without household" scenario
        ApplicationUser user = userRepository.findUserByEmail("userb@test.com");
        roommateRepository.findByUserIdAndActiveTrue(user.getId()).ifPresent(r -> {
            r.setActive(false);
            roommateRepository.save(r);
        });

        ShoppingItemCreateDto dto = new ShoppingItemCreateDto("Milk", null, null, null, null, ShoppingItemVisibility.PERSONAL, null);
        String body = jsonMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(SHOPPING_ITEM_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userb@test.com", USER_ROLES))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)).andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void givenUserCreatesTargetedItem_whenTargetGetsTargetedAtMe_thenItemVisible() throws Exception {
        ShoppingItemCreateDto dto = new ShoppingItemCreateDto(
            "Tomatoes", null, new BigDecimal("1"), QuantityUnit.KILOGRAM, null, ShoppingItemVisibility.TARGETED, "userb@test.com");
        String body = jsonMapper.writeValueAsString(dto);

        MvcResult createResult = mockMvc.perform(post(SHOPPING_ITEM_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)).andReturn();
        assertEquals(HttpStatus.CREATED.value(), createResult.getResponse().getStatus());

        MvcResult targetedResult = mockMvc.perform(get(SHOPPING_ITEM_BASE_URI + "/targeted-at-me")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userb@test.com", USER_ROLES)))
            .andReturn();
        assertEquals(HttpStatus.OK.value(), targetedResult.getResponse().getStatus());

        ShoppingItemDto[] items = jsonMapper.readValue(
            targetedResult.getResponse().getContentAsString(), ShoppingItemDto[].class);
        assertEquals(1, items.length);
        assertEquals("Tomatoes", items[0].name());
        assertEquals("userb@test.com", items[0].targetUserEmail());
    }

    @Test
    public void givenTargetedItem_whenTargetMarksAsBought_thenStatusUpdates() throws Exception {
        ShoppingItem item = seedItem("Apples", userA, ShoppingItemVisibility.TARGETED, userB);

        String body = jsonMapper.writeValueAsString(new ShoppingItemStatusUpdateDto(ShoppingItemStatus.BOUGHT));
        MvcResult result = mockMvc.perform(patch(SHOPPING_ITEM_BASE_URI + "/" + item.getId() + "/status")
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userb@test.com", USER_ROLES))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        ShoppingItemDto dto = jsonMapper.readValue(result.getResponse().getContentAsString(), ShoppingItemDto.class);
        assertEquals(ShoppingItemStatus.BOUGHT, dto.status());
    }

    @Test
    public void givenSharedItemInHousehold_whenOtherMemberGetsAll_thenItemVisible() throws Exception {
        seedItem("Toilet paper", userA, ShoppingItemVisibility.SHARED, null);

        MvcResult result = mockMvc.perform(get(SHOPPING_ITEM_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userb@test.com", USER_ROLES)))
            .andReturn();

        ShoppingItemDto[] items = jsonMapper.readValue(result.getResponse().getContentAsString(), ShoppingItemDto[].class);
        assertEquals(1, items.length);
        assertEquals("Toilet paper", items[0].name());
    }

    @Test
    public void givenSharedItem_whenUserFromOtherHouseholdGetsAll_thenInvisible() throws Exception {
        seedItem("Toilet paper", userA, ShoppingItemVisibility.SHARED, null);

        MvcResult result = mockMvc.perform(get(SHOPPING_ITEM_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userc@test.com", USER_ROLES)))
            .andReturn();

        ShoppingItemDto[] items = jsonMapper.readValue(result.getResponse().getContentAsString(), ShoppingItemDto[].class);
        assertTrue(items.length == 0, "SHARED visibility must NOT leak across households");
    }

    @Test
    public void givenPersonalItemOfRoommate_whenOtherMemberGetsAll_thenInvisible() throws Exception {
        seedItem("Diary entry", userA, ShoppingItemVisibility.PERSONAL, null);

        MvcResult result = mockMvc.perform(get(SHOPPING_ITEM_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userb@test.com", USER_ROLES)))
            .andReturn();

        ShoppingItemDto[] items = jsonMapper.readValue(result.getResponse().getContentAsString(), ShoppingItemDto[].class);
        assertEquals(0, items.length);
    }

    @Test
    public void givenCreateTargetedAtOutsideUser_then409() throws Exception {
        ShoppingItemCreateDto dto = new ShoppingItemCreateDto(
            "Tomatoes", null, null, null, null, ShoppingItemVisibility.TARGETED, "userc@test.com");
        String body = jsonMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(SHOPPING_ITEM_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)).andReturn();

        assertEquals(HttpStatus.CONFLICT.value(), result.getResponse().getStatus());
    }

    @Test
    public void givenCreateTargetedAtSelf_then409() throws Exception {
        ShoppingItemCreateDto dto = new ShoppingItemCreateDto(
            "Tomatoes", null, null, null, null, ShoppingItemVisibility.TARGETED, "usera@test.com");
        String body = jsonMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(SHOPPING_ITEM_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)).andReturn();

        assertEquals(HttpStatus.CONFLICT.value(), result.getResponse().getStatus());
    }

    @Test
    public void givenNoJwt_whenCreate_then403() throws Exception {
        ShoppingItemCreateDto dto = new ShoppingItemCreateDto(
            "Bread", null, null, null, null, ShoppingItemVisibility.PERSONAL, null);
        String body = jsonMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(SHOPPING_ITEM_BASE_URI)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)).andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void givenBlankName_whenCreate_then400WithValidationErrors() throws Exception {
        ShoppingItemCreateDto dto = new ShoppingItemCreateDto(
            "", null, null, null, null, ShoppingItemVisibility.PERSONAL, null);
        String body = jsonMapper.writeValueAsString(dto);

        MvcResult result = mockMvc.perform(post(SHOPPING_ITEM_BASE_URI)
            .header(securityProperties.getAuthHeader(),
                jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)).andReturn();

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("Validation errors"),
            "Response should contain 'Validation errors' key; was: " + responseBody);
    }

    @Test
    public void givenOwnItems_whenFindAll_thenItemsReturned() throws Exception {
        seedItem("Bread", userA, ShoppingItemVisibility.PERSONAL, null);
        seedItem("Eggs", userA, ShoppingItemVisibility.PERSONAL, null);

        MvcResult result = mockMvc.perform(get(SHOPPING_ITEM_BASE_URI)
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        ShoppingItemDto[] items = jsonMapper.readValue(result.getResponse().getContentAsString(), ShoppingItemDto[].class);
        assertEquals(2, items.length);
    }

    @Test
    public void givenItemOfOtherHousehold_whenFindOne_then403() throws Exception {
        ShoppingItem itemInA = seedItem("Soap", userA, ShoppingItemVisibility.PERSONAL, null);

        MvcResult result = mockMvc.perform(get(SHOPPING_ITEM_BASE_URI + "/" + itemInA.getId())
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userc@test.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }

    @Test
    public void givenOwnItem_whenDelete_then204AndItemGone() throws Exception {
        ShoppingItem item = seedItem("Old item", userA, ShoppingItemVisibility.PERSONAL, null);

        MvcResult result = mockMvc.perform(delete(SHOPPING_ITEM_BASE_URI + "/" + item.getId())
            .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.NO_CONTENT.value(), result.getResponse().getStatus());
        assertEquals(0, shoppingItemRepository.findAll().size());
    }

    @Test
    public void givenSharedNeededItems_whenGetAggregatedShoppingList_thenOnlyHouseholdSharedNeededItemsGrouped() throws Exception {
        seedItem("Milk", userA, ShoppingItemVisibility.SHARED, null);
        seedItem(" milk ", userB, ShoppingItemVisibility.SHARED, null);

        ShoppingItem bought = seedItem("Bread", userA, ShoppingItemVisibility.SHARED, null);
        bought.setStatus(ShoppingItemStatus.BOUGHT);
        shoppingItemRepository.save(bought);

        seedItem("Private item", userA, ShoppingItemVisibility.PERSONAL, null);

        MvcResult result = mockMvc.perform(get(SHOPPING_ITEM_BASE_URI + "/aggregated")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        AggregatedShoppingItemDto[] items = jsonMapper.readValue(
            result.getResponse().getContentAsString(),
            AggregatedShoppingItemDto[].class
        );

        assertEquals(1, items.length);
        assertEquals("Milk", items[0].name());
        assertEquals(2, items[0].itemCount());
        assertEquals(2, items[0].sourceItems().size());
    }

    @Test
    public void givenUserFromOtherHousehold_whenGetAggregatedShoppingList_thenDoesNotLeakItems() throws Exception {
        seedItem("Toilet paper", userA, ShoppingItemVisibility.SHARED, null);

        MvcResult result = mockMvc.perform(get(SHOPPING_ITEM_BASE_URI + "/aggregated")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("userc@test.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        AggregatedShoppingItemDto[] items = jsonMapper.readValue(
            result.getResponse().getContentAsString(),
            AggregatedShoppingItemDto[].class
        );

        assertEquals(0, items.length);
    }

    @Test
    public void givenHistoryItems_whenGetShoppingHistory_thenOnlyCurrentHouseholdBoughtAndCancelledItemsReturned() throws Exception {
        ShoppingItem bought = seedItem("Bought milk", userA, ShoppingItemVisibility.SHARED, null);
        bought.setStatus(ShoppingItemStatus.BOUGHT);
        shoppingItemRepository.save(bought);

        ShoppingItem cancelled = seedItem("Cancelled bread", userB, ShoppingItemVisibility.PERSONAL, null);
        cancelled.setStatus(ShoppingItemStatus.CANCELLED);
        shoppingItemRepository.save(cancelled);

        seedItem("Needed eggs", userA, ShoppingItemVisibility.SHARED, null);

        ShoppingItem otherHouseholdBought = seedItemInHousehold(
            "Other household bought",
            otherHousehold,
            userC,
            ShoppingItemVisibility.SHARED,
            null
        );
        otherHouseholdBought.setStatus(ShoppingItemStatus.BOUGHT);
        shoppingItemRepository.save(otherHouseholdBought);

        MvcResult result = mockMvc.perform(get(SHOPPING_ITEM_BASE_URI + "/history")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("usera@test.com", USER_ROLES)))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        ShoppingItemDto[] items = jsonMapper.readValue(
            result.getResponse().getContentAsString(),
            ShoppingItemDto[].class
        );
        List<String> names = java.util.Arrays.stream(items).map(ShoppingItemDto::name).toList();

        assertEquals(2, items.length);
        assertTrue(names.contains("Bought milk"));
        assertTrue(names.contains("Cancelled bread"));
        assertTrue(!names.contains("Needed eggs"));
        assertTrue(!names.contains("Other household bought"));
        assertTrue(java.util.Arrays.stream(items).allMatch(item -> item.householdId().equals(household.getId())));
        assertTrue(java.util.Arrays.stream(items).allMatch(item ->
            item.status() == ShoppingItemStatus.BOUGHT || item.status() == ShoppingItemStatus.CANCELLED));
    }

    private ShoppingItem seedItem(String name, ApplicationUser creator, ShoppingItemVisibility visibility, ApplicationUser target) {
        return seedItemInHousehold(name, household, creator, visibility, target);
    }

    private ShoppingItem seedItemInHousehold(String name, Household household, ApplicationUser creator,
                                             ShoppingItemVisibility visibility, ApplicationUser target) {
        ShoppingItem item = new ShoppingItem();
        item.setName(name);
        item.setHousehold(household);
        item.setCreatedBy(creator);
        item.setStatus(ShoppingItemStatus.NEEDED);
        item.setVisibility(visibility);
        item.setTargetUser(target);
        return shoppingItemRepository.save(item);
    }
}
