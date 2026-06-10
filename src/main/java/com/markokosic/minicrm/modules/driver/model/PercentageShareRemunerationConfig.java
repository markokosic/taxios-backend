package com.markokosic.minicrm.modules.driver.model;

import com.markokosic.minicrm.modules.remuneration.PercentageRemunerationCalculator;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@DiscriminatorValue("PERCENTAGE_SHARE")
public class PercentageShareRemunerationConfig extends DriverRemunerationConfig {

	@DecimalMin(value = "0.0", inclusive = false, message = "{driver.driverRevenueSharePercentage.invalid}")
	@DecimalMax(value = "100.0", message = "{driver.driverRevenueSharePercentage.invalid}")
	@Column(name="driver_revenue_share_percentage", precision = 19, scale = 2)
	private BigDecimal driverRevenueSharePercentage;

	@DecimalMin(value = "0.0", message = "{driver.minDriverPayout.negative}")
	@Column(name="driver_min_payout", precision = 19, scale = 2)
	private BigDecimal minDriverPayout;

	@Override
	public RemunerationModelType getType() {
		return RemunerationModelType.PERCENTAGE_SHARE;
	}

	@Override
	public RemunerationSplit calculateRemuneration(BigDecimal revenue) {
		return new PercentageRemunerationCalculator().calculateRemuneration(revenue, this);
	}


}
