package com.markokosic.minicrm.modules.driver.dto.response;

import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Response object for percentage share remuneration model")
public record PercentageShareRemunerationResponseDTO(
		Long id,
		LocalDate validFrom,
		LocalDate validUntil,
		boolean current,
		RemunerationModelType remunerationModelType,
		@Schema(description = "Revenue share factor (e.g. 0.4500 for 45%)", example = "0.4500")
		BigDecimal driverRevenueSharePercentage,
		@Schema(description = "Minimum guaranteed driver payout per shift in EUR", example = "50.00")
		BigDecimal minDriverPayoutPerShift
) implements RemunerationConfigResponseDTO {}
