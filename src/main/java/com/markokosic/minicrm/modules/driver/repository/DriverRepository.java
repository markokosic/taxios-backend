package com.markokosic.minicrm.modules.driver.repository;


import com.markokosic.minicrm.modules.driver.dto.response.DriverSelectDTO;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DriverRepository extends JpaRepository<Driver, Long> {
	Page<Driver> findAllByStatus(DriverStatus status, Pageable pageable);
	List<Driver> findAllByIdIn(Set<Long> ids);
	Optional<Driver> findByEmail(String email);

	//TODO refactor and use mapper
	@Query("""
        SELECT new com.markokosic.minicrm.modules.driver.dto.response.DriverSelectDTO(d.id, CONCAT(d.firstName, ' ', d.lastName))
        FROM Driver d
        ORDER BY d.lastName ASC, d.firstName ASC
    """)
	List<DriverSelectDTO> findAllDriversForSelect();
}
