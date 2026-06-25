package com.markokosic.minicrm.modules.revenue;

import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.common.error.ApiErrorCode;
import com.markokosic.minicrm.exception.NotFoundException;
import com.markokosic.minicrm.modules.car.CarRepository;
import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import com.markokosic.minicrm.modules.driver.service.DriverLookupService;
import com.markokosic.minicrm.modules.remuneration.RemunerationService;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import com.markokosic.minicrm.modules.tenant.TenantService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RevenueService {

	private final DriverLookupService driverLookupService;
	private final DailyRevenueRepository dailyRevenueRepository;
	private final RevenueMapper revenueMapper;
	private final TenantService tenantService;
	private final DriverRepository driverRepository;
	private final RemunerationService remunerationService;
	private final CarRepository carRepository;

//	@Transactional
//	public void createDailyRevenue(CreateDailyRevenueRequestDTO request){
//		Driver driver = driverLookupService.validateDriverExistsOrThrow(request.driverId());
//		Long tenantId = tenantService.getTenantIdFromContextHolder();
//
//		Car car = carRepository.findByIdAndTenantId(request.carId(), tenantId)
//				.orElseThrow(() -> new NotFoundException(ApiErrorCode.CAR_NOT_FOUND));
//
//		DriverRemunerationConfig currentConfig;
//		if (request.driverRemunerationType() == RemunerationModelType.FLAT_RATE_TRIP) {
//			currentConfig = driver.getActiveFlatRateRemunerationConfig();
//			if (currentConfig == null) {
//				throw new IllegalStateException("No active flat-rate remuneration config found for driver: " + driver.getId());
//			}
//		} else {
//			currentConfig = driver.getActivePrimaryRemunerationConfig();
//			if (currentConfig == null) {
//				throw new IllegalStateException("No active primary remuneration config found for driver: " + driver.getId());
//			}
//		}
//
//
//		RemunerationSplit remunerationSplit = remunerationService.calculateRemunerationSplitFromDailyRevenue(request.revenue(), currentConfig, request.companyRemuneration());
//
//		DailyRevenue dailyRevenue = revenueMapper.toEntity(request, tenantId, driver, car, currentConfig, remunerationSplit.companyRemuneration(), remunerationSplit.driverRemuneration());
//		dailyRevenueRepository.save(dailyRevenue);
//	}

	public PageResponseDTO<DailyRevenueResponseDTO> getAllRevenues (Pageable pageable) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();
		Page<DailyRevenueResponseDTO> page = dailyRevenueRepository.findAllByTenantId(tenantId, pageable).map(revenueMapper::toDto);;
	return PageResponseDTO.from(page);
	}

	@Transactional
	public void createDailyRevenuesBulk(List<CreateDailyRevenueRequestDTO> revenueRequests) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();


		//TODO REFACTOR THIS DOUBLE CHECK
		Set<Long> driverIds = revenueRequests.stream().map(CreateDailyRevenueRequestDTO::driverId).collect(Collectors.toSet());
		driverLookupService.validateAllExistOrThrow(driverIds);

		List<Driver> drivers = driverRepository.findAllByTenantIdAndIdIn(tenantId, driverIds)
				.orElseThrow(() -> new IllegalStateException("Drivers not found"));

		Map<Long, Driver> driversMap = drivers.stream()
				.collect(Collectors.toMap(Driver::getId, d -> d));

		Set<Long> carIds = revenueRequests.stream().map(CreateDailyRevenueRequestDTO::carId).collect(Collectors.toSet());
		List<Car> cars = carRepository.findAllById(carIds);
		Map<Long, Car> carsMap = cars.stream()
				.filter(c -> c.getTenantId().equals(tenantId))
				.collect(Collectors.toMap(Car::getId, c -> c));

		List<DailyRevenue> dailyRevenues = revenueRequests.stream()
				.map(dto -> {
					Driver driver = driversMap.get(dto.driverId());

					if (driver == null) {
						throw new IllegalStateException("Driver not found in map: " + dto.driverId());
					}

					Car car = carsMap.get(dto.carId());
					if (car == null) {
						throw new NotFoundException(ApiErrorCode.CAR_NOT_FOUND);
					}

					DriverRemunerationConfig currentConfig = driver.getCurrentRemunerationConfigByType(dto.driverRemunerationType());

					RemunerationSplit remunerationSplit = remunerationService.calculateRemunerationSplitFromDailyRevenue(
							dto.revenue(), currentConfig);

					return revenueMapper.toEntity(dto, tenantId, driver, car, currentConfig,
							remunerationSplit.companyRemuneration(), remunerationSplit.driverRemuneration());
				})
				.toList();

		dailyRevenueRepository.saveAll(dailyRevenues);
	}

	@Transactional
	public DailyRevenueResponseDTO updateDailyRevenue(Long id, CreateDailyRevenueRequestDTO request) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();

		DailyRevenue dailyRevenue = dailyRevenueRepository.findByIdAndTenantId(id, tenantId)
				.orElseThrow(() -> new NotFoundException(ApiErrorCode.REVENUE_NOT_FOUND));

		Driver driver = driverLookupService.validateDriverExistsOrThrow(request.driverId());
		if (!driver.getTenantId().equals(tenantId)) {
			throw new NotFoundException(ApiErrorCode.DRIVER_NOT_FOUND);
		}

		Car car = carRepository.findByIdAndTenantId(request.carId(), tenantId)
				.orElseThrow(() -> new NotFoundException(ApiErrorCode.CAR_NOT_FOUND));

		DriverRemunerationConfig configToUse;
		DriverRemunerationConfig oldConfig = dailyRevenue.getRemunerationConfig();

		// Bedingung: Fahrer hat sich geändert ODER Typ hat sich geändert ODER es gab vorher keine Config
		if (!dailyRevenue.getDriver().getId().equals(driver.getId()) ||
				oldConfig == null ||
				!oldConfig.getType().equals(request.driverRemunerationType())) {

			// Hole die aktuelle Config für den (evtl. neuen) Fahrer und den neuen Typ
			configToUse = driver.getCurrentRemunerationConfigByType(request.driverRemunerationType());
			if (configToUse == null) {
				throw new IllegalStateException("No remuneration config found for driver of type: " + request.driverRemunerationType());
			}
		} else {
			// Fahrer und Typ sind identisch -> Nutze die historisch bereits verknüpfte Config
			configToUse = oldConfig;
		}

		RemunerationSplit remunerationSplit = remunerationService.calculateRemunerationSplitFromDailyRevenue(
				request.revenue(), configToUse);

		revenueMapper.updateEntityFromDto(request, dailyRevenue, driver, car, configToUse,
				remunerationSplit.companyRemuneration(), remunerationSplit.driverRemuneration());

		dailyRevenueRepository.save(dailyRevenue);
		return revenueMapper.toDto(dailyRevenue);
	}

	@Transactional
	public void deleteDailyRevenue(Long id) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();

		DailyRevenue dailyRevenue = dailyRevenueRepository.findByIdAndTenantId(id, tenantId)
				.orElseThrow(() -> new NotFoundException(ApiErrorCode.REVENUE_NOT_FOUND));

		dailyRevenueRepository.delete(dailyRevenue);
	}

}
