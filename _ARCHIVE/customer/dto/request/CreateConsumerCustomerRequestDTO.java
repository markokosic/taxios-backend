package com.markokosic.minicrm.modules.customer.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateConsumerCustomerRequestDTO extends CreateCustomerRequestDTO {
	@NotBlank
	private String firstName;
	@NotBlank
	private String lastName;
	@Email
	private String email;

	@Pattern(
			regexp = "^(\\+?\\d{1,3})?[- .\\/]?(\\d{1,4}[- .\\/]?){1,5}\\d{1,5}$",
			message = "Ungültiges Telefonnummern-Format"
	)
	private String phone;
}
