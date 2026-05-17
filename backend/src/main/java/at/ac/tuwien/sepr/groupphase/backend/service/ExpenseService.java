package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BalanceDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseSplitCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseUpdateDto;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseService {

    ExpenseDto createExpense(ExpenseCreateDto expenseCreateDto, String currentUserEmail);

    List<ExpenseDto> findAllExpenses(String currentUserEmail);

    ExpenseDto findExpense(Long id, String currentUserEmail);

    ExpenseDto updateExpense(Long id, ExpenseUpdateDto expenseUpdateDto, String currentUserEmail);

    void deleteExpense(Long id, String currentUserEmail);

    List<BalanceDto> calculateBalances(String currentUserEmail);

    List<ExpenseSplitCreateDto> calculateEqualSplits(
        BigDecimal totalAmount,
        List<Long> participantRoommateIds,
        String currentUserEmail
    );
}
