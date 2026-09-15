package com.markokosic.minicrm.modules.driver.model;

import com.markokosic.minicrm.modules.driver.dto.request.CreatePercentageShareRemunerationConfigDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateRemunerationRequestDTO;
import com.markokosic.minicrm.modules.remuneration.PercentageRemunerationCalculator;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "remuneration_percentage_configs")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("PERCENTAGE_SHARE")
public class PercentageShareRemunerationConfig extends DriverRemunerationConfig {

	@DecimalMin(value = "0.0", inclusive = true, message = "{driver.driverRevenueSharePercentage.invalid}")
	@DecimalMax(value = "1.0", message = "{driver.driverRevenueSharePercentage.invalid}")
	@Column(name="driver_revenue_share_percentage", nullable = false, precision = 5, scale = 4)
	private BigDecimal driverRevenueSharePercentage;

	@DecimalMin(value = "0.0", message = "{driver.minDriverPayout.negative}")
	@Column(name="min_driver_payout_per_shift", precision = 19, scale = 2)
	private BigDecimal minDriverPayoutPerShift;

	@Override
	public RemunerationModelType getType() {
		return RemunerationModelType.PERCENTAGE_SHARE;
	}

	@Override
	public boolean isIdenticalTo(DriverRemunerationConfig other) {
		if (!(other instanceof PercentageShareRemunerationConfig pOther)) {
			return false;
		}
		
		return areEqual(this.driverRevenueSharePercentage, pOther.getDriverRevenueSharePercentage())
				&& areEqual(this.minDriverPayoutPerShift, pOther.getMinDriverPayoutPerShift());
	}


	@Override
	public RemunerationSplit calculateRemuneration(BigDecimal revenue) {
		return new PercentageRemunerationCalculator().calculateRemuneration(revenue, this);
	}


}
