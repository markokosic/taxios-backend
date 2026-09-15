package com.markokosic.minicrm.modules.auth.dto.response;

import com.markokosic.minicrm.modules.role.dto.Roles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Roles role;
    private boolean mustChangePassword;
    private Long tenantId;
    private String tenantName;
}
