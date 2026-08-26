package com.markokosic.minicrm.modules.shift.dto.request;

import com.markokosic.minicrm.modules.shift.model.ShiftStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateShiftRequestDTO(
		@NotNull
		Long driverId,

		@NotNull
		Long carId,

		@NotNull
		BigDecimal odometerStart,

		@NotNull
		BigDecimal odometerEnd,

		@NotNull
		LocalDateTime shiftStart,

		@NotNull
		LocalDateTime shiftEnd,


		//TODO REMOVE LATER WHEN RBAC IS IMPLEMENTED THAT STATE OF STATUS IS DEFINED BY BACKEND
		ShiftStatus status,

		@NotEmpty
		@Valid
		List<CreateShiftRevenueEntryRequestDTO> revenues
) {}
