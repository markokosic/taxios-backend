package com.markokosic.minicrm.modules.shift.service;

import com.markokosic.minicrm.modules.car.CarRepository;
import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import com.markokosic.minicrm.modules.remuneration.RemunerationService;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import com.markokosic.minicrm.modules.shift.ShiftMapper;
import com.markokosic.minicrm.modules.shift.ShiftRevenueEntryMapper;
import com.markokosic.minicrm.modules.shift.dto.request.UpdateShiftRequestDTO;
import com.markokosic.minicrm.modules.shift.dto.request.UpdateShiftRevenueEntryRequestDTO;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import com.markokosic.minicrm.modules.flatratetype.repository.FlatRateTypeRepository;
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
    private RemunerationService remunerationService;
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

        entry1 = new ShiftRevenueEntry();
        entry1.setId(101L);
        entry1.setEntryCategory(ShiftEntryCategory.REGULAR);
        entry1.setRevenue(new BigDecimal("150.00"));
        entry1.setRemunerationConfig(config);
        shift.addRevenueEntry(entry1);

        entry2 = new ShiftRevenueEntry();
        entry2.setId(102L);
        entry2.setEntryCategory(ShiftEntryCategory.FLAT_RATE);
        entry2.setRevenue(new BigDecimal("50.00"));
        entry2.setRemunerationConfig(config);
        shift.addRevenueEntry(entry2);
    }

    @Test
    void updateShift_withUpdate_Add_and_Delete() {
        when(shiftRepository.findById(50L)).thenReturn(Optional.of(shift));
        when(driver.getRemunerationConfigForEntry(eq(ShiftEntryCategory.REGULAR), any())).thenReturn(config);
        when(remunerationService.calculateRemunerationSplit(any(), any()))
                .thenReturn(new RemunerationSplit(new BigDecimal("60.00"), new BigDecimal("140.00")));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShiftRevenueEntry newlyCreatedEntry = new ShiftRevenueEntry();
        newlyCreatedEntry.setEntryCategory(ShiftEntryCategory.REGULAR);
        newlyCreatedEntry.setRevenue(new BigDecimal("80.00"));
        when(shiftRevenueEntryMapper.toEntity(any(), any(), any(), any(), any(), any(), any(), any()))
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
                new BigDecimal("150.00"),
                new BigDecimal("300.00"),
                LocalDateTime.of(2025, 5, 10, 9, 0),
                LocalDateTime.of(2025, 5, 10, 17, 0),
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
    }

    @Test
    void getMyShifts_Success() {
        Driver driver = new Driver();
        driver.setId(10L);

        when(driverRepository.findByUserId(5L)).thenReturn(Optional.of(driver));
        when(shiftRepository.findAllFiltered(eq(10L), isNull(), isNull(), any())).thenReturn(org.springframework.data.domain.Page.empty());

        var result = shiftService.getMyShifts(5L, org.springframework.data.domain.PageRequest.of(0, 10));

        assertNotNull(result);
        verify(driverRepository).findByUserId(5L);
        verify(shiftRepository).findAllFiltered(eq(10L), isNull(), isNull(), any());
    }
}
