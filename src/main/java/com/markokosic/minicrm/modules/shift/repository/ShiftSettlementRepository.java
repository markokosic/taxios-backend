package com.markokosic.minicrm.modules.shift.repository;

import com.markokosic.minicrm.modules.shift.model.ShiftSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShiftSettlementRepository extends JpaRepository<ShiftSettlement, Long> {

    @Query("""
        SELECT ss FROM ShiftSettlement ss
        JOIN FETCH ss.shift s
        JOIN FETCH s.driver d
        JOIN FETCH s.car c
        WHERE s.shiftStart >= :fromDateTime AND s.shiftStart <= :toDateTime
        AND s.status = 'APPROVED'
        AND (:driverId IS NULL OR d.id = :driverId)
        ORDER BY s.shiftStart ASC
    """)
    List<ShiftSettlement> findSettlementsForReport(
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            @Param("driverId") Long driverId
    );
}
