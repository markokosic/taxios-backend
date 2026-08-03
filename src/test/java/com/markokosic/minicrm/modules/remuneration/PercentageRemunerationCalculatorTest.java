package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PercentageRemunerationCalculatorTest {

    private final PercentageRemunerationCalculator calculator = new PercentageRemunerationCalculator();

    @Test
    void testCalculateRemuneration_whenShareIsAboveMinPayout_shouldReturnPercentageShare() {
        // Arrange
        BigDecimal revenue = new BigDecimal("200.00");
        PercentageShareRemunerationConfig config = new PercentageShareRemunerationConfig();
        config.setDriverRevenueSharePercentage(new BigDecimal("40.00"));
        config.setMinDriverPayout(new BigDecimal("30.00"));

        // Act
        RemunerationSplit split = calculator.calculateRemuneration(revenue, config);

        // Assert
        assertNotNull(split);
        assertEquals(new BigDecimal("80.00"), split.driverRemuneration());
        assertEquals(new BigDecimal("120.00"), split.companyRemuneration());
    }

    @Test
    void testCalculateRemuneration_whenShareIsBelowMinPayout_shouldReturnMinPayout() {
        // Arrange
        BigDecimal revenue = new BigDecimal("50.00");
        PercentageShareRemunerationConfig config = new PercentageShareRemunerationConfig();
        config.setDriverRevenueSharePercentage(new BigDecimal("40.00"));
        config.setMinDriverPayout(new BigDecimal("30.00"));

        // Act
        RemunerationSplit split = calculator.calculateRemuneration(revenue, config);

        // Assert
        assertNotNull(split);
        assertEquals(new BigDecimal("30.00"), split.driverRemuneration());
        assertEquals(new BigDecimal("20.00"), split.companyRemuneration());
    }

    @Test
    void testCalculateRemuneration_whenHalfUpRoundingNeeded_shouldRoundCorrectly() {
        // Arrange
        BigDecimal revenue = new BigDecimal("100.15");
        PercentageShareRemunerationConfig config = new PercentageShareRemunerationConfig();
        config.setDriverRevenueSharePercentage(new BigDecimal("45.00"));
        config.setMinDriverPayout(new BigDecimal("0.00"));

        // Act
        RemunerationSplit split = calculator.calculateRemuneration(revenue, config);

        // Assert
        assertNotNull(split);
        // (100.15 * 45) / 100 = 45.0675 -> HALF_UP -> 45.07
        assertEquals(new BigDecimal("45.07"), split.driverRemuneration());
        assertEquals(new BigDecimal("55.08"), split.companyRemuneration()); // 100.15 - 45.07 = 55.08
    }
}
