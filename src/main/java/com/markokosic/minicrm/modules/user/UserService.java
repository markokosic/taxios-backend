package com.markokosic.minicrm.modules.user;

import com.markokosic.minicrm.common.util.TemporaryPasswordGenerator;
import com.markokosic.minicrm.exception.BadRequestException;
import com.markokosic.minicrm.exception.ResourceConflictException;
import com.markokosic.minicrm.exception.ResourceNotFoundException;
import com.markokosic.minicrm.modules.auth.dto.request.RegisterTenantRequestDTO;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import com.markokosic.minicrm.modules.role.dto.Roles;
import com.markokosic.minicrm.modules.user.dto.request.CreateUserRequestDTO;
import com.markokosic.minicrm.modules.user.dto.request.UpdateUserRequestDTO;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.modules.user.dto.response.CreateUserResponseDTO;
import com.markokosic.minicrm.modules.user.dto.response.UserResponseDTO;
import com.markokosic.minicrm.modules.user.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
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
        if (request.roles() == Roles.OWNER || request.roles() == Roles.PRE_AUTH) {
            throw new BadRequestException("domain.user.role.owner_not_allowed");
        }

        if (request.roles() == Roles.DRIVER) {
            throw new BadRequestException("domain.user.role.driver_not_allowed");
        }

        CreatedUserResult result = persistUserWithTemporaryPassword(
                request.email(),
                request.firstName(),
                request.lastName(),
                request.roles()
        );
        return userMapper.toCreateUserResponseDTO(result.user(), result.temporaryPassword());
    }

    @Transactional
    public CreateUserResponseDTO createDriverUser(Driver driver, String loginEmail) {
        CreatedUserResult result = persistUserWithTemporaryPassword(
                loginEmail,
                driver.getFirstName(),
                driver.getLastName(),
                Roles.DRIVER
        );

        driver.setUser(result.user());
        driverRepository.save(driver);

        return userMapper.toCreateUserResponseDTO(result.user(), result.temporaryPassword());
    }

    private record CreatedUserResult(User user, String temporaryPassword) {}

    private CreatedUserResult persistUserWithTemporaryPassword(
            String email,
            String firstName,
            String lastName,
            Roles role
    ) {
        if (userRepository.existsByEmail(email)) {
            throw new ResourceConflictException("domain.user.email.duplicate");
        }

        String temporaryPassword = TemporaryPasswordGenerator.generate();

        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        user.setRoles(role);
        user.setMustChangePassword(true);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        return new CreatedUserResult(savedUser, temporaryPassword);
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("domain.user.not_found"));
        if (UserStatus.DELETED.equals(user.getStatus())) {
            throw new ResourceNotFoundException("domain.user.not_found");
        }
        return convertToUserResponseDto(user);
    }

    public PageResponseDTO<UserResponseDTO> getAllUsers(Pageable pageable) {
        Page<UserResponseDTO> page = userRepository.findAllByStatus(UserStatus.ACTIVE, pageable)
                .map(this::convertToUserResponseDto);
        return PageResponseDTO.from(page);
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UpdateUserRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("domain.user.not_found"));

        if (UserStatus.DELETED.equals(user.getStatus())) {
            throw new ResourceNotFoundException("domain.user.not_found");
        }

        if (request.roles() == Roles.OWNER || request.roles() == Roles.PRE_AUTH) {
            throw new BadRequestException("domain.user.role.owner_not_allowed");
        }

        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new ResourceConflictException("domain.user.email.duplicate");
        }

        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRoles(request.roles());

        User savedUser = userRepository.save(user);
        return convertToUserResponseDto(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("domain.user.not_found"));

        if (UserStatus.DELETED.equals(user.getStatus())) {
            throw new ResourceNotFoundException("domain.user.not_found");
        }

        if (Roles.OWNER.equals(user.getRoles())) {
            throw new BadRequestException("domain.user.cannot_delete_owner");
        }

        user.setStatus(UserStatus.DELETED);

        driverRepository.findByUserId(user.getId()).ifPresent(driver -> {
            driver.setUser(null);
            driverRepository.save(driver);
        });

        userRepository.save(user);
    }

    public UserResponseDTO convertToUserResponseDto(User user) {
        return userMapper.userToUserResponseDTO(user);
    }
}
