package com.markokosic.minicrm.modules.shift.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import com.markokosic.minicrm.modules.driver.dto.response.RemunerationConfigResponseDTO;
import java.time.LocalDateTime;

@Schema(description = "Shift settlement snapshot containing totals for revenue, driver remuneration, and company share")
public record ShiftSettlementResponseDTO(
		Long id,
		@Schema(description = "Total revenue earned in the shift in EUR", example = "350.00")
		BigDecimal totalRevenue,
		@Schema(description = "Total remuneration paid to driver in EUR", example = "157.50")
		BigDecimal driverRemuneration,
		@Schema(description = "Total retained by company in EUR", example = "192.50")
		BigDecimal companyRemuneration,
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
		@Schema(description = "Timestamp when settlement was finalized", example = "2025-05-10T16:00")
		LocalDateTime settledAt
) {}
