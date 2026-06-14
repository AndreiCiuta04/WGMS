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
import at.ac.tuwien.sepr.groupphase.backend.service.EmailService;
import at.ac.tuwien.sepr.groupphase.backend.service.EmailTemplateRenderer;
import at.ac.tuwien.sepr.groupphase.backend.repository.VerificationCodeRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.NotificationSettingRepository;
import at.ac.tuwien.sepr.groupphase.backend.entity.VerificationCode;
import at.ac.tuwien.sepr.groupphase.backend.entity.NotificationSetting;
import at.ac.tuwien.sepr.groupphase.backend.exception.VerificationRequiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String TERMS_VERSION = "v1.0";
    private static final String PRIVACY_VERSION = "v1.0";
    private final UserRepository userRepository;
    private final RoommateRepository roommateRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;
    private final EmailService emailService;
    private final EmailTemplateRenderer emailTemplateRenderer;
    private final VerificationCodeRepository verificationCodeRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Value("${wgms.mail.verification-enabled:false}")
    private boolean verificationEnabled;

    @Autowired
    public CustomUserDetailService(UserRepository userRepository,
                                   RoommateRepository roommateRepository,
                                   PasswordEncoder passwordEncoder,
                                   JwtTokenizer jwtTokenizer,
                                   EmailService emailService,
                                   EmailTemplateRenderer emailTemplateRenderer,
                                   VerificationCodeRepository verificationCodeRepository,
                                   NotificationSettingRepository notificationSettingRepository) {
        this.userRepository = userRepository;
        this.roommateRepository = roommateRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenizer = jwtTokenizer;
        this.emailService = emailService;
        this.emailTemplateRenderer = emailTemplateRenderer;
        this.verificationCodeRepository = verificationCodeRepository;
        this.notificationSettingRepository = notificationSettingRepository;
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
            // Check verification status
            if (verificationEnabled && !Boolean.TRUE.equals(applicationUser.getVerified())) {
                sendVerificationCode(applicationUser.getEmail());
                throw new BadCredentialsException("VERIFICATION_REQUIRED");
            }

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
        java.time.LocalDateTime acceptedAt = java.time.LocalDateTime.now();
        newUser.setTermsAcceptedAt(acceptedAt);
        newUser.setTermsVersion(TERMS_VERSION);
        newUser.setPrivacyAcknowledgedAt(acceptedAt);
        newUser.setPrivacyVersion(PRIVACY_VERSION);

        if (verificationEnabled) {
            newUser.setVerified(false);
        } else {
            newUser.setVerified(true);
        }

        newUser = userRepository.save(newUser);

        // Save default settings
        NotificationSetting settings = new NotificationSetting(newUser);
        notificationSettingRepository.save(settings);

        if (verificationEnabled) {
            sendVerificationCode(newUser.getEmail());
            throw new VerificationRequiredException("VERIFICATION_REQUIRED");
        }

        // Auto-login the user after registration
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail(userRegistrationDto.getEmail());
        loginDto.setPassword(userRegistrationDto.getPassword()); // Use raw password for login check

        return login(loginDto);
    }

    @Override
    @Transactional
    public String verify(at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserVerifyDto userVerifyDto) {
        LOGGER.debug("Verify user email {}", userVerifyDto.getEmail());

        VerificationCode verificationCode = verificationCodeRepository.findByEmail(userVerifyDto.getEmail())
            .orElseThrow(() -> new at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException("Invalid email or verification code."));

        if (verificationCode.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            verificationCodeRepository.delete(verificationCode);
            throw new at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException("Verification code has expired.");
        }

        if (!verificationCode.getCode().equals(userVerifyDto.getCode())) {
            throw new at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException("Invalid verification code.");
        }

        // Activate user
        ApplicationUser user = findApplicationUserByEmail(userVerifyDto.getEmail());
        user.setVerified(true);
        userRepository.save(user);

        // Delete code
        verificationCodeRepository.delete(verificationCode);

        // Generate token for auto-login
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils.createAuthorityList("ROLE_USER");
        List<String> roles = grantedAuthorities.stream().map(GrantedAuthority::getAuthority).toList();
        return jwtTokenizer.getAuthToken(user.getEmail(), roles);
    }

    private void sendVerificationCode(String email) {
        // Generate random 6-digit code
        String code = String.format("%06d", new java.util.Random().nextInt(1000000));
        java.time.LocalDateTime expiresAt = java.time.LocalDateTime.now().plusMinutes(15);

        // Remove old code if any
        verificationCodeRepository.findByEmail(email).ifPresent(verificationCodeRepository::delete);

        VerificationCode verificationCode = new VerificationCode(email, code, expiresAt);
        verificationCodeRepository.save(verificationCode);

        String subject = "WGMS: Verify your email";
        String body = emailTemplateRenderer.renderVerificationEmail(code);
        emailService.sendEmail(email, subject, body);
    }
}
