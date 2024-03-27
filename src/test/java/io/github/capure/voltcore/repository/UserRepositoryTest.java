package io.github.capure.voltcore.repository;

import io.github.capure.voltcore.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User getUser() {
        return new User(1L,
                "tester",
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
                Set.of());
    }

    @Test
    @DirtiesContext
    public void updateEnabledByUserIdShouldWorkForValidData() {
        User user = getUser();
        userRepository.save(user);

        int updated = assertDoesNotThrow(() -> userRepository.updateEnabledByUserId(user.getId(), false));

        assertEquals(1, updated);
        Optional<User> result = userRepository.findByUsername(user.getUsername());
        assertTrue(result.isPresent());
        User resultUser = result.get();
        assertFalse(resultUser.isEnabled());
    }

    @Test
    @DirtiesContext
    public void updateEnableByUserIdThrowsForInvalidUserId() {
        User user = getUser();
        userRepository.save(user);

        int updated = assertDoesNotThrow(() -> userRepository.updateEnabledByUserId(user.getId() + 1, false));

        assertEquals(0, updated);
        Optional<User> result = userRepository.findByUsername(user.getUsername());
        assertTrue(result.isPresent());
        User resultUser = result.get();
        assertTrue(resultUser.isEnabled());
    }

    @Test
    @DirtiesContext
    public void findAllByUsernameLikeIgnoreCaseWorks() {
        for (int i = 0; i < 100; i++) {
            User user = getUser();
            user.setId(null);
            user.setUsername(getUser().getUsername() + i);
            userRepository.save(user);
        }
        List<User> results = assertDoesNotThrow(() -> userRepository.findAllByUsernameLikeIgnoreCase("%tes%", PageRequest.of(0, 10)));
        assertEquals(10, results.size());
        assertEquals(getUser().getUsername() + 9, results.getLast().getUsername());
        results = assertDoesNotThrow(() -> userRepository.findAllByUsernameLikeIgnoreCase("%tes%", PageRequest.of(1, 10)));
        assertEquals(10, results.size());
        assertEquals(getUser().getUsername() + 19, results.getLast().getUsername());
    }

    @Test
    @DirtiesContext
    public void findAllByEnabledAndUsernameLikeIgnoreCaseWorks() {
        for (int i = 0; i < 100; i++) {
            User user = getUser();
            user.setId(null);
            user.setUsername(getUser().getUsername() + i);
            user.setEnabled(i % 2 == 0);
            userRepository.save(user);
        }
        List<User> results = assertDoesNotThrow(() -> userRepository.findAllByEnabledAndUsernameLikeIgnoreCase(true, "%tes%", PageRequest.of(0, 10)));
        assertEquals(10, results.size());
        assertEquals(getUser().getUsername() + 18, results.getLast().getUsername());
        results = assertDoesNotThrow(() -> userRepository.findAllByEnabledAndUsernameLikeIgnoreCase(true, "%tes%", PageRequest.of(1, 10)));
        assertEquals(10, results.size());
        assertEquals(getUser().getUsername() + 38, results.getLast().getUsername());
    }
}
