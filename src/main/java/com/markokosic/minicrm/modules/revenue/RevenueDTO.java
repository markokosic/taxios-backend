package com.markokosic.minicrm.modules.revenue;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueDTO(
		@NotNull
		Long id,

		@NotNull
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
		LocalDate revenueDate,

		@NotNull
		BigDecimal kilometersDriven,

		@NotNull
		BigDecimal revenue



) {

}
