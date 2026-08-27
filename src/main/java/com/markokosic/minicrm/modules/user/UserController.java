package com.markokosic.minicrm.modules.user;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.ApiResponseDTO;
import com.markokosic.minicrm.modules.user.dto.request.CreateUserRequestDTO;
import com.markokosic.minicrm.modules.user.dto.response.CreateUserResponseDTO;
import com.markokosic.minicrm.modules.user.dto.response.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for managing system users/administrators")
public class UserController {

    private final UserService userService;
    private final I18nService i18n;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create user", description = "Creates a new system user for the current tenant with a generated temporary password. Defaults to mustChangePassword=true.")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request payload", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or OWNER role", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PreAuthorize("hasAnyRole(T(com.markokosic.minicrm.modules.role.dto.Roles).ADMIN.name(), T(com.markokosic.minicrm.modules.role.dto.Roles).OWNER.name())")
    public ResponseEntity<ApiResponseDTO<CreateUserResponseDTO>> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        CreateUserResponseDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDTO<>(true, user, i18n.getMessage("success.added")));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves profile details of a specific system user.")
    @ApiResponse(responseCode = "200", description = "User details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ApiResponseDTO<UserResponseDTO>> getUser(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponseDTO<>(true, user, i18n.getMessage("success.fetched")));
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all system users/administrators for the current tenant.")
    @ApiResponse(responseCode = "200", description = "Users list retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ApiResponseDTO<List<UserResponseDTO>>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponseDTO<>(true, users, i18n.getMessage("success.fetched")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a system user by their ID.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or OWNER role", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PreAuthorize("hasAnyRole(T(com.markokosic.minicrm.modules.role.dto.Roles).ADMIN.name(), T(com.markokosic.minicrm.modules.role.dto.Roles).OWNER.name())")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
