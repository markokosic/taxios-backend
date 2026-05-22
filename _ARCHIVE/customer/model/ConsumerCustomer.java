package com.markokosic.minicrm.modules.customer.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("CONSUMER")
@Getter
@Setter
public class ConsumerCustomer extends Customer {
	@Column(name="first_name")
	private String firstName;

	@Column(name="last_name")
	private String lastName;

	@Column(name="email")
	private String email;

	@Column(name="phone")
	private String phone;

	@Override
	protected String computeDisplayName() {
		return (firstName != null ? firstName : "")
				+ " "
				+ (lastName != null ? lastName : "");
	}

}
