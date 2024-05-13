package io.github.capure.voltcore.repository;

import io.github.capure.voltcore.model.Contest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestRepository extends JpaRepository<Contest, Long> {
    public List<Contest> findAllByVisibleOrderByStartTimeDesc(Boolean visible);

    public List<Contest> findAllByVisibleAndNameLikeIgnoreCaseOrderByStartTimeDesc(Boolean visible, String search, Pageable pageable);

    public List<Contest> findAllByNameLikeIgnoreCaseOrderByStartTimeDesc(String search, Pageable pageable);
}
