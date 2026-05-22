package com.markokosic.minicrm.modules.customer.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBusinessCustomerRequestDTO extends CreateCustomerRequestDTO {
	@NotBlank
	private String companyName;

	@NotBlank
	private String vat;
	@Email
	private String email;

	@Pattern(
			regexp = "^(\\+?\\d{1,3})?[- .\\/]?(\\d{1,4}[- .\\/]?){1,5}\\d{1,5}$",
			message = "Ungültiges Telefonnummern-Format"
	)
	private String phone;
	private String website;
}
