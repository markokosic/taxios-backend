package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FlatRateRemunerationCalculatorTest {

    private final FlatRateRemunerationCalculator calculator = new FlatRateRemunerationCalculator();



    @Test
    void testCalculateRemuneration_whenRevenueIsGreaterThanFee_shouldSplitCorrectly() {
        BigDecimal revenue = new BigDecimal("100.00");
        FlatRateRemunerationConfig config = new FlatRateRemunerationConfig();
        config.setDriverFlatRatePayoutPerShift(new BigDecimal("30.00"));

        RemunerationSplit split = calculator.calculateRemuneration(revenue, config);

        assertNotNull(split);
        assertEquals(new BigDecimal("30.00"), split.driverRemuneration());
        assertEquals(new BigDecimal("70.00"), split.companyRemuneration());
    }

    @Test
    void testCalculateRemuneration_whenRevenueIsLessThanFee_shouldSplitCorrectly() {
        BigDecimal revenue = new BigDecimal("20.00");
        FlatRateRemunerationConfig config = new FlatRateRemunerationConfig();
        config.setDriverFlatRatePayoutPerShift(new BigDecimal("30.00"));

        RemunerationSplit split = calculator.calculateRemuneration(revenue, config);

        assertNotNull(split);
        assertEquals(new BigDecimal("30.00"), split.driverRemuneration());
        assertEquals(new BigDecimal("-10.00"), split.companyRemuneration());
    }
}
