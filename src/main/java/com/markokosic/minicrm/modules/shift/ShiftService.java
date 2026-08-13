package com.markokosic.minicrm.modules.shift;

import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.exception.ResourceNotFoundException;
import com.markokosic.minicrm.modules.car.CarRepository;
import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import com.markokosic.minicrm.modules.remuneration.RemunerationService;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShiftService {

	private final DriverRepository driverRepository;
	private final CarRepository carRepository;
	private final FlatRateTypeRepository flatRateTypeRepository;
	private final ShiftRepository shiftRepository;
	private final RemunerationService remunerationService;
	private final ShiftMapper shiftMapper;

	@Transactional
	public ShiftResponseDTO createShift(CreateShiftRequestDTO request) {
		Driver driver = driverRepository.findById(request.driverId())
				.orElseThrow(() -> new ResourceNotFoundException("domain.driver.not_found"));

		Car car = carRepository.findById(request.carId())
				.orElseThrow(() -> new ResourceNotFoundException("domain.car.not_found"));

		ShiftStatus status = request.status() != null ? request.status() : ShiftStatus.APPROVED;

		Shift shift = shiftMapper.toShiftEntity(request, driver, car, status);

		for (CreateShiftRevenueEntryRequestDTO revenueReq : request.revenues()) {
			FlatRateType flatRateType = null;
			if (revenueReq.flatRateTypeId() != null) {
				flatRateType = flatRateTypeRepository.findById(revenueReq.flatRateTypeId())
						.orElseThrow(() -> new ResourceNotFoundException("domain.flat_rate_type.not_found"));
			}

			DriverRemunerationConfig config = driver.getRemunerationConfigForEntry(revenueReq.entryCategory(), flatRateType);
			if (config == null) {
				throw new IllegalStateException("No valid remuneration config found for driver " + driver.getId() + " and category " + revenueReq.entryCategory());
			}

			BigDecimal effectiveRevenue = revenueReq.getEffectiveRevenue();
			RemunerationSplit split = remunerationService.calculateRemunerationSplitFromDailyRevenue(effectiveRevenue, config);

			ShiftRevenueEntry entry = shiftMapper.toRevenueEntryEntity(revenueReq, shift, config, flatRateType, effectiveRevenue, split);
			shift.addRevenueEntry(entry);
		}

		Shift saved = shiftRepository.save(shift);
		return shiftMapper.toDto(saved);
	}

	public PageResponseDTO<ShiftResponseDTO> getAllShifts(Long driverId, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
		Page<ShiftResponseDTO> page = shiftRepository.findAllFiltered(driverId, dateFrom, dateTo, pageable).map(shiftMapper::toDto);
		return PageResponseDTO.from(page);
	}

	public ShiftResponseDTO getShiftById(Long id) {
		Shift shift = shiftRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("domain.shift.not_found"));
		return shiftMapper.toDto(shift);
	}

	@Transactional
	public void deleteShift(Long id) {
		Shift shift = shiftRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("domain.shift.not_found"));
		shiftRepository.delete(shift);
	}
}
