package com.markokosic.minicrm.modules.revenue;

import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface RevenueMapper {

//	@Mapping(target = "driver", expression = "java(driverRepository.getReferenceById(dto.driverId()))")
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", expression = "java(tenantId)")
	@Mapping(target = "car", source = "car")
	@Mapping(target = "remunerationConfig", source = "remunerationConfig")
	@Mapping(target= "companyRemuneration", source="companyRemuneration")
	@Mapping(target= "driverRemuneration", source="driverRemuneration")
	DailyRevenue toEntity(
			CreateDailyRevenueRequestDTO dto,
			@Context Long tenantId,
			Driver driver,
			com.markokosic.minicrm.modules.car.model.Car car,
			DriverRemunerationConfig remunerationConfig,
			BigDecimal companyRemuneration,
			BigDecimal driverRemuneration
	);
}
