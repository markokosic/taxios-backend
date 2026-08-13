package com.markokosic.minicrm.modules.shift;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FlatRateTypeMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", ignore = true)
	@Mapping(target = "active", constant = "true")
	FlatRateType toEntity(CreateFlatRateTypeRequestDTO dto);

	FlatRateTypeResponseDTO toDto(FlatRateType entity);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", ignore = true)
	@Mapping(target = "active", ignore = true)
	void updateEntityFromDto(CreateFlatRateTypeRequestDTO dto, @MappingTarget FlatRateType entity);
}
