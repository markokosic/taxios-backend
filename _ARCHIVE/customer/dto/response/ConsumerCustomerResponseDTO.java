package com.markokosic.minicrm.modules.customer.dto.response;

import lombok.Data;

@Data
public class ConsumerCustomerResponseDTO extends CustomerResponseDTO {
	private String displayName;
	private String firstName;
	private String lastName;
	private String email;
	private String phone;
}
