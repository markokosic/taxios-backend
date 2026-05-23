package com.markokosic.minicrm.modules.revenue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyRevenueRepository extends JpaRepository<DailyRevenue, Long> {

    @Query("""
        SELECT dr FROM DailyRevenue dr
        JOIN FETCH dr.driver
        JOIN FETCH dr.car
        WHERE dr.date BETWEEN :dateFrom AND :dateTo
        AND dr.tenantId = :tenantId
        AND (:driverId IS NULL OR dr.driver.id = :driverId)
        ORDER BY dr.date ASC
    """)
    List<DailyRevenue> findRawRevenues(
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("tenantId") Long tenantId,
            @Param("driverId") Long driverId
    );

}
