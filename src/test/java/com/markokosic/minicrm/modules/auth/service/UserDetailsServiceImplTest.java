package com.markokosic.minicrm.modules.auth.service;

import com.markokosic.minicrm.modules.auth.model.UserPrincipal;
import com.markokosic.minicrm.modules.user.User;
import com.markokosic.minicrm.modules.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(10L);
        user.setEmail("admin@example.com");
        user.setPassword("hashedpassword");
        user.setTenantId(1L);
    }

    @Test
    void loadUserByUsername_UserFound_Success() {
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@example.com");

        assertNotNull(userDetails);
        assertTrue(userDetails instanceof UserPrincipal);
        UserPrincipal principal = (UserPrincipal) userDetails;

        assertEquals("admin@example.com", principal.getUsername());
        assertEquals("admin@example.com", principal.getEmail());
        assertEquals("hashedpassword", principal.getPassword());
        assertEquals(10L, principal.getId());
        assertEquals(1L, principal.getTenantId());
        assertFalse(principal.getAuthorities().isEmpty());
        assertTrue(principal.isAccountNonExpired());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.isCredentialsNonExpired());
        assertTrue(principal.isEnabled());
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("notfound@example.com")
        );
    }

    @Test
    void loadUserByUsername_UserDeleted_PrincipalIsDisabled() {
        user.setStatus(com.markokosic.minicrm.modules.user.model.UserStatus.DELETED);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@example.com");

        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
    }
}
