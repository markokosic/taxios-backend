package com.markokosic.minicrm.modules.revenue;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDailyRevenueRequestDTO(
		@NotNull
		Long driverId,

		@NotNull
		Long carId,

		@NotNull
		@PastOrPresent
		LocalDate date,

		BigDecimal kilometersDriven,

		@NotNull
		BigDecimal revenue,


		BigDecimal kilometersFrom,

		BigDecimal kilometersTo,

		@NotNull
		@Enumerated(EnumType.STRING)
		RemunerationModelType driverRemunerationType,

		@JsonFormat(pattern = "HH:mm")
		java.time.LocalTime drivingStartTime,

		@JsonFormat(pattern = "HH:mm")
		java.time.LocalTime drivingEndTime,

		Long tripCount,
		BigDecimal pricePerTrip,
		BigDecimal companyRemuneration

) {
}
