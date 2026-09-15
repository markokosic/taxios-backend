package com.markokosic.minicrm.modules.flatratetype;

import com.markokosic.minicrm.modules.flatratetype.dto.request.CreateFlatRateTypeRequestDTO;
import com.markokosic.minicrm.modules.flatratetype.dto.response.FlatRateTypeResponseDTO;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FlatRateTypeMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", ignore = true)
	@Mapping(target = "flatRateCode", ignore = true)
	@Mapping(target = "current", ignore = true)
	@Mapping(target = "validFrom", ignore = true)
	@Mapping(target = "validUntil", ignore = true)
	FlatRateType toEntity(CreateFlatRateTypeRequestDTO dto);

	FlatRateTypeResponseDTO toDto(FlatRateType entity);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "tenantId", ignore = true)
	@Mapping(target = "flatRateCode", ignore = true)
	@Mapping(target = "current", ignore = true)
	@Mapping(target = "validFrom", ignore = true)
	@Mapping(target = "validUntil", ignore = true)
	void updateEntityFromDto(CreateFlatRateTypeRequestDTO dto, @MappingTarget FlatRateType entity);
}
