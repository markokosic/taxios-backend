package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.WeeklyFixedRateRemunerationConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class WeeklyFixedRateRemunerationCalculatorTest {

    private final WeeklyFixedRateRemunerationCalculator calculator = new WeeklyFixedRateRemunerationCalculator();

    @Test
    void testCalculateRemuneration_shouldSplitAllToDriverAndZeroToCompany() {
        // Arrange
        BigDecimal revenue = new BigDecimal("150.00");
        WeeklyFixedRateRemunerationConfig config = new WeeklyFixedRateRemunerationConfig();
        config.setWeeklyFixedCompanySettlement(new BigDecimal("100.00"));
        config.setSettlementDay(5);

        // Act
        RemunerationSplit split = calculator.calculateRemuneration(revenue, config);

        // Assert
        assertNotNull(split);
        assertEquals(BigDecimal.ZERO, split.companyRemuneration());
        assertEquals(revenue, split.driverRemuneration());
    }
}
