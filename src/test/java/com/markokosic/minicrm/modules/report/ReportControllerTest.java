package com.markokosic.minicrm.modules.report;

import com.markokosic.minicrm.common.I18nService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private I18nService i18n;

    @Test
    @WithMockUser
    void getRevenueReport_Success() throws Exception {
        LocalDate dateFrom = LocalDate.of(2025, 1, 1);
        LocalDate dateTo = LocalDate.of(2025, 1, 31);
        RevenueReportSummaryDTO totals = new RevenueReportSummaryDTO(BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, 1);
        RevenueReportResponseDTO responseDTO = new RevenueReportResponseDTO(dateFrom, dateTo, GroupBy.NONE, totals, List.of());

        when(reportService.generateRevenueReport(eq(dateFrom), eq(dateTo), any(), any())).thenReturn(responseDTO);
        when(i18n.getMessage("success.fetched")).thenReturn("Fetched");

        mockMvc.perform(get("/api/reports/revenue")
                        .param("dateFrom", "2025-01-01")
                        .param("dateTo", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    void getDashboardReport_Success() throws Exception {
        DashboardReportDTO dashboardDTO = new DashboardReportDTO(
                2025, 5, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.TEN, 1L
        );

        when(reportService.generateDashboardReport(2025, 5)).thenReturn(dashboardDTO);
        when(i18n.getMessage("success.fetched")).thenReturn("Fetched");

        mockMvc.perform(get("/api/reports/dashboard")
                        .param("year", "2025")
                        .param("month", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.year").value(2025));
    }
}
