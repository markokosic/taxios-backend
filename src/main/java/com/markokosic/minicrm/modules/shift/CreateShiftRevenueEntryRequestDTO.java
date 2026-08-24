package com.markokosic.minicrm.modules.shift;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateShiftRevenueEntryRequestDTO(
		@NotNull
		ShiftEntryCategory entryCategory,

		Long flatRateTypeId,

		@PositiveOrZero
		BigDecimal revenue,

		@PositiveOrZero
		Long tripCount,

		@PositiveOrZero
		BigDecimal pricePerTrip
) {
	public BigDecimal getEffectiveRevenue() {
		if (revenue != null) {
			return revenue;
		}
		if (tripCount != null && pricePerTrip != null) {
			return pricePerTrip.multiply(BigDecimal.valueOf(tripCount));
		}
		throw new IllegalArgumentException("Either 'revenue' or ('tripCount' and 'pricePerTrip') must be provided.");
	}
}
