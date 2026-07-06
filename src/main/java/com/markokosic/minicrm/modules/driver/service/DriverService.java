package com.markokosic.minicrm.modules.driver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.modules.driver.DriverMapper;
import com.markokosic.minicrm.modules.driver.RemunerationConfigMapper;
import com.markokosic.minicrm.modules.driver.dto.request.CreateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateRemunerationRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.request.UpdateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverSelectDTO;
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

import java.util.List;

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

		List<DriverRemunerationConfig> configs = request.remunerationConfigs().stream()
				.map(config -> remunerationConfigMapper.toEntity(config, tenantId, driver))
				.toList();

		boolean hasDuplicates = configs.size() != configs.stream()
				.map(DriverRemunerationConfig::getType)
				.distinct()
				.count();

		if (hasDuplicates) {
			throw new ValidationException(ApiErrorCode.DRIVER_MULTIPLE_CONFIGURATIONS);
		}

		driver.initializeWithRemunerationConfigs(configs);

		driverRepository.save(driver);

		return driverMapper.toDto(driver, remunerationConfigMapper);
	}

	@Transactional(readOnly = true)
	public PageResponseDTO<DriverResponseDTO> getAllDrivers(Pageable pageable ) {
		return getDriversByTenant(pageable);
	}

	@Transactional(readOnly = true)
	public  DriverResponseDTO getDriverById(Long id ) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(id);
		return driverMapper.toDto(driver, remunerationConfigMapper);
	}

	@Transactional(readOnly = true)
	public List<DriverSelectDTO> getAllDriversForSelect() {
		Long tenantId = tenantService.getTenantIdFromContextHolder();

		return driverRepository.findAllDriversForSelectByTenant(tenantId);
	}

	@Transactional
	public DriverResponseDTO updateDriver(Long id, UpdateDriverRequestDTO request) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();

		Driver driver = driverLookupService.validateDriverExistsOrThrow(id);

		driverMapper.updateEntityFromDto(request, driver);

		if (request.remunerationConfigs() != null) {
			boolean hasDuplicates = request.remunerationConfigs().size() != request.remunerationConfigs().stream()
					.map(CreateRemunerationRequestDTO::remunerationModelType)
					.distinct()
					.count();

			if (hasDuplicates) {
				throw new ValidationException(ApiErrorCode.DRIVER_MULTIPLE_CONFIGURATIONS);
			}

			driver.syncRemunerationConfigs(
					request.remunerationConfigs(),
					dto -> remunerationConfigMapper.toEntity(dto, tenantId, driver)
			);
		}

		driverRepository.save(driver);
		return driverMapper.toDto(driver, remunerationConfigMapper);

	}

	@Transactional
	public void deleteDriver(Long id) {
		Driver customer = validateDriverDeletion(id);
		customer.setStatus(DriverStatus.DELETED);
	}

	@Transactional
	public void stopRemunerationConfig(Long driverId, Long configId) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(driverId);
		driver.deactivateConfig(configId);
		driverRepository.save(driver);
	}


	private Driver validateDriverDeletion(Long id) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(id);

		if (DriverStatus.DELETED.equals(driver.getStatus())) {
			throw new NotFoundException(ApiErrorCode.DRIVER_NOT_FOUND);
		}

//		if (hasActiveOrders(driver.getId())) {
//			throw new ConflictException(ApiErrorCode.CUSTOMER_HAS_ACTIVE_ORDERS);
//		}

		return driver;
	}



	private PageResponseDTO<DriverResponseDTO> getDriversByTenant(Pageable pageable) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();
		Page<DriverResponseDTO> page = driverRepository.findAllByTenantIdAndStatus(tenantId, DriverStatus.ACTIVE, pageable)
				.map(driver -> driverMapper.toDto(driver, remunerationConfigMapper));
		return PageResponseDTO.from(page);
	}


}
