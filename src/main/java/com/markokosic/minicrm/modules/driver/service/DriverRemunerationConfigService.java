package com.markokosic.minicrm.modules.driver.service;

import com.markokosic.minicrm.modules.driver.RemunerationConfigMapper;
import com.markokosic.minicrm.modules.driver.dto.request.CreateRemunerationRequestDTO;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.repository.DriverRemunerationConfigRepository;
import com.markokosic.minicrm.modules.tenant.TenantService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Service
@Validated
@RequiredArgsConstructor
public class DriverRemunerationConfigService {
	private final TenantService tenantService;
	private final RemunerationConfigMapper configMapper;
	private final DriverRemunerationConfigRepository configRepository;
	private final DriverLookupService driverLookupService;


	//TODO add Validation for creating new remunerationConfigs, currently no validation
	@Transactional
	public DriverRemunerationConfig createRemunerationConfig(@Valid CreateRemunerationRequestDTO dto, Long driverId) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(driverId);

		Long tenantId = tenantService.getTenantIdFromContextHolder();
		DriverRemunerationConfig newConfig = configMapper.toEntity(dto, tenantId, driver);
		return configRepository.save(newConfig);
	}

}
