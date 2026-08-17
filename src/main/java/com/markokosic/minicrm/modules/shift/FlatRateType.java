package com.markokosic.minicrm.modules.shift;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "flat_rate_types")
public class FlatRateType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@TenantId
	@Column(name = "tenant_id", nullable = false, updatable = false)
	private Long tenantId;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private FlatRateTypeStatus status = FlatRateTypeStatus.ACTIVE;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "default_price", precision = 19, scale = 2)
	private BigDecimal defaultPrice;

	@Column(name = "flat_rate_code", nullable = false)
	private String flatRateCode;

	@Column(name = "is_current", nullable = false)
	private boolean current = true;

	@Column(name = "valid_from", nullable = false)
	private java.time.LocalDate validFrom;

	@Column(name = "valid_until")
	private java.time.LocalDate validUntil;

	public void activate(java.time.LocalDate from) {
		this.current = true;
		this.validFrom = from;
		this.validUntil = null;
	}

	public void deactivate(java.time.LocalDate until) {
		this.current = false;
		this.validUntil = until;
	}
}
