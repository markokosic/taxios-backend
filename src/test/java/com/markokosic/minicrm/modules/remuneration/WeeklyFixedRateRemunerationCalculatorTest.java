package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.exception.BadRequestException;
import com.markokosic.minicrm.modules.driver.model.WeeklyFixedRateRemunerationConfig;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WeeklyFixedRateRemunerationCalculatorTest {

    private final WeeklyFixedRateRemunerationCalculator calculator = new WeeklyFixedRateRemunerationCalculator();



    @Test
    void testCalculateRemuneration_legacySingleNumber_shouldSplitAllToDriverAndZeroToCompany() {
        BigDecimal revenue = new BigDecimal("150.00");
        WeeklyFixedRateRemunerationConfig config = new WeeklyFixedRateRemunerationConfig();
        config.setWeeklyFixedCompanySettlement(new BigDecimal("100.00"));
        config.setSettlementDay(5);

        RemunerationSplit split = calculator.calculateRemuneration(revenue, config);

        assertNotNull(split);
        assertEquals(BigDecimal.ZERO, split.companyRemuneration());
        assertEquals(revenue, split.driverRemuneration());
    }
}
