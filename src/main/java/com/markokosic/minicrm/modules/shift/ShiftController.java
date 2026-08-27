package com.markokosic.minicrm.modules.shift;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.modules.shift.dto.request.CreateShiftRequestDTO;
import com.markokosic.minicrm.modules.shift.dto.request.UpdateShiftRequestDTO;
import com.markokosic.minicrm.modules.shift.dto.response.ShiftResponseDTO;
import com.markokosic.minicrm.modules.shift.service.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.markokosic.minicrm.modules.auth.model.UserPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
@Validated
@Tag(name = "Shifts", description = "Endpoints for managing driver shifts")
public class ShiftController {

	private final ShiftService shiftService;
	private final I18nService i18n;

	@GetMapping(value = "/my", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get my shifts", description = "Fetches a paginated list of shifts for the currently authenticated driver.")
	@ApiResponse(responseCode = "200", description = "Driver shifts fetched successfully")
	@PreAuthorize("hasAnyRole(T(com.markokosic.minicrm.modules.role.dto.Roles).DRIVER.name(), T(com.markokosic.minicrm.modules.role.dto.Roles).ADMIN.name(), T(com.markokosic.minicrm.modules.role.dto.Roles).OWNER.name())")
	public ResponseEntity<ApiResponseDTO<PageResponseDTO<ShiftResponseDTO>>> getMyShifts(
			@AuthenticationPrincipal UserPrincipal principal,
			@ParameterObject Pageable pageable
	) {
		PageResponseDTO<ShiftResponseDTO> shifts = shiftService.getMyShifts(principal.getId(), pageable);
		return ResponseEntity.ok(new ApiResponseDTO<>(true, shifts, i18n.getMessage("success.fetched")));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Create a new shift", description = "Logs a new shift with revenue entries.")
	@ApiResponse(responseCode = "201", description = "Shift created successfully")
	@ApiResponse(responseCode = "400", description = "Invalid shift data", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	@PreAuthorize("hasAnyRole(T(com.markokosic.minicrm.modules.role.dto.Roles).ADMIN.name(), T(com.markokosic.minicrm.modules.role.dto.Roles).OWNER.name())")
	public ResponseEntity<ApiResponseDTO<ShiftResponseDTO>> createShift(@Valid @RequestBody CreateShiftRequestDTO request) {
		ShiftResponseDTO created = shiftService.createShift(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(true, created, i18n.getMessage("success.added")));
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get all shifts", description = "Fetches a paginated list of shifts filtered by driver or date range.")
	@PreAuthorize("hasAnyRole(T(com.markokosic.minicrm.modules.role.dto.Roles).ADMIN.name(), T(com.markokosic.minicrm.modules.role.dto.Roles).OWNER.name())")
	public ResponseEntity<ApiResponseDTO<PageResponseDTO<ShiftResponseDTO>>> getAllShifts(
			@RequestParam(required = false) Long driverId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
			@ParameterObject Pageable pageable
	) {
		PageResponseDTO<ShiftResponseDTO> shifts = shiftService.getAllShifts(driverId, dateFrom, dateTo, pageable);
		return ResponseEntity.ok(new ApiResponseDTO<>(true, shifts, i18n.getMessage("success.fetched")));
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get shift by ID")
	@PreAuthorize("hasAnyRole(T(com.markokosic.minicrm.modules.role.dto.Roles).ADMIN.name(), T(com.markokosic.minicrm.modules.role.dto.Roles).OWNER.name())")
	public ResponseEntity<ApiResponseDTO<ShiftResponseDTO>> getShiftById(@PathVariable Long id) {
		ShiftResponseDTO shift = shiftService.getShiftById(id);
		return ResponseEntity.ok(new ApiResponseDTO<>(true, shift, i18n.getMessage("success.fetched")));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Update a shift", description = "Updates shift metadata (odometer, dates) and revenue amounts. Does not allow changing driver, car, or remuneration configs.")
	@ApiResponse(responseCode = "200", description = "Shift updated successfully")
	@ApiResponse(responseCode = "400", description = "Invalid shift data", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "404", description = "Shift not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	@PreAuthorize("hasAnyRole(T(com.markokosic.minicrm.modules.role.dto.Roles).ADMIN.name(), T(com.markokosic.minicrm.modules.role.dto.Roles).OWNER.name())")
	public ResponseEntity<ApiResponseDTO<ShiftResponseDTO>> updateShift(@PathVariable Long id, @Valid @RequestBody UpdateShiftRequestDTO request) {
		ShiftResponseDTO updated = shiftService.updateShift(id, request);
		return ResponseEntity.ok(new ApiResponseDTO<>(true, updated, i18n.getMessage("success.updated")));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a shift")
	@PreAuthorize("hasAnyRole(T(com.markokosic.minicrm.modules.role.dto.Roles).ADMIN.name(), T(com.markokosic.minicrm.modules.role.dto.Roles).OWNER.name())")
	public ResponseEntity<ApiResponseDTO<Void>> deleteShift(@PathVariable Long id) {
		shiftService.deleteShift(id);
		return ResponseEntity.ok(new ApiResponseDTO<>(true, null, i18n.getMessage("success.deleted")));
	}
}
