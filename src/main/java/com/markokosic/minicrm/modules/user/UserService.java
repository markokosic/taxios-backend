package com.markokosic.minicrm.modules.user;

import com.markokosic.minicrm.common.util.TemporaryPasswordGenerator;
import com.markokosic.minicrm.exception.BadRequestException;
import com.markokosic.minicrm.exception.ResourceConflictException;
import com.markokosic.minicrm.exception.ResourceNotFoundException;
import com.markokosic.minicrm.modules.auth.dto.request.RegisterTenantRequestDTO;
import com.markokosic.minicrm.modules.role.dto.Roles;
import com.markokosic.minicrm.modules.user.dto.request.CreateUserRequestDTO;
import com.markokosic.minicrm.modules.user.dto.response.CreateUserResponseDTO;
import com.markokosic.minicrm.modules.user.dto.response.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createTenantOwner(RegisterTenantRequestDTO request, Long tenantId) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("domain.user.email.duplicate");
        }

        userRepository.insertUser(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                passwordEncoder.encode(request.getPassword()),
                tenantId,
                Roles.OWNER.name(),
                false
        );
    }

    @Transactional
    public CreateUserResponseDTO createUser(CreateUserRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceConflictException("domain.user.email.duplicate");
        }

        if (request.roles() == Roles.OWNER || request.roles() == Roles.PRE_AUTH) {
            throw new BadRequestException("domain.user.role.owner_not_allowed");
        }

        String temporaryPassword = TemporaryPasswordGenerator.generate();

        User user = new User();
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setRoles(request.roles() != null ? request.roles() : Roles.ADMIN);
        user.setMustChangePassword(true);

        User savedUser = userRepository.save(user);
        return userMapper.toCreateUserResponseDTO(savedUser, temporaryPassword);
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("domain.user.not_found"));
        return convertToUserResponseDto(user);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("domain.user.not_found"));
        userRepository.delete(user);
    }

    public UserResponseDTO convertToUserResponseDto(User user) {
        return userMapper.userToUserResponseDTO(user);
    }
}
