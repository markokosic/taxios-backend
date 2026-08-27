package com.markokosic.minicrm.modules.shift;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.modules.shift.dto.request.CreateShiftRequestDTO;
import com.markokosic.minicrm.modules.shift.dto.response.ShiftResponseDTO;
import com.markokosic.minicrm.modules.shift.model.ShiftStatus;
import com.markokosic.minicrm.modules.shift.service.ShiftService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShiftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShiftService shiftService;

    @MockBean
    private I18nService i18n;

    @Test
    void getMyShifts_Success_WhenDriverRole() throws Exception {
        com.markokosic.minicrm.modules.user.User driverUser = new com.markokosic.minicrm.modules.user.User();
        driverUser.setId(5L);
        driverUser.setEmail("driver@taxi.com");
        driverUser.setRoles(com.markokosic.minicrm.modules.role.dto.Roles.DRIVER);
        com.markokosic.minicrm.modules.auth.model.UserPrincipal principal = new com.markokosic.minicrm.modules.auth.model.UserPrincipal(driverUser);

        PageResponseDTO<ShiftResponseDTO> pageResponse = new PageResponseDTO<>(List.of(), 1, 10, 0L, 0, true, true);

        when(shiftService.getMyShifts(eq(5L), any())).thenReturn(pageResponse);
        when(i18n.getMessage("success.fetched")).thenReturn("Shifts fetched");

        mockMvc.perform(get("/api/shifts/my").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createMyShift_Success_WhenDriverRole() throws Exception {
        com.markokosic.minicrm.modules.user.User driverUser = new com.markokosic.minicrm.modules.user.User();
        driverUser.setId(5L);
        driverUser.setEmail("driver@taxi.com");
        driverUser.setRoles(com.markokosic.minicrm.modules.role.dto.Roles.DRIVER);
        com.markokosic.minicrm.modules.auth.model.UserPrincipal principal = new com.markokosic.minicrm.modules.auth.model.UserPrincipal(driverUser);

        var revenueEntry = new com.markokosic.minicrm.modules.shift.dto.request.CreateShiftRevenueEntryRequestDTO(
                com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory.REGULAR,
                null, new BigDecimal("100.00"), null, null
        );
        var requestDTO = new com.markokosic.minicrm.modules.shift.dto.request.CreateMyShiftRequestDTO(
                1L, new BigDecimal("100.00"), new BigDecimal("200.00"),
                LocalDateTime.now(), LocalDateTime.now().plusHours(8), List.of(revenueEntry)
        );

        ShiftResponseDTO responseDTO = new ShiftResponseDTO(
                1L, null, null, new BigDecimal("100.00"), new BigDecimal("200.00"),
                new BigDecimal("100.00"), LocalDateTime.now(), LocalDateTime.now().plusHours(8),
                ShiftStatus.PENDING, List.of()
        );

        when(shiftService.createMyShift(eq(5L), any())).thenReturn(responseDTO);
        when(i18n.getMessage("success.added")).thenReturn("Shift added");

        mockMvc.perform(post("/api/shifts/my")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void getAllShifts_Forbidden_WhenDriverRole() throws Exception {
        mockMvc.perform(get("/api/shifts"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void createShift_Forbidden_WhenDriverRole() throws Exception {
        var revenueEntry = new com.markokosic.minicrm.modules.shift.dto.request.CreateShiftRevenueEntryRequestDTO(
                com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory.REGULAR,
                null, new BigDecimal("100.00"), null, null
        );
        CreateShiftRequestDTO requestDTO = new CreateShiftRequestDTO(
                1L, 1L, new BigDecimal("100.00"), new BigDecimal("200.00"),
                LocalDateTime.now(), LocalDateTime.now().plusHours(8), ShiftStatus.APPROVED, List.of(revenueEntry)
        );

        mockMvc.perform(post("/api/shifts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllShifts_Success_WhenAdminRole() throws Exception {
        PageResponseDTO<ShiftResponseDTO> pageResponse = new PageResponseDTO<>(List.of(), 1, 10, 0L, 0, true, true);

        when(shiftService.getAllShifts(any(), any(), any(), any())).thenReturn(pageResponse);
        when(i18n.getMessage("success.fetched")).thenReturn("Shifts fetched");

        mockMvc.perform(get("/api/shifts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveShift_Success_WhenAdminRole() throws Exception {
        ShiftResponseDTO responseDTO = new ShiftResponseDTO(
                1L, null, null, new BigDecimal("100.00"), new BigDecimal("200.00"),
                new BigDecimal("100.00"), LocalDateTime.now(), LocalDateTime.now().plusHours(8),
                ShiftStatus.APPROVED, List.of()
        );

        when(shiftService.approveShift(1L)).thenReturn(responseDTO);
        when(i18n.getMessage("success.updated")).thenReturn("Shift updated");

        mockMvc.perform(post("/api/shifts/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void approveShift_Forbidden_WhenDriverRole() throws Exception {
        mockMvc.perform(post("/api/shifts/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateMyShift_Success_WhenDriverRole() throws Exception {
        com.markokosic.minicrm.modules.user.User driverUser = new com.markokosic.minicrm.modules.user.User();
        driverUser.setId(5L);
        driverUser.setEmail("driver@taxi.com");
        driverUser.setRoles(com.markokosic.minicrm.modules.role.dto.Roles.DRIVER);
        com.markokosic.minicrm.modules.auth.model.UserPrincipal principal = new com.markokosic.minicrm.modules.auth.model.UserPrincipal(driverUser);

        var updateReq = new com.markokosic.minicrm.modules.shift.dto.request.UpdateShiftRevenueEntryRequestDTO(
                101L, com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory.REGULAR, null, new BigDecimal("150.00"), null, null
        );
        var requestDTO = new com.markokosic.minicrm.modules.shift.dto.request.UpdateShiftRequestDTO(
                new BigDecimal("100.00"), new BigDecimal("250.00"),
                LocalDateTime.now(), LocalDateTime.now().plusHours(8), List.of(updateReq)
        );

        ShiftResponseDTO responseDTO = new ShiftResponseDTO(
                1L, null, null, new BigDecimal("100.00"), new BigDecimal("250.00"),
                new BigDecimal("150.00"), LocalDateTime.now(), LocalDateTime.now().plusHours(8),
                ShiftStatus.PENDING, List.of()
        );

        when(shiftService.updateMyShift(eq(5L), eq(1L), any())).thenReturn(responseDTO);
        when(i18n.getMessage("success.updated")).thenReturn("Shift updated");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/shifts/my/1")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteMyShift_Success_WhenDriverRole() throws Exception {
        com.markokosic.minicrm.modules.user.User driverUser = new com.markokosic.minicrm.modules.user.User();
        driverUser.setId(5L);
        driverUser.setEmail("driver@taxi.com");
        driverUser.setRoles(com.markokosic.minicrm.modules.role.dto.Roles.DRIVER);
        com.markokosic.minicrm.modules.auth.model.UserPrincipal principal = new com.markokosic.minicrm.modules.auth.model.UserPrincipal(driverUser);

        when(i18n.getMessage("success.deleted")).thenReturn("Shift deleted");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/shifts/my/1")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
