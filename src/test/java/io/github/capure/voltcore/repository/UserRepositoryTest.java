package io.github.capure.voltcore.repository;

import io.github.capure.voltcore.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
                0);
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
}
