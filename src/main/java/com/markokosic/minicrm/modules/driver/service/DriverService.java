package com.markokosic.minicrm.modules.driver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.exception.ResourceConflictException;
import com.markokosic.minicrm.modules.driver.DriverMapper;
import com.markokosic.minicrm.modules.driver.RemunerationConfigMapper;
import com.markokosic.minicrm.modules.driver.dto.request.*;
import com.markokosic.minicrm.modules.driver.dto.response.DriverResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverRevenueOptionDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverSelectDTO;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.DriverStatus;
import com.markokosic.minicrm.modules.driver.repository.DriverRemunerationConfigRepository;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateTypeStatus;
import com.markokosic.minicrm.modules.flatratetype.repository.FlatRateTypeRepository;
import com.markokosic.minicrm.exception.BadRequestException;
import com.markokosic.minicrm.exception.ResourceNotFoundException;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import com.markokosic.minicrm.modules.user.User;
import com.markokosic.minicrm.modules.user.UserRepository;
import com.markokosic.minicrm.modules.user.UserService;
import com.markokosic.minicrm.modules.user.dto.response.CreateUserResponseDTO;
import com.markokosic.minicrm.modules.user.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverService {

	private final DriverMapper driverMapper;
	private final DriverRepository driverRepository;
	private final ObjectMapper objectMapper;
	private final RemunerationConfigMapper remunerationConfigMapper;
	private final DriverLookupService driverLookupService;
	private final DriverRemunerationConfigRepository driverRemunerationConfigRepository;
	private final FlatRateTypeRepository flatRateTypeRepository;
	private final UserRepository userRepository;
	private final UserService userService;

	@Transactional
	public DriverResponseDTO createDriver(CreateDriverRequestDTO request) {
		Driver driver = driverMapper.toEntity(request);

		List<DriverRemunerationConfig> configs = request.remunerationConfigs().stream()
				.map(dto -> mapToRemunerationEntity(dto, driver))
				.toList();

		boolean hasDuplicates = configs.size() != configs.stream()
				.map(c -> c.getType() + "_" + (c.getFlatRateType() != null ? c.getFlatRateType().getId() : "ALL"))
				.distinct()
				.count();

		if (hasDuplicates) {
			throw new BadRequestException("domain.driver.multiple_configurations");
		}

		driver.initializeWithRemunerationConfigs(configs);

		driverRepository.save(driver);

		return driverMapper.toDto(driver, remunerationConfigMapper);
	}

	@Transactional(readOnly = true)
	public PageResponseDTO<DriverResponseDTO> getAllDrivers(Pageable pageable) {
		return getDriversByTenant(pageable);
	}

	@Transactional(readOnly = true)
	public DriverResponseDTO getDriverById(Long id) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(id);
		return driverMapper.toDto(driver, remunerationConfigMapper);
	}

	@Transactional(readOnly = true)
	public List<DriverSelectDTO> getAllDriversForSelect() {
		return driverRepository.findAllDriversForSelect();
	}

	@Transactional
	public DriverResponseDTO updateDriver(Long id, UpdateDriverRequestDTO request) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(id);

		driverMapper.updateEntityFromDto(request, driver);

		if (request.remunerationConfigs() != null) {
			boolean hasDuplicates = request.remunerationConfigs().size() != request.remunerationConfigs().stream()
					.map(dto -> {
						if (dto instanceof CreateFlatRateRemunerationConfigDTO flatDto) {
							return dto.remunerationModelType() + "_" + (flatDto.flatRateTypeId() != null ? flatDto.flatRateTypeId() : "ALL");
						}
						return dto.remunerationModelType().name();
					})
					.distinct()
					.count();

			if (hasDuplicates) {
				throw new BadRequestException("domain.driver.multiple_configurations");
			}

			driver.syncRemunerationConfigs(
					request.remunerationConfigs(),
					dto -> mapToRemunerationEntity(dto, driver)
			);
		}

		driverRepository.save(driver);
		return driverMapper.toDto(driver, remunerationConfigMapper);

	}

	@Transactional(readOnly = true)
	public List<DriverRevenueOptionDTO> getRevenueOptionsForDriver(Long driverId) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(driverId);
		List<DriverRemunerationConfig> activeConfigs = driver.getActiveRemunerationConfigs();

		List<DriverRevenueOptionDTO> options = new java.util.ArrayList<>();

		// 1. Regular Trips (Taxameter)
		boolean hasPercentage = activeConfigs.stream()
				.anyMatch(c -> c.getType() == RemunerationModelType.PERCENTAGE_SHARE);
		if (hasPercentage) {
			options.add(new DriverRevenueOptionDTO(
					ShiftEntryCategory.REGULAR, null, "Regular Fare (Taxameter)", null
			));
		}

		// 2. FlatRateTypes
		boolean hasAnyFlatRate = activeConfigs.stream()
				.anyMatch(c -> c.getType() == RemunerationModelType.FLAT_RATE);

		if (hasAnyFlatRate) {
			List<FlatRateType> activeFlatRates = flatRateTypeRepository.findAllByCurrentIsTrueAndStatus(FlatRateTypeStatus.ACTIVE);
			for (FlatRateType fr : activeFlatRates) {
				options.add(new DriverRevenueOptionDTO(
						ShiftEntryCategory.FLAT_RATE, fr.getId(), fr.getName(), fr.getDefaultPrice()
				));
			}
		}

		// 3. Weekly Rent option
		boolean hasWeekly = activeConfigs.stream()
				.anyMatch(c -> c.getType() == RemunerationModelType.WEEKLY_FIXED_RATE);
		if (hasWeekly) {
			options.add(new DriverRevenueOptionDTO(
					ShiftEntryCategory.WEEKLY, null, "Weekly Fixed Fee / Rental", null
			));
		}

		return options;
	}

	@Transactional
	public void deleteDriver(Long id) {
		Driver driver = validateDriverDeletion(id);
		driver.setStatus(DriverStatus.DELETED);
		if (driver.getUser() != null) {
			User user = driver.getUser();
			user.setStatus(UserStatus.DELETED);
			userRepository.save(user);
			driver.setUser(null);
		}
	}

	@Transactional
	public void stopRemunerationConfig(Long driverId, Long configId) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(driverId);
		driver.deactivateConfig(configId);
		driverRepository.save(driver);
	}

	private DriverRemunerationConfig mapToRemunerationEntity(CreateRemunerationRequestDTO dto, Driver driver) {
		if (dto instanceof CreateFlatRateRemunerationConfigDTO flatDto && flatDto.flatRateTypeId() != null) {
			FlatRateType flatRateType = flatRateTypeRepository.findById(flatDto.flatRateTypeId())
					.orElseThrow(() -> new ResourceNotFoundException("domain.flat_rate_type.not_found"));
			return remunerationConfigMapper.toFlatRateEntity(flatDto, driver, flatRateType);
		}
		return remunerationConfigMapper.toEntity(dto, driver);
	}

	private Driver validateDriverDeletion(Long id) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(id);

		if (DriverStatus.DELETED.equals(driver.getStatus())) {
			throw new ResourceNotFoundException("domain.driver.not_found");
		}

		return driver;
	}

	private PageResponseDTO<DriverResponseDTO> getDriversByTenant(Pageable pageable) {
		Page<DriverResponseDTO> page = driverRepository.findAllByStatus(DriverStatus.ACTIVE, pageable)
				.map(driver -> driverMapper.toDto(driver, remunerationConfigMapper));
		return PageResponseDTO.from(page);
	}

	@Transactional
	public CreateUserResponseDTO createDriverUser(Long driverId, String emailOverride) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(driverId);

		if (driver.getUser() != null && driver.getUser().getStatus() == UserStatus.ACTIVE) {
			throw new ResourceConflictException("domain.driver.already_has_user");
		}

		String loginEmail = (emailOverride != null && !emailOverride.isBlank())
				? emailOverride.trim()
				: driver.getEmail();

		return userService.createDriverUser(driver, loginEmail);
	}

	@Transactional
	public void deactivateDriverUser(Long driverId) {
		Driver driver = driverLookupService.validateDriverExistsOrThrow(driverId);

		if (driver.getUser() == null || driver.getUser().getStatus() == UserStatus.DELETED) {
			throw new ResourceNotFoundException("domain.driver.no_active_user");
		}

		User user = driver.getUser();
		user.setStatus(UserStatus.DELETED);
		userRepository.save(user);

		driver.setUser(null);
		driverRepository.save(driver);
	}
}
