package com.markokosic.minicrm.modules.car;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.modules.car.dto.request.CreateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.request.UpdateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.response.CarResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;
    private final I18nService i18n;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<CarResponseDTO>> createCar(@Valid @RequestBody CreateCarRequestDTO request) {
        CarResponseDTO newCar = carService.createCar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(true, newCar, i18n.getMessage("success.created")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CarResponseDTO>> getCar(@PathVariable Long id) {
        CarResponseDTO car = carService.getCarById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseDTO<>(true, car, i18n.getMessage("success.fetched")));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<CarResponseDTO>>> getAllCars(
            @PageableDefault(sort = {"licensePlate", "id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponseDTO<CarResponseDTO> cars = carService.getAllCars(pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseDTO<>(true, cars, i18n.getMessage("success.fetched")));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<CarResponseDTO>> updateCar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCarRequestDTO request) {
        CarResponseDTO updatedCar = carService.updateCar(id, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponseDTO<>(true, updatedCar, i18n.getMessage("success.updated")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(new ApiResponseDTO<>(true, null, i18n.getMessage("success.deleted")));
    }
}
