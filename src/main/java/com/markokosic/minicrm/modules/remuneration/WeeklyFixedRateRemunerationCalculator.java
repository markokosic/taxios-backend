package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.exception.BadRequestException;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.shift.model.Shift;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public non-sealed class WeeklyFixedRateRemunerationCalculator implements IRemunerationCalculator {

	@Override
	public RemunerationModelType getSupportedModelType() {
		return RemunerationModelType.WEEKLY_FIXED_RATE;
	}

	@Override
	public RemunerationSplit applyShiftAdjustments(Shift shift, DriverRemunerationConfig config, RemunerationSplit currentSplit) {
		BigDecimal weeklyRent = shift.getWeeklyDriverRent();
		if (weeklyRent == null) {
			throw new BadRequestException("domain.shift.weekly_driver_rent_required");
		}
		if (weeklyRent.compareTo(BigDecimal.ZERO) < 0) {
			throw new BadRequestException("domain.shift.weekly_driver_rent_negative");
		}

		return new RemunerationSplit(
				currentSplit.companyRemuneration().add(weeklyRent).setScale(2, java.math.RoundingMode.HALF_UP),
				currentSplit.driverRemuneration().subtract(weeklyRent).setScale(2, java.math.RoundingMode.HALF_UP)
		);
	}

	@Override
	public RemunerationSplit calculateRemuneration(BigDecimal revenue, DriverRemunerationConfig config) {
		// Base calculation: driver keeps all revenue until weekly rent is applied
		return new RemunerationSplit(BigDecimal.ZERO, revenue);
	}
}
