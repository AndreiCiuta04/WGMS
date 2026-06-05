package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserProfileUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Roommate;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoommateRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailService implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final UserRepository userRepository;
    private final RoommateRepository roommateRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;

    @Autowired
    public CustomUserDetailService(
        UserRepository userRepository,
        RoommateRepository roommateRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenizer jwtTokenizer
    ) {
        this.userRepository = userRepository;
        this.roommateRepository = roommateRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenizer = jwtTokenizer;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.debug("Load all user by email");
        try {
            ApplicationUser applicationUser = findApplicationUserByEmail(email);
            if (applicationUser.isAnonymized()) {
                throw new UsernameNotFoundException("User account is anonymized");
            }

            List<GrantedAuthority> grantedAuthorities;
            if (Boolean.TRUE.equals(applicationUser.getAdmin())) {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER");
            } else {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_USER");
            }

            return new User(applicationUser.getEmail(), applicationUser.getPassword(), grantedAuthorities);
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public ApplicationUser findApplicationUserByEmail(String email) {
        LOGGER.debug("Find application user by email");
        ApplicationUser applicationUser = userRepository.findUserByEmail(email);
        if (applicationUser != null) {
            return applicationUser;
        }
        throw new NotFoundException(String.format("Could not find the user with the email address %s", email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile(String email) {
        LOGGER.debug("Get safe profile for authenticated user");
        ApplicationUser user = findApplicationUserByEmail(email);
        if (user.isAnonymized()) {
            throw new NotFoundException("User account is anonymized.");
        }
        return toUserProfileDto(user);
    }

    @Override
    @Transactional
    public UserProfileDto updateCurrentUserProfile(String email, UserProfileUpdateDto userProfileUpdateDto) {
        LOGGER.debug("Update safe profile for authenticated user");
        ApplicationUser user = findApplicationUserByEmail(email);
        if (user.isAnonymized()) {
            throw new NotFoundException("User account is anonymized.");
        }

        String newEmail = userProfileUpdateDto.email().trim();
        if (!user.getEmail().equalsIgnoreCase(newEmail)) {
            ApplicationUser existingUser = userRepository.findUserByEmail(newEmail);
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                throw new ConflictException("User already exists.");
            }
            user.setEmail(newEmail);
        }

        user.setFirstName(userProfileUpdateDto.firstName().trim());
        user.setLastName(userProfileUpdateDto.lastName().trim());
        return toUserProfileDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void anonymizeCurrentUser(String email) {
        LOGGER.debug("Anonymize authenticated user account");
        ApplicationUser user = findApplicationUserByEmail(email);
        if (user.isAnonymized()) {
            return;
        }

        user.setEmail("anonymized-user-" + user.getId() + "@deleted.local");
        user.setFirstName("Deleted");
        user.setLastName("User");
        user.setPassword(passwordEncoder.encode(randomToken()));
        user.setAdmin(false);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setAnonymized(true);

        roommateRepository.findByUserIdAndActiveTrue(user.getId())
            .ifPresent(roommate -> {
                roommate.setActive(false);
                roommateRepository.save(roommate);
            });

        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserProfileDto leaveCurrentHousehold(String email) {
        LOGGER.debug("Leave current household for authenticated user");
        ApplicationUser user = findApplicationUserByEmail(email);
        if (user.isAnonymized()) {
            throw new NotFoundException("User account is anonymized.");
        }

        Roommate roommate = roommateRepository.findByUserIdAndActiveTrue(user.getId())
            .orElseThrow(() -> new NotFoundException("User does not belong to any household."));
        roommate.setActive(false);
        roommateRepository.save(roommate);
        return toUserProfileDto(user);
    }

    private UserProfileDto toUserProfileDto(ApplicationUser user) {
        Optional<Roommate> activeRoommate = roommateRepository.findByUserIdAndActiveTrue(user.getId());

        return new UserProfileDto(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            Boolean.TRUE.equals(user.getAdmin()) ? "ADMIN" : "USER",
            activeRoommate.map(roommate -> roommate.getHousehold().getId()).orElse(null),
            activeRoommate.map(roommate -> roommate.getHousehold().getName()).orElse(null),
            activeRoommate.map(Roommate::getId).orElse(null)
        );
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    @Override
    public String login(UserLoginDto userLoginDto) {
        ApplicationUser applicationUser;
        try {
            applicationUser = findApplicationUserByEmail(userLoginDto.getEmail());
        } catch (NotFoundException e) {
            throw new BadCredentialsException("Username or password is incorrect");
        }

        if (applicationUser.isAnonymized()) {
            throw new BadCredentialsException("Username or password is incorrect");
        }

        if (applicationUser.getAccountLockedUntil() != null) {
            if (applicationUser.getAccountLockedUntil().isAfter(java.time.LocalDateTime.now())) {
                throw new BadCredentialsException("Account is locked due to too many failed attempts. Try again later.");
            } else {
                applicationUser.setAccountLockedUntil(null);
                applicationUser.setFailedLoginAttempts(0);
                userRepository.save(applicationUser);
            }
        }

        if (passwordEncoder.matches(userLoginDto.getPassword(), applicationUser.getPassword())) {
            applicationUser.setFailedLoginAttempts(0);
            applicationUser.setAccountLockedUntil(null);
            userRepository.save(applicationUser);

            List<GrantedAuthority> grantedAuthorities;
            if (Boolean.TRUE.equals(applicationUser.getAdmin())) {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER");
            } else {
                grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_USER");
            }

            List<String> roles = grantedAuthorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
            return jwtTokenizer.getAuthToken(applicationUser.getEmail(), roles);
        } else {
            int attempts = applicationUser.getFailedLoginAttempts() + 1;
            applicationUser.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                applicationUser.setAccountLockedUntil(java.time.LocalDateTime.now().plusMinutes(15));
            }
            userRepository.save(applicationUser);
            throw new BadCredentialsException("Username or password is incorrect");
        }
    }

    @Override
    public String register(at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegistrationDto userRegistrationDto) {
        LOGGER.debug("Register new user with email {}", userRegistrationDto.getEmail());

        // Check if user already exists
        ApplicationUser existingUser = userRepository.findUserByEmail(userRegistrationDto.getEmail());
        if (existingUser != null) {
            throw new at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException("User already exists.");
        }

        ApplicationUser newUser = new ApplicationUser(
            userRegistrationDto.getEmail(),
            passwordEncoder.encode(userRegistrationDto.getPassword()),
            userRegistrationDto.getFirstName(),
            userRegistrationDto.getLastName(),
            false // Admin is false by default for new users
        );

        userRepository.save(newUser);

        // Auto-login the user after registration
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail(userRegistrationDto.getEmail());
        loginDto.setPassword(userRegistrationDto.getPassword()); // Use raw password for login check

        return login(loginDto);
    }
}
