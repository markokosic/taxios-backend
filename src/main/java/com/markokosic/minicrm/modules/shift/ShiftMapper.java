package com.markokosic.minicrm.modules.shift;

import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.shift.dto.request.CreateShiftRequestDTO;
import com.markokosic.minicrm.modules.shift.dto.response.ShiftResponseDTO;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.springframework.beans.factory.annotation.Autowired;
import com.markokosic.minicrm.modules.driver.RemunerationConfigMapper;
import com.markokosic.minicrm.modules.driver.dto.response.RemunerationConfigResponseDTO;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {ShiftRevenueEntryMapper.class, ShiftSettlementMapper.class})
public abstract class ShiftMapper {

    @Autowired
    protected RemunerationConfigMapper remunerationConfigMapper;

    protected List<RemunerationConfigResponseDTO> mapAppliedConfigs(Shift shift) {
        if (shift.getAppliedRemunerationConfigs() == null) {
            return new ArrayList<>();
        }
        return shift.getAppliedRemunerationConfigs().stream()
                .map(c -> remunerationConfigMapper.toResponseDto(c))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

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
	@Mapping(target = "settlement", ignore = true)
	@Mapping(target = "appliedRemunerationConfigs", ignore = true)
	@Mapping(target = "weeklyDriverRent", source = "dto.weeklyDriverRent")
	public abstract Shift toShiftEntity(
			CreateShiftRequestDTO dto,
			Driver driver,
			Car car,
			ShiftStatus status
	);

	@Mapping(source = "driver", target = "driver")
	@Mapping(source = "car", target = "car")
	@Mapping(expression = "java(entity.getKilometersDriven())", target = "kilometersDriven")
	@Mapping(source = "revenues", target = "revenues")
	@Mapping(expression = "java(mapAppliedConfigs(entity))", target = "appliedRemunerationConfigs")
	public abstract ShiftResponseDTO toDto(Shift entity);
}
