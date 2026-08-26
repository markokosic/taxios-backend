package com.markokosic.minicrm.modules.report;

import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import com.markokosic.minicrm.modules.shift.repository.ShiftRevenueEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private final ShiftRevenueEntryRepository shiftRevenueEntryRepository;

    @Transactional(readOnly = true)
    public DashboardReportDTO generateDashboardReport(int year, Integer month) {
        LocalDateTime fromDateTime;
        LocalDateTime toDateTime;

        if (month != null) {
            YearMonth yearMonth = YearMonth.of(year, month);
            fromDateTime = yearMonth.atDay(1).atStartOfDay();
            toDateTime = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
        } else {
            fromDateTime = LocalDate.of(year, 1, 1).atStartOfDay();
            toDateTime = LocalDate.of(year, 12, 31).atTime(LocalTime.MAX);
        }

        List<ShiftRevenueEntry> entries = shiftRevenueEntryRepository.findRevenuesForReport(fromDateTime, toDateTime, null);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCompany = BigDecimal.ZERO;
        BigDecimal totalDriver = BigDecimal.ZERO;
        long entryCount = entries.size();

        for (ShiftRevenueEntry entry : entries) {
            totalRevenue = totalRevenue.add(entry.getRevenue());
            totalCompany = totalCompany.add(entry.getCompanyRemuneration());
            totalDriver = totalDriver.add(entry.getDriverRemuneration());
        }

        return new DashboardReportDTO(
                year,
                month,
                totalRevenue,
                totalCompany,
                totalDriver,
                entryCount
        );
    }

    @Transactional(readOnly = true)
    public RevenueReportResponseDTO generateRevenueReport(
            LocalDate dateFrom,
            LocalDate dateTo,
            Long driverId,
            GroupBy groupBy
    ) {
        GroupBy effectiveGroupBy = groupBy != null ? groupBy : GroupBy.NONE;
        LocalDateTime fromDateTime = dateFrom.atStartOfDay();
        LocalDateTime toDateTime = dateTo.atTime(LocalTime.MAX);

        List<ShiftRevenueEntry> entries = shiftRevenueEntryRepository.findRevenuesForReport(fromDateTime, toDateTime, driverId);

        List<RevenueReportEntryDTO> rows;

        if (effectiveGroupBy == GroupBy.NONE) {
            rows = entries.stream()
                    .map(entry -> {
                        Shift shift = entry.getShift();
                        Driver driver = shift != null ? shift.getDriver() : null;
                        List<DriverInfoDTO> drivers = driver != null
                                ? List.of(new DriverInfoDTO(driver.getId(), driver.getFirstName(), driver.getLastName()))
                                : List.of();
                        LocalDate rowDate = shift != null && shift.getShiftStart() != null
                                ? shift.getShiftStart().toLocalDate()
                                : null;

                        return new RevenueReportEntryDTO(
                                rowDate,
                                shift != null ? shift.getId() : null,
                                entry.getId(),
                                entry.getEntryCategory(),
                                entry.getRevenue(),
                                entry.getCompanyRemuneration(),
                                entry.getDriverRemuneration(),
                                1L,
                                drivers
                        );
                    })
                    .collect(Collectors.toList());
        } else {
            Function<ShiftRevenueEntry, Object> keyExtractor = entry -> {
                Shift shift = entry.getShift();
                LocalDate shiftDate = (shift != null && shift.getShiftStart() != null)
                        ? shift.getShiftStart().toLocalDate()
                        : LocalDate.MIN;

                return switch (effectiveGroupBy) {
                    case DAY -> shiftDate;
                    case MONTH -> shiftDate.withDayOfMonth(1);
                    case YEAR -> shiftDate.withDayOfYear(1);
                    case DRIVER -> (shift != null && shift.getDriver() != null) ? shift.getDriver().getId() : null;
                    case CAR -> (shift != null && shift.getCar() != null) ? shift.getCar().getId() : null;
                    default -> shiftDate;
                };
            };

            Map<Object, List<ShiftRevenueEntry>> grouped = entries.stream()
                    .collect(Collectors.groupingBy(keyExtractor, LinkedHashMap::new, Collectors.toList()));

            rows = grouped.entrySet().stream()
                    .map(groupEntry -> {
                        Object key = groupEntry.getKey();
                        List<ShiftRevenueEntry> list = groupEntry.getValue();

                        LocalDate rowDate = null;
                        if (key instanceof LocalDate) {
                            rowDate = (LocalDate) key;
                        }

                        BigDecimal totalRevenue = BigDecimal.ZERO;
                        BigDecimal totalCompany = BigDecimal.ZERO;
                        BigDecimal totalDriver = BigDecimal.ZERO;
                        Set<DriverInfoDTO> driversSet = new LinkedHashSet<>();

                        for (ShiftRevenueEntry entry : list) {
                            totalRevenue = totalRevenue.add(entry.getRevenue());
                            totalCompany = totalCompany.add(entry.getCompanyRemuneration());
                            totalDriver = totalDriver.add(entry.getDriverRemuneration());

                            if (entry.getShift() != null && entry.getShift().getDriver() != null) {
                                Driver driver = entry.getShift().getDriver();
                                driversSet.add(new DriverInfoDTO(
                                        driver.getId(),
                                        driver.getFirstName(),
                                        driver.getLastName()
                                ));
                            }
                        }

                        return new RevenueReportEntryDTO(
                                rowDate,
                                null,
                                null,
                                null,
                                totalRevenue,
                                totalCompany,
                                totalDriver,
                                (long) list.size(),
                                new ArrayList<>(driversSet)
                        );
                    })
                    .collect(Collectors.toList());
        }

        RevenueReportSummaryDTO totals = calculateRevenueReportTotals(rows);

        return new RevenueReportResponseDTO(dateFrom, dateTo, effectiveGroupBy, totals, rows);
    }

    private RevenueReportSummaryDTO calculateRevenueReportTotals(List<RevenueReportEntryDTO> rows) {
        return rows.stream()
                .reduce(
                        new RevenueReportSummaryDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0),
                        (summary, row) -> new RevenueReportSummaryDTO(
                                summary.revenue().add(row.revenue()),
                                summary.companyShare().add(row.companyRemuneration()),
                                summary.driverShare().add(row.driverRemuneration()),
                                summary.entryCount() + row.entryCount().intValue()
                        ),
                        (s1, s2) -> new RevenueReportSummaryDTO(
                                s1.revenue().add(s2.revenue()),
                                s1.companyShare().add(s2.companyShare()),
                                s1.driverShare().add(s2.driverShare()),
                                s1.entryCount() + s2.entryCount()
                        )
                );
    }
}
