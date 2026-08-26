package com.markokosic.minicrm.modules.shift;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import com.markokosic.minicrm.modules.shift.dto.response.ShiftRevenueEntryResponseDTO;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ShiftRevenueEntryMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", ignore = true)
	@Mapping(target = "shift", source = "shift")
	@Mapping(target = "remunerationConfig", source = "config")
	@Mapping(target = "flatRateType", source = "flatRateType")
	@Mapping(target = "entryCategory", source = "category")
	@Mapping(target = "revenue", source = "effectiveRevenue")
	@Mapping(target = "companyRemuneration", source = "split.companyRemuneration")
	@Mapping(target = "driverRemuneration", source = "split.driverRemuneration")
	@Mapping(target = "tripCount", source = "tripCount")
	@Mapping(target = "pricePerTrip", source = "effectivePricePerTrip")
	ShiftRevenueEntry toEntity(
			Shift shift,
			DriverRemunerationConfig config,
			FlatRateType flatRateType,
			ShiftEntryCategory category,
			BigDecimal effectiveRevenue,
			BigDecimal effectivePricePerTrip,
			Long tripCount,
			RemunerationSplit split
	);

	@Mapping(source = "flatRateType.id", target = "flatRateTypeId")
	@Mapping(source = "flatRateType.name", target = "flatRateTypeName")
	@Mapping(source = "remunerationConfig.type", target = "remunerationModelType")
	@Mapping(expression = "java(entity.getEntryCategory() == ShiftEntryCategory.FLAT_RATE)", target = "isFlatRate")
	ShiftRevenueEntryResponseDTO toDto(ShiftRevenueEntry entity);
}
