package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.exception.BadRequestException;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.driver.model.PercentageShareRemunerationConfig;
import com.markokosic.minicrm.modules.shift.model.Shift;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public non-sealed class PercentageRemunerationCalculator implements IRemunerationCalculator {

	@Override
	public RemunerationModelType getSupportedModelType() {
		return RemunerationModelType.PERCENTAGE_SHARE;
	}

	@Override
	public RemunerationSplit applyShiftAdjustments(Shift shift, DriverRemunerationConfig config, RemunerationSplit currentSplit) {
		if (shift.getWeeklyDriverRent() != null && shift.getWeeklyDriverRent().compareTo(BigDecimal.ZERO) >= 0) {
			throw new BadRequestException("domain.shift.weekly_driver_rent_not_allowed");
		}
		return currentSplit;
	}

	@Override
	public RemunerationSplit calculateRemuneration(BigDecimal revenue, DriverRemunerationConfig config) {
		PercentageShareRemunerationConfig pc = (PercentageShareRemunerationConfig) org.hibernate.Hibernate.unproxy(config);

		BigDecimal factor = pc.getDriverRevenueSharePercentage();
		if (factor != null && factor.compareTo(BigDecimal.ONE) > 0) {
			factor = factor.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
		}

		BigDecimal driverShare = revenue
				.multiply(factor != null ? factor : BigDecimal.ZERO)
				.setScale(2, RoundingMode.HALF_UP);

		BigDecimal finalDriverShare = pc.getMinDriverPayoutPerShift() != null
				? driverShare.max(pc.getMinDriverPayoutPerShift())
				: driverShare;

		BigDecimal companyShare = revenue.subtract(finalDriverShare);

		return new RemunerationSplit(companyShare, finalDriverShare);
	}
}
