package com.markokosic.minicrm.modules.driver.dto.request;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;

@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.EXISTING_PROPERTY,
		property = "remunerationModelType",
		visible = true
)
@JsonSubTypes({
		@JsonSubTypes.Type(value = CreatePercentageShareRemunerationConfigDTO.class, name = "PERCENTAGE_SHARE"),
		@JsonSubTypes.Type(value = CreateWeeklyFixedRemunerationConfigDTO.class, name = "WEEKLY_FIXED_RATE"),
		@JsonSubTypes.Type(value = CreateFlatRateRemunerationConfigDTO.class, name = "FLAT_RATE")
})
public sealed interface CreateRemunerationRequestDTO
		permits CreatePercentageShareRemunerationConfigDTO, CreateWeeklyFixedRemunerationConfigDTO, CreateFlatRateRemunerationConfigDTO {
	RemunerationModelType remunerationModelType();
}
