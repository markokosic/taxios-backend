package com.markokosic.minicrm.modules.driver.dto.response;


import com.markokosic.minicrm.modules.driver.model.DriverStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Response object representing a driver in the system")
public record DriverResponseDTO(

		@Schema(description = "Unique identifier of the driver", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
		Long id,

		@Schema(description = "First name of the driver", example = "Max", requiredMode = Schema.RequiredMode.REQUIRED)
		String firstName,

		@Schema(description = "Last name of the driver", example = "Mustermann", requiredMode = Schema.RequiredMode.REQUIRED)
		String lastName,

		@Schema(description = "Email address", example = "max.mustermann@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
		String email,

		@Schema(description = "Phone number", example = "+43 660 1234567", requiredMode = Schema.RequiredMode.REQUIRED)
		String phone,

		@Schema(description = "Current employment status", requiredMode = Schema.RequiredMode.REQUIRED)
		DriverStatus status,

		@Schema(description = "Current remuneration configuration", requiredMode = Schema.RequiredMode.REQUIRED)
		List<RemunerationConfigResponseDTO> currentRemunerationConfigs,

		@Schema(description = "Timestamp when the driver was created", requiredMode = Schema.RequiredMode.REQUIRED)
		LocalDateTime createdAt,

		@Schema(description = "Timestamp of the last update", requiredMode = Schema.RequiredMode.REQUIRED)
		LocalDateTime updatedAt
) {}