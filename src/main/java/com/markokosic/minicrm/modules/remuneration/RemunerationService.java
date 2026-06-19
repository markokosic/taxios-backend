package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.WeeklyFixedRateRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig;
import com.markokosic.minicrm.modules.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RemunerationService {


	public RemunerationSplit calculateRemunerationSplitFromDailyRevenue(BigDecimal dailyRevenue, DriverRemunerationConfig config) {

		return config.calculateRemuneration(dailyRevenue);

	}

}
