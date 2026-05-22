package com.markokosic.minicrm.modules.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RawReportRow(
		Integer year,
		Integer month,
		LocalDate date,
		BigDecimal revenue,
		BigDecimal companyShare,
		BigDecimal driverShare,
		BigDecimal km,
		Long count
) {}
