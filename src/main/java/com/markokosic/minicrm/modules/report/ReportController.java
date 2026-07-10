package com.markokosic.minicrm.modules.report;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Endpoints for generating financial reports and dashboard analytics")
public class ReportController {

	private final ReportService reportService;
	private final I18nService i18n;

	@GetMapping("/revenue")
	@Operation(summary = "Get revenue report", description = "Generates a comprehensive revenue report for a specified date range, optionally filtered by driver and grouped by date or driver.")
	@ApiResponse(responseCode = "200", description = "Revenue report generated successfully")
	@ApiResponse(responseCode = "400", description = "Invalid date range or request parameters")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	public ResponseEntity<ApiResponseDTO<RevenueReportResponseDTO>> getRevenueReport(
			@RequestParam
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateFrom,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate dateTo,
			@RequestParam (required = false) Long driverId,
			@RequestParam (required = false) GroupBy groupBy
	) {
		RevenueReportResponseDTO revenueReport = reportService.generateRevenueReport(dateFrom, dateTo, driverId, groupBy);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, revenueReport, i18n.getMessage("success.fetched")));
	}

	@GetMapping("/dashboard")
	@Operation(summary = "Get dashboard analytics summary", description = "Fetches aggregate analytics (totals, trends) for the dashboard for a given year and optional month.")
	@ApiResponse(responseCode = "200", description = "Dashboard analytics fetched successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	public ResponseEntity<ApiResponseDTO<DashboardReportDTO>> getDashboardReport(
			@RequestParam int year,
			@RequestParam(required = false) Integer month
	) {
		DashboardReportDTO dashboardReport = reportService.generateDashboardReport(year, month);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, dashboardReport, i18n.getMessage("success.fetched")));
	}

}
