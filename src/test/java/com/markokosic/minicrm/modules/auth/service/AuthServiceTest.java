package com.markokosic.minicrm.modules.auth.service;

import com.markokosic.minicrm.exception.BadRequestException;
import com.markokosic.minicrm.exception.ResourceConflictException;
import com.markokosic.minicrm.exception.UnauthorizedException;
import com.markokosic.minicrm.modules.auth.config.TokenProperties;
import com.markokosic.minicrm.modules.auth.dto.request.ChangePasswordRequestDTO;
import com.markokosic.minicrm.modules.auth.dto.request.LoginRequestDTO;
import com.markokosic.minicrm.modules.auth.dto.request.RegisterTenantRequestDTO;
import com.markokosic.minicrm.modules.auth.dto.response.MeResponseDTO;
import com.markokosic.minicrm.modules.auth.dto.response.RegisterTenantResponseDTO;
import com.markokosic.minicrm.modules.auth.model.UserPrincipal;
import com.markokosic.minicrm.modules.role.dto.Roles;
import com.markokosic.minicrm.modules.tenant.Tenant;
import com.markokosic.minicrm.modules.tenant.TenantRepository;
import com.markokosic.minicrm.modules.user.User;
import com.markokosic.minicrm.modules.user.UserRepository;
import com.markokosic.minicrm.modules.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

	@Mock
	private TenantRepository tenantRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private UserService userService;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JWTService jwtService;
	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private TokenProperties tokenProperties;

	@InjectMocks
	private AuthService authService;

	@Test
	void testCreateTenant_withUniqueName_shouldSaveToRepo() {
		// ARRANGE
		String tenantName = "testTenant";

		Mockito.when(tenantRepository.existsByName(tenantName)).thenReturn(false);
		Mockito.when(tenantRepository.save(Mockito.any(Tenant.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		// ACT
		Tenant savedTenant = authService.createTenant(tenantName);

		// ASSERT
		assertNotNull(savedTenant);
		assertEquals(tenantName, savedTenant.getName());
		Mockito.verify(tenantRepository, Mockito.times(1)).existsByName(tenantName);
		Mockito.verify(tenantRepository, Mockito.times(1)).save(Mockito.any(Tenant.class));
	}

	@Test
	void testCreateTenant_withExistingName_shouldThrowValidationException() {
		String tenantName = "testTenant";

		Mockito.when(tenantRepository.existsByName(tenantName)).thenReturn(true);

		assertThrows(ResourceConflictException.class, () -> {
			authService.createTenant(tenantName);
		});
	}

	@Test
	void testRegisterNewTenant_shouldCreateTenantAndOwner() {
		Tenant tenant = new Tenant();
		tenant.setId(100L);
		tenant.setName("TEST TENANT");
		tenant.setCreatedAt(LocalDateTime.now());
		tenant.setUpdatedAt(LocalDateTime.now());

		RegisterTenantRequestDTO dto = new RegisterTenantRequestDTO();
		dto.setTenantName(tenant.getName());
		dto.setPassword("password123");
		dto.setFirstName("Max");
		dto.setLastName("Mustermann");
		dto.setEmail("test@test.com");

		Mockito.when(tenantRepository.existsByName(dto.getTenantName())).thenReturn(false);
		Mockito.when(tenantRepository.save(Mockito.any(Tenant.class))).thenReturn(tenant);

		RegisterTenantResponseDTO response = authService.registerNewTenant(dto);

		assertNotNull(response);
		assertEquals(100L, response.getTenantId());
		assertEquals("TEST TENANT", response.getTenantName());

		Mockito.verify(userService, Mockito.times(1)).createTenantOwner(Mockito.eq(dto), Mockito.eq(100L));
	}

	@Test
	void testGetMe_Success() {
		User user = new User();
		user.setId(1L);
		user.setTenantId(100L);
		user.setEmail("test@test.com");
		user.setFirstName("Max");
		user.setLastName("Mustermann");
		user.setRoles(Roles.OWNER);

		UserPrincipal principal = new UserPrincipal(user);
		Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);

		Tenant tenant = new Tenant();
		tenant.setId(100L);
		tenant.setName("TEST TENANT");

		Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
		Mockito.when(tenantRepository.findById(100L)).thenReturn(Optional.of(tenant));

		MeResponseDTO result = authService.getMe();

		assertNotNull(result);
		assertEquals("test@test.com", result.getEmail());
		assertEquals("TEST TENANT", result.getTenantName());
		assertEquals(100L, result.getTenantId());

		SecurityContextHolder.clearContext();
	}

	@Test
	void testChangePassword_Success() {
		User user = new User();
		user.setId(1L);
		user.setEmail("test@test.com");
		user.setPassword("hashedOldPassword");
		user.setTenantId(100L);
		user.setRoles(Roles.ADMIN);
		user.setMustChangePassword(true);

		UserPrincipal principal = new UserPrincipal(user);
		Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);

		TokenProperties.Token access = new TokenProperties.Token();
		access.setExpirationMinutes(30L);
		TokenProperties.Token refresh = new TokenProperties.Token();
		refresh.setExpirationMinutes(10080L);
		Mockito.when(tokenProperties.getAccess()).thenReturn(access);
		Mockito.when(tokenProperties.getRefresh()).thenReturn(refresh);

		ChangePasswordRequestDTO request = new ChangePasswordRequestDTO("oldPass123", "newPass456");

		Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
		Mockito.when(passwordEncoder.matches("oldPass123", "hashedOldPassword")).thenReturn(true);
		Mockito.when(passwordEncoder.matches("newPass456", "hashedOldPassword")).thenReturn(false);
		Mockito.when(passwordEncoder.encode("newPass456")).thenReturn("hashedNewPassword");
		Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArgument(0));
		Mockito.when(jwtService.generateToken(Mockito.eq(1L), Mockito.eq("test@test.com"), Mockito.eq(100L), Mockito.eq(Roles.ADMIN), Mockito.eq(30L)))
				.thenReturn("full-access-token");
		Mockito.when(jwtService.generateToken(Mockito.eq(1L), Mockito.eq("test@test.com"), Mockito.eq(100L), Mockito.eq(Roles.ADMIN), Mockito.eq(10080L)))
				.thenReturn("full-refresh-token");

		var response = authService.changePassword(request);

		assertNotNull(response);
		assertEquals("full-access-token", response.getAccessToken());
		assertEquals("full-refresh-token", response.getRefreshToken());
		assertEquals("hashedNewPassword", user.getPassword());
		assertFalse(user.isMustChangePassword());
		Mockito.verify(userRepository).save(user);

		SecurityContextHolder.clearContext();
	}

	@Test
	void testChangePassword_InvalidCurrentPassword() {
		User user = new User();
		user.setEmail("test@test.com");
		user.setPassword("hashedOldPassword");

		UserPrincipal principal = new UserPrincipal(user);
		Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);

		ChangePasswordRequestDTO request = new ChangePasswordRequestDTO("wrongPass", "newPass456");

		Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
		Mockito.when(passwordEncoder.matches("wrongPass", "hashedOldPassword")).thenReturn(false);

		assertThrows(BadRequestException.class, () -> authService.changePassword(request));
		Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());

		SecurityContextHolder.clearContext();
	}

	@Test
	void testChangePassword_SameAsOldPassword() {
		User user = new User();
		user.setEmail("test@test.com");
		user.setPassword("hashedOldPassword");

		UserPrincipal principal = new UserPrincipal(user);
		Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);

		ChangePasswordRequestDTO request = new ChangePasswordRequestDTO("oldPass123", "oldPass123");

		Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
		Mockito.when(passwordEncoder.matches("oldPass123", "hashedOldPassword")).thenReturn(true);

		assertThrows(BadRequestException.class, () -> authService.changePassword(request));
		Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());

		SecurityContextHolder.clearContext();
	}

	@Test
	void testChangePassword_Unauthenticated() {
		SecurityContextHolder.clearContext();
		ChangePasswordRequestDTO request = new ChangePasswordRequestDTO("oldPass123", "newPass456");

		assertThrows(UnauthorizedException.class, () -> authService.changePassword(request));
	}

	@Test
	void testLogin_whenMustChangePasswordIsTrue_shouldIssuePreAuthToken() {
		User user = new User();
		user.setId(1L);
		user.setEmail("user@test.com");
		user.setTenantId(10L);
		user.setRoles(Roles.DRIVER);
		user.setMustChangePassword(true);

		LoginRequestDTO loginRequest = new LoginRequestDTO();
		loginRequest.setEmail("user@test.com");
		loginRequest.setPassword("tempPass123");

		Mockito.when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
		Mockito.when(jwtService.generateToken(1L, "user@test.com", 10L, Roles.PRE_AUTH, 15L))
				.thenReturn("pre-auth-token");

		var response = authService.login(loginRequest);

		assertNotNull(response);
		assertEquals("pre-auth-token", response.getAccessToken());
		assertEquals("", response.getRefreshToken());
		assertTrue(response.getUser().isMustChangePassword());
	}
}
