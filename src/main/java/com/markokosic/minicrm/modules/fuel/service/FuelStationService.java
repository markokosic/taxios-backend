package com.markokosic.minicrm.modules.fuel.service;

import com.markokosic.minicrm.modules.fuel.client.EControlGasStation;
import com.markokosic.minicrm.modules.fuel.client.EControlPrice;
import com.markokosic.minicrm.modules.fuel.dto.response.CalculatedCostDto;
import com.markokosic.minicrm.modules.fuel.dto.response.FuelStationRecommendationResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FuelStationService {

    private static final double DEFAULT_CONSUMPTION_LITERS_PER_100_KM = 5.5;
    private static final double DEFAULT_TANK_AMOUNT_LITERS = 30.0;
    private static final String DEFAULT_FUEL_TYPE = "SUP"; //DIE, SUP, GAS
    private static final String E_CONTROL_API_URL = "https://api.e-control.at/sprit/1.0/search/gas-stations/by-address";
    
    private final RestClient restClient;

    public FuelStationService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public List<FuelStationRecommendationResponse> getRecommendations(double lat, double lng, String fuelType, double tankAmount) {
        List<EControlGasStation> stations = fetchGasStations(lat, lng, fuelType);

        return stations.stream()
                .filter(EControlGasStation::open)
                .map(station -> mapToRecommendation(station, tankAmount))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(r -> r.calculatedCost().totalEur()))
                .collect(Collectors.toList());
    }

    private List<EControlGasStation> fetchGasStations(double lat, double lng, String fuelType) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.e-control.at")
                        .path("/sprit/1.0/search/gas-stations/by-address")
                        .queryParam("latitude", lat)
                        .queryParam("longitude", lng)
                        .queryParam("fuelType", fuelType)
                        .queryParam("includeClosed", false)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<EControlGasStation>>() {});
    }

    private FuelStationRecommendationResponse mapToRecommendation(EControlGasStation station, double tankAmount) {
        if (station.prices() == null || station.prices().isEmpty()) {
            return null;
        }

        EControlPrice priceData = station.prices().get(0);
        BigDecimal pricePerLiter = priceData.amount();
        double distanceKm = station.distance() != null ? station.distance() : 0.0;

        CalculatedCostDto costDto = calculateCosts(pricePerLiter, distanceKm, tankAmount);
        
        String address = formatAddress(station);
        String googleMapsUrl = generateGoogleMapsUrl(station);

        return new FuelStationRecommendationResponse(
                station.id(),
                station.name(),
                address,
                distanceKm,
                pricePerLiter,
                costDto,
                googleMapsUrl,
                station.open()
        );
    }

    private CalculatedCostDto calculateCosts(BigDecimal pricePerLiter, double distanceKm, double tankAmount) {
        // refuelTotal = pricePerLiter * tankAmount
        BigDecimal refuelTotal = pricePerLiter.multiply(BigDecimal.valueOf(tankAmount)).setScale(2, RoundingMode.HALF_UP);
        
        // tripCost = distanceKm * 2 * (avg. consumption / 100) * pricePerLiter
        double consumedLiters = (distanceKm * 2) * (DEFAULT_CONSUMPTION_LITERS_PER_100_KM / 100.0);
        BigDecimal tripCost = pricePerLiter.multiply(BigDecimal.valueOf(consumedLiters)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = refuelTotal.add(tripCost);

        return new CalculatedCostDto(
                refuelTotal,
                tripCost,
                total
        );
    }

    private String formatAddress(EControlGasStation station) {
        if (station.location() == null) return "";
        return String.format("%s, %s %s", 
                station.location().address() != null ? station.location().address() : "",
                station.location().postalCode() != null ? station.location().postalCode() : "",
                station.location().city() != null ? station.location().city() : ""
        ).trim();
    }

    private String generateGoogleMapsUrl(EControlGasStation station) {
        if (station.location() == null || station.location().latitude() == null || station.location().longitude() == null) {
            return "";
        }
        return String.format("https://www.google.com/maps/dir/?api=1&destination=%s,%s",
                station.location().latitude(),
                station.location().longitude()
        );
    }
}
