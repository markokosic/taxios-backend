package com.markokosic.minicrm.modules.driver.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for percentage share remuneration model")
public record CreatePercentageShareRemunerationConfigDTO(

	@NotNull
	@Enumerated(EnumType.STRING)
	@Schema(description = "Remuneration model type", example = "PERCENTAGE_SHARE", requiredMode = Schema.RequiredMode.REQUIRED)
	RemunerationModelType remunerationModelType,

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	@PositiveOrZero(message = "{driver.dailyMinPayout.invalid}")
	@Schema(description = "Minimum guaranteed driver payout per shift in EUR", example = "50.00")
	BigDecimal minDriverPayoutPerShift,

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	@DecimalMin(value = "0.0", inclusive = true, message = "{driver.driverRevenueSharePercentage.invalid}")
	@DecimalMax(value = "1.0", message = "{driver.driverRevenueSharePercentage.invalid}")
	@Schema(description = "Revenue share factor (e.g. 0.4500 for 45%)", example = "0.4500", requiredMode = Schema.RequiredMode.REQUIRED)
	BigDecimal driverRevenueSharePercentage

) implements CreateRemunerationRequestDTO {
}


