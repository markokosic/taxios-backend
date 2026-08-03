package com.markokosic.minicrm.modules.revenue;

import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.exception.ResourceNotFoundException;
import com.markokosic.minicrm.modules.car.CarRepository;
import com.markokosic.minicrm.modules.car.dto.response.CarSummaryDTO;
import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.driver.dto.response.DriverSummaryDTO;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import com.markokosic.minicrm.modules.driver.service.DriverLookupService;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.remuneration.RemunerationService;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RevenueServiceTest {

    @Mock
    private DriverLookupService driverLookupService;

    @Mock
    private DailyRevenueRepository dailyRevenueRepository;

    @Mock
    private RevenueMapper revenueMapper;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RemunerationService remunerationService;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private RevenueService revenueService;

    @Test
    void testGetAllRevenues_shouldReturnPagedRevenues() {
        // Arrange
        Long driverId = 1L;
        LocalDate dateFrom = LocalDate.now().minusDays(5);
        LocalDate dateTo = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);

        DailyRevenue dailyRevenue = new DailyRevenue();
        dailyRevenue.setId(10L);
        Page<DailyRevenue> page = new PageImpl<>(List.of(dailyRevenue));

        DriverSummaryDTO driverSummary = new DriverSummaryDTO(1L, "Max", "Mustermann");
        CarSummaryDTO carSummary = new CarSummaryDTO(2L, "WI-XX-1234", "VW", "Golf");

        DailyRevenueResponseDTO dto = new DailyRevenueResponseDTO(
                10L, LocalDate.now(), RemunerationModelType.FLAT_RATE, 5L, new BigDecimal("30.00"),
                driverSummary, carSummary, new BigDecimal("10.00"), new BigDecimal("100.00"), new BigDecimal("110.00"),
                new BigDecimal("150.00"), new BigDecimal("120.00"), new BigDecimal("30.00"),
                null, null
        );

        when(dailyRevenueRepository.findAllFiltered(driverId, dateFrom, dateTo, pageable)).thenReturn(page);
        when(revenueMapper.toDto(dailyRevenue)).thenReturn(dto);

        // Act
        PageResponseDTO<DailyRevenueResponseDTO> result = revenueService.getAllRevenues(driverId, dateFrom, dateTo, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(10L, result.getContent().get(0).id());
        verify(dailyRevenueRepository, times(1)).findAllFiltered(driverId, dateFrom, dateTo, pageable);
    }

    @Test
    void testCreateDailyRevenuesBulk_whenValidRequest_shouldSaveRevenues() {
        // Arrange
        Long driverId = 1L;
        Long carId = 2L;
        BigDecimal revenueAmount = new BigDecimal("150.00");

        CreateDailyRevenueRequestDTO dto = new CreateDailyRevenueRequestDTO(
                driverId, carId, LocalDate.now(), new BigDecimal("10.00"), revenueAmount,
                new BigDecimal("100.00"), new BigDecimal("110.00"),
                RemunerationModelType.FLAT_RATE, null, null, null, null, null
        );

        Driver driver = new Driver();
        driver.setId(driverId);

        Car car = new Car();
        car.setId(carId);

        DriverRemunerationConfig config = mock(DriverRemunerationConfig.class);
        when(config.getType()).thenReturn(RemunerationModelType.FLAT_RATE);
        when(config.isCurrent()).thenReturn(true);
        driver.getRemunerationConfigs().add(config);

        RemunerationSplit split = new RemunerationSplit(new BigDecimal("120.00"), new BigDecimal("30.00"));
        DailyRevenue dailyRevenue = new DailyRevenue();

        when(driverRepository.findAllByIdIn(Set.of(driverId))).thenReturn(List.of(driver));
        when(carRepository.findAllById(Set.of(carId))).thenReturn(List.of(car));
        when(remunerationService.calculateRemunerationSplitFromDailyRevenue(revenueAmount, config)).thenReturn(split);
        when(revenueMapper.toEntity(dto, driver, car, config, new BigDecimal("120.00"), new BigDecimal("30.00"))).thenReturn(dailyRevenue);

        // Act
        revenueService.createDailyRevenuesBulk(List.of(dto));

        // Assert
        verify(driverLookupService, times(1)).validateAllExistOrThrow(Set.of(driverId));
        verify(dailyRevenueRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testUpdateDailyRevenue_whenDailyRevenueExists_shouldUpdateAndSave() {
        // Arrange
        Long revenueId = 10L;
        Long driverId = 1L;
        Long carId = 2L;
        BigDecimal revenueAmount = new BigDecimal("150.00");

        CreateDailyRevenueRequestDTO dto = new CreateDailyRevenueRequestDTO(
                driverId, carId, LocalDate.now(), new BigDecimal("10.00"), revenueAmount,
                new BigDecimal("100.00"), new BigDecimal("110.00"),
                RemunerationModelType.FLAT_RATE, null, null, null, null, null
        );

        DailyRevenue dailyRevenue = new DailyRevenue();
        dailyRevenue.setId(revenueId);

        Driver driver = new Driver();
        driver.setId(driverId);
        dailyRevenue.setDriver(driver);

        Car car = new Car();
        car.setId(carId);

        DriverRemunerationConfig config = mock(DriverRemunerationConfig.class);
        when(config.getType()).thenReturn(RemunerationModelType.FLAT_RATE);
        dailyRevenue.setRemunerationConfig(config);
        driver.getRemunerationConfigs().add(config);

        RemunerationSplit split = new RemunerationSplit(new BigDecimal("120.00"), new BigDecimal("30.00"));
        DailyRevenueResponseDTO expectedResponse = mock(DailyRevenueResponseDTO.class);

        when(dailyRevenueRepository.findById(revenueId)).thenReturn(Optional.of(dailyRevenue));
        when(driverLookupService.validateDriverExistsOrThrow(driverId)).thenReturn(driver);
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(remunerationService.calculateRemunerationSplitFromDailyRevenue(revenueAmount, config)).thenReturn(split);
        when(revenueMapper.toDto(dailyRevenue)).thenReturn(expectedResponse);

        // Act
        DailyRevenueResponseDTO result = revenueService.updateDailyRevenue(revenueId, dto);

        // Assert
        assertNotNull(result);
        verify(dailyRevenueRepository, times(1)).findById(revenueId);
        verify(dailyRevenueRepository, times(1)).save(dailyRevenue);
        verify(revenueMapper, times(1)).updateEntityFromDto(dto, dailyRevenue, driver, car, config, new BigDecimal("120.00"), new BigDecimal("30.00"));
    }

    @Test
    void testDeleteDailyRevenue_whenRevenueExists_shouldDeleteIt() {
        // Arrange
        Long revenueId = 10L;
        DailyRevenue dailyRevenue = new DailyRevenue();
        dailyRevenue.setId(revenueId);

        when(dailyRevenueRepository.findById(revenueId)).thenReturn(Optional.of(dailyRevenue));

        // Act
        revenueService.deleteDailyRevenue(revenueId);

        // Assert
        verify(dailyRevenueRepository, times(1)).findById(revenueId);
        verify(dailyRevenueRepository, times(1)).delete(dailyRevenue);
    }

    @Test
    void testDeleteDailyRevenue_whenDoesNotExist_shouldThrowResourceNotFoundException() {
        // Arrange
        Long revenueId = 10L;
        when(dailyRevenueRepository.findById(revenueId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                revenueService.deleteDailyRevenue(revenueId)
        );
        verify(dailyRevenueRepository, never()).delete(any(DailyRevenue.class));
    }
}
