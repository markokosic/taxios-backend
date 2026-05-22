package com.markokosic.minicrm.modules.car;

import com.markokosic.minicrm.modules.car.dto.request.CreateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.request.UpdateCarRequestDTO;
import com.markokosic.minicrm.modules.car.dto.response.CarResponseDTO;
import com.markokosic.minicrm.modules.car.model.Car;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CarMapper {

    CarResponseDTO toDto(Car car);

    @Mapping(target = "tenantId", expression = "java(tenantId)")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Car toEntity(CreateCarRequestDTO dto, @Context Long tenantId);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateCarRequestDTO dto, @MappingTarget Car car);
}
