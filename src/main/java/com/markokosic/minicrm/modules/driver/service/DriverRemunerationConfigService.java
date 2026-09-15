package com.markokosic.minicrm.modules.driver.service;

import com.markokosic.minicrm.exception.ResourceNotFoundException;
import com.markokosic.minicrm.modules.driver.RemunerationConfigMapper;
import com.markokosic.minicrm.modules.driver.dto.request.CreateFlatRateRemunerationConfigDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateRemunerationRequestDTO;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.repository.DriverRemunerationConfigRepository;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import com.markokosic.minicrm.modules.flatratetype.repository.FlatRateTypeRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class DriverRemunerationConfigService {
	private final RemunerationConfigMapper configMapper;
	private final DriverRemunerationConfigRepository configRepository;
	private final DriverLookupService driverLookupService;
	private final FlatRateTypeRepository flatRateTypeRepository;

	@Transactional
	public DriverRemunerationConfig createRemunerationConfig(@Valid CreateRemunerationRequestDTO dto, Long driverId) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(driverId);

		DriverRemunerationConfig newConfig;
		if (dto instanceof CreateFlatRateRemunerationConfigDTO flatDto && flatDto.flatRateTypeId() != null) {
			FlatRateType flatRateType = flatRateTypeRepository.findById(flatDto.flatRateTypeId())
					.orElseThrow(() -> new ResourceNotFoundException("domain.flat_rate_type.not_found"));
			newConfig = configMapper.toFlatRateEntity(flatDto, driver, flatRateType);
		} else {
			newConfig = configMapper.toEntity(dto, driver);
		}

		return configRepository.save(newConfig);
	}
}
