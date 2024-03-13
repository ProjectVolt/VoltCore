package io.github.capure.voltcore.service;


import io.github.capure.voltcore.model.VoltSettings;
import io.github.capure.voltcore.repository.VoltSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { VoltSettingsService.class, VoltSettingsRepository.class })
public class VoltSettingsServiceTest {
    @Autowired
    private VoltSettingsService voltSettingsService;

    @MockBean
    private VoltSettingsRepository voltSettingsRepository;

    @Test
    public void shouldInitializeProperlyForEmptyTable() {
        AtomicBoolean saveCalled = new AtomicBoolean(false);
        Mockito.doAnswer((e) -> {
            saveCalled.set(true);
            return null;
        }).when(voltSettingsRepository).save(any());

        assertDoesNotThrow(voltSettingsService::init);

        assertTrue(saveCalled.get());
    }

    @Test
    public void shouldInitializeProperlyForSingleRow() {
        AtomicBoolean saveCalled = new AtomicBoolean(false);
        Mockito.doAnswer((e) -> {
            saveCalled.set(true);
            return null;
        }).when(voltSettingsRepository).save(any());
        Mockito.when(voltSettingsRepository.findAll()).thenReturn(Collections.singletonList(VoltSettings.getDefault()));

        assertDoesNotThrow(voltSettingsService::init);

        assertFalse(saveCalled.get());
    }

    @Test
    public void initShouldThrowForMultipleRows() {
        Mockito.when(voltSettingsRepository.findAll()).thenReturn(List.of(VoltSettings.getDefault(), VoltSettings.getDefault()));

        assertThrows(IllegalStateException.class, voltSettingsService::init);
    }

    @Test
    public void getSettingsShouldThrowOnIllegalState() {
        Mockito.when(voltSettingsRepository.findAll()).thenReturn(List.of(VoltSettings.getDefault(), VoltSettings.getDefault()));

        assertThrows(IllegalStateException.class, voltSettingsService::getVoltSettings);

        Mockito.when(voltSettingsRepository.findAll()).thenReturn(List.of());

        assertThrows(IllegalStateException.class, voltSettingsService::getVoltSettings);
    }

    @Test
    public void getSettingsShouldReturnSettings() {
        Mockito.when(voltSettingsRepository.findAll()).thenReturn(Collections.singletonList(VoltSettings.getDefault()));

        VoltSettings result = assertDoesNotThrow(voltSettingsService::getVoltSettings);
        assertEquals(VoltSettings.getDefault().getDeploymentName(), result.getDeploymentName());
    }

    @Test
    public void editSettingsShouldCorrectId() {
        AtomicReference<VoltSettings> saved = new AtomicReference<>();
        Mockito.doAnswer((e) -> {
            saved.set(e.getArgument(0, VoltSettings.class));
            return null;
        }).when(voltSettingsRepository).save(any());
        VoltSettings mockedOg = new VoltSettings(2L, "test", "test", false);
        Mockito.when(voltSettingsRepository.findAll()).thenReturn(Collections.singletonList(mockedOg));

        assertDoesNotThrow(() -> voltSettingsService.editVoltSettings(VoltSettings.getDefault()));

        assertNotNull(saved.get());
        assertEquals(2L, saved.get().getId());
        assertEquals(VoltSettings.getDefault().getDeploymentName(), saved.get().getDeploymentName());
    }
}
