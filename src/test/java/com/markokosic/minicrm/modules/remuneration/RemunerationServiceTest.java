package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RemunerationServiceTest {

    @Mock
    private DriverRemunerationConfig config;

    @InjectMocks
    private RemunerationService remunerationService;

    @Test
    void testCalculateRemunerationSplit_shouldDelegateToConfig() {
        // Arrange
        BigDecimal revenue = new BigDecimal("120.00");
        RemunerationSplit expectedSplit = new RemunerationSplit(
                new BigDecimal("90.00"), new BigDecimal("30.00")
        );

        when(config.calculateRemuneration(revenue)).thenReturn(expectedSplit);

        // Act
        RemunerationSplit result = remunerationService.calculateRemunerationSplit(revenue, config);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("90.00"), result.companyRemuneration());
        assertEquals(new BigDecimal("30.00"), result.driverRemuneration());
        verify(config, times(1)).calculateRemuneration(revenue);
    }
}
