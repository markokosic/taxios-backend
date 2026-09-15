package com.markokosic.minicrm.modules.shift;

import com.markokosic.minicrm.modules.car.CarRepository;
import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.car.model.CarStatus;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverStatus;
import com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig;
import com.markokosic.minicrm.modules.driver.repository.DriverRepository;
import com.markokosic.minicrm.modules.shift.model.Shift;
import com.markokosic.minicrm.modules.shift.model.ShiftEntryCategory;
import com.markokosic.minicrm.modules.shift.model.ShiftRevenueEntry;
import com.markokosic.minicrm.modules.shift.model.ShiftStatus;
import com.markokosic.minicrm.modules.shift.repository.ShiftRepository;
import com.markokosic.minicrm.modules.shift.service.ShiftService;
import com.markokosic.minicrm.modules.tenant.Tenant;
import com.markokosic.minicrm.modules.tenant.TenantContextHolder;
import com.markokosic.minicrm.modules.tenant.TenantRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ShiftIntegrationTest {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant testTenant;
    private Driver driver;
    private Car car;

    @BeforeEach
    void setUp() {
        TenantContextHolder.clear();
        testTenant = new Tenant();
        testTenant.setName("Shift Test Tenant");
        testTenant = tenantRepository.save(testTenant);
        TenantContextHolder.setTenantId(testTenant.getId());

        driver = new Driver();
        driver.setFirstName("John");
        driver.setLastName("Doe");
        driver.setEmail("john.shift.test@example.com");
        driver.setPhone("+436601234567");
        driver.setStatus(DriverStatus.ACTIVE);

        PercentageShareRemunerationConfig config = new PercentageShareRemunerationConfig();
        config.setDriverRevenueSharePercentage(new BigDecimal("0.4500"));
        config.setMinDriverPayoutPerShift(new BigDecimal("50.00"));
        driver.initializeWithRemunerationConfigs(List.of(config));

        driver = driverRepository.save(driver);

        car = new Car();
        car.setLicensePlate("W-12345S");
        car.setBrand("Toyota");
        car.setModel("Prius");
        car.setHorsepower("122");
        car.setStatus(CarStatus.ACTIVE);
        car = carRepository.save(car);
    }

    @AfterEach
    void tearDown() {
        if (testTenant != null && testTenant.getId() != null) {
            TenantContextHolder.setTenantId(testTenant.getId());
            shiftRepository.deleteAll();
            driverRepository.deleteAll();
            carRepository.deleteAll();
            TenantContextHolder.clear();
            tenantRepository.deleteById(testTenant.getId());
        }
    }

    @Test
    void testFindAllFiltered_ExecutesCleanlyWithJoinedInheritance() {
        Shift shift = new Shift();
        shift.setDriver(driver);
        shift.setCar(car);
        shift.setOdometerStart(new BigDecimal("1000.00"));
        shift.setOdometerEnd(new BigDecimal("1200.00"));
        shift.setShiftStart(LocalDateTime.now().minusHours(8));
        shift.setShiftEnd(LocalDateTime.now());
        shift.setStatus(ShiftStatus.APPROVED);

        ShiftRevenueEntry entry = new ShiftRevenueEntry();
        entry.setEntryCategory(ShiftEntryCategory.REGULAR);
        entry.setRevenue(new BigDecimal("200.00"));
        shift.addRevenueEntry(entry);

        shift = shiftRepository.save(shift);
        assertNotNull(shift.getId());

        // Execute findAllFiltered
        var page = shiftRepository.findAllFiltered(null, null, null, null, PageRequest.of(0, 10));
        assertNotNull(page);
        assertEquals(1, page.getTotalElements());

        Shift fetchedShift = page.getContent().get(0);
        assertEquals(driver.getId(), fetchedShift.getDriver().getId());
        assertEquals(car.getId(), fetchedShift.getCar().getId());
        assertEquals(1, fetchedShift.getRevenues().size());
        assertEquals(new BigDecimal("200.00"), fetchedShift.getRevenues().get(0).getRevenue());

        // Test shiftService.getAllShifts
        var serviceResult = shiftService.getAllShifts(null, null, null, null, PageRequest.of(0, 10));
        assertNotNull(serviceResult);
        assertEquals(1, serviceResult.getTotalElements());
        assertEquals(1, serviceResult.getContent().size());
        assertEquals(new BigDecimal("200.00"), serviceResult.getContent().get(0).revenues().get(0).revenue());
    }
}
