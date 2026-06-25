package com.markokosic.minicrm.modules.remuneration;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;

import java.math.BigDecimal;

public sealed interface IRemunerationCalculator permits WeeklyFixedRateRemunerationCalculator, PercentageRemunerationCalculator, FlatRateRemunerationCalculator {
	public RemunerationSplit calculateRemuneration(BigDecimal revenue, DriverRemunerationConfig config);
}
