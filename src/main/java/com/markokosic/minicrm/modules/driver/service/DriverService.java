package com.markokosic.minicrm.modules.driver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.common.error.ApiErrorCode;
import com.markokosic.minicrm.exception.NotFoundException;
import com.markokosic.minicrm.modules.driver.DriverMapper;
import com.markokosic.minicrm.modules.driver.RemunerationConfigMapper;
import com.markokosic.minicrm.modules.driver.dto.request.CreateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.request.UpdateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverResponseDTO;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.DriverStatus;
import com.markokosic.minicrm.modules.driver.repository.DriverRemunerationConfigRepository;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import com.markokosic.minicrm.modules.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverService {

	private final TenantService tenantService;
	private final DriverMapper driverMapper;
	private final DriverRepository driverRepository;
	private final ObjectMapper objectMapper;
	private final RemunerationConfigMapper remunerationConfigMapper;
	private final DriverLookupService driverLookupService;
	private final DriverRemunerationConfigRepository driverRemunerationConfigRepository;

	@Transactional
	public DriverResponseDTO createDriver(CreateDriverRequestDTO request ) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();
		Driver driver = driverMapper.toEntity(request, tenantId);
		DriverRemunerationConfig config = remunerationConfigMapper.toEntity(
				request.remunerationConfig(),
				tenantId,
				driver
		);

		driver.initializeWithRemuneration(config);

		driverRepository.save(driver);

		return driverMapper.toDto(driver);
	}

	@Transactional(readOnly = true)
	public PageResponseDTO<DriverResponseDTO> getAllDrivers(Pageable pageable ) {
		return getDriversByTenant(pageable);
	}

	@Transactional(readOnly = true)
	public  DriverResponseDTO getDriverById(Long id ) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(id);


		return driverMapper.toDto(driver);
	}

	@Transactional
	public DriverResponseDTO updateDriver(Long id, UpdateDriverRequestDTO request) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();

		Driver driver = driverLookupService.validateDriverExistsOrThrow(id);

		driverMapper.updateEntityFromDto(request, driver);

		if(request.remunerationConfig() != null){
			DriverRemunerationConfig config = remunerationConfigMapper.toEntity(
					request.remunerationConfig(),
					tenantId,
					driver
			);
			driver.activateNewRemuneration(config);
		}

		driverRepository.save(driver);
		return driverMapper.toDto(driver);

	}

	@Transactional
	public void deleteDriver(Long id) {
		Driver customer = validateDriverDeletion(id);
		customer.setStatus(DriverStatus.DELETED);
	}


	private Driver validateDriverDeletion(Long id) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(id);

		if (DriverStatus.DELETED.equals(driver.getStatus())) {
			throw new NotFoundException(ApiErrorCode.Driver_NOT_FOUND);
		}

//		if (hasActiveOrders(driver.getId())) {
//			throw new ConflictException(ApiErrorCode.CUSTOMER_HAS_ACTIVE_ORDERS);
//		}

		return driver;
	}



	private PageResponseDTO<DriverResponseDTO> getDriversByTenant(Pageable pageable) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();
		Page<DriverResponseDTO> page = driverRepository.findAllByTenantIdAndStatus(tenantId, DriverStatus.ACTIVE, pageable).map(driverMapper::toDto);
		return PageResponseDTO.from(page);
	}


}
