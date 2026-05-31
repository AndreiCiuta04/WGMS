package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BalanceDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Payment;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BalanceCalculator {

    private static final int MONEY_SCALE = 2;

    public List<BalanceDto> calculateOpenBalances(List<ExpenseSplit> splits, List<Payment> payments) {
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

    private String toDisplayName(Roommate roommate) {
        return roommate.getUser().getFirstName() + " " + roommate.getUser().getLastName();
    }

    private BigDecimal toMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private record RoommateBalance(Roommate roommate, BigDecimal amount) {}
}
