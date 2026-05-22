package com.markokosic.minicrm.modules.customer;

import com.markokosic.minicrm.modules.customer.model.Customer;
import com.markokosic.minicrm.modules.customer.model.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	Page<Customer> findAllByTenantIdAndStatus(Long tenantId, CustomerStatus status, Pageable pageable);
	Optional<Customer> findByIdAndTenantId(Long id, Long tenantId);
}
