package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import at.ac.tuwien.sepr.groupphase.backend.entity.QuantityUnit;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record UserDataExportDto(
    Instant generatedAt,
    AccountExportDto account,
    HouseholdMembershipExportDto householdMembership,
    List<TaskExportDto> tasks,
    List<CleaningScheduleExportDto> cleaningSchedules,
    List<ExpenseExportDto> expenses,
    List<PaymentExportDto> payments,
    List<ShoppingItemExportDto> shoppingItems,
    HistoryExportDto history
) {
    public record AccountExportDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role,
        LocalDateTime termsAcceptedAt,
        String termsVersion,
        LocalDateTime privacyAcknowledgedAt,
        String privacyVersion
    ) {}

    public record HouseholdMembershipExportDto(
        Long householdId,
        String householdName,
        Long roommateId,
        String membershipRole,
        LocalDateTime joinedAt
    ) {}

    public record TaskExportDto(
        Long id,
        Long householdId,
        String title,
        String description,
        LocalDate dueDate,
        TaskStatus status,
        String createdByEmail,
        String assignedToEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record CleaningScheduleExportDto(
        Long id,
        Long householdId,
        String title,
        String description,
        CleaningFrequency frequency,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate nextDueDate,
        boolean active,
        String createdByEmail,
        String assignedToEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record ExpenseExportDto(
        Long id,
        Long householdId,
        String description,
        BigDecimal totalAmount,
        LocalDate date,
        String paidByEmail,
        String createdByEmail,
        List<ExpenseSplitExportDto> splits,
        LocalDateTime createdAt
    ) {}

    public record ExpenseSplitExportDto(
        Long roommateId,
        String roommateEmail,
        BigDecimal amountOwed
    ) {}

    public record PaymentExportDto(
        Long id,
        Long householdId,
        Long fromRoommateId,
        String fromEmail,
        Long toRoommateId,
        String toEmail,
        BigDecimal amount,
        LocalDate date,
        String createdByEmail,
        LocalDateTime createdAt
    ) {}

    public record ShoppingItemExportDto(
        Long id,
        Long householdId,
        String name,
        Integer quantityCount,
        BigDecimal quantityAmount,
        QuantityUnit quantityUnit,
        String notes,
        ShoppingItemStatus status,
        ShoppingItemVisibility visibility,
        String createdByEmail,
        String targetUserEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record HistoryExportDto(
        List<TaskExportDto> completedOrCancelledTasks,
        List<ExpenseExportDto> recordedExpenses,
        List<PaymentExportDto> recordedPayments
    ) {}
}
