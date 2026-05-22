package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;

public non-sealed class PercentageRemunerationCalculator implements IRemunerationCalculator {

	@Override
	public RemunerationSplit calculateRemuneration(BigDecimal revenue, DriverRemunerationConfig config) {
		PercentageShareRemunerationConfig pc = (PercentageShareRemunerationConfig) config;

		//  (Revenue * Percent) / 100
		BigDecimal driverShare = revenue
				.multiply(pc.getDriverRevenueSharePercentage())
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

		// driver will always receive a payment even on bad days
		BigDecimal finalDriverShare = driverShare.max(pc.getMinDriverPayout());

		BigDecimal companyShare = revenue.subtract(finalDriverShare);

		return new RemunerationSplit(companyShare, finalDriverShare);
	}
}
