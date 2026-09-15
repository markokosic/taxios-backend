package com.markokosic.minicrm.modules.shift.service;

import com.markokosic.minicrm.exception.BadRequestException;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.WeeklyFixedRateRemunerationConfig;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import com.markokosic.minicrm.modules.remuneration.FlatRateRemunerationCalculator;
import com.markokosic.minicrm.modules.remuneration.PercentageRemunerationCalculator;
import com.markokosic.minicrm.modules.remuneration.WeeklyFixedRateRemunerationCalculator;
import com.markokosic.minicrm.modules.shift.ShiftSettlementMapper;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import com.markokosic.minicrm.modules.shift.model.ShiftSettlement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShiftSettlementCalculatorTest {

    private ShiftSettlementCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ShiftSettlementCalculator(
                List.of(
                        new WeeklyFixedRateRemunerationCalculator(),
                        new PercentageRemunerationCalculator(),
                        new FlatRateRemunerationCalculator()
                ),
                Mappers.getMapper(ShiftSettlementMapper.class)
        );
    }

    @Test
    void calculate_WeeklyDriver_RequiresWeeklyDriverRent() {
        Driver driver = new Driver();
        WeeklyFixedRateRemunerationConfig weeklyConfig = new WeeklyFixedRateRemunerationConfig();
        weeklyConfig.setWeeklyFixedCompanySettlement(new BigDecimal("400.00"));
        driver.initializeWithRemunerationConfigs(List.of(weeklyConfig));

        Shift shift = new Shift();
        shift.setDriver(driver);
        shift.setAppliedRemunerationConfigs(new ArrayList<>(driver.getActiveRemunerationConfigs()));
        shift.setRevenues(new ArrayList<>());

        assertThrows(BadRequestException.class, () -> calculator.calculate(shift, null));
        assertThrows(BadRequestException.class, () -> calculator.calculate(shift, new BigDecimal("-10.00")));
    }

    @Test
    void calculate_WeeklyDriver_CalculatesCorrectRemuneration() {
        Driver driver = new Driver();
        WeeklyFixedRateRemunerationConfig weeklyConfig = new WeeklyFixedRateRemunerationConfig();
        weeklyConfig.setWeeklyFixedCompanySettlement(new BigDecimal("400.00"));
        driver.initializeWithRemunerationConfigs(List.of(weeklyConfig));

        Shift shift = new Shift();
        shift.setDriver(driver);
        shift.setAppliedRemunerationConfigs(new ArrayList<>(driver.getActiveRemunerationConfigs()));

        ShiftRevenueEntry entry1 = new ShiftRevenueEntry();
        entry1.setEntryCategory(ShiftEntryCategory.REGULAR);
        entry1.setRevenue(new BigDecimal("200.00"));

        ShiftRevenueEntry entry2 = new ShiftRevenueEntry();
        entry2.setEntryCategory(ShiftEntryCategory.REGULAR);
        entry2.setRevenue(new BigDecimal("100.00"));

        shift.setRevenues(new ArrayList<>(List.of(entry1, entry2)));

        ShiftSettlement settlement = calculator.calculate(shift, new BigDecimal("400.00"));

        assertNotNull(settlement);
        assertEquals(new BigDecimal("300.00"), settlement.getTotalRevenue());
        assertEquals(new BigDecimal("300.00"), settlement.getRevenue());
        assertEquals(new BigDecimal("400.00"), shift.getWeeklyDriverRent());
        assertEquals(new BigDecimal("-100.00"), settlement.getDriverRemuneration());
        assertEquals(new BigDecimal("400.00"), settlement.getCompanyRemuneration());
    }

    @Test
    void calculate_WeeklyDriver_ZeroRentAllowed() {
        Driver driver = new Driver();
        WeeklyFixedRateRemunerationConfig weeklyConfig = new WeeklyFixedRateRemunerationConfig();
        weeklyConfig.setWeeklyFixedCompanySettlement(new BigDecimal("400.00"));
        driver.initializeWithRemunerationConfigs(List.of(weeklyConfig));

        Shift shift = new Shift();
        shift.setDriver(driver);
        shift.setAppliedRemunerationConfigs(new ArrayList<>(driver.getActiveRemunerationConfigs()));

        ShiftRevenueEntry entry = new ShiftRevenueEntry();
        entry.setEntryCategory(ShiftEntryCategory.REGULAR);
        entry.setRevenue(new BigDecimal("150.00"));
        shift.setRevenues(new ArrayList<>(List.of(entry)));

        ShiftSettlement settlement = calculator.calculate(shift, BigDecimal.ZERO);

        assertNotNull(settlement);
        assertEquals(new BigDecimal("150.00"), settlement.getTotalRevenue());
        assertEquals(BigDecimal.ZERO, shift.getWeeklyDriverRent());
        assertEquals(new BigDecimal("150.00"), settlement.getDriverRemuneration());
        assertEquals(new BigDecimal("0.00"), settlement.getCompanyRemuneration());
    }

    @Test
    void calculate_PercentageDriver_WeeklyRentNotAllowed() {
        Driver driver = new Driver();
        PercentageShareRemunerationConfig config = new PercentageShareRemunerationConfig();
        config.setDriverRevenueSharePercentage(new BigDecimal("0.4500"));
        config.setMinDriverPayoutPerShift(new BigDecimal("50.00"));
        driver.initializeWithRemunerationConfigs(List.of(config));

        Shift shift = new Shift();
        shift.setDriver(driver);
        shift.setAppliedRemunerationConfigs(new ArrayList<>(driver.getActiveRemunerationConfigs()));
        shift.setRevenues(new ArrayList<>());

        assertThrows(BadRequestException.class, () -> calculator.calculate(shift, new BigDecimal("100.00")));
    }

    @Test
    void calculate_PercentageDriver_RegularShareAboveMinPayout() {
        Driver driver = new Driver();
        PercentageShareRemunerationConfig config = new PercentageShareRemunerationConfig();
        config.setDriverRevenueSharePercentage(new BigDecimal("40.00"));
        config.setMinDriverPayoutPerShift(new BigDecimal("30.00"));
        driver.initializeWithRemunerationConfigs(List.of(config));

        Shift shift = new Shift();
        shift.setDriver(driver);
        shift.setAppliedRemunerationConfigs(new ArrayList<>(driver.getActiveRemunerationConfigs()));

        ShiftRevenueEntry entry = new ShiftRevenueEntry();
        entry.setEntryCategory(ShiftEntryCategory.REGULAR);
        entry.setRevenue(new BigDecimal("200.00"));
        shift.setRevenues(new ArrayList<>(List.of(entry)));

        ShiftSettlement settlement = calculator.calculate(shift, null);

        assertNotNull(settlement);
        assertEquals(new BigDecimal("200.00"), settlement.getTotalRevenue());
        assertNull(shift.getWeeklyDriverRent());
        // 40% of 200 = 80.00 > 30.00 min payout
        assertEquals(new BigDecimal("80.00"), settlement.getDriverRemuneration());
        assertEquals(new BigDecimal("120.00"), settlement.getCompanyRemuneration());
    }

    @Test
    void calculate_PercentageDriver_MinPayoutPerShiftAppliedAcrossShift() {
        Driver driver = new Driver();
        PercentageShareRemunerationConfig config = new PercentageShareRemunerationConfig();
        config.setDriverRevenueSharePercentage(new BigDecimal("40.00"));
        config.setMinDriverPayoutPerShift(new BigDecimal("50.00"));
        driver.initializeWithRemunerationConfigs(List.of(config));

        Shift shift = new Shift();
        shift.setDriver(driver);
        shift.setAppliedRemunerationConfigs(new ArrayList<>(driver.getActiveRemunerationConfigs()));

        // Multiple regular entries whose total revenue yields less than min payout
        ShiftRevenueEntry entry1 = new ShiftRevenueEntry();
        entry1.setEntryCategory(ShiftEntryCategory.REGULAR);
        entry1.setRevenue(new BigDecimal("40.00"));

        ShiftRevenueEntry entry2 = new ShiftRevenueEntry();
        entry2.setEntryCategory(ShiftEntryCategory.REGULAR);
        entry2.setRevenue(new BigDecimal("60.00"));

        shift.setRevenues(new ArrayList<>(List.of(entry1, entry2)));

        ShiftSettlement settlement = calculator.calculate(shift, null);

        assertNotNull(settlement);
        assertEquals(new BigDecimal("100.00"), settlement.getTotalRevenue());
        // 40% of 100 = 40.00 < min payout 50.00 -> driver gets 50.00
        assertEquals(new BigDecimal("50.00"), settlement.getDriverRemuneration());
        assertEquals(new BigDecimal("50.00"), settlement.getCompanyRemuneration());
    }

    @Test
    void calculate_PercentageDriver_WithFlatRateEarningsAddedOnTop() {
        Driver driver = new Driver();
        PercentageShareRemunerationConfig percentConfig = new PercentageShareRemunerationConfig();
        percentConfig.setDriverRevenueSharePercentage(new BigDecimal("45.00"));
        percentConfig.setMinDriverPayoutPerShift(new BigDecimal("50.00"));

        FlatRateType flatRateType = new FlatRateType();
        flatRateType.setId(1L);

        FlatRateRemunerationConfig flatConfig = new FlatRateRemunerationConfig();
        flatConfig.setFlatRateType(flatRateType);
        flatConfig.setDriverFlatRatePayoutPerShift(new BigDecimal("15.00"));

        driver.initializeWithRemunerationConfigs(List.of(percentConfig, flatConfig));

        Shift shift = new Shift();
        shift.setDriver(driver);
        shift.setAppliedRemunerationConfigs(new ArrayList<>(driver.getActiveRemunerationConfigs()));

        // Regular entry: 80.00 -> 45% = 36.00 -> min payout triggers = 50.00
        ShiftRevenueEntry regularEntry = new ShiftRevenueEntry();
        regularEntry.setEntryCategory(ShiftEntryCategory.REGULAR);
        regularEntry.setRevenue(new BigDecimal("80.00"));

        // Flat rate entry: 2 trips @ 15.00 flat payout = 30.00 driver earnings; revenue = 70.00
        ShiftRevenueEntry flatEntry = new ShiftRevenueEntry();
        flatEntry.setEntryCategory(ShiftEntryCategory.FLAT_RATE);
        flatEntry.setFlatRateType(flatRateType);
        flatEntry.setTripCount(2L);
        flatEntry.setRevenue(new BigDecimal("70.00"));

        shift.setRevenues(new ArrayList<>(List.of(regularEntry, flatEntry)));

        ShiftSettlement settlement = calculator.calculate(shift, null);

        assertNotNull(settlement);
        // Total revenue = 80 + 70 = 150.00
        assertEquals(new BigDecimal("150.00"), settlement.getTotalRevenue());
        // Driver earnings: 50.00 (min payout regular) + 15.00 (fixed flat rate payout per shift) = 65.00
        assertEquals(new BigDecimal("65.00"), settlement.getDriverRemuneration());
        // Company: 150.00 - 65.00 = 85.00
        assertEquals(new BigDecimal("85.00"), settlement.getCompanyRemuneration());
    }

    @Test
    void calculate_PercentageDriver_WithSpecificFlatRate_UserExample_40Cash_76Flat() {
        Driver driver = new Driver();
        PercentageShareRemunerationConfig percentConfig = new PercentageShareRemunerationConfig();
        percentConfig.setDriverRevenueSharePercentage(new BigDecimal("50.00"));
        percentConfig.setMinDriverPayoutPerShift(new BigDecimal("50.00"));

        FlatRateType flatRateType = new FlatRateType();
        flatRateType.setId(1L);

        FlatRateRemunerationConfig flatConfig = new FlatRateRemunerationConfig();
        flatConfig.setFlatRateType(flatRateType);
        flatConfig.setDriverFlatRatePayoutPerShift(new BigDecimal("100.00"));

        driver.initializeWithRemunerationConfigs(List.of(percentConfig, flatConfig));

        Shift shift = new Shift();
        shift.setDriver(driver);
        shift.setAppliedRemunerationConfigs(new ArrayList<>(driver.getActiveRemunerationConfigs()));

        ShiftRevenueEntry cash = new ShiftRevenueEntry();
        cash.setEntryCategory(ShiftEntryCategory.REGULAR);
        cash.setRevenue(new BigDecimal("40.00")); // 50% = 20 -> min payout triggers 50.00

        ShiftRevenueEntry flat = new ShiftRevenueEntry();
        flat.setEntryCategory(ShiftEntryCategory.FLAT_RATE);
        flat.setFlatRateType(flatRateType);
        flat.setRevenue(new BigDecimal("76.00")); // driver gets 100.00 flat rate payout per shift

        shift.setRevenues(new ArrayList<>(List.of(cash, flat)));

        ShiftSettlement settlement = calculator.calculate(shift, null);

        assertNotNull(settlement);
        // Total revenue = 40 + 76 = 116.00
        assertEquals(new BigDecimal("116.00"), settlement.getTotalRevenue());
        // Driver: 50.00 (min cash payout) + 100.00 (flat rate) = 150.00
        assertEquals(new BigDecimal("150.00"), settlement.getDriverRemuneration());
        // Company: 116.00 - 150.00 = -34.00
        assertEquals(new BigDecimal("-34.00"), settlement.getCompanyRemuneration());
    }

    @Test
    void calculate_FlatRateDriver_CalculatesFixedDriverFlatRatePayoutPerShift() {
        Driver driver = new Driver();
        FlatRateRemunerationConfig flatConfig = new FlatRateRemunerationConfig();
        flatConfig.setDriverFlatRatePayoutPerShift(new BigDecimal("100.00"));
        driver.initializeWithRemunerationConfigs(List.of(flatConfig));

        Shift shift = new Shift();
        shift.setDriver(driver);
        shift.setAppliedRemunerationConfigs(new ArrayList<>(driver.getActiveRemunerationConfigs()));

        ShiftRevenueEntry flat = new ShiftRevenueEntry();
        flat.setEntryCategory(ShiftEntryCategory.FLAT_RATE);
        flat.setTripCount(10L);
        flat.setRevenue(new BigDecimal("38.00"));

        shift.setRevenues(new ArrayList<>(List.of(flat)));

        ShiftSettlement settlement = calculator.calculate(shift, null);

        assertNotNull(settlement);
        assertEquals(new BigDecimal("38.00"), settlement.getTotalRevenue());
        // Driver gets fixed 100.00 per shift regardless of trips or revenue
        assertEquals(new BigDecimal("100.00"), settlement.getDriverRemuneration());
        // Company gets 38.00 - 100.00 = -62.00
        assertEquals(new BigDecimal("-62.00"), settlement.getCompanyRemuneration());
    }
}
