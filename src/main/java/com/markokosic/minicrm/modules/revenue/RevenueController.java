package com.markokosic.minicrm.modules.revenue;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/revenues")
@Slf4j
@RequiredArgsConstructor
@Validated
@Tag(name = "Revenues", description = "Endpoints for logging and updating driver daily revenue data")
public class RevenueController {

	private final RevenueService revenueService;
	private final I18nService i18n;

	@PostMapping("/bulk")
	@Operation(summary = "Bulk create daily revenues", description = "Logs multiple daily revenue entries for drivers at once.")
	@ApiResponse(responseCode = "201", description = "Daily revenues logged successfully")
	@ApiResponse(responseCode = "400", description = "Invalid daily revenue logs payload")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	public ResponseEntity<ApiResponseDTO<Void>> createDailyRevenuesBulk(@Valid @RequestBody List<CreateDailyRevenueRequestDTO> request){
		revenueService.createDailyRevenuesBulk(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(true, null, i18n.getMessage("success.added")));
	};

	@GetMapping
	@Operation(summary = "Get all daily revenues", description = "Retrieves a paginated list of logged daily revenues.")
	@ApiResponse(responseCode = "200", description = "Revenues list retrieved successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	public ResponseEntity<ApiResponseDTO<PageResponseDTO<DailyRevenueResponseDTO>>> getAllDailyRevenues(@PageableDefault(sort = {"date", "drivingStartTime"}, direction = Sort.Direction.DESC) Pageable pageable){
		PageResponseDTO<DailyRevenueResponseDTO> revenues = revenueService.getAllRevenues(pageable);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, revenues, i18n.getMessage("success.fetched")));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update a daily revenue log", description = "Modifies an existing daily revenue log.")
	@ApiResponse(responseCode = "200", description = "Daily revenue log updated successfully")
	@ApiResponse(responseCode = "400", description = "Invalid request payload")
	@ApiResponse(responseCode = "404", description = "Daily revenue log not found")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	public ResponseEntity<ApiResponseDTO<DailyRevenueResponseDTO>> updateDailyRevenue(
			@PathVariable Long id,
			@Valid @RequestBody CreateDailyRevenueRequestDTO request) {
		DailyRevenueResponseDTO updated = revenueService.updateDailyRevenue(id, request);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponseDTO<>(true, updated, i18n.getMessage("success.updated")));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a daily revenue log", description = "Permanently deletes a logged daily revenue entry.")
	@ApiResponse(responseCode = "200", description = "Daily revenue log deleted successfully")
	@ApiResponse(responseCode = "404", description = "Daily revenue log not found")
	@ApiResponse(responseCode = "401", description = "Unauthorized")
	public ResponseEntity<ApiResponseDTO<Void>> deleteDailyRevenue(
			@PathVariable Long id) {
		 revenueService.deleteDailyRevenue(id);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponseDTO<>(true, null, i18n.getMessage("success.deleted")));
	}

}
