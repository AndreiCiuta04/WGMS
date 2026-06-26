package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BalanceDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentOverviewDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PendingSettlementDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Payment;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.SettlementDecision;
import at.ac.tuwien.sepr.groupphase.backend.entity.SettlementDecisionStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SettlementDecisionRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PaymentService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.NotificationService;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int MONEY_SCALE = 2;

    private final ExpenseSplitRepository expenseSplitRepository;
    private final ExpenseRepository expenseRepository;
    private final PaymentRepository paymentRepository;
    private final SettlementDecisionRepository settlementDecisionRepository;
    private final HouseholdRepository householdRepository;
    private final RoommateRepository roommateRepository;
    private final UserService userService;
    private final BalanceCalculator balanceCalculator;
    private final NotificationService notificationService;

    public PaymentServiceImpl(
        ExpenseSplitRepository expenseSplitRepository,
        ExpenseRepository expenseRepository,
        PaymentRepository paymentRepository,
        SettlementDecisionRepository settlementDecisionRepository,
        HouseholdRepository householdRepository,
        RoommateRepository roommateRepository,
        UserService userService,
        BalanceCalculator balanceCalculator,
        NotificationService notificationService
    ) {
        this.expenseSplitRepository = expenseSplitRepository;
        this.expenseRepository = expenseRepository;
        this.paymentRepository = paymentRepository;
        this.settlementDecisionRepository = settlementDecisionRepository;
        this.householdRepository = householdRepository;
        this.roommateRepository = roommateRepository;
        this.userService = userService;
        this.balanceCalculator = balanceCalculator;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentOverviewDto getOverview(String currentUserEmail) {
        LOGGER.debug("Get payment overview for user {}", currentUserEmail);

        Roommate currentRoommate = getCurrentRoommateByEmail(currentUserEmail);
        Long householdId = currentRoommate.getHousehold().getId();
        List<BalanceDto> allOpenBalances = calculateOpenBalances(householdId);
        List<BalanceDto> visibleOpenBalances = visibleBalancesFor(currentRoommate, allOpenBalances);
        List<Payment> visiblePayments = visiblePaymentsFor(currentRoommate);

        return new PaymentOverviewDto(
            visibleOpenBalances,
            visiblePayments.stream().map(this::toDto).toList(),
            sumAmountsFrom(currentRoommate, visibleOpenBalances),
            sumAmountsTo(currentRoommate, visibleOpenBalances)
        );
    }

    @Override
    @Transactional
    public PaymentDto createPayment(PaymentCreateDto paymentCreateDto, String currentUserEmail) {
        LOGGER.debug("Create payment by user {}", currentUserEmail);

        Roommate currentRoommate = getCurrentRoommateByEmail(currentUserEmail);
        validatePaymentInput(paymentCreateDto);
        BigDecimal amount = toMoney(paymentCreateDto.amount());
        validatePositiveAmount(amount);

        Long householdId = currentRoommate.getHousehold().getId();
        lockHouseholdForSettlement(householdId);

        Roommate toRoommate = getActiveRoommateInHousehold(
            paymentCreateDto.toRoommateId(),
            householdId
        );
        validateDifferentRoommates(currentRoommate, toRoommate);
        validatePaymentReducesOpenBalance(currentRoommate, toRoommate, amount);

        Payment payment = new Payment();
        payment.setHousehold(currentRoommate.getHousehold());
        payment.setFromRoommate(currentRoommate);
        payment.setToRoommate(toRoommate);
        payment.setAmount(amount);
        payment.setDate(paymentCreateDto.date());
        payment.setCreatedBy(currentRoommate);

        Payment savedPayment = paymentRepository.save(payment);

        String fromName = currentRoommate.getUser().getFirstName() + " " + currentRoommate.getUser().getLastName();
        notificationService.triggerNotification(
            toRoommate.getUser(),
            String.format("%s paid you EUR %s.", fromName, savedPayment.getAmount()),
            NotificationType.PAYMENT_RECEIVED,
            savedPayment.getId()
        );

        return toDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingSettlementDto> getPendingSettlements(String currentUserEmail) {
        LOGGER.debug("Get pending settlement approvals for user {}", currentUserEmail);

        Roommate currentRoommate = getCurrentRoommateByEmail(currentUserEmail);
        Long householdId = currentRoommate.getHousehold().getId();
        return calculateOpenBalances(householdId).stream()
            .filter(balance -> currentRoommate.getId().equals(balance.fromRoommateId()))
            .filter(balance -> latestDecisionFor(householdId, balance).isEmpty())
            .map(balance -> toPendingSettlementDto(balance, currentRoommate))
            .toList();
    }

    @Override
    @Transactional
    public PendingSettlementDto approveSettlement(String settlementId, String currentUserEmail) {
        LOGGER.debug("Approve settlement {} by user {}", settlementId, currentUserEmail);

        Roommate currentRoommate = getCurrentRoommateByEmail(currentUserEmail);
        Long householdId = currentRoommate.getHousehold().getId();
        lockHouseholdForSettlement(householdId);
        BalanceDto balance = validateSettlementDecisionInput(settlementId, currentRoommate);

        Payment payment = new Payment();
        payment.setHousehold(currentRoommate.getHousehold());
        payment.setFromRoommate(currentRoommate);
        payment.setToRoommate(getActiveRoommateInHousehold(balance.toRoommateId(), householdId));
        payment.setAmount(toMoney(balance.amount()));
        payment.setDate(LocalDate.now());
        payment.setCreatedBy(currentRoommate);
        Payment savedPayment = paymentRepository.save(payment);

        String fromName = toDisplayName(currentRoommate);
        notificationService.triggerNotification(
            savedPayment.getToRoommate().getUser(),
            String.format("%s approved and recorded settlement EUR %s.", fromName, savedPayment.getAmount()),
            NotificationType.PAYMENT_RECEIVED,
            savedPayment.getId()
        );

        SettlementDecision decision = saveSettlementDecision(balance, currentRoommate, settlementId, SettlementDecisionStatus.APPROVED);
        return toDecidedSettlementDto(balance, currentRoommate, decision);
    }

    @Override
    @Transactional
    public PendingSettlementDto rejectSettlement(String settlementId, String currentUserEmail) {
        LOGGER.debug("Reject settlement {} by user {}", settlementId, currentUserEmail);

        Roommate currentRoommate = getCurrentRoommateByEmail(currentUserEmail);
        Long householdId = currentRoommate.getHousehold().getId();
        lockHouseholdForSettlement(householdId);
        BalanceDto balance = validateSettlementDecisionInput(settlementId, currentRoommate);
        SettlementDecision decision = saveSettlementDecision(balance, currentRoommate, settlementId, SettlementDecisionStatus.REJECTED);
        return toDecidedSettlementDto(balance, currentRoommate, decision);
    }

    private void lockHouseholdForSettlement(Long householdId) {
        householdRepository.findByIdForUpdate(householdId)
            .orElseThrow(() -> new NotFoundException("Household does not exist."));
    }

    private void validatePaymentInput(PaymentCreateDto paymentCreateDto) {
        if (paymentCreateDto == null) {
            throw new ConflictException("Payment data must not be null.");
        }
        if (paymentCreateDto.toRoommateId() == null) {
            throw new ConflictException("Recipient roommate id must not be null.");
        }
        if (paymentCreateDto.amount() == null) {
            throw new ConflictException("Payment amount must not be null.");
        }
        if (paymentCreateDto.date() == null) {
            throw new ConflictException("Payment date must not be null.");
        }
        if (paymentCreateDto.date().isAfter(LocalDate.now())) {
            throw new ConflictException("Payment date must not be in the future.");
        }
    }

    private Roommate getCurrentRoommateByEmail(String currentUserEmail) {
        ApplicationUser currentUser = userService.findApplicationUserByEmail(currentUserEmail);
        return roommateRepository.findByUserIdAndActiveTrue(currentUser.getId())
            .orElseThrow(() -> new ForbiddenException("User does not belong to an active household."));
    }

    private Roommate getActiveRoommateInHousehold(Long roommateId, Long householdId) {
        return roommateRepository.findByIdAndHouseholdIdAndActiveTrue(roommateId, householdId)
            .orElseThrow(() -> new NotFoundException("Roommate is not an active member of your household."));
    }

    private List<BalanceDto> calculateOpenBalances(Long householdId) {
        List<ExpenseSplit> splits = expenseSplitRepository.findAllByExpenseHouseholdId(householdId);
        List<Payment> payments = paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(householdId);
        return balanceCalculator.calculateOpenBalances(splits, payments);
    }

    private BalanceDto validateSettlementDecisionInput(String settlementId, Roommate currentRoommate) {
        SettlementKey settlementKey = parseSettlementId(settlementId);
        Long householdId = currentRoommate.getHousehold().getId();

        if (!currentRoommate.getId().equals(settlementKey.fromRoommateId())) {
            throw new ForbiddenException("Only the roommate who owes the settlement may approve or reject it.");
        }

        Roommate toRoommate = getActiveRoommateInHousehold(settlementKey.toRoommateId(), householdId);
        validateDifferentRoommates(currentRoommate, toRoommate);

        BigDecimal amount = settlementKey.amount();
        validatePositiveAmount(amount);

        Optional<SettlementDecision> existingDecision = settlementDecisionRepository
            .findTopByHouseholdIdAndSettlementIdOrderByDecidedAtDescIdDesc(householdId, settlementId);
        if (existingDecision.isPresent()) {
            throw new ConflictException("This settlement has already been decided.");
        }

        return calculateOpenBalances(householdId).stream()
            .filter(balance -> currentRoommate.getId().equals(balance.fromRoommateId())
                && toRoommate.getId().equals(balance.toRoommateId())
                && amount.compareTo(toMoney(balance.amount())) == 0
                && settlementId.equals(settlementId(balance.fromRoommateId(), balance.toRoommateId(), balance.amount(), householdId)))
            .findFirst()
            .orElseThrow(() -> new ConflictException("Settlement is no longer pending."));
    }

    private Optional<SettlementDecision> latestDecisionFor(Long householdId, BalanceDto balance) {
        return settlementDecisionRepository
            .findTopByHouseholdIdAndSettlementIdOrderByDecidedAtDescIdDesc(
                householdId,
                settlementId(balance.fromRoommateId(), balance.toRoommateId(), balance.amount(), householdId)
            );
    }

    private SettlementDecision saveSettlementDecision(
        BalanceDto balance,
        Roommate currentRoommate,
        String settlementId,
        SettlementDecisionStatus status
    ) {
        SettlementDecision decision = new SettlementDecision();
        decision.setHousehold(currentRoommate.getHousehold());
        decision.setFromRoommate(currentRoommate);
        decision.setToRoommate(getActiveRoommateInHousehold(balance.toRoommateId(), currentRoommate.getHousehold().getId()));
        decision.setSettlementId(settlementId);
        decision.setAmount(toMoney(balance.amount()));
        decision.setStatus(status);
        decision.setDecidedBy(currentRoommate);
        decision.setDecidedAt(LocalDateTime.now());
        return settlementDecisionRepository.save(decision);
    }

    private List<BalanceDto> visibleBalancesFor(Roommate currentRoommate, List<BalanceDto> balances) {
        if (currentRoommate.getRole() == HouseholdRole.MAIN_TENANT) {
            return balances;
        }

        return balances.stream()
            .filter(balance -> currentRoommate.getId().equals(balance.fromRoommateId())
                || currentRoommate.getId().equals(balance.toRoommateId()))
            .toList();
    }

    private List<Payment> visiblePaymentsFor(Roommate currentRoommate) {
        Long householdId = currentRoommate.getHousehold().getId();
        if (currentRoommate.getRole() == HouseholdRole.MAIN_TENANT) {
            return paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(householdId);
        }

        return paymentRepository.findAllInvolvingRoommate(householdId, currentRoommate.getId());
    }

    private void validatePaymentReducesOpenBalance(Roommate fromRoommate, Roommate toRoommate, BigDecimal amount) {
        BigDecimal openAmount = calculateOpenBalances(fromRoommate.getHousehold().getId()).stream()
            .filter(balance -> fromRoommate.getId().equals(balance.fromRoommateId())
                && toRoommate.getId().equals(balance.toRoommateId()))
            .map(BalanceDto::amount)
            .findFirst()
            .orElse(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));

        if (openAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ConflictException("There is no open balance to this roommate.");
        }
        if (amount.compareTo(openAmount) > 0) {
            throw new ConflictException("Payment amount must not exceed the open balance.");
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ConflictException("Payment amount must be greater than zero.");
        }
    }

    private void validateDifferentRoommates(Roommate fromRoommate, Roommate toRoommate) {
        if (fromRoommate.getId().equals(toRoommate.getId())) {
            throw new ConflictException("You cannot record a payment to yourself.");
        }
    }

    private BigDecimal sumAmountsFrom(Roommate roommate, List<BalanceDto> balances) {
        return balances.stream()
            .filter(balance -> roommate.getId().equals(balance.fromRoommateId()))
            .map(BalanceDto::amount)
            .reduce(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP), BigDecimal::add);
    }

    private BigDecimal sumAmountsTo(Roommate roommate, List<BalanceDto> balances) {
        return balances.stream()
            .filter(balance -> roommate.getId().equals(balance.toRoommateId()))
            .map(BalanceDto::amount)
            .reduce(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP), BigDecimal::add);
    }

    private PaymentDto toDto(Payment payment) {
        Roommate fromRoommate = payment.getFromRoommate();
        Roommate toRoommate = payment.getToRoommate();

        return new PaymentDto(
            payment.getId(),
            fromRoommate.getId(),
            toDisplayName(fromRoommate),
            fromRoommate.getUser().getEmail(),
            toRoommate.getId(),
            toDisplayName(toRoommate),
            toRoommate.getUser().getEmail(),
            toMoney(payment.getAmount()),
            payment.getDate(),
            payment.getCreatedAt()
        );
    }

    private PendingSettlementDto toPendingSettlementDto(BalanceDto balance, Roommate currentRoommate) {
        return new PendingSettlementDto(
            settlementId(balance.fromRoommateId(), balance.toRoommateId(), balance.amount(), currentRoommate.getHousehold().getId()),
            expenseDescriptionFor(balance, currentRoommate.getHousehold().getId()),
            balance.fromRoommateId(),
            balance.fromRoommateName(),
            balance.fromRoommateEmail(),
            balance.toRoommateId(),
            balance.toRoommateName(),
            balance.toRoommateEmail(),
            toMoney(balance.amount()),
            SettlementDecisionStatus.PENDING,
            currentRoommate.getId().equals(balance.fromRoommateId()),
            null,
            null,
            null
        );
    }

    private PendingSettlementDto toDecidedSettlementDto(
        BalanceDto balance,
        Roommate currentRoommate,
        SettlementDecision decision
    ) {
        return new PendingSettlementDto(
            decision.getSettlementId(),
            expenseDescriptionFor(balance, currentRoommate.getHousehold().getId()),
            balance.fromRoommateId(),
            balance.fromRoommateName(),
            balance.fromRoommateEmail(),
            balance.toRoommateId(),
            balance.toRoommateName(),
            balance.toRoommateEmail(),
            toMoney(balance.amount()),
            decision.getStatus(),
            currentRoommate.getId().equals(balance.fromRoommateId()),
            decision.getCreatedAt(),
            decision.getDecidedAt(),
            toDisplayName(decision.getDecidedBy())
        );
    }

    private String expenseDescriptionFor(BalanceDto balance, Long householdId) {
        List<String> descriptions = expenseSplitRepository.findAllByExpenseHouseholdId(householdId).stream()
            .filter(split -> balance.fromRoommateId().equals(split.getRoommate().getId()))
            .filter(split -> balance.toRoommateId().equals(split.getExpense().getPaidBy().getId()))
            .filter(split -> toMoney(split.getAmountOwed()).compareTo(BigDecimal.ZERO) > 0)
            .sorted(Comparator
                .comparing((ExpenseSplit split) -> split.getExpense().getDate()).reversed()
                .thenComparing(split -> split.getExpense().getId(), Comparator.reverseOrder()))
            .map(split -> split.getExpense().getDescription())
            .filter(description -> description != null && !description.isBlank())
            .distinct()
            .toList();

        if (descriptions.isEmpty()) {
            return "Open settlement";
        }
        if (descriptions.size() == 1) {
            return descriptions.get(0);
        }
        return descriptions.get(0) + " +" + (descriptions.size() - 1) + " more";
    }

    private String settlementId(Long fromRoommateId, Long toRoommateId, BigDecimal amount, Long householdId) {
        long amountInCents = toMoney(amount).movePointRight(MONEY_SCALE).longValueExact();
        return fromRoommateId + "-" + toRoommateId + "-" + amountInCents + "-" + settlementSnapshotVersion(householdId);
    }

    private String settlementSnapshotVersion(Long householdId) {
        long maxExpenseId = Optional.ofNullable(expenseRepository.findMaxIdByHouseholdId(householdId)).orElse(0L);
        long maxPaymentId = Optional.ofNullable(paymentRepository.findMaxIdByHouseholdId(householdId)).orElse(0L);
        return "e" + maxExpenseId + "p" + maxPaymentId;
    }

    private SettlementKey parseSettlementId(String settlementId) {
        if (settlementId == null || !settlementId.matches("\\d+-\\d+-\\d+-e\\d+p\\d+")) {
            throw new NotFoundException("Settlement does not exist.");
        }

        String[] parts = settlementId.split("-");
        try {
            Long fromRoommateId = Long.valueOf(parts[0]);
            Long toRoommateId = Long.valueOf(parts[1]);
            BigDecimal amount = new BigDecimal(parts[2]).movePointLeft(MONEY_SCALE).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            return new SettlementKey(fromRoommateId, toRoommateId, amount);
        } catch (NumberFormatException ex) {
            throw new NotFoundException("Settlement does not exist.");
        }
    }

    private String toDisplayName(Roommate roommate) {
        return roommate.getUser().getFirstName() + " " + roommate.getUser().getLastName();
    }

    private BigDecimal toMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private record SettlementKey(Long fromRoommateId, Long toRoommateId, BigDecimal amount) {}

}
