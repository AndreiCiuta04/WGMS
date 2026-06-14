package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BalanceDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PaymentOverviewDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Payment;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
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
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int MONEY_SCALE = 2;

    private final ExpenseSplitRepository expenseSplitRepository;
    private final PaymentRepository paymentRepository;
    private final HouseholdRepository householdRepository;
    private final RoommateRepository roommateRepository;
    private final UserService userService;
    private final BalanceCalculator balanceCalculator;
    private final NotificationService notificationService;

    public PaymentServiceImpl(
        ExpenseSplitRepository expenseSplitRepository,
        PaymentRepository paymentRepository,
        HouseholdRepository householdRepository,
        RoommateRepository roommateRepository,
        UserService userService,
        BalanceCalculator balanceCalculator,
        NotificationService notificationService
    ) {
        this.expenseSplitRepository = expenseSplitRepository;
        this.paymentRepository = paymentRepository;
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

    private String toDisplayName(Roommate roommate) {
        return roommate.getUser().getFirstName() + " " + roommate.getUser().getLastName();
    }

    private BigDecimal toMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

}
