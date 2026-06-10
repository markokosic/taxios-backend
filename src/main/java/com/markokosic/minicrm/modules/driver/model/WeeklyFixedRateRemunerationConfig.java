package com.markokosic.minicrm.modules.driver.model;

import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import com.markokosic.minicrm.modules.remuneration.WeeklyFixedRateRemunerationCalculator;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@DiscriminatorValue("WEEKLY_FIXED_RATE")
public class WeeklyFixedRateRemunerationConfig extends DriverRemunerationConfig {

	@Column(name="weekly_company_settlement", precision = 10, scale = 2)
	private BigDecimal weeklyFixedCompanySettlement;

	@Min(1)
	@Max(7)
	@Column(name = "settlement_day")
	private Integer settlementDay;

	@Override
	public RemunerationModelType getType() {
		return RemunerationModelType.WEEKLY_FIXED_RATE;
	}

	@Override
	public RemunerationSplit calculateRemuneration(BigDecimal revenue) {
		return new WeeklyFixedRateRemunerationCalculator().calculateRemuneration(revenue, this);
	}


}


