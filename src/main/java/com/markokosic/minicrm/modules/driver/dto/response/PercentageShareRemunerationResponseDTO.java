package com.markokosic.minicrm.modules.driver.dto.response;

import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PercentageShareRemunerationResponseDTO(
		Long id,
		LocalDate validFrom,
		LocalDate validUntil,
		boolean current,
		RemunerationModelType remunerationModelType,
		BigDecimal driverRevenueSharePercentage,
		BigDecimal minDriverPayout
) implements RemunerationConfigResponseDTO {}
