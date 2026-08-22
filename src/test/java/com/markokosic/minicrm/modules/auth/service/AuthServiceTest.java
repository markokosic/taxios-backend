package com.markokosic.minicrm.modules.auth.service;

import com.markokosic.minicrm.modules.auth.config.TokenProperties;
import com.markokosic.minicrm.modules.auth.dto.request.RegisterTenantRequestDTO;
import com.markokosic.minicrm.modules.tenant.Tenant;
import com.markokosic.minicrm.modules.user.User;
import com.markokosic.minicrm.modules.tenant.TenantRepository;
import com.markokosic.minicrm.modules.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.markokosic.minicrm.exception.ResourceConflictException;

import com.markokosic.minicrm.modules.auth.dto.response.MeResponseDTO;
import com.markokosic.minicrm.modules.auth.model.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
	private PasswordEncoder passwordEncoder;
	@Mock
	private JWTService jwtService;
	@Mock
	private AuthenticationManager authenticationManager;
	private TokenProperties tokenProperties;
	private ResourceConflictException resourceConflictException;

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
	void testCreateTenant_withExistingName_shouldThrowValidationException(){
		String tenantName = "testTenant";

		Mockito.when(tenantRepository.existsByName(tenantName)).thenReturn(true);

		ResourceConflictException exception = assertThrows(ResourceConflictException.class, () -> {
			authService.createTenant(tenantName);
		});
	}

	@Test
	void testCreateUser_withUniqueEmail_shouldSaveToRepo() {

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

		Mockito.when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
		authService.createUser(dto, tenant);

		Mockito.verify(userRepository, Mockito.times(1)).existsByEmail(dto.getEmail());
		Mockito.verify(userRepository, Mockito.times(1)).insertUser(
				Mockito.eq("test@test.com"),
				Mockito.eq("Max"),
				Mockito.eq("Mustermann"),
				Mockito.any(),
				Mockito.eq(100L)
		);


	}

	@Test
	void testCreateUser_withExistingEmail_shouldThrowValidationException() {
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

		Mockito.when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

		ResourceConflictException exception = assertThrows(ResourceConflictException.class, () -> {
			authService.createUser(dto, tenant);
		});
	}

	@Test
	void testGetMe_Success() {
		User user = new User();
		user.setId(1L);
		user.setTenantId(100L);
		user.setEmail("test@test.com");
		user.setFirstName("Max");
		user.setLastName("Mustermann");

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

}
