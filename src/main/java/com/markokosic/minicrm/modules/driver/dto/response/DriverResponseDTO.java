package com.markokosic.minicrm.modules.driver.dto.response;


import com.markokosic.minicrm.modules.driver.model.DriverStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Response object representing a driver in the system")
public record DriverResponseDTO(

		@Schema(description = "Unique identifier of the driver", example = "1")
		Long id,

		@Schema(description = "First name of the driver", example = "Max")
		String firstName,

		@Schema(description = "Last name of the driver", example = "Mustermann")
		String lastName,

		@Schema(description = "Email address", example = "max.mustermann@example.com")
		String email,

		@Schema(description = "Phone number", example = "+43 660 1234567")
		String phone,

		@Schema(description = "Current employment status")
		DriverStatus status,

		@Schema(description = "Current remuneration configuration")
		RemunerationConfigResponseDTO currentRemunerationConfig,

		@Schema(description = "Timestamp when the driver was created")
		LocalDateTime createdAt,

		@Schema(description = "Timestamp of the last update")
		LocalDateTime updatedAt
) {}