package com.markokosic.minicrm.modules.driver;

import com.markokosic.minicrm.modules.driver.dto.request.CreatePercentageShareRemunerationConfigDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateRemunerationRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateWeeklyFixedRemunerationConfigDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateFlatRateRemunerationConfigDTO;
import com.markokosic.minicrm.modules.driver.dto.response.PercentageShareRemunerationResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.response.RemunerationConfigResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.response.WeeklyFixedRateRemunerationResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.response.FlatRateRemunerationResponseDTO;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.WeeklyFixedRateRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig;
import com.markokosic.minicrm.modules.shift.FlatRateType;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RemunerationConfigMapper {

	default DriverRemunerationConfig toEntity(
			CreateRemunerationRequestDTO dto,
			Driver driver
	) {
		if (dto instanceof CreatePercentageShareRemunerationConfigDTO percentageDto) {
			return toPercentageShareEntity(percentageDto, driver);
		} else if (dto instanceof CreateWeeklyFixedRemunerationConfigDTO weeklyDto) {
			return toWeeklyFixedEntity(weeklyDto, driver);
		} else if (dto instanceof CreateFlatRateRemunerationConfigDTO flatDto) {
			return toFlatRateEntity(flatDto, driver, null);
		}
		throw new IllegalArgumentException("Unknown DTO type: " + dto.getClass());
	}

	default RemunerationConfigResponseDTO toResponseDto(DriverRemunerationConfig entity) {
		if (entity instanceof PercentageShareRemunerationConfig percentage) {
			return toPercentageShareResponseDto(percentage);
		} else if (entity instanceof WeeklyFixedRateRemunerationConfig weekly) {
			return toWeeklyFixedResponseDto(weekly);
		} else if (entity instanceof FlatRateRemunerationConfig flat) {
			return toFlatRateResponseDto(flat);
		}
		return null;
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", ignore = true)
	@Mapping(target = "current", ignore = true)
	@Mapping(target = "validFrom", ignore = true)
	@Mapping(target = "validUntil", ignore = true)
	@Mapping(target = "flatRateType", ignore = true)
	@Mapping(target = "driverRevenueSharePercentage", source = "driverRevenueSharePercentage")
	@Mapping(target = "minDriverPayout", source = "minDriverPayout")
	PercentageShareRemunerationConfig toPercentageShareEntity(
			CreatePercentageShareRemunerationConfigDTO dto,
			@Context Driver driver
	);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", ignore = true)
	@Mapping(target = "current", ignore = true)
	@Mapping(target = "validFrom", ignore = true)
	@Mapping(target = "validUntil", ignore = true)
	@Mapping(target = "flatRateType", ignore = true)
	@Mapping(target = "weeklyFixedCompanySettlement", source = "weeklyFixedCompanySettlement")
	WeeklyFixedRateRemunerationConfig toWeeklyFixedEntity(
			CreateWeeklyFixedRemunerationConfigDTO dto,
			@Context Driver driver
	);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", ignore = true)
	@Mapping(target = "current", ignore = true)
	@Mapping(target = "validFrom", ignore = true)
	@Mapping(target = "validUntil", ignore = true)
	@Mapping(target = "flatRateFee", source = "dto.flatRateFee")
	@Mapping(target = "flatRateType", source = "flatRateType")
	FlatRateRemunerationConfig toFlatRateEntity(
			CreateFlatRateRemunerationConfigDTO dto,
			@Context Driver driver,
			FlatRateType flatRateType
	);

	@Mapping(target = "remunerationModelType", source = "type")
	PercentageShareRemunerationResponseDTO toPercentageShareResponseDto(PercentageShareRemunerationConfig entity);

	@Mapping(target = "remunerationModelType", source = "type")
	WeeklyFixedRateRemunerationResponseDTO toWeeklyFixedResponseDto(WeeklyFixedRateRemunerationConfig entity);

	@Mapping(target = "remunerationModelType", source = "type")
	@Mapping(source = "flatRateType.id", target = "flatRateTypeId")
	@Mapping(source = "flatRateType.name", target = "flatRateTypeName")
	FlatRateRemunerationResponseDTO toFlatRateResponseDto(FlatRateRemunerationConfig entity);
}
