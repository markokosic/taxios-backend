package com.markokosic.minicrm.modules.report;

import com.markokosic.minicrm.modules.revenue.DailyRevenueRepository;
import com.markokosic.minicrm.modules.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final DailyRevenueRepository dailyRevenueRepository;
    private final TenantService tenantService;

    @Transactional(readOnly = true)
    public RevenueReportResponseDTO generateRevenueReport(
            LocalDate dateFrom,
            LocalDate dateTo,
            Long driverId,
            GroupBy groupBy
    ) {
        Long tenantId = tenantService.getTenantIdFromContextHolder();


        List<RawReportRow> rawRows = switch (groupBy) {
            case NONE -> dailyRevenueRepository.findRowsRaw(dateFrom, dateTo, tenantId, driverId);
            case DAY -> dailyRevenueRepository.findRowsGroupedByDay(dateFrom, dateTo, tenantId, driverId);
            default -> throw new UnsupportedOperationException("Grouping not yet implemented: " + groupBy);
        };


        List<RevenueReportEntryDTO> rows = rawRows.stream()
                .map(row -> new RevenueReportEntryDTO(
                        row.date(),
                        row.revenue(),
                        row.companyShare(),
                        row.driverShare(),
                        row.km(),
                        row.count()
                ))
                .collect(Collectors.toList());


        RevenueReportSummaryDTO totals = calculateRevenueReportTotals(rows);

        return new RevenueReportResponseDTO(dateFrom, dateTo, groupBy, totals, rows);
    }

    private RevenueReportSummaryDTO calculateRevenueReportTotals(List<RevenueReportEntryDTO> rows) {
        return rows.stream()
                .reduce(
                        new RevenueReportSummaryDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0),
                        (summary, row) -> new RevenueReportSummaryDTO(
                                summary.revenue().add(row.revenue()),
                                summary.companyShare().add(row.companyRemuneration()),
                                summary.totalKm().add(row.kilometersDriven()),
                                summary.entryCount() + row.entryCount().intValue()
                        ),
                        (s1, s2) -> new RevenueReportSummaryDTO(
                                s1.revenue().add(s2.revenue()),
                                s1.companyShare().add(s2.companyShare()),
                                s1.totalKm().add(s2.totalKm()),
                                s1.entryCount() + s2.entryCount()
                        )
                );
    }
}
