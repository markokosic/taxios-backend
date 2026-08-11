package com.markokosic.minicrm.modules.user;

import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.modules.user.dto.response.UserResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private I18nService i18n;

    @Test
    @WithMockUser
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
    @WithMockUser
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
    @WithMockUser
    void deleteUser_Success() throws Exception {
        doNothing().when(userService).deleteUser(1L);
        when(i18n.getMessage("success.deleted")).thenReturn("Deleted successfully");

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
