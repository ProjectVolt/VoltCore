package io.github.capure.voltcore.service;

import io.github.capure.voltcore.dto.*;
import io.github.capure.voltcore.exception.InvalidIdException;
import io.github.capure.voltcore.model.*;
import io.github.capure.voltcore.repository.ContestRepository;
import io.github.capure.voltcore.repository.UserRepository;
import io.github.capure.voltcore.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ContestService.class, ContestRepository.class, UserDetailsServiceImpl.class})
@EnableMethodSecurity
public class ContestServiceTest {
    @Autowired
    private ContestService contestService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private ContestRepository contestRepository;

    @MockBean
    private UserRepository userRepository;

    private User getUser() {
        return new User(1L,
                "admin",
                "password1",
                "tester@example.com",
                true,
                "ROLE_ADMIN",
                "https://example.com",
                "https://github.com/Capure",
                null,
                0,
                0,
                0,
                Set.of(),
                Set.of(),
                Set.of());
    }

    @BeforeEach
    public void setup() {
        User staff = getUser();
        staff.setId(2L);
        staff.setRole("ROLE_STAFF");
        Mockito.when(userRepository.findByUsername("staff")).thenReturn(Optional.of(staff));
    }

    private CreateContestDto getCreateData() {
        return new CreateContestDto("Test contest", "description", null, 1L, 2000000000L, true);
    }

    private PutContestDto getPutData() {
        return new PutContestDto(false, "Changed contest", "desc", "set", 2L, 2100000000L);
    }

    private Contest getContestData() {
        return new Contest(1L, "con", "desc", "pass", Instant.ofEpochSecond(1L), Instant.ofEpochSecond(2000000000L), true, getUser(), Set.of());
    }

