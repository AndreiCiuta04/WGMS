package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentOverviewDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PendingSettlementDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Payment;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.SettlementDecision;
import at.ac.tuwien.sepr.groupphase.backend.entity.SettlementDecisionStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SettlementDecisionRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PaymentService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.NotificationService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.BalanceCalculator;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    private static final String MAIN_EMAIL = "main@test.com";
    private static final String ROOMMATE_EMAIL = "roommate@test.com";

    @Mock
    private ExpenseSplitRepository expenseSplitRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private SettlementDecisionRepository settlementDecisionRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private RoommateRepository roommateRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    private PaymentService paymentService;

    private Household household;
    private Household otherHousehold;
    private ApplicationUser mainUser;
    private ApplicationUser roommateUser;
    private ApplicationUser outsiderUser;
    private Roommate mainTenant;
    private Roommate roommate;
    private Roommate outsider;

    @BeforeEach
    public void setUp() {
        paymentService = new PaymentServiceImpl(
            expenseSplitRepository,
            paymentRepository,
            settlementDecisionRepository,
            householdRepository,
            roommateRepository,
            userService,
            new BalanceCalculator(),
            notificationService
        );

        household = createHousehold(1L, "Test WG");
        otherHousehold = createHousehold(2L, "Other WG");
        mainUser = createUser(1L, MAIN_EMAIL, "Main", "Tenant");
        roommateUser = createUser(2L, ROOMMATE_EMAIL, "Normal", "Roommate");
        outsiderUser = createUser(3L, "outsider@test.com", "Other", "User");
        mainTenant = createRoommate(1L, mainUser, household, HouseholdRole.MAIN_TENANT);
        roommate = createRoommate(2L, roommateUser, household, HouseholdRole.ROOMMATE);
        outsider = createRoommate(3L, outsiderUser, otherHousehold, HouseholdRole.ROOMMATE);
    }

    @Test
    public void givenEmptyHousehold_whenGetOverview_thenReturnsEmptyOverview() {
        mockCurrentRoommate(MAIN_EMAIL, mainUser, mainTenant);
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId())).thenReturn(List.of());
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of());

        PaymentOverviewDto result = paymentService.getOverview(MAIN_EMAIL);

        assertAll(
            () -> assertEquals(0, result.openBalances().size()),
            () -> assertEquals(0, result.payments().size()),
            () -> assertEquals(money("0.00"), result.totalIowe()),
            () -> assertEquals(money("0.00"), result.totalOwedToMe())
        );
    }

    @Test
    public void givenOneExpenseWithoutPayments_whenGetOverview_thenReturnsExpectedOpenBalance() {
        mockCurrentRoommate(MAIN_EMAIL, mainUser, mainTenant);
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId()))
            .thenReturn(List.of(createSplit(createExpense(mainTenant), roommate, "10.00")));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of());

        PaymentOverviewDto result = paymentService.getOverview(MAIN_EMAIL);

        assertAll(
            () -> assertEquals(1, result.openBalances().size()),
            () -> assertEquals(roommate.getId(), result.openBalances().get(0).fromRoommateId()),
            () -> assertEquals(mainTenant.getId(), result.openBalances().get(0).toRoommateId()),
            () -> assertEquals(money("10.00"), result.openBalances().get(0).amount()),
            () -> assertEquals(money("0.00"), result.totalIowe()),
            () -> assertEquals(money("10.00"), result.totalOwedToMe())
        );
    }

    @Test
    public void givenPartialPayment_whenGetOverview_thenReducesOpenBalance() {
        mockCurrentRoommate(MAIN_EMAIL, mainUser, mainTenant);
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId()))
            .thenReturn(List.of(createSplit(createExpense(mainTenant), roommate, "10.00")));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of(createPayment(1L, roommate, mainTenant, "4.00")));

        PaymentOverviewDto result = paymentService.getOverview(MAIN_EMAIL);

        assertAll(
            () -> assertEquals(1, result.openBalances().size()),
            () -> assertEquals(money("6.00"), result.openBalances().get(0).amount()),
            () -> assertEquals(1, result.payments().size()),
            () -> assertEquals(money("4.00"), result.payments().get(0).amount()),
            () -> assertEquals(money("6.00"), result.totalOwedToMe())
        );
    }

    @Test
    public void givenFullPayment_whenGetOverview_thenOpenBalanceIsRemoved() {
        mockCurrentRoommate(MAIN_EMAIL, mainUser, mainTenant);
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId()))
            .thenReturn(List.of(createSplit(createExpense(mainTenant), roommate, "10.00")));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of(createPayment(1L, roommate, mainTenant, "10.00")));

        PaymentOverviewDto result = paymentService.getOverview(MAIN_EMAIL);

        assertAll(
            () -> assertEquals(0, result.openBalances().size()),
            () -> assertEquals(1, result.payments().size()),
            () -> assertEquals(money("0.00"), result.totalIowe()),
            () -> assertEquals(money("0.00"), result.totalOwedToMe())
        );
    }

    @Test
    public void givenRoommate_whenGetOverview_thenOnlyOwnBalancesAndPaymentsAreReturned() {
        ApplicationUser thirdUser = createUser(4L, "third@test.com", "Third", "Roommate");
        Roommate thirdRoommate = createRoommate(4L, thirdUser, household, HouseholdRole.ROOMMATE);
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId()))
            .thenReturn(List.of(
                createSplit(createExpense(mainTenant), roommate, "10.00"),
                createSplit(createExpense(thirdRoommate), mainTenant, "8.00")
            ));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of());
        when(paymentRepository.findAllInvolvingRoommate(household.getId(), roommate.getId())).thenReturn(List.of());

        PaymentOverviewDto result = paymentService.getOverview(ROOMMATE_EMAIL);

        assertAll(
            () -> assertEquals(2, result.openBalances().size()),
            () -> assertEquals(roommate.getId(), result.openBalances().get(0).fromRoommateId()),
            () -> assertEquals(mainTenant.getId(), result.openBalances().get(0).toRoommateId()),
            () -> assertEquals(money("2.00"), result.openBalances().get(0).amount()),
            () -> assertEquals(roommate.getId(), result.openBalances().get(1).fromRoommateId()),
            () -> assertEquals(thirdRoommate.getId(), result.openBalances().get(1).toRoommateId()),
            () -> assertEquals(money("8.00"), result.openBalances().get(1).amount()),
            () -> assertEquals(0, result.payments().size()),
            () -> assertEquals(money("10.00"), result.totalIowe()),
            () -> assertEquals(money("0.00"), result.totalOwedToMe())
        );
    }

    @Test
    public void givenValidPayment_whenCreatePayment_thenPaymentIsSaved() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(householdRepository.findByIdForUpdate(household.getId())).thenReturn(Optional.of(household));
        when(roommateRepository.findByIdAndHouseholdId(mainTenant.getId(), household.getId()))
            .thenReturn(Optional.of(mainTenant));
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId()))
            .thenReturn(List.of(createSplit(createExpense(mainTenant), roommate, "10.00")));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(99L);
            payment.setCreatedAt(LocalDateTime.of(2026, 5, 25, 12, 0));
            return payment;
        });

        PaymentDto result = paymentService.createPayment(
            new PaymentCreateDto(mainTenant.getId(), money("4.50"), LocalDate.of(2026, 5, 25)),
            ROOMMATE_EMAIL
        );

        assertAll(
            () -> assertEquals(99L, result.id()),
            () -> assertEquals(roommate.getId(), result.fromRoommateId()),
            () -> assertEquals(mainTenant.getId(), result.toRoommateId()),
            () -> assertEquals(money("4.50"), result.amount())
        );
    }

    @Test
    public void givenOpenBalance_whenGetPendingSettlements_thenReturnsActionableSettlement() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId()))
            .thenReturn(List.of(createSplit(createExpense(mainTenant), roommate, "10.00")));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of());
        when(settlementDecisionRepository.findTopByHouseholdIdAndFromRoommateIdAndToRoommateIdAndAmountOrderByDecidedAtDescIdDesc(
            household.getId(), roommate.getId(), mainTenant.getId(), money("10.00")))
            .thenReturn(Optional.empty());

        List<PendingSettlementDto> result = paymentService.getPendingSettlements(ROOMMATE_EMAIL);

        assertAll(
            () -> assertEquals(1, result.size()),
            () -> assertEquals(roommate.getId() + "-" + mainTenant.getId() + "-1000", result.get(0).id()),
            () -> assertEquals("Shared expense", result.get(0).expenseDescription()),
            () -> assertEquals(SettlementDecisionStatus.PENDING, result.get(0).status()),
            () -> assertEquals(true, result.get(0).canDecide()),
            () -> assertEquals(money("10.00"), result.get(0).amount())
        );
    }

    @Test
    public void givenOpenBalanceOwedToCurrentUser_whenGetPendingSettlements_thenReturnsNoApproval() {
        mockCurrentRoommate(MAIN_EMAIL, mainUser, mainTenant);
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId()))
            .thenReturn(List.of(createSplit(createExpense(mainTenant), roommate, "10.00")));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of());

        List<PendingSettlementDto> result = paymentService.getPendingSettlements(MAIN_EMAIL);

        assertEquals(0, result.size());
    }

    @Test
    public void givenOpenSettlement_whenApproveSettlement_thenRecordsPaymentAndDecision() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(householdRepository.findByIdForUpdate(household.getId())).thenReturn(Optional.of(household));
        when(roommateRepository.findByIdAndHouseholdId(mainTenant.getId(), household.getId()))
            .thenReturn(Optional.of(mainTenant));
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId()))
            .thenReturn(List.of(createSplit(createExpense(mainTenant), roommate, "10.00")));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of());
        when(settlementDecisionRepository.findTopByHouseholdIdAndFromRoommateIdAndToRoommateIdAndAmountOrderByDecidedAtDescIdDesc(
            household.getId(), roommate.getId(), mainTenant.getId(), money("10.00")))
            .thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(100L);
            payment.setCreatedAt(LocalDateTime.of(2026, 5, 25, 12, 0));
            return payment;
        });
        when(settlementDecisionRepository.save(any(SettlementDecision.class))).thenAnswer(invocation -> {
            SettlementDecision decision = invocation.getArgument(0);
            decision.setId(200L);
            decision.setCreatedAt(LocalDateTime.of(2026, 5, 25, 12, 1));
            return decision;
        });

        PendingSettlementDto result = paymentService.approveSettlement(roommate.getId() + "-" + mainTenant.getId() + "-1000", ROOMMATE_EMAIL);

        assertAll(
            () -> assertEquals(SettlementDecisionStatus.APPROVED, result.status()),
            () -> assertEquals(money("10.00"), result.amount())
        );
        verify(paymentRepository).save(any(Payment.class));
        verify(settlementDecisionRepository).save(any(SettlementDecision.class));
    }

    @Test
    public void givenOpenSettlement_whenRejectSettlement_thenRecordsDecisionWithoutPayment() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(householdRepository.findByIdForUpdate(household.getId())).thenReturn(Optional.of(household));
        when(roommateRepository.findByIdAndHouseholdId(mainTenant.getId(), household.getId()))
            .thenReturn(Optional.of(mainTenant));
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId()))
            .thenReturn(List.of(createSplit(createExpense(mainTenant), roommate, "10.00")));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of());
        when(settlementDecisionRepository.findTopByHouseholdIdAndFromRoommateIdAndToRoommateIdAndAmountOrderByDecidedAtDescIdDesc(
            household.getId(), roommate.getId(), mainTenant.getId(), money("10.00")))
            .thenReturn(Optional.empty());
        when(settlementDecisionRepository.save(any(SettlementDecision.class))).thenAnswer(invocation -> {
            SettlementDecision decision = invocation.getArgument(0);
            decision.setId(201L);
            decision.setCreatedAt(LocalDateTime.of(2026, 5, 25, 12, 2));
            return decision;
        });

        PendingSettlementDto result = paymentService.rejectSettlement(roommate.getId() + "-" + mainTenant.getId() + "-1000", ROOMMATE_EMAIL);

        assertEquals(SettlementDecisionStatus.REJECTED, result.status());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(settlementDecisionRepository).save(any(SettlementDecision.class));
    }

    @Test
    public void givenAlreadyDecidedSettlement_whenApproveSettlement_thenConflictAndNoPayment() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(householdRepository.findByIdForUpdate(household.getId())).thenReturn(Optional.of(household));
        when(roommateRepository.findByIdAndHouseholdId(mainTenant.getId(), household.getId()))
            .thenReturn(Optional.of(mainTenant));
        when(settlementDecisionRepository.findTopByHouseholdIdAndFromRoommateIdAndToRoommateIdAndAmountOrderByDecidedAtDescIdDesc(
            household.getId(), roommate.getId(), mainTenant.getId(), money("10.00")))
            .thenReturn(Optional.of(createDecision(roommate, mainTenant, "10.00", SettlementDecisionStatus.REJECTED)));

        assertThrows(ConflictException.class, () ->
            paymentService.approveSettlement(roommate.getId() + "-" + mainTenant.getId() + "-1000", ROOMMATE_EMAIL));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    public void givenUserWithoutActiveHousehold_whenGetOverview_thenForbidden() {
        when(userService.findApplicationUserByEmail(MAIN_EMAIL)).thenReturn(mainUser);
        when(roommateRepository.findByUserIdAndActiveTrue(mainUser.getId())).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> paymentService.getOverview(MAIN_EMAIL));
    }

    @Test
    public void givenPaymentToRoommateInOtherHousehold_whenCreatePayment_thenNotFoundAndNoSave() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(householdRepository.findByIdForUpdate(household.getId())).thenReturn(Optional.of(household));
        when(roommateRepository.findByIdAndHouseholdId(outsider.getId(), household.getId()))
            .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
            paymentService.createPayment(
                new PaymentCreateDto(outsider.getId(), money("1.00"), LocalDate.of(2026, 5, 25)),
                ROOMMATE_EMAIL
            )
        );
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    public void givenInvalidAmount_whenCreatePayment_thenConflictAndNoSave() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);

        assertThrows(ConflictException.class, () ->
            paymentService.createPayment(
                new PaymentCreateDto(mainTenant.getId(), money("0.00"), LocalDate.of(2026, 5, 25)),
                ROOMMATE_EMAIL
            )
        );
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    public void givenFutureDate_whenCreatePayment_thenConflictAndNoSave() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);

        assertThrows(ConflictException.class, () ->
            paymentService.createPayment(
                new PaymentCreateDto(mainTenant.getId(), money("1.00"), LocalDate.now().plusDays(1)),
                ROOMMATE_EMAIL
            )
        );
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    public void givenNullPaymentData_whenCreatePayment_thenConflictAndNoSave() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);

        assertThrows(ConflictException.class, () -> paymentService.createPayment(null, ROOMMATE_EMAIL));

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    public void givenMissingPaymentFields_whenCreatePayment_thenConflictAndNoSave() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);

        assertAll(
            () -> assertThrows(ConflictException.class, () ->
                paymentService.createPayment(new PaymentCreateDto(null, money("1.00"), LocalDate.of(2026, 5, 25)), ROOMMATE_EMAIL)),
            () -> assertThrows(ConflictException.class, () ->
                paymentService.createPayment(new PaymentCreateDto(mainTenant.getId(), null, LocalDate.of(2026, 5, 25)), ROOMMATE_EMAIL)),
            () -> assertThrows(ConflictException.class, () ->
                paymentService.createPayment(new PaymentCreateDto(mainTenant.getId(), money("1.00"), null), ROOMMATE_EMAIL))
        );
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    public void givenNoOpenBalance_whenCreatePayment_thenConflictAndNoSave() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(householdRepository.findByIdForUpdate(household.getId())).thenReturn(Optional.of(household));
        when(roommateRepository.findByIdAndHouseholdId(mainTenant.getId(), household.getId()))
            .thenReturn(Optional.of(mainTenant));
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId())).thenReturn(List.of());
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of());

        assertThrows(ConflictException.class, () ->
            paymentService.createPayment(
                new PaymentCreateDto(mainTenant.getId(), money("1.00"), LocalDate.of(2026, 5, 25)),
                ROOMMATE_EMAIL
            )
        );

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    public void givenPaymentAboveOpenBalance_whenCreatePayment_thenConflictAndNoSave() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(householdRepository.findByIdForUpdate(household.getId())).thenReturn(Optional.of(household));
        when(roommateRepository.findByIdAndHouseholdId(mainTenant.getId(), household.getId()))
            .thenReturn(Optional.of(mainTenant));
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId()))
            .thenReturn(List.of(createSplit(createExpense(mainTenant), roommate, "10.00")));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of());

        assertThrows(ConflictException.class, () ->
            paymentService.createPayment(
                new PaymentCreateDto(mainTenant.getId(), money("10.01"), LocalDate.of(2026, 5, 25)),
                ROOMMATE_EMAIL
            )
        );

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    public void givenPaymentToSelf_whenCreatePayment_thenConflictAndNoSave() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(householdRepository.findByIdForUpdate(household.getId())).thenReturn(Optional.of(household));
        when(roommateRepository.findByIdAndHouseholdId(roommate.getId(), household.getId()))
            .thenReturn(Optional.of(roommate));

        assertThrows(ConflictException.class, () ->
            paymentService.createPayment(
                new PaymentCreateDto(roommate.getId(), money("1.00"), LocalDate.of(2026, 5, 25)),
                ROOMMATE_EMAIL
            )
        );

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    public void givenInvalidSettlementId_whenApproveSettlement_thenNotFoundAndNoDecision() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(householdRepository.findByIdForUpdate(household.getId())).thenReturn(Optional.of(household));

        assertThrows(NotFoundException.class, () -> paymentService.approveSettlement("not-a-settlement", ROOMMATE_EMAIL));

        verify(settlementDecisionRepository, never()).save(any(SettlementDecision.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    public void givenOverflowingSettlementId_whenApproveSettlement_thenNotFoundAndNoDecision() {
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(householdRepository.findByIdForUpdate(household.getId())).thenReturn(Optional.of(household));

        String overflowingId = "999999999999999999999999999999-1-100";

        assertThrows(NotFoundException.class, () -> paymentService.approveSettlement(overflowingId, ROOMMATE_EMAIL));

        verify(settlementDecisionRepository, never()).save(any(SettlementDecision.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    public void givenSettlementNotOwedByCurrentUser_whenApproveSettlement_thenForbiddenAndNoDecision() {
        mockCurrentRoommate(MAIN_EMAIL, mainUser, mainTenant);
        when(householdRepository.findByIdForUpdate(household.getId())).thenReturn(Optional.of(household));

        assertThrows(ForbiddenException.class, () ->
            paymentService.approveSettlement(roommate.getId() + "-" + mainTenant.getId() + "-1000", MAIN_EMAIL));

        verify(settlementDecisionRepository, never()).save(any(SettlementDecision.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    public void givenMultipleExpenseDescriptions_whenGetPendingSettlements_thenUsesMostRecentDescriptionWithCount() {
        Expense olderExpense = createExpense(mainTenant);
        olderExpense.setId(10L);
        olderExpense.setDescription("Groceries");
        olderExpense.setDate(LocalDate.of(2026, 5, 20));
        Expense newerExpense = createExpense(mainTenant);
        newerExpense.setId(11L);
        newerExpense.setDescription("Internet");
        newerExpense.setDate(LocalDate.of(2026, 5, 25));
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId()))
            .thenReturn(List.of(
                createSplit(olderExpense, roommate, "5.00"),
                createSplit(newerExpense, roommate, "5.00")
            ));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of());
        when(settlementDecisionRepository.findTopByHouseholdIdAndFromRoommateIdAndToRoommateIdAndAmountOrderByDecidedAtDescIdDesc(
            household.getId(), roommate.getId(), mainTenant.getId(), money("10.00")))
            .thenReturn(Optional.empty());

        List<PendingSettlementDto> result = paymentService.getPendingSettlements(ROOMMATE_EMAIL);

        assertEquals("Internet +1 more", result.get(0).expenseDescription());
    }

    @Test
    public void givenBlankExpenseDescription_whenGetPendingSettlements_thenUsesFallbackDescription() {
        Expense expense = createExpense(mainTenant);
        expense.setDescription(" ");
        mockCurrentRoommate(ROOMMATE_EMAIL, roommateUser, roommate);
        when(expenseSplitRepository.findAllByExpenseHouseholdId(household.getId()))
            .thenReturn(List.of(createSplit(expense, roommate, "10.00")));
        when(paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId()))
            .thenReturn(List.of());
        when(settlementDecisionRepository.findTopByHouseholdIdAndFromRoommateIdAndToRoommateIdAndAmountOrderByDecidedAtDescIdDesc(
            household.getId(), roommate.getId(), mainTenant.getId(), money("10.00")))
            .thenReturn(Optional.empty());

        List<PendingSettlementDto> result = paymentService.getPendingSettlements(ROOMMATE_EMAIL);

        assertEquals("Open settlement", result.get(0).expenseDescription());
    }

    private void mockCurrentRoommate(String email, ApplicationUser user, Roommate currentRoommate) {
        when(userService.findApplicationUserByEmail(email)).thenReturn(user);
        when(roommateRepository.findByUserIdAndActiveTrue(user.getId())).thenReturn(Optional.of(currentRoommate));
    }

    private Household createHousehold(Long id, String name) {
        Household household = new Household();
        household.setId(id);
        household.setName(name);
        return household;
    }

    private ApplicationUser createUser(Long id, String email, String firstName, String lastName) {
        ApplicationUser user = new ApplicationUser();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }

    private Roommate createRoommate(Long id, ApplicationUser user, Household household, HouseholdRole role) {
        Roommate roommate = new Roommate();
        roommate.setId(id);
        roommate.setUser(user);
        roommate.setHousehold(household);
        roommate.setRole(role);
        roommate.setActive(true);
        return roommate;
    }

    private Expense createExpense(Roommate paidBy) {
        Expense expense = new Expense();
        expense.setHousehold(paidBy.getHousehold());
        expense.setPaidBy(paidBy);
        expense.setCreatedBy(paidBy);
        expense.setDescription("Shared expense");
        expense.setTotalAmount(money("10.00"));
        expense.setDate(LocalDate.of(2026, 5, 25));
        return expense;
    }

    private ExpenseSplit createSplit(Expense expense, Roommate roommate, String amount) {
        ExpenseSplit split = new ExpenseSplit();
        split.setExpense(expense);
        split.setRoommate(roommate);
        split.setAmountOwed(money(amount));
        return split;
    }

    private Payment createPayment(Long id, Roommate fromRoommate, Roommate toRoommate, String amount) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setHousehold(fromRoommate.getHousehold());
        payment.setFromRoommate(fromRoommate);
        payment.setToRoommate(toRoommate);
        payment.setCreatedBy(fromRoommate);
        payment.setAmount(money(amount));
        payment.setDate(LocalDate.of(2026, 5, 25));
        payment.setCreatedAt(LocalDateTime.of(2026, 5, 25, 12, 0));
        return payment;
    }

    private SettlementDecision createDecision(
        Roommate fromRoommate,
        Roommate toRoommate,
        String amount,
        SettlementDecisionStatus status
    ) {
        SettlementDecision decision = new SettlementDecision();
        decision.setId(1L);
        decision.setHousehold(fromRoommate.getHousehold());
        decision.setFromRoommate(fromRoommate);
        decision.setToRoommate(toRoommate);
        decision.setAmount(money(amount));
        decision.setStatus(status);
        decision.setDecidedBy(fromRoommate);
        decision.setDecidedAt(LocalDateTime.of(2026, 5, 25, 12, 0));
        decision.setCreatedAt(LocalDateTime.of(2026, 5, 25, 12, 0));
        return decision;
    }

    private BigDecimal money(String amount) {
        return new BigDecimal(amount).setScale(2);
    }
}
