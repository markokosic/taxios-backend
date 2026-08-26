package com.markokosic.minicrm.modules.shift.repository;

import com.markokosic.minicrm.modules.shift.model.Shift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

	@EntityGraph(attributePaths = {"driver", "car", "revenues", "revenues.remunerationConfig", "revenues.flatRateType"})
	@Query("""
		SELECT s FROM Shift s
		WHERE (:driverId IS NULL OR s.driver.id = :driverId)
		AND (cast(:dateFrom as timestamp) IS NULL OR s.shiftStart >= :dateFrom)
		AND (cast(:dateTo as timestamp) IS NULL OR s.shiftEnd <= :dateTo)
		ORDER BY s.shiftStart DESC
	""")
	Page<Shift> findAllFiltered(
			@Param("driverId") Long driverId,
			@Param("dateFrom") LocalDateTime dateFrom,
			@Param("dateTo") LocalDateTime dateTo,
			Pageable pageable
	);
}
