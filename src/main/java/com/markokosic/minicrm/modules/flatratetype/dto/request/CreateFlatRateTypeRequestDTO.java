package com.markokosic.minicrm.modules.flatratetype.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateFlatRateTypeRequestDTO(
		@NotBlank
		String name,

		@PositiveOrZero
		BigDecimal defaultPrice
) {}
