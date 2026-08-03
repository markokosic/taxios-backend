package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class FlatRateRemunerationCalculatorTest {

    private final FlatRateRemunerationCalculator calculator = new FlatRateRemunerationCalculator();

    @Test
    void testCalculateRemuneration_whenRevenueIsGreaterThanFee_shouldSplitCorrectly() {
        // Arrange
        BigDecimal revenue = new BigDecimal("100.00");
        FlatRateRemunerationConfig config = new FlatRateRemunerationConfig();
        config.setFlatRateFee(new BigDecimal("30.00"));

        // Act
        RemunerationSplit split = calculator.calculateRemuneration(revenue, config);

        // Assert
        assertNotNull(split);
        assertEquals(new BigDecimal("30.00"), split.driverRemuneration());
        assertEquals(new BigDecimal("70.00"), split.companyRemuneration());
    }

    @Test
    void testCalculateRemuneration_whenRevenueIsLessThanFee_shouldSplitCorrectly() {
        // Arrange
        BigDecimal revenue = new BigDecimal("20.00");
        FlatRateRemunerationConfig config = new FlatRateRemunerationConfig();
        config.setFlatRateFee(new BigDecimal("30.00"));

        // Act
        RemunerationSplit split = calculator.calculateRemuneration(revenue, config);

        // Assert
        assertNotNull(split);
        assertEquals(new BigDecimal("30.00"), split.driverRemuneration());
        assertEquals(new BigDecimal("-10.00"), split.companyRemuneration());
    }
}
