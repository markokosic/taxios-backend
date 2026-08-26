package com.markokosic.minicrm.modules.shift.model;

import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.flatratetype.model.FlatRateType;
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
@Table(name = "shift_revenue_entries")
public class ShiftRevenueEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@TenantId
	@Column(name = "tenant_id", nullable = false, updatable = false)
	private Long tenantId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shift_id", nullable = false)
	private Shift shift;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "remuneration_version_id", nullable = false)
	private DriverRemunerationConfig remunerationConfig;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flat_rate_type_id")
	private FlatRateType flatRateType;

	@Enumerated(EnumType.STRING)
	@Column(name = "entry_category", nullable = false)
	private ShiftEntryCategory entryCategory = ShiftEntryCategory.REGULAR;

	@Column(name = "revenue", nullable = false, precision = 19, scale = 2)
	private BigDecimal revenue;

	@Column(name = "company_remuneration", nullable = false, precision = 19, scale = 2)
	private BigDecimal companyRemuneration;

	@Column(name = "driver_remuneration", nullable = false, precision = 19, scale = 2)
	private BigDecimal driverRemuneration;

	@Column(name = "trip_count")
	private Long tripCount;

	@Column(name = "price_per_trip", precision = 19, scale = 2)
	private BigDecimal pricePerTrip;
}
