package com.markokosic.minicrm.modules.driver.model;

import com.markokosic.minicrm.modules.driver.dto.request.CreateRemunerationRequestDTO;
import com.markokosic.minicrm.modules.remuneration.RemunerationModelType;
import com.markokosic.minicrm.modules.remuneration.RemunerationSplit;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name="driver_remuneration_configs")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "config_type", discriminatorType = DiscriminatorType.STRING)
public abstract class DriverRemunerationConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "driver_id", nullable = false)
	private Driver driver;

	@TenantId
	@Column(name = "tenant_id", nullable = false, updatable = false)
	private Long tenantId;

	@Column(name = "is_current_remuneration", nullable = false)
	private boolean current;

	@Column(name = "valid_from", nullable = false)
	private LocalDate validFrom;

	@Column(name = "valid_until")
	private LocalDate validUntil;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flat_rate_type_id")
	private FlatRateType flatRateType;

	public abstract RemunerationModelType getType();

	public abstract boolean isIdenticalTo(CreateRemunerationRequestDTO dto);

	public abstract RemunerationSplit calculateRemuneration(BigDecimal revenue);

	protected boolean areEqual(BigDecimal a, BigDecimal b) {
		if (a == null && b == null) return true;
		if (a == null || b == null) return false;
		return a.compareTo(b) == 0;
	}

	public void activate(LocalDate from) {
		this.current = true;
		this.validFrom = from;
		this.validUntil = null;
	}

	public void deactivate(LocalDate until) {
		this.current = false;
		this.validUntil = until;
	}
}
