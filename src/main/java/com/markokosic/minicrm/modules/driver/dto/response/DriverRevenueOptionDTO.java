package com.markokosic.minicrm.modules.driver.dto.response;

import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;

import java.math.BigDecimal;

public record DriverRevenueOptionDTO(
		ShiftEntryCategory entryCategory,
		Long flatRateTypeId,
		String label,
		BigDecimal defaultPrice
) {}
