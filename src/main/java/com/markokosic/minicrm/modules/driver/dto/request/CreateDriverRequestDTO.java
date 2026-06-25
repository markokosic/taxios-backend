package com.markokosic.minicrm.modules.driver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;

@Schema(description = "Request object for creating a new driver")

public record CreateDriverRequestDTO(

		@NotBlank(message = "{validation.notBlank}")
		@Size(max = 50, message = "{driver.firstName.size}")
		@Schema(description = "The driver's first name", example = "Max")
		String firstName,

		@NotBlank(message = "{validation.notBlank}")
		@Size(max = 50, message = "{driver.lastName.size}")
		@Schema(description = "The driver's last name", example = "Mustermann")
		String lastName,

		@NotBlank(message = "{validation.notBlank}")
		@Email(message = "{validation.email.invalid}")
		@Schema(description = "Primary contact email address", example = "max.mustermann@example.com")
		String email,

		@Pattern(regexp = "^\\+?[0-9\\s\\-]{7,20}$", message = "{driver.phone.invalid}")
		@Schema(description = "International phone number format", example = "+43 660 1234567")
		String phone,

		@NotNull(message = "{driver.remunerationModel.notNull}")
		@Schema(description = "The internal remuneration models assigned to the driver")
		List<CreateRemunerationRequestDTO> remunerationConfigs
) {}