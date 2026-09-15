package com.markokosic.minicrm.modules.shift.service;

import com.markokosic.minicrm.exception.ForbiddenException;
import com.markokosic.minicrm.modules.car.CarRepository;
import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import com.markokosic.minicrm.modules.flatratetype.repository.FlatRateTypeRepository;
import com.markokosic.minicrm.modules.shift.ShiftMapper;
import com.markokosic.minicrm.modules.shift.ShiftRevenueEntryMapper;
import com.markokosic.minicrm.modules.shift.dto.request.CreateMyShiftRequestDTO;
import com.markokosic.minicrm.modules.shift.dto.request.CreateShiftRevenueEntryRequestDTO;
import com.markokosic.minicrm.modules.shift.dto.request.UpdateShiftRequestDTO;
import com.markokosic.minicrm.modules.shift.dto.request.UpdateShiftRevenueEntryRequestDTO;
import com.markokosic.minicrm.modules.shift.dto.response.ShiftResponseDTO;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import com.markokosic.minicrm.modules.shift.model.ShiftStatus;
import com.markokosic.minicrm.modules.shift.repository.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private DriverRepository driverRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private FlatRateTypeRepository flatRateTypeRepository;
    @Mock
    private ShiftRepository shiftRepository;
    @Mock
    private ShiftSettlementCalculator shiftSettlementCalculator;
    @Mock
    private ShiftMapper shiftMapper;
    @Mock
    private ShiftRevenueEntryMapper shiftRevenueEntryMapper;

    @InjectMocks
    private ShiftService shiftService;

    private Driver driver;
    private Car car;
    private DriverRemunerationConfig config;
    private Shift shift;
    private ShiftRevenueEntry entry1;
    private ShiftRevenueEntry entry2;

    @BeforeEach
    void setUp() {
        driver = mock(Driver.class);
        car = new Car();
        car.setId(10L);

        config = mock(DriverRemunerationConfig.class);

        shift = new Shift();
        shift.setId(50L);
        shift.setDriver(driver);
        shift.setCar(car);
        shift.setOdometerStart(new BigDecimal("100.00"));
        shift.setOdometerEnd(new BigDecimal("200.00"));
        shift.setShiftStart(LocalDateTime.of(2025, 5, 10, 8, 0));
        shift.setShiftEnd(LocalDateTime.of(2025, 5, 10, 16, 0));
        shift.setRevenues(new ArrayList<>());
        
        lenient().when(config.getType()).thenReturn(com.markokosic.minicrm.modules.remuneration.RemunerationModelType.PERCENTAGE_SHARE);
        shift.getAppliedRemunerationConfigs().add(config);

        entry1 = new ShiftRevenueEntry();
        entry1.setId(101L);
        entry1.setEntryCategory(ShiftEntryCategory.REGULAR);
        entry1.setRevenue(new BigDecimal("150.00"));
        shift.addRevenueEntry(entry1);

        entry2 = new ShiftRevenueEntry();
        entry2.setId(102L);
        entry2.setEntryCategory(ShiftEntryCategory.FLAT_RATE);
        entry2.setRevenue(new BigDecimal("50.00"));
        shift.addRevenueEntry(entry2);
    }

    @Test
    void updateShift_withUpdate_Add_and_Delete() {
        when(shiftRepository.findById(50L)).thenReturn(Optional.of(shift));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShiftRevenueEntry newlyCreatedEntry = new ShiftRevenueEntry();
        newlyCreatedEntry.setEntryCategory(ShiftEntryCategory.REGULAR);
        newlyCreatedEntry.setRevenue(new BigDecimal("80.00"));
        when(shiftRevenueEntryMapper.toEntity(any(), any(), any(), any(), any(), any()))
                .thenReturn(newlyCreatedEntry);

        // Request:
        // - entry 101 updated to 200.00
        // - entry 102 omitted (should be deleted)
        // - new entry added (id = null) with 80.00
        UpdateShiftRevenueEntryRequestDTO updateReq1 = new UpdateShiftRevenueEntryRequestDTO(
                101L, ShiftEntryCategory.REGULAR, null, new BigDecimal("200.00"), null, null
        );
        UpdateShiftRevenueEntryRequestDTO newReq = new UpdateShiftRevenueEntryRequestDTO(
                null, ShiftEntryCategory.REGULAR, null, new BigDecimal("80.00"), null, null
        );

        UpdateShiftRequestDTO request = new UpdateShiftRequestDTO(
                null,
                new BigDecimal("150.00"),
                new BigDecimal("300.00"),
                LocalDateTime.of(2025, 5, 10, 9, 0),
                LocalDateTime.of(2025, 5, 10, 17, 0),
                null,
                List.of(updateReq1, newReq)
        );

        shiftService.updateShift(50L, request);

        // Verify metadata updated
        assertEquals(new BigDecimal("150.00"), shift.getOdometerStart());
        assertEquals(new BigDecimal("300.00"), shift.getOdometerEnd());
        assertEquals(LocalDateTime.of(2025, 5, 10, 9, 0), shift.getShiftStart());
        assertEquals(LocalDateTime.of(2025, 5, 10, 17, 0), shift.getShiftEnd());

        // Verify entries: entry2 (102L) should be removed, entry1 (101L) updated, new entry added
        assertEquals(2, shift.getRevenues().size());
        assertTrue(shift.getRevenues().stream().anyMatch(e -> Long.valueOf(101L).equals(e.getId()) && e.getRevenue().compareTo(new BigDecimal("200.00")) == 0));
        assertTrue(shift.getRevenues().stream().anyMatch(e -> e.getId() == null && e.getRevenue().compareTo(new BigDecimal("80.00")) == 0));
        assertFalse(shift.getRevenues().stream().anyMatch(e -> Long.valueOf(102L).equals(e.getId())));
        verify(shiftSettlementCalculator).calculate(eq(shift), isNull());
    }

    @Test
    void getMyShiftById_Success() {
        Driver driver = new Driver();
        driver.setId(10L);
        shift.setDriver(driver);

        when(driverRepository.findByUserId(5L)).thenReturn(Optional.of(driver));
        when(shiftRepository.findById(50L)).thenReturn(Optional.of(shift));
        when(shiftMapper.toDto(shift)).thenReturn(mock(ShiftResponseDTO.class));

        var result = shiftService.getMyShiftById(5L, 50L);

        assertNotNull(result);
        verify(driverRepository).findByUserId(5L);
        verify(shiftRepository).findById(50L);
    }

    @Test
    void getMyShiftById_ThrowsNotFound_WhenShiftBelongsToAnotherDriver() {
        Driver myDriver = new Driver();
        myDriver.setId(10L);
        Driver otherDriver = new Driver();
        otherDriver.setId(99L);
        shift.setDriver(otherDriver);

        when(driverRepository.findByUserId(5L)).thenReturn(Optional.of(myDriver));
        when(shiftRepository.findById(50L)).thenReturn(Optional.of(shift));

        assertThrows(com.markokosic.minicrm.exception.ResourceNotFoundException.class,
                () -> shiftService.getMyShiftById(5L, 50L));
    }

    @Test
    void getMyShifts_Success() {
        Driver driver = new Driver();
        driver.setId(10L);

        when(driverRepository.findByUserId(5L)).thenReturn(Optional.of(driver));
        when(shiftRepository.findAllFiltered(eq(10L), isNull(), isNull(), isNull(), any())).thenReturn(org.springframework.data.domain.Page.empty());

        var result = shiftService.getMyShifts(5L, org.springframework.data.domain.PageRequest.of(0, 10));

        assertNotNull(result);
        verify(driverRepository).findByUserId(5L);
        verify(shiftRepository).findAllFiltered(eq(10L), isNull(), isNull(), isNull(), any());
    }

    @Test
    void createMyShift_Success() {
        when(driver.getId()).thenReturn(10L);
        when(driverRepository.findByUserId(5L)).thenReturn(Optional.of(driver));
        when(driverRepository.findById(10L)).thenReturn(Optional.of(driver));
        when(driver.getActiveRemunerationConfigs()).thenReturn(List.of(config));
        when(carRepository.findById(10L)).thenReturn(Optional.of(car));
        when(shiftRevenueEntryMapper.toEntity(any(), any(), any(), any(), any(), any())).thenReturn(entry1);
        when(shiftMapper.toShiftEntity(any(), eq(driver), eq(car), eq(ShiftStatus.PENDING))).thenReturn(shift);
        when(shiftRepository.save(any())).thenReturn(shift);
        when(shiftMapper.toDto(any())).thenReturn(mock(ShiftResponseDTO.class));

        var revenueEntry = new CreateShiftRevenueEntryRequestDTO(
                ShiftEntryCategory.REGULAR, null, new BigDecimal("150.00"), null, null
        );
        var request = new CreateMyShiftRequestDTO(
                10L, new BigDecimal("100.00"), new BigDecimal("200.00"),
                LocalDateTime.of(2025, 5, 10, 8, 0), LocalDateTime.of(2025, 5, 10, 16, 0),
                null,
                List.of(revenueEntry)
        );

        var result = shiftService.createMyShift(5L, request);

        assertNotNull(result);
        verify(driverRepository).findByUserId(5L);
        verify(shiftSettlementCalculator).calculate(eq(shift), isNull());
        verify(shiftRepository).save(any());
    }

    @Test
    void updateMyShift_Success_WhenPending_AndCarUpdated() {
        when(driver.getId()).thenReturn(10L);
        shift.setStatus(ShiftStatus.PENDING);

        Car newCar = new Car();
        newCar.setId(20L);

        when(driverRepository.findByUserId(5L)).thenReturn(Optional.of(driver));
        when(shiftRepository.findById(50L)).thenReturn(Optional.of(shift));
        when(carRepository.findById(20L)).thenReturn(Optional.of(newCar));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shiftMapper.toDto(any())).thenReturn(mock(ShiftResponseDTO.class));

        UpdateShiftRequestDTO request = new UpdateShiftRequestDTO(
                20L,
                new BigDecimal("150.00"), new BigDecimal("300.00"),
                LocalDateTime.of(2025, 5, 10, 9, 0), LocalDateTime.of(2025, 5, 10, 17, 0),
                null,
                List.of()
        );

        var result = shiftService.updateMyShift(5L, 50L, request);

        assertNotNull(result);
        assertEquals(20L, shift.getCar().getId());
        verify(shiftSettlementCalculator).calculate(eq(shift), isNull());
        verify(shiftRepository).save(shift);
    }

    @Test
    void updateMyShift_ThrowsForbidden_WhenApproved() {
        when(driver.getId()).thenReturn(10L);
        shift.setStatus(ShiftStatus.APPROVED);

        when(driverRepository.findByUserId(5L)).thenReturn(Optional.of(driver));
        when(shiftRepository.findById(50L)).thenReturn(Optional.of(shift));

        UpdateShiftRequestDTO request = new UpdateShiftRequestDTO(
                null,
                new BigDecimal("150.00"), new BigDecimal("300.00"),
                LocalDateTime.of(2025, 5, 10, 9, 0), LocalDateTime.of(2025, 5, 10, 17, 0),
                null,
                List.of()
        );

        assertThrows(ForbiddenException.class, () -> shiftService.updateMyShift(5L, 50L, request));
    }

    @Test
    void deleteMyShift_Success_WhenPending() {
        when(driver.getId()).thenReturn(10L);
        shift.setStatus(ShiftStatus.PENDING);

        when(driverRepository.findByUserId(5L)).thenReturn(Optional.of(driver));
        when(shiftRepository.findById(50L)).thenReturn(Optional.of(shift));

        shiftService.deleteMyShift(5L, 50L);

        verify(shiftRepository).delete(shift);
    }

    @Test
    void deleteMyShift_ThrowsForbidden_WhenApproved() {
        when(driver.getId()).thenReturn(10L);
        shift.setStatus(ShiftStatus.APPROVED);

        when(driverRepository.findByUserId(5L)).thenReturn(Optional.of(driver));
        when(shiftRepository.findById(50L)).thenReturn(Optional.of(shift));

        assertThrows(ForbiddenException.class, () -> shiftService.deleteMyShift(5L, 50L));
    }

    @Test
    void approveShift_Success_CalculatesSettlement() {
        when(shiftRepository.findById(50L)).thenReturn(Optional.of(shift));
        when(shiftMapper.toDto(shift)).thenReturn(mock(ShiftResponseDTO.class));

        var result = shiftService.approveShift(50L);

        assertNotNull(result);
        assertEquals(ShiftStatus.APPROVED, shift.getStatus());
        verify(shiftSettlementCalculator).calculate(eq(shift));
    }

    @Test
    void updateShift_withWeeklyRent_CallsCalculatorWithRent() {
        when(shiftRepository.findById(50L)).thenReturn(Optional.of(shift));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateShiftRequestDTO request = new UpdateShiftRequestDTO(
                null,
                new BigDecimal("100.00"),
                new BigDecimal("200.00"),
                LocalDateTime.of(2025, 5, 10, 8, 0),
                LocalDateTime.of(2025, 5, 10, 16, 0),
                new BigDecimal("400.00"),
                List.of()
        );

        shiftService.updateShift(50L, request);

        verify(shiftSettlementCalculator).calculate(eq(shift), eq(new BigDecimal("400.00")));
    }

    @Test
    void updateShift_withFlatRateConnectedDefaultPrice_EnforcesDefaultPrice() {
        when(shiftRepository.findById(50L)).thenReturn(Optional.of(shift));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlatRateType flatRateType = new FlatRateType();
        flatRateType.setDefaultPrice(new BigDecimal("40.00"));
        entry2.setFlatRateType(flatRateType);

        UpdateShiftRevenueEntryRequestDTO flatRateReq = new UpdateShiftRevenueEntryRequestDTO(
                102L, ShiftEntryCategory.FLAT_RATE, null, null, 2L, new BigDecimal("999.00")
        );

        UpdateShiftRequestDTO request = new UpdateShiftRequestDTO(
                null,
                new BigDecimal("100.00"),
                new BigDecimal("200.00"),
                LocalDateTime.of(2025, 5, 10, 8, 0),
                LocalDateTime.of(2025, 5, 10, 16, 0),
                null,
                List.of(flatRateReq)
        );

        shiftService.updateShift(50L, request);

        assertEquals(1, shift.getRevenues().size());
        ShiftRevenueEntry updatedFlat = shift.getRevenues().get(0);
        assertEquals(new BigDecimal("40.00"), updatedFlat.getPricePerTrip());
        assertEquals(new BigDecimal("80.00"), updatedFlat.getRevenue());
        verify(shiftSettlementCalculator).calculate(eq(shift), isNull());
    }

    @Test
    void updateShift_withFlatRateCustomPrice_UsesRequestedPrice() {
        when(shiftRepository.findById(50L)).thenReturn(Optional.of(shift));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

        entry2.setFlatRateType(null); // Custom flat rate without connected FlatRateType

        UpdateShiftRevenueEntryRequestDTO flatRateReq = new UpdateShiftRevenueEntryRequestDTO(
                102L, ShiftEntryCategory.FLAT_RATE, null, null, 2L, new BigDecimal("25.00")
        );

        UpdateShiftRequestDTO request = new UpdateShiftRequestDTO(
                null,
                new BigDecimal("100.00"),
                new BigDecimal("200.00"),
                LocalDateTime.of(2025, 5, 10, 8, 0),
                LocalDateTime.of(2025, 5, 10, 16, 0),
                null,
                List.of(flatRateReq)
        );

        shiftService.updateShift(50L, request);

        assertEquals(1, shift.getRevenues().size());
        ShiftRevenueEntry updatedFlat = shift.getRevenues().get(0);
        assertEquals(new BigDecimal("25.00"), updatedFlat.getPricePerTrip());
        assertEquals(new BigDecimal("50.00"), updatedFlat.getRevenue());
        verify(shiftSettlementCalculator).calculate(eq(shift), isNull());
    }
}
