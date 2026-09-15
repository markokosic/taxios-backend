package com.markokosic.minicrm.modules.fuel.dto.response;

import java.math.BigDecimal;

public record FuelStationRecommendationResponse(
        Long id,
        String name,
        String address,
        Double distanceKm,
        BigDecimal pricePerLiter,
        CalculatedCostDto calculatedCost,
        String googleMapsUrl,
        boolean isOpen
) {}
