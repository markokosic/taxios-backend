package com.markokosic.minicrm.modules.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for changing user password")
public record ChangePasswordRequestDTO(
        @NotBlank(message = "validation.password.required")
        @Schema(description = "Current password of the logged-in user", example = "oldPassword123")
        String currentPassword,

        @NotBlank(message = "validation.password.required")
        @Size(min = 8, message = "validation.password.too_short")
        @Size(max = 100, message = "validation.size.max")
        @Schema(description = "New password", example = "newSecurePassword123")
        String newPassword
) {}
