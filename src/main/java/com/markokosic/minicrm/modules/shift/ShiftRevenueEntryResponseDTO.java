package com.markokosic.minicrm.modules.shift;

import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;

import java.math.BigDecimal;

public record ShiftRevenueEntryResponseDTO(
		Long id,
		ShiftEntryCategory entryCategory,
		Long flatRateTypeId,
		String flatRateTypeName,
		RemunerationModelType remunerationModelType,
		boolean isFlatRate,
		BigDecimal revenue,
		BigDecimal companyRemuneration,
		BigDecimal driverRemuneration,
		Long tripCount,
		BigDecimal pricePerTrip
) {}
