package com.markokosic.minicrm.modules.shift;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import com.markokosic.minicrm.modules.shift.dto.response.ShiftSettlementResponseDTO;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftSettlement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {com.markokosic.minicrm.modules.driver.RemunerationConfigMapper.class})
public interface ShiftSettlementMapper {

	ShiftSettlementResponseDTO toDto(ShiftSettlement entity);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", source = "shift.tenantId")
	@Mapping(target = "shift", source = "shift")
	@Mapping(target = "totalRevenue", source = "totalRevenue")
	@Mapping(target = "driverRemuneration", source = "split.driverRemuneration")
	@Mapping(target = "companyRemuneration", source = "split.companyRemuneration")
	@Mapping(target = "settledAt", expression = "java(java.time.LocalDateTime.now())")
	ShiftSettlement toEntity(Shift shift, BigDecimal totalRevenue, RemunerationSplit split);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", ignore = true)
	@Mapping(target = "shift", ignore = true)
	@Mapping(target = "totalRevenue", source = "totalRevenue")
	@Mapping(target = "driverRemuneration", source = "split.driverRemuneration")
	@Mapping(target = "companyRemuneration", source = "split.companyRemuneration")
	@Mapping(target = "settledAt", expression = "java(java.time.LocalDateTime.now())")
	void updateEntity(@MappingTarget ShiftSettlement settlement, BigDecimal totalRevenue, RemunerationSplit split);
}
