package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.datagenerator.DemoDataGenerator;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DemoDataGeneratorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private RoommateRepository roommateRepository;

    @Mock
    private CleaningScheduleRepository cleaningScheduleRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseSplitRepository expenseSplitRepository;

    @Mock
    private ShoppingItemRepository shoppingItemRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @Mock
    private ApplicationArguments applicationArguments;

    private DemoDataGenerator generator;

    @BeforeEach
    public void setUp() {
        generator = new DemoDataGenerator(
            userRepository,
            householdRepository,
            roommateRepository,
            cleaningScheduleRepository,
            taskRepository,
            expenseRepository,
            expenseSplitRepository,
            shoppingItemRepository,
            passwordEncoder,
            entityManager
        );

        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(ApplicationUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(householdRepository.save(any(Household.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roommateRepository.save(any(Roommate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cleaningScheduleRepository.save(any(CleaningSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(expenseSplitRepository.save(any(ExpenseSplit.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shoppingItemRepository.save(any(ShoppingItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);
    }

    @Test
    public void givenNoExistingDemoUsers_whenRun_thenGeneratesPresentationFr15AndFr17SeedData() {
        generator.run(applicationArguments);

        verify(userRepository, atLeast(18)).save(any(ApplicationUser.class));
        verify(householdRepository, atLeast(4)).save(any(Household.class));
        verify(roommateRepository, atLeast(16)).save(any(Roommate.class));
        verify(cleaningScheduleRepository, atLeast(6)).save(any(CleaningSchedule.class));
        verify(taskRepository, atLeast(30)).save(any(Task.class));
        verify(expenseRepository, atLeast(20)).save(any(Expense.class));
        verify(expenseSplitRepository, atLeast(90)).save(any(ExpenseSplit.class));
        verify(shoppingItemRepository, atLeast(20)).save(any(ShoppingItem.class));
    }
}
