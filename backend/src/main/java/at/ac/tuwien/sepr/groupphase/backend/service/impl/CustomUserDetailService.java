package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
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

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class CustomUserDetailService implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;

    @Autowired
    public CustomUserDetailService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenizer jwtTokenizer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenizer = jwtTokenizer;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        LOGGER.debug("Load all user by email");
        try {
            ApplicationUser applicationUser = findApplicationUserByEmail(email);

            List<GrantedAuthority> grantedAuthorities;
            if (applicationUser.getAdmin()) {
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
    public String login(UserLoginDto userLoginDto) {
        ApplicationUser applicationUser;
        try {
            applicationUser = findApplicationUserByEmail(userLoginDto.getEmail());
        } catch (NotFoundException e) {
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
            if (applicationUser.getAdmin()) {
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
