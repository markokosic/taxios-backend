package com.markokosic.minicrm.modules.revenue;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/revenues")
@Slf4j
@RequiredArgsConstructor
@Validated
public class RevenueController {

	private final RevenueService revenueService;
	private final I18nService i18n;

	@PostMapping
	public ResponseEntity<ApiResponseDTO<Void>> createDailyRevenue(@Valid @RequestBody CreateDailyRevenueRequestDTO request){
		 revenueService.createDailyRevenue(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(true, null, i18n.getMessage("success.added")));};

	@PostMapping("/bulk")
	public ResponseEntity<ApiResponseDTO<Void>> createDailyRevenuesBulk(@Valid @RequestBody List<CreateDailyRevenueRequestDTO> request){
		revenueService.createDailyRevenuesBulk(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(true, null, i18n.getMessage("success.added")));};


}
