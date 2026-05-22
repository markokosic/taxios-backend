package com.markokosic.minicrm.modules.customer.dto.response;

import lombok.Data;

@Data
public class BusinessCustomerResponseDTO extends CustomerResponseDTO {
	private String displayName;
	private String companyName;
	private String vat;
	private String email;
	private String phone;
	private String website;
}
