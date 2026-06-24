package at.ac.tuwien.sepr.groupphase.backend.basetest;

import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdInvitationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationSettingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.SettlementDecisionRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.VerificationCodeRepository;

public final class TestDbCleaner {

    private TestDbCleaner() {
    }

    public static void clearNotificationData(NotificationRepository notificationRepository,
                                             NotificationSettingRepository notificationSettingRepository) {
        notificationRepository.deleteAll();
        notificationSettingRepository.deleteAll();
    }

    public static void clearAllApplicationData(SettlementDecisionRepository settlementDecisionRepository,
                                               PaymentRepository paymentRepository,
                                               ExpenseSplitRepository expenseSplitRepository,
                                               ExpenseRepository expenseRepository,
                                               TaskRepository taskRepository,
                                               CleaningScheduleRepository cleaningScheduleRepository,
                                               ShoppingItemRepository shoppingItemRepository,
                                               HouseholdInvitationRepository invitationRepository,
                                               RoommateRepository roommateRepository,
                                               HouseholdRepository householdRepository,
                                               NotificationRepository notificationRepository,
                                               NotificationSettingRepository notificationSettingRepository,
                                               VerificationCodeRepository verificationCodeRepository,
                                               UserRepository userRepository) {
        settlementDecisionRepository.deleteAll();
        paymentRepository.deleteAll();
        expenseSplitRepository.deleteAll();
        expenseRepository.deleteAll();
        taskRepository.deleteAll();
        cleaningScheduleRepository.deleteAll();
        shoppingItemRepository.deleteAll();
        invitationRepository.deleteAll();
        roommateRepository.deleteAll();
        householdRepository.deleteAll();
        clearNotificationData(notificationRepository, notificationSettingRepository);
        verificationCodeRepository.deleteAll();
        userRepository.deleteAll();
    }
}
