package com.markokosic.minicrm.modules.driver.repository;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRemunerationConfigRepository extends JpaRepository<DriverRemunerationConfig, Long> {

	DriverRemunerationConfig findByTenantIdAndDriverIdAndCurrentTrue(
			Long tenantId,
			Long driverId
	);
}
