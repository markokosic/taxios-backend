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
//			@RequestParam (required = false) long vehicleId,
			@RequestParam (required = false) GroupBy groupBy
	) {
		RevenueReportResponseDTO revenueReport = reportService.generateRevenueReport(dateFrom, dateTo, driverId, groupBy);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, revenueReport, i18n.getMessage("success.fetched")));


		//dateFrom, dateTo, -> holt alle Einträge von daily_revenues im Datumsbereich
		//dateFrom, dateTo, groupBy = DAY, MONTH, YEAR, -> alle Einträge im Datumsbereich UND DAY: alle Einträge vom Tag (N), ODER MONTH: aggregierte Einträge pro Tag (max. 31), ODER YEAR: aggregierte Einträge pro Monat (12)
		//dateFrom, dateTo, driverId, groupBy = NONE, DAY, MONTH, YEAR, -> alle Einträge im Datumsbereich, aggregiert wie oben, bei NONE einfach alle Einträge
		//dateFrom, dateTo, vehicleId, groupBy = NONE, DAY, MONTH, YEAR,
		//jeder Report gibt aber noch folgendes mit: groupedBy, totalRevenue, totalCompanyShare, totalKm, entryCount, entries (kann man besseres Vokabel für finden)
		//jeder entry? hat  driverName, driverId, vehicleModel, vehicleLicencePlate, revenue, companyShare, km, datum
	}

}
