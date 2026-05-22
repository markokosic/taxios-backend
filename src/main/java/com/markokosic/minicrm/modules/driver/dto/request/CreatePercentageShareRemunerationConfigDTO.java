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

public record CreatePercentageShareRemunerationConfigDTO(

	@NotNull
	@Enumerated(EnumType.STRING)
	RemunerationModelType remunerationModelType,

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	@PositiveOrZero(message = "{driver.dailyMinPayout.invalid}")
	Integer minDriverPayout,

	@JsonFormat(shape = JsonFormat.Shape.NUMBER)
	@DecimalMin(value = "0.0", inclusive = false, message = "{driver.driverRevenueSharePercentage.invalid}")
	@DecimalMax(value = "100.0", message = "{driver.driverRevenueSharePercentage.invalid}")
	BigDecimal driverRevenueSharePercentage

) implements CreateRemunerationRequestDTO {
}


