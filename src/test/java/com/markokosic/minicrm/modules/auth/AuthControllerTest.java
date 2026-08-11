package com.markokosic.minicrm.modules.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markokosic.minicrm.exception.ForbiddenException;
import com.markokosic.minicrm.modules.auth.config.TokenProperties;
import com.markokosic.minicrm.modules.auth.dto.request.LoginRequestDTO;
import com.markokosic.minicrm.modules.auth.dto.request.RegisterTenantRequestDTO;
import com.markokosic.minicrm.modules.auth.dto.response.AuthResponseDTO;
import com.markokosic.minicrm.modules.auth.dto.response.RegisterTenantResponseDTO;
import com.markokosic.minicrm.modules.auth.service.AuthService;
import com.markokosic.minicrm.modules.user.dto.response.UserResponseDTO;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private TokenProperties tokenProperties;

    @BeforeEach
    void setUp() {
        TokenProperties.Token access = new TokenProperties.Token();
        access.setExpirationMinutes(30L);
        TokenProperties.Token refresh = new TokenProperties.Token();
        refresh.setExpirationMinutes(10080L);

        when(tokenProperties.getAccess()).thenReturn(access);
        when(tokenProperties.getRefresh()).thenReturn(refresh);
    }

    @Test
    @WithMockUser
    void getMe_Success() throws Exception {
        UserResponseDTO userDTO = new UserResponseDTO(1L, "Max", "Mustermann", "max@example.com");
        when(authService.getMe()).thenReturn(userDTO);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("max@example.com"));
    }

    @Test
    void register_Success() throws Exception {
        RegisterTenantRequestDTO requestDTO = new RegisterTenantRequestDTO("Tenant1", "password123", "Max", "Mustermann", "max@tenant1.com");
        RegisterTenantResponseDTO responseDTO = new RegisterTenantResponseDTO(1L, "Tenant1");

        when(authService.registerNewTenant(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tenantName").value("Tenant1"));
    }

    @Test
    void login_Success() throws Exception {
        LoginRequestDTO requestDTO = new LoginRequestDTO();
        requestDTO.setEmail("max@tenant1.com");
        requestDTO.setPassword("password123");

        UserResponseDTO userDTO = new UserResponseDTO(1L, "Max", "Mustermann", "max@tenant1.com");
        AuthResponseDTO responseDTO = new AuthResponseDTO("access-token-123", "refresh-token-123", userDTO);

        when(authService.login(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("max@tenant1.com"));
    }

    @Test
    void refreshAccessToken_Success() throws Exception {
        when(authService.refreshAccessToken("valid-refresh-token")).thenReturn("new-access-token");

        mockMvc.perform(get("/api/auth/refresh-token")
                        .cookie(new Cookie("refreshToken", "valid-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void refreshAccessToken_ExceptionHandled() throws Exception {
        when(authService.refreshAccessToken("invalid-refresh-token")).thenThrow(new ForbiddenException("error.forbidden"));

        mockMvc.perform(get("/api/auth/refresh-token")
                        .cookie(new Cookie("refreshToken", "invalid-refresh-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    void logout_Success() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.success").value(true));
    }
}
