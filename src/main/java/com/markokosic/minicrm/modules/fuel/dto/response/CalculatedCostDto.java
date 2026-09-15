package com.markokosic.minicrm.modules.fuel.dto.response;

import java.math.BigDecimal;

public record CalculatedCostDto(
        BigDecimal refuelTotalEur,
        BigDecimal tripCostEur,
        BigDecimal totalEur
) {}
