package com.markokosic.minicrm.modules.fuel.client;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EControlGasStation(
        Long id,
        String name,
        EControlLocation location,
        Double distance,
        List<EControlPrice> prices,
        boolean open
) {}
