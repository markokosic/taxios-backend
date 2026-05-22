package com.markokosic.minicrm.modules.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueReportEntryDTO(
    LocalDate date,
    BigDecimal revenue,
    BigDecimal companyRemuneration,
    BigDecimal driverRemuneration,
    BigDecimal kilometersDriven,
    Long entryCount
) {
}
