package com.markokosic.minicrm.modules.auth.dto.response;

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
    private Long tenantId;
    private String tenantName;
}
