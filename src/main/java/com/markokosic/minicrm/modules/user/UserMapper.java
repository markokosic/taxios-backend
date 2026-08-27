package com.markokosic.minicrm.modules.user;

import com.markokosic.minicrm.modules.user.dto.response.CreateUserResponseDTO;
import com.markokosic.minicrm.modules.user.dto.response.UserResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO userToUserResponseDTO(User user);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "roles", source = "user.roles")
    @Mapping(target = "mustChangePassword", source = "user.mustChangePassword")
    @Mapping(target = "temporaryPassword", source = "temporaryPassword")
    CreateUserResponseDTO toCreateUserResponseDTO(User user, String temporaryPassword);
}
