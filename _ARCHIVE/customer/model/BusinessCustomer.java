package com.markokosic.minicrm.modules.customer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("BUSINESS")
@Getter
@Setter
public class BusinessCustomer extends Customer{

	@Column(name="company_name")
	private String companyName;


	@Column(name="vat")
	private String vat;

	@Email
	@Column(name="email")
	private String email;

	@Column(name="phone")
	private String phone;

	@Column(name="website")
	private String website;

	@Override
	protected String computeDisplayName() {
		return companyName;
	}

}


