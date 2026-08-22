package com.markokosic.minicrm.modules.driver.service;

import com.markokosic.minicrm.modules.driver.RemunerationConfigMapper;
import com.markokosic.minicrm.modules.driver.dto.request.CreateFlatRateRemunerationConfigDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateRemunerationRequestDTO;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig;
import com.markokosic.minicrm.modules.driver.repository.DriverRemunerationConfigRepository;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverRemunerationConfigServiceTest {

    @Mock
    private RemunerationConfigMapper configMapper;

    @Mock
    private DriverRemunerationConfigRepository configRepository;

    @Mock
    private DriverLookupService driverLookupService;

    @InjectMocks
    private DriverRemunerationConfigService service;

    @Test
    void createRemunerationConfig_Success() {
        Long driverId = 1L;
        Driver driver = new Driver();
        driver.setId(driverId);

        CreateRemunerationRequestDTO requestDTO = new CreateFlatRateRemunerationConfigDTO(
                RemunerationModelType.FLAT_RATE,
                new BigDecimal("50.00"),
                null
        );

        FlatRateRemunerationConfig configEntity = new FlatRateRemunerationConfig();
        configEntity.setFlatRateFee(new BigDecimal("50.00"));

        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);
        when(configMapper.toEntity(requestDTO, driver)).thenReturn(configEntity);
        when(configRepository.save(any())).thenReturn(configEntity);

        var result = service.createRemunerationConfig(requestDTO, driverId);

        assertNotNull(result);
        verify(driverLookupService).validateDriverExistsOrThrow(driverId);
        verify(configMapper).toEntity(requestDTO, driver);
        verify(configRepository).save(configEntity);
    }
}
