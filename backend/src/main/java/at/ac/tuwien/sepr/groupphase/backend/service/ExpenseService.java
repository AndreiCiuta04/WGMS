package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseSplitCreateDto;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseService {

    ExpenseDto createExpense(ExpenseCreateDto expenseCreateDto, String currentUserEmail);

    List<ExpenseSplitCreateDto> calculateEqualSplits(
        BigDecimal totalAmount,
        List<Long> participantRoommateIds,
        String currentUserEmail
    );
}
