package com.markokosic.minicrm.modules.user.dto.response;

import com.markokosic.minicrm.modules.role.dto.Roles;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Roles roles;
    private boolean mustChangePassword;

    public UserResponseDTO(Long id, String firstName, String lastName, String email) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roles = Roles.ADMIN;
        this.mustChangePassword = false;
    }
}