    @Test
    @WithMockUser
    public void createShouldThrowForNormalUser() {
        User user = getUser();
        user.setRole("ROLE_USER");
        assertThrows(AccessDeniedException.class, () -> contestService.create(user, getCreateData()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createShouldSetDataProperly() {
        CreateContestDto data = getCreateData();
        User user = getUser();
        AtomicReference<Contest> saved = new AtomicReference<>(null);

        Mockito.when(contestRepository.save(any())).thenAnswer(a -> {
            Contest arg = a.getArgument(0);
            arg.setId(1L);
            saved.set(arg);
            return arg;
        });

        GetContestDto result = assertDoesNotThrow(() -> contestService.create(user, data));

        assertEquals(data.getName(), saved.get().getName());
        assertEquals(data.getDescription(), saved.get().getDescription());
        assertEquals(data.getVisible(), saved.get().getVisible());
        assertEquals(user.getId(), saved.get().getAddedBy().getId());
        assertEquals(data.getStartTime(), saved.get().getStartTime().getEpochSecond());
        assertEquals(data.getEndTime(), saved.get().getEndTime().getEpochSecond());

        assertEquals(data.getName(), result.getName());
        assertEquals(data.getDescription(), result.getDescription());
        assertEquals(data.getVisible(), result.getVisible());
        assertEquals(user.getId(), result.getAddedBy());
        assertEquals(data.getStartTime(), result.getStartTime());
        assertEquals(data.getEndTime(), result.getEndTime());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void createShouldSetPasswordToNullIfAbsent() {
        CreateContestDto data = getCreateData();
        User user = getUser();
        AtomicReference<Contest> saved = new AtomicReference<>(null);

        Mockito.when(contestRepository.save(any())).thenAnswer(a -> {
            Contest arg = a.getArgument(0);
            arg.setId(1L);
            saved.set(arg);
            return arg;
        });

        assertDoesNotThrow(() -> contestService.create(user, data));

        assertNull(saved.get().getPassword());

        data.setPassword("");
        assertDoesNotThrow(() -> contestService.create(user, data));

        assertNull(saved.get().getPassword());

        data.setPassword("password");
        assertDoesNotThrow(() -> contestService.create(user, data));

        assertEquals("password", saved.get().getPassword());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldThrowForInvalidId() {
        PutContestDto data = getPutData();
        User user = getUser();

        Mockito.when(contestRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(InvalidIdException.class, () -> contestService.edit(user, 1L, data));
    }

    @Test
    @WithMockUser
    public void editShouldThrowForNormalUser() {
        User user = getUser();
        user.setRole("ROLE_USER");
        assertThrows(AccessDeniedException.class, () -> contestService.edit(user, 1L, getPutData()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldThrowForStaffNotOwner() {
        PutContestDto data = getPutData();
        User user = getUser();
        user.setRole("ROLE_STAFF");
        user.setId(2L);

        Mockito.when(contestRepository.findById(any())).thenReturn(Optional.of(getContestData()));

        assertThrows(AccessDeniedException.class, () -> contestService.edit(user, 1L, data));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldSetDataProperly() {
        PutContestDto data = getPutData();
        User user = getUser();
        Mockito.when(contestRepository.findById(any())).thenReturn(Optional.of(getContestData()));

        AtomicReference<Contest> saved = new AtomicReference<>(null);
        Mockito.when(contestRepository.save(any())).thenAnswer(a -> {
            Contest arg = a.getArgument(0);
            arg.setId(1L);
            saved.set(arg);
            return arg;
        });

        GetContestDto result = assertDoesNotThrow(() -> contestService.edit(user, 1L, data));

        assertEquals(data.getName(), saved.get().getName());
        assertEquals(data.getDescription(), saved.get().getDescription());
        assertEquals(data.getVisible(), saved.get().getVisible());
        assertEquals(user.getId(), saved.get().getAddedBy().getId());
        assertEquals(data.getStartTime(), saved.get().getStartTime().getEpochSecond());
        assertEquals(data.getEndTime(), saved.get().getEndTime().getEpochSecond());

        assertEquals(data.getName(), result.getName());
        assertEquals(data.getDescription(), result.getDescription());
        assertEquals(data.getVisible(), result.getVisible());
        assertEquals(user.getId(), result.getAddedBy());
        assertEquals(data.getStartTime(), result.getStartTime());
        assertEquals(data.getEndTime(), result.getEndTime());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void editShouldSetPasswordToNullIfAbsent() {
        PutContestDto data = getPutData();
        User user = getUser();
        Mockito.when(contestRepository.findById(any())).thenReturn(Optional.of(getContestData()));

        AtomicReference<Contest> saved = new AtomicReference<>(null);
        Mockito.when(contestRepository.save(any())).thenAnswer(a -> {
            Contest arg = a.getArgument(0);
            arg.setId(1L);
            saved.set(arg);
            return arg;
        });

        data.setPassword(null);
        assertDoesNotThrow(() -> contestService.edit(user, 1L, data));

        assertNull(saved.get().getPassword());

        data.setPassword("");
        assertDoesNotThrow(() -> contestService.edit(user, 1L, data));

        assertNull(saved.get().getPassword());

        data.setPassword("password");
        assertDoesNotThrow(() -> contestService.edit(user, 1L, data));

        assertEquals("password", saved.get().getPassword());
    }

    @Test
    @WithMockUser
    public void getAllShouldThrowForNormalUserIfVisibleIsNotTrue() {
        assertThrows(AccessDeniedException.class, () -> contestService.getAll(null, "search", 0, 10));
        assertThrows(AccessDeniedException.class, () -> contestService.getAll(false, "search", 0, 10));
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "staff", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void getAllShouldFilterOutInvisibleNotOwnedByCurrentStaff() {
        Contest contest = getContestData();
        contest.setVisible(false);
        Mockito.when(contestRepository.findAllByNameLikeIgnoreCaseOrderByStartTimeDesc(any(), any())).thenReturn(List.of(contest));

        List<GetContestDto> result = assertDoesNotThrow(() -> contestService.getAll(null, "search", 0, 10));
        assertEquals(0, result.size());
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "staff", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void getAllShouldLeaveInvisibleOwnedByCurrentStaff() {
        User staff = getUser();
        staff.setId(2L);
        staff.setRole("ROLE_STAFF");
        Contest contest = getContestData();
        contest.setVisible(false);
        contest.setAddedBy(staff);
        Mockito.when(contestRepository.findAllByNameLikeIgnoreCaseOrderByStartTimeDesc(any(), any())).thenReturn(List.of(contest));

        List<GetContestDto> result = assertDoesNotThrow(() -> contestService.getAll(null, "search", 0, 10));
        assertEquals(1, result.size());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllShouldLeaveInvisibleForAdmin() {
        User staff = getUser();
        staff.setId(2L);
        staff.setRole("ROLE_STAFF");
        Contest contest = getContestData();
        contest.setVisible(false);
        contest.setAddedBy(staff);
        Mockito.when(contestRepository.findAllByNameLikeIgnoreCaseOrderByStartTimeDesc(any(), any())).thenReturn(List.of(contest));

        List<GetContestDto> result = assertDoesNotThrow(() -> contestService.getAll(null, "search", 0, 10));
        assertEquals(1, result.size());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllShouldReturnLockedGetContestDto() {
        Contest contest = getContestData();
        Mockito.when(contestRepository.findAllByNameLikeIgnoreCaseOrderByStartTimeDesc(any(), any())).thenReturn(List.of(contest));
        Mockito.when(contestRepository.findAllByVisibleAndNameLikeIgnoreCaseOrderByStartTimeDesc(any(), any(), any())).thenReturn(List.of(contest));

        List<GetContestDto> result1 = assertDoesNotThrow(() -> contestService.getAll(null, "search", 0, 10));
        List<GetContestDto> result2 = assertDoesNotThrow(() -> contestService.getAll(true, "search", 0, 10));

        assertEquals(1, result1.size());
        assertNull(result1.getFirst().getProblems());
        assertEquals(1, result2.size());
        assertNull(result2.getFirst().getProblems());
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "staff", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void getThrowsForInvisibleContestIfNotOwner() {
        Contest contest = getContestData();
        contest.setVisible(false);
        User user = getUser();
        user.setRole("ROLE_USER");
        Mockito.when(contestRepository.findById(any())).thenReturn(Optional.of(contest));

        assertThrows(AccessDeniedException.class, () -> contestService.get(user, 1L, null));
    }

    @Test
    @WithMockUser
    public void getThrowsForInvalidId() {
        Mockito.when(contestRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(InvalidIdException.class, () -> contestService.get(getUser(), 1L, null));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getIsUnlockedForAdmin() {
        Contest contest = getContestData();
        contest.setVisible(false);
        User user = getUser();
        Mockito.when(contestRepository.findById(any())).thenReturn(Optional.of(contest));

        GetContestDto result = assertDoesNotThrow(() -> contestService.get(user, 1L, null));

        assertNotNull(result.getProblems());
    }

    @Test
    @WithUserDetails(userDetailsServiceBeanName = "userDetailsServiceImpl", value = "staff", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void getIsUnlockedForOwner() {
        User user = getUser();
        user.setId(2L);
        user.setRole("ROLE_STAFF");
        Contest contest = getContestData();
        contest.setPassword("pass");
        contest.setVisible(true);
        contest.setAddedBy(user);
        Mockito.when(contestRepository.findById(any())).thenAnswer(a -> Optional.of(contest));

        GetContestDto result = assertDoesNotThrow(() -> contestService.get(user, 1L, null));

        assertNotNull(result.getProblems());

        User user2 = getUser();
        user2.setId(1L);
        user2.setRole("ROLE_STAFF");
        GetContestDto result2 = assertDoesNotThrow(() -> contestService.get(user2, 1L, null));

        assertNull(result2.getProblems());
    }

    @Test
    @WithMockUser
    public void getIsUnlockedIfContestHasNoPassword() {
        User user = getUser();
        user.setId(2L);
        user.setRole("ROLE_STAFF");
        Contest contest = getContestData();
        contest.setPassword(null);
        contest.setVisible(true);
        Mockito.when(contestRepository.findById(any())).thenAnswer(a -> Optional.of(contest));

        GetContestDto result = assertDoesNotThrow(() -> contestService.get(user, 1L, null));

        assertNotNull(result.getProblems());
    }

    @Test
    @WithMockUser
    public void getIsUnlockedIfContestPasswordMatches() {
        User user = getUser();
        user.setId(2L);
        user.setRole("ROLE_STAFF");
        Contest contest = getContestData();
        contest.setPassword("pass");
        contest.setVisible(true);
        Mockito.when(contestRepository.findById(any())).thenAnswer(a -> Optional.of(contest));

        GetContestDto result = assertDoesNotThrow(() -> contestService.get(user, 1L, "pass"));

        assertNotNull(result.getProblems());
    }

    @Test
    @WithMockUser
    public void getThrowsIfPasswordDoesNotMatch() {
        User user = getUser();
        user.setId(2L);
        user.setRole("ROLE_STAFF");
        Contest contest = getContestData();
        contest.setPassword("pass");
        contest.setVisible(true);
        Mockito.when(contestRepository.findById(any())).thenAnswer(a -> Optional.of(contest));

        assertThrows(AccessDeniedException.class, () -> contestService.get(user, 1L, "other"));
    }
}
