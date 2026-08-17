package com.markokosic.minicrm.modules.shift;

import java.math.BigDecimal;

public record FlatRateTypeResponseDTO(
		Long id,
		String name,
		BigDecimal defaultPrice,
		FlatRateTypeStatus status
) {}
