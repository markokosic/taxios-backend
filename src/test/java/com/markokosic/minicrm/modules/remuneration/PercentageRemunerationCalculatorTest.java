package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.exception.BadRequestException;
import com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PercentageRemunerationCalculatorTest {

    private final PercentageRemunerationCalculator calculator = new PercentageRemunerationCalculator();




    @Test
    void testCalculateRemuneration_whenShareIsAboveMinPayout_shouldReturnPercentageShare() {
        BigDecimal revenue = new BigDecimal("200.00");
        PercentageShareRemunerationConfig config = new PercentageShareRemunerationConfig();
        config.setDriverRevenueSharePercentage(new BigDecimal("0.4000"));
        config.setMinDriverPayoutPerShift(new BigDecimal("30.00"));

        RemunerationSplit split = calculator.calculateRemuneration(revenue, config);

        assertNotNull(split);
        assertEquals(new BigDecimal("80.00"), split.driverRemuneration());
        assertEquals(new BigDecimal("120.00"), split.companyRemuneration());
    }

    @Test
    void testCalculateRemuneration_whenShareIsBelowMinPayout_shouldReturnMinPayout() {
        BigDecimal revenue = new BigDecimal("50.00");
        PercentageShareRemunerationConfig config = new PercentageShareRemunerationConfig();
        config.setDriverRevenueSharePercentage(new BigDecimal("0.4000"));
        config.setMinDriverPayoutPerShift(new BigDecimal("30.00"));

        RemunerationSplit split = calculator.calculateRemuneration(revenue, config);

        assertNotNull(split);
        assertEquals(new BigDecimal("30.00"), split.driverRemuneration());
        assertEquals(new BigDecimal("20.00"), split.companyRemuneration());
    }

    @Test
    void testCalculateRemuneration_whenHalfUpRoundingNeeded_shouldRoundCorrectly() {
        BigDecimal revenue = new BigDecimal("100.15");
        PercentageShareRemunerationConfig config = new PercentageShareRemunerationConfig();
        config.setDriverRevenueSharePercentage(new BigDecimal("0.4500"));
        config.setMinDriverPayoutPerShift(new BigDecimal("0.00"));

        RemunerationSplit split = calculator.calculateRemuneration(revenue, config);

        assertNotNull(split);
        assertEquals(new BigDecimal("45.07"), split.driverRemuneration());
        assertEquals(new BigDecimal("55.08"), split.companyRemuneration());
    }
}
