package com.markokosic.minicrm.modules.address.model;

import com.markokosic.minicrm.modules.customer.model.Customer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="addresses")
public class Address {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tenant_id", nullable = false)
	@NotNull
	private Long tenantId;

	@NotBlank
	@Column(name="street", nullable = false)
	private String street;

	@NotBlank
	@Column(name="city", nullable = false)
	private String city;

	@NotBlank
	@Column(name="postal_code", nullable = false, length = 20)
	private String postalCode;

	@NotBlank
	@Column(name="country", nullable = false)
	private String country;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private AddressType type;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private Customer customer;

	@Enumerated(EnumType.STRING)
	private AddressStatus status = AddressStatus.ACTIVE;

}