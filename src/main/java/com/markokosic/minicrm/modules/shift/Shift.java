package com.markokosic.minicrm.modules.shift;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.markokosic.minicrm.modules.car.model.Car;
import com.markokosic.minicrm.modules.driver.model.Driver;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "shifts")
public class Shift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@TenantId
	@Column(name = "tenant_id", nullable = false, updatable = false)
	private Long tenantId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "driver_id", nullable = false)
	private Driver driver;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "car_id", nullable = false)
	private Car car;

	@Column(name = "odometer_start", nullable = false, precision = 10, scale = 2)
	private BigDecimal odometerStart;

	@Column(name = "odometer_end", nullable = false, precision = 10, scale = 2)
	private BigDecimal odometerEnd;

	@Column(name = "shift_start", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime shiftStart;

	@Column(name = "shift_end", nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime shiftEnd;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private ShiftStatus status = ShiftStatus.APPROVED;

	@OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ShiftRevenueEntry> revenues = new ArrayList<>();

	public void addRevenueEntry(ShiftRevenueEntry entry) {
		revenues.add(entry);
		entry.setShift(this);
	}

	public void removeRevenueEntry(ShiftRevenueEntry entry) {
		revenues.remove(entry);
		entry.setShift(null);
	}

	public Duration getShiftDuration() {
		if (shiftStart == null || shiftEnd == null) return Duration.ZERO;
		return Duration.between(shiftStart, shiftEnd);
	}

	public BigDecimal getKilometersDriven() {
		if (odometerStart == null || odometerEnd == null) return BigDecimal.ZERO;
		return odometerEnd.subtract(odometerStart);
	}
}
