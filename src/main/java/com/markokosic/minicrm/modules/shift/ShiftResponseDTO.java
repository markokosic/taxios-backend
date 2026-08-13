package com.markokosic.minicrm.modules.shift;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.markokosic.minicrm.modules.car.dto.response.CarSummaryDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverSummaryDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ShiftResponseDTO(
		Long id,
		DriverSummaryDTO driver,
		CarSummaryDTO car,
		BigDecimal odometerStart,
		BigDecimal odometerEnd,
		BigDecimal kilometersDriven,
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
		LocalDateTime shiftStart,
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
		LocalDateTime shiftEnd,
		ShiftStatus status,
		List<ShiftRevenueEntryResponseDTO> revenues
) {}
