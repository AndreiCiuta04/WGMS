package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseSplitCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseSplitDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ForbiddenException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
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
import java.util.List;
import java.util.Set;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int MONEY_SCALE = 2;

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final RoommateRepository roommateRepository;
    private final UserService userService;

    public ExpenseServiceImpl(
        ExpenseRepository expenseRepository,
        ExpenseSplitRepository expenseSplitRepository,
        RoommateRepository roommateRepository,
        UserService userService
    ) {
        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.roommateRepository = roommateRepository;
        this.userService = userService;
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

    private Roommate getCurrentRoommate(ApplicationUser currentUser) {
        return roommateRepository.findByUserIdAndActiveTrue(currentUser.getId())
            .orElseThrow(() -> new ForbiddenException("User does not belong to an active household."));
    }

    private Roommate getActiveRoommateInHousehold(Long roommateId, Long householdId) {
        return roommateRepository.findByIdAndHouseholdIdAndActiveTrue(roommateId, householdId)
            .orElseThrow(() -> new NotFoundException("Roommate is not an active member of your household."));
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

    private ExpenseDto toDto(Expense expense, List<ExpenseSplit> splits) {
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
