package com.markokosic.minicrm.modules.driver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request object for updating a driver")
public record UpdateDriverRequestDTO(

		@Size(max = 50, message = "{driver.firstName.size}")
		@Schema(description = "The driver's first name (optional)", example = "Max", nullable = true)
		String firstName,

		@Size(max = 50, message = "{driver.lastName.size}")
		@Schema(description = "The driver's last name (optional)", example = "Mustermann", nullable = true)
		String lastName,

		@Email(message = "{validation.email.invalid}")
		@Schema(description = "Primary contact email address (optional)", example = "max.mustermann@example.com", nullable = true)
		String email,

		@Pattern(regexp = "^\\+?[0-9\\s\\-]{7,20}$", message = "{driver.phone.invalid}")
		@Schema(description = "International phone number format (optional)", example = "+43 660 1234567", nullable = true)
		String phone,


		CreateRemunerationRequestDTO remunerationConfig
) {}