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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
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
    @WithMockUser
    void createDriver_Success() throws Exception {
        var remConfig = new CreateFlatRateRemunerationConfigDTO(RemunerationModelType.FLAT_RATE, new BigDecimal("15.00"), null);
        CreateDriverRequestDTO requestDTO = new CreateDriverRequestDTO("John", "Doe", "john@example.com", "+12345678", List.of(remConfig));
        DriverResponseDTO responseDTO = new DriverResponseDTO(1L, "John", "Doe", "john@example.com", "+12345678", DriverStatus.ACTIVE, null, null, null);

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
    @WithMockUser
    void getDriver_Success() throws Exception {
        DriverResponseDTO responseDTO = new DriverResponseDTO(1L, "John", "Doe", "john@example.com", "+12345678", DriverStatus.ACTIVE, null, null, null);

        when(driverService.getDriverById(1L)).thenReturn(responseDTO);
        when(i18n.getMessage("success.fetched")).thenReturn("Driver fetched");

        mockMvc.perform(get("/api/drivers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser
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
    @WithMockUser
    void getAllDrivers_Success() throws Exception {
        DriverResponseDTO responseDTO = new DriverResponseDTO(1L, "John", "Doe", "john@example.com", "+12345678", DriverStatus.ACTIVE, null, null, null);
        PageResponseDTO<DriverResponseDTO> pageResponse = new PageResponseDTO<>(List.of(responseDTO), 1, 10, 1L, 1, true, true);

        when(driverService.getAllDrivers(any())).thenReturn(pageResponse);
        when(i18n.getMessage("success.fetched")).thenReturn("Drivers fetched");

        mockMvc.perform(get("/api/drivers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].firstName").value("John"));
    }

    @Test
    @WithMockUser
    void updateDriver_Success() throws Exception {
        UpdateDriverRequestDTO requestDTO = new UpdateDriverRequestDTO("John", "Smith", "john@example.com", "+12345678", List.of());
        DriverResponseDTO responseDTO = new DriverResponseDTO(1L, "John", "Smith", "john@example.com", "+12345678", DriverStatus.ACTIVE, null, null, null);

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
    @WithMockUser
    void deleteDriver_Success() throws Exception {
        doNothing().when(driverService).deleteDriver(1L);
        when(i18n.getMessage("success.deleted")).thenReturn("Driver deleted");

        mockMvc.perform(delete("/api/drivers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void stopRemunerationConfig_Success() throws Exception {
        doNothing().when(driverService).stopRemunerationConfig(1L, 10L);

        mockMvc.perform(delete("/api/drivers/1/remuneration-configs/10"))
                .andExpect(status().isNoContent());
    }
}
