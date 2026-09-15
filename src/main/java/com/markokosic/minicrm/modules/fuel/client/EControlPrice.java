package com.markokosic.minicrm.modules.fuel.client;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EControlPrice(
        String fuelType,
        BigDecimal amount,
        String label
) {}
