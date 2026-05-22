package com.markokosic.minicrm.modules.customer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.common.error.ApiErrorCode;
import com.markokosic.minicrm.common.utils.JsonUtils;
import com.markokosic.minicrm.exception.NotFoundException;
import com.markokosic.minicrm.modules.customer.dto.request.CreateCustomerRequestDTO;
import com.markokosic.minicrm.modules.customer.dto.request.UpdateBusinessCustomerRequestDTO;
import com.markokosic.minicrm.modules.customer.dto.request.UpdateConsumerCustomerRequestDTO;
import com.markokosic.minicrm.modules.customer.dto.response.CustomerResponseDTO;
import com.markokosic.minicrm.modules.customer.model.BusinessCustomer;
import com.markokosic.minicrm.modules.customer.model.ConsumerCustomer;
import com.markokosic.minicrm.modules.customer.model.Customer;
import com.markokosic.minicrm.modules.customer.model.CustomerStatus;
import com.markokosic.minicrm.modules.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerService {

	private final TenantService tenantService;
	private final CustomerMapper customerMapper;
	private final CustomerRepository customerRepository;
	private final ObjectMapper objectMapper;
	private final CustomerTenantValidator validator;


	@Transactional
	public CustomerResponseDTO createCustomer(CreateCustomerRequestDTO request ) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();
		Customer customer = customerMapper.toEntity(request, tenantId);
		customerRepository.save(customer);
			return customerMapper.toDto(customer);
	}

	public PageResponseDTO<CustomerResponseDTO> getAllCustomers(Pageable pageable ) {
		return getCustomersByTenant(pageable);
	}

	public  CustomerResponseDTO getCustomerById(Long id ) {
		return customerMapper.toDto(validator.getCustomerByTenantAndIdOrThrow(id));
	}

	@Transactional
	public CustomerResponseDTO updateCustomer(Long id, JsonNode requestBody) {

		Customer customer = validator.getCustomerByTenantAndIdOrThrow(id);

		if (customer instanceof BusinessCustomer) {
			validateAllowedFieldsForBusinessCustomer(requestBody);
			UpdateBusinessCustomerRequestDTO dto =
					objectMapper.convertValue(requestBody, UpdateBusinessCustomerRequestDTO.class);
			customerMapper.updateBusinessCustomerFromDTO(dto, (BusinessCustomer) customer);
		} else if (customer instanceof ConsumerCustomer) {
			validateAllowedFieldsForConsumerCustomer(requestBody);
			UpdateConsumerCustomerRequestDTO dto =
					objectMapper.convertValue(requestBody, UpdateConsumerCustomerRequestDTO.class);
			customerMapper.updateConsumerCustomerFromDTO(dto, (ConsumerCustomer) customer);
		}

		customerRepository.save(customer);
		return customerMapper.toDto(customer);

	}

	@Transactional
	public void deleteCustomer(Long id) {
		Customer customer = validateCustomerDeletion(id);
		customer.setStatus(CustomerStatus.DELETED);
	}


	private void validateAllowedFieldsForBusinessCustomer(JsonNode requestBody) {
		var allowedFields = Set.of("companyName", "vat", "email", "phone", "website");
		JsonUtils.validateAllowedFields(requestBody, allowedFields);
	}

	private void validateAllowedFieldsForConsumerCustomer(JsonNode requestBody) {
		var allowedFields = Set.of("firstName", "lastName", "email", "phone");
		JsonUtils.validateAllowedFields(requestBody, allowedFields);
	}

//	 private Customer getCustomerByTenantAndIdOrThrow(Long id) {
//		 Long tenantId = tenantService.getTenantIdFromContextHolder();
//		return customerRepository.findByIdAndTenantId(id, tenantId)
//			.orElseThrow(() -> new NotFoundException(ApiErrorCode.CUSTOMER_NOT_FOUND));
//	}

	private Customer validateCustomerDeletion(Long id) {
		Customer customer = validator.getCustomerByTenantAndIdOrThrow(id);

		if (CustomerStatus.DELETED.equals(customer.getStatus())) {
			throw new NotFoundException(ApiErrorCode.USER_NOT_FOUND);
		}

//		if (hasActiveOrders(customer.getId())) {
//			throw new ConflictException(ApiErrorCode.CUSTOMER_HAS_ACTIVE_ORDERS);
//		}

		return customer;
	}


	private PageResponseDTO<CustomerResponseDTO> getCustomersByTenant(Pageable pageable) {
		Long tenantId = tenantService.getTenantIdFromContextHolder();
		Page<CustomerResponseDTO> page = customerRepository.findAllByTenantIdAndStatus(tenantId, CustomerStatus.ACTIVE, pageable).map(customerMapper::toDto);

		return PageResponseDTO.from(page);
	}


}
