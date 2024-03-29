package io.github.capure.voltcore.repository;

import io.github.capure.voltcore.model.Problem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    public List<Problem> findAllByNameLikeIgnoreCase(String search, Pageable pageable);

    public List<Problem> findAllByVisibleAndNameLikeIgnoreCase(Boolean visible, String search, Pageable pageable);
}
