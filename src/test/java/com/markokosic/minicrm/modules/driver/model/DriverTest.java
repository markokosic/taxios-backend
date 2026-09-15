package com.markokosic.minicrm.modules.driver.model;

import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DriverTest {


    @Test
    void findFlatRateConfig_matchesByIdOrCode() {
        Driver driver = new Driver();

        FlatRateType airport = new FlatRateType();
        airport.setId(10L);
        airport.setFlatRateCode("AIRPORT");

        FlatRateRemunerationConfig config = new FlatRateRemunerationConfig();
        config.setFlatRateType(airport);
        config.setDriverFlatRatePayoutPerShift(new BigDecimal("30.00"));
        config.activate(LocalDate.now());

        driver.setRemunerationConfigs(List.of(config));

        // Search by ID
        FlatRateType queryById = new FlatRateType();
        queryById.setId(10L);
        Optional<FlatRateRemunerationConfig> foundById = driver.findFlatRateConfig(queryById);
        assertTrue(foundById.isPresent());
        assertEquals(new BigDecimal("30.00"), foundById.get().getDriverFlatRatePayoutPerShift());

        // Search by code
        FlatRateType queryByCode = new FlatRateType();
        queryByCode.setFlatRateCode("airport");
        Optional<FlatRateRemunerationConfig> foundByCode = driver.findFlatRateConfig(queryByCode);
        assertTrue(foundByCode.isPresent());

        // Null search
        assertTrue(driver.findFlatRateConfig(null).isEmpty());

        // Not matching
        FlatRateType notMatching = new FlatRateType();
        notMatching.setId(999L);
        assertTrue(driver.findFlatRateConfig(notMatching).isEmpty());
    }
}
