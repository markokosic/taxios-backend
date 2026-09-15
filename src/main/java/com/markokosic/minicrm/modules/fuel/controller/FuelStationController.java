package com.markokosic.minicrm.modules.fuel.controller;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import com.markokosic.minicrm.modules.fuel.dto.response.FuelStationRecommendationResponse;
import com.markokosic.minicrm.modules.fuel.service.FuelStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fuel-stations")
@RequiredArgsConstructor
@Tag(name = "Fuel Station", description = "Fuel station endpoints")
public class FuelStationController {

    private final FuelStationService fuelStationService;
    private final I18nService i18n;

    @GetMapping("/recommendations")
    @Operation(summary = "Get fuel station recommendations", description = "Calculates the most cost-effective fuel stations based on current location")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole(T(com.markokosic.minicrm.modules.role.dto.Roles).DRIVER.name(), T(com.markokosic.minicrm.modules.role.dto.Roles).ADMIN.name(), T(com.markokosic.minicrm.modules.role.dto.Roles).OWNER.name())")
    public ResponseEntity<ApiResponseDTO<List<FuelStationRecommendationResponse>>> getRecommendations(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false, defaultValue = "SUP") String fuelType,
            @RequestParam(required = false, defaultValue = "30.0") double tankAmount
    ) {
        List<FuelStationRecommendationResponse> recommendations = fuelStationService.getRecommendations(lat, lng, fuelType, tankAmount);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, recommendations, i18n.getMessage("success.fetched")));
    }
}
