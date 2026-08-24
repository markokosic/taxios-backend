package com.markokosic.minicrm.modules.driver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.exception.BadRequestException;
import com.markokosic.minicrm.exception.ResourceNotFoundException;
import com.markokosic.minicrm.modules.driver.DriverMapper;
import com.markokosic.minicrm.modules.driver.RemunerationConfigMapper;
import com.markokosic.minicrm.modules.driver.dto.request.CreateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateFlatRateRemunerationConfigDTO;
import com.markokosic.minicrm.modules.driver.dto.request.UpdateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverSelectDTO;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.DriverStatus;
import com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig;
import com.markokosic.minicrm.modules.driver.repository.DriverRemunerationConfigRepository;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DriverServiceTest {

    @Mock
    private DriverMapper driverMapper;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RemunerationConfigMapper remunerationConfigMapper;

    @Mock
    private DriverLookupService driverLookupService;

    @Mock
    private DriverRemunerationConfigRepository driverRemunerationConfigRepository;

    @InjectMocks
    private DriverService driverService;

    @Test
    void testCreateDriver_whenValidRequest_shouldSaveAndReturnDriver() {
        // Arrange
        CreateFlatRateRemunerationConfigDTO configDto = new CreateFlatRateRemunerationConfigDTO(
                RemunerationModelType.FLAT_RATE, new BigDecimal("30.00"), null
        );
        CreateDriverRequestDTO request = new CreateDriverRequestDTO(
                "Max", "Mustermann", "max@email.com", "+436601234567", List.of(configDto)
        );

        Driver driver = new Driver();
        driver.setFirstName("Max");
        driver.setLastName("Mustermann");

        FlatRateRemunerationConfig config = new FlatRateRemunerationConfig();
        config.setFlatRateFee(new BigDecimal("30.00"));

        DriverResponseDTO expectedResponse = new DriverResponseDTO(
                1L, "Max", "Mustermann", "max@email.com", "+436601234567",
                DriverStatus.ACTIVE, Collections.emptyList(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(driverMapper.toEntity(request)).thenReturn(driver);
        when(remunerationConfigMapper.toEntity(configDto, driver)).thenReturn(config);
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);
        when(driverMapper.toDto(driver, remunerationConfigMapper)).thenReturn(expectedResponse);

        // Act
        DriverResponseDTO result = driverService.createDriver(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Max", result.firstName());
        verify(driverMapper, times(1)).toEntity(request);
        verify(driverRepository, times(1)).save(driver);
        verify(driverMapper, times(1)).toDto(driver, remunerationConfigMapper);
    }

    @Test
    void testCreateDriver_whenDuplicateConfigurations_shouldThrowBadRequestException() {
        // Arrange
        CreateFlatRateRemunerationConfigDTO configDto1 = new CreateFlatRateRemunerationConfigDTO(
                RemunerationModelType.FLAT_RATE, new BigDecimal("30.00"), null
        );
        CreateFlatRateRemunerationConfigDTO configDto2 = new CreateFlatRateRemunerationConfigDTO(
                RemunerationModelType.FLAT_RATE, new BigDecimal("50.00"), null
        );
        CreateDriverRequestDTO request = new CreateDriverRequestDTO(
                "Max", "Mustermann", "max@email.com", "+436601234567", List.of(configDto1, configDto2)
        );

        Driver driver = new Driver();
        FlatRateRemunerationConfig config1 = new FlatRateRemunerationConfig();
        FlatRateRemunerationConfig config2 = new FlatRateRemunerationConfig();

        when(driverMapper.toEntity(request)).thenReturn(driver);
        when(remunerationConfigMapper.toEntity(configDto1, driver)).thenReturn(config1);
        when(remunerationConfigMapper.toEntity(configDto2, driver)).thenReturn(config2);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                driverService.createDriver(request)
        );
        assertEquals("domain.driver.multiple_configurations", exception.getMessage());
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    void testGetDriverById_whenDriverExists_shouldReturnDriverResponse() {
        // Arrange
        Long driverId = 1L;
        Driver driver = new Driver();
        driver.setId(driverId);

        DriverResponseDTO expectedResponse = new DriverResponseDTO(
                driverId, "Max", "Mustermann", "max@email.com", "+436601234567",
                DriverStatus.ACTIVE, Collections.emptyList(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);
        when(driverMapper.toDto(driver, remunerationConfigMapper)).thenReturn(expectedResponse);

        // Act
        DriverResponseDTO result = driverService.getDriverById(driverId);

        // Assert
        assertNotNull(result);
        assertEquals(driverId, result.id());
        verify(driverLookupService, times(1)).validateDriverExistsOrThrow(driverId);
        verify(driverMapper, times(1)).toDto(driver, remunerationConfigMapper);
    }

    @Test
    void testGetAllDrivers_shouldReturnPagedDrivers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Driver driver = new Driver();
        driver.setId(1L);
        Page<Driver> page = new PageImpl<>(List.of(driver));

        DriverResponseDTO responseDto = new DriverResponseDTO(
                1L, "Max", "Mustermann", "max@email.com", "+436601234567",
                DriverStatus.ACTIVE, Collections.emptyList(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(driverRepository.findAllByStatus(DriverStatus.ACTIVE, pageable)).thenReturn(page);
        when(driverMapper.toDto(driver, remunerationConfigMapper)).thenReturn(responseDto);

        // Act
        PageResponseDTO<DriverResponseDTO> result = driverService.getAllDrivers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).id());
        verify(driverRepository, times(1)).findAllByStatus(DriverStatus.ACTIVE, pageable);
    }

    @Test
    void testDeleteDriver_whenDriverIsActive_shouldSetStatusToDeleted() {
        // Arrange
        Long driverId = 1L;
        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setStatus(DriverStatus.ACTIVE);

        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);

        // Act
        driverService.deleteDriver(driverId);

        // Assert
        assertEquals(DriverStatus.DELETED, driver.getStatus());
        verify(driverLookupService, times(1)).validateDriverExistsOrThrow(driverId);
    }

    @Test
    void testDeleteDriver_whenDriverIsAlreadyDeleted_shouldThrowResourceNotFoundException() {
        // Arrange
        Long driverId = 1L;
        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setStatus(DriverStatus.DELETED);

        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                driverService.deleteDriver(driverId)
        );
        assertEquals("domain.driver.not_found", exception.getMessage());
    }

    @Test
    void testStopRemunerationConfig_whenConfigExistsAndCurrent_shouldDeactivateIt() {
        // Arrange
        Long driverId = 1L;
        Long configId = 10L;
        Driver driver = new Driver();
        driver.setId(driverId);

        FlatRateRemunerationConfig config = new FlatRateRemunerationConfig();
        config.setId(configId);
        config.setCurrent(true);
        driver.getRemunerationConfigs().add(config);

        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);

        // Act
        driverService.stopRemunerationConfig(driverId, configId);

        // Assert
        assertFalse(config.isCurrent());
        assertNotNull(config.getValidUntil());
        verify(driverRepository, times(1)).save(driver);
    }

    @Test
    void testUpdateDriver_whenValidRequest_shouldUpdateAndSave() {
        // Arrange
        Long driverId = 1L;
        UpdateDriverRequestDTO request = new UpdateDriverRequestDTO(
                "Moritz", null, null, null, null
        );

        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setFirstName("Max");

        DriverResponseDTO expectedResponse = new DriverResponseDTO(
                driverId, "Moritz", "Mustermann", "max@email.com", "+436601234567",
                DriverStatus.ACTIVE, Collections.emptyList(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);
        when(driverMapper.toDto(driver, remunerationConfigMapper)).thenReturn(expectedResponse);

        // Act
        DriverResponseDTO result = driverService.updateDriver(driverId, request);

        // Assert
        assertNotNull(result);
        verify(driverLookupService, times(1)).validateDriverExistsOrThrow(driverId);
        verify(driverMapper, times(1)).updateEntityFromDto(request, driver);
        verify(driverRepository, times(1)).save(driver);
    }
}
