package com.markokosic.minicrm.modules.auth.service;

import com.markokosic.minicrm.modules.user.User;
import com.markokosic.minicrm.modules.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private JWTService jwtService;

    // 256 bit base64 key
    private final String secretKey = "AQEAQEAQEAQEAQEAQEAQEAQEAQEAQEAQEAQEAQEAQEA=";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
    }

    @Test
    void generateToken_AndExtractClaims_Success() {
        String email = "test@example.com";
        Long tenantId = 5L;
        String token = jwtService.generateToken(email, tenantId, 10L);

        assertNotNull(token);
        assertEquals(email, jwtService.extractEmail(token));
        assertEquals(tenantId, jwtService.extractTenantId(token));
        assertFalse(jwtService.isTokenExpired(token));
        assertFalse(jwtService.isTokenSigned(token));
    }

    @Test
    void validateToken_Valid_ReturnsTrue() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email, 1L, 10L);

        when(userDetails.getUsername()).thenReturn(email);

        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_InvalidUser_ReturnsFalse() {
        String token = jwtService.generateToken("test@example.com", 1L, 10L);

        when(userDetails.getUsername()).thenReturn("other@example.com");

        assertFalse(jwtService.validateToken(token, userDetails));
    }

    @Test
    void validateRefreshToken_UserExists_ReturnsTrue() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email, 1L, 10L);

        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertTrue(jwtService.validateRefreshToken(token));
    }

    @Test
    void validateRefreshToken_UserNotFound_ReturnsFalse() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email, 1L, 10L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertFalse(jwtService.validateRefreshToken(token));
    }

    @Test
    void isTokenSigned_InvalidToken_ReturnsTrue() {
        assertTrue(jwtService.isTokenSigned("invalid.token.structure"));
    }
}
