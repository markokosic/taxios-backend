package com.markokosic.minicrm.modules.driver.model;

import com.markokosic.minicrm.modules.driver.dto.request.CreateFlatRateRemunerationConfigDTO;
import com.markokosic.minicrm.modules.driver.dto.request.CreateRemunerationRequestDTO;
import com.markokosic.minicrm.modules.remuneration.FlatRateRemunerationCalculator;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "remuneration_flat_rate_configs")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("FLAT_RATE")
public class FlatRateRemunerationConfig extends DriverRemunerationConfig {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flat_rate_type_id")
	private FlatRateType flatRateType;

	@DecimalMin(value = "0.0", message = "{driver.minDriverPayout.negative}")
	@Column(name="driver_flat_rate_payout_per_shift", precision = 19, scale = 2)
	private BigDecimal driverFlatRatePayoutPerShift;

	@Override
	public RemunerationModelType getType() {
		return RemunerationModelType.FLAT_RATE;
	}

	@Override
	public boolean isIdenticalTo(DriverRemunerationConfig other) {
		if (!(other instanceof FlatRateRemunerationConfig fOther)) {
			return false;
		}
		Long currentTypeId = getFlatRateType() != null ? getFlatRateType().getId() : null;
		Long otherTypeId = fOther.getFlatRateType() != null ? fOther.getFlatRateType().getId() : null;
		return areEqual(this.driverFlatRatePayoutPerShift, fOther.driverFlatRatePayoutPerShift) && Objects.equals(currentTypeId, otherTypeId);
	}

	@Override
	public RemunerationSplit calculateRemuneration(BigDecimal revenue) {
		return new FlatRateRemunerationCalculator().calculateRemuneration(revenue, this);
	}
}
