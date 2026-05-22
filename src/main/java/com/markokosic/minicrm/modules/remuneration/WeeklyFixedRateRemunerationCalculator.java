package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.WeeklyFixedRateRemunerationConfig;

import java.math.BigDecimal;

public non-sealed class WeeklyFixedRateRemunerationCalculator  implements IRemunerationCalculator {

	@Override
	public RemunerationSplit calculateRemuneration(BigDecimal revenue, DriverRemunerationConfig config) {
//		WeeklyFixedRateRemunerationConfig wc = (WeeklyFixedRateRemunerationConfig) config;
//		BigDecimal companyShare = wc.getWeeklyFixedCompanySettlement();
//		BigDecimal finalDriverShare = revenue;

		return new RemunerationSplit(BigDecimal.ZERO, revenue);
	}
}


