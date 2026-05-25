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
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PaymentService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int MONEY_SCALE = 2;

    private final ExpenseSplitRepository expenseSplitRepository;
    private final PaymentRepository paymentRepository;
    private final RoommateRepository roommateRepository;
    private final UserService userService;

    public PaymentServiceImpl(
        ExpenseSplitRepository expenseSplitRepository,
        PaymentRepository paymentRepository,
        RoommateRepository roommateRepository,
        UserService userService
    ) {
        this.expenseSplitRepository = expenseSplitRepository;
        this.paymentRepository = paymentRepository;
        this.roommateRepository = roommateRepository;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentOverviewDto getOverview(String currentUserEmail) {
        LOGGER.debug("Get payment overview for user {}", currentUserEmail);

        Roommate currentRoommate = getCurrentRoommateByEmail(currentUserEmail);
        Long householdId = currentRoommate.getHousehold().getId();
        List<BalanceDto> allOpenBalances = buildOpenBalances(householdId);
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

        Roommate toRoommate = getActiveRoommateInHousehold(
            paymentCreateDto.toRoommateId(),
            currentRoommate.getHousehold().getId()
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

        return toDto(paymentRepository.save(payment));
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

    private List<BalanceDto> buildOpenBalances(Long householdId) {
        List<ExpenseSplit> splits = expenseSplitRepository.findAllByExpenseHouseholdId(householdId);
        List<Payment> payments = paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(householdId);
        Map<Long, BigDecimal> netAmountsByRoommateId = new HashMap<>();
        Map<Long, Roommate> roommatesById = new HashMap<>();

        for (ExpenseSplit split : splits) {
            Roommate paidBy = split.getExpense().getPaidBy();
            Roommate owedBy = split.getRoommate();
            BigDecimal amount = toMoney(split.getAmountOwed());

            roommatesById.put(paidBy.getId(), paidBy);
            roommatesById.put(owedBy.getId(), owedBy);
            netAmountsByRoommateId.merge(paidBy.getId(), amount, BigDecimal::add);
            netAmountsByRoommateId.merge(owedBy.getId(), amount.negate(), BigDecimal::add);
        }

        for (Payment payment : payments) {
            Roommate fromRoommate = payment.getFromRoommate();
            Roommate toRoommate = payment.getToRoommate();
            BigDecimal amount = toMoney(payment.getAmount());

            roommatesById.put(fromRoommate.getId(), fromRoommate);
            roommatesById.put(toRoommate.getId(), toRoommate);
            netAmountsByRoommateId.merge(fromRoommate.getId(), amount, BigDecimal::add);
            netAmountsByRoommateId.merge(toRoommate.getId(), amount.negate(), BigDecimal::add);
        }

        return settleNetAmounts(netAmountsByRoommateId, roommatesById);
    }

    private List<BalanceDto> settleNetAmounts(Map<Long, BigDecimal> netAmountsByRoommateId, Map<Long, Roommate> roommatesById) {
        List<RoommateBalance> debtors = new ArrayList<>(toRoommateBalances(netAmountsByRoommateId, roommatesById, -1));
        List<RoommateBalance> creditors = new ArrayList<>(toRoommateBalances(netAmountsByRoommateId, roommatesById, 1));
        List<BalanceDto> balances = new ArrayList<>();
        int debtorIndex = 0;
        int creditorIndex = 0;

        while (debtorIndex < debtors.size() && creditorIndex < creditors.size()) {
            RoommateBalance debtor = debtors.get(debtorIndex);
            RoommateBalance creditor = creditors.get(creditorIndex);
            BigDecimal amount = debtor.amount().min(creditor.amount());

            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                balances.add(toBalanceDto(debtor.roommate(), creditor.roommate(), amount));
            }

            debtor = new RoommateBalance(debtor.roommate(), debtor.amount().subtract(amount));
            creditor = new RoommateBalance(creditor.roommate(), creditor.amount().subtract(amount));
            debtors.set(debtorIndex, debtor);
            creditors.set(creditorIndex, creditor);

            if (debtor.amount().compareTo(BigDecimal.ZERO) == 0) {
                debtorIndex++;
            }
            if (creditor.amount().compareTo(BigDecimal.ZERO) == 0) {
                creditorIndex++;
            }
        }

        return balances;
    }

    private List<RoommateBalance> toRoommateBalances(
        Map<Long, BigDecimal> netAmountsByRoommateId,
        Map<Long, Roommate> roommatesById,
        int sign
    ) {
        return netAmountsByRoommateId.entrySet().stream()
            .filter(entry -> toMoney(entry.getValue()).signum() == sign)
            .map(entry -> new RoommateBalance(roommatesById.get(entry.getKey()), toMoney(entry.getValue().abs())))
            .sorted(Comparator.comparing(balance -> balance.roommate().getId()))
            .toList();
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
        BigDecimal openAmount = buildOpenBalances(fromRoommate.getHousehold().getId()).stream()
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

    private BalanceDto toBalanceDto(Roommate fromRoommate, Roommate toRoommate, BigDecimal amount) {
        return new BalanceDto(
            fromRoommate.getId(),
            toDisplayName(fromRoommate),
            fromRoommate.getUser().getEmail(),
            toRoommate.getId(),
            toDisplayName(toRoommate),
            toRoommate.getUser().getEmail(),
            toMoney(amount)
        );
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

    private record RoommateBalance(Roommate roommate, BigDecimal amount) {}
}
