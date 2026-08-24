package com.markokosic.minicrm.modules.shift;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateFlatRateTypeRequestDTO(
		@NotBlank
		String name,

		@PositiveOrZero
		BigDecimal defaultPrice
) {}
