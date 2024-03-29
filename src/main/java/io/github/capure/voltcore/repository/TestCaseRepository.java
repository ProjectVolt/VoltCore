package io.github.capure.voltcore.repository;

import io.github.capure.voltcore.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
}
