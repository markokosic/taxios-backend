package com.markokosic.minicrm.modules.driver.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateFlatRateRemunerationConfigDTO(

		@NotNull
		@Enumerated(EnumType.STRING)
		RemunerationModelType remunerationModelType,

		@JsonFormat(shape = JsonFormat.Shape.NUMBER)
		@NotNull
		@PositiveOrZero( message = "{driver.flatRateFee.negative}")
		BigDecimal flatRateFee

) implements CreateRemunerationRequestDTO {
}
