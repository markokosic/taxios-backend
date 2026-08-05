package com.markokosic.minicrm.modules.revenue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.markokosic.minicrm.common.I18nService;
import com.markokosic.minicrm.common.dto.response.PageResponseDTO;
import com.markokosic.minicrm.modules.car.dto.response.CarSummaryDTO;
import com.markokosic.minicrm.modules.driver.dto.response.DriverSummaryDTO;
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
import java.time.LocalDate;
import java.time.LocalTime;
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
class RevenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RevenueService revenueService;

    @MockBean
    private I18nService i18n;

    @Test
    @WithMockUser
    void createDailyRevenuesBulk_Success() throws Exception {
        CreateDailyRevenueRequestDTO requestDTO = new CreateDailyRevenueRequestDTO(
                1L,
                2L,
                LocalDate.now(),
                new BigDecimal("100.00"),
                new BigDecimal("250.00"),
                new BigDecimal("10.00"),
                new BigDecimal("110.00"),
                RemunerationModelType.FLAT_RATE,
                LocalTime.of(8, 0),
                LocalTime.of(16, 0),
                5L,
                new BigDecimal("50.00"),
                new BigDecimal("100.00")
        );

        doNothing().when(revenueService).createDailyRevenuesBulk(any());
        when(i18n.getMessage("success.added")).thenReturn("Added");

        mockMvc.perform(post("/api/revenues/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(requestDTO))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void getAllDailyRevenues_Success() throws Exception {
        DailyRevenueResponseDTO dto = new DailyRevenueResponseDTO(
                1L,
                LocalDate.now(),
                RemunerationModelType.FLAT_RATE,
                5L,
                new BigDecimal("50.00"),
                new DriverSummaryDTO(1L, "John", "Doe"),
                new CarSummaryDTO(2L, "M-AB1234", "Toyota", "Prius"),
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("110.00"),
                new BigDecimal("250.00"),
                new BigDecimal("100.00"),
                new BigDecimal("150.00"),
                LocalTime.of(8, 0),
                LocalTime.of(16, 0)
        );

        PageResponseDTO<DailyRevenueResponseDTO> pageResponse = new PageResponseDTO<>(List.of(dto), 1, 10, 1L, 1, true, true);

        when(revenueService.getAllRevenues(any(), any(), any(), any())).thenReturn(pageResponse);
        when(i18n.getMessage("success.fetched")).thenReturn("Fetched");

        mockMvc.perform(get("/api/revenues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    @WithMockUser
    void updateDailyRevenue_Success() throws Exception {
        CreateDailyRevenueRequestDTO requestDTO = new CreateDailyRevenueRequestDTO(
                1L,
                2L,
                LocalDate.now(),
                new BigDecimal("100.00"),
                new BigDecimal("300.00"),
                new BigDecimal("10.00"),
                new BigDecimal("110.00"),
                RemunerationModelType.FLAT_RATE,
                LocalTime.of(8, 0),
                LocalTime.of(16, 0),
                5L,
                new BigDecimal("60.00"),
                new BigDecimal("120.00")
        );

        DailyRevenueResponseDTO responseDTO = new DailyRevenueResponseDTO(
                1L,
                LocalDate.now(),
                RemunerationModelType.FLAT_RATE,
                5L,
                new BigDecimal("60.00"),
                new DriverSummaryDTO(1L, "John", "Doe"),
                new CarSummaryDTO(2L, "M-AB1234", "Toyota", "Prius"),
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("110.00"),
                new BigDecimal("300.00"),
                new BigDecimal("120.00"),
                new BigDecimal("180.00"),
                LocalTime.of(8, 0),
                LocalTime.of(16, 0)
        );

        when(revenueService.updateDailyRevenue(eq(1L), any())).thenReturn(responseDTO);
        when(i18n.getMessage("success.updated")).thenReturn("Updated");

        mockMvc.perform(put("/api/revenues/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.revenue").value(300.00));
    }

    @Test
    @WithMockUser
    void deleteDailyRevenue_Success() throws Exception {
        doNothing().when(revenueService).deleteDailyRevenue(1L);
        when(i18n.getMessage("success.deleted")).thenReturn("Deleted");

        mockMvc.perform(delete("/api/revenues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
