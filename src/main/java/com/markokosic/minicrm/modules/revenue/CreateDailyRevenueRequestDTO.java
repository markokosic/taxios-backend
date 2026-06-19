package com.markokosic.minicrm.modules.revenue;

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

		@NotNull
		@Positive
		BigDecimal kilometersDriven,

		@NotNull
		BigDecimal revenue,

		@NotNull
		BigDecimal kilometersFrom,

		@NotNull
		BigDecimal kilometersTo,

		@NotNull
		@Enumerated(EnumType.STRING)
		RemunerationModelType driverRemunerationType,

		java.time.LocalTime drivenFrom,

		java.time.LocalTime drivenTo,

		Long tripCount,

		BigDecimal pricePerTrip,

		BigDecimal companyRemuneration

) {
}
