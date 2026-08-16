package com.markokosic.minicrm.modules.shift;

import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface ShiftMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", ignore = true)
	@Mapping(target = "driver", source = "driver")
	@Mapping(target = "car", source = "car")
	@Mapping(target = "status", source = "status")
	@Mapping(target = "odometerStart", source = "dto.odometerStart")
	@Mapping(target = "odometerEnd", source = "dto.odometerEnd")
	@Mapping(target = "shiftStart", source = "dto.shiftStart")
	@Mapping(target = "shiftEnd", source = "dto.shiftEnd")
	@Mapping(target = "revenues", ignore = true)
	Shift toShiftEntity(
			CreateShiftRequestDTO dto,
			Driver driver,
			Car car,
			ShiftStatus status
	);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", ignore = true)
	@Mapping(target = "shift", source = "shift")
	@Mapping(target = "remunerationConfig", source = "config")
	@Mapping(target = "flatRateType", source = "flatRateType")
	@Mapping(target = "entryCategory", source = "dto.entryCategory")
	@Mapping(target = "flatRate", expression = "java(dto.entryCategory() == ShiftEntryCategory.FLAT_RATE)")
	@Mapping(target = "revenue", source = "effectiveRevenue")
	@Mapping(target = "companyRemuneration", source = "split.companyRemuneration")
	@Mapping(target = "driverRemuneration", source = "split.driverRemuneration")
	@Mapping(target = "tripCount", source = "dto.tripCount")
	@Mapping(target = "pricePerTrip", source = "effectivePricePerTrip")
	ShiftRevenueEntry toRevenueEntryEntity(
			CreateShiftRevenueEntryRequestDTO dto,
			Shift shift,
			DriverRemunerationConfig config,
			FlatRateType flatRateType,
			BigDecimal effectiveRevenue,
			BigDecimal effectivePricePerTrip,
			RemunerationSplit split
	);

	@Mapping(source = "driver", target = "driver")
	@Mapping(source = "car", target = "car")
	@Mapping(expression = "java(entity.getKilometersDriven())", target = "kilometersDriven")
	@Mapping(source = "revenues", target = "revenues")
	ShiftResponseDTO toDto(Shift entity);

	@Mapping(source = "flatRateType.id", target = "flatRateTypeId")
	@Mapping(source = "flatRateType.name", target = "flatRateTypeName")
	@Mapping(source = "remunerationConfig.type", target = "remunerationModelType")
	@Mapping(source = "flatRate", target = "isFlatRate")
	ShiftRevenueEntryResponseDTO toDto(ShiftRevenueEntry entity);
}
