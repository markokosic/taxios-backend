package com.markokosic.minicrm.modules.shift;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.shift.dto.response.ShiftRevenueEntryResponseDTO;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {com.markokosic.minicrm.modules.driver.RemunerationConfigMapper.class})
public interface ShiftRevenueEntryMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", ignore = true)
	@Mapping(target = "shift", source = "shift")
	@Mapping(target = "flatRateType", source = "flatRateType")
	@Mapping(target = "entryCategory", source = "category")
	@Mapping(target = "revenue", source = "effectiveRevenue")
	@Mapping(target = "tripCount", source = "tripCount")
	@Mapping(target = "pricePerTrip", source = "effectivePricePerTrip")
	ShiftRevenueEntry toEntity(
			Shift shift,
			FlatRateType flatRateType,
			ShiftEntryCategory category,
			BigDecimal effectiveRevenue,
			BigDecimal effectivePricePerTrip,
			Long tripCount
	);

	@Mapping(source = "flatRateType.id", target = "flatRateTypeId")
	@Mapping(source = "flatRateType.name", target = "flatRateTypeName")
	@Mapping(expression = "java(entity.getEntryCategory() == com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory.FLAT_RATE ? com.markokosic.minicrm.modules.remuneration.RemunerationModelType.FLAT_RATE : com.markokosic.minicrm.modules.remuneration.RemunerationModelType.PERCENTAGE_SHARE)", target = "remunerationModelType")
	@Mapping(expression = "java(entity.getEntryCategory() == ShiftEntryCategory.FLAT_RATE)", target = "isFlatRate")
	ShiftRevenueEntryResponseDTO toDto(ShiftRevenueEntry entity);
}
