package io.github.capure.voltcore.repository;

import io.github.capure.voltcore.model.Problem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    public List<Problem> findAllByNameLikeIgnoreCaseOrderByIdAsc(String search, Pageable pageable);

    public List<Problem> findAllByVisibleAndNameLikeIgnoreCaseOrderByIdAsc(Boolean visible, String search, Pageable pageable);
}
