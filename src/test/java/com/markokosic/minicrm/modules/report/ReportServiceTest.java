package com.markokosic.minicrm.modules.report;

import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import com.markokosic.minicrm.modules.shift.repository.ShiftRevenueEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ShiftRevenueEntryRepository shiftRevenueEntryRepository;

    @InjectMocks
    private ReportService reportService;

    private Driver driver1;
    private Car car1;
    private Shift shift1;
    private Shift shift2;
    private ShiftRevenueEntry revenueEntry1;
    private ShiftRevenueEntry revenueEntry2;

    @BeforeEach
    void setUp() {
        driver1 = new Driver();
        driver1.setId(1L);
        driver1.setFirstName("John");
        driver1.setLastName("Doe");

        car1 = new Car();
        car1.setId(10L);

        shift1 = new Shift();
        shift1.setId(50L);
        shift1.setDriver(driver1);
        shift1.setCar(car1);
        shift1.setShiftStart(LocalDateTime.of(2025, 5, 10, 8, 0));
        shift1.setShiftEnd(LocalDateTime.of(2025, 5, 10, 16, 0));

        shift2 = new Shift();
        shift2.setId(51L);
        shift2.setDriver(driver1);
        shift2.setCar(car1);
        shift2.setShiftStart(LocalDateTime.of(2025, 5, 15, 8, 0));
        shift2.setShiftEnd(LocalDateTime.of(2025, 5, 15, 16, 0));

        revenueEntry1 = new ShiftRevenueEntry();
        revenueEntry1.setId(100L);
        revenueEntry1.setShift(shift1);
        revenueEntry1.setEntryCategory(ShiftEntryCategory.REGULAR);
        revenueEntry1.setRevenue(new BigDecimal("200.00"));
        revenueEntry1.setCompanyRemuneration(new BigDecimal("80.00"));
        revenueEntry1.setDriverRemuneration(new BigDecimal("120.00"));

        revenueEntry2 = new ShiftRevenueEntry();
        revenueEntry2.setId(101L);
        revenueEntry2.setShift(shift2);
        revenueEntry2.setEntryCategory(ShiftEntryCategory.FLAT_RATE);
        revenueEntry2.setRevenue(new BigDecimal("300.00"));
        revenueEntry2.setCompanyRemuneration(new BigDecimal("120.00"));
        revenueEntry2.setDriverRemuneration(new BigDecimal("180.00"));
    }

    @Test
    void generateDashboardReport_WithMonth_Success() {
        when(shiftRevenueEntryRepository.findRevenuesForReport(any(), any(), isNull()))
                .thenReturn(List.of(revenueEntry1, revenueEntry2));

        DashboardReportDTO report = reportService.generateDashboardReport(2025, 5);

        assertNotNull(report);
        assertEquals(2025, report.year());
        assertEquals(5, report.month());
        assertEquals(new BigDecimal("500.00"), report.totalRevenue());
        assertEquals(new BigDecimal("200.00"), report.companyShare());
        assertEquals(new BigDecimal("300.00"), report.driverShare());
        assertEquals(2L, report.entryCount());
    }

    @Test
    void generateDashboardReport_WithoutMonth() {
        when(shiftRevenueEntryRepository.findRevenuesForReport(any(), any(), isNull()))
                .thenReturn(List.of(revenueEntry1));

        DashboardReportDTO report = reportService.generateDashboardReport(2025, null);

        assertNotNull(report);
        assertEquals(2025, report.year());
        assertNull(report.month());
        assertEquals(new BigDecimal("200.00"), report.totalRevenue());
        assertEquals(1L, report.entryCount());
    }

    @Test
    void generateRevenueReport_GroupByNone() {
        LocalDate dateFrom = LocalDate.of(2025, 5, 1);
        LocalDate dateTo = LocalDate.of(2025, 5, 31);
        when(shiftRevenueEntryRepository.findRevenuesForReport(any(), any(), eq(1L)))
                .thenReturn(List.of(revenueEntry1, revenueEntry2));

        RevenueReportResponseDTO response = reportService.generateRevenueReport(dateFrom, dateTo, 1L, GroupBy.NONE);

        assertNotNull(response);
        assertEquals(2, response.rows().size());
        assertEquals(GroupBy.NONE, response.groupBy());
        assertEquals(new BigDecimal("500.00"), response.totals().revenue());
        assertEquals(50L, response.rows().get(0).shiftId());
        assertEquals(ShiftEntryCategory.REGULAR, response.rows().get(0).entryCategory());
        assertEquals(LocalDate.of(2025, 5, 10), response.rows().get(0).date());
    }

    @Test
    void generateRevenueReport_GroupByDay() {
        LocalDate dateFrom = LocalDate.of(2025, 5, 1);
        LocalDate dateTo = LocalDate.of(2025, 5, 31);
        when(shiftRevenueEntryRepository.findRevenuesForReport(any(), any(), isNull()))
                .thenReturn(List.of(revenueEntry1, revenueEntry2));

        RevenueReportResponseDTO response = reportService.generateRevenueReport(dateFrom, dateTo, null, GroupBy.DAY);

        assertNotNull(response);
        assertEquals(2, response.rows().size());
        assertEquals(GroupBy.DAY, response.groupBy());
    }

    @Test
    void generateRevenueReport_GroupByMonth() {
        LocalDate dateFrom = LocalDate.of(2025, 1, 1);
        LocalDate dateTo = LocalDate.of(2025, 12, 31);
        when(shiftRevenueEntryRepository.findRevenuesForReport(any(), any(), isNull()))
                .thenReturn(List.of(revenueEntry1, revenueEntry2));

        RevenueReportResponseDTO response = reportService.generateRevenueReport(dateFrom, dateTo, null, GroupBy.MONTH);

        assertNotNull(response);
        assertEquals(1, response.rows().size());
        assertEquals(GroupBy.MONTH, response.groupBy());
    }

    @Test
    void generateRevenueReport_GroupByYear() {
        LocalDate dateFrom = LocalDate.of(2025, 1, 1);
        LocalDate dateTo = LocalDate.of(2025, 12, 31);
        when(shiftRevenueEntryRepository.findRevenuesForReport(any(), any(), isNull()))
                .thenReturn(List.of(revenueEntry1, revenueEntry2));

        RevenueReportResponseDTO response = reportService.generateRevenueReport(dateFrom, dateTo, null, GroupBy.YEAR);

        assertNotNull(response);
        assertEquals(1, response.rows().size());
        assertEquals(GroupBy.YEAR, response.groupBy());
    }

    @Test
    void generateRevenueReport_GroupByDriver() {
        LocalDate dateFrom = LocalDate.of(2025, 1, 1);
        LocalDate dateTo = LocalDate.of(2025, 12, 31);
        when(shiftRevenueEntryRepository.findRevenuesForReport(any(), any(), isNull()))
                .thenReturn(List.of(revenueEntry1, revenueEntry2));

        RevenueReportResponseDTO response = reportService.generateRevenueReport(dateFrom, dateTo, null, GroupBy.DRIVER);

        assertNotNull(response);
        assertEquals(1, response.rows().size());
        assertEquals(GroupBy.DRIVER, response.groupBy());
    }

    @Test
    void generateRevenueReport_GroupByCar() {
        LocalDate dateFrom = LocalDate.of(2025, 1, 1);
        LocalDate dateTo = LocalDate.of(2025, 12, 31);
        when(shiftRevenueEntryRepository.findRevenuesForReport(any(), any(), isNull()))
                .thenReturn(List.of(revenueEntry1, revenueEntry2));

        RevenueReportResponseDTO response = reportService.generateRevenueReport(dateFrom, dateTo, null, GroupBy.CAR);

        assertNotNull(response);
        assertEquals(1, response.rows().size());
        assertEquals(GroupBy.CAR, response.groupBy());
    }
}
