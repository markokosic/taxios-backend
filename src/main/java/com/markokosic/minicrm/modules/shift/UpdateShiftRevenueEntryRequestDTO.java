package com.markokosic.minicrm.modules.shift;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Schema(description = "DTO for updating existing or adding new revenue entries within a shift")
public record UpdateShiftRevenueEntryRequestDTO(
		@Schema(description = "ID of the existing revenue entry. If present, only amounts are updated (category is immutable). If omitted/null, a new entry is created.", example = "100")
		Long id,

		@Schema(description = "Entry category (REGULAR, FLAT_RATE, WEEKLY). Required when adding a new entry (id is null). Ignored for existing entries.", example = "REGULAR")
		ShiftEntryCategory entryCategory,

		@Schema(description = "Flat rate type ID (optional for FLAT_RATE category when adding a new entry). Ignored for existing entries.", example = "1")
		Long flatRateTypeId,

		@PositiveOrZero
		@Schema(description = "Direct revenue amount", example = "250.00")
		BigDecimal revenue,

		@PositiveOrZero
		@Schema(description = "Number of trips", example = "10")
		Long tripCount,

		@PositiveOrZero
		@Schema(description = "Price per trip", example = "25.00")
		BigDecimal pricePerTrip
) {}
