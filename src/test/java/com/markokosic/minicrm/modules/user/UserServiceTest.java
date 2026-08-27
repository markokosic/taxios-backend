package com.markokosic.minicrm.modules.user;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateTenantOwner_whenEmailIsUnique_shouldInsertOwner() {
        RegisterTenantRequestDTO request = new RegisterTenantRequestDTO();
        request.setEmail("owner@test.com");
        request.setFirstName("OwnerFirst");
        request.setLastName("OwnerLast");
        request.setPassword("ownerPass123");

        when(userRepository.existsByEmail("owner@test.com")).thenReturn(false);
        when(passwordEncoder.encode("ownerPass123")).thenReturn("encodedOwnerPass");

        userService.createTenantOwner(request, 100L);

        verify(userRepository, times(1)).insertUser(
                "owner@test.com", "OwnerFirst", "OwnerLast", "encodedOwnerPass", 100L, "OWNER", false
        );
    }

    @Test
    void testCreateTenantOwner_whenEmailExists_shouldThrowConflict() {
        RegisterTenantRequestDTO request = new RegisterTenantRequestDTO();
        request.setEmail("owner@test.com");

        when(userRepository.existsByEmail("owner@test.com")).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> userService.createTenantOwner(request, 100L));
        verify(userRepository, never()).insertUser(any(), any(), any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void testCreateUser_whenEmailIsUnique_shouldSaveAndReturnUser() {
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "admin@test.com", "John", "Doe", Roles.ADMIN
        );

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setEmail("admin@test.com");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setRoles(Roles.ADMIN);
        savedUser.setMustChangePassword(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        CreateUserResponseDTO responseDTO = new CreateUserResponseDTO(10L, "John", "Doe", "admin@test.com", Roles.ADMIN, true, "tempPass123");
        when(userMapper.toCreateUserResponseDTO(eq(savedUser), anyString())).thenReturn(responseDTO);

        CreateUserResponseDTO result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(10L, result.id());
        assertTrue(result.mustChangePassword());
        assertEquals(Roles.ADMIN, result.roles());
        assertEquals("tempPass123", result.temporaryPassword());

        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_whenDriverRolePassed_shouldThrowBadRequest() {
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "driver@test.com", "John", "Doe", Roles.DRIVER
        );

        assertThrows(BadRequestException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_whenOwnerRolePassed_shouldThrowBadRequest() {
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "driver@test.com", "John", "Doe", Roles.OWNER
        );

        assertThrows(BadRequestException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_whenEmailExists_shouldThrowConflict() {
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "admin@test.com", "John", "Doe", Roles.ADMIN
        );

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateDriverUser_Success() {
        Driver driver = new Driver();
        driver.setId(15L);
        driver.setFirstName("Max");
        driver.setLastName("Mustermann");
        driver.setEmail("driver@taxi.com");

        when(userRepository.existsByEmail("driver@taxi.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(20L);
        savedUser.setEmail("driver@taxi.com");
        savedUser.setRoles(Roles.DRIVER);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        CreateUserResponseDTO responseDTO = new CreateUserResponseDTO(20L, "Max", "Mustermann", "driver@taxi.com", Roles.DRIVER, true, "tempPass123");
        when(userMapper.toCreateUserResponseDTO(eq(savedUser), anyString())).thenReturn(responseDTO);

        CreateUserResponseDTO result = userService.createDriverUser(driver, "driver@taxi.com");

        assertNotNull(result);
        assertEquals(Roles.DRIVER, result.roles());
        assertEquals("driver@taxi.com", result.email());
        assertEquals(savedUser, driver.getUser());
        verify(driverRepository, times(1)).save(driver);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testGetUserById_whenUserExists_shouldReturnUserResponse() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("test@email.com");

        UserResponseDTO expectedResponse = new UserResponseDTO(userId, "Max", "Mustermann", "test@email.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.userToUserResponseDTO(user)).thenReturn(expectedResponse);

        // Act
        UserResponseDTO result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("test@email.com", result.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).userToUserResponseDTO(user);
    }

    @Test
    void testGetUserById_whenUserDoesNotExist_shouldThrowResourceNotFoundException() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.getUserById(userId)
        );
        assertEquals("domain.user.not_found", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).userToUserResponseDTO(any(User.class));
    }

    @Test
    void testGetAllUsers_shouldReturnPagedUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        UserResponseDTO response1 = new UserResponseDTO(1L, "Max", "Mustermann", "max@email.com");
        UserResponseDTO response2 = new UserResponseDTO(2L, "Erika", "Musterfrau", "erika@email.com");

        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);

        when(userRepository.findAllByStatus(UserStatus.ACTIVE, pageable)).thenReturn(userPage);
        when(userMapper.userToUserResponseDTO(user1)).thenReturn(response1);
        when(userMapper.userToUserResponseDTO(user2)).thenReturn(response2);

        // Act
        PageResponseDTO<UserResponseDTO> result = userService.getAllUsers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2L, result.getTotalElements());
        assertEquals("max@email.com", result.getContent().get(0).getEmail());
        assertEquals("erika@email.com", result.getContent().get(1).getEmail());
        verify(userRepository, times(1)).findAllByStatus(UserStatus.ACTIVE, pageable);
    }

    @Test
    void testUpdateUser_Success() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("old@email.com");
        user.setRoles(Roles.DRIVER);
        user.setStatus(UserStatus.ACTIVE);

        UpdateUserRequestDTO request = new UpdateUserRequestDTO("new@email.com", "John", "Doe", Roles.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO expected = new UserResponseDTO(userId, "John", "Doe", "new@email.com");
        when(userMapper.userToUserResponseDTO(user)).thenReturn(expected);

        UserResponseDTO result = userService.updateUser(userId, request);

        assertNotNull(result);
        assertEquals("new@email.com", user.getEmail());
        assertEquals(Roles.ADMIN, user.getRoles());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUser_ThrowsConflict_WhenEmailAlreadyExists() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("old@email.com");
        user.setStatus(UserStatus.ACTIVE);

        UpdateUserRequestDTO request = new UpdateUserRequestDTO("taken@email.com", "John", "Doe", Roles.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@email.com")).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    void testUpdateUser_ThrowsBadRequest_WhenOwnerRole() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("user@email.com");
        user.setStatus(UserStatus.ACTIVE);

        UpdateUserRequestDTO request = new UpdateUserRequestDTO("user@email.com", "John", "Doe", Roles.OWNER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> userService.updateUser(userId, request));
    }

    @Test
    void testDeleteUser_whenUserExists_shouldSoftDeleteAndUnlinkDriver() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setRoles(Roles.DRIVER);
        user.setStatus(UserStatus.ACTIVE);

        Driver driver = new Driver();
        driver.setId(10L);
        driver.setUser(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(driverRepository.findByUserId(userId)).thenReturn(Optional.of(driver));

        // Act
        userService.deleteUser(userId);

        // Assert
        assertEquals(UserStatus.DELETED, user.getStatus());
        assertNull(driver.getUser());
        verify(driverRepository, times(1)).save(driver);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeleteUser_whenOwner_shouldThrowBadRequest() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setRoles(Roles.OWNER);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> userService.deleteUser(userId));
    }

    @Test
    void testDeleteUser_whenUserDoesNotExist_shouldThrowResourceNotFoundException() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.deleteUser(userId)
        );
        assertEquals("domain.user.not_found", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }
}
