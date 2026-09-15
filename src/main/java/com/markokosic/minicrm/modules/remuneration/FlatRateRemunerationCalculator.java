package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.FlatRateRemunerationConfig;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public non-sealed class FlatRateRemunerationCalculator implements IRemunerationCalculator {

	@Override
	public RemunerationModelType getSupportedModelType() {
		return RemunerationModelType.FLAT_RATE;
	}

	@Override
	public RemunerationSplit calculateRemuneration(BigDecimal revenue, DriverRemunerationConfig config) {
		FlatRateRemunerationConfig fc = (FlatRateRemunerationConfig) org.hibernate.Hibernate.unproxy(config);
		BigDecimal driverShare = fc.getDriverFlatRatePayoutPerShift();
		BigDecimal companyShare = revenue.subtract(driverShare);
		return new RemunerationSplit(companyShare, driverShare);
	}
}
