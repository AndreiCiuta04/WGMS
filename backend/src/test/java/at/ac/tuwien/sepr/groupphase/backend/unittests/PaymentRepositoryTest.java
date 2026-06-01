package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Payment;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.PaymentRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
public class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private RoommateRepository roommateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private Household household;
    private Roommate mainTenant;
    private Roommate roommate;

    @BeforeEach
    public void setUp() {
        ApplicationUser mainUser = saveUser("payment-main@test.com", "Payment", "Main");
        ApplicationUser roommateUser = saveUser("payment-room@test.com", "Payment", "Room");
        household = saveHousehold(mainUser);
        mainTenant = saveRoommate(mainUser, HouseholdRole.MAIN_TENANT);
        roommate = saveRoommate(roommateUser, HouseholdRole.ROOMMATE);
    }

    @Test
    public void findAllByHousehold_fetchesRoommateUsersWithoutAdditionalQueries() {
        savePayment(roommate, mainTenant, "4.00");
        savePayment(mainTenant, roommate, "2.00");
        Statistics statistics = prepareStatistics();

        List<Payment> payments = paymentRepository.findAllByHouseholdIdOrderByDateDescCreatedAtDescIdDesc(household.getId());
        long statementsAfterLoad = statistics.getPrepareStatementCount();
        accessMappedFields(payments);

        assertEquals(2, payments.size());
        assertEquals(statementsAfterLoad, statistics.getPrepareStatementCount());
    }

    @Test
    public void findAllInvolvingRoommate_fetchesRoommateUsersWithoutAdditionalQueries() {
        savePayment(roommate, mainTenant, "4.00");
        savePayment(mainTenant, roommate, "2.00");
        Statistics statistics = prepareStatistics();

        List<Payment> payments = paymentRepository.findAllInvolvingRoommate(household.getId(), roommate.getId());
        long statementsAfterLoad = statistics.getPrepareStatementCount();
        accessMappedFields(payments);

        assertEquals(2, payments.size());
        assertEquals(statementsAfterLoad, statistics.getPrepareStatementCount());
    }

    private Statistics prepareStatistics() {
        paymentRepository.flush();
        entityManager.clear();

        Statistics statistics = entityManager.getEntityManagerFactory()
            .unwrap(SessionFactory.class)
            .getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        return statistics;
    }

    private void accessMappedFields(List<Payment> payments) {
        payments.forEach(payment -> {
            payment.getHousehold().getName();
            payment.getFromRoommate().getUser().getEmail();
            payment.getToRoommate().getUser().getEmail();
            payment.getCreatedBy().getUser().getEmail();
        });
    }

    private ApplicationUser saveUser(String email, String firstName, String lastName) {
        ApplicationUser user = new ApplicationUser();
        user.setEmail(email);
        user.setPassword("encoded-password-for-test");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAdmin(false);
        return userRepository.save(user);
    }

    private Household saveHousehold(ApplicationUser createdBy) {
        Household householdToSave = new Household();
        householdToSave.setName("Payment repository test household");
        householdToSave.setAddress("Test address");
        householdToSave.setCreatedBy(createdBy);
        return householdRepository.save(householdToSave);
    }

    private Roommate saveRoommate(ApplicationUser user, HouseholdRole role) {
        Roommate roommateToSave = new Roommate();
        roommateToSave.setUser(user);
        roommateToSave.setHousehold(household);
        roommateToSave.setRole(role);
        roommateToSave.setActive(true);
        return roommateRepository.save(roommateToSave);
    }

    private Payment savePayment(Roommate fromRoommate, Roommate toRoommate, String amount) {
        Payment payment = new Payment();
        payment.setHousehold(household);
        payment.setFromRoommate(fromRoommate);
        payment.setToRoommate(toRoommate);
        payment.setCreatedBy(fromRoommate);
        payment.setAmount(new BigDecimal(amount));
        payment.setDate(LocalDate.now());
        return paymentRepository.save(payment);
    }
}
