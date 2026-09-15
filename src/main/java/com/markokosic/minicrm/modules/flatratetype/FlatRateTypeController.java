package com.markokosic.minicrm.modules.flatratetype;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import com.markokosic.minicrm.modules.flatratetype.dto.request.CreateFlatRateTypeRequestDTO;
import com.markokosic.minicrm.modules.flatratetype.dto.response.FlatRateTypeResponseDTO;
import com.markokosic.minicrm.modules.flatratetype.service.FlatRateTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flat-rate-types")
@RequiredArgsConstructor
@Validated
@Tag(name = "Flat Rate Types", description = "Endpoints for managing custom flat rate trip types")
@PreAuthorize("hasAnyRole(T(com.markokosic.minicrm.modules.role.dto.Roles).ADMIN.name(), T(com.markokosic.minicrm.modules.role.dto.Roles).OWNER.name())")
public class FlatRateTypeController {

	private final FlatRateTypeService flatRateTypeService;
	private final I18nService i18n;

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get all active flat rate types", description = "Fetches a list of active custom flat rate trip types for dropdown selection.")
	public ResponseEntity<ApiResponseDTO<List<FlatRateTypeResponseDTO>>> getActiveFlatRateTypes() {
		List<FlatRateTypeResponseDTO> list = flatRateTypeService.getAllActiveFlatRateTypes();
		return ResponseEntity.ok(new ApiResponseDTO<>(true, list, i18n.getMessage("success.fetched")));
	}

	@GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get all flat rate types including inactive")
	public ResponseEntity<ApiResponseDTO<List<FlatRateTypeResponseDTO>>> getAllFlatRateTypes() {
		List<FlatRateTypeResponseDTO> list = flatRateTypeService.getAllFlatRateTypes();
		return ResponseEntity.ok(new ApiResponseDTO<>(true, list, i18n.getMessage("success.fetched")));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Create a new flat rate type", description = "Adds a custom flat rate trip type (e.g. 'Wien -> Graz').")
	@ApiResponse(responseCode = "201", description = "Flat rate type created successfully")
	@ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public ResponseEntity<ApiResponseDTO<FlatRateTypeResponseDTO>> createFlatRateType(@Valid @RequestBody CreateFlatRateTypeRequestDTO request) {
		FlatRateTypeResponseDTO created = flatRateTypeService.createFlatRateType(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(true, created, i18n.getMessage("success.added")));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Update flat rate type")
	public ResponseEntity<ApiResponseDTO<FlatRateTypeResponseDTO>> updateFlatRateType(
			@PathVariable Long id,
			@Valid @RequestBody CreateFlatRateTypeRequestDTO request
	) {
		FlatRateTypeResponseDTO updated = flatRateTypeService.updateFlatRateType(id, request);
		return ResponseEntity.ok(new ApiResponseDTO<>(true, updated, i18n.getMessage("success.updated")));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Deactivate a flat rate type")
	public ResponseEntity<ApiResponseDTO<Void>> deactivateFlatRateType(@PathVariable Long id) {
		flatRateTypeService.deactivateFlatRateType(id);
		return ResponseEntity.ok(new ApiResponseDTO<>(true, null, i18n.getMessage("success.deleted")));
	}
}
