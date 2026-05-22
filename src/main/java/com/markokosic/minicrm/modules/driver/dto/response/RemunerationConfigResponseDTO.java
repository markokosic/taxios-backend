package com.markokosic.minicrm.modules.driver.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import java.time.LocalDate;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		property = "remunerationModelType",
		visible = true
)
@JsonSubTypes({
		@JsonSubTypes.Type(value = PercentageShareRemunerationResponseDTO.class, name = "PERCENTAGE_SHARE"),
		@JsonSubTypes.Type(value = WeeklyFixedRateRemunerationResponseDTO.class, name = "WEEKLY_FIXED_RATE")
})
public sealed interface RemunerationConfigResponseDTO
		permits PercentageShareRemunerationResponseDTO, WeeklyFixedRateRemunerationResponseDTO {
	Long id();
	LocalDate validFrom();
	LocalDate validUntil();
	boolean current();
	RemunerationModelType remunerationModelType();
}
