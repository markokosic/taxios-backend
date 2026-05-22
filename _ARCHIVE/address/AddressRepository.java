package com.markokosic.minicrm.modules.address;

import com.markokosic.minicrm.modules.address.model.Address;
import com.markokosic.minicrm.modules.address.model.AddressStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
	List<Address> findAllByTenantIdAndCustomerIdAndStatus(Long tenantId, Long customerId, AddressStatus status);
	Optional<Address> findByTenantIdAndCustomerIdAndIdAndStatus(Long tenantId, Long customerId, Long addressId, AddressStatus status);

}
