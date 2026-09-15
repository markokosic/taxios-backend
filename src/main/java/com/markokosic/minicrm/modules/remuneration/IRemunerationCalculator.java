package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.shift.model.Shift;

import java.math.BigDecimal;

public sealed interface IRemunerationCalculator permits WeeklyFixedRateRemunerationCalculator, PercentageRemunerationCalculator, FlatRateRemunerationCalculator {

	RemunerationModelType getSupportedModelType();

	RemunerationSplit calculateRemuneration(BigDecimal revenue, DriverRemunerationConfig config);

	default RemunerationSplit applyShiftAdjustments(Shift shift, DriverRemunerationConfig config, RemunerationSplit currentSplit) {
		return currentSplit;
	}
}
