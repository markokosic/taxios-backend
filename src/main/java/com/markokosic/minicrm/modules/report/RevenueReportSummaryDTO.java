package com.markokosic.minicrm.modules.report;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Overall financial summary totals for the revenue report")
public record RevenueReportSummaryDTO(
    @Schema(description = "Total revenue amount", example = "5000.00")
    BigDecimal revenue,

    @Schema(description = "Total company share amount", example = "2000.00")
    BigDecimal companyShare,

    @Schema(description = "Total driver share amount", example = "3000.00")
    BigDecimal driverShare,

    @Schema(description = "Total count of revenue entries", example = "25")
    Integer entryCount
) {}
