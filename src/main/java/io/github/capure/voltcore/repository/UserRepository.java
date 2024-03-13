package io.github.capure.voltcore.repository;

import io.github.capure.voltcore.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE User u SET u.enabled = :enabled WHERE u.id = :userId")
    int updateEnabledByUserId(@Param("userId") Long userId, @Param("enabled") boolean enabled);

    List<User> findAllByUsernameLikeIgnoreCase(String search, Pageable pageable);

    List<User> findAllByEnabledAndUsernameLikeIgnoreCase(Boolean enabled, String search, Pageable pageable);
}
