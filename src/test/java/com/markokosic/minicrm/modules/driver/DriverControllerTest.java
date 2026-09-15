package com.markokosic.minicrm.modules.driver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateFlatRateRemunerationConfigDTO;
import com.markokosic.minicrm.modules.driver.dto.request.UpdateDriverRequestDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverResponseDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverSelectDTO;
import com.markokosic.minicrm.modules.driver.model.DriverStatus;
import com.markokosic.minicrm.modules.driver.service.DriverService;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.role.dto.Roles;
import com.markokosic.minicrm.modules.user.dto.response.CreateUserResponseDTO;
import com.markokosic.minicrm.modules.auth.model.UserPrincipal;
import com.markokosic.minicrm.modules.driver.dto.response.DriverRevenueOptionDTO;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import com.markokosic.minicrm.modules.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DriverService driverService;

    @MockBean
    private I18nService i18n;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDriver_Success() throws Exception {
        var remConfig = new CreateFlatRateRemunerationConfigDTO(RemunerationModelType.FLAT_RATE, new BigDecimal("15.00"), null);
        CreateDriverRequestDTO requestDTO = new CreateDriverRequestDTO("John", "Doe", "john@example.com", "+12345678", List.of(remConfig));
        DriverResponseDTO responseDTO = new DriverResponseDTO(1L, null, "John", "Doe", "john@example.com", "+12345678", DriverStatus.ACTIVE, null, null, null);

        when(driverService.createDriver(any())).thenReturn(responseDTO);
        when(i18n.getMessage("success.created")).thenReturn("Driver created");

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDriver_Success() throws Exception {
        DriverResponseDTO responseDTO = new DriverResponseDTO(1L, 10L, "John", "Doe", "john@example.com", "+12345678", DriverStatus.ACTIVE, null, null, null);

        when(driverService.getDriverById(1L)).thenReturn(responseDTO);
        when(i18n.getMessage("success.fetched")).thenReturn("Driver fetched");

        mockMvc.perform(get("/api/drivers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDriversForSelect_Success() throws Exception {
        DriverSelectDTO selectDTO = new DriverSelectDTO(1L, "John Doe");
        when(driverService.getAllDriversForSelect()).thenReturn(List.of(selectDTO));
        when(i18n.getMessage("success.fetched")).thenReturn("Drivers fetched");

        mockMvc.perform(get("/api/drivers/select"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fullName").value("John Doe"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllDrivers_Success() throws Exception {
        DriverResponseDTO responseDTO = new DriverResponseDTO(1L, null, "John", "Doe", "john@example.com", "+12345678", DriverStatus.ACTIVE, null, null, null);
        PageResponseDTO<DriverResponseDTO> pageResponse = new PageResponseDTO<>(List.of(responseDTO), 1, 10, 1L, 1, true, true);

        when(driverService.getAllDrivers(any())).thenReturn(pageResponse);
        when(i18n.getMessage("success.fetched")).thenReturn("Drivers fetched");

        mockMvc.perform(get("/api/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].firstName").value("John"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateDriver_Success() throws Exception {
        UpdateDriverRequestDTO requestDTO = new UpdateDriverRequestDTO("John", "Smith", "john@example.com", "+12345678", List.of());
        DriverResponseDTO responseDTO = new DriverResponseDTO(1L, null, "John", "Smith", "john@example.com", "+12345678", DriverStatus.ACTIVE, null, null, null);

        when(driverService.updateDriver(eq(1L), any())).thenReturn(responseDTO);
        when(i18n.getMessage("success.updated")).thenReturn("Driver updated");

        mockMvc.perform(patch("/api/drivers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lastName").value("Smith"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDriver_Success() throws Exception {
        doNothing().when(driverService).deleteDriver(1L);
        when(i18n.getMessage("success.deleted")).thenReturn("Driver deleted");

        mockMvc.perform(delete("/api/drivers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void stopRemunerationConfig_Success() throws Exception {
        doNothing().when(driverService).stopRemunerationConfig(1L, 10L);

        mockMvc.perform(delete("/api/drivers/1/remuneration-configs/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDriverUser_withoutBody_Success() throws Exception {
        CreateUserResponseDTO responseDTO = new CreateUserResponseDTO(
                10L, "Max", "Mustermann", "driver@taxi.com", Roles.DRIVER, true, "tempPass123"
        );

        when(driverService.createDriverUser(1L, null)).thenReturn(responseDTO);
        when(i18n.getMessage("success.created")).thenReturn("User created");

        mockMvc.perform(post("/api/drivers/1/user"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("driver@taxi.com"))
                .andExpect(jsonPath("$.data.temporaryPassword").value("tempPass123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDriverUser_withCustomEmail_Success() throws Exception {
        CreateUserResponseDTO responseDTO = new CreateUserResponseDTO(
                10L, "Max", "Mustermann", "custom.login@taxi.com", Roles.DRIVER, true, "tempPass123"
        );

        when(driverService.createDriverUser(1L, "custom.login@taxi.com")).thenReturn(responseDTO);
        when(i18n.getMessage("success.created")).thenReturn("User created");

        mockMvc.perform(post("/api/drivers/1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"custom.login@taxi.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("custom.login@taxi.com"))
                .andExpect(jsonPath("$.data.temporaryPassword").value("tempPass123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateDriverUser_Success() throws Exception {
        doNothing().when(driverService).deactivateDriverUser(1L);

        mockMvc.perform(delete("/api/drivers/1/user"))
                .andExpect(status().isNoContent());

        verify(driverService, times(1)).deactivateDriverUser(1L);
    }

    @Test
    void getMyDriverProfile_Success_WhenDriverRole() throws Exception {
        User driverUser = new User();
        driverUser.setId(5L);
        driverUser.setEmail("driver@taxi.com");
        driverUser.setRoles(Roles.DRIVER);
        UserPrincipal principal = new UserPrincipal(driverUser);

        DriverResponseDTO responseDTO = new DriverResponseDTO(
                1L, 5L, "Max", "Mustermann", "driver@taxi.com", "+12345678",
                DriverStatus.ACTIVE, List.of(), null, null
        );

        when(driverService.getMyDriverProfile(5L)).thenReturn(responseDTO);
        when(i18n.getMessage("success.fetched")).thenReturn("Driver fetched");

        mockMvc.perform(get("/api/drivers/my").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("Max"));
    }

    @Test
    void getMyRevenueOptions_Success_WhenDriverRole() throws Exception {
        User driverUser = new User();
        driverUser.setId(5L);
        driverUser.setEmail("driver@taxi.com");
        driverUser.setRoles(Roles.DRIVER);
        UserPrincipal principal = new UserPrincipal(driverUser);

        List<DriverRevenueOptionDTO> options = List.of(
                new DriverRevenueOptionDTO(ShiftEntryCategory.REGULAR, null, "Regular Fare (Taxameter)", null, null)
        );

        when(driverService.getMyRevenueOptions(5L)).thenReturn(options);
        when(i18n.getMessage("success.fetched")).thenReturn("Revenue options fetched");

        mockMvc.perform(get("/api/drivers/my/revenue-options").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].entryCategory").value("REGULAR"))
                .andExpect(jsonPath("$.data[0].label").value("Regular Fare (Taxameter)"));
    }
}
