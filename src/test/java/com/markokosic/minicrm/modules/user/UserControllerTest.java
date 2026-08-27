package com.markokosic.minicrm.modules.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.modules.role.dto.Roles;
import com.markokosic.minicrm.modules.user.dto.request.CreateUserRequestDTO;
import com.markokosic.minicrm.modules.user.dto.response.CreateUserResponseDTO;
import com.markokosic.minicrm.modules.user.dto.response.UserResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private I18nService i18n;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_Success() throws Exception {
        CreateUserRequestDTO requestDTO = new CreateUserRequestDTO(
                "driver@example.com", "Max", "Mustermann", Roles.DRIVER
        );
        CreateUserResponseDTO userDTO = new CreateUserResponseDTO(1L, "Max", "Mustermann", "driver@example.com", Roles.DRIVER, true, "tempPass123");

        when(userService.createUser(any(CreateUserRequestDTO.class))).thenReturn(userDTO);
        when(i18n.getMessage("success.added")).thenReturn("Added successfully");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("driver@example.com"))
                .andExpect(jsonPath("$.data.roles").value("DRIVER"))
                .andExpect(jsonPath("$.data.mustChangePassword").value(true))
                .andExpect(jsonPath("$.data.temporaryPassword").value("tempPass123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_Success() throws Exception {
        UserResponseDTO userDTO = new UserResponseDTO(1L, "Max", "Mustermann", "max@example.com");
        when(userService.getUserById(1L)).thenReturn(userDTO);
        when(i18n.getMessage("success.fetched")).thenReturn("Fetched successfully");

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("max@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_Success() throws Exception {
        UserResponseDTO userDTO = new UserResponseDTO(1L, "Max", "Mustermann", "max@example.com");
        when(userService.getAllUsers()).thenReturn(List.of(userDTO));
        when(i18n.getMessage("success.fetched")).thenReturn("Fetched successfully");

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("max@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);
        when(i18n.getMessage("success.deleted")).thenReturn("Deleted successfully");

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
