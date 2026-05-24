package com.markokosic.minicrm.modules.report;

import java.math.BigDecimal;

public record DashboardReportDTO(
    Integer year,
    Integer month,
    BigDecimal totalRevenue,
    BigDecimal companyShare,
    BigDecimal driverShare,
    BigDecimal totalKm,
    BigDecimal revenuePerKm,
    Long tripCount
) {}
