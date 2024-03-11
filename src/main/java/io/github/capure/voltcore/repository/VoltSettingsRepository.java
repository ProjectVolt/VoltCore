package io.github.capure.voltcore.repository;

import io.github.capure.voltcore.model.VoltSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoltSettingsRepository extends JpaRepository<VoltSettings, Long> {
}
