package com.markokosic.minicrm.modules.report;

import com.markokosic.minicrm.modules.revenue.DailyRevenue;
import com.markokosic.minicrm.modules.revenue.DailyRevenueRepository;
import com.markokosic.minicrm.modules.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final DailyRevenueRepository dailyRevenueRepository;
    private final TenantService tenantService;

    @Transactional(readOnly = true)
    public DashboardReportDTO generateDashboardReport(int year, Integer month) {
        Long tenantId = tenantService.getTenantIdFromContextHolder();
        LocalDate dateFrom;
        LocalDate dateTo;

        if (month != null) {
            YearMonth yearMonth = YearMonth.of(year, month);
            dateFrom = yearMonth.atDay(1);
            dateTo = yearMonth.atEndOfMonth();
        } else {
            dateFrom = LocalDate.of(year, 1, 1);
            dateTo = LocalDate.of(year, 12, 31);
        }

        List<DailyRevenue> rawRevenues = dailyRevenueRepository.findRawRevenues(dateFrom, dateTo, tenantId, null);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCompany = BigDecimal.ZERO;
        BigDecimal totalDriver = BigDecimal.ZERO;
        BigDecimal totalKm = BigDecimal.ZERO;
        long tripCount = rawRevenues.size();

        for (DailyRevenue dr : rawRevenues) {
            totalRevenue = totalRevenue.add(dr.getRevenue());
            totalCompany = totalCompany.add(dr.getCompanyRemuneration());
            totalDriver = totalDriver.add(dr.getDriverRemuneration());
            totalKm = totalKm.add(dr.getKilometersDriven());
        }

        BigDecimal revenuePerKm = BigDecimal.ZERO;
        if (totalKm.compareTo(BigDecimal.ZERO) > 0) {
            revenuePerKm = totalRevenue.divide(totalKm, 2, RoundingMode.HALF_UP);
        }

        return new DashboardReportDTO(
                year,
                month,
                totalRevenue,
                totalCompany,
                totalDriver,
                totalKm,
                revenuePerKm,
                tripCount
        );
    }

    @Transactional(readOnly = true)
    public RevenueReportResponseDTO generateRevenueReport(
            LocalDate dateFrom,
            LocalDate dateTo,
            Long driverId,
            GroupBy groupBy
    ) {
        Long tenantId = tenantService.getTenantIdFromContextHolder();

        List<DailyRevenue> rawRevenues = dailyRevenueRepository.findRawRevenues(dateFrom, dateTo, tenantId, driverId);

        List<RevenueReportEntryDTO> rows;

        if (groupBy == GroupBy.NONE) {
            rows = rawRevenues.stream()
                    .map(dr -> new RevenueReportEntryDTO(
                            dr.getDate(),
                            dr.getRevenue(),
                            dr.getCompanyRemuneration(),
                            dr.getDriverRemuneration(),
                            dr.getKilometersDriven(),
                            1L,
                            List.of(new DriverInfoDTO(
                                    dr.getDriver().getId(),
                                    dr.getDriver().getFirstName(),
                                    dr.getDriver().getLastName()
                            ))
                    ))
                    .collect(Collectors.toList());
        } else {

            Function<DailyRevenue, Object> keyExtractor = dr -> {
                return switch (groupBy) {
                    case DAY -> dr.getDate();
                    case MONTH -> dr.getDate().withDayOfMonth(1);
                    case YEAR -> dr.getDate().withDayOfYear(1);
                    case DRIVER -> dr.getDriver().getId();
                    case CAR -> dr.getCar().getId();
                    default -> dr.getDate();
                };
            };


            Map<Object, List<DailyRevenue>> grouped = rawRevenues.stream()
                    .collect(Collectors.groupingBy(keyExtractor, LinkedHashMap::new, Collectors.toList()));

            rows = grouped.entrySet().stream()
                    .map(entry -> {
                        Object key = entry.getKey();
                        List<DailyRevenue> list = entry.getValue();

                        LocalDate rowDate = null;
                        if (key instanceof LocalDate) {
                            rowDate = (LocalDate) key;
                        }

                        BigDecimal totalRevenue = BigDecimal.ZERO;
                        BigDecimal totalCompany = BigDecimal.ZERO;
                        BigDecimal totalDriver = BigDecimal.ZERO;
                        BigDecimal totalKm = BigDecimal.ZERO;

                        Set<DriverInfoDTO> driversSet = new LinkedHashSet<>();

                        for (DailyRevenue dr : list) {
                            totalRevenue = totalRevenue.add(dr.getRevenue());
                            totalCompany = totalCompany.add(dr.getCompanyRemuneration());
                            totalDriver = totalDriver.add(dr.getDriverRemuneration());
                            totalKm = totalKm.add(dr.getKilometersDriven());
                            driversSet.add(new DriverInfoDTO(
                                    dr.getDriver().getId(),
                                    dr.getDriver().getFirstName(),
                                    dr.getDriver().getLastName()
                            ));
                        }

                        return new RevenueReportEntryDTO(
                                rowDate,
                                totalRevenue,
                                totalCompany,
                                totalDriver,
                                totalKm,
                                (long) list.size(),
                                new ArrayList<>(driversSet)
                        );
                    })
                    .collect(Collectors.toList());
        }

        RevenueReportSummaryDTO totals = calculateRevenueReportTotals(rows);

        return new RevenueReportResponseDTO(dateFrom, dateTo, groupBy, totals, rows);
    }

    private RevenueReportSummaryDTO calculateRevenueReportTotals(List<RevenueReportEntryDTO> rows) {
        return rows.stream()
                .reduce(
                        new RevenueReportSummaryDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0),
                        (summary, row) -> new RevenueReportSummaryDTO(
                                summary.revenue().add(row.revenue()),
                                summary.companyShare().add(row.companyRemuneration()),
                                summary.driverShare().add(row.driverRemuneration()),
                                summary.totalKm().add(row.kilometersDriven()),
                                summary.entryCount() + row.entryCount().intValue()
                        ),
                        (s1, s2) -> new RevenueReportSummaryDTO(
                                s1.revenue().add(s2.revenue()),
                                s1.companyShare().add(s2.companyShare()),
                                s1.driverShare().add(s2.driverShare()),
                                s1.totalKm().add(s2.totalKm()),
                                s1.entryCount() + s2.entryCount()
                        )
                );
    }
}
