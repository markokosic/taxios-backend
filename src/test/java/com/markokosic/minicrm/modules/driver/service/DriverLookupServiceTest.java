package com.markokosic.minicrm.modules.driver.service;

import com.markokosic.minicrm.exception.ResourceNotFoundException;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DriverLookupServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverLookupService driverLookupService;

    @Test
    void testValidateDriverExistsOrThrow_whenDriverExists_shouldReturnDriver() {
        // Arrange
        Long driverId = 1L;
        Driver driver = new Driver();
        driver.setId(driverId);
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));

        // Act
        Driver result = driverLookupService.validateDriverExistsOrThrow(driverId);

        // Assert
        assertNotNull(result);
        assertEquals(driverId, result.getId());
        verify(driverRepository, times(1)).findById(driverId);
    }

    @Test
    void testValidateDriverExistsOrThrow_whenDriverDoesNotExist_shouldThrowResourceNotFoundException() {
        // Arrange
        Long driverId = 1L;
        when(driverRepository.findById(driverId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                driverLookupService.validateDriverExistsOrThrow(driverId)
        );
        assertEquals("domain.driver.not_found", exception.getMessage());
        verify(driverRepository, times(1)).findById(driverId);
    }

    @Test
    void testValidateAllExistOrThrow_whenAllExist_shouldReturnDrivers() {
        // Arrange
        Set<Long> ids = Set.of(1L, 2L);
        Driver driver1 = new Driver();
        driver1.setId(1L);
        Driver driver2 = new Driver();
        driver2.setId(2L);
        when(driverRepository.findAllByIdIn(ids)).thenReturn(List.of(driver1, driver2));

        // Act
        List<Driver> result = driverLookupService.validateAllExistOrThrow(ids);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(driverRepository, times(1)).findAllByIdIn(ids);
    }

    @Test
    void testValidateAllExistOrThrow_whenSomeDoNotExist_shouldThrowResourceNotFoundException() {
        // Arrange
        Set<Long> ids = Set.of(1L, 2L);
        Driver driver1 = new Driver();
        driver1.setId(1L);
        when(driverRepository.findAllByIdIn(ids)).thenReturn(List.of(driver1));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                driverLookupService.validateAllExistOrThrow(ids)
        );
        assertEquals("domain.driver.not_found", exception.getMessage());
        verify(driverRepository, times(1)).findAllByIdIn(ids);
    }
}
