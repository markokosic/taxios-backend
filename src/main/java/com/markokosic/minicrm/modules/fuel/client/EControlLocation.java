package com.markokosic.minicrm.modules.fuel.client;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EControlLocation(
        String address,
        String postalCode,
        String city,
        Double latitude,
        Double longitude
) {}
