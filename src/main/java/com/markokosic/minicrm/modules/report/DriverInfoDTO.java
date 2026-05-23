package com.markokosic.minicrm.modules.report;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Driver information in a report entry")
public record DriverInfoDTO(
        @Schema(description = "Unique identifier of the driver", example = "1")
        Long id,

        @Schema(description = "First name of the driver", example = "Max")
        String firstName,

        @Schema(description = "Last name of the driver", example = "Mustermann")
        String lastName
) {
}
