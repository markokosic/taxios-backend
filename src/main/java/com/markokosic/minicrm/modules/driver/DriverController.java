package com.markokosic.minicrm.modules.driver;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.request.UpdateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverRevenueOptionDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverSelectDTO;
import com.markokosic.minicrm.modules.driver.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/drivers", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "Endpoints for managing drivers and their remuneration settings")
public class DriverController {

	private final DriverService driverService;
	private final I18nService i18n;

	@PostMapping
	@Operation(summary = "Create a new driver", description = "Registers a new driver and sets up their initial profile.")
	@ApiResponse(responseCode = "201", description = "Driver profile created successfully")
	@ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public ResponseEntity<ApiResponseDTO<DriverResponseDTO>> createDriver(@Valid @RequestBody CreateDriverRequestDTO request){
		DriverResponseDTO newDriver = driverService.createDriver(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(true, newDriver, i18n.getMessage("success.created")));};

	@GetMapping("/{id}")
	@Operation(summary = "Get driver by ID", description = "Fetches details of a specific driver.")
	@ApiResponse(responseCode = "200", description = "Driver details fetched successfully")
	@ApiResponse(responseCode = "404", description = "Driver not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public ResponseEntity<ApiResponseDTO<DriverResponseDTO>> getDriver(@PathVariable Long id){
		DriverResponseDTO driver = driverService.getDriverById(id);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, driver, i18n.getMessage("success.fetched")));
	};

	@GetMapping("/{id}/revenue-options")
	@Operation(summary = "Get selectable revenue options for driver", description = "Fetches the list of selectable revenue categories and flat rate options for a driver.")
	@ApiResponse(responseCode = "200", description = "Revenue options fetched successfully")
	public ResponseEntity<ApiResponseDTO<List<DriverRevenueOptionDTO>>> getDriverRevenueOptions(@PathVariable Long id) {
		List<DriverRevenueOptionDTO> options = driverService.getRevenueOptionsForDriver(id);
		return ResponseEntity.ok(new ApiResponseDTO<>(true, options, i18n.getMessage("success.fetched")));
	}

	@GetMapping("/select")
	@Operation(summary = "Get drivers list for dropdowns", description = "Retrieves a simplified list of drivers optimized for selection controls.")
	@ApiResponse(responseCode = "200", description = "Drivers list retrieved successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public ResponseEntity<ApiResponseDTO<List<DriverSelectDTO>>> getAllDriversForSelect() {
		List<DriverSelectDTO> drivers = driverService.getAllDriversForSelect();
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, drivers, i18n.getMessage("success.fetched")));

	}

	@GetMapping
	@Operation(summary = "Get all drivers", description = "Retrieves a paginated list of all drivers for the current tenant.")
	@ApiResponse(responseCode = "200", description = "Drivers list retrieved successfully")
	@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public ResponseEntity<ApiResponseDTO<PageResponseDTO<DriverResponseDTO>>> getAllDrivers(@ParameterObject @PageableDefault(sort={"lastName", "id"}, direction = Sort.Direction.ASC) Pageable pageable){
		PageResponseDTO<DriverResponseDTO> drivers = driverService.getAllDrivers(pageable);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, drivers, i18n.getMessage("success.fetched")));
	};

	@PatchMapping("/{id}")
	@Operation(summary = "Update driver details", description = "Updates fields of an existing driver profile.")
	@ApiResponse(responseCode = "200", description = "Driver profile updated successfully")
	@ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "404", description = "Driver not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public ResponseEntity<ApiResponseDTO<DriverResponseDTO>> updateDriver(
			@PathVariable Long id,
			@RequestBody @Valid UpdateDriverRequestDTO request
	) {
		DriverResponseDTO updatedCustomer = driverService.updateDriver(id, request);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, updatedCustomer, i18n.getMessage("success.updated")));
	};

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete driver profile", description = "Deletes a driver's profile.")
	@ApiResponse(responseCode = "204", description = "Driver deleted successfully")
	@ApiResponse(responseCode = "404", description = "Driver not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public ResponseEntity<Void> deleteDriver(
			@PathVariable Long id
	) {
		driverService.deleteDriver(id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}/remuneration-configs/{configId}")
	@Operation(summary = "Stop remuneration configuration", description = "Deactivates a specific remuneration configuration for a driver.")
	@ApiResponse(responseCode = "204", description = "Remuneration configuration stopped successfully")
	@ApiResponse(responseCode = "404", description = "Driver or configuration not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	public ResponseEntity<Void> stopRemunerationConfig(
			@PathVariable Long id,
			@PathVariable Long configId
	) {
		driverService.stopRemunerationConfig(id, configId);
		return ResponseEntity.noContent().build();
	}
}

//@PostMapping(/{id}/remuneration-configs)
