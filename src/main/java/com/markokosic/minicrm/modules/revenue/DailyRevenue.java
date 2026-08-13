package com.markokosic.minicrm.modules.revenue;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.driver.model.Driver;
import com.markokosic.minicrm.modules.driver.model.DriverRemunerationConfig;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="daily_revenue")
public class DailyRevenue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@TenantId
	@Column(name = "tenant_id", nullable = false, updatable = false)
	private Long tenantId;

	@Column(name = "trip_count")
	private Long tripCount;

	@Column(name = "price_per_trip",  precision = 19, scale = 2)
	private BigDecimal pricePerTrip;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "driver_id", nullable = false)
	private Driver driver;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "car_id", nullable = false)
	private Car car;

	@Column(name="date", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate date;

	@Column(name="kilometers_driven", precision = 10, scale = 3)
	private BigDecimal kilometersDriven;

	@Column(name="kilometers_from", nullable = false, precision = 10, scale = 3)
	private BigDecimal kilometersFrom;

	@Column(name="kilometers_to", nullable = false, precision = 10, scale = 3)
	private BigDecimal kilometersTo;

	@Column(name="revenue", nullable = false,  precision = 19, scale = 2)
	private BigDecimal revenue;

	@Column(name="companyRemuneration", nullable = false,  precision = 19, scale = 2)
	private BigDecimal companyRemuneration;

	@Column(name="driverRemuneration", nullable = false,  precision = 19, scale = 2)
	private BigDecimal driverRemuneration;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "remuneration_version_id", nullable = false)
	private DriverRemunerationConfig remunerationConfig;

	@Column(name = "driven_from")
	private LocalTime drivingStartTime;

	@Column(name = "driven_to")
	private LocalTime drivingEndTime;

}
