package com.markokosic.minicrm.modules.report;

import java.math.BigDecimal;

public record RevenueReportSummaryDTO(
		BigDecimal revenue,
		BigDecimal companyShare,
		BigDecimal totalKm,
		Integer entryCount
) {}
