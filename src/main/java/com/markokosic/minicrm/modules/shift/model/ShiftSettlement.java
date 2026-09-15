package com.markokosic.minicrm.modules.shift.model;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "shift_settlements")
public class ShiftSettlement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@TenantId
	@Column(name = "tenant_id", nullable = false, updatable = false)
	private Long tenantId;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shift_id", nullable = false, unique = true)
	private Shift shift;

	@Column(name = "total_revenue", nullable = false, precision = 19, scale = 2)
	private BigDecimal totalRevenue;

	public BigDecimal getRevenue() {
		return totalRevenue;
	}

	@Column(name = "driver_remuneration", nullable = false, precision = 19, scale = 2)
	private BigDecimal driverRemuneration;

	@Column(name = "company_remuneration", nullable = false, precision = 19, scale = 2)
	private BigDecimal companyRemuneration;

	@Column(name = "settled_at", nullable = false)
	private LocalDateTime settledAt;
}
