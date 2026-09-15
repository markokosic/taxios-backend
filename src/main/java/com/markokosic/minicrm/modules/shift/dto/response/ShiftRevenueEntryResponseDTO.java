package com.markokosic.minicrm.modules.shift.dto.response;

import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;

import java.math.BigDecimal;
import com.markokosic.minicrm.modules.driver.dto.response.RemunerationConfigResponseDTO;

public record ShiftRevenueEntryResponseDTO(
		Long id,
		ShiftEntryCategory entryCategory,
		Long flatRateTypeId,
		String flatRateTypeName,
		RemunerationModelType remunerationModelType,
		boolean isFlatRate,
		BigDecimal revenue,
		Long tripCount,
		BigDecimal pricePerTrip
) {}
