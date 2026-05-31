package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BalanceDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseSplitCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseSplitDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Payment;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ExpenseService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int MONEY_SCALE = 2;

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final PaymentRepository paymentRepository;
    private final RoommateRepository roommateRepository;
    private final UserService userService;
    private final BalanceCalculator balanceCalculator;

    public ExpenseServiceImpl(
        ExpenseRepository expenseRepository,
        ExpenseSplitRepository expenseSplitRepository,
        PaymentRepository paymentRepository,
        RoommateRepository roommateRepository,
        UserService userService,
        BalanceCalculator balanceCalculator
    ) {
        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.paymentRepository = paymentRepository;
        this.roommateRepository = roommateRepository;
        this.userService = userService;
        this.balanceCalculator = balanceCalculator;
    }

    @Override
    @Transactional
    public ExpenseDto createExpense(ExpenseCreateDto expenseCreateDto, String currentUserEmail) {
        LOGGER.debug("Create expense by user {}", currentUserEmail);

        ApplicationUser currentUser = userService.findApplicationUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);

        validateSplitParticipantsAreUnique(expenseCreateDto.splits());
        validateCurrentRoommatePaid(expenseCreateDto.paidByRoommateId(), currentRoommate);
        validatePaidByIsIncludedInSplits(expenseCreateDto.paidByRoommateId(), expenseCreateDto.splits());
        validateSplitSum(expenseCreateDto.totalAmount(), expenseCreateDto.splits());

        Long householdId = currentRoommate.getHousehold().getId();
        Roommate paidBy = getActiveRoommateInHousehold(expenseCreateDto.paidByRoommateId(), householdId);
        Expense expense = new Expense();
        expense.setHousehold(currentRoommate.getHousehold());
        expense.setDescription(expenseCreateDto.description());
        expense.setTotalAmount(toMoney(expenseCreateDto.totalAmount()));
        expense.setDate(expenseCreateDto.date());
        expense.setPaidBy(paidBy);
        expense.setCreatedBy(currentRoommate);

        Expense savedExpense = expenseRepository.save(expense);
        List<ExpenseSplit> savedSplits = expenseSplitRepository.saveAll(createSplits(savedExpense, expenseCreateDto.splits(), householdId));

        return toDto(savedExpense, savedSplits);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDto> findAllExpenses(String currentUserEmail) {
        LOGGER.debug("Find expenses for user {}", currentUserEmail);

        Roommate currentRoommate = getCurrentRoommateByEmail(currentUserEmail);
        Long householdId = currentRoommate.getHousehold().getId();

        return expenseRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDesc(householdId).stream()
            .map(this::toDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDto findExpense(Long id, String currentUserEmail) {
        LOGGER.debug("Find expense with id {} for user {}", id, currentUserEmail);

        Roommate currentRoommate = getCurrentRoommateByEmail(currentUserEmail);
        Expense expense = getExpenseInCurrentHousehold(id, currentRoommate);

        return toDto(expense);
    }

    @Override
    @Transactional
    public ExpenseDto updateExpense(Long id, ExpenseUpdateDto expenseUpdateDto, String currentUserEmail) {
        LOGGER.debug("Update expense with id {} by user {}", id, currentUserEmail);

        Roommate currentRoommate = getCurrentRoommateByEmail(currentUserEmail);
        Expense expense = getExpenseInCurrentHousehold(id, currentRoommate);

        requireCanManageExpense(expense, currentRoommate);
        validateSplitParticipantsAreUnique(expenseUpdateDto.splits());
        validateCurrentRoommatePaid(expenseUpdateDto.paidByRoommateId(), currentRoommate);
        validatePaidByIsIncludedInSplits(expenseUpdateDto.paidByRoommateId(), expenseUpdateDto.splits());
        validateSplitSum(expenseUpdateDto.totalAmount(), expenseUpdateDto.splits());

        Long householdId = currentRoommate.getHousehold().getId();
        Roommate paidBy = getActiveRoommateInHousehold(expenseUpdateDto.paidByRoommateId(), householdId);
        expense.setDescription(expenseUpdateDto.description());
        expense.setTotalAmount(toMoney(expenseUpdateDto.totalAmount()));
        expense.setDate(expenseUpdateDto.date());
        expense.setPaidBy(paidBy);

        Expense savedExpense = expenseRepository.save(expense);
        List<ExpenseSplit> savedSplits = updateExistingSplits(savedExpense.getId(), expenseUpdateDto.splits());

        return toDto(savedExpense, savedSplits);
    }

    @Override
    @Transactional
    public void deleteExpense(Long id, String currentUserEmail) {
        LOGGER.debug("Delete expense with id {} by user {}", id, currentUserEmail);

        Roommate currentRoommate = getCurrentRoommateByEmail(currentUserEmail);
        Expense expense = getExpenseInCurrentHousehold(id, currentRoommate);

        requireCanManageExpense(expense, currentRoommate);

        expenseSplitRepository.deleteAllByExpenseId(expense.getId());
        expenseRepository.delete(expense);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BalanceDto> calculateBalances(String currentUserEmail) {
        LOGGER.debug("Calculate expense balances for user {}", currentUserEmail);

        Roommate currentRoommate = getCurrentRoommateByEmail(currentUserEmail);
        Long householdId = currentRoommate.getHousehold().getId();
        List<ExpenseSplit> splits = expenseSplitRepository.findAllByExpenseHouseholdId(householdId);
        List<Payment> payments = paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(householdId);

        return balanceCalculator.calculateOpenBalances(splits, payments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseSplitCreateDto> calculateEqualSplits(
        BigDecimal totalAmount,
        List<Long> participantRoommateIds,
        String currentUserEmail
    ) {
        LOGGER.debug("Calculate equal expense splits for user {}", currentUserEmail);

        ApplicationUser currentUser = userService.findApplicationUserByEmail(currentUserEmail);
        Roommate currentRoommate = getCurrentRoommate(currentUser);

        validateEqualSplitInput(totalAmount, participantRoommateIds);
        validateParticipantRoommatesAreUnique(participantRoommateIds);
        validateParticipantsAreInCurrentHousehold(participantRoommateIds, currentRoommate.getHousehold().getId());

        return buildEqualSplits(totalAmount, participantRoommateIds);
    }

    private Roommate getCurrentRoommateByEmail(String currentUserEmail) {
        ApplicationUser currentUser = userService.findApplicationUserByEmail(currentUserEmail);
        return getCurrentRoommate(currentUser);
    }

    private Roommate getCurrentRoommate(ApplicationUser currentUser) {
        return roommateRepository.findByUserIdAndActiveTrue(currentUser.getId())
            .orElseThrow(() -> new ForbiddenException("User does not belong to an active household."));
    }

    private Roommate getActiveRoommateInHousehold(Long roommateId, Long householdId) {
        return roommateRepository.findByIdAndHouseholdIdAndActiveTrue(roommateId, householdId)
            .orElseThrow(() -> new NotFoundException("Roommate is not an active member of your household."));
    }

    private Expense getExpenseInCurrentHousehold(Long expenseId, Roommate currentRoommate) {
        return expenseRepository.findByIdAndHouseholdId(expenseId, currentRoommate.getHousehold().getId())
            .orElseThrow(() -> new NotFoundException(String.format("Could not find expense with id %s", expenseId)));
    }

    private void requireCanManageExpense(Expense expense, Roommate currentRoommate) {
        if (expense.getCreatedBy().getId().equals(currentRoommate.getId())) {
            return;
        }

        throw new ForbiddenException("You are not allowed to manage this expense.");
    }

    private void validateSplitParticipantsAreUnique(List<ExpenseSplitCreateDto> splitDtos) {
        Set<Long> roommateIds = new HashSet<>();
        for (ExpenseSplitCreateDto splitDto : splitDtos) {
            if (!roommateIds.add(splitDto.roommateId())) {
                throw new ConflictException("Each roommate may only appear once in the expense splits.");
            }
        }
    }

    private void validateEqualSplitInput(BigDecimal totalAmount, List<Long> participantRoommateIds) {
        if (totalAmount == null) {
            throw new ConflictException("Total amount must not be null.");
        }
        if (toMoney(totalAmount).compareTo(BigDecimal.ZERO) <= 0) {
            throw new ConflictException("Total amount must be greater than zero.");
        }
        if (participantRoommateIds == null || participantRoommateIds.isEmpty()) {
            throw new ConflictException("At least one participant is required.");
        }
    }

    private void validateParticipantRoommatesAreUnique(List<Long> participantRoommateIds) {
        Set<Long> uniqueRoommateIds = new HashSet<>();
        for (Long roommateId : participantRoommateIds) {
            if (roommateId == null) {
                throw new ConflictException("Participant roommate id must not be null.");
            }
            if (!uniqueRoommateIds.add(roommateId)) {
                throw new ConflictException("Each roommate may only appear once in the expense splits.");
            }
        }
    }

    private void validateParticipantsAreInCurrentHousehold(List<Long> participantRoommateIds, Long householdId) {
        for (Long roommateId : participantRoommateIds) {
            getActiveRoommateInHousehold(roommateId, householdId);
        }
    }

    private void validateCurrentRoommatePaid(Long paidByRoommateId, Roommate currentRoommate) {
        if (!currentRoommate.getId().equals(paidByRoommateId)) {
            throw new ForbiddenException("You may only create expenses that you paid yourself.");
        }
    }

    private void validatePaidByIsIncludedInSplits(Long paidByRoommateId, List<ExpenseSplitCreateDto> splitDtos) {
        boolean paidByIsIncluded = splitDtos.stream()
            .anyMatch(splitDto -> paidByRoommateId.equals(splitDto.roommateId()));

        if (!paidByIsIncluded) {
            throw new ConflictException("The paying roommate must be included in the expense splits.");
        }
    }

    private void validateSplitSum(BigDecimal totalAmount, List<ExpenseSplitCreateDto> splitDtos) {
        BigDecimal splitSum = splitDtos.stream()
            .map(ExpenseSplitCreateDto::amountOwed)
            .map(this::toMoney)
            .reduce(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP), BigDecimal::add);

        if (toMoney(totalAmount).compareTo(splitSum) != 0) {
            throw new ConflictException("Sum of expense splits must match the total amount.");
        }
    }

    private List<ExpenseSplit> createSplits(Expense expense, List<ExpenseSplitCreateDto> splitDtos, Long householdId) {
        return splitDtos.stream()
            .map(splitDto -> createSplit(expense, splitDto, householdId))
            .toList();
    }

    private ExpenseSplit createSplit(Expense expense, ExpenseSplitCreateDto splitDto, Long householdId) {
        Roommate roommate = getActiveRoommateInHousehold(splitDto.roommateId(), householdId);

        ExpenseSplit split = new ExpenseSplit();
        split.setExpense(expense);
        split.setRoommate(roommate);
        split.setAmountOwed(toMoney(splitDto.amountOwed()));
        return split;
    }

    private List<ExpenseSplit> updateExistingSplits(Long expenseId, List<ExpenseSplitCreateDto> splitDtos) {
        List<ExpenseSplit> existingSplits = expenseSplitRepository.findAllByExpenseId(expenseId);
        Map<Long, ExpenseSplit> existingSplitsByRoommateId = new HashMap<>();

        for (ExpenseSplit existingSplit : existingSplits) {
            existingSplitsByRoommateId.put(existingSplit.getRoommate().getId(), existingSplit);
        }

        validateUpdatedSplitParticipantsMatchExistingSplits(splitDtos, existingSplitsByRoommateId.keySet());

        for (ExpenseSplitCreateDto splitDto : splitDtos) {
            ExpenseSplit existingSplit = existingSplitsByRoommateId.get(splitDto.roommateId());
            existingSplit.setAmountOwed(toMoney(splitDto.amountOwed()));
        }

        return expenseSplitRepository.saveAll(existingSplits);
    }

    private void validateUpdatedSplitParticipantsMatchExistingSplits(List<ExpenseSplitCreateDto> splitDtos, Set<Long> existingRoommateIds) {
        Set<Long> updatedRoommateIds = new HashSet<>();
        for (ExpenseSplitCreateDto splitDto : splitDtos) {
            updatedRoommateIds.add(splitDto.roommateId());
        }

        if (!existingRoommateIds.equals(updatedRoommateIds)) {
            throw new ConflictException("Expense split participants cannot be changed during update.");
        }
    }

    private List<ExpenseSplitCreateDto> buildEqualSplits(BigDecimal totalAmount, List<Long> participantRoommateIds) {
        long totalCents = toCents(totalAmount);
        int participantCount = participantRoommateIds.size();

        if (totalCents < participantCount) {
            throw new ConflictException("Total amount is too small to split between all participants.");
        }

        long baseCents = totalCents / participantCount;
        long remainderCents = totalCents % participantCount;
        List<ExpenseSplitCreateDto> splits = new ArrayList<>();

        for (int i = 0; i < participantCount; i++) {
            long participantCents = baseCents;
            if (i < remainderCents) {
                participantCents++;
            }
            splits.add(new ExpenseSplitCreateDto(participantRoommateIds.get(i), BigDecimal.valueOf(participantCents, MONEY_SCALE)));
        }

        return splits;
    }

    private ExpenseDto  toDto(Expense expense, List<ExpenseSplit> splits) {
        Roommate paidBy = expense.getPaidBy();
        Roommate createdBy = expense.getCreatedBy();

        return new ExpenseDto(
            expense.getId(),
            expense.getHousehold().getId(),
            expense.getDescription(),
            expense.getTotalAmount(),
            expense.getDate(),
            expense.getCreatedAt(),
            paidBy.getId(),
            toDisplayName(paidBy),
            paidBy.getUser().getEmail(),
            createdBy.getId(),
            toDisplayName(createdBy),
            createdBy.getUser().getEmail(),
            splits.stream().map(this::toSplitDto).toList()
        );
    }

    private ExpenseDto toDto(Expense expense) {
        return toDto(expense, expenseSplitRepository.findAllByExpenseId(expense.getId()));
    }

    private ExpenseSplitDto toSplitDto(ExpenseSplit split) {
        Roommate roommate = split.getRoommate();
        return new ExpenseSplitDto(
            split.getId(),
            roommate.getId(),
            toDisplayName(roommate),
            roommate.getUser().getEmail(),
            split.getAmountOwed()
        );
    }

    private String toDisplayName(Roommate roommate) {
        return roommate.getUser().getFirstName() + " " + roommate.getUser().getLastName();
    }

    private BigDecimal toMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private long toCents(BigDecimal amount) {
        return toMoney(amount).movePointRight(MONEY_SCALE).longValueExact();
    }

}
