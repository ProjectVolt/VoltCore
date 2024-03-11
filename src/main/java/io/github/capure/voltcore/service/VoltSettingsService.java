package io.github.capure.voltcore.service;

import io.github.capure.voltcore.model.VoltSettings;
import io.github.capure.voltcore.repository.VoltSettingsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VoltSettingsService {
    @Autowired
    private VoltSettingsRepository voltSettingsRepository;

    public void init() {
        var settings = voltSettingsRepository.findAll();
        if ((long) settings.size() > 1) {
            log.error("Volt settings table has more than 1 row");
            log.error("Application settings cannot be trusted");
            throw new IllegalStateException("Multiple VoltSettings rows");
        } else if (settings.isEmpty()) {
            log.info("Volt settings empty, initializing with defaults");
            voltSettingsRepository.save(VoltSettings.getDefault());
        } else {
            VoltSettings voltSettings = settings.getFirst();
            log.info("Volt settings - {}", voltSettings);
        }
    }

    public VoltSettings getVoltSettings() {
        var settings = voltSettingsRepository.findAll();
        if ((long) settings.size() > 1) {
            log.error("Volt settings table has more than 1 row");
            log.error("Application settings cannot be trusted");
            throw new IllegalStateException("Multiple VoltSettings rows");
        } else if (settings.isEmpty()) {
            log.error("Volt settings not found");
            log.error("Recovery impossible, this is an unexpected behaviour");
            throw new IllegalStateException("VoltSettings not found");
        } else {
            return settings.getFirst();
        }
    }

    public void editVoltSettings(VoltSettings newData) {
        log.info("Editing Volt settings");
        VoltSettings current = getVoltSettings();
        VoltSettings toSave = new VoltSettings(current.getId(),
                newData.getDeploymentName(),
                newData.getDeploymentBaseUrl(),
                newData.getAllowRegister());
        voltSettingsRepository.save(toSave);
        log.info("Settings changes saved");
        log.info("New Volt settings - {}", toSave);
    }
}
