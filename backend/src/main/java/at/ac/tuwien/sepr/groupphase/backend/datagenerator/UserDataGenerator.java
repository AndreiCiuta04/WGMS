package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;

@Profile("generateData")
@Component
public class UserDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String TEST_USER_EMAIL = "admin@email.com";
    private static final String TEST_USER_PASSWORD = "password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataGenerator(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    private void generateUser() {
        if (userRepository.findUserByEmail(TEST_USER_EMAIL) != null) {
            LOGGER.debug("user already generated");
        } else {
            LOGGER.debug("generating user entry for E2E tests");
            ApplicationUser user = new ApplicationUser(
                TEST_USER_EMAIL,
                passwordEncoder.encode(TEST_USER_PASSWORD),
                "Admin",
                "User",
                true
            );
            LOGGER.debug("saving user {}", user);
            userRepository.save(user);
        }
    }
}
