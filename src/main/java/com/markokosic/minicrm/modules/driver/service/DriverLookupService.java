package com.markokosic.minicrm.modules.driver.service;

import com.markokosic.minicrm.exception.ResourceNotFoundException;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import com.markokosic.minicrm.modules.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

//TODO refactor this service into driverService
@Component
@RequiredArgsConstructor
public class DriverLookupService {
	private final DriverRepository driverRepository;
	private final TenantService tenantService;

	public Driver validateDriverExistsOrThrow(Long id) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();
		return driverRepository.findByIdAndTenantId(id, tenantId).orElseThrow(() -> new ResourceNotFoundException("domain.driver.not_found"));
	}

	public List<Driver> validateAllExistOrThrow(Set<Long> ids){
		Long tenantId = tenantService.getTenantIdFromContextHolder();
		return driverRepository.findAllByTenantIdAndIdIn(tenantId, ids).orElseThrow(() -> new ResourceNotFoundException("domain.driver.not_found"));
	}
}
