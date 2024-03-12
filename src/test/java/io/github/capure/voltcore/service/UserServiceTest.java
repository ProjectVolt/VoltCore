package io.github.capure.voltcore.service;

import io.github.capure.voltcore.dto.GetUserDto;
import io.github.capure.voltcore.dto.PutUserDto;
import io.github.capure.voltcore.dto.UserLoginDto;
import io.github.capure.voltcore.dto.UserRegisterDto;
import io.github.capure.voltcore.dto.admin.AdminGetUserDto;
import io.github.capure.voltcore.exception.*;
import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.model.VoltSettings;
import io.github.capure.voltcore.repository.UserRepository;
import io.github.capure.voltcore.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { UserService.class, UserDetailsServiceImpl.class, UserRepository.class, VoltSettingsService.class, AuthenticationManager.class, JwtUtil.class, BCryptPasswordEncoder.class })
@EnableMethodSecurity
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private VoltSettingsService voltSettingsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    private User getUser() {
        return new User(1L,
                "tester",
                "password1",
                "tester@example.com",
                true,
                "ROLE_USER",
                "https://example.com",
                "https://github.com/Capure",
                null,
                0,
                0,
                0);
    }

    private User getAdmin() {
        return new User(2L,
                "admin",
                "password1",
                "admin@example.com",
                true,
                "ROLE_ADMIN",
                "https://example.com",
                "https://github.com/Capure",
                null,
                0,
                0,
                0);
    }

    private UserRegisterDto getRegisterData() {
        return new UserRegisterDto("tester",
                "password1",
                "tester@example.com",
                "https://github.com/Capure",
                null);
    }

    @BeforeEach
    public void setup() {
        Mockito.when(userRepository.findByUsername("admin")).thenReturn(Optional.of(getAdmin()));
        Mockito.when(userRepository.findByUsername("tester")).thenReturn(Optional.of(getUser()));
        Mockito.when(voltSettingsService.getVoltSettings()).thenReturn(VoltSettings.getDefault());
    }

    @Test
    public void shouldThrowIfRegistrationIsDisabled() {
        Mockito.when(voltSettingsService.getVoltSettings()).thenReturn(new VoltSettings(null, "test", "test", false));

        assertThrows(RegistrationDisabledException.class, () -> userService.register(getRegisterData()));
    }

    @Test
    public void shouldReturnUserForValidUsername() {
        User user = getUser();
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));

        UserDetails result = assertDoesNotThrow(() -> userDetailsService.loadUserByUsername(user.getUsername()));
        assertEquals(user, result);
    }

    @Test
    public void shouldThrowForInvalidUsername() {
        User user = getUser();
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(user.getUsername()));
    }

    @Test
    public void shouldLetUserLoginForValidData() {
        User user = getUser();
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
        Mockito.when(jwtUtil.generate(any())).thenReturn("token");
        AtomicBoolean authCalled = new AtomicBoolean(false);
        Mockito.doAnswer((e) -> {
            authCalled.set(true);
            return null;
        }).when(authenticationManager).authenticate(any());

        String token = assertDoesNotThrow(() -> userService.login(new UserLoginDto(user.getUsername(), user.getPassword())));

        assertEquals("token", token);
        assertTrue(authCalled.get());
    }

    @Test
    public void shouldHandleInvalidDataForLogin() {
        User user = getUser();
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
        Mockito.doThrow(BadCredentialsException.class).when(authenticationManager).authenticate(any());

        assertThrows(FailedLoginException.class, () -> userService.login(new UserLoginDto(user.getUsername(), "password")));
    }

    @Test
    public void shouldHandleTakenEmailForRegister() {
        UserRegisterDto registerData = getRegisterData();
        User user = getUser();
        Mockito.when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyInUseException.class, () -> userService.register(registerData));
    }

    @Test
    public void shouldHandleTakenUsernameForRegister() {
        UserRegisterDto registerData = getRegisterData();
        User user = getUser();
        Mockito.when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));

        assertThrows(UsernameAlreadyInUseException.class, () -> userService.register(registerData));
    }

    @Test
    public void shouldHandleDBErrorForRegister() {
        UserRegisterDto registerData = getRegisterData();
        Mockito.when(userRepository.findByUsername(registerData.getUsername())).thenReturn(Optional.empty());
        Mockito.doThrow(RuntimeException.class).when(userRepository).save(any());

        assertThrows(FailedRegistrationException.class, () -> userService.register(registerData));
    }

    @Test
    public void shouldLetUserRegister() {
        UserRegisterDto registerData = getRegisterData();
        AtomicReference<User> user = new AtomicReference<>();
        Mockito.when(userRepository.findByUsername(registerData.getUsername())).thenReturn(Optional.empty());
        Mockito.doAnswer((e) -> {
            user.set(e.getArgument(0, User.class));
            return null;
        }).when(userRepository).save(any());

        assertDoesNotThrow(() -> userService.register(registerData));
        assertEquals("ROLE_USER", user.get().getRole());
        assertEquals(getUser().getUsername(), user.get().getUsername());
        assertEquals(getUser().getEmail(), user.get().getEmail());
        assertTrue(passwordEncoder.matches(getUser().getPassword(), user.get().getPassword()));
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void shouldLetUserDeleteAccount() {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long mockId = principal.getId();
        assertTrue(principal.getEnabled());
        Mockito.when(userRepository.updateEnabledByUserId(mockId, false)).thenReturn(1);

        assertDoesNotThrow(() -> userService.delete(mockId));
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void shouldLetAdminDeleteAccount() {
        Long mockId = getUser().getId();
        Mockito.when(userRepository.updateEnabledByUserId(mockId, false)).thenReturn(1);

        assertDoesNotThrow(() -> userService.delete(mockId));
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void shouldNotLetUserDeleteOtherAccount() {
        Long mockId = getAdmin().getId();
        Mockito.when(userRepository.updateEnabledByUserId(mockId, false)).thenReturn(1);

        assertThrows(AccessDeniedException.class, () -> userService.delete(mockId));
    }

    @Test
    public void shouldLetUserGetUserDataById() {
        Mockito.when(userRepository.findById(getUser().getId())).thenReturn(Optional.of(getUser()));
        GetUserDto result = assertDoesNotThrow(() -> userService.get(getUser().getId()));
        assertEquals(getUser().getUsername(), result.getUsername());
    }

    @Test
    public void shouldThrowForGetWithInvalidId() {
        Mockito.when(userRepository.findById(getUser().getId())).thenReturn(Optional.empty());
        assertThrows(InvalidIdException.class, () -> userService.get(getUser().getId()));
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void shouldLetLoggedInUserUpdateTheirProfile() {
        PutUserDto putData = new PutUserDto("abcdef", "Capure", "Volt LO");
        Mockito.when(userRepository.findById(getUser().getId())).thenReturn(Optional.of(getUser()));
        AtomicReference<User> saved = new AtomicReference<>();
        Mockito.doAnswer(a -> {
            saved.set(a.getArgument(0));
            return null;
        }).when(userRepository).save(any());

        assertDoesNotThrow(() -> userService.update(getUser().getId(), putData));

        assertEquals(putData.getAvatar(), saved.get().getAvatar());
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void shouldLetLoggedInUserUpdateTheirProfileWithPartialDto() {
        PutUserDto putData1 = new PutUserDto(null, null, null);
        PutUserDto putData2 = new PutUserDto(null, "Capure", "Volt LO");
        PutUserDto putData3 = new PutUserDto("abcdef", null, "Volt LO");
        PutUserDto putData4 = new PutUserDto("abcdef", "Capure", null);
        Mockito.when(userRepository.findById(getUser().getId())).thenAnswer(a -> Optional.of(getUser()));
        AtomicReference<User> saved = new AtomicReference<>();
        Mockito.doAnswer(a -> {
            saved.set(a.getArgument(0));
            return null;
        }).when(userRepository).save(any());

        assertDoesNotThrow(() -> userService.update(getUser().getId(), putData1));
        assertEquals(getUser().getAvatar(), saved.get().getAvatar());

        assertDoesNotThrow(() -> userService.update(getUser().getId(), putData2));
        assertEquals(getUser().getAvatar(), saved.get().getAvatar());

        assertDoesNotThrow(() -> userService.update(getUser().getId(), putData3));
        assertEquals(getUser().getGithub(), saved.get().getGithub());

        assertDoesNotThrow(() -> userService.update(getUser().getId(), putData4));
        assertEquals(getUser().getSchool(), saved.get().getSchool());
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void shouldThrowInvalidIdExceptionForUserUpdate() {
        PutUserDto putData = new PutUserDto("abcdef", "Capure", "Volt LO");
        assertThrows(InvalidIdException.class, () -> userService.update(getUser().getId(), putData));
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void shouldThrowAccessDeniedExceptionForUserUpdateForOtherUser() {
        PutUserDto putData = new PutUserDto("abcdef", "Capure", "Volt LO");
        assertThrows(AccessDeniedException.class, () -> userService.update(getAdmin().getId(), putData));
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void shouldThrowFailedUpdateExceptionForFailedUserUpdate() {
        PutUserDto putData = new PutUserDto("abcdef", "Capure", "Volt LO");
        Mockito.when(userRepository.findById(getUser().getId())).thenReturn(Optional.of(getUser()));
        Mockito.doThrow(NullPointerException.class).when(userRepository).save(any());
        assertThrows(FailedUpdateException.class, () -> userService.update(getUser().getId(), putData));
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void shouldThrowAccessDeniedExceptionForAdminGetIfCalledByNormalUser() {
        assertThrows(AccessDeniedException.class, () -> userService.adminGet(getUser().getId()));
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void shouldThrowInvalidIdExceptionForAdminGetWithInvalidId() {
        assertThrows(InvalidIdException.class, () -> userService.adminGet(3L));
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "admin", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void shouldWorkForAdminGetWithValidId() {
        Mockito.when(userRepository.findById(getUser().getId())).thenReturn(Optional.of(getUser()));
        AdminGetUserDto result = assertDoesNotThrow(() -> userService.adminGet(getUser().getId()));

        assertEquals(getUser().getId(), result.getId());
        assertEquals(getUser().getUsername(), result.getUsername());
    }
}
