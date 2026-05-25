package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BalanceDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CleaningScheduleDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DashboardDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ShoppingItemDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.CleaningScheduleMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.TaskMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.CleaningScheduleService;
import at.ac.tuwien.sepr.groupphase.backend.service.DashboardService;
import at.ac.tuwien.sepr.groupphase.backend.service.ExpenseService;
import at.ac.tuwien.sepr.groupphase.backend.service.ShoppingItemService;
import at.ac.tuwien.sepr.groupphase.backend.service.TaskService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private CleaningScheduleService cleaningScheduleService;

    @Mock
    private ShoppingItemService shoppingItemService;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private UserService userService;

    @Mock
    private RoommateRepository roommateRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private CleaningScheduleMapper cleaningScheduleMapper;

    private DashboardService dashboardService;

    private ApplicationUser user;
    private Roommate roommate;
    private Household household;

    @BeforeEach
    public void setUp() {
        dashboardService = new DashboardServiceImpl(
            taskService,
            cleaningScheduleService,
            shoppingItemService,
            expenseService,
            userService,
            roommateRepository,
            taskMapper,
            cleaningScheduleMapper
        );

        user = new ApplicationUser();
        user.setId(1L);
        user.setEmail("user@test.com");

        household = new Household();
        household.setId(10L);
        household.setName("Test Flat");

        roommate = new Roommate();
        roommate.setId(20L);
        roommate.setUser(user);
        roommate.setHousehold(household);
        roommate.setActive(true);
    }

    @Test
    public void givenValidUser_whenGetDashboardData_thenSuccess() {
        // Arrange
        when(userService.findApplicationUserByEmail("user@test.com")).thenReturn(user);
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.of(roommate));

        when(taskService.findVisibleTasks("user@test.com", false)).thenReturn(new ArrayList<>());
        when(taskService.findAssignedToCurrentUser("user@test.com")).thenReturn(new ArrayList<>());
        when(cleaningScheduleService.findActiveSchedules("user@test.com", false)).thenReturn(new ArrayList<>());
        when(roommateRepository.findAllByHouseholdIdAndActiveTrue(10L)).thenReturn(Collections.singletonList(roommate));
        when(shoppingItemService.findVisibleItems(user)).thenReturn(new ArrayList<>());
        when(expenseService.calculateBalances("user@test.com")).thenReturn(new ArrayList<>());

        // Act
        DashboardDto result = dashboardService.getDashboardData("user@test.com");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getOpenTaskCount());
        assertEquals(0, result.getMyTaskCount());
        assertEquals(0, result.getActiveScheduleCount());
        assertEquals(1, result.getHousemateCount());
        assertEquals(BigDecimal.ZERO, result.getNetBalance());
    }

    @Test
    public void givenUserWithoutHousehold_whenGetDashboardData_thenForbiddenException() {
        // Arrange
        when(userService.findApplicationUserByEmail("user@test.com")).thenReturn(user);
        when(roommateRepository.findByUserIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> {
            dashboardService.getDashboardData("user@test.com");
        });
    }
}
