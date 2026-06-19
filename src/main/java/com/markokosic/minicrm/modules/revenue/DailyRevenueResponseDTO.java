package com.markokosic.minicrm.modules.revenue;

import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record DailyRevenueResponseDTO(
		Long id,
		LocalDate date,
		RemunerationModelType remunerationModelType,
		Long tripCount,
		BigDecimal pricePerTrip,
		String driverFirstName,
		String driverLastName,
		BigDecimal kilometersDriven,
		BigDecimal kilometersFrom,
		BigDecimal kilometersTo,
		BigDecimal revenue,
		BigDecimal companyRemuneration,
		BigDecimal driverRemuneration,
		LocalTime drivingStartTime,
		LocalTime drivingEndTime
) {

}


