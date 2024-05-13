package io.github.capure.voltcore.service;

import io.github.capure.voltcore.dto.GetUserDto;
import io.github.capure.voltcore.dto.PutUserDto;
import io.github.capure.voltcore.dto.UserLoginDto;
import io.github.capure.voltcore.dto.UserRegisterDto;
import io.github.capure.voltcore.dto.admin.AdminGetUserDto;
import io.github.capure.voltcore.dto.admin.AdminPutUserDto;
import io.github.capure.voltcore.exception.*;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.repository.UserRepository;
import io.github.capure.voltcore.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoltSettingsService voltSettingsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminGetUserDto> adminGetAll(String search, int pageNumber, int pageSize, Boolean enabled) {
        List<User> users;
        if (enabled == null)
            users = userRepository.findAllByUsernameLikeIgnoreCase(search, PageRequest.of(pageNumber, pageSize));
        else
            users = userRepository.findAllByEnabledAndUsernameLikeIgnoreCase(enabled, search, PageRequest.of(pageNumber, pageSize));
        return users.parallelStream().map(AdminGetUserDto::getFromUser).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AdminGetUserDto adminGet(Long id) throws InvalidIdException {
        return AdminGetUserDto.getFromUser(userRepository.findById(id).orElseThrow(InvalidIdException::new));
    }

    public List<GetUserDto> getAll(String search, int pageNumber, int pageSize) {
        List<User> users = userRepository.findAllByEnabledAndUsernameLikeIgnoreCase(true, search, PageRequest.of(pageNumber, pageSize));
        return users.parallelStream().map(GetUserDto::getFromUser).toList();
    }

    public GetUserDto get(Long id) throws InvalidIdException {
        User user = userRepository.findById(id).orElseThrow(InvalidIdException::new);
        return GetUserDto.getFromUser(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void adminUpdate(Long id, AdminPutUserDto data) throws InvalidIdException, FailedUpdateException {
        User user = userRepository.findById(id).orElseThrow(InvalidIdException::new);
        try {
            if (data.getPassword() != null && !data.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(data.getPassword()));
            }
            if (data.getEnabled() != null) user.setEnabled(data.getEnabled());
            if (data.getRole() != null && !data.getRole().isEmpty()) user.setRole(data.getRole());
            if (data.getAvatar() != null && !data.getAvatar().isEmpty()) user.setAvatar(data.getAvatar());
            if (data.getGithub() != null) {
                user.setGithub(!data.getGithub().isEmpty() ? data.getGithub() : null);
            }
            if (data.getSchool() != null) {
                user.setSchool(!data.getSchool().isEmpty() ? data.getSchool() : null);
            }
            log.info("Updating user {}...", id);
            userRepository.save(user);
            log.info("Update successful");
        } catch (Exception e) {
            log.error("Update failed", e);
            throw new FailedUpdateException();
        }
    }

    @PreAuthorize("#id == authentication.principal.id")
    @Transactional("transactionManager")
    public void update(Long id, PutUserDto data) throws InvalidIdException, FailedUpdateException {
        User user = userRepository.findById(id).orElseThrow(InvalidIdException::new);
        if (data.getAvatar() != null && !data.getAvatar().isEmpty()) user.setAvatar(data.getAvatar());
        if (data.getGithub() != null) {
            user.setGithub(!data.getGithub().isEmpty() ? data.getGithub() : null);
        }
        if (data.getSchool() != null) {
            user.setSchool(!data.getSchool().isEmpty() ? data.getSchool() : null);
        }
        log.info("Updating user {}...", id);
        try {
            userRepository.save(user);
            log.info("Update successful");
        } catch (Exception e) {
            log.error("Update failed", e);
            throw new FailedUpdateException();
        }
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Transactional("transactionManager")
    public void delete(Long id) throws FailedDeletionException {
        try {
            int updated = userRepository.updateEnabledByUserId(id, false);
            if (updated == 1) {
                log.info("User disabled successfully");
            } else {
                log.info("Failed updating the user, id might be invalid");
                throw new FailedDeletionException();
            }
        } catch (Exception e) {
            log.error("Unexpected exception", e);
            throw new FailedDeletionException();
        }
    }

    public void register(UserRegisterDto registerData) throws FailedRegistrationException, EmailAlreadyInUseException, UsernameAlreadyInUseException, RegistrationDisabledException {
        // todo send a verification email
        log.info("Attempting to register a user - [{}] {}", registerData.getEmail(), registerData.getUsername());
        if (!voltSettingsService.getVoltSettings().getAllowRegister()) {
            log.info("Registration is disabled, attempt rejected");
            throw new RegistrationDisabledException();
        }
        if (userRepository.findByEmail(registerData.getEmail()).isPresent()) {
            log.info("Couldn't register user, email is already in use");
            throw new EmailAlreadyInUseException();
        }
        if (userRepository.findByUsername(registerData.getUsername()).isPresent()) {
            log.info("Couldn't register user, username is already in use");
            throw new UsernameAlreadyInUseException();
        }
        User user = new User(null,
                registerData.getUsername(),
                passwordEncoder.encode(registerData.getPassword()),
                registerData.getEmail(),
                true, // todo disable until email gets verified
                "ROLE_USER",
                "default",
                registerData.getGithub(),
                registerData.getSchool(),
                0,
                0,
                0,
                Set.of(),
                Set.of(),
                Set.of());
        try {
            userRepository.save(user);
            log.info("User registered - [{}] {}", registerData.getEmail(), registerData.getUsername());
        } catch (Exception e) {
            log.error("Saving user to database failed", e);
            throw new FailedRegistrationException("Failed to register user", e);
        }
    }

    public String login(UserLoginDto loginData) throws FailedLoginException {
        try {
            log.info("Attempting to login a user with username {}", loginData.getUsername());
            User user = userRepository.findByUsername(loginData.getUsername()).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginData.getUsername(), loginData.getPassword()));
            return jwtUtil.generate(user);
        } catch (Exception e) {
            log.info("Failed to login - {}", e.getMessage());
            throw new FailedLoginException();
        }
    }
}
