package com.markokosic.minicrm.modules.customer;

import com.markokosic.minicrm.common.error.ApiErrorCode;
import com.markokosic.minicrm.exception.NotFoundException;
import com.markokosic.minicrm.modules.customer.model.Customer;
import com.markokosic.minicrm.modules.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerTenantValidator {
	private final CustomerRepository customerRepository;
	private final TenantService tenantService;

	public Customer getCustomerByTenantAndIdOrThrow(Long id) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();
		return customerRepository.findByIdAndTenantId(id, tenantId)
				.orElseThrow(() -> new NotFoundException(ApiErrorCode.CUSTOMER_NOT_FOUND));
	}



}
