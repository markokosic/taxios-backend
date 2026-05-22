package com.markokosic.minicrm.modules.address.dto.request;

import com.markokosic.minicrm.modules.address.model.AddressType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateAddressRequestDTO {

	@NotBlank(message = "Street is mandatory")
	@Size(max = 255)
	private String street;

	@NotBlank(message = "City is mandatory")
	private String city;

	@NotBlank(message = "Postal code is mandatory")
	@Pattern(regexp = "^[0-9]{4,5}$", message = "Invalid postal code format")
	private String postalCode;

	@NotBlank(message = "Country is mandatory")
//	@Size(min = 2, max = 30, message = "Country must be valid")
	private String country;

	@Enumerated(EnumType.STRING)
	private AddressType type;
}