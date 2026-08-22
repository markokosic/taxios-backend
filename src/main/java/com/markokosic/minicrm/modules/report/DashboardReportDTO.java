package com.markokosic.minicrm.modules.report;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Dashboard financial analytics summary")
public record DashboardReportDTO(
    @Schema(description = "Reporting year", example = "2025")
    Integer year,

    @Schema(description = "Reporting month (null for full year)", example = "5")
    Integer month,

    @Schema(description = "Total revenue amount", example = "15000.00")
    BigDecimal totalRevenue,

    @Schema(description = "Total company share amount", example = "6000.00")
    BigDecimal companyShare,

    @Schema(description = "Total driver share amount", example = "9000.00")
    BigDecimal driverShare,

    @Schema(description = "Total count of revenue entries", example = "75")
    Long entryCount
) {}
