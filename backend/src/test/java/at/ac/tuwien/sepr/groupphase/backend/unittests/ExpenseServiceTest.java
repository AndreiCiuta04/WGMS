package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BillExpensePreviewRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseSplitCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ExpenseService;
import at.ac.tuwien.sepr.groupphase.backend.service.NotificationService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.BalanceCalculator;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.ExpenseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    private static final String MAIN_EMAIL = "main@test.com";

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseSplitRepository expenseSplitRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RoommateRepository roommateRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    private ExpenseService expenseService;
    private Household household;
    private ApplicationUser mainUser;
    private ApplicationUser roommateUser;
    private Roommate mainTenant;
    private Roommate roommate;

    @BeforeEach
    public void setUp() {
        expenseService = new ExpenseServiceImpl(
            expenseRepository,
            expenseSplitRepository,
            householdRepository,
            paymentRepository,
            roommateRepository,
            userService,
            new BalanceCalculator(),
            notificationService
        );
        household = createHousehold(1L);
        mainUser = createUser(1L, MAIN_EMAIL, "Main", "Tenant");
        roommateUser = createUser(2L, "roommate@test.com", "Normal", "Roommate");
        mainTenant = createRoommate(1L, mainUser, HouseholdRole.MAIN_TENANT);
        roommate = createRoommate(2L, roommateUser, HouseholdRole.ROOMMATE);
    }

    @Test
    public void givenInvalidEqualSplitInput_whenCalculateEqualSplits_thenConflictAndNoPersistence() {
        mockCurrentRoommate();

        assertAll(
            () -> assertThrows(ConflictException.class, () ->
                expenseService.calculateEqualSplits(null, List.of(mainTenant.getId()), MAIN_EMAIL)),
            () -> assertThrows(ConflictException.class, () ->
                expenseService.calculateEqualSplits(money("0.00"), List.of(mainTenant.getId()), MAIN_EMAIL)),
            () -> assertThrows(ConflictException.class, () ->
                expenseService.calculateEqualSplits(money("1.00"), null, MAIN_EMAIL)),
            () -> assertThrows(ConflictException.class, () ->
                expenseService.calculateEqualSplits(money("1.00"), List.of(), MAIN_EMAIL)),
            () -> assertThrows(ConflictException.class, () ->
                expenseService.calculateEqualSplits(money("1.00"), listWithNullParticipant(), MAIN_EMAIL)),
            () -> assertThrows(ConflictException.class, () ->
                expenseService.calculateEqualSplits(money("1.00"), List.of(mainTenant.getId(), mainTenant.getId()), MAIN_EMAIL))
        );
        verify(expenseRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void givenAmountTooSmallForParticipants_whenCalculateEqualSplits_thenConflict() {
        mockCurrentRoommate();
        mockActiveRoommate(mainTenant);
        mockActiveRoommate(roommate);

        assertThrows(ConflictException.class, () ->
            expenseService.calculateEqualSplits(money("0.01"), List.of(mainTenant.getId(), roommate.getId()), MAIN_EMAIL));
    }

    @Test
    public void givenBillPreviewWithoutCurrentRoommate_whenPreviewBillExpense_thenConflict() {
        mockCurrentRoommate();
        mockActiveRoommate(roommate);
        BillExpensePreviewRequestDto preview = new BillExpensePreviewRequestDto(
            "Receipt",
            money("10.00"),
            LocalDate.of(2026, 5, 25),
            List.of(roommate.getId())
        );

        assertThrows(ConflictException.class, () -> expenseService.previewBillExpense(preview, MAIN_EMAIL));
    }

    @Test
    public void givenPayerMissingFromSplits_whenCreateExpense_thenConflictAndNoPersistence() {
        mockCurrentRoommate();
        ExpenseCreateDto dto = new ExpenseCreateDto(
            "Groceries",
            money("10.00"),
            LocalDate.of(2026, 5, 25),
            mainTenant.getId(),
            List.of(new ExpenseSplitCreateDto(roommate.getId(), money("10.00")))
        );

        assertThrows(ConflictException.class, () -> expenseService.createExpense(dto, MAIN_EMAIL));

        verify(expenseRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private void mockCurrentRoommate() {
        when(userService.findApplicationUserByEmail(MAIN_EMAIL)).thenReturn(mainUser);
        when(roommateRepository.findByUserIdAndActiveTrue(mainUser.getId())).thenReturn(Optional.of(mainTenant));
    }

    private void mockActiveRoommate(Roommate activeRoommate) {
        when(roommateRepository.findByIdAndHouseholdIdAndActiveTrue(activeRoommate.getId(), household.getId()))
            .thenReturn(Optional.of(activeRoommate));
    }

    private Household createHousehold(Long id) {
        Household result = new Household();
        result.setId(id);
        result.setName("Test WG");
        return result;
    }

    private ApplicationUser createUser(Long id, String email, String firstName, String lastName) {
        ApplicationUser result = new ApplicationUser();
        result.setId(id);
        result.setEmail(email);
        result.setFirstName(firstName);
        result.setLastName(lastName);
        return result;
    }

    private Roommate createRoommate(Long id, ApplicationUser user, HouseholdRole role) {
        Roommate result = new Roommate();
        result.setId(id);
        result.setUser(user);
        result.setHousehold(household);
        result.setRole(role);
        result.setActive(true);
        return result;
    }

    private BigDecimal money(String amount) {
        return new BigDecimal(amount).setScale(2);
    }

    private List<Long> listWithNullParticipant() {
        List<Long> participants = new ArrayList<>();
        participants.add(mainTenant.getId());
        participants.add(null);
        return participants;
    }
}
