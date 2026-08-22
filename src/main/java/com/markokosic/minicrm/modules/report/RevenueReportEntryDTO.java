package com.markokosic.minicrm.modules.report;

import com.markokosic.minicrm.modules.shift.ShiftEntryCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Single entry or grouped summary row in the revenue report")
public record RevenueReportEntryDTO(
    @Schema(description = "Date of the shift or grouped period", example = "2025-05-10")
    LocalDate date,

    @Schema(description = "ID of the shift (populated for detailed entries, null for grouped rows)", example = "50")
    Long shiftId,

    @Schema(description = "ID of the revenue entry (populated for detailed entries, null for grouped rows)", example = "100")
    Long entryId,

    @Schema(description = "Category of the revenue entry (REGULAR, FLAT_RATE, WEEKLY)", example = "REGULAR")
    ShiftEntryCategory entryCategory,

    @Schema(description = "Gross revenue amount", example = "200.00")
    BigDecimal revenue,

    @Schema(description = "Company share amount", example = "80.00")
    BigDecimal companyRemuneration,

    @Schema(description = "Driver share amount", example = "120.00")
    BigDecimal driverRemuneration,

    @Schema(description = "Number of revenue entries represented in this row", example = "1")
    Long entryCount,

    @Schema(description = "List of associated drivers")
    List<DriverInfoDTO> drivers
) {
}
