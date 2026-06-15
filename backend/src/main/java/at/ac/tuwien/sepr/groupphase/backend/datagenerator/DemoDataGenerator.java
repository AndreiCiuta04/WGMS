package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningFrequency;
import at.ac.tuwien.sepr.groupphase.backend.entity.CleaningSchedule;
import at.ac.tuwien.sepr.groupphase.backend.entity.Expense;
import at.ac.tuwien.sepr.groupphase.backend.entity.ExpenseSplit;
import at.ac.tuwien.sepr.groupphase.backend.entity.Household;
import at.ac.tuwien.sepr.groupphase.backend.entity.HouseholdRole;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.entity.QuantityUnit;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItem;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemStatus;
import at.ac.tuwien.sepr.groupphase.backend.entity.ShoppingItemVisibility;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.repository.CleaningScheduleRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ExpenseSplitRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.HouseholdRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ShoppingItemRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Profile({"datagen", "generateData"})
@Component
public class DemoDataGenerator implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String PRESENTATION_DEMO_EMAIL = "cemil.demo@wgms.local";
    private static final String DEMO_PASSWORD = "Password123!";
    private static final String FR15_MAIN_TENANT_EMAIL = "maintenant.fr15@example.com";
    private static final String FR15_PASSWORD = "password";

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final RoommateRepository roommateRepository;
    private final CleaningScheduleRepository cleaningScheduleRepository;
    private final TaskRepository taskRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    public DemoDataGenerator(UserRepository userRepository,
                             HouseholdRepository householdRepository,
                             RoommateRepository roommateRepository,
                             CleaningScheduleRepository cleaningScheduleRepository,
                             TaskRepository taskRepository,
                             ExpenseRepository expenseRepository,
                             ExpenseSplitRepository expenseSplitRepository,
                             ShoppingItemRepository shoppingItemRepository,
                             PasswordEncoder passwordEncoder,
                             EntityManager entityManager) {
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
        this.roommateRepository = roommateRepository;
        this.cleaningScheduleRepository = cleaningScheduleRepository;
        this.taskRepository = taskRepository;
        this.expenseRepository = expenseRepository;
        this.expenseSplitRepository = expenseSplitRepository;
        this.shoppingItemRepository = shoppingItemRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findUserByEmail(PRESENTATION_DEMO_EMAIL) == null) {
            generatePresentationDemoData();
        } else {
            LOGGER.info("Presentation demo data already exists. Skipping generation.");
        }

        if (userRepository.findUserByEmail(FR15_MAIN_TENANT_EMAIL) == null) {
            generateFr15FairChoreDemoData();
        } else {
            resetFr15FairChoreDemoData();
        }
    }

    private void generatePresentationDemoData() {
        LOGGER.info("Generating WGMS presentation demo data for UC-01 to UC-05.");

        DemoUsers users = createUsers();
        Household household = createHousehold(users.cemil());
        DemoRoommates roommates = createRoommates(household, users);

        List<CleaningSchedule> schedules = createCleaningSchedules(household, users);
        createTasks(household, users, schedules);
        createExpenses(household, roommates);
        createShoppingItems(household, users);

        LOGGER.info("Demo data generated successfully.");
        LOGGER.info("Demo login accounts were generated. Use documented demo credentials outside application logs.");
        LOGGER.info("Active WG users:");
        LOGGER.info("cemil.demo@wgms.local     Main Tenant");
        LOGGER.info("dzovani.demo@wgms.local   Roommate");
        LOGGER.info("joan.demo@wgms.local      Roommate");
        LOGGER.info("daniel.demo@wgms.local    Roommate");
        LOGGER.info("taras.demo@wgms.local     Roommate");
        LOGGER.info("andrei.demo@wgms.local    Roommate");
        LOGGER.info("Users without WG for live invitation demo:");
        LOGGER.info("invite.link.demo@wgms.local    use for invitation-link demo");
        LOGGER.info("invite.email.demo@wgms.local   use for direct email invitation demo");
    }

    private void generateFr15FairChoreDemoData() {
        LOGGER.info("Generating FR-15 fair chore balancing demo data.");

        Fr15Users users = createFr15Users();
        Household household = createFr15Household("Fair Chore Demo WG", "FR-15 Demo Street 15, 1040 Wien", users.mainTenant());
        createFr15Roommates(household, users);
        createFr15FairnessTasks(household, users);
        createFr15NoiseHousehold();

        LOGGER.info("FR-15 demo data generated successfully.");
        LOGGER.info("FR-15 login account: maintenant.fr15@example.com");
        LOGGER.info("FR-15 expected recommendation in the task form: Light Worker");
    }

    private void resetFr15FairChoreDemoData() {
        ApplicationUser mainTenant = userRepository.findUserByEmail(FR15_MAIN_TENANT_EMAIL);
        if (mainTenant == null) {
            generateFr15FairChoreDemoData();
            return;
        }

        Roommate mainTenantRoommate = roommateRepository.findByUserIdAndActiveTrue(mainTenant.getId()).orElse(null);
        if (mainTenantRoommate == null) {
            generateFr15FairChoreDemoData();
            return;
        }

        Household household = mainTenantRoommate.getHousehold();
        Fr15Users users = createFr15Users();
        ensureFr15Roommate(users.mainTenant(), household, HouseholdRole.MAIN_TENANT, LocalDate.now().minusMonths(6));
        ensureFr15Roommate(users.heavyWorker(), household, HouseholdRole.ROOMMATE, LocalDate.now().minusMonths(5));
        ensureFr15Roommate(users.mediumWorker(), household, HouseholdRole.ROOMMATE, LocalDate.now().minusMonths(4));
        ensureFr15Roommate(users.lightWorker(), household, HouseholdRole.ROOMMATE, LocalDate.now().minusMonths(3));

        entityManager.flush();
        entityManager.createQuery("delete from Task task where task.household.id = :householdId")
            .setParameter("householdId", household.getId())
            .executeUpdate();
        createFr15FairnessTasks(household, users);

        LOGGER.info("FR-15 fair chore demo data already exists. Reset deterministic task seed.");
        LOGGER.info("FR-15 expected recommendation in the task form: Light Worker");
    }

    // -------------------------------------------------------------------------
    // UC-01: Registration, login, WG initialization, roles, invitation demo users
    // -------------------------------------------------------------------------

    private DemoUsers createUsers() {
        ApplicationUser cemil = createUser("cemil.demo@wgms.local", "Cemil", "Aslan", false);
        ApplicationUser dzovani = createUser("dzovani.demo@wgms.local", "Dzovani", "Koller", false);
        ApplicationUser joan = createUser("joan.demo@wgms.local", "Joan", "de la Fuente Camps", false);
        ApplicationUser daniel = createUser("daniel.demo@wgms.local", "Daniel", "Petriceanu", false);
        ApplicationUser taras = createUser("taras.demo@wgms.local", "Taras", "Trishch", false);
        ApplicationUser andrei = createUser("andrei.demo@wgms.local", "Andrei", "Ciuta", false);


        ApplicationUser inviteLinkUser = createUser("new.roommate1.demo@wgms.local", "Some", "Guy", false);
        ApplicationUser inviteEmailUser = createUser("new.roommate2.demo@wgms.local", "Other", "Guy", false);

        return new DemoUsers(
            cemil,
            dzovani,
            joan,
            daniel,
            taras,
            andrei,
            inviteLinkUser,
            inviteEmailUser
        );
    }

    private ApplicationUser createUser(String email, String firstName, String lastName, boolean admin) {
        return createUser(email, firstName, lastName, admin, DEMO_PASSWORD);
    }

    private ApplicationUser createUser(String email, String firstName, String lastName, boolean admin, String rawPassword) {
        ApplicationUser existingUser = userRepository.findUserByEmail(email);
        if (existingUser != null) {
            return existingUser;
        }

        ApplicationUser user = new ApplicationUser(
            email,
            passwordEncoder.encode(rawPassword),
            firstName,
            lastName,
            admin
        );
        return userRepository.save(user);
    }

    private Household createHousehold(ApplicationUser createdBy) {
        Household household = new Household();
        household.setName("Demo WG");
        household.setAddress("A Street 23, 1040 Wien");
        household.setCreatedBy(createdBy);
        household.setCreatedAt(LocalDateTime.now().minusMonths(5));
        return householdRepository.save(household);
    }

    private DemoRoommates createRoommates(Household household, DemoUsers users) {
        Roommate cemil = createRoommate(users.cemil(), household, HouseholdRole.MAIN_TENANT, LocalDate.now().minusMonths(18));
        Roommate dzovani = createRoommate(users.dzovani(), household, HouseholdRole.ROOMMATE, LocalDate.now().minusMonths(12));
        Roommate joan = createRoommate(users.joan(), household, HouseholdRole.ROOMMATE, LocalDate.now().minusMonths(10));
        Roommate daniel = createRoommate(users.daniel(), household, HouseholdRole.ROOMMATE, LocalDate.now().minusMonths(8));
        Roommate taras = createRoommate(users.taras(), household, HouseholdRole.ROOMMATE, LocalDate.now().minusMonths(5));
        Roommate andrei = createRoommate(users.andrei(), household, HouseholdRole.ROOMMATE, LocalDate.now().minusMonths(3));

        return new DemoRoommates(cemil, dzovani, joan, daniel, taras, andrei);
    }

    private Roommate createRoommate(ApplicationUser user, Household household, HouseholdRole role, LocalDate joinedDate) {
        Roommate roommate = new Roommate();
        roommate.setUser(user);
        roommate.setHousehold(household);
        roommate.setRole(role);
        roommate.setActive(true);
        roommate.setJoinedAt(joinedDate.atTime(10, 0));
        return roommateRepository.save(roommate);
    }

    // -------------------------------------------------------------------------
    // UC-02: Cleaning schedules and task management
    // -------------------------------------------------------------------------

    private List<CleaningSchedule> createCleaningSchedules(Household household, DemoUsers users) {
        LocalDate today = LocalDate.now();

        return List.of(
            createSchedule(
                household,
                "Kitchen cleaning rotation",
                "Wipe counters, clean stove, empty dishwasher, mop kitchen floor.",
                CleaningFrequency.WEEKLY,
                today.minusWeeks(8),
                today.plusDays(1),
                users.cemil(),
                users.dzovani(),
                true
            ),
            createSchedule(
                household,
                "Bathroom deep clean",
                "Clean sink, toilet, shower, mirror, and replace hand towels.",
                CleaningFrequency.WEEKLY,
                today.minusWeeks(7),
                today.minusDays(1),
                users.cemil(),
                users.joan(),
                true
            ),
            createSchedule(
                household,
                "Trash and recycling duty",
                "Take out general waste, paper, plastic, and glass recycling.",
                CleaningFrequency.BIWEEKLY,
                today.minusWeeks(6),
                today.plusDays(3),
                users.cemil(),
                users.daniel(),
                true
            ),
            createSchedule(
                household,
                "Hallway vacuuming",
                "Vacuum hallway and shared entrance area.",
                CleaningFrequency.WEEKLY,
                today.minusWeeks(5),
                today.plusDays(5),
                users.cemil(),
                users.taras(),
                true
            ),
            createSchedule(
                household,
                "Monthly fridge check",
                "Remove expired food and wipe fridge shelves.",
                CleaningFrequency.MONTHLY,
                today.minusMonths(3),
                today.plusDays(8),
                users.cemil(),
                users.andrei(),
                true
            ),
            createSchedule(
                household,
                "Daily kitchen reset",
                "Short evening reset: dishes, surfaces, bins.",
                CleaningFrequency.DAILY,
                today.minusWeeks(3),
                today,
                users.cemil(),
                users.cemil(),
                true
            )
        );
    }

    private CleaningSchedule createSchedule(Household household,
                                            String title,
                                            String description,
                                            CleaningFrequency frequency,
                                            LocalDate startDate,
                                            LocalDate nextDueDate,
                                            ApplicationUser createdBy,
                                            ApplicationUser assignedTo,
                                            boolean active) {
        CleaningSchedule schedule = new CleaningSchedule();
        schedule.setHousehold(household);
        schedule.setTitle(title);
        schedule.setDescription(description);
        schedule.setFrequency(frequency);
        schedule.setStartDate(startDate);
        schedule.setNextDueDate(nextDueDate);
        schedule.setActive(active);
        schedule.setCreatedBy(createdBy);
        schedule.setAssignedTo(assignedTo);
        schedule.setCreatedAt(LocalDateTime.now().minusDays(35));
        schedule.setUpdatedAt(LocalDateTime.now().minusDays(1));
        return cleaningScheduleRepository.save(schedule);
    }

    private void createTasks(Household household, DemoUsers users, List<CleaningSchedule> schedules) {
        LocalDate today = LocalDate.now();

        createTask(household, "Clean kitchen after pasta night", "Counters, stove, sink, and floor.", today.minusDays(2),
            TaskStatus.OPEN, users.cemil(), users.dzovani(), schedules.get(0), 22);
        createTask(household, "Bathroom deep clean", "Shower glass and toilet need special attention.", today.minusDays(1),
            TaskStatus.IN_PROGRESS, users.cemil(), users.joan(), schedules.get(1), 21);
        createTask(household, "Take out recycling", "Plastic and paper bins are full.", today,
            TaskStatus.OPEN, users.cemil(), users.daniel(), schedules.get(2), 20);
        createTask(household, "Vacuum hallway", "Especially near the entrance mat.", today.plusDays(1),
            TaskStatus.OPEN, users.cemil(), users.taras(), schedules.get(3), 19);
        createTask(household, "Fridge check", "Remove expired leftovers.", today.plusDays(3),
            TaskStatus.OPEN, users.cemil(), users.andrei(), schedules.get(4), 18);
        createTask(household, "Daily kitchen reset", "Fast evening cleanup before tomorrow.", today,
            TaskStatus.IN_PROGRESS, users.cemil(), users.cemil(), schedules.get(5), 17);

        createTask(household, "Clean microwave", "Food splashes inside microwave.", today.minusDays(8),
            TaskStatus.DONE, users.dzovani(), users.dzovani(), null, 28);
        createTask(household, "Restock toilet paper", "Move bulk pack from storage to bathroom.", today.minusDays(7),
            TaskStatus.DONE, users.daniel(), users.daniel(), null, 27);
        createTask(household, "Water balcony plants", "Water herbs and tomatoes.", today.minusDays(6),
            TaskStatus.DONE, users.joan(), users.joan(), null, 26);
        createTask(household, "Clean coffee machine", "Descale and rinse water tank.", today.minusDays(5),
            TaskStatus.DONE, users.cemil(), users.taras(), null, 25);
        createTask(household, "Sort shared mail", "Separate utility letters from advertisements.", today.minusDays(4),
            TaskStatus.DONE, users.cemil(), users.andrei(), null, 24);

        createTask(household, "Buy replacement sponge", "Kitchen sponge is worn out.", today.plusDays(2),
            TaskStatus.OPEN, users.joan(), users.dzovani(), null, 16);
        createTask(household, "Clean windows in living room", "Only inside windows needed.", today.plusDays(4),
            TaskStatus.OPEN, users.cemil(), users.daniel(), null, 15);
        createTask(household, "Organize cleaning supplies shelf", "Group detergents and sponges.", today.plusDays(6),
            TaskStatus.OPEN, users.taras(), users.taras(), null, 14);
        createTask(household, "Check smoke detector batteries", "Press test button in hallway and kitchen.", today.plusDays(7),
            TaskStatus.OPEN, users.andrei(), users.andrei(), null, 13);
        createTask(household, "Wipe dining table", "After group study evening.", today.plusDays(1),
            TaskStatus.OPEN, users.daniel(), users.joan(), null, 12);

        createTask(household, "Old party cleanup", "Cancelled because party did not happen.", today.minusDays(12),
            TaskStatus.CANCELLED, users.cemil(), users.dzovani(), null, 31);
        createTask(household, "Move sofa for vacuuming", "Cancelled due to exam week.", today.minusDays(10),
            TaskStatus.CANCELLED, users.joan(), users.daniel(), null, 30);

        createTask(household, "Kitchen floor weekly follow-up", "Generated from recurring kitchen schedule.", today.plusDays(7),
            TaskStatus.OPEN, users.cemil(), users.joan(), schedules.get(0), 11);
        createTask(household, "Bathroom towels wash", "Wash shared bathroom towels.", today.plusDays(8),
            TaskStatus.OPEN, users.cemil(), users.taras(), schedules.get(1), 10);
        createTask(household, "Glass recycling run", "Bring glass bottles to container.", today.plusDays(9),
            TaskStatus.OPEN, users.daniel(), users.daniel(), schedules.get(2), 9);
        createTask(household, "Entrance mat cleaning", "Shake out and vacuum entrance mat.", today.plusDays(10),
            TaskStatus.OPEN, users.taras(), users.andrei(), schedules.get(3), 8);
        createTask(household, "Freezer drawer cleanup", "Remove old ice and sort shared frozen food.", today.plusDays(12),
            TaskStatus.OPEN, users.andrei(), users.cemil(), schedules.get(4), 7);
    }

    private Task createTask(Household household,
                            String title,
                            String description,
                            LocalDate dueDate,
                            TaskStatus status,
                            ApplicationUser createdBy,
                            ApplicationUser assignedTo,
                            CleaningSchedule cleaningSchedule,
                            int daysAgoCreated) {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(daysAgoCreated);

        Task task = new Task();
        task.setHousehold(household);
        task.setTitle(title);
        task.setDescription(description);
        task.setDueDate(dueDate);
        task.setStatus(status);
        task.setCreatedBy(createdBy);
        task.setAssignedTo(assignedTo);
        task.setCleaningSchedule(cleaningSchedule);
        task.setCreatedAt(createdAt);
        task.setUpdatedAt(createdAt.plusHours(4));
        return taskRepository.save(task);
    }

    // -------------------------------------------------------------------------
    // FR-15: Deterministic fair chore balancing demo data
    // -------------------------------------------------------------------------

    private Fr15Users createFr15Users() {
        ApplicationUser mainTenant = createUser(FR15_MAIN_TENANT_EMAIL, "Anna", "Fair", false, FR15_PASSWORD);
        ApplicationUser heavyWorker = createUser("roommate.heavy@example.com", "Heavy", "Worker", false, FR15_PASSWORD);
        ApplicationUser mediumWorker = createUser("roommate.medium@example.com", "Medium", "Worker", false, FR15_PASSWORD);
        ApplicationUser lightWorker = createUser("roommate.light@example.com", "Light", "Worker", false, FR15_PASSWORD);

        return new Fr15Users(mainTenant, heavyWorker, mediumWorker, lightWorker);
    }

    private Household createFr15Household(String name, String address, ApplicationUser createdBy) {
        Household household = new Household();
        household.setName(name);
        household.setAddress(address);
        household.setCreatedBy(createdBy);
        household.setCreatedAt(LocalDateTime.now().minusMonths(2));
        return householdRepository.save(household);
    }

    private void createFr15Roommates(Household household, Fr15Users users) {
        createRoommate(users.mainTenant(), household, HouseholdRole.MAIN_TENANT, LocalDate.now().minusMonths(6));
        createRoommate(users.heavyWorker(), household, HouseholdRole.ROOMMATE, LocalDate.now().minusMonths(5));
        createRoommate(users.mediumWorker(), household, HouseholdRole.ROOMMATE, LocalDate.now().minusMonths(4));
        createRoommate(users.lightWorker(), household, HouseholdRole.ROOMMATE, LocalDate.now().minusMonths(3));
    }

    private Roommate ensureFr15Roommate(ApplicationUser user, Household household, HouseholdRole role, LocalDate joinedDate) {
        return roommateRepository.findByUserIdAndHouseholdId(user.getId(), household.getId())
            .map(existingRoommate -> {
                existingRoommate.setRole(role);
                existingRoommate.setActive(true);
                existingRoommate.setJoinedAt(joinedDate.atTime(10, 0));
                return roommateRepository.save(existingRoommate);
            })
            .orElseGet(() -> createRoommate(user, household, role, joinedDate));
    }

    private void createFr15FairnessTasks(Household household, Fr15Users users) {
        LocalDate today = LocalDate.now();

        // Anna Fair is the logged-in main tenant and is also an active roommate, so she needs some burden
        // to keep the manual demo recommendation focused on Light Worker.
        // Expected score: 2 * 1.0 + 1 * 0.75 = 2.75, Bucket A.
        createFr15CompletedTasks(household, users.mainTenant(), users.mainTenant(), "FR-15 Anna completed chore", 2, 5);
        createTask(household, "FR-15 Anna open chore", "Open non-overdue task for the main tenant.",
            today.plusDays(2), TaskStatus.OPEN, users.mainTenant(), users.mainTenant(), null, 2);

        // Heavy Worker: 5 current overdue tasks. This crosses the overdue reminder threshold while
        // keeping Heavy Worker outside the available recommendation group.
        // Expected score: 5 * 2.0 = 10.0, Bucket C.
        createFr15OverdueTasks(household, users.mainTenant(), users.heavyWorker(), "FR-15 Heavy overdue chore", 5, 1);

        // Medium Worker: 3 completed tasks in the 30-day window and 1 overdue task.
        // Expected score: 3 * 1.0 + 1 * 2.0 = 5.0, Bucket C.
        createFr15CompletedTasks(household, users.mainTenant(), users.mediumWorker(), "FR-15 Medium completed chore", 3, 7);
        createTask(household, "FR-15 Medium overdue chore", "Overdue task for the medium-burden roommate.",
            today.minusDays(2), TaskStatus.OPEN, users.mainTenant(), users.mediumWorker(), null, 4);

        // Light Worker: 1 completed task in the 30-day window and no current open/overdue tasks.
        // Expected score: 1 * 1.0 = 1.0, Bucket A. Light Worker is the exact minimum-score Bucket A candidate.
        createFr15CompletedTasks(household, users.mainTenant(), users.lightWorker(), "FR-15 Light completed chore", 1, 10);

        Task oldCompleted = createTask(household, "FR-15 Light old completed chore",
            "Completed outside the default 30-day history window; must not affect the default score.",
            today.minusDays(45), TaskStatus.DONE, users.mainTenant(), users.lightWorker(), null, 45);
        overrideTaskUpdatedAt(oldCompleted, LocalDateTime.now().minusDays(45));
    }

    private void createFr15CompletedTasks(Household household,
                                          ApplicationUser createdBy,
                                          ApplicationUser assignedTo,
                                          String titlePrefix,
                                          int count,
                                          int firstDaysAgo) {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < count; i++) {
            int daysAgo = firstDaysAgo + i;
            createTask(household, titlePrefix + " " + (i + 1), "Completed FR-15 scoring task.",
                today.minusDays(daysAgo), TaskStatus.DONE, createdBy, assignedTo, null, daysAgo);
        }
    }

    private void createFr15OverdueTasks(Household household,
                                        ApplicationUser createdBy,
                                        ApplicationUser assignedTo,
                                        String titlePrefix,
                                        int count,
                                        int firstDaysAgo) {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < count; i++) {
            int daysAgo = firstDaysAgo + i;
            createTask(household, titlePrefix + " " + (i + 1), "Overdue FR-15 reminder threshold task.",
                today.minusDays(daysAgo), i % 2 == 0 ? TaskStatus.OPEN : TaskStatus.IN_PROGRESS,
                createdBy, assignedTo, null, daysAgo);
        }
    }

    private void createFr15NoiseHousehold() {
        ApplicationUser noiseMain = createUser("fr15.noise.main@example.com", "Noise", "Tenant", false, FR15_PASSWORD);
        ApplicationUser noiseRoommate = createUser("fr15.noise.roommate@example.com", "Noise", "Roommate", false, FR15_PASSWORD);
        Household noiseHousehold = createFr15Household("FR-15 Noise WG", "Noise Street 1, 1040 Wien", noiseMain);
        createRoommate(noiseMain, noiseHousehold, HouseholdRole.MAIN_TENANT, LocalDate.now().minusMonths(3));
        createRoommate(noiseRoommate, noiseHousehold, HouseholdRole.ROOMMATE, LocalDate.now().minusMonths(2));

        LocalDate today = LocalDate.now();
        createTask(noiseHousehold, "FR-15 Noise completed chore", "Other-household completed task; must be ignored.",
            today.minusDays(5), TaskStatus.DONE, noiseMain, noiseRoommate, null, 5);
        createTask(noiseHousehold, "FR-15 Noise overdue chore", "Other-household overdue task; must be ignored.",
            today.minusDays(1), TaskStatus.OPEN, noiseMain, noiseRoommate, null, 1);
        createTask(noiseHousehold, "FR-15 Noise undone chore",
            "Represents an undone completion in the current model: the current status is OPEN, not DONE.",
            today.plusDays(5), TaskStatus.OPEN, noiseMain, noiseRoommate, null, 2);
    }

    private void overrideTaskUpdatedAt(Task task, LocalDateTime updatedAt) {
        entityManager.flush();
        entityManager.createQuery("update Task task set task.updatedAt = :updatedAt where task.id = :id")
            .setParameter("updatedAt", updatedAt)
            .setParameter("id", task.getId())
            .executeUpdate();
    }

    // -------------------------------------------------------------------------
    // UC-03: Shared expenses, uneven splits, settlement overview
    // -------------------------------------------------------------------------

    private void createExpenses(Household household, DemoRoommates roommates) {
        LocalDate today = LocalDate.now();

        createExpense(
            household,
            "Rent May",
            "2160.00",
            today.minusDays(35),
            roommates.cemil(),
            roommates.cemil(),
            List.of(
                share(roommates.cemil(), "360.00"),
                share(roommates.dzovani(), "360.00"),
                share(roommates.joan(), "360.00"),
                share(roommates.daniel(), "360.00"),
                share(roommates.taras(), "360.00"),
                share(roommates.andrei(), "360.00")
            )
        );

        createExpense(
            household,
            "Electricity April",
            "151.20",
            today.minusDays(32),
            roommates.cemil(),
            roommates.cemil(),
            List.of(
                share(roommates.cemil(), "25.20"),
                share(roommates.dzovani(), "25.20"),
                share(roommates.joan(), "25.20"),
                share(roommates.daniel(), "25.20"),
                share(roommates.taras(), "25.20"),
                share(roommates.andrei(), "25.20")
            )
        );

        createExpense(
            household,
            "Internet May",
            "39.90",
            today.minusDays(31),
            roommates.dzovani(),
            roommates.dzovani(),
            List.of(
                share(roommates.cemil(), "6.65"),
                share(roommates.dzovani(), "6.65"),
                share(roommates.joan(), "6.65"),
                share(roommates.daniel(), "6.65"),
                share(roommates.taras(), "6.65"),
                share(roommates.andrei(), "6.65")
            )
        );

        createExpense(
            household,
            "Hofer groceries",
            "92.40",
            today.minusDays(29),
            roommates.joan(),
            roommates.joan(),
            List.of(
                share(roommates.cemil(), "12.40"),
                share(roommates.dzovani(), "20.00"),
                share(roommates.joan(), "18.00"),
                share(roommates.daniel(), "14.00"),
                share(roommates.taras(), "16.00"),
                share(roommates.andrei(), "12.00")
            )
        );

        createExpense(
            household,
            "Cleaning supplies DM",
            "34.80",
            today.minusDays(27),
            roommates.daniel(),
            roommates.daniel(),
            List.of(
                share(roommates.cemil(), "5.80"),
                share(roommates.dzovani(), "5.80"),
                share(roommates.joan(), "5.80"),
                share(roommates.daniel(), "5.80"),
                share(roommates.taras(), "5.80"),
                share(roommates.andrei(), "5.80")
            )
        );

        createExpense(
            household,
            "Shared dinner ingredients",
            "48.60",
            today.minusDays(24),
            roommates.dzovani(),
            roommates.dzovani(),
            List.of(
                share(roommates.cemil(), "9.72"),
                share(roommates.dzovani(), "9.72"),
                share(roommates.joan(), "9.72"),
                share(roommates.taras(), "9.72"),
                share(roommates.andrei(), "9.72")
            )
        );

        createExpense(
            household,
            "Bathroom shelf replacement",
            "31.20",
            today.minusDays(22),
            roommates.taras(),
            roommates.taras(),
            List.of(
                share(roommates.cemil(), "7.80"),
                share(roommates.joan(), "7.80"),
                share(roommates.daniel(), "7.80"),
                share(roommates.taras(), "7.80")
            )
        );

        createExpense(
            household,
            "Rent June",
            "2160.00",
            today.minusDays(18),
            roommates.cemil(),
            roommates.cemil(),
            List.of(
                share(roommates.cemil(), "360.00"),
                share(roommates.dzovani(), "360.00"),
                share(roommates.joan(), "360.00"),
                share(roommates.daniel(), "360.00"),
                share(roommates.taras(), "360.00"),
                share(roommates.andrei(), "360.00")
            )
        );

        createExpense(
            household,
            "Electricity May",
            "143.70",
            today.minusDays(17),
            roommates.cemil(),
            roommates.cemil(),
            List.of(
                share(roommates.cemil(), "23.95"),
                share(roommates.dzovani(), "23.95"),
                share(roommates.joan(), "23.95"),
                share(roommates.daniel(), "23.95"),
                share(roommates.taras(), "23.95"),
                share(roommates.andrei(), "23.95")
            )
        );

        createExpense(
            household,
            "Billa groceries",
            "81.30",
            today.minusDays(15),
            roommates.taras(),
            roommates.taras(),
            List.of(
                share(roommates.cemil(), "11.30"),
                share(roommates.dzovani(), "10.00"),
                share(roommates.joan(), "17.00"),
                share(roommates.daniel(), "13.00"),
                share(roommates.taras(), "18.00"),
                share(roommates.andrei(), "12.00")
            )
        );

        createExpense(
            household,
            "Dishwasher tabs",
            "13.49",
            today.minusDays(13),
            roommates.joan(),
            roommates.joan(),
            List.of(
                share(roommates.cemil(), "2.25"),
                share(roommates.dzovani(), "2.25"),
                share(roommates.joan(), "2.24"),
                share(roommates.daniel(), "2.25"),
                share(roommates.taras(), "2.25"),
                share(roommates.andrei(), "2.25")
            )
        );

        createExpense(
            household,
            "Coffee beans shared",
            "21.80",
            today.minusDays(11),
            roommates.taras(),
            roommates.taras(),
            List.of(
                share(roommates.cemil(), "5.45"),
                share(roommates.dzovani(), "5.45"),
                share(roommates.joan(), "5.45"),
                share(roommates.taras(), "5.45")
            )
        );

        createExpense(
            household,
            "Light bulbs hallway",
            "11.50",
            today.minusDays(9),
            roommates.cemil(),
            roommates.cemil(),
            List.of(
                share(roommates.cemil(), "1.92"),
                share(roommates.dzovani(), "1.92"),
                share(roommates.joan(), "1.92"),
                share(roommates.daniel(), "1.92"),
                share(roommates.taras(), "1.91"),
                share(roommates.andrei(), "1.91")
            )
        );

        createExpense(
            household,
            "Kitchen paper and sponges",
            "9.75",
            today.minusDays(7),
            roommates.daniel(),
            roommates.daniel(),
            List.of(
                share(roommates.cemil(), "1.63"),
                share(roommates.dzovani(), "1.63"),
                share(roommates.joan(), "1.63"),
                share(roommates.daniel(), "1.62"),
                share(roommates.taras(), "1.62"),
                share(roommates.andrei(), "1.62")
            )
        );

        createExpense(
            household,
            "Shared breakfast groceries",
            "37.25",
            today.minusDays(5),
            roommates.dzovani(),
            roommates.dzovani(),
            List.of(
                share(roommates.cemil(), "6.20"),
                share(roommates.dzovani(), "6.20"),
                share(roommates.joan(), "6.20"),
                share(roommates.daniel(), "6.20"),
                share(roommates.taras(), "6.20"),
                share(roommates.andrei(), "6.25")
            )
        );

        createExpense(
            household,
            "Operating costs adjustment",
            "96.00",
            today.minusDays(4),
            roommates.cemil(),
            roommates.cemil(),
            List.of(
                share(roommates.cemil(), "16.00"),
                share(roommates.dzovani(), "16.00"),
                share(roommates.joan(), "16.00"),
                share(roommates.daniel(), "16.00"),
                share(roommates.taras(), "16.00"),
                share(roommates.andrei(), "16.00")
            )
        );

        createExpense(
            household,
            "Late-night pizza",
            "54.00",
            today.minusDays(3),
            roommates.andrei(),
            roommates.andrei(),
            List.of(
                share(roommates.cemil(), "13.50"),
                share(roommates.dzovani(), "13.50"),
                share(roommates.daniel(), "13.50"),
                share(roommates.andrei(), "13.50")
            )
        );

        createExpense(
            household,
            "Shared study snacks",
            "24.60",
            today.minusDays(2),
            roommates.daniel(),
            roommates.daniel(),
            List.of(
                share(roommates.joan(), "8.20"),
                share(roommates.taras(), "8.20"),
                share(roommates.daniel(), "8.20")
            )
        );

        createExpense(
            household,
            "Replacement mop",
            "17.90",
            today.minusDays(1),
            roommates.joan(),
            roommates.joan(),
            List.of(
                share(roommates.cemil(), "2.98"),
                share(roommates.dzovani(), "2.98"),
                share(roommates.joan(), "2.99"),
                share(roommates.daniel(), "2.98"),
                share(roommates.taras(), "2.98"),
                share(roommates.andrei(), "2.99")
            )
        );
    }

    private ExpenseShare share(Roommate roommate, String amount) {
        return new ExpenseShare(roommate, new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP));
    }

    private void createExpense(Household household,
                               String description,
                               String amount,
                               LocalDate date,
                               Roommate paidBy,
                               Roommate createdBy,
                               List<ExpenseShare> shares) {
        BigDecimal total = new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal splitSum = shares.stream()
            .map(ExpenseShare::amountOwed)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);

        if (total.compareTo(splitSum) != 0) {
            throw new IllegalStateException(
                "Demo expense '" + description + "' total " + total + " does not match split sum " + splitSum);
        }

        Expense expense = new Expense();
        expense.setHousehold(household);
        expense.setDescription(description);
        expense.setTotalAmount(total);
        expense.setDate(date);
        expense.setPaidBy(paidBy);
        expense.setCreatedBy(createdBy);
        expense.setCreatedAt(date.atTime(12, 0));

        Expense savedExpense = expenseRepository.save(expense);

        for (ExpenseShare share : shares) {
            ExpenseSplit split = new ExpenseSplit();
            split.setExpense(savedExpense);
            split.setRoommate(share.roommate());
            split.setAmountOwed(share.amountOwed());
            expenseSplitRepository.save(split);
        }
    }

    // -------------------------------------------------------------------------
    // UC-04: Shopping list, targeted requests, aggregated list, shopping history
    // -------------------------------------------------------------------------

    private void createShoppingItems(Household household, DemoUsers users) {
        createShoppingItem(household, "Milk", null, bd("2"), QuantityUnit.LITER, "For coffee and breakfast", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.SHARED, users.cemil(), null, 22);
        createShoppingItem(household, " milk ", null, bd("2"), QuantityUnit.LITER, "Second request to demonstrate aggregation", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.SHARED, users.dzovani(), null, 21);
        createShoppingItem(household, "Pasta", null, bd("3"), QuantityUnit.PACK, "For shared dinner", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.SHARED, users.joan(), null, 20);
        createShoppingItem(household, "Tomatoes", null, bd("1"), QuantityUnit.KILOGRAM, "Dzovani passes the supermarket today", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.TARGETED, users.cemil(), users.dzovani(), 19);
        createShoppingItem(household, "Coffee beans", null, bd("500"), QuantityUnit.GRAM, "Personal coffee stash", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.PERSONAL, users.cemil(), null, 18);
        createShoppingItem(household, "Toilet paper", null, bd("12"), QuantityUnit.PIECE, "Bulk pack already bought", ShoppingItemStatus.BOUGHT,
            ShoppingItemVisibility.SHARED, users.daniel(), null, 17);
        createShoppingItem(household, "Dish soap", null, bd("1"), QuantityUnit.PIECE, "Cancelled because Joan found one", ShoppingItemStatus.CANCELLED,
            ShoppingItemVisibility.SHARED, users.joan(), null, 16);
        createShoppingItem(household, "Oat milk", null, bd("2"), QuantityUnit.PIECE, "For Joan", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.TARGETED, users.taras(), users.joan(), 15);
        createShoppingItem(household, "Rice", null, bd("2"), QuantityUnit.KILOGRAM, "Shared pantry restock", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.SHARED, users.dzovani(), null, 14);
        createShoppingItem(household, "Rice", null, bd("2"), QuantityUnit.KILOGRAM, "Duplicate request to show aggregation count", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.SHARED, users.daniel(), null, 13);
        createShoppingItem(household, "Laundry detergent", null, bd("1"), QuantityUnit.PIECE, "Already bought after last wash", ShoppingItemStatus.BOUGHT,
            ShoppingItemVisibility.SHARED, users.joan(), null, 12);
        createShoppingItem(household, "Apples", null, bd("1"), QuantityUnit.PIECE, "For shared fruit bowl", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.SHARED, users.andrei(), null, 11);
        createShoppingItem(household, "Trash bags", 5, bd("3"), QuantityUnit.LITER, "Kitchen bin size", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.SHARED, users.taras(), null, 10);
        createShoppingItem(household, "Toothpaste", null, bd("1"), QuantityUnit.PIECE, "Personal item example", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.PERSONAL, users.dzovani(), null, 9);
        createShoppingItem(household, "Kitchen sponges", null, bd("4"), QuantityUnit.PACK, "Bought with cleaning supplies", ShoppingItemStatus.BOUGHT,
            ShoppingItemVisibility.SHARED, users.daniel(), null, 8);
        createShoppingItem(household, "Olive oil", null, bd("1"), QuantityUnit.PIECE, "For shared cooking", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.SHARED, users.joan(), null, 7);
        createShoppingItem(household, "Salt", null, bd("1"), QuantityUnit.PACK, "Cancelled duplicate", ShoppingItemStatus.CANCELLED,
            ShoppingItemVisibility.SHARED, users.dzovani(), null, 6);
        createShoppingItem(household, "Bananas", null, bd("6"), QuantityUnit.PIECE, "Breakfast supply", ShoppingItemStatus.BOUGHT,
            ShoppingItemVisibility.SHARED, users.taras(), null, 5);
        createShoppingItem(household, "Paper towels", null, bd("2"), QuantityUnit.PIECE, "Kitchen cleanup", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.SHARED, users.cemil(), null, 4);
        createShoppingItem(household, "Mozzarella", null, bd("2"), QuantityUnit.PACK, "Targeted request for Dzovani", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.TARGETED, users.joan(), users.dzovani(), 3);
        createShoppingItem(household, "Cereal", null, bd("1"), QuantityUnit.PACK, "For breakfast shelf", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.SHARED, users.andrei(), null, 2);
        createShoppingItem(household, "Cereal", null, bd("1"), QuantityUnit.PACK, "Aggregation example", ShoppingItemStatus.NEEDED,
            ShoppingItemVisibility.SHARED, users.taras(), null, 1);
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private void createShoppingItem(Household household,
                                    String name,
                                    Integer quantityCount,
                                    BigDecimal quantityAmount,
                                    QuantityUnit quantityUnit,
                                    String notes,
                                    ShoppingItemStatus status,
                                    ShoppingItemVisibility visibility,
                                    ApplicationUser createdBy,
                                    ApplicationUser targetUser,
                                    int daysAgoCreated) {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(daysAgoCreated);

        ShoppingItem item = new ShoppingItem();
        item.setHousehold(household);
        item.setName(name);
        item.setQuantityCount(quantityCount);
        item.setQuantityAmount(quantityAmount);
        item.setQuantityUnit(quantityUnit);
        item.setNotes(notes);
        item.setStatus(status);
        item.setVisibility(visibility);
        item.setCreatedBy(createdBy);
        item.setTargetUser(targetUser);
        item.setCreatedAt(createdAt);
        item.setUpdatedAt(createdAt.plusHours(2));

        shoppingItemRepository.save(item);
    }

    // -------------------------------------------------------------------------
    // Records
    // -------------------------------------------------------------------------

    private record DemoUsers(
        ApplicationUser cemil,
        ApplicationUser dzovani,
        ApplicationUser joan,
        ApplicationUser daniel,
        ApplicationUser taras,
        ApplicationUser andrei,
        ApplicationUser inviteLinkUser,
        ApplicationUser inviteEmailUser
    ) {}

    private record DemoRoommates(
        Roommate cemil,
        Roommate dzovani,
        Roommate joan,
        Roommate daniel,
        Roommate taras,
        Roommate andrei
    ) {}

    private record Fr15Users(
        ApplicationUser mainTenant,
        ApplicationUser heavyWorker,
        ApplicationUser mediumWorker,
        ApplicationUser lightWorker
    ) {}

    private record ExpenseShare(Roommate roommate, BigDecimal amountOwed) {}
}
