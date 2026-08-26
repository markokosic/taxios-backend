package com.markokosic.minicrm.modules.user;

import com.markokosic.minicrm.exception.BadRequestException;
import com.markokosic.minicrm.exception.ResourceConflictException;
import com.markokosic.minicrm.exception.ResourceNotFoundException;
import com.markokosic.minicrm.modules.auth.dto.request.RegisterTenantRequestDTO;
import com.markokosic.minicrm.modules.role.dto.Roles;
import com.markokosic.minicrm.modules.user.dto.request.CreateUserRequestDTO;
import com.markokosic.minicrm.modules.user.dto.response.UserResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
                "driver@test.com", "John", "Doe", "secret123", Roles.DRIVER
        );

        when(userRepository.existsByEmail("driver@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(10L);
        savedUser.setEmail("driver@test.com");
        savedUser.setRoles(Roles.DRIVER);
        savedUser.setMustChangePassword(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDTO responseDTO = new UserResponseDTO(10L, "John", "Doe", "driver@test.com", Roles.DRIVER, true);
        when(userMapper.userToUserResponseDTO(savedUser)).thenReturn(responseDTO);

        UserResponseDTO result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertTrue(result.isMustChangePassword());
        assertEquals(Roles.DRIVER, result.getRoles());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_whenOwnerRolePassed_shouldThrowBadRequest() {
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "driver@test.com", "John", "Doe", "secret123", Roles.OWNER
        );

        when(userRepository.existsByEmail("driver@test.com")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_whenEmailExists_shouldThrowConflict() {
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "driver@test.com", "John", "Doe", "secret123", Roles.DRIVER
        );

        when(userRepository.existsByEmail("driver@test.com")).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
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
    void testGetAllUsers_shouldReturnAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        UserResponseDTO response1 = new UserResponseDTO(1L, "Max", "Mustermann", "max@email.com");
        UserResponseDTO response2 = new UserResponseDTO(2L, "Erika", "Musterfrau", "erika@email.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.userToUserResponseDTO(user1)).thenReturn(response1);
        when(userMapper.userToUserResponseDTO(user2)).thenReturn(response2);

        // Act
        List<UserResponseDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("max@email.com", result.get(0).getEmail());
        assertEquals("erika@email.com", result.get(1).getEmail());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testDeleteUser_whenUserExists_shouldDeleteUser() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user);
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
        verify(userRepository, never()).delete(any(User.class));
    }
}
