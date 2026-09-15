package com.markokosic.minicrm.modules.driver.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

@Schema(description = "Request payload for creating a driver user account")
public record CreateDriverUserRequestDTO(
        @Email
        @Schema(description = "Optional login email. If omitted, the driver's contact email is used.", example = "driver.login@taxi.com")
        String email
) {}
