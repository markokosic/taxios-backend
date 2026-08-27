package com.markokosic.minicrm.modules.user.dto.response;

import com.markokosic.minicrm.modules.role.dto.Roles;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response payload after creating a new user, containing the generated temporary password")
public record CreateUserResponseDTO(
        @Schema(description = "Unique identifier of the user", example = "1")
        Long id,

        @Schema(description = "User's first name", example = "Max")
        String firstName,

        @Schema(description = "User's last name", example = "Mustermann")
        String lastName,

        @Schema(description = "User's email address", example = "driver@example.com")
        String email,

        @Schema(description = "User role", example = "DRIVER")
        Roles roles,

        @Schema(description = "Whether the user must change their password on next login", example = "true")
        boolean mustChangePassword,

        @Schema(description = "One-time temporary password generated for the user", example = "aB3!k9Zq#m")
        String temporaryPassword
) {}
