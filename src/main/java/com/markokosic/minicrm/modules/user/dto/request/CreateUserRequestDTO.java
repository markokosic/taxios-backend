package com.markokosic.minicrm.modules.user.dto.request;

import com.markokosic.minicrm.modules.role.dto.Roles;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for creating a new user")
public record CreateUserRequestDTO(
        @NotBlank
        @Email
        @Schema(description = "User's email address", example = "driver@example.com")
        String email,

        @NotBlank
        @Schema(description = "User's first name", example = "Max")
        String firstName,

        @NotBlank
        @Schema(description = "User's last name", example = "Mustermann")
        String lastName,

        @Schema(description = "User role", example = "DRIVER")
        Roles roles
) {}
