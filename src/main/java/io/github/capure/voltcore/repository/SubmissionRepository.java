package io.github.capure.voltcore.repository;

import io.github.capure.voltcore.model.Contest;
import io.github.capure.voltcore.model.Submission;
import io.github.capure.voltcore.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAddedByAndProblem_IdOrderByCreatedOnDesc(User addedBy, Long problemId, Pageable pageable);

    List<Submission> findAllByProblem_ContestOrderByCreatedOnDesc(Contest contest, Pageable pageable);
}
