package com.markokosic.minicrm.modules.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RevenueReportEntryDTO(
    LocalDate date,
    BigDecimal revenue,
    BigDecimal companyRemuneration,
    BigDecimal driverRemuneration,
    BigDecimal kilometersDriven,
    Long entryCount,
    List<DriverInfoDTO> drivers
) {
}
