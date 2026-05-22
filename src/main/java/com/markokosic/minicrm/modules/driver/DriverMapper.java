package com.markokosic.minicrm.modules.driver;

import com.markokosic.minicrm.modules.driver.dto.request.CreateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.request.UpdateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverResponseDTO;
import com.markokosic.minicrm.modules.driver.model.Driver;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {RemunerationConfigMapper.class})
public interface DriverMapper {

	@Mapping(target = "currentRemunerationConfig", source = "currentRemunerationConfig")
	DriverResponseDTO toDto(Driver driver);

	@Mapping(target = "tenantId", expression = "java(tenantId)")
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "status", ignore = true)
	Driver toEntity(CreateDriverRequestDTO dto, @Context Long tenantId);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target ="remunerationConfigs", ignore = true)
	void updateEntityFromDto(UpdateDriverRequestDTO dto, @MappingTarget Driver driver);

}
