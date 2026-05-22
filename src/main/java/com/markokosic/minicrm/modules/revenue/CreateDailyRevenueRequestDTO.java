package com.markokosic.minicrm.modules.revenue;

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

		BigDecimal companyRemuneration

) {
}
