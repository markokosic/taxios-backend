package com.markokosic.minicrm.modules.shift.dto.response;

import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;

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
