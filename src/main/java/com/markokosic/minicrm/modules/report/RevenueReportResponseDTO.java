package com.markokosic.minicrm.modules.report;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Response object representing a detailed revenues report in the system")
public record RevenueReportResponseDTO(
		LocalDate dateFrom,
		LocalDate dateTo,
		GroupBy groupBy,
		RevenueReportSummaryDTO totals,
		List<RevenueReportEntryDTO> rows
		) {
}




