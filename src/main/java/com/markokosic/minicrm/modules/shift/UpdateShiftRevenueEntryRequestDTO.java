package com.markokosic.minicrm.modules.shift;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateShiftRevenueEntryRequestDTO(
		@NotNull
		Long id,

		@PositiveOrZero
		BigDecimal revenue,

		@PositiveOrZero
		Long tripCount,

		@PositiveOrZero
		BigDecimal pricePerTrip
) {}
