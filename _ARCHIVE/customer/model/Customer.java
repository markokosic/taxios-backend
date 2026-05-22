package com.markokosic.minicrm.modules.customer.model;


import com.markokosic.minicrm.modules.address.model.Address;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="customers")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;

	@Column(name = "tenant_id", nullable = false)
	@NotNull
	private Long tenantId;

	@Column(name="display_name", nullable = false)
	@NotNull
	private String displayName;

	@Column(name="status", nullable = false)
	@Enumerated(EnumType.STRING)
	private CustomerStatus status;

	@Column(name="type", insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	private CustomerType type;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
		this.status = CustomerStatus.ACTIVE;
		this.displayName = computeDisplayName();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
		this.displayName = computeDisplayName();
	}

	@OneToMany(mappedBy = "customer", orphanRemoval = true)
	private List<Address> addresses = new ArrayList<>();

	protected String computeDisplayName(
	) {
		//TODO REFACTOR CUSTOMER TO ABSTRACT CLASS
		throw new UnsupportedOperationException("Not implemented");
	}

}
