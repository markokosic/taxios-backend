package com.markokosic.minicrm.modules.car;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.modules.car.dto.request.CreateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.request.UpdateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.response.CarResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/cars", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Cars", description = "Endpoints for managing vehicles in the CRM")
@PreAuthorize("hasAnyRole(T(com.markokosic.minicrm.modules.role.dto.Roles).ADMIN.name(), T(com.markokosic.minicrm.modules.role.dto.Roles).OWNER.name())")
public class CarController {

    private final CarService carService;
    private final I18nService i18n;

    @PostMapping
    @Operation(summary = "Create a new car", description = "Creates a new car profile associated with the current tenant.")
    @ApiResponse(responseCode = "201", description = "Car created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized - Access token missing or invalid", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ApiResponseDTO<CarResponseDTO>> createCar(@Valid @RequestBody CreateCarRequestDTO request) {
        CarResponseDTO newCar = carService.createCar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, newCar, i18n.getMessage("success.created")));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a car by ID", description = "Fetches details of a specific car belonging to the tenant.")
    @ApiResponse(responseCode = "200", description = "Car details fetched successfully")
    @ApiResponse(responseCode = "404", description = "Car not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ApiResponseDTO<CarResponseDTO>> getCar(@PathVariable Long id) {
        CarResponseDTO car = carService.getCarById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseDTO<>(true, car, i18n.getMessage("success.fetched")));
    }

    @GetMapping
    @Operation(summary = "Get all cars", description = "Retrieves a paginated list of all cars for the current tenant.")
    @ApiResponse(responseCode = "200", description = "Cars list retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<CarResponseDTO>>> getAllCars(
            @ParameterObject @PageableDefault(sort = {"licensePlate", "id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponseDTO<CarResponseDTO> cars = carService.getAllCars(pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseDTO<>(true, cars, i18n.getMessage("success.fetched")));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a car", description = "Updates specific properties of an existing car.")
    @ApiResponse(responseCode = "200", description = "Car updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid update payload", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Car not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ApiResponseDTO<CarResponseDTO>> updateCar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCarRequestDTO request) {
        CarResponseDTO updatedCar = carService.updateCar(id, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseDTO<>(true, updatedCar, i18n.getMessage("success.updated")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a car", description = "Deletes a car by its ID.")
    @ApiResponse(responseCode = "204", description = "Car deleted successfully")
    @ApiResponse(responseCode = "404", description = "Car not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.noContent().build();
    }
}
