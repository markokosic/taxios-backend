package com.markokosic.minicrm.modules.shift.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record UpdateShiftRequestDTO(
		Long carId,

		@NotNull
		BigDecimal odometerStart,

		@NotNull
		BigDecimal odometerEnd,

		@NotNull
		LocalDateTime shiftStart,

		@NotNull
		LocalDateTime shiftEnd,

		@jakarta.validation.constraints.PositiveOrZero
		BigDecimal weeklyDriverRent,

		@NotEmpty
		@Valid
		List<UpdateShiftRevenueEntryRequestDTO> revenues
) {}
