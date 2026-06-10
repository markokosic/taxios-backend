package com.markokosic.minicrm.modules.driver;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.request.UpdateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverSelectDTO;
import com.markokosic.minicrm.modules.driver.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@Slf4j
@RequiredArgsConstructor
public class DriverController {

	private final DriverService driverService;
	private final I18nService i18n;

	@PostMapping
	public ResponseEntity<ApiResponseDTO<DriverResponseDTO>> createDriver(@Valid @RequestBody CreateDriverRequestDTO request){
		DriverResponseDTO newDriver = driverService.createDriver(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(true, newDriver, i18n.getMessage("success.created")));};

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponseDTO<DriverResponseDTO>> getDriver(@PathVariable Long id){
		DriverResponseDTO driver = driverService.getDriverById(id);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, driver, i18n.getMessage("success.fetched")));
	};

	@GetMapping("/select")
	public ResponseEntity<ApiResponseDTO<List<DriverSelectDTO>>> getAllDriversForSelect() {
		List<DriverSelectDTO> drivers = driverService.getAllDriversForSelect();
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, drivers, i18n.getMessage("success.fetched")));

	}


	@GetMapping
	public ResponseEntity<ApiResponseDTO<PageResponseDTO<DriverResponseDTO>>> getAllDrivers(@PageableDefault(sort={"lastName", "id"}, direction = Sort.Direction.ASC) Pageable pageable){
		PageResponseDTO<DriverResponseDTO> drivers = driverService.getAllDrivers(pageable);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, drivers, i18n.getMessage("success.fetched")));
	};

	@PatchMapping("/{id}")
	public ResponseEntity<ApiResponseDTO<DriverResponseDTO>> updateDriver(
			@PathVariable Long id,
			@RequestBody @Valid UpdateDriverRequestDTO request
	) {
		DriverResponseDTO updatedCustomer = driverService.updateDriver(id, request);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, updatedCustomer, i18n.getMessage("success.updated")));
	};

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponseDTO<DriverResponseDTO>> deleteDriver(
			@PathVariable Long id
	) {
		driverService.deleteDriver(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponseDTO<>(true, null, i18n.getMessage("success.deleted")));
	}

	@DeleteMapping("/{id}/remuneration-configs/{configId}")
	public ResponseEntity<ApiResponseDTO<Void>> stopRemunerationConfig(
			@PathVariable Long id,
			@PathVariable Long configId
	) {
		driverService.stopRemunerationConfig(id, configId);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, null, i18n.getMessage("success.deleted")));
	}
}

//@PostMapping(/{id}/remuneration-configs)
//public ResponseEntity<ApiResponseDTO<DriverResponseDTO>> createDriver(@Valid @RequestBody CreateDriverRequestDTO request){
//	DriverResponseDTO newDriver = driverService.createDriver(request);
//	return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(true, newDriver, i18n.getMessage("success.created")));};