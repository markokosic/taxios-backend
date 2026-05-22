package com.markokosic.minicrm.modules.driver;

import com.markokosic.minicrm.modules.driver.dto.request.CreatePercentageShareRemunerationConfigDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateRemunerationRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateWeeklyFixedRemunerationConfigDTO;
import com.markokosic.minicrm.modules.driver.dto.response.PercentageShareRemunerationResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.response.RemunerationConfigResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.response.WeeklyFixedRateRemunerationResponseDTO;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.WeeklyFixedRateRemunerationConfig;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RemunerationConfigMapper {


	default DriverRemunerationConfig toEntity(
			CreateRemunerationRequestDTO dto,
			Long tenantId,
			Driver driver
	) {
		if (dto instanceof CreatePercentageShareRemunerationConfigDTO percentageDto) {
			return toPercentageShareEntity(percentageDto, tenantId, driver);
		} else if (dto instanceof CreateWeeklyFixedRemunerationConfigDTO weeklyDto) {
			return toWeeklyFixedEntity(weeklyDto, tenantId, driver);
		}
		throw new IllegalArgumentException("Unknown DTO type: " + dto.getClass());
	}

	default RemunerationConfigResponseDTO toResponseDto(DriverRemunerationConfig entity) {
		if (entity instanceof PercentageShareRemunerationConfig percentage) {
			return toPercentageShareResponseDto(percentage);
		} else if (entity instanceof WeeklyFixedRateRemunerationConfig weekly) {
			return toWeeklyFixedResponseDto(weekly);
		}
		return null;
	}

	@Mapping(target = "id", ignore = true)
//	@Mapping(target = "driver", ignore = true)
	@Mapping(target = "tenantId", expression = "java(tenantId)")
	@Mapping(target = "current", ignore = true)
	@Mapping(target = "validFrom", ignore = true)
	@Mapping(target = "validUntil", ignore = true)
	@Mapping(target = "driverRevenueSharePercentage", source = "driverRevenueSharePercentage")
	@Mapping(target = "minDriverPayout", source = "minDriverPayout")
	PercentageShareRemunerationConfig toPercentageShareEntity(
			CreatePercentageShareRemunerationConfigDTO dto,
			@Context Long tenantId,
			@Context Driver driver
	);

	@Mapping(target = "id", ignore = true)
//	@Mapping(target = "driver", ignore = true)
	@Mapping(target = "tenantId", expression = "java(tenantId)")
	@Mapping(target = "current", ignore = true)
	@Mapping(target = "validFrom", ignore = true)
	@Mapping(target = "validUntil", ignore = true)
	@Mapping(target = "weeklyFixedCompanySettlement", source = "weeklyFixedCompanySettlement")
	WeeklyFixedRateRemunerationConfig toWeeklyFixedEntity(
			CreateWeeklyFixedRemunerationConfigDTO dto,
			@Context Long tenantId,
			@Context Driver driver
	);

	@Mapping(target = "remunerationModelType", expression = "java(com.markokosic.minicrm.modules.remuneration.RemunerationModelType.PERCENTAGE_SHARE)")
	PercentageShareRemunerationResponseDTO toPercentageShareResponseDto(PercentageShareRemunerationConfig entity);

	@Mapping(target = "remunerationModelType", expression = "java(com.markokosic.minicrm.modules.remuneration.RemunerationModelType.WEEKLY_FIXED_RATE)")
	WeeklyFixedRateRemunerationResponseDTO toWeeklyFixedResponseDto(WeeklyFixedRateRemunerationConfig entity);


}
