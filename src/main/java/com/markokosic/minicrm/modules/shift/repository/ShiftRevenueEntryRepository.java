package com.markokosic.minicrm.modules.shift.repository;

import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShiftRevenueEntryRepository extends JpaRepository<ShiftRevenueEntry, Long> {

    @Query("""
        SELECT e FROM ShiftRevenueEntry e
        JOIN FETCH e.shift s
        JOIN FETCH s.driver d
        JOIN FETCH s.car c
        WHERE s.shiftStart >= :fromDateTime AND s.shiftStart <= :toDateTime
        AND (:driverId IS NULL OR d.id = :driverId)
        ORDER BY s.shiftStart ASC, e.id ASC
    """)
    List<ShiftRevenueEntry> findRevenuesForReport(
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            @Param("driverId") Long driverId
    );
}
