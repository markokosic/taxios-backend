package com.markokosic.minicrm.modules.flatratetype.dto.response;

import com.markokosic.minicrm.modules.flatratetype.model.FlatRateTypeStatus;

import java.math.BigDecimal;

public record FlatRateTypeResponseDTO(
		Long id,
		String name,
		BigDecimal defaultPrice,
		FlatRateTypeStatus status
) {}
