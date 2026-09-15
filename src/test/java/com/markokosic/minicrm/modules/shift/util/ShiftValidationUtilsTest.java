package com.markokosic.minicrm.modules.shift.util;

import com.markokosic.minicrm.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ShiftValidationUtilsTest {

	@Test
	void validateShiftParameters_ValidRegularShift_Success() {
		BigDecimal startKm = new BigDecimal("100.00");
		BigDecimal endKm = new BigDecimal("250.00");
		LocalDateTime start = LocalDateTime.of(2026, 9, 1, 8, 0);
		LocalDateTime end = LocalDateTime.of(2026, 9, 1, 16, 0);

		assertDoesNotThrow(() -> ShiftValidationUtils.validateShiftParameters(startKm, endKm, start, end));
	}

	@Test
	void validateShiftParameters_ValidOvernightShift_Success() {
		// Starts at 23:00 and ends next day at 06:00
		BigDecimal startKm = new BigDecimal("500.00");
		BigDecimal endKm = new BigDecimal("750.00");
		LocalDateTime start = LocalDateTime.of(2026, 9, 1, 23, 0);
		LocalDateTime end = LocalDateTime.of(2026, 9, 2, 6, 0);

		assertDoesNotThrow(() -> ShiftValidationUtils.validateShiftParameters(startKm, endKm, start, end));
	}

	@Test
	void validateShiftParameters_EqualOdometer_Success() {
		// 0 km driven is allowed (e.g. shift started and ended without trips)
		BigDecimal startKm = new BigDecimal("100.00");
		BigDecimal endKm = new BigDecimal("100.00");
		LocalDateTime start = LocalDateTime.of(2026, 9, 1, 8, 0);
		LocalDateTime end = LocalDateTime.of(2026, 9, 1, 16, 0);

		assertDoesNotThrow(() -> ShiftValidationUtils.validateShiftParameters(startKm, endKm, start, end));
	}

	@Test
	void validateShiftParameters_EndOdometerLowerThanStart_ThrowsBadRequestException() {
		BigDecimal startKm = new BigDecimal("200.00");
		BigDecimal endKm = new BigDecimal("150.00");
		LocalDateTime start = LocalDateTime.of(2026, 9, 1, 8, 0);
		LocalDateTime end = LocalDateTime.of(2026, 9, 1, 16, 0);

		BadRequestException ex = assertThrows(
				BadRequestException.class,
				() -> ShiftValidationUtils.validateShiftParameters(startKm, endKm, start, end)
		);
		assertEquals("domain.shift.invalid_odometer", ex.getI18nKey());
	}

	@Test
	void validateShiftParameters_NegativeOdometer_ThrowsBadRequestException() {
		BigDecimal startKm = new BigDecimal("-10.00");
		BigDecimal endKm = new BigDecimal("100.00");
		LocalDateTime start = LocalDateTime.of(2026, 9, 1, 8, 0);
		LocalDateTime end = LocalDateTime.of(2026, 9, 1, 16, 0);

		BadRequestException ex = assertThrows(
				BadRequestException.class,
				() -> ShiftValidationUtils.validateShiftParameters(startKm, endKm, start, end)
		);
		assertEquals("domain.shift.negative_odometer", ex.getI18nKey());
	}

	@Test
	void validateShiftParameters_NullOdometer_ThrowsBadRequestException() {
		LocalDateTime start = LocalDateTime.of(2026, 9, 1, 8, 0);
		LocalDateTime end = LocalDateTime.of(2026, 9, 1, 16, 0);

		BadRequestException ex = assertThrows(
				BadRequestException.class,
				() -> ShiftValidationUtils.validateShiftParameters(null, new BigDecimal("100.00"), start, end)
		);
		assertEquals("validation.required", ex.getI18nKey());
	}

	@Test
	void validateShiftParameters_EndTimeBeforeStartTime_ThrowsBadRequestException() {
		BigDecimal startKm = new BigDecimal("100.00");
		BigDecimal endKm = new BigDecimal("200.00");
		LocalDateTime start = LocalDateTime.of(2026, 9, 1, 16, 0);
		LocalDateTime end = LocalDateTime.of(2026, 9, 1, 8, 0);

		BadRequestException ex = assertThrows(
				BadRequestException.class,
				() -> ShiftValidationUtils.validateShiftParameters(startKm, endKm, start, end)
		);
		assertEquals("domain.shift.invalid_time_range", ex.getI18nKey());
	}

	@Test
	void validateShiftParameters_EndTimeEqualsStartTime_ThrowsBadRequestException() {
		BigDecimal startKm = new BigDecimal("100.00");
		BigDecimal endKm = new BigDecimal("200.00");
		LocalDateTime start = LocalDateTime.of(2026, 9, 1, 8, 0);
		LocalDateTime end = LocalDateTime.of(2026, 9, 1, 8, 0);

		BadRequestException ex = assertThrows(
				BadRequestException.class,
				() -> ShiftValidationUtils.validateShiftParameters(startKm, endKm, start, end)
		);
		assertEquals("domain.shift.invalid_time_range", ex.getI18nKey());
	}

	@Test
	void validateShiftParameters_NullTimes_ThrowsBadRequestException() {
		BigDecimal startKm = new BigDecimal("100.00");
		BigDecimal endKm = new BigDecimal("200.00");

		BadRequestException ex = assertThrows(
				BadRequestException.class,
				() -> ShiftValidationUtils.validateShiftParameters(startKm, endKm, null, LocalDateTime.now())
		);
		assertEquals("validation.required", ex.getI18nKey());
	}
}
