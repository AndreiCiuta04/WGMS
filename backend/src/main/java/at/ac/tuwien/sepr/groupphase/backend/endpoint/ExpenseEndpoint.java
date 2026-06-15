package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BalanceDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BillExpensePreviewDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BillExpensePreviewRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EqualSplitRequestDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseSplitCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ExpenseUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.service.ExpenseService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/expenses")
public class ExpenseEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ExpenseService expenseService;

    public ExpenseEndpoint(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public ExpenseDto createExpense(@Valid @RequestBody ExpenseCreateDto expenseCreateDto, Authentication authentication) {
        LOGGER.info("POST /api/v1/expenses");
        return expenseService.createExpense(expenseCreateDto, authentication.getName());
    }

    @PostMapping("/equal-split")
    @PreAuthorize("hasRole('USER')")
    public List<ExpenseSplitCreateDto> calculateEqualSplits(
        @Valid @RequestBody EqualSplitRequestDto equalSplitRequestDto,
        Authentication authentication
    ) {
        LOGGER.info("POST /api/v1/expenses/equal-split");
        return expenseService.calculateEqualSplits(
            equalSplitRequestDto.totalAmount(),
            equalSplitRequestDto.participantRoommateIds(),
            authentication.getName()
        );
    }

    @PostMapping("/bill-preview")
    @PreAuthorize("hasRole('USER')")
    public BillExpensePreviewDto previewBillExpense(
        @Valid @RequestBody BillExpensePreviewRequestDto previewRequestDto,
        Authentication authentication
    ) {
        LOGGER.info("POST /api/v1/expenses/bill-preview");
        return expenseService.previewBillExpense(previewRequestDto, authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<ExpenseDto> findAllExpenses(Authentication authentication) {
        LOGGER.info("GET /api/v1/expenses");
        return expenseService.findAllExpenses(authentication.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ExpenseDto findExpense(@PathVariable("id") Long id, Authentication authentication) {
        LOGGER.info("GET /api/v1/expenses/{}", id);
        return expenseService.findExpense(id, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ExpenseDto updateExpense(
        @PathVariable("id") Long id,
        @Valid @RequestBody ExpenseUpdateDto expenseUpdateDto,
        Authentication authentication
    ) {
        LOGGER.info("PUT /api/v1/expenses/{}", id);
        return expenseService.updateExpense(id, expenseUpdateDto, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void deleteExpense(@PathVariable("id") Long id, Authentication authentication) {
        LOGGER.info("DELETE /api/v1/expenses/{}", id);
        expenseService.deleteExpense(id, authentication.getName());
    }

    @GetMapping("/balances")
    @PreAuthorize("hasRole('USER')")
    public List<BalanceDto> calculateBalances(Authentication authentication) {
        LOGGER.info("GET /api/v1/expenses/balances");
        return expenseService.calculateBalances(authentication.getName());
    }
}
