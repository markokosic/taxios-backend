package com.markokosic.minicrm.modules.tenant;

import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverStatus;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MultiTenancyIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DriverRepository driverRepository;

    private Tenant tenantAlpha;
    private Tenant tenantBeta;

    @BeforeEach
    void setUp() {
        TenantContextHolder.clear();

        tenantAlpha = new Tenant();
        tenantAlpha.setName("Tenant Alpha");
        tenantAlpha = tenantRepository.save(tenantAlpha);

        tenantBeta = new Tenant();
        tenantBeta.setName("Tenant Beta");
        tenantBeta = tenantRepository.save(tenantBeta);
    }

    @AfterEach
    void tearDown() {
        if (tenantAlpha != null && tenantAlpha.getId() != null) {
            TenantContextHolder.setTenantId(tenantAlpha.getId());
            driverRepository.deleteAll();
        }
        if (tenantBeta != null && tenantBeta.getId() != null) {
            TenantContextHolder.setTenantId(tenantBeta.getId());
            driverRepository.deleteAll();
        }
        TenantContextHolder.clear();
        if (tenantAlpha != null && tenantAlpha.getId() != null) {
            tenantRepository.deleteById(tenantAlpha.getId());
        }
        if (tenantBeta != null && tenantBeta.getId() != null) {
            tenantRepository.deleteById(tenantBeta.getId());
        }
    }

    @Test
    void testTenantDataIsolation() {
        // 1. Create a driver under Tenant Alpha
        TenantContextHolder.setTenantId(tenantAlpha.getId());
        Driver driverAlpha = new Driver();
        driverAlpha.setFirstName("Alpha");
        driverAlpha.setLastName("Driver");
        driverAlpha.setEmail("alpha@test.com");
        driverAlpha.setPhone("+43660111111");
        driverAlpha.setStatus(DriverStatus.ACTIVE);
        driverAlpha = driverRepository.save(driverAlpha);

        // Verify that Driver has Tenant Alpha ID populated
        assertEquals(tenantAlpha.getId(), driverAlpha.getTenantId());

        // Query driver under Tenant Alpha -> should find 1 driver
        List<Driver> alphaDrivers = driverRepository.findAll();
        assertEquals(1, alphaDrivers.size());
        assertEquals("Alpha", alphaDrivers.get(0).getFirstName());

        // 2. Switch to Tenant Beta
        TenantContextHolder.setTenantId(tenantBeta.getId());

        // Query drivers under Tenant Beta -> should be empty
        List<Driver> betaDriversBefore = driverRepository.findAll();
        assertTrue(betaDriversBefore.isEmpty());

        // Create a driver under Tenant Beta
        Driver driverBeta = new Driver();
        driverBeta.setFirstName("Beta");
        driverBeta.setLastName("Driver");
        driverBeta.setEmail("beta@test.com");
        driverBeta.setPhone("+43660222222");
        driverBeta.setStatus(DriverStatus.ACTIVE);
        driverBeta = driverRepository.save(driverBeta);

        assertEquals(tenantBeta.getId(), driverBeta.getTenantId());

        // Query driver under Tenant Beta -> should find 1 driver (Beta) and not Alpha
        List<Driver> betaDriversAfter = driverRepository.findAll();
        assertEquals(1, betaDriversAfter.size());
        assertEquals("Beta", betaDriversAfter.get(0).getFirstName());

        // Try to find the Alpha driver by ID under Tenant Beta -> should return empty
        Optional<Driver> findAlphaFromBeta = driverRepository.findById(driverAlpha.getId());
        assertTrue(findAlphaFromBeta.isEmpty());

        // 3. Switch back to Tenant Alpha
        TenantContextHolder.setTenantId(tenantAlpha.getId());

        // Query driver under Tenant Alpha -> should still find only Alpha, not Beta
        List<Driver> alphaDriversFinal = driverRepository.findAll();
        assertEquals(1, alphaDriversFinal.size());
        assertEquals("Alpha", alphaDriversFinal.get(0).getFirstName());

        // Try to find the Beta driver by ID under Tenant Alpha -> should return empty
        Optional<Driver> findBetaFromAlpha = driverRepository.findById(driverBeta.getId());
        assertTrue(findBetaFromAlpha.isEmpty());
    }
}
