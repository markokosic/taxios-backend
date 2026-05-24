package com.markokosic.minicrm.modules.report;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
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
@RequestMapping("/api/reports")
@Slf4j
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;
	private final I18nService i18n;

	@GetMapping("/revenue")
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
	public ResponseEntity<ApiResponseDTO<DashboardReportDTO>> getDashboardReport(
			@RequestParam int year,
			@RequestParam(required = false) Integer month
	) {
		DashboardReportDTO dashboardReport = reportService.generateDashboardReport(year, month);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, dashboardReport, i18n.getMessage("success.fetched")));
	}

}
