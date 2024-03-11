package io.github.capure.voltcore.service;

import io.github.capure.voltcore.model.User;
import io.github.capure.voltcore.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username - {}", username);
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElseThrow(() -> {
            log.info("Failed loading user by username - {}", username);
            return new UsernameNotFoundException("User not found");
        });
    }
}
