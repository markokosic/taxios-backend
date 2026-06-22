package com.markokosic.minicrm.modules.revenue;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/revenues")
@Slf4j
@RequiredArgsConstructor
@Validated
public class RevenueController {

	private final RevenueService revenueService;
	private final I18nService i18n;

//	@PostMapping
//	public ResponseEntity<ApiResponseDTO<Void>> createDailyRevenue(@Valid @RequestBody CreateDailyRevenueRequestDTO request){
//		 revenueService.createDailyRevenue(request);
//		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(true, null, i18n.getMessage("success.added")));};

	@PostMapping("/bulk")
	public ResponseEntity<ApiResponseDTO<Void>> createDailyRevenuesBulk(@Valid @RequestBody List<CreateDailyRevenueRequestDTO> request){
		revenueService.createDailyRevenuesBulk(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(true, null, i18n.getMessage("success.added")));
	};

	@GetMapping
	public ResponseEntity<ApiResponseDTO<PageResponseDTO<DailyRevenueResponseDTO>>> getAllDailyRevenues(@PageableDefault(sort = {"date", "drivingStartTime"}, direction = Sort.Direction.DESC) Pageable pageable){
		PageResponseDTO<DailyRevenueResponseDTO> revenues = revenueService.getAllRevenues(pageable);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, revenues, i18n.getMessage("success.fetched")));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponseDTO<DailyRevenueResponseDTO>> updateDailyRevenue(
			@PathVariable Long id,
			@Valid @RequestBody CreateDailyRevenueRequestDTO request) {
		DailyRevenueResponseDTO updated = revenueService.updateDailyRevenue(id, request);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponseDTO<>(true, updated, i18n.getMessage("success.updated")));
	}

}
