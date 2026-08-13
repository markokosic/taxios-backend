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

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "default_price", precision = 19, scale = 2)
	private BigDecimal defaultPrice;

	@Column(name = "active", nullable = false)
	private boolean active = true;
}
