package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestDbCleaner;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.QuantityUnit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationSettingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
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

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Integration tests for the FR-18 QR scan endpoint
 * ({@code POST /api/v1/shopping-items/scan}). Exercises the real security layer,
 * service, validation boundary, and persistence.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ShoppingItemScanEndpointTest implements TestData {

    private static final String SCAN_URI = BASE_URI + "/shopping-items/scan";

    @Autowired private MockMvc mockMvc;
    @Autowired private ShoppingItemRepository shoppingItemRepository;
    @Autowired private HouseholdRepository householdRepository;
    @Autowired private RoommateRepository roommateRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private NotificationSettingRepository notificationSettingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtTokenizer jwtTokenizer;
    @Autowired private SecurityProperties securityProperties;
    @Autowired private tools.jackson.databind.json.JsonMapper jsonMapper;
    @Autowired private TaskRepository taskRepository;
    @Autowired private CleaningScheduleRepository cleaningScheduleRepository;

    private ApplicationUser userInHousehold;
    private ApplicationUser userWithoutHousehold;

    private static String scanBody(String payload) {
        // Wrap the raw QR text as the JSON string value of the "payload" field.
        return "{\"payload\":" + jsonString(payload) + "}";
    }

    private static String jsonString(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    @BeforeEach
    public void beforeEach() {
        taskRepository.deleteAll();
        cleaningScheduleRepository.deleteAll();
        shoppingItemRepository.deleteAll();
        roommateRepository.deleteAll();
        householdRepository.deleteAll();
        TestDbCleaner.clearNotificationData(notificationRepository, notificationSettingRepository);
        userRepository.deleteAll();

        userInHousehold = new ApplicationUser("scanner@test.com", "pass", "Scan", "User", false);
        userWithoutHousehold = new ApplicationUser("nohome@test.com", "pass", "No", "Home", false);
        userRepository.saveAll(List.of(userInHousehold, userWithoutHousehold));

        Household household = new Household();
        household.setName("Scan WG");
        household.setCreatedBy(userInHousehold);
        householdRepository.save(household);

        Roommate roommate = new Roommate();
        roommate.setUser(userInHousehold);
        roommate.setHousehold(household);
        roommate.setRole(HouseholdRole.MAIN_TENANT);
        roommate.setActive(true);
        roommateRepository.save(roommate);
    }

    @Test
    public void scanValidPayloadCreatesSharedNeededItemForCurrentUser() throws Exception {
        String qr = "{\"type\":\"wgms.shopping-item\",\"version\":1,\"name\":\"Oat Milk\","
            + "\"quantityCount\":2,\"quantityAmount\":1,\"quantityUnit\":\"LITER\",\"visibility\":\"SHARED\"}";

        MvcResult result = mockMvc.perform(post(SCAN_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("scanner@test.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(scanBody(qr)))
            .andReturn();

        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());

        ShoppingItemDto created = jsonMapper.readValue(
            result.getResponse().getContentAsString(), ShoppingItemDto.class);
        assertNotNull(created.id());
        assertEquals("Oat Milk", created.name());
        assertEquals(2, created.quantityCount());
        assertEquals(0, new BigDecimal("1").compareTo(created.quantityAmount()));
        assertEquals(QuantityUnit.LITER, created.quantityUnit());
        assertEquals(ShoppingItemVisibility.SHARED, created.visibility());
        assertEquals(ShoppingItemStatus.NEEDED, created.status());
        assertEquals("scanner@test.com", created.createdByEmail());
        assertNull(created.targetUserId());

        assertEquals(1, shoppingItemRepository.findAll().size());
    }

    @Test
    public void scanMinimalPayloadDefaultsToSharedItem() throws Exception {
        String qr = "{\"type\":\"wgms.shopping-item\",\"version\":1,\"name\":\"Bread\"}";

        MvcResult result = mockMvc.perform(post(SCAN_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("scanner@test.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(scanBody(qr)))
            .andReturn();

        assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus());
        ShoppingItemDto created = jsonMapper.readValue(
            result.getResponse().getContentAsString(), ShoppingItemDto.class);
        assertEquals("Bread", created.name());
        assertEquals(ShoppingItemVisibility.SHARED, created.visibility());
    }

    @Test
    public void scanInvalidJsonReturns400() throws Exception {
        MvcResult result = mockMvc.perform(post(SCAN_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("scanner@test.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(scanBody("https://example.com/just-a-url")))
            .andReturn();

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertEquals(0, shoppingItemRepository.findAll().size());
    }

    @Test
    public void scanForeignTypeReturns400() throws Exception {
        String qr = "{\"type\":\"some.other.app\",\"version\":1,\"name\":\"Milk\"}";

        MvcResult result = mockMvc.perform(post(SCAN_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("scanner@test.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(scanBody(qr)))
            .andReturn();

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    public void scanTargetedVisibilityReturns400() throws Exception {
        String qr = "{\"type\":\"wgms.shopping-item\",\"version\":1,\"name\":\"Milk\",\"visibility\":\"TARGETED\"}";

        MvcResult result = mockMvc.perform(post(SCAN_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("scanner@test.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(scanBody(qr)))
            .andReturn();

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }

    @Test
    public void scanWithoutActiveHouseholdReturns403() throws Exception {
        String qr = "{\"type\":\"wgms.shopping-item\",\"version\":1,\"name\":\"Milk\"}";

        MvcResult result = mockMvc.perform(post(SCAN_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken("nohome@test.com", USER_ROLES))
                .contentType(MediaType.APPLICATION_JSON)
                .content(scanBody(qr)))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
        assertEquals(0, shoppingItemRepository.findAll().size());
    }

    @Test
    public void scanWithoutAuthenticationIsRejected() throws Exception {
        String qr = "{\"type\":\"wgms.shopping-item\",\"version\":1,\"name\":\"Milk\"}";

        MvcResult result = mockMvc.perform(post(SCAN_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(scanBody(qr)))
            .andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(true, status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value());
    }
}