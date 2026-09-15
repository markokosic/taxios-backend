package com.markokosic.minicrm.modules.driver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.exception.BadRequestException;
import com.markokosic.minicrm.exception.ResourceConflictException;
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
import com.markokosic.minicrm.modules.role.dto.Roles;
import com.markokosic.minicrm.modules.user.User;
import com.markokosic.minicrm.modules.user.UserRepository;
import com.markokosic.minicrm.modules.user.UserService;
import com.markokosic.minicrm.modules.user.dto.response.CreateUserResponseDTO;
import com.markokosic.minicrm.modules.user.model.UserStatus;
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

import com.markokosic.minicrm.modules.driver.dto.response.DriverRevenueOptionDTO;
import com.markokosic.minicrm.modules.flatratetype.repository.FlatRateTypeRepository;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import java.util.Optional;

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

    @Mock
    private FlatRateTypeRepository flatRateTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

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
        config.setDriverFlatRatePayoutPerShift(new BigDecimal("30.00"));

        DriverResponseDTO expectedResponse = new DriverResponseDTO(
                1L, null, "Max", "Mustermann", "max@email.com", "+436601234567",
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
                driverId, null, "Max", "Mustermann", "max@email.com", "+436601234567",
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
                1L, null, "Max", "Mustermann", "max@email.com", "+436601234567",
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
                driverId, null, "Moritz", "Mustermann", "max@email.com", "+436601234567",
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

    @Test
    void testDeleteDriver_withLinkedUser_shouldSoftDeleteDriverAndUser() {
        Long driverId = 1L;
        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setStatus(DriverStatus.ACTIVE);

        com.markokosic.minicrm.modules.user.User user = new com.markokosic.minicrm.modules.user.User();
        user.setId(5L);
        user.setStatus(com.markokosic.minicrm.modules.user.model.UserStatus.ACTIVE);
        driver.setUser(user);

        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);

        driverService.deleteDriver(driverId);

        assertEquals(DriverStatus.DELETED, driver.getStatus());
        assertEquals(UserStatus.DELETED, user.getStatus());
        assertNull(driver.getUser());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testCreateDriverUser_withDriverEmail_Success() {
        Long driverId = 1L;
        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setFirstName("Max");
        driver.setLastName("Mustermann");
        driver.setEmail("driver@taxi.com");

        CreateUserResponseDTO responseDTO = new CreateUserResponseDTO(
                10L, "Max", "Mustermann", "driver@taxi.com", Roles.DRIVER, true, "tempPass123"
        );

        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);
        when(userService.createDriverUser(driver, "driver@taxi.com")).thenReturn(responseDTO);

        CreateUserResponseDTO result = driverService.createDriverUser(driverId, null);

        assertNotNull(result);
        assertEquals(10L, result.id());
        verify(userService, times(1)).createDriverUser(driver, "driver@taxi.com");
    }

    @Test
    void testCreateDriverUser_withCustomEmail_Success() {
        Long driverId = 1L;
        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setFirstName("Max");
        driver.setLastName("Mustermann");
        driver.setEmail("driver@taxi.com");

        CreateUserResponseDTO responseDTO = new CreateUserResponseDTO(
                10L, "Max", "Mustermann", "custom@taxi.com", Roles.DRIVER, true, "tempPass123"
        );

        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);
        when(userService.createDriverUser(driver, "custom@taxi.com")).thenReturn(responseDTO);

        CreateUserResponseDTO result = driverService.createDriverUser(driverId, "custom@taxi.com");

        assertNotNull(result);
        assertEquals(10L, result.id());
        verify(userService, times(1)).createDriverUser(driver, "custom@taxi.com");
    }

    @Test
    void testCreateDriverUser_ThrowsConflict_WhenAlreadyHasActiveUser() {
        Long driverId = 1L;
        Driver driver = new Driver();
        driver.setId(driverId);

        User existingUser = new User();
        existingUser.setId(5L);
        existingUser.setStatus(UserStatus.ACTIVE);
        driver.setUser(existingUser);

        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);

        assertThrows(ResourceConflictException.class, () ->
                driverService.createDriverUser(driverId, null)
        );
    }

    @Test
    void testDeactivateDriverUser_Success() {
        Long driverId = 1L;
        Driver driver = new Driver();
        driver.setId(driverId);

        User user = new User();
        user.setId(5L);
        user.setStatus(UserStatus.ACTIVE);
        driver.setUser(user);

        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);

        driverService.deactivateDriverUser(driverId);

        assertEquals(UserStatus.DELETED, user.getStatus());
        assertNull(driver.getUser());
        verify(userRepository, times(1)).save(user);
        verify(driverRepository, times(1)).save(driver);
    }

    @Test
    void testDeactivateDriverUser_ThrowsNotFound_WhenNoUser() {
        Long driverId = 1L;
        Driver driver = new Driver();
        driver.setId(driverId);
        driver.setUser(null);

        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);

        assertThrows(ResourceNotFoundException.class, () ->
                driverService.deactivateDriverUser(driverId)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void testGetMyDriverProfile_Success() {
        Long userId = 5L;
        Driver driver = new Driver();
        driver.setId(1L);

        DriverResponseDTO responseDTO = new DriverResponseDTO(
                1L, userId, "Max", "Mustermann", "max@taxi.com", "+123456",
                DriverStatus.ACTIVE, List.of(), null, null
        );

        when(driverRepository.findByUserId(userId)).thenReturn(Optional.of(driver));
        when(driverMapper.toDto(driver, remunerationConfigMapper)).thenReturn(responseDTO);

        DriverResponseDTO result = driverService.getMyDriverProfile(userId);

        assertNotNull(result);
        assertEquals("Max", result.firstName());
        verify(driverRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testGetMyDriverProfile_ThrowsNotFound_WhenDriverNotFound() {
        Long userId = 99L;
        when(driverRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                driverService.getMyDriverProfile(userId)
        );
    }

    @Test
    void testGetMyRevenueOptions_Success() {
        Long userId = 5L;
        Driver driver = new Driver();
        driver.setId(1L);

        com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig config =
                new com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig();
        config.setDriver(driver);
        config.setCurrent(true);
        config.setDriverRevenueSharePercentage(new BigDecimal("60.00"));
        driver.setRemunerationConfigs(List.of(config));

        when(driverRepository.findByUserId(userId)).thenReturn(Optional.of(driver));
        when(driverLookupService.validateDriverExistsOrThrow(1L)).thenReturn(driver);

        List<DriverRevenueOptionDTO> result = driverService.getMyRevenueOptions(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(ShiftEntryCategory.REGULAR, result.get(0).entryCategory());
    }

    @Test
    void testGetMyRevenueOptions_WithFlatRate_ReturnsDriverFlatRatePayoutPerShift() {
        Long userId = 5L;
        Driver driver = new Driver();
        driver.setId(1L);

        com.markokosic.minicrm.modules.flatratetype.model.FlatRateType flatRateType =
                new com.markokosic.minicrm.modules.flatratetype.model.FlatRateType();
        flatRateType.setId(10L);
        flatRateType.setName("Wien -> Airport");
        flatRateType.setDefaultPrice(new BigDecimal("36.00"));
        flatRateType.setFlatRateCode("VIE_AIRPORT");

        com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig config =
                new com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig();
        config.setDriver(driver);
        config.setCurrent(true);
        config.setFlatRateType(flatRateType);
        config.setDriverFlatRatePayoutPerShift(new BigDecimal("25.00"));
        driver.setRemunerationConfigs(List.of(config));

        when(driverRepository.findByUserId(userId)).thenReturn(Optional.of(driver));
        when(driverLookupService.validateDriverExistsOrThrow(1L)).thenReturn(driver);
        when(flatRateTypeRepository.findAllByCurrentIsTrueAndStatus(any())).thenReturn(List.of(flatRateType));

        List<DriverRevenueOptionDTO> result = driverService.getMyRevenueOptions(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        DriverRevenueOptionDTO option = result.get(0);
        assertEquals(ShiftEntryCategory.FLAT_RATE, option.entryCategory());
        assertEquals(10L, option.flatRateTypeId());
        assertEquals("Wien -> Airport", option.label());
        assertEquals(new BigDecimal("36.00"), option.defaultPrice());
        assertEquals(new BigDecimal("25.00"), option.driverFlatRatePayoutPerShift());
    }

    @Test
    void testGetMyRevenueOptions_ThrowsNotFound_WhenDriverNotFound() {
        Long userId = 99L;
        when(driverRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                driverService.getMyRevenueOptions(userId)
        );
    }

    @Test
    void testCreateDriver_ThrowsBadRequest_WhenBothPercentageAndWeeklyConfigSupplied() {
        Driver driver = new Driver();
        when(driverMapper.toEntity(any())).thenReturn(driver);

        com.markokosic.minicrm.modules.driver.dto.request.CreatePercentageShareRemunerationConfigDTO pctDto =
                new com.markokosic.minicrm.modules.driver.dto.request.CreatePercentageShareRemunerationConfigDTO(RemunerationModelType.PERCENTAGE_SHARE, new BigDecimal("30.00"), new BigDecimal("0.4000"));
        com.markokosic.minicrm.modules.driver.dto.request.CreateWeeklyFixedRemunerationConfigDTO weeklyDto =
                new com.markokosic.minicrm.modules.driver.dto.request.CreateWeeklyFixedRemunerationConfigDTO(RemunerationModelType.WEEKLY_FIXED_RATE, new BigDecimal("400.00"), 7);

        com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig pctEntity =
                new com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig();
        com.markokosic.minicrm.modules.driver.model.WeeklyFixedRateRemunerationConfig weeklyEntity =
                new com.markokosic.minicrm.modules.driver.model.WeeklyFixedRateRemunerationConfig();

        when(remunerationConfigMapper.toEntity(pctDto, driver)).thenReturn(pctEntity);
        when(remunerationConfigMapper.toEntity(weeklyDto, driver)).thenReturn(weeklyEntity);

        CreateDriverRequestDTO request = new CreateDriverRequestDTO(
                "Max", "Mustermann", "max@example.com", "+436601234567",
                List.of(pctDto, weeklyDto)
        );

        assertThrows(BadRequestException.class, () -> driverService.createDriver(request));
    }

    @Test
    void testUpdateDriver_ThrowsBadRequest_WhenBothPercentageAndWeeklyConfigSupplied() {
        Long driverId = 1L;
        Driver driver = new Driver();
        driver.setId(driverId);
        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);

        com.markokosic.minicrm.modules.driver.dto.request.CreatePercentageShareRemunerationConfigDTO pctDto =
                new com.markokosic.minicrm.modules.driver.dto.request.CreatePercentageShareRemunerationConfigDTO(RemunerationModelType.PERCENTAGE_SHARE, new BigDecimal("30.00"), new BigDecimal("0.4000"));
        com.markokosic.minicrm.modules.driver.dto.request.CreateWeeklyFixedRemunerationConfigDTO weeklyDto =
                new com.markokosic.minicrm.modules.driver.dto.request.CreateWeeklyFixedRemunerationConfigDTO(RemunerationModelType.WEEKLY_FIXED_RATE, new BigDecimal("400.00"), 7);

        com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig pctEntity =
                new com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig();
        com.markokosic.minicrm.modules.driver.model.WeeklyFixedRateRemunerationConfig weeklyEntity =
                new com.markokosic.minicrm.modules.driver.model.WeeklyFixedRateRemunerationConfig();

        when(remunerationConfigMapper.toEntity(pctDto, driver)).thenReturn(pctEntity);
        when(remunerationConfigMapper.toEntity(weeklyDto, driver)).thenReturn(weeklyEntity);

        UpdateDriverRequestDTO request = new UpdateDriverRequestDTO(
                "Max", "Mustermann", "max@example.com", "+436601234567",
                List.of(pctDto, weeklyDto)
        );

        assertThrows(BadRequestException.class, () -> driverService.updateDriver(driverId, request));
    }

    @Test
    void testGetMyRevenueOptions_WithWeeklyFixedRate_IncludesRegularFare() {
        Long userId = 5L;
        Driver driver = new Driver();
        driver.setId(1L);

        com.markokosic.minicrm.modules.driver.model.WeeklyFixedRateRemunerationConfig config =
                new com.markokosic.minicrm.modules.driver.model.WeeklyFixedRateRemunerationConfig();
        config.setDriver(driver);
        config.setCurrent(true);
        driver.setRemunerationConfigs(List.of(config));

        when(driverRepository.findByUserId(userId)).thenReturn(Optional.of(driver));
        when(driverLookupService.validateDriverExistsOrThrow(1L)).thenReturn(driver);

        List<DriverRevenueOptionDTO> result = driverService.getMyRevenueOptions(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(o -> o.entryCategory() == ShiftEntryCategory.REGULAR));
        assertTrue(result.stream().anyMatch(o -> o.entryCategory() == ShiftEntryCategory.WEEKLY));
    }
}
