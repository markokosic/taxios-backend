package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;

import java.math.BigDecimal;

public non-sealed class WeeklyFixedRateRemunerationCalculator  implements IRemunerationCalculator {

	@Override
	public RemunerationSplit calculateRemuneration(BigDecimal revenue, DriverRemunerationConfig config) {

		return new RemunerationSplit(BigDecimal.ZERO, revenue);
	}
}


