package com.markokosic.minicrm.modules.driver.model;

import com.markokosic.minicrm.modules.driver.dto.request.CreateFlatRateRemunerationConfigDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateRemunerationRequestDTO;
import com.markokosic.minicrm.modules.remuneration.FlatRateRemunerationCalculator;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@DiscriminatorValue("FLAT_RATE")
public class FlatRateRemunerationConfig extends DriverRemunerationConfig {

	@DecimalMin(value = "0.0", message = "{driver.minDriverPayout.negative}")
	@Column(name="flat_rate_fee", precision = 19, scale = 2)
	private BigDecimal flatRateFee;

	@Override
	public RemunerationModelType getType() {
		return RemunerationModelType.FLAT_RATE;
	}

	@Override
	public boolean isIdenticalTo(CreateRemunerationRequestDTO dto) {
		if (!(dto instanceof CreateFlatRateRemunerationConfigDTO fDto)) {
			return false;
		}
		return areEqual(this.flatRateFee, fDto.flatRateFee());
	}


	@Override
	public RemunerationSplit calculateRemuneration(BigDecimal revenue) {
		return new FlatRateRemunerationCalculator().calculateRemuneration(revenue, this);
	}

}
