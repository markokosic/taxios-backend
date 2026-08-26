package com.markokosic.minicrm.modules.shift;

import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.shift.dto.request.CreateShiftRequestDTO;
import com.markokosic.minicrm.modules.shift.dto.response.ShiftResponseDTO;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ShiftRevenueEntryMapper.class})
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

	@Mapping(source = "driver", target = "driver")
	@Mapping(source = "car", target = "car")
	@Mapping(expression = "java(entity.getKilometersDriven())", target = "kilometersDriven")
	@Mapping(source = "revenues", target = "revenues")
	ShiftResponseDTO toDto(Shift entity);
}
