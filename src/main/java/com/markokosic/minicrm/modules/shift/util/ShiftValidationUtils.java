package com.markokosic.minicrm.modules.shift.util;

import com.markokosic.minicrm.exception.BadRequestException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class ShiftValidationUtils {

	private ShiftValidationUtils() {}

	public static void validateShiftParameters(
			BigDecimal odometerStart,
			BigDecimal odometerEnd,
			LocalDateTime shiftStart,
			LocalDateTime shiftEnd
	) {
		validateOdometer(odometerStart, odometerEnd);
		validateShiftTimes(shiftStart, shiftEnd);
	}

	public static void validateOdometer(BigDecimal odometerStart, BigDecimal odometerEnd) {
		if (odometerStart == null || odometerEnd == null) {
			throw new BadRequestException("validation.required");
		}
		if (odometerStart.compareTo(BigDecimal.ZERO) < 0 || odometerEnd.compareTo(BigDecimal.ZERO) < 0) {
			throw new BadRequestException("domain.shift.negative_odometer");
		}
		if (odometerEnd.compareTo(odometerStart) < 0) {
			throw new BadRequestException("domain.shift.invalid_odometer");
		}
	}

	public static void validateShiftTimes(LocalDateTime shiftStart, LocalDateTime shiftEnd) {
		if (shiftStart == null || shiftEnd == null) {
			throw new BadRequestException("validation.required");
		}
		if (!shiftEnd.isAfter(shiftStart)) {
			throw new BadRequestException("domain.shift.invalid_time_range");
		}
	}
}
