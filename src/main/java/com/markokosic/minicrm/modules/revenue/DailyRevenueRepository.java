package com.markokosic.minicrm.modules.revenue;

import com.markokosic.minicrm.modules.report.RawReportRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyRevenueRepository extends JpaRepository<DailyRevenue, Long> {

    // Case: NONE (Raw entries, each counts as 1)
    @Query("""
        SELECT new com.markokosic.minicrm.modules.report.RawReportRow(
            null, null, dr.date, 
            dr.revenue, dr.companyRemuneration, dr.driverRemuneration, dr.kilometersDriven, 
            1L
        )
        FROM DailyRevenue dr
        WHERE dr.date BETWEEN :dateFrom AND :dateTo
        AND dr.tenantId = :tenantId
        AND (:driverId IS NULL OR dr.driver.id = :driverId)
        ORDER BY dr.date ASC
    """)
    List<RawReportRow> findRowsRaw(
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("tenantId") Long tenantId,
            @Param("driverId") Long driverId
    );

    // Case: DAY (Aggregated by date)
    @Query("""
        SELECT new com.markokosic.minicrm.modules.report.RawReportRow(
            null, null, dr.date, 
            COALESCE(SUM(dr.revenue), 0), 
            COALESCE(SUM(dr.companyRemuneration), 0), 
            COALESCE(SUM(dr.driverRemuneration), 0), 
            COALESCE(SUM(dr.kilometersDriven), 0), 
            COUNT(dr)
        )
        FROM DailyRevenue dr
        WHERE dr.date BETWEEN :dateFrom AND :dateTo
        AND dr.tenantId = :tenantId
        AND (:driverId IS NULL OR dr.driver.id = :driverId)
        GROUP BY dr.date
        ORDER BY dr.date ASC
    """)
    List<RawReportRow> findRowsGroupedByDay(
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("tenantId") Long tenantId,
            @Param("driverId") Long driverId
    );
}
