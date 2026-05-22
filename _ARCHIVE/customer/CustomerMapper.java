package com.markokosic.minicrm.modules.customer;

import com.markokosic.minicrm.modules.customer.dto.request.*;
import com.markokosic.minicrm.modules.customer.dto.response.BusinessCustomerResponseDTO;
import com.markokosic.minicrm.modules.customer.dto.response.ConsumerCustomerResponseDTO;
import com.markokosic.minicrm.modules.customer.dto.response.CustomerResponseDTO;
import com.markokosic.minicrm.modules.customer.model.BusinessCustomer;
import com.markokosic.minicrm.modules.customer.model.ConsumerCustomer;
import com.markokosic.minicrm.modules.customer.model.Customer;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

	@SubclassMapping(source = ConsumerCustomer.class, target = ConsumerCustomerResponseDTO.class)
	@SubclassMapping(source = BusinessCustomer.class, target = BusinessCustomerResponseDTO.class)
	CustomerResponseDTO toDto(Customer customer);

	@SubclassMapping(source = CreateConsumerCustomerRequestDTO.class, target = ConsumerCustomer.class)
	@SubclassMapping(source = CreateBusinessCustomerRequestDTO.class, target = BusinessCustomer.class)
	@Mapping(target = "tenantId", expression = "java(tenantId)")
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "status", ignore = true)
	Customer toEntity(CreateCustomerRequestDTO dto, @Context Long tenantId);


	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateBusinessCustomerFromDTO(UpdateBusinessCustomerRequestDTO dto, @MappingTarget BusinessCustomer customer);
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateConsumerCustomerFromDTO(UpdateConsumerCustomerRequestDTO dto, @MappingTarget ConsumerCustomer customer);

}
