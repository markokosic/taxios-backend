package com.markokosic.minicrm.modules.report;

import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.revenue.DailyRevenue;
import com.markokosic.minicrm.modules.revenue.DailyRevenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private DailyRevenueRepository dailyRevenueRepository;

    @InjectMocks
    private ReportService reportService;

    private Driver driver1;
    private Car car1;
    private DailyRevenue revenue1;
    private DailyRevenue revenue2;

    @BeforeEach
    void setUp() {
        driver1 = new Driver();
        driver1.setId(1L);
        driver1.setFirstName("John");
        driver1.setLastName("Doe");

        car1 = new Car();
        car1.setId(10L);

        revenue1 = new DailyRevenue();
        revenue1.setId(100L);
        revenue1.setDate(LocalDate.of(2025, 5, 10));
        revenue1.setRevenue(new BigDecimal("200.00"));
        revenue1.setCompanyRemuneration(new BigDecimal("80.00"));
        revenue1.setDriverRemuneration(new BigDecimal("120.00"));
        revenue1.setKilometersDriven(new BigDecimal("100.00"));
        revenue1.setDriver(driver1);
        revenue1.setCar(car1);

        revenue2 = new DailyRevenue();
        revenue2.setId(101L);
        revenue2.setDate(LocalDate.of(2025, 5, 15));
        revenue2.setRevenue(new BigDecimal("300.00"));
        revenue2.setCompanyRemuneration(new BigDecimal("120.00"));
        revenue2.setDriverRemuneration(new BigDecimal("180.00"));
        revenue2.setKilometersDriven(new BigDecimal("150.00"));
        revenue2.setDriver(driver1);
        revenue2.setCar(car1);
    }

    @Test
    void generateDashboardReport_WithMonth_Success() {
        when(dailyRevenueRepository.findRawRevenues(any(), any(), isNull()))
                .thenReturn(List.of(revenue1, revenue2));

        DashboardReportDTO report = reportService.generateDashboardReport(2025, 5);

        assertNotNull(report);
        assertEquals(2025, report.year());
        assertEquals(5, report.month());
        assertEquals(new BigDecimal("500.00"), report.totalRevenue());
        assertEquals(new BigDecimal("200.00"), report.companyShare());
        assertEquals(new BigDecimal("300.00"), report.driverShare());
        assertEquals(new BigDecimal("250.00"), report.totalKm());
        assertEquals(new BigDecimal("2.00"), report.revenuePerKm());
        assertEquals(2L, report.tripCount());
    }

    @Test
    void generateDashboardReport_WithoutMonth_AndZeroKm() {
        revenue1.setKilometersDriven(BigDecimal.ZERO);
        revenue1.setRevenue(BigDecimal.ZERO);
        when(dailyRevenueRepository.findRawRevenues(any(), any(), isNull()))
                .thenReturn(List.of(revenue1));

        DashboardReportDTO report = reportService.generateDashboardReport(2025, null);

        assertNotNull(report);
        assertEquals(2025, report.year());
        assertNull(report.month());
        assertEquals(BigDecimal.ZERO, report.revenuePerKm());
    }

    @Test
    void generateRevenueReport_GroupByNone() {
        LocalDate dateFrom = LocalDate.of(2025, 5, 1);
        LocalDate dateTo = LocalDate.of(2025, 5, 31);
        when(dailyRevenueRepository.findRawRevenues(dateFrom, dateTo, 1L))
                .thenReturn(List.of(revenue1, revenue2));

        RevenueReportResponseDTO response = reportService.generateRevenueReport(dateFrom, dateTo, 1L, GroupBy.NONE);

        assertNotNull(response);
        assertEquals(2, response.rows().size());
        assertEquals(GroupBy.NONE, response.groupBy());
        assertEquals(new BigDecimal("500.00"), response.totals().revenue());
    }

    @Test
    void generateRevenueReport_GroupByDay() {
        LocalDate dateFrom = LocalDate.of(2025, 5, 1);
        LocalDate dateTo = LocalDate.of(2025, 5, 31);
        when(dailyRevenueRepository.findRawRevenues(dateFrom, dateTo, null))
                .thenReturn(List.of(revenue1, revenue2));

        RevenueReportResponseDTO response = reportService.generateRevenueReport(dateFrom, dateTo, null, GroupBy.DAY);

        assertNotNull(response);
        assertEquals(2, response.rows().size());
        assertEquals(GroupBy.DAY, response.groupBy());
    }

    @Test
    void generateRevenueReport_GroupByMonth() {
        LocalDate dateFrom = LocalDate.of(2025, 1, 1);
        LocalDate dateTo = LocalDate.of(2025, 12, 31);
        when(dailyRevenueRepository.findRawRevenues(dateFrom, dateTo, null))
                .thenReturn(List.of(revenue1, revenue2));

        RevenueReportResponseDTO response = reportService.generateRevenueReport(dateFrom, dateTo, null, GroupBy.MONTH);

        assertNotNull(response);
        assertEquals(1, response.rows().size());
        assertEquals(GroupBy.MONTH, response.groupBy());
    }

    @Test
    void generateRevenueReport_GroupByYear() {
        LocalDate dateFrom = LocalDate.of(2025, 1, 1);
        LocalDate dateTo = LocalDate.of(2025, 12, 31);
        when(dailyRevenueRepository.findRawRevenues(dateFrom, dateTo, null))
                .thenReturn(List.of(revenue1, revenue2));

        RevenueReportResponseDTO response = reportService.generateRevenueReport(dateFrom, dateTo, null, GroupBy.YEAR);

        assertNotNull(response);
        assertEquals(1, response.rows().size());
        assertEquals(GroupBy.YEAR, response.groupBy());
    }

    @Test
    void generateRevenueReport_GroupByDriver() {
        LocalDate dateFrom = LocalDate.of(2025, 1, 1);
        LocalDate dateTo = LocalDate.of(2025, 12, 31);
        when(dailyRevenueRepository.findRawRevenues(dateFrom, dateTo, null))
                .thenReturn(List.of(revenue1, revenue2));

        RevenueReportResponseDTO response = reportService.generateRevenueReport(dateFrom, dateTo, null, GroupBy.DRIVER);

        assertNotNull(response);
        assertEquals(1, response.rows().size());
        assertEquals(GroupBy.DRIVER, response.groupBy());
    }

    @Test
    void generateRevenueReport_GroupByCar() {
        LocalDate dateFrom = LocalDate.of(2025, 1, 1);
        LocalDate dateTo = LocalDate.of(2025, 12, 31);
        when(dailyRevenueRepository.findRawRevenues(dateFrom, dateTo, null))
                .thenReturn(List.of(revenue1, revenue2));

        RevenueReportResponseDTO response = reportService.generateRevenueReport(dateFrom, dateTo, null, GroupBy.CAR);

        assertNotNull(response);
        assertEquals(1, response.rows().size());
        assertEquals(GroupBy.CAR, response.groupBy());
    }
}
