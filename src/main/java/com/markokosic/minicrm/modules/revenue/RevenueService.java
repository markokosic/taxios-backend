package com.markokosic.minicrm.modules.revenue;

import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import com.markokosic.minicrm.modules.driver.service.DriverLookupService;
import com.markokosic.minicrm.modules.remuneration.RemunerationService;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import com.markokosic.minicrm.modules.tenant.TenantService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
	private final com.markokosic.minicrm.modules.car.CarRepository carRepository;

	@Transactional
	public void createDailyRevenue(CreateDailyRevenueRequestDTO request){
		Driver driver = driverLookupService.validateDriverExistsOrThrow(request.driverId());
		Long tenantId = tenantService.getTenantIdFromContextHolder();

		com.markokosic.minicrm.modules.car.model.Car car = carRepository.findByIdAndTenantId(request.carId(), tenantId)
				.orElseThrow(() -> new com.markokosic.minicrm.exception.NotFoundException(com.markokosic.minicrm.common.error.ApiErrorCode.CAR_NOT_FOUND));

		DriverRemunerationConfig currentConfig = driver.getRemunerationConfigs().stream()
				.filter(DriverRemunerationConfig::isCurrent)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No current remuneration config found"));


		RemunerationSplit remunerationSplit = remunerationService.calculateRemunerationSplitFromDailyRevenue(request.revenue(), currentConfig, request.companyRemuneration());

		DailyRevenue dailyRevenue = revenueMapper.toEntity(request, tenantId, driver, car, currentConfig, remunerationSplit.companyRemuneration(), remunerationSplit.driverRemuneration());
		dailyRevenueRepository.save(dailyRevenue);
	}

	@Transactional
	public void createDailyRevenuesBulk(List<CreateDailyRevenueRequestDTO> request) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();

		Set<Long> driverIds = request.stream().map(CreateDailyRevenueRequestDTO::driverId).collect(Collectors.toSet());
		driverLookupService.validateAllExistOrThrow(driverIds);

		List<Driver> drivers = driverRepository.findAllByTenantIdAndIdIn(tenantId, driverIds)
				.orElseThrow(() -> new IllegalStateException("Drivers not found"));

		Map<Long, Driver> driversMap = drivers.stream()
				.collect(Collectors.toMap(Driver::getId, d -> d));

		Set<Long> carIds = request.stream().map(CreateDailyRevenueRequestDTO::carId).collect(Collectors.toSet());
		List<com.markokosic.minicrm.modules.car.model.Car> cars = carRepository.findAllById(carIds);
		Map<Long, com.markokosic.minicrm.modules.car.model.Car> carsMap = cars.stream()
				.filter(c -> c.getTenantId().equals(tenantId))
				.collect(Collectors.toMap(com.markokosic.minicrm.modules.car.model.Car::getId, c -> c));

		List<DailyRevenue> dailyRevenues = request.stream()
				.map(dto -> {
					Driver driver = driversMap.get(dto.driverId());
					if (driver == null) {
						throw new IllegalStateException("Driver not found in map: " + dto.driverId());
					}

					com.markokosic.minicrm.modules.car.model.Car car = carsMap.get(dto.carId());
					if (car == null) {
						throw new com.markokosic.minicrm.exception.NotFoundException(com.markokosic.minicrm.common.error.ApiErrorCode.CAR_NOT_FOUND);
					}

					DriverRemunerationConfig currentConfig = driver.getRemunerationConfigs().stream()
							.filter(DriverRemunerationConfig::isCurrent)
							.findFirst()
							.orElseThrow(() -> new IllegalStateException("No current remuneration config found for driver: " + dto.driverId()));

					RemunerationSplit remunerationSplit = remunerationService.calculateRemunerationSplitFromDailyRevenue(
							dto.revenue(), currentConfig, dto.companyRemuneration());

					return revenueMapper.toEntity(dto, tenantId, driver, car, currentConfig,
							remunerationSplit.companyRemuneration(), remunerationSplit.driverRemuneration());
				})
				.toList();

		dailyRevenueRepository.saveAll(dailyRevenues);
	}


}
