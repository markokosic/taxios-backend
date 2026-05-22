package com.markokosic.minicrm.modules.revenue;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import com.markokosic.minicrm.modules.tenant.Tenant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="daily_revenue")
public class DailyRevenue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tenant_id", nullable = false)
	@NotNull
	private Long tenantId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "driver_id", nullable = false)
	private Driver driver;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "car_id", nullable = false)
	private com.markokosic.minicrm.modules.car.model.Car car;

	@Column(name="date", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate date;

	@Column(name="kilometers_driven", nullable = false)
	private BigDecimal kilometersDriven;

	@Column(name="revenue", nullable = false)
	private BigDecimal revenue;

	@Column(name="companyRemuneration", nullable = false)
	private BigDecimal companyRemuneration;

	@Column(name="driverRemuneration", nullable = false)
	private BigDecimal driverRemuneration;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "remuneration_version_id", nullable = false)
	private DriverRemunerationConfig remunerationConfig;

}
