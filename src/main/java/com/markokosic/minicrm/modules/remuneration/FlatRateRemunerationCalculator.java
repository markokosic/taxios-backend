package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig;
import java.math.BigDecimal;

public non-sealed class FlatRateRemunerationCalculator implements IRemunerationCalculator {

	@Override
	public RemunerationSplit calculateRemuneration(BigDecimal revenue, DriverRemunerationConfig config) {
		FlatRateRemunerationConfig fc = (FlatRateRemunerationConfig) config;
		BigDecimal driverShare = fc.getFlatRateFee();
		BigDecimal companyShare = revenue.subtract(driverShare);
		return new RemunerationSplit(companyShare, driverShare);
	}

}
