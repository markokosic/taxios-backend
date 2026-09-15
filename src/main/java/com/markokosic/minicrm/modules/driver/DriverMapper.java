package com.markokosic.minicrm.modules.driver;

import com.markokosic.minicrm.modules.driver.dto.request.CreateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.request.UpdateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverResponseDTO;
import com.markokosic.minicrm.modules.driver.model.Driver;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {RemunerationConfigMapper.class})
public interface DriverMapper {

	@Mapping(target = "userId", source = "user.id")
	@Mapping(target = "currentRemunerationConfigs", source = "driver", qualifiedByName = "mapCurrentConfigs")
	DriverResponseDTO toDto(Driver driver, @Context RemunerationConfigMapper remunerationConfigMapper);

	@Named("mapCurrentConfigs")
	default java.util.List<com.markokosic.minicrm.modules.driver.dto.response.RemunerationConfigResponseDTO> mapCurrentConfigs(Driver driver, @Context RemunerationConfigMapper remunerationConfigMapper) {
		return driver.getRemunerationConfigs().stream()
				.filter(com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig::isCurrent)
				.map(remunerationConfigMapper::toResponseDto)
				.toList();
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "remunerationConfigs", ignore = true)
	Driver toEntity(CreateDriverRequestDTO dto);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target ="remunerationConfigs", ignore = true)
	void updateEntityFromDto(UpdateDriverRequestDTO dto, @MappingTarget Driver driver);

}
