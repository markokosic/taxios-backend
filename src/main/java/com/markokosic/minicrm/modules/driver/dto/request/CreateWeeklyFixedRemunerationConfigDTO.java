package com.markokosic.minicrm.modules.driver.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateWeeklyFixedRemunerationConfigDTO(

		@NotNull
		@Enumerated(EnumType.STRING)
		RemunerationModelType remunerationModelType,

		@JsonFormat(shape = JsonFormat.Shape.NUMBER)
		@NotNull
		BigDecimal weeklyFixedCompanySettlement,

		@Min(1) //Monday
		@Max(7)
		@NotNull
		Integer settlementDay

) implements CreateRemunerationRequestDTO {
}
