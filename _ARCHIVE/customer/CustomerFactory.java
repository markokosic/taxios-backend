package com.markokosic.minicrm.modules.customer;

import com.markokosic.minicrm.modules.customer.dto.request.CreateCustomerRequestDTO;
import com.markokosic.minicrm.modules.customer.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerFactory {

	private final CustomerMapper mapper;

	public CustomerFactory(CustomerMapper mapper) {
		this.mapper = mapper;
	}

	public Customer createCustomer(CreateCustomerRequestDTO dto, Long tenantId) {
		return mapper.toEntity(dto, tenantId);
	}

}
