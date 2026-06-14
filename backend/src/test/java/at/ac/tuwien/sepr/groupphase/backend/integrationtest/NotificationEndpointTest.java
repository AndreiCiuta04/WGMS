package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestDbCleaner;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.NotificationDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.NotificationSettingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Notification;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationSetting;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationType;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdInvitationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationSettingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.VerificationCodeRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class NotificationEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ExpenseSplitRepository expenseSplitRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CleaningScheduleRepository cleaningScheduleRepository;

    @Autowired
    private ShoppingItemRepository shoppingItemRepository;

    @Autowired
    private HouseholdInvitationRepository invitationRepository;

    @Autowired
    private RoommateRepository roommateRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private JsonMapper jsonMapper;

    private ApplicationUser user;
    private String token;

    @BeforeEach
    public void beforeEach() {
        TestDbCleaner.clearAllApplicationData(
            paymentRepository,
            expenseSplitRepository,
            expenseRepository,
            taskRepository,
            cleaningScheduleRepository,
            shoppingItemRepository,
            invitationRepository,
            roommateRepository,
            householdRepository,
            notificationRepository,
            notificationSettingRepository,
            verificationCodeRepository,
            userRepository
        );

        user = new ApplicationUser("notifuser@example.com", "Password123!", "Notif", "User", false);
        user.setVerified(true);
        user = userRepository.save(user);

        NotificationSetting setting = new NotificationSetting(user);
        notificationSettingRepository.save(setting);

        List<String> roles = AuthorityUtils.createAuthorityList("ROLE_USER").stream().map(r -> r.getAuthority()).toList();
        token = "Bearer " + jwtTokenizer.getAuthToken(user.getEmail(), roles);
    }

    @Test
    public void givenUserWithNotifications_whenGetNotifications_thenReturnsNotificationsList() throws Exception {
        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setMessage("Test task assigned");
        notification.setType(NotificationType.TASK_ASSIGNED);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        MvcResult result = this.mockMvc.perform(get("/api/v1/notifications")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        List<NotificationDto> dtos = jsonMapper.readValue(response.getContentAsString(),
            jsonMapper.getTypeFactory().constructCollectionType(List.class, NotificationDto.class));

        assertEquals(1, dtos.size());
        assertEquals("Test task assigned", dtos.get(0).getMessage());
        assertFalse(dtos.get(0).isRead());
    }

    @Test
    public void givenUnreadNotification_whenMarkAsRead_thenSavesReadStatus() throws Exception {
        Notification notification = new Notification();
        notification.setRecipient(user);
        notification.setMessage("Test message");
        notification.setType(NotificationType.TASK_ASSIGNED);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        MvcResult result = this.mockMvc.perform(patch("/api/v1/notifications/" + notification.getId() + "/read")
                .header("Authorization", token))
            .andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertTrue(updated.isRead());
    }

    @Test
    public void givenMultipleUnreadNotifications_whenMarkAllRead_thenAllAreMarkedRead() throws Exception {
        Notification n1 = new Notification();
        n1.setRecipient(user);
        n1.setMessage("N1");
        n1.setType(NotificationType.TASK_ASSIGNED);
        n1.setRead(false);
        n1.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n1);

        Notification n2 = new Notification();
        n2.setRecipient(user);
        n2.setMessage("N2");
        n2.setType(NotificationType.TASK_ASSIGNED);
        n2.setRead(false);
        n2.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n2);

        MvcResult result = this.mockMvc.perform(post("/api/v1/notifications/read-all")
                .header("Authorization", token))
            .andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        List<Notification> all = notificationRepository.findAll();
        assertTrue(all.stream().allMatch(Notification::isRead));
    }

    @Test
    public void givenDefaultSettings_whenGetSettings_thenReturnsDefaults() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/v1/notifications/settings")
                .header("Authorization", token))
            .andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        NotificationSettingDto dto = jsonMapper.readValue(response.getContentAsString(), NotificationSettingDto.class);

        assertTrue(dto.isNotifyTaskAssigned());
        assertTrue(dto.isChannelInApp());
    }

    @Test
    public void givenNewSettings_whenUpdateSettings_thenSavesPreferences() throws Exception {
        NotificationSettingDto dto = new NotificationSettingDto();
        dto.setNotifyTaskAssigned(false);
        dto.setNotifyTaskOverdue(true);
        dto.setNotifyExpenseCreated(false);
        dto.setNotifyPaymentReceived(true);
        dto.setNotifyCleaningReminder(false);
        dto.setChannelInApp(true);
        dto.setChannelEmail(false);
        dto.setChannelPush(false);

        String body = jsonMapper.writeValueAsString(dto);

        MvcResult result = this.mockMvc.perform(put("/api/v1/notifications/settings")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andReturn();
        MockHttpServletResponse response = result.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        NotificationSetting updated = notificationSettingRepository.findByUserId(user.getId()).orElseThrow();
        assertFalse(updated.isNotifyTaskAssigned());
        assertTrue(updated.isNotifyTaskOverdue());
        assertFalse(updated.isChannelEmail());
    }

    @Test
    public void givenNoAuthentication_whenGetNotifications_thenRequestIsRejected() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void givenEmptyNotifications_whenGetNotifications_thenReturnsEmptyList() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/api/v1/notifications")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        List<NotificationDto> dtos = jsonMapper.readValue(result.getResponse().getContentAsString(),
            jsonMapper.getTypeFactory().constructCollectionType(List.class, NotificationDto.class));
        assertTrue(dtos.isEmpty());
    }

    @Test
    public void givenOtherUsersNotification_whenMarkAsRead_thenForbidden() throws Exception {
        ApplicationUser otherUser = new ApplicationUser("other@example.com", "Password123!", "Other", "User", false);
        otherUser.setVerified(true);
        otherUser = userRepository.save(otherUser);

        NotificationSetting otherSetting = new NotificationSetting(otherUser);
        notificationSettingRepository.save(otherSetting);

        Notification notification = new Notification();
        notification.setRecipient(otherUser);
        notification.setMessage("Private reminder");
        notification.setType(NotificationType.TASK_ASSIGNED);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        MvcResult result = this.mockMvc.perform(patch("/api/v1/notifications/" + notification.getId() + "/read")
                .header("Authorization", token))
            .andReturn();

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());
    }
}
