package com.markokosic.minicrm.modules.address;

import com.markokosic.minicrm.common.error.ApiErrorCode;
import com.markokosic.minicrm.exception.NotFoundException;
import com.markokosic.minicrm.modules.address.dto.request.CreateAddressRequestDTO;
import com.markokosic.minicrm.modules.address.dto.response.AddressResponseDTO;
import com.markokosic.minicrm.modules.address.model.Address;
import com.markokosic.minicrm.modules.address.model.AddressStatus;
import com.markokosic.minicrm.modules.customer.CustomerTenantValidator;
import com.markokosic.minicrm.modules.customer.model.Customer;
import com.markokosic.minicrm.modules.tenant.TenantService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
	private final CustomerTenantValidator customerTenantValidator;
	private final AddressMapper addressMapper;
	private final AddressRepository addressRepository;
	private final TenantService tenantService;

	@Transactional
	public AddressResponseDTO createAddress(Long customerId , CreateAddressRequestDTO dto) {
		Customer customer = customerTenantValidator.getCustomerByTenantAndIdOrThrow(customerId);
		Long tenantId = tenantService.getTenantIdFromContextHolder();
		Address address = addressMapper.toEntity(dto,tenantId, customer);
		Address saved = addressRepository.save(address);
		return addressMapper.toResponseDto(saved);
	}

	public List<AddressResponseDTO> getAddressesByCustomer(Long customerId){
		 customerTenantValidator.getCustomerByTenantAndIdOrThrow(customerId);
		Long tenantId = tenantService.getTenantIdFromContextHolder();
		return addressRepository.findAllByTenantIdAndCustomerIdAndStatus(tenantId, customerId, AddressStatus.ACTIVE)
				.stream()
				.map(addressMapper::toResponseDto)
				.toList();
	}

	public AddressResponseDTO getAddressById(Long customerId, Long addressId) {
		customerTenantValidator.getCustomerByTenantAndIdOrThrow(customerId);
		Long tenantId = tenantService.getTenantIdFromContextHolder();

		return addressMapper.toResponseDto(
				addressRepository.findByTenantIdAndCustomerIdAndIdAndStatus(tenantId, customerId, addressId, AddressStatus.ACTIVE)
						.orElseThrow(() -> new NotFoundException(ApiErrorCode.ADDRESS_NOT_FOUND))
		);
	}

	@Transactional
	public AddressResponseDTO updateAddress(Long customerId, Long addressId, CreateAddressRequestDTO dto) {
		customerTenantValidator.getCustomerByTenantAndIdOrThrow(customerId);
		Long tenantId = tenantService.getTenantIdFromContextHolder();

		Address address = addressRepository.findByTenantIdAndCustomerIdAndIdAndStatus(addressId, customerId, tenantId, AddressStatus.ACTIVE)
				.orElseThrow(() -> new NotFoundException(ApiErrorCode.ADDRESS_NOT_FOUND));

		addressMapper.updateEntityFromDto(dto, address);
		Address updated = addressRepository.save(address);
		return addressMapper.toResponseDto(updated);
	}

	@Transactional
	public void deleteAddress(Long customerId, Long addressId) {
		customerTenantValidator.getCustomerByTenantAndIdOrThrow(customerId);
		Long tenantId = tenantService.getTenantIdFromContextHolder();

		Address address = addressRepository
				.findByTenantIdAndCustomerIdAndIdAndStatus(tenantId, customerId, addressId, AddressStatus.ACTIVE)
				.orElseThrow(() -> new NotFoundException(ApiErrorCode.ADDRESS_NOT_FOUND));

		address.setStatus(AddressStatus.DELETED);
	}



}